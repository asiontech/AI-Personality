package com.shure.surdes.survey.controller.pay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ijpay.core.enums.SignType;
import com.ijpay.core.enums.TradeType;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.IpKit;
import com.ijpay.core.kit.QrCodeKit;
import com.ijpay.core.kit.RsaKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.WxPayApiConfig;
import com.ijpay.wxpay.WxPayApiConfigKit;
import com.ijpay.wxpay.model.GetTransferInfoModel;
import com.ijpay.wxpay.model.MicroPayModel;
import com.ijpay.wxpay.model.OrderQueryModel;
import com.ijpay.wxpay.model.ProfitSharingModel;
import com.ijpay.wxpay.model.ReceiverModel;
import com.ijpay.wxpay.model.RefundModel;
import com.ijpay.wxpay.model.RefundQueryModel;
import com.ijpay.wxpay.model.SendRedPackModel;
import com.ijpay.wxpay.model.TransferModel;
import com.ijpay.wxpay.model.UnifiedOrderModel;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.pay.wx.H5SceneInfo;
import com.shure.surdes.survey.pay.wx.WxPayConfig;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875</p>
 *
 * <p>Node.js 版: https://gitee.com/javen205/TNWX</p>
 *
 * <p>微信支付 Demo</p>
 *
 * @author Javen
 */
@Controller
@RequestMapping("/wxPay")
@Slf4j
public class WxPayController extends AbstractWxPayApiController {
	
    @Autowired
    WxPayConfig wxPayConfig;

    private String notifyUrl;
    private String refundNotifyUrl;
    private static final String USER_PAYING = "USERPAYING";


    @Override
    public WxPayApiConfig getApiConfig() {
        WxPayApiConfig apiConfig;

        try {
            apiConfig = WxPayApiConfigKit.getApiConfig(wxPayConfig.getAppId());
        } catch (Exception e) {
            apiConfig = WxPayApiConfig.builder()
                    .appId(wxPayConfig.getAppId())
                    .mchId(wxPayConfig.getMchId())
                    .partnerKey(wxPayConfig.getPartnerKey())
                    .certPath(wxPayConfig.getCertPath())
                    .domain(wxPayConfig.getDomain())
                    .build();
        }
        // 微信支付回调地址
        notifyUrl = apiConfig.getDomain().concat("/wxPay/payNotify");
        // 微信退款回调地址
        refundNotifyUrl = apiConfig.getDomain().concat("/wxPay/refundNotify");
        return apiConfig;
    }


    /**
     * 测试接口
     */
    @GetMapping("/test")
    @ResponseBody
    public WxPayConfig test() {
        return wxPayConfig;
    }

    @GetMapping("/getKey")
    @ResponseBody
    public String getKey() {
        return WxPayApi.getSignKey(wxPayConfig.getMchId(), wxPayConfig.getPartnerKey(), SignType.MD5);
    }

    /**
     * 微信H5 支付
     * 注意：必须再web页面中发起支付且域名已添加到开发配置中
     */
    @ApiOperation(value = "微信H5 支付")
    @RequestMapping(value = "/wapPay", method = {RequestMethod.POST, RequestMethod.GET})
    public void wapPay(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            // TODO: 2024/10/14  
            ip = "127.0.0.1";
        }

        H5SceneInfo sceneInfo = new H5SceneInfo();

