package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskQueryForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeDeviceVO;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.common.domain.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 固件升级服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface FirmwareUpgradeService {

    // ==================== 任务管理 ====================

    /**
     * 创建固件升级任务
     *
     * @param taskForm   任务表单
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 任务ID
     */
    Long createUpgradeTask(FirmwareUpgradeTaskForm taskForm, Long operatorId, String operatorName);

    /**
     * 启动升级任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    Boolean startUpgradeTask(Long taskId);

    /**
     * 暂停升级任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    Boolean pauseUpgradeTask(Long taskId);

    /**
     * 恢复升级任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    Boolean resumeUpgradeTask(Long taskId);

    /**
     * 停止升级任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    Boolean stopUpgradeTask(Long taskId);

    /**
     * 删除升级任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    Boolean deleteUpgradeTask(Long taskId);

    // ==================== 任务查询 ====================

    /**
     * 分页查询升级任务列表
     *
     * @param queryForm 查询表单
     * @return 任务分页结果
     */
    PageResult<FirmwareUpgradeTaskVO> queryTasksPage(FirmwareUpgradeTaskQueryForm queryForm);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    FirmwareUpgradeTaskVO getTaskDetail(Long taskId);

    /**
     * 获取任务设备列表
     *
     * @param taskId 任务ID
     * @return 设备列表
     */
    List<FirmwareUpgradeDeviceVO> getTaskDevices(Long taskId);

    /**
     * 获取任务进度统计
     *
     * @param taskId 任务ID
     * @return 统计信息
     */
    Map<String, Object> getTaskProgress(Long taskId);

    // ==================== 设备升级管理 ====================

    /**
     * 重试失败的设备
     *
     * @param taskId 任务ID
     * @return 重试的设备数量
     */
    Integer retryFailedDevices(Long taskId);

    /**
     * 获取待升级设备列表
     *
     * @param taskId 任务ID
     * @return 待升级设备列表
     */
    List<FirmwareUpgradeDeviceVO> getPendingDevices(Long taskId);

    /**
     * 获取升级失败的设备列表
     *
     * @param taskId 任务ID
     * @return 失败设备列表
     */
    List<FirmwareUpgradeDeviceVO> getFailedDevices(Long taskId);

    // ==================== 回滚管理 ====================

    /**
     * 回滚升级任务
     *
     * @param taskId 任务ID
     * @return 回滚任务ID
     */
    Long rollbackUpgradeTask(Long taskId);

    /**
     * 检查任务是否支持回滚
     *
     * @param taskId 任务ID
     * @return 是否支持回滚
     */
    Boolean isRollbackSupported(Long taskId);

    // ==================== 定时任务 ====================

    /**
     * 处理定时任务
     * 由定时任务调度器调用
     */
    void processScheduledTasks();

    /**
     * 处理分批升级任务
     * 由定时任务调度器调用
     */
    void processBatchTasks();
}
