package net.lab1024.sa.common.entity.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 数据库版本实体
 * <p>
 * 存储数据库版本管理信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 4个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("database_version")
@Schema(description = "数据库版本实体")
public class DatabaseVersionEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 数据库名称
     */
    @Schema(description = "数据库名称")
    private String dbName;

    /**
     * 数据库版本号
     */
    @Schema(description = "数据库版本号")
    private String dbVersion;

    /**
     * 同步状态
     */
    @Schema(description = "同步状态")
    private String status;

    /**
     * 描述信息
     */
    @Schema(description = "描述信息")
    private String description;
}
