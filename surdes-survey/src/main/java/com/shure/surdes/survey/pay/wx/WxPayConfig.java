package com.shure.surdes.survey.pay.wx;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875</p>
 *
 * <p>Node.js 版: https://gitee.com/javen205/TNWX</p>
 *
 * <p>微信配置 Bean</p>
 *
 * @author Javen
 */
@Component
//@PropertySource("classpath:/wxpay.properties")
@ConfigurationProperties(prefix = "wxpay")
@Data
public class WxPayConfig {
	
    private String appId;
    
    private String appSecret;
    
    private String mchId;
    
    private String partnerKey;
    
    private String certPath;
    
    private String domain;

}

