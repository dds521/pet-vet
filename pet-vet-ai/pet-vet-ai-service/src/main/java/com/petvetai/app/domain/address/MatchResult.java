package com.petvetai.app.domain.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址匹配结果
 * 
 * @author PetVetAI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    
    /**
     * 匹配的行政区划编码
     */
    private String code;
    
    /**
     * 匹配的行政区划信息
     */
    private AdministrativeDivision division;
    
    /**
     * 匹配度分数（0-100）
     */
    private double score;
    
    /**
     * 是否检测到异常（多行政区划）
     */
    private boolean abnormal;
    
    /**
     * 异常原因
     */
    private String abnormalReason;
    
    public MatchResult(String code, AdministrativeDivision division, double score) {
        this.code = code;
        this.division = division;
        this.score = score;
        this.abnormal = false;
    }
}

