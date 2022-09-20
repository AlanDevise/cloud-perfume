package com.alandevise.config;

import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Filename: SwaggerConfig.java
 * @Package: com.alandevise.config
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-20 15:23
 */

@EnableSwagger2
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //为当前包路径,控制器类包
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build();
    }
    //构建 api文档的详细信息函数
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("API接口文档")
                //创建人
                .contact(new Contact("Alan", "https://www.alandevise.com", "initiator@alandevise.com"))
                //版本号
                .version("1.0")
                //描述
                .description("SuperServer系统API描述")
                .build();
    }
}
