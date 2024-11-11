package com.shure.surdes.survey.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.common.constant.Constants;
import com.shure.surdes.survey.domain.StarAnswer;
import com.shure.surdes.survey.mapper.StarAnswerMapper;
import com.shure.surdes.survey.service.IStarAnswerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service("starAnswerService")
public class StarAnswerServiceImpl extends ServiceImpl<StarAnswerMapper, StarAnswer> implements IStarAnswerService {

    @Value(value = "${star.imgUrl}")
    private String startImgUrl;

    @Override
    public JSONObject queryStarAnswerByPage(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<StarAnswer> wrapper = new LambdaQueryWrapper<>();
        IPage<StarAnswer> pageObj = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<StarAnswer> list=pageObj.getRecords();
        for (StarAnswer starAnswer:list){
            starAnswer.setStarImgUrl(Constants.Star_Img_PREFIX+"/"+starAnswer.getStarImg());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", pageObj.getTotal());
        jsonObject.put("list", list);
        return jsonObject;
    }

    @Override
    public List<JSONObject> selectAllTree() {
        List<JSONObject> jsonObjects = new ArrayList<>();
        List<StarAnswer> starAnswers = baseMapper.selectList(null);
        for (StarAnswer starAnswer : starAnswers) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("img", Constants.Star_Img_PREFIX+"/"+starAnswer.getStarImg());
            jsonObject.put("uid", starAnswer.getStarUid());
            jsonObject.put("name", starAnswer.getStarName());
            jsonObjects.add(jsonObject);
        }
        return jsonObjects;
    }
}