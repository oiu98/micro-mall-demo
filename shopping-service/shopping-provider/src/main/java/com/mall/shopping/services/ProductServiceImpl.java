package com.mall.shopping.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mall.shopping.IProductService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.converter.ProductConverter;
import com.mall.shopping.dal.entitys.*;
import com.mall.shopping.dal.persistence.ItemDescMapper;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.*;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/25
 * <p>
 * Description:
 * 商品相关接口的实现
 */

@Slf4j
@Component
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ProductConverter productConverter;
    @Autowired
    private ItemDescMapper itemDescMapper;
    @Autowired
    private PanelContentMapper panelContentMapper;
    @Autowired
    private PanelMapper panelMapper;
    @Autowired
    private ContentConverter contentConverter;
    @Autowired
    private CacheManager cacheManager;

    /**
     * 获得商品详情
     */
    @Override
    public ProductDetailResponse getProductDetail(ProductDetailRequest productDetailRequest) {
        ProductDetailResponse productDetailResponse = new ProductDetailResponse();

        try {
            // 参数校验
            productDetailRequest.requestCheck();
            // 获取id
            Long id = productDetailRequest.getId();

            // 查询redis中是否有缓存
            String cache = cacheManager.checkCache(GlobalConstants.PRODUCT_ITEM_CACHE_KEY + id);
            if (cache != null) {
                ProductDetailDto productDetailDto = new ObjectMapper().readValue(cache, ProductDetailDto.class);
                productDetailResponse.setProductDetailDto(productDetailDto);
                productDetailResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
                productDetailResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            }

            // 查询商品
            id = productDetailRequest.getId();
            Item item = itemMapper.selectByPrimaryKey(id);
            ProductDetailDto productDetailDto = productConverter.item2DetailDto(item);
            productDetailDto.setProductImageSmall(Arrays.asList(item.getImages()));
            ItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(id);
            productDetailDto.setDetail(itemDesc.getItemDesc());

            // 存入redis
            cacheManager.setCache(GlobalConstants.PRODUCT_ITEM_CACHE_KEY + id,
                                    new ObjectMapper().writeValueAsString(productDetailDto),
                                    GlobalConstants.PRODUCT_ITEM_EXPIRE_TIME);

            // 封装
            productDetailResponse.setProductDetailDto(productDetailDto);
            productDetailResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            productDetailResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ProductServiceImpl.getProductDetail occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(productDetailResponse, e);
        }

        return productDetailResponse;
    }

    /**
     * 获得商品列表
     */
    @Override
    public AllProductResponse getAllProduct(AllProductRequest allProductRequest) {
        AllProductResponse allProductResponse = new AllProductResponse();

        try {
            // 参数校验
            allProductRequest.requestCheck();

            // 开启分页
            PageHelper.startPage(allProductRequest.getPage(),allProductRequest.getSize());
            // 排序
            String orderCol = "created";
            String orderDir = "desc";
            if ("1".equals(allProductRequest.getSort())) {
                orderCol = "price";
                orderDir = "asc";
            }
            if ("-1".equals(allProductRequest.getSort())) {
                orderCol = "price";
                orderDir = "desc";
            }

            // 查询商品
            List<Item> items =
                    itemMapper.selectItemFront(allProductRequest.getCid(),
                                                orderCol, orderDir,
                                                allProductRequest.getPriceGt(),
                                                allProductRequest.getPriceLte());
            List<ProductDto> productDtos = productConverter.items2Dto(items);
            allProductResponse.setProductDtoList(productDtos);
            // 获取查询总数
            PageInfo<Item> itemPageInfo = new PageInfo<>(items);
            allProductResponse.setTotal(itemPageInfo.getTotal());
            allProductResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            allProductResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ProductServiceImpl.getAllProduct occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(allProductResponse, e);
        }

        return allProductResponse;
    }

    /**
     *  获取推荐的商品板块
     */
    @Override
    public RecommendResponse getRecommendGoods() {
        RecommendResponse recommendResponse = new RecommendResponse();

        try {
            // 检查redis中是否有缓存
            String recommendCache = cacheManager.checkCache(GlobalConstants.RECOMMEND_PANEL_CACHE_KEY);
            if (recommendCache != null) {
                HashSet<PanelDto> panelDtosCache = new ObjectMapper().readValue(recommendCache, HashSet.class);
                recommendResponse.setPanelContentItemDtos(panelDtosCache);
                recommendResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
                recommendResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            }
            // 没有缓存，查表
            HashSet<PanelDto> panelDtos = new HashSet<>();
            Panel panel = panelMapper.selectByPrimaryKey(GlobalConstants.RECOMMEND_PANEL_ID);
            PanelDto panelDto = contentConverter.panel2Dto(panel);
            List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(GlobalConstants.RECOMMEND_PANEL_ID);
            List<PanelContentItemDto> panelContentItemDtos = contentConverter.panelContentItem2Dto(panelContentItems);
            panelDto.setPanelContentItems(panelContentItemDtos);
            panelDtos.add(panelDto);
            // 放入redis缓存
            cacheManager.setCache(GlobalConstants.RECOMMEND_PANEL_CACHE_KEY,
                                    new ObjectMapper().writeValueAsString(panelDtos),
                                    GlobalConstants.RECOMMEND_CACHE_EXPIRE);
            // 封装数据
            recommendResponse.setPanelContentItemDtos(panelDtos);
            recommendResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            recommendResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ProductServiceImpl.getRecommendGoods occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(recommendResponse, e);
        }

        return recommendResponse;
    }
}
