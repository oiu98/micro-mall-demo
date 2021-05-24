package com.mall.shopping.services;

import com.mall.shopping.IContentService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.ItemCat;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dto.NavListResponse;
import com.mall.shopping.dto.PanelContentDto;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description:
 * 目录相关
 */

@Slf4j
@Component
@Service
public class ContentServiceImpl implements IContentService {

    @Autowired
    ItemCatMapper itemCatMapper;
    @Autowired
    PanelContentMapper panelContentMapper;
    @Autowired
    ContentConverter contentConverter;

    @Override
    public NavListResponse queryNavList() {
        NavListResponse navListResponse = new NavListResponse();

        try {
            PanelContent panelContent = new PanelContent();
            panelContent.setPanelId(GlobalConstants.HEADER_PANEL_ID);
            List<PanelContent> panelContents = panelContentMapper.select(panelContent);
            List<PanelContentDto> panelContentDtos = contentConverter.panelContents2Dto(panelContents);

            navListResponse.setPannelContentDtos(panelContentDtos);
            navListResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            navListResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ContentServiceImpl.queryNavList occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(navListResponse, e);
        }

        return navListResponse;
    }
}
