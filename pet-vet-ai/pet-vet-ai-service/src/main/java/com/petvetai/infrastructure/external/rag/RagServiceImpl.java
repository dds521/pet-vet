package com.petvetai.infrastructure.external.rag;

import com.petvet.rag.api.feign.RagServiceFeignClient;
import com.petvet.rag.api.dto.ApiResponse;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * RAG服务实现
 * 
 * 实现与RAG服务的交互
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {
    
    private final RagServiceFeignClient ragServiceFeignClient;
    
    @Override
    public RagResult query(String query) {
        // 构建 RAG 查询请求
        RagQueryReq ragRequest = RagQueryReq.builder()
            .query(query)
            .maxResults(5)  // 检索前5个最相关的文档
            .minScore(0.7)  // 最小相似度分数
            .enableGeneration(true)  // 启用 LLM 生成答案
            .modelName("deepseek")  // 使用 DeepSeek 模型
            .contextWindowSize(5)  // 使用前5个检索结果作为上下文
            .build();
        
        try {
            // 调用 RAG 服务
            ApiResponse<RagQueryResp> response = ragServiceFeignClient.query(ragRequest);
            
            if (response == null || response.getCode() != 200 || response.getData() == null) {
                log.warn("RAG 查询失败，响应: {}", response);
                throw new RuntimeException("RAG 服务调用失败: " + (response != null ? response.getMessage() : "未知错误"));
            }
            
            RagQueryResp ragResult = response.getData();
            
            // 提取生成的答案
            String answer = ragResult.getGeneratedAnswer();
            if (answer == null || answer.trim().isEmpty()) {
                // 如果没有生成答案，使用检索到的文档
                if (ragResult.getRetrievedDocuments() != null && !ragResult.getRetrievedDocuments().isEmpty()) {
                    answer = ragResult.getRetrievedDocuments().get(0).getText();
                } else {
                    answer = "未找到相关信息";
                }
            }
            
            // 计算置信度（基于检索到的文档数量和相似度分数）
            Double confidence = calculateConfidence(ragResult);
            
            log.info("RAG 查询完成，检索到 {} 个相关文档, 置信度: {}", 
                ragResult.getRetrievedCount(), confidence);
            
            return new RagResult(answer, confidence);
            
        } catch (Exception e) {
            log.error("RAG 查询失败", e);
            throw new RuntimeException("RAG 查询失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 计算诊断置信度
     * 基于检索到的文档数量和平均相似度分数
     * 
     * @param ragResult RAG 查询结果
     * @return 置信度分数（0.0-1.0）
     * @author daidasheng
     * @date 2024-12-20
     */
    private Double calculateConfidence(RagQueryResp ragResult) {
        if (ragResult.getRetrievedDocuments() == null || ragResult.getRetrievedDocuments().isEmpty()) {
            return 0.3;  // 没有检索到文档，置信度较低
        }
        
        // 计算平均相似度分数
        double avgScore = ragResult.getRetrievedDocuments().stream()
            .mapToDouble(doc -> doc.getScore() != null ? doc.getScore() : 0.0)
            .average()
            .orElse(0.0);
        
        // 基于文档数量和平均分数计算置信度
        int docCount = ragResult.getRetrievedCount();
        double confidence = Math.min(0.95, avgScore * 0.7 + (docCount >= 3 ? 0.2 : docCount * 0.067));
        
        return Math.max(0.5, confidence);  // 最低置信度 0.5
    }
}

