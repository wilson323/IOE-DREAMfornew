package net.lab1024.sa.admin.module.system.area.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 区域人员关联实体类
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
@TableName(value = "t_area_user", autoResultMap = true)
public class AreaUserEntity {

    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long relationId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 关联类型
     */
    private String relationType;

    /**
     * 访问级别
     */
    private Integer accessLevel;

    /**
     * 访问时间配置JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> accessTimeConfig;

    /**
     * 有效开始时间
     */
    private LocalDateTime validStartTime;

    /**
     * 有效结束时间
     */
    private LocalDateTime validEndTime;

    /**
     * 授权人ID
     */
    private Long grantUserId;

    /**
     * 授权时间
     */
    private LocalDateTime grantTime;

    /**
     * 撤销人ID
     */
    private Long revokeUserId;

    /**
     * 撤销时间
     */
    private LocalDateTime revokeTime;

    /**
     * 授权备注
     */
    private String grantRemark;

    /**
     * 状态：1-有效，0-已撤销
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
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deletedFlag;

}
