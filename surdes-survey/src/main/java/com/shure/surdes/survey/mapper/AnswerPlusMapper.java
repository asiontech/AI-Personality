package com.shure.surdes.survey.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shure.surdes.survey.domain.AnswerPlus;
import com.shure.surdes.survey.vo.AnswerPlusVo;

/**
 * answermapper
 * @author color
 *
 */
public interface AnswerPlusMapper extends BaseMapper<AnswerPlus> {

	/**
	 * 查询问题答案统计数量
	 * @param surveyId
	 * @return
	 */
	@Select("SELECT question_id,option_code,count(option_code) num "
			+ "from tb_answer "
			+ "where survey_id = #{surveyId} " 
			+ "group by question_id,option_code "
			+ "order by question_id asc"
			)
	public List<AnswerPlusVo> getAnswerPlusStaNum(Long surveyId);
	
}
