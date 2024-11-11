package com.shure.surdes.survey.remote;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.shure.surdes.survey.vo.AiTestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;

/**
 * 爬虫api
 *
 * @author color
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
     *
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
     *
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

    //uid 微博账号id 必须传
    //start_time:起始时间
    //end_time:终止时间
    //update:是否强制重爬
    //num:爬取贴文数里
    public JSONObject getMbtiByWb(String wbUid, String user, String token, String startTime, String endTime,
                                  String isUpdate, Integer num) {
        if (num == null || num <= 0) {
            num = 50;
        }


        JSONObject json = new JSONObject();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", wbUid);
        if(StringUtils.isNotEmpty(startTime)) {
            map.put("start_time", startTime);
            map.put("end_time", endTime);
        }else {
            Date endDate = DateUtil.beginOfDay(new Date());
            Date startDate = DateUtil.offsetMonth(endDate, -3);
            map.put("start_time",DateUtil.formatDateTime(startDate));
            map.put("end_time",DateUtil.formatDateTime(endDate));
        }
        map.put("num", num);
//		map.put("user", user);
//		map.put("user_token", token)
        if (StringUtils.isNotEmpty(isUpdate)) {
            map.put("update", isUpdate);
        }else {
            map.put("update","false");
        }
        log.info("ai测试参数：{}", map);
        String url = modelUrl + "mbti";
//        String url = /*modelUrl +*/ "http://192.168.101.23:1230/mbti";
        HttpEntity<HashMap<String, Object>> entity = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, JSONObject.class);
            json = exchange.getBody();
            log.info("wbuid:{},ai测试结果:{}", wbUid, json.toString());
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

    public void buildStartAndEndTime(AiTestVo vo) {
        String startTime = vo.getStartTime();
        String endTime = vo.getEndTime();
        String format = "yyyy-MM-dd HH:mm:ss";
        Date startDate;
        Date endDate;
        //时间默认，未设值默认三个月
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(startTime)) {
            startDate = DateUtil.parse(startTime, format);
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(endTime)) {
                endDate = new Date();
                if (DateUtil.between(startDate, endDate, DateUnit.DAY) > 3) {
                    endDate = DateUtil.offsetMonth(startDate, 3);
                }
            } else {
                endDate = DateUtil.parse(endTime, format);
                if (DateUtil.compare(startDate, endDate) > 0) {
                    Date temp = startDate;
                    startDate = endDate;
                    endDate = temp;
                }
            }
        } else {
            if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isEmpty(endTime)) {
                endDate = DateUtil.beginOfDay(new Date());
                startDate = DateUtil.offsetMonth(endDate, -3);
            } else {
                endDate = DateUtil.parse(endTime, format);
                startDate = DateUtil.offsetMonth(endDate, -3);
            }
        }
        startTime = DateUtil.formatDateTime(startDate);
        endTime = DateUtil.formatDateTime(endDate);
        vo.setEndTime(endTime);
        vo.setStartTime(startTime);
    }


}
