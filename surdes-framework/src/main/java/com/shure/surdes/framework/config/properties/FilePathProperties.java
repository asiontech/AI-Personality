package com.shure.surdes.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 读取服务器上传文件磁盘路径
 */
@Configuration
@ConfigurationProperties(prefix = "star")
public class FilePathProperties {
    /**
     * 传文件磁盘路径
     */
    private String imgUrl;


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}