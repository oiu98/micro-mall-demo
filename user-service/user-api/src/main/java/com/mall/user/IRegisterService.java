package com.mall.user;

import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;

/**
 * create : ZhaoJiachen
 * disc: 注册相关的服务
 *      1. 用户注册
 *      2. 注册激活
 */
public interface IRegisterService {

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return UserRegisterResponse
     */
    UserRegisterResponse userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 注册激活
     * @param userVerifyRequest
     * @return UserVerifyResponse
     */
    UserVerifyResponse userVerify(UserVerifyRequest userVerifyRequest);
}
