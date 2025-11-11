package net.lab1024.sa.admin.module.system.area.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 区域实体类
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
@TableName(value = "t_area", autoResultMap = true)
public class AreaEntity {

    /**
     * 区域ID
     */
    @TableId(type = IdType.AUTO)
    private Long areaId;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域类型
     */
    private String areaType;

    /**
     * 区域层级
     */
    private Integer areaLevel;

    /**
     * 父区域ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 区域配置JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> areaConfig;

    /**
     * 区域描述
     */
    private String areaDesc;

    /**
     * 区域负责人ID
     */
    private Long managerId;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deletedFlag;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

}
