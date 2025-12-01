package net.lab1024.sa.attendance.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
import net.lab1024.sa.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤服务集成测试
 * 测试完整的考勤业务流程，包括打卡、记录管理、统计等
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("考勤服务集成测试")
class AttendanceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;

    private MockMvc mockMvc;
    private Long testEmployeeId = 1001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试完整考勤流程：打卡->查询->统计")
    void testCompleteAttendanceWorkflow() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime punchTime = LocalDateTime.now();

        // 1. 员工上班打卡
        AttendancePunchDTO punchDTO = new AttendancePunchDTO();
        punchDTO.setEmployeeId(testEmployeeId);
        punchDTO.setPunchType("上班");
        punchDTO.setPunchTime(punchTime);

        String punchResult = mockMvc.perform(post("/attendance/punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(punchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<String> punchResponse = objectMapper.readValue(punchResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, String.class));
        assertTrue(punchResponse.getOk());

        // 2. 查询今日考勤记录
        mockMvc.perform(get("/attendance/records/page")
                .param("employeeId", String.valueOf(testEmployeeId))
                .param("startDate", today.toString())
                .param("endDate", today.toString())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.list").isArray());

        // 3. 员工下班打卡
        AttendancePunchDTO punchOutDTO = new AttendancePunchDTO();
        punchOutDTO.setEmployeeId(testEmployeeId);
        punchOutDTO.setPunchType("下班");
        punchOutDTO.setPunchTime(LocalDateTime.now().plusHours(8));

        mockMvc.perform(post("/attendance/punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(punchOutDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 4. 获取考勤统计
        mockMvc.perform(get("/attendance/statistics")
                .param("employeeId", String.valueOf(testEmployeeId))
                .param("startDate", today.toString())
                .param("endDate", today.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试创建考勤记录流程")
    void testCreateAttendanceRecordWorkflow() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. 创建考勤记录
        AttendanceRecordCreateDTO createDTO = new AttendanceRecordCreateDTO();
        createDTO.setEmployeeId(testEmployeeId);
        createDTO.setAttendanceDate(today);
        createDTO.setPunchInTime(LocalTime.of(9, 0));
        createDTO.setPunchOutTime(LocalTime.of(18, 0));
        createDTO.setAttendanceStatus("NORMAL");

        String createResult = mockMvc.perform(post("/api/attendance/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Long> createResponse = objectMapper.readValue(createResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Long.class));
        Long recordId = createResponse.getData();
        assertNotNull(recordId);

        // 2. 查询创建的记录（通过分页查询验证）
        mockMvc.perform(get("/attendance/records/page")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.recordId").value(recordId))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployeeId));

        // 3. 验证数据库中的记录
        AttendanceRecordEntity record = attendanceRecordRepository.selectById(recordId);
        assertNotNull(record);
        assertEquals(testEmployeeId, record.getEmployeeId());
        assertEquals(today, record.getAttendanceDate());
    }

    @Test
    @DisplayName("测试数据库事务回滚")
    void testTransactionRollback() throws Exception {
        LocalDate today = LocalDate.now();

        // 尝试创建重复的考勤记录（应该失败）
        AttendanceRecordCreateDTO createDTO = new AttendanceRecordCreateDTO();
        createDTO.setEmployeeId(testEmployeeId);
        createDTO.setAttendanceDate(today);
        createDTO.setPunchInTime(LocalTime.of(9, 0));

        // 第一次创建应该成功
        mockMvc.perform(post("/attendance/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 第二次创建应该失败（重复记录）
        mockMvc.perform(post("/api/attendance/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").value(org.hamcrest.Matchers.containsString("已存在")));
    }

    @Test
    @DisplayName("测试补卡申请流程")
    void testMakeupPunchWorkflow() throws Exception {
        LocalDate makeupDate = LocalDate.now().minusDays(1);

        String requestBody = String.format("""
                {
                    "userId": %d,
                    "makeupDate": "%sT09:00:00",
                    "punchType": "上班",
                    "makeupReason": "忘记打卡",
                    "actualPunchTime": "%sT09:00:00",
                    "remark": "集成测试补卡"
                }
                """, testEmployeeId, makeupDate, makeupDate);

        mockMvc.perform(post("/attendance/makeup-punch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 验证补卡记录已创建
        AttendanceRecordEntity record = attendanceRecordRepository
                .findByEmployeeAndDate(testEmployeeId, makeupDate)
                .orElse(null);
        assertNotNull(record);
        assertEquals("补卡", record.getAttendanceStatus());
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() throws Exception {
        LocalDate today = LocalDate.now();

        // 创建多条记录
        for (int i = 0; i < 3; i++) {
            AttendanceRecordCreateDTO createDTO = new AttendanceRecordCreateDTO();
            createDTO.setEmployeeId(testEmployeeId + i);
            createDTO.setAttendanceDate(today);
            createDTO.setPunchInTime(LocalTime.of(9, 0));

            mockMvc.perform(post("/attendance/records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ok").value(true));
        }

        // 批量查询
        mockMvc.perform(get("/attendance/records/page")
                .param("startDate", today.toString())
                .param("endDate", today.toString())
                .param("pageNum", "1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
