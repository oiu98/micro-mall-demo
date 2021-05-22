package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.IRegisterService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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
    @Reference(timeout = 3000,check = false)
    IKaptchaService kaptchaService;

    @PostMapping("/register")
        public ResponseData userRegister(@RequestBody Map<String ,String> map, HttpServletRequest request) {
        // 允许匿名访问 解析参数
        String userName = map.get("userName");
        String userPwd = map.get("userPwd");
        String email = map.get("email");
        String captcha = map.get("captcha");

        // 验证验证码
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        String kaptcha_uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
        kaptchaCodeRequest.setCode(captcha);
        kaptchaCodeRequest.setUuid(kaptcha_uuid);
        KaptchaCodeResponse kaptchaCodeResponse = kaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        String code = kaptchaCodeResponse.getCode();
        if (!code.equals(SysRetCodeConstants.SUCCESS.getCode())) { // 验证码错误
            return new ResponseUtil<>().setErrorMsg(kaptchaCodeResponse.getMsg());
        }

        // 用户注册
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
        UserRegisterResponse response = registerService.userRegister(userRegisterRequest);
        if (response.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {

        }
        return null;
    }
}
