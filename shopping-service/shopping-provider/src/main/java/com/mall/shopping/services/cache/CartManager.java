package com.mall.shopping.services.cache;

import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.dto.CartProductDto;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Zhaojiachen
 * create-date: 2021/5/22
 */

@Slf4j
@Service
public class CartManager {

    @Autowired
    private RedissonClient redissonClient;

    public List<CartProductDto> getCarts(String key) {
        // cart_key
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + key);
        List<CartProductDto> cartProductDtos = new ArrayList<>(map.values());

        return cartProductDtos;
    }

//    public Object updateCarts(UpdateCartNumRequest request, String key3) {
//            RMap map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + key3);
//            Object item = map.get(request.getItemId());
//            return item;
//    }

}
