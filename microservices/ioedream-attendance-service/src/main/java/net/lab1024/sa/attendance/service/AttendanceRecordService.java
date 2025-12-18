package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.domain.form.AttendanceRecordQueryForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordStatisticsVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;

import java.time.LocalDate;

/**
 * 考勤记录服务接口
 * <p>
 * 提供考勤记录查询和统计功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-attendance-service中
 * - 方法命名规范
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceRecordService {

    /**
     * 分页查询考勤记录
     *
     * @param form 查询表单
     * @return 考勤记录分页结果
     */
    ResponseDTO<PageResult<AttendanceRecordVO>> queryAttendanceRecords(AttendanceRecordQueryForm form);

    /**
     * 获取考勤记录统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param employeeId 员工ID（可选）
     * @return 统计数据
     */
    ResponseDTO<AttendanceRecordStatisticsVO> getAttendanceRecordStatistics(LocalDate startDate, LocalDate endDate, Long employeeId);

    /**
     * 创建考勤记录
     * <p>
     * 用于设备协议推送考勤记录
     * 支持设备自动推送和手动创建
     * </p>
     *
     * @param form 考勤记录创建表单
     * @return 创建的考勤记录ID
     */
    ResponseDTO<Long> createAttendanceRecord(net.lab1024.sa.attendance.domain.form.AttendanceRecordAddForm form);
}



