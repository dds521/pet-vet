package com.petvetai.domain.doctor.model;

/**
 * 医生状态枚举
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public enum DoctorStatus {
    
    /**
     * 待审核：医生已注册，等待审核资质
     */
    PENDING(0, "待审核"),
    
    /**
     * 已审核：资质审核通过，可以提供服务
     */
    APPROVED(1, "已审核"),
    
    /**
     * 已禁用：医生被禁用，不能提供服务
     */
    DISABLED(2, "已禁用"),
    
    /**
     * 审核失败：资质审核未通过
     */
    REJECTED(3, "审核失败");
    
    private final Integer code;
    private final String description;
    
    DoctorStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取状态
     * 
     * @param code 状态代码
     * @return 医生状态
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorStatus fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (DoctorStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
    
    /**
     * 获取状态代码
     * 
     * @return 状态代码
     * @author daidasheng
     * @date 2024-12-27
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     * @author daidasheng
     * @date 2024-12-27
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为已审核状态
     * 
     * @return 是否已审核
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 判断是否可以提供服务
     * 
     * @return 是否可以提供服务
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean canProvideService() {
        return this == APPROVED;
    }
}

