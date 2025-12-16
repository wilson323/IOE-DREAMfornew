package net.lab1024.sa.common.organization.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域实体类
 * <p>
 * 统一区域实体，用于所有业务模块的区域管理
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 门禁区域管理
 * - 考勤区域管理
 * - 消费区域管理
 * - 访客区域管理
 * </p>
 * <p>
 * 数据库表：t_common_area（根据DAO中的SQL推断）
 * 注意：DAO中使用了t_common_area表，但SQL脚本中可能是t_area
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_area")
public class AreaEntity extends BaseEntity {

    /**
     * 区域ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列area_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "area_id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域编码
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 区域名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 区域类型
     */
    @TableField("area_type")
    private String areaType;

    /**
     * 区域层级
     */
    @TableField("area_level")
    private Integer areaLevel;

    /**
     * 父区域ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序索引
     */
    @TableField("sort_index")
    private Integer sortIndex;

    /**
     * 区域路径（层级路径，如：/1/2/3/）
     */
    @TableField("path")
    private String path;

    /**
     * 区域描述
     */
    @TableField("area_desc")
    private String areaDesc;

    /**
     * 区域负责人ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 状态（1-启用，0-禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 子区域列表（仅用于树形结构构建，不持久化）
     */
    @TableField(exist = false)
    private java.util.List<AreaEntity> children;

    /**
     * 获取GPS位置信息（格式：经度,纬度）
     * <p>
     * 用于消费推荐服务等场景
     * </p>
     *
     * @return GPS位置字符串，格式为"经度,纬度"，如果经纬度为空则返回null
     */
    public String getGpsLocation() {
        if (longitude != null && latitude != null) {
            return longitude + "," + latitude;
        }
        return null;
    }
}
