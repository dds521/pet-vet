package com.petvetai.app.service;

import com.petvetai.app.domain.address.MatchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 地址匹配服务单元测试
 * 
 * @author PetVetAI
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("地址匹配服务测试")
class AddressMatchingServiceTest {
    
    @Autowired
    private AddressMatchingService addressMatchingService;
    
    @Test
    @DisplayName("测试完整地址匹配")
    void testFullAddressMatch() {
        // 测试用例：输入完整地址，应该返回33011
        MatchResult result = addressMatchingService.matchAddress("浙江省杭州市余杭区仓前街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertNotNull(result.getCode(), "应该返回行政区划编码");
        assertEquals("33011", result.getCode(), "应该匹配到33011");
        assertTrue(result.getScore() > 80, "完整匹配的分数应该很高");
        assertFalse(result.isAbnormal(), "正常地址不应标记为异常");
    }
    
    @Test
    @DisplayName("测试同音错别字匹配")
    void testHomophoneMatch() {
        // 测试用例：输入"与杭"（错别字），应该匹配到"余杭"
        MatchResult result = addressMatchingService.matchAddress("浙江省杭州市与杭区仓前街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertNotNull(result.getCode(), "应该返回行政区划编码");
        assertEquals("33011", result.getCode(), "应该匹配到33011（通过同音字匹配）");
        assertTrue(result.getScore() > 50, "同音字匹配应该有合理的分数");
    }
    
    @Test
    @DisplayName("测试缺少行政区划的地址匹配")
    void testPartialAddressMatch() {
        // 测试用例：缺少省份和城市，只有区和街道
        MatchResult result = addressMatchingService.matchAddress("余杭区仓前街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertNotNull(result.getCode(), "应该返回行政区划编码");
        assertEquals("33011", result.getCode(), "应该匹配到33011");
        assertTrue(result.getScore() > 30, "部分匹配应该有合理的分数");
    }
    
    @Test
    @DisplayName("测试多行政区划异常地址检测")
    void testAbnormalAddressDetection() {
        // 测试用例：包含多个省份的异常地址
        MatchResult result = addressMatchingService.matchAddress("北京北京市海淀区浙江省杭州市余杭区五常街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertTrue(result.isAbnormal(), "应该检测到异常地址");
        assertNotNull(result.getAbnormalReason(), "应该有异常原因说明");
        assertTrue(result.getAbnormalReason().contains("多个"), "异常原因应该提到多个行政区划");
        
        // 即使异常，也应该返回最佳匹配
        assertNotNull(result.getCode(), "即使异常也应该返回最佳匹配的编码");
    }
    
    @Test
    @DisplayName("测试不同街道的匹配")
    void testDifferentStreetMatch() {
        // 测试用例：匹配五常街道
        MatchResult result = addressMatchingService.matchAddress("浙江省杭州市余杭区五常街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertEquals("33012", result.getCode(), "应该匹配到33012（五常街道）");
    }
    
    @Test
    @DisplayName("测试不同区的匹配")
    void testDifferentDistrictMatch() {
        // 测试用例：匹配西湖区
        MatchResult result = addressMatchingService.matchAddress("浙江省杭州市西湖区蒋村街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertEquals("33013", result.getCode(), "应该匹配到33013（西湖区）");
    }
    
    @Test
    @DisplayName("测试空地址处理")
    void testEmptyAddress() {
        MatchResult result = addressMatchingService.matchAddress("");
        
        assertNull(result, "空地址应该返回null");
    }
    
    @Test
    @DisplayName("测试不存在的地址")
    void testNonExistentAddress() {
        MatchResult result = addressMatchingService.matchAddress("不存在的地址测试");
        
        // 可能返回null或低分匹配
        if (result != null) {
            assertTrue(result.getScore() < 30, "不存在地址的分数应该很低");
        }
    }
    
    @Test
    @DisplayName("测试北京市地址匹配")
    void testBeijingAddress() {
        MatchResult result = addressMatchingService.matchAddress("北京市海淀区中关村街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertEquals("11001", result.getCode(), "应该匹配到11001");
    }
    
    @Test
    @DisplayName("测试上海市地址匹配")
    void testShanghaiAddress() {
        MatchResult result = addressMatchingService.matchAddress("上海市黄浦区外滩街道");
        
        assertNotNull(result, "匹配结果不应为空");
        assertEquals("31001", result.getCode(), "应该匹配到31001");
    }
}

