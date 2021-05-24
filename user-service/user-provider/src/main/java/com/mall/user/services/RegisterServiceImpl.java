package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.user.IRegisterService;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import com.mall.user.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import javax.xml.bind.ValidationException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 用户注册相关的实现
 */

@Slf4j
@Component
@Service
public class RegisterServiceImpl implements IRegisterService {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private UserVerifyMapper userVerifyMapper;
    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public UserRegisterResponse userRegister(UserRegisterRequest userRegisterRequest) {

        UserRegisterResponse userRegisterResponse = new UserRegisterResponse();

        try {
            // 参数校验
            userRegisterRequest.requestCheck();

            // 解析参数
            String userName = userRegisterRequest.getUserName();
            String userPwd = userRegisterRequest.getUserPwd();
            String email = userRegisterRequest.getEmail();

            // 验证用户名是否已使用
            Member member = new Member();
            member.setUsername(userName);
            List<Member> members = memberMapper.select(member);
            if (!CollectionUtils.isEmpty(members)) {
                throw new ValidateException(SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getCode(),
                                            SysRetCodeConstants.USERNAME_ALREADY_EXISTS.getMessage());
            }

            // 插入用户表
            Member newMember = new Member();
            newMember.setUsername(userName);
            String md5_passwd = DigestUtils.md5DigestAsHex(userPwd.getBytes());
            newMember.setPassword(md5_passwd);
            newMember.setEmail(email);
            newMember.setCreated(new Date());
            newMember.setUpdated(new Date());
            newMember.setIsVerified("N");
            newMember.setState(1);
            int memEffectedRows = memberMapper.insertSelective(newMember);
            if (memEffectedRows == 0) {
                userRegisterResponse.setCode(SysRetCodeConstants.USER_REGISTER_FAILED.getCode());
                userRegisterResponse.setMsg(SysRetCodeConstants.USER_REGISTER_FAILED.getMessage());
                return userRegisterResponse;
            }

            // 发送邮件 ---> 异步方式 TODO 后续将邮件服务用RocketMQ实现
            String uuid = userName + userPwd + UUID.randomUUID().toString(); // 用户验证表uuid
            Thread t = new Thread(() -> {
                sendEmail(uuid,userName,email);
            });
            t.start(); // 启动新线程，先凑活用一下

            // 插入用户验证表
            UserVerify userVerify = new UserVerify();
            userVerify.setUsername(userName);
            userVerify.setIsVerify("N");
            userVerify.setIsExpire("N");
            userVerify.setRegisterDate(new Date());
            userVerify.setUuid(uuid);
            int verifyEffectedRows = userVerifyMapper.insert(userVerify);
            if (verifyEffectedRows ==0) {
                userRegisterResponse.setCode(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getCode());
                userRegisterResponse.setMsg(SysRetCodeConstants.USER_REGISTER_VERIFY_FAILED.getMessage());
                return userRegisterResponse;
            }

            userRegisterResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            userRegisterResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("RegisterServiceImpl.userRegister occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(userRegisterResponse, e);
        }

        log.info("用户注册成功，用户参数：{}", JSON.toJSONString(userRegisterRequest));
        return userRegisterResponse;
    }

    /**
     *  发送邮件服务
     */
    private void sendEmail(String uuid, String userName, String email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("micro-mall账户激活");
        simpleMailMessage.setFrom("479358584@qq.com");
        simpleMailMessage.setTo(email);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://localhost:8080/user/verify?uuid=")
                     .append(uuid)
                     .append("&userName=")
                     .append(userName);
        simpleMailMessage.setText(stringBuilder.toString());

        javaMailSender.send(simpleMailMessage);
    }

    /**
     * 用户激活
     * @param userVerifyRequest
     * @return
     */
    @Override
    public UserVerifyResponse userVerify(UserVerifyRequest userVerifyRequest) {
        UserVerifyResponse userVerifyResponse = new UserVerifyResponse();

        try {
            // 参数校验
            userVerifyRequest.requestCheck();

            // 解析参数
            String userName = userVerifyRequest.getUserName();
            String uuid = userVerifyRequest.getUuid();

            // 获取用户验证表记录
            UserVerify userVerifyEx = new UserVerify();
            userVerifyEx.setUuid(uuid);
            userVerifyEx.setUsername(userName);
            List<UserVerify> userVerifies = userVerifyMapper.select(userVerifyEx);
            if (CollectionUtils.isEmpty(userVerifies)) {
                userVerifyResponse.setCode(SysRetCodeConstants.USERVERIFY_INFOR_INVALID.getCode());
                userVerifyResponse.setMsg(SysRetCodeConstants.USERVERIFY_INFOR_INVALID.getMessage());
                return userVerifyResponse;
            }

            UserVerify userVerify = userVerifies.get(0);
            // 记录是否过期 TODO 实现激活信息过期
            if ("Y".equals(userVerify.getIsExpire())) {
                userVerifyResponse.setCode(SysRetCodeConstants.USERVERIFY_INFOR_EXPIRED.getCode());
                userVerifyResponse.setMsg(SysRetCodeConstants.USERVERIFY_INFOR_EXPIRED.getMessage());
                return userVerifyResponse;
            }

            // 激活，修改用户表和用户验证表字段
            userName = userVerify.getUsername(); // 确保userName正确
            userVerify.setIsVerify("Y");
            int VeriEffectedRows = userVerifyMapper.updateByPrimaryKeySelective(userVerify);
            Member member = new Member();
            member.setIsVerified("Y");
            Example example = new Example(Member.class);
            example.createCriteria().andEqualTo("username",userName);
            int memEffectedRows = memberMapper.updateByExampleSelective(member, example);

            // 成功 return
            userVerifyResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("RegisterServiceImpl.userVerify occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(userVerifyResponse, e);
        }

        return userVerifyResponse;
    }
}
