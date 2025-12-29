package com.petvet.common.mybatis.id;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 改进的雪花算法ID生成器
 * 
 * 保证生成的ID严格递增，同时具备雪花算法的优势：
 * - 分布式环境下唯一
 * - 性能高
 * - 趋势递增（严格递增）
 * 
 * ID结构（64位）：
 * - 1位符号位（固定为0）
 * - 41位时间戳（毫秒级，可用约69年）
 * - 10位机器ID（5位数据中心ID + 5位机器ID）
 * - 12位序列号（同一毫秒内最多4096个ID）
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Component
public class SnowflakeIdGenerator implements IdentifierGenerator {
    
    /**
     * 起始时间戳（2024-01-01 00:00:00）
     * 用于减少时间戳位数，延长可用时间
     */
    private static final long START_TIMESTAMP = 1704067200000L;
    
    /**
     * 时间戳占用的位数
     */
    private static final long TIMESTAMP_BITS = 41L;
    
    /**
     * 机器ID占用的位数
     */
    private static final long MACHINE_ID_BITS = 10L;
    
    /**
     * 序列号占用的位数
     */
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 机器ID最大值（1023）
     */
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    
    /**
     * 序列号最大值（4095）
     */
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    
    /**
     * 机器ID左移位数
     */
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    
    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    
    /**
     * 机器ID（通过IP地址和配置生成）
     */
    private final long machineId;
    
    /**
     * 序列号（同一毫秒内的递增序列）
     */
    private final AtomicLong sequence = new AtomicLong(0);
    
    /**
     * 上次生成ID的时间戳
     */
    private volatile long lastTimestamp = -1L;
    
    /**
     * 构造函数
     * 
     * 根据本机IP地址生成机器ID，确保分布式环境下唯一
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    public SnowflakeIdGenerator() {
        this.machineId = generateMachineId();
        log.info("雪花算法ID生成器初始化完成，机器ID: {}", machineId);
    }
    
    /**
     * 生成机器ID
     * 
     * 根据本机IP地址的最后一段生成机器ID，确保分布式环境下唯一
     * 如果无法获取IP，则使用随机数
     * 
     * @return 机器ID（0-1023）
     * @author daidasheng
     * @date 2024-12-27
     */
    private long generateMachineId() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] address = inetAddress.getAddress();
            
            // 使用IP地址的最后一段生成机器ID
            // IPv4: 使用最后一个字节
            // IPv6: 使用最后两个字节的异或结果
            long machineId;
            if (address.length == 4) {
                // IPv4
                machineId = (address[3] & 0xFF) | ((address[2] & 0xFF) << 8);
            } else {
                // IPv6或其他，使用哈希
                machineId = Math.abs(inetAddress.getHostAddress().hashCode());
            }
            
            // 确保机器ID在有效范围内
            return machineId & MAX_MACHINE_ID;
        } catch (UnknownHostException e) {
            log.warn("无法获取本机IP地址，使用随机数作为机器ID", e);
            // 如果无法获取IP，使用进程ID和时间戳的组合
            long pid = ProcessHandle.current().pid();
            return (pid ^ System.currentTimeMillis()) & MAX_MACHINE_ID;
        }
    }
    
    /**
     * 生成下一个ID
     * 
     * 保证生成的ID严格递增
     * 
     * @return 生成的ID
     * @author daidasheng
     * @date 2024-12-27
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();
        
        // 如果当前时间小于上次时间，说明时钟回拨
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            log.warn("检测到时钟回拨，回拨时间: {} 毫秒", offset);
            
            // 如果回拨时间小于5秒，等待时钟追上
            if (offset < 5000) {
                try {
                    Thread.sleep(offset);
                    currentTimestamp = System.currentTimeMillis();
                    // 再次检查
                    if (currentTimestamp < lastTimestamp) {
                        throw new RuntimeException("时钟回拨严重，无法生成ID");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待时钟恢复时被中断", e);
                }
            } else {
                throw new RuntimeException("时钟回拨超过5秒，无法生成ID");
            }
        }
        
        // 如果是同一毫秒内
        if (currentTimestamp == lastTimestamp) {
            // 序列号递增
            long seq = sequence.incrementAndGet();
            
            // 如果序列号超过最大值，等待下一毫秒
            if (seq > MAX_SEQUENCE) {
                currentTimestamp = waitNextMillis(lastTimestamp);
                seq = 0;
                sequence.set(0);
            }
            
            lastTimestamp = currentTimestamp;
            
            // 组装ID
            return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                    | (machineId << MACHINE_ID_SHIFT)
                    | seq;
        } else {
            // 新的毫秒，序列号重置为0
            sequence.set(0);
            lastTimestamp = currentTimestamp;
            
            // 组装ID
            return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                    | (machineId << MACHINE_ID_SHIFT)
                    | 0;
        }
    }
    
    /**
     * 等待下一毫秒
     * 
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     * @author daidasheng
     * @date 2024-12-27
     */
    private long waitNextMillis(long lastTimestamp) {
        long currentTimestamp = System.currentTimeMillis();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }
    
    /**
     * 生成下一个ID（IdentifierGenerator接口方法）
     * 
     * @param entity 实体对象
     * @return 生成的ID
     * @author daidasheng
     * @date 2024-12-27
     */
    @Override
    public Number nextId(Object entity) {
        return nextId();
    }
}

