package net.lab1024.sa.access.domain.vo;

import lombok.Data;

/**
 * 门禁区域概览视图对象
 * <p>
 * 用于区域空间管理模块显示区域概览信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAreaOverviewVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域类型
     * <p>
     * 1-园区 2-建筑 3-楼层 4-房间 5-区域 6-点位
     * </p>
     */
    private Integer areaType;

    /**
     * 区域类型名称
     */
    private String areaTypeName;

    /**
     * 父区域ID
     */
    private Long parentAreaId;

    /**
     * 父区域名称
     */
    private String parentAreaName;

    /**
     * 区域状态
     * <p>
     * 1-正常 2-停用 3-装修 4-关闭
     * </p>
     */
    private Integer areaStatus;

    /**
     * 区域状态名称
     */
    private String areaStatusName;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 当前人数
     */
    private Integer currentCount;

    /**
     * 设备总数
     */
    private Long deviceCount;

    /**
     * 在线设备数
     */
    private Long onlineDeviceCount;

    /**
     * 离线设备数
     */
    private Long offlineDeviceCount;

    /**
     * 人员总数（有权限的人员数）
     */
    private Long personCount;

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
    private Long unhandledAlarmCount;
}
