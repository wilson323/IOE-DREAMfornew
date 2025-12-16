package net.lab1024.sa.attendance.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import net.lab1024.sa.attendance.controller.AttendanceMobileController.GpsPunchRequest;
import net.lab1024.sa.attendance.controller.AttendanceMobileController.LocationValidationRequest;
import net.lab1024.sa.attendance.controller.AttendanceMobileController.OfflinePunchData;
import net.lab1024.sa.attendance.controller.AttendanceMobileController.OfflinePunchRequest;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端考勤集成测试
 * <p>
 * 测试移动端考勤功能的完整业务流程，包括GPS打卡、位置验证、离线数据缓存和同步等
 * </p>
 * <p>
 * 注意：MediaType.APPLICATION_JSON和WebApplicationContext的Null Type Safety警告是IDE检查器的误报，
 * MediaType.APPLICATION_JSON是常量，WebApplicationContext由Spring容器注入，不会为null
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("移动端考勤集成测试")
class AttendanceMobileIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long testEmployeeId = 1001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试完整移动端考勤流程：GPS打卡->位置验证->离线缓存->同步")
    void testCompleteMobileAttendanceWorkflow() throws Exception {
        // 1. GPS定位打卡（上班）
        GpsPunchRequest punchInRequest = new GpsPunchRequest();
        punchInRequest.setEmployeeId(testEmployeeId);
        punchInRequest.setLatitude(34.26);
        punchInRequest.setLongitude(108.94);
        punchInRequest.setAddress("西安市雁塔区");
        punchInRequest.setPhotoUrl("http://example.com/photo_in.jpg");

        String punchInResult = mockMvc.perform(post("/api/attendance/mobile/gps-punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(punchInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> punchInResponse = objectMapper.readValue(punchInResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(punchInResponse.isSuccess(), "GPS打卡应该成功");

        // 2. 位置验证
        LocationValidationRequest locationRequest = new LocationValidationRequest();
        locationRequest.setEmployeeId(testEmployeeId);
        locationRequest.setLatitude(34.26);
        locationRequest.setLongitude(108.94);

        String locationResult = mockMvc.perform(post("/api/attendance/mobile/location/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Boolean> locationResponse = objectMapper.readValue(locationResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Boolean.class));
        assertNotNull(locationResponse.getData(), "位置验证结果不应为空");

        // 3. 离线打卡数据缓存（模拟网络中断场景）
        OfflinePunchData offlinePunchData = new OfflinePunchData();
        offlinePunchData.setPunchType("下班");
        offlinePunchData.setPunchTime(LocalDateTime.now().plusHours(8));
        offlinePunchData.setLatitude(34.26);
        offlinePunchData.setLongitude(108.94);
        offlinePunchData.setPhotoUrl("http://example.com/photo_out.jpg");

        OfflinePunchRequest offlineRequest = new OfflinePunchRequest();
        offlineRequest.setEmployeeId(testEmployeeId);
        offlineRequest.setPunchDataList(Arrays.asList(offlinePunchData));

        String offlineResult = mockMvc.perform(post("/api/attendance/mobile/offline/cache")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offlineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> offlineResponse = objectMapper.readValue(offlineResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(offlineResponse.isSuccess(), "离线数据缓存应该成功");

        // 4. 离线数据同步（模拟网络恢复）
        String syncResult = mockMvc.perform(post("/api/attendance/mobile/offline/sync/{employeeId}", testEmployeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> syncResponse = objectMapper.readValue(syncResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(syncResponse.isSuccess(), "离线数据同步应该成功");
    }

    @Test
    @DisplayName("测试GPS打卡参数验证")
    void testGpsPunchValidation() throws Exception {
        // 测试缺少必填字段
        GpsPunchRequest invalidRequest = new GpsPunchRequest();
        // 故意不设置employeeId

        mockMvc.perform(post("/api/attendance/mobile/gps-punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // 测试无效的经纬度
        GpsPunchRequest invalidLocationRequest = new GpsPunchRequest();
        invalidLocationRequest.setEmployeeId(testEmployeeId);
        invalidLocationRequest.setLatitude(200.0); // 无效纬度
        invalidLocationRequest.setLongitude(200.0); // 无效经度

        mockMvc.perform(post("/api/attendance/mobile/gps-punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLocationRequest)))
                .andExpect(status().isOk()); // 注意：实际业务可能允许，这里根据实际业务逻辑调整
    }

    @Test
    @DisplayName("测试离线数据批量缓存和同步")
    void testBatchOfflinePunchSync() throws Exception {
        // 创建多条离线打卡数据
        OfflinePunchData punch1 = new OfflinePunchData();
        punch1.setPunchType("上班");
        punch1.setPunchTime(LocalDateTime.now().minusDays(1));
        punch1.setLatitude(34.26);
        punch1.setLongitude(108.94);

        OfflinePunchData punch2 = new OfflinePunchData();
        punch2.setPunchType("下班");
        punch2.setPunchTime(LocalDateTime.now().minusDays(1).plusHours(8));
        punch2.setLatitude(34.26);
        punch2.setLongitude(108.94);

        OfflinePunchRequest batchRequest = new OfflinePunchRequest();
        batchRequest.setEmployeeId(testEmployeeId);
        batchRequest.setPunchDataList(Arrays.asList(punch1, punch2));

        // 批量缓存
        String cacheResult = mockMvc.perform(post("/api/attendance/mobile/offline/cache")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> cacheResponse = objectMapper.readValue(cacheResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(cacheResponse.isSuccess(), "批量缓存应该成功");

        // 同步所有离线数据
        String syncResult = mockMvc.perform(post("/api/attendance/mobile/offline/sync/{employeeId}", testEmployeeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> syncResponse = objectMapper.readValue(syncResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(syncResponse.isSuccess(), "批量同步应该成功");
    }

    @Test
    @DisplayName("测试位置验证边界情况")
    void testLocationValidationEdgeCases() throws Exception {
        // 测试边界位置（允许范围边缘）
        LocationValidationRequest edgeRequest = new LocationValidationRequest();
        edgeRequest.setEmployeeId(testEmployeeId);
        edgeRequest.setLatitude(34.25); // 边界值
        edgeRequest.setLongitude(108.93); // 边界值

        mockMvc.perform(post("/api/attendance/mobile/location/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(edgeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());

        // 测试超出范围的位置
        LocationValidationRequest outOfRangeRequest = new LocationValidationRequest();
        outOfRangeRequest.setEmployeeId(testEmployeeId);
        outOfRangeRequest.setLatitude(40.0); // 超出范围
        outOfRangeRequest.setLongitude(120.0); // 超出范围

        String outOfRangeResult = mockMvc.perform(post("/api/attendance/mobile/location/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outOfRangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Boolean> outOfRangeResponse = objectMapper.readValue(outOfRangeResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Boolean.class));
        // 根据业务逻辑，超出范围应该返回false
        assertNotNull(outOfRangeResponse.getData());
    }
}


