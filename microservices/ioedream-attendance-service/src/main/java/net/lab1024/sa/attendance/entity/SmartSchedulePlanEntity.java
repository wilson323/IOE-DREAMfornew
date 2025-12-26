package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 智能排班计划实体
 * <p>
 * 存储智能排班算法生成的排班计划：
 * - 计划基础信息（名称、周期、范围）
 * - 优化目标配置
 * - 约束条件配置
 * - 执行状态和结果统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_schedule_plan")
@Schema(description = "智能排班计划实体")
public class SmartSchedulePlanEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "排班计划ID")
    private Long planId;

    @Schema(description = "计划名称", required = true, example = "2025年1月排班计划")
    private String planName;

    @Schema(description = "计划编码", example = "PLAN_202501")
    private String planCode;

    @Schema(description = "计划类型: 1-自动生成 2-手动优化 3-混合模式", example = "1")
    private Integer planType;

    @Schema(description = "计划描述")
    private String description;

    // ==================== 排班周期 ====================

    @Schema(description = "计划开始日期", required = true)
    private LocalDate startDate;

    @Schema(description = "计划结束日期", required = true)
    private LocalDate endDate;

    @Schema(description = "排班周期（天）", example = "30")
    private Integer periodDays;

    // ==================== 排班范围 ====================

    @Schema(description = "部门ID（可选，为空则全员排班）")
    private Long departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "员工ID列表（JSON数组）")
    private String employeeIds;

    @Schema(description = "员工数量", example = "50")
    private Integer employeeCount;
    @Schema(description = "班次ID列表（JSON数组）")
    private String shiftIds;

    // ==================== 优化目标配置 ====================

    @Schema(description = "优化目标: 1-公平性优先 2-成本优先 3-效率优先 4-满意度优先 5-综合优化", example = "5")
    private Integer optimizationGoal;

    @Schema(description = "优化目标权重（JSON格式，如{\"fairness\":0.3,\"cost\":0.3,\"efficiency\":0.2,\"satisfaction\":0.2}）")
    private String optimizationWeights;

    // ==================== 权重字段（独立存储）====================

    @Schema(description = "公平性权重 (0.0-1.0)", example = "0.4")
    private Double fairnessWeight;

    @Schema(description = "成本权重 (0.0-1.0)", example = "0.3")
    private Double costWeight;

    @Schema(description = "效率权重 (0.0-1.0)", example = "0.2")
    private Double efficiencyWeight;

    @Schema(description = "满意度权重 (0.0-1.0)", example = "0.1")
    private Double satisfactionWeight;

    // ==================== 约束条件配置 ====================

    @Schema(description = "约束条件（JSON格式）")
    private String constraints;


    @Schema(description = "最小连续工作天数", example = "1")
    private Integer minConsecutiveWorkDays;

    @Schema(description = "最大连续工作天数", example = "7")
    private Integer maxConsecutiveWorkDays;

    @Schema(description = "最小休息天数", example = "2")
    private Integer minRestDays;

    @Schema(description = "每日最少在岗人数", example = "5")
    private Integer minDailyStaff;

    @Schema(description = "每日最多在岗人数", example = "20")
    private Integer maxDailyStaff;

    // ==================== 算法配置 ====================

    @Schema(description = "优化算法: 1-遗传算法 2-模拟退火 3-贪心算法 4-整数规划", example = "1")
    private Integer algorithmType;

    @Schema(description = "算法参数（JSON格式）")
    private String algorithmParams;

    // ==================== 遗传算法参数（独立存储）====================

    @Schema(description = "种群大小", example = "50")
    private Integer populationSize;

    @Schema(description = "最大迭代次数（遗传算法）", example = "50")
    private Integer maxGenerations;

    @Schema(description = "最大迭代次数", example = "1000")
    private Integer maxIterations;

    @Schema(description = "交叉率 (0.0-1.0)", example = "0.8")
    private Double crossoverRate;

    @Schema(description = "变异率 (0.0-1.0)", example = "0.1")
    private Double mutationRate;
    
    @Schema(description = "选择率 (0.0-1.0)", example = "0.5")
    private Double selectionRate;
    
    @Schema(description = "精英保留率 (0.0-1.0)", example = "0.1")
    private Double elitismRate;

    @Schema(description = "收敛阈值", example = "0.001")
    private Double convergenceThreshold;
    
    // ==================== 成本参数 ====================
    
    @Schema(description = "加班班次成本", example = "100.0")
    private Double overtimeCostPerShift;
    
    @Schema(description = "周末班次成本", example = "150.0")
    private Double weekendCostPerShift;
    
    @Schema(description = "节假日班次成本", example = "200.0")
    private Double holidayCostPerShift;

    // ==================== 执行状态 ====================

    @Schema(description = "执行状态: 0-待执行 1-执行中 2-已完成 3-执行失败", example = "0")
    private Integer executionStatus;

    @Schema(description = "执行开始时间")
    private java.time.LocalDateTime executeStartTime;

    @Schema(description = "执行结束时间")
    private java.time.LocalDateTime executeEndTime;

    @Schema(description = "执行耗时（毫秒）", example = "15230")
    private Long executionDurationMs;

    @Schema(description = "执行进度（%）", example = "50.5")
    private Double executionProgress;

    // ==================== 优化结果统计 ====================

    @Schema(description = "目标函数值（最优解）", example = "0.85")
    private Double objectiveValue;

    @Schema(description = "适应度值（0-1之间，越大越好）", example = "0.92")
    private Double fitnessScore;

    @Schema(description = "迭代次数", example = "856")
    private Integer iterationCount;

    @Schema(description = "公平性得分（0-1）", example = "0.88")
    private Double fairnessScore;

    @Schema(description = "成本得分（0-1）", example = "0.75")
    private Double costScore;

    @Schema(description = "效率得分（0-1）", example = "0.91")
    private Double efficiencyScore;

    @Schema(description = "满意度得分（0-1）", example = "0.86")
    private Double satisfactionScore;

    @Schema(description = "冲突数量", example = "5")
    private Integer conflictCount;

    @Schema(description = "未满足约束数量", example = "2")
    private Integer unsatisfiedConstraintCount;

    // ==================== 执行结果详情 ====================

    @Schema(description = "是否收敛（算法是否找到稳定解）: 0-未收敛 1-已收敛", example = "1")
    private Integer converged;

    @Schema(description = "执行错误信息（执行失败时记录）", example = "算法执行超时")
    private String errorMessage;

    // ==================== 操作信息 ====================

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "备注")
    private String remark;
}
