package com.shure.surdes.survey.vo;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

/**
 * 返回用户信息封装对象
 * @author color
 *
 */
@Data
public class SysUserPlusVo {

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
    
    /** 关注数量 */
    private Integer friendsCount;
    
    /** 粉丝数量 */
    private Integer followersCount;
}
