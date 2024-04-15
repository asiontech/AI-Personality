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
	
	private String rePerson;
	
	private String feature;
	
	private String job;
	
	private String guard;
	
	private String advantage;
	
	private String defect;
	
}
