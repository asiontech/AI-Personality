package com.shure.surdes.survey.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.SecurityUtils;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.OrderPayStatus;
import com.shure.surdes.survey.constant.PayType;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.Survey;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.SurveyOderMapper;
import com.shure.surdes.survey.pay.zfb.AliPayDTO;
import com.shure.surdes.survey.pay.zfb.AliPayService;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.ISurveyService;
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
	
	@Autowired
	ISurveyService surveyService;
	
	@Value("${surdes.price}")
	private String price;

	@Override
	public JSONObject submitOrder(SurveyOrder so) {
		JSONObject json = new JSONObject();
		log.info("提交订单，订单信息：" + so);
		Long anId = so.getAnId();
		Long surveyId = so.getSurveyId();
		Long userId = so.getUserId();
		if (null == userId) {
			log.error("参数错误，没有userId！");
			json.put("code", 500);
			json.put("msg", "userId获取失败！");
			return json;
		}
		if (null == surveyId) {
			log.error("参数错误，没有问卷id！");
			json.put("code", 500);
			json.put("msg", "参数错误，没有问卷id！");
			return json;
		}
		// 查询问卷信息
		Survey survey = surveyService.selectSurveyBySurveyId(surveyId);
		if (survey == null) {
			log.error("没有找到对应的问卷！");
			json.put("code", 500);
			json.put("msg", "没有找到对应的问卷！");
			return json;
		}
		// 优惠价格
		Double discountPrice = survey.getDiscountPrice();
		Integer freeFlag = survey.getFreeFlag();
		if (0 == freeFlag) {
			log.error("免费问卷，无需支付！");
			json.put("code", 500);
			json.put("msg", "没有找到对应的问卷！");
			return json;
		}
		if (1001L == surveyId && null != anId) { // ai测试时间段
			AnswerJson answer = answerJsonMapper.selectAnswerJsonByAnId(anId);
			Date startTime = answer.getStartTime();
			Date endTime = answer.getEndTime();
			// 计算有几个月
			Integer monthNum = DateUtils.getMonthNumBetweenTime(startTime, endTime);
			BigDecimal b1 = new BigDecimal(discountPrice);
			BigDecimal b2 = new BigDecimal(monthNum);
			discountPrice = b1.multiply(b2).doubleValue(); // 计算总价
			so.setAmount(discountPrice); // 设置订单总价格
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
				log.error("没有测试结果，请重新测试完成之后再重新提交！");
				json.put("code", 500);
				json.put("msg", "没有测试结果，请重新测试完成之后再重新提交！");
				return json;
			}
		}
		so.setCreateTime(new Date());
		boolean flag = this.save(so);
		if (!flag) {
			log.error("订单提交失败，请稍后重试！");
			json.put("code", 500);
			json.put("msg", "订单提交失败，请稍后重试！");
			return json;
		}
		// 按照业务查询订单信息
		AliPayDTO dto = new AliPayDTO();
		// 订单编号用户id加上生成的订单编号
		dto.setOutTradeNo(so.getOrderId().toString()); 
		// 支付金额,单位为元
		dto.setTotalAmount(discountPrice.toString());
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
		json.put("code", 200);
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
		surveyOrder.setStatus(OrderPayStatus.HAVE_PAY); // 设置订单状态已付款
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
