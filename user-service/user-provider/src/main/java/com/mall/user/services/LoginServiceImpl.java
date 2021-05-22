package com.mall.user.services;

import com.mall.commons.result.ResponseData;
import com.mall.user.ILoginService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.MemberConverter;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.User;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.*;
import com.mall.user.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 用户登录相关功能的实现
 * 1. 用户登录
 * 2. 验证登录
 * 3. 用户退出
 */

@Slf4j
@Component
@Service
public class LoginServiceImpl implements ILoginService {

    @Autowired
    MemberMapper memberMapper;
    @Autowired
    MemberConverter memberConverter;

    @Override
    public UserLoginResponse userLogin(UserLoginRequest userLoginRequest) {

        UserLoginResponse userLoginResponse = new UserLoginResponse();

        try {
            // 合法性检查
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

            // 登陆成功 封装数据
            userLoginResponse = memberConverter.member2UserLoginRes(members.get(0));
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
        return null;
    }

    @Override
    public ResponseData userLoginOut() {
        return null;
    }
}
