package com.mall.promo.mq;

import com.alibaba.fastjson.JSON;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.dal.persistence.PromoItemMapper;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Component
public class PromoOrderProducer {

    TransactionMQProducer transactionMQProducer;

    String promoOrderTopic = "promo_create_order";

    @Autowired
    PromoItemMapper promoItemMapper;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        transactionMQProducer
                = new TransactionMQProducer("promo_order_producer");

        transactionMQProducer.setNamesrvAddr("127.0.0.1:9876");

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                // 执行秒杀服务的本地事物，扣减库存
                Map<String, Long> argMap = (Map<String, Long>) arg;
                Long psId = argMap.get("psId");
                Long productId = argMap.get("productId");

                String key = "promo_local_transaction:" + msg.getTransactionId();

                // 使用分布式锁，在扣减库存的时候加锁，，扣减完库存之后释放锁
                String lockKey = "promo_item_stock_lock:" + psId + "-" + productId;
                // 等价于一个分布式的可重入锁
                RLock lock = redissonClient.getLock(lockKey);
                lock.lock();
                try {
                    Integer effectiveRow = promoItemMapper.decreaseStock(productId, psId);
                    if (effectiveRow < 1) {
                        // 库存扣减失败，将本地事务的执行结果，存储到redis中，
                        cacheManager.setCache(key, "fail", 1);
                        return LocalTransactionState.ROLLBACK_MESSAGE;
                    }
                } finally {
                    lock.unlock();
                }


                // 如果扣减库存成功
                cacheManager.setCache(key, "success", 1);
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                String key = "promo_local_transaction:" + msg.getTransactionId();
                String s = cacheManager.checkCache(key);
                if (s == null || s.trim().isEmpty()) {
                   return LocalTransactionState.UNKNOW;
                }
                if ("fail".equals(s)) {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        });

        try {
            transactionMQProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    public boolean sendPromoOrderMessage(CreateSeckillOrderRequest request, Long psId, Long productId) {
        Message message = new Message();
        message.setTopic(promoOrderTopic);
        message.setBody(JSON.toJSONString(request).getBytes(Charset.forName("utf-8")));

        Map<String, Long> argMap = new HashMap<>();
        argMap.put("psId", psId);
        argMap.put("productId", productId);
        TransactionSendResult sendResult = null;
        try {
           sendResult = transactionMQProducer.sendMessageInTransaction(message, argMap);
        } catch (MQClientException e) {
            e.printStackTrace();
        }

        if (sendResult != null && LocalTransactionState.COMMIT_MESSAGE.equals(sendResult.getLocalTransactionState())) {
            //记录一个日志，说明消息发送成功，且本地事物执行成功
            return true;
        }

        return false;
    }


}
