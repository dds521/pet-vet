package com.petvetai.app.service;

import com.petvetai.app.domain.address.AdministrativeDivision;
import com.petvetai.app.domain.address.MatchResult;
import com.petvetai.app.util.address.AddressIndex;
import com.petvetai.app.util.address.AddressMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 地址匹配服务
 * 
 * @author PetVetAI
 */
@Slf4j
@Service
public class AddressMatchingService {
    
    private final AddressIndex index;
    private final AddressMatcher matcher;
    
    public AddressMatchingService() {
        this.index = new AddressIndex();
        this.matcher = new AddressMatcher(index);
        loadDataFromCsv();
    }
    
    /**
     * 从CSV文件加载数据
     */
    private void loadDataFromCsv() {
        List<AdministrativeDivision> divisions = new ArrayList<>();
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("data/administrative_divisions.csv");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // 跳过空行和注释
                }
                
                AdministrativeDivision division = parseCsvLine(line);
                if (division != null) {
                    divisions.add(division);
                }
            }
            
            log.info("成功加载 {} 条行政区划数据", divisions.size());
            index.buildIndex(divisions);
            
        } catch (Exception e) {
            log.error("加载CSV数据失败", e);
            throw new RuntimeException("加载行政区划数据失败", e);
        }
    }
    
    /**
     * 解析CSV行
     * 格式：编码,省,市,区,街道
     */
    private AdministrativeDivision parseCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            log.warn("CSV行格式不正确: {}", line);
            return null;
        }
        
        return new AdministrativeDivision(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim()
        );
    }
    
    /**
     * 匹配地址
     * 
     * @param address 用户输入的地址
     * @return 匹配结果
     */
    public MatchResult matchAddress(String address) {
        return matcher.match(address);
    }
}

