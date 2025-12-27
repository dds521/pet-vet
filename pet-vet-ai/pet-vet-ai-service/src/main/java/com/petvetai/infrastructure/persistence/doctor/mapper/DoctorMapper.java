package com.petvetai.infrastructure.persistence.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petvetai.infrastructure.persistence.doctor.po.VetAiDoctorPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医生Mapper接口
 * 
 * 提供医生数据的CRUD操作，使用注解方式实现简单查询
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Mapper
public interface DoctorMapper extends BaseMapper<VetAiDoctorPO> {
    
    /**
     * 根据手机号查询医生
     * 
     * @param phone 手机号
     * @return 医生信息
     * @author daidasheng
     * @date 2024-12-27
     */
    @Select("SELECT * FROM vet_ai_doctor WHERE phone = #{phone} AND is_void = 0 LIMIT 1")
    VetAiDoctorPO selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据状态查询医生列表
     * 
     * @param status 医生状态
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    default List<VetAiDoctorPO> selectByStatus(Integer status) {
        LambdaQueryWrapper<VetAiDoctorPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VetAiDoctorPO::getStatus, status)
               .eq(VetAiDoctorPO::getIsVoid, 0)
               .orderByDesc(VetAiDoctorPO::getCreateTime);
        return selectList(wrapper);
    }
    
    /**
     * 根据类型查询医生列表
     * 
     * @param type 医生类型
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    default List<VetAiDoctorPO> selectByType(Integer type) {
        LambdaQueryWrapper<VetAiDoctorPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VetAiDoctorPO::getType, type)
               .eq(VetAiDoctorPO::getIsVoid, 0)
               .orderByDesc(VetAiDoctorPO::getCreateTime);
        return selectList(wrapper);
    }
    
    /**
     * 根据地址编码查询医生列表
     * 
     * @param addressCode 地址编码
     * @param status 医生状态（可选）
     * @return 医生列表
     * @author daidasheng
     * @date 2024-12-27
     */
    default List<VetAiDoctorPO> selectByAddressCode(String addressCode, Integer status) {
        LambdaQueryWrapper<VetAiDoctorPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VetAiDoctorPO::getAddressCode, addressCode)
               .eq(VetAiDoctorPO::getIsVoid, 0);
        if (status != null) {
            wrapper.eq(VetAiDoctorPO::getStatus, status);
        }
        wrapper.orderByDesc(VetAiDoctorPO::getCreateTime);
        return selectList(wrapper);
    }
    
    /**
     * 根据地理位置查询附近的医生列表（使用Haversine公式计算距离）
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @param maxDistance 最大距离（公里）
     * @param limit 返回数量限制
     * @return 医生列表（按距离从近到远排序）
     * @author daidasheng
     * @date 2024-12-27
     */
    @Select("SELECT *, " +
            "(6371 * acos(cos(radians(#{latitude})) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(#{longitude})) + " +
            "sin(radians(#{latitude})) * sin(radians(latitude)))) AS distance " +
            "FROM vet_ai_doctor " +
            "WHERE is_void = 0 " +
            "AND status = 1 " +
            "AND longitude IS NOT NULL " +
            "AND latitude IS NOT NULL " +
            "HAVING distance <= #{maxDistance} " +
            "ORDER BY distance ASC " +
            "LIMIT #{limit}")
    List<VetAiDoctorPO> selectNearbyDoctors(@Param("longitude") Double longitude,
                                          @Param("latitude") Double latitude,
                                          @Param("maxDistance") Double maxDistance,
                                          @Param("limit") Integer limit);
}

