package com.mall.pay.constants;

import com.mall.pay.IEnum;


public enum PayResultEnum implements IEnum {

    PAY_PROCESSING("1","支付处理中"),
    PAY_SUCCESS("2","支付成功"),
    PAY_FAIL("3","支付失败");


    private String code;

    private String desc;

    PayResultEnum(String code, String desc){
        this.code=code;
        this.desc=desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
