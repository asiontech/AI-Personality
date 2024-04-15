package com.shure.surdes.survey.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 用户测试结果
 * @author color
 *
 */
@Data
@TableName(value = "tb_user_survey_result")
public class UserSurveyResult {

	@TableId
	private Long userId;
	
	private String surveyResult;
	
	private String surveyType;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
