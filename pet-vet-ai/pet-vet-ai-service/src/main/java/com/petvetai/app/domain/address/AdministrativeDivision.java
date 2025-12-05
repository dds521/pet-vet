package com.petvetai.app.domain.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行政区划数据模型
 * 
 * @author PetVetAI
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
     * 街道/乡镇
     */
    private String street;
    
    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        return province + city + district + street;
    }
    
    /**
     * 获取地址层级数组 [省, 市, 区, 街道]
     */
    public String[] getLevels() {
        return new String[]{province, city, district, street};
    }
    
    /**
     * 获取地址层级数量（非空层级）
     */
    public int getLevelCount() {
        int count = 0;
        if (province != null && !province.isEmpty()) count++;
        if (city != null && !city.isEmpty()) count++;
        if (district != null && !district.isEmpty()) count++;
        if (street != null && !street.isEmpty()) count++;
        return count;
    }
}

