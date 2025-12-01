package net.lab1024.sa.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础数据传输对象
 * 严格遵循repowiki规范
 */
@Data
public abstract class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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