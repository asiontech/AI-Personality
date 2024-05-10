package com.shure.surdes.survey.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.partitioning.utilities.OrderedTuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.SecurityUtils;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.activemq.ActiveMQUtils;
import com.shure.surdes.survey.activemq.QueueDictionary;
import com.shure.surdes.survey.constant.MBTI16Type;
import com.shure.surdes.survey.constant.OrderPayStatus;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.AnswerPlus;
import com.shure.surdes.survey.domain.DiscTypeDesc;
import com.shure.surdes.survey.domain.MbtiTypeDesc;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.domain.SysUserPlus;
import com.shure.surdes.survey.domain.UserDiscResult;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.AnswerMapper;
import com.shure.surdes.survey.mapper.DiscTypeDescMapper;
import com.shure.surdes.survey.mapper.MbtiTypeDescMapper;
import com.shure.surdes.survey.mapper.UserSurveyResultMapper;
import com.shure.surdes.survey.remote.ModelApi;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.service.IAnswerPlusService;
import com.shure.surdes.survey.service.IMbtiTypeDescService;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.ISysUserPlusService;
import com.shure.surdes.survey.service.IUserDiscResultService;
import com.shure.surdes.survey.service.IUserSurveyResultService;
import com.shure.surdes.survey.vo.AiTestVo;
import com.shure.surdes.system.service.ISysUserService;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.log.Log;

/**
 * 问卷答案结果jsonService业务层处理
 *
 * @author Shure
 * @date 2021-10-18
 */
@Service
@Slf4j
public class AnswerJsonServiceImpl implements IAnswerJsonService {
    @Autowired
    private AnswerJsonMapper answerJsonMapper;
    
    @Autowired
    AnswerMapper answerMapper;
    
    @Autowired
    IAnswerPlusService answerPlusService;
    
    @Autowired
    IMbtiTypeDescService mbtiTypeDescService;
    
    @Autowired
    MbtiTypeDescMapper mbtiTypeDescMapper;
    
    @Autowired
    DiscTypeDescMapper discTypeDescMapper;
    
    @Autowired
    ISysUserService sysUserService;
    
    @Autowired
    IUserSurveyResultService userSurveyResultService;
    
    @Autowired
    ISysUserPlusService sysUserPlusService;
    
    @Autowired
    ModelApi modelApi;
    
    @Autowired
    ISurveyOrderService surveyOrderService;
    
    @Autowired
    ActiveMQUtils activeMQUtils;
    
    @Autowired
    RedisCache redisCache;
    
    @Autowired
    UserSurveyResultMapper userSurveyResultMapper;
    
    @Autowired
    IUserDiscResultService userDiscResultService;
    
    /**
     * 查询问卷答案结果json
     *
     * @param anId 问卷答案结果json主键
     * @return 问卷答案结果json
     */
    @Override
    public AnswerJson selectAnswerJsonByAnId(Long anId) {
        return answerJsonMapper.selectAnswerJsonByAnId(anId);
    }

    /**
     * 查询问卷答案结果json列表
     *
     * @param answerJson 问卷答案结果json
     * @return 问卷答案结果json
     */
    @Override
    public List<AnswerJson> selectAnswerJsonList(AnswerJson answerJson) {
        return answerJsonMapper.selectAnswerJsonList(answerJson);
    }
    
    @Override
    public JSONObject getMbtiDesc(String mbti, String userId) {
    	JSONObject json = new JSONObject();
    	LambdaQueryWrapper<MbtiTypeDesc> wrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
		wrapper.eq(MbtiTypeDesc::getCharaCode, mbti);
		MbtiTypeDesc mbtiTypeDesc = mbtiTypeDescMapper.selectOne(wrapper);
		String match = mbtiTypeDesc.getMatchMbti();
		// 查询性格匹配的人员
		if (StringUtils.isNotEmpty(match)) {
			String[] split = match.split(",");
			List<SysUserPlus> matchPeople = getMatchPeople(Arrays.asList(split));
			json.put("matchPeople", matchPeople);
			// 查询匹配性格
			String pmatch = "";
			for (String pm : split) {
    			LambdaQueryWrapper<MbtiTypeDesc> pwrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
    			pwrapper.eq(MbtiTypeDesc::getCharaCode, pm);
    			try {
    				MbtiTypeDesc pmbtidesc = mbtiTypeDescMapper.selectOne(pwrapper);
    				String pcharaName = pmbtidesc.getCharaName();
    				pmatch += pm + "（" + pcharaName + "），";
    			} catch (Exception e) {
					log.error("匹配性格人员查询失败:"+ e.getMessage() );
				}
			}
			pmatch = pmatch.substring(0, pmatch.length() - 1);
			mbtiTypeDesc.setMatchMbti(pmatch);
		}
		json.put("mbtiTypeDesc", mbtiTypeDesc);
		// 查询性格相同的人员
		try {
			List<SysUserPlus> samePeople = getSamePeople(mbti, userId);
			json.put("samePeople", samePeople);
		} catch (Exception e) {
			log.error("相同性格人员查询失败:"+ e.getMessage() );
		}
    	return json;
    }
    
