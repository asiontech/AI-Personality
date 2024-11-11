package com.shure.surdes.survey.activemq;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shure.surdes.common.core.redis.RedisCache;
import com.shure.surdes.common.utils.DateUtils;
import com.shure.surdes.common.utils.StringUtils;
import com.shure.surdes.survey.constant.SurveyType;
import com.shure.surdes.survey.domain.AnswerJson;
import com.shure.surdes.survey.domain.StarAnswer;
import com.shure.surdes.survey.mapper.AnswerJsonMapper;
import com.shure.surdes.survey.remote.ModelApi;
import com.shure.surdes.survey.service.IStarAnswerService;
import com.shure.surdes.survey.vo.AiTestVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TopicListner {

    @Autowired
    AnswerJsonMapper answerJsonMapper;

    @Autowired
    ModelApi modelApi;

    @Autowired
    RedisCache redisCache;

    @Autowired
    private IStarAnswerService starAnswerService;

    // 消息队列消费者监听
    // 添加用户逻辑
    @JmsListener(destination = QueueDictionary.AI_TEST)
    public void aiTest(String message) throws InterruptedException {
        // 处理接收到的消息逻辑
        log.debug("获取队列原始消息: " + message);
        AiTestVo vo = JSON.parseObject(message, AiTestVo.class);
        String uid = vo.getUid();
        Long surveyId = vo.getSurveyId();
        String startTime = vo.getStartTime();
        String endTime = vo.getEndTime();
        String aid = vo.getAid();
        Integer retest = vo.getRetest(); // 强制更新字段
        String update = null;
        if (null != retest && 1 == retest) {
            update = "true";
        }
        String surveyType = SurveyType.MBTI_AI_SURVEY_TEST;
        boolean timeFlag = false;
        if (1001L==surveyId) {
            surveyType = SurveyType.MBTI_AI_TIME_SURVEY_TEST;
            Date start = DateUtil.parse(startTime);
            Date end = DateUtil.parse(endTime);

            startTime=DateUtil.formatDateTime(start);
            endTime=DateUtil.formatDateTime(end);

            timeFlag = true;
        }
        if("pk_user".equals(vo.getType())||"pk_star".equals(vo.getType())){
            surveyType=SurveyType.MBTI_AI_PK;
        }
        // 业务处理
        // 调用接口得出mbti
        JSONObject json = modelApi.getMbtiByWb(vo.getUid(), vo.getUser(), vo.getUserToken(),
                startTime, endTime, update, vo.getNum());
        log.debug("用户id:{}ai测试结束，返回结果:{}", uid, json);
        JSONObject redisJson = new JSONObject();
        if (null != json) {
            Integer code = json.getInteger("status");
            if (null != code && 201 == code) {
                redisJson.fluentPut("code", 500);
                redisJson.fluentPut("msg", "系统繁忙，请稍后重试！");
                redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
                log.debug("AI测试队列缓存消息：{}:{}", uid, redisJson.toString());
                return;
            }
            String data = json.getString("data");
            if (StringUtils.isNotEmpty(data) && "updating".equals(data)) { // 从数据库查询上一份记录
                redisJson.fluentPut("code", 201);
                redisJson.fluentPut("msg", "系统正在生成AI测试结果，请稍等！");
                redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
                log.debug("AI测试队列缓存消息：{}:{}", uid, redisJson.toString());
                return;
            }
            if (null != code && 200 == code) {
                JSONObject jsonObject = json.getJSONObject("data");
                String mbti = jsonObject.getString("type");
                if (StringUtils.isEmpty(mbti)) {
                    redisJson.fluentPut("code", 500);
                    redisJson.fluentPut("msg", "没有帖文数据，不能得出AI测试结果！");
                    redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
                    log.debug("AI测试队列缓存消息：{}:{}", uid, redisJson.toString());
                    return;
                }
                JSONObject score = jsonObject.getJSONObject("dim_score");
                // 计算得分
                AnswerJson answer = new AnswerJson();
                answer.setSurveyId(surveyId); // AI测试id固定1000
                if (!"pk_star".equals(vo.getType())) {
                    answer.setUserId(vo.getUserId().toString());
                    answer.setWbUid(uid);
                } else {
                    answer.setWbUid(uid);
                    // TODO: 2024/10/23
                }
                answer.setCreateTime(new Date());
                answer.setAnswerResult(mbti);
                answer.setAnswerResultOrigin(score.toJSONString());
                answer.setSurveyType(surveyType);
                if (timeFlag) { // ai时间段测试
                    // 查询词云数据
                    JSONObject cloudJson = modelApi.getKeyCloud(vo.getUid(), startTime, endTime);
                    log.info("词云数据：{}", cloudJson);
                    if (null != cloudJson) {
                        Integer status = cloudJson.getInteger("status");
                        if (null != status && 200 == status) {
                            JSONObject keyCloud = cloudJson.getJSONObject("data");
                            answer.setKeyCloud(keyCloud.toJSONString());
                        }
//                        answer.setStartTime(DateUtil.parse(startTime));
//                        answer.setEndTime(DateUtil.parse(endTime));
                        try {
                            answer.setStartTime(DateUtil.parse(startTime, "yyyy-MM-dd"));
                            answer.setEndTime( DateUtil.parse(endTime, "yyyy-MM-dd"));
                            log.debug("时间 start:{} end:{}",startTime,endTime);
                        } catch (Exception e) {
                            log.error("日期转换失败，格式不符！startTime:{},endTime:{}", startTime, endTime);
                        }
                    }
                }
                
                answerJsonMapper.insertAnswerJson(answer);
                if (vo.getUserId() == null) {
                    LambdaQueryWrapper<StarAnswer> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(StarAnswer::getStarUid, vo.getUid());
                    StarAnswer starAnswer = StarAnswer.builder()
                            .starAnswerId(answer.getAnId())
                            .starMbti(answer.getAnswerResult())
                            .updateTime(answer.getUpdateTime())
                            .updateBy("Automatic Updates")
//                            .starName()
                            .build();
                    starAnswerService.update(starAnswer, wrapper);
                }
                redisJson.fluentPut("code", 200);
                redisJson.fluentPut("data", answer.getAnId());
                redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
                log.debug("AI测试队列缓存消息：{}:{}", uid, redisJson.toString());
            }
        } else {
            redisJson.fluentPut("code", 500);
            redisJson.fluentPut("msg", "系统繁忙，请稍后重试！");
            redisCache.setCacheObject(aid, redisJson, 30, TimeUnit.MINUTES);
            log.debug("AI测试队列缓存消息：{}:{}", uid, redisJson.toString());
        }
    }


}
