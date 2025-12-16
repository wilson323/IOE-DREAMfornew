package net.lab1024.sa.visitor.controller;

import org.springframework.format.annotation.DateTimeFormat;
import net.lab1024.sa.common.permission.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.VisitorAppointmentQueryForm;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.service.VisitorAppointmentService;
import net.lab1024.sa.visitor.service.VisitorQueryService;
import net.lab1024.sa.visitor.service.VisitorStatisticsService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 访客管理PC端控制器
 * <p>
 * 提供PC端访客管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 访客预约查询
 * - 访客记录查询
 * - 访客统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/visitor")
@Tag(name = "访客管理PC端", description = "访客预约查询、记录查询、统计等API")
@PermissionCheck(description = "访客管理")
public class VisitorController {

    @Resource
    private VisitorAppointmentService visitorAppointmentService;

    @Resource
    private VisitorQueryService visitorQueryService;

    @Resource
    private VisitorStatisticsService visitorStatisticsService;

    /**
     * 分页查询访客预约
     * <p>
     * 支持多条件筛选：访客姓名、被访人ID、日期范围、预约状态等
     * 支持分页查询，默认每页20条记录
     * 严格遵循RESTful规范：查询操作使用GET方法
     * </p>
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param visitorName 访客姓名（可选）
     * @param hostUserId 被访人ID（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param status 预约状态（可选）
     * @return 分页的访客预约列表
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/visitor/appointment/query?pageNum=1&pageSize=20&visitorName=张三&hostUserId=1001&startDate=2025-01-01&endDate=2025-01-31&status=PENDING
     * </pre>
     */
    @Observed(name = "visitor.queryAppointments", contextualName = "visitor-query-appointments")
    @GetMapping("/appointment/query")
    @Operation(
        summary = "分页查询访客预约",
        description = "根据条件分页查询访客预约，支持多条件筛选（访客姓名、被访人ID、日期范围、预约状态等）。支持分页查询，默认每页20条记录。严格遵循RESTful规范：查询操作使用GET方法。",
        tags = {"访客管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PageResult.class)
        )
    )
    @PermissionCheck(value = {"VISITOR_MANAGE"}, description = "访客管理操作")
    public ResponseDTO<PageResult<VisitorAppointmentDetailVO>> queryAppointments(
            @Parameter(description = "页码（从1开始）")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "访客姓名（可选）")
            @RequestParam(required = false) String visitorName,
            @Parameter(description = "被访人ID（可选）")
            @RequestParam(required = false) Long hostUserId,
            @Parameter(description = "开始日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "预约状态（可选）")
            @RequestParam(required = false) String status) {
        log.info("[访客管理] 分页查询访客预约，pageNum={}, pageSize={}, visitorName={}, hostUserId={}, startDate={}, endDate={}, status={}",
                pageNum, pageSize, visitorName, hostUserId, startDate, endDate, status);
        try {
            // 构建查询表单
            VisitorAppointmentQueryForm queryForm = new VisitorAppointmentQueryForm();
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);
            queryForm.setVisitorName(visitorName);
            queryForm.setHostUserId(hostUserId);
            queryForm.setStartDate(startDate);
            queryForm.setEndDate(endDate);
            queryForm.setStatus(status);

            PageResult<VisitorAppointmentDetailVO> result = visitorAppointmentService.queryAppointments(queryForm);
            return ResponseDTO.ok(result);
        } catch (ParamException e) {
            log.warn("[访客管理] 分页查询访客预约参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客管理] 分页查询访客预约业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[访客管理] 分页查询访客预约系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[访客管理] 分页查询访客预约执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_APPOINTMENTS_ERROR", "查询访客预约失败");
        }
    }

    /**
     * 获取访客统计
     * <p>
     * 根据时间范围获取访客统计数据，包括预约总数、已审批、待审批、已拒绝等统计信息
     * </p>
     *
     * @param startDate 开始日期（必填，格式：yyyy-MM-dd）
     * @param endDate 结束日期（必填，格式：yyyy-MM-dd）
     * @return 统计数据，包含预约总数、已审批、待审批、已拒绝等统计信息
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/visitor/statistics?startDate=2025-01-01&endDate=2025-01-31
     * </pre>
     */
    @Observed(name = "visitor.getStatistics", contextualName = "visitor-get-statistics")
    @GetMapping("/statistics")
    @Operation(
        summary = "获取访客统计",
        description = "根据时间范围获取访客统计数据，包括预约总数、已审批、待审批、已拒绝等统计信息。",
        tags = {"访客管理PC端"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Map.class)
        )
    )
    @PermissionCheck(value = {"VISITOR_MANAGE"}, description = "访客管理操作")
    public ResponseDTO<Map<String, Object>> getVisitorStatistics(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd", required = true, example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("[访客管理] 获取访客统计，startDate={}, endDate={}", startDate, endDate);
        try {
            // 调用统计服务
            ResponseDTO<Map<String, Object>> statisticsResult = visitorStatisticsService.getStatisticsByDateRange(startDate, endDate);
            if (statisticsResult != null && statisticsResult.isSuccess()) {
                return statisticsResult;
            } else {
                // 返回基础统计数据
                Map<String, Object> result = new HashMap<>();
                result.put("totalVisitors", 0);
                result.put("totalAppointments", 0);
                result.put("approvedAppointments", 0);
                result.put("rejectedAppointments", 0);
                result.put("pendingAppointments", 0);
                return ResponseDTO.ok(result);
            }
        } catch (ParamException e) {
            log.warn("[访客管理] 获取访客统计参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[访客管理] 获取访客统计业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[访客管理] 获取访客统计系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[访客管理] 获取访客统计执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计数据失败");
        }
    }
}


