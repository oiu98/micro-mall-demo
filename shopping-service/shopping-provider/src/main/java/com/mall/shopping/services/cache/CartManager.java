package com.mall.shopping.services.cache;

import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.dto.CartProductDto;
import com.mall.shopping.dto.UpdateCartNumRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Zhaojiachen
 * create-date: 2021/5/22
 */

@Slf4j
@Service
public class CartManager {

    @Autowired
    private RedissonClient redissonClient;

    public List<CartProductDto> getCarts(Long uid) {
        // cart_key
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + uid);
        List<CartProductDto> cartProductDtos = new ArrayList<>(map.values());

        return cartProductDtos;
    }

    public void addCartProduct(Long uid,CartProductDto cartProductDto){
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + uid);
        map.put(String.valueOf(cartProductDto.getProductId()),cartProductDto);
    }

    public void updateCartProduct(Long uid, UpdateCartNumRequest request) {
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + uid);
        CartProductDto cartProductDto = map.get(String.valueOf(request.getItemId()));
        cartProductDto.setProductNum(Long.valueOf(request.getNum()));
        cartProductDto.setChecked(request.getChecked());
        map.put(String.valueOf(cartProductDto.getProductId()),cartProductDto);
    }

    public void deleteCartProduct(Long uid, Long pid) {
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + uid);
        map.remove(pid);
    }

    public void deleteCheckedCartProduct(Long uid) {
        RMap<String, CartProductDto> map = redissonClient.getMap(GlobalConstants.CART_CACHE_PREFIX + uid);
        map.values().parallelStream().forEach(cartProductDto -> {
            if ("true".equals(cartProductDto.getChecked())) {
                map.remove(cartProductDto.getProductId());
            }
        });
    }

}
