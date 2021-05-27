package com.mall.order.biz.handler;

import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.shopping.ICartService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.ClearCartItemRequest;
import com.mall.shopping.dto.ClearCartItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  ciggar
 * create-date: 2019/8/1-下午5:05
 * 将购物车中的缓存失效
 */
@Slf4j
@Component
public class ClearCartItemHandler extends AbstractTransHandler {

    @Reference(timeout = 3000,check = false)
    private ICartService cartService;


    //是否采用异步方式执行
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean handle(TransHandlerContext context) {
        // 向下转型
        CreateOrderContext createOrderContext = (CreateOrderContext) context;

        ClearCartItemRequest request = new ClearCartItemRequest();
        request.setUserId(createOrderContext.getUserId());
        request.setProductIds(createOrderContext.getBuyProductIds());
        ClearCartItemResponse clearCartItemResponse = cartService.clearCartItemByUserID(request);
        if (!clearCartItemResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            throw new BizException(clearCartItemResponse.getCode(),clearCartItemResponse.getMsg());
        }

        return true;
    }
}
