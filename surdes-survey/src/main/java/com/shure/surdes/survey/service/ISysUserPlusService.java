package com.shure.surdes.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shure.surdes.survey.domain.SysUserPlus;

/**
 * 第三方登录账号业务层
 * @author color
 *
 */
public interface ISysUserPlusService extends IService<SysUserPlus> {

    SysUserPlus selectWxUserByOpenId(String openId);

}
