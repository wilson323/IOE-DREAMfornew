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

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.service.AccessDeviceService;

/**
 * AccessDeviceController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AccessDeviceController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessDeviceController单元测试")
class AccessDeviceControllerTest {
    @Mock
    private AccessDeviceService accessDeviceService;

    @InjectMocks
    private AccessDeviceController accessDeviceController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("queryDevices-成功场景-返回分页结果")
    void test_queryDevices_Success_ReturnsPageResult() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        String keyword = "门禁";
        Long areaId = 4001L;
        String deviceStatus = "ONLINE";
        Integer enabledFlag = 1;

        PageResult<AccessDeviceVO> pageResult = new PageResult<>();
        when(accessDeviceService.queryDevices(any(net.lab1024.sa.access.domain.form.AccessDeviceQueryForm.class)))
            .thenReturn(ResponseDTO.ok(pageResult));

        // When
        ResponseDTO<PageResult<AccessDeviceVO>> response = accessDeviceController.queryDevices(
            pageNum, pageSize, keyword, areaId, deviceStatus, enabledFlag);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(accessDeviceService).queryDevices(any(net.lab1024.sa.access.domain.form.AccessDeviceQueryForm.class));
    }
}
