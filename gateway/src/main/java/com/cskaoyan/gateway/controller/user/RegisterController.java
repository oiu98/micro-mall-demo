package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.user.IRegisterService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 注册相关
 */

@RestController
@RequestMapping("/user")
@Anoymous
public class RegisterController {

    @Reference(timeout = 3000,check = false)
    IRegisterService registerService;

//    @PostMapping("/register")
//    public ResponseData userRegister(@RequestBody Map<String ,String> map, ) {
//        // 允许匿名访问 直接转发给用户服务下的注册接口
//        UserRegisterResponse response = registerService.userRegister(request);
//
//    }
}
