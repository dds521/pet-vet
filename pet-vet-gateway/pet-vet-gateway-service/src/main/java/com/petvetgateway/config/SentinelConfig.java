package com.petvetgateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Sentinel流量控制配置
 * 
 * 配置Sentinel网关流量控制规则，包括API分组、流控规则等
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Configuration
public class SentinelConfig {
    
    /**
     * 初始化Sentinel配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostConstruct
    public void init() {
        initGatewayRules();
        initCustomizedApis();
        initBlockHandlers();
    }
    
    /**
     * 初始化网关流控规则
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        
        // AI服务流控规则
        GatewayFlowRule aiServiceRule = new GatewayFlowRule("pet-vet-ai-service")
                .setCount(100) // QPS限制
                .setIntervalSec(1); // 时间窗口（秒）
        rules.add(aiServiceRule);
        
        // 嵌入服务流控规则
        GatewayFlowRule embeddingServiceRule = new GatewayFlowRule("pet-vet-embedding-service")
                .setCount(50)
                .setIntervalSec(1);
        rules.add(embeddingServiceRule);
        
        // RAG服务流控规则
        GatewayFlowRule ragServiceRule = new GatewayFlowRule("pet-vet-rag-service")
                .setCount(50)
                .setIntervalSec(1);
        rules.add(ragServiceRule);
        
        // MCP服务流控规则
        GatewayFlowRule mcpServiceRule = new GatewayFlowRule("pet-vet-mcp-service")
                .setCount(30)
                .setIntervalSec(1);
        rules.add(mcpServiceRule);
        
        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel网关流控规则初始化完成，规则数量: {}", rules.size());
    }
    
    /**
     * 初始化自定义API分组
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        
        // AI服务API分组
        ApiDefinition aiApi = new ApiDefinition("ai-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/ai/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(aiApi);
        
        // 嵌入服务API分组
        ApiDefinition embeddingApi = new ApiDefinition("embedding-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/embedding/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(embeddingApi);
        
        // RAG服务API分组
        ApiDefinition ragApi = new ApiDefinition("rag-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/rag/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(ragApi);
        
        // MCP服务API分组
        ApiDefinition mcpApi = new ApiDefinition("mcp-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/mcp/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(mcpApi);
        
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("Sentinel自定义API分组初始化完成，分组数量: {}", definitions.size());
    }
    
    /**
     * 初始化限流降级处理器
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private void initBlockHandlers() {
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(org.springframework.web.server.ServerWebExchange exchange, Throwable ex) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("code", 429);
                result.put("message", "请求过于频繁，请稍后再试");
                result.put("data", null);
                
                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(result));
            }
        });
        
        log.info("Sentinel限流降级处理器初始化完成");
    }
    
    /**
     * 配置Sentinel网关过滤器
     * 
     * @return Sentinel网关过滤器
     * @author daidasheng
     * @date 2024-12-27
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
    
    /**
     * 配置Sentinel网关异常处理器
     * 
     * @param viewResolvers 视图解析器列表
     * @param serverCodecConfigurer 服务器编解码器配置器
     * @return Sentinel网关异常处理器
     * @author daidasheng
     * @date 2024-12-27
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler(
            List<ViewResolver> viewResolvers,
            ServerCodecConfigurer serverCodecConfigurer) {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }
}

