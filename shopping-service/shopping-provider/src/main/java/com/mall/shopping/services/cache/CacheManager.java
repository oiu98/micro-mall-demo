package com.mall.shopping.services.cache;

import com.mall.shopping.dto.CartProductDto;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  ciggar
 * create-date: 2019/7/24-17:17
 */
@Service
public class CacheManager {

    @Autowired
    private RedissonClient redissonClient;

    public String checkCache(String key){
        try {
            RBucket rBucket = redissonClient.getBucket(key);
            return rBucket.get().toString();
        }catch (Exception e){
            return null;
        }
    }

    public void setCache(String key,String val,int expire){
        RBucket rBucket = redissonClient.getBucket(key);
        rBucket.set(val);
        rBucket.expire(expire, TimeUnit.DAYS);
    }

    public void expire(String key,int expire){
        RBucket rBucket = redissonClient.getBucket(key);
        rBucket.expire(expire,TimeUnit.DAYS);
    }

}
