package com.shure.surdes.survey.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * disc测试用户数据记录
 * @author color
 *
 */
@Data
@TableName(value = "tb_user_disc_result")
public class UserDiscResult {

	@TableId
	private Long userId;
	
	/** 测试结果 */
	private String answerResult;
	
	/** 测试结果原始值 */
	private String answerResultOrigin;
	
	/** 测试类型 */
	private String surveyType;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
