package com.cskaoyan.gateway.controller.promo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.promo.PromoService;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.dto.*;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/6/13
 * <p>
 * Description:
 * 秒杀业务控制层
 */

@RestController
@RequestMapping("/shopping")
public class PromoController {

    @Reference(timeout = 20000,check = false,retries = 0)
    private PromoService promoService;

    public static String USER_INFO_KEY = "userInfo";

    @GetMapping("/seckilllist")
    public ResponseData seckilllist(Integer sessionId) {

        PromoInfoRequest promoInfoRequest = new PromoInfoRequest();
        promoInfoRequest.setSessionId(sessionId);
        String yyyymmdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
        promoInfoRequest.setYyyymmdd(yyyymmdd);
        PromoInfoResponse response = promoService.getPromoList(promoInfoRequest);

        if (!PromoRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        return new ResponseUtil<>().setData(response);
    }

    @PostMapping("/promoProductDetail")
    public ResponseData promoProductDetail(@RequestBody Map<String,Long> map){
        Long psId = map.get("psId");
        Long productId = map.get("productId");

        PromoProductDetailRequest request = new PromoProductDetailRequest();
        request.setProductId(productId);
        request.setPsId(psId);
        PromoProductDetailResponse response = promoService.getPromoProductDetail(request);

        if (!PromoRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        return new ResponseUtil<>().setData(response);
    }

    @PostMapping("/seckill")
    public ResponseData seckill(@RequestBody CreatePromoOrderRequest request, HttpServletRequest httpServletRequest) {

        String userInfo = (String) httpServletRequest.getAttribute(USER_INFO_KEY);
        Map<String, Object> userInfos = null;
        try {
            userInfos = new ObjectMapper().readValue(userInfo, HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Integer uid = (Integer) userInfos.get("uid");
        Object username = userInfos.get("username");
        request.setUserId(uid.longValue());
        request.setUsername((String) username);

        CreatePromoOrderResponse response = promoService.createPromoOrder(request);// 创建秒杀订单 - 非分布式事务的方式

        if (!PromoRetCode.SUCCESS.getCode().equals(response.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        return new ResponseUtil<>().setData(response);
    }
}
