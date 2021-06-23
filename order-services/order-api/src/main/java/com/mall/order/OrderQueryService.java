package com.mall.order;

import com.mall.order.dto.*;

/**
 *  ciggar
 * create-date: 2019/7/30-上午10:01
 */
public interface OrderQueryService {

    /**
     * 获取当前用户的所有订单
     * @param request
     * @return
     */
    OrderListResponse getOrderList(OrderListRequest request);

    /**
     * 查询订单详情
     * @param request
     * @return
     */
    OrderDetailResponse getOrderDetail(OrderDetailRequest request);

}
