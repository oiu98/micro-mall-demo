package com.cskaoyan.gateway.controller.shopping;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.shopping.IContentService;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.dto.NavListResponse;
import com.mall.user.annotation.Anoymous;
import io.swagger.annotations.Api;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: 目录相关的控制层
 */

@RestController
@RequestMapping("/shopping")
@Anoymous
//@Api(tags = "ContentController", description = "目录相关的控制层")
public class ContentController {

    @Reference(timeout = 3000,check = false)
    IContentService contentService;

    @GetMapping("/navigation")
    public ResponseData queryNaviList() {
        NavListResponse navListResponse = contentService.queryNavList();
        if (!navListResponse.getCode().equals(ShoppingRetCode.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(navListResponse.getMsg());
        }
        return new ResponseUtil<>().setData(navListResponse.getPannelContentDtos());
    }

}
