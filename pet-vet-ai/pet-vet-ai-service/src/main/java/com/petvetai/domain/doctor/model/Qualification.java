package com.petvetai.domain.doctor.model;

import java.util.Objects;

/**
 * 资质证明值对象
 * 
 * 包含医生的执业资质证明信息
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public class Qualification {
    
    /**
     * 执业证书编号
     */
    private final String licenseNumber;
    
    /**
     * 执业证书照片URL
     */
    private final String licensePhotoUrl;
    
    /**
     * 资格证书编号
     */
    private final String certificateNumber;
    
    /**
     * 资格证书照片URL
     */
    private final String certificatePhotoUrl;
    
    /**
     * 专业领域（如：小动物医学、大动物医学等）
     */
    private final String specialty;
    
    /**
     * 从业年限
     */
    private final Integer yearsOfExperience;
    
    /**
     * 教育背景（学历、毕业院校等）
     */
    private final String education;
    
    /**
     * 私有构造函数
     * 
     * @param licenseNumber 执业证书编号
     * @param licensePhotoUrl 执业证书照片URL
     * @param certificateNumber 资格证书编号
     * @param certificatePhotoUrl 资格证书照片URL
     * @param specialty 专业领域
     * @param yearsOfExperience 从业年限
     * @param education 教育背景
     * @author daidasheng
     * @date 2024-12-27
     */
    private Qualification(String licenseNumber, String licensePhotoUrl,
                          String certificateNumber, String certificatePhotoUrl,
                          String specialty, Integer yearsOfExperience, String education) {
        this.licenseNumber = licenseNumber;
        this.licensePhotoUrl = licensePhotoUrl;
        this.certificateNumber = certificateNumber;
        this.certificatePhotoUrl = certificatePhotoUrl;
        this.specialty = specialty;
        this.yearsOfExperience = yearsOfExperience;
        this.education = education;
    }
    
    /**
     * 创建资质证明值对象
     * 
     * @param licenseNumber 执业证书编号
     * @param licensePhotoUrl 执业证书照片URL
     * @param certificateNumber 资格证书编号
     * @param certificatePhotoUrl 资格证书照片URL
     * @param specialty 专业领域
     * @param yearsOfExperience 从业年限
     * @param education 教育背景
     * @return 资质证明值对象
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Qualification of(String licenseNumber, String licensePhotoUrl,
                                   String certificateNumber, String certificatePhotoUrl,
                                   String specialty, Integer yearsOfExperience, String education) {
        return new Qualification(licenseNumber, licensePhotoUrl, certificateNumber, 
                                certificatePhotoUrl, specialty, yearsOfExperience, education);
    }
    
    /**
     * 判断资质证明是否完整
     * 
     * @return 是否完整
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean isComplete() {
        return licenseNumber != null && !licenseNumber.isEmpty() &&
               licensePhotoUrl != null && !licensePhotoUrl.isEmpty() &&
               certificateNumber != null && !certificateNumber.isEmpty() &&
               certificatePhotoUrl != null && !certificatePhotoUrl.isEmpty();
    }
    
    // Getters
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public String getLicensePhotoUrl() {
        return licensePhotoUrl;
    }
    
    public String getCertificateNumber() {
        return certificateNumber;
    }
    
    public String getCertificatePhotoUrl() {
        return certificatePhotoUrl;
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public String getEducation() {
        return education;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Qualification that = (Qualification) o;
        return Objects.equals(licenseNumber, that.licenseNumber) &&
               Objects.equals(certificateNumber, that.certificateNumber);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(licenseNumber, certificateNumber);
    }
}

