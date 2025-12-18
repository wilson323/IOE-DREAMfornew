package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessMonitorQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAlarmVO;
import net.lab1024.sa.access.domain.vo.AccessDeviceStatusVO;
import net.lab1024.sa.access.domain.vo.AccessPersonTrackVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 门禁实时监控服务接口
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 实时状态监控、报警处理、视频联动、人员追踪
 * </p>
 * <p>
 * 核心职责：
 * - 实时设备状态监控
 * - 报警接收和处理
 * - 视频联动触发
 * - 人员轨迹追踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessMonitorService {

    /**
     * 查询实时设备状态列表
     * <p>
     * 查询所有门禁设备的实时状态，包括在线状态、响应时间、网络质量等
     * </p>
     *
     * @param queryForm 查询表单
     * @return 设备状态列表
     */
    ResponseDTO<PageResult<AccessDeviceStatusVO>> queryDeviceStatusList(AccessMonitorQueryForm queryForm);

    /**
     * 查询单个设备实时状态
     * <p>
     * 查询指定设备的详细实时状态信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态详情
     */
    ResponseDTO<AccessDeviceStatusVO> getDeviceStatus(String deviceId);

    /**
     * 查询报警列表
     * <p>
     * 分页查询报警信息，支持按报警类型、级别、设备等条件过滤
     * </p>
     *
     * @param queryForm 查询表单
     * @return 报警列表
     */
    ResponseDTO<PageResult<AccessAlarmVO>> queryAlarmList(AccessMonitorQueryForm queryForm);

    /**
     * 处理报警
     * <p>
     * 处理指定的报警事件，记录处理结果
     * </p>
     *
     * @param alarmId 报警ID
     * @param handleRemark 处理备注
     * @return 操作结果
     */
    ResponseDTO<Void> handleAlarm(Long alarmId, String handleRemark);

    /**
     * 触发视频联动
     * <p>
     * 根据门禁事件触发视频联动，启动录像和抓拍
     * </p>
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型（ACCESS_GRANTED/ACCESS_DENIED等）
     * @return 操作结果
     */
    ResponseDTO<Void> triggerVideoLinkage(String deviceId, String eventType);

    /**
     * 查询人员轨迹
     * <p>
     * 查询指定人员在指定时间范围内的通行轨迹
     * </p>
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 人员轨迹信息
     */
    ResponseDTO<AccessPersonTrackVO> queryPersonTrack(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 查询实时通行事件
     * <p>
     * 查询最近的通行事件，用于实时监控大屏展示
     * </p>
     *
     * @param limit 返回数量限制
     * @return 通行事件列表
     */
    ResponseDTO<List<net.lab1024.sa.access.domain.vo.AccessEventVO>> queryRealtimeEvents(Integer limit);

    /**
     * 统计监控数据
     * <p>
     * 统计实时监控相关的数据，包括在线设备数、报警数、今日通行数等
     * </p>
     *
     * @return 监控统计数据
     */
    ResponseDTO<net.lab1024.sa.access.domain.vo.AccessMonitorStatisticsVO> getMonitorStatistics();
}
