package com.cskaoyan.gateway.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.ILoginService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.*;
import com.mall.user.intercepter.TokenIntercepter;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 登录相关
 *      1. 用户登录
 *      2. 验证用户登录
 *      3. 用户退出
 */

@RestController
@RequestMapping("/user")
public class LoginController {

    @Reference(timeout = 3000,check = false)
    IKaptchaService kaptchaService;
    @Reference(timeout = 3000,check = false)
    ILoginService loginService;

    /**
     *  用户登录
     */
    @PostMapping("/login")
    @Anoymous
    public ResponseData userLogin(@RequestBody Map<String,String> map,
                                  HttpServletRequest request,
                                  HttpServletResponse response){

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

        // 登录验证
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserName(userName);
        userLoginRequest.setPassword(userPwd);
        UserLoginResponse userLoginResponse = loginService.userLogin(userLoginRequest);
        if (!userLoginResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(userLoginResponse.getMsg());
        }

        // 设置token进cookie
        Cookie cookie = new Cookie(TokenIntercepter.ACCESS_TOKEN, userLoginResponse.getToken());
        cookie.setPath("/");
        CookieUtil.setCookie(response,cookie);
        return new ResponseUtil<>().setData(userLoginResponse);
    }

    /**
     *  验证用户登录状态
     */
    @GetMapping("/login")
    public ResponseData loginVerify(HttpServletRequest request) {
        // 因为在 TokenIntercepter 中已将 userInfo 放入到了 attribute 域中，所以直接return
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);

        Map<String,Object> map = null;
        try {
            map = new ObjectMapper().readValue(userInfo, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseUtil<>().setData(map);
    }

    /**
     *  用户登出
     */
    @GetMapping("/loginOut")
    public ResponseData userLoginOut(HttpServletResponse response) {

        Cookie cookie = new Cookie(TokenIntercepter.ACCESS_TOKEN, null);
        cookie.setMaxAge(0); // 销毁cookiee
        cookie.setPath("/");
        CookieUtil.setCookie(response,cookie);

        return new ResponseUtil<>().setData(null);
    }
}
