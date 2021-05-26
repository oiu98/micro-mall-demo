package com.mall.shopping.dal.persistence;

import com.mall.commons.tool.tkmapper.TkMapper;
import com.mall.shopping.dal.entitys.Stock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Author： ciggar
 * @Date: 2019-09-16 00:09
 **/
public interface StockMapper extends TkMapper<Stock> {
 void updateStock(Stock stock);
 Stock selectStockForUpdate(Long itemId);
 Stock selectStock(Long itemId);

 List<Stock> findStocksForUpdate(@Param("itemIds")List<Long> itemIds);
}