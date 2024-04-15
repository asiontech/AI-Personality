package com.shure.surdes.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * mybatis-plus配置
 * @author color
 * @date 2023-12-18 02:42:33
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
@MapperScan("com.shure.**.mapper")
public class MybatisPlusConfig {
    /**
     * 分页插件
     *
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor intercepter = new MybatisPlusInterceptor();
        // 分页配置
        intercepter.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁
        intercepter.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return intercepter;
    }

}