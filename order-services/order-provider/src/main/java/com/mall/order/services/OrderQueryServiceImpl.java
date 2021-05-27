package com.mall.order.services;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.order.OrderQueryService;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.converter.OrderConverter;
import com.mall.order.dal.entitys.*;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.OrderMapper;
import com.mall.order.dal.persistence.OrderShippingMapper;
import com.mall.order.dto.*;
import com.mall.order.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 *  ciggar
 * create-date: 2019/7/30-上午10:04
 */
@Slf4j
@Component
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    /**
     * 获取所有订单的处理流程
     * @param request
     * @return
     */
    @Override
    public OrderListResponse getOrderList(OrderListRequest request) {
        return null;
    }

    /**
     * 获取订单详情的处理流程
     * @param request
     * @return
     */
    @Override
    public OrderItemResponse getOrderItem(OrderItemRequest request) {
        return null;
    }
}
