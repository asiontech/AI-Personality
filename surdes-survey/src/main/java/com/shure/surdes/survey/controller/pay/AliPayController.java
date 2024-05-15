package com.shure.surdes.survey.controller.pay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.survey.constant.OrderPayStatus;
import com.shure.surdes.survey.domain.Survey;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.pay.zfb.AliPayDTO;
import com.shure.surdes.survey.pay.zfb.AliPayService;
import com.shure.surdes.survey.pay.zfb.AliyunProperties;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.ISurveyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "支付宝支付相关接口")
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/ali")
public class AliPayController {

    @Resource
    private AliPayService aliPayService;
    
    @Resource
    private AliyunProperties aliyunProperties;
    
    @Autowired
    ISurveyOrderService surveyOrderService;
    
    @Autowired
    ISurveyService surveyService;

    @ApiOperation(value = "手机网站支付,返回跳转链接")
    @PostMapping("/mobilePay")
    public AjaxResult aliPay(@RequestBody AliPayDTO dto) {
        //todo 按照业务查询订单信息
//        AliPayDTO dto = new AliPayDTO();
//        //订单编号
//        dto.setOutTradeNo(orderNumber);
//        //支付金额,单位为元
//        dto.setTotalAmount("0.01");
//        //订单标题，不可使用特殊符号
//        dto.setSubject("订单标题");
        // 请求阿里云支付
//        String pay = aliPayService.certificatePayment(dto,"QUICK_WAP_WAY");
		String pay = aliPayService.keyPayment(dto, "QUICK_WAP_WAY");
        return AjaxResult.success("", pay);
    }

    @ApiOperation(value = "电脑网站支付,返回支付二维码")
    @PostMapping("/webPay")
    public AjaxResult scanCodePay(@RequestBody AliPayDTO dto) {
        //todo 按照业务查询订单信息
//        AliPayDTO dto = new AliPayDTO();
//        //订单编号
//        dto.setOutTradeNo(orderNumber);
//        //支付金额,单位为元
//        dto.setTotalAmount("0.01");
//        //订单标题，不可使用特殊符号
//        dto.setSubject("订单标题");
        // 请求阿里云支付 FAST_INSTANT_TRADE_PAY
//        String pay = aliPayService.certificatePayment(dto,"FAST_INSTANT_TRADE_PAY");
        String pay = aliPayService.keyWebPayment(dto, "FAST_INSTANT_TRADE_PAY");
        return AjaxResult.success("", pay);
    }

    @ApiOperation(value = "支付宝支付回调")
    @GetMapping("/callback")
    public AjaxResult aliCallback(HttpServletRequest request) {
        String paramsJson = null;
        try {
            //获取支付回调信息
            Map<String,String> params = new HashMap<String,String>();
            Map requestParams = request.getParameterMap();
            log.info("异步通知信息为："+ JSON.toJSONString(requestParams));
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                // 乱码解决，这段代码在出现乱码时使用。
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }
            paramsJson = JSON.toJSONString(params);
            // 查询是否重复请求
            String orderId = params.get("out_trade_no");
            if (StringUtils.isNotEmpty(orderId)) {
            	SurveyOrder so = surveyOrderService.getById(Long.valueOf(orderId));

            	if (so != null) {
            		Integer status = so.getStatus();
            		if (OrderPayStatus.HAVE_PAY == status) { // 已支付完成，直接返回，
                    	Long surveyId = so.getSurveyId();
                    	Survey survey = surveyService.selectSurveyBySurveyId(surveyId);
            			so.setSurveyName(survey.getSurveyName());
            			log.info("重复请求查询订单状态：" + so);
            			return AjaxResult.success(so);
            		}
            	}
            }
            log.info("支付宝回调信息为: {}", paramsJson);
            //todo 调用SDK验证签名,是支付宝公钥不是应用公钥
//            boolean signVerified = AlipaySignature.rsaCertCheckV1(params, aliyunProperties.getAliPublicKeyPath(),"utf-8", "RSA2");
            String aliPublicKey = aliyunProperties.getAliPublicKey();
            log.debug("支付宝公钥：" + aliPublicKey);
        	String outTradeNo = params.get("out_trade_no");
        	log.debug("订单号" + outTradeNo);
        	// 支付宝交易号
        	String tradeNo = params.get("trade_no");
        	log.debug("支付宝交易流水号" + tradeNo);
        	// 支付总金额
        	String totalAmount = params.get("total_amount");
        	log.debug("实付总金额" + totalAmount);
        	String sellerId = params.get("seller_id");
        	log.debug("收款账号" + sellerId);
        	String timestamp = params.get("timestamp");
        	log.debug("时间戳" + timestamp);
        	String signType = params.get("sign_type");
        	log.debug("签名类型" + signType);
        	String sign = params.get("sign");
        	log.debug("签名" + sign);
        	String continueFlag = params.get("continueFlag");
        	log.debug("continueFlag:" + continueFlag);
        	String authAppId = params.get("auth_app_id");
        	log.debug("authAppId:" + authAppId);
        	String appId = params.get("app_id");
        	log.debug("appId：" + appId);
        	String charset = params.get("charset");
        	log.debug("编码类型：" + charset);
        	String method = params.get("method");
        	log.debug("method：" + method);
            boolean signVerified = AlipaySignature.rsaCheckV1(params, aliPublicKey, "utf-8", "RSA2");
            log.info("验签结果：" + signVerified);
            if (signVerified) {
                // 做业务操作,如改订单状态,保存回调信息等
            	// 商户订单号
            	// 更新订单状态，记录回调信息
            	SurveyOrder order = surveyOrderService.callbackUpdateOrder(outTradeNo, tradeNo, totalAmount, sellerId, timestamp, OrderPayStatus.HAVE_PAY);
                log.info("验签成功回调保存订单信息：" + order);
            	Long surveyId = order.getSurveyId();
            	Survey survey = surveyService.selectSurveyBySurveyId(surveyId);
            	order.setSurveyName(survey.getSurveyName());
            	// 如果签名验证正确，返回success
                return AjaxResult.success(order);
            } else {
            	// 商户订单号
            	// 更新订单状态，记录回调信息
            	SurveyOrder order = surveyOrderService.callbackUpdateOrder(outTradeNo, tradeNo, totalAmount, sellerId, timestamp, OrderPayStatus.VERIFICATION_FAILED);
            	Long surveyId = order.getSurveyId();
            	Survey survey = surveyService.selectSurveyBySurveyId(surveyId);
            	order.setSurveyName(survey.getSurveyName());
            	log.info("验签失败回调保存订单信息：" + order);
            	log.info("支付宝回调签名认证失败，signVerified=false, paramsJson:{}", paramsJson);
                return AjaxResult.error("支付宝回调签名认证失败！", order);
            }
        } catch (AlipayApiException e) {
        	e.printStackTrace();
            log.error("支付宝回调签名认证失败,paramsJson:{},errorMsg:{}", paramsJson, e.getMessage());
            return AjaxResult.error("failure");
        }
    }

}


