package com.shure.surdes.survey.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.survey.domain.DiscTypeDesc;
import com.shure.surdes.survey.mapper.DiscTypeDescMapper;
import com.shure.surdes.survey.service.IDiscTypeDescService;

/**
 * disc类型解析业务层实现类
 * @author color
 *
 */
@Service
public class DiscTypeDescServiceImpl extends ServiceImpl<DiscTypeDescMapper, DiscTypeDesc> 
	implements IDiscTypeDescService {

}
