package com.shure.surdes.survey.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.survey.domain.UserSurveyResult;
import com.shure.surdes.survey.mapper.UserSurveyResultMapper;
import com.shure.surdes.survey.service.IUserSurveyResultService;

/**
 * 用户测评结果业务层实现类
 * @author color
 *
 */
@Service
public class UserSurveyResultServiceImpl extends ServiceImpl<UserSurveyResultMapper, UserSurveyResult>
		implements IUserSurveyResultService {

}
