package com.alandevise.sse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页路由控制器。
 */
@Controller
public class PageController {

    /**
     * 返回默认演示首页。
     *
     * @return 首页名称
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
