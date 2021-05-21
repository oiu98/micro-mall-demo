package com.mall.user.services;

import com.mall.user.IRegisterService;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 用户注册相关的实现
 */

@Slf4j
@Component
@Service
public class RegisterServiceImpl implements IRegisterService {

    @Override
    public UserRegisterResponse userRegister(UserRegisterRequest userRegisterRequest) {
        return null;
    }

    @Override
    public UserVerifyResponse userVerify(UserVerifyRequest userVerifyRequest) {
        return null;
    }
}
