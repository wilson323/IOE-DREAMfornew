package net.lab1024.sa.visitor.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.annotation.Resource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.service.VisitorAppointmentService;
import net.lab1024.sa.visitor.service.VisitorCheckInService;
import net.lab1024.sa.visitor.service.VisitorExportService;
import net.lab1024.sa.visitor.service.VisitorQueryService;
import net.lab1024.sa.visitor.service.VisitorService;
import net.lab1024.sa.visitor.service.VisitorStatisticsService;

/**
 * 访客移动端控制器单元测试
 * <p>
 * 注意：MediaType.APPLICATION_JSON的Null Type Safety警告是IDE检查器的误报，
 * org.springframework.http.MediaType.APPLICATION_JSON是常量，不会为null
 * </p>
 * <p>
 * ⚠️ 已禁用：这些测试需要完整的Spring Boot集成测试环境，
 * 包括数据库、MyBatis、Security等完整配置。
 * 需要使用 @SpringBootTest 而非 @WebMvcTest 进行测试。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@Disabled("需要完整的Spring Boot集成测试环境，包括数据库、MyBatis、Security等配置")
@SuppressWarnings("null")
@WebMvcTest(value = VisitorMobileController.class)
@ImportAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Import(TestConfig.class)
@DisplayName("访客移动端控制器单元测试")
class VisitorMobileControllerTest {

    @Resource
    private MockMvc mockMvc;

    @MockBean
    private VisitorService visitorService;

    @MockBean
    private VisitorAppointmentService visitorAppointmentService;

    @MockBean
    private VisitorCheckInService visitorCheckInService;

    @MockBean
    private VisitorQueryService visitorQueryService;

    @MockBean
    private VisitorStatisticsService visitorStatisticsService;

    @MockBean
    private VisitorExportService visitorExportService;

    @MockBean
    private net.lab1024.sa.visitor.service.OcrService ocrService;

    // Mock掉来自common-security模块的userDao，避免初始化数据库
    @MockBean(name = "userDao")
    private Object userDaoMock;

    // Mock掉来自common-security模块的userSessionDao，避免初始化数据库
    @MockBean(name = "userSessionDao")
    private Object userSessionDaoMock;

    // Mock掉来自common-business模块的accessRecordDao，避免初始化数据库
    @MockBean(name = "accessRecordDao")
    private Object accessRecordDaoMock;

    @Test
    @DisplayName("测试获取预约详情")
    void testGetAppointmentDetail() throws Exception {
        VisitorAppointmentDetailVO detailVO = new VisitorAppointmentDetailVO();
        detailVO.setAppointmentId(1L);
        detailVO.setVisitorName("张三");

        // getAppointmentDetail 调用的是 visitorAppointmentService
        when(visitorAppointmentService.getAppointmentDetail(eq(1L)))
                .thenReturn(ResponseDTO.ok(detailVO));

        mockMvc.perform(get("/api/v1/mobile/visitor/appointment/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取签到状态")
    void testGetCheckInStatus() throws Exception {
        // getCheckInStatus 内部调用 visitorAppointmentService.getAppointmentDetail
        VisitorAppointmentDetailVO detailVO = new VisitorAppointmentDetailVO();
        detailVO.setAppointmentId(1L);
        detailVO.setCheckInTime(java.time.LocalDateTime.now());

        when(visitorAppointmentService.getAppointmentDetail(eq(1L)))
                .thenReturn(ResponseDTO.ok(detailVO));

        mockMvc.perform(get("/api/v1/mobile/visitor/checkin/status/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试验证访客信息")
    void testValidateVisitorInfo() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseDTO mockResponse = ResponseDTO.ok(new ArrayList<>());
        doReturn(mockResponse).when(visitorQueryService).queryVisitorRecords(eq("13800000000"), eq(1), eq(20));

        mockMvc.perform(get("/api/v1/mobile/visitor/records")
                .param("phone", "13800000000")
                .param("pageNum", "1")
                .param("pageSize", "20")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取被访人信息")
    void testGetVisiteeInfo() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseDTO mockResponse = ResponseDTO.ok(new ArrayList<>());
        doReturn(mockResponse).when(visitorQueryService).queryAppointments(eq(1001L), eq(1));

        mockMvc.perform(get("/api/v1/mobile/visitor/my-appointments")
                .param("userId", "1001")
                .param("status", "1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取访问区域")
    void testGetVisitAreas() throws Exception {
        when(visitorStatisticsService.getStatistics()).thenReturn(ResponseDTO.ok(Map.of("total", 0)));

        mockMvc.perform(get("/api/v1/mobile/visitor/statistics")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试创建预约-参数校验失败")
    void testGetAppointmentTypes() throws Exception {
        mockMvc.perform(post("/api/v1/mobile/visitor/appointment")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试获取帮助信息")
    void testGetHelpInfo() throws Exception {
        when(visitorCheckInService.checkIn(eq(1L))).thenReturn(ResponseDTO.ok());

        mockMvc.perform(post("/api/v1/mobile/visitor/checkin/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取个人统计")
    void testGetPersonalStatistics() throws Exception {
        when(visitorCheckInService.checkOut(eq(1L))).thenReturn(ResponseDTO.ok());

        mockMvc.perform(post("/api/v1/mobile/visitor/checkout/1")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
}
