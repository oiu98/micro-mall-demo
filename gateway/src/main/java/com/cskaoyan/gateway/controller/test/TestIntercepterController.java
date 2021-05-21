package com.cskaoyan.gateway.controller.test;

import com.mall.user.annotation.Anoymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZhaoJiachen on 2021/5/20
 * <p>
 * Description: 测试sdk是否生效
 */

@RestController
@RequestMapping("/test")
@Anoymous
public class TestIntercepterController {

    @GetMapping("intercepter")
    public String intercept() {
        return "hello, jdk";
    }
}
