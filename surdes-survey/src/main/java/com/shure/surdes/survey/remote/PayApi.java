package com.shure.surdes.survey.remote;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.shure.surdes.survey.pay.client.JeepayClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.rmi.ServerException;

@Slf4j
@Component
public class PayApi {

    @Autowired
    RestTemplate restTemplate;

    @Value("${pay.url}")
    private String payUrl;
    /**
     * 调用支付接口
     *
     * @param outTradeNo
     * @return
     */
    public JSONObject payOrder(String outTradeNo) {
        String url = payUrl + "api/design/pay/payOrders?mchOrderNo=" + outTradeNo;
        JSONObject json = null;
        try {
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
            json = exchange.getBody();
            log.info("outTradeNo:{},支付返回:{}", outTradeNo, json.toString());
//			Integer code = json.getInteger("status");
//			if (200 == code) {
//				json = json.getJSONObject("data");
//				return json;
//			}
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接口调用失败！");
        }
        return null;
    }
}
