package com.petvetgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 网关配置类
 * 
 * 从配置文件读取网关相关配置
 * 支持 Nacos 配置动态刷新
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    
    /**
     * 白名单路径列表（不需要鉴权的路径）
     */
    private List<String> whitelist;
    
    /**
     * JWT配置
     */
    private JwtConfig jwt;
    
    /**
     * 授权配置
     */
    private AuthConfig auth;
    
    /**
     * 日志配置
     */
    private LogConfig log;
    
    /**
     * JWT配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class JwtConfig {
        /**
         * JWT密钥
         */
        private String secret;
        
        /**
         * JWT过期时间（毫秒）
         */
        private Long expiration;
        
        /**
         * JWT请求头名称
         */
        private String header;
        
        /**
         * JWT Token前缀
         */
        private String prefix;
    }
    
    /**
     * 授权配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class AuthConfig {
        /**
         * 企业微信配置
         */
        private WeWorkConfig wework;
        
        /**
         * 微信配置
         */
        private WeChatConfig wechat;
        
        /**
         * 支付宝配置
         */
        private AlipayConfig alipay;
        
        /**
         * QQ配置
         */
        private QqConfig qq;
    }
    
    /**
     * 企业微信配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class WeWorkConfig {
        /**
         * 是否启用
         */
        private Boolean enabled;
        
        /**
         * 企业ID
         */
        private String corpId;
        
        /**
         * 应用ID
         */
        private String agentId;
        
        /**
         * 应用密钥
         */
        private String secret;
        
        /**
         * 重定向URI
         */
        private String redirectUri;
        
        /**
         * API URL配置
         */
        private WeWorkUrlConfig urls;
    }
    
    /**
     * 企业微信URL配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class WeWorkUrlConfig {
        /**
         * 授权URL模板
         */
        private String authUrlTemplate;
        
        /**
         * 获取用户信息URL模板
         */
        private String userInfoUrlTemplate;
        
        /**
         * 获取Access Token URL模板
         */
        private String accessTokenUrlTemplate;
    }
    
    /**
     * 微信配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class WeChatConfig {
        /**
         * 是否启用
         */
        private Boolean enabled;
        
        /**
         * 应用ID
         */
        private String appId;
        
        /**
         * 应用密钥
         */
        private String appSecret;
        
        /**
         * 重定向URI
         */
        private String redirectUri;
        
        /**
         * API URL配置
         */
        private WeChatUrlConfig urls;
    }
    
    /**
     * 微信URL配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class WeChatUrlConfig {
        /**
         * 授权URL模板
         */
        private String authUrlTemplate;
        
        /**
         * 获取Access Token URL模板
         */
        private String accessTokenUrlTemplate;
        
        /**
         * 获取用户信息URL模板
         */
        private String userInfoUrlTemplate;
    }
    
    /**
     * 支付宝配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class AlipayConfig {
        /**
         * 是否启用
         */
        private Boolean enabled;
        
        /**
         * 应用ID
         */
        private String appId;
        
        /**
         * 私钥
         */
        private String privateKey;
        
        /**
         * 公钥
         */
        private String publicKey;
        
        /**
         * 重定向URI
         */
        private String redirectUri;
        
        /**
         * API URL配置
         */
        private AlipayUrlConfig urls;
    }
    
    /**
     * 支付宝URL配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class AlipayUrlConfig {
        /**
         * 授权URL模板
         */
        private String authUrlTemplate;
        
        /**
         * 获取用户信息URL模板
         */
        private String userInfoUrlTemplate;
    }
    
    /**
     * QQ配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class QqConfig {
        /**
         * 是否启用
         */
        private Boolean enabled;
        
        /**
         * 应用ID
         */
        private String appId;
        
        /**
         * 应用密钥
         */
        private String appKey;
        
        /**
         * 重定向URI
         */
        private String redirectUri;
        
        /**
         * API URL配置
         */
        private QqUrlConfig urls;
    }
    
    /**
     * QQ URL配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class QqUrlConfig {
        /**
         * 授权URL模板
         */
        private String authUrlTemplate;
        
        /**
         * 获取Access Token URL模板
         */
        private String accessTokenUrlTemplate;
        
        /**
         * 获取OpenId URL模板
         */
        private String openIdUrlTemplate;
        
        /**
         * 获取用户信息URL模板
         */
        private String userInfoUrlTemplate;
    }
    
    /**
     * 日志配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class LogConfig {
        /**
         * 是否启用日志
         */
        private Boolean enabled;
        
        /**
         * 是否记录请求体
         */
        private Boolean requestBody;
        
        /**
         * 是否记录响应体
         */
        private Boolean responseBody;
        
        /**
         * 最大请求/响应体大小（字节）
         */
        private Integer maxBodySize;
    }
    
    /**
     * Sentinel流控规则配置
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private SentinelConfig sentinel;
    
    /**
     * Sentinel流控规则配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class SentinelConfig {
        /**
         * 流控规则配置
         */
        private FlowRuleConfig flowRule;
        
        /**
         * API分组配置
         */
        private ApiGroupConfig apiGroup;
    }
    
    /**
     * API分组配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class ApiGroupConfig {
        /**
         * AI服务API分组
         */
        private ApiGroupDefinition aiApi;
        
        /**
         * Embedding服务API分组
         */
        private ApiGroupDefinition embeddingApi;
        
        /**
         * RAG服务API分组
         */
        private ApiGroupDefinition ragApi;
        
        /**
         * MCP服务API分组
         */
        private ApiGroupDefinition mcpApi;
    }
    
    /**
     * API分组定义内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class ApiGroupDefinition {
        /**
         * URL路径模式（支持通配符，如 "/api/ai/**"）
         */
        private String pattern;
    }
    
    /**
     * 流控规则配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class FlowRuleConfig {
        /**
         * 时间窗口（秒），默认1秒
         */
        private Integer intervalSec = 1;
        
        /**
         * AI服务QPS限制
         */
        private ServiceFlowRule aiService;
        
        /**
         * Embedding服务QPS限制
         */
        private ServiceFlowRule embeddingService;
        
        /**
         * RAG服务QPS限制
         */
        private ServiceFlowRule ragService;
        
        /**
         * MCP服务QPS限制
         */
        private ServiceFlowRule mcpService;
    }
    
    /**
     * 服务流控规则配置内部类
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    @Data
    public static class ServiceFlowRule {
        /**
         * QPS限制（每秒请求数）
         */
        private Integer count;
        
        /**
         * 时间窗口（秒），如果未设置则使用父级配置
         */
        private Integer intervalSec;
    }
}

