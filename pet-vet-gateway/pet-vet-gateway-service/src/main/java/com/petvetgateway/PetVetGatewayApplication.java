package com.petvetgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 * 
 * 提供统一的流量控制、鉴权、日志管理功能
 * 
 * @author daidasheng
 * @date 2024-12-27
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PetVetGatewayApplication {

    /**
     * 主方法
     * 
     * @param args 启动参数
     * @author daidasheng
     * @date 2024-12-27
     */
    public static void main(String[] args) {
        SpringApplication.run(PetVetGatewayApplication.class, args);
    }
}

