package net.lab1024.sa.access.domain.vo;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 门禁记录统计视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 用于Controller返回统计数据
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessRecordStatisticsVO {

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 成功记录数
     */
    private Long successCount;

    /**
     * 失败记录数
     */
    private Long failedCount;

    /**
     * 异常记录数
     */
    private Long abnormalCount;

    /**
     * 按操作类型统计
     */
    private List<Map<String, Object>> statisticsByOperation;

    /**
     * 按区域统计
     */
    private List<Map<String, Object>> statisticsByArea;

    /**
     * 按设备统计
     */
    private List<Map<String, Object>> statisticsByDevice;

    /**
     * 按时间统计（按日）
     */
    private List<Map<String, Object>> statisticsByDate;
}


