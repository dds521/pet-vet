package com.petvetai.domain.doctor.service;

import com.petvetai.domain.doctor.model.Doctor;
import com.petvetai.domain.doctor.model.DoctorId;
import com.petvetai.domain.doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 医生领域服务
 * 
 * 处理医生相关的业务逻辑，如注册验证、资质验证等
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorDomainService {
    
    private final DoctorRepository doctorRepository;
    
    /**
     * 注册医生（个人类型）
     * 
     * @param doctor 医生聚合根
     * @return 注册后的医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor registerIndividual(Doctor doctor) {
        // 验证手机号是否已注册
        Doctor existingDoctor = doctorRepository.findByPhone(doctor.getDoctorInfo().getPhone());
        if (existingDoctor != null) {
            throw new IllegalArgumentException("该手机号已被注册");
        }
        
        // 保存医生
        return doctorRepository.save(doctor);
    }
    
    /**
     * 注册医生（机构类型）
     * 
     * @param doctor 医生聚合根
     * @return 注册后的医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor registerInstitution(Doctor doctor) {
        // 验证手机号是否已注册
        Doctor existingDoctor = doctorRepository.findByPhone(doctor.getDoctorInfo().getPhone());
        if (existingDoctor != null) {
            throw new IllegalArgumentException("该手机号已被注册");
        }
        
        // 保存医生
        return doctorRepository.save(doctor);
    }
    
    /**
     * 审核医生资质
     * 
     * @param doctorId 医生ID
     * @param approved 是否通过
     * @param rejectReason 失败原因（如果未通过）
     * @return 审核后的医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor approveDoctor(DoctorId doctorId, boolean approved, String rejectReason) {
        Doctor doctor = doctorRepository.findById(doctorId);
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        
        if (approved) {
            doctor.approve();
        } else {
            doctor.reject(rejectReason);
        }
        
        return doctorRepository.save(doctor);
    }
    
    /**
     * 更新医生信息
     * 
     * @param doctor 医生聚合根
     * @return 更新后的医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor updateDoctor(Doctor doctor) {
        Doctor existingDoctor = doctorRepository.findById(doctor.getId());
        if (existingDoctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        
        return doctorRepository.save(doctor);
    }
    
    /**
     * 根据ID查找医生
     * 
     * @param doctorId 医生ID
     * @return 医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor findDoctorById(DoctorId doctorId) {
        return doctorRepository.findById(doctorId);
    }
}

