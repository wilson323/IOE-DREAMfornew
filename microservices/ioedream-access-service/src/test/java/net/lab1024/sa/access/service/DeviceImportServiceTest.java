package net.lab1024.sa.access.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceImportBatchDao;
import net.lab1024.sa.access.dao.DeviceImportErrorDao;
import net.lab1024.sa.common.entity.access.DeviceImportBatchEntity;
import net.lab1024.sa.common.entity.access.DeviceImportErrorEntity;
import net.lab1024.sa.access.domain.form.DeviceImportQueryForm;
import net.lab1024.sa.access.domain.vo.DeviceImportBatchVO;
import net.lab1024.sa.access.domain.vo.DeviceImportStatisticsVO;
import net.lab1024.sa.access.service.impl.DeviceImportServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 设备导入服务单元测试
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 单元测试版本
 * @since 2025-12-25
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("设备导入服务单元测试")
class DeviceImportServiceTest {

    @Mock
    private DeviceImportBatchDao deviceImportBatchDao;

    @Mock
    private DeviceImportErrorDao deviceImportErrorDao;

    @InjectMocks
    private DeviceImportServiceImpl deviceImportService;

    private DeviceImportBatchEntity mockBatch;
    private DeviceImportQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化测试批次数据
        mockBatch = new DeviceImportBatchEntity();
        mockBatch.setBatchId(1L);
        mockBatch.setBatchName("测试导入批次");
        mockBatch.setFileName("devices.xlsx");
        mockBatch.setImportStatus(1); // 成功状态
        mockBatch.setTotalCount(10);
        mockBatch.setSuccessCount(8);
        mockBatch.setFailedCount(2);
        mockBatch.setOperatorId(1L);
        mockBatch.setOperatorName("测试用户");
        mockBatch.setCreateTime(LocalDateTime.now());
        mockBatch.setDeletedFlag(0); // 0-未删除 1-已删除

