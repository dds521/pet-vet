package com.petvet.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.petvet.common.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 * 
 * 自动填充 BaseEntity 中的公共字段：
 * - createTime: 创建时间（插入时）
 * - updateTime: 更新时间（插入和更新时）
 * - createBy: 创建人（插入时）
 * - updateBy: 更新人（插入和更新时）
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
public class DefaultMetaObjectHandler implements MetaObjectHandler {
    
    /**
     * 创建时间字段名
     */
    private static final String CREATE_TIME_FIELD = "createTime";
    
    /**
     * 更新时间字段名
     */
    private static final String UPDATE_TIME_FIELD = "updateTime";
    
    /**
     * 创建人字段名
     */
    private static final String CREATE_BY_FIELD = "createBy";
    
    /**
     * 更新人字段名
     */
    private static final String UPDATE_BY_FIELD = "updateBy";
    
    /**
     * 插入时自动填充
     * 
     * 填充字段：createTime, updateTime, createBy, updateBy
     * 
     * @param metaObject 元对象
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("执行插入自动填充");
        
        // 填充创建时间
        this.strictInsertFill(metaObject, CREATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新时间
        this.strictInsertFill(metaObject, UPDATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        
        // 填充创建人
        String currentUserId = UserContextUtil.getCurrentUserId();
        this.strictInsertFill(metaObject, CREATE_BY_FIELD, String.class, currentUserId);
        
        // 填充更新人
        this.strictInsertFill(metaObject, UPDATE_BY_FIELD, String.class, currentUserId);
    }
    
    /**
     * 更新时自动填充
     * 
     * 填充字段：updateTime, updateBy
     * 
     * @param metaObject 元对象
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("执行更新自动填充");
        
        // 填充更新时间
        this.strictUpdateFill(metaObject, UPDATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新人
        String currentUserId = UserContextUtil.getCurrentUserId();
        this.strictUpdateFill(metaObject, UPDATE_BY_FIELD, String.class, currentUserId);
    }
}

