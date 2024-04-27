package com.shure.surdes.survey.pay.zfb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取支付宝支付所需yml配置
 **/

@Data
@ConfigurationProperties(prefix = "aliyun")
@Component
@Slf4j
public class AliyunProperties {

    //支付回调
    private String notifyUrl;
    //应用id
    private String appId;
    /** 服务器url */
    private String serverUrl;
    //应用私钥
    private String privateKey;
    //应用公钥
    private String appPublicKey;
    //应用公钥地址
    private String appPublicKeyPath;
    //支付宝公钥
    private String aliPublicKey;
    //支付宝公钥地址
    private String aliPublicKeyPath;
    //支付宝根证书地址
    private String aliRootKeyPath;

}


