package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanAddForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlanVO;

/**
 * 视频录像计划服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
public interface VideoRecordingPlanService {

    /**
     * 创建录像计划
     *
     * @param addForm 新增表单
     * @return 计划ID
     */
    Long createPlan(VideoRecordingPlanAddForm addForm);

    /**
     * 更新录像计划
     *
     * @param updateForm 更新表单
     * @return 影响行数
     */
    Integer updatePlan(VideoRecordingPlanUpdateForm updateForm);

    /**
     * 删除录像计划
     *
     * @param planId 计划ID
     * @return 影响行数
     */
    Integer deletePlan(Long planId);

    /**
     * 启用/禁用录像计划
     *
     * @param planId 计划ID
     * @param enabled 是否启用
     * @return 影响行数
     */
    Integer enablePlan(Long planId, Boolean enabled);

    /**
     * 获取录像计划详情
     *
     * @param planId 计划ID
     * @return 计划详情
     */
    VideoRecordingPlanVO getPlan(Long planId);

    /**
     * 分页查询录像计划
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<VideoRecordingPlanVO> queryPlans(VideoRecordingPlanQueryForm queryForm);

    /**
     * 获取设备的所有启用的录像计划
     *
     * @param deviceId 设备ID
     * @return 计划列表
     */
    java.util.List<VideoRecordingPlanVO> getEnabledPlansByDevice(String deviceId);

    /**
     * 检查设备是否存在启用的录像计划
     *
     * @param deviceId 设备ID
     * @return 是否存在
     */
    Boolean hasEnabledPlan(String deviceId);

    /**
     * 复制录像计划
     *
     * @param planId 原计划ID
     * @param newPlanName 新计划名称
     * @return 新计划ID
     */
    Long copyPlan(Long planId, String newPlanName);

    /**
     * 批量启用/禁用录像计划
     *
     * @param planIds 计划ID列表
     * @param enabled 是否启用
     * @return 影响行数
     */
    Integer batchEnablePlans(java.util.List<Long> planIds, Boolean enabled);

    /**
     * 批量删除录像计划
     *
     * @param planIds 计划ID列表
     * @return 影响行数
     */
    Integer batchDeletePlans(java.util.List<Long> planIds);
}
