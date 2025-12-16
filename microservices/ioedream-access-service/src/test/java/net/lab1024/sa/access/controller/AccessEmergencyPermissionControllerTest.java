package net.lab1024.sa.access.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.access.entity.AccessPermissionApplyEntity;
import net.lab1024.sa.access.domain.form.AccessPermissionApplyForm;
import net.lab1024.sa.access.service.AccessEmergencyPermissionService;

/**
 * AccessEmergencyPermissionController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AccessEmergencyPermissionController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessEmergencyPermissionController单元测试")
class AccessEmergencyPermissionControllerTest {
    @Mock
    private AccessEmergencyPermissionService accessEmergencyPermissionService;

    @InjectMocks
    private AccessEmergencyPermissionController accessEmergencyPermissionController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("submitEmergencyPermissionApply-成功场景-返回权限申请实体")
    void test_submitEmergencyPermissionApply_Success_ReturnsPermissionApplyEntity() {
        // Given
        AccessPermissionApplyForm form = new AccessPermissionApplyForm();
        form.setApplicantId(1L);
        form.setAreaId(1L);  // 修复：areaId是Long类型
        form.setApplyType("EMERGENCY");

        AccessPermissionApplyEntity entity = new AccessPermissionApplyEntity();
        entity.setApplyNo("EMERGENCY001");
        when(accessEmergencyPermissionService.submitEmergencyPermissionApply(any(AccessPermissionApplyForm.class)))
            .thenReturn(entity);

        // When
        ResponseDTO<AccessPermissionApplyEntity> response = accessEmergencyPermissionController.submitEmergencyPermissionApply(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("EMERGENCY001", response.getData().getApplyNo());
        verify(accessEmergencyPermissionService).submitEmergencyPermissionApply(any(AccessPermissionApplyForm.class));
    }
}
