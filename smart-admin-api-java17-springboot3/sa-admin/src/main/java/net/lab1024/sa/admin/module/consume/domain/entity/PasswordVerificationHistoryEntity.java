/*
 * 密码验证历史实体类
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 密码验证历史实体
 * 记录支付密码的验证尝试历史
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("t_password_verification_history")
public class PasswordVerificationHistoryEntity extends BaseEntity {

    /**
     * 历史记录ID
     */
    @TableId
    private Long historyId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户类型(0-管理员,1-员工,2-用户)
     */
    private Integer userType;

    /**
     * 验证结果(0-失败,1-成功)
     */
    @NotNull(message = "验证结果不能为空")
    private Integer verificationResult;

    /**
     * 验证方式(1-密码验证,2-指纹验证,3-人脸验证,4-短信验证)
     */
    private Integer verificationType;

    /**
     * 验证设备ID
     */
    private String deviceId;

    /**
     * 验证设备名称
     */
    private String deviceName;

    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 客户端User-Agent
     */
    private String userAgent;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 验证时间
     */
    private LocalDateTime verificationTime;

    /**
     * 验证地点
     */
    private String verificationLocation;

    /**
     * 风险等级(0-低风险,1-中风险,2-高风险)
     */
    private Integer riskLevel;

    /**
     * 备注
     */
    private String remark;
}
