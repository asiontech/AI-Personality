package com.shure.surdes.survey.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.MBTI16Type;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.AnswerPlus;
import com.shure.surdes.survey.domain.MbtiTypeDesc;
import com.shure.surdes.survey.domain.SysUserPlus;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.AnswerMapper;
import com.shure.surdes.survey.mapper.MbtiTypeDescMapper;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.service.IAnswerPlusService;
import com.shure.surdes.survey.service.IMbtiTypeDescService;
import com.shure.surdes.survey.service.ISysUserPlusService;
import com.shure.surdes.survey.service.IUserSurveyResultService;
import com.shure.surdes.system.service.ISysUserService;

/**
 * 问卷答案结果jsonService业务层处理
 *
 * @author Shure
 * @date 2021-10-18
 */
@Service
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
    ISysUserService sysUserService;
    
    @Autowired
    IUserSurveyResultService userSurveyResultService;
    
    @Autowired
    ISysUserPlusService sysUserPlusService;
    
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
    
    /**
     * 查询用户最新的答案结果
     */
    @Override
    public JSONObject selectAnswerJsonLatest(AnswerJson answerJson) {
    	JSONObject json = new JSONObject();
    	List<AnswerJson> list = answerJsonMapper.selectAnswerJsonLatest(answerJson);
    	if (StringUtils.isNotEmpty(list)) {
    		AnswerJson result = list.get(0);
    		// 用户得分
    		String origin = result.getAnswerResultOrigin();
    		// 用户性格结果
    		String mbti = result.getAnswerResult();
    		List<String> characters = MBTI16Type.CHARACTER_8_TYPE;
    		// 雷达图数据
    		List<Map<String, Object>> radar = new ArrayList<>();
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
    				map.put("name", cha);
    				map.put("value", count);
    				radar.add(map);
    			}
    		}
    		// 查询mbti性格解析
    		if (StringUtils.isNotEmpty(mbti)) {
    			LambdaQueryWrapper<MbtiTypeDesc> wrapper = new LambdaQueryWrapper<MbtiTypeDesc>();
    			wrapper.eq(MbtiTypeDesc::getCharaCode, mbti);
    			MbtiTypeDesc mbtiTypeDesc = mbtiTypeDescMapper.selectOne(wrapper);
    			String match = mbtiTypeDesc.getMatch();
    			// 查询性格匹配的人员
    			if (StringUtils.isNotEmpty(match)) {
    				String[] split = match.split(",");
    				List<SysUserPlus> matchPeople = getMatchPeople(Arrays.asList(split));
    				json.put("matchPeople", matchPeople);
    			}
    			json.put("mbtiTypeDesc", mbtiTypeDesc);
    		}
    		// 查询性格相同的人员
    		List<SysUserPlus> samePeople = getSamePeople(mbti);
    		json.put("samePeople", samePeople);
    		
    		// 查询性格匹配的人员
    		json.put("answerJson", result);
    		json.put("radar", radar);
    		return json;
    	}
    	return null;
    }
    
    /**
     * 获取性格匹配的人员
     * @param mbtis
     * @return
     */
    private List<SysUserPlus> getMatchPeople(List<String> mbtis) {
    	// 查询性格相同的人员
    	LambdaQueryWrapper<UserSurveyResult> wrapper = new LambdaQueryWrapper<UserSurveyResult>();
    	wrapper.in(UserSurveyResult::getSurveyResult, mbtis);
    	List<UserSurveyResult> list = userSurveyResultService.list(wrapper);
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		return userPlusList;
    	}
    	return null;
    }
    
    /**
     * 获取性格相同的人员
     * @param mbti
     * @return
     */
    private List<SysUserPlus> getSamePeople(String mbti) {
    	// 查询性格相同的人员
    	LambdaQueryWrapper<UserSurveyResult> wrapper = new LambdaQueryWrapper<UserSurveyResult>();
    	wrapper.eq(UserSurveyResult::getSurveyResult, mbti);
    	List<UserSurveyResult> list = userSurveyResultService.list(wrapper);
    	if (StringUtils.isNotEmpty(list)) {
    		// 用户ids
    		List<Long> userIds = list.stream().map(UserSurveyResult::getUserId).collect(Collectors.toList());
    		// 查询用户信息
    		LambdaQueryWrapper<SysUserPlus> swr = new LambdaQueryWrapper<SysUserPlus>();
    		swr.in(SysUserPlus::getUserId, userIds);
    		List<SysUserPlus> userPlusList = sysUserPlusService.list(swr);
    		return userPlusList;
    	}
    	return null;
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

    /**
     * 新增问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    @Override
    public int insertAnswerJson(AnswerJson answerJson) {
    	String json = answerJson.getAnswerJson();
    	if (StringUtils.isNotEmpty(json)) {
    		List<AnswerPlus> answerList = JSONArray.parseArray(json, AnswerPlus.class);
    		answerList.forEach(s -> {
    			s.setCreateTime(new Date());
    			s.setUserId(answerJson.getUserId());
    		});
    		answerPlusService.saveBatch(answerList);
    		// 计算结果
    		getResult(answerList, answerJson);
    		
    	}
        answerJson.setCreateTime(DateUtils.getNowDate());
        return answerJsonMapper.insertAnswerJson(answerJson);
    }
    
	/**
	 * 统计结果得出性格结论
	 * @param list
	 */
	private String getResult(List<AnswerPlus> list, AnswerJson answerJson) {
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
				for (String str : strs) {
					if (cha.contains(str)) {
						resultStrs.add(str);
						break;
					}
				}
			}
			String join = String.join("", resultStrs);
			answerJson.setAnswerResult(join);
			String join2 = String.join("", types);
			answerJson.setAnswerResultOrigin(join2);
			// 存储到用户测评结果表里
			UserSurveyResult result = new UserSurveyResult();
			result.setUserId(Long.valueOf(answerJson.getUserId()));
			result.setSurveyResult(join);
			result.setSurveyType(SurveyType.MBTI_28_QUESTION_SURVEY);
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
