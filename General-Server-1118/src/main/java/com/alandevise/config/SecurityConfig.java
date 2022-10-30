// package com.alandevise.config;
//
// import com.alandevise.handler.MyAccessDeniedHandler;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
// import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
//
// import javax.annotation.Resource;
// import javax.sql.DataSource;
//
// /**
//  * @Filename: SecurityConfig.java
//  * @Package: com.alandevise.config
//  * @Version: V1.0.0
//  * @Description: 1. SpringSecurity配置类
//  * @Author: Alan Zhang [initiator@alandevise.com]
//  * @Date: 2022年10月06日 14:59
//  */
//
// @Configuration
// @EnableWebSecurity
// public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//     @Resource
//     private MyAccessDeniedHandler myAccessDeniedHandler;
//
//     @Resource
//     private UserDetailsServiceImpl userDetailsService;
//
//     @Resource
//     private DataSource dataSource;
//
//     @Resource
//     private PersistentTokenRepository persistentTokenRepository;
//
//     // 重写configure
//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         // 表单提交
//         http.formLogin().usernameParameter("username").passwordParameter("password")
//                 // 当发现/login时，认为是登录，登录页中login action 同名
//                 .loginProcessingUrl("/login")
//                 // 自定义登录页
//                 .loginPage("/login.html")
//                 // 登录成功后跳转页面，Post请求
//                 .successForwardUrl("/toMain")
//                 // 登录成功后的处理器，不能和successForwardUrl共存
//                 // .successHandler(new MyAuthenticationSuccessHandler("https://www.baidu.com"))
//                 // 登录失败后跳转页面，Post请求
//                 .failureForwardUrl("/toError");
//         // 登录失败后的处理器，不能和failureForwardUrl共存
//         // .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
//
//         // 授权认证
//         String ipAddress = "127.0.0.1";
//         // http.authorizeHttpRequests()
//         //         // 除了/login.html，/error.html不需要认证
//         //         .antMatchers("/login.html", "/error.html").permitAll()
//         //         // 访问main1.html要求有admin权限，“admin”区分大小写
//         //         // .antMatchers("/main1.html").hasAuthority("admin")
//         //         // 访问main1.html要求有admin 或者adminN 权限
//         //         // .antMatchers("/main1.html").hasAnyAuthority("admin", "adminN")
//         //         // 访问main1.html要求为abc角色
//         //         // .antMatchers("/main1.html").hasRole("abc")
//         //         // 所有请求都必须被验证，所有请求都必须登录之后访问
//         //         // .antMatchers("/swagger-ui/index.html").hasIpAddress("127.0.0.1")
//         //         .anyRequest().authenticated();
//
//         // 关闭csrf防护
//         http.csrf().disable();
//
//         // 异常处理
//         http.exceptionHandling()
//                 // 无权限进入，跳转至accessDenied页面
//                 // .accessDeniedPage("/accessDenied.html");
//                 // 无权限进入，返回json数据
//                 .accessDeniedHandler(myAccessDeniedHandler);
//
//         // 记住我功能
//         http.rememberMe()
//                 // 失效时间，单位秒，默认为2周，这里设置为2小时
//                 .tokenValiditySeconds(2 * 60 * 60)
//                 // .rememberMeParameter()
//                 // 自定义登录逻辑
//                 .userDetailsService(userDetailsService)
//                 // 持久层对象
//                 .tokenRepository(persistentTokenRepository);
//
//         // 退出登录
//         http.logout()
//                 // 设定退出登录跳转页面
//                 .logoutSuccessUrl("/login.html");
//     }
//
//     @Bean
//     public PasswordEncoder getPw() {
//         return new BCryptPasswordEncoder();
//     }
//
//     @Bean
//     public PersistentTokenRepository getPersistentTokenRepository() {
//         JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
//         jdbcTokenRepository.setDataSource(dataSource);
//         // 自动建表，第一次启动需要，之后启动注释掉
//         // jdbcTokenRepository.setCreateTableOnStartup(true);
//         return jdbcTokenRepository;
//     }
// }
