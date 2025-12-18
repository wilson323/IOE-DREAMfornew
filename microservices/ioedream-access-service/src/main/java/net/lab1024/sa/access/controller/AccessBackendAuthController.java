package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.service.AccessVerificationService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门禁后台验证控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 实现安防PUSH协议V4.8后台验证接口
 * </p>
 * <p>
 * 核心职责：
 * - 接收设备后台验证请求
 * - 解析安防PUSH协议请求格式
 * - 返回标准HTTP响应格式
 * </p>
 * <p>
 * 协议接口：POST /iclock/cdata?SN=xxx&AuthType=device
 * 协议规范：安防PUSH通讯协议 V4.8 - 13. 后台验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/iclock")
@Tag(name = "门禁后台验证", description = "门禁设备后台验证接口（设备端调用）")
public class AccessBackendAuthController {

    @Resource
    private AccessVerificationService accessVerificationService;

    @Resource
    private AreaAccessExtDao areaAccessExtDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_DEVICE_SN = "access:device:sn:";
    private static final String CACHE_KEY_DEVICE_AREA = "access:device:area:";
    
    /**
     * 缓存过期时间（1小时）
     */
    private static final Duration CACHE_EXPIRE = Duration.ofHours(1);

    /**
     * 后台验证接口
     * <p>
     * 协议：POST /iclock/cdata?SN=xxx&AuthType=device
     * </p>
     * <p>
     * 请求格式（form-data）：
     * time=2025-01-30 10:00:00{HT}pin=1001{HT}cardno=CARD001{HT}addrtype=1{HT}eventaddr=1{HT}event=0{HT}inoutstatus=1{HT}verifytype=1
     * </p>
     * <p>
     * 响应格式：
     * AUTH=SUCCEED{CR}{LF}
     * time=2025-01-30 10:00:00{HT}pin=1001{HT}cardno=CARD001{HT}addrtype=1{HT}eventaddr=1{HT}event=0{HT}inoutstatus=1{HT}verifytype=1{CR}{LF}
     * CONTROL DEVICE 0101000300{CR}{LF}
     * TIPS=验证通过,欢迎进入
     * </p>
     *
     * @param serialNumber 设备序列号（SN参数）
     * @param authType 验证类型（AuthType参数，device表示后台验证）
     * @param requestBody 请求体（form-data格式）
     * @return HTTP响应（文本格式，符合安防PUSH协议）
     */
    @PostMapping(value = "/cdata", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "后台验证", description = "门禁设备后台验证接口(设备端调用)，符合安防PUSH协议V4.8")
    public String backendVerification(
            @RequestParam("SN") String serialNumber,
            @RequestParam(value = "AuthType", required = false, defaultValue = "device") String authType,
            @RequestBody(required = false) String requestBody) {

        log.info("[后台验证] 收到验证请求: SN={}, AuthType={}", serialNumber, authType);

        try {
            // 1. 解析请求体（form-data格式）
            Map<String, String> params = parseFormData(requestBody);
            log.debug("[后台验证] 解析参数: {}", params);

            // 2. 构建验证请求
            AccessVerificationRequest request = buildVerificationRequest(serialNumber, params);

            // 3. 执行后台验证
            VerificationResult result = accessVerificationService.verifyAccess(request);

            // 4. 构建HTTP响应（安防PUSH协议格式）
            String response = buildProtocolResponse(requestBody, result);

            log.info("[后台验证] 验证完成: userId={}, result={}", request.getUserId(), result.getAuthStatus());

            return response;

        } catch (Exception e) {
            log.error("[后台验证] 验证异常: SN={}, error={}", serialNumber, e.getMessage(), e);
            // 异常时返回失败响应
            return buildErrorResponse(requestBody, "SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 解析form-data格式请求体
     * <p>
     * 格式：time=xxx{HT}pin=xxx{HT}cardno=xxx...
     * {HT} = \t (Tab键)
     * </p>
     *
     * @param body 请求体
     * @return 参数Map
     */
    private Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();

        if (body == null || body.trim().isEmpty()) {
            return params;
        }

        // 按Tab键分割
        String[] pairs = body.split("\t");
        for (String pair : pairs) {
            if (pair != null && !pair.trim().isEmpty()) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        return params;
    }

    /**
     * 构建验证请求
     *
     * @param serialNumber 设备序列号
     * @param params 解析后的参数
     * @return 验证请求对象
     */
    private AccessVerificationRequest buildVerificationRequest(String serialNumber, Map<String, String> params) {
        // 解析时间
        LocalDateTime verifyTime = null;
        if (params.containsKey("time")) {
            try {
                verifyTime = LocalDateTime.parse(params.get("time"), DATE_TIME_FORMATTER);
            } catch (Exception e) {
                log.warn("[后台验证] 时间解析失败: time={}", params.get("time"));
                verifyTime = LocalDateTime.now();
            }
        } else {
            verifyTime = LocalDateTime.now();
        }

        // 解析用户ID（pin字段）
        Long userId = null;
        if (params.containsKey("pin")) {
            try {
                userId = Long.parseLong(params.get("pin"));
            } catch (NumberFormatException e) {
                log.warn("[后台验证] 用户ID解析失败: pin={}", params.get("pin"));
            }
        }

        // 获取设备ID（通过序列号查询）
        String deviceId = getDeviceIdBySerialNumber(serialNumber);
        // 获取区域ID（通过设备ID查询）
        Long areaId = getAreaIdByDeviceId(deviceId);

        // 转换deviceId为Long类型（如果deviceId是数字字符串）
        Long deviceIdLong = null;
        if (deviceId != null) {
            try {
                deviceIdLong = Long.parseLong(deviceId);
            } catch (NumberFormatException e) {
                log.warn("[后台验证] 设备ID不是数字格式: deviceId={}", deviceId);
            }
        }

        return AccessVerificationRequest.builder()
                .userId(userId)
                .deviceId(deviceIdLong)
                .areaId(areaId)
                .serialNumber(serialNumber)
                .cardNo(params.get("cardno"))
                .event(parseInt(params.get("event"), 0))
                .verifyType(parseInt(params.get("verifytype"), 1))
                .inOutStatus(parseInt(params.get("inoutstatus"), 1))
                .verifyTime(verifyTime)
                .doorNumber(parseInt(params.get("eventaddr"), 1))
                .build();
    }

    /**
     * 根据设备序列号获取设备ID
     * <p>
     * 实现逻辑：
     * 1. 从Redis缓存查询（L2缓存）
     * 2. 缓存未命中时，从数据库查询（使用DeviceDao）
     * 3. 查询结果写入缓存（TTL: 1小时）
     * </p>
     *
     * @param serialNumber 设备序列号
     * @return 设备ID（String类型），如果设备不存在则返回null
     */
    private String getDeviceIdBySerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            log.warn("[后台验证] 设备序列号为空");
            return null;
        }

        try {
            // 1. 从Redis缓存查询
            String cacheKey = CACHE_KEY_DEVICE_SN + serialNumber;
            Object cachedDeviceId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedDeviceId != null) {
                log.debug("[后台验证] 从缓存获取设备ID: SN={}, deviceId={}", serialNumber, cachedDeviceId);
                return cachedDeviceId.toString();
            }

            // 2. 从数据库查询（使用DeviceDao直接查询，避免跨服务调用）
            DeviceEntity device = deviceDao.selectBySerialNumber(serialNumber);
            if (device == null) {
                // 尝试通过设备编码查询（某些设备序列号可能等于设备编码）
                device = deviceDao.selectByDeviceCode(serialNumber);
            }

            if (device == null || device.getDeviceId() == null) {
                log.warn("[后台验证] 设备不存在: SN={}", serialNumber);
                return null;
            }

            String deviceId = device.getDeviceId();

            // 3. 写入缓存（TTL: 1小时）
            redisTemplate.opsForValue().set(cacheKey, deviceId, CACHE_EXPIRE);
            log.debug("[后台验证] 设备ID已缓存: SN={}, deviceId={}", serialNumber, deviceId);

            return deviceId;

        } catch (Exception e) {
            log.error("[后台验证] 获取设备ID失败: SN={}, error={}", serialNumber, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据设备ID获取区域ID
     * <p>
     * 实现逻辑：
     * 1. 从Redis缓存查询（L2缓存）
     * 2. 缓存未命中时，优先从DeviceEntity.areaId获取（如果设备实体有区域ID）
     * 3. 如果DeviceEntity没有区域ID，则从AreaDeviceDao查询设备-区域关联
     * 4. 查询结果写入缓存（TTL: 1小时）
     * </p>
     *
     * @param deviceId 设备ID（String类型）
     * @return 区域ID，如果查询失败则返回null
     */
    private Long getAreaIdByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.warn("[后台验证] 设备ID为空");
            return null;
        }

        try {
            // 1. 从Redis缓存查询
            String cacheKey = CACHE_KEY_DEVICE_AREA + deviceId;
            Object cachedAreaId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedAreaId != null) {
                log.debug("[后台验证] 从缓存获取区域ID: deviceId={}, areaId={}", deviceId, cachedAreaId);
                return parseAreaId(cachedAreaId);
            }

            // 2. 优先从DeviceEntity获取区域ID（如果设备实体有区域ID字段）
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null && device.getAreaId() != null) {
                Long areaId = device.getAreaId();
                // 写入缓存
                redisTemplate.opsForValue().set(cacheKey, areaId, CACHE_EXPIRE);
                log.debug("[后台验证] 从DeviceEntity获取区域ID: deviceId={}, areaId={}", deviceId, areaId);
                return areaId;
            }

            // 3. 从AreaDeviceDao查询设备-区域关联
            List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByDeviceId(deviceId);
            
            if (areaDevices == null || areaDevices.isEmpty()) {
                log.warn("[后台验证] 设备未关联区域: deviceId={}", deviceId);
                return null;
            }

            // 4. 获取第一个关联区域的区域ID（优先获取主设备关联的区域）
            AreaDeviceEntity areaDevice = areaDevices.stream()
                    .filter(ad -> ad.getPriority() != null && ad.getPriority() == 1) // 优先获取主设备
                    .findFirst()
                    .orElse(areaDevices.get(0)); // 如果没有主设备，使用第一个

            Long areaId = areaDevice.getAreaId();
            if (areaId == null) {
                log.warn("[后台验证] 区域ID为空: deviceId={}", deviceId);
                return null;
            }

            // 5. 写入缓存（TTL: 1小时）
            redisTemplate.opsForValue().set(cacheKey, areaId, CACHE_EXPIRE);
            log.debug("[后台验证] 区域ID已缓存: deviceId={}, areaId={}", deviceId, areaId);

            return areaId;

        } catch (Exception e) {
            log.error("[后台验证] 获取区域ID失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析区域ID（支持Integer和Long类型）
     *
     * @param areaId 区域ID（可能是Integer或Long类型）
     * @return Long类型的区域ID，如果无法转换则返回null
     */
    private Long parseAreaId(Object areaId) {
        if (areaId == null) {
            return null;
        }

        if (areaId instanceof Long) {
            return (Long) areaId;
        }

        if (areaId instanceof Integer) {
            return ((Integer) areaId).longValue();
        }

        if (areaId instanceof String) {
            try {
                return Long.parseLong((String) areaId);
            } catch (NumberFormatException e) {
                log.warn("[后台验证] 区域ID格式错误，无法转换为Long: areaId={}", areaId);
                return null;
            }
        }

        log.warn("[后台验证] 区域ID类型不支持: areaId={}, type={}", areaId, areaId.getClass().getName());
        return null;
    }

    /**
     * 解析整数参数
     *
     * @param value 字符串值
     * @param defaultValue 默认值
     * @return 整数值
     */
    private Integer parseInt(String value, Integer defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 构建协议响应
     * <p>
     * 响应格式：
     * 第一行：AUTH=SUCCEED/FAILED/TIMEOUT
     * 第二行：原始事件记录（回显）
     * 第三行：控制指令（验证成功时）
     * 第四行：提示信息
     * </p>
     *
     * @param originalBody 原始请求体
     * @param result 验证结果
     * @return 协议响应字符串
     */
    private String buildProtocolResponse(String originalBody, VerificationResult result) {
        StringBuilder response = new StringBuilder();

        // 第一行：AUTH结果
        response.append("AUTH=").append(result.getAuthStatus()).append("\r\n");

        // 第二行：原始事件记录（回显）
        if (originalBody != null && !originalBody.trim().isEmpty()) {
            response.append(originalBody).append("\r\n");
        }

        // 第三行：控制指令（验证成功时）
        if (result.isSuccess() && result.getControlCommand() != null) {
            response.append("CONTROL DEVICE ").append(result.getControlCommand()).append("\r\n");
        }

        // 第四行：提示信息
        response.append("TIPS=").append(result.getMessage() != null ? result.getMessage() : "");

        return response.toString();
    }

    /**
     * 构建错误响应
     *
     * @param originalBody 原始请求体
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     * @return 协议响应字符串
     */
    private String buildErrorResponse(String originalBody, String errorCode, String errorMessage) {
        StringBuilder response = new StringBuilder();

        // 第一行：AUTH=FAILED
        response.append("AUTH=FAILED\r\n");

        // 第二行：原始事件记录（回显）
        if (originalBody != null && !originalBody.trim().isEmpty()) {
            response.append(originalBody).append("\r\n");
        }

        // 第三行：提示信息
        response.append("TIPS=").append(errorMessage);

        return response.toString();
    }
}
