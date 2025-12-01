package net.lab1024.sa.base.module.area.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;

/**
 * 区域视图对象
 * 用于API响应和数据展示
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AreaVO extends AreaEntity {

    /**
     * 创建人姓名（扩展字段）
     */
    private String createUserName;

    /**
     * 更新人姓名（扩展字段）
     */
    private String updateUserName;

    /**
     * 是否有子区域（业务计算字段）
     */
    private Boolean hasChildren;

    /**
     * 子区域数量（业务计算字段）
     */
    private Integer childrenCount;

    /**
     * 区域类型名称（业务计算字段）
     */
    private String areaTypeName;

    /**
     * 区域类型英文名称（业务计算字段）
     */
    private String areaTypeEnglishName;

    /**
     * 父级区域名称（业务计算字段）
     */
    private String parentName;

    /**
     * 设备数量（业务计算字段）
     */
    private Integer deviceCount;

    /**
     * 人员数量（业务计算字段）
     */
    private Integer personCount;

    /**
     * 包含自身和所有子区域的ID列表（业务计算字段）
     */
    private String selfAndAllChildrenIdsStr;

    /**
     * 区域完整路径（业务计算字段）
     */
    private String fullPath;
}