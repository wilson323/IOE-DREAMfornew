package net.lab1024.sa.base.module.biometric.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生物特征记录实体类
 * 用于记录生物特征的操作历史记录
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@TableName("t_biometric_record")
public class BiometricRecordEntity {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 人员编号
     */
    private String personCode;

    /**
     * 生物特征ID
     */
    private Long biometricId;

    /**
     * 生物特征类型
     * FINGERPRINT-指纹，FACE-人脸，IRIS-虹膜
     */
    private String biometricType;

    /**
     * 操作类型
     * CREATE-创建，UPDATE-更新，DELETE-删除，SYNC-同步
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作前数据（JSON格式）
     */
    private String beforeData;

    /**
     * 操作后数据（JSON格式）
     */
    private String afterData;

    /**
     * 操作用户ID
     */
    private Long operateUserId;

    /**
     * 操作用户姓名
     */
    private String operateUserName;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 结果状态
     * SUCCESS-成功，FAILED-失败
     */
    private String resultStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 批次号
     * 用于批量操作的关联
     */
    private String batchNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(resultStatus);
    }

    /**
     * 是否为同步操作
     */
    public boolean isSyncOperation() {
        return "SYNC".equals(operationType);
    }

    /**
     * 是否为删除操作
     */
    public boolean isDeleteOperation() {
        return "DELETE".equals(operationType);
    }

    /**
     * 是否为创建操作
     */
    public boolean isCreateOperation() {
        return "CREATE".equals(operationType);
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }
}