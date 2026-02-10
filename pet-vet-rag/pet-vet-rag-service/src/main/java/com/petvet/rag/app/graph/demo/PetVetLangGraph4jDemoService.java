package com.petvet.rag.app.graph.demo;

import com.petvet.rag.api.demo.DemoTriageReq;
import com.petvet.rag.api.demo.DemoTriageResp;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 使用 LangGraph4j 的智能多专家会诊 Demo 服务
 *
 * 升级说明：
 * 从简单的规则匹配升级为基于 LLM 的多专家协作模式。
 * 展示了 LangGraph 的并行执行与状态聚合能力。
 *
 * 业务场景：
 * 1. 症状分析专家 (Symptom Expert): 分析病情严重程度。
 * 2. 护理建议专家 (Care Expert): 提供家庭护理建议。
 * 3. 主治医师 (Attending Vet): 汇总各方意见，给出最终诊断。
 *
 * @author daidasheng
 * @date 2026-02-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PetVetLangGraph4jDemoService {

    private final ChatModel chatLanguageModel;

    /**
     * 会诊状态
     */
    static class ConsultationState extends AgentState {

        static final String INPUT_KEY = "input";
        static final String SYMPTOM_ANALYSIS_KEY = "symptomAnalysis";
        static final String CARE_ADVICE_KEY = "careAdvice";
        static final String FINAL_DIAGNOSIS_KEY = "finalDiagnosis";

        static final Map<String, Channel<?>> SCHEMA = Map.of(
            INPUT_KEY, Channels.base((a, b) -> b), // 存储原始输入
            SYMPTOM_ANALYSIS_KEY, Channels.base((a, b) -> b), // 存储症状分析结果
            CARE_ADVICE_KEY, Channels.base((a, b) -> b), // 存储护理建议
            FINAL_DIAGNOSIS_KEY, Channels.base((a, b) -> b) // 存储最终诊断
        );

        ConsultationState(Map<String, Object> initData) {
            super(initData);
        }

        String input() {
            return (String) this.value(INPUT_KEY).orElse("");
        }
        
        String symptomAnalysis() {
            return (String) this.value(SYMPTOM_ANALYSIS_KEY).orElse("（未提供分析）");
        }

        String careAdvice() {
            return (String) this.value(CARE_ADVICE_KEY).orElse("（未提供建议）");
        }
    }

    /**
     * 节点：症状分析专家
     */
    class SymptomAnalysisNode implements NodeAction<ConsultationState> {
        @Override
        public Map<String, Object> apply(ConsultationState state) {
            String input = state.input();
            log.info("🔍 [症状分析专家] 开始分析: {}", input);
            
            String prompt = String.format(
                "你是一位经验丰富的兽医病理学家。请分析以下宠物症状。\n" +
                "\n" +
                "输入信息: %s\n" +
                "\n" +
                "请提供：\n" +
                "1. 潜在的病因分析\n" +
                "2. 症状的危急程度评估 (低/中/高)\n" +
                "3. 是否需要立即就医\n" +
                "\n" +
                "请保持专业、客观。",
                input
            );
                
            String response = chatLanguageModel.chat(prompt);
            log.info("✅ [症状分析专家] 分析完成");
            return Map.of(ConsultationState.SYMPTOM_ANALYSIS_KEY, response);
        }
    }

    /**
     * 节点：护理建议专家
     */
    class CareAdviceNode implements NodeAction<ConsultationState> {
        @Override
        public Map<String, Object> apply(ConsultationState state) {
            String input = state.input();
            log.info("❤️ [护理建议专家] 开始分析: {}", input);
            
            String prompt = String.format(
                "你是一位宠物家庭护理专家。请针对以下情况提供家庭护理建议。\n" +
                "\n" +
                "输入信息: %s\n" +
                "\n" +
                "请注意：\n" +
                "1. 不要开具处方药\n" +
                "2. 提供缓解不适的方法\n" +
                "3. 列出观察重点（如果发生什么情况需要立即去医院）\n" +
                "\n" +
                "语气要温和、安抚。",
                input
            );
                
            String response = chatLanguageModel.chat(prompt);
            log.info("✅ [护理建议专家] 分析完成");
            return Map.of(ConsultationState.CARE_ADVICE_KEY, response);
        }
    }

    /**
     * 节点：主治医师（汇总）
     */
    class AttendingPhysicianNode implements NodeAction<ConsultationState> {
        @Override
        public Map<String, Object> apply(ConsultationState state) {
            String input = state.input();
            String analysis = state.symptomAnalysis();
            String advice = state.careAdvice();
            
            log.info("👨‍⚕️ [主治医师] 开始汇总会诊意见...");
            
            String prompt = String.format(
                "你是一位资深主治兽医。请根据你的专家团队的意见，为用户生成一份最终的会诊报告。\n" +
                "\n" +
                "用户描述: %s\n" +
                "\n" +
                "专家 A (病理分析):\n" +
                "%s\n" +
                "\n" +
                "专家 B (护理建议):\n" +
                "%s\n" +
                "\n" +
                "请生成一份结构清晰的报告，包含：\n" +
                "1. 【分诊建议】：明确指出是需要紧急就医、预约就诊还是居家观察。\n" +
                "2. 【病情摘要】：综合病理分析。\n" +
                "3. 【护理指导】：综合护理建议。\n" +
                "\n" +
                "请直接对宠物主人说话，语气专业且富有同理心。",
                input, analysis, advice
            );
                
            String response = chatLanguageModel.chat(prompt);
            log.info("✅ [主治医师] 报告生成完毕");
            return Map.of(ConsultationState.FINAL_DIAGNOSIS_KEY, response);
        }
    }

    public DemoTriageResp runDemo(DemoTriageReq req) {
        String input = "宠物名: " + req.getPetName() + ", 症状: " + req.getSymptomDesc();
        
        try {
            // 1. 构建图
            AgentStateFactory<ConsultationState> factory = ConsultationState::new;
            StateGraph<ConsultationState> graph = new StateGraph<>(ConsultationState.SCHEMA, factory);
            
            // 2. 添加节点
            graph.addNode("symptom_expert", node_async(new SymptomAnalysisNode()));
            graph.addNode("care_expert", node_async(new CareAdviceNode()));
            graph.addNode("attending_vet", node_async(new AttendingPhysicianNode()));
            
            // 3. 定义边 (并行执行：Start -> Expert A & Expert B)
            graph.addEdge(START, "symptom_expert");
            graph.addEdge(START, "care_expert");
            
            // 4. 汇聚 (Expert A & Expert B -> Attending)
            graph.addEdge("symptom_expert", "attending_vet");
            graph.addEdge("care_expert", "attending_vet");
            
            // 5. 结束
            graph.addEdge("attending_vet", END);
            
            // 6. 编译并运行
            var app = graph.compile();
            var result = app.invoke(Map.of(ConsultationState.INPUT_KEY, input));
            
            // 7. 提取结果
            // result 是 Optional<State> 类型
            Optional<Object> diagnosisOpt = result.flatMap(state -> state.value(ConsultationState.FINAL_DIAGNOSIS_KEY));
            String diagnosis = diagnosisOpt.map(Object::toString).orElse("诊断生成失败");
            
            // 简单解析分诊级别（Demo用途）
            String level = "建议就诊";
            if (diagnosis.contains("紧急")) level = "紧急就诊";
            else if (diagnosis.contains("观察")) level = "居家观察";
            
            return DemoTriageResp.builder()
                .triageLevel(level)
                .summary(diagnosis)
                .build();
                
        } catch (Exception e) {
            log.error("LangGraph 执行异常", e);
            throw new RuntimeException(e);
        }
    }
}