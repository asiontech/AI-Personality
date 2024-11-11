package com.shure.surdes.survey.mapper;

import com.shure.surdes.survey.domain.AnswerJson;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 问卷答案结果jsonMapper接口
 *
 * @author Shure
 * @date 2021-10-18
 */
@Mapper
public interface AnswerJsonMapper {
    /**
     * 查询问卷答案结果json
     *
     * @param anId 问卷答案结果json主键
     * @return 问卷答案结果json
     */
    public AnswerJson selectAnswerJsonByAnId(Long anId);

    public AnswerJson selectAnswerJsonByWbUid(String wbUid);
    /**
     * 查询问卷答案结果json列表
     *
     * @param answerJson 问卷答案结果json
     * @return 问卷答案结果json集合
     */
    public List<AnswerJson> selectAnswerJsonList(AnswerJson answerJson);
    
    /**
     * 查询用户最新的答案结果，新添加限制，只查询最新的一条数据
     * @param answerJson
     * @return
     */
    public List<AnswerJson> selectAnswerJsonLatest(AnswerJson answerJson);
    
    /**
     * 查询用户的所有答题结果，按照开始时间、结束时间排序
     * @param answerJson
     * @return
     */
    public List<AnswerJson> selectAnswerJsonAll(AnswerJson answerJson);

    /**
     * 新增问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    public int insertAnswerJson(AnswerJson answerJson);

    /**
     * 修改问卷答案结果json
     *
     * @param answerJson 问卷答案结果json
     * @return 结果
     */
    public int updateAnswerJson(AnswerJson answerJson);

    /**
     * 删除问卷答案结果json
     *
     * @param anId 问卷答案结果json主键
     * @return 结果
     */
    public int deleteAnswerJsonByAnId(Long anId);

    /**
     * 批量删除问卷答案结果json
     *
     * @param anIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAnswerJsonByAnIds(Long[] anIds);

    /**
     * 根据问卷主键查询问卷采集内容
     *
     * @param surveyId 问卷ID
     * @return 问卷答案结果json集合
     */
    List<AnswerJson> answerJsonBySurvey(Long surveyId);

    /**
     * 根据问卷主键删除答案结果
     *
     * @param surveyIds
     * @return
     */
    int deleteAnswerJsonBySurveyIds(Long[] surveyIds);
}
