package com.shure.surdes.framework.web.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.core.domain.model.WxLoginBody;
import com.shure.surdes.common.utils.http.HttpUtils;
import com.shure.surdes.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.shure.surdes.common.constant.Constants;
import com.shure.surdes.common.core.domain.entity.SysUser;
import com.shure.surdes.common.core.domain.model.LoginUser;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.exception.ServiceException;
import com.shure.surdes.common.exception.user.CaptchaException;
import com.shure.surdes.common.exception.user.CaptchaExpireException;
import com.shure.surdes.common.exception.user.UserPasswordNotMatchException;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.MessageUtils;
import com.shure.surdes.common.utils.SecurityUtils;
import com.shure.surdes.common.utils.ServletUtils;
import com.shure.surdes.common.utils.ip.IpUtils;
import com.shure.surdes.framework.manager.AsyncManager;
import com.shure.surdes.framework.manager.factory.AsyncFactory;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.SysUserPlus;
import com.shure.surdes.survey.remote.SinaApi;
import com.shure.surdes.survey.service.IAnswerJsonService;
import com.shure.surdes.survey.service.ISysUserPlusService;
import com.shure.surdes.system.service.ISysConfigService;
import com.shure.surdes.system.service.ISysUserService;

import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeiboRequest;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Slf4j
@Component
public class SysLoginService {
	@Autowired
	private TokenService tokenService;

	@Resource
	private AuthenticationManager authenticationManager;

	@Autowired
	private RedisCache redisCache;

	@Autowired
	private ISysUserService userService;

	@Autowired
	private ISysConfigService configService;

	@Autowired
	SysPermissionService permissionService;

	@Autowired
	ISysUserPlusService sysUserPlusService;
	
	UserDetailsServiceImpl UserDetailsServiceImpl;
	
	@Autowired
	IAnswerJsonService answerJsonService;
	
	@Autowired
	SinaApi sinaApi;

	/**
	 * 登录验证
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public String login(String username, String password, String code, String uuid) {
		boolean captchaOnOff = configService.selectCaptchaOnOff();
		// 验证码开关
		if (captchaOnOff) {
			validateCaptcha(username, code, uuid);
		}
		// 用户验证
		Authentication authentication = null;
		try {
			// 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
			authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (Exception e) {
			if (e instanceof BadCredentialsException) {
				AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
						MessageUtils.message("user.password.not.match")));
				throw new UserPasswordNotMatchException();
			} else {
				AsyncManager.me()
						.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
				throw new ServiceException(e.getMessage());
			}
		}
		AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS,
				MessageUtils.message("user.login.success")));
		LoginUser loginUser = (LoginUser) authentication.getPrincipal();
		recordLoginInfo(loginUser.getUserId());
		// 生成token
		return tokenService.createToken(loginUser);
	}

	/**
	 * 校验验证码
	 *
	 * @param username 用户名
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public void validateCaptcha(String username, String code, String uuid) {
		String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
		String captcha = redisCache.getCacheObject(verifyKey);
		redisCache.deleteObject(verifyKey);
		if (captcha == null) {
			AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
					MessageUtils.message("user.jcaptcha.expire")));
			throw new CaptchaExpireException();
		}
		if (!code.equalsIgnoreCase(captcha)) {
			AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
					MessageUtils.message("user.jcaptcha.error")));
			throw new CaptchaException();
		}
	}

	/**
	 * 记录登录信息
	 *
	 * @param userId 用户ID
	 */
	public void recordLoginInfo(Long userId) {
		SysUser sysUser = new SysUser();
		sysUser.setUserId(userId);
		sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
		sysUser.setLoginDate(DateUtils.getNowDate());
		userService.updateUserProfile(sysUser);
	}

	@Value("${justauth.weibo.clientId}")
	private String clientId;

	@Value("${justauth.weibo.clientSecret}")
	private String clientSecret;

	@Value("${justauth.weibo.redirectUri}")
	private String redirectUri;

