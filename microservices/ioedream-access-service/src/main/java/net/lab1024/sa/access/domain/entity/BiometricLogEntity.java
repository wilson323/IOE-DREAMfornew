package net.lab1024.sa.access.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 生物识别操作日志实体类
 * <p>
 * 用于记录生物识别系统的操作日志，包括设备操作、用户认证、系统事件等
 * 用于监控、审计和统计分析
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_biometric_log")
public class BiometricLogEntity extends BaseEntity {

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备名称（冗余字段，便于查询）
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备编号（冗余字段，便于查询）
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名（冗余字段，便于查询）
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户编号（冗余字段，便于查询）
     */
    @TableField("user_code")
    private String userCode;

    /**
     * 生物特征类型 FACE-人脸, FINGERPRINT-指纹, PALMPRINT-掌纹, IRIS-虹膜
     */
    @TableField("biometric_type")
    private String biometricType;

    /**
     * 操作类型 1-注册, 2-验证, 3-搜索, 4-更新, 5-删除, 6-设备管理, 7-系统配置
     */
    @TableField("operation_type")
    private Integer operationType;

    /**
     * 操作结果 0-失败, 1-成功, 2-超时, 3-系统错误
     */
    @TableField("operation_result")
    private Integer operationResult;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 处理耗时（毫秒）
     */
    @TableField("processing_time")
    private Integer processingTime;

    /**
     * 置信度分数 0.0000-1.0000
     */
    @TableField("confidence_score")
    private java.math.BigDecimal confidenceScore;

    /**
     * 失败原因（操作失败时记录）
     */
    @TableField("failure_reason")
    private String failureReason;

    /**
     * 客户端IP地址
     */
    @TableField("client_ip")
    private String clientIp;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 业务类型 ACCESS-门禁, ATTENDANCE-考勤, PAYMENT-支付, AUTHENTICATION-身份认证
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务流水号
     */
    @TableField("business_flow_no")
    private String businessFlowNo;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField("extend_info")
    private String extendInfo;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;
}
