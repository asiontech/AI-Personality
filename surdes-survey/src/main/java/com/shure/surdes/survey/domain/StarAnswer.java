package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.Date;

/**
 * 明星
 *
 * @author lixiyang
 * @date 2024-10-30
 */
@Data
@TableName("tb_star_answer")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StarAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long starId;
    /**
     * 明星最新mbti
     */
    private String starMbti;
    /**
     * 明星最新测验结果id
     */
    private Long starAnswerId;
    /**
     * 明星头像
     */
    private String starImg;
    /**
     * 明星微博id
     */
    private String starUid;
    /**
     * 明星姓名
     */
    private String starName;
    /**
     * 明星性别
     */
    private Integer starSex;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String updateBy;

    @TableField(exist = false)
    private String starImgUrl;

}
