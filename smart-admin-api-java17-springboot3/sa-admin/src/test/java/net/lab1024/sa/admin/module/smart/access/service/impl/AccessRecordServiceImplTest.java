/*
 * 门禁记录服务实现类单元测试
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.smart.access.service.impl;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.smart.access.dao.AccessRecordDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.admin.module.smart.access.manager.AccessRecordManager;
import net.lab1024.sa.admin.module.smart.access.service.AccessRecordService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁记录服务实现类单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("门禁记录服务实现类单元测试")
class AccessRecordServiceImplTest {

    @Mock
    private AccessRecordDao accessRecordDao;

    @Mock
    private AccessRecordManager accessRecordManager;

    @InjectMocks
    private AccessRecordServiceImpl accessRecordService;

    private AccessRecordEntity testRecord;
    private AccessRecordQueryForm queryForm;
    private PageParam pageParam;

    @BeforeEach
    void setUp() {
        // 测试数据初始化
        testRecord = AccessRecordEntity.builder()
                .recordId(1L)
                .userId(1001L)
                .userName("张三")
                .userCardNumber("100001")
                .deviceId(1L)
                .deviceName("前门读卡器")
                .deviceCode("FRONT_CARD_001")
                .areaId(1L)
                .areaName("总部大楼1楼")
                .accessTime(LocalDateTime.now())
                .accessType("in")
                .accessResult("success")
                .verificationMethod("card_only")
                .verificationSuccess(true)
                .verificationDuration(150L)
                .verificationScore(95)
                .isAbnormal(false)
                .photoUrl("https://example.com/photo1.jpg")
                .temperature(36.5)
                .maskDetected(true)
                .createUserId(1001L)
                .createTime(LocalDateTime.now())
                .build();

        queryForm = AccessRecordQueryForm.builder()
                .userId(1001L)
                .userName("张三")
                .deviceId(1L)
                .accessType("in")
                .accessResult("success")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now())
                .build();

        pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(20);
    }

    @Test
    @DisplayName("测试获取记录列表 - 成功")
    void testGetRecordList_Success() {
        // Arrange
        List<AccessRecordEntity> recordList = Arrays.asList(testRecord);
        when(accessRecordDao.selectList(any())).thenReturn(recordList);
        when(accessRecordDao.selectCount(any())).thenReturn(1);

        // Act
        PageResult<AccessRecordVO> result = accessRecordService.getRecordList(pageParam, queryForm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(1, result.getList().size());
        assertEquals("张三", result.getList().get(0).getUserName());

        verify(accessRecordDao, times(1)).selectList(any());
        verify(accessRecordDao, times(1)).selectCount(any());
    }

    @Test
    @DisplayName("测试获取记录详情 - 成功")
    void testGetRecordDetail_Success() {
        // Arrange
        when(accessRecordDao.selectDetailById(1L)).thenReturn(
                AccessRecordVO.builder()
                        .recordId(1L)
                        .userId(1001L)
                        .userName("张三")
                        .userCardNumber("100001")
                        .deviceName("前门读卡器")
                        .deviceCode("FRONT_CARD_001")
                        .areaName("总部大楼1楼")
                        .accessTime(LocalDateTime.now())
                        .accessType("in")
                        .accessResult("success")
                        .verificationMethod("card_only")
                        .verificationSuccess(true)
                        .verificationDuration(150L)
                        .verificationScore(95)
                        .photoUrl("https://example.com/photo1.jpg")
                        .temperature(36.5)
                        .maskDetected(true)
                        .build()
        );

        // Act
        AccessRecordVO result = accessRecordService.getRecordDetail(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRecordId());
        assertEquals("张三", result.getUserName());
        assertEquals("100001", result.getUserCardNumber());
        assertEquals("前门读卡器", result.getDeviceName());

        verify(accessRecordDao, times(1)).selectDetailById(1L);
    }

    @Test
    @DisplayName("测试获取记录详情 - 记录不存在")
    void testGetRecordDetail_NotFound() {
        // Arrange
        when(accessRecordDao.selectDetailById(999L)).thenReturn(null);

        // Act
        AccessRecordVO result = accessRecordService.getRecordDetail(999L);

        // Assert
        assertNull(result);
        verify(accessRecordDao, times(1)).selectDetailById(999L);
    }

    @Test
    @DisplayName("测试获取记录统计 - 成功")
    void testGetRecordStats_Success() {
        // Arrange
        when(accessRecordDao.selectTotalCount()).thenReturn(1000L);
        when(accessRecordDao.selectCountByResult("success")).thenReturn(950L);
        when(accessRecordDao.selectCountByResult("failed")).thenReturn(30L);
        when(accessRecordDao.selectCountByResult("denied")).thenReturn(15L);
        when(accessRecordDao.selectCountByResult("timeout")).thenReturn(5L);
        when(accessRecordDao.selectUniqueUserCount()).thenReturn(200L);
        when(accessRecordDao.selectTodayCount()).thenReturn(150L);
        when(accessRecordDao.selectPeakHour()).thenReturn("09:00");
        when(accessRecordDao.selectAvgVerificationTime()).thenReturn(180L);
        when(accessRecordDao.selectDeviceStats()).thenReturn(Arrays.asList(
                Map.of("deviceName", "前门读卡器", "totalCount", 500L, "successCount", 480L),
                Map.of("deviceName", "后门人脸识别", "totalCount", 300L, "successCount", 285L)
        ));
        when(accessRecordDao.selectUserStats()).thenReturn(Arrays.asList(
                Map.of("userName", "张三", "totalCount", 25L, "successCount", 24L),
                Map.of("userName", "李四", "totalCount", 20L, "successCount", 19L)
        ));
        when(accessRecordDao.selectVerificationStats()).thenReturn(Arrays.asList(
                Map.of("method", "card_only", "count", 600L),
                Map.of("method", "face_only", "count", 250L),
                Map.of("method", "fingerprint_only", "count", 150L)
        ));
        when(accessRecordDao.selectTrendStats(any(), any())).thenReturn(Arrays.asList(
                Map.of("time", "2025-01-17 08:00", "successCount", 45L, "failedCount", 2L),
                Map.of("time", "2025-01-17 09:00", "successCount", 120L, "failedCount", 8L)
        ));
        when(accessRecordDao.selectHourlyStats(any(), any())).thenReturn(Arrays.asList(
                Map.of("hour", "08:00", "count", 50L),
                Map.of("hour", "09:00", "count", 130L)
        ));

        // Act
        ResponseDTO<Map<String, Object>> result = accessRecordService.getRecordStats(queryForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());

        Map<String, Object> stats = result.getData();
        assertNotNull(stats);
        assertEquals(1000L, stats.get("totalCount"));
        assertEquals(950L, stats.get("successCount"));
        assertEquals(30L, stats.get("failedCount"));
        assertEquals(95.0, (Double) stats.get("successRate"));
        assertEquals(200L, stats.get("uniqueUserCount"));

        verify(accessRecordDao, times(1)).selectTotalCount();
        verify(accessRecordDao, times(1)).selectCountByResult("success");
        verify(accessRecordDao, times(1)).selectCountByResult("failed");
    }

    @Test
    @DisplayName("测试处理异常记录 - 成功")
    void testHandleAbnormalRecord_Success() {
        // Arrange
        Long recordId = 1L;
        String action = "resolve";
        String remark = "验证通过，卡片已更新";

        when(accessRecordDao.selectById(recordId)).thenReturn(testRecord);
        when(accessRecordManager.handleAbnormalRecord(recordId, action, remark, 1001L)).thenReturn(true);

        // Act
        ResponseDTO<String> result = accessRecordService.handleAbnormalRecord(recordId, action, remark);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("异常记录处理成功", result.getMsg());

        verify(accessRecordDao, times(1)).selectById(recordId);
        verify(accessRecordManager, times(1)).handleAbnormalRecord(recordId, action, remark, 1001L);
    }

    @Test
    @DisplayName("测试处理异常记录 - 记录不存在")
    void testHandleAbnormalRecord_NotFound() {
        // Arrange
        Long recordId = 999L;
        when(accessRecordDao.selectById(recordId)).thenReturn(null);

        // Act
        ResponseDTO<String> result = accessRecordService.handleAbnormalRecord(recordId, "resolve", "测试");

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("记录不存在"));

        verify(accessRecordDao, times(1)).selectById(recordId);
        verify(accessRecordManager, never()).handleAbnormalRecord(any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试处理异常记录 - 不是异常记录")
    void testHandleAbnormalRecord_NotAbnormal() {
        // Arrange
        AccessRecordEntity normalRecord = AccessRecordEntity.builder()
                .recordId(1L)
                .isAbnormal(false)
                .build();

        when(accessRecordDao.selectById(1L)).thenReturn(normalRecord);

        // Act
        ResponseDTO<String> result = accessRecordService.handleAbnormalRecord(1L, "resolve", "测试");

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("不是异常记录"));

        verify(accessRecordDao, times(1)).selectById(1L);
        verify(accessRecordManager, never()).handleAbnormalRecord(any(), any(), any(), any());
    }

    @Test
    @DisplayName("测试批量处理异常记录 - 成功")
    void testBatchProcessAbnormalRecords_Success() {
        // Arrange
        List<Long> recordIds = Arrays.asList(1L, 2L);
        String action = "resolve";
        String remark = "批量验证通过";

        AccessRecordEntity abnormalRecord = AccessRecordEntity.builder()
                .recordId(1L)
                .isAbnormal(true)
                .build();

        when(accessRecordDao.selectById(1L)).thenReturn(abnormalRecord);
        when(accessRecordDao.selectById(2L)).thenReturn(abnormalRecord);
        when(accessRecordManager.batchProcessAbnormalRecords(recordIds, action, remark, 1001L)).thenReturn(true);

        // Act
        ResponseDTO<String> result = accessRecordService.batchProcessAbnormalRecords(recordIds, action, remark);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("批量处理成功", result.getMsg());

        verify(accessRecordDao, times(1)).selectById(1L);
        verify(accessRecordDao, times(1)).selectById(2L);
        verify(accessRecordManager, times(1)).batchProcessAbnormalRecords(recordIds, action, remark, 1001L);
    }

    @Test
    @DisplayName("测试导出记录 - 成功")
    void testExportRecords_Success() {
        // Arrange
        when(accessRecordManager.exportRecords(any())).thenReturn("export_url");

        // Act
        ResponseDTO<String> result = accessRecordService.exportRecords(queryForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals("export_url", result.getData());

        verify(accessRecordManager, times(1)).exportRecords(any());
    }

    @Test
    @DisplayName("测试获取热力图数据 - 成功")
    void testGetHeatmapData_Success() {
        // Arrange
        String type = "time_location";
        String granularity = "hour";

        Map<String, Object> heatmapData = Map.of(
                "statistics", Map.of(
                        "totalCount", 1500L,
                        "peakTime", "09:00",
                        "activeLocation", "总部大楼1楼",
                        "avgDensity", 25.5
                ),
                "heatmapData", Map.of(
                        "xAxis", Arrays.asList("08:00", "09:00", "10:00"),
                        "yAxis", Arrays.asList("1楼", "2楼"),
                        "data", Arrays.asList(
                                Arrays.asList(0, 0, 15), Arrays.asList(1, 0, 45)
                        )
                )
        );

        when(accessRecordManager.getHeatmapData(type, granularity, queryForm)).thenReturn(heatmapData);

        // Act
        ResponseDTO<Map<String, Object>> result = accessRecordService.getHeatmapData(type, granularity, queryForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(heatmapData, result.getData());

        verify(accessRecordManager, times(1)).getHeatmapData(type, granularity, queryForm);
    }

    @Test
    @DisplayName("测试验证查询参数 - 有效参数")
    void testValidateQueryParams_ValidParams() {
        // Arrange
        AccessRecordQueryForm validForm = AccessRecordQueryForm.builder()
                .userName("张三")
                .accessType("in")
                .accessResult("success")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now())
                .build();

        // Act
        ResponseDTO<Void> result = accessRecordService.validateQueryParams(validForm);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
    }

    @Test
    @DisplayName("测试验证查询参数 - 时间范围无效")
    void testValidateQueryParams_InvalidTimeRange() {
        // Arrange
        AccessRecordQueryForm invalidForm = AccessRecordQueryForm.builder()
                .userName("张三")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().minusDays(1)) // 结束时间早于开始时间
                .build();

        // Act
        ResponseDTO<Void> result = accessRecordService.validateQueryParams(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("时间范围无效"));
    }

    @Test
    @DisplayName("测试验证查询参数 - 通行类型无效")
    void testValidateQueryParams_InvalidAccessType() {
        // Arrange
        AccessRecordQueryForm invalidForm = AccessRecordQueryForm.builder()
                .userName("张三")
                .accessType("invalid_type") // 无效的通行类型
                .build();

        // Act
        ResponseDTO<Void> result = accessRecordService.validateQueryParams(invalidForm);

        // Assert
        assertNotNull(result);
        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("通行类型无效"));
    }

    @Test
    @DisplayName("测试获取今日通行记录数 - 成功")
    void testGetTodayRecordCount_Success() {
        // Arrange
        when(accessRecordDao.selectTodayCount()).thenReturn(150L);

        // Act
        ResponseDTO<Long> result = accessRecordService.getTodayRecordCount();

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(150L, result.getData());

        verify(accessRecordDao, times(1)).selectTodayCount();
    }

    @Test
    @DisplayName("测试获取异常记录列表 - 成功")
    void testGetAbnormalRecords_Success() {
        // Arrange
        List<AccessRecordEntity> abnormalRecords = Arrays.asList(
                AccessRecordEntity.builder()
                        .recordId(1L)
                        .isAbnormal(true)
                        .abnormalType("verification_failed")
                        .abnormalReason("验证失败")
                        .build()
        );

        when(accessRecordDao.selectAbnormalRecords(any())).thenReturn(abnormalRecords);

        // Act
        ResponseDTO<List<AccessRecordEntity>> result = accessRecordService.getAbnormalRecords(pageParam);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(1, result.getData().size());
        assertTrue(result.getData().get(0).getIsAbnormal());

        verify(accessRecordDao, times(1)).selectAbnormalRecords(any());
    }

    @Test
    @DisplayName("测试获取用户通行记录 - 成功")
    void testGetUserRecords_Success() {
        // Arrange
        Long userId = 1001L;
        List<AccessRecordEntity> userRecords = Arrays.asList(testRecord);

        when(accessRecordDao.selectByUserId(userId, any())).thenReturn(userRecords);

        // Act
        ResponseDTO<List<AccessRecordEntity>> result = accessRecordService.getUserRecords(userId, pageParam);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(1, result.getData().size());
        assertEquals(userId, result.getData().get(0).getUserId());

        verify(accessRecordDao, times(1)).selectByUserId(userId, any());
    }

    @Test
    @DisplayName("测试获取设备通行记录 - 成功")
    void testGetDeviceRecords_Success() {
        // Arrange
        Long deviceId = 1L;
        List<AccessRecordEntity> deviceRecords = Arrays.asList(testRecord);

        when(accessRecordDao.selectByDeviceId(deviceId, any())).thenReturn(deviceRecords);

        // Act
        ResponseDTO<List<AccessRecordEntity>> result = accessRecordService.getDeviceRecords(deviceId, pageParam);

        // Assert
        assertNotNull(result);
        assertTrue(result.getOk());
        assertEquals(1, result.getData().size());
        assertEquals(deviceId, result.getData().get(0).getDeviceId());

        verify(accessRecordDao, times(1)).selectByDeviceId(deviceId, any());
    }
}