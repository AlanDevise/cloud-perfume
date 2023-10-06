package com.alandevise.controller;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Filename: UserController.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年10月06日 14:20
 */

@RestController
@RequestMapping("/user/")
public class UserController {

    // 测试登录，浏览器访问： http://localhost:1111/user/doLogin?username=zhang&password=123456
    @RequestMapping("doLogin")
    public String doLogin(String username, String password) {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if ("zhang".equals(username) && "123456".equals(password)) {
            StpUtil.login(2348923);
            return "登录成功";
        }
        return "登录失败";
    }

    // 查询登录状态，浏览器访问： http://localhost:1111/user/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    @GetMapping("getPermissionList")
    public List<String> GetPermissionList() {
        // 获取：当前账号所拥有的权限集合
        return StpUtil.getPermissionList();
    }

    @GetMapping("HasPermission")
    public String HasPermission() {
        // 判断：当前账号是否含有指定权限, 返回 true 或 false
        return StpUtil.hasPermission("user.add") ? "有指定权限" : "无指定权限";
    }

    @GetMapping("CheckPermission")
    public String CheckPermission() {
        // 校验：当前账号是否含有指定权限, 如果验证未通过，则抛出异常: NotPermissionException
        try {
            StpUtil.checkPermission("user.add");
        } catch (NotPermissionException e) {
            return "未包含指定权限";
        }
        return "含有指定权限";
    }

    @GetMapping("CheckPermissionAnd")
    public String CheckPermissionAnd() {
        // 校验：当前账号是否含有指定权限 [指定多个，必须全部验证通过]
        try {
            StpUtil.checkPermissionAnd("user.add", "user.delete", "user.get");
        } catch (NotPermissionException e) {
            return "未包含指定权限";
        }
        return "含有指定权限";
    }

    @GetMapping("CheckPermissionOr")
    public String CheckPermissionOr() {
        // 校验：当前账号是否含有指定权限 [指定多个，只要其一验证通过即可]
        try {
            StpUtil.checkPermissionOr("user.add", "user.delete", "user.get");
        } catch (NotPermissionException e) {
            return "未包含指定权限";
        }
        return "含有指定权限";
    }
}

