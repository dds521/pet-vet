package com.petvet.rag.app.classifier.config;

import com.petvet.rag.app.classifier.chain.ClassificationChain;
import com.petvet.rag.app.classifier.strategy.ClassificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 分类器配置类
 * 自动将策略注入到责任链中
 * 
 * @author daidasheng
 * @date 2024-12-15
 */
@Configuration
@EnableConfigurationProperties(ClassifierProperties.class)
@Slf4j
public class ClassifierConfig {
    
    /**
     * 初始化责任链，自动注入所有策略
     */
    @Bean
    public ClassificationChain classificationChain(@Autowired(required = false) List<ClassificationStrategy> strategies) {
        ClassificationChain chain = new ClassificationChain();
        
        if (strategies != null && !strategies.isEmpty()) {
            for (ClassificationStrategy strategy : strategies) {
                if (strategy.isEnabled()) {
                    chain.addStrategy(strategy);
                    log.info("注册分类策略: {}, 优先级: {}", strategy.getName(), strategy.getPriority());
                } else {
                    log.debug("跳过未启用的策略: {}", strategy.getName());
                }
            }
        }
        
        log.info("分类责任链初始化完成，共注册 {} 个策略", chain.getStrategies().size());
        return chain;
    }
}