        H5SceneInfo.H5 h5_info = new H5SceneInfo.H5();
        h5_info.setType("Wap");
        //此域名必须在商户平台--"产品中心"--"开发配置"中添加
        h5_info.setWap_url("https://gitee.com/javen205/IJPay");
        h5_info.setWap_name("IJPay VIP 充值");
        sceneInfo.setH5Info(h5_info);

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-H5支付")
                .attach("Node.js 版:https://gitee.com/javen205/TNWX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1000")
                .spbill_create_ip(ip)
                .notify_url(notifyUrl)
                .trade_type(TradeType.MWEB.getTradeType())
                .scene_info(JSON.toJSONString(sceneInfo))
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);
        log.info(xmlResult);

        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String return_code = result.get("return_code");
        String return_msg = result.get("return_msg");
        if (!WxPayKit.codeIsOk(return_code)) {
            throw new RuntimeException(return_msg);
        }
        String result_code = result.get("result_code");
        if (!WxPayKit.codeIsOk(result_code)) {
            throw new RuntimeException(return_msg);
        }
        // 以下字段在return_code 和result_code都为SUCCESS的时候有返回

        String prepayId = result.get("prepay_id");
        String webUrl = result.get("mweb_url");

        log.info("prepay_id:" + prepayId + " mweb_url:" + webUrl);
        response.sendRedirect(webUrl);
    }

    /**
     * 公众号支付
     */
    @ApiOperation(value = "公众号支付")
    @RequestMapping(value = "/webPay", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult webPay(HttpServletRequest request, @RequestParam("total_fee") String totalFee) {
        // openId，采用 网页授权获取 access_token API：SnsAccessTokenApi获取
        String openId = (String) request.getSession().getAttribute("openId");
        if (openId == null) {
            // TODO: 2024/10/14  
            openId = "11111111";
        }

        if (StrUtil.isEmpty(openId)) {
            return AjaxResult.error("openId is null");
        }
        if (StrUtil.isEmpty(totalFee)) {
            return AjaxResult.error("请输入数字金额");
        }
        String ip = IpKit.getRealIp(request);
        if (StrUtil.isEmpty(ip)) {
            ip = "127.0.0.1";
        }

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-公众号支付")
                .attach("Node.js 版:https://gitee.com/javen205/TNWX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1000")
                .spbill_create_ip(ip)
                .notify_url(notifyUrl)
                .trade_type(TradeType.JSAPI.getTradeType())
                .openid(openId)
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);
        log.info(xmlResult);

        Map<String, String> resultMap = WxPayKit.xmlToMap(xmlResult);
        String returnCode = resultMap.get("return_code");
        String returnMsg = resultMap.get("return_msg");
        if (!WxPayKit.codeIsOk(returnCode)) {
            return AjaxResult.error(returnMsg);
        }
        String resultCode = resultMap.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            return AjaxResult.error(returnMsg);
        }

        // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回

        String prepayId = resultMap.get("prepay_id");

        Map<String, String> packageParams = WxPayKit.prepayIdCreateSign(prepayId, wxPayApiConfig.getAppId(),
                wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String jsonStr = JSON.toJSONString(packageParams);
        return AjaxResult.success(jsonStr);
    }

    /**
     * 扫码模式一
     */
    @ApiOperation(value = "扫码模式一")
    @RequestMapping(value = "/scanCode1", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult scanCode1(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam("productId") String productId) {
        try {
            if (StrUtil.isBlank(productId)) {
                return AjaxResult.error("productId is null");
            }
            WxPayApiConfig config = getApiConfig();
            //获取扫码支付（模式一）url
            String qrCodeUrl = WxPayKit.bizPayUrl(config.getPartnerKey(), config.getAppId(), config.getMchId(), productId);
            log.info(qrCodeUrl);
            //生成二维码保存的路径
            String name = "payQRCode1.png";
            log.info(ResourceUtils.getURL("classpath:").getPath());
            boolean encode = QrCodeKit.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 3, ErrorCorrectionLevel.H,
                    "png", 200, 200,
                    ResourceUtils.getURL("classpath:").getPath().concat("static").concat(File.separator).concat(name));
            if (encode) {
                //在页面上显示
                return AjaxResult.success(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("系统异常：" + e.getMessage());
        }
        return null;
    }

    /**
     * 扫码支付模式一回调
     */
    @ApiOperation(value = "扫码支付模式一回调")
    @RequestMapping(value = "/scanCodeNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String scanCodeNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            String result = HttpKit.readData(request);
            log.info("scanCodeNotify>>>" + result);
            /**
             * 获取返回的信息内容中各个参数的值
             */
            Map<String, String> map = WxPayKit.xmlToMap(result);
            for (String key : map.keySet()) {
                log.info("key= " + key + " and value= " + map.get(key));
            }

            String appId = map.get("appid");
            String openId = map.get("openid");
            String mchId = map.get("mch_id");
            String isSubscribe = map.get("is_subscribe");
            String nonceStr = map.get("nonce_str");
            String productId = map.get("product_id");
            String sign = map.get("sign");

            Map<String, String> packageParams = new HashMap<String, String>(6);
            packageParams.put("appid", appId);
            packageParams.put("openid", openId);
            packageParams.put("mch_id", mchId);
            packageParams.put("is_subscribe", isSubscribe);
            packageParams.put("nonce_str", nonceStr);
            packageParams.put("product_id", productId);

            WxPayApiConfig wxPayApiConfig = getApiConfig();

            String packageSign = WxPayKit.createSign(packageParams, wxPayApiConfig.getPartnerKey(), SignType.MD5);

            String ip = IpKit.getRealIp(request);
            if (StrUtil.isBlank(ip)) {
                ip = "127.0.0.1";
            }
            Map<String, String> params = UnifiedOrderModel
                    .builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .nonce_str(WxPayKit.generateStr())
                    .body("IJPay 让支付触手可及-扫码支付模式一")
                    .attach("Node.js 版:https://gitee.com/javen205/TNWX")
                    .out_trade_no(WxPayKit.generateStr())
                    .total_fee("1")
                    .spbill_create_ip(ip)
                    .notify_url(notifyUrl)
                    .trade_type(TradeType.NATIVE.getTradeType())
                    .openid(openId)
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);
            String xmlResult = WxPayApi.pushOrder(false, params);
            log.info("统一下单:" + xmlResult);
            /**
             * 发送信息给微信服务器
             */
            Map<String, String> payResult = WxPayKit.xmlToMap(xmlResult);
            String returnCode = payResult.get("return_code");
            String resultCode = payResult.get("result_code");
            if (WxPayKit.codeIsOk(returnCode) && WxPayKit.codeIsOk(resultCode)) {
                // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
                String prepayId = payResult.get("prepay_id");

                Map<String, String> prepayParams = new HashMap<String, String>(10);
                prepayParams.put("return_code", "SUCCESS");
                prepayParams.put("appid", appId);
                prepayParams.put("mch_id", mchId);
                prepayParams.put("nonce_str", System.currentTimeMillis() + "");
                prepayParams.put("prepay_id", prepayId);
                String prepaySign;
                if (sign.equals(packageSign)) {
                    prepayParams.put("result_code", "SUCCESS");
                } else {
                    prepayParams.put("result_code", "FAIL");
                    //result_code为FAIL时，添加该键值对，value值是微信告诉客户的信息
                    prepayParams.put("err_code_des", "订单失效");
                }
                prepaySign = WxPayKit.createSign(prepayParams, wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);
                prepayParams.put("sign", prepaySign);
                String xml = WxPayKit.toXml(prepayParams);
                log.error(xml);
                return xml;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 扫码支付模式二
     */
    @RequestMapping(value = "/scanCode2", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult scanCode2(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam("total_fee") String totalFee) {

        if (StrUtil.isBlank(totalFee)) {
            return AjaxResult.error("支付金额不能为空");
        }

        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-扫码支付模式二")
                .attach("Node.js 版:https://gitee.com/javen205/TNWXX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1")
                .spbill_create_ip(ip)
                .notify_url(notifyUrl)
                .trade_type(TradeType.NATIVE.getTradeType())
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);
        log.info("统一下单:" + xmlResult);

        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");
        System.out.println(returnMsg);
        if (!WxPayKit.codeIsOk(returnCode)) {
            return AjaxResult.error("error:" + returnMsg);
        }
        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            return AjaxResult.error("error:" + returnMsg);
        }
        //生成预付订单success

        String qrCodeUrl = result.get("code_url");
        String name = "payQRCode2.png";

        boolean encode = QrCodeKit.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 3, ErrorCorrectionLevel.H, "png", 200, 200,
                request.getSession().getServletContext().getRealPath("/") + File.separator + name);
        if (encode) {
            //在页面上显示
            return AjaxResult.success(name);
        }
        return null;
    }

    /**
     * 刷卡支付
     */
    @RequestMapping(value = "/micropay", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult microPay(HttpServletRequest request, HttpServletResponse response) {
        String authCode = request.getParameter("auth_code");
        String totalFee = request.getParameter("total_fee");
        if (StrUtil.isBlank(totalFee)) {
            return AjaxResult.error("支付金额不能为空");
        }
        if (StrUtil.isBlank(authCode)) {
            return AjaxResult.error("auth_code参数错误");
        }
        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            ip = "127.0.0.1";
        }
        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = MicroPayModel.builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-刷卡支付")
                .attach("Node.js 版:https://gitee.com/javen205/TNWXX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1")
                .spbill_create_ip(ip)
                .auth_code(authCode)
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.microPay(false, params);
        //同步返回结果
        log.info("xmlResult:" + xmlResult);
        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);
        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");
        if (!WxPayKit.codeIsOk(returnCode)) {
            //通讯失败
            String errCode = result.get("err_code");
            if (StringUtils.isNotBlank(errCode)) {
                //用户支付中，需要输入密码
                if (USER_PAYING.equals(errCode)) {
                    //等待5秒后调用【查询订单API】
                    // TODO: 2024/10/14

                }
            }
            log.info("提交刷卡支付失败>>" + xmlResult);
            return AjaxResult.error(returnMsg);
        }

        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            log.info("支付失败>>" + xmlResult);
            String errCodeDes = result.get("err_code_des");
            return AjaxResult.error(errCodeDes);
        }
        //支付成功
        return AjaxResult.success(xmlResult);
    }

    /**
     * 微信APP支付
     */
    @RequestMapping(value = "/appPay", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult appPay(HttpServletRequest request) {

        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            ip = "127.0.0.1";
        }

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-App支付")
                .attach("Node.js 版:https://gitee.com/javen205/TNWXX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1000")
                .spbill_create_ip(ip)
                .notify_url(notifyUrl)
                .trade_type(TradeType.APP.getTradeType())
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);

        log.info(xmlResult);
        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");
        if (!WxPayKit.codeIsOk(returnCode)) {
            return AjaxResult.error(returnMsg);
        }
        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            return AjaxResult.error(returnMsg);
        }
        // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
        String prepayId = result.get("prepay_id");

        Map<String, String> packageParams = WxPayKit.appPrepayIdCreateSign(wxPayApiConfig.getAppId(), wxPayApiConfig.getMchId(), prepayId,
                wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String jsonStr = JSON.toJSONString(packageParams);
        log.info("返回apk的参数:" + jsonStr);
        return AjaxResult.success(jsonStr);
    }

    /**
     * 微信小程序支付
     */
    @RequestMapping(value = "/miniAppPay", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public AjaxResult miniAppPay(HttpServletRequest request) {
        //需要通过授权来获取openId
        String openId = (String) request.getSession().getAttribute("openId");

        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            ip = "127.0.0.1";
        }

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = UnifiedOrderModel
                .builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .body("IJPay 让支付触手可及-小程序支付")
                .attach("Node.js 版:https://gitee.com/javen205/TNWXX")
                .out_trade_no(WxPayKit.generateStr())
                .total_fee("1000")
                .spbill_create_ip(ip)
                .notify_url(notifyUrl)
                .trade_type(TradeType.JSAPI.getTradeType())
                .openid(openId)
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);

        String xmlResult = WxPayApi.pushOrder(false, params);

        log.info(xmlResult);
        Map<String, String> result = WxPayKit.xmlToMap(xmlResult);

        String returnCode = result.get("return_code");
        String returnMsg = result.get("return_msg");
        if (!WxPayKit.codeIsOk(returnCode)) {
            return AjaxResult.error(returnMsg);
        }
        String resultCode = result.get("result_code");
        if (!WxPayKit.codeIsOk(resultCode)) {
            return AjaxResult.error(returnMsg);
        }
        // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
        String prepayId = result.get("prepay_id");
        Map<String, String> packageParams = WxPayKit.miniAppPrepayIdCreateSign(wxPayApiConfig.getAppId(), prepayId,
                wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256);
        String jsonStr = JSON.toJSONString(packageParams);
        log.info("小程序支付的参数:" + jsonStr);
        return AjaxResult.success(jsonStr);
    }

    @RequestMapping(value = "/queryOrder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String queryOrder(@RequestParam(value = "transactionId", required = false) String transactionId, @RequestParam(value = "outTradeNo", required = false) String outTradeNo) {
        try {
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = OrderQueryModel.builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .transaction_id(transactionId)
                    .out_trade_no(outTradeNo)
                    .nonce_str(WxPayKit.generateStr())
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            log.info("请求参数：{}", WxPayKit.toXml(params));
            String query = WxPayApi.orderQuery(params);
            log.info("查询结果: {}", query);
            return query;
        } catch (Exception e) {
            e.printStackTrace();
            return "系统错误";
        }
    }

    /**
     * 企业付款到零钱
     */
    @RequestMapping(value = "/transfer", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String transfer(HttpServletRequest request, @RequestParam("openId") String openId) {

        String ip = IpKit.getRealIp(request);
        if (StrUtil.isBlank(ip)) {
            ip = "127.0.0.1";
        }

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = TransferModel.builder()
                .mch_appid(wxPayApiConfig.getAppId())
                .mchid(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .partner_trade_no(WxPayKit.generateStr())
                .openid(openId)
                .check_name("NO_CHECK")
                .amount("100")
                .desc("IJPay 让支付触手可及-企业付款")
                .spbill_create_ip(ip)
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5, false);

        // 提现
        String transfers = WxPayApi.transfers(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
        log.info("提现结果:" + transfers);
        Map<String, String> map = WxPayKit.xmlToMap(transfers);
        String returnCode = map.get("return_code");
        String resultCode = map.get("result_code");
        if (WxPayKit.codeIsOk(returnCode) && WxPayKit.codeIsOk(resultCode)) {
            // 提现成功
        } else {
            // 提现失败
        }
        return transfers;
    }

    /**
     * 查询企业付款到零钱
     */
    @RequestMapping(value = "/transferInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String transferInfo(@RequestParam("partner_trade_no") String partnerTradeNo) {
        try {
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = GetTransferInfoModel.builder()
                    .nonce_str(WxPayKit.generateStr())
                    .partner_trade_no(partnerTradeNo)
                    .mch_id(wxPayApiConfig.getMchId())
                    .appid(wxPayApiConfig.getAppId())
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5, false);

            return WxPayApi.getTransferInfo(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取RSA加密公钥
     */
    @RequestMapping(value = "/getPublicKey", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getPublicKey() {
        try {
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = new HashMap<String, String>(4);
            params.put("mch_id", wxPayApiConfig.getMchId());
            params.put("nonce_str", String.valueOf(System.currentTimeMillis()));
            params.put("sign_type", "MD5");
            String createSign = WxPayKit.createSign(params, wxPayApiConfig.getPartnerKey(), SignType.MD5);
            params.put("sign", createSign);
            return WxPayApi.getPublicKey(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 企业付款到银行卡
     */
    @RequestMapping(value = "/payBank", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String payBank() {
        try {
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            //通过WxPayApi.getPublicKey接口获取RSA加密公钥
            //假设获取到的RSA加密公钥为PUBLIC_KEY(PKCS#8格式)
            final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6Bl76IwSvBTiibZ+CNRUA6BfahMshZ0WJpHD1GpmvcQjeN6Yrv6c9eIl6gB4nU3isN7bn+LmoVTpH1gHViaV2YyG/zXj4z4h7r+V+ezesMqqorEg38BCNUHNmhnw4/C0I4gBAQ4x0SJOGnfKGZKR9yzvbkJtvEn732JcEZCbdTZmaxkwlenXvM+mStcJaxBCB/h5xJ5VOF5nDbTPzLphIpzddr3zx/Jxjna9QB1v/YSKYXn+iuwruNUXGCvvxBWaBGKrjOdRTRy9adWOgNmtuYDQJ2YOfG8PtPe06ELKjmr2CfaAGrKKUroyaGvy3qxAV0PlT+UQ4ADSXWt/zl0o5wIDAQAB";

            Map<String, String> params = new HashMap<String, String>(10);
            params.put("mch_id", wxPayApiConfig.getMchId());
            params.put("partner_trade_no", System.currentTimeMillis() + "");
            params.put("nonce_str", System.currentTimeMillis() + "");
            //收款方银行卡号
            // TODO: 2024/10/14
            params.put("enc_bank_no", RsaKit.encryptByPublicKeyByWx("银行卡号", PUBLIC_KEY));
            //收款方用户名
            params.put("enc_true_name", RsaKit.encryptByPublicKeyByWx("银行卡持有人姓名", PUBLIC_KEY));
            //收款方开户行
            params.put("bank_code", "1001");
            params.put("amount", "1");
            params.put("desc", "IJPay 让支付触手可及-付款到银行卡");
            params.put("sign", WxPayKit.createSign(params, wxPayApiConfig.getPartnerKey(), SignType.HMACSHA256));
            return WxPayApi.payBank(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询企业付款到银行
     */
    @RequestMapping(value = "/queryBank", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String queryBank(@RequestParam("partner_trade_no") String partnerTradeNo) {
        try {
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = new HashMap<String, String>(4);
            params.put("mch_id", wxPayApiConfig.getMchId());
            params.put("partner_trade_no", partnerTradeNo);
            params.put("nonce_str", System.currentTimeMillis() + "");
            params.put("sign", WxPayKit.createSign(params, wxPayApiConfig.getPartnerKey(), SignType.MD5));
            return WxPayApi.queryBank(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加分账接收方
     */
    @RequestMapping(value = "/profitSharingAddReceiver", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String profitSharingAddReceiver() {
        try {
            ReceiverModel receiver = ReceiverModel.builder()
                    .type("PERSONAL_OPENID")
                    .account("openid")
                    .relation_type("PARTNER")
                    .build();

            Map<String, String> params = ProfitSharingModel.builder()
                    .mch_id(wxPayConfig.getMchId())
                    .appid(wxPayConfig.getAppId())
                    .nonce_str(WxPayKit.generateStr())
                    .receiver(JSON.toJSONString(receiver))
                    .build()
                    .createSign(wxPayConfig.getPartnerKey(), SignType.HMACSHA256);
            log.info("请求参数:{}", WxPayKit.toXml(params));
            String result = WxPayApi.profitSharingAddReceiver(params);
            log.info("请求结果:{}", result);
            return JSON.toJSONString(WxPayKit.xmlToMap(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求单次分账
     */
    @RequestMapping(value = "/profitSharing", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String profitSharing(@RequestParam(value = "transactionId") String transactionId) {
        List<ReceiverModel> list = new ArrayList<>();

        list.add(ReceiverModel.builder()
                .type("PERSONAL_OPENID")
                .account("openid")
                .amount(66)
                .description("IJPay 分账")
                .build());

        Map<String, String> params = ProfitSharingModel.builder()
                .mch_id(wxPayConfig.getMchId())
                .appid(wxPayConfig.getAppId())
                .nonce_str(WxPayKit.generateStr())
                .transaction_id(transactionId)
                .out_order_no(WxPayKit.generateStr())
                .receivers(JSON.toJSONString(list))
                .build()
                .createSign(wxPayConfig.getPartnerKey(), SignType.HMACSHA256);

        log.info("请求参数:{}", WxPayKit.toXml(params));
        String result = WxPayApi.profitSharing(params, wxPayConfig.getCertPath(), wxPayConfig.getMchId());
        log.info("请求结果:{}", result);
        return JSON.toJSONString(WxPayKit.xmlToMap(result));
    }


    /**
     * 微信退款
     */
    @RequestMapping(value = "/refund", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String refund(@RequestParam(value = "transactionId", required = false) String transactionId,
                         @RequestParam(value = "outTradeNo", required = false) String outTradeNo) {
        try {
            log.info("transactionId: {} outTradeNo:{}", transactionId, outTradeNo);

            if (StrUtil.isBlank(outTradeNo) && StrUtil.isBlank(transactionId)) {
                return "transactionId、out_trade_no二选一";
            }
            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = RefundModel.builder()
                    .appid(wxPayApiConfig.getAppId())
                    .mch_id(wxPayApiConfig.getMchId())
                    .nonce_str(WxPayKit.generateStr())
                    .transaction_id(transactionId)
                    .out_trade_no(outTradeNo)
                    .out_refund_no(WxPayKit.generateStr())
                    .total_fee("1")
                    .refund_fee("1")
                    .notify_url(refundNotifyUrl)
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            String refundStr = WxPayApi.orderRefund(false, params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
            log.info("refundStr: {}", refundStr);
            return refundStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 微信退款查询
     */
    @RequestMapping(value = "/refundQuery", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String refundQuery(@RequestParam("transactionId") String transactionId,
                              @RequestParam("out_trade_no") String outTradeNo,
                              @RequestParam("out_refund_no") String outRefundNo,
                              @RequestParam("refund_id") String refundId) {

        WxPayApiConfig wxPayApiConfig = getApiConfig();

        Map<String, String> params = RefundQueryModel.builder()
                .appid(wxPayApiConfig.getAppId())
                .mch_id(wxPayApiConfig.getMchId())
                .nonce_str(WxPayKit.generateStr())
                .transaction_id(transactionId)
                .out_trade_no(outTradeNo)
                .out_refund_no(outRefundNo)
                .refund_id(refundId)
                .build()
                .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);

        return WxPayApi.orderRefundQuery(false, params);
    }

    /**
     * 退款通知
     */
    @RequestMapping(value = "/refundNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String refundNotify(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        log.info("退款通知=" + xmlMsg);
        Map<String, String> params = WxPayKit.xmlToMap(xmlMsg);

        String returnCode = params.get("return_code");
        // 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态
        if (WxPayKit.codeIsOk(returnCode)) {
            String reqInfo = params.get("req_info");
            String decryptData = WxPayKit.decryptData(reqInfo, WxPayApiConfigKit.getWxPayApiConfig().getPartnerKey());
            log.info("退款通知解密后的数据=" + decryptData);
            // 更新订单信息
            // 发送通知等
            // TODO: 2024/10/14  
            Map<String, String> xml = new HashMap<String, String>(2);
            xml.put("return_code", "SUCCESS");
            xml.put("return_msg", "OK");
            return WxPayKit.toXml(xml);
        }
        return null;
    }

    @RequestMapping(value = "/sendRedPack", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String sendRedPack(HttpServletRequest request, @RequestParam("openId") String openId) {
        try {
            String ip = IpKit.getRealIp(request);
            if (StrUtil.isBlank(ip)) {
                ip = "127.0.0.1";
            }

            WxPayApiConfig wxPayApiConfig = getApiConfig();

            Map<String, String> params = SendRedPackModel.builder()
                    .nonce_str(WxPayKit.generateStr())
                    .mch_billno(WxPayKit.generateStr())
                    .mch_id(wxPayApiConfig.getMchId())
                    .wxappid(wxPayApiConfig.getAppId())
                    .send_name("IJPay 红包测试")
                    .re_openid(openId)
                    .total_amount("1000")
                    .total_num("1")
                    .wishing("感谢您使用 IJPay")
                    .client_ip(ip)
                    .act_name("感恩回馈活动")
                    .remark("点 start 送红包，快来抢!")
                    .build()
                    .createSign(wxPayApiConfig.getPartnerKey(), SignType.MD5);
            String result = WxPayApi.sendRedPack(params, wxPayApiConfig.getCertPath(), wxPayApiConfig.getMchId());
            System.out.println("发送红包结果:" + result);
            Map<String, String> map = WxPayKit.xmlToMap(result);
            return JSON.toJSONString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步通知
     * 这里面可以写判断支付成功之后的及具体业务需求
     */
    @RequestMapping(value = "/payNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String payNotify(HttpServletRequest request) {
        String xmlMsg = HttpKit.readData(request);
        log.info("支付通知=" + xmlMsg);
        Map<String, String> params = WxPayKit.xmlToMap(xmlMsg);

        String returnCode = params.get("return_code");

        // 注意重复通知的情况，同一订单号可能收到多次通知，请注意一定先判断订单状态
        // 注意此处签名方式需与统一下单的签名类型一致
        if (WxPayKit.verifyNotify(params, getApiConfig().getPartnerKey(), SignType.HMACSHA256)) {
            if (WxPayKit.codeIsOk(returnCode)) {
                // 更新订单信息
                // TODO: 2024/10/14
                // 发送通知等
                Map<String, String> xml = new HashMap<String, String>(2);
                xml.put("return_code", "SUCCESS");
                xml.put("return_msg", "OK");
                return WxPayKit.toXml(xml);
            }
        }
        return null;
    }
}

