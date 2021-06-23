package com.mall.pay;

import com.mall.pay.dto.PaymentRequest;
import com.mall.pay.dto.alipay.AlipayQueryRetResponse;
import com.mall.pay.dto.alipay.AlipaymentResponse;

/**
 * 支付操作相关的服务
 */
public interface PayCoreService {


    /**
     * 支付宝支获取支付二维码
     * @param request
     * @return
     */
    AlipaymentResponse aliPay(PaymentRequest request);

    /**
     * 获取支付宝支付结果
     * @param request
     * @return
     */
    AlipayQueryRetResponse queryAlipayRet(PaymentRequest request);


}
