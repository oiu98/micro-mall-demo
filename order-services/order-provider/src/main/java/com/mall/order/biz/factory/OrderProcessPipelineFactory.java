package com.mall.order.biz.factory;

import com.mall.order.biz.handler.*;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.biz.convert.CreateOrderConvert;
import com.mall.order.biz.convert.TransConvert;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.biz.handler.ClearCartItemHandler;
import com.mall.order.biz.handler.SendMessageHandler;
import com.mall.order.biz.handler.SubStockHandler;
import com.mall.order.biz.handler.ValidateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ciggar
 * create-date: 2019/8/2-下午10:52
 * <p>
 * 构建订单处理链
 */
@Slf4j
@Service
public class OrderProcessPipelineFactory extends AbstranctTransPipelineFactory<CreateOrderRequest> {

    @Autowired
    private InitOrderHandler initOrderHandler;
    @Autowired
    private ValidateHandler validateHandler;
    @Autowired
    private LogisticalHandler logisticalHandler;
    @Autowired
    private ClearCartItemHandler clearCartItemHandler;
    @Autowired
    private SubStockHandler subStockHandler;
    @Autowired
    private SendMessageHandler sendMessageHandler;


    @Override
    protected TransHandlerContext createContext() {
        CreateOrderContext createOrderContext = new CreateOrderContext();
        return createOrderContext;
    }

    @Override
    protected void doBuild(TransPipeline pipeline) {
        pipeline.addLast(validateHandler); // 验证用户合法性
        pipeline.addLast(subStockHandler); // 扣减库存
        pipeline.addLast(initOrderHandler); // 初始化订单
        pipeline.addLast(logisticalHandler); // 生成邮寄表
        pipeline.addLast(clearCartItemHandler); // 清空购物车中购买的商品
        pipeline.addLast(sendMessageHandler); // 发送订单超时取消的消息
    }

    @Override
    protected TransConvert createConvert() { //构建转换器
        return new CreateOrderConvert();
    }
}
