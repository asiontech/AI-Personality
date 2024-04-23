package com.shure.surdes.survey.remote;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 新浪api调用
 * 
 * @author color
 *
 */
@Slf4j
@Component
public class SinaApi {

	@Autowired
	RestTemplate restTemplate;

	/**
	 * 获取头像
	 * @param url
	 * @return
	 */
	public String readImage(String url) {
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.IMAGE_JPEG);
		try {
			ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
			// 获取entity中的数据
			byte[] body = responseEntity.getBody();
			// 创建输出流 输出到本地
			String base64Image = Base64.getEncoder().encodeToString(body);
			return base64Image;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("接口调用失败！");
		}

		return null;
	}

}
