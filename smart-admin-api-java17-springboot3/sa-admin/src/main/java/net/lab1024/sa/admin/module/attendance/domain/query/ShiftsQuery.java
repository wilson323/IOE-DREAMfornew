package net.lab1024.sa.admin.module.attendance.domain.query;

import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 班次查询条件类
 *
 * 提供灵活的班次查询条件构建，支持多维度筛选
 * 严格遵循项目开发规范和编码标准
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
public class ShiftsQuery extends PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班次编码（模糊查询）
     */
    private String shiftCode;

    /**
     * 班次名称（模糊查询）
     */
    private String shiftName;

    /**
     * 班次类型
     * REGULAR-规律班次
     * FLEXIBLE-弹性班次
     * ROTATING-轮班班次
     */
    private String shiftType;

    /**
     * 状态：1-启用 0-禁用
     */
    private Boolean status;

    /**
     * 最小工作时长（小时）
     */
    private BigDecimal minWorkHours;

    /**
     * 最大工作时长（小时）
     */
    private BigDecimal maxWorkHours;

    /**
     * 是否支持弹性工作
     */
    private Boolean supportsFlexible;

    /**
     * 是否支持轮班
     */
    private Boolean supportsRotating;

    /**
     * 工作日筛选：是否包含周一
     */
    private Boolean includeMonday;

    /**
     * 工作日筛选：是否包含周二
     */
    private Boolean includeTuesday;

    /**
     * 工作日筛选：是否包含周三
     */
    private Boolean includeWednesday;

    /**
     * 工作日筛选：是否包含周四
     */
    private Boolean includeThursday;

    /**
     * 工作日筛选：是否包含周五
     */
    private Boolean includeFriday;

    /**
     * 工作日筛选：是否包含周六
     */
    private Boolean includeSaturday;

    /**
     * 工作日筛选：是否包含周日
     */
    private Boolean includeSunday;

    /**
     * 加班配置筛选：是否启用加班
     */
    private Boolean overtimeEnabled;

    /**
     * 加班阈值筛选：最小阈值
     */
    private BigDecimal minOvertimeThreshold;

    /**
     * 加班阈值筛选：最大阈值
     */
    private BigDecimal maxOvertimeThreshold;

    /**
     * 创建时间范围-开始
     */
    private String createTimeStart;

    /**
     * 创建时间范围-结束
     */
    private String createTimeEnd;

    /**
     * 更新时间范围-开始
     */
    private String updateTimeStart;

    /**
     * 更新时间范围-结束
     */
    private String updateTimeEnd;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * 关键词搜索（班次编码、名称、描述）
     */
    private String keyword;

    /**
     * 排序方式预设
     * DEFAULT-默认排序
     * WORK_HOURS-工作时长排序
     * CREATE_TIME-创建时间排序
     */
    private String sortPreset;
}