package com.petvetai.app.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 配置类
 * 可以在这里配置默认的流控规则（也可以通过 Dashboard 动态配置）
 */
@Configuration
public class SentinelConfig {

    /**
     * 初始化默认流控规则
     * 注意：实际生产环境建议通过 Sentinel Dashboard 动态配置
     */
    @PostConstruct
    public void initRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // 示例：为 flowControl 资源设置默认流控规则
        // QPS 阈值为 10
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("flowControl");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(10);
        rules.add(flowRule);
        
        // 加载规则
        FlowRuleManager.loadRules(rules);
    }
}

