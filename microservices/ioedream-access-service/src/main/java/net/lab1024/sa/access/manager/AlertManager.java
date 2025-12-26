package net.lab1024.sa.access.manager;

import net.lab1024.sa.access.domain.form.DeviceAlertHandleForm;
import net.lab1024.sa.access.domain.form.DeviceAlertQueryForm;
import net.lab1024.sa.common.entity.access.DeviceAlertEntity;
import net.lab1024.sa.access.domain.vo.AlertStatisticsVO;
import net.lab1024.sa.access.domain.vo.DeviceAlertVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 告警业务管理器接口
 * <p>
 * 负责告警系统的业务编排和协调：
 * - 告警检测和创建
 * - 告警规则匹配
 * - 告警聚合（防刷屏）
 * - 告警升级处理
 * - 告警确认和处理
 * - 告警统计分析
 * </p>
 * <p>
 * Manager层定位：
 * - 协调多个Service（AlertRuleService、AlertNotificationService）
 * - 处理复杂业务逻辑
 * - 事务管理
 * - 缓存管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AlertManager {

    /**
     * 检测并创建告警
     * <p>
     * 根据设备状态和告警规则，自动检测是否需要创建告警
     * </p>
     *
     * @param deviceId      设备ID
     * @param deviceStatus  设备状态
     * @param deviceData    设备数据（如温度、存储使用率等）
     * @return 是否创建告警
     */
    ResponseDTO<Boolean> detectAndCreateAlert(Long deviceId, String deviceStatus, Map<String, Object> deviceData);

    /**
     * 手动创建告警
     *
     * @param alert 告警实体
     * @return 告警ID
     */
    ResponseDTO<Long> createAlert(DeviceAlertEntity alert);

    /**
     * 处理告警（确认或处理）
     *
     * @param handleForm 处理表单
     * @return 是否成功
     */
    ResponseDTO<Void> handleAlert(DeviceAlertHandleForm handleForm);

    /**
     * 分页查询告警列表
     *
     * @param queryForm 查询表单
     * @return 告警列表
     */
    ResponseDTO<PageResult<DeviceAlertVO>> queryAlerts(DeviceAlertQueryForm queryForm);

    /**
     * 查询告警详情
     *
     * @param alertId 告警ID
     * @return 告警详情
     */
    ResponseDTO<DeviceAlertVO> getAlertDetail(Long alertId);

    /**
     * 获取告警统计数据
     *
     * @return 统计数据
     */
    ResponseDTO<AlertStatisticsVO> getAlertStatistics();

    /**
     * 批量确认告警
     *
     * @param alertIds 告警ID列表
     * @param remark   确认备注
     * @return 确认成功的数量
     */
    ResponseDTO<Integer> batchConfirmAlerts(List<Long> alertIds, String remark);

    /**
     * 批量处理告警
     *
     * @param alertIds 告警ID列表
     * @param result   处理结果
     * @return 处理成功的数量
     */
    ResponseDTO<Integer> batchHandleAlerts(List<Long> alertIds, String result);

    /**
     * 查询紧急告警（需要立即处理的告警）
     *
     * @param limit 数量限制
     * @return 紧急告警列表
     */
    ResponseDTO<List<DeviceAlertVO>> queryCriticalAlerts(Integer limit);

    /**
     * 获取告警趋势数据（最近7天）
     *
     * @return 趋势数据
     */
    ResponseDTO<Map<String, Long>> getAlertTrend();

    /**
     * 清理过期告警记录
     *
     * @param daysToKeep 保留天数
     * @return 清理的记录数
     */
    ResponseDTO<Integer> cleanupExpiredAlerts(Integer daysToKeep);
}
