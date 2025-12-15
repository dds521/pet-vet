package com.petvet.rag.app.service;

import com.petvet.embedding.api.dto.ApiResponse;
import com.petvet.embedding.api.feign.ResumeParseFeignClient;
import com.petvet.embedding.api.req.ResumeSearchReq;
import com.petvet.embedding.api.resp.ResumeSearchResp;
import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.resp.RagQueryResp;
import com.petvet.rag.app.config.LangChainConfig;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 服务
 * 实现增强型检索（Retrieval-Augmented Generation）
 * 
 * @author PetVetRAG Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagService {
    
    private final ResumeParseFeignClient resumeParseFeignClient;
    private final ChatModel chatModel;
    private final LangChainConfig langChainConfig;
    
    @Value("${rag.generation.prompt-template:基于以下上下文信息回答用户的问题。如果上下文中没有相关信息，请说明无法从提供的信息中找到答案。\n\n上下文信息：\n{context}\n\n用户问题：{question}\n\n请提供详细、准确的答案：}")
    private String promptTemplate;
    
    /**
     * 执行 RAG 查询
     * 
     * @param request 查询请求
     * @return 查询响应
     */
    public RagQueryResp query(RagQueryReq request) {
        log.info("执行RAG查询，查询文本: {}", request.getQuery());
        
        // 1. 参数处理
        int maxResults = request.getMaxResults() != null ? request.getMaxResults() : 10;
        double minScore = request.getMinScore() != null ? request.getMinScore() : 0.7;
        boolean enableGeneration = request.getEnableGeneration() != null ? request.getEnableGeneration() : true;
        
        // 2. 检索阶段（Retrieval）
        List<RagQueryResp.RetrievedDocument> retrievedDocuments = retrieve(request.getQuery(), maxResults, minScore);
        
        // 3. 生成阶段（Generation，如果启用）
        String generatedAnswer = null;
        if (enableGeneration && !retrievedDocuments.isEmpty()) {
            generatedAnswer = generate(request.getQuery(), retrievedDocuments, request.getModelName());
        }
        
        // 4. 构建响应
        return RagQueryResp.builder()
            .retrievedCount(retrievedDocuments.size())
            .retrievedDocuments(retrievedDocuments)
            .generatedAnswer(generatedAnswer)
            .usedGeneration(enableGeneration && !retrievedDocuments.isEmpty())
            .build();
    }
    
    /**
     * 检索相关文档
     * 
     * @param query 查询文本
     * @param maxResults 最大结果数
     * @param minScore 最小相似度分数
     * @return 检索到的文档列表
     */
    private List<RagQueryResp.RetrievedDocument> retrieve(String query, int maxResults, double minScore) {
        log.debug("开始检索，查询: {}, maxResults: {}, minScore: {}", query, maxResults, minScore);
        
        try {
            // 调用 embedding 服务进行向量检索
            ResumeSearchReq searchReq = ResumeSearchReq.builder()
                .query(query)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
            
            log.debug("调用 embedding 服务进行向量检索，请求: {}", searchReq);
            log.debug("Feign 客户端服务名称: pet-vet-embedding, Nacos group: DEFAULT_GROUP");
            ApiResponse<ResumeSearchResp> response = resumeParseFeignClient.searchResume(searchReq);
            
            // 注意：pet-vet-embedding-api 的 ApiResponse 使用 success 字段，而不是 code 字段
            if (response == null || response.getSuccess() == null || !response.getSuccess() || response.getData() == null) {
                log.warn("向量检索失败，响应: {}", response);
                return new ArrayList<>();
            }
            
            ResumeSearchResp searchResp = response.getData();
            if (searchResp.getResults() == null || searchResp.getResults().isEmpty()) {
                log.debug("未检索到相关文档");
                return new ArrayList<>();
            }
            
            // 转换为 RAG 响应格式
            List<RagQueryResp.RetrievedDocument> documents = searchResp.getResults().stream()
                .map(item -> RagQueryResp.RetrievedDocument.builder()
                    .chunkId(item.getChunkId())
                    .score(item.getScore())
                    .text(item.getText())
                    // 注意：embedding 服务返回的 ResumeSearchResp 可能不包含 resumeId 和 fieldType
                    // 如果需要这些信息，需要扩展 ResumeSearchResp 或调用其他接口
                    .build())
                .collect(Collectors.toList());
            
            log.info("检索完成，找到 {} 个相关文档", documents.size());
            return documents;
            
        } catch (feign.FeignException.BadGateway e) {
            log.error("向量检索失败 - 502 Bad Gateway，可能是 embedding 服务不可用或负载均衡问题。错误信息: {}", e.getMessage());
            log.error("请检查：1) embedding 服务是否正常运行 2) 服务注册是否正常 3) 负载均衡器配置是否正确");
            return new ArrayList<>();
        } catch (feign.FeignException.ServiceUnavailable e) {
            log.error("向量检索失败 - 503 Service Unavailable，embedding 服务暂时不可用。错误信息: {}", e.getMessage());
            return new ArrayList<>();
        } catch (feign.FeignException e) {
            log.error("向量检索失败 - Feign 异常，状态码: {}, 错误信息: {}", e.status(), e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("向量检索失败 - 未知异常", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 基于检索到的文档生成答案
     * 
     * @param query 用户问题
     * @param documents 检索到的文档
     * @param modelName 模型名称（可选，用于动态选择模型）
     * @return 生成的答案
     */
    private String generate(String query, List<RagQueryResp.RetrievedDocument> documents, String modelName) {
        log.debug("开始生成答案，查询: {}, 文档数量: {}, 模型: {}", query, documents.size(), modelName);
        
        try {
            // 1. 构建上下文（取前N个文档，根据配置的上下文窗口大小）
            int contextWindowSize = 5; // 默认值，可以从配置读取
            List<RagQueryResp.RetrievedDocument> contextDocuments = documents.stream()
                .limit(contextWindowSize)
                .collect(Collectors.toList());
            
            // 2. 构建上下文文本
            StringBuilder contextBuilder = new StringBuilder();
            for (int i = 0; i < contextDocuments.size(); i++) {
                RagQueryResp.RetrievedDocument doc = contextDocuments.get(i);
                contextBuilder.append("文档").append(i + 1).append("：\n");
                contextBuilder.append(doc.getText()).append("\n\n");
            }
            String context = contextBuilder.toString().trim();
            
            // 3. 构建提示词
            String prompt = promptTemplate
                .replace("{context}", context)
                .replace("{question}", query);
            
            log.debug("生成的提示词长度: {}", prompt.length());
            
            // 4. 选择模型（如果指定了模型名称，使用指定的模型；否则使用默认模型）
            ChatModel model = chatModel;
            if (modelName != null && !modelName.trim().isEmpty()) {
                try {
                    model = langChainConfig.createModelByName(modelName);
                    log.debug("使用指定的模型: {}", modelName);
                } catch (Exception e) {
                    log.warn("无法创建指定模型 {}，使用默认模型", modelName, e);
                }
            }
            
            // 5. 调用LLM生成答案
            String answer = model.chat(prompt);
            
            log.info("答案生成完成，答案长度: {}", answer != null ? answer.length() : 0);
            return answer;
            
        } catch (Exception e) {
            log.error("答案生成失败", e);
            return "抱歉，生成答案时出现错误：" + e.getMessage();
        }
    }
}
