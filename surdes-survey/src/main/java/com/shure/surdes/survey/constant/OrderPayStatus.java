package com.shure.surdes.survey.constant;

/**
 * 订单状态
 * @author color
 *
 */
public class OrderPayStatus {

	/** 未支付 */
	public final static Integer NON_PAY = 0;
	
	/** 已支付 */
	public final static Integer HAVE_PAY = 1;
	
	/** 已退款 */
	public final static Integer REFUNDED = 2;
	
	/** 验签失败 */
	public final static Integer VERIFICATION_FAILED = 3;
}
