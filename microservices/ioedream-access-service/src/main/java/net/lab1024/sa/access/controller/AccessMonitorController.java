package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessMonitorQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAlarmVO;
import net.lab1024.sa.access.domain.vo.AccessDeviceStatusVO;
import net.lab1024.sa.access.domain.vo.AccessEventVO;
import net.lab1024.sa.access.domain.vo.AccessMonitorStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessPersonTrackVO;
import net.lab1024.sa.access.service.AccessMonitorService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁实时监控控制器
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 实时状态监控、报警处理、视频联动、人员追踪
 * </p>
 * <p>
 * 核心职责：
 * - 实时设备状态监控
 * - 报警接收和处理
 * - 视频联动触发
 * - 人员轨迹追踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/monitor")
@Tag(name = "门禁实时监控", description = "门禁实时监控接口，包括设备状态监控、报警处理、视频联动、人员追踪")
public class AccessMonitorController {

    @Resource
    private AccessMonitorService accessMonitorService;

    /**
     * 查询实时设备状态列表
     * <p>
     * 查询所有门禁设备的实时状态，包括在线状态、响应时间、网络质量等
     * </p>
     *
     * @param queryForm 查询表单
     * @return 设备状态列表
     */
    @PostMapping("/device/status/query")
    @Operation(summary = "查询实时设备状态列表", description = "查询所有门禁设备的实时状态，包括在线状态、响应时间、网络质量等")
    public ResponseDTO<PageResult<AccessDeviceStatusVO>> queryDeviceStatusList(
            @Valid @RequestBody AccessMonitorQueryForm queryForm) {
        log.info("[实时监控] 查询设备状态列表: pageNum={}, pageSize={}", 
                queryForm.getPageNum(), queryForm.getPageSize());
        
        try {
            return accessMonitorService.queryDeviceStatusList(queryForm);
        } catch (Exception e) {
            log.error("[实时监控] 查询设备状态列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICE_STATUS_ERROR", "查询设备状态列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询单个设备实时状态
     * <p>
     * 查询指定设备的详细实时状态信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态详情
     */
    @GetMapping("/device/status/{deviceId}")
    @Operation(summary = "查询单个设备实时状态", description = "查询指定设备的详细实时状态信息")
    public ResponseDTO<AccessDeviceStatusVO> getDeviceStatus(
            @Parameter(description = "设备ID", required = true)
            @PathVariable String deviceId) {
        log.info("[实时监控] 查询设备实时状态: deviceId={}", deviceId);
        
        try {
            return accessMonitorService.getDeviceStatus(deviceId);
        } catch (Exception e) {
            log.error("[实时监控] 查询设备实时状态异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_STATUS_ERROR", "查询设备实时状态失败: " + e.getMessage());
        }
    }

    /**
     * 查询报警列表
     * <p>
     * 分页查询报警信息，支持按报警类型、级别、设备等条件过滤
     * </p>
     *
     * @param queryForm 查询表单
     * @return 报警列表
     */
    @PostMapping("/alarm/query")
    @Operation(summary = "查询报警列表", description = "分页查询报警信息，支持按报警类型、级别、设备等条件过滤")
    public ResponseDTO<PageResult<AccessAlarmVO>> queryAlarmList(
            @Valid @RequestBody AccessMonitorQueryForm queryForm) {
        log.info("[实时监控] 查询报警列表: pageNum={}, pageSize={}, alarmLevel={}", 
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getAlarmLevel());
        
        try {
            return accessMonitorService.queryAlarmList(queryForm);
        } catch (Exception e) {
            log.error("[实时监控] 查询报警列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_ALARM_LIST_ERROR", "查询报警列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理报警
     * <p>
     * 处理指定的报警事件，记录处理结果
     * </p>
     *
     * @param alarmId 报警ID
     * @param handleRemark 处理备注
     * @return 操作结果
     */
    @PutMapping("/alarm/{alarmId}/handle")
    @Operation(summary = "处理报警", description = "处理指定的报警事件，记录处理结果")
    public ResponseDTO<Void> handleAlarm(
            @Parameter(description = "报警ID", required = true)
            @PathVariable Long alarmId,
            @Parameter(description = "处理备注")
            @RequestParam(required = false) String handleRemark) {
        log.info("[实时监控] 处理报警: alarmId={}, handleRemark={}", alarmId, handleRemark);
        
        try {
            return accessMonitorService.handleAlarm(alarmId, handleRemark);
        } catch (Exception e) {
            log.error("[实时监控] 处理报警异常: alarmId={}, error={}", alarmId, e.getMessage(), e);
            return ResponseDTO.error("HANDLE_ALARM_ERROR", "处理报警失败: " + e.getMessage());
        }
    }

    /**
     * 触发视频联动
     * <p>
     * 根据门禁事件触发视频联动，启动录像和抓拍
     * </p>
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型（ACCESS_GRANTED/ACCESS_DENIED等）
     * @return 操作结果
     */
    @PostMapping("/video/linkage")
    @Operation(summary = "触发视频联动", description = "根据门禁事件触发视频联动，启动录像和抓拍")
    public ResponseDTO<Void> triggerVideoLinkage(
            @Parameter(description = "设备ID", required = true)
            @RequestParam String deviceId,
            @Parameter(description = "事件类型（ACCESS_GRANTED/ACCESS_DENIED等）", required = true)
            @RequestParam String eventType) {
        log.info("[实时监控] 触发视频联动: deviceId={}, eventType={}", deviceId, eventType);
        
        try {
            return accessMonitorService.triggerVideoLinkage(deviceId, eventType);
        } catch (Exception e) {
            log.error("[实时监控] 触发视频联动异常: deviceId={}, eventType={}, error={}", 
                    deviceId, eventType, e.getMessage(), e);
            return ResponseDTO.error("TRIGGER_VIDEO_LINKAGE_ERROR", "触发视频联动失败: " + e.getMessage());
        }
    }

    /**
     * 查询人员轨迹
     * <p>
     * 查询指定人员在指定时间范围内的通行轨迹
     * </p>
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 人员轨迹信息
     */
    @GetMapping("/person/track")
    @Operation(summary = "查询人员轨迹", description = "查询指定人员在指定时间范围内的通行轨迹")
    public ResponseDTO<AccessPersonTrackVO> queryPersonTrack(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("[实时监控] 查询人员轨迹: userId={}, startTime={}, endTime={}", userId, startTime, endTime);
        
        try {
            return accessMonitorService.queryPersonTrack(userId, startTime, endTime);
        } catch (Exception e) {
            log.error("[实时监控] 查询人员轨迹异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_PERSON_TRACK_ERROR", "查询人员轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 查询实时通行事件
     * <p>
     * 查询最近的通行事件，用于实时监控大屏展示
     * </p>
     *
     * @param limit 返回数量限制（默认20）
     * @return 通行事件列表
     */
    @GetMapping("/events/realtime")
    @Operation(summary = "查询实时通行事件", description = "查询最近的通行事件，用于实时监控大屏展示")
    public ResponseDTO<List<AccessEventVO>> queryRealtimeEvents(
            @Parameter(description = "返回数量限制（默认20）")
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        log.info("[实时监控] 查询实时通行事件: limit={}", limit);
        
        try {
            return accessMonitorService.queryRealtimeEvents(limit);
        } catch (Exception e) {
            log.error("[实时监控] 查询实时通行事件异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_REALTIME_EVENTS_ERROR", "查询实时通行事件失败: " + e.getMessage());
        }
    }

    /**
     * 统计监控数据
     * <p>
     * 统计实时监控相关的数据，包括在线设备数、报警数、今日通行数等
     * </p>
     *
     * @return 监控统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "统计监控数据", description = "统计实时监控相关的数据，包括在线设备数、报警数、今日通行数等")
    public ResponseDTO<AccessMonitorStatisticsVO> getMonitorStatistics() {
        log.info("[实时监控] 统计监控数据");
        
        try {
            return accessMonitorService.getMonitorStatistics();
        } catch (Exception e) {
            log.error("[实时监控] 统计监控数据异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_MONITOR_STATISTICS_ERROR", "统计监控数据失败: " + e.getMessage());
        }
    }
}