    /**
     * 查询用户最新的答案结果
     */
    @Override
    public JSONObject selectAnswerJsonLatest(AnswerJson answerJson) {
    	JSONObject json = new JSONObject();
    	String userId = answerJson.getUserId();
    	Long surveyId = answerJson.getSurveyId();
    	// 查询的问卷类型
    	String stype = answerJson.getSurveyType();
    	log.info("查询用户最新答案传过来的用户id:{},surveyId:{},stype:{}",userId,surveyId,stype);
    	if (SurveyType.MBTI_AI_TIME_SURVEY_TEST.equals(stype)) { // ai测试时间段
    		json = getAiTestByTimeResult(answerJson);
    		return json;
    	}
    	List<AnswerJson> list = answerJsonMapper.selectAnswerJsonLatest(answerJson);
    	AnswerJson result = null;
    	if (SurveyType.MBTI_AI_SURVEY_TEST.equals(stype)) { // ai测试结果
    		if (StringUtils.isEmpty(list)) {
    			json.put("code", 500);
    			json.put("msg", "无AI测试结果，请前往极速AI测试进行测试！");
    			return json;
    		}
    		// 查询用户的订单信息
    		LambdaQueryWrapper<SurveyOrder> sowrapper = new LambdaQueryWrapper<SurveyOrder>();
    		sowrapper.eq(SurveyOrder::getUserId, userId);
    		sowrapper.eq(SurveyOrder::getSurveyId, surveyId);
    		sowrapper.eq(SurveyOrder::getStatus, OrderPayStatus.HAVE_PAY);
    		sowrapper.orderByDesc(SurveyOrder::getOrderTimestamp);
    		// 查询订单信息
    		List<SurveyOrder> orderList = surveyOrderService.list(sowrapper);
    		if (StringUtils.isEmpty(orderList)) {
    			json.put("code", 500);
    			json.put("msg", "没有支付订单信息，无法查看AI测试结果！");
    			return json;
    		}
    		// 最新的订单
    		SurveyOrder surveyOrder = orderList.get(0);
    		Long anId = surveyOrder.getAnId(); // 结果id
    		// 订单结果
    		result = answerJsonMapper.selectAnswerJsonByAnId(anId); 
    	} else if (SurveyType.DISC_40_QUESTION_SURVEY.equals(stype)) { // disc测试结果
    		if (StringUtils.isEmpty(list)) {
    			json.put("code", 500);
    			json.put("msg", "无DISC个性测试结果，请前往DISC个性测试进行测试！");
    			return json;
    		}
    		// 查询用户的订单信息
    		LambdaQueryWrapper<SurveyOrder> sowrapper = new LambdaQueryWrapper<SurveyOrder>();
    		sowrapper.eq(SurveyOrder::getUserId, userId);
    		sowrapper.eq(SurveyOrder::getSurveyId, surveyId);
    		sowrapper.eq(SurveyOrder::getStatus, OrderPayStatus.HAVE_PAY);
    		sowrapper.orderByDesc(SurveyOrder::getOrderTimestamp);
    		// 查询订单信息
    		List<SurveyOrder> orderList = surveyOrderService.list(sowrapper);
    		if (StringUtils.isEmpty(orderList)) {
    			json.put("code", 500);
    			json.put("msg", "没有支付订单信息，无法查看DISC个性测试结果！");
    			return json;
    		}
    		// 最新的订单
    		SurveyOrder surveyOrder = orderList.get(0);
    		Long anId = surveyOrder.getAnId(); // 结果id
    		// 订单结果
    		result = answerJsonMapper.selectAnswerJsonByAnId(anId); 
    	}
    	// 查询结果表最新数据
    	if (StringUtils.isNotEmpty(list)) {
    		if (result == null) { 
    			result = list.get(0);
    		}
    		// 用户得分
    		String origin = result.getAnswerResultOrigin();
    		// 用户性格结果
    		String answerResult = result.getAnswerResult();
    		String surveyType = result.getSurveyType();
    		// 雷达图数据
    		List<Map<String, Object>> radar = new ArrayList<>();
    		List<String> characters = MBTI16Type.CHARACTER_8_TYPE;
    		List<String> character4Type = MBTI16Type.CHARACTER_4_TYPE;
    		if (SurveyType.MBTI_AI_SURVEY_TEST.equals(surveyType)) { // ai测试结果
    			log.info("查询ai测试结果,开始封装得分数据 : " + origin);
    			JSONObject aiJson = JSONObject.parseObject(origin);
    			for (String chara2 : character4Type) {
    				String[] split = chara2.split("");
					Map<String, Object> map11 = new HashMap<>();
					String chara11 = split[0];
					Double value11 = aiJson.getDouble(chara11);
					Map<String, Object> map22 = new HashMap<>();
					String chara22 = split[1];
					Double value22 = aiJson.getDouble(chara22);
					if (null != value11) {
						value22 = 1 - value11;
					} else {
						value11 = 1 - value22;
					}
					map11.put("name", chara11);
					map11.put("value", (int) (value11 * 100));
					radar.add(map11);
					map22.put("name", chara22);
					map22.put("value", (int) (value22 * 100));
					radar.add(map22);
    					
    			}
    			
    		} else if (SurveyType.MBTI_28_QUESTION_SURVEY.equals(surveyType)) { // 问卷结果
    			if (StringUtils.isNotEmpty(origin)) {
    				// 遍历封装
    				for (String cha : characters) {
    					Map<String, Object> map = new HashMap<>();
    					int count = 0;
    					for (int i = 0; i < origin.length(); i++) {
    						char charAt = origin.charAt(i);
    						if (cha.equals(String.valueOf(charAt))) {
    							count++;
    						}
    					}
    					Double value = count / 7.0;
    					map.put("name", cha);
    					map.put("value", Math.round(value * 100));
    					radar.add(map);
    				}
    			}
    		} else if (SurveyType.MBTI_93_QUESTION_SURVEY.equals(surveyType)) {
    			if (StringUtils.isNotEmpty(origin)) {
    				// 遍历封装 IE21 
    				for (String cha : characters) {
    					Map<String, Object> map = new HashMap<>();
    					int count = 0;
    					for (int i = 0; i < origin.length(); i++) {
    						char charAt = origin.charAt(i);
    						if (cha.equals(String.valueOf(charAt))) {
    							count++;
    						}
    					}
    					Double total = 10.0;
    					if ("EI".contains(cha)) {
    						total = 21.0;
    					} else if ("NS".contains(cha)) {
    						total = 26.0;
    					} else if ("FT".contains(cha)) {
    						total = 24.0;
    					} else if ("JP".contains(cha)) {
    						total = 22.0;
    					}
    					
    					Double value = count / total;
    					map.put("name", cha);
    					map.put("value", Math.round(value * 100));
    					radar.add(map);
    				}
    			}
    		} else if (SurveyType.DISC_40_QUESTION_SURVEY.equals(surveyType)) {
    			List<String> discs = MBTI16Type.DISC_4_TYPE;
    			JSONArray discScore = new JSONArray();
    			if (StringUtils.isNotEmpty(origin)) {
        			log.debug("查询DISC测试结果,开始封装得分数据 : " + origin);
        			JSONObject discJson = JSONObject.parseObject(origin);
        			for (String disc : discs) {
        				JSONObject map = new JSONObject(); 
        				Integer value = discJson.getInteger(disc);
        				if (null == value) {
        					value = 0;
        				}
        				map.put("name", disc);
        				map.put("value", value);
        				discScore.add(map);
        			}
        			json.put("discScore", discScore); // 得分
        			// 查询类型解析数据
        			LambdaQueryWrapper<DiscTypeDesc> wrapper = new LambdaQueryWrapper<DiscTypeDesc>();
        			wrapper.eq(DiscTypeDesc::getType, answerResult);
        			DiscTypeDesc discTypeDesc = discTypeDescMapper.selectOne(wrapper);
        			json.put("discTypeDesc", discTypeDesc);
    			} 
    			json.put("answerJson", result);
    			json.put("code", 200);
    			return json;
    		}
    		
    		// 查询mbti性格解析
    		if (StringUtils.isNotEmpty(answerResult)) {
    			LambdaQueryWrapper<MbtiTypeDesc> wrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
    			wrapper.eq(MbtiTypeDesc::getCharaCode, answerResult);
    			MbtiTypeDesc mbtiTypeDesc = mbtiTypeDescMapper.selectOne(wrapper);
    			String match = mbtiTypeDesc.getMatchMbti();
    			// 查询性格匹配的人员
    			if (StringUtils.isNotEmpty(match)) {
    				String[] split = match.split(",");
    				List<SysUserPlus> matchPeople = getMatchPeople(Arrays.asList(split));
    				json.put("matchPeople", matchPeople);
    				// 查询匹配性格
    				String pmatch = "";
    				for (String pm : split) {
    	    			LambdaQueryWrapper<MbtiTypeDesc> pwrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
    	    			pwrapper.eq(MbtiTypeDesc::getCharaCode, pm);
    	    			try {
    	    				MbtiTypeDesc pmbtidesc = mbtiTypeDescMapper.selectOne(pwrapper);
    	    				String pcharaName = pmbtidesc.getCharaName();
    	    				pmatch += pm + "（" + pcharaName + "），";
    	    			} catch (Exception e) {
							log.error("匹配性格人员查询失败:"+ e.getMessage() );
						}
    				}
    				pmatch = pmatch.substring(0, pmatch.length() - 1);
    				mbtiTypeDesc.setMatchMbti(pmatch);
    			}
    			json.put("mbtiTypeDesc", mbtiTypeDesc);
    		}
    		// 查询性格相同的人员
    		try {
    			List<SysUserPlus> samePeople = getSamePeople(answerResult, userId);
    			json.put("samePeople", samePeople);
    		} catch (Exception e) {
				log.error("相同性格人员查询失败:"+ e.getMessage() );
			}
    		result.setAnswerJson(null); // 设置空限制减少流量
    		// 查询性格匹配的人员
    		json.put("answerJson", result);
    		json.put("radar", radar);
    		json.put("code", 200);
    		return json;
    	}
    	json.put("code", 500);
    	json.put("msg", "没有测试数据哦，请先测试！");
    	return json;
    }
    
