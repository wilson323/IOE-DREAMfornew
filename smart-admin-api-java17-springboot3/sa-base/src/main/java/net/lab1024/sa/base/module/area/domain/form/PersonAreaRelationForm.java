package net.lab1024.sa.base.module.area.domain.form;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 人员区域关联表单对象
 * 用于人员区域关联的创建和更新
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
public class PersonAreaRelationForm {

    /**
     * 关联ID（更新时必填）
     */
    private Long relationId;

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 人员类型
     * EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-外包
     */
    @NotBlank(message = "人员类型不能为空")
    @Size(max = 32, message = "人员类型长度不能超过32个字符")
    private String personType;

    /**
     * 人员姓名
     */
    @Size(max = 100, message = "人员姓名长度不能超过100个字符")
    private String personName;

    /**
     * 人员编号
     */
    @Size(max = 50, message = "人员编号长度不能超过50个字符")
    private String personCode;

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 关联类型
     * PRIMARY-主归属, SECONDARY-次要归属, TEMPORARY-临时
     */
    @NotBlank(message = "关联类型不能为空")
    @Size(max = 32, message = "关联类型长度不能超过32个字符")
    private String relationType;

    /**
     * 生效时间
     */
    @NotNull(message = "生效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（NULL表示永久有效）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 同步设备类型列表
     */
    private List<String> syncDeviceTypes;

    /**
     * 同步配置
     */
    private String syncConfig;

    /**
     * 优先级（1-10，数字越小优先级越高）
     */
    private Integer priorityLevel;

    /**
     * 是否自动续期
     */
    private Boolean autoRenew;

    /**
     * 自动续期天数
     */
    private Integer renewDays;

    /**
     * 状态
     * 0-停用, 1-启用
     */
    private Integer status;

    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;

    /**
     * 分页参数 - 当前页
     */
    private Integer current;

    /**
     * 分页参数 - 每页大小
     */
    private Integer pageSize;
}

