package net.lab1024.sa.consume.report.domain.form;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

/**
 * 报表参数表单
 * <p>
 * 用于报表生成的参数传递
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 包含完整的参数验证注解
 * - 符合企业级表单设计
 * </p>
 * <p>
 * 业务场景：
 * - 报表数据查询参数
 * - 报表统计维度配置
 * - 报表过滤条件设置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ReportParams {

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 统计维度
     * <p>
     * 支持的维度：
     * - area: 按区域统计
     * - device: 按设备统计
     * - user: 按用户统计
     * - product: 按商品统计
     * </p>
     */
    private Map<String, Object> dimensions;

    /**
     * 过滤条件
     * <p>
     * 支持多种过滤条件：
     * - areaId: 区域ID
     * - deviceId: 设备ID
     * - userId: 用户ID
     * - productId: 商品ID
     * </p>
     */
    private Map<String, Object> filters;

    /**
     * 分组维度
     * <p>
     * 用于报表数据分组统计
     * </p>
     */
    private java.util.List<String> groupBy;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderDirection;

    /**
     * 分页页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;
}
