package com.shure.surdes.survey.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shure.surdes.survey.domain.UserSurveyResult;

/**
 * 用户测评结果mapper
 * @author color
 *
 */
public interface UserSurveyResultMapper extends BaseMapper<UserSurveyResult> {

	/**
	 * 随机查询限量人数id
	 * @param mbtis
	 * @param limit
	 * @return
	 */
	@Select("<script>"
			+ "select * "
			+ "from tb_user_survey_result "
			+ "where 1 = 1 and answer_result in "
			+ "<foreach item='item' index='index' collection='mbtis' open='(' separator=',' close=')'>" 
            	+ "#{item}" 
            + "</foreach> " 
			+ "order by rand() "
			+ "limit #{limit} "
		+ "</script>")
	public List<UserSurveyResult> getRandList1(@Param("mbtis") List<String> mbtis, @Param("limit")Integer limit);
	
	@Select("<script>"
			+ "select * "
			+ "from tb_user_survey_result "
			+ "where 1 = 1 and answer_result in "
			+ "<foreach item='item' index='index' collection='mbtis' open='(' separator=',' close=')'>" 
            	+ "#{item}" 
            + "</foreach> " 
			+ "order by rand() "
			+ "limit #{startIndex},#{limit} "
		+ "</script>")
	public List<UserSurveyResult> getRandList11(@Param("mbtis") List<String> mbtis, 
											   @Param("startIndex")Integer startIndex, 
											   @Param("limit")Integer limit);
	
	/**
	 * 随机查询相同的人
	 * @param mbti
	 * @param limit
	 * @return
	 */
	@Select("select * "
			+ "from tb_user_survey_result "
			+ "where answer_result = #{mbti} "
			+ "order by rand() "
			+ "limit #{limit} ")
	public List<UserSurveyResult> getRandList2(@Param("mbti")String mbti, @Param("limit")Integer limit);
	
	@Select("select * "
			+ "from tb_user_survey_result "
			+ "where answer_result = #{mbti} "
			+ "order by rand() "
			+ "limit #{startIndex},#{limit} ")
	public List<UserSurveyResult> getRandList22(@Param("mbti")String mbti, 
											    @Param("startIndex")Integer startIndex,
											    @Param("limit")Integer limit);
}
