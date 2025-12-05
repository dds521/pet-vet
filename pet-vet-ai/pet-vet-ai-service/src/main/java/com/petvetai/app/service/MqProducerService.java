package com.petvetai.app.service;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqProducerService {

    private final RocketMQTemplate rocketMQTemplate;

    @Autowired
    public MqProducerService(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendDiagnosticEvent(String petId, String diagnosisResult) {
        String message = String.format("Pet:%s,Result:%s", petId, diagnosisResult);
        rocketMQTemplate.convertAndSend("pet-diagnosis-topic", message);
    }
}

