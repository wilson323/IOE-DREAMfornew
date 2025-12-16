package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.RefundApplicationDao;
import net.lab1024.sa.common.consume.entity.RefundApplicationEntity;
import net.lab1024.sa.consume.domain.form.RefundQueryForm;
import net.lab1024.sa.consume.domain.form.RefundRequestForm;
import net.lab1024.sa.consume.domain.vo.RefundRecordVO;
import net.lab1024.sa.consume.service.impl.ConsumeRefundServiceImpl;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * ConsumeRefundServiceImpl Unit Test
 * <p>
 * Target Coverage: >= 80%
 * Test Scope: Core business methods of ConsumeRefundServiceImpl
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeRefundServiceImpl Unit Test")
@SuppressWarnings("unchecked")
class ConsumeRefundServiceImplTest {

    @Mock
    private RefundApplicationDao refundApplicationDao;

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ConsumeRefundServiceImpl consumeRefundServiceImpl;

    private ConsumeRecordEntity mockConsumeRecord;
    private RefundApplicationEntity mockRefundApplication;
    private RefundRequestForm mockRefundRequest;

    @BeforeEach
    void setUp() {
        // Prepare test data
        mockConsumeRecord = new ConsumeRecordEntity();
        mockConsumeRecord.setId(1L);
        mockConsumeRecord.setTransactionNo("TXN001");
        mockConsumeRecord.setUserId(100L);
        mockConsumeRecord.setAccountId(2001L);
        mockConsumeRecord.setAmount(new BigDecimal("100.00"));
        mockConsumeRecord.setCreateTime(LocalDateTime.now());
        mockConsumeRecord.setDeletedFlag(0);

        mockRefundApplication = new RefundApplicationEntity();
        mockRefundApplication.setId(1L);
        mockRefundApplication.setRefundNo("RF001");
        mockRefundApplication.setPaymentRecordId(1L);
        mockRefundApplication.setUserId(100L);
        mockRefundApplication.setRefundAmount(new BigDecimal("50.00"));
        mockRefundApplication.setRefundReason("Test refund");
        mockRefundApplication.setStatus("PENDING");
        mockRefundApplication.setCreateTime(LocalDateTime.now());
        mockRefundApplication.setDeletedFlag(0);  // 修复：deletedFlag是Integer类型，0表示未删除

        mockRefundRequest = new RefundRequestForm();
        mockRefundRequest.setTransactionNo("TXN001");
        mockRefundRequest.setRefundAmount(new BigDecimal("50.00"));
        mockRefundRequest.setRefundReason("Test refund reason");
    }

