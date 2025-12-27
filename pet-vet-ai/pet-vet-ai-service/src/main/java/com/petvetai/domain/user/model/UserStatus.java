package com.petvetai.domain.user.model;

/**
 * 用户状态值对象
 * 
 * 封装用户状态信息，不可变
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public enum UserStatus {
    
    /**
     * 启用状态
     */
    ACTIVE(1, "启用"),
    
    /**
     * 禁用状态
     */
    DISABLED(0, "禁用");
    
    private final Integer code;
    private final String desc;
    
    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 获取状态码
     * 
     * @return 状态码
     * @author daidasheng
     * @date 2024-12-20
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据状态码获取状态
     * 
     * @param code 状态码
     * @return 用户状态
     * @author daidasheng
     * @date 2024-12-20
     */
    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return DISABLED;
        }
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return DISABLED;
    }
}

