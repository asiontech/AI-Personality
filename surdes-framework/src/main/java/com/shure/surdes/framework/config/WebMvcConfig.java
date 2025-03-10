package com.shure.surdes.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration //配置类
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * 全局配置跨域，启用CORS
     * 允许跨域请求的配置，包括允许的来源、允许的请求头、允许的请求方法等
     * allowCredentials(false) 表示是否允许发送 Cookie 等认证信息。
     * allowedOrigins("*") 允许的来源域名，这里设置为通配符表示允许所有来源。
     * allowedHeaders("*") 允许的请求头。
     * allowedMethods("GET", "PUT", "POST", "DELETE") 允许的请求方法。
     * exposedHeaders("*") 暴露给前端的响应头。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                //是否发送Cookie
                .allowCredentials(true)
                //放行哪些原始域
                .allowedOrigins("*")
//                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
