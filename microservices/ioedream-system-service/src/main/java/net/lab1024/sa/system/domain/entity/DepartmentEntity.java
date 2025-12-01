package net.lab1024.sa.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 部门实体
 * <p>
 * 严格遵循repowiki实体规范：
 * - 继承BaseEntity获取审计字段
 * - 使用jakarta包名
 * - 完整的审计字段
 * - 软删除支持
 * - 标准注解配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_department")
@Schema(description = "部门实体")
public class DepartmentEntity extends BaseEntity {

    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 部门编码
     */
    @Schema(description = "部门编码", example = "TECH")
    private String departmentCode;

    /**
     * 父部门ID
     */
    @Schema(description = "父部门ID", example = "0")
    private Long parentId;

    /**
     * 部门层级
     */
    @Schema(description = "部门层级", example = "1")
    private Integer level;

    /**
     * 部门路径（从根到当前部门的ID路径）
     */
    @Schema(description = "部门路径", example = "0,1,2")
    private String departmentPath;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortNumber;

    /**
     * 部门状态（1-启用，0-禁用）
     */
    @Schema(description = "部门状态", example = "1")
    private Integer status;

    /**
     * 部门描述
     */
    @Schema(description = "部门描述", example = "技术研发部")
    private String description;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "021-12345678")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱", example = "tech@company.com")
    private String contactEmail;

    /**
     * 部门负责人
     */
    @Schema(description = "部门负责人", example = "张三")
    private String manager;

    /**
     * 负责人ID
     */
    @Schema(description = "负责人ID", example = "100")
    private Long managerUserId;

    /**
     * 部门地址
     */
    @Schema(description = "部门地址", example = "上海市浦东新区XXX路XXX号")
    private String address;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段", example = "{}")
    private String extendInfo;
}
