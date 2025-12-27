package com.petvetai.infrastructure.util.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行政区划数据模型
 * 
 * 用于地址匹配工具
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministrativeDivision {
    
    /**
     * 行政区划编码
     */
    private String code;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 街道
     */
    private String street;
    
    /**
     * 完整地址
     */
    private String fullAddress;
    
    /**
     * 级别：1-省，2-市，3-区县，4-街道
     */
    private Integer level;
    
    /**
     * 获取地址层级数组
     * 
     * @return 地址层级数组 [省份, 城市, 区县, 街道]
     * @author daidasheng
     * @date 2024-12-20
     */
    public String[] getLevels() {
        return new String[]{province, city, district, street};
    }
}

