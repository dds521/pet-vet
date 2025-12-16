package com.petvetai.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Seata 配置类
 * 
 * 确保 Seata 客户端在注册失败时不阻塞应用启动
 * 通过配置系统属性和监听器来优化 Seata 的初始化过程
 * 
 * @author daidasheng
 * @date 2024-12-16
 */
@Configuration
@ConditionalOnProperty(name = "seata.enabled", havingValue = "true", matchIfMissing = false)
public class SeataConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(SeataConfig.class);
	
	private static boolean systemPropertiesSet = false;

	/**
	 * 在应用环境准备阶段设置系统属性
	 * 
	 * 这个阶段在 Seata 初始化之前执行，确保超时配置生效
	 * 
	 * @param event 应用环境准备事件
	 * @author daidasheng
	 * @date 2024-12-16
	 */
	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		if (!systemPropertiesSet) {
			// 设置 Seata 传输层连接超时（毫秒），减少等待时间，避免阻塞启动
			// 如果 Seata Server 不可用，快速失败，不阻塞应用启动
			String connectTimeout = System.getProperty("seata.transport.connectTimeoutMillis");
			if (connectTimeout == null || connectTimeout.isEmpty()) {
				System.setProperty("seata.transport.connectTimeoutMillis", "3000");
				logger.info("已设置 Seata 连接超时: 3000ms");
			}
			
			// 设置 Seata 传输层请求超时（毫秒）
			String requestTimeout = System.getProperty("seata.transport.requestTimeoutMillis");
			if (requestTimeout == null || requestTimeout.isEmpty()) {
				System.setProperty("seata.transport.requestTimeoutMillis", "3000");
				logger.info("已设置 Seata 请求超时: 3000ms");
			}
			
			systemPropertiesSet = true;
		}
	}

	/**
	 * 初始化方法
	 * 
	 * 在 Seata 启用时，记录配置信息
	 * 如果 Seata Server 不可用，Seata 会自动降级为本地事务模式
	 * 
	 * @author daidasheng
	 * @date 2024-12-16
	 */
	@PostConstruct
	public void init() {
		logger.info("==========================================");
		logger.info("Seata 配置已加载");
		logger.info("注意：如果 Seata Server 不可用，Seata 会自动降级为本地事务模式");
		logger.info("应用可以正常启动，只是分布式事务功能不可用（会使用本地事务）");
		logger.info("==========================================");
	}
}
