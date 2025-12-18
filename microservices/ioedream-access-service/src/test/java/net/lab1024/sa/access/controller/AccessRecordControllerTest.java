package net.lab1024.sa.access.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.access.service.AccessEventService;

/**
 * AccessRecordController鍗曞厓娴嬭瘯
 * <p>
 * 鐩爣瑕嗙洊鐜囷細>= 80%
 * 娴嬭瘯鑼冨洿锛欰ccessRecordController鏍稿績API鏂规硶
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessRecordController鍗曞厓娴嬭瘯")
class AccessRecordControllerTest {
    @Mock
    private AccessEventService accessEventService;

    @InjectMocks
    private AccessRecordController accessRecordController;

    @BeforeEach
    void setUp() {
        // 鍑嗗娴嬭瘯鏁版嵁
    }

    @Test
    @DisplayName("queryAccessRecords-鎴愬姛鍦烘櫙-杩斿洖鍒嗛〉缁撴灉")
    void test_queryAccessRecords_Success_ReturnsPageResult() {
        // Given
        Integer pageNum = 1;
        Integer pageSize = 10;
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        PageResult<AccessRecordVO> pageResult = new PageResult<>();
        when(accessEventService.queryAccessRecords(any(net.lab1024.sa.access.domain.form.AccessRecordQueryForm.class)))
            .thenReturn(ResponseDTO.ok(pageResult));

        // When
        ResponseDTO<PageResult<AccessRecordVO>> response = accessRecordController.queryAccessRecords(
            pageNum, pageSize, null, null, null, startDate, endDate, null);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(accessEventService).queryAccessRecords(any(net.lab1024.sa.access.domain.form.AccessRecordQueryForm.class));
    }

    @Test
    @DisplayName("createAccessRecord-鎴愬姛鍦烘櫙-杩斿洖鎴愬姛")
    void test_createAccessRecord_Success_ReturnsSuccess() {
        // Given
        AccessRecordAddForm form = new AccessRecordAddForm();
        when(accessEventService.createAccessRecord(any(AccessRecordAddForm.class)))
            .thenReturn(ResponseDTO.ok(1L));

        // When
        ResponseDTO<Long> response = accessRecordController.createAccessRecord(form);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        verify(accessEventService).createAccessRecord(any(AccessRecordAddForm.class));
    }
}
