package com.mall.order.services;

import com.mall.order.OrderPromoService;
import com.mall.order.biz.context.CreateOrderContext;
import com.mall.order.biz.handler.InitOrderHandler;
import com.mall.order.biz.handler.LogisticalHandler;
import com.mall.order.constant.OrderRetCode;
import com.mall.order.dto.CartProductDto;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.shopping.IProductService;
import com.mall.shopping.dto.ProductDetailDto;
import com.mall.shopping.dto.ProductDetailRequest;
import com.mall.shopping.dto.ProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class OrderPromoServiceImpl implements OrderPromoService {

    @Reference(check = false)
    IProductService productService;

    @Autowired
    InitOrderHandler initOrderHandler;

    @Autowired
    LogisticalHandler logisticalHandler;

    @Override
    public CreateSeckillOrderResponse createPromoOrder(CreateSeckillOrderRequest request) {

        CreateSeckillOrderResponse createSeckillOrderResponse = new CreateSeckillOrderResponse();
        createSeckillOrderResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
        createSeckillOrderResponse.setCode(OrderRetCode.SUCCESS.getCode());

        request.requestCheck();

        // 创建订单逻辑
        CreateOrderContext createOrderContext = new CreateOrderContext();

        // 1.填充必要参数，生成订单
        inflateOrderParam(request, createOrderContext);
        boolean isSuccess = initOrderHandler.handle(createOrderContext);

        // 2.物流信息
        createOrderContext.setTel(request.getTel());
        createOrderContext.setStreetName(request.getStreetName());
        logisticalHandler.handle(createOrderContext);

        log.info("秒杀订单，物流信息插入数据库成功! " + isSuccess);
        if (isSuccess) {
            createSeckillOrderResponse.setCode(OrderRetCode.SUCCESS.getCode());
            createSeckillOrderResponse.setMsg(OrderRetCode.SUCCESS.getMessage());
            return createSeckillOrderResponse;
        }
        createSeckillOrderResponse.setCode(OrderRetCode.INIT_ORDER_EXCEPTION.getCode());
        createSeckillOrderResponse.setMsg(OrderRetCode.INIT_ORDER_EXCEPTION.getMessage());
        return createSeckillOrderResponse;
    }

    private void inflateOrderParam(CreateSeckillOrderRequest request, CreateOrderContext createOrderContext) {
        List<CartProductDto> list = getCarProductList(request);

        // 2.构造下单参数
        createOrderContext.setUserId(request.getUserId());
        createOrderContext.setBuyerNickName(request.getUsername());
        createOrderContext.setAddressId(request.getAddressId());
        createOrderContext.setCartProductDtoList(list);
        createOrderContext.setOrderTotal(request.getPrice());
    }

    private List<CartProductDto> getCarProductList(CreateSeckillOrderRequest request) {
        ProductDetailDto productDetail = getProductDetail(request);

        // 2.构建购物车列表
        CartProductDto cartProductDto = new CartProductDto();
        cartProductDto.setProductId(request.getProductId());
        cartProductDto.setProductImg(productDetail.getProductImageBig());
        // 秒杀商品一次只能买一个
        cartProductDto.setProductNum(1L);
        cartProductDto.setProductName(productDetail.getProductName());
        cartProductDto.setSalePrice(request.getPrice());

        ArrayList<CartProductDto> cartProductDtos = new ArrayList<>();
        cartProductDtos.add(cartProductDto);
        return cartProductDtos;
    }

    private ProductDetailDto getProductDetail(CreateSeckillOrderRequest request) {
        ProductDetailRequest productDetailRequest = new ProductDetailRequest();
        productDetailRequest.setId(request.getProductId());
        ProductDetailResponse productDetail = productService.getProductDetail(productDetailRequest);
        return productDetail.getProductDetailDto();
    }
}
