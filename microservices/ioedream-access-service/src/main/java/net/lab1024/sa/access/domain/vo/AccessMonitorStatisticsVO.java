package net.lab1024.sa.access.domain.vo;

import lombok.Data;

/**
 * 门禁监控统计视图对象
 * <p>
 * 用于实时监控模块显示统计数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessMonitorStatisticsVO {

    /**
     * 设备总数
     */
    private Long totalDevices;

    /**
     * 在线设备数
     */
    private Long onlineDevices;

    /**
     * 离线设备数
     */
    private Long offlineDevices;

    /**
     * 故障设备数
     */
    private Long faultDevices;

    /**
     * 今日通行总数
     */
    private Long todayAccessTotal;

    /**
     * 今日成功通行数
     */
    private Long todayAccessSuccess;

    /**
     * 今日失败通行数
     */
    private Long todayAccessFailed;

    /**
     * 未处理报警数
     */
    private Long unhandledAlarms;

    /**
     * 紧急报警数
     */
    private Long criticalAlarms;

    /**
     * 当前在线人数（根据最近通行记录估算）
     */
    private Long currentOnlinePersons;
}
