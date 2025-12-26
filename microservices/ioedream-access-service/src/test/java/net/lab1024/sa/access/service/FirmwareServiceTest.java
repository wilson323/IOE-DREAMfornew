package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.access.domain.entity.DeviceFirmwareEntity;
import net.lab1024.sa.access.domain.form.FirmwareQueryForm;
import net.lab1024.sa.access.domain.form.FirmwareUploadForm;
import net.lab1024.sa.access.domain.vo.FirmwareDetailVO;
import net.lab1024.sa.access.domain.vo.FirmwareVO;
import net.lab1024.sa.access.service.impl.FirmwareServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 固件管理服务单元测试
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 单元测试版本
 * @since 2025-12-25
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("固件管理服务单元测试")
class FirmwareServiceTest {

    @Mock
    private DeviceFirmwareDao deviceFirmwareDao;

    @InjectMocks
    private FirmwareServiceImpl firmwareService;

    private DeviceFirmwareEntity mockFirmware;
    private FirmwareUploadForm mockUploadForm;
    private FirmwareQueryForm mockQueryForm;

    @BeforeEach
    void setUp() {
        // 初始化测试固件数据
        mockFirmware = new DeviceFirmwareEntity();
        mockFirmware.setFirmwareId(1L);
        mockFirmware.setFirmwareName("测试固件");
        mockFirmware.setFirmwareVersion("1.0.0");
        mockFirmware.setDeviceType(1);
        mockFirmware.setDeviceModel("ACS-3000");
        mockFirmware.setBrand("Hikvision");
        mockFirmware.setMinVersion("1.0.0");
        mockFirmware.setMaxVersion("2.0.0");
        mockFirmware.setFirmwareStatus(2);  // 正式发布（满足selectAvailableFirmware查询条件）
        mockFirmware.setIsEnabled(1);        // 启用
        mockFirmware.setDownloadCount(0);
        mockFirmware.setFirmwareFilePath("/firmware/test.bin");
        mockFirmware.setFirmwareFileSize(1024L);
        mockFirmware.setFirmwareFileMd5("abc123def456");
        mockFirmware.setCreateTime(LocalDateTime.now());

        // 初始化上传表单
        mockUploadForm = new FirmwareUploadForm();
        mockUploadForm.setFirmwareName("测试固件");
        mockUploadForm.setFirmwareVersion("1.0.0");
        mockUploadForm.setDeviceType(1);
        mockUploadForm.setDeviceModel("ACS-3000");
        mockUploadForm.setBrand("Hikvision");
        mockUploadForm.setMinVersion("1.0.0");
        mockUploadForm.setMaxVersion("2.0.0");
        mockUploadForm.setIsForce(0);
        mockUploadForm.setReleaseNotes("测试固件发布说明");

        // 初始化查询表单
        mockQueryForm = new FirmwareQueryForm();
        mockQueryForm.setPageNum(1);
        mockQueryForm.setPageSize(10);
        mockQueryForm.setDeviceType(1);
    }

