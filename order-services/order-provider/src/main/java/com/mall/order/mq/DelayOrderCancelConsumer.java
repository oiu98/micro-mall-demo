package com.mall.order.mq;
/*
      延迟消息的消费者，会在指定的延迟时间之后，消费到这条消息：
      1. message中包含了订单id，获取延迟消息中的订单id
      2. 去数据库，查询订单状态，有没有被成功支付方，如果已支付对已支付的订单什么都不做
      3. 如果，发现该orderId对应的订单，没有被支付，此时，我们的消费逻辑就是取消订单
         a. 修改数据库中，已经存储的订单信息，将其状态改为已取消 ORDER_STATUS_TRANSACTION_CANCEL
         b. 将订单中的每一个商品对应的锁定库存，在库存表中，还原
         c. 在订单条目表中，将取消订单中的每一个商品条目的库存状态信息改为 2
 */

import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.StockMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
@Slf4j
public class DelayOrderCancelConsumer {

    DefaultMQPushConsumer delayCancelOrderConsumer;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private StockMapper stockMapper;

    @PostConstruct
    public void init() {

        delayCancelOrderConsumer
                = new DefaultMQPushConsumer("delay_cancel_order_consumer");

        delayCancelOrderConsumer.setNamesrvAddr("127.0.0.1:9876");

        try {
            delayCancelOrderConsumer.subscribe(DelayOrderCancelProducer.DELAY_CANCEL_TOPIC, "*");

            delayCancelOrderConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 执行延迟取消订单的业务逻辑
                    // 获得订单
                    MessageExt message = msgs.get(0);
                    String orderId = null;
                    try {
                        orderId = new String(message.getBody(), "utf-8");
                        log.info("接收到延迟消息，执行订单号：" + orderId); // 日志
                    } catch (UnsupportedEncodingException e) {
                        log.error("解析延迟消息失败！");
                        e.printStackTrace();
                    }
                    Order order = orderMapper.selectByPrimaryKey(orderId);
                    Integer status = order.getStatus();
                    if (status > 0) {
                        log.info("订单非待付款状态，不执行任何操作");
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    order.setStatus(5); // 关闭交易
                    orderMapper.updateByPrimaryKey(order);
                    log.info("订单号：" + orderId + "，订单状态变更成功（0--->5）");
                    // 释放库存,更新订单商品表
                    List<OrderItem> orderItems = orderItemMapper.queryByOrderId(orderId);
                    String finalOrderId = orderId; // 提供多线程使用的orderId
                    orderItems.parallelStream().forEach(orderItem -> {
                        Stock stock = stockMapper.selectStockForUpdate(orderItem.getItemId());
                        stock.setStockCount(Long.valueOf(orderItem.getNum()));
                        stock.setLockCount(-orderItem.getNum());
                        // 更新库存
                        stockMapper.updateStock(stock);
                        log.info("订单号: {},商品id: {},释放库存: {}", new Object[]{
                                finalOrderId, orderItem.getItemId(), orderItem.getNum()});
                        // 更新订单商品表
                        orderItemMapper.updateStockStatus(2, finalOrderId);
                        log.info("订单号: {},商品id: {}, 商品状态：1 ---> 2", new Object[]{
                                finalOrderId, orderItem.getItemId()});
                    });

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            // 启动消费者
            delayCancelOrderConsumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }

    }


}
