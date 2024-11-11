package com.shure.surdes.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shure.surdes.common.annotation.DataSource;
import com.shure.surdes.common.enums.DataSourceType;
import com.shure.surdes.survey.domain.JeepayOrder;
import com.shure.surdes.survey.domain.StarAnswer;
import com.shure.surdes.survey.mapper.JeepayOrderMapper;
import com.shure.surdes.survey.mapper.StarAnswerMapper;
import com.shure.surdes.survey.service.IJeepayOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//@DS("slave")
@Transactional(propagation = Propagation.REQUIRES_NEW) // 开启新事务
@DataSource(value = DataSourceType.SLAVE) // 切换的数据源
public class JeepayOrderServiceImpl extends ServiceImpl<JeepayOrderMapper, JeepayOrder> implements IJeepayOrderService {

//    @Autowired
//    private JeepayOrderMapper jeepayOrderMapper;

    @Override
    public JeepayOrder selectByMchOrderNo(String mchOrderNo) {
        if(StringUtils.isEmpty(mchOrderNo)){
            return null;
        }
        return baseMapper.selectByMchOrderNo(mchOrderNo);
    }

    @Override
    public List<JeepayOrder> selectByIdList(List<String> idList) {
        return listByIds(idList);
    }


}
