package com.shure.surdes.survey.vo;

import lombok.Data;

/**
 * 封装答案
 * @author color
 *
 */
@Data
public class AnswerPlusVo {

    private Long questionId;
    
    private String optionCode;
    
    private Integer num;
}
