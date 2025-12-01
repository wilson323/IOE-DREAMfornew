package net.lab1024.sa.admin.module.video.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 人脸特征实体（占位实现）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_face_feature")
public class FaceFeatureEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long featureId;

    private Long deviceId;

    private Long personId;

    private String faceImage;

    private String featureData;

    private Double confidence;

    private String processStatus;

    private LocalDateTime captureTime;

    private String imageData;

    private String faceRectangle;

    private String gender;

    private Integer age;

    private String remark;
}