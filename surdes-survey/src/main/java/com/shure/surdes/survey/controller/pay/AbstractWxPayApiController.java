package com.shure.surdes.survey.controller.pay;


import com.ijpay.wxpay.WxPayApiConfig;

/**
 * @author Javen
 */
public abstract class AbstractWxPayApiController {

    /**
     * 获取微信支付配置
     *
     * @return {@link WxPayApiConfig} 微信支付配置
     */
    public abstract WxPayApiConfig getApiConfig();
}

