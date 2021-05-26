package com.mall.shopping.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.shopping.IContentService;
import com.mall.shopping.constant.GlobalConstants;
import com.mall.shopping.constants.ShoppingRetCode;
import com.mall.shopping.converter.ContentConverter;
import com.mall.shopping.dal.entitys.Panel;
import com.mall.shopping.dal.entitys.PanelContent;
import com.mall.shopping.dal.entitys.PanelContentItem;
import com.mall.shopping.dal.persistence.ItemCatMapper;
import com.mall.shopping.dal.persistence.PanelContentMapper;
import com.mall.shopping.dal.persistence.PanelMapper;
import com.mall.shopping.dto.*;
import com.mall.shopping.services.cache.CacheManager;
import com.mall.shopping.utils.ExceptionProcessorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ItemCatMapper itemCatMapper;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private PanelContentMapper panelContentMapper;
    @Autowired
    private PanelMapper panelMapper;
    @Autowired
    private ContentConverter contentConverter;

    /**
     *  导航栏数据服务接口
     */
    @Override
    public NavListResponse queryNavList() {
        NavListResponse navListResponse = new NavListResponse();

        try {
            // 查询redis中是否有缓存
            String navListCache = cacheManager.checkCache(GlobalConstants.HEADER_PANEL_CACHE_KEY);
            ObjectMapper objectMapper = new ObjectMapper();
            if (navListCache != null) { // redis中存在cache
                List<PanelContentDto> panelContentDtosCache = objectMapper.readValue(navListCache, List.class);
                navListResponse.setPannelContentDtos(panelContentDtosCache);
                navListResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
                navListResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
                return navListResponse;
            }

            // 查询数据
            PanelContent panelContent = new PanelContent();
            panelContent.setPanelId(GlobalConstants.HEADER_PANEL_ID);
            List<PanelContent> panelContents = panelContentMapper.select(panelContent);
            List<PanelContentDto> panelContentDtos = contentConverter.panelContents2Dto(panelContents);
            // 缓存到redis
            String cache = objectMapper.writeValueAsString(panelContentDtos);
            cacheManager.setCache(GlobalConstants.HEADER_PANEL_CACHE_KEY,cache,1);

            navListResponse.setPannelContentDtos(panelContentDtos);
            navListResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            navListResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ContentServiceImpl.queryNavList occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(navListResponse, e);
        }

        return navListResponse;
    }

    /**
     *  主页数据服务接口
     */
    @Override
    public HomePageResponse queryHomePage() {
        HomePageResponse homePageResponse = new HomePageResponse();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 查询redis中是否有缓存
            String cache = cacheManager.checkCache(GlobalConstants.HOMEPAGE_CACHE_KEY);
            if (cache != null) {
                Set<PanelDto> panelDtos = objectMapper.readValue(cache, Set.class);
                homePageResponse.setPanelContentItemDtos(panelDtos);
                homePageResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
                homePageResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
            }

            // 获取首页商品板块
            List<Panel> panels = panelMapper.selectAll();
            // 获取各板块的商品，转换，封装
            Set<PanelDto> panelDtos = new HashSet<>();
            for (Panel panel : panels) {
                // panel ---> panelDTO
                PanelDto panelDto = contentConverter.panel2Dto(panel);
                Integer panelId = panel.getId();
                List<PanelContentItem> panelContentItems = panelContentMapper.selectPanelContentAndProductWithPanelId(panelId);
                // PanelContentItem ---> PanelContentItemDto
                List<PanelContentItemDto> panelContentItemDtos = contentConverter.panelContentItem2Dto(panelContentItems);
                panelDto.setPanelContentItems(panelContentItemDtos);
                // 放入 set
                panelDtos.add(panelDto);
            }
            // 存入redis
            cacheManager.setCache(GlobalConstants.HOMEPAGE_CACHE_KEY,
                                    objectMapper.writeValueAsString(panelDtos),
                                    GlobalConstants.HOMEPAGE_EXPIRE_TIME);
            // 封装
            homePageResponse.setPanelContentItemDtos(panelDtos);
            homePageResponse.setCode(ShoppingRetCode.SUCCESS.getCode());
            homePageResponse.setMsg(ShoppingRetCode.SUCCESS.getMessage());
        } catch (Exception e) {
            log.error("ContentServiceImpl.queryHomePage occur Exception :" + e);
            ExceptionProcessorUtils.wrapperHandlerException(homePageResponse, e);
        }

        return homePageResponse;
    }
}
