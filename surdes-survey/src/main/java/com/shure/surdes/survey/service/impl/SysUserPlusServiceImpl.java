package com.shure.surdes.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.survey.domain.SysUserPlus;
import com.shure.surdes.survey.mapper.SysUserPlusMapper;
import com.shure.surdes.survey.service.ISysUserPlusService;

/**
 * 第三方登录账号业务层实现类
 * @author color
 *
 */
@Service
public class SysUserPlusServiceImpl extends ServiceImpl<SysUserPlusMapper, SysUserPlus> 
	implements ISysUserPlusService {

	@Override
	public SysUserPlus selectWxUserByOpenId(String openId) {
		if(openId==null){
			return null;
		}
		LambdaQueryWrapper<SysUserPlus> wrapper=new LambdaQueryWrapper<>();
		wrapper.eq(SysUserPlus::getOpenId,openId);
		return baseMapper.selectOne(wrapper);
	}
}
