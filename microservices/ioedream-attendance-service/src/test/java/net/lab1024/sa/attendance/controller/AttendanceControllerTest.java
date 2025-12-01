package net.lab1024.sa.attendance.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

/**
 * 考勤控制器单元测试
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤控制器单元测试")
class AttendanceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试分页查询考勤记录")
    void testPageRecords() throws Exception {
        mockMvc.perform(get("/api/attendance/records")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists());
    }

    @Test
    @DisplayName("测试员工打卡")
    void testEmployeePunch() throws Exception {
        String requestBody = """
                {
                    "employeeId": 1001,
                    "punchType": "上班",
                    "punchTime": "2025-01-30T09:00:00"
                }
                """;

        mockMvc.perform(post("/api/attendance/punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists());
    }

    @Test
    @DisplayName("测试获取考勤统计")
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/attendance/statistics")
                .param("employeeId", "1001")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-01-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").exists());
    }
}
