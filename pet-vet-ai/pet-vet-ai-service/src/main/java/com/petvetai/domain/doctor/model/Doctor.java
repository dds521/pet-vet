package com.petvetai.domain.doctor.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 医生聚合根
 * 
 * 医生是医生域的聚合根，负责管理医生相关的所有业务逻辑
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
public class Doctor {
    
    private DoctorId id;                    // 医生ID值对象
    private DoctorType type;                 // 医生类型（个人/机构）
    private DoctorInfo doctorInfo;           // 医生信息值对象
    private Address address;                 // 地址值对象
    private Qualification qualification;     // 资质证明值对象
    private DoctorStatus status;             // 医生状态
    private LocalDateTime createTime;       // 创建时间
    private LocalDateTime updateTime;        // 更新时间
    private LocalDateTime approveTime;       // 审核通过时间
    private String rejectReason;             // 审核失败原因
    
    /**
     * 私有构造函数，防止直接创建
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    private Doctor() {
    }
    
    /**
     * 注册医生（个人类型）
     * 
     * @param doctorInfo 医生信息
     * @param address 地址信息
     * @param qualification 资质证明
     * @return 医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Doctor registerIndividual(DoctorInfo doctorInfo, Address address, Qualification qualification) {
        if (doctorInfo == null) {
            throw new IllegalArgumentException("医生信息不能为空");
        }
        if (address == null) {
            throw new IllegalArgumentException("地址信息不能为空");
        }
        if (qualification == null) {
            throw new IllegalArgumentException("资质证明不能为空");
        }
        if (!qualification.isComplete()) {
            throw new IllegalArgumentException("资质证明信息不完整");
        }
        if (doctorInfo.isInstitution()) {
            throw new IllegalArgumentException("个人类型医生不能包含机构信息");
        }
        
        Doctor doctor = new Doctor();
        doctor.id = DoctorId.generate();
        doctor.type = DoctorType.INDIVIDUAL;
        doctor.doctorInfo = doctorInfo;
        doctor.address = address;
        doctor.qualification = qualification;
        doctor.status = DoctorStatus.PENDING;
        doctor.createTime = LocalDateTime.now();
        doctor.updateTime = LocalDateTime.now();
        
        return doctor;
    }
    
    /**
     * 注册医生（机构类型）
     * 
     * @param doctorInfo 医生信息（必须包含机构信息）
     * @param address 地址信息
     * @param qualification 资质证明
     * @return 医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Doctor registerInstitution(DoctorInfo doctorInfo, Address address, Qualification qualification) {
        if (doctorInfo == null) {
            throw new IllegalArgumentException("医生信息不能为空");
        }
        if (address == null) {
            throw new IllegalArgumentException("地址信息不能为空");
        }
        if (qualification == null) {
            throw new IllegalArgumentException("资质证明不能为空");
        }
        if (!qualification.isComplete()) {
            throw new IllegalArgumentException("资质证明信息不完整");
        }
        if (!doctorInfo.isInstitution()) {
            throw new IllegalArgumentException("机构类型医生必须包含机构信息");
        }
        
        Doctor doctor = new Doctor();
        doctor.id = DoctorId.generate();
        doctor.type = DoctorType.INSTITUTION;
        doctor.doctorInfo = doctorInfo;
        doctor.address = address;
        doctor.qualification = qualification;
        doctor.status = DoctorStatus.PENDING;
        doctor.createTime = LocalDateTime.now();
        doctor.updateTime = LocalDateTime.now();
        
        return doctor;
    }
    
    /**
     * 从已有数据重建医生（用于从数据库加载）
     * 
     * @param id 医生ID
     * @param type 医生类型
     * @param doctorInfo 医生信息
     * @param address 地址信息
     * @param qualification 资质证明
     * @param status 医生状态
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @param approveTime 审核通过时间
     * @param rejectReason 审核失败原因
     * @return 医生聚合根
     * @author daidasheng
     * @date 2024-12-27
     */
    public static Doctor reconstruct(DoctorId id, DoctorType type, DoctorInfo doctorInfo, Address address,
                                    Qualification qualification, DoctorStatus status,
                                    LocalDateTime createTime, LocalDateTime updateTime,
                                    LocalDateTime approveTime, String rejectReason) {
        Doctor doctor = new Doctor();
        doctor.id = id;
        doctor.type = type;
        doctor.doctorInfo = doctorInfo;
        doctor.address = address;
        doctor.qualification = qualification;
        doctor.status = status;
        doctor.createTime = createTime;
        doctor.updateTime = updateTime;
        doctor.approveTime = approveTime;
        doctor.rejectReason = rejectReason;
        return doctor;
    }
    
