package com.shure.surdes.survey.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.OrderPayStatus;
import com.shure.surdes.survey.constant.PayType;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.*;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.mapper.SurveyOderMapper;
import com.shure.surdes.survey.pay.zfb.AliPayDTO;
import com.shure.surdes.survey.pay.zfb.AliPayService;
import com.shure.surdes.survey.remote.PayApi;
import com.shure.surdes.survey.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 订单业务层实现类
 *
 * @author color
 */
@Service
@Slf4j
public class SurveyOderServiceImpl extends ServiceImpl<SurveyOderMapper, SurveyOrder> implements ISurveyOrderService {

    @Resource
    private AliPayService aliPayService;

    @Autowired
    IAnswerJsonService answerJsonService;

    @Autowired
    IUserSurveyResultService userSurveyResultService;

    @Autowired
    AnswerJsonMapper answerJsonMapper;

    @Autowired
    ISurveyService surveyService;

    @Autowired
    IJeepayOrderService jeepayOrderService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PayApi payApi;

    @Value("${surdes.price}")
    private String price;

    private final String currentMchNo = "M1728962831";

    @Value("${pay.return_url}")
    private String payReturnUrl;

    @Autowired
    private IStarAnswerService starAnswerService;

    @Autowired
    private ISurveyOrderService surveyOrderService;

