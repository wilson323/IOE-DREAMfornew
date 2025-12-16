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
 * AccessRecordController单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：AccessRecordController核心API方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-11
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessRecordController单元测试")
class AccessRecordControllerTest {
    @Mock
    private AccessEventService accessEventService;

    @InjectMocks
    private AccessRecordController accessRecordController;

    @BeforeEach
    void setUp() {
        // 准备测试数据
    }

    @Test
    @DisplayName("queryAccessRecords-成功场景-返回分页结果")
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
    @DisplayName("createAccessRecord-成功场景-返回成功")
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
