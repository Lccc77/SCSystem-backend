package com.scsystem.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsystem.dto.Result;
import com.scsystem.entity.Shop;
import com.scsystem.mapper.ShopMapper;
import com.scsystem.service.IShopService;
import com.scsystem.utils.CacheClient;
import com.scsystem.utils.SystemConstants;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.scsystem.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 解决缓存穿透
        Shop shop = cacheClient
                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        // 7.返回
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 1.判断是否需要根据坐标查询
        if (x == null || y == null) {
            // 不需要坐标查询，按数据库查询，根据类型分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }
        // 2.计算分页参数
        int begin = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

        // 3.查询redis、按照距离排序、分页。结果：shopId、distance
        String key = SHOP_GEO_KEY + typeId;
        Circle circle = new Circle(new Point(x,y), new Distance(10000, Metrics.METERS));
        // 设置搜索要求
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                //设置搜索个数（由近及远且不跳过）
                .limit(end)
                //包含成员的坐标信息
                .includeCoordinates()
                //包含成员距离中心点的距离
                .includeDistance();
        //返回符合信息的数据
        GeoResults<RedisGeoCommands.GeoLocation<String>> search = stringRedisTemplate.opsForGeo().radius(key, circle, args);
        //获取Content里面的集合
        assert search != null;
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = search.getContent();

//        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
//                key,
//                GeoReference.fromCoordinate(x, y),
//                new Distance(5000),
//                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
//        );
//        if (results == null) {
//            return Result.ok(Collections.emptyList());
//        }
//        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();

        // 4.解析出id
        List<Long> ids = new ArrayList<>(end - begin + 1);
        Map<String, Distance> map = new HashMap<>(end - begin + 1);
        // 4.1.截取 from ~ end的部分
        list.stream().skip(begin).forEach(
                result -> {
                    // 4.2.获取店铺id
                    String shopName = result.getContent().getName();
                    ids.add(Long.valueOf(shopName));
                    // 4.3.获取距离
                    map.put(shopName, result.getDistance());
                }
        );
        // 没有下一页了，结束
        if (ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 5.根据id查询Shop详细信息
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size() - 1; i++) {
            sb.append(ids.get(i));
            sb.append(", ");
        }
        sb.append(ids.get(ids.size() - 1));
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id, " + sb + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(map.get(shop.getId().toString()).getValue());
        }
        // 6.返回
        return Result.ok(shops);
    }
}