    /**
     * 更新医生信息
     * 
     * @param doctorInfo 新的医生信息
     * @author daidasheng
     * @date 2024-12-27
     */
    public void updateInfo(DoctorInfo doctorInfo) {
        if (doctorInfo == null) {
            throw new IllegalArgumentException("医生信息不能为空");
        }
        // 验证类型一致性
        if (this.type == DoctorType.INDIVIDUAL && doctorInfo.isInstitution()) {
            throw new IllegalArgumentException("个人类型医生不能更新为机构信息");
        }
        if (this.type == DoctorType.INSTITUTION && !doctorInfo.isInstitution()) {
            throw new IllegalArgumentException("机构类型医生必须包含机构信息");
        }
        this.doctorInfo = doctorInfo;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新地址信息
     * 
     * @param address 新的地址信息
     * @author daidasheng
     * @date 2024-12-27
     */
    public void updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("地址信息不能为空");
        }
        this.address = address;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新资质证明
     * 
     * @param qualification 新的资质证明
     * @author daidasheng
     * @date 2024-12-27
     */
    public void updateQualification(Qualification qualification) {
        if (qualification == null) {
            throw new IllegalArgumentException("资质证明不能为空");
        }
        if (!qualification.isComplete()) {
            throw new IllegalArgumentException("资质证明信息不完整");
        }
        this.qualification = qualification;
        this.status = DoctorStatus.PENDING; // 更新资质后需要重新审核
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 审核通过
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    public void approve() {
        if (this.status != DoctorStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的医生才能审核通过");
        }
        this.status = DoctorStatus.APPROVED;
        this.approveTime = LocalDateTime.now();
        this.rejectReason = null;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 审核失败
     * 
     * @param reason 失败原因
     * @author daidasheng
     * @date 2024-12-27
     */
    public void reject(String reason) {
        if (this.status != DoctorStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态的医生才能审核失败");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("审核失败原因不能为空");
        }
        this.status = DoctorStatus.REJECTED;
        this.rejectReason = reason;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用医生
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    public void disable() {
        if (this.status == DoctorStatus.DISABLED) {
            return; // 已经禁用，无需重复操作
        }
        this.status = DoctorStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用医生
     * 
     * @author daidasheng
     * @date 2024-12-27
     */
    public void enable() {
        if (this.status == DoctorStatus.DISABLED) {
            this.status = DoctorStatus.APPROVED;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 判断是否可以提供服务
     * 
     * @return 是否可以提供服务
     * @author daidasheng
     * @date 2024-12-27
     */
    public boolean canProvideService() {
        return status.canProvideService();
    }
    
    /**
     * 计算到指定坐标的距离
     * 
     * @param longitude 目标经度
     * @param latitude 目标纬度
     * @return 距离（公里），如果地址没有地理位置信息则返回null
     * @author daidasheng
     * @date 2024-12-27
     */
    public Double calculateDistance(Double longitude, Double latitude) {
        if (address == null) {
            return null;
        }
        return address.calculateDistance(longitude, latitude);
    }
    
    // Getters
    public DoctorId getId() {
        return id;
    }
    
    public DoctorType getType() {
        return type;
    }
    
    public DoctorInfo getDoctorInfo() {
        return doctorInfo;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public Qualification getQualification() {
        return qualification;
    }
    
    public DoctorStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public LocalDateTime getApproveTime() {
        return approveTime;
    }
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    // 包级私有方法，用于转换器设置字段
    void setIdInternal(DoctorId id) {
        this.id = id;
    }
    
    void setTypeInternal(DoctorType type) {
        this.type = type;
    }
    
    void setDoctorInfoInternal(DoctorInfo doctorInfo) {
        this.doctorInfo = doctorInfo;
    }
    
    void setAddressInternal(Address address) {
        this.address = address;
    }
    
    void setQualificationInternal(Qualification qualification) {
        this.qualification = qualification;
    }
    
    void setStatusInternal(DoctorStatus status) {
        this.status = status;
    }
    
    void setCreateTimeInternal(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    void setUpdateTimeInternal(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    void setApproveTimeInternal(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }
    
    void setRejectReasonInternal(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(id, doctor.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

