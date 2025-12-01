package net.lab1024.sa.access.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 人员区域授权实体类
 * 表名: t_area_person
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area_person")
public class AreaPersonEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 数据域(AREA|DEPT|SELF|CUSTOM)
     */
    private String dataScope;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;
}