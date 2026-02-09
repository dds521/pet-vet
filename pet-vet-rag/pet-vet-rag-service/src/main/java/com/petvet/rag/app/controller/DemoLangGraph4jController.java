package com.petvet.rag.app.controller;

import com.petvet.rag.api.constants.ApiConstants;
import com.petvet.rag.api.demo.DemoTriageReq;
import com.petvet.rag.api.demo.DemoTriageResp;
import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.app.graph.demo.PetVetLangGraph4jDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LangGraph4j 简单分诊 Demo 控制器
 * 
 * 新增接口，仅用于验证 LangGraph4j 集成，不影响现有 RAG 功能。
 *
 * @author daidasheng
 * @date 2026-02-09
 */
@RestController
@RequestMapping(ApiConstants.RAG_API_PREFIX + "/demo")
@RequiredArgsConstructor
@Slf4j
public class DemoLangGraph4jController {

    private final PetVetLangGraph4jDemoService demoService;

    /**
     * LangGraph4j 简单分诊 Demo
     *
     * @param req DemoTriageReq
     * @return DemoTriageResp
     * @author daidasheng
     * @date 2026-02-09
     */
    @PostMapping("/langgraph4j-triage")
    public ResponseEntity<ApiResponse<DemoTriageResp>> triage(@Validated @RequestBody DemoTriageReq req) {
        try {
            DemoTriageResp resp = demoService.runDemo(req);
            return ResponseEntity.ok(ApiResponse.success(resp, "LangGraph4j 分诊 Demo 执行成功"));
        } catch (Exception e) {
            log.error("LangGraph4j 分诊 Demo 执行失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("分诊 Demo 执行失败: " + e.getMessage()));
        }
    }
}

