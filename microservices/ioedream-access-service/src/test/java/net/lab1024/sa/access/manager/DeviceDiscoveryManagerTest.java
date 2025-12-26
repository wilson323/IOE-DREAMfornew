package net.lab1024.sa.access.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceImportBatchDao;
import net.lab1024.sa.access.dao.DeviceImportErrorDao;
import net.lab1024.sa.access.dao.DeviceImportSuccessDao;
import net.lab1024.sa.access.domain.entity.DeviceImportBatchEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportErrorEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportSuccessEntity;
import net.lab1024.sa.access.domain.form.DeviceDiscoveryRequestForm;
import net.lab1024.sa.access.domain.vo.DeviceDiscoveryResultVO;
import net.lab1024.sa.access.domain.vo.DiscoveredDeviceVO;
import net.lab1024.sa.access.service.DeviceDiscoveryService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 设备发现管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 - 单元测试版本
 * @since 2025-12-25
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("设备发现管理器单元测试")
class DeviceDiscoveryManagerTest {

    @Mock
    private DeviceDao deviceDao;

    @Mock
    private DeviceImportBatchDao deviceImportBatchDao;

    @Mock
    private DeviceImportSuccessDao deviceImportSuccessDao;

    @Mock
    private DeviceImportErrorDao deviceImportErrorDao;

    @Mock
    private DeviceDiscoveryService deviceDiscoveryService;

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DeviceDiscoveryManager deviceDiscoveryManager;

