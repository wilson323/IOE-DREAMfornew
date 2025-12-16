package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.service.AccessEventService;

import java.time.LocalDate;

/**
 * 门禁记录管理PC端控制器
 * <p>
 * 提供PC端门禁记录管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 门禁记录查询（分页）
 * - 门禁记录统计
 * - 门禁记录导出
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/record")
@Tag(name = "门禁记录管理PC端", description = "门禁记录查询、统计、导出等API")
public class AccessRecordController {

    @Resource
    private AccessEventService accessEventService;

    /**
     * 创建门禁记录
     * <p>
     * 用于设备协议推送门禁记录
     * 通过审计日志记录门禁事件
     * </p>
     *
     * @param form 门禁记录创建表单
     * @return 创建的门禁记录ID（审计日志ID）
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/access/record/create
     * {
     *   "userId": 1001,
     *   "deviceId": 1,
     *   "deviceCode": "DEV001",
     *   "passTime": 1706582400,
     *   "passType": 0,
     *   "doorNo": 1,
     *   "passMethod": 1,
     *   "accessResult": 1
     * }
     * </pre>
     */
    @Observed(name = "accessRecord.createAccessRecord", contextualName = "access-record-create")
    @PostMapping("/create")
    @Operation(
        summary = "创建门禁记录",
        description = "创建门禁记录，用于设备协议推送门禁记录。通过审计日志记录门禁事件。",
        tags = {"门禁记录管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    public ResponseDTO<Long> createAccessRecord(
            @Valid @RequestBody AccessRecordAddForm form) {
        log.info("[门禁记录] 创建门禁记录，userId={}, deviceId={}, passType={}",
                form.getUserId(), form.getDeviceId(), form.getPassType());
        return accessEventService.createAccessRecord(form);
    }

    /**
     * 分页查询门禁记录
     * <p>
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param areaId 区域ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param accessResult 通行结果（可选）
     * @return 门禁记录列表
     */
    @Observed(name = "accessRecord.queryAccessRecords", contextualName = "access-record-query")
    @GetMapping("/query")
    @Operation(summary = "分页查询门禁记录", description = "根据条件分页查询门禁记录")
    @PreAuthorize("hasRole('ACCESS_MANAGER')")
    public ResponseDTO<PageResult<net.lab1024.sa.access.domain.vo.AccessRecordVO>> queryAccessRecords(
            @Parameter(description = "页码，从1开始")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID（可选）")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "设备ID（可选）")
            @RequestParam(required = false) Long deviceId,
            @Parameter(description = "区域ID（可选）")
            @RequestParam(required = false) String areaId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "通行结果（可选），1-成功，2-失败，3-异常")
            @RequestParam(required = false) Integer accessResult) {
        log.info("[门禁记录] 分页查询门禁记录，pageNum={}, pageSize={}, userId={}, deviceId={}, areaId={}, startDate={}, endDate={}, accessResult={}",
                pageNum, pageSize, userId, deviceId, areaId, startDate, endDate, accessResult);
        try {
            // 构建查询表单
            net.lab1024.sa.access.domain.form.AccessRecordQueryForm queryForm = new net.lab1024.sa.access.domain.form.AccessRecordQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setUserId(userId);
            queryForm.setDeviceId(deviceId);
            queryForm.setAreaId(areaId);
            queryForm.setStartDate(startDate);
            queryForm.setEndDate(endDate);
            queryForm.setAccessResult(accessResult);

            // 调用Service层查询
            return accessEventService.queryAccessRecords(queryForm);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁记录] 分页查询门禁记录参数错误: pageNum={}, pageSize={}, error={}", pageNum, pageSize, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁记录] 分页查询门禁记录业务异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁记录] 分页查询门禁记录系统异常: pageNum={}, pageSize={}, code={}, message={}", pageNum, pageSize, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("QUERY_ACCESS_RECORDS_SYSTEM_ERROR", "查询门禁记录失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁记录] 分页查询门禁记录未知异常: pageNum={}, pageSize={}", pageNum, pageSize, e);
            return ResponseDTO.error("QUERY_ACCESS_RECORDS_ERROR", "查询门禁记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取门禁记录统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID（可选）
     * @return 统计数据
     */
    @Observed(name = "accessRecord.getAccessRecordStatistics", contextualName = "access-record-statistics")
    @GetMapping("/statistics")
    @Operation(summary = "获取门禁记录统计", description = "根据时间范围和区域获取门禁记录统计数据")
    @PreAuthorize("hasRole('ACCESS_MANAGER')")
    public ResponseDTO<net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO> getAccessRecordStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String areaId) {
        log.info("[门禁记录] 获取门禁记录统计，startDate={}, endDate={}, areaId={}", startDate, endDate, areaId);
        try {
            // 调用Service层统计
            return accessEventService.getAccessRecordStatistics(startDate, endDate, areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁记录] 获取门禁记录统计参数错误: startDate={}, endDate={}, areaId={}, error={}", startDate, endDate, areaId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[门禁记录] 获取门禁记录统计业务异常: startDate={}, endDate={}, areaId={}, code={}, message={}", startDate, endDate, areaId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[门禁记录] 获取门禁记录统计系统异常: startDate={}, endDate={}, areaId={}, code={}, message={}", startDate, endDate, areaId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_SYSTEM_ERROR", "获取统计数据失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[门禁记录] 获取门禁记录统计未知异常: startDate={}, endDate={}, areaId={}", startDate, endDate, areaId, e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计数据失败: " + e.getMessage());
        }
    }
}


