package {{package}};

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * {{EntityName}}业务实体类
 * <p>
 * 业务实体继承自BaseEntity，包含审计字段，支持复杂业务逻辑
 * </p>
 *
 * @author {{author}}
 * @since {{date}}
 */
@Entity
@Table(name = "{{tableName}}", indexes = {
    @Index(name = "idx_{{tableName}}_business_key", columnList = "businessKey", unique = true),
    @Index(name = "idx_{{tableName}}_status", columnList = "status"),
    @Index(name = "idx_{{tableName}}_create_time", columnList = "createTime")
})
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class {{EntityName}}Entity extends BaseEntity {

    // ==================== 核心业务字段 ====================

    /**
     * 业务键 - 业务唯一标识
     */
    @Column(name = "business_key", nullable = false, unique = true, length = 64)
    @NotBlank(message = "业务键不能为空")
    @Size(max = 64, message = "业务键长度不能超过64个字符")
    private String businessKey;

    /**
     * 业务名称 - 人类可读的名称
     */
    @Column(name = "business_name", nullable = false, length = 128)
    @NotBlank(message = "业务名称不能为空")
    @Size(max = 128, message = "业务名称长度不能超过128个字符")
    private String businessName;

    /**
     * 业务状态 - 使用枚举类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private {{EntityName}}Status status;

    /**
     * 业务类型 - 分类标识
     */
    @Column(name = "business_type", nullable = false, length = 32)
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 优先级 - 数值越大优先级越高
     */
    @Column(name = "priority")
    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 100, message = "优先级不能大于100")
    private Integer priority;

    // ==================== 扩展业务字段 ====================

    /**
     * 数值属性 - 支持业务计算
     */
    @Column(name = "amount", precision = 18, scale = 2)
    @DecimalMin(value = "0.00", message = "金额不能小于0")
    @Digits(integer = 16, fraction = 2, message = "金额格式不正确")
    private BigDecimal amount;

    /**
     * 百分比属性 - 0-100之间
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.00", message = "百分比不能小于0")
    @DecimalMax(value = "100.00", message = "百分比不能大于100")
    private BigDecimal percentage;

    /**
     * 开始时间 - 业务生效时间
     */
    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间 - 业务失效时间
     */
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 描述信息 - 详细说明
     */
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    /**
     * 备注信息 - 额外说明
     */
    @Column(name = "remark", length = 200)
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;

    // ==================== 关联业务字段 ====================

    /**
     * 关联用户ID - 业务归属用户
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 关联部门ID - 业务归属部门
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 关联组织ID - 业务归属组织
     */
    @Column(name = "organization_id")
    private Long organizationId;

    /**
     * 外部系统ID - 集成标识
     */
    @Column(name = "external_id", length = 64)
    @Size(max = 64, message = "外部系统ID长度不能超过64个字符")
    private String externalId;

    /**
     * 扩展属性 - JSON格式存储
     */
    @Column(name = "extend_data", columnDefinition = "TEXT")
    private String extendData;

    // ==================== 标准业务方法 ====================

    /**
     * 获取描述信息
     * 优先返回自定义描述，其次返回业务名称，最后返回默认描述
     */
    public String getDescription() {
        if (this.description != null && !this.description.trim().isEmpty()) {
            return this.description;
        }
        if (this.businessName != null && !this.businessName.trim().isEmpty()) {
            return this.businessName;
        }
        return String.format("%s-%s", this.getClass().getSimpleName(), this.getId());
    }

    /**
     * 获取显示文本
     * 返回人类可读的业务名称
     */
    public String getText() {
        return this.businessName != null ? this.businessName : this.getDescription();
    }

    /**
     * 获取权重值
     * 基于优先级和业务状态计算权重
     */
    public Double getWeight() {
        if (this.priority == null) {
            return 0.0;
        }

        double baseWeight = this.priority.doubleValue();

        // 根据业务状态调整权重
        if (this.status != null) {
            switch (this.status) {
                case ACTIVE:
                    baseWeight *= 1.0;
                    break;
                case PENDING:
                    baseWeight *= 0.8;
                    break;
                case SUSPENDED:
                    baseWeight *= 0.5;
                    break;
                case COMPLETED:
                    baseWeight *= 0.3;
                    break;
                case CANCELLED:
                    baseWeight *= 0.1;
                    break;
                default:
                    baseWeight *= 0.6;
            }
        }

        return baseWeight;
    }

    /**
     * 检查业务对象是否有效
     * 基于业务状态和时间范围验证有效性
     */
    public boolean isValid() {
        // 检查业务状态
        if (this.status == null || this.status == {{EntityName}}Status.CANCELLED) {
            return false;
        }

        // 检查时间范围
        LocalDateTime now = LocalDateTime.now();
        if (this.startTime != null && now.isBefore(this.startTime)) {
            return false;
        }
        if (this.endTime != null && now.isAfter(this.endTime)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否处于活跃状态
     */
    public boolean isActive() {
        return {{EntityName}}Status.ACTIVE.equals(this.status) && isValid();
    }

    /**
     * 检查是否处于待处理状态
     */
    public boolean isPending() {
        return {{EntityName}}Status.PENDING.equals(this.status);
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return {{EntityName}}Status.COMPLETED.equals(this.status);
    }

    /**
     * 检查是否在有效时间范围内
     */
    public boolean isInTimeRange() {
        LocalDateTime now = LocalDateTime.now();

        boolean afterStart = true;
        if (this.startTime != null) {
            afterStart = !now.isBefore(this.startTime);
        }

        boolean beforeEnd = true;
        if (this.endTime != null) {
            beforeEnd = !now.isAfter(this.endTime);
        }

        return afterStart && beforeEnd;
    }

    /**
     * 激活业务对象
     */
    public void activate() {
        this.status = {{EntityName}}Status.ACTIVE;
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }

    /**
     * 暂停业务对象
     */
    public void suspend() {
        this.status = {{EntityName}}Status.SUSPENDED;
    }

    /**
     * 取消业务对象
     */
    public void cancel() {
        this.status = {{EntityName}}Status.CANCELLED;
    }

    /**
     * 完成业务对象
     */
    public void complete() {
        this.status = {{EntityName}}Status.COMPLETED;
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    // ==================== 业务逻辑方法 ====================

    /**
     * 更新业务数据
     * 包含业务验证和数据更新逻辑
     */
    public void updateBusinessData({{EntityName}}Entity source) {
        if (source == null) {
            return;
        }

        // 更新核心业务字段
        if (source.getBusinessName() != null) {
            this.businessName = source.getBusinessName();
        }
        if (source.getBusinessType() != null) {
            this.businessType = source.getBusinessType();
        }
        if (source.getPriority() != null) {
            this.priority = source.getPriority();
        }

        // 更新扩展业务字段
        if (source.getAmount() != null) {
            this.amount = source.getAmount();
        }
        if (source.getPercentage() != null) {
            this.percentage = source.getPercentage();
        }
        if (source.getStartTime() != null) {
            this.startTime = source.getStartTime();
        }
        if (source.getEndTime() != null) {
            this.endTime = source.getEndTime();
        }
        if (source.getDescription() != null) {
            this.description = source.getDescription();
        }
        if (source.getRemark() != null) {
            this.remark = source.getRemark();
        }

        // 更新关联业务字段
        if (source.getUserId() != null) {
            this.userId = source.getUserId();
        }
        if (source.getDepartmentId() != null) {
            this.departmentId = source.getDepartmentId();
        }
        if (source.getOrganizationId() != null) {
            this.organizationId = source.getOrganizationId();
        }
        if (source.getExternalId() != null) {
            this.externalId = source.getExternalId();
        }
        if (source.getExtendData() != null) {
            this.extendData = source.getExtendData();
        }
    }

    /**
     * 复制业务对象
     * 创建当前对象的深拷贝，不包含ID和审计字段
     */
    public {{EntityName}}Entity copy() {
        {{EntityName}}Entity copy = new {{EntityName}}Entity();

        // 复制业务字段
        copy.setBusinessKey(this.businessKey);
        copy.setBusinessName(this.businessName);
        copy.setStatus(this.status);
        copy.setBusinessType(this.businessType);
        copy.setPriority(this.priority);
        copy.setAmount(this.amount);
        copy.setPercentage(this.percentage);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setDescription(this.description);
        copy.setRemark(this.remark);

        // 复制关联字段
        copy.setUserId(this.userId);
        copy.setDepartmentId(this.departmentId);
        copy.setOrganizationId(this.organizationId);
        copy.setExternalId(this.externalId);
        copy.setExtendData(this.extendData);

        return copy;
    }

    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        return String.format(
            "%s{id=%d, businessKey='%s', businessName='%s', status=%s, businessType='%s'}",
            this.getClass().getSimpleName(),
            this.getId(),
            this.businessKey,
            this.businessName,
            this.status,
            this.businessType
        );
    }

    /**
     * 计算哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(
            this.getId(),
            this.businessKey,
            this.businessName,
            this.status,
            this.businessType
        );
    }

    /**
     * 对象相等性比较
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        {{EntityName}}Entity that = ({{EntityName}}Entity) obj;
        return Objects.equals(this.getId(), that.getId()) &&
               Objects.equals(this.businessKey, that.businessKey) &&
               Objects.equals(this.businessName, that.businessName) &&
               this.status == that.status &&
               Objects.equals(this.businessType, that.businessType);
    }
}

/**
 * {{EntityName}}业务状态枚举
 */
enum {{EntityName}}Status {
    /**
     * 活跃状态
     */
    ACTIVE("活跃"),

    /**
     * 待处理状态
     */
    PENDING("待处理"),

    /**
     * 暂停状态
     */
    SUSPENDED("暂停"),

    /**
     * 已完成状态
     */
    COMPLETED("已完成"),

    /**
     * 已取消状态
     */
    CANCELLED("已取消");

    private final String description;

    {{EntityName}}Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}