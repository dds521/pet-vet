package com.petvetai.infrastructure.external.wechat;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信API服务实现
 * 
 * 实现与微信API的交互
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
@Slf4j
@Service
public class WeChatApiServiceImpl implements WeChatApiService {
    
    @Value("${wechat.miniapp.appid:}")
    private String appId;
    
    @Value("${wechat.miniapp.secret:}")
    private String appSecret;
    
    @Value("${wechat.api.login-url:https://api.weixin.qq.com/sns/jscode2session}")
    private String wechatLoginUrl;
    
    @Override
    public WeChatSessionResult getWeChatSession(String code) {
        try {
            // 构建请求URL
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wechatLoginUrl, appId, appSecret, code);
            
            log.info("请求微信API: {}", url.replace(appSecret, "***"));
            
            // 发送HTTP请求
            String response = HttpUtil.get(url);
            log.info("微信API响应: {}", response);
            
            // 解析响应
            JSONObject json = JSONUtil.parseObj(response);
            
            // 检查是否有错误
            if (json.containsKey("errcode")) {
                Integer errcode = json.getInt("errcode");
                String errmsg = json.getStr("errmsg");
                log.error("微信API返回错误，errcode: {}, errmsg: {}", errcode, errmsg);
                throw new RuntimeException("微信登录失败：" + errmsg);
            }
            
            // 提取openId和sessionKey
            String openId = json.getStr("openid");
            String sessionKey = json.getStr("session_key");
            String unionId = json.getStr("unionid"); // 可选字段
            
            return new WeChatSessionResult(openId, sessionKey, unionId);
        } catch (Exception e) {
            log.error("获取微信session异常", e);
            throw new RuntimeException("获取微信用户信息失败：" + e.getMessage(), e);
        }
    }
}

