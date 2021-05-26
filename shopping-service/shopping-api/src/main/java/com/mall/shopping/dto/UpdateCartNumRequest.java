package com.mall.shopping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mall.commons.result.AbstractRequest;
import com.mall.commons.tool.exception.ValidateException;
import com.mall.shopping.constants.ShoppingRetCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ciggar on 2019/7/23.
 */
@Data
public class UpdateCartNumRequest extends AbstractRequest{

    private Long userId;
    @JsonProperty(value = "productId")
    private Long itemId;
    @JsonProperty(value = "productNum")
    private Integer num;
    private String checked;

    @Override
    public void requestCheck() {
        if(userId==null||itemId==null||num==null|| StringUtils.isBlank(checked)){
            throw new ValidateException(ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getCode(),ShoppingRetCode.REQUISITE_PARAMETER_NOT_EXIST.getMessage());
        }
    }
}
