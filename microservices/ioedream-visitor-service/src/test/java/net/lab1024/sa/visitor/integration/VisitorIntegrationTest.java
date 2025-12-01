package net.lab1024.sa.visitor.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO;
import net.lab1024.sa.visitor.domain.vo.VisitorBatchApprovalVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCheckinVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCheckoutVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCreateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorSearchVO;
import net.lab1024.sa.visitor.domain.vo.VisitorUpdateVO;

/**
 * 访客模块集成测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class VisitorIntegrationTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @Resource
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void testCompleteVisitorWorkflow() throws Exception {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 1. 创建访客
        VisitorCreateVO createVO = createTestVisitorCreateVO();

        String createResult = mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andReturn().getResponse().getContentAsString();

        ResponseDTO<Long> createResponse = objectMapper.readValue(createResult,
                objectMapper.getTypeFactory().constructParametricType(ResponseDTO.class, Long.class));
        Long visitorId = createResponse.getData();
        assertNotNull(visitorId);

        // 2. 获取访客详情
        mockMvc.perform(get("/visitor/{visitorId}", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.visitorId").value(visitorId))
                .andExpect(jsonPath("$.data.visitorName").value(createVO.getVisitorName()));

        // 3. 搜索访客
        VisitorSearchVO searchVO = new VisitorSearchVO();
        searchVO.setField(createVO.getVisitorName());

        mockMvc.perform(post("/visitor/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));

        // 4. 审批访客
        VisitorApprovalVO approvalVO = new VisitorApprovalVO();
        approvalVO.setVisitorId(visitorId);
        approvalVO.setApproved(true);
        approvalVO.setApproverId(1001L);
        approvalVO.setApprovalComment("集成测试审批");

        mockMvc.perform(post("/visitor/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. 访客签到
        VisitorCheckinVO checkinVO = new VisitorCheckinVO();
        checkinVO.setVisitorId(visitorId);
        checkinVO.setVerificationMethod("ID_CARD");
        checkinVO.setVerificationData(createVO.getIdNumber());
        checkinVO.setAreaId(1L);

        mockMvc.perform(post("/visitor/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkinVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 6. 访客签退
        VisitorCheckoutVO checkoutVO = new VisitorCheckoutVO();
        checkoutVO.setVisitorId(visitorId);
        checkoutVO.setVerificationMethod("ID_CARD");

        mockMvc.perform(post("/visitor/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkoutVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. 更新访客信息
        VisitorUpdateVO updateVO = new VisitorUpdateVO();
        updateVO.setVisitorId(visitorId);
        updateVO.setVisitorName(createVO.getVisitorName() + "-更新");
        updateVO.setEmail("updated@example.com");

        mockMvc.perform(put("/visitor/{visitorId}", visitorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. 获取统计信息
        mockMvc.perform(get("/visitor/statistics")
                .param("startTime", LocalDateTime.now().minusDays(1).toString())
                .param("endTime", LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").exists());

        // 9. 删除访客
        mockMvc.perform(delete("/visitor/{visitorId}", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testVisitorValidation() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 测试必填字段验证
        VisitorCreateVO invalidVO = new VisitorCreateVO();
        // 故意不设置必填字段

        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVO)))
                .andExpect(status().isBadRequest());

        // 测试邮箱格式验证
        VisitorCreateVO invalidEmailVO = new VisitorCreateVO();
        invalidEmailVO.setVisitorName("测试");
        invalidEmailVO.setGender(1);
        invalidEmailVO.setPhoneNumber("13812345678");
        invalidEmailVO.setIdNumber("110101199001011234");
        invalidEmailVO.setVisiteeId(1001L);
        invalidEmailVO.setVisiteeName("李四");
        invalidEmailVO.setVisitPurpose("测试");
        invalidEmailVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        invalidEmailVO.setUrgencyLevel(1);
        invalidEmailVO.setEmail("invalid-email"); // 无效邮箱格式

        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailVO)))
                .andExpect(status().isBadRequest());

        // 测试手机号格式验证
        VisitorCreateVO invalidPhoneVO = new VisitorCreateVO();
        invalidPhoneVO.setVisitorName("测试");
        invalidPhoneVO.setGender(1);
        invalidPhoneVO.setPhoneNumber("123456789"); // 无效手机号
        invalidPhoneVO.setIdNumber("110101199001011234");
        invalidPhoneVO.setVisiteeId(1001L);
        invalidPhoneVO.setVisiteeName("李四");
        invalidPhoneVO.setVisitPurpose("测试");
        invalidPhoneVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        invalidPhoneVO.setUrgencyLevel(1);

        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPhoneVO)))
                .andExpect(status().isBadRequest());

        // 测试身份证号格式验证
        VisitorCreateVO invalidIdVO = new VisitorCreateVO();
        invalidIdVO.setVisitorName("测试");
        invalidIdVO.setGender(1);
        invalidIdVO.setPhoneNumber("13812345678");
        invalidIdVO.setIdNumber("invalid-id"); // 无效身份证号
        invalidIdVO.setVisiteeId(1001L);
        invalidIdVO.setVisiteeName("李四");
        invalidIdVO.setVisitPurpose("测试");
        invalidIdVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        invalidIdVO.setUrgencyLevel(1);

        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidIdVO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testErrorHandling() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 测试不存在的访客ID
        mockMvc.perform(get("/visitor/{visitorId}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        // 测试无效的参数
        mockMvc.perform(get("/visitor/statistics")
                .param("startTime", "invalid-date")
                .param("endTime", "invalid-date"))
                .andExpect(status().isBadRequest());

        // 测试重复的身份证号
        VisitorCreateVO createVO1 = createTestVisitorCreateVO();
        VisitorCreateVO createVO2 = createTestVisitorCreateVO();
        createVO2.setVisitorName("另一个访客");
        createVO2.setPhoneNumber("13812345679");

        // 第一个访客创建成功
        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 第二个访客使用相同身份证号应该失败
        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testAPIEndpoints() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 测试获取正在访问的访客
        mockMvc.perform(get("/visitor/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 测试获取即将到期的访客
        mockMvc.perform(get("/visitor/expiring")
                .param("hours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 测试导出访客数据
        VisitorSearchVO exportVO = new VisitorSearchVO();
        mockMvc.perform(post("/visitor/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exportVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());

        // 测试批量审批
        VisitorBatchApprovalVO batchApprovalVO = new VisitorBatchApprovalVO();
        batchApprovalVO.setVisitorIds(java.util.Arrays.asList(1L, 2L));
        batchApprovalVO.setApproved(true);
        batchApprovalVO.setApproverId(1001L);
        batchApprovalVO.setApprovalComment("批量审批通过");

        mockMvc.perform(post("/visitor/batch-approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchApprovalVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 创建测试用的访客创建VO
     */
    private VisitorCreateVO createTestVisitorCreateVO() {
        VisitorCreateVO createVO = new VisitorCreateVO();
        createVO.setVisitorName("集成测试访客");
        createVO.setGender(1);
        createVO.setPhoneNumber("13812345678");
        createVO.setEmail("integration@test.com");
        createVO.setIdNumber("110101199001011234");
        createVO.setCompany("测试公司");
        createVO.setVisiteeId(1001L);
        createVO.setVisiteeName("测试被访人");
        createVO.setVisiteeDepartment("测试部门");
        createVO.setVisitPurpose("集成测试访问");
        createVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        createVO.setExpectedDepartureTime(LocalDateTime.now().plusDays(1).plusHours(8));
        createVO.setUrgencyLevel(1);
        createVO.setNotes("集成测试备注");
        return createVO;
    }
}
