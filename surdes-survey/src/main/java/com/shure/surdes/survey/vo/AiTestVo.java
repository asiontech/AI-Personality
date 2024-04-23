package com.shure.surdes.survey.vo;

import lombok.Data;

/**
 * ai测试参数
 * @author color
 *
 */
@Data
public class AiTestVo {
	
	/** 用户id */
	private Long userId;

	/** sina用户uuid */
	private String uid;
	
	/** sina用户名称 */
	private String user;
	
	/** sina用户token */
	private String userToken;
}
