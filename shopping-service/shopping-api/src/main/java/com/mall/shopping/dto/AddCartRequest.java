package com.mall.shopping.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mall.commons.result.AbstractRequest;
import lombok.Data;

/**
 * Created by ciggar on 2019/7/23.
 */
@Data
public class AddCartRequest extends AbstractRequest{

    private Long userId;
    @JsonProperty(value = "productId")
    private Long itemId;
    @JsonProperty(value = "productNum")
    private Integer num;

    @Override
    public void requestCheck() {
    }
}
