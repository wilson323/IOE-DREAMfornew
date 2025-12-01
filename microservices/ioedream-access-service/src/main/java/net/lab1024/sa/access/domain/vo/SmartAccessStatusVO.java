package net.lab1024.sa.access.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 智能门禁状态VO
 * <p>
 * 用于返回智能门禁系统的实时状态信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class SmartAccessStatusVO {

    /**
     * 系统状态 0-离线 1-在线 2-故障
     */
    private Integer systemStatus;

    /**
     * 在线设备数量
     */
    private Integer onlineDeviceCount;

    /**
     * 总设备数量
     */
    private Integer totalDeviceCount;

    /**
     * 今日访问次数
     */
    private Long todayAccessCount;

    /**
     * 今日成功次数
     */
    private Long todaySuccessCount;

    /**
     * 今日失败次数
     */
    private Long todayFailCount;

    /**
     * 今日成功率
     */
    private Double todaySuccessRate;

    /**
     * 当前活跃用户数
     */
    private Integer activeUserCount;

    /**
     * 系统负载
     */
    private Double systemLoad;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 告警数量
     */
    private Integer alertCount;

    /**
     * 异常设备数量
     */
    private Integer abnormalDeviceCount;
}
