package com.mall.pay.services;

import  com.mall.commons.tool.exception.ProcessException;
import com.mall.order.OrderCoreService;
import com.mall.order.OrderQueryService;
import com.mall.pay.PayCoreService;
import com.mall.pay.constants.PayResultEnum;
import com.mall.pay.constants.PayReturnCodeEnum;
import com.mall.pay.dal.entitys.Payment;
import com.mall.pay.dal.persistence.PaymentMapper;
import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.alipay.AlipayQueryRetResponse;
import com.mall.pay.dto.alipay.AlipaymentResponse;
import com.mall.pay.utils.ExceptionProcessorUtils;
import com.mall.shopping.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
@Component
@Slf4j
public class PayCoreServiceImpl implements PayCoreService {

    @Autowired
    PayHelper payHelper;
    @Autowired
    PaymentMapper paymentMapper;
    @Reference(timeout = 3000,check = false)
    OrderCoreService orderCoreService;

    @Override
    public AlipaymentResponse aliPay(PaymentRequest request) {
        AlipaymentResponse alipaymentResponse = new AlipaymentResponse();

        try {
            // 1.  使用支付宝sdk的功能去请求，支付二维码
            String qrFileName = payHelper.test_trade_precreate(request);

            if (qrFileName == null) {
                // 请求支付二维码失败
                throw new ProcessException(PayReturnCodeEnum.GET_CODE_FALIED.getCode(),
                        PayReturnCodeEnum.GET_CODE_FALIED.getMsg());
            } else {
                // 否则，请求到支付二维码

                // 2.  在本地插入tb_payment，插入一条本次订单的一条支付记录
                Payment payment = new Payment();
                payment.setOrderId(request.getTradeNo());
                payment.setStatus(PayResultEnum.PAY_PROCESSING.getCode()); // 支付处理中
                payment.setCreateTime(new Date());
                payment.setUpdateTime(new Date());
                payment.setProductName(request.getSubject());
                payment.setOrderAmount(request.getOrderFee());
                payment.setPayerUid(request.getUserId());
                payment.setPayerAmount(request.getTotalFee());
                payment.setPayWay(request.getPayChannel());

                int affectedRows = paymentMapper.insert(payment); // 可以做异常处理

                alipaymentResponse.setQrCode(qrFileName);
                alipaymentResponse.setCode(PayReturnCodeEnum.SUCCESS.getCode());
                alipaymentResponse.setMsg(PayReturnCodeEnum.SUCCESS.getMsg());
            }
        } catch (Exception e) {
            log.error("error occur in PayCoreServiceImpl.aliPay" + e);
            ExceptionProcessorUtils.wrapperHandlerException(alipaymentResponse, e);
        }

        return alipaymentResponse;
    }

    @Override
    public AlipayQueryRetResponse queryAlipayRet(PaymentRequest request) {

        AlipayQueryRetResponse alipayQueryRetResponse = new AlipayQueryRetResponse();

        boolean isSuccess = payHelper.test_trade_query(request);

        String orderId = request.getTradeNo();
        if (isSuccess) {
            // 支付成功
            // 1. 修改本订单对应的支付记录： 修改为支付成功
            Example example = new Example(Payment.class);
            example.createCriteria().andEqualTo("order_id",request.getTradeNo());
            Payment payment = new Payment();
            payment.setStatus(PayResultEnum.PAY_SUCCESS.getCode());
            int affectedRows = paymentMapper.updateByExample(payment, example);
            // 2. 修改订单状态，为已支付状态
            boolean result = orderCoreService.updatePaymentStatus(orderId,isSuccess);
            // 3. 修改库存，将订单中商品对应的库存，锁定库存清零
            orderCoreService.updateStock(orderId,isSuccess);
            // 成功扣减库存

            alipayQueryRetResponse.setCode(PayReturnCodeEnum.SUCCESS.getCode());
            alipayQueryRetResponse.setMsg(PayReturnCodeEnum.SUCCESS.getMsg());
        } else {
            // 没有成功支付
            // 修改本订单对应的支付记录： 修改为支付失败
            orderCoreService.updatePaymentStatus(orderId,isSuccess);
            alipayQueryRetResponse.setCode(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getCode());
            alipayQueryRetResponse.setMsg(PayReturnCodeEnum.PAYMENT_PROCESSOR_FAILED.getMsg());
        }

        return alipayQueryRetResponse;
    }
}
