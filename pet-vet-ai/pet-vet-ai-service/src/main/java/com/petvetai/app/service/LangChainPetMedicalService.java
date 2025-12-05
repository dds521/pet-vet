package com.petvetai.app.service;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.domain.Pet;
import com.petvetai.app.domain.Symptom;
import com.petvetai.app.mapper.PetMapper;
import com.petvetai.app.mapper.SymptomMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基于 LangChain4j 的宠物医疗服务
 * 
 * 使用 LangChain4j 框架进行 AI 对话处理
 * 支持更复杂的 AI 工作流，如 RAG、工具调用等
 */
@Service
public class LangChainPetMedicalService {

    private final ChatLanguageModel chatLanguageModel;
    private final PetMapper petMapper;
    private final SymptomMapper symptomMapper;

    public LangChainPetMedicalService(
            ChatLanguageModel chatLanguageModel,
            PetMapper petMapper,
            SymptomMapper symptomMapper) {
        this.chatLanguageModel = chatLanguageModel;
        this.petMapper = petMapper;
        this.symptomMapper = symptomMapper;
    }

    @Transactional
    public Diagnosis analyzeSymptom(Long petId, String symptomDesc) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("Pet not found");
        }

        // 构建提示词
        String prompt = String.format(
                "作为专业的宠物兽医 AI，基于以下宠物信息分析症状：品种=%s, 年龄=%d。症状：%s。请提供初步诊断建议、可能原因和推荐行动。输出格式：建议：[建议文本]；置信度：[0-1 分数]。",
                pet.getBreed(), pet.getAge(), symptomDesc
        );

        // 使用 LangChain4j 进行 AI 对话
        String aiResponse = chatLanguageModel.generate(prompt);

        // 解析响应
        String suggestion = aiResponse.contains("建议：") 
                ? aiResponse.split("建议：")[1].split("；")[0] 
                : aiResponse;
        Double confidence = 0.8;

        Diagnosis diagnosis = new Diagnosis(suggestion, confidence);

        // 使用 MyBatis Plus 保存症状
        Symptom symptom = new Symptom(symptomDesc, pet.getId());
        symptomMapper.insert(symptom);

        return diagnosis;
    }

    /**
     * 使用 LangChain4j 进行带上下文的对话
     * 
     * @param petId 宠物ID
     * @param symptomDesc 症状描述
     * @param conversationHistory 对话历史
     * @return 诊断结果
     */
    @Transactional
    public Diagnosis analyzeSymptomWithHistory(Long petId, String symptomDesc, String conversationHistory) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new RuntimeException("Pet not found");
        }

        // 构建包含历史对话的提示词
        String prompt = String.format(
                "作为专业的宠物兽医 AI，基于以下信息进行分析：\n" +
                "宠物信息：品种=%s, 年龄=%d\n" +
                "当前症状：%s\n" +
                "历史对话：%s\n" +
                "请提供初步诊断建议、可能原因和推荐行动。输出格式：建议：[建议文本]；置信度：[0-1 分数]。",
                pet.getBreed(), pet.getAge(), symptomDesc, conversationHistory
        );

        // 使用 LangChain4j 进行 AI 对话
        String aiResponse = chatLanguageModel.generate(prompt);

        // 解析响应
        String suggestion = aiResponse.contains("建议：") 
                ? aiResponse.split("建议：")[1].split("；")[0] 
                : aiResponse;
        Double confidence = 0.8;

        Diagnosis diagnosis = new Diagnosis(suggestion, confidence);

        // 使用 MyBatis Plus 保存症状
        Symptom symptom = new Symptom(symptomDesc, pet.getId());
        symptomMapper.insert(symptom);

        return diagnosis;
    }
}

