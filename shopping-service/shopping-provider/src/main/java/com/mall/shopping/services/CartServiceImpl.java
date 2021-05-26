package com.mall.shopping.services;

import com.mall.commons.tool.exception.ProcessException;
import com.mall.shopping.ICartService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.CartItemConverter;
import com.mall.shopping.dal.entitys.Item;
import com.mall.shopping.dal.entitys.Stock;
import com.mall.shopping.dal.persistence.ItemMapper;
import com.mall.shopping.dal.persistence.StockMapper;
import com.mall.shopping.dto.*;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.services.cache.CartManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/22
 * <p>
 * Description: 购物车相关
 */

@Slf4j
@Component
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartManager cartManager;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private StockMapper stockMapper;

    /**
     *  获取购物车列表接口
     */
    @Override
    public CartListByIdResponse getCartListById(CartListByIdRequest request) {

        CartListByIdResponse response = new CartListByIdResponse();

        try {
            // 获得 userId
            Long userId = request.getUserId();

            // 从redis中获取carts
            List<CartProductDto> carts = cartManager.getCarts(userId);

            // 封装
            response.setCartProductDtos(carts);
            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("CartServiceImpl.getCartListById occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 添加购物车接口
     * @param request
     * @return
     */
    @Override
    public AddCartResponse addToCart(AddCartRequest request) {
        AddCartResponse response = new AddCartResponse();

        try {
            // 参数校验
            request.requestCheck();

            // 获取参数
            Long userId = request.getUserId();
            Long itemId = request.getItemId();
            Integer num = request.getNum();

            // 构造购物车数据单元 CartProductDto
            Stock stock = stockMapper.selectStock(itemId);
            if (stock.getStockCount() < num) { // 库存不足
                throw new ProcessException(ShoppingRetCode.STOCK_SHORTAGE.getCode(),
                                            ShoppingRetCode.STOCK_SHORTAGE.getMessage());
            }
            Item item = itemMapper.selectByPrimaryKey(itemId);
            CartProductDto cartProductDto = CartItemConverter.item2Dto(item);
            cartProductDto.setProductNum(Long.valueOf(num));
            cartProductDto.setChecked("true");
            // 保存进redis中
            cartManager.addCartProduct(userId,cartProductDto);

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("CartServiceImpl.addToCart occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 更新购物车接口
     * @param request
     * @return
     */
    @Override
    public UpdateCartNumResponse updateCartNum(UpdateCartNumRequest request) {
        UpdateCartNumResponse response = new UpdateCartNumResponse();

        try {
            // 参数校验
            request.requestCheck();

            // 获取参数
            Long userId = request.getUserId();
            Long itemId = request.getItemId();
            Integer num = request.getNum();

            Stock stock = stockMapper.selectStock(itemId);
            if (stock.getStockCount() < num) { // 库存不足
                throw new ProcessException(ShoppingRetCode.STOCK_SHORTAGE.getCode(),
                        ShoppingRetCode.STOCK_SHORTAGE.getMessage());
            }
            // 更新redis中
            cartManager.updateCartProduct(userId,request);

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("CartServiceImpl.updateCartNum occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 选中购物车所有商品接口
     * @param request
     * @return
     */
    @Override
    public CheckAllItemResponse checkAllCartItem(CheckAllItemRequest request) {
        return null;
    }

    /**
     * 删除购物车商品接口
     * @param request
     * @return
     */
    @Override
    public DeleteCartItemResponse deleteCartItem(DeleteCartItemRequest request) {
        DeleteCartItemResponse response = new DeleteCartItemResponse();

        try {
            // 参数校验
            request.requestCheck();

            // 获取参数
            Long userId = request.getUserId();
            Long itemId = request.getItemId();

            // 更新redis中
            cartManager.deleteCartProduct(userId,itemId);

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("CartServiceImpl.deleteCartItem occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 删除购物车选中商品接口
     * @param request
     * @return
     */
    @Override
    public DeleteCheckedItemResposne deleteCheckedItem(DeleteCheckedItemRequest request) {
        DeleteCheckedItemResposne response = new DeleteCheckedItemResposne();

        try {
            // 参数校验
            request.requestCheck();

            // 获取参数
            Long userId = request.getUserId();

            // 更新redis中
            cartManager.deleteCheckedCartProduct(userId);

            response.setCode(ShoppingRetCode.SUCCESS.getCode());
            response.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("CartServiceImpl.deleteCheckedItem occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(response, e);
        }

        return response;
    }

    /**
     * 清空指定用户的购物车缓存(用户下完订单之后清理）
     * @param request
     * @return
     */
    @Override
    public ClearCartItemResponse clearCartItemByUserID(ClearCartItemRequest request) {
        return null;
    }
}
