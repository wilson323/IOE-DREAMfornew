package net.lab1024.sa.visitor.config;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;

/**
 * AreaUnifiedService适配器配置
 * <p>
 * 由于AreaUnifiedService的实现类在ioedream-common-service中，
 * 而ioedream-visitor-service是独立的微服务，无法直接注入该Bean。
 * 因此创建适配器类，通过GatewayServiceClient调用common-service的API。
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Configuration
@Slf4j
public class AreaUnifiedServiceAdapter {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 创建AreaUnifiedService适配器Bean
     * <p>
     * 通过GatewayServiceClient调用common-service的API实现AreaUnifiedService接口
     * </p>
     *
     * @return AreaUnifiedService适配器实例
     */
    @Bean
    @org.springframework.context.annotation.DependsOn("gatewayServiceClient")
    public AreaUnifiedService areaUnifiedService() {
        log.info("[区域统一服务适配器] 创建AreaUnifiedService适配器Bean");
        if (gatewayServiceClient == null) {
            throw new IllegalStateException(
                    "GatewayServiceClient is not available. Please ensure CommonBeanAutoConfiguration is loaded.");
        }
        return new AreaUnifiedServiceGatewayAdapter(gatewayServiceClient);
    }

    /**
     * AreaUnifiedService网关适配器实现类
     * <p>
     * 通过GatewayServiceClient调用common-service的AreaUnifiedService API
     * </p>
     */
    private static class AreaUnifiedServiceGatewayAdapter implements AreaUnifiedService {

        private final GatewayServiceClient gatewayServiceClient;

        /**
         * 构造函数注入依赖
         *
         * @param gatewayServiceClient 网关服务客户端
         */
        public AreaUnifiedServiceGatewayAdapter(GatewayServiceClient gatewayServiceClient) {
            this.gatewayServiceClient = gatewayServiceClient;
        }

        @Override
        public List<net.lab1024.sa.common.organization.entity.AreaEntity> getAreaTree() {
            log.debug("[区域统一服务适配器] 获取完整区域树");
            ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/tree",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public List<net.lab1024.sa.common.organization.entity.AreaEntity> getUserAccessibleAreas(Long userId) {
            log.debug("[区域统一服务适配器] 获取用户可访问区域, userId={}", userId);
            ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/user/" + userId + "/accessible",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public Boolean hasAreaAccess(Long userId, Long areaId) {
            log.debug("[区域统一服务适配器] 检查用户区域权限, userId={}, areaId={}", userId, areaId);
            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/user/" + userId + "/area/" + areaId + "/has-access",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Boolean>>() {
                    });
            return response != null && response.isSuccess() && Boolean.TRUE.equals(response.getData());
        }

        @Override
        public AreaEntity getAreaByCode(String areaCode) {
            log.debug("[区域统一服务适配器] 根据编码获取区域, areaCode={}", areaCode);
            ResponseDTO<AreaEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/code/" + areaCode,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<AreaEntity>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : null;
        }

        @Override
        public List<net.lab1024.sa.common.organization.entity.AreaEntity> getAreaPath(Long areaId) {
            log.debug("[区域统一服务适配器] 获取区域路径, areaId={}", areaId);
            ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + areaId + "/path",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public List<net.lab1024.sa.common.organization.entity.AreaEntity> getChildAreas(Long parentAreaId) {
            log.debug("[区域统一服务适配器] 获取子区域, parentAreaId={}", parentAreaId);
            ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + parentAreaId + "/children",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public Map<String, Object> getAreaBusinessAttributes(Long areaId, String businessModule) {
            log.debug("[区域统一服务适配器] 获取区域业务属性, areaId={}, businessModule={}", areaId, businessModule);
            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + areaId + "/business/" + businessModule + "/attributes",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new HashMap<>();
        }

        @Override
        public boolean setAreaBusinessAttributes(Long areaId, String businessModule, Map<String, Object> attributes) {
            log.debug("[区域统一服务适配器] 设置区域业务属性, areaId={}, businessModule={}", areaId, businessModule);
            Map<String, Object> request = new HashMap<>();
            request.put("areaId", areaId);
            request.put("businessModule", businessModule);
            request.put("attributes", attributes);
            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/business/attributes",
                    HttpMethod.POST,
                    request,
                    new TypeReference<ResponseDTO<Boolean>>() {
                    });
            return response != null && response.isSuccess() && Boolean.TRUE.equals(response.getData());
        }

        @Override
        public List<Map<String, Object>> getAreaDevices(Long areaId, String deviceType) {
            log.debug("[区域统一服务适配器] 获取区域设备, areaId={}, deviceType={}", areaId, deviceType);
            String path = "/api/v1/area/unified/" + areaId + "/devices";
            if (deviceType != null && !deviceType.isEmpty()) {
                path += "?deviceType=" + deviceType;
            }
            ResponseDTO<List<Map<String, Object>>> response = gatewayServiceClient.callCommonService(
                    path,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<Map<String, Object>>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public Map<String, Object> getAreaStatistics(Long areaId) {
            log.debug("[区域统一服务适配器] 获取区域统计信息, areaId={}", areaId);
            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + areaId + "/statistics",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new HashMap<>();
        }

        @Override
        public List<net.lab1024.sa.common.organization.entity.AreaEntity> getAreasByBusinessType(String businessType) {
            log.debug("[区域统一服务适配器] 根据业务类型获取区域, businessType={}", businessType);
            ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/business-type/" + businessType,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<net.lab1024.sa.common.organization.entity.AreaEntity>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new ArrayList<>();
        }

        @Override
        public Boolean isAreaSupportBusiness(Long areaId, String businessModule) {
            log.debug("[区域统一服务适配器] 检查区域业务支持, areaId={}, businessModule={}", areaId, businessModule);
            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + areaId + "/business/" + businessModule + "/support",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Boolean>>() {
                    });
            return response != null && response.isSuccess() && Boolean.TRUE.equals(response.getData());
        }

        @Override
        public Set<String> getAreaSupportedBusinessModules(Long areaId) {
            log.debug("[区域统一服务适配器] 获取区域支持的业务模块, areaId={}", areaId);
            ResponseDTO<Set<String>> response = gatewayServiceClient.callCommonService(
                    "/api/v1/area/unified/" + areaId + "/supported-modules",
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Set<String>>>() {
                    });
            return response != null && response.isSuccess() ? response.getData() : new HashSet<>();
        }
    }
}

