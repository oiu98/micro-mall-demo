package com.mall.order.biz.handler;

import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.mq.DelayOrderCancelConsumer;
import com.mall.order.mq.DelayOrderCancelProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.UnsupportedEncodingException;

/**
 * @Description: 利用mq发送延迟取消订单消息
 * @Author： ciggar
 * @Date: 2019-09-17 23:14
 **/
@Component
@Slf4j
public class SendMessageHandler extends AbstractTransHandler {

	@Autowired
	DelayOrderCancelProducer delayOrderCancelProducer;


	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	public boolean handle(TransHandlerContext context) {
		CreateOrderContext orderContext = (CreateOrderContext) context;

		// 当代码执行到这个handler，我们的下单基本就已经成功
		// 实现超时未支付订单的自动取消

		// 思路：
		//  发出一个延迟消息，延迟时间，由我们规定的，订单支付的超时时间来决定
		//  延迟消息里包含的数据：订单id

		// 取出订单id, 返回发送结果
		boolean sendResult = delayOrderCancelProducer.sendDelayCancelMessage(orderContext.getOrderId());
		return sendResult;
	}
}