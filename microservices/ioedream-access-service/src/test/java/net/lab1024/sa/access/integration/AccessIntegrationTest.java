package net.lab1024.sa.access.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.BiometricRecordDao;
import net.lab1024.sa.access.domain.entity.AccessDeviceEntity;
import net.lab1024.sa.access.domain.entity.BiometricRecordEntity;
import net.lab1024.sa.access.service.BiometricMonitorService;

/**
 * 门禁服务集成测试
 * 测试完整的门禁业务流程，包括设备管理、生物识别、监控等
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("门禁服务集成测试")
class AccessIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private BiometricMonitorService biometricMonitorService;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private BiometricRecordDao biometricRecordDao;

    private MockMvc mockMvc;
    private Long testDeviceId = 1L;
    private Long testEmployeeId = 1001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试设备监控完整流程")
    void testDeviceMonitoringWorkflow() throws Exception {
        // 1. 获取所有设备状态
        mockMvc.perform(get("/api/access/biometric/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 2. 获取设备详情
        mockMvc.perform(get("/api/access/biometric/device/{deviceId}", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 3. 获取设备健康状态
        mockMvc.perform(get("/api/access/biometric/device/{deviceId}/health", testDeviceId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());

        // 4. 获取系统健康状态
        mockMvc.perform(get("/api/access/biometric/system/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试生物识别日志查询流程")
    void testBiometricLogsWorkflow() throws Exception {
        // 1. 分页查询生物识别日志
        String queryBody = """
                {
                    "pageNum": 1,
                    "pageSize": 10,
                    "deviceId": %d,
                    "userId": %d
                }
                """.formatted(testDeviceId, testEmployeeId);

        mockMvc.perform(post("/api/access/biometric/logs/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content(queryBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.list").isArray());

        // 2. 获取今日统计
        mockMvc.perform(get("/api/access/biometric/statistics/today")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试设备状态更新流程")
    void testDeviceStatusUpdateWorkflow() throws Exception {
        // 1. 查询设备
        AccessDeviceEntity device = accessDeviceDao.selectById(testDeviceId);
        if (device == null) {
            // 创建测试设备
            device = new AccessDeviceEntity();
            device.setDeviceId(testDeviceId);
            device.setDeviceName("测试设备");
            device.setDeviceCode("TEST001");
            device.setDeviceType("FACE");
            device.setDeviceStatus("ONLINE");
            device.setDeletedFlag(0);
            accessDeviceDao.insert(device);
        }

        // 2. 获取设备状态
        var statusList = biometricMonitorService.getAllDeviceStatus();
        assertNotNull(statusList);

        // 3. 获取设备详情
        var deviceDetail = biometricMonitorService.getDeviceDetail(testDeviceId);
        assertNotNull(deviceDetail);
    }

    @Test
    @DisplayName("测试生物识别记录创建和查询")
    void testBiometricRecordWorkflow() throws Exception {
        // 1. 创建生物识别记录（通过服务层）
        BiometricRecordEntity record = new BiometricRecordEntity();
        record.setDeviceId(testDeviceId);
        record.setEmployeeId(testEmployeeId);
        record.setRecognitionMode("FACE");
        record.setVerificationResult("success");
        record.setProcessingTime(150);
        record.setCreateTime(java.time.LocalDateTime.now());

        int insertResult = biometricRecordDao.insert(record);
        assertTrue(insertResult > 0);
        assertNotNull(record.getRecordId());

        // 2. 查询记录
        BiometricRecordEntity savedRecord = biometricRecordDao.selectById(record.getRecordId());
        assertNotNull(savedRecord);
        assertEquals(testEmployeeId, savedRecord.getEmployeeId());
        assertEquals("success", savedRecord.getVerificationResult());

        // 3. 通过服务层查询日志
        net.lab1024.sa.access.domain.query.BiometricQueryForm queryForm = new net.lab1024.sa.access.domain.query.BiometricQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);
        queryForm.setDeviceId(testDeviceId);

        var logs = biometricMonitorService.getBiometricLogs(queryForm);
        assertNotNull(logs);
        assertNotNull(logs.getList());
    }

    @Test
    @DisplayName("测试数据库事务一致性")
    void testDatabaseTransactionConsistency() throws Exception {
        // 1. 创建设备
        AccessDeviceEntity device = new AccessDeviceEntity();
        device.setDeviceName("事务测试设备");
        device.setDeviceCode("TX001");
        device.setDeviceType("FACE");
        device.setDeviceStatus("ONLINE");
        device.setDeletedFlag(0);
        accessDeviceDao.insert(device);
        Long deviceId = device.getDeviceId();

        // 2. 创建关联的记录
        BiometricRecordEntity record = new BiometricRecordEntity();
        record.setDeviceId(deviceId);
        record.setEmployeeId(testEmployeeId);
        record.setRecognitionMode("FACE");
        record.setVerificationResult("success");
        record.setProcessingTime(150);
        record.setCreateTime(java.time.LocalDateTime.now());
        biometricRecordDao.insert(record);

        // 3. 验证关联关系
        BiometricRecordEntity savedRecord = biometricRecordDao.selectById(record.getRecordId());
        assertNotNull(savedRecord);
        assertEquals(deviceId, savedRecord.getDeviceId());

        // 4. 通过设备查询记录
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BiometricRecordEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(BiometricRecordEntity::getDeviceId, deviceId);
        Long count = biometricRecordDao.selectCount(wrapper);
        assertTrue(count > 0);
    }
}
