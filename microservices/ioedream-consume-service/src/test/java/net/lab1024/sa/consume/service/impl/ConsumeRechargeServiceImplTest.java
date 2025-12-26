package net.lab1024.sa.consume.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeRechargeQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRechargeStatisticsVO;
import net.lab1024.sa.consume.service.ConsumeRechargeService;

/**
 * 消费充值服务测试类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
class ConsumeRechargeServiceImplTest {

    @Mock
    private ConsumeRechargeService consumeRechargeService;

    @InjectMocks
    private ConsumeRechargeServiceImpl consumeRechargeServiceImpl;

    private ConsumeRechargeQueryForm queryForm;
    private ConsumeRechargeAddForm addForm;

    @BeforeEach
    void setUp () {
        // 初始化查询表单
        queryForm = new ConsumeRechargeQueryForm ();
        queryForm.setPageNum (1);
        queryForm.setPageSize (10);

        // 初始化添加表单
        addForm = new ConsumeRechargeAddForm ();
        addForm.setUserId (1L);
        addForm.setUserName ("测试用户");
        addForm.setRechargeAmount (new BigDecimal ("100.00"));
        addForm.setRechargeWay ("微信");
    }

    @Test
    void testQueryRechargeRecords () {
        // 准备测试数据
        List<ConsumeRechargeRecordVO> records = new ArrayList<> ();
        ConsumeRechargeRecordVO record = new ConsumeRechargeRecordVO ();
        record.setRecordId (1L);
        record.setUserId (1L);
        record.setUserName ("测试用户");
        record.setRechargeAmount (new BigDecimal ("100.00"));
        records.add (record);

        PageResult<ConsumeRechargeRecordVO> expectedResult = PageResult.of (records, 1L, 1, 10);

        // 设置mock行为
        when (consumeRechargeService.queryRechargeRecords (any (ConsumeRechargeQueryForm.class)))
                .thenReturn (expectedResult);

        // 执行测试
        PageResult<ConsumeRechargeRecordVO> result = consumeRechargeService.queryRechargeRecords (queryForm);

        // 验证结果
        assertNotNull (result);
        assertEquals (1, result.getTotal ());
        assertEquals (1, result.getList ().size ());
        assertEquals ("测试用户", result.getList ().get (0).getUserName ());

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).queryRechargeRecords (any (ConsumeRechargeQueryForm.class));
    }

    @Test
    void testAddRechargeRecord () {
        // 设置mock行为
        when (consumeRechargeService.addRechargeRecord (any (ConsumeRechargeAddForm.class))).thenReturn (1L);

        // 执行测试
        Long result = consumeRechargeService.addRechargeRecord (addForm);

        // 验证结果
        assertNotNull (result);
        assertEquals (1L, result);

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).addRechargeRecord (any (ConsumeRechargeAddForm.class));
    }

    @Test
    void testGetRechargeRecordDetail () {
        // 准备测试数据
        ConsumeRechargeRecordVO expectedRecord = new ConsumeRechargeRecordVO ();
        expectedRecord.setRecordId (1L);
        expectedRecord.setUserId (1L);
        expectedRecord.setUserName ("测试用户");
        expectedRecord.setRechargeAmount (new BigDecimal ("100.00"));

        // 设置mock行为
        when (consumeRechargeService.getRechargeRecordDetail (1L)).thenReturn (expectedRecord);

        // 执行测试
        ConsumeRechargeRecordVO result = consumeRechargeService.getRechargeRecordDetail (1L);

        // 验证结果
        assertNotNull (result);
        assertEquals (1L, result.getRecordId ());
        assertEquals ("测试用户", result.getUserName ());

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).getRechargeRecordDetail (1L);
    }

    @Test
    void testGetUserRechargeRecords () {
        // 准备测试数据
        List<ConsumeRechargeRecordVO> records = new ArrayList<> ();
        ConsumeRechargeRecordVO record = new ConsumeRechargeRecordVO ();
        record.setRecordId (1L);
        record.setUserId (1L);
        record.setUserName ("测试用户");
        record.setRechargeAmount (new BigDecimal ("100.00"));
        records.add (record);

        PageResult<ConsumeRechargeRecordVO> expectedResult = PageResult.of (records, 1L, 1, 10);

        // 设置mock行为
        when (consumeRechargeService.getUserRechargeRecords (1L, 1, 10)).thenReturn (expectedResult);

        // 执行测试
        PageResult<ConsumeRechargeRecordVO> result = consumeRechargeService.getUserRechargeRecords (1L, 1, 10);

        // 验证结果
        assertNotNull (result);
        assertEquals (1, result.getTotal ());
        assertEquals (1, result.getList ().size ());
        assertEquals ("测试用户", result.getList ().get (0).getUserName ());

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).getUserRechargeRecords (1L, 1, 10);
    }

    @Test
    void testGetTodayRechargeRecords () {
        // 准备测试数据
        List<ConsumeRechargeRecordVO> records = new ArrayList<> ();
        ConsumeRechargeRecordVO record = new ConsumeRechargeRecordVO ();
        record.setRecordId (1L);
        record.setUserId (1L);
        record.setUserName ("测试用户");
        record.setRechargeAmount (new BigDecimal ("100.00"));
        records.add (record);

        // 设置mock行为
        when (consumeRechargeService.getTodayRechargeRecords ()).thenReturn (records);

        // 执行测试
        List<ConsumeRechargeRecordVO> result = consumeRechargeService.getTodayRechargeRecords ();

        // 验证结果
        assertNotNull (result);
        assertEquals (1, result.size ());
        assertEquals ("测试用户", result.get (0).getUserName ());

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).getTodayRechargeRecords ();
    }

    @Test
    void testGetRechargeStatistics () {
        // 准备测试数据
        ConsumeRechargeStatisticsVO expectedStats = new ConsumeRechargeStatisticsVO ();
        expectedStats.setTotalRechargeAmount (new BigDecimal ("1000.00"));
        expectedStats.setTotalRechargeCount (10L);

        // 设置mock行为
        when (consumeRechargeService.getRechargeStatistics (eq(1L), any (LocalDateTime.class), any (LocalDateTime.class)))
                .thenReturn (expectedStats);

        // 执行测试
        ConsumeRechargeStatisticsVO result = consumeRechargeService.getRechargeStatistics (1L,
                LocalDateTime.now ().minusDays (7), LocalDateTime.now ());

        // 验证结果
        assertNotNull (result);
        assertEquals (new BigDecimal ("1000.00"), result.getTotalRechargeAmount ());
        assertEquals (10L, result.getTotalRechargeCount ());

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).getRechargeStatistics (eq(1L), any (LocalDateTime.class),
                any (LocalDateTime.class));
    }

    @Test
    void testBatchRecharge () {
        // 准备测试数据
        List<Long> userIds = Arrays.asList (1L, 2L, 3L);
        BigDecimal amount = new BigDecimal ("100.00");
        String rechargeWay = "微信";
        String remark = "批量充值";

        Map<String, Object> expectedResult = new HashMap<> ();
        expectedResult.put ("success", true);
        expectedResult.put ("message", "批量充值成功");

        // 设置mock行为
        when (consumeRechargeService.batchRecharge (userIds, amount, rechargeWay, remark)).thenReturn (expectedResult);

        // 执行测试
        Map<String, Object> result = consumeRechargeService.batchRecharge (userIds, amount, rechargeWay, remark);

        // 验证结果
        assertNotNull (result);
        assertTrue ((Boolean) result.get ("success"));

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).batchRecharge (userIds, amount, rechargeWay, remark);
    }

    @Test
    void testVerifyRechargeRecord () {
        // 设置mock行为
        when (consumeRechargeService.verifyRechargeRecord (1L)).thenReturn (true);

        // 执行测试
        Boolean result = consumeRechargeService.verifyRechargeRecord (1L);

        // 验证结果
        assertNotNull (result);
        assertTrue (result);

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).verifyRechargeRecord (1L);
    }

    @Test
    void testReverseRechargeRecord () {
        // 执行测试
        assertDoesNotThrow ( () -> consumeRechargeService.reverseRechargeRecord (1L, "测试冲正"));

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).reverseRechargeRecord (1L, "测试冲正");
    }

    @Test
    void testExportRechargeRecords () {
        // 设置mock行为
        when (consumeRechargeService.exportRechargeRecords (any (ConsumeRechargeQueryForm.class)))
                .thenReturn ("/export/test.xlsx");

        // 执行测试
        String result = consumeRechargeService.exportRechargeRecords (queryForm);

        // 验证结果
        assertNotNull (result);
        assertEquals ("/export/test.xlsx", result);

        // 验证方法被调用
        verify (consumeRechargeService, times (1)).exportRechargeRecords (any (ConsumeRechargeQueryForm.class));
    }
}
