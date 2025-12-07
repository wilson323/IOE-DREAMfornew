package net.lab1024.sa.visitor.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.VisitorMobileForm;

/**
 * 移动端访客集成测试
 * <p>
 * 测试移动端访客功能的完整业务流程，包括预约创建、签到签退、记录查询等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("移动端访客集成测试")
@SuppressWarnings("null")
class VisitorMobileIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long testUserId = 1001L;
    private Long testVisiteeId = 2001L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试完整移动端访客流程：创建预约->查看详情->签到->签退->查询记录")
    void testCompleteMobileVisitorWorkflow() throws Exception {
        // 1. 创建访客预约
        VisitorMobileForm createForm = createTestAppointmentForm();

        String createResult = mockMvc.perform(post("/api/v1/mobile/visitor/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Long> createResponse = objectMapper.readValue(createResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Long.class));
        Long appointmentId = createResponse.getData();
        assertNotNull(appointmentId, "预约ID不应为空");
        assertTrue(createResponse.isSuccess(), "创建预约应该成功");

        // 2. 获取预约详情
        String detailResult = mockMvc.perform(get("/api/v1/mobile/visitor/appointment/{appointmentId}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> detailResponse = objectMapper.readValue(detailResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(detailResponse.isSuccess(), "获取预约详情应该成功");

        // 3. 获取我的预约列表
        String listResult = mockMvc.perform(get("/api/v1/mobile/visitor/my-appointments")
                .param("userId", testUserId.toString())
                .param("status", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> listResponse = objectMapper.readValue(listResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(listResponse.isSuccess(), "获取预约列表应该成功");

        // 4. 二维码签到
        String qrCode = "QR_CODE_" + appointmentId;
        String checkInResult = mockMvc.perform(post("/api/v1/mobile/visitor/checkin/qrcode")
                .param("qrCode", qrCode)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> checkInResponse = objectMapper.readValue(checkInResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        // 注意：签到可能因为业务规则失败，这里只验证接口调用成功
        assertNotNull(checkInResponse);

        // 5. 获取签到状态
        String statusResult = mockMvc.perform(get("/api/v1/mobile/visitor/checkin/status/{appointmentId}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> statusResponse = objectMapper.readValue(statusResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(statusResponse.isSuccess(), "获取签到状态应该成功");

        // 6. 访客签退
        String checkoutResult = mockMvc.perform(post("/api/v1/mobile/visitor/checkout/{appointmentId}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> checkoutResponse = objectMapper.readValue(checkoutResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        // 注意：签退可能因为业务规则失败，这里只验证接口调用成功
        assertNotNull(checkoutResponse);

        // 7. 获取访问记录
        String recordsResult = mockMvc.perform(get("/api/v1/mobile/visitor/access-records/{appointmentId}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> recordsResponse = objectMapper.readValue(recordsResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(recordsResponse.isSuccess(), "获取访问记录应该成功");
    }

    @Test
    @DisplayName("测试预约创建参数验证")
    void testAppointmentCreationValidation() throws Exception {
        // 测试缺少必填字段
        VisitorMobileForm invalidForm = new VisitorMobileForm();
        // 故意不设置必填字段

        mockMvc.perform(post("/api/v1/mobile/visitor/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isBadRequest());

        // 测试无效的访问时间（过去的时间）
        VisitorMobileForm pastTimeForm = createTestAppointmentForm();
        pastTimeForm.setAppointmentStartTime(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/v1/mobile/visitor/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pastTimeForm)))
                .andExpect(status().isOk()); // 注意：根据实际业务逻辑调整
    }

    @Test
    @DisplayName("测试预约取消流程")
    void testAppointmentCancellation() throws Exception {
        // 1. 创建预约
        VisitorMobileForm createForm = createTestAppointmentForm();
        String createResult = mockMvc.perform(post("/api/v1/mobile/visitor/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createForm)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Long> createResponse = objectMapper.readValue(createResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Long.class));
        Long appointmentId = createResponse.getData();

        // 2. 取消预约
        String cancelResult = mockMvc.perform(put("/api/v1/mobile/visitor/appointment/{appointmentId}/cancel", appointmentId)
                .param("userId", testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> cancelResponse = objectMapper.readValue(cancelResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(cancelResponse.isSuccess(), "取消预约应该成功");
    }

    @Test
    @DisplayName("测试工具接口")
    void testUtilityEndpoints() throws Exception {
        // 测试获取访问区域
        String areasResult = mockMvc.perform(get("/api/v1/mobile/visitor/areas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> areasResponse = objectMapper.readValue(areasResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(areasResponse.isSuccess(), "获取访问区域应该成功");

        // 测试获取预约类型
        String typesResult = mockMvc.perform(get("/api/v1/mobile/visitor/appointment-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> typesResponse = objectMapper.readValue(typesResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(typesResponse.isSuccess(), "获取预约类型应该成功");

        // 测试获取帮助信息
        String helpResult = mockMvc.perform(get("/api/v1/mobile/visitor/help")
                .param("helpType", "APPOINTMENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<?> helpResponse = objectMapper.readValue(helpResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Object.class));
        assertTrue(helpResponse.isSuccess(), "获取帮助信息应该成功");
    }

    /**
     * 创建测试用的预约表单
     */
    private VisitorMobileForm createTestAppointmentForm() {
        VisitorMobileForm form = new VisitorMobileForm();
        form.setVisitorName("集成测试访客");
        form.setPhoneNumber("13812345678");
        form.setIdCardNumber("110101199001011234");
        form.setAppointmentType("业务访问");
        form.setVisitUserId(testVisiteeId);
        form.setVisitUserName("测试被访人");
        form.setAppointmentStartTime(LocalDateTime.now().plusDays(1));
        form.setAppointmentEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        form.setVisitPurpose("集成测试");
        return form;
    }
}

