package com.scsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scsystem.dto.LoginFormDTO;
import com.scsystem.dto.Result;
import com.scsystem.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

    Result logout(HttpServletRequest request);
}
