package com.mall.user.services;

import com.mall.commons.result.ResponseData;
import com.mall.user.ILoginService;
import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description:
 * 用户登录相关功能的实现
 */

@Slf4j
@Component
@Service
public class LoginServiceImpl implements ILoginService {

    @Override
    public UserLoginResponse userLogin(UserLoginRequest userLoginRequest) {
        return null;
    }

    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) {
        return null;
    }

    @Override
    public ResponseData userLoginOut() {
        return null;
    }
}
