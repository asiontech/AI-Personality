package com.shure.surdes.survey.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shure.surdes.survey.domain.StarAnswer;

import java.util.List;
import java.util.Map;

/**
 * 明星业务
 *
 * @author lixiyang
 * @date 2024-10-30
 */
public interface IStarAnswerService extends IService<StarAnswer> {

    /**
     * 分页
     *
     * @param pageNum  页码
     * @param pageSize 页容量
     *
     */
    JSONObject queryStarAnswerByPage(Integer pageNum, Integer pageSize);

    List<JSONObject> selectAllTree();
}

