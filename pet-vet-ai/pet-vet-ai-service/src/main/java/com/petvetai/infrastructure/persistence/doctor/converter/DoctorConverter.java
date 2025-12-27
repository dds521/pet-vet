package com.petvetai.infrastructure.persistence.doctor.converter;

import com.petvetai.domain.doctor.model.*;
import com.petvetai.infrastructure.persistence.doctor.po.DoctorPO;
import org.springframework.stereotype.Component;

/**
 * 医生转换器
 * 
 * 负责领域对象和持久化对象之间的转换
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Component
public class DoctorConverter {
    
    /**
     * 将领域对象转换为持久化对象
     * 
     * @param doctor 医生聚合根
     * @return 持久化对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public DoctorPO toPO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        DoctorPO po = new DoctorPO();
        po.setId(doctor.getId() != null ? doctor.getId().getValue() : null);
        po.setType(doctor.getType() != null ? doctor.getType().getCode() : null);
        
        // 医生信息
        if (doctor.getDoctorInfo() != null) {
            DoctorInfo info = doctor.getDoctorInfo();
            po.setName(info.getName());
            po.setGender(info.getGender());
            po.setAge(info.getAge());
            po.setPhone(info.getPhone());
            po.setEmail(info.getEmail());
            po.setAvatarUrl(info.getAvatarUrl());
            po.setBio(info.getBio());
            po.setInstitutionName(info.getInstitutionName());
            po.setInstitutionAddress(info.getInstitutionAddress());
        }
        
        // 地址信息
        if (doctor.getAddress() != null) {
            Address address = doctor.getAddress();
            po.setProvince(address.getProvince());
            po.setCity(address.getCity());
            po.setDistrict(address.getDistrict());
            po.setDetail(address.getDetail());
            po.setFullAddress(address.getFullAddress());
            po.setLongitude(address.getLongitude());
            po.setLatitude(address.getLatitude());
            po.setAddressCode(address.getAddressCode());
        }
        
        // 资质证明
        if (doctor.getQualification() != null) {
            Qualification qualification = doctor.getQualification();
            po.setLicenseNumber(qualification.getLicenseNumber());
            po.setLicensePhotoUrl(qualification.getLicensePhotoUrl());
            po.setCertificateNumber(qualification.getCertificateNumber());
            po.setCertificatePhotoUrl(qualification.getCertificatePhotoUrl());
            po.setSpecialty(qualification.getSpecialty());
            po.setYearsOfExperience(qualification.getYearsOfExperience());
            po.setEducation(qualification.getEducation());
        }
        
        po.setStatus(doctor.getStatus() != null ? doctor.getStatus().getCode() : DoctorStatus.PENDING.getCode());
        po.setApproveTime(doctor.getApproveTime());
        po.setRejectReason(doctor.getRejectReason());
        
        return po;
    }
    
    /**
     * 将持久化对象转换为领域对象
     * 
     * @param po 持久化对象
     * @return 医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public Doctor toDomain(DoctorPO po) {
        if (po == null) {
            return null;
        }
        
        // 构建值对象
        DoctorId id = po.getId() != null ? DoctorId.of(po.getId()) : null;
        DoctorType type = DoctorType.fromCode(po.getType());
        
        // 构建医生信息
        DoctorInfo doctorInfo;
        if (type == DoctorType.INSTITUTION) {
            doctorInfo = DoctorInfo.ofInstitution(
                po.getName(),
                po.getGender(),
                po.getAge(),
                po.getPhone(),
                po.getEmail(),
                po.getAvatarUrl(),
                po.getBio(),
                po.getInstitutionName(),
                po.getInstitutionAddress()
            );
        } else {
            doctorInfo = DoctorInfo.ofIndividual(
                po.getName(),
                po.getGender(),
                po.getAge(),
                po.getPhone(),
                po.getEmail(),
                po.getAvatarUrl(),
                po.getBio()
            );
        }
        
        // 构建地址
        Address address = Address.of(
            po.getProvince(),
            po.getCity(),
            po.getDistrict(),
            po.getDetail(),
            po.getLongitude(),
            po.getLatitude(),
            po.getAddressCode()
        );
        
        // 构建资质证明
        Qualification qualification = Qualification.of(
            po.getLicenseNumber(),
            po.getLicensePhotoUrl(),
            po.getCertificateNumber(),
            po.getCertificatePhotoUrl(),
            po.getSpecialty(),
            po.getYearsOfExperience(),
            po.getEducation()
        );
        
        // 重建聚合根
        Doctor doctor = Doctor.reconstruct(
            id,
            type,
            doctorInfo,
            address,
            qualification,
            DoctorStatus.fromCode(po.getStatus()),
            po.getCreateTime(),
            po.getUpdateTime(),
            po.getApproveTime(),
            po.getRejectReason()
        );
        
        return doctor;
    }
}

