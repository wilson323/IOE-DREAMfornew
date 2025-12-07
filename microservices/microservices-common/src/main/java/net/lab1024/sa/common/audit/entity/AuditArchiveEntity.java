package net.lab1024.sa.common.audit.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审计归档记录实体
 * <p>
 * 用于记录审计日志归档操作，支持归档记录查询和追溯
 * 符合企业级审计归档管理要求
 * </p>
 * <p>
 * 业务场景：
 * - 定期归档历史审计日志
 * - 记录归档操作信息
 * - 支持归档记录查询
 * - 满足合规性要求
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_audit_archive")
public class AuditArchiveEntity extends BaseEntity {

    /**
     * 归档记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long archiveId;

    /**
     * 归档编号（系统唯一）
     */
    @NotBlank(message = "归档编号不能为空")
    @Size(max = 64, message = "归档编号长度不能超过64个字符")
    private String archiveCode;

    /**
     * 归档时间点（归档此时间点之前的日志）
     */
    @NotNull(message = "归档时间点不能为空")
    private LocalDateTime archiveTimePoint;

    /**
     * 归档日志数量
     */
    @NotNull(message = "归档日志数量不能为空")
    private Integer archiveCount;

    /**
     * 归档文件路径
     */
    @NotBlank(message = "归档文件路径不能为空")
    @Size(max = 500, message = "归档文件路径长度不能超过500个字符")
    private String archiveFilePath;

    /**
     * 归档文件大小（字节）
     */
    private Long archiveFileSize;

    /**
     * 归档文件格式（ZIP/CSV/EXCEL等）
     */
    @Size(max = 20, message = "归档文件格式长度不能超过20个字符")
    private String archiveFileFormat;

    /**
     * 归档状态（1-进行中 2-成功 3-失败）
     */
    private Integer archiveStatus;

    /**
     * 归档状态描述
     */
    @Size(max = 200, message = "归档状态描述长度不能超过200个字符")
    private String archiveStatusDesc;

    /**
     * 归档开始时间
     */
    private LocalDateTime archiveStartTime;

    /**
     * 归档结束时间
     */
    private LocalDateTime archiveEndTime;

    /**
     * 归档耗时（毫秒）
     */
    private Long archiveDuration;

    /**
     * 归档操作人ID
     */
    private Long archiveUserId;

    /**
     * 归档操作人姓名
     */
    @Size(max = 100, message = "归档操作人姓名长度不能超过100个字符")
    private String archiveUserName;

    /**
     * 归档备注
     */
    @Size(max = 500, message = "归档备注长度不能超过500个字符")
    private String archiveRemark;

    /**
     * 归档扩展信息（JSON格式）
     */
    private String archiveExtensions;
}
