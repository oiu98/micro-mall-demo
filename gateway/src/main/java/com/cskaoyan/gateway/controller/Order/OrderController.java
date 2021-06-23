package com.cskaoyan.gateway.controller.Order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.order.OrderCoreService;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.*;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.user.intercepter.TokenIntercepter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Reference(timeout = 6000,check = false,retries = 0)
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
    @GetMapping("/order")
    public ResponseData getOrderList(OrderListRequest orderListRequest,HttpServletRequest request) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));

        // 调用服务接口
        orderListRequest.setUserId(uid);
        OrderListResponse orderListResponse = orderQueryService.getOrderList(orderListRequest);

        if (!orderListResponse.getCode().equals(OrderRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(orderListResponse.getMsg());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("data",orderListResponse.getDetailInfoList());
        result.put("total",orderListResponse.getTotal());
        return new ResponseUtil<>().setData(result);
    }

    /**
     *  查询订单详情
     */
    @GetMapping("/order/{id}")
    public ResponseData getOrderItem(@PathVariable(value = "id")String orderId, HttpServletRequest request) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));

        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setOrderId(orderId);
        // 调用服务接口
        OrderDetailResponse response = orderQueryService.getOrderDetail(orderDetailRequest);
        if (!response.getCode().equals(OrderRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("userId",response.getUserId());
        result.put("userName",response.getOrderShippingDto().getReceiverName());
        result.put("orderTotal",response.getPayment());
        result.put("orderStatus",response.getStatus());
        result.put("streetName",response.getOrderShippingDto().getReceiverAddress());
        result.put("tel",response.getOrderShippingDto().getReceiverPhone());
        result.put("goodsList",response.getOrderItemDto());
        return new ResponseUtil<>().setData(result);
    }

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
