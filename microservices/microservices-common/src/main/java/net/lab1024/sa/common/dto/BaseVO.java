package net.lab1024.sa.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础视图对象
 * 严格遵循repowiki规范
 */
@Data
public abstract class BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

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
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 备注
     */
    private String remark;
}