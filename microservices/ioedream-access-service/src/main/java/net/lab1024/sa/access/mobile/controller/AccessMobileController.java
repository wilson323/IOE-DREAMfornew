package net.lab1024.sa.access.mobile.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.common.auth.util.JwtTokenUtil;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.util.TypeUtils;
import net.lab1024.sa.access.domain.form.*;
import net.lab1024.sa.access.domain.vo.*;
import net.lab1024.sa.access.service.AccessVerificationService;

/**
 * 门禁移动端控制器
 * <p>
 * 负责处理移动设备的门禁访问请求
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 * <li>移动端认证初始化、令牌刷新、注销</li>
 * <li>二维码生成与验证</li>
 * <li>生物识别验证（人脸、指纹等）</li>
 * <li>设备信息查询</li>
 * <li>设备心跳处理</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/mobile/v1/access")
@Tag(name = "门禁移动端", description = "门禁移动端接口，支持移动设备访问控制")
@Slf4j
public class AccessMobileController {

    @Resource
    private AccessVerificationService accessVerificationService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化移动端认证
     * <p>
     * 移动端初始化时调用，验证设备合法性并获取访问令牌
     * </p>
     *
     * @param params 认证参数
     * @return 认证结果
     */
    @PostMapping("/auth/initialize")
    @Operation(summary = "初始化移动端认证", description = "移动端初始化时调用，验证设备合法性并获取访问令牌")
    public ResponseDTO<MobileAuthInitVO> initializeAuth(@Valid @RequestBody MobileAuthInitForm params) {
        log.info("[门禁移动端] 初始化认证: deviceId={}, deviceType={}, userId={}",
            params.getDeviceId(), params.getDeviceType(), params.getUserId());

        try {
            // 1. 验证设备指纹（防止伪造设备）
            String deviceId = validateDeviceFingerprint(params);

            // 2. 如果是已登录用户，获取用户信息并生成令牌
            MobileAuthInitVO.MobileAuthInitVOBuilder builder = MobileAuthInitVO.builder()
                .deviceId(deviceId)
                .serverTimestamp(System.currentTimeMillis())
                .tokenType("Bearer")
                .serverPublicKey(getServerPublicKey())
                .challenge(generateChallenge());

            if (params.getUserId() != null) {
                // 已登录用户，生成令牌
                String accessToken = jwtTokenUtil.generateAccessToken(params.getUserId(), "mobile_user");
                String refreshToken = jwtTokenUtil.generateRefreshToken(params.getUserId(), "mobile_user");

                builder.accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtTokenUtil.getRemainingTimeFromAccessToken(accessToken))
                    .refreshExpiresIn(jwtTokenUtil.getRemainingTime(refreshToken))
                    .userInfo(getUserInfo(params.getUserId()));

                // 缓存设备信息
                cacheDeviceInfo(deviceId, params);

                log.info("[门禁移动端] 认证初始化成功: deviceId={}, userId={}", deviceId, params.getUserId());
            } else {
                // 未登录用户，返回设备信息和挑战字符串
                log.info("[门禁移动端] 设备验证成功: deviceId={}", deviceId);
            }

            return ResponseDTO.ok(builder.build());

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 认证初始化失败: deviceId={}, error={}", params.getDeviceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 初始化认证异常: deviceId={}, error={}", params.getDeviceId(), e.getMessage(), e);
            throw new BusinessException("AUTH_INIT_ERROR", "认证初始化失败: " + e.getMessage());
        }
    }

    /**
     * 刷新移动端认证令牌
     *
     * @param params 刷新参数
     * @return 新的访问令牌
     */
    @PostMapping("/auth/refresh")
    @Operation(summary = "刷新移动端认证令牌", description = "刷新过期的访问令牌")
    public ResponseDTO<MobileTokenVO> refreshToken(@Valid @RequestBody MobileRefreshTokenForm params) {
        log.info("[门禁移动端] 刷新令牌: deviceId={}", params.getDeviceId());

        try {
            String refreshToken = params.getRefreshToken();

            // 1. 验证刷新令牌
            if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
                log.warn("[门禁移动端] 刷新令牌无效: deviceId={}", params.getDeviceId());
                throw new BusinessException("TOKEN_INVALID", "刷新令牌无效或已过期");
            }

            // 2. 获取用户ID
            Long userId = jwtTokenUtil.getUserIdFromRefreshToken(refreshToken);
            if (userId == null) {
                log.warn("[门禁移动端] 无法从令牌获取用户ID: deviceId={}", params.getDeviceId());
                throw new BusinessException("TOKEN_INVALID", "令牌解析失败");
            }

            // 3. 检查令牌黑名单
            if (isTokenBlacklisted(refreshToken)) {
                log.warn("[门禁移动端] 令牌已在黑名单中: deviceId={}, userId={}", params.getDeviceId(), userId);
                throw new BusinessException("TOKEN_BLACKLISTED", "令牌已被撤销");
            }

            // 4. 验证设备ID匹配
            String cachedDeviceId = getCachedDeviceId(userId);
            if (cachedDeviceId != null && !cachedDeviceId.equals(params.getDeviceId())) {
                log.warn("[门禁移动端] 设备ID不匹配: deviceId={}, cachedDeviceId={}",
                    params.getDeviceId(), cachedDeviceId);
                throw new BusinessException("DEVICE_MISMATCH", "设备ID不匹配");
            }

            // 5. 生成新令牌对
            String newAccessToken = jwtTokenUtil.generateAccessToken(userId, "mobile_user");
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, "mobile_user");

            // 6. 旧刷新令牌加入黑名单
            Long ttl = jwtTokenUtil.getRemainingTime(refreshToken);
            addToBlacklist(refreshToken, ttl);

            // 7. 构建响应
            MobileTokenVO tokenVO = MobileTokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenUtil.getRemainingTimeFromAccessToken(newAccessToken))
                .refreshExpiresIn(jwtTokenUtil.getRemainingTime(newRefreshToken))
                .build();

            log.info("[门禁移动端] 令牌刷新成功: deviceId={}, userId={}", params.getDeviceId(), userId);

            return ResponseDTO.ok(tokenVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 令牌刷新失败: deviceId={}, error={}", params.getDeviceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 刷新令牌异常: deviceId={}, error={}", params.getDeviceId(), e.getMessage(), e);
            throw new BusinessException("TOKEN_REFRESH_ERROR", "令牌刷新失败: " + e.getMessage());
        }
    }

    /**
     * 注销移动端认证
     *
     * @param params 注销参数
     * @return 注销结果
     */
    @PostMapping("/auth/logout")
    @Operation(summary = "注销移动端认证", description = "注销当前设备的访问令牌")
    public ResponseDTO<Void> logout(@Valid @RequestBody MobileLogoutForm params) {
        log.info("[门禁移动端] 注销认证: deviceId={}", params.getDeviceId());

        try {
            String accessToken = params.getAccessToken();

            // 1. 验证访问令牌并获取用户ID
            Long userId = jwtTokenUtil.getUserIdFromAccessToken(accessToken);
            if (userId == null) {
                log.warn("[门禁移动端] 无法从令牌获取用户ID: deviceId={}", params.getDeviceId());
                throw new BusinessException("TOKEN_INVALID", "访问令牌无效");
            }

            // 2. 验证设备ID匹配
            String cachedDeviceId = getCachedDeviceId(userId);
            if (cachedDeviceId != null && !cachedDeviceId.equals(params.getDeviceId())) {
                log.warn("[门禁移动端] 设备ID不匹配: deviceId={}, cachedDeviceId={}",
                    params.getDeviceId(), cachedDeviceId);
                throw new BusinessException("DEVICE_MISMATCH", "设备ID不匹配");
            }

            // 3. 撤销访问令牌
            jwtTokenUtil.revokeToken(accessToken);

            // 4. 清除设备缓存
            clearDeviceCache(userId);

            // 5. 撤销用户所有令牌（可选，根据业务需求）
            // TODO: JwtTokenUtil需要添加revokeAllUserTokens方法支持
            // jwtTokenUtil.revokeAllUserTokens(userId);

            log.info("[门禁移动端] 注销成功: deviceId={}, userId={}", params.getDeviceId(), userId);

            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 注销失败: deviceId={}, error={}", params.getDeviceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 注销认证异常: deviceId={}, error={}", params.getDeviceId(), e.getMessage(), e);
            throw new BusinessException("LOGOUT_ERROR", "注销失败: " + e.getMessage());
        }
    }

    /**
     * 生成临时访问二维码
     *
     * @param params 二维码生成参数
     * @return 二维码数据
     */
    @PostMapping("/qrcode/generate")
    @Operation(summary = "生成临时访问二维码", description = "为临时访客生成访问二维码")
    public ResponseDTO<MobileQRCodeVO> generateQRCode(@Valid @RequestBody MobileQRCodeForm params) {
        log.info("[门禁移动端] 生成二维码: areaId={}, qrCodeType={}, userId={}, visitorId={}",
            params.getAreaId(), params.getQrCodeType(), params.getUserId(), params.getVisitorId());

        try {
            // 1. 生成唯一会话ID
            String sessionId = java.util.UUID.randomUUID().toString();

            // 2. 生成时间戳防重放攻击
            long timestamp = System.currentTimeMillis();

            // 3. 生成一次性令牌
            String oneTimeToken = generateOneTimeToken(sessionId, timestamp);

            // 4. 确定有效期（默认5分钟）
            int validityMinutes = params.getValidityMinutes() != null && params.getValidityMinutes() > 0
                ? params.getValidityMinutes() : 5;

            // 5. 构建二维码内容
            String qrContent = String.format("iot://access/auth?sid=%s&ts=%d&token=%s&type=%s",
                sessionId, timestamp, oneTimeToken, params.getQrCodeType());

            // 6. 缓存会话信息
            QRCodeSession session = QRCodeSession.builder()
                .sessionId(sessionId)
                .timestamp(timestamp)
                .token(oneTimeToken)
                .areaId(params.getAreaId())
                .userId(params.getUserId())
                .visitorId(params.getVisitorId())
                .qrCodeType(params.getQrCodeType())
                .expireTime(java.time.LocalDateTime.now().plusMinutes(validityMinutes))
                .status("pending")
                .build();

            String cacheKey = "qrcode:session:" + sessionId;
            redisTemplate.opsForValue().set(cacheKey, session, validityMinutes, java.util.concurrent.TimeUnit.MINUTES);

            // 7. 生成二维码图片（Base64）
            String qrImage = generateQRCodeImage(qrContent);

            // 8. 构建响应
            MobileQRCodeVO qrCodeVO = MobileQRCodeVO.builder()
                .sessionId(sessionId)
                .qrContent(qrContent)
                .qrImage(qrImage)
                .expireTime(session.getExpireTime())
                .validSeconds((long) validityMinutes * 60)
                .status("pending")
                .qrCodeType(params.getQrCodeType())
                .build();

            log.info("[门禁移动端] 二维码生成成功: sessionId={}, areaId={}, validMinutes={}",
                sessionId, params.getAreaId(), validityMinutes);

            return ResponseDTO.ok(qrCodeVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 二维码生成失败: areaId={}, error={}", params.getAreaId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 生成二维码异常: areaId={}, error={}", params.getAreaId(), e.getMessage(), e);
            throw new BusinessException("QRCODE_GENERATE_ERROR", "二维码生成失败: " + e.getMessage());
        }
    }

    /**
     * 验证二维码访问
     *
     * @param params 验证参数
     * @return 验证结果
     */
    @PostMapping("/qrcode/verify")
    @Operation(summary = "验证二维码访问", description = "验证二维码有效性并授权访问")
    public ResponseDTO<MobileBiometricVO> verifyQRCode(@Valid @RequestBody MobileQRCodeForm params) {
        log.info("[门禁移动端] 验证二维码: areaId={}, sessionId={}",
            params.getAreaId(), params.getSessionId());

        try {
            String sessionId = params.getSessionId();

            // 1. 从缓存获取会话信息
            String cacheKey = "qrcode:session:" + sessionId;
            QRCodeSession session = (QRCodeSession) redisTemplate.opsForValue().get(cacheKey);

            if (session == null) {
                log.warn("[门禁移动端] 二维码会话不存在: sessionId={}", sessionId);
                throw new BusinessException("QRCODE_INVALID", "二维码不存在或已过期");
            }

            // 2. 检查会话状态
            if ("used".equals(session.getStatus()) || "expired".equals(session.getStatus())) {
                log.warn("[门禁移动端] 二维码已使用或过期: sessionId={}, status={}", sessionId, session.getStatus());
                throw new BusinessException("QRCODE_USED", "二维码已使用或已过期");
            }

            // 3. 检查区域ID匹配
            if (!session.getAreaId().equals(params.getAreaId())) {
                log.warn("[门禁移动端] 区域ID不匹配: sessionId={}, sessionAreaId={}, requestAreaId={}",
                    sessionId, session.getAreaId(), params.getAreaId());
                throw new BusinessException("AREA_MISMATCH", "区域ID不匹配");
            }

            // 4. 验证用户权限（如果是用户二维码）
            boolean hasAccess = true;
            if (session.getUserId() != null) {
                hasAccess = checkUserAreaAccess(session.getUserId(), session.getAreaId());
            } else if (session.getVisitorId() != null) {
                // 访客权限检查
                hasAccess = checkVisitorAreaAccess(session.getVisitorId(), session.getAreaId());
            }

            // 5. 更新会话状态为已使用
            session.setStatus("used");
            redisTemplate.opsForValue().set(cacheKey, session, 1, java.util.concurrent.TimeUnit.MINUTES);

            // 6. 构建验证结果
            var builder = MobileBiometricVO.builder()
                .result(hasAccess ? "success" : "failed")
                .matched(hasAccess)
                .hasAccess(hasAccess)
                .areaId(session.getAreaId())
                .verifyTime(java.time.LocalDateTime.now())
                .biometricType("qrcode")
                .message(hasAccess ? "二维码验证通过" : "无访问权限");

            if (session.getUserId() != null) {
                MobileAuthInitVO.UserInfo userInfo = getUserInfo(session.getUserId());
                builder.userId(userInfo.getUserId())
                    .username(userInfo.getUsername())
                    .confidence(1.0); // 二维码验证置信度为1.0
            }

            MobileBiometricVO resultVO = builder.build();

            log.info("[门禁移动端] 二维码验证完成: sessionId={}, hasAccess={}", sessionId, hasAccess);

            return ResponseDTO.ok(resultVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 二维码验证失败: areaId={}, error={}", params.getAreaId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 验证二维码异常: areaId={}, error={}", params.getAreaId(), e.getMessage(), e);
            throw new BusinessException("QRCODE_VERIFY_ERROR", "二维码验证失败: " + e.getMessage());
        }
    }

    /**
     * 生物识别验证
     *
     * @param params 生物识别参数
     * @return 验证结果
     */
    @PostMapping("/biometric/verify")
    @Operation(summary = "生物识别验证", description = "使用人脸、指纹等生物特征进行身份验证")
    public ResponseDTO<MobileBiometricVO> verifyBiometric(@Valid @RequestBody MobileBiometricForm params) {
        log.info("[门禁移动端] 生物识别验证: userId={}, deviceId={}, areaId={}, biometricType={}",
            params.getUserId(), params.getDeviceId(), params.getAreaId(), params.getBiometricType());

        try {
            // 1. 参数校验
            if (params.getUserId() == null) {
                throw new BusinessException("USER_ID_REQUIRED", "用户ID不能为空");
            }

            // 2. 检查用户区域权限
            boolean hasAccess = checkUserAreaAccess(params.getUserId(), params.getAreaId());

            // 3. 如果有生物特征数据，调用生物识别服务进行验证
            boolean matched = true;
            Double confidence = 1.0;

            if (TypeUtils.hasText(params.getFeatureVector())) {
                // 调用生物识别服务验证特征向量
                BiometricVerificationResult verificationResult = verifyBiometricFeature(
                    params.getUserId(),
                    params.getBiometricType(),
                    params.getFeatureVector(),
                    params.getConfidenceThreshold()
                );

                matched = verificationResult.isMatched();
                confidence = verificationResult.getConfidence();
            }

            // 4. 综合判断验证结果
            boolean finalResult = hasAccess && matched;

            // 5. 获取用户信息
            MobileAuthInitVO.UserInfo userInfo = getUserInfo(params.getUserId());

            // 6. 构建验证结果
            MobileBiometricVO resultVO = MobileBiometricVO.builder()
                .result(finalResult ? "success" : "failed")
                .userId(userInfo.getUserId())
                .username(userInfo.getUsername())
                .confidence(confidence)
                .matched(matched)
                .hasAccess(hasAccess && matched)
                .areaId(params.getAreaId())
                .verifyTime(java.time.LocalDateTime.now())
                .message(finalResult ? "生物识别验证通过" : (matched ? "无访问权限" : "生物识别失败"))
                .biometricType(params.getBiometricType())
                .build();

            log.info("[门禁移动端] 生物识别验证完成: userId={}, biometricType={}, matched={}, hasAccess={}, result={}",
                params.getUserId(), params.getBiometricType(), matched, hasAccess, finalResult);

            return ResponseDTO.ok(resultVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 生物识别验证失败: userId={}, error={}", params.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 生物识别验证异常: userId={}, error={}", params.getUserId(), e.getMessage(), e);
            throw new BusinessException("BIOMETRIC_VERIFY_ERROR", "生物识别验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备信息", description = "根据设备ID获取设备详细信息")
    public ResponseDTO<MobileDeviceInfoVO> getDeviceInfo(
            @Parameter(description = "设备ID", required = true) @PathVariable String deviceId) {
        log.info("[门禁移动端] 获取设备信息: deviceId={}", deviceId);

        try {
            // 1. 通过GatewayClient调用设备通讯服务获取设备信息
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            requestMap.put("deviceId", deviceId);

            ResponseDTO<java.util.Map<String, Object>> response = gatewayServiceClient.callDeviceCommService(
                "/api/device/info",
                org.springframework.http.HttpMethod.POST,
                requestMap,
                new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<java.util.Map<String, Object>>>() {}
            );

            if (response == null || !response.getCode().equals(200)) {
                log.warn("[门禁移动端] 设备不存在: deviceId={}", deviceId);
                throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在");
            }

            java.util.Map<String, Object> deviceData = response.getData();

            // 2. 构建设备信息VO
            MobileDeviceInfoVO deviceInfoVO = MobileDeviceInfoVO.builder()
                .deviceId((String) deviceData.get("deviceId"))
                .deviceName((String) deviceData.get("deviceName"))
                .deviceType((String) deviceData.get("deviceType"))
                .deviceSubType(TypeUtils.parseInt(String.valueOf(deviceData.get("deviceSubType")), 0))
                .deviceStatus((String) deviceData.get("deviceStatus"))
                .areaId(TypeUtils.parseLong(String.valueOf(deviceData.get("areaId"))))
                .areaName((String) deviceData.get("areaName"))
                .ipAddress((String) deviceData.get("ipAddress"))
                .port(TypeUtils.parseInt(String.valueOf(deviceData.get("port")), 0))
                .firmwareVersion((String) deviceData.get("firmwareVersion"))
                .lastCommunicateTime(parseDateTime(deviceData.get("lastCommunicateTime")))
                .extendedAttributes(parseExtendedAttributes(deviceData.get("extendedAttributes")))
                .build();

            log.info("[门禁移动端] 设备信息获取成功: deviceId={}, deviceName={}, status={}",
                deviceId, deviceInfoVO.getDeviceName(), deviceInfoVO.getDeviceStatus());

            return ResponseDTO.ok(deviceInfoVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 获取设备信息失败: deviceId={}, error={}", deviceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 获取设备信息异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new BusinessException("DEVICE_INFO_ERROR", "获取设备信息失败: " + e.getMessage());
        }
    }

    /**
     * 发送设备心跳
     *
     * @param params 心跳参数
     * @return 心跳结果
     */
    @PostMapping("/heartbeat")
    @Operation(summary = "发送设备心跳", description = "移动端定期发送心跳保持连接")
    public ResponseDTO<MobileHeartbeatVO> sendHeartbeat(@Valid @RequestBody MobileHeartbeatForm params) {
        log.info("[门禁移动端] 发送心跳: deviceId={}, status={}, batteryLevel={}",
            params.getDeviceId(), params.getDeviceStatus(), params.getBatteryLevel());

        try {
            // 1. 更新设备在线状态和心跳时间
            String cacheKey = "mobile:device:" + params.getDeviceId();
            DeviceHeartbeatInfo heartbeatInfo = DeviceHeartbeatInfo.builder()
                .deviceId(params.getDeviceId())
                .status(params.getDeviceStatus())
                .batteryLevel(params.getBatteryLevel())
                .networkType(params.getNetworkType())
                .signalStrength(params.getSignalStrength())
                .appVersion(params.getAppVersion())
                .location(params.getLocation())
                .lastHeartbeatTime(java.time.LocalDateTime.now())
                .build();

            // 缓存心跳信息（1小时过期）
            redisTemplate.opsForValue().set(cacheKey, heartbeatInfo, 1, java.util.concurrent.TimeUnit.HOURS);

            // 2. 更新设备在线状态集合
            redisTemplate.opsForSet().add("mobile:devices:online", params.getDeviceId());

            // 3. 检查应用更新
            boolean needUpdate = checkAppUpdate(params.getAppVersion());
            String latestVersion = null;
            String updateUrl = null;

            if (needUpdate) {
                latestVersion = getLatestAppVersion();
                updateUrl = getAppUpdateUrl();
            }

            // 4. 检查维护模式
            boolean maintenanceMode = checkMaintenanceMode();
            String maintenanceMessage = maintenanceMode ? getMaintenanceMessage() : null;

            // 5. 构建心跳响应
            MobileHeartbeatVO heartbeatVO = MobileHeartbeatVO.builder()
                .deviceId(params.getDeviceId())
                .serverTimestamp(System.currentTimeMillis())
                .status("success")
                .message("心跳接收成功")
                .nextHeartbeatInterval(30) // 下次心跳间隔30秒
                .needUpdate(needUpdate)
                .latestVersion(latestVersion)
                .updateUrl(updateUrl)
                .maintenanceMode(maintenanceMode)
                .maintenanceMessage(maintenanceMessage)
                .recordTime(java.time.LocalDateTime.now())
                .build();

            log.debug("[门禁移动端] 心跳处理成功: deviceId={}, nextInterval=30s", params.getDeviceId());

            return ResponseDTO.ok(heartbeatVO);

        } catch (BusinessException e) {
            log.warn("[门禁移动端] 心跳处理失败: deviceId={}, error={}", params.getDeviceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[门禁移动端] 发送心跳异常: deviceId={}, error={}", params.getDeviceId(), e.getMessage(), e);
            throw new BusinessException("HEARTBEAT_ERROR", "心跳处理失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证设备指纹
     */
    private String validateDeviceFingerprint(MobileAuthInitForm params) {
        String deviceId = params.getDeviceId();

        // 1. 检查设备是否已注册（根据业务需求）
        // String cacheKey = "mobile:device:fingerprint:" + deviceId;
        // String cachedFingerprint = (String) redisTemplate.opsForValue().get(cacheKey);
        // if (cachedFingerprint != null && !cachedFingerprint.equals(params.getDeviceFingerprint())) {
        //     throw new BusinessException("DEVICE_FINGERPRINT_MISMATCH", "设备指纹不匹配");
        // }

        // 2. 验证设备类型
        if (!"android".equalsIgnoreCase(params.getDeviceType())
            && !"ios".equalsIgnoreCase(params.getDeviceType())) {
            throw new BusinessException("DEVICE_TYPE_INVALID", "不支持的设备类型");
        }

        return deviceId;
    }

    /**
     * 获取服务器公钥（用于设备验证）
     */
    private String getServerPublicKey() {
        // TODO: 实现服务器公钥获取逻辑
        // 这里可以使用配置文件中的公钥，或者动态生成
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
    }

    /**
     * 生成挑战字符串（用于设备认证）
     */
    private String generateChallenge() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取用户信息
     */
    private MobileAuthInitVO.UserInfo getUserInfo(Long userId) {
        try {
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            requestMap.put("userId", userId);

            ResponseDTO<java.util.Map<String, Object>> response = gatewayServiceClient.callCommonService(
                "/api/user/info",
                org.springframework.http.HttpMethod.POST,
                requestMap,
                new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<java.util.Map<String, Object>>>() {}
            );

            if (response != null && response.getCode().equals(200) && response.getData() != null) {
                java.util.Map<String, Object> userData = response.getData();
                return MobileAuthInitVO.UserInfo.builder()
                    .userId(TypeUtils.parseLong(String.valueOf(userData.get("userId"))))
                    .username((String) userData.get("username"))
                    .realName((String) userData.get("realName"))
                    .phone((String) userData.get("phone"))
                    .email((String) userData.get("email"))
                    .build();
            }

            // 返回默认用户信息
            return MobileAuthInitVO.UserInfo.builder()
                .userId(userId)
                .username("User" + userId)
                .build();

        } catch (Exception e) {
            log.warn("[门禁移动端] 获取用户信息失败: userId={}, error={}", userId, e.getMessage());
            return MobileAuthInitVO.UserInfo.builder()
                .userId(userId)
                .username("User" + userId)
                .build();
        }
    }

    /**
     * 缓存设备信息
     */
    private void cacheDeviceInfo(String deviceId, MobileAuthInitForm params) {
        String cacheKey = "mobile:device:info:" + deviceId;
        redisTemplate.opsForValue().set(cacheKey, params, 24, java.util.concurrent.TimeUnit.HOURS);
    }

    /**
     * 获取缓存的设备ID
     */
    private String getCachedDeviceId(Long userId) {
        String cacheKey = "mobile:user:device:" + userId;
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 清除设备缓存
     */
    private void clearDeviceCache(Long userId) {
        String cacheKey = "mobile:user:device:" + userId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 检查令牌是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String cacheKey = "token:blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    /**
     * 将令牌加入黑名单
     */
    private void addToBlacklist(String token, Long ttl) {
        String cacheKey = "token:blacklist:" + token;
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(cacheKey, "blacklisted", ttl, java.util.concurrent.TimeUnit.SECONDS);
        } else {
            // 24小时 = 86400秒
            redisTemplate.opsForValue().set(cacheKey, "blacklisted", 86400, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    /**
     * 生成一次性令牌
     */
    private String generateOneTimeToken(String sessionId, long timestamp) {
        String data = sessionId + ":" + timestamp;
        return org.springframework.util.DigestUtils.md5DigestAsHex(data.getBytes());
    }

    /**
     * 生成二维码图片（Base64）
     */
    private String generateQRCodeImage(String content) {
        // TODO: 实现二维码图片生成逻辑
        // 可以使用ZXing库生成二维码图片，然后Base64编码
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
    }

    /**
     * 检查用户区域访问权限
     */
    private boolean checkUserAreaAccess(Long userId, Long areaId) {
        try {
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            requestMap.put("userId", userId);
            requestMap.put("areaId", areaId);

            ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(
                "/api/access/check",
                org.springframework.http.HttpMethod.POST,
                requestMap,
                new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<Boolean>>() {}
            );

            return response != null && response.getCode().equals(200)
                && Boolean.TRUE.equals(response.getData());

        } catch (Exception e) {
            log.warn("[门禁移动端] 检查用户区域权限失败: userId={}, areaId={}, error={}",
                userId, areaId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查访客区域访问权限
     */
    private boolean checkVisitorAreaAccess(Long visitorId, Long areaId) {
        try {
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            requestMap.put("visitorId", visitorId);
            requestMap.put("areaId", areaId);

            ResponseDTO<Boolean> response = gatewayServiceClient.callVisitorService(
                "/api/visitor/access/check",
                org.springframework.http.HttpMethod.POST,
                requestMap,
                new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<Boolean>>() {}
            );

            return response != null && response.getCode().equals(200)
                && Boolean.TRUE.equals(response.getData());

        } catch (Exception e) {
            log.warn("[门禁移动端] 检查访客区域权限失败: visitorId={}, areaId={}, error={}",
                visitorId, areaId, e.getMessage());
            return false;
        }
    }

    /**
     * 验证生物识别特征
     */
    private BiometricVerificationResult verifyBiometricFeature(Long userId, String biometricType,
                                                              String featureVector, Double confidenceThreshold) {
        try {
            // 调用生物识别服务
            java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
            requestMap.put("userId", userId);
            requestMap.put("biometricType", biometricType);
            requestMap.put("featureVector", featureVector);
            requestMap.put("confidenceThreshold", confidenceThreshold);

            ResponseDTO<java.util.Map<String, Object>> response = gatewayServiceClient.callDeviceCommService(
                "/api/biometric/verify",
                org.springframework.http.HttpMethod.POST,
                requestMap,
                new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<java.util.Map<String, Object>>>() {}
            );

            if (response != null && response.getCode().equals(200) && response.getData() != null) {
                java.util.Map<String, Object> resultData = response.getData();
                // 安全地转换confidence为Double
                Object confidenceObj = resultData.get("confidence");
                double confidence = 0.0;
                if (confidenceObj instanceof Number) {
                    confidence = ((Number) confidenceObj).doubleValue();
                }
                return BiometricVerificationResult.builder()
                    .matched(Boolean.TRUE.equals(resultData.get("matched")))
                    .confidence(confidence)
                    .build();
            }

            return BiometricVerificationResult.builder()
                .matched(false)
                .confidence(0.0)
                .build();

        } catch (Exception e) {
            log.warn("[门禁移动端] 生物识别验证失败: userId={}, biometricType={}, error={}",
                userId, biometricType, e.getMessage());
            return BiometricVerificationResult.builder()
                .matched(false)
                .confidence(0.0)
                .build();
        }
    }

    /**
     * 解析日期时间
     */
    private java.time.LocalDateTime parseDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return null;
        }
        if (dateTimeObj instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) dateTimeObj;
        }
        if (dateTimeObj instanceof String) {
            return java.time.LocalDateTime.parse((String) dateTimeObj);
        }
        if (dateTimeObj instanceof Long) {
            return java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli((Long) dateTimeObj),
                java.time.ZoneId.systemDefault()
            );
        }
        return null;
    }

    /**
     * 解析扩展属性（避免unchecked cast警告）
     */
    private java.util.Map<String, Object> parseExtendedAttributes(Object extendedAttrObj) {
        if (extendedAttrObj == null) {
            return new java.util.HashMap<>();
        }

        if (extendedAttrObj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> result = (java.util.Map<String, Object>) extendedAttrObj;
            return result;
        }

        log.warn("[门禁移动端] 扩展属性格式错误，应为Map类型: type={}",
            extendedAttrObj.getClass().getName());
        return new java.util.HashMap<>();
    }

    /**
     * 检查应用更新
     */
    private boolean checkAppUpdate(String currentVersion) {
        // TODO: 实现应用版本检查逻辑
        return false;
    }

    /**
     * 获取最新应用版本
     */
    private String getLatestAppVersion() {
        // TODO: 从配置或数据库获取最新版本
        return "1.0.1";
    }

    /**
     * 获取应用更新URL
     */
    private String getAppUpdateUrl() {
        // TODO: 从配置获取更新URL
        return "https://example.com/app-update.apk";
    }

    /**
     * 检查维护模式
     */
    private boolean checkMaintenanceMode() {
        String cacheKey = "system:maintenance:mode";
        return Boolean.TRUE.equals(redisTemplate.opsForValue().get(cacheKey));
    }

    /**
     * 获取维护消息
     */
    private String getMaintenanceMessage() {
        String cacheKey = "system:maintenance:message";
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }

    // ==================== 内部类 ====================

    /**
     * 二维码会话
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class QRCodeSession {
        private String sessionId;
        private Long timestamp;
        private String token;
        private Long areaId;
        private Long userId;
        private Long visitorId;
        private String qrCodeType;
        private java.time.LocalDateTime expireTime;
        private String status;
    }

    /**
     * 设备心跳信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DeviceHeartbeatInfo {
        private String deviceId;
        private String status;
        private Integer batteryLevel;
        private String networkType;
        private String signalStrength;
        private String appVersion;
        private MobileHeartbeatForm.LocationInfo location;
        private java.time.LocalDateTime lastHeartbeatTime;
    }

    /**
     * 生物识别验证结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class BiometricVerificationResult {
        private boolean matched;
        private Double confidence;
    }
}