        // 初始化查询表单
        mockQueryForm = new DeviceImportQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
        mockQueryForm.setImportStatus(1);
    }

    @Test
    @DisplayName("测试数据验证 - 必填字段验证")
    void testValidateDeviceData_RequiredFields() {
        // Given - 准备测试数据（缺少设备编码）
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", ""); // 空字符串
        rowData.put("deviceName", "测试设备");
        rowData.put("deviceType", 1);

        // When - 执行验证
        String errorMessage = deviceImportService.validateDeviceData(rowData, 2);

        // Then - 验证结果
        assertNotNull(errorMessage, "应该返回错误消息");
        assertTrue(errorMessage.contains("设备编码不能为空"), "错误消息应该提示设备编码为空");
    }

    @Test
    @DisplayName("测试数据验证 - 格式验证")
    void testValidateDeviceData_FormatValidation() {
        // Given - 设备编码格式不正确
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", "123"); // 数字开头，不符合格式要求
        rowData.put("deviceName", "测试设备");
        rowData.put("deviceType", 1);

        // When - 执行验证
        String errorMessage = deviceImportService.validateDeviceData(rowData, 3);

        // Then - 验证结果
        assertNotNull(errorMessage, "应该返回错误消息");
        assertTrue(errorMessage.contains("设备编码格式不正确"), "错误消息应该提示格式不正确");
    }

    @Test
    @DisplayName("测试数据验证 - IP地址格式验证")
    void testValidateDeviceData_IPAddressValidation() {
        // Given - IP地址格式不正确
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", "DEV001");
        rowData.put("deviceName", "测试设备");
        rowData.put("deviceType", 1);
        rowData.put("ipAddress", "192.168.1.256"); // 无效IP

        // When - 执行验证
        String errorMessage = deviceImportService.validateDeviceData(rowData, 4);

        // Then - 验证结果
        assertNotNull(errorMessage, "应该返回错误消息");
        assertTrue(errorMessage.contains("IP地址格式不正确"), "错误消息应该提示IP格式不正确");
    }

    @Test
    @DisplayName("测试数据验证 - 端口范围验证")
    void testValidateDeviceData_PortRangeValidation() {
        // Given - 端口号超出范围
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", "DEV001");
        rowData.put("deviceName", "测试设备");
        rowData.put("deviceType", 1);
        rowData.put("port", "99999"); // 超出有效范围

        // When - 执行验证
        String errorMessage = deviceImportService.validateDeviceData(rowData, 5);

        // Then - 验证结果
        assertNotNull(errorMessage, "应该返回错误消息");
        assertTrue(errorMessage.contains("端口号必须在1-65535之间"), "错误消息应该提示端口范围");
    }

    @Test
    @DisplayName("测试数据验证 - 验证成功")
    void testValidateDeviceData_Success() {
        // Given - 所有字段都有效
        JSONObject rowData = new JSONObject();
        rowData.put("deviceCode", "DEV001");
        rowData.put("deviceName", "测试设备");
        rowData.put("deviceType", 1);
        rowData.put("ipAddress", "192.168.1.100");
        rowData.put("port", "8080");

        // When - 执行验证
        String errorMessage = deviceImportService.validateDeviceData(rowData, 6);

        // Then - 验证结果
        assertNull(errorMessage, "验证成功应该返回null");
    }

    @Test
    @DisplayName("测试分页查询导入批次")
    void testQueryImportBatches() {
        // Given - 准备分页数据
        Page<DeviceImportBatchEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockBatch));
        page.setTotal(1);

        when(deviceImportBatchDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When - 执行查询
        PageResult<DeviceImportBatchVO> pageResult = deviceImportService.queryImportBatches(mockQueryForm);

        // Then - 验证结果
        assertNotNull(pageResult, "分页结果不应为null");
        assertNotNull(pageResult.getList(), "记录列表不应为null");
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertTrue(pageResult.getList().size() <= 10, "记录数不应超过每页大小");

        verify(deviceImportBatchDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取导入统计信息")
    void testGetImportStatistics() {
        // Given - 准备统计数据
        List<DeviceImportBatchEntity> batchList = Arrays.asList(mockBatch);
        when(deviceImportBatchDao.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(batchList);

        // When - 执行查询
        DeviceImportStatisticsVO statistics = deviceImportService.getImportStatistics();

        // Then - 验证结果
        assertNotNull(statistics, "统计信息不应为null");
        assertNotNull(statistics.getTotalImportCount(), "总导入次数不应为null");
        assertNotNull(statistics.getOverallSuccessRate(), "整体成功率不应为null");
        assertTrue(statistics.getOverallSuccessRate() >= 0, "成功率应该>=0");
        assertTrue(statistics.getOverallSuccessRate() <= 100, "成功率应该<=100");

        verify(deviceImportBatchDao, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试查询批次详情 - 不存在的批次")
    void testGetImportBatchDetail_NotFound() {
        // Given - 批次不存在
        Long batchId = 999999L;
        when(deviceImportBatchDao.selectById(999999L)).thenReturn(null);

        // When - 查询不存在的批次
        DeviceImportBatchVO batchVO = deviceImportService.getImportBatchDetail(batchId);

        // Then - 验证结果
        assertNull(batchVO, "不存在的批次应该返回null");

        verify(deviceImportBatchDao, times(1)).selectById(999999L);
    }

    @Test
    @DisplayName("测试查询批次详情 - 成功")
    void testGetImportBatchDetail_Success() {
        // Given - 批次存在
        when(deviceImportBatchDao.selectById(1L)).thenReturn(mockBatch);

        // When - 查询存在的批次
        DeviceImportBatchVO batchVO = deviceImportService.getImportBatchDetail(1L);

        // Then - 验证结果
        assertNotNull(batchVO, "批次应该存在");
        assertEquals(1L, batchVO.getBatchId());
        assertEquals("测试导入批次", batchVO.getBatchName());

        verify(deviceImportBatchDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("测试下载导入模板")
    void testDownloadTemplate() {
        // When - 执行下载
        byte[] templateBytes = deviceImportService.downloadTemplate();

        // Then - 验证结果
        assertNotNull(templateBytes, "模板字节数组不应为null");
        assertTrue(templateBytes.length > 0, "模板文件不应为空");

        // 验证CSV内容包含表头
        String csvContent = new String(templateBytes);
        assertTrue(csvContent.contains("设备编码*,设备名称*,设备类型*"),
                "模板应该包含表头");
    }

    @Test
    @DisplayName("测试导出错误记录")
    void testExportErrors() {
        // Given - 准备错误记录数据
        DeviceImportErrorEntity errorEntity = new DeviceImportErrorEntity();
        errorEntity.setErrorId(1L);
        errorEntity.setBatchId(1L);
        errorEntity.setRowNumber(2);
        errorEntity.setErrorCode("VALIDATION_ERROR");
        errorEntity.setErrorMessage("设备编码格式不正确");
        errorEntity.setRawData("{\"deviceCode\":\"123\"}");

        List<DeviceImportErrorEntity> errorList = Arrays.asList(errorEntity);
        when(deviceImportErrorDao.selectByBatchId(999999L)).thenReturn(errorList);

        // When - 导出错误记录
        byte[] errorBytes = deviceImportService.exportErrors(999999L);

        // Then - 验证结果
        assertNotNull(errorBytes, "错误记录字节数组不应为null");
        assertTrue(errorBytes.length > 0, "错误记录不应为空");

        verify(deviceImportErrorDao, times(1)).selectByBatchId(999999L);
    }

    @Test
    @DisplayName("测试导出错误记录 - 批次无错误")
    void testExportErrors_NoErrors() {
        // Given - 批次无错误记录
        when(deviceImportErrorDao.selectByBatchId(999999L)).thenReturn(Arrays.asList());

        // When - 导出错误记录
        byte[] errorBytes = deviceImportService.exportErrors(999999L);

        // Then - 验证结果
        assertNotNull(errorBytes, "错误记录字节数组不应为null");
        assertEquals(0, errorBytes.length, "无错误时应返回空数组");

        verify(deviceImportErrorDao, times(1)).selectByBatchId(999999L);
    }
}
