package com.shure.surdes.survey.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 订单
 * @author color
 *
 */
@Data
@TableName(value = "tb_order")
public class SurveyOrder {

	@TableId
	private Long orderId;
	
	/** 订单时间戳 */
    private Long orderTimestamp;
	
	/** 交易流水号 */
	private String tradeNo;
	
	private Long userId;
	
	private Long surveyId;
	
	@TableField(exist = false)
	private String surveyName;
	
	@TableField(exist = false)
	private String sruveyDesc;
	
	/** 金额 */
	private Double amount;
	
	/** 支付方式 */
	private String payType;
	
	/** 订单状态0-未付款1-已付款2-已退款 */
	private Integer status;
	
	private Date createTime;
	
	private Date payTime;
	
	private Date refundTime;
	
	/** 收款支付宝账号对应的用户号 */
	private String sellerId;
	
	/** 前台返回的时间 */
	private String backTimestamp;
	
	/** 实际付款金额 */
	private String payAmount;
	
	/** 关联结果 */
	private Long anId;
}
