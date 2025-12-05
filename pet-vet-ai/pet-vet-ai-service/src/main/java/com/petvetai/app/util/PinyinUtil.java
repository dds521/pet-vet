package com.petvetai.app.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.util.HashSet;
import java.util.Set;

/**
 * 拼音工具类
 * 用于处理同音字匹配
 * 
 * @author PetVetAI
 */
public class PinyinUtil {
    
    private static final HanyuPinyinOutputFormat format;
    
    static {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
    }
    
    /**
     * 获取汉字的所有拼音（多音字）
     */
    public static Set<String> getPinyinSet(char c) {
        Set<String> pinyinSet = new HashSet<>();
        try {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyins != null) {
                for (String pinyin : pinyins) {
                    pinyinSet.add(pinyin);
                }
            }
        } catch (Exception e) {
            // 非汉字字符，返回空集合
        }
        return pinyinSet;
    }
    
    /**
     * 获取字符串的所有拼音组合（处理多音字）
     * 例如："与杭" -> ["yuhang", "yuxing"]
     */
    public static Set<String> getAllPinyinCombinations(String text) {
        Set<String> result = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return result;
        }
        
        char[] chars = text.toCharArray();
        @SuppressWarnings("unchecked")
        Set<String>[] pinyinSets = new Set[chars.length];
        
        // 为每个字符获取所有可能的拼音
        for (int i = 0; i < chars.length; i++) {
            pinyinSets[i] = getPinyinSet(chars[i]);
            if (pinyinSets[i].isEmpty()) {
                // 非汉字字符，直接使用原字符
                pinyinSets[i] = new HashSet<>();
                pinyinSets[i].add(String.valueOf(chars[i]));
            }
        }
        
        // 生成所有组合
        generateCombinations(pinyinSets, 0, "", result);
        
        return result;
    }
    
    /**
     * 递归生成所有拼音组合
     */
    private static void generateCombinations(Set<String>[] pinyinSets, int index, 
                                            String current, Set<String> result) {
        if (index >= pinyinSets.length) {
            result.add(current);
            return;
        }
        
        for (String pinyin : pinyinSets[index]) {
            generateCombinations(pinyinSets, index + 1, current + pinyin, result);
        }
    }
    
    /**
     * 获取字符串的首选拼音（取第一个多音字读音）
     */
    public static String getPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            Set<String> pinyins = getPinyinSet(c);
            if (!pinyins.isEmpty()) {
                sb.append(pinyins.iterator().next());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * 判断两个字符串是否同音（拼音相同）
     */
    public static boolean isHomophone(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        
        Set<String> pinyin1 = getAllPinyinCombinations(str1);
        Set<String> pinyin2 = getAllPinyinCombinations(str2);
        
        // 如果两个集合有交集，则认为同音
        pinyin1.retainAll(pinyin2);
        return !pinyin1.isEmpty();
    }
}

