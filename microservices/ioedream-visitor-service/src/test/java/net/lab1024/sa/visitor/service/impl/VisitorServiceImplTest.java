package net.lab1024.sa.visitor.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.SmartException;
import net.lab1024.sa.visitor.dao.VisitRecordDao;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.visitor.dao.VisitorDao;
import net.lab1024.sa.visitor.domain.entity.VisitRecordEntity;
import net.lab1024.sa.visitor.domain.entity.VisitorAppointmentEntity;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.enums.VisitorStatusEnum;
import net.lab1024.sa.visitor.domain.query.VisitorQueryVO;
import net.lab1024.sa.visitor.domain.vo.VisitorApprovalVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCheckinVO;
import net.lab1024.sa.visitor.domain.vo.VisitorCreateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorDetailVO;
import net.lab1024.sa.visitor.domain.vo.VisitorSearchVO;
import net.lab1024.sa.visitor.domain.vo.VisitorStatisticsVO;
import net.lab1024.sa.visitor.domain.vo.VisitorUpdateVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;

/**
 * 访客服务实现测试
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
class VisitorServiceImplTest {

    @Mock
    private VisitorDao visitorDao;

    @Mock
    private VisitorAppointmentDao appointmentDao;

    @Mock
    private VisitRecordDao recordDao;

    @InjectMocks
    private VisitorServiceImpl visitorService;

    private VisitorEntity testVisitor;
    private VisitorCreateVO testCreateVO;
    private VisitorUpdateVO testUpdateVO;

    @BeforeEach
    void setUp() {
        testVisitor = createTestVisitor();
        testCreateVO = createTestCreateVO();
        testUpdateVO = createTestUpdateVO();
    }

    @Test
    void testCreateVisitor_Success() {
        // Given
        when(visitorDao.selectByIdNumber(anyString())).thenReturn(null);
        when(visitorDao.insert(any(VisitorEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Long> result = visitorService.createVisitor(testCreateVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());
        assertNotNull(result.getData());

        ArgumentCaptor<VisitorEntity> visitorCaptor = ArgumentCaptor.forClass(VisitorEntity.class);
        verify(visitorDao, times(1)).insert(visitorCaptor.capture());

        VisitorEntity savedVisitor = visitorCaptor.getValue();
        assertEquals(testCreateVO.getVisitorName(), savedVisitor.getVisitorName());
        assertEquals(testCreateVO.getIdNumber(), savedVisitor.getIdNumber());
        assertEquals(VisitorStatusEnum.PENDING.getCode(), savedVisitor.getStatus());
        assertNotNull(savedVisitor.getCreateTime());
        assertNotNull(savedVisitor.getUpdateTime());
        assertFalse(savedVisitor.getDeletedFlag() != null && savedVisitor.getDeletedFlag() == 1);
    }

    @Test
    void testCreateVisitor_DuplicateIdNumber() {
        // Given
        VisitorEntity existingVisitor = new VisitorEntity();
        // VisitorEntity继承自BaseEntity，id字段由BaseEntity管理，不能直接setId
        // 使用反射或通过其他方式设置id，或者直接使用mock对象
        when(visitorDao.selectByIdNumber(testCreateVO.getIdNumber())).thenReturn(existingVisitor);

        // When
        SmartException exception = assertThrows(SmartException.class,
                () -> visitorService.createVisitor(testCreateVO));

        // Then
        assertEquals("该身份证号已注册访客信息", exception.getMessage());
        verify(visitorDao, never()).insert(any(VisitorEntity.class));
    }

    @Test
    void testCreateVisitor_DatabaseError() {
        // Given
        when(visitorDao.selectByIdNumber(anyString())).thenReturn(null);
        when(visitorDao.insert(any(VisitorEntity.class))).thenReturn(0);

        // When
        SmartException exception = assertThrows(SmartException.class,
                () -> visitorService.createVisitor(testCreateVO));

        // Then
        assertEquals("创建访客失败", exception.getMessage());
    }

    @Test
    void testUpdateVisitor_Success() {
        // Given
        Long visitorId = 1L;
        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);
        when(visitorDao.updateById(any(VisitorEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorService.updateVisitor(visitorId, testUpdateVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());

        ArgumentCaptor<VisitorEntity> visitorCaptor = ArgumentCaptor.forClass(VisitorEntity.class);
        verify(visitorDao, times(1)).updateById(visitorCaptor.capture());

        VisitorEntity updatedVisitor = visitorCaptor.getValue();
        assertEquals(testUpdateVO.getVisitorName(), updatedVisitor.getVisitorName());
        assertEquals(testUpdateVO.getEmail(), updatedVisitor.getEmail());
        assertNotNull(updatedVisitor.getUpdateTime());
        assertEquals(2, updatedVisitor.getVersion()); // 版本号+1
    }

    @Test
    void testUpdateVisitor_NotFound() {
        // Given
        Long visitorId = 999L;
        when(visitorDao.selectById(visitorId)).thenReturn(null);

        // When
        SmartException exception = assertThrows(SmartException.class,
                () -> visitorService.updateVisitor(visitorId, testUpdateVO));

        // Then
        assertEquals("访客不存在", exception.getMessage());
        verify(visitorDao, never()).updateById(any(VisitorEntity.class));
    }

    @Test
    void testDeleteVisitor_Success() {
        // Given
        Long visitorId = 1L;
        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);
        when(visitorDao.updateById(any(VisitorEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorService.deleteVisitor(visitorId);

        // Then
        assertTrue(result.getOk() != null && result.getOk());

        ArgumentCaptor<VisitorEntity> visitorCaptor = ArgumentCaptor.forClass(VisitorEntity.class);
        verify(visitorDao, times(1)).updateById(visitorCaptor.capture());

        VisitorEntity deletedVisitor = visitorCaptor.getValue();
        assertNotNull(deletedVisitor.getUpdateTime());
    }

    @Test
    void testGetVisitorDetail_Success() {
        // Given
        Long visitorId = 1L;
        VisitorAppointmentEntity appointment = new VisitorAppointmentEntity();
        appointment.setAppointmentId(1L);

        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);
        when(appointmentDao.selectByVisitorId(visitorId)).thenReturn(Arrays.asList(appointment));
        when(recordDao.selectByVisitorId(visitorId)).thenReturn(Collections.emptyList());

        // When
        ResponseDTO<VisitorDetailVO> result = visitorService.getVisitorDetail(visitorId);

        // Then
        assertTrue(result.getOk() != null && result.getOk());
        assertNotNull(result.getData());
        assertEquals(testVisitor.getVisitorId(), result.getData().getVisitorId());
        assertEquals(testVisitor.getVisitorName(), result.getData().getVisitorName());
        assertEquals("已通过", result.getData().getStatusDesc());
    }

    @Test
    void testSearchVisitors_Success() {
        // Given
        VisitorSearchVO searchVO = new VisitorSearchVO();
        searchVO.setField("张");

        List<VisitorEntity> visitors = Arrays.asList(testVisitor);
        when(visitorDao.selectVisitorList(searchVO)).thenReturn(visitors);

        // When
        ResponseDTO<List<VisitorVO>> result = visitorService.searchVisitors(searchVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(testVisitor.getVisitorName(), result.getData().get(0).getVisitorName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testQueryVisitors_Success() {
        // Given
        VisitorQueryVO queryVO = new VisitorQueryVO();
        queryVO.setPageNum(1);
        queryVO.setPageSize(10);
        queryVO.setVisitorName("张");

        IPage<VisitorEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testVisitor));
        page.setTotal(1L);
        page.setPages(1);

        when(visitorDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        ResponseDTO<PageResult<VisitorVO>> result = visitorService.queryVisitors(queryVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getTotal());
        assertEquals(1, result.getData().getList().size());
    }

    @Test
    void testApproveVisitor_Success() {
        // Given
        Long visitorId = 1L;
        VisitorApprovalVO approvalVO = new VisitorApprovalVO();
        approvalVO.setVisitorId(visitorId);
        approvalVO.setApproved(true);
        approvalVO.setApproverId(1001L);
        approvalVO.setApprovalComment("审批通过");

        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);
        when(visitorDao.updateById(any(VisitorEntity.class))).thenReturn(1);

        VisitorAppointmentEntity appointment = new VisitorAppointmentEntity();
        appointment.setAppointmentId(1L);
        appointment.setStatusCode(VisitorStatusEnum.PENDING.getCode().toString());
        when(appointmentDao.selectByVisitorId(visitorId)).thenReturn(Arrays.asList(appointment));
        when(appointmentDao.updateById(any(VisitorAppointmentEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorService.approveVisitor(approvalVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());

        // 验证访客状态更新
        ArgumentCaptor<VisitorEntity> visitorCaptor = ArgumentCaptor.forClass(VisitorEntity.class);
        verify(visitorDao, times(1)).updateById(visitorCaptor.capture());
        assertEquals(VisitorStatusEnum.APPROVED.getCode(), visitorCaptor.getValue().getStatus());

        // 验证预约状态更新
        ArgumentCaptor<VisitorAppointmentEntity> appointmentCaptor = ArgumentCaptor
                .forClass(VisitorAppointmentEntity.class);
        verify(appointmentDao, times(1)).updateById(appointmentCaptor.capture());
        assertEquals(VisitorStatusEnum.APPROVED.getCode().toString(), appointmentCaptor.getValue().getStatusCode());
        assertEquals(1001L, appointmentCaptor.getValue().getApproverId());
    }

    @Test
    void testVisitorCheckin_Success() {
        // Given
        Long visitorId = 1L;
        VisitorCheckinVO checkinVO = new VisitorCheckinVO();
        checkinVO.setVisitorId(visitorId);
        checkinVO.setVerificationMethod("FACE_RECOGNITION");
        checkinVO.setVerificationData("face_data");
        checkinVO.setAreaId(1L);

        testVisitor.setStatus(VisitorStatusEnum.APPROVED.getCode());
        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);
        when(recordDao.insert(any(VisitRecordEntity.class))).thenReturn(1);
        when(visitorDao.updateById(any(VisitorEntity.class))).thenReturn(1);

        // When
        ResponseDTO<Void> result = visitorService.visitorCheckin(checkinVO);

        // Then
        assertTrue(result.getOk() != null && result.getOk());

        // 验证访问记录创建
        verify(recordDao, times(1)).insert(any(VisitRecordEntity.class));

        // 验证访客状态更新
        ArgumentCaptor<VisitorEntity> visitorCaptor = ArgumentCaptor.forClass(VisitorEntity.class);
        verify(visitorDao, times(1)).updateById(visitorCaptor.capture());
        assertEquals(VisitorStatusEnum.VISITING.getCode(), visitorCaptor.getValue().getStatus());
    }

    @Test
    void testVisitorCheckin_InvalidStatus() {
        // Given
        Long visitorId = 1L;
        VisitorCheckinVO checkinVO = new VisitorCheckinVO();
        checkinVO.setVisitorId(visitorId);

        testVisitor.setStatus(VisitorStatusEnum.PENDING.getCode());
        when(visitorDao.selectById(visitorId)).thenReturn(testVisitor);

        // When
        SmartException exception = assertThrows(SmartException.class,
                () -> visitorService.visitorCheckin(checkinVO));

        // Then
        assertEquals("访客状态异常，无法签到", exception.getMessage());
        verify(recordDao, never()).insert(any(VisitRecordEntity.class));
    }

    @Test
    void testGetVisitorStatistics_Success() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        when(visitorDao.selectVisitorByTimeRange(startTime, endTime))
                .thenReturn(Arrays.asList(testVisitor));
        when(visitorDao.countByStatus(VisitorStatusEnum.PENDING.getCode())).thenReturn(5L);
        when(visitorDao.countByStatus(VisitorStatusEnum.APPROVED.getCode())).thenReturn(20L);
        when(visitorDao.countByStatus(VisitorStatusEnum.REJECTED.getCode())).thenReturn(3L);
        when(visitorDao.countByStatus(VisitorStatusEnum.COMPLETED.getCode())).thenReturn(2L);

        // When
        ResponseDTO<VisitorStatisticsVO> result = visitorService.getVisitorStatistics(startTime, endTime);

        // Then
        assertTrue(result.getOk() != null && result.getOk());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotalCount());
        assertEquals(5, result.getData().getPendingCount());
        assertEquals(20, result.getData().getApprovedCount());
        assertEquals(3, result.getData().getRejectedCount());
        assertEquals(2, result.getData().getCompletedCount());
    }

    /**
     * 创建测试用的访客实体
     */
    private VisitorEntity createTestVisitor() {
        VisitorEntity visitor = new VisitorEntity();
        visitor.setVisitorId(1L);
        visitor.setVisitorName("张三");
        visitor.setPhoneNumber("13812345678");
        visitor.setEmail("zhangsan@example.com");
        visitor.setIdNumber("110101199001011234");
        visitor.setCompany("测试公司");
        visitor.setVisitUserId(1001L);
        visitor.setVisitUserName("李四");
        visitor.setVisitUserDept("技术部");
        visitor.setVisitPurpose("商务洽谈");
        visitor.setAppointmentStartTime(LocalDateTime.now().plusDays(1));
        visitor.setAppointmentEndTime(LocalDateTime.now().plusDays(1).plusHours(8));
        visitor.setUrgencyLevel("1");
        visitor.setStatus(VisitorStatusEnum.APPROVED.getCode());
        visitor.setVersion(1);
        visitor.setCreateTime(LocalDateTime.now().minusDays(1));
        visitor.setUpdateTime(LocalDateTime.now().minusDays(1));
        return visitor;
    }

    /**
     * 创建测试用的访客创建VO
     */
    private VisitorCreateVO createTestCreateVO() {
        VisitorCreateVO createVO = new VisitorCreateVO();
        createVO.setVisitorName("测试访客");
        createVO.setGender(1);
        createVO.setPhoneNumber("13812345678");
        createVO.setEmail("test@example.com");
        createVO.setIdNumber("110101199001011234");
        createVO.setCompany("测试公司");
        createVO.setVisiteeId(1001L);
        createVO.setVisiteeName("李四");
        createVO.setVisiteeDepartment("技术部");
        createVO.setVisitPurpose("测试访问");
        createVO.setExpectedArrivalTime(LocalDateTime.now().plusDays(1));
        createVO.setExpectedDepartureTime(LocalDateTime.now().plusDays(1).plusHours(8));
        createVO.setUrgencyLevel(1);
        createVO.setNotes("测试备注");
        return createVO;
    }

    /**
     * 创建测试用的访客更新VO
     */
    private VisitorUpdateVO createTestUpdateVO() {
        VisitorUpdateVO updateVO = new VisitorUpdateVO();
        updateVO.setVisitorName("更新的访客");
        updateVO.setEmail("updated@example.com");
        updateVO.setVisitPurpose("更新的访问事由");
        updateVO.setNotes("更新的备注");
        return updateVO;
    }
}
