package net.lab1024.sa.attendance.controller;

import net.lab1024.sa.attendance.config.EnhancedTestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyDetectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import net.lab1024.sa.attendance.monitor.ApiPerformanceMonitor;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.dao.InterlockRecordDao;
import net.lab1024.sa.common.organization.dao.MultiPersonRecordDao;
import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.preference.dao.UserPreferenceDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceLeaveDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeApprovalDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeRuleDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.dao.AttendanceShiftDao;
import net.lab1024.sa.attendance.dao.AttendanceSummaryDao;
import net.lab1024.sa.attendance.dao.AttendanceSupplementDao;
import net.lab1024.sa.attendance.dao.AttendanceTravelDao;
import net.lab1024.sa.attendance.dao.DepartmentStatisticsDao;
import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.dao.ScheduleTemplateDao;
import net.lab1024.sa.attendance.dao.SmartSchedulePlanDao;
import net.lab1024.sa.attendance.dao.SmartScheduleResultDao;




/**
 * 考勤异常管理Controller测试
 * 测试文件已简化，待后续完善
 */
@WebMvcTest(value = AttendanceAnomalyController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
    })
@Import(EnhancedTestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("考勤异常管理Controller测试")
class AttendanceAnomalyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceAnomalyDetectionService detectionService;
    @MockBean
    private ApiPerformanceMonitor apiPerformanceMonitor;
    @MockBean
    private AccessRecordDao accessRecordDao;
    @MockBean
    private AreaDao areaDao;
    @MockBean
    private DeviceDao deviceDao;    @MockBean
    private AttendanceAnomalyDao attendanceAnomalyDao;
    @MockBean
    private AttendanceRecordDao attendanceRecordDao;
    @MockBean
    private AttendanceRuleConfigDao attendanceRuleConfigDao;
    @MockBean
    private WorkShiftDao workShiftDao;
    @MockBean
    private AntiPassbackRecordDao antiPassbackRecordDao;
    @MockBean
    private AreaAccessExtDao areaAccessExtDao;
    @MockBean
    private AreaDeviceDao areaDeviceDao;
    @MockBean
    private AreaUserDao areaUserDao;
    @MockBean
    private InterlockRecordDao interlockRecordDao;
    @MockBean
    private MultiPersonRecordDao multiPersonRecordDao;
    @MockBean
    private UserAreaPermissionDao userAreaPermissionDao;
    @MockBean
    private UserPreferenceDao userPreferenceDao;
    @MockBean
    private AttendanceAnomalyApplyDao attendanceAnomalyApplyDao;
    @MockBean
    private AttendanceLeaveDao attendanceLeaveDao;
    @MockBean
    private AttendanceOvertimeApplyDao attendanceOvertimeApplyDao;
    @MockBean
    private AttendanceOvertimeApprovalDao attendanceOvertimeApprovalDao;
    @MockBean
    private AttendanceOvertimeDao attendanceOvertimeDao;
    @MockBean
    private AttendanceOvertimeRecordDao attendanceOvertimeRecordDao;
    @MockBean
    private AttendanceOvertimeRuleDao attendanceOvertimeRuleDao;
    @MockBean
    private AttendanceRuleDao attendanceRuleDao;
    @MockBean
    private AttendanceShiftDao attendanceShiftDao;
    @MockBean
    private AttendanceSummaryDao attendanceSummaryDao;
    @MockBean
    private AttendanceSupplementDao attendanceSupplementDao;
    @MockBean
    private AttendanceTravelDao attendanceTravelDao;
    @MockBean
    private DepartmentStatisticsDao departmentStatisticsDao;
    @MockBean
    private ScheduleRecordDao scheduleRecordDao;
    @MockBean
    private ScheduleTemplateDao scheduleTemplateDao;
    @MockBean
    private SmartSchedulePlanDao smartSchedulePlanDao;
    @MockBean
    private SmartScheduleResultDao smartScheduleResultDao;





    @Test
    @DisplayName("测试：Controller可以正常加载")
    void contextLoads() {
        System.out.println("[测试] Controller加载成功");
    }
}
