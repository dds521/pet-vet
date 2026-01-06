package com.petvetgateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
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
@RequiredArgsConstructor
public class SentinelConfig {
    
    /**
     * 网关配置（支持动态刷新）
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * 初始化Sentinel配置
     * 
     * 在Spring容器启动后自动执行，完成以下三个关键配置：
     * 1. initGatewayRules() - 设置每个服务的流量限制规则（QPS限制）
     * 2. initCustomizedApis() - 定义API分组，将URL路径映射到分组名称
     * 3. initBlockHandlers() - 自定义当流量超限时的返回信息格式
     * 
     * 执行顺序很重要：
     * - 先定义API分组（initCustomizedApis），因为流控规则可能会引用这些分组
     * - 再设置流控规则（initGatewayRules），基于服务名或API分组进行限流
     * - 最后设置降级处理器（initBlockHandlers），定义被限流时的响应格式
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
     * 【作用】为每个后端服务设置流量限制规则，防止某个服务被过多请求压垮
     * 
     * 【工作原理】
     * 1. 创建 GatewayFlowRule 对象，指定要限流的资源（服务名或API分组名）
     * 2. 设置限流参数：
     *    - count: 允许的最大QPS（每秒请求数）
     *    - intervalSec: 时间窗口（秒），通常为1秒
     * 3. 通过 GatewayRuleManager.loadRules() 加载规则到Sentinel
     * 
     * 【实际效果】
     * - 当请求到达网关时，Sentinel会检查该请求对应的服务是否超过QPS限制
     * - 如果超过限制，请求会被拦截，触发 initBlockHandlers() 中定义的降级处理
     * - 例如：AI服务设置为100 QPS，如果1秒内超过100个请求，第101个请求会被限流
     * 
     * 【示例说明】
     * - pet-vet-ai-service: 100 QPS（AI服务处理能力强，允许更多请求）
     * - pet-vet-embedding-service: 50 QPS（嵌入服务资源消耗大，限制更严格）
     * - pet-vet-rag-service: 50 QPS（RAG服务需要检索，限制更严格）
     * - pet-vet-mcp-service: 30 QPS（MCP服务可能是新服务，限制最严格）
     * 
     * 【注意事项】
     * - 这里的规则是代码中硬编码的默认规则
     * - 实际生产环境建议通过Nacos配置中心动态配置（见application.yml中的sentinel.datasource配置）
     * - 如果Nacos中有配置，会覆盖这里的默认规则
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    /**
     * 初始化网关流控规则
     * 
     * 从配置文件读取QPS和时间窗口配置，支持动态调整
     * 配置路径：gateway.sentinel.flowRule
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        
        // 获取流控规则配置
        GatewayConfig.SentinelConfig sentinelConfig = gatewayConfig.getSentinel();
        GatewayConfig.FlowRuleConfig flowRuleConfig = sentinelConfig != null ? sentinelConfig.getFlowRule() : null;
        
        // 默认时间窗口（秒）
        int defaultIntervalSec = flowRuleConfig != null && flowRuleConfig.getIntervalSec() != null 
            ? flowRuleConfig.getIntervalSec() 
            : 1;
        
        // AI服务流控规则
        GatewayConfig.ServiceFlowRule aiServiceRuleConfig = flowRuleConfig != null 
            ? flowRuleConfig.getAiService() 
            : null;
        int aiServiceQps = aiServiceRuleConfig != null && aiServiceRuleConfig.getCount() != null 
            ? aiServiceRuleConfig.getCount() 
            : 50; // 默认值
        int aiServiceIntervalSec = aiServiceRuleConfig != null && aiServiceRuleConfig.getIntervalSec() != null 
            ? aiServiceRuleConfig.getIntervalSec() 
            : defaultIntervalSec;
        GatewayFlowRule aiServiceRule = new GatewayFlowRule("pet-vet-ai-service")
                .setCount(aiServiceQps)
                .setIntervalSec(aiServiceIntervalSec);
        rules.add(aiServiceRule);
        log.info("AI服务流控规则：QPS={}, 时间窗口={}秒", aiServiceQps, aiServiceIntervalSec);
        
        // 嵌入服务流控规则
        GatewayConfig.ServiceFlowRule embeddingServiceRuleConfig = flowRuleConfig != null 
            ? flowRuleConfig.getEmbeddingService() 
            : null;
        int embeddingServiceQps = embeddingServiceRuleConfig != null && embeddingServiceRuleConfig.getCount() != null 
            ? embeddingServiceRuleConfig.getCount() 
            : 50; // 默认值
        int embeddingServiceIntervalSec = embeddingServiceRuleConfig != null && embeddingServiceRuleConfig.getIntervalSec() != null 
            ? embeddingServiceRuleConfig.getIntervalSec() 
            : defaultIntervalSec;
        GatewayFlowRule embeddingServiceRule = new GatewayFlowRule("pet-vet-embedding-service")
                .setCount(embeddingServiceQps)
                .setIntervalSec(embeddingServiceIntervalSec);
        rules.add(embeddingServiceRule);
        log.info("Embedding服务流控规则：QPS={}, 时间窗口={}秒", embeddingServiceQps, embeddingServiceIntervalSec);
        
        // RAG服务流控规则
        GatewayConfig.ServiceFlowRule ragServiceRuleConfig = flowRuleConfig != null 
            ? flowRuleConfig.getRagService() 
            : null;
        int ragServiceQps = ragServiceRuleConfig != null && ragServiceRuleConfig.getCount() != null 
            ? ragServiceRuleConfig.getCount() 
            : 50; // 默认值
        int ragServiceIntervalSec = ragServiceRuleConfig != null && ragServiceRuleConfig.getIntervalSec() != null 
            ? ragServiceRuleConfig.getIntervalSec() 
            : defaultIntervalSec;
        GatewayFlowRule ragServiceRule = new GatewayFlowRule("pet-vet-rag-service")
                .setCount(ragServiceQps)
                .setIntervalSec(ragServiceIntervalSec);
        rules.add(ragServiceRule);
        log.info("RAG服务流控规则：QPS={}, 时间窗口={}秒", ragServiceQps, ragServiceIntervalSec);
        
        // MCP服务流控规则
        GatewayConfig.ServiceFlowRule mcpServiceRuleConfig = flowRuleConfig != null 
            ? flowRuleConfig.getMcpService() 
            : null;
        int mcpServiceQps = mcpServiceRuleConfig != null && mcpServiceRuleConfig.getCount() != null 
            ? mcpServiceRuleConfig.getCount() 
            : 30; // 默认值
        int mcpServiceIntervalSec = mcpServiceRuleConfig != null && mcpServiceRuleConfig.getIntervalSec() != null 
            ? mcpServiceRuleConfig.getIntervalSec() 
            : defaultIntervalSec;
        GatewayFlowRule mcpServiceRule = new GatewayFlowRule("pet-vet-mcp-service")
                .setCount(mcpServiceQps)
                .setIntervalSec(mcpServiceIntervalSec);
        rules.add(mcpServiceRule);
        log.info("MCP服务流控规则：QPS={}, 时间窗口={}秒", mcpServiceQps, mcpServiceIntervalSec);
        
        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel网关流控规则初始化完成，规则数量: {}", rules.size());
        log.info("✅ 流控规则已从配置文件加载，支持通过Nacos动态调整");
    }
    
    /**
     * 初始化自定义API分组
     * 
     * 【作用】将URL路径模式映射到有意义的API分组名称，便于在流控规则中引用
     * 
     * 【工作原理】
     * 1. 创建 ApiDefinition 对象，定义分组名称（如 "ai-api"）
     * 2. 使用 ApiPathPredicateItem 设置URL匹配规则：
     *    - pattern: URL路径模式（支持通配符，如 "/api/ai/**"）
     *    - matchStrategy: 匹配策略（PREFIX表示前缀匹配）
     * 3. 通过 GatewayApiDefinitionManager.loadApiDefinitions() 加载分组定义
     * 
     * 【为什么需要API分组？】
     * - 流控规则可以基于服务名（如 "pet-vet-ai-service"）进行限流
     * - 也可以基于API分组名（如 "ai-api"）进行限流，更灵活
     * - 例如：可以对 "/api/ai/chat" 和 "/api/ai/image" 统一限流，而不是分别限流
     * 
     * 【实际效果】
     * - 当请求路径为 "/api/ai/**" 时，会被识别为 "ai-api" 分组
     * - 流控规则可以针对 "ai-api" 分组设置限流，而不是针对具体的服务名
     * - 这样即使服务名改变，只要URL路径不变，流控规则仍然有效
     * 
     * 【示例说明】
     * - "ai-api" 分组：匹配所有 "/api/ai/**" 路径的请求
     * - "embedding-api" 分组：匹配所有 "/api/embedding/**" 路径的请求
     * - "rag-api" 分组：匹配所有 "/api/rag/**" 路径的请求
     * - "mcp-api" 分组：匹配所有 "/api/mcp/**" 路径的请求
     * 
     * 【与流控规则的关系】
     * - 流控规则中的资源名可以是服务名（如 "pet-vet-ai-service"）
     * - 也可以是API分组名（如 "ai-api"）
     * - 如果使用API分组名，所有匹配该分组的请求都会受到统一的限流控制
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    /**
     * 初始化自定义API分组
     * 
     * 【作用】将URL路径模式映射到有意义的API分组名称，便于在流控规则中引用
     * 
     * 【与配置文件的区别】
     * 1. 代码中的 initCustomizedApis()：
     *    - 硬编码的默认API分组定义
     *    - 应用启动时执行，作为默认配置
     *    - 如果Nacos中没有配置，使用这里的默认值
     * 
     * 2. 配置文件中的 gw-api-group（Nacos数据源）：
     *    - 存储在Nacos配置中心的API分组规则
     *    - 支持动态更新，无需重启应用
     *    - 优先级高于代码中的默认配置
     *    - 配置路径：pet-vet-gateway-gw-api-group-rules (SENTINEL_GROUP)
     * 
     * 【工作原理】
     * 1. 创建 ApiDefinition 对象，定义分组名称（如 "ai-api"）
     * 2. 使用 ApiPathPredicateItem 设置URL匹配规则：
     *    - pattern: URL路径模式（支持通配符，如 "/api/ai/**"）
     *    - matchStrategy: 匹配策略（PREFIX表示前缀匹配）
     * 3. 通过 GatewayApiDefinitionManager.loadApiDefinitions() 加载分组定义
     * 
     * 【为什么需要API分组？】
     * - 流控规则可以基于服务名（如 "pet-vet-ai-service"）进行限流
     * - 也可以基于API分组名（如 "ai-api"）进行限流，更灵活
     * - 例如：可以对 "/api/ai/chat" 和 "/api/ai/image" 统一限流，而不是分别限流
     * 
     * 【实际效果】
     * - 当请求路径为 "/api/ai/**" 时，会被识别为 "ai-api" 分组
     * - 流控规则可以针对 "ai-api" 分组设置限流，而不是针对具体的服务名
     * - 这样即使服务名改变，只要URL路径不变，流控规则仍然有效
     * 
     * 【配置优先级】
     * Nacos配置 > 代码默认配置
     * 如果Nacos中有配置，会覆盖代码中的默认配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        
        // 获取API分组配置
        GatewayConfig.SentinelConfig sentinelConfig = gatewayConfig.getSentinel();
        GatewayConfig.ApiGroupConfig apiGroupConfig = sentinelConfig != null ? sentinelConfig.getApiGroup() : null;
        
        // AI服务API分组
        GatewayConfig.ApiGroupDefinition aiApiConfig = apiGroupConfig != null ? apiGroupConfig.getAiApi() : null;
        String aiApiPattern = aiApiConfig != null && aiApiConfig.getPattern() != null ? aiApiConfig.getPattern() : "/api/ai/**"; // 默认值
        ApiDefinition aiApi = new ApiDefinition("ai-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern(aiApiPattern)
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(aiApi);
        log.info("AI服务API分组：pattern={}", aiApiPattern);
        
        // 嵌入服务API分组
        GatewayConfig.ApiGroupDefinition embeddingApiConfig = apiGroupConfig != null ? apiGroupConfig.getEmbeddingApi() : null;
        String embeddingApiPattern = embeddingApiConfig != null && embeddingApiConfig.getPattern() != null ? embeddingApiConfig.getPattern() : "/api/embedding/**"; // 默认值
        ApiDefinition embeddingApi = new ApiDefinition("embedding-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern(embeddingApiPattern)
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(embeddingApi);
        log.info("Embedding服务API分组：pattern={}", embeddingApiPattern);
        
        // RAG服务API分组
        GatewayConfig.ApiGroupDefinition ragApiConfig = apiGroupConfig != null ? apiGroupConfig.getRagApi() : null;
        String ragApiPattern = ragApiConfig != null && ragApiConfig.getPattern() != null ? ragApiConfig.getPattern() : "/api/rag/**"; // 默认值
        ApiDefinition ragApi = new ApiDefinition("rag-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern(ragApiPattern)
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(ragApi);
        log.info("RAG服务API分组：pattern={}", ragApiPattern);
        
        // MCP服务API分组
        GatewayConfig.ApiGroupDefinition mcpApiConfig = apiGroupConfig != null ? apiGroupConfig.getMcpApi() : null;
        String mcpApiPattern = mcpApiConfig != null && mcpApiConfig.getPattern() != null ? mcpApiConfig.getPattern() : "/api/mcp/**"; // 默认值
        ApiDefinition mcpApi = new ApiDefinition("mcp-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern(mcpApiPattern)
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(mcpApi);
        log.info("MCP服务API分组：pattern={}", mcpApiPattern);
        
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("Sentinel自定义API分组初始化完成，分组数量: {}", definitions.size());
        log.info("✅ API分组已从配置文件加载，支持通过Nacos动态调整");
        log.info("⚠️ 注意：Nacos中的gw-api-group配置优先级更高，会覆盖这里的默认配置");
    }
    
    /**
     * 初始化限流降级处理器
     * 
     * 【作用】自定义当请求被限流时返回给客户端的响应内容
     * 
     * 【工作原理】
     * 1. 实现 BlockRequestHandler 接口，定义限流时的处理逻辑
     * 2. 通过 GatewayCallbackManager.setBlockHandler() 注册自定义处理器
     * 3. 当请求被限流时，Sentinel会自动调用这个处理器
     * 
     * 【触发时机】
     * - 当请求的QPS超过 initGatewayRules() 中设置的限流阈值时
     * - Sentinel会拦截该请求，不再转发到后端服务
     * - 而是调用这个处理器，返回自定义的响应信息
     * 
     * 【实际效果】
     * - 客户端收到HTTP 429（Too Many Requests）状态码
     * - 响应体为JSON格式，包含：
     *   {
     *     "success": false,
     *     "code": 429,
     *     "message": "请求过于频繁，请稍后再试",
     *     "data": null
     *   }
     * - 客户端可以根据这个响应提示用户稍后重试
     * 
     * 【为什么需要自定义？】
     * - Sentinel默认的限流响应可能不符合项目的统一响应格式
     * - 自定义响应可以保持API响应格式的一致性
     * - 可以提供更友好的错误提示信息
     * 
     * 【与流控规则的关系】
     * - initGatewayRules() 定义了"什么时候限流"（QPS阈值）
     * - initBlockHandlers() 定义了"限流后返回什么"（响应内容）
     * - 两者配合，形成完整的限流机制
     * 
     * 【示例场景】
     * 1. 用户频繁调用AI服务接口，1秒内超过100次请求
     * 2. 第101个请求到达网关时，Sentinel检测到超过QPS限制
     * 3. 请求被拦截，不转发到后端服务
     * 4. 调用本处理器，返回429状态码和友好的提示信息
     * 5. 客户端收到响应，提示用户"请求过于频繁，请稍后再试"
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
     * 注意：
     * - sentinelGatewayFilter 和 sentinelGatewayBlockExceptionHandler 已由
     *   Spring Cloud Alibaba Sentinel Gateway 自动配置提供，无需手动定义
     * - 如果手动定义会导致 bean 名称冲突，应用启动失败
     * - 本配置类主要负责初始化 Sentinel 规则和自定义处理器
     */
}

