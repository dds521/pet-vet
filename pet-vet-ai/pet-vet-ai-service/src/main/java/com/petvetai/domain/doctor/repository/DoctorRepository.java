package com.petvetai.domain.doctor.repository;

import com.petvetai.domain.doctor.model.Doctor;
import com.petvetai.domain.doctor.model.DoctorId;
import com.petvetai.domain.doctor.model.DoctorStatus;
import com.petvetai.domain.doctor.model.DoctorType;

import java.util.List;

/**
 * 医生仓储接口
 * 
 * 定义医生聚合的持久化和查询能力
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public interface DoctorRepository {
    
    /**
     * 保存医生
     * 
     * @param doctor 医生聚合根
     * @return 保存后的医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    Doctor save(Doctor doctor);
    
    /**
     * 根据ID查找医生
     * 
     * @param id 医生ID
     * @return 医生聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    Doctor findById(DoctorId id);
    
    /**
     * 根据手机号查找医生
     * 
     * @param phone 手机号
     * @return 医生聚合根，如果不存在则返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    Doctor findByPhone(String phone);
    
    /**
     * 根据状态查找医生列表
     * 
     * @param status 医生状态
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    List<Doctor> findByStatus(DoctorStatus status);
    
    /**
     * 根据类型查找医生列表
     * 
     * @param type 医生类型
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    List<Doctor> findByType(DoctorType type);
    
    /**
     * 根据地理位置查找附近的医生列表（按距离排序）
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @param maxDistance 最大距离（公里）
     * @param limit 返回数量限制
     * @return 医生列表（按距离从近到远排序）
     * @author daidasheng
     * @date 2024-12-27
     */
    List<Doctor> findNearbyDoctors(Double longitude, Double latitude, Double maxDistance, Integer limit);
    
    /**
     * 根据地址编码查找医生列表
     * 
     * @param addressCode 地址编码（行政区划编码）
     * @param status 医生状态（可选，如果为null则查询所有状态）
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    List<Doctor> findByAddressCode(String addressCode, DoctorStatus status);
    
    /**
     * 删除医生
     * 
     * @param id 医生ID
     * @author daidasheng
     * @date 2024-12-27
     */
    void delete(DoctorId id);
}

