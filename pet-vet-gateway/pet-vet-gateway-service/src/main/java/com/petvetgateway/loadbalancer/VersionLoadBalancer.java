package com.petvetgateway.loadbalancer;

import com.petvetgateway.config.GatewayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 版本负载均衡器
 * 
 * 根据 Nacos 元数据中的 version 字段选择对应的服务实例
 * 实现灰度发布：10% 流量走新版本，90% 流量走旧版本
 * 
 * 工作原理：
 * 1. GrayReleaseFilter 将目标版本添加到请求头 X-Target-Version（仅网关内部使用）
 * 2. VersionLoadBalancer 从请求头中读取目标版本
 * 3. 从服务实例列表中筛选出匹配版本的服务实例
 * 4. 如果没有匹配的实例，返回所有实例（降级处理）
 * 
 * 注意：请求头 X-Target-Version 仅在网关内部使用，不会传递给下游服务
 * 
 * @author daidasheng
 * @date 2026-01-07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class VersionLoadBalancer {
    
    /**
     * 版本请求头名称（仅网关内部使用，不会传递给下游服务）
     */
    private static final String VERSION_HEADER = "X-Target-Version";
    
    /**
     * 网关配置（用于获取负载均衡配置）
     */
    private final GatewayConfig gatewayConfig;
    
    /**
     * 创建版本感知的服务实例列表提供者配置
     * 
     * 使用装饰器模式，为每个服务创建版本感知的 ServiceInstanceListSupplier
     * 
     * @param clientFactory LoadBalancer 客户端工厂
     * @return 服务实例列表提供者配置
     * @author daidasheng
     * @date 2026-01-07
     */
    @Bean
    @Primary
    public ServiceInstanceListSupplier versionAwareServiceInstanceListSupplier(LoadBalancerClientFactory clientFactory) {
        return new VersionAwareServiceInstanceListSupplier(clientFactory, gatewayConfig);
    }
    
    /**
     * 版本感知的服务实例列表提供者
     * 
     * 使用装饰器模式，包装原始的 ServiceInstanceListSupplier
     */
    private static class VersionAwareServiceInstanceListSupplier implements ServiceInstanceListSupplier {
        
        private final LoadBalancerClientFactory clientFactory;
        private final GatewayConfig gatewayConfig;
        private ServiceInstanceListSupplier delegate;
        private String serviceId;
        
        public VersionAwareServiceInstanceListSupplier(LoadBalancerClientFactory clientFactory, 
                                                       GatewayConfig gatewayConfig) {
            this.clientFactory = clientFactory;
            this.gatewayConfig = gatewayConfig;
        }
        
        @Override
        public String getServiceId() {
            return serviceId;
        }
        
        @Override
        public Flux<List<ServiceInstance>> get() {
            // Supplier 接口的方法，Gateway 中通常不使用
            if (delegate != null) {
                return delegate.get();
            }
            return Flux.just(List.of());
        }
        
        @Override
        public Flux<List<ServiceInstance>> get(Request request) {
            // 延迟初始化 delegate
            if (delegate == null) {
                // 从请求中获取服务ID
                String requestServiceId = getServiceIdFromRequest(request);
                if (requestServiceId == null) {
                    log.warn("无法从请求中获取服务ID");
                    return Flux.just(List.of());
                }
                
                this.serviceId = requestServiceId;
                
                // 获取原始的服务实例列表提供者
                ObjectProvider<ServiceInstanceListSupplier> provider = clientFactory.getLazyProvider(requestServiceId, ServiceInstanceListSupplier.class);
                
                if (provider == null) {
                    log.warn("无法获取服务实例列表提供者，服务ID: {}", requestServiceId);
                    return Flux.just(List.of());
                }
                
                this.delegate = provider.getIfAvailable();
                if (this.delegate == null) {
                    log.warn("服务实例列表提供者不可用，服务ID: {}", requestServiceId);
                    return Flux.just(List.of());
                }
            }
            
            // 获取服务实例列表并过滤
            return delegate.get(request).map(instances -> filterInstancesByVersion(instances, request));
        }
        
        /**
         * 从请求中获取服务ID
         * 
         * Gateway 会在请求上下文中设置路由信息，我们可以从多个地方获取服务名：
         * 1. 从 GATEWAY_REQUEST_URL_ATTR 获取完整 URL（最可靠）
         * 2. 从路由ID推断服务名（备用方案）
         * 
         * @param request 请求对象
         * @return 服务ID
         * @author daidasheng
         * @date 2026-01-07
         */
        private String getServiceIdFromRequest(Request request) {
            if (request == null || request.getContext() == null) {
                return null;
            }
            
            if (request.getContext() instanceof RequestDataContext) {
                RequestDataContext context = (RequestDataContext) request.getContext();
                
                // 方法1：从请求 URL 属性获取（Gateway 标准属性）
                Object urlAttr = context.getClientRequest().getAttributes()
                        .get("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRequestUrl");
                if (urlAttr != null) {
                    String urlStr = urlAttr.toString();
                    // 提取服务名（例如：lb://pet-vet-ai -> pet-vet-ai）
                    if (urlStr.startsWith("lb://")) {
                        String serviceId = urlStr.substring(5); // 移除 "lb://" 前缀
                        if (serviceId.contains("/")) {
                            serviceId = serviceId.substring(0, serviceId.indexOf("/"));
                        }
                        log.debug("从请求 URL 获取服务ID: {}", serviceId);
                        return serviceId;
                    }
                }
                
                // 方法2：从路由ID推断服务名（备用方案）
                // 注意：路由ID可能与服务名不同，但通常包含服务名信息
                Object routeId = context.getClientRequest().getAttributes()
                        .get("org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRouteId");
                if (routeId != null) {
                    String routeIdStr = routeId.toString();
                    log.debug("从路由ID推断服务ID: {}", routeIdStr);
                    // 路由ID通常是服务名，直接返回
                    return routeIdStr;
                }
                
                // 方法3：从 URI 属性获取（兼容旧版本）
                Object uriAttr = context.getClientRequest().getAttributes().get("GATEWAY_REQUEST_URL_ATTR");
                if (uriAttr != null) {
                    String uriStr = uriAttr.toString();
                    if (uriStr.startsWith("lb://")) {
                        String serviceId = uriStr.substring(5);
                        if (serviceId.contains("/")) {
                            serviceId = serviceId.substring(0, serviceId.indexOf("/"));
                        }
                        log.debug("从 URI 属性获取服务ID: {}", serviceId);
                        return serviceId;
                    }
                }
            }
            
            return null;
        }
        
        /**
         * 根据版本过滤服务实例
         * 
         * @param instances 所有服务实例
         * @param request 请求对象
         * @return 过滤后的服务实例列表
         * @author daidasheng
         * @date 2026-01-07
         */
        private List<ServiceInstance> filterInstancesByVersion(List<ServiceInstance> instances, Request request) {
            
            // 从请求上下文中获取目标版本
            String targetVersion = getTargetVersion(request);
            
            if (targetVersion == null) {
                // 如果没有目标版本，返回所有实例（降级处理）
                log.debug("未指定目标版本，返回所有服务实例，数量: {}", instances.size());
                return instances;
            }
            
            // 获取负载均衡配置
            GatewayConfig.LoadBalancerConfig lbConfig = gatewayConfig.getGrayRelease() != null 
                    ? gatewayConfig.getGrayRelease().getLoadBalancer() 
                    : null;
            
            String defaultVersion = lbConfig != null ? lbConfig.getDefaultVersion() : null;
            boolean allowNoVersion = lbConfig == null || lbConfig.getAllowNoVersion() == null 
                    || lbConfig.getAllowNoVersion();
            String fallbackStrategy = lbConfig != null && lbConfig.getFallbackStrategy() != null 
                    ? lbConfig.getFallbackStrategy() : "all";
            
            // 分离有版本和无版本的实例
            List<ServiceInstance> versionedInstances = new ArrayList<>();
            List<ServiceInstance> noVersionInstances = new ArrayList<>();
            
            for (ServiceInstance instance : instances) {
                Map<String, String> metadata = instance.getMetadata();
                String instanceVersion = metadata != null ? metadata.get("version") : null;
                
                if (instanceVersion == null || instanceVersion.isEmpty()) {
                    noVersionInstances.add(instance);
                } else {
                    versionedInstances.add(instance);
                }
            }
            
            // 根据版本过滤实例
            List<ServiceInstance> matchedInstances = versionedInstances.stream()
                    .filter(instance -> {
                        Map<String, String> metadata = instance.getMetadata();
                        String instanceVersion = metadata.get("version");
                        boolean matched = targetVersion.equals(instanceVersion);
                        
                        if (matched) {
                            log.debug("服务实例匹配 - 实例: {}:{}, 版本: {}", 
                                    instance.getHost(), instance.getPort(), instanceVersion);
                        }
                        
                        return matched;
                    })
                    .collect(Collectors.toList());
            
            // 处理无版本实例
            if (!matchedInstances.isEmpty()) {
                // 如果找到匹配的实例，检查是否需要包含无版本实例
                if (allowNoVersion && defaultVersion != null && defaultVersion.equals(targetVersion)) {
                    log.debug("目标版本 {} 匹配默认版本，包含 {} 个无版本实例", targetVersion, noVersionInstances.size());
                    matchedInstances.addAll(noVersionInstances);
                }
                
                log.info("版本过滤完成 - 目标版本: {}, 匹配实例数: {}/{} (有版本: {}, 无版本: {})", 
                        targetVersion, matchedInstances.size(), instances.size(), 
                        versionedInstances.size(), noVersionInstances.size());
                
                return matchedInstances;
            }
            
            // 如果没有匹配的实例，根据降级策略处理
            return handleFallback(targetVersion, instances, versionedInstances, noVersionInstances, 
                    defaultVersion, allowNoVersion, fallbackStrategy);
        }
        
        /**
         * 处理降级策略
         * 
         * @param targetVersion 目标版本
         * @param allInstances 所有实例
         * @param versionedInstances 有版本的实例
         * @param noVersionInstances 无版本的实例
         * @param defaultVersion 默认版本
         * @param allowNoVersion 是否允许无版本实例
         * @param fallbackStrategy 降级策略
         * @return 降级后的实例列表
         * @author daidasheng
         * @date 2026-01-07
         */
        private List<ServiceInstance> handleFallback(String targetVersion, 
                                                     List<ServiceInstance> allInstances,
                                                     List<ServiceInstance> versionedInstances,
                                                     List<ServiceInstance> noVersionInstances,
                                                     String defaultVersion,
                                                     boolean allowNoVersion,
                                                     String fallbackStrategy) {
            
            switch (fallbackStrategy) {
                case "default-version":
                    // 返回默认版本的实例
                    if (defaultVersion != null) {
                        List<ServiceInstance> defaultVersionInstances = versionedInstances.stream()
                                .filter(instance -> {
                                    String instanceVersion = instance.getMetadata().get("version");
                                    return defaultVersion.equals(instanceVersion);
                                })
                                .collect(Collectors.toList());
                        
                        if (!defaultVersionInstances.isEmpty()) {
                            log.warn("未找到版本 {} 的服务实例，降级到默认版本 {}，实例数: {}", 
                                    targetVersion, defaultVersion, defaultVersionInstances.size());
                            return defaultVersionInstances;
                        }
                    }
                    // 如果默认版本也没有实例，继续执行 fail 策略
                    log.warn("默认版本 {} 也没有实例，执行 fail 策略", defaultVersion);
                    return new ArrayList<>();
                    
                case "fail":
                    // 返回空列表（可能导致请求失败）
                    log.error("未找到版本 {} 的服务实例，且降级策略为 fail，返回空列表", targetVersion);
                    return new ArrayList<>();
                    
                case "all":
                default:
                    // 返回所有实例（包括无版本实例）
                    if (allowNoVersion) {
                        log.warn("未找到版本 {} 的服务实例，返回所有实例作为降级处理（包括 {} 个无版本实例）", 
                                targetVersion, noVersionInstances.size());
                        return allInstances;
                    } else {
                        log.warn("未找到版本 {} 的服务实例，且不允许无版本实例，返回所有有版本实例", targetVersion);
                        return versionedInstances.isEmpty() ? allInstances : versionedInstances;
                    }
            }
        }
        
        /**
         * 从请求上下文中获取目标版本
         * 
         * @param request 请求对象
         * @return 目标版本，如果不存在返回 null
         * @author daidasheng
         * @date 2026-01-07
         */
        private String getTargetVersion(Request request) {
            if (request == null || request.getContext() == null) {
                return null;
            }
            
            if (request.getContext() instanceof RequestDataContext) {
                RequestDataContext context = (RequestDataContext) request.getContext();
                // 从请求头中获取版本信息
                String version = context.getClientRequest().getHeaders().getFirst(VERSION_HEADER);
                return version;
            }
            
            return null;
        }
    }
}
