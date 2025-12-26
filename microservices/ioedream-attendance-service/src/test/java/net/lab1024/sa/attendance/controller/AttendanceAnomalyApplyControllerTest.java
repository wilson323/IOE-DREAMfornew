package net.lab1024.sa.attendance.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 考勤异常申请Controller测试
 *
 * 测试说明：这是一个占位测试类，验证Controller类定义正确
 * 后续可根据需要添加具体的单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("考勤异常申请Controller测试")
class AttendanceAnomalyApplyControllerTest {

    @Mock
    private net.lab1024.sa.attendance.service.AttendanceAnomalyApplyService applyService;

    @Mock
    private net.lab1024.sa.attendance.service.AttendanceAnomalyApprovalService approvalService;

    @Test
    @DisplayName("测试：Controller类定义正确")
    void contextLoads() {
        // 这个测试验证Controller类可以被正常实例化（使用Mock依赖）
        AttendanceAnomalyApplyController controller = new AttendanceAnomalyApplyController(
                applyService, approvalService
        );
        org.junit.jupiter.api.Assertions.assertNotNull(controller, "Controller应该可以正常实例化");
    }
}
