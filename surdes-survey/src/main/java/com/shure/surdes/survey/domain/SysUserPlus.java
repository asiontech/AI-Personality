package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户表
 * @author color
 *
 */
@Data
@TableName(value = "sys_user")
public class SysUserPlus {

    /** 用户ID */
	@TableId
    private Long userId;

    /** 用户账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;
    
    private String email;

    /** 用户性别 */
    private String sex;

    /** 用户头像 */
    private String avatar;
    
    private String remark;
    
    /** 新浪uuid */
    private String sinaUuid;
    
    /** 来源 */
    private String source;
    
    /** 博客地址 */
    private String blog;
    
    /** 位置 */
    private String location;
    
    /** 大头像 */
    private String largeAvatar;
    
    /** 性别 */
    private String gender;
   
    
}
