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
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

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
	public JSONObject getMbti(String uuid, String user, String token, String startTime, 
			String endTime, String isupdate) {
		JSONObject json = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HashMap<String, Object> map = new HashMap<>();
		map.put("uid", uuid);
		map.put("user", user);
		map.put("user_token", token);
		if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
			startTime = startTime + " 00:00:00";
			endTime = endTime + " 23:59:59";
			map.put("start_time", startTime);
			map.put("end_time", endTime);
		}
		if (StringUtils.isNotEmpty(isupdate)) {
			map.put("update", isupdate);
		}
		log.info("ai测试参数：{}", map);
		String url = modelUrl + "mbti";
		HttpEntity<HashMap<String, Object>> entity = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, JSONObject.class);
			json = exchange.getBody();
			log.info("uuid:{},ai测试结果:{}", uuid, json.toString());
//			Integer code = json.getInteger("status");
//			if (200 == code) {
//				json = json.getJSONObject("data");
//				return json;
//			}
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("接口调用失败！");
		}
		return null;
	}
	
	/**
	 * 查询词云数据
	 * @param uuid
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public JSONObject getKeyCloud(String uuid, String startTime, String endTime) {
		JSONObject json = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HashMap<String, Object> map = new HashMap<>();
		map.put("uid", uuid);
		startTime = startTime + " 00:00:00";
		endTime = endTime + " 23:59:59";
		map.put("start_time", startTime);
		map.put("end_time", endTime);
		String url = modelUrl + "wordcloud";
		HttpEntity<HashMap<String, Object>> entity = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, JSONObject.class);
			json = exchange.getBody();
			log.info("uid:{}词云数据：{}", uuid, json.toString());
			return json;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("接口调用失败！");
		}
		return null;
		
	}
}
