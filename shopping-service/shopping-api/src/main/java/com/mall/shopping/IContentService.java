package com.mall.shopping;

import com.mall.shopping.dto.HomePageResponse;
import com.mall.shopping.dto.NavListResponse;


public interface IContentService {

    NavListResponse queryNavList();

    HomePageResponse queryHomePage();

}
