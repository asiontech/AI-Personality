package com.shure.surdes.survey.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "tb_mbti_match")
public class MbtiMatch {

    private String userMbti;

    private String starMbti;

    private String matchLevel;

}
