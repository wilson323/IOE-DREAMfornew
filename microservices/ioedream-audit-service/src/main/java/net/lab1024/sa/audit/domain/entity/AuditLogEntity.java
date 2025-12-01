package net.lab1024.sa.audit.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 * <p>
 * 用于记录系统中的所有操作审计日志
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的审计字段
 * - 支持多种操作类型和结果状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@Accessors(chain = true)
public class AuditLogEntity {

    /**
     * 审计日志ID
     */
    private Long auditId;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型：1-登录 2-登出 3-增 4-删 5-改 6-查 7-导出 8-导入 9-配置 10-其他
     */
    private Integer operationType;

    /**
     * 操作模块
     */
    private String moduleName;

    /**
     * 操作功能
     */
    private String functionName;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseData;

    /**
     * 操作结果：1-成功 2-失败 3-异常
     */
    private Integer resultStatus;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作前后数据对比
     */
    private String dataChange;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 风险等级：1-低 2-中 3-高
     */
    private Integer riskLevel;

    /**
     * 审计标签
     */
    private String auditTags;

    /**
     * 扩展属性
     */
    private String extensions;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 获取操作类型描述
     *
     * @return 操作类型描述
     */
    public String getOperationTypeText() {
        if (operationType == null) {
            return "未知";
        }
        switch (operationType) {
            case 1:
                return "登录";
            case 2:
                return "登出";
            case 3:
                return "新增";
            case 4:
                return "删除";
            case 5:
                return "修改";
            case 6:
                return "查询";
            case 7:
                return "导出";
            case 8:
                return "导入";
            case 9:
                return "配置";
            case 10:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取操作结果描述
     *
     * @return 操作结果描述
     */
    public String getResultStatusText() {
        if (resultStatus == null) {
            return "未知";
        }
        switch (resultStatus) {
            case 1:
                return "成功";
            case 2:
                return "失败";
            case 3:
                return "异常";
            default:
                return "未知";
        }
    }

    /**
     * 获取风险等级描述
     *
     * @return 风险等级描述
     */
    public String getRiskLevelText() {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case 1:
                return "低风险";
            case 2:
                return "中风险";
            case 3:
                return "高风险";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否为成功操作
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(1).equals(resultStatus);
    }

    /**
     * 判断是否为失败操作
     *
     * @return 是否失败
     */
    public boolean isFailure() {
        return Integer.valueOf(2).equals(resultStatus);
    }

    /**
     * 判断是否为异常操作
     *
     * @return 是否异常
     */
    public boolean isException() {
        return Integer.valueOf(3).equals(resultStatus);
    }

    /**
     * 判断是否为高风险操作
     *
     * @return 是否高风险
     */
    public boolean isHighRisk() {
        return Integer.valueOf(3).equals(riskLevel);
    }

    /**
     * 判断是否为登录相关操作
     *
     * @return 是否为登录操作
     */
    public boolean isLoginOperation() {
        return Integer.valueOf(1).equals(operationType) ||
               Integer.valueOf(2).equals(operationType);
    }

    /**
     * 判断是否为数据修改操作
     *
     * @return 是否为数据修改操作
     */
    public boolean isDataModificationOperation() {
        return Integer.valueOf(3).equals(operationType) || // 新增
               Integer.valueOf(4).equals(operationType) || // 删除
               Integer.valueOf(5).equals(operationType);    // 修改
    }
}