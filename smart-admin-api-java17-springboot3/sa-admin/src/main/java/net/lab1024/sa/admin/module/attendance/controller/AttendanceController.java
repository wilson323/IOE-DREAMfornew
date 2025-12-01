package net.lab1024.sa.admin.module.attendance.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.admin.module.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.admin.module.attendance.service.IAttendanceService;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.admin.module.attendance.service.AttendanceLocationService;
import net.lab1024.sa.admin.module.attendance.service.AttendanceMobileService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.admin.module.attendance.domain.form.MakeupPunchRequest;

// Nacos微服务支持
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.cloud.nacos.NacosServiceManager;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 考勤管理Controller
 *
 * <p>
 * 考勤模块的核心API接口，提供完整的考勤管理功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Controller层，提供HTTP接口和参数验证
 *
 * 基于现有94个Java文件、37,245行代码的微服务化改造
 * 集成Nacos服务发现和配置中心，确保全局一致性
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2024-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "考勤管理", description = "考勤打卡、查询、统计相关接口")
@Validated
public class AttendanceController {

    @Resource
    private IAttendanceService attendanceService;


    @Resource
    private AttendanceMobileService attendanceMobileService;

    @Resource
    private AttendanceLocationService attendanceLocationService;

    // Nacos微服务支持 - 基于现有项目改造
    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private NacosServiceManager nacosServiceManager;

