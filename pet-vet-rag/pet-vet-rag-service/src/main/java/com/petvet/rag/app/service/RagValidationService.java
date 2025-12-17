package com.petvet.rag.app.service;

import com.petvet.rag.api.req.RagQueryReq;
import com.petvet.rag.api.req.RagValidationReq;
import com.petvet.rag.api.resp.RagQueryResp;
import com.petvet.rag.api.resp.RagValidationResp;
import com.petvet.rag.app.classifier.config.ClassifierProperties;
import com.petvet.rag.app.classifier.model.ClassificationResult;
import com.petvet.rag.app.config.LangChainConfig;
import com.petvet.rag.app.domain.VetRagQueryHistoryEntity;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RAG 验证服务
 * 核心业务逻辑：整合检索、生成、记忆管理等功能
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagValidationService {
    
    private final RagService ragService;
    private final MemoryService memoryService;
    private final HistoryService historyService;
    private final QueryClassifier queryClassifier; // 原有分类器
    private final HybridQueryClassifier hybridQueryClassifier; // 混合方案分类器
    private final ClassifierProperties classifierProperties; // 分类器配置
    private final ChatModel chatModel;
    private final LangChainConfig langChainConfig;
    
    @Value("${rag.generation.prompt-template:基于以下上下文信息回答用户的问题。如果上下文中没有相关信息，请说明无法从提供的信息中找到答案。\n\n上下文信息：\n{context}\n\n用户问题：{question}\n\n请提供详细、准确的答案：}")
    private String promptTemplate;
    
    @Value("${rag.retrieval.min-retrieval-score:0.6}")
    private Double minRetrievalScore;
    
    /**
     * 执行 RAG 验证
     * 
     * @param request 验证请求
     * @author daidasheng
     * @date 2024-12-11
     * @return 验证响应
     */
    public RagValidationResp validate(RagValidationReq request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 参数处理
            String userId = request.getUserId();
            String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : generateSessionId();
            String query = request.getQuery();
            int maxResults = request.getMaxResults() != null ? request.getMaxResults() : 5;
            double minScore = request.getMinScore() != null ? request.getMinScore() : 0.7;
            boolean enableGeneration = request.getEnableGeneration() != null ? request.getEnableGeneration() : true;
            int contextWindowSize = request.getContextWindowSize() != null ? request.getContextWindowSize() : 5;
            
            log.info("开始RAG验证，用户: {}, 会话: {}, 查询: {}", userId, sessionId, query);
            
            // 2. 加载历史记忆
            MemoryService.ConversationMemory memory = memoryService.loadConversation(userId, sessionId);
            log.debug("加载历史记忆，消息数: {}", memory.getMessages().size());
            
            // 3. 判断是否需要检索知识库（支持新旧方案对比验证）
            ClassificationResult classificationResult = null;
            boolean needRetrieval;
            String classifierType = "original"; // 记录使用的分类器类型
            Boolean originalResult = null; // 用于对比验证
            
            // 判断是否启用对比模式
            boolean enableCompare = classifierProperties.getCompareMode() != null && classifierProperties.getCompareMode();
            boolean useHybrid = classifierProperties.getHybrid().getEnabled() != null && classifierProperties.getHybrid().getEnabled();
            
            if (enableCompare && useHybrid) {
                // 对比模式：同时运行新旧方案
                originalResult = queryClassifier.needRetrieval(query, memory);
                try {
                    classificationResult = hybridQueryClassifier.classify(query, memory);
                    if (classificationResult != null && classificationResult.getNeedRetrieval() != null) {
                        needRetrieval = classificationResult.getNeedRetrieval();
                        classifierType = "hybrid";
                        
                        // 对比结果
                        boolean isSame = originalResult.equals(needRetrieval);
                        log.info("【对比验证】query: {}, 原有方案: {}, 混合方案: {}, 结果一致: {}, 策略: {}, 置信度: {}, 耗时: {}ms",
                            query, originalResult, needRetrieval, isSame,
                            classificationResult.getStrategyName(), classificationResult.getConfidence(),
                            classificationResult.getCostTime());
                        
                        if (!isSame) {
                            log.warn("【对比验证】新旧方案结果不一致！query: {}, 原有: {}, 混合: {}, 策略: {}, 原因: {}",
                                query, originalResult, needRetrieval, classificationResult.getStrategyName(),
                                classificationResult.getReason());
                        }
                    } else {
                        // 混合方案返回null，使用原有结果
                        needRetrieval = originalResult;
                        log.warn("混合方案返回null，使用原有结果, query: {}, needRetrieval: {}", query, needRetrieval);
                    }
                } catch (Exception e) {
                    // 异常时使用原有结果
                    needRetrieval = originalResult;
                    log.error("混合方案执行失败，使用原有结果, query: {}", query, e);
                }
            } else if (useHybrid) {
                // 仅使用混合方案
                try {
                    classificationResult = hybridQueryClassifier.classify(query, memory);
                    if (classificationResult != null && classificationResult.getNeedRetrieval() != null) {
                        needRetrieval = classificationResult.getNeedRetrieval();
                        classifierType = "hybrid";
                        log.info("使用混合方案分类, query: {}, needRetrieval: {}, strategy: {}, confidence: {}, cost: {}ms",
                            query, needRetrieval, classificationResult.getStrategyName(),
                            classificationResult.getConfidence(), classificationResult.getCostTime());
                    } else {
                        // 降级到原有实现
                        needRetrieval = queryClassifier.needRetrieval(query, memory);
                        log.warn("混合方案返回null，降级到原有实现, query: {}", query);
                    }
                } catch (Exception e) {
                    // 异常时降级到原有实现
                    needRetrieval = queryClassifier.needRetrieval(query, memory);
                    log.error("混合方案执行失败，降级到原有实现, query: {}", query, e);
                }
            } else {
                // 使用原有实现
                needRetrieval = queryClassifier.needRetrieval(query, memory);
                log.debug("使用原有分类器, query: {}, needRetrieval: {}", query, needRetrieval);
            }
            
            // 记录分类结果详情用于验证
            if (classificationResult != null) {
                log.info("分类结果详情 - query: {}, classifier: {}, needRetrieval: {}, strategy: {}, confidence: {}, cacheHit: {}, cost: {}ms",
                    query, classifierType, needRetrieval, classificationResult.getStrategyName(),
                    classificationResult.getConfidence(), classificationResult.getCacheHit(),
                    classificationResult.getCostTime());
            }
            
            List<RagValidationResp.RetrievedDocument> retrievedDocuments = new ArrayList<>();
            String answer = null;
            boolean usedKnowledgeBase = false;
            
            if (needRetrieval) {
                // 4. 向量检索
                RagQueryResp ragResult = ragService.query(RagQueryReq.builder()
                    .query(query)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .enableGeneration(false) // 先只检索，不生成
                    .build());
                
                if (ragResult.getRetrievedDocuments() != null && !ragResult.getRetrievedDocuments().isEmpty()) {
                    // 检查检索结果质量
                    double topScore = ragResult.getRetrievedDocuments().get(0).getScore();
                    if (topScore >= minRetrievalScore) {
                        retrievedDocuments = convertToValidationDocuments(ragResult.getRetrievedDocuments());
                        usedKnowledgeBase = true;
                        log.info("检索到高质量结果，相似度: {}, 文档数: {}", topScore, retrievedDocuments.size());
                    } else {
                        log.info("检索结果质量低，相似度: {}, 跳过使用知识库", topScore);
                    }
                }
            }
            
            // 5. 生成答案
            if (enableGeneration) {
                if (usedKnowledgeBase && !retrievedDocuments.isEmpty()) {
                    // RAG 模式：使用知识库 + 历史对话
                    answer = generateWithRag(query, retrievedDocuments, memory, contextWindowSize, request.getModelName());
                } else {
                    // 纯大模型模式：只使用历史对话
                    answer = generateWithHistoryOnly(query, memory, request.getModelName());
                }
            }
            
            // 6. 更新记忆
            memoryService.addMessage(userId, sessionId, "USER", query);
            if (answer != null) {
                memoryService.addMessage(userId, sessionId, "ASSISTANT", answer);
            }
            
            // 7. 构建对话历史响应
            RagValidationResp.ConversationHistory conversationHistory = buildConversationHistory(memory);
            
            // 8. 计算置信度
            Double confidence = calculateConfidence(retrievedDocuments, usedKnowledgeBase);
            
            // 9. 计算查询耗时
            long queryTime = System.currentTimeMillis() - startTime;
            
            // 10. 异步保存历史记录
            saveHistoryAsync(userId, sessionId, request, answer, retrievedDocuments, conversationHistory, queryTime, confidence);
            
            // 11. 构建响应
            return RagValidationResp.builder()
                .answer(answer)
                .retrievedCount(retrievedDocuments.size())
                .retrievedDocuments(retrievedDocuments)
                .conversationHistory(conversationHistory)
                .confidence(confidence)
                .queryTime(queryTime)
                .sessionId(sessionId)
                .usedKnowledgeBase(usedKnowledgeBase)
                .build();
                
        } catch (Exception e) {
            log.error("RAG验证失败", e);
            throw new RuntimeException("RAG验证失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * RAG 模式生成答案（使用知识库 + 历史对话）
     * 
     * @param query 用户查询
     * @param documents 检索到的文档
     * @param memory 对话记忆
     * @param contextWindowSize 上下文窗口大小
     * @param modelName 模型名称
     * @return 生成的答案
     * @author daidasheng
     * @date 2024-12-11
     */
    private String generateWithRag(String query, 
                                   List<RagValidationResp.RetrievedDocument> documents,
                                   MemoryService.ConversationMemory memory,
                                   int contextWindowSize,
                                   String modelName) {
        try {
            // 1. 构建历史对话上下文（最近3-5轮）
            String historyContext = buildHistoryContext(memory, 3);
            
            // 2. 构建检索文档上下文
            String documentContext = buildDocumentContext(documents, contextWindowSize);
            
            // 3. 构建完整上下文
            String context = "";
            if (StringUtils.hasText(historyContext)) {
                context += "[历史对话上下文]\n" + historyContext + "\n\n";
            }
            if (StringUtils.hasText(documentContext)) {
                context += "[检索到的相关文档]\n" + documentContext;
            }
            
            // 4. 构建Prompt
            String prompt = promptTemplate
                .replace("{context}", context)
                .replace("{question}", query);
            
            // 5. 选择模型
            ChatModel model = chatModel;
            if (StringUtils.hasText(modelName)) {
                try {
                    model = langChainConfig.createModelByName(modelName);
                } catch (Exception e) {
                    log.warn("无法创建指定模型 {}，使用默认模型", modelName, e);
                }
            }
            
            // 6. 调用大模型生成答案
            String answer = model.chat(prompt);
            log.info("RAG模式生成答案完成，答案长度: {}", answer != null ? answer.length() : 0);
            return answer;
            
        } catch (Exception e) {
            log.error("RAG模式生成答案失败", e);
            return "抱歉，生成答案时出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 纯大模型模式生成答案（只使用历史对话）
     * 
     * @param query 用户查询
     * @param memory 对话记忆
     * @param modelName 模型名称
     * @return 生成的答案
     * @author daidasheng
     * @date 2024-12-11
     */
    private String generateWithHistoryOnly(String query,
                                           MemoryService.ConversationMemory memory,
                                           String modelName) {
        try {
            // 1. 构建历史对话上下文
            String historyContext = buildHistoryContext(memory, 5);
            
            // 2. 构建Prompt
            String prompt = "作为专业的宠物医疗AI助手，基于以下对话历史回答用户的问题。\n\n";
            if (StringUtils.hasText(historyContext)) {
                prompt += "对话历史：\n" + historyContext + "\n\n";
            }
            prompt += "用户当前问题：" + query + "\n\n请提供详细、准确的答案：";
            
            // 3. 选择模型
            ChatModel model = chatModel;
            if (StringUtils.hasText(modelName)) {
                try {
                    model = langChainConfig.createModelByName(modelName);
                } catch (Exception e) {
                    log.warn("无法创建指定模型 {}，使用默认模型", modelName, e);
                }
            }
            
            // 4. 调用大模型生成答案
            String answer = model.chat(prompt);
            log.info("纯大模型模式生成答案完成，答案长度: {}", answer != null ? answer.length() : 0);
            return answer;
            
        } catch (Exception e) {
            log.error("纯大模型模式生成答案失败", e);
            return "抱歉，生成答案时出现错误：" + e.getMessage();
        }
    }
    
    /**
     * 构建历史对话上下文
     * 
     * @param memory 对话记忆
     * @param maxRounds 最大轮数
     * @return 历史对话上下文文本
     * @author daidasheng
     * @date 2024-12-11
     */
    private String buildHistoryContext(MemoryService.ConversationMemory memory, int maxRounds) {
        if (memory == null || memory.getMessages() == null || memory.getMessages().isEmpty()) {
            return "";
        }
        
        List<MemoryService.ConversationMemory.Message> messages = memory.getMessages();
        int maxMessages = maxRounds * 2; // 每轮2条消息
        int startIndex = Math.max(0, messages.size() - maxMessages);
        
        StringBuilder context = new StringBuilder();
        for (int i = startIndex; i < messages.size(); i++) {
            MemoryService.ConversationMemory.Message msg = messages.get(i);
            String role = "用户".equals(msg.getRole()) || "USER".equals(msg.getRole()) ? "用户" : "助手";
            context.append(role).append("：").append(msg.getContent()).append("\n");
        }
        
        return context.toString().trim();
    }
    
    /**
     * 构建文档上下文
     * 
     * @param documents 检索到的文档
     * @param contextWindowSize 上下文窗口大小
     * @return 文档上下文文本
     * @author daidasheng
     * @date 2024-12-11
     */
    private String buildDocumentContext(List<RagValidationResp.RetrievedDocument> documents, int contextWindowSize) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        int count = Math.min(documents.size(), contextWindowSize);
        
        for (int i = 0; i < count; i++) {
            RagValidationResp.RetrievedDocument doc = documents.get(i);
            context.append("文档").append(i + 1).append("（相似度：").append(doc.getScore()).append("）：\n");
            context.append(doc.getText()).append("\n\n");
        }
        
        return context.toString().trim();
    }
    
    /**
     * 转换文档格式
     * 
     * @param documents 原始文档列表
     * @return 转换后的文档列表
     * @author daidasheng
     * @date 2024-12-11
     */
    private List<RagValidationResp.RetrievedDocument> convertToValidationDocuments(
            List<RagQueryResp.RetrievedDocument> documents) {
        return documents.stream()
            .map(doc -> RagValidationResp.RetrievedDocument.builder()
                .chunkId(doc.getChunkId())
                .score(doc.getScore())
                .text(doc.getText())
                .resumeId(doc.getResumeId())
                .fieldType(doc.getFieldType())
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * 构建对话历史响应
     * 
     * @param memory 对话记忆
     * @return 对话历史响应
     * @author daidasheng
     * @date 2024-12-11
     */
    private RagValidationResp.ConversationHistory buildConversationHistory(
            MemoryService.ConversationMemory memory) {
        if (memory == null || memory.getMessages() == null) {
            return RagValidationResp.ConversationHistory.builder()
                .messageCount(0)
                .recentMessages(new ArrayList<>())
                .build();
        }
        
        // 转换消息格式
        List<RagValidationResp.Message> messages = memory.getMessages().stream()
            .map(msg -> RagValidationResp.Message.builder()
                .role(msg.getRole())
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .build())
            .collect(Collectors.toList());
        
        // 只返回最近的消息（最多10条）
        int maxMessages = 10;
        List<RagValidationResp.Message> recentMessages = messages.size() > maxMessages
            ? messages.subList(messages.size() - maxMessages, messages.size())
            : messages;
        
        return RagValidationResp.ConversationHistory.builder()
            .messageCount(messages.size())
            .recentMessages(recentMessages)
            .build();
    }
    
    /**
     * 计算置信度
     * 
     * @param documents 检索到的文档
     * @param usedKnowledgeBase 是否使用了知识库
     * @return 置信度分数
     * @author daidasheng
     * @date 2024-12-11
     */
    private Double calculateConfidence(List<RagValidationResp.RetrievedDocument> documents,
                                      boolean usedKnowledgeBase) {
        if (!usedKnowledgeBase || documents == null || documents.isEmpty()) {
            return 0.7; // 默认置信度
        }
        
        // 基于平均相似度分数计算置信度
        double avgScore = documents.stream()
            .mapToDouble(doc -> doc.getScore() != null ? doc.getScore() : 0.0)
            .average()
            .orElse(0.0);
        
        // 基于文档数量和平均分数计算置信度
        int docCount = documents.size();
        double confidence = Math.min(0.95, avgScore * 0.7 + (docCount >= 3 ? 0.2 : docCount * 0.067));
        
        return Math.max(0.5, confidence);
    }
    
    /**
     * 异步保存历史记录
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param request 请求
     * @param answer 答案
     * @param documents 文档
     * @param conversationHistory 对话历史
     * @param queryTime 查询耗时
     * @param confidence 置信度
     * @author daidasheng
     * @date 2024-12-11
     */
    private void saveHistoryAsync(String userId, String sessionId,
                                  RagValidationReq request, String answer,
                                  List<RagValidationResp.RetrievedDocument> documents,
                                  RagValidationResp.ConversationHistory conversationHistory,
                                  Long queryTime, Double confidence) {
        try {
            VetRagQueryHistoryEntity entity = historyService.buildHistoryEntity(
                userId, sessionId,
                request.getQuery(), answer,
                documents.size(),
                documents,
                conversationHistory,
                request.getModelName(),
                queryTime,
                confidence
            );
            
            historyService.saveAsync(entity);
        } catch (Exception e) {
            log.error("异步保存历史记录失败", e);
        }
    }
    
    /**
     * 生成会话ID
     * 
     * @return 会话ID
     * @author daidasheng
     * @date 2024-12-11
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    
}
