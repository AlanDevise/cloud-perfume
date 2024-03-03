package com.alandevise.multidatasource.controller;

import com.alandevise.multidatasource.po.User;
import com.alandevise.multidatasource.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Filename: UserController.java
 * @Package: com.alandevise.annotation
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2023年03月11日 12:25
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;


    @RequestMapping("/getUserById")
    public User getUserById(@RequestParam("userId") Long userId) {
        return userService.getUserById(userId);
    }


    @PostMapping("/saveRandomUser")
    public User saveRandomUser() {
        return userService.saveRandomUser();
    }


}
