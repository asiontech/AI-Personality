package com.shure.surdes.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shure.surdes.survey.domain.JeepayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JeepayOrderMapper extends BaseMapper<JeepayOrder> {

    JeepayOrder selectByMchOrderNo(String mchOrderNo);

}
