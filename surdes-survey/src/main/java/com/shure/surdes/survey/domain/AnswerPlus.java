package com.shure.surdes.survey.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shure.surdes.common.annotation.Excel;

import lombok.Data;

/**
 * 问卷答案结果对象 tb_answer
 *
 * @author Shure
 * @date 2021-10-18
 */
@TableName(value = "tb_answer")
@Data
public class AnswerPlus {

    /**
     * 答案主键
     */
	@TableId
    private Long answerId;

    /**
     * 问卷主键
     */
    @Excel(name = "问卷主键")
    private Long surveyId;

    /**
     * 问题主键
     */
    @Excel(name = "问题主键")
    private Long questionId;

    /**
     * 选项编码
     */
    @Excel(name = "选项编码")
    private String optionCode;
    
    @Excel(name = "选项对应的性格类型")
    private String optionCharacterType;

    /**
     * 答案结果
     */
    @Excel(name = "答案结果")
    private String answerValue;

    /**
     * 扩展填空值
     */
    @Excel(name = "扩展填空值")
    private String extendValue;

    /**
     * 答题人唯一标识
     */
    @Excel(name = "答题人唯一标识")
    private String userId;

    /**
     * 答题人姓名
     */
    @Excel(name = "答题人姓名")
    private String userName;

    /**
     * 账套
     */
    @Excel(name = "账套")
    private String bookCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
