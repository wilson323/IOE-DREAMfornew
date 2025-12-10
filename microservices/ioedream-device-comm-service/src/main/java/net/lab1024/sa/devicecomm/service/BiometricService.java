package net.lab1024.sa.devicecomm.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.biometric.BiometricDataManager;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import net.lab1024.sa.devicecomm.protocol.handler.impl.BiometricProtocolHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 生物识别服务
 * <p>
 * 提供完整的生物识别功能API：
 * - 生物识别注册
 * - 生物识别验证
 * - 生物识别数据管理
 * - 生物识别设备管理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class BiometricService {

    @Resource
    private BiometricDataManager biometricDataManager;

    @Resource
    private BiometricProtocolHandler biometricProtocolHandler;

    // 异步处理线程池
    private final Executor asyncExecutor = Executors.newFixedThreadPool(10);

    /**
     * 注册生物识别特征
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param templateData 模板数据
     * @param deviceId 设备ID
     * @return 注册结果
     */
    public ResponseDTO<Void> registerBiometric(Long userId, VerifyTypeEnum verifyType,
                                             byte[] featureData, byte[] templateData, Long deviceId) {
        try {
            log.info("[生物识别注册] 开始: 用户={}, 验证方式={}, 设备={}",
                    userId, verifyType.getName(), deviceId);

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            if (verifyType == null || verifyType == VerifyTypeEnum.UNKNOWN) {
                return ResponseDTO.error("VERIFY_TYPE_INVALID", "验证类型无效");
            }

            if (featureData == null || featureData.length == 0) {
                return ResponseDTO.error("FEATURE_DATA_INVALID", "特征数据无效");
            }

            // 保存生物识别数据
            boolean success = biometricDataManager.saveBiometricData(userId, verifyType, featureData, templateData, deviceId);

            if (success) {
                log.info("[生物识别注册] 成功: 用户={}, 验证方式={}", userId, verifyType.getName());
                return ResponseDTO.ok();
            } else {
                log.error("[生物识别注册] 失败: 用户={}, 验证方式={}", userId, verifyType.getName());
                return ResponseDTO.error("REGISTER_FAILED", "注册失败");
            }

        } catch (Exception e) {
            log.error("[生物识别注册] 异常: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType != null ? verifyType.getName() : "null", e.getMessage(), e);
            return ResponseDTO.error("REGISTER_ERROR", "注册异常: " + e.getMessage());
        }
    }

    /**
     * 异步注册生物识别特征
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param templateData 模板数据
     * @param deviceId 设备ID
     * @return 异步注册结果
     */
    public CompletableFuture<ResponseDTO<Void>> registerBiometricAsync(Long userId, VerifyTypeEnum verifyType,
                                                                     byte[] featureData, byte[] templateData, Long deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            return registerBiometric(userId, verifyType, featureData, templateData, deviceId);
        }, asyncExecutor);
    }

    /**
     * 验证生物识别特征
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @return 验证结果
     */
    public ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyBiometric(Long userId, VerifyTypeEnum verifyType,
                                                                                 byte[] featureData) {
        try {
            log.info("[生物识别验证] 开始: 用户={}, 验证方式={}",
                    userId, verifyType.getName());

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            if (verifyType == null || verifyType == VerifyTypeEnum.UNKNOWN) {
                return ResponseDTO.error("VERIFY_TYPE_INVALID", "验证类型无效");
            }

            if (featureData == null || featureData.length == 0) {
                return ResponseDTO.error("FEATURE_DATA_INVALID", "特征数据无效");
            }

            // 执行匹配
            BiometricDataManager.BiometricMatchResult result = biometricDataManager.matchBiometricFeature(userId, verifyType, featureData);

            log.info("[生物识别验证] 完成: 用户={}, 验证方式={}, 结果={}, 相似度={}",
                    userId, verifyType.getName(), result.isMatched() ? "成功" : "失败", result.getSimilarity());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[生物识别验证] 异常: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType != null ? verifyType.getName() : "null", e.getMessage(), e);
            return ResponseDTO.error("VERIFY_ERROR", "验证异常: " + e.getMessage());
        }
    }

    /**
     * 查找最佳匹配用户
     *
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param candidateUserIds 候选用户ID列表
     * @return 匹配结果
     */
    public ResponseDTO<BiometricDataManager.BiometricMatchResult> findBestMatch(VerifyTypeEnum verifyType,
                                                                               byte[] featureData,
                                                                               List<Long> candidateUserIds) {
        try {
            log.info("[生物识别匹配] 开始: 验证方式={}, 候选用户数量={}",
                    verifyType.getName(), candidateUserIds != null ? candidateUserIds.size() : 0);

            // 验证参数
            if (verifyType == null || verifyType == VerifyTypeEnum.UNKNOWN) {
                return ResponseDTO.error("VERIFY_TYPE_INVALID", "验证类型无效");
            }

            if (featureData == null || featureData.length == 0) {
                return ResponseDTO.error("FEATURE_DATA_INVALID", "特征数据无效");
            }

            // 执行匹配
            BiometricDataManager.BiometricMatchResult result = biometricDataManager.findBestMatch(verifyType, featureData, candidateUserIds);

            if (result.isMatched()) {
                log.info("[生物识别匹配] 成功: 验证方式={}, 匹配用户={}, 相似度={}",
                        verifyType.getName(), result.getUserId(), result.getSimilarity());
            } else {
                log.info("[生物识别匹配] 失败: 验证方式={}, 消息={}",
                        verifyType.getName(), result.getMessage());
            }

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[生物识别匹配] 异常: 验证方式={}, 错误={}",
                    verifyType != null ? verifyType.getName() : "null", e.getMessage(), e);
            return ResponseDTO.error("MATCH_ERROR", "匹配异常: " + e.getMessage());
        }
    }

    /**
     * 删除生物识别数据
     *
     * @param userId 用户ID
     * @param verifyType 验证类型
     * @return 删除结果
     */
    public ResponseDTO<Void> deleteBiometricData(Long userId, VerifyTypeEnum verifyType) {
        try {
            log.info("[生物识别删除] 开始: 用户={}, 验证方式={}", userId, verifyType.getName());

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            if (verifyType == null || verifyType == VerifyTypeEnum.UNKNOWN) {
                return ResponseDTO.error("VERIFY_TYPE_INVALID", "验证类型无效");
            }

            // 删除数据
            boolean success = biometricDataManager.deleteBiometricData(userId, verifyType);

            if (success) {
                log.info("[生物识别删除] 成功: 用户={}, 验证方式={}", userId, verifyType.getName());
                return ResponseDTO.ok();
            } else {
                log.warn("[生物识别删除] 数据不存在: 用户={}, 验证方式={}", userId, verifyType.getName());
                return ResponseDTO.error("DATA_NOT_FOUND", "生物识别数据不存在");
            }

        } catch (Exception e) {
            log.error("[生物识别删除] 异常: 用户={}, 验证方式={}, 错误={}",
                    userId, verifyType != null ? verifyType.getName() : "null", e.getMessage(), e);
            return ResponseDTO.error("DELETE_ERROR", "删除异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户支持的验证方式
     *
     * @param userId 用户ID
     * @return 验证方式列表
     */
    public ResponseDTO<List<VerifyTypeEnum>> getSupportedVerifyTypes(Long userId) {
        try {
            log.debug("[获取验证方式] 开始: 用户={}", userId);

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            // 获取验证方式列表
            List<VerifyTypeEnum> verifyTypes = biometricDataManager.getSupportedVerifyTypes(userId);

            log.debug("[获取验证方式] 完成: 用户={}, 验证方式数量={}", userId, verifyTypes.size());

            return ResponseDTO.ok(verifyTypes);

        } catch (Exception e) {
            log.error("[获取验证方式] 异常: 用户={}, 错误={}", userId, e.getMessage(), e);
            return ResponseDTO.error("GET_VERIFY_TYPES_ERROR", "获取验证方式异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户所有生物识别数据
     *
     * @param userId 用户ID
     * @return 生物识别数据列表
     */
    public ResponseDTO<List<BiometricDataManager.BiometricData>> getUserBiometricData(Long userId) {
        try {
            log.debug("[获取生物数据] 开始: 用户={}", userId);

            // 验证参数
            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            // 获取生物识别数据
            List<BiometricDataManager.BiometricData> dataList = biometricDataManager.getAllBiometricData(userId);

            log.debug("[获取生物数据] 完成: 用户={}, 数据数量={}", userId, dataList.size());

            return ResponseDTO.ok(dataList);

        } catch (Exception e) {
            log.error("[获取生物数据] 异常: 用户={}, 错误={}", userId, e.getMessage(), e);
            return ResponseDTO.error("GET_BIOMETRIC_DATA_ERROR", "获取生物数据异常: " + e.getMessage());
        }
    }

    /**
     * 处理设备生物识别消息
     *
     * @param deviceId 设备ID
     * @param rawData 原始数据
     * @return 处理结果
     */
    public ResponseDTO<String> processDeviceBiometricMessage(Long deviceId, byte[] rawData) {
        try {
            log.info("[设备消息处理] 开始: 设备={}, 数据长度={}", deviceId, rawData.length);

            // 解析协议消息
            ProtocolMessage message = biometricProtocolHandler.parseMessage(rawData);

            // 验证消息
            if (!biometricProtocolHandler.validateMessage(message)) {
                return ResponseDTO.error("MESSAGE_INVALID", "消息验证失败");
            }

            // 处理消息
            biometricProtocolHandler.processMessage(message, deviceId);

            // 构建响应
            byte[] responseData = biometricProtocolHandler.buildResponse(message, true, "0", "处理成功");
            String response = new String(responseData);

            log.info("[设备消息处理] 完成: 设备={}, 响应长度={}", deviceId, response.length());

            return ResponseDTO.ok(response);

        } catch (Exception e) {
            log.error("[设备消息处理] 异常: 设备={}, 错误={}", deviceId, e.getMessage(), e);

            try {
                // 构建错误响应
                ProtocolMessage errorMessage = new ProtocolMessage();
                errorMessage.setDeviceId(deviceId);
                byte[] errorResponse = biometricProtocolHandler.buildResponse(errorMessage, false, "PROCESS_ERROR", e.getMessage());
                String response = new String(errorResponse);
                return ResponseDTO.error("PROCESS_ERROR", response);
            } catch (Exception ex) {
                return ResponseDTO.error("PROCESS_ERROR", "处理异常: " + e.getMessage());
            }
        }
    }

    /**
     * 清理过期数据
     *
     * @return 清理结果
     */
    public ResponseDTO<String> cleanExpiredData() {
        try {
            log.info("[清理过期数据] 开始");

            biometricDataManager.cleanExpiredData();

            // 获取统计信息
            BiometricDataManager.BiometricDataStatistics statistics = biometricDataManager.getStatistics();

            String result = String.format("清理完成，当前统计: 用户数=%d, 数据总数=%d, 缓存大小=%d",
                    statistics.getTotalUsers(), statistics.getTotalDataCount(), statistics.getCacheSize());

            log.info("[清理过期数据] 完成: {}", result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[清理过期数据] 异常: {}", e.getMessage(), e);
            return ResponseDTO.error("CLEAN_ERROR", "清理异常: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计信息
     *
     * @return 统计信息
     */
    public ResponseDTO<BiometricDataManager.BiometricDataStatistics> getStatistics() {
        try {
            log.debug("[获取统计信息] 开始");

            BiometricDataManager.BiometricDataStatistics statistics = biometricDataManager.getStatistics();

            log.debug("[获取统计信息] 完成: 用户数={}, 数据总数={}",
                    statistics.getTotalUsers(), statistics.getTotalDataCount());

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[获取统计信息] 异常: {}", e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计异常: " + e.getMessage());
        }
    }

    /**
     * 批量注册生物识别特征
     *
     * @param userId 用户ID
     * @param biometricDataList 生物识别数据列表
     * @return 批量注册结果
     */
    public ResponseDTO<Void> batchRegisterBiometric(Long userId, List<BiometricRegisterRequest> biometricDataList) {
        try {
            log.info("[批量注册] 开始: 用户={}, 数据数量={}", userId, biometricDataList.size());

            if (userId == null || userId <= 0) {
                return ResponseDTO.error("USER_ID_INVALID", "用户ID无效");
            }

            if (biometricDataList == null || biometricDataList.isEmpty()) {
                return ResponseDTO.error("DATA_LIST_EMPTY", "数据列表为空");
            }

            int successCount = 0;
            int failCount = 0;

            for (BiometricRegisterRequest request : biometricDataList) {
                try {
                    ResponseDTO<Void> result = registerBiometric(
                            userId, request.getVerifyType(),
                            request.getFeatureData(), request.getTemplateData(),
                            request.getDeviceId());

                    if (result.getOk()) {
                        successCount++;
                    } else {
                        failCount++;
                        log.warn("[批量注册] 注册失败: 用户={}, 验证方式={}, 错误={}",
                                userId, request.getVerifyType().getName(), result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("[批量注册] 注册异常: 用户={}, 验证方式={}, 错误={}",
                            userId, request.getVerifyType().getName(), e.getMessage(), e);
                }
            }

            log.info("[批量注册] 完成: 用户={}, 成功={}, 失败={}", userId, successCount, failCount);

            if (failCount == 0) {
                return ResponseDTO.ok();
            } else {
                return ResponseDTO.error("BATCH_PARTIAL_FAILED",
                        String.format("批量注册部分失败: 成功=%d, 失败=%d", successCount, failCount));
            }

        } catch (Exception e) {
            log.error("[批量注册] 异常: 用户={}, 错误={}", userId, e.getMessage(), e);
            return ResponseDTO.error("BATCH_REGISTER_ERROR", "批量注册异常: " + e.getMessage());
        }
    }

    /**
     * 处理生物识别验证结果 (供协议处理器调用)
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param verifyTypeEnum 验证类型
     * @param score 相似度分数
     * @param featureData 特征数据
     * @param verifyResult 验证结果 (1=成功, 0=失败)
     */
    public void processBiometricVerify(Long deviceId, Long userId, VerifyTypeEnum verifyTypeEnum,
                                           Double score, byte[] featureData, String verifyResult) {
        try {
            log.info("[生物识别验证处理] 设备={}, 用户={}, 验证方式={}, 分数={}, 结果={}",
                    deviceId, userId, verifyTypeEnum.getName(), score, verifyResult);

            // 记录验证日志
            // 这里可以扩展实现具体的业务逻辑，如：
            // 1. 记录访问日志
            // 2. 更新设备状态
            // 3. 发送通知
            // 4. 触发门禁控制

            boolean success = "1".equals(verifyResult);

            if (success) {
                log.info("[生物识别验证处理] 验证成功: 设备={}, 用户={}, 相似度={}", deviceId, userId, score);
            } else {
                log.warn("[生物识别验证处理] 验证失败: 设备={}, 用户={}, 原因={}", deviceId, userId, verifyResult);
            }

        } catch (Exception e) {
            log.error("[生物识别验证处理] 异常: 设备={}, 用户={}, 错误={}", deviceId, userId, e.getMessage(), e);
            // 验证处理异常不应影响主要流程
        }
    }

    /**
     * 处理生物识别注册 (供协议处理器调用)
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param verifyTypeEnum 验证类型
     * @param bioData 生物识别数据
     */
    public void processBiometricRegister(Long deviceId, Long userId, VerifyTypeEnum verifyTypeEnum, byte[] bioData) {
        try {
            log.info("[生物识别注册处理] 设备={}, 用户={}, 验证方式={}, 数据长度={}",
                    deviceId, userId, verifyTypeEnum.getName(), bioData != null ? bioData.length : 0);

            // 执行注册
            ResponseDTO<Void> result = registerBiometric(userId, verifyTypeEnum, bioData, null, deviceId);

            if (result.getOk()) {
                log.info("[生物识别注册处理] 注册成功: 设备={}, 用户={}", deviceId, userId);
            } else {
                log.error("[生物识别注册处理] 注册失败: 设备={}, 用户={}, 错误={}", deviceId, userId, result.getMessage());
            }

        } catch (Exception e) {
            log.error("[生物识别注册处理] 异常: 设备={}, 用户={}, 错误={}", deviceId, userId, e.getMessage(), e);
        }
    }

    /**
     * 处理生物识别删除 (供协议处理器调用)
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param verifyTypeEnum 验证类型
     */
    public void processBiometricDelete(Long deviceId, Long userId, VerifyTypeEnum verifyTypeEnum) {
        try {
            log.info("[生物识别删除处理] 设备={}, 用户={}, 验证方式={}", deviceId, userId, verifyTypeEnum.getName());

            // 执行删除
            ResponseDTO<Void> result = deleteBiometricData(userId, verifyTypeEnum);

            if (result.getOk()) {
                log.info("[生物识别删除处理] 删除成功: 设备={}, 用户={}", deviceId, userId);
            } else {
                log.warn("[生物识别删除处理] 删除失败: 设备={}, 用户={}, 错误={}", deviceId, userId, result.getMessage());
            }

        } catch (Exception e) {
            log.error("[生物识别删除处理] 异常: 设备={}, 用户={}, 错误={}", deviceId, userId, e.getMessage(), e);
        }
    }

    /**
     * 处理生物识别更新 (供协议处理器调用)
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param verifyTypeEnum 验证类型
     * @param bioData 生物识别数据
     */
    public void processBiometricUpdate(Long deviceId, Long userId, VerifyTypeEnum verifyTypeEnum, byte[] bioData) {
        try {
            log.info("[生物识别更新处理] 设备={}, 用户={}, 验证方式={}, 数据长度={}",
                    deviceId, userId, verifyTypeEnum.getName(), bioData != null ? bioData.length : 0);

            // 先删除旧数据，再注册新数据
            deleteBiometricData(userId, verifyTypeEnum);

            ResponseDTO<Void> result = registerBiometric(userId, verifyTypeEnum, bioData, null, deviceId);

            if (result.getOk()) {
                log.info("[生物识别更新处理] 更新成功: 设备={}, 用户={}", deviceId, userId);
            } else {
                log.error("[生物识别更新处理] 更新失败: 设备={}, 用户={}, 错误={}", deviceId, userId, result.getMessage());
            }

        } catch (Exception e) {
            log.error("[生物识别更新处理] 异常: 设备={}, 用户={}, 错误={}", deviceId, userId, e.getMessage(), e);
        }
    }

    /**
     * 生物识别注册请求类
     */
    public static class BiometricRegisterRequest {
        private VerifyTypeEnum verifyType;
        private byte[] featureData;
        private byte[] templateData;
        private Long deviceId;

        // Getters and Setters
        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public byte[] getFeatureData() { return featureData; }
        public void setFeatureData(byte[] featureData) { this.featureData = featureData; }

        public byte[] getTemplateData() { return templateData; }
        public void setTemplateData(byte[] templateData) { this.templateData = templateData; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    }
}