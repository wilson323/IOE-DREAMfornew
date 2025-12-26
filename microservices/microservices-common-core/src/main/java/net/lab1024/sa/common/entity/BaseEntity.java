package net.lab1024.sa.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础实体类
 * <p>
 * 提供通用审计字段与逻辑删除标识
 * </p>
 */
@Data
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUserId;
    private Long updateUserId;
    private Integer deletedFlag;
    private Integer version;
    private Integer deleted;
}
