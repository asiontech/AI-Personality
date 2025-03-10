package com.shure.surdes.survey.service;

import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.vo.AiTestVo;

import java.util.List;

/**
 * 问卷答案结果jsonService接口
 *
 * @author Shure
 * @date 2021-10-18
 */
public interface IAnswerJsonService {
    /**
     * 查询问卷答案结果json
     *
     * @param anId 问卷答案结果json主键
     * @return 问卷答案结果json
     */
    public AnswerJson selectAnswerJsonByAnId(Long anId);

    /**
     * 查询问卷答案结果json列表
     *
     * @param answerJson 问卷答案结果json
     * @return 问卷答案结果json集合
     */
    public List<AnswerJson> selectAnswerJsonList(AnswerJson answerJson);

    /**
     * 查询用户最新的答案结果
     *
     * @param answerJson
     * @return
     */
    public JSONObject selectAnswerJsonLatest(AnswerJson answerJson);

    /**
     * 查询mbti解析
     *
     * @param mbti
     * @return
     */
    public JSONObject getMbtiDesc(String mbti, String userId);

    /**
     * 查询用户的disc状态数据
     *
     * @param userId
     * @return
     */
    public JSONObject getUserDisc(Long userId);

    /**
     * 查询性格匹配用户
     *
     * @param mbti
     * @param pageNum
     * @param pageSize
     * @return
     */
    public JSONObject getMatchUser(String mbti, Integer pageNum, Integer pageSize);

    /**
     * 查询性格相同用户
     *
     * @param mbti
     * @param pageNum
     * @param pageSize
     * @return
     */
    public JSONObject getSameUser(String mbti, Integer pageNum, Integer pageSize);

    /**
     * 根据问卷主键查询问卷采集内容
     *
     * @param surveyId 问卷ID
     * @return 问卷答案结果json集合
     */
    public List<AnswerJson> answerJsonBySurvey(Long surveyId);

    /**
     * 新增问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    public JSONObject insertAnswerJson(AnswerJson answerJson);

    /**
     * ai测试获取结果并存储
     *
     * @param vo
     */
    public JSONObject aiTest(AiTestVo vo);

    /**
     * ai测试，选择时间段
     *
     * @param vo
     * @return
     */
    public JSONObject aiTestByTime(AiTestVo vo);

    /**
     * 修改问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    public int updateAnswerJson(AnswerJson answerJson);

    /**
     * 批量删除问卷答案结果json
     *
     * @param anIds 需要删除的问卷答案结果json主键集合
     * @return 结果
     */
    public int deleteAnswerJsonByAnIds(Long[] anIds);

    /**
     * 删除问卷答案结果json信息
     *
     * @param anId 问卷答案结果json主键
     * @return 结果
     */
    public int deleteAnswerJsonByAnId(Long anId);

    /**
     * 根据问卷主键删除答案结果
     *
     * @param surveyIds
     * @return
     */
    public int deleteAnswerJsonBySurveyIds(Long[] surveyIds);


    /**
     * 获取最新支付订单
     *
     * @param userId
     * @param surveyId
     * @return
     */
    public SurveyOrder selectOrder(String userId, String surveyId);
}
