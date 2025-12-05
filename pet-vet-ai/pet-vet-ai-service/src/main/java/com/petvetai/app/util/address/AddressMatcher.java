package com.petvetai.app.util.address;

import com.petvetai.app.domain.address.AdministrativeDivision;
import com.petvetai.app.domain.address.MatchResult;
import com.petvetai.app.util.PinyinUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 地址匹配器
 * 实现地址匹配的核心算法
 * 
 * @author PetVetAI
 */
public class AddressMatcher {
    
    private final AddressIndex index;
    
    // 行政区划关键词
    private static final Set<String> PROVINCE_KEYWORDS = Set.of("省", "自治区", "特别行政区");
    private static final Set<String> CITY_KEYWORDS = Set.of("市", "州", "盟", "地区");
    private static final Set<String> DISTRICT_KEYWORDS = Set.of("区", "县", "旗", "自治县");
    private static final Set<String> STREET_KEYWORDS = Set.of("街道", "镇", "乡", "街道办");
    
    // 常见行政区划名称（用于异常检测）
    private static final Pattern PROVINCE_PATTERN = Pattern.compile(".*?(省|自治区|特别行政区)");
    private static final Pattern CITY_PATTERN = Pattern.compile(".*?(市|州|盟|地区)");
    
    public AddressMatcher(AddressIndex index) {
        this.index = index;
    }
    
    /**
     * 匹配地址
     */
    public MatchResult match(String inputAddress) {
        if (inputAddress == null || inputAddress.trim().isEmpty()) {
            return null;
        }
        
        String normalizedAddress = normalizeAddress(inputAddress);
        
        // 1. 检测异常地址（多行政区划）
        String abnormalReason = detectAbnormalAddress(normalizedAddress);
        boolean isAbnormal = abnormalReason != null;
        
        // 2. 提取地址关键词
        List<String> keywords = extractKeywords(normalizedAddress);
        
        // 3. 通过索引查找候选编码
        Set<String> candidateCodes = findCandidateCodes(keywords);
        
        if (candidateCodes.isEmpty()) {
            return new MatchResult(null, null, 0.0, isAbnormal, abnormalReason);
        }
        
        // 4. 计算匹配度并排序
        List<MatchResult> results = candidateCodes.stream()
                .map(code -> calculateMatchScore(code, normalizedAddress, keywords))
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
        
        MatchResult bestMatch = results.get(0);
        bestMatch.setAbnormal(isAbnormal);
        bestMatch.setAbnormalReason(abnormalReason);
        
        return bestMatch;
    }
    
    /**
     * 标准化地址（去除空格、标点等）
     */
    private String normalizeAddress(String address) {
        return address.replaceAll("[\\s,，。、]", "");
    }
    
    /**
     * 检测异常地址（多行政区划）
     */
    private String detectAbnormalAddress(String address) {
        // 检测多个省份
        long provinceCount = countMatches(address, PROVINCE_PATTERN);
        if (provinceCount > 1) {
            return "检测到多个省份";
        }
        
        // 检测多个城市（排除直辖市）
        long cityCount = countMatches(address, CITY_PATTERN);
        if (cityCount > 2) { // 允许直辖市（如：北京市海淀区）
            return "检测到多个城市";
        }
        
        // 检测重复的行政区划层级
        String[] commonProvinces = {"北京", "上海", "天津", "重庆", "浙江", "江苏", "广东", "山东", 
                                     "河南", "四川", "湖北", "湖南", "河北", "安徽", "福建", "江西"};
        int provinceOccurrences = 0;
        for (String province : commonProvinces) {
            if (address.contains(province + "省") || address.contains(province + "市")) {
                provinceOccurrences++;
            }
        }
        if (provinceOccurrences > 1) {
            return "检测到多个省份名称";
        }
        
        return null;
    }
    
    /**
     * 统计匹配次数
     */
    private long countMatches(String text, Pattern pattern) {
        return pattern.matcher(text).results().count();
    }
    
