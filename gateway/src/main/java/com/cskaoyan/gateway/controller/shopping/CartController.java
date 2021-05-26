package com.cskaoyan.gateway.controller.shopping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.shopping.ICartService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/22
 * <p>
 * Description:购物车相关
 * 1. 获得购物车列表
 * 2. 添加商品到购物车
 * 3. 更新购物车中的商品
 * 4. 删除购物车的商品
 * 5. 删除购物车选中的商品
 */

@Slf4j
@RestController
@RequestMapping("/shopping")
@Api(tags = "CartController", description = "购物车控制层")
public class CartController {

    public static String USER_INFO_KEY = "userInfo";

    @Reference(timeout = 3000, check = false)
    ICartService cartService;

    /**
     * 获得购物车列表
     */
    @GetMapping("/carts")
    public ResponseData queryCarts(HttpServletRequest request) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));

        // 转发给服务
        CartListByIdRequest cartListByIdRequest = new CartListByIdRequest();
        cartListByIdRequest.setUserId(uid);
        CartListByIdResponse cartListByIdResponse = cartService.getCartListById(cartListByIdRequest);
        if (!cartListByIdResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(cartListByIdResponse.getMsg());
        }
        return new ResponseUtil<>().setData(cartListByIdResponse.getCartProductDtos());
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/carts")
    public ResponseData addToCart(HttpServletRequest request, @RequestBody AddCartRequest addCartRequest) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));

        // 保证用户一致
        if(uid != addCartRequest.getUserId()) {
            return new ResponseUtil<>().setErrorMsg(ShoppingRetCode.NOT_ALLOWED_REQUEST.getMessage());
        }
        // 调用接口
        AddCartResponse addCartResponse = cartService.addToCart(addCartRequest);

        if (!addCartResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(addCartResponse.getMsg());
        }
        return new ResponseUtil<>().setData("成功");
    }

    /**
     * 更新购物车中的商品
     */
    @PutMapping("/carts")
    public ResponseData updateCartProduct(HttpServletRequest request, @RequestBody UpdateCartNumRequest updateCartNumRequest) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uid = Long.valueOf((Integer) userInfos.get("uid"));

        // 保证用户一致
        if(uid != updateCartNumRequest.getUserId()) {
            return new ResponseUtil<>().setErrorMsg(ShoppingRetCode.NOT_ALLOWED_REQUEST.getMessage());
        }
        // 调用接口
        UpdateCartNumResponse updateCartNumResponse = cartService.updateCartNum(updateCartNumRequest);

        if (!updateCartNumResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(updateCartNumResponse.getMsg());
        }
        return new ResponseUtil<>().setData("成功");
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping("/carts/{uid}/{pid}")
    public ResponseData deleteCartProduct(HttpServletRequest request,
                                          @PathVariable(value = "uid")Long uid,
                                          @PathVariable(value = "pid")Long pid) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uidd = Long.valueOf((Integer) userInfos.get("uid"));

        // 保证用户一致
        if(uidd != uid) {
            return new ResponseUtil<>().setErrorMsg(ShoppingRetCode.NOT_ALLOWED_REQUEST.getMessage());
        }
        // 调用接口
        DeleteCartItemRequest deleteCartItemRequest = new DeleteCartItemRequest();
        deleteCartItemRequest.setItemId(pid);
        deleteCartItemRequest.setUserId(uid);
        DeleteCartItemResponse deleteCartItemResponse = cartService.deleteCartItem(deleteCartItemRequest);

        if (!deleteCartItemResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(deleteCartItemResponse.getMsg());
        }
        return new ResponseUtil<>().setData("成功");
    }

    /**
     * 删除购物车中选中的商品
     */
    @DeleteMapping("/items/{id}")
    public ResponseData deleteCartCheckedProducts(HttpServletRequest request,
                                                  @PathVariable(value = "id")Long uid) {
        Map userInfos = checkUserInfo(request);
        if (userInfos == null) {
            new ResponseUtil<>().setErrorMsg(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
        Long uidd = Long.valueOf((Integer) userInfos.get("uid"));

        // 保证用户一致
        if(uidd != uid) {
            return new ResponseUtil<>().setErrorMsg(ShoppingRetCode.NOT_ALLOWED_REQUEST.getMessage());
        }
        // 调用接口
        DeleteCheckedItemRequest deleteCheckedItemRequest = new DeleteCheckedItemRequest();
        deleteCheckedItemRequest.setUserId(uid);
        DeleteCheckedItemResposne deleteCheckedItemResposne = cartService.deleteCheckedItem(deleteCheckedItemRequest);

        if (!deleteCheckedItemResposne.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(deleteCheckedItemResposne.getMsg());
        }
        return new ResponseUtil<>().setData("成功");
    }

    /**
     * json ---> userInfos（Map）
     */
    private Map checkUserInfo(HttpServletRequest request) {
        // 从cookie中获取token
        String userInfo = (String) request.getAttribute(USER_INFO_KEY);
        try {
            return new ObjectMapper().readValue(userInfo, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
