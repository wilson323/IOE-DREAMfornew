package net.lab1024.sa.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 数据库版本实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("database_version")
public class DatabaseVersionEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 数据库版本号
     */
    private String dbVersion;

    /**
     * 同步状态
     */
    private String status;

    /**
     * 描述信息
     */
    private String description;
}
