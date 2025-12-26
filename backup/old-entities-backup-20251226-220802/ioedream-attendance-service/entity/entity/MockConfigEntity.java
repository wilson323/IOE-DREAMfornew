package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mock配置实体
 * <p>
 * 存储规则测试的Mock配置
 * 支持配置Mock数据生成规则和场景
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_attendance_mock_config")
@Schema(description = "Mock配置实体")
public class MockConfigEntity {

    /**
     * 配置ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "配置ID")
    private Long configId;

    /**
     * 配置名称
     */
    @TableField("config_name")
    @Schema(description = "配置名称")
    private String configName;

    /**
     * 配置类型：EMPLOYEE-员工数据/DEPARTMENT-部门数据/SHIFT-班次数据/ATTENDANCE-考勤数据
     */
    @TableField("config_type")
    @Schema(description = "配置类型")
    private String configType;

    /**
     * Mock场景：NORMAL-正常场景/EDGE_CASE-边界场景/ERROR-异常场景/PERFORMANCE-性能场景
     */
    @TableField("mock_scenario")
    @Schema(description = "Mock场景")
    private String mockScenario;

    /**
     * Mock状态：ENABLED-启用/DISABLED-禁用
     */
    @TableField("mock_status")
    @Schema(description = "Mock状态")
    private String mockStatus;

    /**
     * 数据生成规则（JSON格式）
     * 包含数据范围、数据量、随机种子等配置
     */
    @TableField("generation_rules")
    @Schema(description = "数据生成规则（JSON）")
    private String generationRules;

    /**
     * Mock数据模板（JSON格式）
     * 定义Mock数据的结构和默认值
     */
    @TableField("data_template")
    @Schema(description = "Mock数据模板（JSON）")
    private String dataTemplate;

    /**
     * 返回延迟（毫秒）
     * 用于模拟网络延迟
     */
    @TableField("response_delay_ms")
    @Schema(description = "返回延迟（毫秒）")
    private Long responseDelayMs;

    /**
     * 错误率（百分比）
     * 用于模拟接口错误
     */
    @TableField("error_rate")
    @Schema(description = "错误率（百分比）")
    private Double errorRate;

    /**
     * 超时率（百分比）
     * 用于模拟接口超时
     */
    @TableField("timeout_rate")
    @Schema(description = "超时率（百分比）")
    private Double timeoutRate;

    /**
     * 是否启用随机延迟
     */
    @TableField("enable_random_delay")
    @Schema(description = "是否启用随机延迟")
    private Boolean enableRandomDelay;

    /**
     * 随机延迟范围（最小值，毫秒）
     */
    @TableField("random_delay_min_ms")
    @Schema(description = "随机延迟最小值（毫秒）")
    private Long randomDelayMinMs;

    /**
     * 随机延迟范围（最大值，毫秒）
     */
    @TableField("random_delay_max_ms")
    @Schema(description = "随机延迟最大值（毫秒）")
    private Long randomDelayMaxMs;

    /**
     * 使用次数统计
     */
    @TableField("usage_count")
    @Schema(description = "使用次数")
    private Long usageCount;

    /**
     * 最后使用时间
     */
    @TableField("last_used_time")
    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    /**
     * 配置描述
     */
    @TableField("config_description")
    @Schema(description = "配置描述")
    private String configDescription;

    /**
     * 审计字段
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
