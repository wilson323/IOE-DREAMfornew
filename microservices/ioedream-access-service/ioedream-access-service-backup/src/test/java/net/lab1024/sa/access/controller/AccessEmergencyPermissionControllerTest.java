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
 * AccessEmergencyPermissionController鍗曞厓娴嬭瘯
 * <p>
 * 鐩爣瑕嗙洊鐜囷細>= 80%
 * 娴嬭瘯鑼冨洿锛欰ccessEmergencyPermissionController鏍稿績API鏂规硶
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessEmergencyPermissionController鍗曞厓娴嬭瘯")
class AccessEmergencyPermissionControllerTest {
    @Mock
    private AccessEmergencyPermissionService accessEmergencyPermissionService;

    @InjectMocks
    private AccessEmergencyPermissionController accessEmergencyPermissionController;

    @BeforeEach
    void setUp() {
        // 鍑嗗娴嬭瘯鏁版嵁
    }

    @Test
    @DisplayName("submitEmergencyPermissionApply-鎴愬姛鍦烘櫙-杩斿洖鏉冮檺鐢宠瀹炰綋")
    void test_submitEmergencyPermissionApply_Success_ReturnsPermissionApplyEntity() {
        // Given
        AccessPermissionApplyForm form = new AccessPermissionApplyForm();
        form.setApplicantId(1L);
        form.setAreaId(1L);  // 淇锛歛reaId鏄疞ong绫诲瀷
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
