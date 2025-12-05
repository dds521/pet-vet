package com.petvetai.app.controller;

import com.petvetai.app.domain.Diagnosis;
import com.petvetai.app.service.PetMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pet")
public class PetVetController {

    private final PetMedicalService petMedicalService;

    @Autowired
    public PetVetController(PetMedicalService petMedicalService) {
        this.petMedicalService = petMedicalService;
    }

    @PostMapping("/diagnose")
    public ResponseEntity<Diagnosis> diagnose(@RequestBody DiagnosisRequest request) {
        Diagnosis diagnosis = petMedicalService.analyzeSymptom(request.getPetId(), request.getSymptomDesc());
        return ResponseEntity.ok(diagnosis);
    }

    // 辅助类：请求 DTO
    public static class DiagnosisRequest {
        private Long petId;
        private String symptomDesc;

        // Getters and Setters
        public Long getPetId() { return petId; }
        public void setPetId(Long petId) { this.petId = petId; }

        public String getSymptomDesc() { return symptomDesc; }
        public void setSymptomDesc(String symptomDesc) { this.symptomDesc = symptomDesc; }
    }
}