	public JSONObject loginByOtherSource(AuthCallback callback) {
		log.debug("callback:" + JSONObject.toJSONString(callback));
		// 先到数据库查询这个人曾经有没有登录过，没有就注册
		// 创建授权request
		AuthRequest authRequest = new AuthWeiboRequest(
				AuthConfig.builder().clientId(clientId).clientSecret(clientSecret).redirectUri(redirectUri).build());
		AuthResponse<AuthUser> login = authRequest.login(callback);
		if (login == null) {
			throw new ServiceException("登录失败，获取第三方账户信息失败！");
		}
		log.debug("login:" + JSONObject.toJSONString(login));
		// 先查询数据库有没有该用户
		AuthUser authUser = login.getData();
		log.debug("authUser:" + JSONObject.toJSONString(authUser));

		SysUser sysUser = new SysUser();
		sysUser.setUserName(authUser.getUsername()); // 用户名
		sysUser.setSource(authUser.getSource()); // 来源
		List<SysUser> sysUsers = userService.selectUserListNoDataScope(sysUser);
		if (sysUsers.size() > 1) {
			throw new ServiceException("第三方登录异常，账号重叠");
		} else if (sysUsers.size() == 0) {
			SysUserPlus sysUserPlus = new SysUserPlus();
			sysUserPlus.setUserName(authUser.getUsername()); // 用户名
			sysUserPlus.setSource(authUser.getSource()); // 来源
			sysUserPlus.setNickName(authUser.getNickname()); // 昵称
			sysUserPlus.setAvatar(authUser.getAvatar()); // 头像
			sysUserPlus.setEmail(authUser.getEmail()); // 邮箱
			sysUserPlus.setRemark(authUser.getRemark()); // 备注
			sysUserPlus.setSinaUuid(authUser.getUuid());
			sysUserPlus.setBlog(authUser.getBlog());
			sysUserPlus.setLocation(authUser.getLocation());
			sysUserPlus.setLargeAvatar(authUser.getRawUserInfo().getString("avatar_large")); // 大头像
			sysUserPlus.setGender(authUser.getGender().getDesc()); // 性别

			// 相当于注册
//            sysUser.setNickName(authUser.getNickname()); // 昵称
//            sysUser.setAvatar(authUser.getAvatar()); // 头像
//            sysUser.setEmail(authUser.getEmail()); // 邮箱
//            sysUser.setRemark(authUser.getRemark()); // 备注
//            userService.registerUserAndGetUserId(sysUser);
            userService.registerUser(sysUser);

			// 保存新用户，相当于注册
			sysUserPlusService.save(sysUserPlus);

			AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.REGISTER,
					MessageUtils.message("user.register.success")));
			// 授权普通用户
			Long[] roleIds = { 2L };
			userService.insertUserAuth(sysUser.getUserId(), roleIds);
		} else {
			sysUser = sysUsers.get(0);
		}
		log.debug("sysUser:" + JSONObject.toJSONString(sysUser));
		AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.LOGIN_SUCCESS,
				MessageUtils.message("user.login.success")));
		// 注册成功或者是已经存在的用户
		LoginUser loginUser = new LoginUser(sysUser.getUserId(), sysUser.getDeptId(), sysUser,
				permissionService.getMenuPermission(sysUser));
		recordLoginInfo(loginUser.getUserId());
		// 生成token
		JSONObject json = new JSONObject();
		json.put("token", tokenService.createToken(loginUser));
		json.put("sinaToken", authUser.getToken().getAccessToken());
		return json;
	}

	public JSONObject loginByOtherSource(String code, String source, String uuid, Long anId, HttpServletRequest request) {
		JSONObject json = new JSONObject();
		log.debug("登录接口时间点："+ System.currentTimeMillis());
		log.debug("登录后uuid： "+ uuid);
		// 防止重复提交直接缓存获取数据
		JSONObject result = redisCache.getCacheObject("weibo-user:" + uuid);
		if (result != null) {
			String token = result.getString("token");
			Long userId = result.getLong("userId");
			SysUser sysUser = userService.selectUserById(userId);
			LoginUser loginUser = tokenService.getLoginUser(token);
			sysUser.getUserId();
			if (null == loginUser) {
				// 注册成功或者是已经存在的用户
				loginUser = new LoginUser(sysUser.getUserId(), sysUser.getDeptId(), sysUser,
						permissionService.getMenuPermission(sysUser));
				token = tokenService.createToken(loginUser);
				// 存储到上下文
		        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
		        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		        
				recordLoginInfo(loginUser.getUserId());
			} else {
				tokenService.verifyToken(loginUser);
			}
			if (null != anId) {
				AnswerJson answerJson = answerJsonService.selectAnswerJsonByAnId(anId);
				answerJson.setUserId(userId.toString());
				int row = answerJsonService.updateAnswerJson(answerJson);
				if (row < 1) {
					log.info("更新用户userId：{}问卷结果失败", userId);
				}
			}
			return result;
		}
		if (StringUtils.isEmpty(code)) {
//			throw new ServiceException("登录失败，获取参数code失败！");
			json.put("code", 500);
			json.put("msg", "登录失败，获取参数code失败！");
			return json;
		}
		// 先到数据库查询这个人曾经有没有登录过，没有就注册
		// 创建授权request
		AuthRequest authRequest = new AuthWeiboRequest(AuthConfig.builder()
													   .clientId(clientId)
													   .clientSecret(clientSecret)
													   .redirectUri(redirectUri)
													   .ignoreCheckState(true) // 忽略校验code
													   .build());
		AuthResponse<AuthUser> login = authRequest.login(AuthCallback.builder().state(uuid).code(code).build());
		log.debug("login:" + JSONObject.toJSONString(login));
		if (login == null) {
//			throw new ServiceException("登录失败，获取第三方账户信息失败！");
			json.put("code", 500);
			json.put("msg", "登录失败，获取第三方账户信息失败！");
			return json;
		}
		int loginCode = login.getCode();
		if (5009 == loginCode) {
//			throw new ServiceException("微博登录信息获取失败，请返回重新登录！");
			json.put("code", 500);
			json.put("msg", "微博登录信息获取失败，请返回重新登录！");
			return json;
		}
		if (5000 == loginCode) {
//			throw new ServiceException("登录失败，获取第三方账户信息失败！");
			json.put("code", 500);
			json.put("msg", "登录失败，获取第三方账户信息失败！");
			return json;
		}
		// 先查询数据库有没有该用户
		AuthUser authUser = login.getData();
		log.debug("authUser:" + JSONObject.toJSONString(authUser));
		if (authUser == null) {
//			throw new ServiceException("登录失败，获取第三方账户信息失败！");
			json.put("code", 500);
			json.put("msg", "登录失败，获取第三方账户信息失败！");
			return json;
		}
		SysUserPlus sysUserPlus = new SysUserPlus();
		sysUserPlus.setUserName(authUser.getUsername()); // 用户名
		sysUserPlus.setSource(authUser.getSource()); // 来源
		
		SysUser sysUser = new SysUser();
		
		sysUser.setUserName(sysUserPlus.getUserName());
		List<SysUser> sysUsers = userService.selectUserListNoDataScope(sysUser);
		if (sysUsers.size() > 1) {
//			throw new ServiceException("第三方登录异常，账号重叠");
			json.put("code", 500);
			json.put("msg", "登录失败，第三方登录异常，账号重叠！");
			return json;
		} else if (sysUsers.size() == 0) {
			sysUserPlus.setNickName(authUser.getNickname()); // 昵称
			sysUserPlus.setAvatar(authUser.getAvatar()); // 头像
			sysUserPlus.setEmail(authUser.getEmail()); // 邮箱
			sysUserPlus.setRemark(authUser.getRemark()); // 备注
			sysUserPlus.setSinaUuid(authUser.getUuid()); // uuid
			sysUserPlus.setBlog(authUser.getBlog()); // 地址
			sysUserPlus.setLocation(authUser.getLocation());
			sysUserPlus.setPassword(SecurityUtils.encryptPassword("zwgkqaz@2024"));
			com.alibaba.fastjson.JSONObject rawUserInfo = authUser.getRawUserInfo();
			sysUserPlus.setLargeAvatar(rawUserInfo.getString("avatar_large")); // 大头像
			sysUserPlus.setGender(authUser.getGender().getDesc()); // 性别
			// 关注数量
			sysUserPlus.setFriendsCount(rawUserInfo.getInteger("friends_count"));
			sysUserPlus.setFollowersCount(rawUserInfo.getInteger("followers_count")); // 粉丝数量
			// 插入时间
			sysUserPlus.setCreateTime(new Date());
			// 查询头像转成base64编码
//			String readImage = sinaApi.readImage(authUser.getAvatar());
//			sysUserPlus.setAvatarBase64(readImage);
			// 查询头像转成base64编码
			String readBigImage = sinaApi.readImage(sysUserPlus.getLargeAvatar());
			sysUserPlus.setBigAvatarBase64(readBigImage);
			// 保存新用户，相当于注册
			sysUserPlusService.save(sysUserPlus);
			// 查询用户信息
			sysUser = userService.selectUserById(sysUserPlus.getUserId());
			
			AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUserPlus.getUserName(), Constants.REGISTER,
					MessageUtils.message("user.register.success")));
			// 授权普通用户
			Long[] roleIds = { 2L };
			userService.insertUserAuth(sysUserPlus.getUserId(), roleIds);
		} else {
			sysUser = sysUsers.get(0);
			Long userId = sysUser.getUserId();
			sysUserPlus = sysUserPlusService.getById(userId);
			sysUserPlus.setRemark(authUser.getRemark()); // 备注
			sysUserPlus.setAvatar(authUser.getAvatar()); // 头像
			sysUserPlus.setSinaUuid(authUser.getUuid()); // uuid
			sysUserPlus.setBlog(authUser.getBlog()); // 地址
			sysUserPlus.setSource(authUser.getSource()); // 来源
			com.alibaba.fastjson.JSONObject rawUserInfo = authUser.getRawUserInfo();
			sysUserPlus.setLargeAvatar(rawUserInfo.getString("avatar_large")); // 大头像
			sysUserPlus.setGender(authUser.getGender().getDesc()); // 性别
			String password = sysUser.getPassword();
			if (StringUtils.isEmpty(password)) {
				sysUserPlus.setPassword(SecurityUtils.encryptPassword("zwgkqaz@2024"));
			}
			// 关注数量followers_count
			sysUserPlus.setFriendsCount(rawUserInfo.getInteger("friends_count"));
			sysUserPlus.setFollowersCount(rawUserInfo.getInteger("followers_count")); // 粉丝数量
			// 查询头像转成base64编码
//			String readImage = sinaApi.readImage(authUser.getAvatar());
//			sysUserPlus.setAvatarBase64(readImage);
			// 查询头像转成base64编码
			String readBigImage = sinaApi.readImage(sysUserPlus.getLargeAvatar());
			sysUserPlus.setBigAvatarBase64(readBigImage);
			
			sysUserPlus.setUpdateTime(new Date());
			
//			log.debug("readImage" + readImage);
			// 更新用户信息
			sysUserPlusService.updateById(sysUserPlus);
			
		}
		
		if (null != anId) {
			AnswerJson answerJson = answerJsonService.selectAnswerJsonByAnId(anId);
			answerJson.setUserId(sysUserPlus.getUserId().toString());
			int row = answerJsonService.updateAnswerJson(answerJson);
			if (row < 1) {
				log.info("更新用户userId：{}问卷结果失败");
			}
		}
		
		log.debug("sysUser:" + JSONObject.toJSONString(sysUser));
		AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.LOGIN_SUCCESS,
				MessageUtils.message("user.login.success")));
		
