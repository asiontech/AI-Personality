package com.shure.surdes.survey.pay;

import com.shure.surdes.survey.domain.SurveyOrder;

import lombok.Data;

/**
 * 支付宝统一下单请求参数
 */
@Data
public class PayDTO {

    /**
     * 字段名：商户订单号，商家自定义，保持唯一性
     * 变量名：out_trade_no
     */
    private String outTradeNo;

    /**
     * 字段名：支付金额，最小值0.01元
     * 变量名：total_amount
     */
    private String totalAmount;

    /**
     * 字段名：订单标题，不可使用特殊符号
     * 变量名：subject
     */
    private String subject;

    /** 订单信息 */
    private SurveyOrder order;

    //
    private String wayCode;

    //商户Id
    private String currentMchNo;

    //商户应用
    private String appId;

}


