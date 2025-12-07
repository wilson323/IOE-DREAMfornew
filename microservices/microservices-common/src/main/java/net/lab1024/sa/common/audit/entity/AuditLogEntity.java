package net.lab1024.sa.common.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审计日志实体
 * <p>
 * 用于记录系统操作审计日志，支持完整的操作追踪和合规性审计
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 完整的审计日志字段
 * </p>
 * <p>
 * 业务场景：
 * - 用户操作记录
 * - 系统操作追踪
 * - 合规性审计
 * - 安全事件分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_audit_log")
public class AuditLogEntity extends BaseEntity {

    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作类型
     * <p>
     * 1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 7-登录 8-登出
     * </p>
     */
    private Integer operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 请求方法（GET、POST、PUT、DELETE等）
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（JSON格式）
     */
    private String requestParams;

    /**
     * 响应数据（JSON格式）
     */
    private String responseData;

    /**
     * 结果状态
     * <p>
     * 1-成功 2-失败 3-异常
     * </p>
     */
    private Integer resultStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 风险等级
     * <p>
     * 1-低 2-中 3-高
     * </p>
     */
    private Integer riskLevel;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 追踪ID（用于分布式追踪）
     */
    private String traceId;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
}
