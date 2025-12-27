package com.petvetai.infrastructure.persistence.doctor;

import com.petvetai.domain.doctor.model.Doctor;
import com.petvetai.domain.doctor.model.DoctorId;
import com.petvetai.domain.doctor.model.DoctorStatus;
import com.petvetai.domain.doctor.model.DoctorType;
import com.petvetai.domain.doctor.repository.DoctorRepository;
import com.petvetai.infrastructure.persistence.doctor.converter.DoctorConverter;
import com.petvetai.infrastructure.persistence.doctor.mapper.DoctorMapper;
import com.petvetai.infrastructure.persistence.doctor.po.DoctorPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生仓储实现
 * 
 * 实现医生仓储接口，使用MyBatis-Plus进行数据持久化
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Repository
@RequiredArgsConstructor
public class DoctorRepositoryImpl implements DoctorRepository {
    
    private final DoctorMapper doctorMapper;
    private final DoctorConverter doctorConverter;
    
    @Override
    public Doctor save(Doctor doctor) {
        DoctorPO po = doctorConverter.toPO(doctor);
        if (po.getId() == null) {
            // 新增
            doctorMapper.insert(po);
        } else {
            // 更新
            doctorMapper.updateById(po);
        }
        return doctorConverter.toDomain(po);
    }
    
    @Override
    public Doctor findById(DoctorId id) {
        if (id == null) {
            return null;
        }
        DoctorPO po = doctorMapper.selectById(id.getValue());
        return doctorConverter.toDomain(po);
    }
    
    @Override
    public Doctor findByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        DoctorPO po = doctorMapper.selectByPhone(phone);
        return doctorConverter.toDomain(po);
    }
    
    @Override
    public List<Doctor> findByStatus(DoctorStatus status) {
        if (status == null) {
            return List.of();
        }
        List<DoctorPO> pos = doctorMapper.selectByStatus(status.getCode());
        return pos.stream()
                .map(doctorConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> findByType(DoctorType type) {
        if (type == null) {
            return List.of();
        }
        List<DoctorPO> pos = doctorMapper.selectByType(type.getCode());
        return pos.stream()
                .map(doctorConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> findNearbyDoctors(Double longitude, Double latitude, Double maxDistance, Integer limit) {
        if (longitude == null || latitude == null || maxDistance == null) {
            return List.of();
        }
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条
        }
        List<DoctorPO> pos = doctorMapper.selectNearbyDoctors(longitude, latitude, maxDistance, limit);
        return pos.stream()
                .map(doctorConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Doctor> findByAddressCode(String addressCode, DoctorStatus status) {
        if (addressCode == null || addressCode.isEmpty()) {
            return List.of();
        }
        Integer statusCode = status != null ? status.getCode() : null;
        List<DoctorPO> pos = doctorMapper.selectByAddressCode(addressCode, statusCode);
        return pos.stream()
                .map(doctorConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(DoctorId id) {
        if (id == null) {
            return;
        }
        doctorMapper.deleteById(id.getValue());
    }
}

