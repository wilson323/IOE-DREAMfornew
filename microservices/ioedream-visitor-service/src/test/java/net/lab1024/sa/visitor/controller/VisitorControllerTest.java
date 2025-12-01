package net.lab1024.sa.visitor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.visitor.domain.query.VisitorQueryVO;
import net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCheckinVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCreateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorDetailVO;
import net.lab1024.sa.visitor.domain.vo.VisitorStatisticsVO;
import net.lab1024.sa.visitor.domain.vo.VisitorUpdateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.visitor.service.VisitorService;

/**
 * 访客控制器测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
class VisitorControllerTest {

    @Mock
    private VisitorService visitorService;

    @InjectMocks
    private VisitorController visitorController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(visitorController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreateVisitor_Success() throws Exception {
        // Given
        VisitorCreateVO createVO = new VisitorCreateVO();
        createVO.setVisitorName("张三");
        createVO.setGender(1);
        createVO.setPhoneNumber("13812345678");
        createVO.setEmail("zhangsan@example.com");
        createVO.setIdNumber("110101199001011234");
        createVO.setVisiteeId(1001L);
        createVO.setVisiteeName("李四");
        createVO.setVisitPurpose("商务洽谈");
        createVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        createVO.setUrgencyLevel(1);

        when(visitorService.createVisitor(any(VisitorCreateVO.class)))
                .thenReturn(ResponseDTO.ok(123L));

        // When & Then
        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(123));

        verify(visitorService, times(1)).createVisitor(any(VisitorCreateVO.class));
    }

    @Test
    void testUpdateVisitor_Success() throws Exception {
        // Given
        Long visitorId = 123L;
        VisitorUpdateVO updateVO = new VisitorUpdateVO();
        updateVO.setVisitorId(visitorId);
        updateVO.setVisitorName("张三更新");
        updateVO.setVisitPurpose("更新后的访问事由");

        when(visitorService.updateVisitor(eq(visitorId), any(VisitorUpdateVO.class)))
                .thenReturn(ResponseDTO.<Void>ok());

        // When & Then
        mockMvc.perform(put("/visitor/{visitorId}", visitorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(visitorService, times(1)).updateVisitor(eq(visitorId), any(VisitorUpdateVO.class));
    }

    @Test
    void testGetVisitorDetail_Success() throws Exception {
        // Given
        Long visitorId = 123L;
        VisitorDetailVO detailVO = new VisitorDetailVO();
        detailVO.setVisitorId(visitorId);
        detailVO.setVisitorName("张三");
        detailVO.setPhone("13812345678");
        detailVO.setStatus("1");
        detailVO.setStatusDesc("已通过");

        when(visitorService.getVisitorDetail(visitorId))
                .thenReturn(ResponseDTO.ok(detailVO));

        // When & Then
        mockMvc.perform(get("/visitor/{visitorId}", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.visitorId").value(123))
                .andExpect(jsonPath("$.data.visitorName").value("张三"))
                .andExpect(jsonPath("$.data.statusDesc").value("已通过"));

        verify(visitorService, times(1)).getVisitorDetail(visitorId);
    }

    @Test
    void testDeleteVisitor_Success() throws Exception {
        // Given
        Long visitorId = 123L;

        when(visitorService.deleteVisitor(visitorId))
                .thenReturn(ResponseDTO.<Void>ok());

        // When & Then
        mockMvc.perform(delete("/visitor/{visitorId}", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(visitorService, times(1)).deleteVisitor(visitorId);
    }

    @Test
    void testQueryVisitors_Success() throws Exception {
        // Given
        VisitorQueryVO queryVO = new VisitorQueryVO();
        queryVO.setPageNum(1);
        queryVO.setPageSize(10);
        queryVO.setVisitorName("张三");

        List<VisitorVO> visitors = Arrays.asList(
                createTestVisitor(1L, "张三", "13812345678"),
                createTestVisitor(2L, "张四", "13812345679"));

        PageResult<VisitorVO> pageResult = new PageResult<>();
        pageResult.setList(visitors);
        pageResult.setTotal(2L);
        pageResult.setPageNum(1L);
        pageResult.setPageSize(10L);
        pageResult.setPages(1L);

        when(visitorService.queryVisitors(any(VisitorQueryVO.class)))
                .thenReturn(ResponseDTO.ok(pageResult));

        // When & Then
        mockMvc.perform(post("/visitor/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(queryVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        verify(visitorService, times(1)).queryVisitors(any(VisitorQueryVO.class));
    }

    @Test
    void testApproveVisitor_Success() throws Exception {
        // Given
        Long visitorId = 123L;
        VisitorApprovalVO approvalVO = new VisitorApprovalVO();
        approvalVO.setVisitorId(visitorId);
        approvalVO.setApproved(true);
        approvalVO.setApproverId(1001L);
        approvalVO.setApprovalComment("审批通过");

        when(visitorService.approveVisitor(any(VisitorApprovalVO.class)))
                .thenReturn(ResponseDTO.<Void>ok());

        // When & Then
        mockMvc.perform(post("/visitor/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(visitorService, times(1)).approveVisitor(any(VisitorApprovalVO.class));
    }

    @Test
    void testVisitorCheckin_Success() throws Exception {
        // Given
        Long visitorId = 123L;
        VisitorCheckinVO checkinVO = new VisitorCheckinVO();
        checkinVO.setVisitorId(visitorId);
        checkinVO.setVerificationMethod("FACE_RECOGNITION");
        checkinVO.setVerificationData("face_data_123");

        when(visitorService.visitorCheckin(any(VisitorCheckinVO.class)))
                .thenReturn(ResponseDTO.<Void>ok());

        // When & Then
        mockMvc.perform(post("/visitor/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkinVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        verify(visitorService, times(1)).visitorCheckin(any(VisitorCheckinVO.class));
    }

    @Test
    void testGetVisitorStatistics_Success() throws Exception {
        // Given
        VisitorStatisticsVO statistics = new VisitorStatisticsVO();
        statistics.setTotalCount(100);
        statistics.setPendingCount(10);
        statistics.setApprovedCount(80);
        statistics.setRejectedCount(5);
        statistics.setCompletedCount(5);

        when(visitorService.getVisitorStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(ResponseDTO.ok(statistics));

        // When & Then
        mockMvc.perform(get("/visitor/statistics")
                .param("startTime", "2025-11-01T00:00:00")
                .param("endTime", "2025-11-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(100))
                .andExpect(jsonPath("$.data.pendingCount").value(10))
                .andExpect(jsonPath("$.data.approvedCount").value(80));

        verify(visitorService, times(1))
                .getVisitorStatistics(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCreateVisitor_ValidationError() throws Exception {
        // Given - 缺少必填字段
        VisitorCreateVO createVO = new VisitorCreateVO();
        createVO.setVisitorName(""); // 空字符串

        // When & Then
        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO)))
                .andExpect(status().isBadRequest());

        verify(visitorService, never()).createVisitor(any(VisitorCreateVO.class));
    }

    @Test
    void testCreateVisitor_ServiceError() throws Exception {
        // Given
        VisitorCreateVO createVO = new VisitorCreateVO();
        createVO.setVisitorName("张三");
        createVO.setGender(1);
        createVO.setPhoneNumber("13812345678");
        createVO.setIdNumber("110101199001011234");
        createVO.setVisiteeId(1001L);
        createVO.setVisiteeName("李四");
        createVO.setVisitPurpose("商务洽谈");
        createVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        createVO.setUrgencyLevel(1);

        when(visitorService.createVisitor(any(VisitorCreateVO.class)))
                .thenReturn(ResponseDTO.error("该身份证号已存在"));

        // When & Then
        mockMvc.perform(post("/visitor/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createVO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.msg").value("该身份证号已存在"));

        verify(visitorService, times(1)).createVisitor(any(VisitorCreateVO.class));
    }

    /**
     * 创建测试用的访客VO对象
     */
    private VisitorVO createTestVisitor(Long visitorId, String visitorName, String phoneNumber) {
        VisitorVO visitor = new VisitorVO();
        visitor.setVisitorId(visitorId);
        visitor.setVisitorName(visitorName);
        visitor.setPhone(phoneNumber);
        visitor.setStatus("1");
        visitor.setStatusDesc("已通过");
        visitor.setUrgencyLevelDesc("普通");
        return visitor;
    }
}
