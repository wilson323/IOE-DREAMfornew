package net.lab1024.sa.base.common.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人脸特征实体类
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_face_feature")
public class FaceFeatureEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 人脸特征ID
     */
    @TableId(type = IdType.AUTO)
    private Long featureId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 人员ID（如果识别成功）
     */
    private Long personId;

    /**
     * 人脸特征数据（JSON格式）
     */
    private String featureData;

    /**
     * 人脸图像路径
     */
    private String faceImagePath;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 检测时间
     */
    private LocalDateTime detectTime;

    /**
     * 处理状态（PENDING, PROCESSING, PROCESSED, FAILED）
     */
    private String processStatus;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 匹配结果（JSON格式）
     */
    private String matchResult;

    /**
     * 扩展信息（JSON格式）
     */
    private String extraInfo;
}
