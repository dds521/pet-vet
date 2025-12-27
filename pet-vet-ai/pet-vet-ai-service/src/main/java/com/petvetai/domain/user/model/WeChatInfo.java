package com.petvetai.domain.user.model;

import java.util.Objects;

/**
 * 微信信息值对象
 * 
 * 封装微信相关的信息，不可变
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class WeChatInfo {
    
    private final String openId;
    private final String unionId;
    private final String sessionKey;  // 临时使用，不持久化
    
    /**
     * 构造函数
     * 
     * @param openId 微信openId，不能为空
     * @param unionId 微信unionId，可为空
     * @param sessionKey 微信sessionKey，可为空
     * @author daidasheng
     * @date 2024-12-20
     */
    public WeChatInfo(String openId, String unionId, String sessionKey) {
        if (openId == null || openId.trim().isEmpty()) {
            throw new IllegalArgumentException("openId不能为空");
        }
        this.openId = openId;
        this.unionId = unionId;
        this.sessionKey = sessionKey;
    }
    
    /**
     * 创建微信信息值对象（不包含sessionKey）
     * 
     * @param openId 微信openId
     * @param unionId 微信unionId
     * @return 微信信息值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static WeChatInfo of(String openId, String unionId) {
        return new WeChatInfo(openId, unionId, null);
    }
    
    /**
     * 创建微信信息值对象（包含sessionKey）
     * 
     * @param openId 微信openId
     * @param unionId 微信unionId
     * @param sessionKey 微信sessionKey
     * @return 微信信息值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public static WeChatInfo withSessionKey(String openId, String unionId, String sessionKey) {
        return new WeChatInfo(openId, unionId, sessionKey);
    }
    
    /**
     * 获取openId
     * 
     * @return openId
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getOpenId() {
        return openId;
    }
    
    /**
     * 获取unionId
     * 
     * @return unionId
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getUnionId() {
        return unionId;
    }
    
    /**
     * 获取sessionKey
     * 
     * @return sessionKey
     * @author daidasheng
     * @date 2024-12-20
     */
    public String getSessionKey() {
        return sessionKey;
    }
    
    /**
     * 更新unionId（创建新的值对象）
     * 
     * @param unionId 新的unionId
     * @return 新的微信信息值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public WeChatInfo withUnionId(String unionId) {
        return new WeChatInfo(this.openId, unionId, this.sessionKey);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeChatInfo that = (WeChatInfo) o;
        return Objects.equals(openId, that.openId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(openId);
    }
    
    @Override
    public String toString() {
        return "WeChatInfo{" +
                "openId='" + openId + '\'' +
                ", unionId='" + unionId + '\'' +
                '}';
    }
}

