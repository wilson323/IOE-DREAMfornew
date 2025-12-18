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
 * AccessDeviceController鍗曞厓娴嬭瘯
 * <p>
 * 鐩爣瑕嗙洊鐜囷細>= 80%
 * 娴嬭瘯鑼冨洿锛欰ccessDeviceController鏍稿績API鏂规硶
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessDeviceController鍗曞厓娴嬭瘯")
class AccessDeviceControllerTest {
    @Mock
    private AccessDeviceService accessDeviceService;

    @InjectMocks
    private AccessDeviceController accessDeviceController;

    @BeforeEach
    void setUp() {
        // 鍑嗗娴嬭瘯鏁版嵁
    }

    @Test
    @DisplayName("queryDevices-鎴愬姛鍦烘櫙-杩斿洖鍒嗛〉缁撴灉")
    void test_queryDevices_Success_ReturnsPageResult() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        String keyword = "闂ㄧ";
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
