package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.constants.OrderConstants;
import com.mall.order.dal.entitys.Order;
import com.mall.order.dal.entitys.OrderItem;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dto.CartProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ciggar
 * create-date: 2019/8/1-下午5:01
 * 初始化订单 生成订单
 */

@Slf4j
@Component
public class InitOrderHandler extends AbstractTransHandler {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        // 向下转型
        CreateOrderContext createOrderContext = (CreateOrderContext) context;

        // 插入订单表
        Order order = new Order();
        // 全局ID生成器 --- 生成唯一ID的工具 TODO 后续改为使用发号器
        String orderId = UUID.randomUUID().toString();
        createOrderContext.setOrderId(orderId); // orderId 放入 context 中
        order.setOrderId(orderId);
        order.setStatus(0);
        order.setPayment(createOrderContext.getOrderTotal()); // 总金额
        order.setPaymentType(1); // 在线支付
        order.setStatus(OrderConstants.ORDER_STATUS_INIT);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setUserId(createOrderContext.getUserId());
        order.setBuyerNick(createOrderContext.getUserName());
        int effectedRows = orderMapper.insert(order);
        if (effectedRows == 0) {
            throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                    OrderRetCode.DB_EXCEPTION.getMessage());
        }

        // 插入订单商品关联表
        List<CartProductDto> cartProductDtoList =
                createOrderContext.getCartProductDtoList();
        cartProductDtoList.parallelStream().forEach(cartProductDto -> {
            // 创建订单商品
            OrderItem orderItem = new OrderItem();
            String orderItemId = UUID.randomUUID().toString();
            // 封装参数
            orderItem.setId(orderItemId); // id主键
            orderItem.setItemId(cartProductDto.getProductId()); // 商品Id
            orderItem.setOrderId(orderId); // 订单Id
            orderItem.setNum(cartProductDto.getProductNum().intValue());
            orderItem.setPrice(cartProductDto.getSalePrice());
            orderItem.setTitle(cartProductDto.getProductName());
            orderItem.setPicPath(cartProductDto.getProductImg());
            orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
            orderItem.setStatus(1); // 库存已锁定

            // 插入数据库表
            int oiEffectedRows = orderItemMapper.insert(orderItem);
            if (oiEffectedRows == 0) {
                throw new BizException(OrderRetCode.DB_EXCEPTION.getCode(),
                        OrderRetCode.DB_EXCEPTION.getMessage());
            }
        });

        return true;
    }
}
