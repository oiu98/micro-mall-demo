package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IContentService;
import com.mall.shopping.IProductCateService;
import com.mall.shopping.IProductService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.*;
import com.mall.user.annotation.Anoymous;
import io.swagger.annotations.Api;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 目录相关的控制层,均允许匿名访问
 */

@RestController
@RequestMapping("/shopping")
@Anoymous
@Api(tags = "ContentController", description = "目录相关的控制层")
public class ContentController {

    @Reference(timeout = 3000, check = false)
    IContentService contentService;

    @Reference(timeout = 3000, check = false)
    IProductCateService productCateService;

    @Reference(timeout = 3000, check = false)
    IProductService productService;

    /**
     * 主页商品信息显示
     */
    @GetMapping("homepage")
    public ResponseData queryHomePage() {
        HomePageResponse homePageResponse = contentService.queryHomePage();
        if (!homePageResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(homePageResponse.getMsg());
        }
        return new ResponseUtil<>().setData(homePageResponse.getPanelContentItemDtos());
    }

    /**
     * 导航栏显示
     */
    @GetMapping("/navigation")
    public ResponseData queryNaviList() {
        NavListResponse navListResponse = contentService.queryNavList();
        if (!navListResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(navListResponse.getMsg());
        }
        return new ResponseUtil<>().setData(navListResponse.getPannelContentDtos());
    }

    /**
     * 列举所有商品种类
     * sort String 升序降序标记 ?
     */
    @GetMapping("/categories")
    public ResponseData queryCategories() {
        AllProductCateRequest allProductCateRequest = new AllProductCateRequest();
        allProductCateRequest.setSort("desc");  // 可以设置排序方式 asc、desc
        AllProductCateResponse allProductCateResponse = productCateService.getAllProductCate(allProductCateRequest);
        if (!allProductCateResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(allProductCateResponse.getMsg());
        }

        return new ResponseUtil<>().setData(allProductCateResponse.getProductCateDtoList());
    }

    /**
     * 查看商品详情
     */
    @GetMapping("/product/{id}")
    public ResponseData checkProductDetail(@PathVariable(value = "id") Long id) {
        ProductDetailRequest productDetailRequest = new ProductDetailRequest();
        productDetailRequest.setId(id);
        ProductDetailResponse productDetailResponse = productService.getProductDetail(productDetailRequest);
        if (!productDetailResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(productDetailResponse.getMsg());
        }

        return new ResponseUtil<>().setData(productDetailResponse.getProductDetailDto());
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/goods")
    public ResponseData getGoodsList(AllProductRequest allProductRequest) {
        AllProductResponse allProductResponse = productService.getAllProduct(allProductRequest);
        if (!allProductResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(allProductResponse.getMsg());
        }

        // 适配性处理
        Map<String, Object> map = new HashMap<>();
        map.put("data", allProductResponse.getProductDtoList());
        map.put("total", allProductResponse.getTotal());

        return new ResponseUtil<>().setData(map);
    }

    /**
     * 查询推荐商品
     */
    @GetMapping("/recommend")
    public ResponseData getRecommendGoods() {
        RecommendResponse recommendResponse = productService.getRecommendGoods();
        if (!recommendResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())){
            return new ResponseUtil<>().setErrorMsg(recommendResponse.getMsg());
        }

        return new ResponseUtil<>().setData(recommendResponse.getPanelContentItemDtos());
    }

}