    /**
     * 提取地址关键词
     */
    private List<String> extractKeywords(String address) {
        List<String> keywords = new ArrayList<>();
        
        // 提取完整地址
        keywords.add(address);
        
        // 提取各个层级
        String[] levels = parseAddressLevels(address);
        for (String level : levels) {
            if (level != null && !level.isEmpty()) {
                keywords.add(level);
            }
        }
        
        // 提取部分匹配（处理缺少层级的情况）
        if (levels[0] == null && levels[1] != null) {
            // 缺少省份，只有市
            keywords.add(levels[1]);
        }
        if (levels[1] == null && levels[2] != null) {
            // 缺少市，只有区
            keywords.add(levels[2]);
        }
        
        return keywords;
    }
    
    /**
     * 解析地址层级
     */
    private String[] parseAddressLevels(String address) {
        String province = null;
        String city = null;
        String district = null;
        String street = null;
        
        // 解析省份
        for (String keyword : PROVINCE_KEYWORDS) {
            int index = address.indexOf(keyword);
            if (index > 0) {
                province = address.substring(0, index + keyword.length());
                address = address.substring(index + keyword.length());
                break;
            }
        }
        
        // 解析城市
        for (String keyword : CITY_KEYWORDS) {
            int index = address.indexOf(keyword);
            if (index > 0) {
                city = address.substring(0, index + keyword.length());
                address = address.substring(index + keyword.length());
                break;
            }
        }
        
        // 解析区县
        for (String keyword : DISTRICT_KEYWORDS) {
            int index = address.indexOf(keyword);
            if (index > 0) {
                district = address.substring(0, index + keyword.length());
                address = address.substring(index + keyword.length());
                break;
            }
        }
        
        // 解析街道
        for (String keyword : STREET_KEYWORDS) {
            int index = address.indexOf(keyword);
            if (index > 0) {
                street = address.substring(0, index + keyword.length());
                break;
            }
        }
        
        return new String[]{province, city, district, street};
    }
    
    /**
     * 查找候选编码
     */
    private Set<String> findCandidateCodes(List<String> keywords) {
        Set<String> candidateCodes = new HashSet<>();
        
        for (String keyword : keywords) {
            // 精确匹配
            candidateCodes.addAll(index.searchByKeyword(keyword));
            
            // 同音字匹配
            for (AdministrativeDivision division : index.getAllDivisions()) {
                String[] levels = division.getLevels();
                for (String level : levels) {
                    if (level != null && PinyinUtil.isHomophone(keyword, level)) {
                        candidateCodes.add(division.getCode());
                    }
                }
            }
        }
        
        return candidateCodes;
    }
    
    /**
     * 计算匹配度分数
     */
    private MatchResult calculateMatchScore(String code, String inputAddress, List<String> keywords) {
        AdministrativeDivision division = index.getDivision(code);
        if (division == null) {
            return new MatchResult(code, null, 0.0);
        }
        
        double score = 0.0;
        String fullAddress = division.getFullAddress();
        String[] levels = division.getLevels();
        String[] inputLevels = parseAddressLevels(inputAddress);
        
        // 1. 完整地址匹配（权重最高）
        if (inputAddress.equals(fullAddress)) {
            score += 50.0;
        } else if (fullAddress.contains(inputAddress) || inputAddress.contains(fullAddress)) {
            score += 30.0;
        }
        
        // 2. 层级匹配
        for (int i = 0; i < Math.min(levels.length, inputLevels.length); i++) {
            if (levels[i] != null && inputLevels[i] != null) {
                if (levels[i].equals(inputLevels[i])) {
                    score += 10.0;
                } else if (PinyinUtil.isHomophone(levels[i], inputLevels[i])) {
                    score += 8.0; // 同音字匹配分数稍低
                } else if (levels[i].contains(inputLevels[i]) || inputLevels[i].contains(levels[i])) {
                    score += 5.0;
                }
            }
        }
        
        // 3. 部分匹配（处理缺少层级的情况）
        if (inputLevels[0] == null && levels[1] != null) {
            // 缺少省份，但城市匹配
            if (inputAddress.contains(levels[1])) {
                score += 15.0;
            }
        }
        if (inputLevels[1] == null && levels[2] != null) {
            // 缺少城市，但区匹配
            if (inputAddress.contains(levels[2])) {
                score += 10.0;
            }
        }
        
        // 4. 关键词匹配
        for (String keyword : keywords) {
            if (fullAddress.contains(keyword)) {
                score += 2.0;
            }
        }
        
        // 5. 归一化分数（0-100）
        score = Math.min(100.0, score);
        
        return new MatchResult(code, division, score);
    }
}

