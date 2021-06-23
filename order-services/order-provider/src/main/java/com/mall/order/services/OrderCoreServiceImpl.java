package com.mall.order.services;

import com.mall.order.OrderCoreService;
import com.mall.order.biz.TransOutboundInvoker;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.factory.OrderProcessPipelineFactory;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * ciggar
 * create-date: 2019/7/30-上午10:05
 */
@Slf4j
@Component
@Service(cluster = "failfast")
public class OrderCoreServiceImpl implements OrderCoreService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    OrderShippingMapper orderShippingMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    OrderProcessPipelineFactory orderProcessPipelineFactory;


    /**
     * 创建订单的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        CreateOrderResponse response = new CreateOrderResponse();
        try {
            //创建pipeline对象
            TransOutboundInvoker invoker = orderProcessPipelineFactory.build(request);

            //启动pipeline
            invoker.start(); //启动流程（pipeline来处理）

            //获取处理结果
            AbsTransHandlerContext context = invoker.getContext();

            //把处理结果转换为response
            response = (CreateOrderResponse) context.getConvert().convertCtx2Respond(context);
        } catch (Exception e) {
            log.error("OrderCoreServiceImpl.createOrder Occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }
        return response;
    }

    /**
     * 取消订单的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        return null;
    }

    /**
     * 删除订单的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public DeleteOrderResponse deleteOrder(DeleteOrderRequest request) {

        return null;
    }

    @Override
    public boolean updatePaymentStatus(String orderId, boolean isSuccess) {
        Order order = new Order();
        order.setOrderId(orderId);
        if (isSuccess) {
            order.setStatus(4); // 交易成功
        } else {
            order.setStatus(6); // 交易失败
        }
        orderMapper.updateByPrimaryKeySelective(order); // 可以做异常处理
        return true;
    }

    @Override
    public void updateStock(String orderId, boolean isSuccess) {
        List<OrderItem> orderItems = orderItemMapper.queryByOrderId(orderId);

        if (isSuccess) {
            orderItems.stream().forEach(orderItem -> {
                Stock stock = new Stock();
                stock.setItemId(orderItem.getItemId());
                stock.setLockCount(-orderItem.getNum()); // 释放冻结库存
                Integer affectedRows = stockMapper.updateStock(stock);
            });
        } else {
            orderItems.stream().forEach(orderItem -> {
                Stock stock = new Stock();
                stock.setItemId(orderItem.getItemId());
                stock.setStockCount((long) orderItem.getNum());
                stock.setLockCount(-orderItem.getNum()); // 还原库存
                Integer affectedRows = stockMapper.updateStock(stock);
            });
        }
    }

}
