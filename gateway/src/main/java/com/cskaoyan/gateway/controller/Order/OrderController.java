package com.cskaoyan.gateway.controller.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.dto.CreateOrderResponse;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.user.intercepter.TokenIntercepter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/26
 * <p>
 * Description: 购物车控制层
 */
@Slf4j
@RestController
@RequestMapping("/shopping")
public class OrderController {

    @Reference(timeout = 3000,check = false)
    private OrderCoreService orderCoreService;
    @Reference(timeout = 3000,check = false)
    private OrderQueryService orderQueryService;

    /**
     * 创建订单
     */
    @PostMapping("/order")
    public ResponseData createOrder(@RequestBody CreateOrderRequest createOrderRequest,HttpServletRequest request) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));
        createOrderRequest.setUserId(uid);

        CreateOrderResponse response = orderCoreService.createOrder(createOrderRequest);
        if (!response.getCode().equals(OrderRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        return new ResponseUtil<>().setData(response.getOrderId());
    }

    /**
     * 获取当前⽤户的所有订单
     */

    /**
     *  查询订单详情
     */

    /**
     *  取消订单
     */

    /**
     *  删除订单
     */


    /**
     * json ---> userInfos（Map）
     */
    private Map checkUserInfo(HttpServletRequest request) {
        // 从cookie中获取token
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        try {
            return new ObjectMapper().readValue(userInfo, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
