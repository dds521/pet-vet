package com.petvetai.infrastructure.util.address;

import com.petvetai.infrastructure.util.PinyinUtil;

import java.util.*;

/**
 * 地址索引结构
 * 
 * 实现倒排索引和前缀树，用于快速匹配地址
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class AddressIndex {
    
    /**
     * 倒排索引：关键词 -> 行政区划编码列表
     */
    private final Map<String, Set<String>> invertedIndex = new HashMap<>();
    
    /**
     * 前缀树：用于前缀匹配
     */
    private final TrieNode trieRoot = new TrieNode();
    
    /**
     * 所有行政区划数据：编码 -> 行政区划对象
     */
    private final Map<String, AdministrativeDivision> divisionMap = new HashMap<>();
    
    /**
     * 拼音索引：拼音 -> 原始关键词列表
     */
    private final Map<String, Set<String>> pinyinIndex = new HashMap<>();
    
    /**
     * 构建索引
     * 
     * @param divisions 行政区划列表
     * @author daidasheng
     * @date 2024-12-20
     */
    public void buildIndex(List<AdministrativeDivision> divisions) {
        divisionMap.clear();
        invertedIndex.clear();
        pinyinIndex.clear();
        trieRoot.clear();
        
        for (AdministrativeDivision division : divisions) {
            divisionMap.put(division.getCode(), division);
            
            // 为每个层级建立索引
            String[] levels = division.getLevels();
            for (String level : levels) {
                if (level != null && !level.isEmpty()) {
                    // 倒排索引
                    invertedIndex.computeIfAbsent(level, k -> new HashSet<>()).add(division.getCode());
                    
                    // 前缀树索引
                    addToTrie(level, division.getCode());
                    
                    // 拼音索引
                    String pinyin = PinyinUtil.getPinyin(level);
                    if (!pinyin.equals(level)) {
                        pinyinIndex.computeIfAbsent(pinyin, k -> new HashSet<>()).add(level);
                    }
                }
            }
            
            // 为完整地址建立索引
            String fullAddress = division.getFullAddress();
            invertedIndex.computeIfAbsent(fullAddress, k -> new HashSet<>()).add(division.getCode());
            addToTrie(fullAddress, division.getCode());
        }
    }
    
    /**
     * 添加到前缀树
     * 
     * @param word 关键词
     * @param code 行政区划编码
     * @author daidasheng
     * @date 2024-12-20
     */
    private void addToTrie(String word, String code) {
        TrieNode node = trieRoot;
        for (char c : word.toCharArray()) {
            node = node.getOrCreateChild(c);
            node.addCode(code);
        }
    }
    
    /**
     * 通过关键词查找匹配的编码
     * 
     * @param keyword 关键词
     * @return 匹配的编码集合
     * @author daidasheng
     * @date 2024-12-20
     */
    public Set<String> searchByKeyword(String keyword) {
        Set<String> codes = new HashSet<>();
        
        // 精确匹配
        if (invertedIndex.containsKey(keyword)) {
            codes.addAll(invertedIndex.get(keyword));
        }
        
        // 前缀匹配
        codes.addAll(searchByPrefix(keyword));
        
        // 拼音匹配
        codes.addAll(searchByPinyin(keyword));
        
        return codes;
    }
    
    /**
     * 前缀匹配
     * 
     * @param prefix 前缀
     * @return 匹配的编码集合
     * @author daidasheng
     * @date 2024-12-20
     */
    public Set<String> searchByPrefix(String prefix) {
        Set<String> codes = new HashSet<>();
        TrieNode node = trieRoot;
        
        // 找到前缀对应的节点
        for (char c : prefix.toCharArray()) {
            node = node.getChild(c);
            if (node == null) {
                return codes; // 前缀不存在
            }
        }
        
        // 收集所有子节点的编码
        collectCodes(node, codes);
        return codes;
    }
    
    /**
     * 递归收集所有编码
     * 
     * @param node 前缀树节点
     * @param codes 编码集合
     * @author daidasheng
     * @date 2024-12-20
     */
    private void collectCodes(TrieNode node, Set<String> codes) {
        codes.addAll(node.getCodes());
        for (TrieNode child : node.getChildren().values()) {
            collectCodes(child, codes);
        }
    }
    
    /**
     * 拼音匹配
     * 
     * @param keyword 关键词
     * @return 匹配的编码集合
     * @author daidasheng
     * @date 2024-12-20
     */
    public Set<String> searchByPinyin(String keyword) {
        Set<String> codes = new HashSet<>();
        String keywordPinyin = PinyinUtil.getPinyin(keyword);
        
        // 查找拼音相同的原始关键词
        Set<String> matchedKeywords = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : pinyinIndex.entrySet()) {
            if (entry.getKey().contains(keywordPinyin) || keywordPinyin.contains(entry.getKey())) {
                matchedKeywords.addAll(entry.getValue());
            }
        }
        
        // 通过匹配的关键词查找编码
        for (String matchedKeyword : matchedKeywords) {
            if (invertedIndex.containsKey(matchedKeyword)) {
                codes.addAll(invertedIndex.get(matchedKeyword));
            }
        }
        
        return codes;
    }
    
    /**
     * 获取行政区划对象
     * 
     * @param code 行政区划编码
     * @return 行政区划对象
     * @author daidasheng
     * @date 2024-12-20
     */
    public AdministrativeDivision getDivision(String code) {
        return divisionMap.get(code);
    }
    
    /**
     * 获取所有行政区划
     * 
     * @return 所有行政区划集合
     * @author daidasheng
     * @date 2024-12-20
     */
    public Collection<AdministrativeDivision> getAllDivisions() {
        return divisionMap.values();
    }
    
    /**
     * 前缀树节点
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    private static class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>();
        private final Set<String> codes = new HashSet<>();
        
        public TrieNode getOrCreateChild(char c) {
            return children.computeIfAbsent(c, k -> new TrieNode());
        }
        
        public TrieNode getChild(char c) {
            return children.get(c);
        }
        
        public void addCode(String code) {
            codes.add(code);
        }
        
        public Set<String> getCodes() {
            return codes;
        }
        
        public Map<Character, TrieNode> getChildren() {
            return children;
        }
        
        public void clear() {
            children.clear();
            codes.clear();
        }
    }
}

