package com.petvet.rag.api.demo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LangGraph4j 简单分诊 Demo 请求
 * <p>
 * 用于演示基于 LangGraph4j 的宠物症状分诊流程（不影响现有接口）
 *
 * @author daidasheng
 * @date 2026-02-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoTriageReq {

    /**
     * 宠物症状描述
     */
    @NotBlank(message = "症状描述不能为空")
    private String symptomDesc;

    /**
     * 宠物昵称（可选，仅用于文案友好）
     */
    private String petName;
}

