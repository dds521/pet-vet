package com.petvet.rag.api.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LangGraph4j 简单分诊 Demo 响应
 *
 * @author daidasheng
 * @date 2026-02-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoTriageResp {

    /**
     * 分诊级别（如：紧急 / 建议尽快就诊 / 可观察）
     */
    private String triageLevel;

    /**
     * 生成的总结文案（包含症状与建议）
     */
    private String summary;
}

