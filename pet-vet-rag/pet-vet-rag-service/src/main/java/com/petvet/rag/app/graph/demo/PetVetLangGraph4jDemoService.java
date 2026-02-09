package com.petvet.rag.app.graph.demo;

import com.petvet.rag.api.demo.DemoTriageReq;
import com.petvet.rag.api.demo.DemoTriageResp;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.AgentStateFactory;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 使用 LangGraph4j 的简单宠物分诊 Demo 服务
 * 
 * 业务场景（尽量完整但保持简单）：
 * - 输入：宠物症状描述（可选宠物昵称）
 * - 流程：记录症状 → 简单规则分诊 → 生成总结文案
 * - 输出：分诊级别 + 总结文案
 * 
 * 注意：仅作为 LangGraph4j 集成验证，不影响现有 RAG 流程。
 *
 * @author daidasheng
 * @date 2026-02-09
 */
@Slf4j
public class PetVetLangGraph4jDemoService {

    /**
     * 简单状态：仅维护消息列表
     * 
     * 说明：本 Demo 不引入复杂状态，只通过一个 {@code messages} 渠道累积所有提示语和分诊结果，
     * 最终在汇总时直接将 messages 拼接成 summary 返回给前端。
     */
    static class SimpleState extends AgentState {

        static final String MESSAGES_KEY = "messages";

        /**
         * 状态 Schema：messages 采用追加模式
         */
        static final Map<String, org.bsc.langgraph4j.state.Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
        );

        /**
         * 构造函数
         *
         * @param initData 初始状态数据（由 LangGraph4j 根据 Schema 构建）
         * @author daidasheng
         * @date 2026-02-09
         */
        SimpleState(Map<String, Object> initData) {
            super(initData);
        }

