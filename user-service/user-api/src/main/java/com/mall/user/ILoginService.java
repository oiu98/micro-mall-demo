package com.mall.user;

import com.mall.commons.result.ResponseData;
import com.mall.user.dto.*;

/**
 * create : ZhaoJiachen
 * disc: 登陆相关的服务
 *      1. 用户登录
 *      2. 验证登录
 *      3. 用户退出
 */
public interface ILoginService {

    /**
     * 用户登录
     * @param userLoginRequest
     * @return UserLoginResponse
     */
    UserLoginResponse userLogin(UserLoginRequest userLoginRequest);

    /**
     *  验证用户登陆状态
     * @param checkAuthRequest
     * @return CheckAuthResponse
     */
    CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest);


    /**
     * 用户退出
     * @return ResponseData
     */
    ResponseData userLoginOut();
}