//		String username = sysUserPlus.getUserName();
//		String password = "zwgkqaz@2024";

//		// 用户验证
//		Authentication authentication = null;
//		try {
//			// 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
//			authentication = authenticationManager
//					.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//		} catch (Exception e) {
//			if (e instanceof BadCredentialsException) {
//				AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
//						MessageUtils.message("user.password.not.match")));
//				throw new UserPasswordNotMatchException();
//			} else {
//				AsyncManager.me()
//						.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
//				throw new ServiceException(e.getMessage());
//			}
//		}
//		
//		AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS,
//				MessageUtils.message("user.login.success")));
//		LoginUser loginUser = (LoginUser) authentication.getPrincipal();
//		recordLoginInfo(loginUser.getUserId());

		
		
		// 注册成功或者是已经存在的用户
		LoginUser loginUser = new LoginUser(sysUserPlus.getUserId(), sysUser.getDeptId(), sysUser,
				permissionService.getMenuPermission(sysUser));
		String token = tokenService.createToken(loginUser);
		// 存储到上下文
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        
		recordLoginInfo(loginUser.getUserId());

		json.put("code", 200);
		json.put("token", token);
		json.put("userId", sysUserPlus.getUserId());
		json.put("sinaToken", authUser.getToken().getAccessToken());
		// 缓存uuid对应的token, 防止重复提交错误
		redisCache.setCacheObject("weibo-user:" + uuid, json, 30, TimeUnit.MINUTES);
		return json;
	}










	public JSONObject wxLogin(String decryptResult, Long anId,HttpServletRequest request){
		JSONObject json=new JSONObject();


		//字符串转json
		JSONObject jsonObject = JSONObject.parseObject(decryptResult);
//        String unionid = jsonObject.getString("unionid");
		String openId = jsonObject.getString("openId");
		//获取nickName
		String nickName = jsonObject.getString("nickName");
		//获取头像
		String avatarUrl = jsonObject.getString("avatarUrl");

		JSONObject result = redisCache.getCacheObject("wx-user:" + openId);
		if (result != null) {
			String token = result.getString("token");
			Long userId = result.getLong("userId");
			SysUser sysUser = userService.selectUserById(userId);
			LoginUser loginUser = tokenService.getLoginUser(token);
			sysUser.getUserId();
			if (null == loginUser) {
				// 注册成功或者是已经存在的用户
				loginUser = new LoginUser(sysUser.getUserId(), sysUser.getDeptId(), sysUser,
						permissionService.getMenuPermission(sysUser));
				token = tokenService.createToken(loginUser);
				// 存储到上下文
				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
				authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);

				recordLoginInfo(loginUser.getUserId());
			} else {
				tokenService.verifyToken(loginUser);
			}
			if (null != anId) {
				AnswerJson answerJson = answerJsonService.selectAnswerJsonByAnId(anId);
				answerJson.setUserId(userId.toString());
				int row = answerJsonService.updateAnswerJson(answerJson);
				if (row < 1) {
					log.info("更新用户userId：{}问卷结果失败", userId);
				}
			}
			return result;
		}


		//还可以获取其他信息
		//根据openid判断数据库中是否有该用户
		//根据openid查询用户信息
