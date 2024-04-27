package com.shure.surdes.survey.vo;

import lombok.Data;

/**
 * 订单vo
 * @author color
 *
 */
@Data
public class OrderVo {
	
	private Long surveyId;
	
	private Long userId;
	
	private String surveyName;
	
	private String surveyDesc;

	private Double amount;
}
