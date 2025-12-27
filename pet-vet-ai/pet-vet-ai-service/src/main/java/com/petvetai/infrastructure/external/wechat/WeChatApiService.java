package com.petvetai.infrastructure.external.wechat;

import com.petvetai.domain.user.model.WeChatInfo;

/**
 * 微信API服务接口
 * 
 * 封装与微信API的交互
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public interface WeChatApiService {
    
    /**
     * 通过code获取微信用户信息
     * 
     * @param code 微信登录凭证code
     * @return 微信信息值对象
     * @author daidasheng
     * @date 2024-12-20
     */
    WeChatSessionResult getWeChatSession(String code);
    
    /**
     * 微信Session结果
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    class WeChatSessionResult {
        private final String openId;
        private final String sessionKey;
        private final String unionId;
        
        public WeChatSessionResult(String openId, String sessionKey, String unionId) {
            this.openId = openId;
            this.sessionKey = sessionKey;
            this.unionId = unionId;
        }
        
        public String getOpenId() {
            return openId;
        }
        
        public String getSessionKey() {
            return sessionKey;
        }
        
        public String getUnionId() {
            return unionId;
        }
        
        /**
         * 转换为WeChatInfo值对象
         * 
         * @return 微信信息值对象
         * @author daidasheng
         * @date 2024-12-20
         */
        public WeChatInfo toWeChatInfo() {
            return WeChatInfo.withSessionKey(openId, unionId, sessionKey);
        }
    }
}

