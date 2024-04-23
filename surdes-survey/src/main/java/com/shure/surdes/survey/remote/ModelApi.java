package com.shure.surdes.survey.remote;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * 爬虫api
 * @author color
 *
 */
@Slf4j
@Component
public class ModelApi {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${model.url}")
	private String modelUrl;

	/**
	 * 调用分析服务接口，获取AI测试mbti结果
	 * @param uuid
	 * @param user
	 * @param token
	 * @return
	 */
	public JSONObject getMbti(String uuid, String user, String token) {
		JSONObject json = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HashMap<String, Object> map = new HashMap<>();
		map.put("uid", uuid);
		map.put("user", user);
		map.put("user_token", token);
		HttpEntity<HashMap<String, Object>> entity = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<JSONObject> exchange = restTemplate.exchange(modelUrl, HttpMethod.POST, entity, JSONObject.class);
			json = exchange.getBody();
			log.info(json.toString());
			Integer code = json.getInteger("status");
			if (200 == code) {
				json = json.getJSONObject("data");
				return json;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("接口调用失败！");
		}
		return null;
	}
}
