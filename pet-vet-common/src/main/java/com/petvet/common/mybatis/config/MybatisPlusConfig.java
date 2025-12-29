package com.petvet.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.petvet.common.mybatis.id.SnowflakeIdGenerator;
import com.petvet.common.mybatis.interceptor.IsVoidQueryInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * 
 * 配置全局的 MyBatis Plus 拦截器，包括：
 * - 分页插件
 * - 乐观锁插件
 * - is_void 查询拦截器（自动添加逻辑删除条件）
 * - 雪花算法ID生成器（保证ID严格递增）
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 配置 MyBatis Plus 拦截器
     * 
     * 拦截器执行顺序（从外到内）：
     * 1. PaginationInnerInterceptor - 分页插件
     * 2. OptimisticLockerInnerInterceptor - 乐观锁插件
     * 
     * 注意：IsVoidQueryInterceptor 使用标准的 MyBatis Interceptor 接口，
     * 会通过 MyBatis 的插件机制自动注册
     * 
     * @return MybatisPlusInterceptor
     * @author daidasheng
     * @date 2024-12-27
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页分页条数限制，默认无限制
        paginationInterceptor.setMaxLimit(1000L);
        // 当超过最大页数时，是否继续查询第一页，默认 false
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 2. 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 注册 is_void 查询拦截器
     * 
     * 使用标准的 MyBatis Interceptor 接口，自动拦截所有 SQL 执行
     * 
     * @return IsVoidQueryInterceptor
     * @author daidasheng
     * @date 2024-12-27
     */
    @Bean
    public Interceptor isVoidQueryInterceptor() {
        return new IsVoidQueryInterceptor();
    }
    
    /**
     * 注册雪花算法ID生成器
     * 
     * 保证生成的ID严格递增，同时具备分布式唯一性
     * 
     * @return IdentifierGenerator
     * @author daidasheng
     * @date 2024-12-27
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new SnowflakeIdGenerator();
    }
}

