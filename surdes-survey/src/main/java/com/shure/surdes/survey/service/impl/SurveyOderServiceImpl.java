package com.shure.surdes.survey.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.PayType;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.SurveyOderMapper;
import com.shure.surdes.survey.pay.zfb.AliPayDTO;
import com.shure.surdes.survey.pay.zfb.AliPayService;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.IUserSurveyResultService;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单业务层实现类
 * 
 * @author color
 *
 */
@Service
@Slf4j
public class SurveyOderServiceImpl extends ServiceImpl<SurveyOderMapper, SurveyOrder> implements ISurveyOrderService {

	@Resource
	private AliPayService aliPayService;
	
	@Autowired
	IAnswerJsonService answerJsonService;
	
	@Autowired
	IUserSurveyResultService userSurveyResultService;
	
	@Autowired
	AnswerJsonMapper answerJsonMapper;
	
	@Value("${surdes.price}")
	private String price;

	@Override
	public JSONObject submitOrder(SurveyOrder so) {
		log.info("提交订单，订单信息：" + so);
		Long anId = so.getAnId();
		Long surveyId = so.getSurveyId();
//		if (null != surveyId && 4L == surveyId) {
//			log.info("提交订单，订单信息surveyId：" + surveyId);
//			so.setSurveyId(1000L);
//		}
		if (null == surveyId) {
			log.error("参数错误，没有问卷id！");
			throw new ServiceException("参数错误，没有问卷id！");
		}
		if (null == anId) {
			// 查询最新的结果
			AnswerJson answer = new AnswerJson();
			answer.setUserId(so.getUserId().toString());
			answer.setSurveyId(so.getSurveyId());
			List<AnswerJson> list = answerJsonMapper.selectAnswerJsonLatest(answer);
			if (StringUtils.isNotEmpty(list)) {
				AnswerJson answerJson = list.get(0);
				so.setAnId(answerJson.getAnId());
			} else {
				throw new ServiceException("没有测试结果，请选重新测试完成之后再重新提交！");
			}
		}
		so.setCreateTime(new Date());
		boolean flag = this.save(so);
		if (!flag) {
			throw new ServiceException("订单提交失败，请稍后重试！");
		}
		// 按照业务查询订单信息
		AliPayDTO dto = new AliPayDTO();
		// 订单编号用户id加上生成的订单编号
		dto.setOutTradeNo(so.getOrderId().toString()); 
		// 支付金额,单位为元
		dto.setTotalAmount(so.getAmount().toString());
		// 订单标题，不可使用特殊符号
		dto.setSubject(so.getSurveyName());
		// 调用支付宝支付接口，返回支付链接
		String productCode = "QUICK_WAP_WAY";
		String paySource = so.getPaySource();
		String payLink = "";
		if (StringUtils.isNotEmpty(paySource) && "pc".equals(paySource)) {
			productCode = "FAST_INSTANT_TRADE_PAY";
			payLink = aliPayService.keyWebPayment(dto, productCode);
		} else {
			payLink = aliPayService.keyPayment(dto, productCode);
		}
		JSONObject json = new JSONObject();
		json.put("pay", payLink);
		json.put("order", so);
		return json;
	}

	@Override
	public SurveyOrder callbackUpdateOrder(String outTradeNo, String tradeNo, String payAmount, 
			String sellerId, String timestamp, Integer status) {
		log.info("回调接口outTradeNo：" + outTradeNo);
		// 查询订单信息
		SurveyOrder surveyOrder = this.getById(Long.valueOf(outTradeNo)); // 商户订单号
		surveyOrder.setTradeNo(tradeNo); // 交易流水号
		surveyOrder.setSellerId(sellerId); // 交易收款人用户id
		surveyOrder.setBackTimestamp(timestamp); // 前端回调时间
		surveyOrder.setStatus(1); // 设置订单状态已付款
		surveyOrder.setPayTime(new Date()); // 订单完成时间
		surveyOrder.setPayAmount(payAmount); // 实际付款金额
		surveyOrder.setPayType(PayType.ALI_PAY); // 支付方式
		boolean flag = this.saveOrUpdate(surveyOrder);
		// 查询结果信息，将测试结果存入用户统一结果表
		Long anId = surveyOrder.getAnId();
		AnswerJson answer = answerJsonService.selectAnswerJsonByAnId(anId);
		
		Long surveyId = answer.getSurveyId();
		if (null != surveyId && 1000L == surveyId) { //AI测试
			UserSurveyResult result = new UserSurveyResult();
			result.setUserId(Long.valueOf(answer.getUserId()));
//		result.setSurveyId(surveyId);
			result.setAnswerResult(answer.getAnswerResult());
			result.setSurveyType(SurveyType.MBTI_AI_SURVEY_TEST); //ai测试
			result.setAnswerResultOrigin(answer.getAnswerResultOrigin());
			result.setCreateTime(new Date());
			userSurveyResultService.saveOrUpdate(result);
			if (!flag) {
				throw new ServiceException("更新订单信息失败！");
			}
		} 
		
		return surveyOrder;
	}

}
