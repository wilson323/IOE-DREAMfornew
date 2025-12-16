package net.lab1024.sa.devicecomm.integration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.devicecomm.biometric.BiometricDataManager;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.service.BiometricService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 生物识别集成服务
 * <p>
 * 提供生物识别功能与其他微服务的集成接口：
 * - 与门禁服务集成
 * - 与考勤服务集成
 * - 与访客服务集成
 * - 与消费服务集成
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricIntegrationService {

    @Resource
    private BiometricService biometricService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 门禁生物识别验证
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @return 验证结果
     */
    public ResponseDTO<BiometricAccessResult> verifyAccessBiometric(Long userId, Long deviceId, Long areaId,
                                                                  VerifyTypeEnum verifyType, byte[] featureData) {
        try {
            log.info("[门禁生物识别验证] 开始: 用户={}, 设备={}, 区域={}, 验证方式={}",
                    userId, deviceId, areaId, verifyType.getName());

            // 1. 生物识别验证
            ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyResult =
                    biometricService.verifyBiometric(userId, verifyType, featureData);

            if (!verifyResult.getOk() || verifyResult.getData() == null) {
                return ResponseDTO.error("BIOMETRIC_VERIFY_FAILED", "生物识别验证失败");
            }

            BiometricDataManager.BiometricMatchResult matchResult = verifyResult.getData();
            if (!matchResult.isMatched()) {
                return ResponseDTO.error("BIOMETRIC_NOT_MATCHED", "生物特征不匹配");
            }

            // 2. 验证门禁权限
            ResponseDTO<Boolean> accessPermission = verifyAccessPermission(userId, areaId, deviceId);
            if (!accessPermission.getOk() || !accessPermission.getData()) {
                return ResponseDTO.error("ACCESS_PERMISSION_DENIED", "门禁权限不足");
            }

            // 3. 记录门禁通行记录
            ResponseDTO<Void> recordResult = recordAccessEvent(userId, deviceId, areaId, verifyType, matchResult);
            if (!recordResult.getOk()) {
                log.warn("[门禁生物识别验证] 记录通行事件失败: {}", recordResult.getMessage());
            }

            // 4. 构建结果
            BiometricAccessResult result = new BiometricAccessResult();
            result.setUserId(userId);
            result.setDeviceId(deviceId);
            result.setAreaId(areaId);
            result.setVerifyType(verifyType);
            result.setMatchResult(matchResult);
            result.setAccessGranted(true);
            result.setAccessTime(System.currentTimeMillis());
            result.setMessage("门禁验证成功");

            log.info("[门禁生物识别验证] 成功: 用户={}, 相似度={}", userId, matchResult.getSimilarity());

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁生物识别验证] 参数错误: 用户={}, 设备={}, error={}", userId, deviceId, e.getMessage());
            return ResponseDTO.error("ACCESS_BIOMETRIC_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁生物识别验证] 业务异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁生物识别验证] 系统异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ACCESS_BIOMETRIC_SYSTEM_ERROR", "门禁生物识别验证异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁生物识别验证] 未知异常: 用户={}, 设备={}", userId, deviceId, e);
            return ResponseDTO.error("ACCESS_BIOMETRIC_SYSTEM_ERROR", "门禁生物识别验证异常：" + e.getMessage());
        }
    }

    /**
     * 考勤生物识别打卡
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @param punchType 打卡类型（上班/下班）
     * @return 打卡结果
     */
    public ResponseDTO<BiometricAttendanceResult> punchAttendanceBiometric(Long userId, Long deviceId,
                                                                           VerifyTypeEnum verifyType, byte[] featureData,
                                                                           String punchType) {
        try {
            log.info("[考勤生物识别打卡] 开始: 用户={}, 设备={}, 验证方式={}, 打卡类型={}",
                    userId, deviceId, verifyType.getName(), punchType);

            // 1. 生物识别验证
            ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyResult =
                    biometricService.verifyBiometric(userId, verifyType, featureData);

            if (!verifyResult.getOk() || verifyResult.getData() == null) {
                return ResponseDTO.error("BIOMETRIC_VERIFY_FAILED", "生物识别验证失败");
            }

            BiometricDataManager.BiometricMatchResult matchResult = verifyResult.getData();
            if (!matchResult.isMatched()) {
                return ResponseDTO.error("BIOMETRIC_NOT_MATCHED", "生物特征不匹配");
            }

            // 2. 执行考勤打卡
            ResponseDTO<Object> punchResult = executeAttendancePunch(userId, deviceId, verifyType, punchType);
            if (!punchResult.getOk()) {
                return ResponseDTO.error("ATTENDANCE_PUNCH_FAILED", "考勤打卡失败: " + punchResult.getMessage());
            }

            // 3. 构建结果
            BiometricAttendanceResult result = new BiometricAttendanceResult();
            result.setUserId(userId);
            result.setDeviceId(deviceId);
            result.setVerifyType(verifyType);
            result.setMatchResult(matchResult);
            result.setPunchType(punchType);
            result.setPunchTime(System.currentTimeMillis());
            result.setSuccess(true);
            result.setMessage("考勤打卡成功");
            result.setAttendanceData(punchResult.getData());

            log.info("[考勤生物识别打卡] 成功: 用户={}, 打卡类型={}", userId, punchType);

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[考勤生物识别打卡] 参数错误: 用户={}, 设备={}, error={}", userId, deviceId, e.getMessage());
            return ResponseDTO.error("ATTENDANCE_BIOMETRIC_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[考勤生物识别打卡] 业务异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[考勤生物识别打卡] 系统异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("ATTENDANCE_BIOMETRIC_SYSTEM_ERROR", "考勤生物识别打卡异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[考勤生物识别打卡] 未知异常: 用户={}, 设备={}", userId, deviceId, e);
            return ResponseDTO.error("ATTENDANCE_BIOMETRIC_SYSTEM_ERROR", "考勤生物识别打卡异常：" + e.getMessage());
        }
    }

    /**
     * 访客生物识别验证
     *
     * @param visitorId 访客ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @return 验证结果
     */
    public ResponseDTO<BiometricVisitorResult> verifyVisitorBiometric(Long visitorId, Long deviceId, Long areaId,
                                                                    VerifyTypeEnum verifyType, byte[] featureData) {
        try {
            log.info("[访客生物识别验证] 开始: 访客={}, 设备={}, 区域={}, 验证方式={}",
                    visitorId, deviceId, areaId, verifyType.getName());

            // 1. 获取访客信息
            ResponseDTO<Map<String, Object>> visitorInfo = getVisitorInfo(visitorId);
            if (!visitorInfo.getOk() || visitorInfo.getData() == null) {
                return ResponseDTO.error("VISITOR_NOT_FOUND", "访客信息不存在");
            }

            Map<String, Object> visitorData = visitorInfo.getData();
            Long userId = (Long) visitorData.get("userId");
            String status = (String) visitorData.get("status");

            if (!"APPROVED".equals(status)) {
                return ResponseDTO.error("VISITOR_NOT_APPROVED", "访客未审批通过");
            }

            // 2. 生物识别验证
            ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyResult =
                    biometricService.verifyBiometric(userId, verifyType, featureData);

            if (!verifyResult.getOk() || verifyResult.getData() == null) {
                return ResponseDTO.error("BIOMETRIC_VERIFY_FAILED", "生物识别验证失败");
            }

            BiometricDataManager.BiometricMatchResult matchResult = verifyResult.getData();
            if (!matchResult.isMatched()) {
                return ResponseDTO.error("BIOMETRIC_NOT_MATCHED", "生物特征不匹配");
            }

            // 3. 验证访客权限
            ResponseDTO<Boolean> visitorPermission = verifyVisitorPermission(visitorId, areaId, deviceId);
            if (!visitorPermission.getOk() || !visitorPermission.getData()) {
                return ResponseDTO.error("VISITOR_PERMISSION_DENIED", "访客权限不足");
            }

            // 4. 记录访客通行记录
            ResponseDTO<Void> recordResult = recordVisitorEvent(visitorId, deviceId, areaId, verifyType, matchResult);
            if (!recordResult.getOk()) {
                log.warn("[访客生物识别验证] 记录通行事件失败: {}", recordResult.getMessage());
            }

            // 5. 构建结果
            BiometricVisitorResult result = new BiometricVisitorResult();
            result.setVisitorId(visitorId);
            result.setUserId(userId);
            result.setDeviceId(deviceId);
            result.setAreaId(areaId);
            result.setVerifyType(verifyType);
            result.setMatchResult(matchResult);
            result.setAccessGranted(true);
            result.setAccessTime(System.currentTimeMillis());
            result.setMessage("访客验证成功");
            result.setVisitorInfo(visitorData);

            log.info("[访客生物识别验证] 成功: 访客={}, 用户={}", visitorId, userId);

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[访客生物识别验证] 参数错误: 访客={}, 设备={}, error={}", visitorId, deviceId, e.getMessage());
            return ResponseDTO.error("VISITOR_BIOMETRIC_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客生物识别验证] 业务异常: 访客={}, 设备={}, code={}, message={}", visitorId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[访客生物识别验证] 系统异常: 访客={}, 设备={}, code={}, message={}", visitorId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("VISITOR_BIOMETRIC_SYSTEM_ERROR", "访客生物识别验证异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[访客生物识别验证] 未知异常: 访客={}, 设备={}", visitorId, deviceId, e);
            return ResponseDTO.error("VISITOR_BIOMETRIC_SYSTEM_ERROR", "访客生物识别验证异常：" + e.getMessage());
        }
    }

    /**
     * 消费生物识别验证
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param amount 消费金额
     * @param verifyType 验证类型
     * @param featureData 特征数据
     * @return 验证结果
     */
    public ResponseDTO<BiometricConsumeResult> verifyConsumeBiometric(Long userId, Long deviceId, Double amount,
                                                                     VerifyTypeEnum verifyType, byte[] featureData) {
        try {
            log.info("[消费生物识别验证] 开始: 用户={}, 设备={}, 金额={}, 验证方式={}",
                    userId, deviceId, amount, verifyType.getName());

            // 1. 生物识别验证
            ResponseDTO<BiometricDataManager.BiometricMatchResult> verifyResult =
                    biometricService.verifyBiometric(userId, verifyType, featureData);

            if (!verifyResult.getOk() || verifyResult.getData() == null) {
                return ResponseDTO.error("BIOMETRIC_VERIFY_FAILED", "生物识别验证失败");
            }

            BiometricDataManager.BiometricMatchResult matchResult = verifyResult.getData();
            if (!matchResult.isMatched()) {
                return ResponseDTO.error("BIOMETRIC_NOT_MATCHED", "生物特征不匹配");
            }

            // 2. 验证消费权限和余额
            ResponseDTO<Map<String, Object>> accountInfo = getUserAccountInfo(userId);
            if (!accountInfo.getOk() || accountInfo.getData() == null) {
                return ResponseDTO.error("ACCOUNT_NOT_FOUND", "账户信息不存在");
            }

            Map<String, Object> account = accountInfo.getData();
            Double balance = (Double) account.get("balance");
            String status = (String) account.get("status");

            if (!"ACTIVE".equals(status)) {
                return ResponseDTO.error("ACCOUNT_INACTIVE", "账户状态异常");
            }

            if (balance < amount) {
                return ResponseDTO.error("INSUFFICIENT_BALANCE", "余额不足");
            }

            // 3. 执行消费扣款
            ResponseDTO<Object> consumeResult = executeConsumePayment(userId, deviceId, amount, verifyType);
            if (!consumeResult.getOk()) {
                return ResponseDTO.error("CONSUME_PAYMENT_FAILED", "消费支付失败: " + consumeResult.getMessage());
            }

            // 4. 构建结果
            BiometricConsumeResult result = new BiometricConsumeResult();
            result.setUserId(userId);
            result.setDeviceId(deviceId);
            result.setAmount(amount);
            result.setVerifyType(verifyType);
            result.setMatchResult(matchResult);
            result.setPaymentSuccess(true);
            result.setBalanceBefore(balance);
            result.setBalanceAfter(balance - amount);
            result.setPaymentTime(System.currentTimeMillis());
            result.setMessage("消费支付成功");
            result.setPaymentData(consumeResult.getData());

            log.info("[消费生物识别验证] 成功: 用户={}, 金额={}, 余额={}", userId, amount, balance - amount);

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费生物识别验证] 参数错误: 用户={}, 设备={}, error={}", userId, deviceId, e.getMessage());
            return ResponseDTO.error("CONSUME_BIOMETRIC_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费生物识别验证] 业务异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费生物识别验证] 系统异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CONSUME_BIOMETRIC_SYSTEM_ERROR", "消费生物识别验证异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费生物识别验证] 未知异常: 用户={}, 设备={}", userId, deviceId, e);
            return ResponseDTO.error("CONSUME_BIOMETRIC_SYSTEM_ERROR", "消费生物识别验证异常：" + e.getMessage());
        }
    }

    /**
     * 批量注册用户生物识别
     *
     * @param userId 用户ID
     * @param biometricDataList 生物识别数据列表
     * @return 注册结果
     */
    public ResponseDTO<BiometricBatchRegisterResult> batchRegisterUserBiometric(Long userId,
                                                                               List<BiometricRegisterData> biometricDataList) {
        try {
            log.info("[批量注册生物识别] 开始: 用户={}, 数据数量={}", userId, biometricDataList.size());

            BiometricBatchRegisterResult result = new BiometricBatchRegisterResult();
            result.setUserId(userId);
            result.setTotalCount(biometricDataList.size());
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setDetails(new ArrayList<>());

            for (BiometricRegisterData data : biometricDataList) {
                try {
                    // 注册生物识别
                    ResponseDTO<Void> registerResult = biometricService.registerBiometric(
                            userId, data.getVerifyType(),
                            data.getFeatureData(), data.getTemplateData(),
                            data.getDeviceId());

                    BiometricRegisterDetail detail = new BiometricRegisterDetail();
                    detail.setVerifyType(data.getVerifyType());
                    detail.setDeviceId(data.getDeviceId());
                    detail.setSuccess(registerResult.getOk());
                    detail.setMessage(registerResult.getOk() ? "注册成功" : registerResult.getMessage());

                    result.getDetails().add(detail);

                    if (registerResult.getOk()) {
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    } else {
                        result.setFailCount(result.getFailCount() + 1);
                    }

                } catch (IllegalArgumentException | ParamException e) {
                    BiometricRegisterDetail detail = new BiometricRegisterDetail();
                    detail.setVerifyType(data.getVerifyType());
                    detail.setDeviceId(data.getDeviceId());
                    detail.setSuccess(false);
                    detail.setMessage("注册参数错误: " + e.getMessage());
                    result.getDetails().add(detail);
                    result.setFailCount(result.getFailCount() + 1);
                    log.warn("[批量注册生物识别] 单项参数错误: 用户={}, 验证方式={}, error={}",
                            userId, data.getVerifyType().getName(), e.getMessage());
                } catch (BusinessException e) {
                    BiometricRegisterDetail detail = new BiometricRegisterDetail();
                    detail.setVerifyType(data.getVerifyType());
                    detail.setDeviceId(data.getDeviceId());
                    detail.setSuccess(false);
                    detail.setMessage("注册业务异常: " + e.getMessage());
                    result.getDetails().add(detail);
                    result.setFailCount(result.getFailCount() + 1);
                    log.warn("[批量注册生物识别] 单项业务异常: 用户={}, 验证方式={}, code={}, message={}",
                            userId, data.getVerifyType().getName(), e.getCode(), e.getMessage());
                } catch (SystemException e) {
                    BiometricRegisterDetail detail = new BiometricRegisterDetail();
                    detail.setVerifyType(data.getVerifyType());
                    detail.setDeviceId(data.getDeviceId());
                    detail.setSuccess(false);
                    detail.setMessage("注册系统异常: " + e.getMessage());
                    result.getDetails().add(detail);
                    result.setFailCount(result.getFailCount() + 1);
                    log.error("[批量注册生物识别] 单项系统异常: 用户={}, 验证方式={}, code={}, message={}",
                            userId, data.getVerifyType().getName(), e.getCode(), e.getMessage(), e);
                } catch (Exception e) {
                    BiometricRegisterDetail detail = new BiometricRegisterDetail();
                    detail.setVerifyType(data.getVerifyType());
                    detail.setDeviceId(data.getDeviceId());
                    detail.setSuccess(false);
                    detail.setMessage("注册未知异常: " + e.getMessage());
                    result.getDetails().add(detail);
                    result.setFailCount(result.getFailCount() + 1);
                    log.error("[批量注册生物识别] 单项未知异常: 用户={}, 验证方式={}, error={}",
                            userId, data.getVerifyType().getName(), e.getMessage(), e);
                    log.error("[批量注册生物识别] 单项未知异常: 用户={}, 验证方式={}",
                            userId, data.getVerifyType().getName(), e);
                }
            }

            log.info("[批量注册生物识别] 完成: 用户={}, 成功={}, 失败={}",
                    userId, result.getSuccessCount(), result.getFailCount());

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[批量注册生物识别] 参数错误: 用户={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("BATCH_REGISTER_PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[批量注册生物识别] 业务异常: 用户={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[批量注册生物识别] 系统异常: 用户={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("BATCH_REGISTER_SYSTEM_ERROR", "批量注册异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[批量注册生物识别] 未知异常: 用户={}", userId, e);
            return ResponseDTO.error("BATCH_REGISTER_SYSTEM_ERROR", "批量注册异常：" + e.getMessage());
        }
    }

    /**
     * 异步批量注册用户生物识别
     *
     * @param userId 用户ID
     * @param biometricDataList 生物识别数据列表
     * @return 异步注册结果
     */
    public CompletableFuture<ResponseDTO<BiometricBatchRegisterResult>> batchRegisterUserBiometricAsync(
            Long userId, List<BiometricRegisterData> biometricDataList) {
        return CompletableFuture.supplyAsync(() -> {
            return batchRegisterUserBiometric(userId, biometricDataList);
        });
    }

    // ==================== 私有方法 ====================

    /**
     * 验证门禁权限
     */
    private ResponseDTO<Boolean> verifyAccessPermission(Long userId, Long areaId, Long deviceId) {
        try {
            return gatewayServiceClient.callAccessService(
                    "/api/v1/access/verify-permission",
                    org.springframework.http.HttpMethod.POST,
                    Map.of("userId", userId, "areaId", areaId, "deviceId", deviceId),
                    Boolean.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[验证门禁权限] 调用参数错误: 用户={}, 区域={}, error={}", userId, areaId, e.getMessage());
            return ResponseDTO.error("PERMISSION_CHECK_PARAM_ERROR", "权限验证参数错误");
        } catch (BusinessException e) {
            log.warn("[验证门禁权限] 调用业务异常: 用户={}, 区域={}, code={}, message={}", userId, areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[验证门禁权限] 调用系统异常: 用户={}, 区域={}, code={}, message={}", userId, areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PERMISSION_CHECK_ERROR", "权限验证失败");
        } catch (Exception e) {
            log.error("[验证门禁权限] 调用未知异常: 用户={}, 区域={}", userId, areaId, e);
            return ResponseDTO.error("PERMISSION_CHECK_ERROR", "权限验证失败");
        }
    }

    /**
     * 记录门禁通行事件
     */
    private ResponseDTO<Void> recordAccessEvent(Long userId, Long deviceId, Long areaId,
                                               VerifyTypeEnum verifyType,
                                               BiometricDataManager.BiometricMatchResult matchResult) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("userId", userId);
            eventData.put("deviceId", deviceId);
            eventData.put("areaId", areaId);
            eventData.put("verifyType", verifyType.getCode());
            eventData.put("similarity", matchResult.getSimilarity());
            eventData.put("accessTime", System.currentTimeMillis());

            return gatewayServiceClient.callAccessService(
                    "/api/v1/access/record-event",
                    org.springframework.http.HttpMethod.POST,
                    eventData,
                    Void.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[记录门禁事件] 调用参数错误: 用户={}, 设备={}, error={}", userId, deviceId, e.getMessage());
            return ResponseDTO.error("RECORD_EVENT_PARAM_ERROR", "记录事件参数错误");
        } catch (BusinessException e) {
            log.warn("[记录门禁事件] 调用业务异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[记录门禁事件] 调用系统异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("RECORD_EVENT_ERROR", "记录事件失败");
        } catch (Exception e) {
            log.error("[记录门禁事件] 调用未知异常: 用户={}, 设备={}", userId, deviceId, e);
            return ResponseDTO.error("RECORD_EVENT_ERROR", "记录事件失败");
        }
    }

    /**
     * 执行考勤打卡
     */
    private ResponseDTO<Object> executeAttendancePunch(Long userId, Long deviceId,
                                                      VerifyTypeEnum verifyType, String punchType) {
        try {
            Map<String, Object> punchData = new HashMap<>();
            punchData.put("userId", userId);
            punchData.put("deviceId", deviceId);
            punchData.put("verifyType", verifyType.getCode());
            punchData.put("punchType", punchType);
            punchData.put("punchTime", System.currentTimeMillis());

            return gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/punch",
                    org.springframework.http.HttpMethod.POST,
                    punchData,
                    Object.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[执行考勤打卡] 调用参数错误: 用户={}, 设备={}, error={}", userId, deviceId, e.getMessage());
            return ResponseDTO.error("PUNCH_PARAM_ERROR", "打卡参数错误");
        } catch (BusinessException e) {
            log.warn("[执行考勤打卡] 调用业务异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[执行考勤打卡] 调用系统异常: 用户={}, 设备={}, code={}, message={}", userId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PUNCH_ERROR", "打卡失败");
        } catch (Exception e) {
            log.error("[执行考勤打卡] 调用未知异常: 用户={}, 设备={}", userId, deviceId, e);
            return ResponseDTO.error("PUNCH_ERROR", "打卡失败");
        }
    }

    /**
     * 获取访客信息
     */
    private ResponseDTO<Map<String, Object>> getVisitorInfo(Long visitorId) {
        try {
            @SuppressWarnings("unchecked")
            Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>) (Class<?>) Map.class;
            return gatewayServiceClient.callVisitorService(
                    "/api/v1/visitor/" + visitorId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    mapClass);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取访客信息] 调用参数错误: 访客={}, error={}", visitorId, e.getMessage());
            return ResponseDTO.error("GET_VISITOR_PARAM_ERROR", "获取访客信息参数错误");
        } catch (BusinessException e) {
            log.warn("[获取访客信息] 调用业务异常: 访客={}, code={}, message={}", visitorId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[获取访客信息] 调用系统异常: 访客={}, code={}, message={}", visitorId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_VISITOR_ERROR", "获取访客信息失败");
        } catch (Exception e) {
            log.error("[获取访客信息] 调用未知异常: 访客={}", visitorId, e);
            return ResponseDTO.error("GET_VISITOR_ERROR", "获取访客信息失败");
        }
    }

    /**
     * 验证访客权限
     */
    private ResponseDTO<Boolean> verifyVisitorPermission(Long visitorId, Long areaId, Long deviceId) {
        try {
            Map<String, Object> permissionData = new HashMap<>();
            permissionData.put("visitorId", visitorId);
            permissionData.put("areaId", areaId);
            permissionData.put("deviceId", deviceId);

            return gatewayServiceClient.callVisitorService(
                    "/api/v1/visitor/verify-permission",
                    org.springframework.http.HttpMethod.POST,
                    permissionData,
                    Boolean.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[验证访客权限] 调用参数错误: 访客={}, 区域={}, error={}", visitorId, areaId, e.getMessage());
            return ResponseDTO.error("VISITOR_PERMISSION_PARAM_ERROR", "访客权限验证参数错误");
        } catch (BusinessException e) {
            log.warn("[验证访客权限] 调用业务异常: 访客={}, 区域={}, code={}, message={}", visitorId, areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[验证访客权限] 调用系统异常: 访客={}, 区域={}, code={}, message={}", visitorId, areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("VISITOR_PERMISSION_ERROR", "访客权限验证失败");
        } catch (Exception e) {
            log.error("[验证访客权限] 调用未知异常: 访客={}, 区域={}", visitorId, areaId, e);
            return ResponseDTO.error("VISITOR_PERMISSION_ERROR", "访客权限验证失败");
        }
    }

    /**
     * 记录访客通行事件
     */
    private ResponseDTO<Void> recordVisitorEvent(Long visitorId, Long deviceId, Long areaId,
                                                VerifyTypeEnum verifyType,
                                                BiometricDataManager.BiometricMatchResult matchResult) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("visitorId", visitorId);
            eventData.put("deviceId", deviceId);
            eventData.put("areaId", areaId);
            eventData.put("verifyType", verifyType.getCode());
            eventData.put("similarity", matchResult.getSimilarity());
            eventData.put("accessTime", System.currentTimeMillis());

            return gatewayServiceClient.callVisitorService(
                    "/api/v1/visitor/record-event",
                    org.springframework.http.HttpMethod.POST,
                    eventData,
                    Void.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[记录访客事件] 调用参数错误: 访客={}, 设备={}, error={}", visitorId, deviceId, e.getMessage());
            return ResponseDTO.error("RECORD_VISITOR_EVENT_PARAM_ERROR", "记录访客事件参数错误");
        } catch (BusinessException e) {
            log.warn("[记录访客事件] 调用业务异常: 访客={}, 设备={}, code={}, message={}", visitorId, deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[记录访客事件] 调用系统异常: 访客={}, 设备={}, code={}, message={}", visitorId, deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("RECORD_VISITOR_EVENT_ERROR", "记录访客事件失败");
        } catch (Exception e) {
            log.error("[记录访客事件] 调用未知异常: 访客={}, 设备={}", visitorId, deviceId, e);
            return ResponseDTO.error("RECORD_VISITOR_EVENT_ERROR", "记录访客事件失败");
        }
    }

    /**
     * 获取用户账户信息
     */
    private ResponseDTO<Map<String, Object>> getUserAccountInfo(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>) (Class<?>) Map.class;
            return gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/account/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    mapClass);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取账户信息] 调用参数错误: 用户={}, error={}", userId, e.getMessage());
            return ResponseDTO.error("GET_ACCOUNT_PARAM_ERROR", "获取账户信息参数错误");
        } catch (BusinessException e) {
            log.warn("[获取账户信息] 调用业务异常: 用户={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[获取账户信息] 调用系统异常: 用户={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_ACCOUNT_ERROR", "获取账户信息失败");
        } catch (Exception e) {
            log.error("[获取账户信息] 调用未知异常: 用户={}", userId, e);
            return ResponseDTO.error("GET_ACCOUNT_ERROR", "获取账户信息失败");
        }
    }

    /**
     * 执行消费支付
     */
    private ResponseDTO<Object> executeConsumePayment(Long userId, Long deviceId, Double amount,
                                                      VerifyTypeEnum verifyType) {
        try {
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("userId", userId);
            paymentData.put("deviceId", deviceId);
            paymentData.put("amount", amount);
            paymentData.put("verifyType", verifyType.getCode());
            paymentData.put("paymentTime", System.currentTimeMillis());

            return gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/payment",
                    org.springframework.http.HttpMethod.POST,
                    paymentData,
                    Object.class);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[执行消费支付] 调用参数错误: 用户={}, 金额={}, error={}", userId, amount, e.getMessage());
            return ResponseDTO.error("PAYMENT_PARAM_ERROR", "支付参数错误");
        } catch (BusinessException e) {
            log.warn("[执行消费支付] 调用业务异常: 用户={}, 金额={}, code={}, message={}", userId, amount, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[执行消费支付] 调用系统异常: 用户={}, 金额={}, code={}, message={}", userId, amount, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("PAYMENT_ERROR", "支付失败");
        } catch (Exception e) {
            log.error("[执行消费支付] 调用未知异常: 用户={}, 金额={}", userId, amount, e);
            return ResponseDTO.error("PAYMENT_ERROR", "支付失败");
        }
    }

    // ==================== 结果类 ====================

    /**
     * 门禁生物识别结果
     */
    public static class BiometricAccessResult {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private VerifyTypeEnum verifyType;
        private BiometricDataManager.BiometricMatchResult matchResult;
        private Boolean accessGranted;
        private Long accessTime;
        private String message;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public BiometricDataManager.BiometricMatchResult getMatchResult() { return matchResult; }
        public void setMatchResult(BiometricDataManager.BiometricMatchResult matchResult) { this.matchResult = matchResult; }

        public Boolean getAccessGranted() { return accessGranted; }
        public void setAccessGranted(Boolean accessGranted) { this.accessGranted = accessGranted; }

        public Long getAccessTime() { return accessTime; }
        public void setAccessTime(Long accessTime) { this.accessTime = accessTime; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 考勤生物识别结果
     */
    public static class BiometricAttendanceResult {
        private Long userId;
        private Long deviceId;
        private VerifyTypeEnum verifyType;
        private BiometricDataManager.BiometricMatchResult matchResult;
        private String punchType;
        private Long punchTime;
        private Boolean success;
        private String message;
        private Object attendanceData;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public BiometricDataManager.BiometricMatchResult getMatchResult() { return matchResult; }
        public void setMatchResult(BiometricDataManager.BiometricMatchResult matchResult) { this.matchResult = matchResult; }

        public String getPunchType() { return punchType; }
        public void setPunchType(String punchType) { this.punchType = punchType; }

        public Long getPunchTime() { return punchTime; }
        public void setPunchTime(Long punchTime) { this.punchTime = punchTime; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getAttendanceData() { return attendanceData; }
        public void setAttendanceData(Object attendanceData) { this.attendanceData = attendanceData; }
    }

    /**
     * 访客生物识别结果
     */
    public static class BiometricVisitorResult {
        private Long visitorId;
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private VerifyTypeEnum verifyType;
        private BiometricDataManager.BiometricMatchResult matchResult;
        private Boolean accessGranted;
        private Long accessTime;
        private String message;
        private Map<String, Object> visitorInfo;

        // Getters and Setters
        public Long getVisitorId() { return visitorId; }
        public void setVisitorId(Long visitorId) { this.visitorId = visitorId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public BiometricDataManager.BiometricMatchResult getMatchResult() { return matchResult; }
        public void setMatchResult(BiometricDataManager.BiometricMatchResult matchResult) { this.matchResult = matchResult; }

        public Boolean getAccessGranted() { return accessGranted; }
        public void setAccessGranted(Boolean accessGranted) { this.accessGranted = accessGranted; }

        public Long getAccessTime() { return accessTime; }
        public void setAccessTime(Long accessTime) { this.accessTime = accessTime; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Map<String, Object> getVisitorInfo() { return visitorInfo; }
        public void setVisitorInfo(Map<String, Object> visitorInfo) { this.visitorInfo = visitorInfo; }
    }

    /**
     * 消费生物识别结果
     */
    public static class BiometricConsumeResult {
        private Long userId;
        private Long deviceId;
        private Double amount;
        private VerifyTypeEnum verifyType;
        private BiometricDataManager.BiometricMatchResult matchResult;
        private Boolean paymentSuccess;
        private Double balanceBefore;
        private Double balanceAfter;
        private Long paymentTime;
        private String message;
        private Object paymentData;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public BiometricDataManager.BiometricMatchResult getMatchResult() { return matchResult; }
        public void setMatchResult(BiometricDataManager.BiometricMatchResult matchResult) { this.matchResult = matchResult; }

        public Boolean getPaymentSuccess() { return paymentSuccess; }
        public void setPaymentSuccess(Boolean paymentSuccess) { this.paymentSuccess = paymentSuccess; }

        public Double getBalanceBefore() { return balanceBefore; }
        public void setBalanceBefore(Double balanceBefore) { this.balanceBefore = balanceBefore; }

        public Double getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(Double balanceAfter) { this.balanceAfter = balanceAfter; }

        public Long getPaymentTime() { return paymentTime; }
        public void setPaymentTime(Long paymentTime) { this.paymentTime = paymentTime; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Object getPaymentData() { return paymentData; }
        public void setPaymentData(Object paymentData) { this.paymentData = paymentData; }
    }

    /**
     * 批量注册结果
     */
    public static class BiometricBatchRegisterResult {
        private Long userId;
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
        private List<BiometricRegisterDetail> details;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

        public Integer getFailCount() { return failCount; }
        public void setFailCount(Integer failCount) { this.failCount = failCount; }

        public List<BiometricRegisterDetail> getDetails() { return details; }
        public void setDetails(List<BiometricRegisterDetail> details) { this.details = details; }
    }

    /**
     * 注册详情
     */
    public static class BiometricRegisterDetail {
        private VerifyTypeEnum verifyType;
        private Long deviceId;
        private Boolean success;
        private String message;

        // Getters and Setters
        public VerifyTypeEnum getVerifyType() { return verifyType; }
        public void setVerifyType(VerifyTypeEnum verifyType) { this.verifyType = verifyType; }

        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 生物识别注册数据
     */
    public static class BiometricRegisterData {
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
