package com.shure.surdes.survey.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.Survey;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.SurveyMapper;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.ISurveyService;

/**
 * 问卷Service业务层处理
 *
 * @author Shure
 * @date 2021-10-18
 */
@Service
public class SurveyServiceImpl implements ISurveyService {
	
    @Autowired
    private SurveyMapper surveyMapper;
    
    @Autowired
    AnswerJsonMapper answerJsonMapper;
    
    @Autowired
    ISurveyOrderService surveyOrderService;

    /**
     * 查询问卷
     *
     * @param surveyId 问卷主键
     * @return 问卷
     */
    @Override
    public Survey selectSurveyBySurveyId(Long surveyId) {
        return surveyMapper.selectSurveyBySurveyId(surveyId);
    }

    /**
     * 查询问卷列表
     *
     * @param survey 问卷
     * @return 问卷
     */
    @Override
    public List<Survey> selectSurveyList(Survey survey) {
        return surveyMapper.selectSurveyList(survey);
    }
    
    /**
     * 查询问卷列表，并判断是否已经测试过
     */
    @Override
    public List<Survey> selectSurveyList(Survey survey, Long userId) {
    	List<Survey> list = surveyMapper.selectSurveyList(survey);
    	if (userId != null) {
    		AnswerJson answerJson = new AnswerJson();
    		answerJson.setUserId(userId.toString());
    		// 查询用户做过了的测试
    		List<AnswerJson> answerJsonList = answerJsonMapper.selectAnswerJsonList(answerJson);
    		// 查询用户支付订单
    		LambdaQueryWrapper<SurveyOrder> wrapper = new LambdaQueryWrapper<SurveyOrder>();
    		wrapper.eq(SurveyOrder::getUserId, userId);
    		wrapper.eq(SurveyOrder::getStatus, 1);
    		List<SurveyOrder> orderList = surveyOrderService.list(wrapper);
    		if (StringUtils.isNotEmpty(orderList)) {
    			List<Long> surveyIds = orderList.stream().map(SurveyOrder::getSurveyId).distinct().collect(Collectors.toList());
    			for (Survey su : list) {
    				Long surveyId = su.getSurveyId();
    				// 判断是否已经支付过
    				if (surveyIds.contains(surveyId)) {
    					su.setPayStatus("pay");
    				}
    			}
    		}
    		if (StringUtils.isNotEmpty(answerJsonList)) {
    			List<Long> surveyIds = answerJsonList.stream().map(AnswerJson::getSurveyId).collect(Collectors.toList());
    			for (Survey su : list) {
    				Long surveyId = su.getSurveyId();
    				// 判断是否已经测试过
    				if (surveyIds.contains(surveyId)) {
    					su.setTestStaus("finish"); // 已测评
    				}
    			}
    		}
    	}
//    	if (list.size() > 2) {
//    		int index1 = 0;
//    		int index2 = 1;
//    		
//    		Survey temp = list.remove(index2);
//    		
//    		list.add(index1, temp);
//    	}
    	return list;
    }

    /**
     * 新增问卷
     *
     * @param survey 问卷
     * @return 结果
     */
    @Override
    public int insertSurvey(Survey survey) {
        survey.setCreateTime(DateUtils.getNowDate());
        return surveyMapper.insertSurvey(survey);
    }

    /**
     * 修改问卷
     *
     * @param survey 问卷
     * @return 结果
     */
    @Override
    public int updateSurvey(Survey survey) {
        return surveyMapper.updateSurvey(survey);
    }

    /**
     * 批量永久删除问卷
     *
     * @param surveyIds 需要永久删除的问卷主键
     * @return 结果
     */
    @Override
    public int deleteSurveyBySurveyIds(Long[] surveyIds) {
        return surveyMapper.deleteSurveyBySurveyIds(surveyIds);
    }

    /**
     * 永久删除问卷信息
     *
     * @param surveyId 问卷主键
     * @return 结果
     */
    @Override
    public int deleteSurveyBySurveyId(Long surveyId) {
        return surveyMapper.deleteSurveyBySurveyId(surveyId);
    }

    /**
     * 批量删除问卷
     *
     * @param surveyIds 需要删除的问卷主键
     * @return 结果
     */
    @Override
    public int removeSurveyBySurveyIds(Long[] surveyIds) {
        return surveyMapper.removeSurveyBySurveyIds(surveyIds);
    }

    /**
     * 删除问卷信息
     *
     * @param surveyId 问卷主键
     * @return 结果
     */
    @Override
    public int removeSurveyBySurveyId(Long surveyId) {
        return surveyMapper.removeSurveyBySurveyId(surveyId);
    }

    /**
     * 批量发布问卷
     *
     * @param surveyIds 需要发布的问卷主键集合
     * @return 结果
     */
    @Override
    public int publishSurveyBySurveyIds(Long[] surveyIds) {
        return surveyMapper.publishSurveyBySurveyIds(surveyIds);
    }

    /**
     * 批量还原问卷
     *
     * @param surveyIds 需要发布的问卷主键集合
     * @return 结果
     */
    @Override
    public int restoreSurveyBySurveyIds(Long[] surveyIds) {
        return surveyMapper.restoreSurveyBySurveyIds(surveyIds);
    }

    /**
     * 撤销发布问卷
     *
     * @param surveyId 需要发布的问卷主键
     * @return 结果
     */
    @Override
    public int revokeSurveyBySurveyId(Long surveyId) {
        return surveyMapper.revokeSurveyBySurveyId(surveyId);
    }
}
