package com.petvet.rag.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 记忆服务
 * 基于 Redis 实现对话历史记忆管理
 * 
 * @author daidasheng
 * @date 2024-12-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemoryService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${rag.memory.window-size:10}")
    private Integer windowSize; // 对话窗口大小，默认保留最近10轮
    
    @Value("${rag.memory.ttl-days:30}")
    private Integer ttlDays; // 过期时间，默认30天
    
    private static final String MEMORY_KEY_PREFIX = "rag:memory:";
    
    /**
     * 加载对话记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 对话记忆
     * @author daidasheng
     * @date 2024-12-11
     */
    public ConversationMemory loadConversation(String userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                ConversationMemory memory = objectMapper.convertValue(value, ConversationMemory.class);
                log.debug("从Redis加载对话记忆，用户: {}, 会话: {}, 消息数: {}", userId, sessionId, memory.getMessages().size());
                return memory;
            }
        } catch (Exception e) {
            log.warn("加载对话记忆失败，用户: {}, 会话: {}", userId, sessionId, e);
        }
        
        // 如果Redis中没有，返回空的记忆
        return ConversationMemory.builder()
            .userId(userId)
            .sessionId(sessionId)
            .messages(new ArrayList<>())
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 添加消息到对话记忆
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param role 角色（USER 或 ASSISTANT）
     * @param content 消息内容
     * @author daidasheng
     * @date 2024-12-11
     */
    public void addMessage(String userId, String sessionId, String role, String content) {
        String key = buildKey(userId, sessionId);
        
        try {
            ConversationMemory memory = loadConversation(userId, sessionId);
            
            // 添加新消息
            ConversationMemory.Message message = ConversationMemory.Message.builder()
                .role(role)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
            memory.getMessages().add(message);
            
            // 应用窗口大小限制
            trimToWindowSize(memory);
            
            // 更新最后更新时间
            memory.setLastUpdateTime(System.currentTimeMillis());
            
            // 保存到Redis
            redisTemplate.opsForValue().set(key, memory, ttlDays, TimeUnit.DAYS);
            
            log.debug("保存对话记忆，用户: {}, 会话: {}, 消息数: {}", 
                userId, sessionId, memory.getMessages().size());
                
        } catch (Exception e) {
            log.error("保存对话记忆失败，用户: {}, 会话: {}", userId, sessionId, e);
        }
    }
    
    /**
     * 应用窗口大小限制
     * 保留最近N轮对话（每轮包含USER和ASSISTANT两条消息）
     * 
     * @param memory 对话记忆
     * @author daidasheng
     * @date 2024-12-11
     */
    private void trimToWindowSize(ConversationMemory memory) {
        List<ConversationMemory.Message> messages = memory.getMessages();
        int maxMessages = windowSize * 2; // 每轮2条消息（USER + ASSISTANT）
        
        if (messages.size() > maxMessages) {
            // 保留最近的消息
            List<ConversationMemory.Message> recentMessages = new ArrayList<>(
                messages.subList(messages.size() - maxMessages, messages.size())
            );
            memory.setMessages(recentMessages);
            log.debug("对话记忆已裁剪，保留最近 {} 轮对话", windowSize);
        }
    }
    
    /**
     * 构建Redis Key
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return Redis Key
     * @author daidasheng
     * @date 2024-12-11
     */
    private String buildKey(String userId, String sessionId) {
        return MEMORY_KEY_PREFIX + userId + ":" + sessionId;
    }
    
    /**
     * 对话记忆数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConversationMemory {
        private String userId;
        private String sessionId;
        private List<Message> messages;
        private Long lastUpdateTime;
        
        @lombok.Data
        @lombok.Builder
        @lombok.NoArgsConstructor
        @lombok.AllArgsConstructor
        public static class Message {
            private String role; // USER 或 ASSISTANT
            private String content;
            private Long timestamp;
        }
    }
}
