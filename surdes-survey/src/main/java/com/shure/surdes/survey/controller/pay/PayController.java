package com.shure.surdes.survey.controller.pay;

import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.survey.service.IJeepayOrderService;
import com.shure.surdes.survey.service.ISurveyOrderService;
import com.shure.surdes.survey.service.ISurveyService;
import com.shure.surdes.survey.service.impl.AnswerJsonServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "支付相关接口")
@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/pay")
public class PayController {

    @Autowired
    ISurveyOrderService surveyOrderService;

    @Autowired
    ISurveyService surveyService;

    @Autowired
    private IJeepayOrderService jeepayOrderService;


    @ApiOperation(value = "支付宝支付回调")
    @GetMapping("/callback")
    public AjaxResult aliCallback(HttpServletRequest request) {
        return AjaxResult.success(surveyOrderService.getOrder(request));
    }

    @ApiOperation(value = "微信支付回调")
    @GetMapping("/status/{orderNo}")
    public JSONObject aliCallback(@PathVariable("orderNo") String orderNo) {
//        LambdaQueryWrapper<JeepayOrder > jeepayOrderLambdaQueryWrapper=new LambdaQueryWrapper<>();
//        jeepayOrderLambdaQueryWrapper.eq(JeepayOrder::getMchNo,orderNo);
//        JeepayOrder jeepayOrder=jeepayOrderService.selectByMchOrderNo(orderNo);
        JSONObject jsonObject = surveyOrderService.selectByMchOrderNo(orderNo);
//        Byte status=jeepayOrder.getState();
//
//        if(JeepayOrder.STATE_SUCCESS==status){
//            jsonObject.put("code",200);
//            jsonObject.put("data",jeepayOrder);
//            jsonObject.put("msg","支付成功");
//        }else {
//            jsonObject.put("code", 201);
//            if (JeepayOrder.STATE_FAIL == status) {
//                jsonObject.put("msg", "支付失败");
//            } else if (JeepayOrder.STATE_CANCEL == status) {
//                jsonObject.put("msg", "订单已撤销");
//            } else if (JeepayOrder.STATE_REFUND == status) {
//                jsonObject.put("msg", "订单已退款");
//            } else if (JeepayOrder.STATE_CLOSED == status) {
//                jsonObject.put("msg", "订单已关闭");
//            }else {
//                jsonObject.put("msg", "支付状态异常，请重试");
//            }
//        }
        return jsonObject;
    }

    @GetMapping("/url/notify")
    public AjaxResult notifyURL() {
        System.out.println("notify");
        int y = 1 + 1;
        return AjaxResult.error();
    }

    @GetMapping("/url/return")
    public AjaxResult returnURL() {
        System.out.println("return");
        int y = 1 + 1;
        return AjaxResult.error();
    }

    @Autowired
    AnswerJsonServiceImpl answerJsonService;


    @GetMapping("/status/order")
    public Object getOrder(String userId, String surveyId) {
        return answerJsonService.selectOrder(userId, surveyId);
    }

}
