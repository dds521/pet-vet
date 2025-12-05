package com.petvetai.app.util.address;

import com.petvetai.app.domain.address.AdministrativeDivision;
import com.petvetai.app.domain.address.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 地址匹配器独立测试（不依赖Spring上下文）
 * 
 * @author PetVetAI
 */
@DisplayName("地址匹配器独立测试")
class AddressMatcherStandaloneTest {
    
    private AddressIndex index;
    private AddressMatcher matcher;
    
    @BeforeEach
    void setUp() {
        index = new AddressIndex();
        matcher = new AddressMatcher(index);
        
        // 准备测试数据
        List<AdministrativeDivision> divisions = new ArrayList<>();
        divisions.add(new AdministrativeDivision("33011", "浙江省", "杭州市", "余杭区", "仓前街道"));
        divisions.add(new AdministrativeDivision("33012", "浙江省", "杭州市", "余杭区", "五常街道"));
        divisions.add(new AdministrativeDivision("33013", "浙江省", "杭州市", "西湖区", "蒋村街道"));
        divisions.add(new AdministrativeDivision("11001", "北京市", "北京市", "海淀区", "中关村街道"));
        divisions.add(new AdministrativeDivision("31001", "上海市", "上海市", "黄浦区", "外滩街道"));
        
        index.buildIndex(divisions);
    }
    
    @Test
    @DisplayName("测试完整地址匹配")
    void testFullAddressMatch() {
        MatchResult result = matcher.match("浙江省杭州市余杭区仓前街道");
        
        assertNotNull(result);
        assertEquals("33011", result.getCode());
        assertTrue(result.getScore() > 80);
        assertFalse(result.isAbnormal());
    }
    
    @Test
    @DisplayName("测试同音错别字匹配")
    void testHomophoneMatch() {
        // 测试"与杭"匹配"余杭"
        MatchResult result = matcher.match("浙江省杭州市与杭区仓前街道");
        
        assertNotNull(result);
        assertEquals("33011", result.getCode());
        assertTrue(result.getScore() > 50);
    }
    
    @Test
    @DisplayName("测试缺少行政区划的地址匹配")
    void testPartialAddressMatch() {
        MatchResult result = matcher.match("余杭区仓前街道");
        
        assertNotNull(result);
        assertEquals("33011", result.getCode());
        assertTrue(result.getScore() > 30);
    }
    
    @Test
    @DisplayName("测试多行政区划异常地址检测")
    void testAbnormalAddressDetection() {
        MatchResult result = matcher.match("北京北京市海淀区浙江省杭州市余杭区五常街道");
        
        assertNotNull(result);
        assertTrue(result.isAbnormal());
        assertNotNull(result.getAbnormalReason());
        assertTrue(result.getAbnormalReason().contains("多个"));
    }
    
    @Test
    @DisplayName("测试不同街道的匹配")
    void testDifferentStreetMatch() {
        MatchResult result = matcher.match("浙江省杭州市余杭区五常街道");
        
        assertNotNull(result);
        assertEquals("33012", result.getCode());
    }
    
    @Test
    @DisplayName("测试不同区的匹配")
    void testDifferentDistrictMatch() {
        MatchResult result = matcher.match("浙江省杭州市西湖区蒋村街道");
        
        assertNotNull(result);
        assertEquals("33013", result.getCode());
    }
    
    @Test
    @DisplayName("测试空地址处理")
    void testEmptyAddress() {
        MatchResult result = matcher.match("");
        assertNull(result);
        
        result = matcher.match(null);
        assertNull(result);
    }
    
    @Test
    @DisplayName("测试北京市地址匹配")
    void testBeijingAddress() {
        MatchResult result = matcher.match("北京市海淀区中关村街道");
        
        assertNotNull(result);
        assertEquals("11001", result.getCode());
    }
    
    @Test
    @DisplayName("测试上海市地址匹配")
    void testShanghaiAddress() {
        MatchResult result = matcher.match("上海市黄浦区外滩街道");
        
        assertNotNull(result);
        assertEquals("31001", result.getCode());
    }
}

