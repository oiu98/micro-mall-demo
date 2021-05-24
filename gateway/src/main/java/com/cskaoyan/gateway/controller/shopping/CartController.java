package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.shopping.ICartService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.CartListByIdRequest;
import com.mall.shopping.dto.CartListByIdResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ZhaoJiachen on 2021/5/22
 * <p>
 * Description:购物车相关
 *      1. 获得购物车列表
 *
 */

@Slf4j
@RestController
@RequestMapping("/shopping")
@Api(tags = "CartController", description = "购物车控制层")
public class CartController {

    public static String USER_INFO_KEY="userInfo";

    @Reference(timeout = 3000,check = false)
    ICartService cartService;

    @RequestMapping("/carts")
    public ResponseData queryCarts(HttpServletRequest request) {
        // 从cookie中获取token
        String userInfo = (String) request.getAttribute(USER_INFO_KEY);

        // 转发给服务
        CartListByIdRequest cartListByIdRequest = new CartListByIdRequest();
        cartListByIdRequest.setUserId(Long.parseLong(userInfo));
        CartListByIdResponse cartListByIdResponse = cartService.getCartListById(cartListByIdRequest);
        if (!cartListByIdResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(cartListByIdResponse.getMsg());
        }
        return new ResponseUtil<>().setData(cartListByIdResponse.getCartProductDtos());
    }
}
