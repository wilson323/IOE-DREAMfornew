package net.lab1024.sa.common.entity.visitor;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客生物识别信息实体类
 * <p>
 * 存储访客的生物识别信息，包括人脸照片、人脸特征、身份证照片
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 4个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_biometric")
@Schema(description = "访客生物识别信息实体")
public class VisitorBiometricEntity extends BaseEntity {

    /**
     * 生物识别ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "生物识别ID")
    private Long biometricId;

    /**
     * 关联的自助登记ID（外键）
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 访客人脸照片URL
     */
    @TableField("face_photo_url")
    @Schema(description = "人脸照片URL")
    private String facePhotoUrl;

    /**
     * 人脸特征向量（Base64编码）
     */
    @TableField("face_feature")
    @Schema(description = "人脸特征向量")
    private String faceFeature;

    /**
     * 身份证照片URL
     */
    @TableField("id_card_photo_url")
    @Schema(description = "身份证照片URL")
    private String idCardPhotoUrl;
}
