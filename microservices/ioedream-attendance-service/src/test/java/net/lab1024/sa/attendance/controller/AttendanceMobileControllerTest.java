package net.lab1024.sa.attendance.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

/**
 * 移动端考勤控制器单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("移动端考勤控制器单元测试")
@SuppressWarnings("null")
class AttendanceMobileControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试GPS定位打卡")
    void testGpsPunch() throws Exception {
        String requestBody = """
                {
                    "employeeId": 1001,
                    "latitude": 39.9042,
                    "longitude": 116.4074,
                    "photoUrl": "https://example.com/photo.jpg",
                    "address": "北京市朝阳区"
                }
                """;

        mockMvc.perform(post("/api/attendance/mobile/gps-punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试位置验证")
    void testValidateLocation() throws Exception {
        String requestBody = """
                {
                    "employeeId": 1001,
                    "latitude": 39.9042,
                    "longitude": 116.4074
                }
                """;

        mockMvc.perform(post("/api/attendance/mobile/location/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试离线打卡数据缓存")
    void testCacheOfflinePunch() throws Exception {
        String requestBody = """
                {
                    "employeeId": 1001,
                    "punchDataList": [
                        {
                            "punchType": "上班",
                            "punchTime": "2025-12-04T09:00:00",
                            "latitude": 39.9042,
                            "longitude": 116.4074,
                            "photoUrl": "https://example.com/photo.jpg"
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/attendance/mobile/offline/cache")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试离线数据同步")
    void testSyncOfflinePunches() throws Exception {
        mockMvc.perform(post("/api/attendance/mobile/offline/sync/1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
}
