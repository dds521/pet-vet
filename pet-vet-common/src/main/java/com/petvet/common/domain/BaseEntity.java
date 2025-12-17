package com.petvet.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 
 * 所有实体对象的基础类，包含公共字段：
 * - create_time: 创建时间
 * - update_time: 更新时间
 * - create_by: 创建人
 * - update_by: 更新人
 * - is_void: 逻辑删除标识（0-未删除，1-已删除）
 * - version: 版本号（用于乐观锁）
 * 
 * @author daidasheng
 * @date 2024-12-19
 */
@Data
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建时间
     * 自动填充：插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     * 自动填充：插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     * 自动填充：插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 更新人
     * 自动填充：插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * 逻辑删除标识
     * 0-未删除，1-已删除
     * 使用 MyBatis Plus 的逻辑删除功能
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isVoid;
    
    /**
     * 版本号
     * 用于乐观锁控制，防止并发更新冲突
     */
    @Version
    private Integer version;
}
