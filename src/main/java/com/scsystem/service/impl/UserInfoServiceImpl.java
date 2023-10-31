package com.scsystem.service.impl;

import com.scsystem.entity.UserInfo;
import com.scsystem.mapper.UserInfoMapper;
import com.scsystem.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
