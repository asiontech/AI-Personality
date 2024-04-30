package com.shure.surdes.survey.activemq;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.remote.ModelApi;
import com.shure.surdes.survey.vo.AiTestVo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TopicListner {
	
	@Autowired
	AnswerJsonMapper answerJsonMapper;
	
	@Autowired
	ModelApi modelApi;
	
	@Autowired
	RedisCache redisCache;

	// 消息队列消费者监听
	// 添加用户逻辑
	@JmsListener(destination = QueueDictionary.AI_TEST)
	public void aiTest(String message) throws InterruptedException {
		// 处理接收到的消息逻辑
		log.debug("获取队列原始消息: " + message);
		AiTestVo vo = JSON.parseObject(message, AiTestVo.class);
		Long userId = vo.getUserId();
		Long surveyId = vo.getSurveyId();
		String aid = vo.getAid();
	  	Integer retest = vo.getRetest(); // 强制更新字段
	  	String update = null;
    	if (null != retest && 1 == retest) {
    		update = "true";
    	}
		// 业务处理
    	// 调用接口得出mbti
    	JSONObject json = modelApi.getMbti(vo.getUid(), vo.getUser(), vo.getUserToken(), update);
    	log.debug("用户id:{}ai测试结束，返回结果:{}", userId, json);
    	JSONObject redisJson = new JSONObject();
    	if (null != json) {
    		Integer code = json.getInteger("status");
    		if (null != code && 201 == code) {
    			redisJson.fluentPut("code", 500);
    			redisJson.fluentPut("msg", "系统繁忙，请稍后重试！");
    			redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
    			log.debug("AI测试队列缓存消息：{}:{}", userId, redisJson.toString());
    			return;
    		}
    		String data = json.getString("data");
    		if (StringUtils.isNotEmpty(data) && "updating".equals(data)) { // 从数据库查询上一份记录
    			redisJson.fluentPut("code", 201);
    			redisJson.fluentPut("msg", "系统正在生成AI测试结果，请稍等！");
    			redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
    			log.debug("AI测试队列缓存消息：{}:{}", userId, redisJson.toString());
    			return;
    		}
    		if (null != code && 200 == code) {
    			JSONObject jsonObject = json.getJSONObject("data");
    			String mbti = jsonObject.getString("type");
    			JSONObject score = jsonObject.getJSONObject("dim_score");
    			// 计算得分
    			AnswerJson answer = new AnswerJson();
    			answer.setSurveyId(surveyId); // AI测试id固定1000
    			answer.setUserId(userId.toString());
    			answer.setCreateTime(new Date());
    			answer.setAnswerResult(mbti);
    			answer.setAnswerResultOrigin(score.toJSONString());
    			answer.setSurveyType(SurveyType.MBTI_AI_SURVEY_TEST);
    			answerJsonMapper.insertAnswerJson(answer);
    			redisJson.fluentPut("code", 200);
    			redisJson.fluentPut("data", answer.getAnId());
    			redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
    			log.debug("AI测试队列缓存消息：{}:{}", userId, redisJson.toString());
    		}
    	} else {
			redisJson.fluentPut("code", 500);
			redisJson.fluentPut("msg", "系统繁忙，请稍后重试！");
			redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
			log.debug("AI测试队列缓存消息：{}:{}", userId, redisJson.toString());
    	}
	}
}
