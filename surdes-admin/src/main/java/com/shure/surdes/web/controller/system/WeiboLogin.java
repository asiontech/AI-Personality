package com.shure.surdes.web.controller.system;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.framework.web.service.SysLoginService;
import com.shure.surdes.web.controller.login.LoginByOtherSourceBody;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import me.zhyd.oauth.utils.AuthStateUtils;

/**
 * 微博登录
 * 
 * @author color
 *
 */
@Slf4j
@RestController
@CrossOrigin
public class WeiboLogin {

	@Autowired
	private SysLoginService loginService;
	
    
    @Value("${justauth.weibo.clientId}")
    private String clientId;
    
    @Value("${justauth.weibo.clientSecret}")
    private String clientSecret;
    
    @Value("${justauth.weibo.redirectUri}")
    private String redirectUri;

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
		log.debug("登录前uuid： "+ uuid);
		return ajax;
	}

	@PostMapping("/loginByWeibo")
	public AjaxResult loginByWeibo(@RequestBody LoginByOtherSourceBody loginByOtherSourceBody, HttpServletRequest request) {
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
