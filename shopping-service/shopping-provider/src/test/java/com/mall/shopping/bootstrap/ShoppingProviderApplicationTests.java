package com.mall.shopping.bootstrap;

import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.dto.AddCartRequest;
import com.mall.shopping.dto.AllProductCateRequest;
import com.mall.shopping.dto.NavListResponse;
import com.mall.shopping.dto.ProductDetailRequest;
import com.mall.shopping.*;
import com.mall.shopping.services.cache.CacheManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShoppingProviderApplicationTests {

//    @Autowired
//    private ICartService cartService;


//    @Test
//    public void testCartService() throws IOException {
//        AddCartRequest request = new AddCartRequest();
//        request.setItemId(100023501L);
//        request.setUserId(123L);
//        cartService.addToCart(request);
//        System.in.read();
//    }

    @Autowired
    private IContentService contentService;

    @Test
    public void testContentService() throws IOException {
        NavListResponse navListResponse = contentService.queryNavList();
        System.in.read();
    }


    @Autowired
    CacheManager cacheManager;

    @Test
    public void testCache() {
        String NavListCache = cacheManager.checkCache(GlobalConstants.HEADER_PANEL_CACHE_KEY);
        System.out.println(NavListCache);
    }

//    @Autowired
//    private IHomeService homeService;
//    @Test
//    public void testHomeService() throws IOException {
//        homeService.homepage();
//        System.in.read();
//    }
//
//    @Autowired
//    private IProductCateService productCateService;
//
//    @Test
//    public void testProductCateService() throws IOException {
//        AllProductCateRequest request = new AllProductCateRequest();
//        request.setSort("1");
//        productCateService.getAllProductCate(request);
//        System.in.read();
//    }
//
//    @Autowired
//    private IProductService productService;
//
//    @Test
//    public void testProductService() throws IOException {
//        ProductDetailRequest productDetailRequest = new ProductDetailRequest();
//        productDetailRequest.setId(100023501L);
//        productService.getProductDetail(productDetailRequest);
//
//        // ----------------------------------------------------------------------------
//
////        productService.getRecommendGoods();
//
//        System.in.read();
//    }
}
