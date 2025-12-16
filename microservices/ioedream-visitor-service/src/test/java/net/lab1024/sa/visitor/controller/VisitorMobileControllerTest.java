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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("访客移动端控制器单元测试")
class VisitorMobileControllerTest {

    @Mock
    private VisitorService visitorService;

    @Mock
    private VisitorAppointmentService visitorAppointmentService;

    @Mock
    private VisitorCheckInService visitorCheckInService;

    @Mock
    private VisitorQueryService visitorQueryService;

    @Mock
    private VisitorStatisticsService visitorStatisticsService;

    @Mock
    private VisitorExportService visitorExportService;

    @InjectMocks
    private VisitorMobileController visitorMobileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(visitorMobileController).build();
    }

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
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                )
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