    /**
     * 查询ai测试时间段结果
     * @param answerJson
     * @return
     */
    private JSONObject getAiTestByTimeResult(AnswerJson answerJson) {
       	try {
    		Long userId = SecurityUtils.getUserId();
    		log.debug("查询用户最新答案结果，业务层当前登录用户id为：" + userId);
     	} catch (Exception e) {
//			return AjaxResult.error("获取用户id失败，请重新登录！");
		}
    	JSONObject json = new JSONObject();
    	String userId = answerJson.getUserId();
    	Long surveyId = answerJson.getSurveyId();
    	// 查询的问卷类型
    	String stype = answerJson.getSurveyType();
    	log.info("查询用户最新答案传过来的用户id:{},surveyId:{},stype:{}",userId,surveyId,stype);
    	List<AnswerJson> list = answerJsonMapper.selectAnswerJsonAll(answerJson);
    	// 结果列表
    	List<AnswerJson> resultList = new ArrayList<AnswerJson>();
    	// 查询用户支付的订单
    	if (StringUtils.isNotEmpty(list)) {
    		// 查询用户的订单信息
    		LambdaQueryWrapper<SurveyOrder> sowrapper = new LambdaQueryWrapper<SurveyOrder>();
    		sowrapper.eq(SurveyOrder::getUserId, userId);
    		sowrapper.eq(SurveyOrder::getSurveyId, surveyId);
    		sowrapper.eq(SurveyOrder::getStatus, OrderPayStatus.HAVE_PAY);
    		sowrapper.orderByDesc(SurveyOrder::getOrderTimestamp);
    		// 查询订单信息
    		List<SurveyOrder> orderList = surveyOrderService.list(sowrapper);
    		if (StringUtils.isNotEmpty(orderList)) { 
    			List<Long> anIdList = orderList.stream().map(s -> s.getAnId()).collect(Collectors.toList());
    			if (StringUtils.isNotEmpty(anIdList)) { // 已支付订单对应的结果id
    				for (AnswerJson answer : list) {
    					Long anId = answer.getAnId();
    					if (anIdList.contains(anId)) { // 已支付
    						// 用户得分
        		    		String origin = answer.getAnswerResultOrigin();
        		    		// 雷达图数据
        		    		List<Map<String, Object>> radar = new ArrayList<>();
        		    		List<String> character4Type = MBTI16Type.CHARACTER_4_TYPE;
        					// 雷达图数据
        	    			JSONObject aiJson = JSONObject.parseObject(origin);
        	    			for (String chara2 : character4Type) {
        	    				String[] split = chara2.split("");
        						Map<String, Object> map11 = new HashMap<>();
        						String chara11 = split[0];
        						Double value11 = aiJson.getDouble(chara11);
        						Map<String, Object> map22 = new HashMap<>();
        						String chara22 = split[1];
        						Double value22 = aiJson.getDouble(chara22);
        						if (null != value11) {
        							value22 = 1 - value11;
        						} else {
        							value11 = 1 - value22;
        						}
        						map11.put("name", chara11);
        						map11.put("value", (int) (value11 * 100));
        						radar.add(map11);
        						map22.put("name", chara22);
        						map22.put("value", (int) (value22 * 100));
        						radar.add(map22);
        	    			}
        	    			answer.setRadar(radar);
    						resultList.add(answer);
    					} else { // 未支付，只返回anId，时间等参数
//    						answer.setAnswerResult(null);
//    						answer.setAnswerResultOrigin(null);
//    						answer.setKeyCloud(null);
    					}
    				}
    			}
    		} else { // 全都未支付
//				for (AnswerJson answer : list) {
//					answer.setAnswerResult(null);
//					answer.setAnswerResultOrigin(null);
//					answer.setKeyCloud(null);
//				}
        		json.put("code", 500);
        		json.put("msg", "没有支付记录，请测试完成之后支付！");
    		}
    		if (StringUtils.isNotEmpty(resultList)) {
    			json.put("code", 200);
    			json.put("answerList", resultList);
    			json.put("total", resultList.size());
    			return json;
    		}
    	} else {
    		json.put("code", 500);
    		json.put("msg", "没有测试记录，请先测试！");
    	}
		return json;
    }
    
