package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * mbti 性格解析
 * @author color
 *
 */
@Data
@TableName(value = "tb_mbti_type_desc")
public class MbtiTypeDesc {

	@TableId
	private Long id;
	
	private String charaCode;
	
	private String charaName;
	
	private String description;
	
	/** 代表人物 */
	private String rePerson;
	
	/** 性格特征 */
	private String feature;
	
	/** 适合工作 */
	private String job;
	
	/** 成长道路 */
	private String guard;
	
	/** 优点 */
	private String advantage;
	
	/** 缺点 */
	private String defect;
	
	/** 匹配 */
	private String match;
	
	/** 不匹配 */
	private String notMatch;
	
}
