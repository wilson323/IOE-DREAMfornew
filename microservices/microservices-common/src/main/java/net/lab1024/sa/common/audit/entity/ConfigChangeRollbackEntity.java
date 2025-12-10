package net.lab1024.sa.common.audit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 配置变更回滚实体
 * <p>
 * 从ConfigChangeAuditEntity中拆分出来的回滚专门实体
 * 负责记录配置回滚相关信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_config_change_rollback")
public class ConfigChangeRollbackEntity extends BaseEntity {

    /**
     * 回滚ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列rollback_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "rollback_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的审计ID
     */
    @TableField("audit_id")
    private Long auditId;

    /**
     * 变更批次ID
     */
    @TableField("change_batch_id")
    private String changeBatchId;

    /**
     * 回滚原因
     */
    @TableField("rollback_reason")
    private String rollbackReason;

    /**
     * 回滚操作人ID
     */
    @TableField("rollback_operator_id")
    private Long rollbackOperatorId;

    /**
     * 回滚操作人姓名
     */
    @TableField("rollback_operator_name")
    private String rollbackOperatorName;

    /**
     * 回滚时间
     */
    @TableField("rollback_time")
    private LocalDateTime rollbackTime;

    /**
     * 回滚状态
     * INITIATED-已发起, IN_PROGRESS-进行中, SUCCESS-成功, FAILED-失败, CANCELLED-已取消
     */
    @TableField("rollback_status")
    private String rollbackStatus;

    /**
     * 回滚数据（JSON格式，存储回滚时的完整数据快照）
     */
    @TableField("rollback_data")
    private String rollbackData;

    /**
     * 回滚范围
     * FULL-完全回滚, PARTIAL-部分回滚, STEP-步骤回滚
     */
    @TableField("rollback_scope")
    private String rollbackScope;

    /**
     * 是否为自动回滚
     */
    @TableField("is_auto_rollback")
    private Integer isAutoRollback;

    /**
     * 回滚触发条件
     */
    @TableField("rollback_trigger")
    private String rollbackTrigger;

    /**
     * 回滚脚本
     */
    @TableField("rollback_script")
    private String rollbackScript;
}
