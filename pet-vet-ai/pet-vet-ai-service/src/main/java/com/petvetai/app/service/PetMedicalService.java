package com.petvetai.app.service;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.domain.Pet;
import com.petvetai.app.domain.Symptom;
import com.petvetai.app.mapper.PetMapper;
import com.petvetai.app.mapper.SymptomMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宠物医疗服务
 * 
 * 使用 LangChain4j 的 ChatLanguageModel
 * 支持多种 AI 提供商：DeepSeek、Grok、OpenAI
 */
@Service
public class PetMedicalService {

    private final ChatLanguageModel chatLanguageModel;
    private final PetMapper petMapper;
    private final SymptomMapper symptomMapper;

    public PetMedicalService(ChatLanguageModel chatLanguageModel, PetMapper petMapper, SymptomMapper symptomMapper) {
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

        String prompt = String.format(
                "作为专业的宠物兽医 AI，基于以下宠物信息分析症状：品种=%s, 年龄=%d。症状：%s。请提供初步诊断建议、可能原因和推荐行动。输出格式：建议：[建议文本]；置信度：[0-1 分数]。",
                pet.getBreed(), pet.getAge(), symptomDesc
        );

        // 使用 LangChain4j 的 ChatLanguageModel
        String aiResponse = chatLanguageModel.generate(prompt);

        // 简单解析响应
        String suggestion = aiResponse.contains("建议：") ? aiResponse.split("建议：")[1].split("；")[0] : aiResponse;
        Double confidence = 0.8; 

        Diagnosis diagnosis = new Diagnosis(suggestion, confidence);

        // 使用 MyBatis Plus 保存症状
        Symptom symptom = new Symptom(symptomDesc, pet.getId());
        symptomMapper.insert(symptom);

        return diagnosis;
    }
}
