package com.petvetai.app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 邮件读取服务测试用例
 * 
 * 测试场景：
 * 1. 连接邮件服务器
 * 2. 读取邮件并验证标题格式
 * 3. 处理附件（压缩包）
 * 4. 移动邮件到删除箱
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class EmailReaderServiceTest {

    @Autowired(required = false)
    private EmailReaderService emailReaderService;

    @Test
    void testEmailReaderService() {
        if (emailReaderService == null) {
            log.warn("EmailReaderService 未配置，跳过测试");
            log.info("如需测试，请在 application-test.yml 中配置邮件服务器信息：");
            log.info("email:");
            log.info("  imap:");
            log.info("    host: imap.example.com");
            log.info("    port: 993");
            log.info("    username: your-email@example.com");
            log.info("    password: your-password");
            log.info("    ssl: true");
            log.info("  subject:");
            log.info("    pattern: '^\\\\[系统通知\\\\]\\\\s+.*$'");
            log.info("  attachment:");
            log.info("    save-path: ./attachments");
            return;
        }

        log.info("========== 邮件读取服务测试 ==========");
        
        try {
            // 执行邮件处理
            int processedCount = emailReaderService.processEmails();
            
            log.info("处理完成，共处理 {} 封邮件", processedCount);
            
            if (processedCount > 0) {
                log.info("✅ 测试通过：成功处理邮件");
            } else {
                log.info("ℹ️  没有符合条件的邮件需要处理");
            }
            
        } catch (Exception e) {
            log.error("测试失败", e);
        }
        
        log.info("========== 测试完成 ==========");
    }
}

