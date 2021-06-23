package com.mall.order.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
@Slf4j
public class DelayOrderCancelProducer {

    public final static String DELAY_CANCEL_TOPIC = "order_delay_cancel";

    DefaultMQProducer orderCancelProducer;

    @PostConstruct
    public void init() {
        orderCancelProducer = new DefaultMQProducer("delay_cancel_order_producer");

        orderCancelProducer.setNamesrvAddr("127.0.0.1:9876");

        try {
            orderCancelProducer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    public boolean sendDelayCancelMessage(String orderId) {
        // 加上日志输出
        // 创建消息
        Message message = new Message(DELAY_CANCEL_TOPIC, orderId.getBytes(Charset.forName("utf-8")));
        //private String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h";
        // 延迟时间半小时
        message.setDelayTimeLevel(16);
        // 测试：延迟30s
//        message.setDelayTimeLevel(4);

        // 发送延迟消息
        SendResult send = null;
        try {
            send = orderCancelProducer.send(message);
            // 加上日志
            log.info("尝试发送延迟消息结束，发送结果：" + send.getSendStatus());
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (send.getSendStatus().equals(SendStatus.SEND_OK)) {
            // 日志输出消息发送成功
            return true;
        }

        return false;
    }


}
