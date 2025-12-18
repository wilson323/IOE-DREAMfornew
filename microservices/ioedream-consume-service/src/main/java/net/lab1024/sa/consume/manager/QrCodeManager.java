package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.entity.QrCodeEntity;
import net.lab1024.sa.consume.dao.QrCodeDao;
import net.lab1024.sa.consume.domain.form.QrCodeGenerateForm;
import net.lab1024.sa.consume.domain.form.QrCodeConsumeForm;
import net.lab1024.sa.consume.domain.vo.QrCodeVO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

/**
 * 二维码管理器
 * <p>
 * 企业级二维码业务管理器，负责二维码的生成、解析、验证、消费等核心业务逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class QrCodeManager {

    private final QrCodeDao qrCodeDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final ObjectMapper objectMapper;

    // 构造函数注入依赖
    public QrCodeManager(QrCodeDao qrCodeDao, GatewayServiceClient gatewayServiceClient, ObjectMapper objectMapper) {
        this.qrCodeDao = qrCodeDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成二维码
     *
     * @param form 生成表单
     * @return 二维码实体
     */
    public QrCodeEntity generateQrCode(QrCodeGenerateForm form) {
        log.info("[二维码管理] 开始生成二维码: userId={}, type={}, module={}",
                form.getUserId(), form.getQrType(), form.getBusinessModule());

        try {
            // 1. 创建二维码实体
            QrCodeEntity qrCode = new QrCodeEntity();
            qrCode.setQrId("QR-" + UUID.randomUUID().toString().replace("-", ""));
            qrCode.setUserId(form.getUserId());
            qrCode.setQrType(form.getQrType());
            qrCode.setBusinessModule(form.getBusinessModule());

            // 2. 生成二维码标识和安全令牌
            String qrToken = generateQrToken(form);
            qrCode.setQrToken(qrToken);

            // 3. 设置时间信息
            LocalDateTime now = LocalDateTime.now();
            qrCode.setEffectiveTime(form.getEffectiveTime() != null ? form.getEffectiveTime() : now);
            qrCode.setExpireTime(form.getExpireTime());

            // 4. 设置使用限制
            qrCode.setUsageLimit(form.getUsageLimit() != null ? form.getUsageLimit() : 0);
            qrCode.setUsedCount(0);

            // 5. 设置状态和安全信息
            qrCode.setQrStatus(1); // 有效状态
            qrCode.setSecurityLevel(form.getSecurityLevel() != null ? form.getSecurityLevel() : 2);

            // 6. 设置区域和设备限制
            qrCode.setAreaId(form.getAreaId());
            qrCode.setDeviceId(form.getDeviceId());

            // 7. 设置业务特定限制
            qrCode.setAmountLimit(form.getAmountLimit());
            qrCode.setRequireBiometric(form.getRequireBiometric() != null ? form.getRequireBiometric() : 0);
            qrCode.setRequireLocation(form.getRequireLocation() != null ? form.getRequireLocation() : 0);

            // 8. 生成二维码内容
            Map<String, Object> qrContent = buildQrContent(qrCode, form);
            String qrContentJson = objectMapper.writeValueAsString(qrContent);
            qrCode.setQrContent(qrContentJson);

            // 9. 设置扩展属性
            qrCode.setExtendedAttributes(form.getExtendedAttributes());

            // 10. 设置其他信息
            qrCode.setCreateMethod(form.getCreateMethod() != null ? form.getCreateMethod() : 1);
            qrCode.setGenerateReason(form.getGenerateReason());

            // 11. 保存到数据库
            qrCodeDao.insert(qrCode);

            log.info("[二维码管理] 二维码生成成功: qrId={}, userId={}, token={}",
                    qrCode.getQrId(), qrCode.getUserId(), qrCode.getQrToken());

            return qrCode;

        } catch (Exception e) {
            log.error("[二维码管理] 二维码生成失败: userId={}, error={}", form.getUserId(), e.getMessage(), e);
            throw new SystemException("QR_CODE_GENERATE_ERROR", "二维码生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析并验证二维码
     *
     * @param qrContent 二维码内容
     * @return 解析结果
     */
    public Map<String, Object> parseAndValidateQrCode(String qrContent) {
        log.debug("[二维码管理] 解析二维码内容: length={}", qrContent != null ? qrContent.length() : 0);

        try {
            // 1. 尝试直接解析为JSON
            Map<String, Object> content = parseQrContent(qrContent);

            // 2. 获取二维码标识
            String qrToken = (String) content.get("qrToken");
            if (qrToken == null || qrToken.trim().isEmpty()) {
                throw new IllegalArgumentException("二维码标识无效");
            }

            // 3. 查询二维码记录
            QrCodeEntity qrCode = qrCodeDao.selectByToken(qrToken);
            if (qrCode == null) {
                throw new IllegalArgumentException("二维码不存在或已失效");
            }

            // 4. 验证二维码状态
            validateQrCodeStatus(qrCode);

            // 5. 验证时间和使用次数
            validateQrCodeTimeAndUsage(qrCode);

            // 6. 返回解析结果
            Map<String, Object> result = new HashMap<>();
            result.put("qrCode", qrCode);
            result.put("content", content);
            result.put("valid", true);

            log.debug("[二维码管理] 二维码验证成功: qrId={}, userId={}", qrCode.getQrId(), qrCode.getUserId());

            return result;

        } catch (Exception e) {
            log.error("[二维码管理] 二维码解析验证失败: error={}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 处理二维码消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    public Map<String, Object> processQrCodeConsume(QrCodeConsumeForm form) {
        log.info("[二维码管理] 开始处理二维码消费: device={}, amount={}",
                form.getDeviceId(), form.getConsumeAmount());

        try {
            // 1. 解析并验证二维码
            Map<String, Object> parseResult = parseAndValidateQrCode(form.getQrContent());
            Boolean valid = (Boolean) parseResult.get("valid");

            if (!valid) {
                throw new IllegalArgumentException("二维码验证失败: " + parseResult.get("error"));
            }

            QrCodeEntity qrCode = (QrCodeEntity) parseResult.get("qrCode");

            // 2. 验证业务权限
            validateConsumePermission(qrCode, form);

            // 3. 验证消费限制
            validateConsumeLimit(qrCode, form);

            // 4. 验证区域和设备权限
            validateAreaAndDevicePermission(qrCode, form);

            // 5. 处理消费逻辑
            Map<String, Object> consumeResult = processConsumeLogic(qrCode, form);

            // 6. 更新二维码使用记录
            updateQrCodeUsage(qrCode, form);

            // 7. 返回消费结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("qrId", qrCode.getQrId());
            result.put("userId", qrCode.getUserId());
            result.put("consumeAmount", form.getConsumeAmount());
            result.put("consumeResult", consumeResult);
            result.put("message", "消费成功");

            log.info("[二维码管理] 二维码消费成功: qrId={}, userId={}, amount={}",
                    qrCode.getQrId(), qrCode.getUserId(), form.getConsumeAmount());

            return result;

        } catch (Exception e) {
            log.error("[二维码管理] 二维码消费失败: device={}, amount={}, error={}",
                    form.getDeviceId(), form.getConsumeAmount(), e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 生成二维码图片
     *
     * @param qrContent 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return Base64编码的图片
     */
    public String generateQrImage(String qrContent, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("[二维码管理] 二维码图片生成失败: error={}", e.getMessage(), e);
            throw new SystemException("QR_CODE_IMAGE_GENERATE_ERROR", "二维码图片生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成二维码标识
     */
    private String generateQrToken(QrCodeGenerateForm form) {
        try {
            String rawToken = String.format("%d-%d-%s-%d",
                    form.getUserId(),
                    form.getQrType(),
                    form.getBusinessModule(),
                    System.currentTimeMillis());

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawToken.getBytes("UTF-8"));

            return Base64.getEncoder().encodeToString(hash).substring(0, 32);

        } catch (Exception e) {
            log.debug("[二维码管理] 生成二维码标识失败，使用UUID降级: error={}", e.getMessage());
            // 降级处理：使用UUID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 构建二维码内容
     */
    private Map<String, Object> buildQrContent(QrCodeEntity qrCode, QrCodeGenerateForm form) {
        Map<String, Object> content = new HashMap<>();

        // 基本信息
        content.put("qrId", qrCode.getQrId());
        content.put("qrToken", qrCode.getQrToken());
        content.put("userId", qrCode.getUserId());
        content.put("qrType", qrCode.getQrType());
        content.put("businessModule", qrCode.getBusinessModule());

        // 时间信息
        content.put("effectiveTime", qrCode.getEffectiveTime());
        content.put("expireTime", qrCode.getExpireTime());

        // 使用限制
        content.put("usageLimit", qrCode.getUsageLimit());
        content.put("amountLimit", qrCode.getAmountLimit());

        // 权限要求
        content.put("requireBiometric", qrCode.getRequireBiometric());
        content.put("requireLocation", qrCode.getRequireLocation());
        content.put("areaId", qrCode.getAreaId());
        content.put("deviceId", qrCode.getDeviceId());

        // 安全信息
        content.put("securityLevel", qrCode.getSecurityLevel());
        content.put("timestamp", System.currentTimeMillis());

        return content;
    }

    /**
     * 解析二维码内容
     */
    @SuppressWarnings("null")
    private Map<String, Object> parseQrContent(String qrContent) {
        try {
            // 尝试直接解析为JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(qrContent, Map.class);
            return result;
        } catch (Exception e) {
            log.warn("[二维码管理] 解析二维码内容失败: qrContent={}, error={}", qrContent, e.getMessage());
            // 如果不是JSON，可能是加密字符串，需要解密
            // 这里可以添加解密逻辑
            throw new IllegalArgumentException("二维码格式无效", e);
        }
    }

    /**
     * 验证二维码状态
     */
    private void validateQrCodeStatus(QrCodeEntity qrCode) {
        if (qrCode.getQrStatus() != 1) {
            String statusName = getQrStatusName(qrCode.getQrStatus());
            throw new IllegalArgumentException("二维码状态无效: " + statusName);
        }
    }

    /**
     * 验证二维码时间和使用次数
     */
    private void validateQrCodeTimeAndUsage(QrCodeEntity qrCode) {
        LocalDateTime now = LocalDateTime.now();

        // 验证时间有效性
        if (now.isBefore(qrCode.getEffectiveTime())) {
            throw new IllegalArgumentException("二维码尚未生效");
        }

        if (qrCode.getExpireTime() != null && now.isAfter(qrCode.getExpireTime())) {
            throw new IllegalArgumentException("二维码已过期");
        }

        // 验证使用次数
        if (qrCode.getUsageLimit() != null && qrCode.getUsageLimit() > 0 &&
            qrCode.getUsedCount() >= qrCode.getUsageLimit()) {
            throw new IllegalArgumentException("二维码使用次数已用完");
        }
    }

    /**
     * 验证消费权限
     */
    private void validateConsumePermission(QrCodeEntity qrCode, QrCodeConsumeForm form) {
        // 只允许消费类型的二维码进行消费
        if (!"consume".equals(qrCode.getBusinessModule())) {
            throw new IllegalArgumentException("该二维码不支持消费功能");
        }
    }

    /**
     * 验证消费限制
     */
    private void validateConsumeLimit(QrCodeEntity qrCode, QrCodeConsumeForm form) {
        if (qrCode.getAmountLimit() != null) {
            // 查询已消费金额
            BigDecimal usedAmount = getUsedAmount(qrCode.getQrId());
            BigDecimal remainingAmount = qrCode.getAmountLimit().subtract(usedAmount);

            if (form.getConsumeAmount().compareTo(remainingAmount) > 0) {
                throw new IllegalArgumentException("消费金额超出限制，剩余金额: " + remainingAmount);
            }
        }
    }

    /**
     * 验证区域和设备权限
     */
    private void validateAreaAndDevicePermission(QrCodeEntity qrCode, QrCodeConsumeForm form) {
        // 验证区域权限
        if (qrCode.getAreaId() != null && !qrCode.getAreaId().equals(form.getAreaId())) {
            throw new IllegalArgumentException("二维码区域权限不匹配");
        }

        // 验证设备权限
        if (qrCode.getDeviceId() != null && !qrCode.getDeviceId().equals(form.getDeviceId())) {
            throw new IllegalArgumentException("二维码设备权限不匹配");
        }
    }

    /**
     * 处理消费逻辑
     */
    private Map<String, Object> processConsumeLogic(QrCodeEntity qrCode, QrCodeConsumeForm form) {
        try {
            // 调用消费服务处理实际消费逻辑
            Map<String, Object> consumeRequest = new HashMap<>();
            consumeRequest.put("userId", qrCode.getUserId());
            consumeRequest.put("consumeAmount", form.getConsumeAmount());
            consumeRequest.put("consumeType", form.getConsumeType());
            consumeRequest.put("merchantId", form.getMerchantId());
            consumeRequest.put("merchantName", form.getMerchantName());
            consumeRequest.put("areaId", form.getAreaId());
            consumeRequest.put("deviceId", form.getDeviceId());
            consumeRequest.put("qrCodeId", qrCode.getQrId());
            consumeRequest.put("consumeDescription", form.getConsumeDescription());

            // 通过网关调用消费服务
            Object response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/process",
                    org.springframework.http.HttpMethod.POST,
                    consumeRequest,
                    Object.class
            ).getData();

            return Map.of("response", response, "message", "消费处理成功");

        } catch (Exception e) {
            log.error("[二维码管理] 消费逻辑处理失败: qrId={}, error={}", qrCode.getQrId(), e.getMessage(), e);
            throw new SystemException("QR_CODE_CONSUME_ERROR", "消费处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新二维码使用记录
     */
    private void updateQrCodeUsage(QrCodeEntity qrCode, QrCodeConsumeForm form) {
        try {
            qrCode.setUsedCount(qrCode.getUsedCount() + 1);
            qrCode.setLastUsedTime(LocalDateTime.now());
            qrCode.setLastUsedDevice(form.getDeviceId());
            qrCode.setLastUsedLocation(form.getLocationDescription());

            // 检查是否需要更新状态
            if (qrCode.getUsageLimit() != null && qrCode.getUsageLimit() > 0 &&
                qrCode.getUsedCount() >= qrCode.getUsageLimit()) {
                qrCode.setQrStatus(3); // 已用完
            }

            qrCodeDao.updateById(qrCode);

        } catch (Exception e) {
            log.error("[二维码管理] 更新二维码使用记录失败: qrId={}, error={}", qrCode.getQrId(), e.getMessage(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 获取已消费金额
     */
    private BigDecimal getUsedAmount(String qrCodeId) {
        try {
            // 查询该二维码已消费的金额
            Map<String, Object> request = Map.of("qrCodeId", qrCodeId);

            Object response = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/get-used-amount",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Map.class
            ).getData();

            if (response instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;
                Object amount = responseMap.get("usedAmount");
                if (amount instanceof Number) {
                    return new BigDecimal(amount.toString());
                }
            }

            return BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("[二维码管理] 查询已消费金额失败: qrId={}, error={}", qrCodeId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取二维码状态名称
     */
    private String getQrStatusName(Integer status) {
        switch (status) {
            case 1: return "有效";
            case 2: return "已过期";
            case 3: return "已用完";
            case 4: return "已撤销";
            case 5: return "暂停使用";
            default: return "未知状态";
        }
    }

    /**
     * 转换为VO
     */
    public QrCodeVO convertToVO(QrCodeEntity qrCode) {
        try {
            // 生成二维码图片
            String qrImageUrl = generateQrImage(qrCode.getQrContent(), 200, 200);

            // 计算剩余时间和次数
            LocalDateTime now = LocalDateTime.now();
            Long remainingTime = null;
            if (qrCode.getExpireTime() != null) {
                remainingTime = java.time.Duration.between(now, qrCode.getExpireTime()).getSeconds();
                if (remainingTime < 0) remainingTime = 0L;
            }

            Integer remainingUsage = null;
            if (qrCode.getUsageLimit() != null && qrCode.getUsageLimit() > 0) {
                remainingUsage = qrCode.getUsageLimit() - qrCode.getUsedCount();
                if (remainingUsage < 0) remainingUsage = 0;
            }

            // 计算使用进度
            Integer usageProgress = 0;
            if (qrCode.getUsageLimit() != null && qrCode.getUsageLimit() > 0) {
                usageProgress = (int) ((double) qrCode.getUsedCount() / qrCode.getUsageLimit() * 100);
            }

            // 获取用户信息
            String username = null;
            String nickname = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> userResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/user/" + qrCode.getUserId(),
                        org.springframework.http.HttpMethod.GET,
                        null,
                        Map.class
                ).getData();

                if (userResponse != null) {
                    Map<String, Object> userMap = userResponse;
                    username = (String) userMap.get("username");
                    nickname = (String) userMap.get("nickname");
                }
            } catch (Exception e) {
                log.debug("[二维码管理] 获取用户信息失败: userId={}", qrCode.getUserId());
            }

            return QrCodeVO.builder()
                    .qrId(qrCode.getQrId())
                    .userId(qrCode.getUserId())
                    .username(username)
                    .nickname(nickname)
                    .qrType(qrCode.getQrType())
                    .qrTypeName(getQrTypeName(qrCode.getQrType()))
                    .qrContent(qrCode.getQrContent())
                    .qrImageUrl("data:image/png;base64," + qrImageUrl)
                    .qrToken(qrCode.getQrToken())
                    .businessModule(qrCode.getBusinessModule())
                    .businessModuleName(getBusinessModuleName(qrCode.getBusinessModule()))
                    .effectiveTime(qrCode.getEffectiveTime())
                    .expireTime(qrCode.getExpireTime())
                    .remainingTime(remainingTime)
                    .usageLimit(qrCode.getUsageLimit())
                    .usedCount(qrCode.getUsedCount())
                    .remainingUsage(remainingUsage)
                    .qrStatus(qrCode.getQrStatus())
                    .qrStatusName(getQrStatusName(qrCode.getQrStatus()))
                    .securityLevel(qrCode.getSecurityLevel())
                    .securityLevelName(getSecurityLevelName(qrCode.getSecurityLevel()))
                    .areaId(qrCode.getAreaId())
                    .deviceId(qrCode.getDeviceId())
                    .amountLimit(qrCode.getAmountLimit())
                    .requireBiometric(qrCode.getRequireBiometric())
                    .biometricRequireDesc(getBiometricRequireDesc(qrCode.getRequireBiometric()))
                    .requireLocation(qrCode.getRequireLocation())
                    .lastUsedTime(qrCode.getLastUsedTime())
                    .lastUsedDevice(qrCode.getLastUsedDevice())
                    .lastUsedLocation(qrCode.getLastUsedLocation())
                    .createMethod(qrCode.getCreateMethod())
                    .createMethodName(getCreateMethodName(qrCode.getCreateMethod()))
                    .generateReason(qrCode.getGenerateReason())
                    .createTime(qrCode.getCreateTime())
                    .updateTime(qrCode.getUpdateTime())
                    .extendedAttributes(qrCode.getExtendedAttributes())
                    .immediatelyUsable(now.isAfter(qrCode.getEffectiveTime()) &&
                                      (qrCode.getExpireTime() == null || now.isBefore(qrCode.getExpireTime())))
                    .usageProgress(usageProgress)
                    .build();

        } catch (Exception e) {
            log.error("[二维码管理] 转换VO失败: qrId={}, error={}", qrCode.getQrId(), e.getMessage(), e);
            throw new SystemException("QR_CODE_VO_CONVERT_ERROR", "转换VO失败: " + e.getMessage(), e);
        }
    }

    private String getQrTypeName(Integer qrType) {
        switch (qrType) {
            case 1: return "消费码";
            case 2: return "门禁码";
            case 3: return "考勤码";
            case 4: return "访客码";
            case 5: return "通用码";
            default: return "未知类型";
        }
    }

    private String getBusinessModuleName(String businessModule) {
        switch (businessModule) {
            case "consume": return "消费";
            case "access": return "门禁";
            case "attendance": return "考勤";
            case "visitor": return "访客";
            default: return "其他";
        }
    }

    private String getSecurityLevelName(Integer securityLevel) {
        switch (securityLevel) {
            case 1: return "低";
            case 2: return "中";
            case 3: return "高";
            case 4: return "极高";
            default: return "未知";
        }
    }

    private String getBiometricRequireDesc(Integer requireBiometric) {
        switch (requireBiometric) {
            case 0: return "不需要";
            case 1: return "需要指纹";
            case 2: return "需要人脸";
            case 3: return "需要指纹+人脸";
            default: return "未知";
        }
    }

    private String getCreateMethodName(Integer createMethod) {
        switch (createMethod) {
            case 1: return "用户生成";
            case 2: return "系统生成";
            case 3: return "管理员生成";
            case 4: return "API生成";
            default: return "未知";
        }
    }
}