    private List<DiscoveredDeviceVO> testDevices;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testDevices = Arrays.asList(
                createTestDevice("192.168.1.100", "00:1A:2B:3C:4D:5E", "门禁设备1"),
                createTestDevice("192.168.1.101", "00:1A:2B:3C:4D:5F", "门禁设备2"),
                createTestDevice("192.168.1.102", "00:1A:2B:3C:4D:60", "门禁设备3")
        );
    }

    @Test
    @DisplayName("测试批量添加设备 - 成功场景")
    void testBatchAddDiscoveredDevices_Success() {
        log.info("[测试] 测试批量添加设备 - 成功");

        // Given - Mock批次ID回填
        doAnswer(invocation -> {
            DeviceImportBatchEntity batch = invocation.getArgument(0);
            batch.setBatchId(1L);
            return 1;
        }).when(deviceImportBatchDao).insert(any(DeviceImportBatchEntity.class));

        // Mock设备不存在（selectCount返回0）
        when(deviceDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock设备插入成功
        doAnswer(invocation -> {
            DeviceEntity device = invocation.getArgument(0);
            device.setDeviceId(100L);
            return 1;
        }).when(deviceDao).insert(any(DeviceEntity.class));

        // When - 执行批量添加
        DeviceDiscoveryResultVO result = deviceDiscoveryManager.batchAddDiscoveredDevices(
                testDevices, 1L, "测试管理员");

        // Then - 验证结果
        assertNotNull(result, "结果不应为null");
        assertEquals("BATCH-1", result.getScanId(), "扫描ID格式正确");
        assertEquals("COMPLETED", result.getStatus(), "状态应为COMPLETED");
        assertEquals(100, result.getProgress(), "进度应为100%");
        assertEquals(3, result.getTotalDevices(), "总设备数应为3");
        assertTrue(result.getDiscoveredDevices().size() > 0, "发现的设备数应大于0");

        // 验证批次记录被创建
        verify(deviceImportBatchDao, times(1)).insert(any(DeviceImportBatchEntity.class));

        // 验证设备不存在检查被调用3次
        verify(deviceDao, times(3)).selectCount(any(LambdaQueryWrapper.class));

        // 验证设备插入被调用3次
        verify(deviceDao, times(3)).insert(any(DeviceEntity.class));

        // 验证成功记录被批量插入
        verify(deviceImportSuccessDao, times(1)).insertBatch(anyList());

        // 验证批次统计被更新
        verify(deviceImportBatchDao, times(1)).updateStatistics(eq(1L), eq(3), anyInt(), anyInt(), eq(0));

        // 验证批次完成状态被更新
        verify(deviceImportBatchDao, times(1)).completeBatch(
                eq(1L), eq(1), eq("批量添加完成"), any(LocalDateTime.class), anyLong());

        log.info("[测试] 批量添加成功: total={}, success={}",
                result.getTotalDevices(), result.getDiscoveredDevices().size());
    }

    @Test
    @DisplayName("测试设备去重逻辑 - 设备已存在")
    void testDeviceDeduplication_DeviceExists() {
        log.info("[测试] 测试设备去重逻辑 - 设备已存在");

        // Given - Mock批次ID回填
        doAnswer(invocation -> {
            DeviceImportBatchEntity batch = invocation.getArgument(0);
            batch.setBatchId(2L);
            return 1;
        }).when(deviceImportBatchDao).insert(any(DeviceImportBatchEntity.class));

        // Mock第一个设备已存在（selectCount返回1）
        when(deviceDao.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L)  // 第一个设备已存在
                .thenReturn(0L)  // 第二个设备不存在
                .thenReturn(0L); // 第三个设备不存在

        // Mock设备插入成功（只插入后2个）
        doAnswer(invocation -> {
            DeviceEntity device = invocation.getArgument(0);
            device.setDeviceId(100L);
            return 1;
        }).when(deviceDao).insert(any(DeviceEntity.class));

        // When - 执行批量添加（包含已存在的设备）
        DeviceDiscoveryResultVO result = deviceDiscoveryManager.batchAddDiscoveredDevices(
                testDevices, 1L, "测试管理员");

        // Then - 验证结果
        assertNotNull(result, "结果不应为null");
        assertEquals("BATCH-2", result.getScanId());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(3, result.getTotalDevices(), "总设备数应为3");

        // 验证错误记录被插入（第一个设备因已存在被跳过）
        verify(deviceImportErrorDao, times(1)).insertBatch(anyList());

        // 验证只有2个设备被成功插入（第一个被跳过）
        verify(deviceDao, times(2)).insert(any(DeviceEntity.class));

        // 验证成功记录包含2个设备
        verify(deviceImportSuccessDao, times(1)).insertBatch(argThat(list ->
                list != null && list.size() == 2));

        log.info("[测试] 设备去重测试通过: 部分成功，已存在设备被跳过");
    }

    @Test
    @DisplayName("测试错误处理 - 设备添加失败")
    void testErrorHandling_DeviceAddFailed() {
        log.info("[测试] 测试错误处理 - 设备添加失败");

        // Given - Mock批次ID回填
        doAnswer(invocation -> {
            DeviceImportBatchEntity batch = invocation.getArgument(0);
            batch.setBatchId(3L);
            return 1;
        }).when(deviceImportBatchDao).insert(any(DeviceImportBatchEntity.class));

        // Mock所有设备不存在
        when(deviceDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock前2个设备插入成功，第3个失败
        doAnswer(invocation -> {
            DeviceEntity device = invocation.getArgument(0);
            // 第3个设备（192.168.1.102）插入失败
            if ("192.168.1.102".equals(device.getIpAddress())) {
                throw new RuntimeException("数据库插入失败");
            }
            device.setDeviceId(100L);
            return 1;
        }).when(deviceDao).insert(any(DeviceEntity.class));

        // When - 执行批量添加
        DeviceDiscoveryResultVO result = deviceDiscoveryManager.batchAddDiscoveredDevices(
                testDevices, 1L, "测试管理员");

        // Then - 验证部分成功
        assertNotNull(result, "结果不应为null");
        assertEquals(3, result.getTotalDevices(), "总设备数应为3");
        assertTrue(result.getDiscoveredDevices().size() > 0, "应有部分成功");

        // 验证错误记录被插入
        verify(deviceImportErrorDao, times(1)).insertBatch(argThat(list ->
                list != null && list.size() == 1));

        // 验证成功记录包含2个设备
        verify(deviceImportSuccessDao, times(1)).insertBatch(argThat(list ->
                list != null && list.size() == 2));

        // 验证批次状态为部分成功
        ArgumentCaptor<DeviceImportBatchEntity> batchCaptor =
                ArgumentCaptor.forClass(DeviceImportBatchEntity.class);
        verify(deviceImportBatchDao).completeBatch(
                eq(3L), eq(2), eq("批量添加部分成功"), any(LocalDateTime.class), anyLong());

        log.info("[测试] 错误处理测试通过: 部分成功，错误记录正确");
    }

    @Test
    @DisplayName("测试设备发现和批量添加完整流程")
    void testDiscoverAndBatchAdd_FullWorkflow() {
        log.info("[测试] 测试设备发现和批量添加完整流程");

        // Given - Mock设备发现服务响应
        DeviceDiscoveryResultVO startResult = DeviceDiscoveryResultVO.builder()
                .scanId("TEST-SCAN-001")
                .status("RUNNING")
                .progress(0)
                .totalDevices(0)
                .discoveredDevices(Arrays.asList())
                .build();
        when(deviceDiscoveryService.discoverDevices(any(DeviceDiscoveryRequestForm.class)))
                .thenReturn(ResponseDTO.ok(startResult));

        // Mock设备发现完成（异步发现立即完成）
        DeviceDiscoveryResultVO completedResult = DeviceDiscoveryResultVO.builder()
                .scanId("TEST-SCAN-001")
                .status("COMPLETED")
                .progress(100)
                .totalDevices(2)
                .discoveredDevices(Arrays.asList(
                        createTestDevice("192.168.1.200", "00:1A:2B:3C:4D:70", "发现的设备1"),
                        createTestDevice("192.168.1.201", "00:1A:2B:3C:4D:71", "发现的设备2")
                ))
                .build();

        // 首次调用返回RUNNING，第二次返回COMPLETED（模拟异步发现完成）
        when(deviceDiscoveryService.getDiscoveryProgress("TEST-SCAN-001"))
                .thenReturn(ResponseDTO.ok(completedResult));

        // Mock批次ID回填
        doAnswer(invocation -> {
            DeviceImportBatchEntity batch = invocation.getArgument(0);
            batch.setBatchId(4L);
            return 1;
        }).when(deviceImportBatchDao).insert(any(DeviceImportBatchEntity.class));

        // Mock设备不存在
        when(deviceDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock设备插入成功
        doAnswer(invocation -> {
            DeviceEntity device = invocation.getArgument(0);
            device.setDeviceId(200L);
            return 1;
        }).when(deviceDao).insert(any(DeviceEntity.class));

        // When - 执行完整流程
        DeviceDiscoveryResultVO result = deviceDiscoveryManager.discoverAndBatchAdd(
                "192.168.1.0/24",
                Arrays.asList("ONVIF", "PRIVATE"),
                60,
                1L,
                "测试管理员"
        );

        // Then - 验证结果
        assertNotNull(result, "结果不应为null");
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(2, result.getTotalDevices(), "应发现并添加2台设备");

        // 验证设备发现被启动
        verify(deviceDiscoveryService, times(1)).discoverDevices(any(DeviceDiscoveryRequestForm.class));

        // 验证进度查询被调用
        verify(deviceDiscoveryService, times(1)).getDiscoveryProgress("TEST-SCAN-001");

        // 验证设备被批量添加
        verify(deviceDao, times(2)).insert(any(DeviceEntity.class));

        log.info("[测试] 完整流程测试通过: 发现并批量添加成功");
    }

    @Test
    @DisplayName("测试设备发现超时")
    void testDiscoverAndBatchAdd_Timeout() {
        log.info("[测试] 测试设备发现超时");

        // Given - Mock设备发现启动成功
        DeviceDiscoveryResultVO startResult = DeviceDiscoveryResultVO.builder()
                .scanId("TIMEOUT-SCAN-001")
                .status("RUNNING")
                .progress(0)
                .totalDevices(0)
                .discoveredDevices(Arrays.asList())
                .build();
        when(deviceDiscoveryService.discoverDevices(any(DeviceDiscoveryRequestForm.class)))
                .thenReturn(ResponseDTO.ok(startResult));

        // Mock设备发现一直未完成（超时）
        DeviceDiscoveryResultVO runningResult = DeviceDiscoveryResultVO.builder()
                .scanId("TIMEOUT-SCAN-001")
                .status("RUNNING")
                .progress(50)
                .totalDevices(0)
                .discoveredDevices(Arrays.asList())
                .build();
        when(deviceDiscoveryService.getDiscoveryProgress("TIMEOUT-SCAN-001"))
                .thenReturn(ResponseDTO.ok(runningResult));

        // When & Then - 验证超时异常
        assertThrows(RuntimeException.class, () -> {
            deviceDiscoveryManager.discoverAndBatchAdd(
                    "192.168.1.0/24",
                    Arrays.asList("ONVIF"),
                    1, // 1秒超时（用于快速测试）
                    1L,
                    "测试管理员"
            );
        }, "应抛出超时异常");

        log.info("[测试] 设备发现超时测试通过");
    }

    @Test
    @DisplayName("测试导入统计信息")
    void testImportStatistics() {
        log.info("[测试] 测试导入统计信息");

        // Given - Mock批次ID回填
        doAnswer(invocation -> {
            DeviceImportBatchEntity batch = invocation.getArgument(0);
            batch.setBatchId(5L);
            return 1;
        }).when(deviceImportBatchDao).insert(any(DeviceImportBatchEntity.class));

        // Mock设备不存在
        when(deviceDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock设备插入成功
        doAnswer(invocation -> {
            DeviceEntity device = invocation.getArgument(0);
            device.setDeviceId(300L);
            return 1;
        }).when(deviceDao).insert(any(DeviceEntity.class));

        // When - 执行批量添加
        DeviceDiscoveryResultVO result = deviceDiscoveryManager.batchAddDiscoveredDevices(
                testDevices, 1L, "统计测试管理员");

        // Then - 验证统计信息
        assertNotNull(result, "结果不应为null");
        assertEquals(3, result.getTotalDevices(), "总设备数应为3");
        assertTrue(result.getDiscoveredDevices().size() > 0, "发现的设备数应大于0");

        // 验证批次统计被正确更新
        verify(deviceImportBatchDao, times(1)).updateStatistics(
                eq(5L), eq(3), eq(3), eq(0), eq(0));

        log.info("[测试] 导入统计测试通过: total={}, success={}, failed={}",
                3, 3, 0);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试设备
     */
    private DiscoveredDeviceVO createTestDevice(String ipAddress, String macAddress, String deviceName) {
        return DiscoveredDeviceVO.builder()
                .ipAddress(ipAddress)
                .macAddress(macAddress)
                .deviceName(deviceName)
                .deviceModel("TEST-MODEL")
                .deviceBrand("TEST-BRAND")
                .firmwareVersion("v1.0.0")
                .port(80)
                .protocol("TEST-PROTOCOL")
                .deviceType(1)
                .deviceStatus(1)
                .location("测试位置")
                .verified(false)
                .build();
    }
}