    /**
     * 获取性格匹配的人员
     * @param mbtis
     * @return
     */
    private List<SysUserPlus> getMatchPeople(List<String> mbtis) {
    	// 查询性格相同的人员
//    	LambdaQueryWrapper<UserSurveyResult> wrapper = new LambdaQueryWrapper<UserSurveyResult>();
//    	wrapper.in(UserSurveyResult::getAnswerResult, mbtis);
//    	List<UserSurveyResult> list = userSurveyResultService.list(wrapper);
    	Integer limit = 10;
    	List<UserSurveyResult> list = userSurveyResultMapper.getRandList1(mbtis, limit);
    	
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		Map<Long, String> map = list.stream().collect(Collectors.toMap(UserSurveyResult::getUserId, UserSurveyResult::getAnswerResult));
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		// 密码不显示
    		if (StringUtils.isNotEmpty(userPlusList)) {
    			userPlusList.forEach(s -> {
    				s.setMbti(map.get(s.getUserId()));
    				s.setPassword(null);
    			});
    		}
    		return userPlusList;
    	}
    	return null;
    }
    
    /**
     * 获取性格相同的人员
     * @param mbti
     * @return
     */
    private List<SysUserPlus> getSamePeople(String mbti, String userId) {
    	// 查询性格相同的人员
//    	LambdaQueryWrapper<UserSurveyResult> wrapper = new LambdaQueryWrapper<UserSurveyResult>();
//    	wrapper.eq(UserSurveyResult::getAnswerResult, mbti);
//    	List<UserSurveyResult> list = userSurveyResultService.list(wrapper);
    	
    	Integer limit = 10;
    	List<UserSurveyResult> list = userSurveyResultMapper.getRandList2(mbti, limit);
    	
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		if (StringUtils.isNotEmpty(userId)) {
    			swr.ne(SysUserPlus::getUserId, Long.valueOf(userId));
    		}
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		// 密码不显示
    		if (StringUtils.isNotEmpty(userPlusList)) {
    			userPlusList.forEach(s -> {
    				s.setMbti(mbti);
    				s.setPassword(null);
    			});
    		}
    		return userPlusList;
    	}
    	return null;
    }
    
    @Override
    public JSONObject getUserDisc(Long userId) {
    	log.info("查询用户{}的disc状态数据:", userId);
    	JSONObject json = new JSONObject();
		json.put("payStatus", "");
		json.put("testStaus", "");
    	Long surveyId = 10L;// 查询disc的
    	AnswerJson answerJson = new AnswerJson();
    	answerJson.setUserId(userId.toString());
    	answerJson.setSurveyId(surveyId); 
    	List<AnswerJson> list = answerJsonMapper.selectAnswerJsonLatest(answerJson);
    	if (StringUtils.isNotEmpty(list)) { // 有测试数据
    		json.put("testStaus", "finish");
    		// 查询用户的订单数据
    		LambdaQueryWrapper<SurveyOrder> wrapper = new LambdaQueryWrapper<SurveyOrder>();
    		wrapper.eq(SurveyOrder::getUserId, userId);
    		wrapper.eq(SurveyOrder::getSurveyId, surveyId);
    		wrapper.eq(SurveyOrder::getStatus, OrderPayStatus.HAVE_PAY); // 查询已支付的
    		wrapper.orderByDesc(SurveyOrder::getCreateTime);
    		List<SurveyOrder> orderList = surveyOrderService.list(wrapper);
    		if (StringUtils.isNotEmpty(orderList)) { // 有已支付订单
    			json.put("payStatus", "pay");
    		} 
    		
    	}
    	return json;
    }
    
    @Override
    public JSONObject getMatchUser(String mbti, Integer pageNum, Integer pageSize) {
    	JSONObject json = new JSONObject();
    	log.info("查询匹配性格人员mbti:" + mbti);
    	if (StringUtils.isEmpty(mbti)) {
    		json.put("matchPeople", null);
    		return json;
    	}
    	if (null == pageNum) {
    		pageNum = 1;
    	}
    	if (null == pageSize) {
    		pageSize = 10;
    	}
		LambdaQueryWrapper<MbtiTypeDesc> wrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
		wrapper.eq(MbtiTypeDesc::getCharaCode, mbti);
		MbtiTypeDesc mbtiTypeDesc = mbtiTypeDescMapper.selectOne(wrapper);
		String match = mbtiTypeDesc.getMatchMbti();
		String[] split = match.split(",");
		List<String> mbtis = Arrays.asList(split);
		
		int startIndex = (pageNum - 1) * pageSize;
    	List<UserSurveyResult> list = userSurveyResultMapper.getRandList11(mbtis, startIndex, pageSize);
    	
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		Map<Long, String> map = list.stream().collect(Collectors.toMap(UserSurveyResult::getUserId, UserSurveyResult::getAnswerResult));
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		// 密码不显示
    		if (StringUtils.isNotEmpty(userPlusList)) {
    			userPlusList.forEach(s -> {
    				s.setMbti(map.get(s.getUserId()));
    				s.setPassword(null);
    			});
    		}
    		json.put("matchPeople", userPlusList);
    	} else {
    		json.put("matchPeople", null);
    	}
    	return json;
    }
    
    @Override
    public JSONObject getSameUser(String mbti, Integer pageNum, Integer pageSize) {
    	JSONObject json = new JSONObject();
    	log.info("查询相同性格人员mbti:" + mbti);
    	if (StringUtils.isEmpty(mbti)) {
    		json.put("samePeople", null);
    		return json;
    	}
    	if (null == pageNum) {
    		pageNum = 1;
    	}
    	if (null == pageSize) {
    		pageSize = 10;
    	}
		
		int startIndex = (pageNum - 1) * pageSize;
    	List<UserSurveyResult> list = userSurveyResultMapper.getRandList22(mbti, startIndex, pageSize);
    	
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		Map<Long, String> map = list.stream().collect(Collectors.toMap(UserSurveyResult::getUserId, UserSurveyResult::getAnswerResult));
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		// 密码不显示
    		if (StringUtils.isNotEmpty(userPlusList)) {
    			userPlusList.forEach(s -> {
    				s.setMbti(map.get(s.getUserId()));
    				s.setPassword(null);
    			});
    		}
    		json.put("samePeople", userPlusList);
    	} else {
    		json.put("samePeople", null);
    	}
    	return json;
    }

    /**
     * 根据问卷主键查询问卷采集内容
     *
     * @param surveyId 问卷ID
     * @return 问卷答案结果json集合
     */
    @Override
    public List<AnswerJson> answerJsonBySurvey(Long surveyId) {
        return answerJsonMapper.answerJsonBySurvey(surveyId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject aiTest(AiTestVo vo) {
    	JSONObject result = new JSONObject();
    	Integer retest = vo.getRetest();
    	Long userId = vo.getUserId();
    	if (null == userId) {
    		log.error("未获取到userId，参数错误！");
    		throw new ServiceException("未获取到userId，参数错误！");
    	}
    	Long surveyId = vo.getSurveyId();
    	if (null == surveyId) {
    		surveyId = 1000L;
    	}
    	String aid = vo.getAid();
    	if (StringUtils.isEmpty(aid)) { // 第一次请求
    		// aid组合
    		aid = userId + "-" + surveyId + "-" + System.currentTimeMillis();
    		vo.setAid(aid); // 生成队列唯一标识
        	// 发送到队列
        	activeMQUtils.sendMessageToQueue(QueueDictionary.AI_TEST, vo);
			result.put("code", 201);
    	} else { // 后续轮询请求
    		// 查询缓存中是否有anId
    		JSONObject redisJson = redisCache.getCacheObject(aid);
    		log.info("轮询查询缓存中的数据为：{}:{}", userId, redisJson);
    		if (null != redisJson) {
    			Integer code = redisJson.getInteger("code");
    			String msg = redisJson.getString("msg");
    			if (null != code) {
    				if (code == 200) { // 正确结果
    					Long anId = redisJson.getLong("data");
    					result.put("anId", anId);
    					result.put("code", 200);
    				} else if (code == 201) { // 还在处理中
    		        	// 重新发送到队列，重新请求
    					vo.setRetest(null); // 置空
    		        	activeMQUtils.sendMessageToQueue(QueueDictionary.AI_TEST, vo);
    					result.put("code", 201);
    					result.put("msg", msg);
    				} else if (code == 500) { // 服务器异常
    					result.put("code", 500);
    					result.put("msg", msg);
    				}
    			} else {
    				result.put("code", 201);
    				result.put("msg", "系统正在生成AI测试结果，请稍等！");
    			}
    		} else { // 缓存中没有数据，说明还没有执行到
    			result.put("code", 201);
    			result.put("msg", "系统正在生成AI测试结果，请稍等！");
    		}
    	}
    	result.put("params", vo);
    	return result;
    }

    @Override
    public JSONObject aiTestByTime(AiTestVo vo) {
    	JSONObject result = new JSONObject();
    	Integer retest = vo.getRetest();
    	Long userId = vo.getUserId();
    	if (null == userId) {
    		log.error("未获取到userId，参数错误！");
    		throw new ServiceException("未获取到userId，参数错误！");
    	}
    	Long surveyId = vo.getSurveyId();
    	if (null == surveyId) {
    		surveyId = 1001L;
    	}
    	String aid = vo.getAid();
    	if (StringUtils.isEmpty(aid)) { // 第一次请求
    		// aid组合
    		aid = userId + "-" + surveyId + "-" + System.currentTimeMillis();
    		vo.setAid(aid); // 生成队列唯一标识
        	// 发送到队列
        	activeMQUtils.sendMessageToQueue(QueueDictionary.AI_TEST, vo);
			result.put("code", 201);
    	} else { // 后续轮询请求
    		// 查询缓存中是否有anId
    		JSONObject redisJson = redisCache.getCacheObject(aid);
    		log.info("轮询查询缓存中的数据为：{}:{}", userId, redisJson);
    		if (null != redisJson) {
    			Integer code = redisJson.getInteger("code");
    			String msg = redisJson.getString("msg");
    			if (null != code) {
    				if (code == 200) { // 正确结果
    					Long anId = redisJson.getLong("data");
    					result.put("anId", anId);
    					result.put("code", 200);
    				} else if (code == 201) { // 还在处理中
    		        	// 重新发送到队列，重新请求
    					vo.setRetest(null); // 置空
    		        	activeMQUtils.sendMessageToQueue(QueueDictionary.AI_TEST, vo);
    					result.put("code", 201);
    					result.put("msg", msg);
    				} else if (code == 500) { // 服务器异常
    					result.put("code", 500);
    					result.put("msg", msg);
    				}
    			} else {
    				result.put("code", 201);
    				result.put("msg", "系统正在生成AI测试结果，请稍等！");
    			}
    		} else { // 缓存中没有数据，说明还没有执行到
    			result.put("code", 201);
    			result.put("msg", "系统正在生成AI测试结果，请稍等！");
    		}
    	}
    	result.put("params", vo);
    	return result;
    }
    
    /**
     * 新增问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JSONObject insertAnswerJson(AnswerJson answerJson) {
    	log.debug("新增问卷答案结果，业务层接收到传过来的用户答题数据为userId：" + answerJson.getUserId());
    	log.debug("新增问卷答案结果，业务层接收到传过来的用户答题数据为surveyId：" + answerJson.getSurveyId());
    	log.debug("新增问卷答案结果，业务层接收到传过来的用户答题数据为json：" + answerJson.getAnswerJson());
		String userId = answerJson.getUserId();
		Long surveyId = answerJson.getSurveyId();
		Log.debug("新增问卷答案结果，业务层接收到传过来的用户id为：" + userId);
		if (StringUtils.isEmpty(userId)) {
			userId = SecurityUtils.getLoginUser().getUserId().toString();
			log.debug("新增问卷答案结果，未接收到控制层传过来的id，业务层查询当前用户id为：" + userId);
			answerJson.setUserId(userId);
		}
    	String json = answerJson.getAnswerJson();
    	if (StringUtils.isNotEmpty(json)) {
    		List<AnswerPlus> answerList = JSONArray.parseArray(json, AnswerPlus.class);
    		answerList.forEach(s -> {
    			s.setCreateTime(new Date());
    			s.setUserId(answerJson.getUserId());
    		});
    		answerPlusService.saveBatch(answerList); // 选项数据存储
    		if (2L == surveyId || 4L == surveyId) { // mbti测试结果
    			// 计算mbti结果
    			getResult(answerList, answerJson);
    			answerJson.setSurveyType(SurveyType.MBTI_28_QUESTION_SURVEY);
    		} else if (5L == surveyId) {
    			// 计算mbti93结果
    			getMbti93Result(answerList, answerJson);
    			answerJson.setSurveyType(SurveyType.MBTI_93_QUESTION_SURVEY);
    		} else if (10L == surveyId) { // disc测试结果
    			// 计算disc结果
    			getDiscResult(answerList, answerJson);
    			answerJson.setSurveyType(SurveyType.DISC_40_QUESTION_SURVEY);
    		}
    	}
        answerJson.setCreateTime(DateUtils.getNowDate());
        int row = answerJsonMapper.insertAnswerJson(answerJson);
        JSONObject result = new JSONObject();
        if (row > 0) {
        	result.put("anId", answerJson.getAnId());
        	return result;
        } else {
        	return null;
        }
    }
    
    /**
	 * 统计mbti93题结果得出性格结论
	 * @param list
	 */
	private String getMbti93Result(List<AnswerPlus> list, AnswerJson answerJson) {
		Long surveyId = answerJson.getSurveyId();
		if (StringUtils.isNotEmpty(list)) {
			Map<String, Integer> map = new HashMap<>();
			List<String> character = MBTI16Type.CHARACTER_4_TYPE;
			// 所有性格字符串
			List<String> types = list.stream().map(AnswerPlus::getOptionCharacterType).collect(Collectors.toList());
			for (AnswerPlus ase : list) {
				String type = ase.getOptionCharacterType();
				Integer num = map.get(type);
				if (num == null) {
					num = 0;
				}
				num++;
				map.put(type, num);
			}
			List<Map.Entry<String, Integer>> numList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
			Collections.sort(numList, new Comparator<Map.Entry<String, Integer>>() {
		        //降序排序
		        @Override
		        public int compare(Map.Entry<String, Integer> o1,
		                           Map.Entry<String, Integer> o2) {
		            return o2.getValue().compareTo(o1.getValue());
		        }
		    });
			// 按顺序封装
			List<String> resultStrs = new ArrayList<>();
			log.info("新增结果：排序numList:+" + numList);
			for (String cha : character) {
				for (Map.Entry<String, Integer> entry : numList) {
					String key = entry.getKey();
					if (cha.contains(key)) { // 拿第一个字符
						log.info("新增结果：取值第一个:+" + key);
						resultStrs.add(key);
						break;
					}
				}
			}
			log.info("新增结果：得出的结果字符串strs:+" + resultStrs);
			String join = String.join("", resultStrs);
			answerJson.setAnswerResult(join);
			String join2 = String.join("", types);
			answerJson.setAnswerResultOrigin(join2);
			
			// 存储到用户测评结果表里
			String userId = answerJson.getUserId();
			if (StringUtils.isEmpty(userId)) {
				userId = SecurityUtils.getLoginUser().getUserId().toString();
			}
			UserSurveyResult result = new UserSurveyResult();
			result.setUserId(Long.valueOf(userId));
//			result.setSurveyId(surveyId);
			result.setAnswerResult(join); // 设置类型
			result.setAnswerResultOrigin(answerJson.getAnswerResultOrigin()); // 设置原始数据
			result.setSurveyType(SurveyType.MBTI_93_QUESTION_SURVEY); // 
			result.setCreateTime(new Date());
			boolean flag = userSurveyResultService.saveOrUpdate(result);
			if (!flag) {
				throw new ServiceException("测评结果存储失败！");
			}
			
		}
		return null;
	}
    
    /**
     * 计算disc结果
     * @param list
     * @param answerJson
     * @return
     */
    private void getDiscResult(List<AnswerPlus> list, AnswerJson answerJson) {
    	if (StringUtils.isNotEmpty(list)) {
    		// 封装得分map
    		Map<String, Integer> map = new HashMap<>();
    		for (AnswerPlus ase : list) {
				String type = ase.getOptionCharacterType();
				Integer num = map.get(type);
				if (num == null) {
					num = 0;
				}
				num++;
				map.put(type, num);
    		}
			List<Map.Entry<String, Integer>> numList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
			Collections.sort(numList, new Comparator<Map.Entry<String, Integer>>() {
		        //降序排序
		        @Override
		        public int compare(Map.Entry<String, Integer> o1,
		                           Map.Entry<String, Integer> o2) {
		            return o2.getValue().compareTo(o1.getValue());
		        }
		    });
			Entry<String, Integer> entry = numList.get(0);
			String key = entry.getKey(); // 第一个类型就是结果类型
			String resultOrigin = JSONObject.toJSONString(map);
			answerJson.setAnswerResult(key);
			answerJson.setAnswerResultOrigin(resultOrigin);
			// 存储到disc user表
			String userId = answerJson.getUserId();
			if (StringUtils.isEmpty(userId)) {
				userId = SecurityUtils.getLoginUser().getUserId().toString();
			}
			UserDiscResult result = new UserDiscResult();
			result.setUserId(Long.valueOf(userId));
			result.setAnswerResult(key); // 设置类型
			result.setAnswerResultOrigin(answerJson.getAnswerResultOrigin()); // 设置原始数据
			result.setSurveyType(SurveyType.DISC_40_QUESTION_SURVEY); // 
			result.setCreateTime(new Date());
			boolean flag = userDiscResultService.saveOrUpdate(result);
			if (!flag) {
				throw new ServiceException("测评结果存储失败！");
			}
    	}
    }
    
	/**
	 * 统计mbti结果得出性格结论
	 * @param list
	 */
	private String getResult(List<AnswerPlus> list, AnswerJson answerJson) {
		Long surveyId = answerJson.getSurveyId();
		if (StringUtils.isNotEmpty(list)) {
			Map<String, Integer> map = new HashMap<>();
			List<String> character = MBTI16Type.CHARACTER_4_TYPE;
			// 所有性格字符串
			List<String> types = list.stream().map(AnswerPlus::getOptionCharacterType).collect(Collectors.toList());
			for (AnswerPlus ase : list) {
				String type = ase.getOptionCharacterType();
				Integer num = map.get(type);
				if (num == null) {
					num = 0;
				}
				num++;
				map.put(type, num);
			}
			List<Map.Entry<String, Integer>> numList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
			Collections.sort(numList, new Comparator<Map.Entry<String, Integer>>() {
		        //降序排序
		        @Override
		        public int compare(Map.Entry<String, Integer> o1,
		                           Map.Entry<String, Integer> o2) {
		            return o2.getValue().compareTo(o1.getValue());
		        }
		    });
			// 性格编码
			List<String> strs = new ArrayList<>();
			int index = 0;
			for (Map.Entry<String, Integer> entry : numList) {
				if (index > 3 ) {
					break;
				}
				String key = entry.getKey();
				strs.add(key);
				index++;
			}
			// 按顺序封装
			List<String> resultStrs = new ArrayList<>();
			for (String cha : character) {
				log.info("新增结果：字符串str:+" + strs);
				for (String str : strs) {
					if (StringUtils.isNotEmpty(str)) {
						if (cha.contains(str)) {
							resultStrs.add(str);
							break;
						}
					}
				}
			}
			String join = String.join("", resultStrs);
			answerJson.setAnswerResult(join);
			String join2 = String.join("", types);
			answerJson.setAnswerResultOrigin(join2);
			
			// 存储到用户测评结果表里
			String userId = answerJson.getUserId();
			if (StringUtils.isEmpty(userId)) {
				userId = SecurityUtils.getLoginUser().getUserId().toString();
			}
			UserSurveyResult result = new UserSurveyResult();
			result.setUserId(Long.valueOf(userId));
//			result.setSurveyId(surveyId);
			result.setAnswerResult(join); // 设置类型
			result.setAnswerResultOrigin(answerJson.getAnswerResultOrigin()); // 设置原始数据
			result.setSurveyType(SurveyType.MBTI_28_QUESTION_SURVEY); // 
			result.setCreateTime(new Date());
			boolean flag = userSurveyResultService.saveOrUpdate(result);
			if (!flag) {
				throw new ServiceException("测评结果存储失败！");
			}
			
		}
		return null;
	}

    /**
     * 修改问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    @Override
    public int updateAnswerJson(AnswerJson answerJson) {
        return answerJsonMapper.updateAnswerJson(answerJson);
    }

    /**
     * 批量删除问卷答案结果json
     *
     * @param anIds 需要删除的问卷答案结果json主键
     * @return 结果
     */
    @Override
    public int deleteAnswerJsonByAnIds(Long[] anIds) {
        return answerJsonMapper.deleteAnswerJsonByAnIds(anIds);
    }

    /**
     * 删除问卷答案结果json信息
     *
     * @param anId 问卷答案结果json主键
     * @return 结果
     */
    @Override
    public int deleteAnswerJsonByAnId(Long anId) {
        return answerJsonMapper.deleteAnswerJsonByAnId(anId);
    }

    /**
     * 根据问卷主键删除答案结果
     *
     * @param surveyIds
     * @return
     */
    @Override
    public int deleteAnswerJsonBySurveyIds(Long[] surveyIds) {
        return answerJsonMapper.deleteAnswerJsonBySurveyIds(surveyIds);
    }
}
