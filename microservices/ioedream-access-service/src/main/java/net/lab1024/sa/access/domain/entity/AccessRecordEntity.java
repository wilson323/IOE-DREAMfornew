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
 * 门禁记录实体
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_record")
public class AccessRecordEntity extends BaseEntity {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("user_id")
    private Long userId;

    @TableField("card_no")
    private String cardNo;

    @TableField("access_type")
    private String accessType;

    @TableField("access_result")
    private String accessResult;

    @TableField("access_time")
    private LocalDateTime accessTime;

    @TableField("access_direction")
    private String accessDirection;

    @TableField("verification_mode")
    private String verificationMode;

    @TableField("temperature")
    private Double temperature;

    @TableField("photo_url")
    private String photoUrl;

    @TableField("fail_reason")
    private String failReason;
}