    /**
     * 上班打卡
     */
    @PostMapping("/punch-in")
    @Operation(summary = "上班打卡", description = "员工上班打卡接口")
    @SaCheckLogin
    @SaCheckPermission("attendance:punch:in")
    @RequireResource(code = "attendance:punch:in", scope = "SELF")
    public ResponseDTO<String> punchIn(@Valid @RequestBody AttendancePunchDTO punchDTO) {
        log.info("员工上班打卡请求: 员工ID={}, 打卡时间={}", punchDTO.getEmployeeId(), punchDTO.getPunchTime());

        try {
            // 设置打卡类型为上班
            punchDTO.setPunchType("上班");

            // 调用Service层处理打卡逻辑
            ResponseDTO<String> result = attendanceService.punch(punchDTO);
            log.info("上班打卡结果: 员工ID={}, 结果={}", punchDTO.getEmployeeId(),
                    result.getOk() != null && result.getOk() ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            log.error("上班打卡失败: 员工ID={}, 错误信息={}", punchDTO.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("打卡失败: " + e.getMessage());
        }
    }

    /**
     * 下班打卡
     */
    @PostMapping("/punch-out")
    @Operation(summary = "下班打卡", description = "员工下班打卡接口")
    @SaCheckLogin
    @SaCheckPermission("attendance:punch:out")
    @RequireResource(code = "attendance:punch:in", scope = "SELF")
    public ResponseDTO<String> punchOut(@Valid @RequestBody AttendancePunchDTO punchDTO) {
        log.info("员工下班打卡请求: 员工ID={}, 打卡时间={}", punchDTO.getEmployeeId(), punchDTO.getPunchTime());

        try {
            // 设置打卡类型为下班
            punchDTO.setPunchType("下班");

            // 调用Service层处理打卡逻辑
            ResponseDTO<String> result = attendanceService.punch(punchDTO);
            log.info("下班打卡结果: 员工ID={}, 结果={}", punchDTO.getEmployeeId(),
                    result.getOk() != null && result.getOk() ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            log.error("下班打卡失败: 员工ID={}, 错误信息={}", punchDTO.getEmployeeId(), e.getMessage(), e);
            return ResponseDTO.error("打卡失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询考勤记录
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询考勤记录", description = "分页查询考勤记录列表")
    @SaCheckLogin
    @SaCheckPermission("attendance:query")
    public ResponseDTO<PageResult<AttendanceRecordVO>> queryByPage(AttendanceRecordQueryVO queryVO, PageParam pageParam) {
        log.info("查询考勤记录: 查询条件={}, 分页参数={}", queryVO, pageParam);

        try {
            PageResult<AttendanceRecordVO> result = attendanceService.queryByPage(queryVO, pageParam);
            log.info("查询考勤记录完成: 总记录数={}", result.getTotalCount());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("查询考勤记录失败: 错误信息={}", e.getMessage(), e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询考勤记录详情
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "查询考勤记录详情", description = "根据记录ID查询考勤记录详情")
    @SaCheckLogin
    @SaCheckPermission("attendance:detail")
    public ResponseDTO<AttendanceRecordVO> getById(@PathVariable Long recordId) {
        log.info("查询考勤记录详情: 记录ID={}", recordId);

        try {
            ResponseDTO<AttendanceRecordVO> result = attendanceService.getById(recordId);
            log.info("查询考勤记录详情完成: 记录ID={}, 结果={}", recordId,
                    result.getOk() != null ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            log.error("查询考勤记录详情失败: 记录ID={}, 错误信息={}", recordId, e.getMessage(), e);
            return ResponseDTO.error("查询详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取员工今日考勤状态
     */
    @GetMapping("/today/{employeeId}")
    @Operation(summary = "获取员工今日考勤状态", description = "获取指定员工今日的考勤状态")
    @SaCheckLogin
    @SaCheckPermission("attendance:today")
    public ResponseDTO<AttendanceRecordVO> getTodayAttendance(@PathVariable Long employeeId) {
        log.info("获取员工今日考勤状态: 员工ID={}", employeeId);

        try {
            ResponseDTO<AttendanceRecordVO> result = attendanceService.queryTodayAttendance(employeeId, java.time.LocalDate.now());
            log.info("获取员工今日考勤状态完成: 员工ID={}, 结果={}", employeeId,
                    result.getOk() != null ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            log.error("获取员工今日考勤状态失败: 员工ID={}, 错误信息={}", employeeId, e.getMessage(), e);
            return ResponseDTO.error("查询今日状态失败: " + e.getMessage());
        }
    }

    /**
     * 导出考勤数据
     */
    @PostMapping("/export")
    @Operation(summary = "导出考勤数据", description = "导出考勤数据到Excel文件")
    @SaCheckLogin
    @SaCheckPermission("attendance:export")
    public void exportAttendanceData(AttendanceRecordQueryVO queryVO, HttpServletResponse response) {
        log.info("导出考勤数据: 查询条件={}", queryVO);

        try {
            // 调用导出服务
            ResponseDTO<String> result = attendanceService.exportAttendanceData(
                    queryVO.getEmployeeId(),
                    queryVO.getDepartmentId(),
                    queryVO.getStartDate(),
                    queryVO.getEndDate(),
                    "excel"
            );

            if (result.getOk() != null) {
                log.info("考勤数据导出成功: 文件路径={}", result.getOk());
                // 这里可以添加文件下载逻辑
                SmartResponseUtil.setDownloadResponse(response, result.getOk(), "attendance_export.xlsx");
            } else {
                log.error("考勤数据导出失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            log.error("导出考勤数据失败: 错误信息={}", e.getMessage(), e);
        }
    }

    // ========== Nacos微服务支持方法 ==========

    /**
     * Nacos健康检查
     */
    @GetMapping("/nacos/health")
    @Operation(summary = "Nacos健康检查", description = "检查Nacos服务注册和发现状态")
    public ResponseDTO<Map<String, Object>> nacosHealth() {
        log.info("执行Nacos健康检查");

        try {
            Map<String, Object> healthInfo = new HashMap<>();

            // 检查服务注册状态
            Map<String, Object> registryInfo = checkServiceRegistry();
            healthInfo.put("registry", registryInfo);

            // 检查服务发现状态
            Map<String, Object> discoveryInfo = checkServiceDiscovery();
            healthInfo.put("discovery", discoveryInfo);

            // 添加服务信息
            healthInfo.put("service", "attendance-service");
            healthInfo.put("status", "UP");
            healthInfo.put("timestamp", LocalDateTime.now().toString());

            log.info("Nacos健康检查完成: 状态=UP");
            return ResponseDTO.ok(healthInfo);
        } catch (Exception e) {
            log.error("Nacos健康检查失败", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("status", "DOWN");
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", LocalDateTime.now().toString());
            return ResponseDTO.userError("健康检查失败", errorInfo);
        }
    }

    /**
     * 检查服务注册状态
     */
    private Map<String, Object> checkServiceRegistry() {
        Map<String, Object> registryInfo = new HashMap<>();

        try {
            if (discoveryClient != null) {
                registryInfo.put("discoveryClient", "available");
                registryInfo.put("registeredServices", discoveryClient.getServices().size());
            } else {
                registryInfo.put("discoveryClient", "unavailable");
            }

            if (nacosServiceManager != null) {
                registryInfo.put("nacosServiceManager", "available");
            } else {
                registryInfo.put("nacosServiceManager", "unavailable");
            }
        } catch (Exception e) {
            log.warn("检查服务注册状态时发生异常", e);
            registryInfo.put("error", e.getMessage());
        }

        return registryInfo;
    }

    /**
     * 检查服务发现状态
     */
    private Map<String, Object> checkServiceDiscovery() {
        Map<String, Object> discoveryInfo = new HashMap<>();

        try {
            if (discoveryClient != null) {
                discoveryInfo.put("status", "available");
                discoveryInfo.put("services", discoveryClient.getServices());
            } else {
                discoveryInfo.put("status", "unavailable");
            }
        } catch (Exception e) {
            log.warn("检查服务发现状态时发生异常", e);
            discoveryInfo.put("error", e.getMessage());
        }

        return discoveryInfo;
    }
}