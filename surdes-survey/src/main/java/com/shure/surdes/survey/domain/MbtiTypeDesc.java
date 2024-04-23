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
	
	/** 性格编码 */
	private String charaCode;
	
	/** 性格名称 */
	private String charaName;
	
	/** 关键词 */
	private String keyword;
	
	/** 类型解析 */
	private String description;
	
	/** 代表人物 */
	private String rePerson;
	
	/** 成长守护 */
	private String growthGuard;
	
	/** 职业指导 */
	private String jobGuide;
	
	/** 恋爱指导 */
	private String loveGuide;
	
	/** 优点 */
	private String advantage;
	
	/** 缺点 */
	private String defect;
	
	/** 匹配 */
	private String matchMbti;
	
	/** 不匹配 */
	private String notMatch;
	
}
