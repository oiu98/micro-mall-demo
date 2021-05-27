package com.mall.order.biz.handler;/**
 * Created by ciggar on 2019/8/1.
 */

import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dal.entitys.Address;
import com.mall.order.dal.entitys.OrderShipping;
import com.mall.order.dal.persistence.AddressMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 *  ciggar
 * create-date: 2019/8/1-下午5:06
 *
 * 处理物流信息（商品寄送的地址）
 */
@Slf4j
@Component
public class LogisticalHandler extends AbstractTransHandler {

    @Autowired
    private OrderShippingMapper orderShippingMapper;
    @Autowired
    private AddressMapper addressMapper;

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {

        // 向下转型
        CreateOrderContext createOrderContext = (CreateOrderContext) context;

        // 插入邮寄信息表
        Long addressId = createOrderContext.getAddressId();
        Address address = addressMapper.selectByPrimaryKey(addressId); // 获得地址详情
        OrderShipping orderShipping = new OrderShipping();
        orderShipping.setOrderId(createOrderContext.getOrderId());
        orderShipping.setReceiverName(address.getUserName());
        orderShipping.setReceiverPhone(address.getTel());
        orderShipping.setReceiverMobile(address.getTel());
        orderShipping.setReceiverAddress(address.getStreetName());
        orderShipping.setCreated(new Date());
        orderShipping.setUpdated(new Date());

        int effectedRows = orderShippingMapper.insert(orderShipping);
        if (effectedRows == 0) {
            throw new BizException(OrderRetCode.SHIPPING_DB_SAVED_FAILED.getCode(),
                                    OrderRetCode.SHIPPING_DB_SAVED_FAILED.getMessage());
        }

        return true;
    }
}