    @Test
    @DisplayName("分页查询固件列表 - 成功")
    void testQueryFirmwarePage_Success() {
        // Given
        Page<DeviceFirmwareEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockFirmware));
        page.setTotal(1);

        when(deviceFirmwareDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        PageResult<FirmwareVO> pageResult = firmwareService.queryFirmwarePage(mockQueryForm);

        // Then
        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertEquals("测试固件", pageResult.getList().get(0).getFirmwareName());

        verify(deviceFirmwareDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取固件详情 - 成功")
    void testGetFirmwareDetail_Success() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When
        FirmwareDetailVO firmwareDetail = firmwareService.getFirmwareDetail(1L);

        // Then
        assertNotNull(firmwareDetail);
        assertEquals(1L, firmwareDetail.getFirmwareId());
        assertEquals("测试固件", firmwareDetail.getFirmwareName());
        assertEquals("1.0.0", firmwareDetail.getFirmwareVersion());
        assertEquals(1, firmwareDetail.getDeviceType());

        verify(deviceFirmwareDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("获取固件详情 - 固件不存在")
    void testGetFirmwareDetail_NotFound() {
        // Given
        when(deviceFirmwareDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            firmwareService.getFirmwareDetail(999L);
        });

        verify(deviceFirmwareDao, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("查询可用固件 - 成功")
    void testQueryAvailableFirmware_Success() {
        // Given
        List<DeviceFirmwareEntity> firmwareList = Arrays.asList(mockFirmware);
        // Mock selectAvailableFirmware 方法（实际调用的DAO方法）
        when(deviceFirmwareDao.selectAvailableFirmware(1, "ACS-3000"))
                .thenReturn(firmwareList);
        // Mock selectById - checkVersionCompatibility 内部会调用此方法
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When
        List<FirmwareVO> result = firmwareService.queryAvailableFirmware(1, "ACS-3000", "1.5.0");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试固件", result.get(0).getFirmwareName());

        verify(deviceFirmwareDao, times(1)).selectAvailableFirmware(1, "ACS-3000");
        verify(deviceFirmwareDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("版本兼容性检查 - 兼容")
    void testCheckVersionCompatibility_Compatible() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When
        Boolean isCompatible = firmwareService.checkVersionCompatibility(1L, "1.5.0");

        // Then
        assertTrue(isCompatible);

        verify(deviceFirmwareDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("版本兼容性检查 - 不兼容（版本过低）")
    void testCheckVersionCompatibility_NotCompatible_TooLow() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When
        Boolean isCompatible = firmwareService.checkVersionCompatibility(1L, "0.9.0");

        // Then
        assertFalse(isCompatible);

        verify(deviceFirmwareDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("版本兼容性检查 - 不兼容（版本过高）")
    void testCheckVersionCompatibility_NotCompatible_TooHigh() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);

        // When
        Boolean isCompatible = firmwareService.checkVersionCompatibility(1L, "2.1.0");

        // Then
        assertFalse(isCompatible);

        verify(deviceFirmwareDao, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("更新固件状态 - 成功")
    void testUpdateFirmwareStatus_Success() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(deviceFirmwareDao.updateById(any(DeviceFirmwareEntity.class))).thenReturn(1);

        // When
        Boolean result = firmwareService.updateFirmwareStatus(1L, 2);

        // Then
        assertTrue(result);
        assertEquals(2, mockFirmware.getFirmwareStatus());

        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(deviceFirmwareDao, times(1)).updateById(any(DeviceFirmwareEntity.class));
    }

    @Test
    @DisplayName("更新固件启用状态 - 成功")
    void testUpdateFirmwareEnabled_Success() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(deviceFirmwareDao.updateById(any(DeviceFirmwareEntity.class))).thenReturn(1);

        // When
        Boolean result = firmwareService.updateFirmwareEnabled(1L, 0);

        // Then
        assertTrue(result);
        assertEquals(0, mockFirmware.getIsEnabled());

        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(deviceFirmwareDao, times(1)).updateById(any(DeviceFirmwareEntity.class));
    }

    @Test
    @DisplayName("计算文件MD5 - 成功")
    void testCalculateFileMd5_Success() {
        // Given
        String testContent = "Test firmware content for MD5 calculation";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.md5",
                "application/octet-stream",
                testContent.getBytes(StandardCharsets.UTF_8)
        );

        // When
        String md5 = firmwareService.calculateFileMd5(mockFile);

        // Then
        assertNotNull(md5);
        assertEquals(32, md5.length()); // MD5应该是32位十六进制字符串
        assertTrue(md5.matches("[a-fA-F0-9]{32}"));
    }

    @Test
    @DisplayName("验证文件MD5 - 成功")
    void testVerifyFileMd5_Success() {
        // Given
        String testContent = "Test firmware content for MD5 verification";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.md5",
                "application/octet-stream",
                testContent.getBytes(StandardCharsets.UTF_8)
        );

        String expectedMd5 = firmwareService.calculateFileMd5(mockFile);

        // When
        Boolean isValid = firmwareService.verifyFileMd5(mockFile, expectedMd5);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证文件MD5 - 失败")
    void testVerifyFileMd5_Failed() {
        // Given
        String testContent = "Test firmware content";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.md5",
                "application/octet-stream",
                testContent.getBytes(StandardCharsets.UTF_8)
        );

        String expectedMd5 = "invalid-md5-value";

        // When
        Boolean isValid = firmwareService.verifyFileMd5(mockFile, expectedMd5);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("下载固件 - 成功")
    void testDownloadFirmware_Success() {
        // Given
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        // Mock incrementDownloadCount 方法（返回更新行数）
        when(deviceFirmwareDao.incrementDownloadCount(1L)).thenReturn(1);

        // When
        FirmwareDetailVO firmwareDetail = firmwareService.downloadFirmware(1L);

        // Then
        assertNotNull(firmwareDetail);
        assertEquals(1L, firmwareDetail.getFirmwareId());
        assertEquals("/firmware/test.bin", firmwareDetail.getFirmwareFilePath());

        // 验证下载次数增加方法被调用
        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(deviceFirmwareDao, times(1)).incrementDownloadCount(1L);
    }

    @Test
    @DisplayName("删除固件 - 成功")
    void testDeleteFirmware_Success() {
        // Given
        mockFirmware.setFirmwareStatus(2); // 已发布，可以删除
        when(deviceFirmwareDao.selectById(1L)).thenReturn(mockFirmware);
        when(deviceFirmwareDao.deleteById(1L)).thenReturn(1);

        // When
        Boolean result = firmwareService.deleteFirmware(1L);

        // Then
        assertTrue(result);

        verify(deviceFirmwareDao, times(1)).selectById(1L);
        verify(deviceFirmwareDao, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除固件 - 固件不存在")
    void testDeleteFirmware_NotFound() {
        // Given
        when(deviceFirmwareDao.selectById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            firmwareService.deleteFirmware(999L);
        });

        verify(deviceFirmwareDao, times(1)).selectById(999L);
        verify(deviceFirmwareDao, never()).deleteById(any());
    }
}
