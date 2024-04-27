package com.shure.surdes.survey.service;

import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shure.surdes.survey.domain.SurveyOrder;

/**
 * 订单业务层
 * @author color
 *
 */
public interface ISurveyOrderService extends IService<SurveyOrder> {

	/**
	 * 提交订单，返回支付链接和订单信息
	 * @param vo
	 * @return
	 */
	public JSONObject submitOrder(@RequestBody SurveyOrder so);
	
	/**
	 * 支付宝回调修改订单状态
	 * @param outTradeNo
	 * @param tradeNo
	 * @param sellerId
	 * @return
	 */
	public SurveyOrder callbackUpdateOrder(String outTradeNo, String tradeNo, String payAmount, 
			String sellerId, String timestamp, Integer status);
}
