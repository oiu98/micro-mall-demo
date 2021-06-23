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
import java.util.HashMap;
import java.util.List;

/**
 * ciggar
 * create-date: 2019/7/30-上午10:04
 */
@Slf4j
@Component
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderShippingMapper orderShippingMapper;
    @Autowired
    private OrderConverter orderConverter;

    /**
     * 获取所有订单的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public OrderListResponse getOrderList(OrderListRequest request) {
        OrderListResponse response = new OrderListResponse();

        try {
            // 参数校验
            request.requestCheck();

            // 获得参数
            Long userId = request.getUserId();
            Integer size = request.getSize();
            String sort = request.getSort();
            Integer page = request.getPage();

            // 分页
            PageHelper.startPage(page, size);
            // 查询
            Example example = new Example(Order.class);
            example.createCriteria().andEqualTo("userId", userId);
            if (sort != null) {
                example.setOrderByClause("update_time " + sort);
            } else {
                example.setOrderByClause("update_time desc");
            }
            List<Order> orders = orderMapper.selectByExample(example);
            List<OrderDetailInfo> orderDetailInfos = orderConverter.order2details(orders);
            orderDetailInfos.parallelStream().forEach(orderDetailInfo -> {
                // 获得orderShippingDto
                OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(orderDetailInfo.getOrderId());
                OrderShippingDto orderShippingDto = orderConverter.shipping2dto(orderShipping);
                orderDetailInfo.setOrderShippingDto(orderShippingDto);

                // 获得订单商品信息
                List<OrderItem> orderItems = orderItemMapper.queryByOrderId(orderDetailInfo.getOrderId());
                List<OrderItemDto> orderItemDtos = orderConverter.item2dto(orderItems);
                orderDetailInfo.setOrderItemDto(orderItemDtos);
            });

            // 获得total信息
            PageInfo<Order> orderPageInfo = new PageInfo<>(orders);
            long total = orderPageInfo.getTotal();

            // 封装
            response.setDetailInfoList(orderDetailInfos);
            response.setTotal(total);
            response.setCode(OrderRetCode.SUCCESS.getCode());
            response.setMsg(OrderRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("OrderQueryServiceImpl.getOrderList occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 获取订单详情的处理流程
     *
     * @param request
     * @return
     */
    @Override
    public OrderDetailResponse getOrderDetail(OrderDetailRequest request) {
        OrderDetailResponse response = new OrderDetailResponse();

        try {
            // 参数校验
            request.requestCheck();

            // 获取参数
            String orderId = request.getOrderId();
            // 获取订单
            Order order = orderMapper.selectByPrimaryKey(orderId);
            response = orderConverter.order2res(order);
            // 获得orderShippingDto
            OrderShipping orderShipping = orderShippingMapper.selectByPrimaryKey(orderId);
            OrderShippingDto orderShippingDto = orderConverter.shipping2dto(orderShipping);
            response.setOrderShippingDto(orderShippingDto);

            // 获得订单商品信息
            List<OrderItem> orderItems = orderItemMapper.queryByOrderId(orderId);
            List<OrderItemDto> orderItemDtos = orderConverter.item2dto(orderItems);
            response.setOrderItemDto(orderItemDtos);

            // 封装
            response.setCode(OrderRetCode.SUCCESS.getCode());
            response.setMsg(OrderRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("OrderQueryServiceImpl.getOrderDetail occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }
}
