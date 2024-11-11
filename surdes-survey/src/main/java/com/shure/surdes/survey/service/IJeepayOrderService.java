package com.shure.surdes.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shure.surdes.survey.domain.JeepayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IJeepayOrderService extends IService<JeepayOrder>{
    JeepayOrder selectByMchOrderNo(@Param("mchOrderNo") String mchOrderNo);

    List<JeepayOrder> selectByIdList(List<String> idList);
}
