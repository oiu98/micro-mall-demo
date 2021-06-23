package com.mall.order.biz.handler;

import com.alibaba.fastjson.JSON;
import com.mall.commons.tool.exception.BizException;
import com.mall.order.biz.context.AbsTransHandlerContext;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.context.TransHandlerContext;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dal.entitys.Stock;
import com.mall.order.dal.persistence.OrderItemMapper;
import com.mall.order.dal.persistence.StockMapper;
import com.mall.order.dto.CartProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: 扣减库存处理器
 * @Author： wz
 * @Date: 2019-09-16 00:03
 **/
@Component
@Slf4j
public class SubStockHandler extends AbstractTransHandler {

    @Autowired
    private StockMapper stockMapper;

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@Transactional
	public boolean handle(TransHandlerContext context) {
		// 向下转型
		CreateOrderContext createOrderContext = (CreateOrderContext) context;

		// 获得订单商品列表
		List<CartProductDto> cartProductDtoList = createOrderContext.getCartProductDtoList();

		// 获得订单商品id列表
		List<Long> buyProductIds = createOrderContext.getBuyProductIds();
		if (CollectionUtils.isEmpty(buyProductIds)) { // 如果为空，手动生成
			buyProductIds = cartProductDtoList.stream()
							.map(productDto -> productDto.getProductId())
							.collect(Collectors.toList());
		}
		buyProductIds.sort(Long::compareTo); // 排序
		createOrderContext.setBuyProductIds(buyProductIds); // productIds放入context

		// 锁定库存
		List<Stock> stockList = stockMapper.findStocksForUpdate(buyProductIds);
		if (CollectionUtils.isEmpty(stockList)) {
			throw new BizException("库存未初始化！");
		}

		if (stockList.size() != buyProductIds.size()) {
			throw new BizException("部分商品库存未初始化！");
		}

		// 冻结库存
		cartProductDtoList.stream().forEach(cartProductDto -> {
			// 获得参数
			Long productId = cartProductDto.getProductId();
			Long productNum = cartProductDto.getProductNum();
			Long limitNum = cartProductDto.getLimitNum();
			if (limitNum < productNum) { // 购买数量超过限制
				throw new BizException("购买数量超过限制！");
			}
			// 冻结库存
			Stock stock = new Stock();
			stock.setItemId(productId);
			stock.setStockCount(-productNum);
			stock.setLockCount(productNum.intValue());
			Integer effectedRows = stockMapper.updateStock(stock);
			if (effectedRows == 0) { // 库存不足
				throw new BizException("库存不足！");
			}
		});

		return true;
	}
}