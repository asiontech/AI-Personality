package com.shure.surdes.framework.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 
 * @author yangqi
 * @date 2019年11月19日上午11:42:00
 */
@Configuration
public class RestTemplateConfig {

	@Value("${remote.maxTotalConnect:0}")
	private int maxTotalConnect; // 连接池的最大连接数默认为0
	@Value("${remote.maxConnectPerRoute:200}")
	private int maxConnectPerRoute; // 单个主机的最大连接数
	@Value("${remote.connectTimeout:10000}")
	private int connectTimeout; // 连接超时默认2s
	@Value("${remote.readTimeout:30000}")
	private int readTimeout; // 读取超时默认30s

	// 创建HTTP客户端工厂
	@Bean
	public ClientHttpRequestFactory createFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//		 HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(connectTimeout); //10s
		factory.setReadTimeout(readTimeout); //30s
//		factory.setConnectionRequestTimeout(3000);
		return factory;
	}

	@Bean
	public RestTemplate getRestTemplate() {
	    RestTemplate restTemplate = new RestTemplate(createFactory());
	    List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();

	    //重新设置StringHttpMessageConverter字符集为UTF-8，解决中文乱码问题
	    for (int i = 0; i < converterList.size(); i++) {
	    	HttpMessageConverter<?> item = converterList.get(i);
	    	if (item.getClass().equals(StringHttpMessageConverter.class)) {
	    		converterList.set(i, new StringHttpMessageConverter(StandardCharsets.UTF_8));
	        }
	    }
	    //加入FastJson转换器
//	    converterList.add(new FastJsonHttpMessageConverter4());
	    return restTemplate;
	  }

	@Bean
	public AsyncRestTemplate asyncRestTemplate() {
		return new AsyncRestTemplate();
	}
}
