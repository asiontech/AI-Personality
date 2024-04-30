package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * disc类型解析
 * @author color
 *
 */
@Data
@TableName(value = "tb_disc_type_desc")
public class DiscTypeDesc {

	@TableId
	private Long discId;
	
	/** 类型 */
	private String type;
	
	/** 类型 */
	private String typeName;
	
	/** 类型名 */
	private String resume;
	
	/** 类型名 */
	@TableField("resume2")
	private String resume2;
	
	/** 描述 */
	@TableField("disc_desc")
	private String discDesc;
	
	/**  DISC十五分类的类型， */
	private String typologyType;
	
	/** 典型特征(中文) */
	private String feature;
	
	/** 当前类型在各维度上的特征(中文)  */
	private String dimInfo;
	
	private String keywordsEn;
	
	private String keywordsZh;
	
	/** 优势 */
	private String advantages;
	
	/** 劣势 */
	private String disadvantages;
	
	/** 代表人物 */
	private String representative;
	
	/** 工作风格 */
	private String workplaceStyle;
	
	/** 适合的职业类型 */
	private String favouredCareer;
	
	/** 对当前类型的人的鼓舞士气的建议方式， */
	private String mettleAdvice;
	
	/** 与当前类型的人(在日常、职场的) 社交接触策略(字段内容不全)  */
	private String contactAdvice;
	
	private String reportFeature;
	
	private String reportCareerDesc;
	
	
}
