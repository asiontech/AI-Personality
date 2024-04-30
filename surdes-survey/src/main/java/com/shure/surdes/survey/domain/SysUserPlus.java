package com.shure.surdes.survey.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    
    private String password;
    
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
   
    /** 头像base64编码 */
    private String avatarBase64;
    
    @TableField("big_avatar_base64")
    private String bigAvatarBase64;
    
    /** 关注数量 */
    private Integer friendsCount;
    
    /** 粉丝数量 */
    private Integer followersCount;
    
    @TableField(exist = false)
    private String mbti;
    
    /** 插入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
}
