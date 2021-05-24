package com.mall.shopping.services;

import com.mall.shopping.ICartService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.dto.*;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/22
 * <p>
 * Description: 购物车相关
 */

@Slf4j
@Component
@Service
public class CartServiceImpl implements ICartService {
    @Override
    public CartListByIdResponse getCartListById(CartListByIdRequest request) {

        CartListByIdResponse cartListByIdResponse = new CartListByIdResponse();

        try {
            // 获得 userId
            Long userId = request.getUserId();

            // 从redis中获取carts
            CacheManager cacheManager = new CacheManager();
            List<CartProductDto> carts = cacheManager.getCartsCache(
                    GlobalConstants.CART_CACHE_PREFIX + String.valueOf(userId));
        } catch (Exception e) {
            log.error("CartServiceImpl.getCartListById occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(cartListByIdResponse, e);
        }

        return cartListByIdResponse;
    }

    @Override
    public AddCartResponse addToCart(AddCartRequest request) {
        return null;
    }

    @Override
    public UpdateCartNumResponse updateCartNum(UpdateCartNumRequest request) {
        return null;
    }

    @Override
    public CheckAllItemResponse checkAllCartItem(CheckAllItemRequest request) {
        return null;
    }

    @Override
    public DeleteCartItemResponse deleteCartItem(DeleteCartItemRequest request) {
        return null;
    }

    @Override
    public DeleteCheckedItemResposne deleteCheckedItem(DeleteCheckedItemRequest request) {
        return null;
    }

    @Override
    public ClearCartItemResponse clearCartItemByUserID(ClearCartItemRequest request) {
        return null;
    }
}
