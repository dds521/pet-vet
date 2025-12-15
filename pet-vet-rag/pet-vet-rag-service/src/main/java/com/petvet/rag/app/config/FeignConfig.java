package com.petvet.rag.app.config;

import feign.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端配置
 * 配置超时时间，解决向量检索超时问题
 * 
 * 注意：在 Spring Cloud 2023.0.1 中，可以通过配置文件或代码方式配置超时
 * 这里使用代码方式确保配置生效
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Configuration
public class FeignConfig {
    
    /**
     * 连接超时时间（毫秒）
     * 默认值：10秒
     */
    @Value("${spring.cloud.openfeign.client.config.default.connect-timeout:10000}")
    private int connectTimeout;
    
    /**
     * 读取超时时间（毫秒）
     * 默认值：120秒（向量检索可能需要较长时间）
     */
    @Value("${spring.cloud.openfeign.client.config.default.read-timeout:120000}")
    private int readTimeout;
    
    /**
     * 配置 Feign 请求选项
     * 设置连接超时和读取超时时间
     * 
     * 注意：这个方法会为所有 Feign 客户端设置默认的超时配置
     * 如果某个客户端需要不同的超时配置，可以在配置文件中单独配置
     * 
     * @return Request.Options 请求选项
     * @author daidasheng
     * @date 2024-12-15
     */
    @Bean
    public Request.Options requestOptions() {
        // 连接超时：10秒，读取超时：120秒
        return new Request.Options(
            connectTimeout,  // 连接超时时间（毫秒）
            readTimeout      // 读取超时时间（毫秒）
        );
    }
}