//		SysUserPlus wxUser = sysUserPlusService.selectWxUserByOpenId(openId);

		SysUserPlus sysUserPlus=new SysUserPlus();
		sysUserPlus=sysUserPlusService.selectWxUserByOpenId(openId);

		SysUser sysUser = new SysUser();
		sysUser.setUserName(sysUserPlus.getUserName());
		List<SysUser> sysUsers = userService.selectUserListNoDataScope(sysUser);
		if (sysUsers.size() > 1) {
//			throw new ServiceException("第三方登录异常，账号重叠");
			json.put("code", 500);
			json.put("msg", "登录失败，第三方登录异常，账号重叠！");
			return json;
		} else if (sysUsers.size() == 0) {
			sysUserPlus.setUserName(nickName);
			sysUserPlus.setNickName(jsonObject.getString("nickName")); // 昵称
			sysUserPlus.setAvatar(jsonObject.getString("avatarUrl")); // 头像
//			sysUserPlus.setEmail(authUser.getEmail()); // 邮箱
//			sysUserPlus.setRemark(authUser.getRemark()); // 备注
//			sysUserPlus.setSinaUuid(authUser.getUuid()); // uuid
//			sysUserPlus.setBlog(authUser.getBlog()); // 地址
//			sysUserPlus.setLocation(authUser.getLocation());
			sysUserPlus.setPassword(SecurityUtils.encryptPassword("zwgkqaz@2024"));
//			com.alibaba.fastjson.JSONObject rawUserInfo = authUser.getRawUserInfo();
//			sysUserPlus.setLargeAvatar(rawUserInfo.getString("avatar_large")); // 大头像
//			sysUserPlus.setGender(authUser.getGender().getDesc()); // 性别
//			// 关注数量
//			sysUserPlus.setFriendsCount(rawUserInfo.getInteger("friends_count"));
//			sysUserPlus.setFollowersCount(rawUserInfo.getInteger("followers_count")); // 粉丝数量
			// 插入时间
			sysUserPlus.setCreateTime(new Date());
			// 查询头像转成base64编码
//			String readImage = sinaApi.readImage(authUser.getAvatar());
//			sysUserPlus.setAvatarBase64(readImage);
			// 查询头像转成base64编码
//			String readBigImage = sinaApi.readImage(sysUserPlus.getLargeAvatar());
//			sysUserPlus.setBigAvatarBase64(readBigImage);
			// 保存新用户，相当于注册
			sysUserPlusService.save(sysUserPlus);
			// 查询用户信息
			sysUser = userService.selectUserById(sysUserPlus.getUserId());

			AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUserPlus.getUserName(), Constants.REGISTER,
					MessageUtils.message("user.register.success")));
			// 授权普通用户
			Long[] roleIds = { 2L };
			userService.insertUserAuth(sysUserPlus.getUserId(), roleIds);
		} else {
			sysUser = sysUsers.get(0);
			Long userId = sysUser.getUserId();
			sysUserPlus = sysUserPlusService.getById(userId);
//			sysUserPlus.setRemark(authUser.getRemark()); // 备注
			sysUserPlus.setAvatar(avatarUrl); // 头像
			sysUserPlus.setNickName(nickName); // 昵称
//			sysUserPlus.setSinaUuid(authUser.getUuid()); // uuid
//			sysUserPlus.setBlog(authUser.getBlog()); // 地址
//			sysUserPlus.setSource(authUser.getSource()); // 来源
//			com.alibaba.fastjson.JSONObject rawUserInfo = authUser.getRawUserInfo();
//			sysUserPlus.setLargeAvatar(rawUserInfo.getString("avatar_large")); // 大头像
//			sysUserPlus.setGender(authUser.getGender().getDesc()); // 性别
			String password = sysUser.getPassword();
			if (StringUtils.isEmpty(password)) {
				sysUserPlus.setPassword(SecurityUtils.encryptPassword("zwgkqaz@2024"));
			}
			// 关注数量followers_count
//			sysUserPlus.setFriendsCount(rawUserInfo.getInteger("friends_count"));
//			sysUserPlus.setFollowersCount(rawUserInfo.getInteger("followers_count")); // 粉丝数量
			// 查询头像转成base64编码
//			String readImage = sinaApi.readImage(authUser.getAvatar());
//			sysUserPlus.setAvatarBase64(readImage);
			// 查询头像转成base64编码
//			String readBigImage = sinaApi.readImage(sysUserPlus.getLargeAvatar());
//			sysUserPlus.setBigAvatarBase64(readBigImage);

			sysUserPlus.setUpdateTime(new Date());

//			log.debug("readImage" + readImage);
			// 更新用户信息
			sysUserPlusService.updateById(sysUserPlus);

		}

		log.debug("sysUser:" + JSONObject.toJSONString(sysUser));
		AsyncManager.me().execute(AsyncFactory.recordLogininfor(sysUser.getUserName(), Constants.LOGIN_SUCCESS,
				MessageUtils.message("user.login.success")));


		LoginUser loginUser = new LoginUser(sysUserPlus.getUserId(), sysUser.getDeptId(), sysUser,
				permissionService.getMenuPermission(sysUser));
		String token = tokenService.createToken(loginUser);
		// 存储到上下文
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		recordLoginInfo(loginUser.getUserId());

		json.put("code", 200);
		json.put("token", token);
		json.put("userId", sysUserPlus.getUserId());
		json.put("openId", openId);
		// 缓存uuid对应的token, 防止重复提交错误
		redisCache.setCacheObject("wx-user:" + openId, json, 30, TimeUnit.MINUTES);
		return json;


	}


	/**
	 * 微信解密工具
	 */
	public static String decryptS5(String sSrc, String encodingFormat, String sKey, String ivParameter) {
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] raw = decoder.decodeBuffer(sKey);
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			IvParameterSpec iv = new IvParameterSpec(decoder.decodeBuffer(ivParameter));
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] myendicod = decoder.decodeBuffer(sSrc);
			byte[] original = cipher.doFinal(myendicod);
			return new String(original, encodingFormat);
		} catch (Exception ex) {
			return null;
		}
	}

}
