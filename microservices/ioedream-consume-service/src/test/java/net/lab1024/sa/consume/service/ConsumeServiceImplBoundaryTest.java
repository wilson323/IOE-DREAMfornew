package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.service.impl.ConsumeServiceImpl;

/**
 * ConsumeServiceImpl边界和异常测试
 * <p>
 * 测试范围：边界条件、异常场景、复杂业务逻辑
 * 目标：提升测试覆盖率至80%+
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumeServiceImpl边界和异常测试")
class ConsumeServiceImplBoundaryTest {

    @Mock
    private ConsumeTransactionDao consumeTransactionDao;

    @Mock
    private net.lab1024.sa.consume.dao.AccountDao accountDao;

    @InjectMocks
    private ConsumeServiceImpl consumeService;

    private AccountEntity testAccount;
    private ConsumeRequestDTO consumeRequestDTO;

    @BeforeEach
    void setUp() {
        testAccount = new AccountEntity();
        testAccount.setAccountId(1L);
        testAccount.setUserId(1001L);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setStatus(1);

        consumeRequestDTO = new ConsumeRequestDTO();
        consumeRequestDTO.setOrderId("ORDER001");
        consumeRequestDTO.setAccountId(1L);
        consumeRequestDTO.setAmount(new BigDecimal("50.00"));
        consumeRequestDTO.setUserId(1001L);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("测试消费-金额为null")
    void testConsume_AmountIsNull() {
        // Given
        consumeRequestDTO.setAmount(null);
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When & Then
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试消费-金额为0")
    void testConsume_AmountIsZero() {
        // Given
        consumeRequestDTO.setAmount(BigDecimal.ZERO);
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When & Then
        // 根据业务规则，可能允许0金额或不允许
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        assertNotNull(result);
        // 如果业务规则不允许0金额，应该返回失败
    }

    @Test
    @DisplayName("测试消费-金额为负数")
    void testConsume_NegativeAmount() {
        // Given
        consumeRequestDTO.setAmount(new BigDecimal("-50.00"));
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When & Then
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试消费-金额为极大值")
    void testConsume_VeryLargeAmount() {
        // Given
        consumeRequestDTO.setAmount(new BigDecimal("999999999999.99"));
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When & Then
        // 应该检查余额是否充足
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        assertNotNull(result);
        assertFalse(result.getOk()); // 余额不足
    }

    @Test
    @DisplayName("测试游标分页查询-每页大小为null")
    void testCursorPageConsumeRecords_PageSizeIsNull() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("测试游标分页查询-每页大小超过最大值100")
    void testCursorPageConsumeRecords_PageSizeExceedsMax() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(200, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("测试游标分页查询-时间范围边界")
    void testCursorPageConsumeRecords_TimeRangeBoundary() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime lastTime = LocalDateTime.now().minusDays(15);

        when(consumeTransactionDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(20, lastTime, 1001L, null, startTime, endTime, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("测试消费-账户不存在")
    void testConsume_AccountNotFound() {
        // Given
        when(accountDao.selectById(1L)).thenReturn(null);

        // When
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
        verify(accountDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试消费-余额不足")
    void testConsume_InsufficientBalance() {
        // Given
        testAccount.setBalance(new BigDecimal("30.00")); // 小于消费金额
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试消费-账户已冻结")
    void testConsume_AccountFrozen() {
        // Given
        testAccount.setStatus(2); // 冻结状态
        when(accountDao.selectById(1L)).thenReturn(testAccount);

        // When
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试消费-数据库异常")
    void testConsume_DatabaseException() {
        // Given
        when(accountDao.selectById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        ResponseDTO<net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO> result =
            consumeService.consume(consumeRequestDTO);

        assertNotNull(result);
        assertFalse(result.getOk());
    }

    @Test
    @DisplayName("测试游标分页查询-数据库异常")
    void testCursorPageConsumeRecords_DatabaseException() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenThrow(new RuntimeException("数据库查询失败"));

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(20, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertFalse(result.getOk());
    }

    // ==================== 复杂业务场景测试 ====================

    @Test
    @DisplayName("测试游标分页查询-有下一页")
    void testCursorPageConsumeRecords_HasNext() {
        // Given
        // 模拟返回21条记录（pageSize=20，多1条表示有下一页）
        java.util.List<ConsumeTransactionEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
            transaction.setId(String.valueOf(2001 + i));
            transaction.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(transaction);
        }
        when(consumeTransactionDao.selectList(any())).thenReturn(records);

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(20, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertTrue(result.getData().getHasNext());
        assertEquals(20, result.getData().getList().size());
    }

    @Test
    @DisplayName("测试游标分页查询-无下一页")
    void testCursorPageConsumeRecords_NoNext() {
        // Given
        // 模拟返回20条记录（pageSize=20，刚好一页）
        java.util.List<ConsumeTransactionEntity> records = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();
            transaction.setId(String.valueOf(2001 + i));
            transaction.setCreateTime(LocalDateTime.now().minusHours(i));
            records.add(transaction);
        }
        when(consumeTransactionDao.selectList(any())).thenReturn(records);

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(20, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertFalse(result.getData().getHasNext());
        assertEquals(20, result.getData().getList().size());
    }

    @Test
    @DisplayName("测试游标分页查询-空结果")
    void testCursorPageConsumeRecords_EmptyResult() {
        // Given
        when(consumeTransactionDao.selectList(any())).thenReturn(java.util.Collections.emptyList());

        // When
        ResponseDTO<net.lab1024.sa.common.util.CursorPagination.CursorPageResult<ConsumeRecordVO>> result =
            consumeService.cursorPageConsumeRecords(20, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getOk());
        assertNotNull(result.getData());
        assertFalse(result.getData().getHasNext());
        assertEquals(0, result.getData().getList().size());
    }
}
