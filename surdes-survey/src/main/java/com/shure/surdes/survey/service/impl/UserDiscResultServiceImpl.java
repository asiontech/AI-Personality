package com.shure.surdes.survey.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.survey.domain.UserDiscResult;
import com.shure.surdes.survey.mapper.UserDiscResultMapper;
import com.shure.surdes.survey.service.IUserDiscResultService;

@Service
public class UserDiscResultServiceImpl extends ServiceImpl<UserDiscResultMapper, UserDiscResult> 
	implements IUserDiscResultService {

}