    @Override
    public JSONObject submitOrder(SurveyOrder so) {

        //商户订单号生成
        String outTradeNo = "M" + new Date().getTime() + RandomUtil.randomInt(1000, 9999);


        JSONObject json = new JSONObject();
        log.info("提交订单，订单信息：" + so);
        Long anId = so.getAnId();
        Long surveyId = so.getSurveyId();
        Long userId = so.getUserId();
        String way = so.getPayWay();
        if (null == userId) {
            log.error("参数错误，没有userId！");
            json.put("code", 500);
            json.put("msg", "userId获取失败！");
            return json;
        }
        if (null == surveyId) {
            log.error("参数错误，没有问卷id！");
            json.put("code", 500);
            json.put("msg", "参数错误，没有问卷id！");
            return json;
        }
        // 查询问卷信息
        Survey survey = surveyService.selectSurveyBySurveyId(surveyId);
        if (survey == null) {
            log.error("没有找到对应的问卷！");
            json.put("code", 500);
            json.put("msg", "没有找到对应的问卷！");
            return json;
        }
        // 优惠价格
        Double discountPrice = survey.getDiscountPrice();
        Integer freeFlag = survey.getFreeFlag();
        if (0 == freeFlag) {
            log.error("免费问卷，无需支付！");
            json.put("code", 500);
            json.put("msg", "没有找到对应的问卷！");
            return json;
        }
        if (1001L == surveyId && null != anId) { // ai测试时间段
            AnswerJson answer = answerJsonMapper.selectAnswerJsonByAnId(anId);
            Date startTime = answer.getStartTime();
            Date endTime = answer.getEndTime();
            // 计算有几个月
            Long dayNum = DateUtil.between(startTime, endTime, DateUnit.DAY);
            Integer monthNum = (int) Math.ceil(NumberUtil.div(dayNum.toString(), "30").doubleValue());

//            Integer monthNum = DateUtils.getMonthNumBetweenTime(startTime, endTime);
            BigDecimal b1 = new BigDecimal(discountPrice);
            BigDecimal b2 = new BigDecimal(monthNum);
            discountPrice = b1.multiply(b2).doubleValue(); // 计算总价
            so.setAmount(discountPrice); // 设置订单总价格
        }
        if (null == anId) {
            // 查询最新的结果
            AnswerJson answer = new AnswerJson();
            answer.setUserId(so.getUserId().toString());
            answer.setSurveyId(so.getSurveyId());
            List<AnswerJson> list = answerJsonMapper.selectAnswerJsonLatest(answer);
            if (StringUtils.isNotEmpty(list)) {
                AnswerJson answerJson = list.get(0);
                so.setAnId(answerJson.getAnId());
            } else {
                log.error("没有测试结果，请重新测试完成之后再重新提交！");
                json.put("code", 500);
                json.put("msg", "没有测试结果，请重新测试完成之后再重新提交！");
                return json;
            }
        }
        if (1002L == surveyId) {
            LambdaQueryWrapper<StarAnswer> starAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
            starAnswerLambdaQueryWrapper.eq(StarAnswer::getStarUid, so.getStarUid());
            StarAnswer starAnswer = starAnswerService.getOne(starAnswerLambdaQueryWrapper);
//            boolean ex
            if (starAnswer == null) {
//                LambdaQueryWrapper<SurveyOrder> sowrapper = new LambdaQueryWrapper<SurveyOrder>();
//                sowrapper.eq(SurveyOrder::getUserId, userId);
//                sowrapper.eq(SurveyOrder::getSurveyId, surveyId);
//                sowrapper.orderByDesc(SurveyOrder::getCreateTime);
//                sowrapper.last("limit 1");
//                SurveyOrder surveyOrder = surveyOrderService.getOne(sowrapper);
//                if (surveyOrder == null) {
                    log.error("参数错误，没有starId！");
                    json.put("code", 500);
                    json.put("msg", "starUid无匹配用户！");
                    return json;
//                }

            }
        }
        so.setCreateTime(new Date());
        so.setMchOrderNo(outTradeNo);
        boolean flag = this.save(so);
        if (!flag) {
            log.error("订单提交失败，请稍后重试！");
            json.put("code", 500);
            json.put("msg", "订单提交失败，请稍后重试！");
            return json;
        }
        // 按照业务查询订单信息
        AliPayDTO dto = new AliPayDTO();
        // 订单编号用户id加上生成的订单编号
        dto.setOutTradeNo(outTradeNo);
        // 支付金额,单位为元
        dto.setTotalAmount(discountPrice.toString());
        // 订单标题，不可使用特殊符号
        dto.setSubject(so.getSurveyName());
        // 调用支付宝支付接口，返回支付链接

//        String productCode = "ALI_LITE";
//        String appId="";
//        String paySource = so.getPaySource();
//        String payLink = "";
//        if (StringUtils.isNotEmpty(paySource) && "pc".equals(paySource)) {
////            productCode = "FAST_INSTANT_TRADE_PAY";
//            productCode="ALI_QR";
//        String payLink = aliPayService.keyWebPayment(dto, "FAST_INSTANT_TRADE_PAY");
//        if ("ali".equals(so.getPayWay()))
//        {
//            log.error(payLink);
//            json.put("payData",payLink);
//            return json;
//        }
//        }
        String productCode = "codeImgUrl";
        String appId = null;
        String paySource = so.getPaySource();
        JSONObject jsonObject = new JSONObject();
        if ("pc".equals(paySource)) {
            jsonObject.put("payDataType", "codeUrl");
        }
        if ("ali".equals(so.getPayWay())) {
            productCode = "ALI_PC";//pc  支付宝二维码支付
//            appId = "670de353ca67c16d94f21a59";
            appId = "672ac9ede4b0b6471d448a5d";
            if (StringUtils.isNotEmpty(paySource) && "pc".equals(paySource)) {
//                productCode = "ALI_PC";//pc  支付宝二维码支付
//                appId = "670de353ca67c16d94f21a59";
            } else {
                productCode = "ALI_WAP";//支付宝app支付
//                appId = "670e1633ca676baee2378f58";
                jsonObject.put("payDataType", "aliapp");
            }
        } else if ("wx".equals(so.getPayWay())) {
//            jsonObject.put("payDataType", "codeImgUrl");
//            appId = "6720477eca6710ada06d2fab";
            appId = "672ac573e4b0b6471d448a5c";
            if (StringUtils.isNotEmpty(paySource) && "pc".equals(paySource)) {
                jsonObject.put("payDataType", "codeImgUrl");
                productCode = "WX_NATIVE";
            } else {
                jsonObject.put("payDataType", "wxapp");
                productCode = "WX_H5";
            }
        }
        jsonObject.put("totalAmount", NumberUtil.round((NumberUtil.mul(discountPrice.toString(), "100")), 0));
        //订单金额
        jsonObject.put("wayCode", productCode);                      //支付方式
        jsonObject.put("subject", so.getSurveyName());               //订单标题
//        jsonObject.put("currentMchNo", "M1728963411");               //jeepay商户id
        jsonObject.put("currentMchNo", "M1728962831");
        jsonObject.put("appId", appId);                              //jeepay商户应用id
        //同步回调地址
        jsonObject.put("returnUrl", payReturnUrl);
//        jsonObject.put("notifyUrl", "http://dawu.asiontech.com:2017/payCallback");
//
//        dto.setWayCode();

        //todo paylink更改
        redisCache.setCacheObject(outTradeNo, jsonObject, 60, TimeUnit.MINUTES);

        log.debug("支付传参：" + JSONObject.toJSONString(jsonObject));
        json = payApi.payOrder(outTradeNo);
//        json.put("code", 200);
//        json.put("pay", /*payUrl
//        */"http://192.168.137.254:9218/" + "api/design/pay/payOrders?mchOrderNo=" + outTradeNo);     //订单编号
//        json.put("order", so);
        return json;
    }

