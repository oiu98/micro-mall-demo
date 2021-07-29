package com.mall.promo.service;
import com.mall.commons.lock.DistributedLockException;
import com.mall.order.OrderPromoService;
import com.mall.order.dto.CreateSeckillOrderRequest;
import com.mall.order.dto.CreateSeckillOrderResponse;
import com.mall.promo.PromoService;
import com.mall.promo.cache.CacheManager;
import com.mall.promo.constant.PromoRetCode;
import com.mall.promo.converter.PromoInfoConverter;
import com.mall.promo.converter.PromoProductConverter;
import com.mall.promo.dal.entitys.PromoItem;
import com.mall.promo.dal.entitys.PromoSession;
import com.mall.promo.dal.persistence.PromoItemMapper;
import com.mall.promo.dal.persistence.PromoSessionMapper;
import com.mall.promo.dto.*;
import com.mall.promo.mq.PromoOrderProducer;
import com.mall.shopping.IProductService;
import com.mall.shopping.dto.ProductDetailDto;
import com.mall.shopping.dto.ProductDetailRequest;
import com.mall.shopping.dto.ProductDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 **/
@Service
@Slf4j
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoSessionMapper sessionMapper;

    @Autowired
    PromoItemMapper promoItemMapper;

    @Reference(check = false)
    IProductService productService;

    @Autowired
    PromoInfoConverter promoInfoConverter;

    @Reference(check = false)
    OrderPromoService orderPromoService;
    @Autowired
    PromoProductConverter promoProductConverter;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    PromoOrderProducer promoOrderProducer;

    @Override
    public PromoInfoResponse getPromoList(PromoInfoRequest request) {
        PromoInfoResponse response = new PromoInfoResponse();

        try {
            request.requestCheck();

            log.info("start getPromoList sessionId = " + request.getSessionId() + ", yyyymmdd " + request.getYyyymmdd());
            //查询场次信息
            Example promoSessionExample = new Example(PromoSession.class);
            promoSessionExample.createCriteria()
                    .andEqualTo("sessionId",request.getSessionId())
                    .andEqualTo("yyyymmdd",request.getYyyymmdd());
            List<PromoSession> sessionList = sessionMapper.selectByExample(promoSessionExample);

            if (CollectionUtils.isEmpty(sessionList)) {
                // 如果没找到秒杀场次
                response.setCode(PromoRetCode.PROMO_NOT_EXIST.getCode());
                response.setMsg(PromoRetCode.PROMO_NOT_EXIST.getMessage());
                return response;
            }

            // 获取某天中唯一的秒杀场次
            PromoSession promoSession = sessionList.get(0);

            Example promoItemExample = new Example(PromoItem.class);
            promoItemExample.createCriteria().andEqualTo("psId", promoSession.getId());
            List<PromoItem> promoItems = promoItemMapper.selectByExample(promoItemExample);

            List<PromoItemInfoDto> productList = new ArrayList<>();

            // 对该秒杀场次的每一个商品，查找其商品详情
            promoItems.stream().forEach(promoItem -> {
                Long itemId = promoItem.getItemId();

                ProductDetailRequest productDetailRequest = new ProductDetailRequest();
                productDetailRequest.setId(itemId);
                ProductDetailResponse productDetail = productService.getProductDetail(productDetailRequest);
                ProductDetailDto productDetailDto = productDetail.getProductDetailDto();
                PromoItemInfoDto promoItemInfoDto = promoInfoConverter.convert2InfoDto(productDetailDto);
                promoItemInfoDto.setInventory(promoItem.getItemStock());
                promoItemInfoDto.setSeckillPrice(promoItem.getSeckillPrice());
                productList.add(promoItemInfoDto);

            });

            //组装参数
            response.setPsId(promoSession.getId());
            response.setSessionId(request.getSessionId());
            response.setCode(PromoRetCode.SUCCESS.getCode());
            response.setMsg(PromoRetCode.SUCCESS.getMessage());
            response.setProductList(productList);
        } catch (Exception e) {
            response.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
            response.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
        }
        return response;
    }


    /*
            1. 库存扣减的问题
               在数据库中，扣减库存的关键代码 SET item_stock = item_stock - 1
               它的执行，其实它是分成了3步来完成，并不是一个不可分割的过程
               a. 读取 item_stock
               b. item_stock - 1
               c. 将b的计算结果，赋值item_stock

               所以，当我们启动了多个秒杀服务，同时多个用户请求多个不同的秒杀服务的时候，
               此时就有可能出现，商品的超卖问题

               如何解决这个问题呢？  加锁
               但是要注意
                a. 如果想要通过加锁的方式，实现对秒杀下单的并发请求，实现同步，此时，必须保证，使用的是同一把锁
                b. 那么这把锁，既不能是服务1进程中的锁对象，也不能是服务2进程中的锁对象，它只能是独立于服务进程的一把分布式锁


             2.  秒杀下单功能，它的结果应该只有两个，要么下单成功，要么下单成功，这就要求(⽣成订单、扣减库存、⽣成商品的邮寄
                 信息), 必须作为一个整体，要么都执行成功，要失败都失败，也就是说我们希望秒杀下单的功能，作为一个事物

                 要让它们作为一个事物，它的一本原理
                  1. 获取数据库连接connection
                 2.  connection.setAutoCommit(false) 开启事物
                     执行对数据库的操作
                 3.  提交事物或者回滚

                 但是，很遗憾，我们的秒杀服务和订单服务，它们在访问数据库的时候，都通过自己各自独立的数据库连接，向数据库发送
                 sql语句，这样一来，这些sql语句，很显然就不是在一个数据库连接中，提交的

                 所以，transactionl会失效，这样一来，即使对于秒杀下单的方法，加了transactional注解，
                 它也不会让秒杀下单的数据库操作，变成一个事物

                 如何让秒杀下单，对数据库的操作，变成一个事物呢？ ——> 分布式式事物
     */
    @Override
    @Transactional
    public CreatePromoOrderResponse createPromoOrder(CreatePromoOrderRequest request) {
        CreatePromoOrderResponse createPromoOrderResponse = new CreatePromoOrderResponse();
        createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());
        List<PromoItem> promoItems = null;
        try {
            request.requestCheck();
            int effectiveRow = promoItemMapper.decreaseStock(request.getProductId(), request.getPsId());
            if (effectiveRow < 1) {
                createPromoOrderResponse.setCode(PromoRetCode.PROMO_ITEM_STOCK_NOT_ENOUGH.getCode());
                createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ITEM_STOCK_NOT_ENOUGH.getMessage());
                return createPromoOrderResponse;
            }

            //获取商品的秒杀价格
            Example example = new Example(PromoItem.class);
            example.createCriteria()
                    .andEqualTo("psId",request.getPsId())
                    .andEqualTo("itemId",request.getProductId());
            promoItems = promoItemMapper.selectByExample(example);
        } catch (Exception e) {
            log.error("PromoServiceImpl.createPromoOrder occurs error" + e);
            createPromoOrderResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
            return createPromoOrderResponse;
        }
        if (CollectionUtils.isEmpty(promoItems)) {
            createPromoOrderResponse.setCode(PromoRetCode.PROMO_ITEM_NOT_EXIST.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.PROMO_ITEM_NOT_EXIST.getMessage());
            return createPromoOrderResponse;
        }

        PromoItem promoItem = promoItems.get(0);

        // 生成订单
        CreateSeckillOrderRequest createSeckillOrderRequest = new  CreateSeckillOrderRequest();
        createSeckillOrderRequest.setUsername(request.getUsername());
        createSeckillOrderRequest.setUserId(request.getUserId());
        createSeckillOrderRequest.setProductId(request.getProductId());
        createSeckillOrderRequest.setPrice(promoItem.getSeckillPrice());
        CreateSeckillOrderResponse createSeckillOrderResponse
                = orderPromoService.createPromoOrder(createSeckillOrderRequest);

        if (!createSeckillOrderResponse.getCode().equals(PromoRetCode.SUCCESS.getCode())) {
            createPromoOrderResponse.setCode(createSeckillOrderResponse.getCode());
            createPromoOrderResponse.setMsg(createSeckillOrderResponse.getMsg());
            return createPromoOrderResponse;
        }

        createPromoOrderResponse.setInventory(promoItem.getItemStock());
        createPromoOrderResponse.setProductId(promoItem.getItemId());
        createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());

        return createPromoOrderResponse;
    }

    @Override
    @Transactional
    public CreatePromoOrderResponse createPromoOrderInTransaction(CreatePromoOrderRequest request) throws DistributedLockException {
        // 秒杀下单接口中，上游服务秒杀服务，订单服务是下游服务

        // 1. 开启一个分布式事物，即发送一个事物消息， 在实现消息发送者代码时，设置监听器，实现本地事务核心功能，扣减库存
        CreatePromoOrderResponse createPromoOrderResponse = new CreatePromoOrderResponse();

        // 构建发起下单请求的参数
        //获取商品的秒杀价格
        Example example = new Example(PromoItem.class);
        example.createCriteria()
                .andEqualTo("psId",request.getPsId())
                .andEqualTo("itemId",request.getProductId());
        List<PromoItem> promoItems = promoItemMapper.selectByExample(example);
        PromoItem promoItem = promoItems.get(0);

        CreateSeckillOrderRequest createSeckillOrderRequest = new  CreateSeckillOrderRequest();
        createSeckillOrderRequest.setUsername(request.getUsername());
        createSeckillOrderRequest.setUserId(request.getUserId());
        createSeckillOrderRequest.setProductId(request.getProductId());
        createSeckillOrderRequest.setPrice(promoItem.getSeckillPrice());
        createSeckillOrderRequest.setAddressId(request.getAddressId());
        createSeckillOrderRequest.setStreetName(request.getStreetName());
        createSeckillOrderRequest.setTel(request.getTel());

        // 发送消息(事物消息)：CreateSeckillOrderRequest对象即请求订单服务下单，所需要的请求参数
        boolean isSuccess
                = promoOrderProducer.sendPromoOrderMessage(createSeckillOrderRequest, request.getPsId(), request.getProductId());

        if (isSuccess) {
            createPromoOrderResponse.setCode(PromoRetCode.SUCCESS.getCode());
            createPromoOrderResponse.setMsg(PromoRetCode.SUCCESS.getMessage());
            return createPromoOrderResponse;
        }

        createPromoOrderResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
        createPromoOrderResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
        return createPromoOrderResponse;
    }

    @Override
    public PromoProductDetailResponse getPromoProductDetail(PromoProductDetailRequest request) {

        PromoProductDetailResponse promoProductDetailResponse = new PromoProductDetailResponse();

        Example example = new Example(PromoItem.class);
        example.createCriteria()
                .andEqualTo("psId", request.getPsId())
                .andEqualTo("itemId", request.getProductId());


        List<PromoItem> promoItems = null;
        try {
            request.requestCheck();
            promoItems = promoItemMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(promoItems)) {
                promoProductDetailResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
                promoProductDetailResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
                return promoProductDetailResponse;
            }

            // 秒杀商品条目
            PromoItem promoItem = promoItems.get(0);

            ProductDetailRequest productDetailRequest = new ProductDetailRequest();
            productDetailRequest.setId(request.getProductId());
            ProductDetailResponse productDetailResponse = productService.getProductDetail(productDetailRequest);
            if (!PromoRetCode.SUCCESS.getCode().equals(productDetailResponse.getCode())) {
                promoProductDetailResponse.setCode(productDetailResponse.getCode());
                promoProductDetailResponse.setMsg(productDetailResponse.getMsg());
                return promoProductDetailResponse;
            }
            PromoProductDetailDTO promoProductDetailDTO
                    = promoProductConverter.convert2DetailDTO(promoItem, productDetailResponse.getProductDetailDto());

            promoProductDetailResponse.setPromoProductDetailDTO(promoProductDetailDTO);
            promoProductDetailResponse.setCode(PromoRetCode.SUCCESS.getCode());
            promoProductDetailResponse.setMsg(PromoRetCode.SUCCESS.getMessage());
            return promoProductDetailResponse;
        } catch (Exception e) {
            log.error("PromoServiceImpl.getPromoProductDetail occurs error");
            promoProductDetailResponse.setCode(PromoRetCode.SYSTEM_ERROR.getCode());
            promoProductDetailResponse.setMsg(PromoRetCode.SYSTEM_ERROR.getMessage());
            return promoProductDetailResponse;
        }


    }
}