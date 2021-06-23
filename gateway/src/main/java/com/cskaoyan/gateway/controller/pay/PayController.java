package com.cskaoyan.gateway.controller.pay;

import com.cskaoyan.gateway.form.pay.PayForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.pay.PayCoreService;
import com.mall.pay.constants.PayReturnCodeEnum;
import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.alipay.AlipayQueryRetResponse;
import com.mall.pay.dto.alipay.AlipaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/6/11
 * <p>
 * Description:
 */

@Slf4j
@RestController
@RequestMapping("/cashier")
public class PayController {

    @Reference(timeout = 3000, check = false)
    private PayCoreService payCoreService;

    public static String USER_INFO_KEY = "userInfo";
    private PaymentRequest request;

    @PostMapping("/pay")
    public ResponseData pay(@RequestBody PayForm payForm, HttpServletRequest httpServletRequest) {

        String userInfo = (String) httpServletRequest.getAttribute(USER_INFO_KEY);
        Map<String, Object> userInfos = null;
        try {
            userInfos = new ObjectMapper().readValue(userInfo, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object uid = userInfos.get("uid");
        Object username = userInfos.get("username");

        AlipaymentResponse alipaymentResponse = null;

        String payType = payForm.getPayType();
        if ("alipay".equals(payType)) {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setTradeNo(payForm.getOrderId());
            paymentRequest.setSubject(payForm.getInfo());
            paymentRequest.setTotalFee(payForm.getMoney());
            paymentRequest.setOrderFee(payForm.getMoney());
            paymentRequest.setPayChannel(payForm.getPayType());
            paymentRequest.setUserId(((Integer) uid).longValue());
            alipaymentResponse = payCoreService.aliPay(paymentRequest);
        }

        if (PayReturnCodeEnum.SUCCESS.getCode().equals(alipaymentResponse.getCode())) {
            String qrCode = alipaymentResponse.getQrCode();
            String qrPath = "http://localhost:8080/images/" + qrCode;

            return new ResponseUtil<>().setData(qrPath);
        }

        return new ResponseUtil<>().setErrorMsg("something wrong");
    }

    @GetMapping("/queryStatus")
    public ResponseData queryStatus(HttpServletRequest httpServletRequest, String orderId) {
        String userInfo = (String) httpServletRequest.getAttribute(USER_INFO_KEY);
        Map<String, Object> userInfos = null;
        try {
            userInfos = new ObjectMapper().readValue(userInfo, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Integer uid = (Integer) userInfos.get("uid");
        Object username = userInfos.get("username");

        PaymentRequest request = new PaymentRequest();
        request.setUserId(uid.longValue());
        request.setTradeNo(orderId);
        AlipayQueryRetResponse alipayQueryRetResponse = payCoreService.queryAlipayRet(request);
        if (alipayQueryRetResponse.getCode().equals(PayReturnCodeEnum.SUCCESS.getCode())) {
            return new ResponseUtil<>().setData("支付成功");
        } else {
            return new ResponseUtil<>().setErrorMsg("支付失败！");
        }
    }
}
