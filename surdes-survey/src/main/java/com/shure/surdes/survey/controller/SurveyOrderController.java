package com.shure.surdes.survey.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.survey.domain.SurveyOrder;
import com.shure.surdes.survey.service.ISurveyOrderService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Api(tags = "订单api")
@RestController
@RequestMapping("/survey/order")
@Slf4j
public class SurveyOrderController {
	
	@Autowired
	ISurveyOrderService surveyOrderService;

    @ApiOperation(value = "提交订单，返回支付链接，以及订单信息")
    @PostMapping("/add")
	public AjaxResult submitOrder(@RequestBody SurveyOrder so) {
		return AjaxResult.success(surveyOrderService.submitOrder(so));
	}
    
    @ApiOperation(value = "查询订单状态，返回订单信息")
    @GetMapping("/{orderId}")
    public AjaxResult checkOrder(@PathVariable("orderId") Long orderId) {
    	log.debug("查询订单状态，orderId：" + orderId);
    	SurveyOrder surveyOrder = surveyOrderService.getById(orderId);
    	log.debug("查询订单状态，订单数据：" + surveyOrder);
    	return AjaxResult.success(surveyOrder);
    }
    
    @ApiOperation(value = "更新订单信息，返回更新之后的订单信息")
    @PostMapping("/update")
    public AjaxResult updateOrder(@RequestBody SurveyOrder so) {
    	boolean flag = surveyOrderService.updateById(so);
    	if (flag) {
    		return AjaxResult.success("修改成功");
    	} else {
    		return AjaxResult.error("修改失败");
    	}
    }
    
    @ApiOperation(value = "订单退款")
    public AjaxResult refoundOrder() {
    	
    	return null;
    }
}
