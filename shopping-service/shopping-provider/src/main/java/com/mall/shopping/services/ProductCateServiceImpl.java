package com.mall.shopping.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ProductCateConverter;
import com.mall.shopping.converter.ProductConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.AllProductCateResponse;
import com.mall.shopping.dto.ProductCateDto;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/25
 * <p>
 * Description:
 * 商品分类相关的接口
 */

@Slf4j
@Service
@Component
public class ProductCateServiceImpl implements IProductCateService {

    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private ProductCateConverter productCateConverter;
    @Autowired
    private CacheManager cacheManager;

    /**
     *  获得所有商品的分类
     */
    @Override
    public AllProductCateResponse getAllProductCate(AllProductCateRequest allProductCateRequest) {
        AllProductCateResponse allProductCateResponse = new AllProductCateResponse();

        try {
            // 参数校验
            allProductCateRequest.requestCheck();

            ObjectMapper objectMapper = new ObjectMapper();
            // 判断redis中是否存在缓存
            String cache = cacheManager.checkCache(GlobalConstants.PRODUCT_CATE_CACHE_KEY);
            if (cache != null) {
                List<ProductCateDto> productCateDtos = objectMapper.readValue(cache,List.class);
                allProductCateResponse.setProductCateDtoList(productCateDtos);
                allProductCateResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
                allProductCateResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            }

            // 获取类目
            List<ItemCat> itemCats = itemCatMapper.selectAll();
            List<ProductCateDto> productCateDtos = productCateConverter.items2Dto(itemCats);

            // 存入 redis 中
            cacheManager.setCache(GlobalConstants.PRODUCT_CATE_CACHE_KEY,
                                    objectMapper.writeValueAsString(productCateDtos),
                                    GlobalConstants.PRODUCT_CATE_EXPIRE_TIME);

            // 放入 response 中
            allProductCateResponse.setProductCateDtoList(productCateDtos);
            allProductCateResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            allProductCateResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ProductCateServiceImpl.getAllProductCate occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(allProductCateResponse, e);
        }

        return allProductCateResponse;
    }
}
