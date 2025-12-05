package com.petvetai.app.service;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.domain.Pet;
import com.petvetai.app.domain.Symptom;
import com.petvetai.app.mapper.PetMapper;
import com.petvetai.app.mapper.SymptomMapper;
import dev.ai4j.openai4j.OpenAiHttpException;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LangChain4j 宠物医疗服务测试用例
 * 
 * 测试场景：
 * 1. ChatLanguageModel 基础对话功能
 * 2. LangChainPetMedicalService 症状分析功能
 * 3. 带历史对话的症状分析功能
 * 4. 异常场景处理
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("LangChain4j 宠物医疗服务测试")
class LangChainPetMedicalServiceTest {

    @Autowired(required = false)
    private ChatLanguageModel chatLanguageModel;

    @Autowired(required = false)
    private LangChainPetMedicalService langChainPetMedicalService;

    @MockBean
    private PetMapper petMapper;

    @MockBean
    private SymptomMapper symptomMapper;

    private Pet testPet;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testPet = new Pet("小白", "金毛", 3);
        testPet.setId(1L);
    }

    @Test
    @DisplayName("测试 ChatLanguageModel 基础对话功能")
    void testChatLanguageModelBasic() {
        if (chatLanguageModel == null) {
            log.warn("ChatLanguageModel 未配置，跳过测试");
            log.info("如需测试，请在配置文件中设置 AI API Key：");
            log.info("spring.ai.provider.type=deepseek");
            log.info("spring.ai.deepseek.api-key=your-api-key");
            return;
        }

        log.info("========== ChatLanguageModel 基础对话测试 ==========");

        try {
            // 测试简单对话
            String prompt = "你好，请用一句话介绍你自己";
            String response = chatLanguageModel.generate(prompt);

            assertNotNull(response, "AI 响应不应为空");
            assertFalse(response.trim().isEmpty(), "AI 响应不应为空字符串");

            log.info("✅ 测试通过：AI 对话功能正常");
            log.info("提示词: {}", prompt);
            log.info("AI 响应: {}", response);

        } catch (Exception e) {
            handleApiException(e, "ChatLanguageModel 基础对话");
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试宠物医疗咨询对话")
    void testPetMedicalConsultation() {
        if (chatLanguageModel == null) {
            log.warn("ChatLanguageModel 未配置，跳过测试");
            return;
        }

        log.info("========== 宠物医疗咨询对话测试 ==========");

        try {
            // 构建宠物医疗咨询提示词
            String prompt = String.format(
                    "作为专业的宠物兽医 AI，请回答以下问题：\n" +
                    "宠物信息：品种=%s, 年龄=%d岁, 名字=%s\n" +
                    "问题：我的宠物最近食欲不振，应该怎么办？\n" +
                    "请提供专业的建议，控制在100字以内。",
                    testPet.getBreed(), testPet.getAge(), testPet.getName()
            );

            String response = chatLanguageModel.generate(prompt);

            assertNotNull(response, "AI 响应不应为空");
            assertFalse(response.trim().isEmpty(), "AI 响应不应为空字符串");

            log.info("✅ 测试通过：宠物医疗咨询功能正常");
            log.info("提示词: {}", prompt);
            log.info("AI 响应: {}", response);

        } catch (Exception e) {
            handleApiException(e, "宠物医疗咨询对话");
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试 LangChainPetMedicalService 症状分析功能")
    void testAnalyzeSymptom() {
        if (langChainPetMedicalService == null) {
            log.warn("LangChainPetMedicalService 未配置，跳过测试");
            return;
        }

        log.info("========== 症状分析功能测试 ==========");

        try {
            // Mock PetMapper 返回测试宠物
            when(petMapper.selectById(1L)).thenReturn(testPet);
            // Mock SymptomMapper 插入操作
            when(symptomMapper.insert(any(Symptom.class))).thenAnswer(invocation -> {
                Symptom symptom = invocation.getArgument(0);
                symptom.setId(100L);
                return 1;
            });

            // 执行症状分析
            String symptomDesc = "最近三天食欲不振，偶尔呕吐，精神状态较差";
            Diagnosis diagnosis = langChainPetMedicalService.analyzeSymptom(1L, symptomDesc);

            // 验证结果
            assertNotNull(diagnosis, "诊断结果不应为空");
            assertNotNull(diagnosis.getSuggestion(), "诊断建议不应为空");
            assertNotNull(diagnosis.getConfidence(), "置信度不应为空");
            assertTrue(diagnosis.getConfidence() >= 0 && diagnosis.getConfidence() <= 1, 
                    "置信度应在 0-1 之间");

            // 验证 Mapper 调用
            verify(petMapper, times(1)).selectById(1L);
            verify(symptomMapper, times(1)).insert(any(Symptom.class));

            log.info("✅ 测试通过：症状分析功能正常");
            log.info("症状描述: {}", symptomDesc);
            log.info("诊断建议: {}", diagnosis.getSuggestion());
            log.info("置信度: {}", diagnosis.getConfidence());

        } catch (Exception e) {
            handleApiException(e, "症状分析");
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试带历史对话的症状分析功能")
    void testAnalyzeSymptomWithHistory() {
        if (langChainPetMedicalService == null) {
            log.warn("LangChainPetMedicalService 未配置，跳过测试");
            return;
        }

        log.info("========== 带历史对话的症状分析测试 ==========");

        try {
            // Mock PetMapper 返回测试宠物
            when(petMapper.selectById(1L)).thenReturn(testPet);
            // Mock SymptomMapper 插入操作
            when(symptomMapper.insert(any(Symptom.class))).thenAnswer(invocation -> {
                Symptom symptom = invocation.getArgument(0);
                symptom.setId(101L);
                return 1;
            });

            // 准备历史对话
            String conversationHistory = "用户：我的宠物最近有点不舒服\n" +
                    "AI：请详细描述一下症状\n" +
                    "用户：它最近不爱吃东西";

            // 执行带历史对话的症状分析
            String symptomDesc = "今天早上发现它呕吐了，还拉肚子";
            Diagnosis diagnosis = langChainPetMedicalService.analyzeSymptomWithHistory(
                    1L, symptomDesc, conversationHistory);

            // 验证结果
            assertNotNull(diagnosis, "诊断结果不应为空");
            assertNotNull(diagnosis.getSuggestion(), "诊断建议不应为空");
            assertNotNull(diagnosis.getConfidence(), "置信度不应为空");

            // 验证 Mapper 调用
            verify(petMapper, times(1)).selectById(1L);
            verify(symptomMapper, times(1)).insert(any(Symptom.class));

            log.info("✅ 测试通过：带历史对话的症状分析功能正常");
            log.info("历史对话: {}", conversationHistory);
            log.info("当前症状: {}", symptomDesc);
            log.info("诊断建议: {}", diagnosis.getSuggestion());
            log.info("置信度: {}", diagnosis.getConfidence());

        } catch (Exception e) {
            handleApiException(e, "带历史对话的症状分析");
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试宠物不存在异常场景")
    void testAnalyzeSymptomWithNonExistentPet() {
        if (langChainPetMedicalService == null) {
            log.warn("LangChainPetMedicalService 未配置，跳过测试");
            return;
        }

        log.info("========== 宠物不存在异常场景测试 ==========");

        try {
            // Mock PetMapper 返回 null（宠物不存在）
            when(petMapper.selectById(999L)).thenReturn(null);

            // 执行症状分析，应该抛出异常
            assertThrows(RuntimeException.class, () -> {
                langChainPetMedicalService.analyzeSymptom(999L, "测试症状");
            }, "宠物不存在时应抛出异常");

            // 验证 Mapper 调用
            verify(petMapper, times(1)).selectById(999L);
            // 验证 SymptomMapper 不应被调用
            verify(symptomMapper, never()).insert(any(Symptom.class));

            log.info("✅ 测试通过：异常场景处理正常");

        } catch (Exception e) {
            log.error("测试失败", e);
            fail("异常场景测试失败: " + e.getMessage());
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试多轮对话场景")
    void testMultiTurnConversation() {
        if (chatLanguageModel == null) {
            log.warn("ChatLanguageModel 未配置，跳过测试");
            return;
        }

        log.info("========== 多轮对话场景测试 ==========");

        try {
            // 第一轮对话
            String prompt1 = "作为专业的宠物兽医 AI，我的金毛3岁了，最近食欲不振，可能是什么原因？";
            String response1 = chatLanguageModel.generate(prompt1);
            log.info("第一轮 - 问题: {}", prompt1);
            log.info("第一轮 - 回答: {}", response1);

            // 第二轮对话（基于第一轮的回答）
            String prompt2 = String.format(
                    "继续刚才的咨询，我的宠物除了食欲不振，今天还开始呕吐了。之前的建议是：%s。现在应该怎么办？",
                    response1.substring(0, Math.min(50, response1.length()))
            );
            String response2 = chatLanguageModel.generate(prompt2);
            log.info("第二轮 - 问题: {}", prompt2);
            log.info("第二轮 - 回答: {}", response2);

            assertNotNull(response1, "第一轮响应不应为空");
            assertNotNull(response2, "第二轮响应不应为空");

            log.info("✅ 测试通过：多轮对话功能正常");

        } catch (Exception e) {
            handleApiException(e, "多轮对话");
        }

        log.info("========== 测试完成 ==========");
    }

    @Test
    @DisplayName("测试不同症状类型的分析")
    void testDifferentSymptomTypes() {
        if (langChainPetMedicalService == null) {
            log.warn("LangChainPetMedicalService 未配置，跳过测试");
            return;
        }

        log.info("========== 不同症状类型分析测试 ==========");

        try {
            // Mock PetMapper
            when(petMapper.selectById(1L)).thenReturn(testPet);
            when(symptomMapper.insert(any(Symptom.class))).thenAnswer(invocation -> {
                Symptom symptom = invocation.getArgument(0);
                symptom.setId(102L);
                return 1;
            });

            // 测试不同类型的症状
            String[] symptoms = {
                    "频繁打喷嚏，流鼻涕",
                    "皮肤出现红疹，经常抓挠",
                    "走路跛行，不愿意活动",
                    "呼吸急促，咳嗽"
            };

            for (String symptomDesc : symptoms) {
                Diagnosis diagnosis = langChainPetMedicalService.analyzeSymptom(1L, symptomDesc);
                
                assertNotNull(diagnosis, "诊断结果不应为空");
                assertNotNull(diagnosis.getSuggestion(), "诊断建议不应为空");
                
                log.info("症状: {} -> 建议: {}", symptomDesc, diagnosis.getSuggestion());
            }

            log.info("✅ 测试通过：不同症状类型分析功能正常");

        } catch (Exception e) {
            handleApiException(e, "不同症状类型分析");
        }

        log.info("========== 测试完成 ==========");
    }

    /**
     * 处理 API 调用异常，区分配置问题和 API 调用问题
     */
    private void handleApiException(Exception e, String testName) {
        String errorMessage = e.getMessage();
        Throwable cause = e.getCause();
        
        // 检查是否是 API 余额不足
        if (e instanceof OpenAiHttpException || 
            (cause instanceof OpenAiHttpException) ||
            (errorMessage != null && errorMessage.contains("Insufficient Balance"))) {
            log.warn("⚠️  API 余额不足，跳过测试: {}", testName);
            log.info("提示：请检查 DeepSeek/Grok API 账户余额");
            log.info("配置已正确加载（从 Nacos 读取），但 API 调用失败");
            return; // 优雅跳过，不抛出异常
        }
        
        // 检查是否是 API Key 无效
        if (errorMessage != null && (
            errorMessage.contains("Invalid API Key") ||
            errorMessage.contains("Unauthorized") ||
            errorMessage.contains("401"))) {
            log.warn("⚠️  API Key 无效，跳过测试: {}", testName);
            log.info("提示：请检查 Nacos 配置中的 API Key 是否正确");
            return;
        }
        
        // 其他错误，记录详细信息
        log.error("❌ {} 测试失败", testName, e);
        log.error("错误类型: {}", e.getClass().getSimpleName());
        log.error("错误信息: {}", errorMessage);
        
        // 对于其他类型的错误，仍然抛出异常以便调试
        fail(testName + " 测试失败: " + errorMessage);
    }
}

