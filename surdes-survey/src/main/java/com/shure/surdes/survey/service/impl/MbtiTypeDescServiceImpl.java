package com.shure.surdes.survey.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.survey.domain.MbtiTypeDesc;
import com.shure.surdes.survey.mapper.MbtiTypeDescMapper;
import com.shure.surdes.survey.service.IMbtiTypeDescService;

/**
 * mbti 性格解析 业务层实现类
 * @author color
 *
 */
@Service
public class MbtiTypeDescServiceImpl extends ServiceImpl<MbtiTypeDescMapper, MbtiTypeDesc> implements IMbtiTypeDescService {

}