        /**
         * 获取当前状态中的消息列表
         * 
         * 如果状态中尚未包含 messages，则返回空列表，方便后续追加。
         *
         * @return 消息列表（只读快照），永不为 null
         * @author daidasheng
         * @date 2026-02-09
         */
        List<String> messages() {
            return (List<String>) this.<List<String>>value(MESSAGES_KEY).orElse(List.of());
        }
    }

    /**
     * 节点1：收集症状 & 打招呼
     * 
     * 职责说明：
     * - 组合欢迎语与症状描述，写入状态的 messages 渠道
     * - 不做任何业务判断，仅负责“输入 → 内部上下文”的转换
     */
    static class CollectSymptomNode implements NodeAction<SimpleState> {

        private final DemoTriageReq req;

        /**
         * 构造函数
         *
         * @param req Demo 请求对象，包含宠物昵称与症状描述
         * @author daidasheng
         * @date 2026-02-09
         */
        CollectSymptomNode(DemoTriageReq req) {
            this.req = req;
        }

        /**
         * 执行节点逻辑
         * 
         * - 从请求中读取宠物昵称与症状描述
         * - 构造两条文案写入 messages：欢迎语 + 症状回显
         * - 返回的 Map 会被 LangGraph4j 根据 Channel 配置追加到状态中
         *
         * @param state 当前图状态（此处不会修改原 state，只基于其构造增量）
         * @return 增量状态 Map，仅包含对 messages 的追加数据
         * @author daidasheng
         * @date 2026-02-09
         */
        @Override
        public Map<String, Object> apply(SimpleState state) {
            String petName = req.getPetName();
            String symptom = req.getSymptomDesc();
            String prefix = petName != null && !petName.isBlank() ? "宠物「" + petName + "」" : "宠物";
            String msg1 = "欢迎使用宠物医疗 AI 简易分诊 Demo。";
            String msg2 = prefix + " 的症状描述为：" + symptom;
            log.debug("CollectSymptomNode, msg1={}, msg2={}", msg1, msg2);
            return Map.of(SimpleState.MESSAGES_KEY, List.of(msg1, msg2));
        }
    }

    /**
     * 节点2：基于简单规则做分诊，并生成建议
     * 
     * 职责说明：
     * - 读取前序节点写入的 messages，并进行字符串拼接
     * - 基于非常简单的关键词规则做“伪分诊”：
     *   - 紧急就诊 / 建议尽快就诊 / 可继续观察
     * - 生成一条包含“分诊级别 + 建议”信息的追加消息
     */
    static class TriageNode implements NodeAction<SimpleState> {

        /**
         * 执行节点逻辑，进行规则分诊
         *
         * @param state 当前图状态，包含前序节点累积的 messages
         * @return 增量状态 Map，仅包含对 messages 的追加数据（分诊结果文案）
         * @author daidasheng
         * @date 2026-02-09
         */
        @Override
        public Map<String, Object> apply(SimpleState state) {
            List<String> messages = state.messages();
            String all = String.join(" ", messages);
            String triageLevel;
            String advice;

            String lower = all.toLowerCase();
            if (containsAny(lower, "呕吐", "吐", "拉稀", "腹泻", "出血", "抽搐", "不吃不喝")) {
                triageLevel = "紧急就诊";
                advice = "根据描述，建议尽快带宠物前往附近宠物医院急诊就诊，途中注意保暖和安抚，不要自行大量用药。";
            } else if (containsAny(lower, "食欲差", "不太吃", "精神不好", "萎靡", "咳嗽", "打喷嚏")) {
                triageLevel = "建议尽快就诊";
                advice = "症状提示可能存在潜在疾病，建议 24 小时内联系宠物医院进行线下检查，并记录近期饮食、排便和疫苗驱虫情况。";
            } else {
                triageLevel = "可继续观察";
                advice = "目前症状描述相对较轻，可先观察 24 小时，如出现食欲明显下降、持续呕吐/腹泻、精神变差等情况，请立即就诊。";
            }

            String msg = "分诊级别：" + triageLevel + "。建议：" + advice;
            log.debug("TriageNode, triageLevel={}, advice={}", triageLevel, advice);
            return Map.of(SimpleState.MESSAGES_KEY, msg);
        }

        /**
         * 判断文本中是否包含任一关键词（忽略大小写）
         *
         * @param text 文本（建议预先转换为小写）
         * @param keywords 关键词列表
         * @return 是否命中任一关键词
         * @author daidasheng
         * @date 2026-02-09
         */
        private boolean containsAny(String text, String... keywords) {
            for (String k : keywords) {
                if (text.contains(k.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 运行 LangGraph4j 简单分诊 Demo
     *
     * @param req 请求
     * @return DemoTriageResp 响应
     * @author daidasheng
     * @date 2026-02-09
     */
    public DemoTriageResp runDemo(DemoTriageReq req) {
        try {
            // 1. 定义图结构
            AgentStateFactory<SimpleState> factory = SimpleState::new;
            StateGraph<SimpleState> stateGraph = new StateGraph<>(
                SimpleState.SCHEMA,
                factory
            )
                // 节点：收集症状
                .addNode("collect_symptom", node_async(new CollectSymptomNode(req)))
                // 节点：简单分诊
                .addNode("triage", node_async(new TriageNode()))
                // 边：START -> collect_symptom -> triage -> END
                .addEdge(START, "collect_symptom")
                .addEdge("collect_symptom", "triage")
                .addEdge("triage", END);

            // 2. 编译图
            var compiledGraph = stateGraph.compile();

            // 3. 执行图，获取最终状态
            var lastOpt = compiledGraph.invoke(Map.of());
            if (lastOpt.isEmpty()) {
                return DemoTriageResp.builder().triageLevel("未知").summary("分诊失败：图执行未返回任何状态").build();
            }
            SimpleState last = (SimpleState) lastOpt.get();

            List<String> messages = last.messages();
            String summary = String.join("\n", messages);
            String triageLevel = parseTriageLevel(summary);

            return DemoTriageResp.builder().triageLevel(triageLevel).summary(summary).build();
        } catch (GraphStateException e) {
            log.error("LangGraph4j Demo 图构建或执行失败", e);
            return DemoTriageResp.builder().triageLevel("未知").summary("分诊失败：图构建或执行异常：" + e.getMessage()).build();
        }
    }

    /**
     * 从总结文案中解析分诊级别
     * 
     * 当前 Demo 中，分诊结果的格式固定为：
     * {@code 分诊级别：{level}。建议：{advice...}}
     * 因此这里只做简单的字符串截取，作为 Demo 使用。
     *
     * @param summary 总结文案（由多条 messages 拼接而成）
     * @return 解析出的分诊级别，解析失败时返回 "未知"
     * @author daidasheng
     * @date 2026-02-09
     */
    private String parseTriageLevel(String summary) {
        int idx = summary.indexOf("分诊级别：");
        if (idx >= 0) {
            String sub = summary.substring(idx + "分诊级别：".length());
            int end = sub.indexOf("。");
            return end > 0 ? sub.substring(0, end) : sub;
        }
        return "未知";
    }
}

