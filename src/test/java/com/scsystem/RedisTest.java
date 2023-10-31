package com.scsystem;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
public class RedisTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 测试 Redis 主从架构下读写分离
     * 可根据控制台打印结果验证是否满足从节点被轮询访问
     */
    @Test
    void slaveTest() {
        String prefix = "test:";

        for (int i = 0; i < 10; i++) {
            stringRedisTemplate.opsForValue().set(prefix + i, "testValue" + i);
            log.info("set value success: {}", i);
            String val = stringRedisTemplate.opsForValue().get(prefix + i);
            log.info("get value success: {}", val);
        }
    }
}