    @Override
    public SurveyOrder callbackUpdateOrder(String outTradeNo, String tradeNo, String payAmount,
                                           String sellerId, String timestamp, Integer status) {
        log.info("回调接口outTradeNo：" + outTradeNo);
        // 查询订单信息
        SurveyOrder surveyOrder = this.getById(Long.valueOf(outTradeNo)); // 商户订单号
        surveyOrder.setTradeNo(tradeNo); // 交易流水号
        surveyOrder.setSellerId(sellerId); // 交易收款人用户id
        surveyOrder.setBackTimestamp(timestamp); // 前端回调时间
        surveyOrder.setStatus(OrderPayStatus.HAVE_PAY); // 设置订单状态已付款
        surveyOrder.setPayTime(new Date()); // 订单完成时间
        surveyOrder.setPayAmount(payAmount); // 实际付款金额
        surveyOrder.setPayType(PayType.ALI_PAY); // 支付方式
        boolean flag = this.saveOrUpdate(surveyOrder);
        // 查询结果信息，将测试结果存入用户统一结果表
        Long anId = surveyOrder.getAnId();
        AnswerJson answer = answerJsonService.selectAnswerJsonByAnId(anId);

        Long surveyId = answer.getSurveyId();
        if (null != surveyId && 1000L == surveyId) { //AI测试
            UserSurveyResult result = new UserSurveyResult();
            result.setUserId(Long.valueOf(answer.getUserId()));
//		result.setSurveyId(surveyId);
            result.setAnswerResult(answer.getAnswerResult());
            result.setSurveyType(SurveyType.MBTI_AI_SURVEY_TEST); //ai测试
            result.setAnswerResultOrigin(answer.getAnswerResultOrigin());
            result.setCreateTime(new Date());
            userSurveyResultService.saveOrUpdate(result);
            if (!flag) {
                throw new ServiceException("更新订单信息失败！");
            }
        }

        return surveyOrder;
    }

    @Override
    public JSONObject getOrder(HttpServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        log.info("异步通知信息为：" + JSON.toJSONString(requestParams));
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        String outTradeNo = params.get("mchOrderNo");
        JSONObject jsonObject = selectByMchOrderNo(outTradeNo);
        return jsonObject;
    }

    @Override
    public JSONObject selectByMchOrderNo(String outTradeNo) {
        JSONObject jsonObject = new JSONObject();

        JeepayOrder jeepayOrder = jeepayOrderService.selectByMchOrderNo(outTradeNo);

        LambdaQueryWrapper<SurveyOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SurveyOrder::getMchOrderNo, outTradeNo);
        SurveyOrder surveyOrder = baseMapper.selectOne(wrapper);
        Survey survey = surveyService.selectSurveyBySurveyId(surveyOrder.getSurveyId());
        surveyOrder.setSurveyName(survey.getSurveyName());
        jsonObject.put("data", surveyOrder);
        if (jeepayOrder != null) {
            Byte status = jeepayOrder.getState();

            if (JeepayOrder.STATE_SUCCESS == status) {


           /* if (surveyOrder == null) {
                SurveyOrder newOrder = new SurveyOrder();
                surveyOrder.setStatus(1);
                surveyOrder.setPayAmount(NumberUtil.div(Integer.parseInt(jeepayOrder.getAmount().toString()), 100, 2)
                .toString());
                surveyOrder.setTradeNo(jeepayOrder.getMchOrderNo());
                surveyOrder.setCreateTime(jeepayOrder.getCreatedAt());
                surveyOrder.;
            } else */
                if (surveyOrder.getStatus() != 1) {
                    surveyOrder.setStatus(1);
                    surveyOrder.setTradeNo(jeepayOrder.getChannelOrderNo());
                    surveyOrder.setSellerId(jeepayOrder.getChannelUser());
//                surveyOrder.setBackTimestamp(jeepayOrder.get);
                    surveyOrder.setPayTime(jeepayOrder.getSuccessTime());
                    surveyOrder.setPayAmount(NumberUtil.round(NumberUtil.div(jeepayOrder.getAmount().toString(), "100"
                    ), 2).toString());
                    surveyOrder.setPayType(jeepayOrder.getIfCode());
//                surveyOrder.setPayTime();
                    baseMapper.update(surveyOrder, wrapper);

                }
                jsonObject.put("code", 200);
                jsonObject.put("data", surveyOrder);
                return jsonObject;
            } else {
                jsonObject.put("code", 201);
                if (JeepayOrder.STATE_FAIL == status) {
                    jsonObject.put("msg", "支付失败");
                } else if (JeepayOrder.STATE_CANCEL == status) {
                    jsonObject.put("msg", "订单已撤销");
                } else if (JeepayOrder.STATE_REFUND == status) {
                    jsonObject.put("msg", "订单已退款");
                } else if (JeepayOrder.STATE_CLOSED == status) {
                    jsonObject.put("msg", "订单已关闭");
                } else if (JeepayOrder.STATE_ING == status || JeepayOrder.STATE_INIT == status) {
                    jsonObject.put("msg", "订单未支付");
                } else {
                    jsonObject.put("msg", "支付状态异常，请重试");
                }
            }
        } else {
            jsonObject.put("code", 500);
            jsonObject.put("msg", "支付失败，请重新发起订单");
            return jsonObject;
        }
        return jsonObject;
    }

}
