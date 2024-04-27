package com.shure.surdes.survey.activemq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActiveMQUtils {
	
    @Autowired
    private JmsTemplate jmsTemplate;

    // 发送消息到队列
    public void sendMessageToQueue(String queueName, Object obj) {
    	log.debug("发送消息:{},到队列:{}", obj.toString(), queueName);
        String jsonStr= JSON.toJSONString(obj);
        jmsTemplate.convertAndSend(queueName, jsonStr);
    }
}


