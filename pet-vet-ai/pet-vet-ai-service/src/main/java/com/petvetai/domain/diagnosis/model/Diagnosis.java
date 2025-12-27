package com.petvetai.domain.diagnosis.model;

import com.petvetai.domain.pet.model.PetId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 诊断聚合根
 * 
 * 诊断是诊断域的聚合根，负责管理诊断相关的业务逻辑
 * 
 * @author daidasheng
 * @date 2024-12-20
 */
public class Diagnosis {
    
    private DiagnosisId id;
    private PetId petId;
    private Symptom symptom;
    private DiagnosisResult result;
    private DiagnosisStatus status;
    private LocalDateTime createTime;
    
    /**
     * 私有构造函数，防止直接创建
     * 
     * @author daidasheng
     * @date 2024-12-20
     */
    private Diagnosis() {
    }
    
    /**
     * 创建诊断
     * 
     * @param petId 宠物ID
     * @param symptomDesc 症状描述
     * @return 诊断聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Diagnosis create(PetId petId, String symptomDesc) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.id = DiagnosisId.generate();
        diagnosis.petId = petId;
        diagnosis.symptom = Symptom.of(symptomDesc);
        diagnosis.status = DiagnosisStatus.PENDING;
        diagnosis.createTime = LocalDateTime.now();
        return diagnosis;
    }
    
    /**
     * 从已有数据重建诊断（用于从数据库加载）
     * 
     * @param id 诊断ID
     * @param petId 宠物ID
     * @param symptom 症状
     * @param status 诊断状态
     * @param createTime 创建时间
     * @return 诊断聚合根
     * @author daidasheng
     * @date 2024-12-20
     */
    public static Diagnosis reconstruct(DiagnosisId id, PetId petId, Symptom symptom, 
                                       DiagnosisStatus status, LocalDateTime createTime) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.id = id;
        diagnosis.petId = petId;
        diagnosis.symptom = symptom;
        diagnosis.status = status;
        diagnosis.createTime = createTime;
        return diagnosis;
    }
    
    /**
     * 完成诊断
     * 
     * @param result 诊断结果
     * @author daidasheng
     * @date 2024-12-20
     */
    public void complete(DiagnosisResult result) {
        if (this.status != DiagnosisStatus.PENDING) {
            throw new IllegalStateException("诊断已完成，无法再次完成");
        }
        if (result == null) {
            throw new IllegalArgumentException("诊断结果不能为空");
        }
        this.result = result;
        this.status = DiagnosisStatus.COMPLETED;
    }
    
    /**
     * 检查诊断是否已完成
     * 
     * @return 是否已完成
     * @author daidasheng
     * @date 2024-12-20
     */
    public boolean isCompleted() {
        return this.status == DiagnosisStatus.COMPLETED;
    }
    
    // Getters
    public DiagnosisId getId() {
        return id;
    }
    
    public PetId getPetId() {
        return petId;
    }
    
    public Symptom getSymptom() {
        return symptom;
    }
    
    public DiagnosisResult getResult() {
        return result;
    }
    
    public DiagnosisStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    // 包级私有方法，用于转换器设置字段
    void setIdInternal(DiagnosisId id) {
        this.id = id;
    }
    
    void setPetIdInternal(PetId petId) {
        this.petId = petId;
    }
    
    void setSymptomInternal(Symptom symptom) {
        this.symptom = symptom;
    }
    
    void setResultInternal(DiagnosisResult result) {
        this.result = result;
    }
    
    void setStatusInternal(DiagnosisStatus status) {
        this.status = status;
    }
    
    void setCreateTimeInternal(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Diagnosis diagnosis = (Diagnosis) o;
        return Objects.equals(id, diagnosis.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

