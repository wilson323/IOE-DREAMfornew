package net.lab1024.sa.consume.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRealtimeStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.service.ConsumeService;

/**
 * 消费管理控制器
 * <p>
 * 提供消费交易管理相关的REST API接口
 * 严格遵循CLAUDE.md规范：
 * - Controller层负责接收请求、参数验证、返回响应
 * - 使用@Resource注入Service
 * - 使用@Valid进行参数验证
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 消费交易执行
 * - 消费记录查询
 * - 设备管理
 * - 实时统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/transaction")
@Tag(name = "消费管理", description = "消费交易管理相关接口")
public class ConsumeController {

    @Resource
    private ConsumeService consumeService;

    /**
     * 执行消费交易
     * <p>
     * 支持多种消费模式：刷卡、刷脸、NFC、手机支付等
     * 自动进行账户余额验证、设备状态检查、权限验证等
     * </p>
     *
     * @param form 消费交易表单
     * @return 交易结果，包含交易流水号、状态、金额等信息
     * @apiNote 示例请求：
     * <pre>
     * {
     *   "userId": 1001,
     *   "accountId": 2001,
     *   "deviceId": 3001,
     *   "areaId": 4001,
     *   "amount": 50.00,
     *   "consumeMode": "CARD"
     * }
     * </pre>
     */
    @PostMapping("/execute")
    @Observed(name = "consume.executeTransaction", contextualName = "consume-execute-transaction")
    @Operation(
        summary = "执行消费交易",
        description = "执行消费交易并返回交易结果。支持多种消费模式（刷卡、刷脸、NFC、手机支付等），自动进行账户余额验证、设备状态检查、权限验证等。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "交易成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ConsumeTransactionResultVO.class)
        )
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<ConsumeTransactionResultVO> executeTransaction(
            @Parameter(description = "消费交易表单", required = true)
            @Valid @RequestBody ConsumeTransactionForm form) {
        log.info("[消费管理] 执行消费交易，userId={}, accountId={}, amount={}, consumeMode={}",
                form.getUserId(), form.getAccountId(), form.getAmount(), form.getConsumeMode());
        try {
            ConsumeTransactionResultVO result = consumeService.executeTransaction(form);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 执行消费交易参数错误，userId={}, accountId={}, error={}",
                    form.getUserId(), form.getAccountId(), e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 执行消费交易业务异常，userId={}, accountId={}, code={}, message={}",
                    form.getUserId(), form.getAccountId(), e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 执行消费交易系统异常，userId={}, accountId={}, code={}, message={}",
                    form.getUserId(), form.getAccountId(), e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("EXECUTE_TRANSACTION_SYSTEM_ERROR", "执行消费交易失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 执行消费交易未知异常，userId={}, accountId={}",
                    form.getUserId(), form.getAccountId(), e);
            return ResponseDTO.error("EXECUTE_TRANSACTION_ERROR", "执行消费交易失败: " + e.getMessage());
        }
    }

    /**
     * 查询交易详情
     * <p>
     * 根据交易流水号查询交易的完整信息，包括交易金额、时间、状态、设备信息等
     * </p>
     *
     * @param transactionNo 交易流水号（必填）
     * @return 交易详情，包含完整的交易信息
     */
    @GetMapping("/detail/{transactionNo}")
    @Observed(name = "consume.getTransactionDetail", contextualName = "consume-get-transaction-detail")
    @Operation(
        summary = "查询交易详情",
        description = "根据交易流水号查询交易的完整信息，包括交易金额、时间、状态、设备信息、账户信息等详细信息。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ConsumeTransactionDetailVO.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "交易记录不存在"
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER') or hasRole('CONSUME_USER')")
    public ResponseDTO<ConsumeTransactionDetailVO> getTransactionDetail(
            @Parameter(description = "交易流水号", required = true, example = "TXN2025013012345678")
            @PathVariable String transactionNo) {
        log.info("[消费管理] 查询交易详情，transactionNo={}", transactionNo);
        try {
            ConsumeTransactionDetailVO detail = consumeService.getTransactionDetail(transactionNo);
            if (detail == null) {
                return ResponseDTO.error("TRANSACTION_NOT_FOUND", "交易记录不存在");
            }
            return ResponseDTO.ok(detail);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 查询交易详情参数错误，transactionNo={}, error={}", transactionNo, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 查询交易详情业务异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 查询交易详情系统异常，transactionNo={}, code={}, message={}", transactionNo, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_TRANSACTION_SYSTEM_ERROR", "查询交易详情失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 查询交易详情未知异常，transactionNo={}", transactionNo, e);
            return ResponseDTO.error("QUERY_TRANSACTION_ERROR", "查询交易详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询消费记录
     * <p>
     * 支持多条件筛选：用户ID、区域ID、时间范围、消费模式、交易状态等
     * 支持分页查询，默认每页10条记录
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param userId 用户ID（可选）
     * @param areaId 区域ID（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param consumeMode 消费模式（可选）
     * @param status 交易状态（可选）
     * @return 分页的消费记录列表
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/consume/transaction/query?pageNum=1&pageSize=10&userId=1001&startDate=2025-01-01&endDate=2025-01-31&consumeMode=CARD&status=SUCCESS
     * </pre>
     */
    @GetMapping("/query")
    @Observed(name = "consume.queryTransactions", contextualName = "consume-query-transactions")
    @Operation(
        summary = "分页查询消费记录",
        description = "分页查询消费记录，支持多条件筛选（用户ID、区域ID、时间范围、消费模式、交易状态等）。支持分页查询，默认每页10条记录。严格遵循RESTful规范：查询操作使用GET方法。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
        )
    )
	    @PreAuthorize("hasRole('CONSUME_MANAGER')")
	    public ResponseDTO<PageResult<ConsumeTransactionDetailVO>> queryTransactions(
	            @Parameter(description = "页码（从1开始）")
	            @RequestParam(defaultValue = "1") Integer pageNum,
	            @Parameter(description = "每页大小")
	            @RequestParam(defaultValue = "10") Integer pageSize,
	            @Parameter(description = "用户ID（可选）")
	            @RequestParam(required = false) Long userId,
	            @Parameter(description = "交易流水号（可选）")
	            @RequestParam(required = false) String transactionNo,
	            @Parameter(description = "设备ID（可选）")
	            @RequestParam(required = false) Long deviceId,
	            @Parameter(description = "区域ID（可选）")
	            @RequestParam(required = false) String areaId,
	            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
	            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
	            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	            @Parameter(description = "消费模式（可选）")
	            @RequestParam(required = false) String consumeMode,
	            @Parameter(description = "交易状态（可选）")
	            @RequestParam(required = false) String status) {
	        log.info("[消费管理] 分页查询消费记录，pageNum={}, pageSize={}, userId={}, transactionNo={}, deviceId={}, areaId={}, startDate={}, endDate={}, consumeMode={}, status={}",
	                pageNum, pageSize, userId, transactionNo, deviceId, areaId, startDate, endDate, consumeMode, status);
	        try {
	            // 构建查询表单
	            ConsumeTransactionQueryForm queryForm = new ConsumeTransactionQueryForm();
	            queryForm.setPageNum(pageNum);
	            queryForm.setPageSize(pageSize);
	            queryForm.setUserId(userId);
	            queryForm.setTransactionNo(transactionNo);
	            queryForm.setDeviceId(deviceId);
	            queryForm.setAreaId(areaId);
	            queryForm.setStartDate(startDate);
	            queryForm.setEndDate(endDate);
	            queryForm.setConsumeMode(consumeMode);
	            queryForm.setStatus(status);

            PageResult<ConsumeTransactionDetailVO> result = consumeService.queryTransactions(queryForm);
            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 分页查询消费记录参数错误，pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 分页查询消费记录业务异常，pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 分页查询消费记录系统异常，pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_TRANSACTIONS_SYSTEM_ERROR", "查询消费记录失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 分页查询消费记录未知异常，pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_TRANSACTIONS_ERROR", "查询消费记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备详情
     * <p>
     * 获取指定消费设备的详细信息，包括设备状态、位置、配置等
     * </p>
     *
     * @param deviceId 设备ID（必填）
     * @return 设备详情，包含设备的所有信息
     */
    @GetMapping("/device/{deviceId}")
    @Observed(name = "consume.getDeviceDetail", contextualName = "consume-get-device-detail")
    @Operation(
        summary = "获取设备详情",
        description = "获取指定消费设备的详细信息，包括设备状态、位置、配置、统计信息等。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ConsumeDeviceVO.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "设备不存在"
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<ConsumeDeviceVO> getDeviceDetail(
            @Parameter(description = "设备ID", required = true, example = "3001")
            @PathVariable Long deviceId) {
        log.info("[消费管理] 获取设备详情，deviceId={}", deviceId);
        try {
            ConsumeDeviceVO device = consumeService.getDeviceDetail(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            return ResponseDTO.ok(device);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 获取设备详情参数错误，deviceId={}, error={}", deviceId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 获取设备详情业务异常，deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 获取设备详情系统异常，deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_SYSTEM_ERROR", "获取设备详情失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 获取设备详情未知异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_ERROR", "获取设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备状态统计
     * <p>
     * 获取指定区域的设备状态统计信息，包括设备总数、在线数、离线数、故障数等
     * 如果不指定区域ID，则统计所有区域的设备
     * </p>
     *
     * @param areaId 区域ID（可选，不指定则统计所有区域）
     * @return 设备统计信息，包含设备数量、状态分布等
     */
    @GetMapping("/device/statistics")
    @Observed(name = "consume.getDeviceStatistics", contextualName = "consume-get-device-statistics")
    @Operation(
        summary = "获取设备状态统计",
        description = "获取指定区域的设备状态统计信息，包括设备总数、在线数、离线数、故障数等。如果不指定区域ID，则统计所有区域的设备。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ConsumeDeviceStatisticsVO.class)
        )
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<ConsumeDeviceStatisticsVO> getDeviceStatistics(
            @Parameter(description = "区域ID（可选）", example = "AREA001")
            @RequestParam(required = false) String areaId) {
        log.info("[消费管理] 获取设备状态统计，areaId={}", areaId);
        try {
            ConsumeDeviceStatisticsVO statistics = consumeService.getDeviceStatistics(areaId);
            return ResponseDTO.ok(statistics);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 获取设备状态统计参数错误，areaId={}, error={}", areaId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 获取设备状态统计业务异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 获取设备状态统计系统异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_STATISTICS_SYSTEM_ERROR", "获取设备状态统计失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 获取设备状态统计未知异常，areaId={}", areaId, e);
            return ResponseDTO.error("GET_DEVICE_STATISTICS_ERROR", "获取设备状态统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时统计
     * <p>
     * 获取指定区域的实时消费统计数据，包括今日消费金额、今日消费笔数、实时交易等
     * 如果不指定区域ID，则统计所有区域的实时数据
     * </p>
     *
     * @param areaId 区域ID（可选，不指定则统计所有区域）
     * @return 实时统计数据，包含今日消费金额、笔数、实时交易等
     */
    @GetMapping("/realtime-statistics")
    @Observed(name = "consume.getRealtimeStatistics", contextualName = "consume-get-realtime-statistics")
    @Operation(
        summary = "获取实时统计",
        description = "获取指定区域的实时消费统计数据，包括今日消费金额、今日消费笔数、实时交易等。如果不指定区域ID，则统计所有区域的实时数据。",
        tags = {"消费管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ConsumeRealtimeStatisticsVO.class)
        )
    )
    @PreAuthorize("hasRole('CONSUME_MANAGER')")
    public ResponseDTO<ConsumeRealtimeStatisticsVO> getRealtimeStatistics(
            @Parameter(description = "区域ID（可选）", example = "AREA001")
            @RequestParam(required = false) String areaId) {
        log.info("[消费管理] 获取实时统计，areaId={}", areaId);
        try {
            ConsumeRealtimeStatisticsVO statistics = consumeService.getRealtimeStatistics(areaId);
            return ResponseDTO.ok(statistics);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费管理] 获取实时统计参数错误，areaId={}, error={}", areaId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费管理] 获取实时统计业务异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费管理] 获取实时统计系统异常，areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_REALTIME_STATISTICS_SYSTEM_ERROR", "获取实时统计失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费管理] 获取实时统计未知异常，areaId={}", areaId, e);
            return ResponseDTO.error("GET_REALTIME_STATISTICS_ERROR", "获取实时统计失败: " + e.getMessage());
        }
    }
}




