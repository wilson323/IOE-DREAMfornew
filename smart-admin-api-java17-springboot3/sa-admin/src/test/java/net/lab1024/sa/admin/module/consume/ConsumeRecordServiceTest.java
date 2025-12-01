package net.lab1024.sa.admin.module.consume;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.service.ConsumeRecordService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.tenant.TenantEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消费记录服务单元测试
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(SpringJUnitConfig.class)
class ConsumeRecordServiceTest {

    @InjectMocks
    private ConsumeRecordService consumeRecordService;

    @Mock
    private ConsumeRecordDao consumeRecordDao;

    @Mock
    private TenantEnvironment tenantEnvironment;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ConsumeRecordEntity testRecord;

    @BeforeEach
    void setUp() {
        // 初始化测试消费记录
        testRecord = new ConsumeRecordEntity();
        testRecord.setRecordId(12345L);
        testRecord.setOrderNo("ORDER_001");
        testRecord.setUserId(1001L);
        testRecord.setPersonId(2001L);
        testRecord.setAccountId(3001L);
        testRecord.setDeviceId(4001L);
        testRecord.setAccountId(3001L);
        testRecord.setConsumeMode("FIXED_AMOUNT");
        testRecord.setOriginalAmount(new BigDecimal("10.00"));
        testRecord.setFinalAmount(new BigDecimal("10.00"));
        testRecord.setDiscountAmount(new BigDecimal("0.00"));
        testRecord.setConsumeStatus("SUCCESS");
        testRecord.setPaymentStatus("PAID");
        testRecord.setRefundStatus("NONE");
        testRecord.setConsumeTime(LocalDateTime.now());
        testRecord.setRemark("测试消费记录");
        testRecord.setCreateTime(LocalDateTime.now());
        testRecord.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试创建消费记录")
    void testCreateRecord_Success() {
        // 准备测试数据
        ConsumeRecordEntity newRecord = new ConsumeRecordEntity();
        newRecord.setOrderNo("ORDER_002");
        newRecord.setUserId(1002L);
        newRecord.setConsumeMode("FIXED_AMOUNT");
        newRecord.setFinalAmount(new BigDecimal("20.00"));

        // 模拟依赖调用
        when(consumeRecordDao.existsByOrderNo(newRecord.getOrderNo()))
            .thenReturn(false);
        when(consumeRecordDao.save(any(ConsumeRecordEntity.class)))
            .thenReturn(testRecord);

        // 执行测试
        ConsumeRecordEntity result = consumeRecordService.createRecord(newRecord);

        // 验证结果
        assertNotNull(result);
        assertEquals(testRecord.getRecordId(), result.getRecordId());
        assertEquals(testRecord.getOrderNo(), result.getOrderNo());

        verify(consumeRecordDao, times(1)).existsByOrderNo(newRecord.getOrderNo());
        verify(consumeRecordDao, times(1)).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试创建消费记录 - 订单号重复")
    void testCreateRecord_DuplicateOrder() {
        // 准备测试数据
        ConsumeRecordEntity newRecord = new ConsumeRecordEntity();
        newRecord.setOrderNo("ORDER_001"); // 重复的订单号

        // 模拟订单号已存在
        when(consumeRecordDao.existsByOrderNo(newRecord.getOrderNo()))
            .thenReturn(true);

        // 执行测试
        assertThrows(IllegalArgumentException.class, () -> {
            consumeRecordService.createRecord(newRecord);
        });

        verify(consumeRecordDao, times(1)).existsByOrderNo(newRecord.getOrderNo());
        verify(consumeRecordDao, never()).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试检查订单号是否存在")
    void testExistsByOrderNo_Success() {
        // 模拟订单号存在
        when(consumeRecordDao.existsByOrderNo(testRecord.getOrderNo()))
            .thenReturn(true);

        // 执行测试
        Boolean result = consumeRecordService.existsByOrderNo(testRecord.getOrderNo());

        // 验证结果
        assertTrue(result);

        verify(consumeRecordDao, times(1)).existsByOrderNo(testRecord.getOrderNo());
    }

    @Test
    @DisplayName("测试检查订单号是否存在 - 订单不存在")
    void testExistsByOrderNo_NotExists() {
        // 模拟订单号不存在
        when(consumeRecordDao.existsByOrderNo("ORDER_999"))
            .thenReturn(false);

        // 执行测试
        Boolean result = consumeRecordService.existsByOrderNo("ORDER_999");

        // 验证结果
        assertFalse(result);

        verify(consumeRecordDao, times(1)).existsByOrderNo("ORDER_999");
    }

    @Test
    @DisplayName("测试根据ID获取消费记录")
    void testGetRecordById_Success() {
        // 模拟依赖调用
        when(consumeRecordDao.findById(testRecord.getRecordId()))
            .thenReturn(Optional.of(testRecord));

        // 执行测试
        ConsumeRecordEntity result = consumeRecordService.getRecordById(testRecord.getRecordId());

        // 验证结果
        assertNotNull(result);
        assertEquals(testRecord.getRecordId(), result.getRecordId());
        assertEquals(testRecord.getOrderNo(), result.getOrderNo());
        assertEquals(testRecord.getUserId(), result.getUserId());

        verify(consumeRecordDao, times(1)).findById(testRecord.getRecordId());
    }

    @Test
    @DisplayName("测试根据ID获取消费记录 - 记录不存在")
    void testGetRecordById_NotFound() {
        // 模拟记录不存在
        when(consumeRecordDao.findById(9999L))
            .thenReturn(Optional.empty());

        // 执行测试
        ConsumeRecordEntity result = consumeRecordService.getRecordById(9999L);

        // 验证结果
        assertNull(result);

        verify(consumeRecordDao, times(1)).findById(9999L);
    }

    @Test
    @DisplayName("测试根据用户ID获取消费记录")
    void testGetRecordsByUserId_Success() {
        // 准备测试数据
        List<ConsumeRecordEntity> recordList = Arrays.asList(testRecord);
        Page<ConsumeRecordEntity> recordPage = new PageImpl<>(recordList);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟依赖调用
        when(consumeRecordDao.findByUserIdOrderByConsumeTimeDesc(testRecord.getUserId(), pageable))
            .thenReturn(recordPage);

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.getRecordsByUserId(
            testRecord.getUserId(), pageable, null, null
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testRecord.getRecordId(), result.getContent().get(0).getRecordId());

        verify(consumeRecordDao, times(1))
            .findByUserIdOrderByConsumeTimeDesc(testRecord.getUserId(), pageable);
    }

    @Test
    @DisplayName("测试根据设备ID获取消费记录")
    void testGetRecordsByDeviceId_Success() {
        // 准备测试数据
        List<ConsumeRecordEntity> recordList = Arrays.asList(testRecord);
        Page<ConsumeRecordEntity> recordPage = new PageImpl<>(recordList);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟依赖调用
        when(consumeRecordDao.findByDeviceIdOrderByConsumeTimeDesc(testRecord.getDeviceId(), pageable))
            .thenReturn(recordPage);

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.getRecordsByDeviceId(
            testRecord.getDeviceId(), pageable, null, null
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testRecord.getRecordId(), result.getContent().get(0).getRecordId());

        verify(consumeRecordDao, times(1))
            .findByDeviceIdOrderByConsumeTimeDesc(testRecord.getDeviceId(), pageable);
    }

    @Test
    @DisplayName("测试更新消费记录状态")
    void testUpdateRecordStatus_Success() {
        // 准备更新数据
        String newStatus = "REFUNDED";
        String remark = "测试状态更新";

        // 模拟依赖调用
        when(consumeRecordDao.findById(testRecord.getRecordId()))
            .thenReturn(Optional.of(testRecord));
        when(consumeRecordDao.save(any(ConsumeRecordEntity.class)))
            .thenReturn(testRecord);

        // 执行测试
        ResponseDTO<String> result = consumeRecordService.updateRecordStatus(
            testRecord.getRecordId(), newStatus, remark
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("状态更新成功", result.getMsg());

        verify(consumeRecordDao, times(1)).findById(testRecord.getRecordId());
        verify(consumeRecordDao, times(1)).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试更新消费记录状态 - 记录不存在")
    void testUpdateRecordStatus_RecordNotFound() {
        // 模拟记录不存在
        when(consumeRecordDao.findById(9999L))
            .thenReturn(Optional.empty());

        // 执行测试
        ResponseDTO<String> result = consumeRecordService.updateRecordStatus(
            9999L, "REFUNDED", "测试"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("消费记录不存在", result.getMsg());

        verify(consumeRecordDao, times(1)).findById(9999L);
        verify(consumeRecordDao, never()).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试处理退款")
    void testProcessRefund_Success() {
        // 准备退款数据
        BigDecimal refundAmount = new BigDecimal("5.00");
        String refundReason = "用户申请退款";

        // 设置消费记录为可退款状态
        testRecord.setRefundStatus("NONE");
        testRecord.setPaymentStatus("PAID");

        // 模拟依赖调用
        when(consumeRecordDao.findById(testRecord.getRecordId()))
            .thenReturn(Optional.of(testRecord));
        when(consumeRecordDao.save(any(ConsumeRecordEntity.class)))
            .thenReturn(testRecord);

        // 执行测试
        ResponseDTO<String> result = consumeRecordService.processRefund(
            testRecord.getRecordId(), refundAmount, refundReason
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("退款处理成功", result.getMsg());

        verify(consumeRecordDao, times(1)).findById(testRecord.getRecordId());
        verify(consumeRecordDao, times(1)).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试处理退款 - 金额无效")
    void testProcessRefund_InvalidAmount() {
        // 准备无效退款金额
        BigDecimal invalidAmount = new BigDecimal("-10.00");

        // 执行测试
        ResponseDTO<String> result = consumeRecordService.processRefund(
            testRecord.getRecordId(), invalidAmount, "测试"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("退款金额必须大于0", result.getMsg());

        verify(consumeRecordDao, never()).findById(anyLong());
        verify(consumeRecordDao, never()).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试处理退款 - 已退款")
    void testProcessRefund_AlreadyRefunded() {
        // 设置为已退款状态
        testRecord.setRefundStatus("FULL_REFUND");

        // 模拟依赖调用
        when(consumeRecordDao.findById(testRecord.getRecordId()))
            .thenReturn(Optional.of(testRecord));

        // 执行测试
        ResponseDTO<String> result = consumeRecordService.processRefund(
            testRecord.getRecordId(), new BigDecimal("5.00"), "测试"
        );

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("该订单已退款", result.getMsg());

        verify(consumeRecordDao, times(1)).findById(testRecord.getRecordId());
        verify(consumeRecordDao, never()).save(any(ConsumeRecordEntity.class));
    }

    @Test
    @DisplayName("测试获取用户消费统计")
    void testGetUserConsumeStatistics_Success() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        // 模拟依赖调用
        when(consumeRecordDao.countByUserIdAndConsumeTimeBetween(testRecord.getUserId(), startTime, endTime))
            .thenReturn(10L);
        when(consumeRecordDao.sumFinalAmountByUserIdAndConsumeTimeBetween(testRecord.getUserId(), startTime, endTime))
            .thenReturn(new BigDecimal("500.00"));
        when(consumeRecordDao.countByUserIdAndConsumeStatusAndConsumeTimeBetween(
            testRecord.getUserId(), "SUCCESS", startTime, endTime))
            .thenReturn(9L);

        // 执行测试
        ResponseDTO<Object> result = consumeRecordService.getUserConsumeStatistics(
            testRecord.getUserId(), startTime, endTime
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        verify(consumeRecordDao, times(1))
            .countByUserIdAndConsumeTimeBetween(testRecord.getUserId(), startTime, endTime);
        verify(consumeRecordDao, times(1))
            .sumFinalAmountByUserIdAndConsumeTimeBetween(testRecord.getUserId(), startTime, endTime);
        verify(consumeRecordDao, times(1))
            .countByUserIdAndConsumeStatusAndConsumeTimeBetween(testRecord.getUserId(), "SUCCESS", startTime, endTime);
    }

    @Test
    @DisplayName("测试获取设备消费统计")
    void testGetDeviceConsumeStatistics_Success() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // 模拟依赖调用
        when(consumeRecordDao.countByDeviceIdAndConsumeTimeBetween(testRecord.getDeviceId(), startTime, endTime))
            .thenReturn(50L);
        when(consumeRecordDao.sumFinalAmountByDeviceIdAndConsumeTimeBetween(testRecord.getDeviceId(), startTime, endTime))
            .thenReturn(new BigDecimal("2500.00"));

        // 执行测试
        ResponseDTO<Object> result = consumeRecordService.getDeviceConsumeStatistics(
            testRecord.getDeviceId(), startTime, endTime
        );

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        verify(consumeRecordDao, times(1))
            .countByDeviceIdAndConsumeTimeBetween(testRecord.getDeviceId(), startTime, endTime);
        verify(consumeRecordDao, times(1))
            .sumFinalAmountByDeviceIdAndConsumeTimeBetween(testRecord.getDeviceId(), startTime, endTime);
    }

    @Test
    @DisplayName("测试获取消费模式分布统计")
    void testGetConsumeModeStatistics_Success() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        // 模拟依赖调用
        when(consumeRecordDao.countByConsumeModeAndConsumeTimeBetween("FIXED_AMOUNT", startTime, endTime))
            .thenReturn(100L);
        when(consumeRecordDao.countByConsumeModeAndConsumeTimeBetween("FREE_AMOUNT", startTime, endTime))
            .thenReturn(50L);

        // 执行测试
        ResponseDTO<Object> result = consumeRecordService.getConsumeModeStatistics(startTime, endTime);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        verify(consumeRecordDao, times(1))
            .countByConsumeModeAndConsumeTimeBetween("FIXED_AMOUNT", startTime, endTime);
        verify(consumeRecordDao, times(1))
            .countByConsumeModeAndConsumeTimeBetween("FREE_AMOUNT", startTime, endTime);
    }

    @Test
    @DisplayName("测试分页查询消费记录")
    void testQueryRecordsWithPagination_Success() {
        // 准备测试数据
        List<ConsumeRecordEntity> recordList = Arrays.asList(testRecord);
        Page<ConsumeRecordEntity> recordPage = new PageImpl<>(recordList);
        Pageable pageable = PageRequest.of(0, 10);

        // 模拟依赖调用
        when(consumeRecordDao.findAll(any(Pageable.class)))
            .thenReturn(recordPage);

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.queryRecordsWithPagination(pageable, null, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(consumeRecordDao, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("测试异常处理")
    void testException() {
        // 模拟异常
        when(consumeRecordDao.findById(anyLong()))
            .thenThrow(new RuntimeException("数据库连接异常"));

        // 执行测试
        assertThrows(RuntimeException.class, () -> {
            consumeRecordService.getRecordById(testRecord.getRecordId());
        });
    }

    @Test
    @DisplayName("测试消费状态验证")
    void testConsumeStatusValidation() {
        // 测试各种消费状态
        String[] validStatuses = {"SUCCESS", "FAILED", "PENDING", "CANCELLED"};
        String[] invalidStatuses = {"INVALID", "UNKNOWN", null};

        for (String status : validStatuses) {
            testRecord.setConsumeStatus(status);
            when(consumeRecordDao.findById(testRecord.getRecordId()))
                .thenReturn(Optional.of(testRecord));

            ConsumeRecordEntity result = consumeRecordService.getRecordById(testRecord.getRecordId());
            assertNotNull(result);
            assertEquals(status, result.getConsumeStatus());
        }

        // 测试无效状态
        for (String status : invalidStatuses) {
            if (status != null) {
                testRecord.setConsumeStatus(status);
                when(consumeRecordDao.findById(testRecord.getRecordId()))
                    .thenReturn(Optional.of(testRecord));

                // 这里应该有相应的状态验证逻辑
                // 具体验证规则取决于业务需求
            }
        }
    }

    @Test
    @DisplayName("测试退款状态验证")
    void testRefundStatusValidation() {
        // 测试各种退款状态
        String[] validRefundStatuses = {"NONE", "PARTIAL_REFUND", "FULL_REFUND", "REFUND_PROCESSING"};

        for (String status : validRefundStatuses) {
            testRecord.setRefundStatus(status);
            when(consumeRecordDao.findById(testRecord.getRecordId()))
                .thenReturn(Optional.of(testRecord));

            ConsumeRecordEntity result = consumeRecordService.getRecordById(testRecord.getRecordId());
            assertNotNull(result);
            assertEquals(status, result.getRefundStatus());
        }
    }

    @Test
    @DisplayName("测试时间范围查询")
    void testTimeRangeQuery() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        // 模拟依赖调用
        when(consumeRecordDao.findByConsumeTimeBetweenOrderByConsumeTimeDesc(startTime, endTime, any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testRecord)));

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.getRecordsByTimeRange(startTime, endTime, PageRequest.of(0, 10));

        // 验证结果
        assertNotNull(result);

        verify(consumeRecordDao, times(1))
            .findByConsumeTimeBetweenOrderByConsumeTimeDesc(startTime, endTime, any(Pageable.class));
    }

    @Test
    @DisplayName("测试金额范围查询")
    void testAmountRangeQuery() {
        // 准备测试数据
        BigDecimal minAmount = new BigDecimal("10.00");
        BigDecimal maxAmount = new BigDecimal("100.00");

        // 模拟依赖调用
        when(consumeRecordDao.findByFinalAmountBetweenOrderByConsumeTimeDesc(minAmount, maxAmount, any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testRecord)));

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.getRecordsByAmountRange(minAmount, maxAmount, PageRequest.of(0, 10));

        // 验证结果
        assertNotNull(result);

        verify(consumeRecordDao, times(1))
            .findByFinalAmountBetweenOrderByConsumeTimeDesc(minAmount, maxAmount, any(Pageable.class));
    }

    @Test
    @DisplayName("测试消费模式查询")
    void testConsumeModeQuery() {
        // 准备测试数据
        String consumeMode = "FIXED_AMOUNT";

        // 模拟依赖调用
        when(consumeRecordDao.findByConsumeModeOrderByConsumeTimeDesc(consumeMode, any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(testRecord)));

        // 执行测试
        Page<ConsumeRecordEntity> result = consumeRecordService.getRecordsByConsumeMode(consumeMode, PageRequest.of(0, 10));

        // 验证结果
        assertNotNull(result);

        verify(consumeRecordDao, times(1))
            .findByConsumeModeOrderByConsumeTimeDesc(consumeMode, any(Pageable.class));
    }
}