package net.lab1024.sa.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批模板实体
 * <p>
 * 用于管理审批模板，支持快速创建相似的审批配置
 * 比钉钉更完善的模板管理功能
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * </p>
 * <p>
 * 功能说明：
 * - 支持审批模板的创建、复制、修改
 * - 支持模板分类管理
 * - 支持模板版本管理
 * - 支持模板预览和测试
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_template")
public class ApprovalTemplateEntity extends BaseEntity {

    /**
     * 模板ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板编码（唯一标识）
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板分类
     * <p>
     * 示例：考勤类、财务类、人事类、自定义类等
     * </p>
     */
    @TableField("category")
    private String category;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * 模板图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 模板配置（JSON格式）
     * <p>
     * 包含完整的审批配置信息，用于快速创建审批配置
     * 示例：
     * {
     *   "business_type": "TEMPLATE_LEAVE",
     *   "business_type_name": "请假审批模板",
     *   "module": "考勤模块",
     *   "approval_rules": {...},
     *   "post_approval_handler": {...},
     *   "timeout_config": {...},
     *   "notification_config": {...},
     *   "node_configs": [...]  // 节点配置列表
     * }
     * </p>
     */
    @TableField("template_config")
    private String templateConfig;

    /**
     * 表单设计（JSON格式）
     * <p>
     * 审批表单的字段配置
     * 示例：
     * {
     *   "fields": [
     *     {
     *       "field_name": "leave_type",
     *       "field_label": "请假类型",
     *       "field_type": "select",
     *       "required": true,
     *       "options": ["年假", "病假", "事假"]
     *     },
     *     {
     *       "field_name": "start_date",
     *       "field_label": "开始日期",
     *       "field_type": "date",
     *       "required": true
     *     }
     *   ],
     *   "layout": "vertical"  // 布局方式：vertical-垂直、horizontal-水平
     * }
     * </p>
     */
    @TableField("form_design")
    private String formDesign;

    /**
     * 使用次数
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 是否公开
     * <p>
     * 0-私有（仅创建人可用）
     * 1-公开（所有人可用）
     * </p>
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * 状态（ENABLED-启用 DISABLED-禁用）
     */
    @TableField("status")
    private String status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