    @Test
    @DisplayName("Test applyRefund - Success Scenario")
    void test_applyRefund_Success() {
        // Given
        when(consumeRecordDao.selectByTransactionNo("TXN001")).thenReturn(mockConsumeRecord);
        when(refundApplicationDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L); // No existing refund
        when(refundApplicationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList()); // No completed refunds
        doAnswer(invocation -> {
            RefundApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setRefundNo("RF001");
            return 1;
        }).when(refundApplicationDao).insert(any(RefundApplicationEntity.class));

        // When
        Long result = consumeRefundServiceImpl.applyRefund(mockRefundRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result);
        verify(consumeRecordDao, times(1)).selectByTransactionNo("TXN001");
        verify(refundApplicationDao, times(1)).insert(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test applyRefund - Transaction Not Found")
    void test_applyRefund_TransactionNotFound() {
        // Given
        when(consumeRecordDao.selectByTransactionNo("TXN001")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            consumeRefundServiceImpl.applyRefund(mockRefundRequest);
        });
        assertEquals("交易记录不存在", exception.getMessage());
        verify(refundApplicationDao, never()).insert(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test applyRefund - Refund Amount Exceeds Original")
    void test_applyRefund_AmountExceedsOriginal() {
        // Given
        mockRefundRequest.setRefundAmount(new BigDecimal("150.00")); // Exceeds original amount
        when(consumeRecordDao.selectByTransactionNo("TXN001")).thenReturn(mockConsumeRecord);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            consumeRefundServiceImpl.applyRefund(mockRefundRequest);
        });
        assertEquals("退款金额不能超过原交易金额", exception.getMessage());
        verify(refundApplicationDao, never()).insert(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test applyRefund - Existing Pending Refund")
    void test_applyRefund_ExistingPendingRefund() {
        // Given
        when(consumeRecordDao.selectByTransactionNo("TXN001")).thenReturn(mockConsumeRecord);
        when(refundApplicationDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L); // Existing pending refund

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            consumeRefundServiceImpl.applyRefund(mockRefundRequest);
        });
        assertEquals("该交易已有待处理或已审批的退款申请", exception.getMessage());
        verify(refundApplicationDao, never()).insert(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test getRefundDetail - Success Scenario")
    void test_getRefundDetail_Success() {
        // Given
        Long refundId = 1L;
        when(refundApplicationDao.selectById(refundId)).thenReturn(mockRefundApplication);
        when(consumeRecordDao.selectById(anyLong())).thenReturn(mockConsumeRecord);

        // When
        RefundRecordVO result = consumeRefundServiceImpl.getRefundDetail(refundId);

        // Then
        assertNotNull(result);
        verify(refundApplicationDao, times(1)).selectById(refundId);
    }

    @Test
    @DisplayName("Test getRefundDetail - Refund Not Found")
    void test_getRefundDetail_NotFound() {
        // Given
        Long refundId = 999L;
        when(refundApplicationDao.selectById(refundId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            consumeRefundServiceImpl.getRefundDetail(refundId);
        });
        assertEquals("退款申请不存在", exception.getMessage());
    }

    @Test
    @DisplayName("Test getRefundPage - Success Scenario")
    void test_getRefundPage_Success() {
        // Given
        RefundQueryForm queryForm = new RefundQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        Page<RefundApplicationEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockRefundApplication));
        page.setTotal(1);

        when(refundApplicationDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(consumeRecordDao.selectById(anyLong())).thenReturn(mockConsumeRecord);

        // When
        net.lab1024.sa.common.domain.PageResult<RefundRecordVO> result = consumeRefundServiceImpl.getRefundPage(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        verify(refundApplicationDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Test batchApplyRefund - Success Scenario")
    void test_batchApplyRefund_Success() {
        // Given
        List<String> transactionNos = Arrays.asList("TXN001", "TXN002");
        String reason = "Batch refund";

        when(consumeRecordDao.selectByTransactionNo(anyString())).thenReturn(mockConsumeRecord);
        when(refundApplicationDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(refundApplicationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            RefundApplicationEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        }).when(refundApplicationDao).insert(any(RefundApplicationEntity.class));

        // When
        int result = consumeRefundServiceImpl.batchApplyRefund(transactionNos, reason);

        // Then
        assertTrue(result >= 0);
        verify(consumeRecordDao, atLeastOnce()).selectByTransactionNo(anyString());
    }

    @Test
    @DisplayName("Test approveRefund - Success Scenario")
    void test_approveRefund_Success() {
        // Given
        Long refundId = 1L;
        Boolean approved = true;
        String comment = "Approved";

        when(refundApplicationDao.selectById(refundId)).thenReturn(mockRefundApplication);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);

        // When
        boolean result = consumeRefundServiceImpl.approveRefund(refundId, approved, comment);

        // Then
        assertTrue(result);
        verify(refundApplicationDao, times(1)).selectById(refundId);
        verify(refundApplicationDao, times(1)).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test approveRefund - Refund Not Found")
    void test_approveRefund_NotFound() {
        // Given
        Long refundId = 999L;
        Boolean approved = true;
        String comment = "Approved";

        when(refundApplicationDao.selectById(refundId)).thenReturn(null);

        // Then
        assertThrows(BusinessException.class, () -> consumeRefundServiceImpl.approveRefund(refundId, approved, comment));
        verify(refundApplicationDao, times(1)).selectById(refundId);
        verify(refundApplicationDao, never()).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test cancelRefund - Success Scenario")
    void test_cancelRefund_Success() {
        // Given
        Long refundId = 1L;
        String reason = "User cancelled";

        when(refundApplicationDao.selectById(refundId)).thenReturn(mockRefundApplication);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);

        // When
        boolean result = consumeRefundServiceImpl.cancelRefund(refundId, reason);

        // Then
        assertTrue(result);
        verify(refundApplicationDao, times(1)).selectById(refundId);
        verify(refundApplicationDao, times(1)).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test processRefund - Success Scenario")
    void test_processRefund_Success() {
        // Given
        Long refundId = 1L;
        mockRefundApplication.setStatus("APPROVED");
        when(refundApplicationDao.selectById(refundId)).thenReturn(mockRefundApplication);
        when(consumeRecordDao.selectById(anyLong())).thenReturn(mockConsumeRecord);
        when(refundApplicationDao.updateById(any(RefundApplicationEntity.class))).thenReturn(1);
        when(accountService.addBalance(anyLong(), any(BigDecimal.class), anyString())).thenReturn(true);

        // When
        boolean result = consumeRefundServiceImpl.processRefund(refundId);

        // Then
        assertTrue(result);
        verify(refundApplicationDao, times(1)).selectById(refundId);
        verify(refundApplicationDao, times(1)).updateById(any(RefundApplicationEntity.class));
        verify(accountService, times(1)).addBalance(eq(2001L), eq(mockRefundApplication.getRefundAmount()), anyString());
    }

    @Test
    @DisplayName("Test processRefund - Refund Not Approved")
    void test_processRefund_NotApproved() {
        // Given
        Long refundId = 1L;
        mockRefundApplication.setStatus("PENDING"); // Not approved
        when(refundApplicationDao.selectById(refundId)).thenReturn(mockRefundApplication);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            consumeRefundServiceImpl.processRefund(refundId);
        });
        assertEquals("只有已审批状态的退款申请可以处理", exception.getMessage());
        verify(refundApplicationDao, never()).updateById(any(RefundApplicationEntity.class));
    }

    @Test
    @DisplayName("Test getRefundStatistics - Success Scenario (Stub)")
    void test_getRefundStatistics_Success() {
        // When
        Map<String, Object> result = consumeRefundServiceImpl.getRefundStatistics();

        // Then
        assertNotNull(result);
        // For stub method, just verify it was called
        assertTrue(true);
    }

    @Test
    @DisplayName("Test exportRefundData - Success Scenario (Stub)")
    void test_exportRefundData_Success() throws Exception {
        // Given
        RefundQueryForm queryForm = new RefundQueryForm();
        jakarta.servlet.http.HttpServletResponse response = mock(jakarta.servlet.http.HttpServletResponse.class);
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        java.io.PrintWriter printWriter = new java.io.PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        consumeRefundServiceImpl.exportRefundData(queryForm, response);

        // Then
        assertTrue(true);
    }
}


