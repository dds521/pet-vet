package com.petvetai.infrastructure.util.address;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址匹配结果
 * 
 * 用于地址匹配工具
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Data
@NoArgsConstructor
public class MatchResult {
    
    /**
     * 匹配到的行政区划编码
     */
    private String code;
    
    /**
     * 匹配到的行政区划对象
     */
    private AdministrativeDivision division;
    
    /**
     * 匹配分数（0.0-100.0）
     */
    private double score;
    
    /**
     * 是否异常
     */
    private boolean isAbnormal;
    
    /**
     * 异常原因
     */
    private String abnormalReason;
    
    /**
     * 构造函数（不含异常信息）
     * 
     * @param code 行政区划编码
     * @param division 行政区划对象
     * @param score 匹配分数
     * @author daidasheng
     * @date 2024-12-20
     */
    public MatchResult(String code, AdministrativeDivision division, double score) {
        this.code = code;
        this.division = division;
        this.score = score;
        this.isAbnormal = false;
    }
    
    /**
     * 构造函数（含异常信息）
     * 
     * @param code 行政区划编码
     * @param division 行政区划对象
     * @param score 匹配分数
     * @param isAbnormal 是否异常
     * @param abnormalReason 异常原因
     * @author daidasheng
     * @date 2024-12-20
     */
    public MatchResult(String code, AdministrativeDivision division, double score, 
                      boolean isAbnormal, String abnormalReason) {
        this.code = code;
        this.division = division;
        this.score = score;
        this.isAbnormal = isAbnormal;
        this.abnormalReason = abnormalReason;
    }
}

