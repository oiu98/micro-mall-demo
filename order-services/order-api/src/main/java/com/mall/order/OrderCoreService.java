package com.mall.order;

import com.mall.order.dto.*;

/**
 *  ciggar
 * create-date: 2019/7/30-上午9:13
 * 订单相关业务
 */
public interface OrderCoreService {

    /**
     * 创建订单
     * @param request
     * @return
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);

    /**
     * 取消订单
     * @param request
     * @return
     */
    CancelOrderResponse cancelOrder(CancelOrderRequest request);

    /**
     * 删除订单
     * @param request
     * @return
     */
    DeleteOrderResponse deleteOrder(DeleteOrderRequest request);

    /**
     * 更新订单的支付状态
     * @param orderId
     * @param isSuccess
     * @return
     */
    boolean updatePaymentStatus(String orderId, boolean isSuccess);

    /**
     * 更新库存
     * @param orderId
     * @param isSuccess
     */
    void updateStock(String orderId, boolean isSuccess);
}
