package net.lab1024.sa.access.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.BiometricRecordDao;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricLogEntity;
import net.lab1024.sa.access.domain.entity.BiometricRecordEntity;
import net.lab1024.sa.access.domain.query.BiometricQueryForm;
import net.lab1024.sa.access.domain.vo.BiometricStatusVO;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 生物识别监控服务实现类单元测试
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生物识别监控服务单元测试")
class BiometricMonitorServiceImplTest {

    @Mock
    private AccessDeviceDao accessDeviceDao;

    @Mock
    private BiometricRecordDao biometricRecordDao;

    @InjectMocks
    private BiometricMonitorServiceImpl biometricMonitorService;

    private AccessDeviceEntity testDevice;
    private BiometricRecordEntity testRecord;

    @BeforeEach
    void setUp() {
        // 初始化测试设备
        testDevice = new AccessDeviceEntity();
        testDevice.setDeviceId(1L);
        testDevice.setDeviceCode("DEV001");
        testDevice.setDeviceName("测试设备");
        testDevice.setDeviceStatus("ONLINE");
        testDevice.setDeviceType("FACE");
        testDevice.setDeletedFlag(0);

        // 初始化测试记录
        testRecord = new BiometricRecordEntity();
        testRecord.setRecordId(1L);
        testRecord.setDeviceId(1L);
        testRecord.setEmployeeId(1001L);
        testRecord.setRecognitionMode("FACE");
        testRecord.setVerificationResult("success");
        testRecord.setProcessingTime(150);
        testRecord.setCreateTime(LocalDateTime.now());

    }

    @Test
    @DisplayName("测试获取所有设备状态")
    void testGetAllDeviceStatus() {
        // Given
        List<AccessDeviceEntity> devices = new ArrayList<>();
        devices.add(testDevice);
        when(accessDeviceDao.selectList(any())).thenReturn(devices);
        when(biometricRecordDao.selectCount(any())).thenReturn(0L);

        // When
        List<BiometricStatusVO> result = biometricMonitorService.getAllDeviceStatus();

        // Then
        assertNotNull(result);
        verify(accessDeviceDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试获取设备详情")
    void testGetDeviceDetail() {
        // Given
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);

        // When
        BiometricDeviceEntity result = biometricMonitorService.getDeviceDetail(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDeviceId());
        verify(accessDeviceDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试获取设备健康状态")
    void testGetDeviceHealth() {
        // Given
        when(accessDeviceDao.selectById(1L)).thenReturn(testDevice);
        when(biometricRecordDao.selectCount(any())).thenReturn(0L);

        // When
        var result = biometricMonitorService.getDeviceHealth(1L);

        // Then
        assertNotNull(result);
        verify(accessDeviceDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试获取今日统计")
    void testGetTodayStatistics() {
        // Given
        List<AccessDeviceEntity> devices = new ArrayList<>();
        devices.add(testDevice);
        when(accessDeviceDao.selectList(any())).thenReturn(devices);
        when(biometricRecordDao.selectCount(any())).thenReturn(0L);

        // When
        var result = biometricMonitorService.getTodayStatistics();

        // Then
        assertNotNull(result);
        verify(accessDeviceDao, times(1)).selectList(any());
    }

    @Test
    @DisplayName("测试获取生物识别日志")
    void testGetBiometricLogs() {
        // Given
        BiometricQueryForm queryForm = new BiometricQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        List<BiometricRecordEntity> records = new ArrayList<>();
        records.add(testRecord);
        com.baomidou.mybatisplus.core.metadata.IPage<BiometricRecordEntity> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                1, 10);
        page.setRecords(records);
        page.setTotal(1L);
        when(biometricRecordDao.selectPage(any(com.baomidou.mybatisplus.core.metadata.IPage.class), any()))
                .thenReturn(page);

        // When
        PageResult<BiometricLogEntity> result = biometricMonitorService.getBiometricLogs(queryForm);

        // Then
        assertNotNull(result);
        assertNotNull(result.getList());
        verify(biometricRecordDao, times(1)).selectPage(any(), any());
    }

    @Test
    @DisplayName("测试获取系统健康状态")
    void testGetSystemHealth() {
        // Given
        List<AccessDeviceEntity> devices = new ArrayList<>();
        devices.add(testDevice);
        when(accessDeviceDao.selectList(any())).thenReturn(devices);
        when(biometricRecordDao.selectCount(any())).thenReturn(0L);

        // When
        var result = biometricMonitorService.getSystemHealth();

        // Then
        assertNotNull(result);
        verify(accessDeviceDao, times(1)).selectList(any());
    }
}
