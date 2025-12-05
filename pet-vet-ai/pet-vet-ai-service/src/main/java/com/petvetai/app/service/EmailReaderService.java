package com.petvetai.app.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 邮件读取服务
 * 
 * 功能模块：
 * 1. 连接邮件服务器（IMAP）
 * 2. 读取邮件（标题验证、附件处理）
 * 3. 移动邮件到删除箱
 */
@Slf4j
@Service
public class EmailReaderService {

    @Value("${email.imap.host:imap.example.com}")
    private String imapHost;

    @Value("${email.imap.port:993}")
    private int imapPort;

    @Value("${email.imap.username:}")
    private String username;

    @Value("${email.imap.password:}")
    private String password;

    @Value("${email.imap.ssl:true}")
    private boolean useSsl;

    @Value("${email.subject.pattern:^\\[系统通知\\]\\s+.*$}")
    private String subjectPattern;

    @Value("${email.attachment.save-path:./attachments}")
    private String attachmentSavePath;

    /**
     * 读取并处理邮件
     * 
     * @return 处理的邮件数量
     */
    public int processEmails() {
        Store store = null;
        Folder inbox = null;
        Folder trash = null;
        
        try {
            // 1. 连接邮件服务器
            store = connectToMailServer();
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            
            // 获取删除箱
            trash = store.getFolder("Trash");
            if (!trash.exists()) {
                trash = store.getFolder("Deleted Messages");
            }
            
            // 2. 读取邮件
            Message[] messages = inbox.getMessages();
            log.info("找到 {} 封邮件", messages.length);
            
            int processedCount = 0;
            for (Message message : messages) {
                try {
                    if (processEmail(message, trash)) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("处理邮件失败: {}", e.getMessage(), e);
                }
            }
            
            return processedCount;
            
        } catch (Exception e) {
            log.error("读取邮件失败", e);
            return 0;
        } finally {
            // 3. 关闭连接
            closeFolders(inbox, trash);
            closeStore(store);
        }
    }

    /**
     * 连接邮件服务器
     */
    private Store connectToMailServer() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", imapPort);
        
        if (useSsl) {
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", imapHost);
        }
        
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.timeout", "10000");
        props.put("mail.imap.connectiontimeout", "10000");
        
        Session session = Session.getInstance(props);
        Store store = session.getStore(useSsl ? "imaps" : "imap");
        store.connect(imapHost, imapPort, username, password);
        
        log.info("成功连接到邮件服务器: {}:{}", imapHost, imapPort);
        return store;
    }

    /**
     * 处理单封邮件
     * 
     * @param message 邮件消息
     * @param trash 删除箱文件夹
     * @return 是否处理成功
     */
    private boolean processEmail(Message message, Folder trash) throws Exception {
        if (!(message instanceof MimeMessage)) {
            return false;
        }
        
        MimeMessage mimeMessage = (MimeMessage) message;
        String subject = mimeMessage.getSubject();
        
        log.info("处理邮件: {}", subject);
        
        // 1. 验证邮件标题格式
        if (!validateSubject(subject)) {
            log.warn("邮件标题不符合格式要求: {}", subject);
            return false;
        }
        
        // 2. 处理附件
        List<File> attachmentFiles = processAttachments(mimeMessage);
        if (attachmentFiles.isEmpty()) {
            log.warn("邮件没有附件: {}", subject);
            return false;
        }
        
        // 3. 移动邮件到删除箱
        moveToTrash(mimeMessage, trash);
        
        log.info("邮件处理成功: {}", subject);
        return true;
    }

    /**
     * 验证邮件标题格式
     */
    private boolean validateSubject(String subject) {
        if (subject == null || subject.isEmpty()) {
            return false;
        }
        return Pattern.compile(subjectPattern).matcher(subject).matches();
    }

    /**
     * 处理邮件附件
     */
    private List<File> processAttachments(MimeMessage message) throws Exception {
        List<File> attachmentFiles = new ArrayList<>();
        
        if (!message.getContentType().contains("multipart")) {
            return attachmentFiles;
        }
        
        Multipart multipart = (Multipart) message.getContent();
        int partCount = multipart.getCount();
        
        // 确保附件保存目录存在
        File saveDir = new File(attachmentSavePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        
        for (int i = 0; i < partCount; i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                String fileName = part.getFileName();
                if (fileName != null && isCompressedFile(fileName)) {
                    File savedFile = saveAttachment(part, saveDir);
                    if (savedFile != null) {
                        attachmentFiles.add(savedFile);
                        log.info("附件已保存: {}", savedFile.getAbsolutePath());
                    }
                }
            }
        }
        
        return attachmentFiles;
    }

    /**
     * 判断是否为压缩文件
     */
    private boolean isCompressedFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".zip") || 
               lowerName.endsWith(".rar") || 
               lowerName.endsWith(".7z") || 
               lowerName.endsWith(".tar") || 
               lowerName.endsWith(".gz");
    }

    /**
     * 保存附件
     */
    private File saveAttachment(BodyPart part, File saveDir) throws Exception {
        String fileName = part.getFileName();
        File file = new File(saveDir, fileName);
        
        try (InputStream is = part.getInputStream();
             FileOutputStream fos = new FileOutputStream(file)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return file;
    }

    /**
     * 移动邮件到删除箱
     */
    private void moveToTrash(Message message, Folder trash) throws MessagingException {
        if (trash == null || !trash.exists()) {
            log.warn("删除箱不存在，无法移动邮件");
            return;
        }
        
        Folder sourceFolder = message.getFolder();
        sourceFolder.copyMessages(new Message[]{message}, trash);
        message.setFlag(Flags.Flag.DELETED, true);
        sourceFolder.expunge();
        
        log.info("邮件已移动到删除箱");
    }

    /**
     * 关闭文件夹
     */
    private void closeFolders(Folder... folders) {
        for (Folder folder : folders) {
            if (folder != null && folder.isOpen()) {
                try {
                    folder.close(false);
                } catch (MessagingException e) {
                    log.error("关闭文件夹失败", e);
                }
            }
        }
    }

    /**
     * 关闭存储连接
     */
    private void closeStore(Store store) {
        if (store != null && store.isConnected()) {
            try {
                store.close();
            } catch (MessagingException e) {
                log.error("关闭邮件服务器连接失败", e);
            }
        }
    }
}

