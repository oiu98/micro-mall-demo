package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.ILoginService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description:
 * 登录相关
 */

@RestController
@RequestMapping("/user")
public class LoginController {

    @Reference(timeout = 3000,check = false)
    IKaptchaService kaptchaService;
    @Reference(timeout = 3000,check = false)
    ILoginService loginService;

    @PostMapping("/login")
    @Anoymous
    public ResponseData userLogin(@RequestBody Map<String,String> map, HttpServletRequest request){

        // 解析参数
        String userName = map.get("userName");
        String userPwd = map.get("userPwd");
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

        // 登录
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserName(userName);
        userLoginRequest.setPassword(userPwd);
        UserLoginResponse userLoginResponse = loginService.userLogin(userLoginRequest);
        if (!userLoginResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(userLoginResponse.getMsg());
        }

        return new ResponseUtil<>().setData(userLoginResponse);
    }

    @GetMapping("/login")
    public ResponseData loginVerify() {
        return new ResponseUtil<>().setErrorMsg("token失效啦");
    }
}
