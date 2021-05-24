package com.mall.user.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.ILoginService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.MemberConverter;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dto.*;
import com.mall.user.utils.ExceptionProcessorUtils;
import com.mall.user.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import javax.xml.bind.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 用户登录相关功能的实现
 *          1. 用户登录
 *          2. 验证登录
 */

@Slf4j
@Component
@Service
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberConverter memberConverter;

    /**
     *  用户登录接口
     */
    @Override
    public UserLoginResponse userLogin(UserLoginRequest userLoginRequest) {

        UserLoginResponse userLoginResponse = new UserLoginResponse();

        try {
            // 参数检查
            userLoginRequest.requestCheck();

            // 解析
            String userName = userLoginRequest.getUserName();
            String password = userLoginRequest.getPassword();

            // 验证密码是否正确
            String md5_passwd = DigestUtils.md5DigestAsHex(password.getBytes());
            Example memberExample = new Example(Member.class);
            memberExample.createCriteria().andEqualTo("username", userName)
                    .andEqualTo("password", md5_passwd);
            List<Member> members = memberMapper.selectByExample(memberExample);

            Member member = members.get(0);
            // 是否激活
            if ("N".equals(member.getIsVerified())) {
                throw new ValidateException(SysRetCodeConstants.USER_ISVERFIED_ERROR.getCode(),
                                            SysRetCodeConstants.USER_ISVERFIED_ERROR.getMessage());
            }

            // 验证通过，登陆成功 封装数据
            userLoginResponse = memberConverter.member2UserLoginRes(member);
            // 生成jwtMsg
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不转化null数据
            Map<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("uid",userLoginResponse.getId());
            userInfoMap.put("username",userLoginResponse.getUsername());
            userInfoMap.put("file",userLoginResponse.getFile());
            String jwtMsg = objectMapper.writeValueAsString(userInfoMap);
            // 生成JwtToken
            JwtTokenUtils build = JwtTokenUtils.builder().msg(jwtMsg).build();
            String jwtToken = build.creatJwtToken();

            userLoginResponse.setToken(jwtToken);
            userLoginResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            userLoginResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) { // 异常 反馈错误
            log.error("LoginServiceImpl.userLogin occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(userLoginResponse, e);
        }

        return userLoginResponse;
    }

    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) {

        CheckAuthResponse checkAuthResponse = new CheckAuthResponse();

        try {
            // 参数校验
            checkAuthRequest.requestCheck();

            // 获取token
            String token = checkAuthRequest.getToken();
            JwtTokenUtils tokenUtils = JwtTokenUtils.builder().token(token).build();
            String jwtMsg = tokenUtils.freeJwt();

            checkAuthResponse.setUserinfo(jwtMsg);
            checkAuthResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            checkAuthResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("LoginServiceImpl.validToken occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(checkAuthResponse, e);
        }

        return checkAuthResponse;
    }

}
