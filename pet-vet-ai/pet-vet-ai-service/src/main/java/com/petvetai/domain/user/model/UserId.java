package com.petvetai.domain.user.model;

import java.util.Objects;

/**
 * 用户ID值对象
 * 
 * 值对象，不可变，通过值来区分
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class UserId {
    
    private final Long value;
    
    private UserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        this.value = value;
    }
    
    /**
     * 创建用户ID值对象
     * 
     * @param value 用户ID值
     * @return 用户ID值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static UserId of(Long value) {
        return new UserId(value);
    }
    
    /**
     * 生成新的用户ID值对象
     * 
     * @return 用户ID值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static UserId generate() {
        // 使用雪花算法生成ID（这里简化处理，实际应该使用ID生成器）
        long id = System.currentTimeMillis();
        return new UserId(id);
    }
    
    /**
     * 获取用户ID值
     * 
     * @return 用户ID值
     * @author daidasheng
     * @date 2024-12-20
     */
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

