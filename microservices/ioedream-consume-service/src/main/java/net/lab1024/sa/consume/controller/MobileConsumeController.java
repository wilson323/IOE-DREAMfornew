package net.lab1024.sa.consume.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.consume.domain.vo.MobileConsumeStatisticsVO;
import net.lab1024.sa.consume.consume.domain.vo.MobileAccountInfoVO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.consume.domain.dto.*;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.consume.service.ConsumeMobileService;
import net.lab1024.sa.consume.service.MobileConsumeStatisticsService;
import net.lab1024.sa.consume.service.MobileAccountInfoService;
import net.lab1024.sa.consume.util.PageResultConverter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 移动端消费控制器
 * 提供移动端专用的消费API接口，优化移动端用户体验
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/v1/consume")
@Tag(name = "移动端消费", description = "移动端消费相关接口")
@Validated
public class MobileConsumeController {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private MobileConsumeStatisticsService mobileConsumeStatisticsService;

    @Resource
    private MobileAccountInfoService mobileAccountInfoService;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ConsumeMobileService consumeMobileService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 移动端快捷消费
     * 简化版消费接口，适合移动端快捷操作
     */
    @PostMapping("/quick-consume")
    @Observed(name = "mobileConsume.quickConsume", contextualName = "mobile-consume-quick-consume")
    @Operation(summary = "快捷消费", description = "移动端快捷消费接口")
    public ResponseDTO<MobileConsumeResultVO> quickConsume(
            @RequestBody @Valid MobileQuickConsumeRequestDTO request) {
        log.info("[移动端快捷消费] orderId={}, amount={}, deviceId={}",
                request.getOrderId(), request.getAmount(), request.getDeviceId());

        try {
            // 转换移动端请求为标准请求
            ConsumeRequestDTO consumeRequest = convertToConsumeRequest(request);
            ResponseDTO<ConsumeTransactionResultVO> response = consumeService.consume(consumeRequest);

            if (!response.getOk()) {
                return ResponseDTO.error(response.getCode(), response.getMessage());
            }

            // 转换结果为移动端响应
            MobileConsumeResultVO mobileResult = convertTransactionResultToMobile(response.getData());

            return ResponseDTO.ok(mobileResult);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端快捷消费] 参数错误: orderId={}, error={}", request.getOrderId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端快捷消费] 业务异常: orderId={}, code={}, message={}", request.getOrderId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端快捷消费] 系统异常: orderId={}, code={}, message={}", request.getOrderId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_CONSUME_SYSTEM_ERROR", "消费处理异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端快捷消费] 未知异常: orderId={}", request.getOrderId(), e);
            return ResponseDTO.error("MOBILE_CONSUME_ERROR", "消费处理异常");
        }
    }

    /**
     * 获取移动端消费记录
     * 移动端优化的消费记录查询
     */
    @GetMapping("/records")
    @Observed(name = "mobileConsume.getConsumeRecords", contextualName = "mobile-consume-get-consume-records")
    @Operation(summary = "获取消费记录", description = "获取用户消费记录列表")
    public ResponseDTO<PageResult<MobileConsumeRecordVO>> getConsumeRecords(
            @Parameter(description = "页码", required = false) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小", required = false) @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "开始日期", required = false) @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期", required = false) @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "消费类型", required = false) @RequestParam(required = false) String consumeType) {
        log.info("[移动端消费记录] 查询参数: pageNum={}, pageSize={}, consumeType={}",
                pageNum, pageSize, consumeType);

        try {
            ConsumeQueryDTO queryDTO = new ConsumeQueryDTO();
            queryDTO.setPageNum(pageNum);
            queryDTO.setPageSize(pageSize);
            queryDTO.setStartDate(startDate);
            queryDTO.setEndDate(endDate);
            queryDTO.setConsumeType(consumeType);

            ResponseDTO<IPage<ConsumeRecordVO>> serviceResult = consumeService.queryConsumeRecordPage(queryDTO);
            PageResult<ConsumeRecordVO> result = PageResultConverter.fromResponse(serviceResult);

            // 转换为移动端格式
            PageResult<MobileConsumeRecordVO> mobileResult = convertToMobilePageResult(result);

            return ResponseDTO.ok(mobileResult);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端消费记录] 参数错误: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端消费记录] 业务异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端消费记录] 系统异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_QUERY_SYSTEM_ERROR", "查询消费记录异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端消费记录] 未知异常: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("MOBILE_QUERY_ERROR", "查询消费记录异常");
        }
    }

    /**
     * 获取移动端消费统计
     * 移动端展示的消费统计信息
     */
    @GetMapping("/statistics")
    @Observed(name = "mobileConsume.getConsumeStatistics", contextualName = "mobile-consume-get-consume-statistics")
    @Operation(summary = "获取消费统计", description = "获取用户消费统计信息")
    public ResponseDTO<MobileConsumeStatisticsVO> getConsumeStatistics(
            @Parameter(description = "统计类型", required = false) @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期", required = false) @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "结束日期", required = false) @RequestParam(required = false) LocalDateTime endDate) {
        log.info("[移动端消费统计] 统计参数: statisticsType={}, startDate={}, endDate={}",
                statisticsType, startDate, endDate);

        try {
            // ✅ OpenSpec规范执行：替换硬编码数据，使用真实业务逻辑
            // 从SecurityContext获取当前用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.warn("[移动端消费统计] 无法获取用户ID，请先登录");
                return ResponseDTO.error("UNAUTHORIZED", "请先登录");
            }

            // 调用Service层获取真实的消费统计数据
            return mobileConsumeStatisticsService.getConsumeStatistics(userId, statisticsType, startDate, endDate);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端消费统计] 参数错误: statisticsType={}, error={}", statisticsType, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端消费统计] 业务异常: statisticsType={}, code={}, message={}", statisticsType, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端消费统计] 系统异常: statisticsType={}, code={}, message={}", statisticsType, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_STATISTICS_SYSTEM_ERROR", "获取消费统计异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端消费统计] 未知异常: statisticsType={}", statisticsType, e);
            return ResponseDTO.error("MOBILE_STATISTICS_ERROR", "获取消费统计异常");
        }
    }

    /**
     * 获取移动端账户信息
     * 移动端显示的账户信息
     */
    @GetMapping("/account-info")
    @Observed(name = "mobileConsume.getAccountInfo", contextualName = "mobile-consume-get-account-info")
    @Operation(summary = "获取账户信息", description = "获取用户账户信息")
    public ResponseDTO<MobileAccountInfoVO> getAccountInfo(
            @Parameter(description = "账户ID", required = true) @RequestParam @NotNull Long accountId) {
        log.info("[移动端账户信息] accountId={}", accountId);

        try {
            // ✅ OpenSpec规范执行：替换硬编码数据，通过网关调用AccountService
            // 调用Service层获取真实的账户信息
            return mobileAccountInfoService.getAccountInfo(accountId, null);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端账户信息] 参数错误: accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端账户信息] 业务异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端账户信息] 系统异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_ACCOUNT_SYSTEM_ERROR", "获取账户信息异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端账户信息] 未知异常: accountId={}", accountId, e);
            return ResponseDTO.error("MOBILE_ACCOUNT_ERROR", "获取账户信息异常");
        }
    }

    /**
     * 移动端账户充值
     * 移动端优化的充值接口
     */
    @PostMapping("/recharge")
    @Observed(name = "mobileConsume.recharge", contextualName = "mobile-consume-recharge")
    @Operation(summary = "账户充值", description = "移动端账户充值")
    public ResponseDTO<MobileRechargeResultVO> recharge(
            @RequestBody @Valid MobileRechargeRequestDTO request) {
        log.info("[移动端账户充值] accountId={}, amount={}", request.getAccountId(), request.getAmount());

        try {
            // 转换充值请求
            RechargeRequestDTO rechargeRequest = convertToRechargeRequest(request);
            ResponseDTO<Void> result = consumeService.recharge(rechargeRequest);

            if (result.getOk()) {
                // 返回充值成功信息
                MobileRechargeResultVO rechargeResult = new MobileRechargeResultVO();
                rechargeResult.setOrderId(request.getOrderId());
                rechargeResult.setAmount(request.getAmount());
                rechargeResult.setRechargeType(request.getRechargeType());
                rechargeResult.setStatus("SUCCESS");
                rechargeResult.setRechargeTime(LocalDateTime.now());

                return ResponseDTO.ok(rechargeResult);
            } else {
                return ResponseDTO.error(result.getCode(), result.getMessage());
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端账户充值] 参数错误: accountId={}, amount={}, error={}", request.getAccountId(), request.getAmount(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端账户充值] 业务异常: accountId={}, amount={}, code={}, message={}", request.getAccountId(), request.getAmount(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端账户充值] 系统异常: accountId={}, amount={}, code={}, message={}", request.getAccountId(), request.getAmount(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_RECHARGE_SYSTEM_ERROR", "充值处理异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端账户充值] 未知异常: accountId={}, amount={}", request.getAccountId(), request.getAmount(), e);
            return ResponseDTO.error("MOBILE_RECHARGE_ERROR", "充值处理异常");
        }
    }

    /**
     * 获取消费类型列表
     * 移动端消费类型选择列表
     */
    @GetMapping("/consume-types")
    @Observed(name = "mobileConsume.getConsumeTypes", contextualName = "mobile-consume-get-consume-types")
    @Operation(summary = "获取消费类型", description = "获取可用消费类型列表")
    public ResponseDTO<List<MobileConsumeTypeVO>> getConsumeTypes() {
        log.info("[移动端消费类型] 获取消费类型列表");

        try {
            List<MobileConsumeTypeVO> consumeTypes = getMobileConsumeTypes();
            return ResponseDTO.ok(consumeTypes);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端消费类型] 参数错误: error={}", e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端消费类型] 业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端消费类型] 系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_CONSUME_TYPES_SYSTEM_ERROR", "获取消费类型异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端消费类型] 未知异常", e);
            return ResponseDTO.error("MOBILE_CONSUME_TYPES_ERROR", "获取消费类型异常");
        }
    }

    /**
     * 获取移动端设备信息
     * 移动端设备识别信息
     */
    @GetMapping("/device-info")
    @Observed(name = "mobileConsume.getDeviceInfo", contextualName = "mobile-consume-get-device-info")
    @Operation(summary = "获取设备信息", description = "获取设备识别信息")
    public ResponseDTO<MobileDeviceInfoVO> getDeviceInfo(
            @Parameter(description = "设备ID", required = false) @RequestParam(required = false) String deviceId) {
        log.info("[移动端设备信息] deviceId={}", deviceId);

        try {
            // 实现设备信息查询
            MobileDeviceInfoVO deviceInfo = new MobileDeviceInfoVO();

            // 如果没有提供deviceId，返回移动端默认信息
            if (deviceId == null || deviceId.trim().isEmpty()) {
                deviceInfo.setDeviceId("MOBILE_DEVICE");
                deviceInfo.setDeviceName("移动设备");
                deviceInfo.setDeviceType("MOBILE");
                deviceInfo.setDeviceTypeDescription("移动端设备");
                deviceInfo.setLocation("未知位置");
                deviceInfo.setStatus("ONLINE");
                deviceInfo.setStatusDescription("在线");
                deviceInfo.setLastActiveTime(LocalDateTime.now());
                return ResponseDTO.ok(deviceInfo);
            }

            // 通过GatewayServiceClient调用公共服务查询设备信息
            // 支持通过设备ID或设备编码查询
            String apiPath;
            try {
                // 尝试将deviceId解析为Long类型（设备ID）
                Long deviceIdLong = Long.parseLong(deviceId);
                apiPath = "/api/v1/device/" + deviceIdLong;
            } catch (NumberFormatException e) {
                // 如果不是数字，则作为设备编码查询
                log.debug("[移动端设备信息] deviceId不是数字格式，作为设备编码查询: deviceId={}", deviceId);
                apiPath = "/api/v1/device/code/" + deviceId;
            }

            ResponseDTO<DeviceEntity> deviceResponse = gatewayServiceClient.callCommonService(
                    apiPath,
                    HttpMethod.GET,
                    null,
                    DeviceEntity.class
            );

            if (deviceResponse != null && deviceResponse.isSuccess() && deviceResponse.getData() != null) {
                DeviceEntity device = deviceResponse.getData();

                // 映射DeviceEntity到MobileDeviceInfoVO
                deviceInfo.setDeviceId(device.getId() != null ? device.getId().toString() : deviceId);
                deviceInfo.setDeviceName(device.getDeviceName() != null ? device.getDeviceName() : "未知设备");
                deviceInfo.setDeviceType(device.getDeviceType() != null ? device.getDeviceType() : "UNKNOWN");
                deviceInfo.setDeviceTypeDescription(getDeviceTypeDescription(device.getDeviceType()));
                deviceInfo.setLocation(device.getAreaId() != null ? "区域ID: " + device.getAreaId() : "未知位置");
                deviceInfo.setStatus(device.getDeviceStatus() != null ? device.getDeviceStatus() : "UNKNOWN");
                deviceInfo.setStatusDescription(getDeviceStatusDescription(device.getDeviceStatus()));
                deviceInfo.setIpAddress(device.getIpAddress());
                deviceInfo.setLastActiveTime(device.getLastOnlineTime() != null ? device.getLastOnlineTime() : LocalDateTime.now());

                // 从扩展属性中获取额外信息
                if (device.getExtendedAttributes() != null && !device.getExtendedAttributes().trim().isEmpty()) {
                    try {
                        // ✅ 使用注入的Spring配置的ObjectMapper bean，而非创建新实例
                        Map<String, Object> extendedAttrs = objectMapper.readValue(
                                device.getExtendedAttributes(),
                                new TypeReference<Map<String, Object>>() {}
                        );
                        if (extendedAttrs != null) {
                            deviceInfo.setDeviceBrand((String) extendedAttrs.get("manufacturer"));
                            deviceInfo.setDeviceModel((String) extendedAttrs.get("model"));
                            deviceInfo.setNetworkType((String) extendedAttrs.get("networkType"));
                        }
                    } catch (Exception e) {
                        log.debug("[移动端设备信息] 解析扩展属性失败: {}", e.getMessage());
                    }
                }

                log.info("[移动端设备信息] 查询成功，deviceId={}, deviceName={}", deviceId, deviceInfo.getDeviceName());
                return ResponseDTO.ok(deviceInfo);
            } else {
                // 设备不存在，返回默认信息
                log.warn("[移动端设备信息] 设备不存在，deviceId={}", deviceId);
                deviceInfo.setDeviceId(deviceId);
                deviceInfo.setDeviceName("设备不存在");
                deviceInfo.setDeviceType("UNKNOWN");
                deviceInfo.setStatus("OFFLINE");
                deviceInfo.setStatusDescription("离线");
                deviceInfo.setLastActiveTime(LocalDateTime.now());
                return ResponseDTO.ok(deviceInfo);
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端设备信息] 参数错误: deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端设备信息] 业务异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端设备信息] 系统异常: deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_DEVICE_SYSTEM_ERROR", "获取设备信息异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端设备信息] 未知异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("MOBILE_DEVICE_ERROR", "获取设备信息异常: " + e.getMessage());
        }
    }

    /**
     * 移动端扫码消费
     * 基于二维码的快捷消费
     */
    @PostMapping("/scan-consume")
    @Observed(name = "mobileConsume.scanConsume", contextualName = "mobile-consume-scan-consume")
    @Operation(summary = "扫码消费", description = "基于二维码的扫码消费")
    public ResponseDTO<MobileConsumeResultVO> scanConsume(
            @RequestBody @Valid MobileScanConsumeRequestDTO request) {
        log.info("[移动端扫码消费] qrCode={}, amount={}", request.getQrCode(), request.getAmount());

        try {
            // 解析二维码获取消费信息
            ConsumeRequestDTO consumeRequest = parseQRCode(request.getQrCode());
            consumeRequest.setAmount(request.getAmount());
            consumeRequest.setDeviceId("QR_SCANNER");

            ResponseDTO<ConsumeTransactionResultVO> response = consumeService.consume(consumeRequest);

            if (!response.getOk()) {
                return ResponseDTO.error(response.getCode(), response.getMessage());
            }

            MobileConsumeResultVO mobileResult = convertTransactionResultToMobile(response.getData());

            return ResponseDTO.ok(mobileResult);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端扫码消费] 参数错误: qrCode={}, error={}", request.getQrCode(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端扫码消费] 业务异常: qrCode={}, code={}, message={}", request.getQrCode(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端扫码消费] 系统异常: qrCode={}, code={}, message={}", request.getQrCode(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_SCAN_CONSUME_SYSTEM_ERROR", "扫码消费异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端扫码消费] 未知异常: qrCode={}", request.getQrCode(), e);
            return ResponseDTO.error("MOBILE_SCAN_CONSUME_ERROR", "扫码消费异常");
        }
    }

    /**
     * 获取移动端账单详情
     */
    @GetMapping("/bill/{orderId}")
    @Observed(name = "mobileConsume.getBillDetail", contextualName = "mobile-consume-get-bill-detail")
    @Operation(summary = "获取账单详情", description = "获取消费账单详细信息")
    public ResponseDTO<MobileBillDetailVO> getBillDetail(
            @Parameter(description = "订单ID", required = true) @PathVariable @NotNull String orderId) {
        log.info("[移动端账单详情] orderId={}", orderId);

        try {
            // 通过Service层获取账单详情，遵循四层架构规范
            MobileBillDetailVO billDetail = consumeMobileService.getBillDetail(orderId);

            log.info("[移动端账单详情] 查询成功，orderId={}, amount={}", orderId, billDetail.getAmount());
            return ResponseDTO.ok(billDetail);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端账单详情] 参数错误: orderId={}, error={}", orderId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端账单详情] 业务异常，orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端账单详情] 系统异常: orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("MOBILE_BILL_DETAIL_SYSTEM_ERROR", "获取账单详情异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("[移动端账单详情] 未知异常: orderId={}", orderId, e);
            return ResponseDTO.error("MOBILE_BILL_DETAIL_ERROR", "获取账单详情异常: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换移动端请求为标准请求
     */
    private ConsumeRequestDTO convertToConsumeRequest(MobileQuickConsumeRequestDTO mobileRequest) {
        ConsumeRequestDTO request = new ConsumeRequestDTO();
        request.setOrderId(mobileRequest.getOrderId());
        request.setAccountId(mobileRequest.getAccountId());
        request.setAmount(mobileRequest.getAmount());
        request.setDeviceId(mobileRequest.getDeviceId());
        request.setAreaId(mobileRequest.getAreaId());
        request.setConsumeType(mobileRequest.getConsumeType());
        return request;
    }

    /**
     * 转换交易结果为移动端结果
     */
    private MobileConsumeResultVO convertTransactionResultToMobile(ConsumeTransactionResultVO transactionResult) {
        MobileConsumeResultVO mobileResult = new MobileConsumeResultVO();
        mobileResult.setOrderId(transactionResult.getTransactionNo()); // 使用交易流水号
        mobileResult.setStatus(String.valueOf(transactionResult.getTransactionStatus())); // 修正：使用transactionStatus
        mobileResult.setMessage("交易成功");
        mobileResult.setConsumeTime(transactionResult.getTransactionTime()); // 修正：使用transactionTime
        mobileResult.setAmount(transactionResult.getAmount());
        mobileResult.setBalanceBefore(BigDecimal.ZERO); // 修正：没有balanceBefore字段，使用默认值
        mobileResult.setBalanceAfter(transactionResult.getBalanceAfter());
        return mobileResult;
    }

    /**
     * 转换标准分页结果为移动端分页结果
     */
    private PageResult<MobileConsumeRecordVO> convertToMobilePageResult(PageResult<ConsumeRecordVO> originalResult) {
        PageResult<MobileConsumeRecordVO> mobileResult = new PageResult<>();
        mobileResult.setPageNum(originalResult.getPageNum());
        mobileResult.setPageSize(originalResult.getPageSize());
        mobileResult.setTotal(originalResult.getTotal());

        // 转换数据列表
        List<MobileConsumeRecordVO> mobileRecords = originalResult.getList().stream()
                .map(this::convertToMobileRecord)
                .collect(java.util.stream.Collectors.toList());

        mobileResult.setList(mobileRecords);
        return mobileResult;
    }

    /**
     * 转换消费记录为移动端格式
     */
    private MobileConsumeRecordVO convertToMobileRecord(ConsumeRecordVO record) {
        MobileConsumeRecordVO mobileRecord = new MobileConsumeRecordVO();
        mobileRecord.setRecordId(record.getRecordId());
        mobileRecord.setOrderId(record.getOrderId());
        mobileRecord.setAmount(record.getAmount());
        mobileRecord.setConsumeType(record.getConsumeType());
        mobileRecord.setConsumeTime(record.getConsumeTime());
        mobileRecord.setMerchantName(record.getMerchantName());
        mobileRecord.setStatus(String.valueOf(record.getStatus())); // 类型转换：Integer -> String
        return mobileRecord;
    }

    /**
     * 转换充值请求
     */
    private RechargeRequestDTO convertToRechargeRequest(MobileRechargeRequestDTO mobileRequest) {
        RechargeRequestDTO request = new RechargeRequestDTO();
        request.setOrderId(mobileRequest.getOrderId());
        request.setAccountId(mobileRequest.getAccountId());
        request.setAmount(mobileRequest.getAmount());
        request.setRechargeType(mobileRequest.getRechargeType());
        // paymentMethod 映射到 rechargeType，如果不存在则使用rechargeType
        String paymentMethod = mobileRequest.getPaymentMethod();
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            request.setRechargeType(paymentMethod);
        }
        return request;
    }

    /**
     * 获取移动端消费类型列表
     */
    private List<MobileConsumeTypeVO> getMobileConsumeTypes() {
        List<MobileConsumeTypeVO> consumeTypes = java.util.Arrays.asList(
            createConsumeType("DINING", "餐饮", "🍽️"),
            createConsumeType("SHOPPING", "购物", "🛍️"),
            createConsumeType("TRANSPORT", "交通", "🚗"),
            createConsumeType("ENTERTAINMENT", "娱乐", "🎮"),
            createConsumeType("OTHER", "其他", "📦")
        );
        return consumeTypes;
    }

    /**
     * 创建消费类型
     */
    private MobileConsumeTypeVO createConsumeType(String code, String name, String icon) {
        MobileConsumeTypeVO consumeType = new MobileConsumeTypeVO();
        consumeType.setCode(code);
        consumeType.setName(name);
        consumeType.setIcon(icon);
        return consumeType;
    }

    /**
     * 解析二维码获取消费信息
     *
     * @param qrCode 二维码内容（JSON格式或简单字符串）
     * @return 消费请求DTO
     */
    private ConsumeRequestDTO parseQRCode(String qrCode) {
        log.debug("[移动端二维码解析] 开始解析二维码，length={}", qrCode != null ? qrCode.length() : 0);

        ConsumeRequestDTO request = new ConsumeRequestDTO();

        try {
            // 1. 尝试解析为JSON格式
            Map<String, Object> qrContent = null;
            try {
                qrContent = objectMapper.readValue(qrCode, new TypeReference<Map<String, Object>>() {});
                log.debug("[移动端二维码解析] 成功解析为JSON格式");
            } catch (Exception jsonException) {
                log.debug("[移动端二维码解析] 不是JSON格式，尝试简单字符串解析: {}", jsonException.getMessage());
            }

            // 2. 如果解析成功，从JSON中提取信息
            if (qrContent != null && !qrContent.isEmpty()) {
                // 提取订单ID
                if (qrContent.containsKey("orderId")) {
                    request.setOrderId(String.valueOf(qrContent.get("orderId")));
                } else if (qrContent.containsKey("qrId")) {
                    request.setOrderId("QR_" + qrContent.get("qrId"));
                } else {
                    request.setOrderId("QR_" + System.currentTimeMillis());
                }

                // 提取用户ID
                if (qrContent.containsKey("userId")) {
                    Object userIdObj = qrContent.get("userId");
                    if (userIdObj instanceof Number) {
                        request.setUserId(((Number) userIdObj).longValue());
                    } else if (userIdObj instanceof String) {
                        try {
                            request.setUserId(Long.parseLong((String) userIdObj));
                        } catch (NumberFormatException e) {
                            log.debug("[移动端二维码解析] 用户ID格式无效: {}", userIdObj);
                        }
                    }
                } else {
                    // 如果二维码中没有用户ID，从SecurityContext获取当前登录用户ID
                    Long currentUserId = getCurrentUserId();
                    if (currentUserId != null) {
                        request.setUserId(currentUserId);
                        log.debug("[移动端二维码解析] 从SecurityContext获取用户ID: {}", currentUserId);
                    }
                }

                // 提取账户ID
                if (qrContent.containsKey("accountId")) {
                    Object accountIdObj = qrContent.get("accountId");
                    if (accountIdObj instanceof Number) {
                        request.setAccountId(((Number) accountIdObj).longValue());
                    } else if (accountIdObj instanceof String) {
                        try {
                            request.setAccountId(Long.parseLong((String) accountIdObj));
                        } catch (NumberFormatException e) {
                            log.debug("[移动端二维码解析] 账户ID格式无效: {}", accountIdObj);
                        }
                    }
                }

                // 提取设备ID
                if (qrContent.containsKey("deviceId")) {
                    Object deviceIdObj = qrContent.get("deviceId");
                    if (deviceIdObj != null) {
                        request.setDeviceId(String.valueOf(deviceIdObj));
                    }
                }

                // 提取区域ID
                if (qrContent.containsKey("areaId")) {
                    Object areaIdObj = qrContent.get("areaId");
                    if (areaIdObj instanceof Number) {
                        request.setAreaId(((Number) areaIdObj).longValue());
                    } else if (areaIdObj instanceof String) {
                        try {
                            request.setAreaId(Long.parseLong((String) areaIdObj));
                        } catch (NumberFormatException e) {
                            log.debug("[移动端二维码解析] 区域ID格式无效: {}", areaIdObj);
                        }
                    }
                }

                // 提取消费类型
                if (qrContent.containsKey("consumeType")) {
                    request.setConsumeType(String.valueOf(qrContent.get("consumeType")));
                } else if (qrContent.containsKey("businessModule")) {
                    String businessModule = String.valueOf(qrContent.get("businessModule"));
                    // 将业务模块转换为消费类型
                    if ("consume".equalsIgnoreCase(businessModule)) {
                        request.setConsumeType("DINING");
                    } else {
                        request.setConsumeType("OTHER");
                    }
                }

                // 提取消费模式
                if (qrContent.containsKey("consumeMode")) {
                    request.setConsumeMode(String.valueOf(qrContent.get("consumeMode")));
                }

                // 提取金额（如果有）
                if (qrContent.containsKey("amount")) {
                    Object amountObj = qrContent.get("amount");
                    if (amountObj instanceof Number) {
                        request.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
                    } else if (amountObj instanceof String) {
                        try {
                            request.setAmount(new BigDecimal((String) amountObj));
                        } catch (NumberFormatException e) {
                            log.debug("[移动端二维码解析] 金额格式无效: {}", amountObj);
                        }
                    }
                }

                log.info("[移动端二维码解析] JSON解析成功，orderId={}, userId={}, deviceId={}, areaId={}",
                        request.getOrderId(), request.getUserId(), request.getDeviceId(), request.getAreaId());
                return request;
            }

            // 3. 如果不是JSON格式，尝试简单字符串解析（兼容旧格式）
            // 格式示例：QR_CONSUME_MERCHANT_001_AREA_001 或 QR_DEVICE_001
            if (qrCode != null && qrCode.startsWith("QR_")) {
                String[] parts = qrCode.split("_");
                if (parts.length >= 2) {
                    request.setOrderId(qrCode);

                    // 尝试从字符串中提取设备ID和区域ID
                    for (String part : parts) {
                        if (part.startsWith("DEVICE") || part.startsWith("MERCHANT")) {
                            // 提取设备ID
                            String devicePart = part.replace("DEVICE", "").replace("MERCHANT", "");
                            if (!devicePart.isEmpty()) {
                                request.setDeviceId(devicePart);
                            }
                        } else if (part.startsWith("AREA")) {
                            // 提取区域ID
                            String areaPart = part.replace("AREA", "");
                            if (!areaPart.isEmpty()) {
                                try {
                                    request.setAreaId(Long.parseLong(areaPart));
                                } catch (NumberFormatException e) {
                                    log.debug("[移动端二维码解析] 区域ID格式无效: {}", areaPart);
                                }
                            }
                        }
                    }

                    // 设置默认消费类型
                    if (request.getConsumeType() == null) {
                        request.setConsumeType("DINING");
                    }

                    log.info("[移动端二维码解析] 简单字符串解析成功，orderId={}, deviceId={}, areaId={}",
                            request.getOrderId(), request.getDeviceId(), request.getAreaId());
                    return request;
                }
            }

            // 4. 如果都不匹配，使用默认值
            log.warn("[移动端二维码解析] 无法解析二维码格式，使用默认值，qrCode={}", qrCode);
            request.setOrderId("QR_" + System.currentTimeMillis());
            request.setConsumeType("DINING");

            // 尝试从SecurityContext获取当前用户ID
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                request.setUserId(currentUserId);
                log.debug("[移动端二维码解析] 从SecurityContext获取用户ID: {}", currentUserId);
            }

            return request;

        } catch (Exception e) {
            log.error("[移动端二维码解析] 解析异常: {}", e.getMessage(), e);
            // 异常情况下返回默认值，确保流程可以继续
            request.setOrderId("QR_" + System.currentTimeMillis());
            request.setConsumeType("DINING");
            return request;
        }
    }

    /**
     * 获取当前用户ID
     * <p>
     * 从SecurityContext中获取当前登录用户的ID
     * 支持多种获取方式：
     * 1. 从HttpServletRequest属性中获取（由认证拦截器设置）
     * 2. 降级处理：如果无法获取，返回null并记录警告
     * </p>
     *
     * @return 用户ID，如果无法获取则返回null
     */
    private Long getCurrentUserId() {
        try {
            // 从HttpServletRequest属性中获取用户ID（由认证拦截器或过滤器设置）
            Long userId = SmartRequestUtil.getUserId();
            if (userId != null) {
                log.debug("[移动端消费] 从请求属性获取用户ID: {}", userId);
                return userId;
            }

            // 如果无法获取用户ID，记录警告但不抛出异常（允许部分场景下继续执行）
            log.warn("[移动端消费] 无法获取当前用户ID，可能未登录或认证拦截器未设置用户ID");
            return null;

        } catch (Exception e) {
            log.error("[移动端消费] 获取当前用户ID异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取设备类型描述
     *
     * @param deviceType 设备类型
     * @return 设备类型描述
     */
    private String getDeviceTypeDescription(String deviceType) {
        if (deviceType == null) {
            return "未知类型";
        }
        switch (deviceType.toUpperCase()) {
            case "CAMERA":
                return "摄像头";
            case "ACCESS":
                return "门禁设备";
            case "CONSUME":
                return "消费机";
            case "ATTENDANCE":
                return "考勤机";
            case "BIOMETRIC":
                return "生物识别设备";
            case "INTERCOM":
                return "对讲机";
            case "ALARM":
                return "报警器";
            case "SENSOR":
                return "传感器";
            case "MOBILE":
                return "移动端设备";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取设备状态描述
     *
     * @param deviceStatus 设备状态
     * @return 设备状态描述
     */
    private String getDeviceStatusDescription(String deviceStatus) {
        if (deviceStatus == null) {
            return "未知状态";
        }
        switch (deviceStatus.toUpperCase()) {
            case "ONLINE":
                return "在线";
            case "OFFLINE":
                return "离线";
            case "MAINTAIN":
                return "维护中";
            case "FAULT":
                return "故障";
            default:
                return "未知状态";
        }
    }

}



