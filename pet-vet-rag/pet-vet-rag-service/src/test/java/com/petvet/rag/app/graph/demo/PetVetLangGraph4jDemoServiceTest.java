package com.petvet.rag.app.graph.demo;

import com.petvet.rag.api.demo.DemoTriageReq;
import com.petvet.rag.api.demo.DemoTriageResp;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetVetLangGraph4jDemoServiceTest {

    @Mock
    private ChatModel chatModel;

    private PetVetLangGraph4jDemoService service;

    @BeforeEach
    void setUp() {
        service = new PetVetLangGraph4jDemoService(chatModel);
    }

    @Test
    void testRunDemo() {
        // Mock LLM responses
        when(chatModel.chat(anyString())).thenAnswer(invocation -> {
            String prompt = invocation.getArgument(0);
            if (prompt.contains("兽医病理学家")) {
                return "症状分析：可能存在胃肠炎。建议：需要就医。";
            } else if (prompt.contains("家庭护理专家")) {
                return "护理建议：禁食禁水4小时，观察精神状态。";
            } else if (prompt.contains("主治兽医")) {
                return "最终报告：\n【分诊建议】：紧急就诊\n【病情摘要】：... \n【护理指导】：...";
            }
            return "Unknown prompt";
        });

        DemoTriageReq req = new DemoTriageReq();
        req.setPetName("Mimi");
        req.setSymptomDesc("呕吐三次");

        DemoTriageResp resp = service.runDemo(req);

        assertNotNull(resp);
        assertEquals("紧急就诊", resp.getTriageLevel());
        assertTrue(resp.getSummary().contains("最终报告"));
        
        System.out.println("Test Passed! Output Summary:\n" + resp.getSummary());
    }
}
