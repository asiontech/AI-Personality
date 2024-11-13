package com.shure.surdes.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.core.domain.model.WxLoginBody;
import com.shure.surdes.common.utils.http.HttpUtils;
import com.shure.surdes.framework.web.service.SysLoginService;
import com.shure.surdes.web.controller.login.LoginByOtherSourceBody;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 微博登录
 *
 * @author color
 */
@Slf4j
@RestController
@CrossOrigin
public class OtherLoginController {

    @Autowired
    private SysLoginService loginService;


    @Value("${justauth.weibo.clientId}")
    private String clientId;

    @Value("${justauth.weibo.clientSecret}")
    private String clientSecret;

    @Value("${justauth.weibo.redirectUri}")
    private String redirectUri;

    @Value("${justauth.wx.appId}")
    private String appId;

    @Value("${justauth.wx.appSecret}")
    private String appSecret;

    @GetMapping("/preLoginByWeibo")
    public AjaxResult PreLoginByWeibo() {
        AjaxResult ajax = AjaxResult.success();
        AuthRequest authRequest = new AuthWeiboRequest(AuthConfig.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .ignoreCheckState(true)
                .build());
        String uuid = AuthStateUtils.createState();
        String authorizeUrl = authRequest.authorize(uuid);
        // 存储
        ajax.put("authorizeUrl", authorizeUrl);
        ajax.put("uuid", uuid);
        log.debug("登录前uuid： " + uuid);
        return ajax;
    }

    @PostMapping("/loginByWeibo")
    public AjaxResult loginByWeibo(@RequestBody LoginByOtherSourceBody loginByOtherSourceBody,
								   HttpServletRequest request) {
        JSONObject json = loginService.loginByOtherSource(loginByOtherSourceBody.getCode(),
                loginByOtherSourceBody.getSource(),
                loginByOtherSourceBody.getUuid(),
                loginByOtherSourceBody.getAnId(),
                request);

//		ajax.put("token", json.getString("token"));
//		ajax.put("sinaToken", json.getString("sinaToken"));
//		ajax.put("userId", json.getString("userId"));
        if (json != null) {
            Integer code = json.getInteger("code");
            if (null != code) {
                if (200 == code) {
                    return AjaxResult.success(json);
                } else {
                    String msg = json.getString("msg");
                    return AjaxResult.error(msg, json);
                }
            }
        }
        return AjaxResult.error("登录失败！");
    }




    @PostMapping("/loginByWx")
    public AjaxResult wxLogin(@RequestBody WxLoginBody wxLoginBody, HttpServletRequest request) {
        log.info("登录参数：" + JSON.toJSONString(wxLoginBody));
        String codeData = wxLoginBody.getCode();
        //秘钥
        String encryptedIv = wxLoginBody.getEncryptedIv();
        //加密数据
        String encryptedData = wxLoginBody.getEncryptedData();

        Long anId=wxLoginBody.getAnId();

        //想微信服务器发送请求获取用户信息
        String url =
                "https://api.weixin.qq.com/snns/jscode2session?appid=" + appId + "&secret=" + appSecret + "&js_code" +
                        "=" + codeData + "&grant_type=authorizatinon_code";
//		String res = restTemplate.getForObject(url, String.class);
        JSONObject jsonObject = JSONObject.parseObject(HttpUtils.sendGet(url, null));

        //获取session_key和openid
        String sessionKey = jsonObject.getString("session_key");
        String openid = jsonObject.getString("openid");

        //解密
        String decryptResult = "";
        try {
            //如果没有绑定微信开放平台，解析结果是没有unionid的。
            decryptResult = SysLoginService.decryptS5(encryptedData, "utf-8", sessionKey, encryptedIv);
        } catch (Exception e) {
            log.error(e.getMessage());
            return AjaxResult.error("微信登录失败！");
        }

        if (StringUtils.hasText(decryptResult)) {
            //如果解析成功,获取token
            JSONObject jsonObject1 = loginService.wxLogin(decryptResult,anId, request);
            if (jsonObject1 != null) {
                Integer code = jsonObject1.getInteger("code");
                if (null != code) {
                    if (200 == code) {
                        return AjaxResult.success(jsonObject1);
                    } else {
                        String msg = jsonObject1.getString("msg");
                        return AjaxResult.error(msg, jsonObject1);
                    }
                }
            }
            return AjaxResult.error("登录失败！");
        } else {
            return AjaxResult.error("微信登录失败！");
        }
    }

    @GetMapping("/callback")
    public AjaxResult loginByWeibo(AuthCallback callback) {
        AjaxResult ajax = AjaxResult.success();
        JSONObject json = loginService.loginByOtherSource(callback);
        log.debug("token:" + JSONObject.toJSONString(json));
        ajax.put("token", json.getString("token"));
        ajax.put("sinaToken", json.getString("sinaToken"));
        return ajax;
    }
}
