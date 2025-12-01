package net.lab1024.sa.attendance.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.manager.AttendanceNotificationManager;
import net.lab1024.sa.attendance.rule.AttendanceRuleEngine;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;

/**
 * 考勤异常处理服务
 *
 * <p>
 * 考勤模块的专门异常处理服务，提供全面的考勤异常检测、处理和管理功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Service层，提供复杂的异常处理和业务协调逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - 异常检测：智能检测各类考勤异常情况
 * - 异常处理：异常记录的审批、驳回、修正等处理流程
 * - 规则验证：基于考勤规则的异常验证和判断
 * - 流程管理：异常处理的完整流程管理
 * - 通知提醒：异常情况的通知和提醒
 * - 数据修正：考勤数据的手动修正和调整
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AttendanceExceptionService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao; // 直接注入DAO接口

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    @Resource
    private AttendanceNotificationManager attendanceNotificationManager;

    // ===== 考勤异常检测服务 =====

    /**
     * 检测员工当日考勤异常
     *
     * @param employeeId 员工ID
     * @param date       考勤日期
     * @return 检测结果
     */
    public ExceptionDetectionResult detectDailyExceptions(Long employeeId, LocalDate date) {
        try {
            log.info("开始检测员工当日考勤异常：员工ID={}, 日期={}", employeeId, date);

            ExceptionDetectionResult result = new ExceptionDetectionResult();
            result.setEmployeeId(employeeId);
            result.setDetectionDate(date);
            result.setDetectedExceptions(new ArrayList<>());

            // 1. 查询当日考勤记录
            AttendanceRecordEntity record = attendanceRecordDao.selectEmployeeTodayRecord(employeeId, date);

            if (record == null) {
                // 全天缺勤
                ExceptionInfo absenceException = new ExceptionInfo();
                absenceException.setExceptionType("ABSENCE");
                absenceException.setDescription("全天缺勤");
                absenceException.setSeverity("HIGH");
                absenceException.setDetectedTime(LocalDateTime.now());
                result.getDetectedExceptions().add(absenceException);
            } else {
                // 2. 应用考勤规则检测异常
                AttendanceRuleEngine.AttendanceRuleProcessResult processResult = attendanceRuleEngine
                        .processAttendanceRecord(record);

                // 3. 提取异常信息
                if (!processResult.isSuccess()) {
                    ExceptionInfo processException = new ExceptionInfo();
                    processException.setExceptionType("PROCESS_ERROR");
                    processException.setDescription(processResult.getErrorMessage());
                    processException.setSeverity("HIGH");
                    processException.setDetectedTime(LocalDateTime.now());
                    result.getDetectedExceptions().add(processException);
                } else if (processResult.getExceptionType() != null) {
                    ExceptionInfo businessException = new ExceptionInfo();
                    businessException.setExceptionType(processResult.getExceptionType());
                    businessException.setDescription(processResult.getExceptionDescription());
                    businessException.setSeverity(determineExceptionSeverity(processResult.getExceptionType()));
                    businessException.setDetectedTime(LocalDateTime.now());
                    result.getDetectedExceptions().add(businessException);
                }
            }

            // 4. 设置检测结果状态
            result.setHasExceptions(!result.getDetectedExceptions().isEmpty());
            result.setTotalExceptionCount(result.getDetectedExceptions().size());
            result.setDetectionTime(LocalDateTime.now());

            log.info("考勤异常检测完成：员工ID={}, 日期={}, 异常数量={}",
                    employeeId, date, result.getTotalExceptionCount());

            return result;

        } catch (Exception e) {
            log.error("检测员工当日考勤异常异常：员工ID={}, 日期={}", employeeId, date, e);
            return ExceptionDetectionResult.failure("检测异常：" + e.getMessage());
        }
    }

    /**
     * 批量检测员工考勤异常
     *
     * @param employeeIds 员工ID列表
     * @param date        考勤日期
     * @return 检测结果列表
     */
    public List<ExceptionDetectionResult> batchDetectExceptions(List<Long> employeeIds, LocalDate date) {
        try {
            log.info("开始批量检测考勤异常：员工数={}, 日期={}", employeeIds.size(), date);

            return employeeIds.stream()
                    .map(employeeId -> detectDailyExceptions(employeeId, date))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("批量检测考勤异常异常：日期={}", date, e);
            return List.of();
        }
    }

    /**
     * 检测指定日期范围的所有异常
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 异常统计结果
     */
    public ExceptionSummary detectRangeExceptions(LocalDate startDate, LocalDate endDate, Long departmentId) {
        try {
            log.info("开始检测指定日期范围考勤异常：{} 到 {}, 部门={}", startDate, endDate, departmentId);

            ExceptionSummary summary = new ExceptionSummary();
            summary.setStartDate(startDate);
            summary.setEndDate(endDate);
            summary.setDepartmentId(departmentId);
            summary.setDetectionTime(LocalDateTime.now());

            // 1. 查询该日期范围的异常记录
            List<AttendanceRecordEntity> exceptionRecords = attendanceRecordDao.selectExceptionRecords(
                    startDate, endDate, null, null);

            // 2. 统计异常类型分布
            Map<String, Long> exceptionTypeCount = exceptionRecords.stream()
                    .filter(record -> record.getExceptionType() != null)
                    .collect(Collectors.groupingBy(
                            record -> record.getExceptionType(), Collectors.counting()));

            // 3. 统计处理状态分布
            Map<String, Long> processStatusCount = exceptionRecords.stream()
                    .collect(Collectors.groupingBy(
                            record -> (record.getIsProcessed() != null && record.getIsProcessed() == 1) ? "PROCESSED" : "PENDING",
                            Collectors.counting()));

            // 4. 设置汇总结果
            summary.setTotalExceptionCount(exceptionRecords.size());
            summary.setExceptionTypeDistribution(exceptionTypeCount);
            summary.setProcessStatusDistribution(processStatusCount);
            summary.setPendingExceptionCount(processStatusCount.getOrDefault("PENDING", 0L).intValue());
            summary.setProcessedExceptionCount(processStatusCount.getOrDefault("PROCESSED", 0L).intValue());

            log.info("指定日期范围考勤异常检测完成：总异常数={}, 待处理={}",
                    summary.getTotalExceptionCount(), summary.getPendingExceptionCount());

            return summary;

        } catch (Exception e) {
            log.error("检测指定日期范围考勤异常异常：{} 到 {}", startDate, endDate, e);
            return new ExceptionSummary();
        }
    }

    // ===== 异常处理服务 =====

    /**
     * 处理考勤异常
     *
     * @param recordId      记录ID
     * @param processAction 处理动作（APPROVE/REJECT/MODIFY）
     * @param processReason 处理原因
     * @param processedBy   处理人ID
     * @param modifications 修改内容（仅当processAction为MODIFY时使用）
     * @return 处理结果
     */
    public ExceptionProcessResult processException(Long recordId, String processAction,
            String processReason, Long processedBy,
            AttendanceRecordModifications modifications) {
        try {
            log.info("开始处理考勤异常：记录ID={}, 处理动作={}, 处理人={}", recordId, processAction, processedBy);

            // 1. 查询考勤记录
            AttendanceRecordEntity record = attendanceRecordDao.selectById(recordId);
            if (record == null) {
                return ExceptionProcessResult.failure("考勤记录不存在");
            }

            ExceptionProcessResult result = new ExceptionProcessResult();
            result.setRecordId(recordId);
            result.setProcessAction(processAction);
            result.setProcessedBy(processedBy);
            result.setProcessTime(LocalDateTime.now());

            // 2. 根据处理动作执行相应操作
            switch (processAction.toUpperCase()) {
                case "APPROVE":
                    result = approveException(record, processReason, processedBy, result);
                    break;
                case "REJECT":
                    result = rejectException(record, processReason, processedBy, result);
                    break;
                case "MODIFY":
                    if (modifications == null) {
                        return ExceptionProcessResult.failure("修改内容不能为空");
                    }
                    result = modifyException(record, modifications, processReason, processedBy, result);
                    break;
                default:
                    return ExceptionProcessResult.failure("不支持的处理动作：" + processAction);
            }

            // 3. 发送处理结果通知
            if (result.isSuccess()) {
                sendProcessNotification(record, processAction, processedBy);
            }

            log.info("考勤异常处理完成：记录ID={}, 处理动作={}, 处理结果={}",
                    recordId, processAction, result.isSuccess() ? "成功" : "失败");

            return result;

        } catch (Exception e) {
            log.error("处理考勤异常异常：记录ID={}, 处理动作={}", recordId, processAction, e);
            return ExceptionProcessResult.failure("处理异常：" + e.getMessage());
        }
    }

    /**
     * 批量处理考勤异常
     *
     * @param recordIds     记录ID列表
     * @param processAction 处理动作
     * @param processReason 处理原因
     * @param processedBy   处理人ID
     * @return 批量处理结果
     */
    public BatchProcessResult batchProcessExceptions(List<Long> recordIds, String processAction,
            String processReason, Long processedBy) {
        try {
            log.info("开始批量处理考勤异常：记录数={}, 处理动作={}", recordIds.size(), processAction);

            BatchProcessResult batchResult = new BatchProcessResult();
            batchResult.setTotalRecords(recordIds.size());
            batchResult.setProcessAction(processAction);
            batchResult.setProcessedBy(processedBy);
            batchResult.setProcessTime(LocalDateTime.now());

            List<ExceptionProcessResult> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            // 1. 逐个处理记录
            for (Long recordId : recordIds) {
                try {
                    ExceptionProcessResult result = processException(recordId, processAction, processReason,
                            processedBy, null);
                    results.add(result);

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                    }

                } catch (Exception e) {
                    log.error("批量处理中单个记录失败：记录ID={}", recordId, e);
                    failureCount++;
                    results.add(ExceptionProcessResult.failure("处理失败：" + e.getMessage()));
                }
            }

            // 2. 设置批量处理结果
            batchResult.setSuccessCount(successCount);
            batchResult.setFailureCount(failureCount);
            batchResult.setProcessResults(results);
            batchResult.setSuccess(failureCount == 0);

            log.info("批量处理考勤异常完成：总数={}, 成功={}, 失败={}",
                    batchResult.getTotalRecords(), successCount, failureCount);

            return batchResult;

        } catch (Exception e) {
            log.error("批量处理考勤异常异常：记录数={}", recordIds.size(), e);
            return BatchProcessResult.failure("批量处理异常：" + e.getMessage());
        }
    }

    /**
     * 自动处理简单异常
     *
     * @param employeeId 员工ID
     * @param date       考勤日期
     * @return 自动处理结果
     */
    public AutoProcessResult autoProcessSimpleExceptions(Long employeeId, LocalDate date) {
        try {
            log.info("开始自动处理简单异常：员工ID={}, 日期={}", employeeId, date);

            AutoProcessResult result = new AutoProcessResult();
            result.setEmployeeId(employeeId);
            result.setProcessDate(date);
            result.setProcessTime(LocalDateTime.now());

            // 1. 检测异常
            ExceptionDetectionResult detectionResult = detectDailyExceptions(employeeId, date);
            if (!detectionResult.isHasExceptions()) {
                result.setSuccess(true);
                result.setMessage("无需处理的异常");
                return result;
            }

            List<ExceptionInfo> processedExceptions = new ArrayList<>();
            List<ExceptionInfo> skippedExceptions = new ArrayList<>();

            // 2. 处理每个异常
            for (ExceptionInfo exception : detectionResult.getDetectedExceptions()) {
                if (canAutoProcess(exception)) {
                    try {
                        ExceptionProcessResult processResult = autoProcessSingleException(employeeId, date, exception);
                        if (processResult.isSuccess()) {
                            processedExceptions.add(exception);
                        } else {
                            skippedExceptions.add(exception);
                        }
                    } catch (Exception e) {
                        log.warn("自动处理单个异常失败：异常类型={}", exception.getExceptionType(), e);
                        skippedExceptions.add(exception);
                    }
                } else {
                    skippedExceptions.add(exception);
                }
            }

            // 3. 设置结果
            result.setSuccess(true);
            result.setProcessedCount(processedExceptions.size());
            result.setSkippedCount(skippedExceptions.size());
            result.setProcessedExceptions(processedExceptions);
            result.setSkippedExceptions(skippedExceptions);

            log.info("自动处理简单异常完成：员工ID={}, 处理数={}, 跳过数={}",
                    employeeId, processedExceptions.size(), skippedExceptions.size());

            return result;

        } catch (Exception e) {
            log.error("自动处理简单异常异常：员工ID={}, 日期={}", employeeId, date, e);
            return AutoProcessResult.failure("自动处理异常：" + e.getMessage());
        }
    }

    // ===== 异常统计分析服务 =====

    /**
     * 生成异常统计报告
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @param employeeId   员工ID（可选）
     * @return 异常统计报告
     */
    public ExceptionStatisticsReport generateExceptionStatistics(LocalDate startDate, LocalDate endDate,
            Long departmentId, Long employeeId) {
        try {
            log.info("开始生成异常统计报告：{} 到 {}, 部门={}, 员工={}",
                    startDate, endDate, departmentId, employeeId);

            ExceptionStatisticsReport report = new ExceptionStatisticsReport();
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setDepartmentId(departmentId);
            report.setEmployeeId(employeeId);
            report.setGenerateTime(LocalDateTime.now());

            // 1. 查询异常记录
            List<AttendanceRecordEntity> exceptionRecords = attendanceRecordDao.selectExceptionRecords(
                    startDate, endDate, null, null);

            // 2. 如果指定了员工，则过滤
            if (employeeId != null) {
                exceptionRecords = exceptionRecords.stream()
                        .filter(record -> employeeId.equals(record.getEmployeeId()))
                        .collect(Collectors.toList());
            }

            // 3. 统计分析
            Map<String, Long> exceptionTypeStats = exceptionRecords.stream()
                    .filter(record -> record.getExceptionType() != null)
                    .collect(Collectors.groupingBy(record -> record.getExceptionType(), Collectors.counting()));

            Map<String, Long> processStatusStats = exceptionRecords.stream()
                    .collect(Collectors.groupingBy(
                            record -> (record.getIsProcessed() != null && record.getIsProcessed() == 1) ? "PROCESSED" : "PENDING",
                            Collectors.counting()));

            // 4. 设置统计结果
            report.setTotalExceptionCount(exceptionRecords.size());
            report.setExceptionTypeStatistics(exceptionTypeStats);
            report.setProcessStatusStatistics(processStatusStats);
            report.setPendingExceptionCount(processStatusStats.getOrDefault("PENDING", 0L).intValue());
            report.setProcessedExceptionCount(processStatusStats.getOrDefault("PROCESSED", 0L).intValue());

            // 5. 计算异常趋势
            report.setExceptionTrends(calculateExceptionTrends(exceptionRecords, startDate, endDate));

            log.info("异常统计报告生成完成：总异常数={}, 待处理={}",
                    report.getTotalExceptionCount(), report.getPendingExceptionCount());

            return report;

        } catch (Exception e) {
            log.error("生成异常统计报告异常：{} 到 {}", startDate, endDate, e);
            return new ExceptionStatisticsReport();
        }
    }

    // ===== 辅助方法 =====

    /**
     * 判断异常严重程度
     *
     * @param exceptionType 异常类型
     * @return 严重程度
     */
    private String determineExceptionSeverity(String exceptionType) {
        switch (exceptionType) {
            case "ABSENCE":
                return "HIGH";
            case "LATE":
            case "EARLY_LEAVE":
                return "MEDIUM";
            case "MISSING_PUNCH_IN":
            case "MISSING_PUNCH_OUT":
                return "LOW";
            case "INSUFFICIENT_HOURS":
                return "MEDIUM";
            default:
                return "MEDIUM";
        }
    }

    /**
     * 批准异常
     */
    private ExceptionProcessResult approveException(AttendanceRecordEntity record, String reason, Long processedBy,
            ExceptionProcessResult result) {
        try {
            record.setIsProcessed(1);
            record.setProcessType("APPROVE");
            record.setProcessReason(reason);
            record.setProcessedBy(processedBy);
            record.setProcessedTime(LocalDateTime.now());

            int updateResult = attendanceRecordDao.updateById(record);
            result.setSuccess(updateResult > 0);
            result.setMessage(updateResult > 0 ? "异常批准成功" : "更新记录失败");

            return result;

        } catch (Exception e) {
            log.error("批准考勤异常失败：记录ID={}", record.getRecordId(), e);
            result.setSuccess(false);
            result.setMessage("批准异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 驳回异常
     */
    private ExceptionProcessResult rejectException(AttendanceRecordEntity record, String reason, Long processedBy,
            ExceptionProcessResult result) {
        try {
            record.setIsProcessed(1);
            record.setProcessType("REJECT");
            record.setProcessReason(reason);
            record.setProcessedBy(processedBy);
            record.setProcessedTime(LocalDateTime.now());

            int updateResult = attendanceRecordDao.updateById(record);
            result.setSuccess(updateResult > 0);
            result.setMessage(updateResult > 0 ? "异常驳回成功" : "更新记录失败");

            return result;

        } catch (Exception e) {
            log.error("驳回考勤异常失败：记录ID={}", record.getRecordId(), e);
            result.setSuccess(false);
            result.setMessage("驳回异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 修改异常记录
     */
    private ExceptionProcessResult modifyException(AttendanceRecordEntity record,
            AttendanceRecordModifications modifications, String reason, Long processedBy,
            ExceptionProcessResult result) {
        try {
            // 应用修改
            if (modifications.getPunchInTime() != null) {
                record.setPunchInTime(modifications.getPunchInTime().toLocalTime());
            }
            if (modifications.getPunchOutTime() != null) {
                record.setPunchOutTime(modifications.getPunchOutTime().toLocalTime());
            }
            if (modifications.getActualWorkHours() != null) {
                record.setActualWorkHours(modifications.getActualWorkHours());
            }
            if (modifications.getOvertimeHours() != null) {
                record.setOvertimeHours(modifications.getOvertimeHours());
            }

            // 重新应用规则
            AttendanceRuleEngine.AttendanceRuleProcessResult processResult = attendanceRuleEngine
                    .processAttendanceRecord(record);
            if (!processResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("重新应用规则失败：" + processResult.getErrorMessage());
                return result;
            }

            // 设置处理信息
            record.setIsProcessed(1);
            record.setProcessType("MODIFY");
            record.setProcessReason(reason);
            record.setProcessedBy(processedBy);
            record.setProcessedTime(LocalDateTime.now());

            int updateResult = attendanceRecordDao.updateById(record);
            result.setSuccess(updateResult > 0);
            result.setMessage(updateResult > 0 ? "异常修改成功" : "更新记录失败");

            return result;

        } catch (Exception e) {
            log.error("修改考勤异常失败：记录ID={}", record.getRecordId(), e);
            result.setSuccess(false);
            result.setMessage("修改异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 发送处理结果通知
     */
    private void sendProcessNotification(AttendanceRecordEntity record, String processAction, Long processedBy) {
        try {
            // 这里应该根据处理结果发送相应的通知
            log.info("发送异常处理通知：员工ID={}, 处理动作={}, 处理人={}",
                    record.getEmployeeId(), processAction, processedBy);

        } catch (Exception e) {
            log.error("发送异常处理通知异常", e);
        }
    }

    /**
     * 判断是否可以自动处理
     */
    private boolean canAutoProcess(ExceptionInfo exception) {
        // 简单的异常可以自动处理
        return "MISSING_PUNCH_IN".equals(exception.getExceptionType()) ||
                "MISSING_PUNCH_OUT".equals(exception.getExceptionType());
    }

    /**
     * 自动处理单个异常
     */
    private ExceptionProcessResult autoProcessSingleException(Long employeeId, LocalDate date,
            ExceptionInfo exception) {
        try {
            // 根据异常类型执行自动处理逻辑
            switch (exception.getExceptionType()) {
                case "MISSING_PUNCH_IN":
                    // 自动设置上班打卡时间为标准上班时间
                    return autoSetPunchInTime(employeeId, date);
                case "MISSING_PUNCH_OUT":
                    // 自动设置下班打卡时间为标准下班时间
                    return autoSetPunchOutTime(employeeId, date);
                default:
                    return ExceptionProcessResult.failure("该异常类型不支持自动处理");
            }

        } catch (Exception e) {
            log.error("自动处理单个异常失败：异常类型={}", exception.getExceptionType(), e);
            return ExceptionProcessResult.failure("自动处理失败：" + e.getMessage());
        }
    }

    /**
     * 自动设置上班打卡时间
     */
    private ExceptionProcessResult autoSetPunchInTime(Long employeeId, LocalDate date) {
        try {
            LocalTime standardStartTime = attendanceRuleEngine.getStandardWorkStartTime(employeeId, date);
            if (standardStartTime == null) {
                return ExceptionProcessResult.failure("无法获取标准上班时间");
            }

            AttendanceRecordEntity record = attendanceRecordDao.selectEmployeeTodayRecord(employeeId, date);
            if (record == null) {
                record = createNewRecord(employeeId, date);
            }

            record.setPunchInTime(standardStartTime);
            int updateResult = record.getRecordId() == null ? attendanceRecordDao.save(record)
                    : attendanceRecordDao.updateById(record);

            ExceptionProcessResult result = new ExceptionProcessResult();
            result.setSuccess(updateResult > 0);
            result.setMessage(updateResult > 0 ? "自动设置上班时间成功" : "设置失败");

            return result;

        } catch (Exception e) {
            log.error("自动设置上班打卡时间失败：员工ID={}, 日期={}", employeeId, date, e);
            return ExceptionProcessResult.failure("设置上班时间失败：" + e.getMessage());
        }
    }

    /**
     * 自动设置下班打卡时间
     */
    private ExceptionProcessResult autoSetPunchOutTime(Long employeeId, LocalDate date) {
        try {
            LocalTime standardEndTime = attendanceRuleEngine.getStandardWorkEndTime(employeeId, date);
            if (standardEndTime == null) {
                return ExceptionProcessResult.failure("无法获取标准下班时间");
            }

            AttendanceRecordEntity record = attendanceRecordDao.selectEmployeeTodayRecord(employeeId, date);
            if (record == null) {
                return ExceptionProcessResult.failure("未找到考勤记录");
            }

            record.setPunchOutTime(standardEndTime);
            int updateResult = attendanceRecordDao.updateById(record);

            ExceptionProcessResult result = new ExceptionProcessResult();
            result.setSuccess(updateResult > 0);
            result.setMessage(updateResult > 0 ? "自动设置下班时间成功" : "设置失败");

            return result;

        } catch (Exception e) {
            log.error("自动设置下班打卡时间失败：员工ID={}, 日期={}", employeeId, date, e);
            return ExceptionProcessResult.failure("设置下班时间失败：" + e.getMessage());
        }
    }

    /**
     * 创建新的考勤记录
     */
    private AttendanceRecordEntity createNewRecord(Long employeeId, LocalDate date) {
        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(date);  // 直接使用LocalDate
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeletedFlag(0);
        return record;
    }

    /**
     * 计算异常趋势
     */
    private Map<String, Integer> calculateExceptionTrends(List<AttendanceRecordEntity> records, LocalDate startDate,
            LocalDate endDate) {
        // 这里应该计算异常趋势数据
        // 暂时返回空Map
        return Map.of();
    }

    // ===== VO类定义 =====

    /**
     * 异常检测结果
     */
    public static class ExceptionDetectionResult {
        private Long employeeId;
        private LocalDate detectionDate;
        private boolean hasExceptions;
        private int totalExceptionCount;
        private List<ExceptionInfo> detectedExceptions;
        private LocalDateTime detectionTime;
        private boolean success;
        private String errorMessage;

        public static ExceptionDetectionResult failure(String message) {
            ExceptionDetectionResult result = new ExceptionDetectionResult();
            result.setSuccess(false);
            result.setErrorMessage(message);
            return result;
        }

        // Getters and Setters
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getDetectionDate() {
            return detectionDate;
        }

        public void setDetectionDate(LocalDate detectionDate) {
            this.detectionDate = detectionDate;
        }

        public boolean isHasExceptions() {
            return hasExceptions;
        }

        public void setHasExceptions(boolean hasExceptions) {
            this.hasExceptions = hasExceptions;
        }

        public int getTotalExceptionCount() {
            return totalExceptionCount;
        }

        public void setTotalExceptionCount(int totalExceptionCount) {
            this.totalExceptionCount = totalExceptionCount;
        }

        public List<ExceptionInfo> getDetectedExceptions() {
            return detectedExceptions;
        }

        public void setDetectedExceptions(List<ExceptionInfo> detectedExceptions) {
            this.detectedExceptions = detectedExceptions;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 异常信息
     */
    public static class ExceptionInfo {
        private String exceptionType;
        private String description;
        private String severity;
        private LocalDateTime detectedTime;

        // Getters and Setters
        public String getExceptionType() {
            return exceptionType;
        }

        public void setExceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public LocalDateTime getDetectedTime() {
            return detectedTime;
        }

        public void setDetectedTime(LocalDateTime detectedTime) {
            this.detectedTime = detectedTime;
        }
    }

    /**
     * 异常汇总
     */
    public static class ExceptionSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long departmentId;
        private LocalDateTime detectionTime;
        private int totalExceptionCount;
        private Map<String, Long> exceptionTypeDistribution;
        private Map<String, Long> processStatusDistribution;
        private int pendingExceptionCount;
        private int processedExceptionCount;

        // Getters and Setters
        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }

        public int getTotalExceptionCount() {
            return totalExceptionCount;
        }

        public void setTotalExceptionCount(int totalExceptionCount) {
            this.totalExceptionCount = totalExceptionCount;
        }

        public Map<String, Long> getExceptionTypeDistribution() {
            return exceptionTypeDistribution;
        }

        public void setExceptionTypeDistribution(Map<String, Long> exceptionTypeDistribution) {
            this.exceptionTypeDistribution = exceptionTypeDistribution;
        }

        public Map<String, Long> getProcessStatusDistribution() {
            return processStatusDistribution;
        }

        public void setProcessStatusDistribution(Map<String, Long> processStatusDistribution) {
            this.processStatusDistribution = processStatusDistribution;
        }

        public int getPendingExceptionCount() {
            return pendingExceptionCount;
        }

        public void setPendingExceptionCount(int pendingExceptionCount) {
            this.pendingExceptionCount = pendingExceptionCount;
        }

        public int getProcessedExceptionCount() {
            return processedExceptionCount;
        }

        public void setProcessedExceptionCount(int processedExceptionCount) {
            this.processedExceptionCount = processedExceptionCount;
        }
    }

    /**
     * 异常处理结果
     */
    public static class ExceptionProcessResult {
        private Long recordId;
        private String processAction;
        private Long processedBy;
        private LocalDateTime processTime;
        private boolean success;
        private String message;

        public static ExceptionProcessResult failure(String message) {
            ExceptionProcessResult result = new ExceptionProcessResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }

        // Getters and Setters
        public Long getRecordId() {
            return recordId;
        }

        public void setRecordId(Long recordId) {
            this.recordId = recordId;
        }

        public String getProcessAction() {
            return processAction;
        }

        public void setProcessAction(String processAction) {
            this.processAction = processAction;
        }

        public Long getProcessedBy() {
            return processedBy;
        }

        public void setProcessedBy(Long processedBy) {
            this.processedBy = processedBy;
        }

        public LocalDateTime getProcessTime() {
            return processTime;
        }

        public void setProcessTime(LocalDateTime processTime) {
            this.processTime = processTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 考勤记录修改内容
     */
    public static class AttendanceRecordModifications {
        private LocalDateTime punchInTime;
        private LocalDateTime punchOutTime;
        private java.math.BigDecimal actualWorkHours;
        private java.math.BigDecimal overtimeHours;

        // Getters and Setters
        public LocalDateTime getPunchInTime() {
            return punchInTime;
        }

        public void setPunchInTime(LocalDateTime punchInTime) {
            this.punchInTime = punchInTime;
        }

        public LocalDateTime getPunchOutTime() {
            return punchOutTime;
        }

        public void setPunchOutTime(LocalDateTime punchOutTime) {
            this.punchOutTime = punchOutTime;
        }

        public java.math.BigDecimal getActualWorkHours() {
            return actualWorkHours;
        }

        public void setActualWorkHours(java.math.BigDecimal actualWorkHours) {
            this.actualWorkHours = actualWorkHours;
        }

        public java.math.BigDecimal getOvertimeHours() {
            return overtimeHours;
        }

        public void setOvertimeHours(java.math.BigDecimal overtimeHours) {
            this.overtimeHours = overtimeHours;
        }
    }

    /**
     * 批量处理结果
     */
    public static class BatchProcessResult {
        private int totalRecords;
        private String processAction;
        private Long processedBy;
        private LocalDateTime processTime;
        private int successCount;
        private int failureCount;
        private boolean success;
        private List<ExceptionProcessResult> processResults;

        public static BatchProcessResult failure(String message) {
            BatchProcessResult result = new BatchProcessResult();
            result.setSuccess(false);
            return result;
        }

        // Getters and Setters
        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public String getProcessAction() {
            return processAction;
        }

        public void setProcessAction(String processAction) {
            this.processAction = processAction;
        }

        public Long getProcessedBy() {
            return processedBy;
        }

        public void setProcessedBy(Long processedBy) {
            this.processedBy = processedBy;
        }

        public LocalDateTime getProcessTime() {
            return processTime;
        }

        public void setProcessTime(LocalDateTime processTime) {
            this.processTime = processTime;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<ExceptionProcessResult> getProcessResults() {
            return processResults;
        }

        public void setProcessResults(List<ExceptionProcessResult> processResults) {
            this.processResults = processResults;
        }
    }

    /**
     * 自动处理结果
     */
    public static class AutoProcessResult {
        private Long employeeId;
        private LocalDate processDate;
        private LocalDateTime processTime;
        private boolean success;
        private String message;
        private int processedCount;
        private int skippedCount;
        private List<ExceptionInfo> processedExceptions;
        private List<ExceptionInfo> skippedExceptions;

        public static AutoProcessResult failure(String message) {
            AutoProcessResult result = new AutoProcessResult();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }

        // Getters and Setters
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDate getProcessDate() {
            return processDate;
        }

        public void setProcessDate(LocalDate processDate) {
            this.processDate = processDate;
        }

        public LocalDateTime getProcessTime() {
            return processTime;
        }

        public void setProcessTime(LocalDateTime processTime) {
            this.processTime = processTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public void setProcessedCount(int processedCount) {
            this.processedCount = processedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public void setSkippedCount(int skippedCount) {
            this.skippedCount = skippedCount;
        }

        public List<ExceptionInfo> getProcessedExceptions() {
            return processedExceptions;
        }

        public void setProcessedExceptions(List<ExceptionInfo> processedExceptions) {
            this.processedExceptions = processedExceptions;
        }

        public List<ExceptionInfo> getSkippedExceptions() {
            return skippedExceptions;
        }

        public void setSkippedExceptions(List<ExceptionInfo> skippedExceptions) {
            this.skippedExceptions = skippedExceptions;
        }
    }

    /**
     * 异常统计报告
     */
    public static class ExceptionStatisticsReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long departmentId;
        private Long employeeId;
        private LocalDateTime generateTime;
        private int totalExceptionCount;
        private Map<String, Long> exceptionTypeStatistics;
        private Map<String, Long> processStatusStatistics;
        private int pendingExceptionCount;
        private int processedExceptionCount;
        private Map<String, Integer> exceptionTrends;

        // Getters and Setters
        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDateTime getGenerateTime() {
            return generateTime;
        }

        public void setGenerateTime(LocalDateTime generateTime) {
            this.generateTime = generateTime;
        }

        public int getTotalExceptionCount() {
            return totalExceptionCount;
        }

        public void setTotalExceptionCount(int totalExceptionCount) {
            this.totalExceptionCount = totalExceptionCount;
        }

        public Map<String, Long> getExceptionTypeStatistics() {
            return exceptionTypeStatistics;
        }

        public void setExceptionTypeStatistics(Map<String, Long> exceptionTypeStatistics) {
            this.exceptionTypeStatistics = exceptionTypeStatistics;
        }

        public Map<String, Long> getProcessStatusStatistics() {
            return processStatusStatistics;
        }

        public void setProcessStatusStatistics(Map<String, Long> processStatusStatistics) {
            this.processStatusStatistics = processStatusStatistics;
        }

        public int getPendingExceptionCount() {
            return pendingExceptionCount;
        }

        public void setPendingExceptionCount(int pendingExceptionCount) {
            this.pendingExceptionCount = pendingExceptionCount;
        }

        public int getProcessedExceptionCount() {
            return processedExceptionCount;
        }

        public void setProcessedExceptionCount(int processedExceptionCount) {
            this.processedExceptionCount = processedExceptionCount;
        }

        public Map<String, Integer> getExceptionTrends() {
            return exceptionTrends;
        }

        public void setExceptionTrends(Map<String, Integer> exceptionTrends) {
            this.exceptionTrends = exceptionTrends;
        }
    }
}