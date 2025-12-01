package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 人脸特征实体
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("video_face_feature")
public class FaceFeatureEntity {

    /**
     * 特征ID
     */
    @TableId(type = IdType.AUTO)
    private Long featureId;

    /**
     * 设备ID
     */
    @NotNull
    private Long deviceId;

    /**
     * 人员ID (如果已识别)
     */
    private String personId;

    /**
     * 人脸特征数据 (JSON格式)
     */
    @NotNull
    private String featureData;

    /**
     * 检测时间
     */
    @NotNull
    private LocalDateTime detectTime;

    /**
     * 置信度分数
     */
    private Double confidenceScore;

    /**
     * 处理状态
     * PENDING - 待处理
     * PROCESSED - 已处理
     * FAILED - 处理失败
     */
    @NotNull
    private String processStatus;

    /**
     * 图像路径
     */
    private String imagePath;

    /**
     * 特征向量 (Base64编码)
     */
    private String featureVector;

    /**
     * 年龄估计
     */
    private Integer estimatedAge;

    /**
     * 性别估计
     */
    private String estimatedGender;

    /**
     * 情绪识别结果
     */
    private String emotionResult;

    /**
     * 面部分析数据 (JSON格式)
     */
    private String faceAnalysisData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}