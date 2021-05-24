package com.cskaoyan.gateway.config;

import com.mall.user.intercepter.TokenIntercepter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ZhaoJiachen on 2021/5/21
 * <p>
 * Description: SpringMVC配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public TokenIntercepter tokenIntercepter(){
        return new TokenIntercepter();
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) { // 启用全局拦截器
        registry.addInterceptor(tokenIntercepter())
//                .addPathPatterns("/shopping/**")
//                .addPathPatterns("/user/**")
//                .addPathPatterns("/cashier/**")
//                .excludePathPatterns("/error");
        ;
    }
}
