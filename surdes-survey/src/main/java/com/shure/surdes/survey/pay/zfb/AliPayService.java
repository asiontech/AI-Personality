package com.shure.surdes.survey.pay.zfb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AliPayService {

	@Autowired
	private AliyunProperties aliyunProperties;

	/**
	 * 支付宝通过证书的方式进行下单
	 * 
	 * @param dto         支付宝统一下单请求参数
	 * @param productCode 手机网站跳转支付宝支付传值QUICK_WAP_WAY,电脑网站扫码支付传FAST_INSTANT_TRADE_PAY
	 * @return
	 */
	public String certificatePayment(AliPayDTO dto, String productCode) {
		try {
			// 构造client
			CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
			// 设置网关地址 沙箱环境请修改网关
			certAlipayRequest.setServerUrl("https://openapi.alipay.com/gateway.do");
			// 设置应用Id
			certAlipayRequest.setAppId(aliyunProperties.getAppId());
			// 设置应用私钥
			certAlipayRequest.setPrivateKey(aliyunProperties.getPrivateKey());
			// 设置请求格式，固定值json
			certAlipayRequest.setFormat("json");
			// 设置字符集
			certAlipayRequest.setCharset("UTF-8");
			// 设置签名类型
			certAlipayRequest.setSignType("RSA2");
			/****** 服务器配置 ******/
			// 设置应用公钥证书路径
//        	certAlipayRequest.setCertPath(aliyunProperties.getAppPublicKeyPath());
//        	//设置支付宝公钥证书路径
//        	certAlipayRequest.setAlipayPublicCertPath(aliyunProperties.getAliPublicKeyPath());
//        	//设置支付宝根证书路径
//        	certAlipayRequest.setRootCertPath(aliyunProperties.getAliRootKeyPath());
			/****** 本地配置 ******/
			// 设置应用公钥证书路径
			certAlipayRequest.setCertPath("C:\\Users\\__yge__\\Desktop\\config\\app_public_key.crt");
			// 设置支付宝公钥证书路径
			certAlipayRequest.setAlipayPublicCertPath("C:\\Users\\__yge__\\Desktop\\config\\ali_public_key.crt");
			// 设置支付宝根证书路径
			certAlipayRequest.setRootCertPath("C:\\Users\\__yge__\\Desktop\\config\\ali_root_key.crt");
			AlipayClient alipayClient = null;
			alipayClient = new DefaultAlipayClient(certAlipayRequest);
			AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
			// 异步接收地址，仅支持http/https，公网可访问
			request.setNotifyUrl(aliyunProperties.getNotifyUrl());
			// 同步跳转地址，仅支持http/https
//        	request.setReturnUrl(aliyunProperties.getNotifyUrl());
			/****** 必传参数 ******/
			JSONObject bizContent = new JSONObject();
			// 商户订单号，商家自定义，保持唯一性
			bizContent.put("out_trade_no", dto.getOutTradeNo());
			// 支付金额，最小值0.01元
			bizContent.put("total_amount", dto.getTotalAmount());
			// 订单标题，不可使用特殊符号
			bizContent.put("subject", dto.getSubject());

			/****** 可选参数 ******/
			// todo 手机网站跳转支付宝支付传值QUICK_WAP_WAY,电脑网站扫码支付传FAST_INSTANT_TRADE_PAY
			bizContent.put("product_code", productCode);
//        	bizContent.put("time_expire", "2022-08-01 22:00:00");

			// 商品明细信息，按需传入
			// JSONArray goodsDetail = new JSONArray();
			// JSONObject goods1 = new JSONObject();
			// goods1.put("goods_id", "goodsNo1");
			// goods1.put("goods_name", "子商品1");
			// goods1.put("quantity", 1);
			// goods1.put("price", 0.01);
			// goodsDetail.add(goods1);
			// bizContent.put("goods_detail", goodsDetail);

			// 扩展信息，按需传入
			// JSONObject extendParams = new JSONObject();
			// extendParams.put("sys_service_provider_id", "2088511833207846");
			// bizContent.put("extend_params", extendParams);

			request.setBizContent(bizContent.toString());
//        	AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
			// 如果需要返回GET请求，请使用
			AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
			String pageRedirectionData = response.getBody();

			if (response.isSuccess()) {
				return pageRedirectionData;
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 支付宝通过秘钥的方式进行下单
	 * 
	 * @param dto         支付宝统一下单请求参数
	 * @param productCode 手机网站跳转支付宝支付传值QUICK_WAP_WAY,电脑网站扫码支付传FAST_INSTANT_TRADE_PAY
	 * @return
	 */
	public String keyPayment(AliPayDTO dto, String productCode) {
		try {
			AlipayClient alipayClient = new DefaultAlipayClient(aliyunProperties.getServerUrl(),
					aliyunProperties.getAppId(), aliyunProperties.getPrivateKey(), "json", "utf-8",
					aliyunProperties.getAppPublicKey(), "RSA2");
			AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
			// 异步接收地址，仅支持http/https，公网可访问
//            request.setNotifyUrl(aliyunProperties.getNotifyUrl());
			// 同步跳转地址，仅支持http/https
			request.setReturnUrl(aliyunProperties.getNotifyUrl());
			/****** 必传参数 ******/
			JSONObject bizContent = new JSONObject();
			// 商户订单号，商家自定义，保持唯一性
			bizContent.put("out_trade_no", dto.getOutTradeNo());
			// 支付金额，最小值0.01元
			bizContent.put("total_amount", dto.getTotalAmount());
			// 订单标题，不可使用特殊符号
			bizContent.put("subject", dto.getSubject());

			/****** 可选参数 ******/
			// todo 手机网站跳转支付宝支付传值QUICK_WAP_WAY,电脑网站扫码支付传FAST_INSTANT_TRADE_PAY
			bizContent.put("product_code", productCode);
//            bizContent.put("timeout_express", "5m");
//        bizContent.put("time_expire", "2022-08-01 22:00:00");

			// 商品明细信息，按需传入
			// JSONArray goodsDetail = new JSONArray();
			// JSONObject goods1 = new JSONObject();
			// goods1.put("goods_id", "goodsNo1");
			// goods1.put("goods_name", "子商品1");
			// goods1.put("quantity", 1);
			// goods1.put("price", 0.01);
			// goodsDetail.add(goods1);
			// bizContent.put("goods_detail", goodsDetail);

			// 扩展信息，按需传入
			// JSONObject extendParams = new JSONObject();
			// extendParams.put("sys_service_provider_id", "2088511833207846");
			// bizContent.put("extend_params", extendParams);

			log.info("支付请求参数：" + bizContent.toString());
			request.setBizContent(bizContent.toString());
//        	AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
			// 如果需要返回GET请求，请使用
			AlipayTradeWapPayResponse response = null;
			response = alipayClient.pageExecute(request, "GET");
			String pageRedirectionData = response.getBody();
			if (response.isSuccess()) {
				return pageRedirectionData;
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return null;
	}
}
