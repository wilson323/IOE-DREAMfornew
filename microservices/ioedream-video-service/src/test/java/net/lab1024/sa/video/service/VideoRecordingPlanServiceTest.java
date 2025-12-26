package net.lab1024.sa.video.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.session.Configuration;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.VideoRecordingPlanDao;
import net.lab1024.sa.common.entity.video.VideoRecordingPlanEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanAddForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlanVO;
import net.lab1024.sa.video.service.impl.VideoRecordingPlanServiceImpl;

/**
 * 视频录像计划服务单元测试
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("视频录像计划服务测试")
class VideoRecordingPlanServiceTest {

    @Mock
    private VideoRecordingPlanDao videoRecordingPlanDao;

    @InjectMocks
    private VideoRecordingPlanServiceImpl videoRecordingPlanService;

    private VideoRecordingPlanAddForm addForm;
    private VideoRecordingPlanUpdateForm updateForm;
    private VideoRecordingPlanEntity mockPlan;

    @BeforeEach
    void setUp() {
        // 创建新增表单
        addForm = new VideoRecordingPlanAddForm();
        addForm.setPlanName("主入口全天录像");
        addForm.setPlanType(1);
        addForm.setDeviceId("CAM001");
        addForm.setChannelId(1);
        addForm.setRecordingType(1);
        addForm.setQuality(3);
        addForm.setStartTime(LocalDateTime.of(2025, 1, 30, 0, 0, 0));
        addForm.setEndTime(LocalDateTime.of(2025, 1, 30, 23, 59, 59));
        addForm.setWeekdays("1,2,3,4,5");
        addForm.setEnabled(true);
        addForm.setPriority(1);
        addForm.setPreRecordSeconds(5);
        addForm.setPostRecordSeconds(10);
        addForm.setEventTypes(Arrays.asList("MOTION_DETECTED", "FACE_DETECTED"));
        addForm.setStorageLocation("/recordings/cam001/");
        addForm.setMaxDurationMinutes(480);
        addForm.setLoopRecording(true);
        addForm.setRemarks("主入口全天录像计划");

        // 创建更新表单
        updateForm = new VideoRecordingPlanUpdateForm();
        updateForm.setPlanId(10001L);
        updateForm.setPlanName("主入口全天录像（更新）");
        updateForm.setQuality(4);
        updateForm.setEnabled(false);

        // 创建模拟实体
        mockPlan = new VideoRecordingPlanEntity();
        mockPlan.setPlanId(10001L);
        mockPlan.setPlanName("主入口全天录像");
        mockPlan.setPlanType(1);
        mockPlan.setDeviceId("CAM001");
        mockPlan.setChannelId(1);
        mockPlan.setRecordingType(1);
        mockPlan.setQuality(3);
        mockPlan.setEnabled(true);
        mockPlan.setCreateTime(LocalDateTime.now());
        mockPlan.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建录像计划 - 成功")
    void testCreatePlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(videoRecordingPlanDao.insert(any(VideoRecordingPlanEntity.class))).thenAnswer(invocation -> {
            VideoRecordingPlanEntity entity = invocation.getArgument(0);
            entity.setPlanId(10001L);
            return 1;
        });

        // When
        Long planId = videoRecordingPlanService.createPlan(addForm);

        // Then
        assertNotNull(planId);
        assertEquals(10001L, planId);
        verify(videoRecordingPlanDao).insert(any(VideoRecordingPlanEntity.class));
    }

    @Test
    @DisplayName("创建录像计划 - 计划名称重复")
    void testCreatePlan_DuplicateName() {
        // Given
        when(videoRecordingPlanDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingPlanService.createPlan(addForm);
        });

        assertEquals("PLAN_NAME_EXISTS", exception.getCode());
        verify(videoRecordingPlanDao, never()).insert(any(VideoRecordingPlanEntity.class));
    }

    @Test
    @DisplayName("更新录像计划 - 成功")
    void testUpdatePlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingPlanDao.updateById(any(VideoRecordingPlanEntity.class))).thenReturn(1);

        // When
        Integer rows = videoRecordingPlanService.updatePlan(updateForm);

        // Then
        assertEquals(1, rows);
        verify(videoRecordingPlanDao).updateById(any(VideoRecordingPlanEntity.class));
    }

    @Test
    @DisplayName("更新录像计划 - 计划不存在")
    void testUpdatePlan_NotFound() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoRecordingPlanService.updatePlan(updateForm);
        });

        assertEquals("PLAN_NOT_FOUND", exception.getCode());
        verify(videoRecordingPlanDao, never()).updateById(any(VideoRecordingPlanEntity.class));
    }

    @Test
    @DisplayName("删除录像计划 - 成功")
    void testDeletePlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingPlanDao.deleteById(10001L)).thenReturn(1);

        // When
        Integer rows = videoRecordingPlanService.deletePlan(10001L);

        // Then
        assertEquals(1, rows);
        verify(videoRecordingPlanDao).deleteById(10001L);
    }

    @Test
    @Disabled("需要MyBatis完整环境，单元测试中lambda wrapper初始化复杂")
    @DisplayName("启用/禁用录像计划 - 成功")
    void testEnablePlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingPlanDao.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When
        Integer rows = videoRecordingPlanService.enablePlan(10001L, false);

        // Then
        assertEquals(1, rows);
        verify(videoRecordingPlanDao).update(any(), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("获取录像计划详情 - 成功")
    void testGetPlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);

        // When
        VideoRecordingPlanVO planVO = videoRecordingPlanService.getPlan(10001L);

        // Then
        assertNotNull(planVO);
        assertEquals(10001L, planVO.getPlanId());
        assertEquals("主入口全天录像", planVO.getPlanName());
        assertEquals("SCHEDULE", planVO.getPlanType());
        assertEquals("定时录像", planVO.getPlanTypeName());
        assertEquals("HIGH", planVO.getQuality());
        assertEquals("高质量", planVO.getQualityName());
    }

    @Test
    @DisplayName("分页查询录像计划 - 成功")
    void testQueryPlans_Success() {
        // Given
        VideoRecordingPlanQueryForm queryForm = new VideoRecordingPlanQueryForm();
        queryForm.setPageNum(1);
        queryForm.setPageSize(20);
        queryForm.setPlanName("主入口");
        queryForm.setEnabled(true);

        Page<VideoRecordingPlanEntity> page = new Page<>(1, 20);
        page.setRecords(Arrays.asList(mockPlan));
        page.setTotal(1);

        when(videoRecordingPlanDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        PageResult<VideoRecordingPlanVO> pageResult = videoRecordingPlanService.queryPlans(queryForm);

        // Then
        assertNotNull(pageResult);
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertFalse(pageResult.getList().isEmpty());
    }

    @Test
    @DisplayName("获取设备的启用录像计划 - 成功")
    void testGetEnabledPlansByDevice_Success() {
        // Given
        when(videoRecordingPlanDao.selectEnabledPlansByDevice("CAM001"))
                .thenReturn(Arrays.asList(mockPlan));

        // When
        List<VideoRecordingPlanVO> plans = videoRecordingPlanService.getEnabledPlansByDevice("CAM001");

        // Then
        assertNotNull(plans);
        assertEquals(1, plans.size());
        assertEquals("主入口全天录像", plans.get(0).getPlanName());
    }

    @Test
    @DisplayName("检查设备是否有启用计划 - 有计划")
    void testHasEnabledPlan_True() {
        // Given
        when(videoRecordingPlanDao.existsEnabledPlan("CAM001")).thenReturn(true);

        // When
        Boolean hasEnabled = videoRecordingPlanService.hasEnabledPlan("CAM001");

        // Then
        assertTrue(hasEnabled);
    }

    @Test
    @DisplayName("检查设备是否有启用计划 - 无计划")
    void testHasEnabledPlan_False() {
        // Given
        when(videoRecordingPlanDao.existsEnabledPlan("CAM002")).thenReturn(false);

        // When
        Boolean hasEnabled = videoRecordingPlanService.hasEnabledPlan("CAM002");

        // Then
        assertFalse(hasEnabled);
    }

    @Test
    @DisplayName("复制录像计划 - 成功")
    void testCopyPlan_Success() {
        // Given
        when(videoRecordingPlanDao.selectById(10001L)).thenReturn(mockPlan);
        when(videoRecordingPlanDao.insert(any(VideoRecordingPlanEntity.class))).thenAnswer(invocation -> {
            VideoRecordingPlanEntity entity = invocation.getArgument(0);
            entity.setPlanId(10002L);
            return 1;
        });

        // When
        Long newPlanId = videoRecordingPlanService.copyPlan(10001L, "主入口全天录像（副本）");

        // Then
        assertNotNull(newPlanId);
        assertEquals(10002L, newPlanId);
        verify(videoRecordingPlanDao).insert(any(VideoRecordingPlanEntity.class));
    }

    @Test
    @Disabled("需要MyBatis完整环境，单元测试中lambda wrapper初始化复杂")
    @DisplayName("批量启用/禁用录像计划 - 成功")
    void testBatchEnablePlans_Success() {
        // Given
        List<Long> planIds = Arrays.asList(10001L, 10002L, 10003L);
        when(videoRecordingPlanDao.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(3);

        // When
        Integer rows = videoRecordingPlanService.batchEnablePlans(planIds, false);

        // Then
        assertEquals(3, rows);
        verify(videoRecordingPlanDao).update(any(), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("批量删除录像计划 - 成功")
    void testBatchDeletePlans_Success() {
        // Given
        List<Long> planIds = Arrays.asList(10001L, 10002L, 10003L);
        when(videoRecordingPlanDao.deleteBatchIds(planIds)).thenReturn(3);

        // When
        Integer rows = videoRecordingPlanService.batchDeletePlans(planIds);

        // Then
        assertEquals(3, rows);
        verify(videoRecordingPlanDao).deleteBatchIds(planIds);
    }

    @Test
    @DisplayName("批量启用/禁用录像计划 - 空列表")
    void testBatchEnablePlans_EmptyList() {
        // Given
        List<Long> planIds = Arrays.asList();

        // When
        Integer rows = videoRecordingPlanService.batchEnablePlans(planIds, true);

        // Then
        assertEquals(0, rows);
        verify(videoRecordingPlanDao, never()).update(any(), any(LambdaUpdateWrapper.class));
    }
}
