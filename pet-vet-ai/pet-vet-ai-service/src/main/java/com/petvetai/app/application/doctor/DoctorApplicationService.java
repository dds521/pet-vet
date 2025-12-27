package com.petvetai.app.application.doctor;

import com.petvetai.app.dto.req.*;
import com.petvetai.app.dto.resp.DoctorDetailResp;
import com.petvetai.app.dto.resp.DoctorListResp;
import com.petvetai.app.dto.resp.DoctorRegisterResp;
import com.petvetai.domain.doctor.model.*;
import com.petvetai.domain.doctor.repository.DoctorRepository;
import com.petvetai.domain.doctor.service.DoctorDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生应用服务
 * 
 * 协调领域对象完成业务用例，处理事务等技术问题
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorApplicationService {
    
    private final DoctorDomainService doctorDomainService;
    private final DoctorRepository doctorRepository;
    
    /**
     * 注册医生（个人类型）
     * 
     * @param req 注册请求
     * @return 注册结果
     * @author daidasheng
     * @date 2024-12-27
     */
    @Transactional
    public DoctorRegisterResp registerIndividual(DoctorRegisterReq req) {
        log.info("开始注册个人类型医生，手机号: {}", req.getPhone());
        
        // 构建值对象
        DoctorInfo doctorInfo = DoctorInfo.ofIndividual(
            req.getName(),
            req.getGender(),
            req.getAge(),
            req.getPhone(),
            req.getEmail(),
            req.getAvatarUrl(),
            req.getBio()
        );
        
        Address address = Address.of(
            req.getProvince(),
            req.getCity(),
            req.getDistrict(),
            req.getDetail(),
            req.getLongitude(),
            req.getLatitude(),
            req.getAddressCode()
        );
        
        Qualification qualification = Qualification.of(
            req.getLicenseNumber(),
            req.getLicensePhotoUrl(),
            req.getCertificateNumber(),
            req.getCertificatePhotoUrl(),
            req.getSpecialty(),
            req.getYearsOfExperience(),
            req.getEducation()
        );
        
        // 创建聚合根
        Doctor doctor = Doctor.registerIndividual(doctorInfo, address, qualification);
        
        // 调用领域服务注册
        doctor = doctorDomainService.registerIndividual(doctor);
        
        log.info("个人类型医生注册成功，医生ID: {}", doctor.getId());
        
        // 转换为响应DTO
        return DoctorRegisterResp.from(doctor);
    }
    
    /**
     * 注册医生（机构类型）
     * 
     * @param req 注册请求
     * @return 注册结果
     * @author daidasheng
     * @date 2024-12-27
     */
    @Transactional
    public DoctorRegisterResp registerInstitution(DoctorRegisterReq req) {
        log.info("开始注册机构类型医生，手机号: {}, 机构名称: {}", req.getPhone(), req.getInstitutionName());
        
        // 构建值对象
        DoctorInfo doctorInfo = DoctorInfo.ofInstitution(
            req.getName(),
            req.getGender(),
            req.getAge(),
            req.getPhone(),
            req.getEmail(),
            req.getAvatarUrl(),
            req.getBio(),
            req.getInstitutionName(),
            req.getInstitutionAddress()
        );
        
        Address address = Address.of(
            req.getProvince(),
            req.getCity(),
            req.getDistrict(),
            req.getDetail(),
            req.getLongitude(),
            req.getLatitude(),
            req.getAddressCode()
        );
        
        Qualification qualification = Qualification.of(
            req.getLicenseNumber(),
            req.getLicensePhotoUrl(),
            req.getCertificateNumber(),
            req.getCertificatePhotoUrl(),
            req.getSpecialty(),
            req.getYearsOfExperience(),
            req.getEducation()
        );
        
        // 创建聚合根
        Doctor doctor = Doctor.registerInstitution(doctorInfo, address, qualification);
        
        // 调用领域服务注册
        doctor = doctorDomainService.registerInstitution(doctor);
        
        log.info("机构类型医生注册成功，医生ID: {}", doctor.getId());
        
        // 转换为响应DTO
        return DoctorRegisterResp.from(doctor);
    }
    
    /**
     * 更新医生信息
     * 
     * @param doctorId 医生ID
     * @param req 更新请求
     * @author daidasheng
     * @date 2024-12-27
     */
    @Transactional
    public void updateDoctor(Long doctorId, DoctorUpdateReq req) {
        log.info("开始更新医生信息，医生ID: {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId));
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        
        // 更新医生信息
        if (req.getName() != null || req.getGender() != null || req.getAge() != null ||
            req.getPhone() != null || req.getEmail() != null || req.getAvatarUrl() != null ||
            req.getBio() != null || req.getInstitutionName() != null || req.getInstitutionAddress() != null) {
            
            DoctorInfo doctorInfo;
            if (doctor.getType() == DoctorType.INSTITUTION) {
                doctorInfo = DoctorInfo.ofInstitution(
                    req.getName() != null ? req.getName() : doctor.getDoctorInfo().getName(),
                    req.getGender() != null ? req.getGender() : doctor.getDoctorInfo().getGender(),
                    req.getAge() != null ? req.getAge() : doctor.getDoctorInfo().getAge(),
                    req.getPhone() != null ? req.getPhone() : doctor.getDoctorInfo().getPhone(),
                    req.getEmail() != null ? req.getEmail() : doctor.getDoctorInfo().getEmail(),
                    req.getAvatarUrl() != null ? req.getAvatarUrl() : doctor.getDoctorInfo().getAvatarUrl(),
                    req.getBio() != null ? req.getBio() : doctor.getDoctorInfo().getBio(),
                    req.getInstitutionName() != null ? req.getInstitutionName() : doctor.getDoctorInfo().getInstitutionName(),
                    req.getInstitutionAddress() != null ? req.getInstitutionAddress() : doctor.getDoctorInfo().getInstitutionAddress()
                );
            } else {
                doctorInfo = DoctorInfo.ofIndividual(
                    req.getName() != null ? req.getName() : doctor.getDoctorInfo().getName(),
                    req.getGender() != null ? req.getGender() : doctor.getDoctorInfo().getGender(),
                    req.getAge() != null ? req.getAge() : doctor.getDoctorInfo().getAge(),
                    req.getPhone() != null ? req.getPhone() : doctor.getDoctorInfo().getPhone(),
                    req.getEmail() != null ? req.getEmail() : doctor.getDoctorInfo().getEmail(),
                    req.getAvatarUrl() != null ? req.getAvatarUrl() : doctor.getDoctorInfo().getAvatarUrl(),
                    req.getBio() != null ? req.getBio() : doctor.getDoctorInfo().getBio()
                );
            }
            doctor.updateInfo(doctorInfo);
        }
        
        // 更新地址
        if (req.getProvince() != null || req.getCity() != null || req.getDistrict() != null ||
            req.getDetail() != null || req.getLongitude() != null || req.getLatitude() != null ||
            req.getAddressCode() != null) {
            Address address = Address.of(
                req.getProvince() != null ? req.getProvince() : doctor.getAddress().getProvince(),
                req.getCity() != null ? req.getCity() : doctor.getAddress().getCity(),
                req.getDistrict() != null ? req.getDistrict() : doctor.getAddress().getDistrict(),
                req.getDetail() != null ? req.getDetail() : doctor.getAddress().getDetail(),
                req.getLongitude() != null ? req.getLongitude() : doctor.getAddress().getLongitude(),
                req.getLatitude() != null ? req.getLatitude() : doctor.getAddress().getLatitude(),
                req.getAddressCode() != null ? req.getAddressCode() : doctor.getAddress().getAddressCode()
            );
            doctor.updateAddress(address);
        }
        
        // 更新资质证明
        if (req.getLicenseNumber() != null || req.getLicensePhotoUrl() != null ||
            req.getCertificateNumber() != null || req.getCertificatePhotoUrl() != null ||
            req.getSpecialty() != null || req.getYearsOfExperience() != null || req.getEducation() != null) {
            Qualification qualification = Qualification.of(
                req.getLicenseNumber() != null ? req.getLicenseNumber() : doctor.getQualification().getLicenseNumber(),
                req.getLicensePhotoUrl() != null ? req.getLicensePhotoUrl() : doctor.getQualification().getLicensePhotoUrl(),
                req.getCertificateNumber() != null ? req.getCertificateNumber() : doctor.getQualification().getCertificateNumber(),
                req.getCertificatePhotoUrl() != null ? req.getCertificatePhotoUrl() : doctor.getQualification().getCertificatePhotoUrl(),
                req.getSpecialty() != null ? req.getSpecialty() : doctor.getQualification().getSpecialty(),
                req.getYearsOfExperience() != null ? req.getYearsOfExperience() : doctor.getQualification().getYearsOfExperience(),
                req.getEducation() != null ? req.getEducation() : doctor.getQualification().getEducation()
            );
            doctor.updateQualification(qualification);
        }
        
        // 保存
        doctorDomainService.updateDoctor(doctor);
        
        log.info("医生信息更新成功，医生ID: {}", doctorId);
    }
    
    /**
     * 审核医生
     * 
     * @param doctorId 医生ID
     * @param req 审核请求
     * @author daidasheng
     * @date 2024-12-27
     */
    @Transactional
    public void approveDoctor(Long doctorId, DoctorApproveReq req) {
        log.info("开始审核医生，医生ID: {}, 审核结果: {}", doctorId, req.getApproved());
        
        doctorDomainService.approveDoctor(
            DoctorId.of(doctorId),
            req.getApproved(),
            req.getRejectReason()
        );
        
        log.info("医生审核完成，医生ID: {}", doctorId);
    }
    
    /**
     * 查询附近的医生列表
     * 
     * @param req 查询请求
     * @return 医生列表响应
     * @author daidasheng
     * @date 2024-12-27
     */
    public DoctorListResp findNearbyDoctors(DoctorListReq req) {
        log.info("查询附近的医生，经度: {}, 纬度: {}, 最大距离: {}公里", 
                req.getLongitude(), req.getLatitude(), req.getMaxDistance());
        
        List<Doctor> doctors = doctorRepository.findNearbyDoctors(
            req.getLongitude(),
            req.getLatitude(),
            req.getMaxDistance() != null ? req.getMaxDistance() : 10.0, // 默认10公里
            req.getLimit() != null ? req.getLimit() : 20 // 默认20条
        );
        
        // 转换为响应DTO
        List<DoctorListResp.DoctorItem> items = doctors.stream()
                .map(doctor -> {
                    Double distance = doctor.calculateDistance(req.getLongitude(), req.getLatitude());
                    return DoctorListResp.DoctorItem.from(doctor, distance);
                })
                .collect(Collectors.toList());
        
        return DoctorListResp.builder()
                .doctors(items)
                .total(items.size())
                .build();
    }
    
    /**
     * 根据地址编码查询医生列表
     * 
     * @param req 查询请求
     * @return 医生列表响应
     * @author daidasheng
     * @date 2024-12-27
     */
    public DoctorListResp findByAddressCode(DoctorListByAddressReq req) {
        log.info("根据地址编码查询医生，地址编码: {}, 状态: {}", 
                req.getAddressCode(), req.getStatus());
        
        DoctorStatus status = req.getStatus() != null ? 
                DoctorStatus.fromCode(req.getStatus()) : DoctorStatus.APPROVED;
        
        List<Doctor> doctors = doctorRepository.findByAddressCode(req.getAddressCode(), status);
        
        // 转换为响应DTO
        List<DoctorListResp.DoctorItem> items = doctors.stream()
                .map(doctor -> DoctorListResp.DoctorItem.from(doctor, null))
                .collect(Collectors.toList());
        
        return DoctorListResp.builder()
                .doctors(items)
                .total(items.size())
                .build();
    }
    
    /**
     * 根据ID查询医生详情
     * 
     * @param doctorId 医生ID
     * @return 医生详情响应
     * @author daidasheng
     * @date 2024-12-27
     */
    public DoctorDetailResp getDoctorDetail(Long doctorId) {
        Doctor doctor = doctorRepository.findById(DoctorId.of(doctorId));
        if (doctor == null) {
            throw new IllegalArgumentException("医生不存在");
        }
        
        return DoctorDetailResp.from(doctor);
    }
}

