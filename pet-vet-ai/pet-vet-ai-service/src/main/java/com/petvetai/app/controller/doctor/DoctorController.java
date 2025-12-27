package com.petvetai.app.controller.doctor;

import com.petvetai.app.application.doctor.DoctorApplicationService;
import com.petvetai.app.dto.req.*;
import com.petvetai.app.dto.resp.DoctorDetailResp;
import com.petvetai.app.dto.resp.DoctorListResp;
import com.petvetai.app.dto.resp.DoctorRegisterResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 医生控制器
 * 
 * 处理医生相关的HTTP请求
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorApplicationService doctorApplicationService;
    
    /**
     * 注册医生（个人类型）
     * 
     * @param req 注册请求
     * @return 注册响应
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/register/individual")
    public ResponseEntity<Map<String, Object>> registerIndividual(@Valid @RequestBody DoctorRegisterReq req) {
        log.info("收到个人类型医生注册请求，手机号: {}", req.getPhone());
        
        try {
            DoctorRegisterResp resp = doctorApplicationService.registerIndividual(req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功，等待审核");
            response.put("data", resp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("个人类型医生注册失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 注册医生（机构类型）
     * 
     * @param req 注册请求
     * @return 注册响应
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/register/institution")
    public ResponseEntity<Map<String, Object>> registerInstitution(@Valid @RequestBody DoctorRegisterReq req) {
        log.info("收到机构类型医生注册请求，手机号: {}, 机构名称: {}", req.getPhone(), req.getInstitutionName());
        
        try {
            DoctorRegisterResp resp = doctorApplicationService.registerInstitution(req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功，等待审核");
            response.put("data", resp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("机构类型医生注册失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 更新医生信息
     * 
     * @param doctorId 医生ID
     * @param req 更新请求
     * @return 更新结果
     * @author daidasheng
     * @date 2024-12-27
     */
    @PutMapping("/{doctorId}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@PathVariable Long doctorId,
                                                           @Valid @RequestBody DoctorUpdateReq req) {
        log.info("收到更新医生信息请求，医生ID: {}", doctorId);
        
        try {
            doctorApplicationService.updateDoctor(doctorId, req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新医生信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 审核医生
     * 
     * @param doctorId 医生ID
     * @param req 审核请求
     * @return 审核结果
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/{doctorId}/approve")
    public ResponseEntity<Map<String, Object>> approveDoctor(@PathVariable Long doctorId,
                                                             @Valid @RequestBody DoctorApproveReq req) {
        log.info("收到审核医生请求，医生ID: {}, 审核结果: {}", doctorId, req.getApproved());
        
        try {
            doctorApplicationService.approveDoctor(doctorId, req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", req.getApproved() ? "审核通过" : "审核失败");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("审核医生失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审核失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 查询附近的医生列表
     * 
     * @param req 查询请求
     * @return 医生列表响应
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/nearby")
    public ResponseEntity<Map<String, Object>> findNearbyDoctors(@Valid @RequestBody DoctorListReq req) {
        log.info("收到查询附近医生请求，经度: {}, 纬度: {}", req.getLongitude(), req.getLatitude());
        
        try {
            DoctorListResp resp = doctorApplicationService.findNearbyDoctors(req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询附近医生失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 根据地址编码查询医生列表
     * 
     * @param req 查询请求
     * @return 医生列表响应
     * @author daidasheng
     * @date 2024-12-27
     */
    @PostMapping("/list/by-address")
    public ResponseEntity<Map<String, Object>> findByAddressCode(@Valid @RequestBody DoctorListByAddressReq req) {
        log.info("收到按地址编码查询医生请求，地址编码: {}", req.getAddressCode());
        
        try {
            DoctorListResp resp = doctorApplicationService.findByAddressCode(req);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("按地址编码查询医生失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 查询医生详情
     * 
     * @param doctorId 医生ID
     * @return 医生详情响应
     * @author daidasheng
     * @date 2024-12-27
     */
    @GetMapping("/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorDetail(@PathVariable Long doctorId) {
        log.info("收到查询医生详情请求，医生ID: {}", doctorId);
        
        try {
            DoctorDetailResp resp = doctorApplicationService.getDoctorDetail(doctorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resp);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询医生详情失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}

