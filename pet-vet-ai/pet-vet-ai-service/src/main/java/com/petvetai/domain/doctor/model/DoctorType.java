package com.petvetai.domain.doctor.model;

/**
 * 医生类型枚举
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public enum DoctorType {
    
    /**
     * 个人：个人执业医生
     */
    INDIVIDUAL(1, "个人"),
    
    /**
     * 机构：机构执业医生
     */
    INSTITUTION(2, "机构");
    
    private final Integer code;
    private final String description;
    
    DoctorType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取类型
     * 
     * @param code 类型代码
     * @return 医生类型
     * @author daidasheng
     * @date 2024-12-27
     */
    public static DoctorType fromCode(Integer code) {
        if (code == null) {
            return INDIVIDUAL;
        }
        for (DoctorType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return INDIVIDUAL;
    }
    
    /**
     * 获取类型代码
     * 
     * @return 类型代码
     * @author daidasheng
     * @date 2024-12-27
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 获取类型描述
     * 
     * @return 类型描述
     * @author daidasheng
     * @date 2024-12-27
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 判断是否为个人类型
     * 
     * @return 是否为个人类型
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isIndividual() {
        return this == INDIVIDUAL;
    }
    
    /**
     * 判断是否为机构类型
     * 
     * @return 是否为机构类型
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isInstitution() {
        return this == INSTITUTION;
    }
}

