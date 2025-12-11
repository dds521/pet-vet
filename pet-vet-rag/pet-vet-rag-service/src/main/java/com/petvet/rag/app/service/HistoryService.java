package com.petvet.rag.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petvet.rag.app.domain.RagQueryHistoryEntity;
import com.petvet.rag.app.mapper.RagQueryHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史记录服务
 * 管理用户查询历史记录的保存和查询
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryService {
    
    private final RagQueryHistoryMapper historyMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 异步保存历史记录
     * 
     * @param entity 历史记录实体
     * @author daidasheng
     * @date 2024-12-11
     */
    @Async
    public void saveAsync(RagQueryHistoryEntity entity) {
        try {
            // 设置创建时间
            if (entity.getCreateTime() == null) {
                entity.setCreateTime(LocalDateTime.now());
            }
            if (entity.getUpdateTime() == null) {
                entity.setUpdateTime(LocalDateTime.now());
            }
            
            // 保存到数据库
            historyMapper.insert(entity);
            
            log.debug("历史记录保存成功，用户: {}, 会话: {}, ID: {}", 
                entity.getUserId(), entity.getSessionId(), entity.getId());
                
        } catch (Exception e) {
            log.error("历史记录保存失败，用户: {}, 会话: {}", 
                entity.getUserId(), entity.getSessionId(), e);
        }
    }
    
    /**
     * 查询用户历史记录
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 历史记录列表
     * @author daidasheng
     * @date 2024-12-11
     */
    public List<RagQueryHistoryEntity> queryHistory(String userId, String sessionId, 
                                                     Integer pageNum, Integer pageSize) {
        try {
            // 简单实现：查询最近N条记录
            // TODO: 后续可以优化为真正的分页查询
            int limit = pageSize != null ? pageSize : 20;
            int offset = pageNum != null ? (pageNum - 1) * limit : 0;
            
            // 这里需要扩展Mapper支持分页查询
            // 暂时返回空列表，后续实现
            log.debug("查询历史记录，用户: {}, 会话: {}, 页码: {}, 大小: {}", 
                userId, sessionId, pageNum, pageSize);
            
            return List.of();
            
        } catch (Exception e) {
            log.error("查询历史记录失败，用户: {}, 会话: {}", userId, sessionId, e);
            return List.of();
        }
    }
    
    /**
     * 构建历史记录实体
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param query 查询文本
     * @param answer 生成的答案
     * @param retrievedCount 检索到的文档数量
     * @param retrievedDocuments 检索到的文档（JSON格式）
     * @param conversationContext 对话上下文（JSON格式）
     * @param modelName 模型名称
     * @param queryTime 查询耗时（毫秒）
     * @param confidence 置信度
     * @return 历史记录实体
     * @author daidasheng
     * @date 2024-12-11
     */
    public RagQueryHistoryEntity buildHistoryEntity(String userId, String sessionId,
                                                    String query, String answer,
                                                    Integer retrievedCount,
                                                    Object retrievedDocuments,
                                                    Object conversationContext,
                                                    String modelName,
                                                    Long queryTime,
                                                    Double confidence) {
        try {
            // 将对象转换为JSON字符串
            String retrievedDocumentsJson = null;
            if (retrievedDocuments != null) {
                retrievedDocumentsJson = objectMapper.writeValueAsString(retrievedDocuments);
            }
            
            String conversationContextJson = null;
            if (conversationContext != null) {
                conversationContextJson = objectMapper.writeValueAsString(conversationContext);
            }
            
            return RagQueryHistoryEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .query(query)
                .generatedAnswer(answer)
                .retrievedCount(retrievedCount)
                .retrievedDocuments(retrievedDocumentsJson)
                .conversationContext(conversationContextJson)
                .modelName(modelName != null ? modelName : "deepseek")
                .queryTime(queryTime)
                .confidence(confidence)
                .enableGeneration(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("构建历史记录实体失败", e);
            // 返回基础实体
            return RagQueryHistoryEntity.builder()
                .userId(userId)
                .sessionId(sessionId)
                .query(query)
                .generatedAnswer(answer)
                .retrievedCount(retrievedCount)
                .modelName(modelName != null ? modelName : "deepseek")
                .queryTime(queryTime)
                .confidence(confidence)
                .enableGeneration(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        }
    }
}
