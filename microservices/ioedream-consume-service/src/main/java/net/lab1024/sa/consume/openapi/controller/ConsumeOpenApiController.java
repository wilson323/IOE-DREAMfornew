package net.lab1024.sa.consume.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.consume.openapi.domain.request.*;
import net.lab1024.sa.consume.openapi.domain.response.*;
import net.lab1024.sa.consume.openapi.service.ConsumeOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台消费管理API控制器
 * 提供账户管理、交易处理、余额查询、充值退款等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/consume")
@RequiredArgsConstructor
@Tag(name = "开放平台消费管理API", description = "提供账户管理、交易处理、余额查询、充值退款等功能")
@Validated
public class ConsumeOpenApiController {

    private final ConsumeOpenApiService consumeOpenApiService;

    /**
     * 账户余额查询
     */
    @GetMapping("/account/balance")
    @Operation(summary = "账户余额查询", description = "查询用户账户余额")
    public ResponseDTO<AccountBalanceResponse> getAccountBalance(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "账户类型") @RequestParam(required = false) String accountType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询账户余额: userId={}, accountType={}", userId, accountType);

        AccountBalanceResponse response = consumeOpenApiService.getAccountBalance(userId, accountType, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取账户信息
     */
    @GetMapping("/account/info")
    @Operation(summary = "获取账户信息", description = "获取用户账户详细信息")
    public ResponseDTO<AccountInfoResponse> getAccountInfo(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "账户类型") @RequestParam(required = false) String accountType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询账户信息: userId={}, accountType={}", userId, accountType);

        AccountInfoResponse response = consumeOpenApiService.getAccountInfo(userId, accountType, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 消费交易
     */
    @PostMapping("/transaction/consume")
    @Operation(summary = "消费交易", description = "执行消费交易")
    public ResponseDTO<ConsumeTransactionResponse> consume(
            @Valid @RequestBody ConsumeRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 消费交易: userId={}, amount={}, clientIp={}",
                request.getUserId(), request.getAmount(), clientIp);

        ConsumeTransactionResponse response = consumeOpenApiService.consume(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 退款处理
     */
    @PostMapping("/transaction/refund")
    @Operation(summary = "退款处理", description = "处理退款交易")
    public ResponseDTO<RefundTransactionResponse> refund(
            @Valid @RequestBody RefundRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 退款处理: originalTransactionId={}, amount={}, clientIp={}",
                request.getOriginalTransactionId(), request.getRefundAmount(), clientIp);

        RefundTransactionResponse response = consumeOpenApiService.refund(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 账户充值
     */
    @PostMapping("/account/recharge")
    @Operation(summary = "账户充值", description = "为账户充值")
    public ResponseDTO<RechargeResponse> recharge(
            @Valid @RequestBody RechargeRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 账户充值: userId={}, amount={}, rechargeType={}, clientIp={}",
                request.getUserId(), request.getRechargeAmount(), request.getRechargeType(), clientIp);

        RechargeResponse response = consumeOpenApiService.recharge(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取交易记录列表
     */
    @GetMapping("/transactions")
    @Operation(summary = "获取交易记录列表", description = "分页获取交易记录")
    public ResponseDTO<PageResult<TransactionResponse>> getTransactions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "交易类型") @RequestParam(required = false) String transactionType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "交易状态") @RequestParam(required = false) String transactionStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        TransactionQueryRequest queryRequest = TransactionQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .transactionType(transactionType)
                .startDate(startDate)
                .endDate(endDate)
                .transactionStatus(transactionStatus)
                .build();

        log.info("[开放API] 查询交易记录: pageNum={}, pageSize={}, transactionType={}",
                pageNum, pageSize, transactionType);

        PageResult<TransactionResponse> result = consumeOpenApiService.getTransactions(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取交易详情
     */
    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "获取交易详情", description = "根据交易ID获取交易详情")
    public ResponseDTO<TransactionDetailResponse> getTransactionDetail(
            @Parameter(description = "交易ID") @PathVariable String transactionId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询交易详情: transactionId={}", transactionId);

        TransactionDetailResponse response = consumeOpenApiService.getTransactionDetail(transactionId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取消费统计
     */
    @GetMapping("/statistics/consume")
    @Operation(summary = "获取消费统计", description = "获取用户或部门的消费统计")
    public ResponseDTO<ConsumeStatisticsResponse> getConsumeStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询消费统计: userId={}, departmentId={}, statisticsType={}",
                userId, departmentId, statisticsType);

        ConsumeStatisticsResponse response = consumeOpenApiService.getConsumeStatistics(
                userId, departmentId, statisticsType, startDate, endDate, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取充值记录
     */
    @GetMapping("/recharges")
    @Operation(summary = "获取充值记录", description = "获取充值记录")
    public ResponseDTO<PageResult<RechargeRecordResponse>> getRechargeRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "充值方式") @RequestParam(required = false) String rechargeMethod,
            @Parameter(description = "充值状态") @RequestParam(required = false) String rechargeStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        RechargeRecordQueryRequest queryRequest = RechargeRecordQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .rechargeMethod(rechargeMethod)
                .rechargeStatus(rechargeStatus)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        log.info("[开放API] 查询充值记录: pageNum={}, pageSize={}, rechargeMethod={}",
                pageNum, pageSize, rechargeMethod);

        PageResult<RechargeRecordResponse> result = consumeOpenApiService.getRechargeRecords(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取退款记录
     */
    @GetMapping("/refunds")
    @Operation(summary = "获取退款记录", description = "获取退款记录")
    public ResponseDTO<PageResult<RefundRecordResponse>> getRefundRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "退款原因") @RequestParam(required = false) String refundReason,
            @Parameter(description = "退款状态") @RequestParam(required = false) String refundStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        RefundRecordQueryRequest queryRequest = RefundRecordQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .refundReason(refundReason)
                .refundStatus(refundStatus)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        log.info("[开放API] 查询退款记录: pageNum={}, pageSize={}, refundReason={}",
                pageNum, pageSize, refundReason);

        PageResult<RefundRecordResponse> result = consumeOpenApiService.getRefundRecords(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取消费套餐列表
     */
    @GetMapping("/packages")
    @Operation(summary = "获取消费套餐列表", description = "获取可用的消费套餐")
    public ResponseDTO<List<ConsumePackageResponse>> getConsumePackages(
            @Parameter(description = "套餐类型") @RequestParam(required = false) String packageType,
            @Parameter(description = "是否仅启用") @RequestParam(defaultValue = "true") Boolean onlyEnabled,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询消费套餐: packageType={}, onlyEnabled={}", packageType, onlyEnabled);

        List<ConsumePackageResponse> packages = consumeOpenApiService.getConsumePackages(packageType, onlyEnabled, token);
        return ResponseDTO.ok(packages);
    }

    /**
     * 获取用户消费套餐
     */
    @GetMapping("/packages/user")
    @Operation(summary = "获取用户消费套餐", description = "获取用户已购买的消费套餐")
    public ResponseDTO<List<UserConsumePackageResponse>> getUserConsumePackages(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "套餐状态") @RequestParam(required = false) String packageStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户消费套餐: userId={}, packageStatus={}", userId, packageStatus);

        List<UserConsumePackageResponse> packages = consumeOpenApiService.getUserConsumePackages(userId, packageStatus, token);
        return ResponseDTO.ok(packages);
    }

    /**
     * 购买消费套餐
     */
    @PostMapping("/packages/purchase")
    @Operation(summary = "购买消费套餐", description = "用户购买消费套餐")
    public ResponseDTO<PurchasePackageResponse> purchasePackage(
            @Valid @RequestBody PurchasePackageRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 购买消费套餐: userId={}, packageId={}, clientIp={}",
                request.getUserId(), request.getPackageId(), clientIp);

        PurchasePackageResponse response = consumeOpenApiService.purchasePackage(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取设备列表
     */
    @GetMapping("/devices")
    @Operation(summary = "获取设备列表", description = "获取消费设备列表")
    public ResponseDTO<List<ConsumeDeviceResponse>> getConsumeDevices(
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "设备状态") @RequestParam(required = false) Integer deviceStatus,
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询消费设备: areaId={}, deviceStatus={}, deviceType={}",
                areaId, deviceStatus, deviceType);

        List<ConsumeDeviceResponse> devices = consumeOpenApiService.getConsumeDevices(areaId, deviceStatus, deviceType, token);
        return ResponseDTO.ok(devices);
    }

    /**
     * 获取商户信息
     */
    @GetMapping("/merchant/info")
    @Operation(summary = "获取商户信息", description = "获取商户基本信息")
    public ResponseDTO<MerchantInfoResponse> getMerchantInfo(
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询商户信息: merchantId={}", merchantId);

        MerchantInfoResponse response = consumeOpenApiService.getMerchantInfo(merchantId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取实时消费状态
     */
    @GetMapping("/realtime/status")
    @Operation(summary = "获取实时消费状态", description = "获取实时消费状态")
    public ResponseDTO<RealtimeConsumeStatusResponse> getRealtimeConsumeStatus(
            @Parameter(description = "商户ID") @RequestParam(required = false) Long merchantId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取实时消费状态: merchantId={}, areaId={}", merchantId, areaId);

        RealtimeConsumeStatusResponse response = consumeOpenApiService.getRealtimeConsumeStatus(merchantId, areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}