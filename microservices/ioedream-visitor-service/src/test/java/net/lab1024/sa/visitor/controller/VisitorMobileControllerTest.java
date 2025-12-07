package net.lab1024.sa.visitor.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

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
        String requestBody = """
                {
                    "visitorName": "张三",
                    "idNumber": "110101199001011234"
                }
                """;

        // validateVisitorInfo 是在 Controller 中直接实现的，需要 mock visitorQueryService
        // VisitorQueryService.getVisitorByIdNumber返回ResponseDTO<?>，使用doReturn避免泛型通配符类型捕获问题
        ResponseDTO<?> mockResponse = ResponseDTO.ok(new net.lab1024.sa.visitor.domain.vo.VisitorVO());
        doReturn(mockResponse).when(visitorQueryService).getVisitorByIdNumber(eq("110101199001011234"));

        mockMvc.perform(post("/api/v1/mobile/visitor/validate")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取被访人信息")
    void testGetVisiteeInfo() throws Exception {
        // getVisiteeInfo 调用的是 visitorQueryService.getVisitorsByVisiteeId()
        // VisitorQueryService.getVisitorsByVisiteeId返回ResponseDTO<?>，使用doReturn避免泛型通配符类型捕获问题
        ResponseDTO<?> mockResponse = ResponseDTO.ok(new ArrayList<>());
        doReturn(mockResponse).when(visitorQueryService).getVisitorsByVisiteeId(eq(1001L), eq(10));

        mockMvc.perform(get("/api/v1/mobile/visitor/visitee/1001")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取访问区域")
    void testGetVisitAreas() throws Exception {
        // getVisitAreas 在Controller中直接返回空列表，不需要mock

        mockMvc.perform(get("/api/v1/mobile/visitor/areas")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取预约类型")
    void testGetAppointmentTypes() throws Exception {
        // getAppointmentTypes 在Controller中直接返回硬编码列表，不需要mock

        mockMvc.perform(get("/api/v1/mobile/visitor/appointment-types")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取帮助信息")
    void testGetHelpInfo() throws Exception {
        // getHelpInfo 在Controller中直接返回硬编码信息，不需要mock

        mockMvc.perform(get("/api/v1/mobile/visitor/help")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    @Test
    @DisplayName("测试获取个人统计")
    void testGetPersonalStatistics() throws Exception {
        // getPersonalStatistics 调用的是 visitorQueryService.getVisitorsByVisiteeId
        // VisitorQueryService.getVisitorsByVisiteeId返回ResponseDTO<?>，使用doReturn避免泛型通配符类型捕获问题
        ResponseDTO<?> mockResponse = ResponseDTO.ok(new ArrayList<>());
        doReturn(mockResponse).when(visitorQueryService).getVisitorsByVisiteeId(eq(1001L), eq(10));

        mockMvc.perform(get("/api/v1/mobile/visitor/statistics/1001")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }
}
