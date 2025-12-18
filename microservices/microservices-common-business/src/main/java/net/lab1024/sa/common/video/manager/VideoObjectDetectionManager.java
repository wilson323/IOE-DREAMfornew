package net.lab1024.sa.common.video.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.video.entity.VideoObjectDetectionEntity;
import net.lab1024.sa.common.video.dao.VideoObjectDetectionDao;
import net.lab1024.sa.common.util.SmartBeanUtil;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频目标检测管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager类处理复杂业务流程编排
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 目标检测数据的业务逻辑处理
 * - 目标识别和属性解析
 * - 告警级别判断和处理
 * - 图像坐标计算和转换
 * - 检测结果验证和分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class VideoObjectDetectionManager {

    private final VideoObjectDetectionDao videoObjectDetectionDao;

    /**
     * ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 避免每次调用getter方法时创建新实例，提升性能
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 构造函数
     *
     * @param videoObjectDetectionDao 视频目标检测DAO
     */
    public VideoObjectDetectionManager(VideoObjectDetectionDao videoObjectDetectionDao) {
        this.videoObjectDetectionDao = videoObjectDetectionDao;
    }

    /**
     * 计算目标面积
     * <p>
     * 根据边界框坐标计算目标的像素面积
     * </p>
     *
     * @param detection 检测记录实体
     * @return 目标面积
     */
    public BigDecimal calculateObjectArea(VideoObjectDetectionEntity detection) {
        if (detection == null || !StringUtils.hasText(detection.getBoundingBox())) {
            return BigDecimal.ZERO;
        }

        try {
            // 解析边界框坐标JSON
            Map<String, Object> boundingBox = OBJECT_MAPPER.readValue(
                    detection.getBoundingBox(),
                    new TypeReference<Map<String, Object>>() {
                    });

            BigDecimal width = new BigDecimal(boundingBox.get("width").toString());
            BigDecimal height = new BigDecimal(boundingBox.get("height").toString());

            return width.multiply(height).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.debug("[视频目标检测] 计算目标面积失败: error={}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算相对大小（占画面比例）
     * <p>
     * 根据目标面积和画面尺寸计算相对大小比例
     * 假设画面分辨率为1920x1080
     * </p>
     *
     * @param detection 检测记录实体
     * @param frameWidth 画面宽度
     * @param frameHeight 画面高度
     * @return 相对大小比例（0-1之间）
     */
    public BigDecimal calculateRelativeSize(VideoObjectDetectionEntity detection, Integer frameWidth, Integer frameHeight) {
        if (detection == null || frameWidth == null || frameHeight == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal objectArea = calculateObjectArea(detection);
        BigDecimal frameArea = new BigDecimal(frameWidth).multiply(new BigDecimal(frameHeight));

        if (frameArea.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return objectArea.divide(frameArea, 6, RoundingMode.HALF_UP);
    }

    /**
     * 计算中心点坐标
     * <p>
     * 根据边界框坐标计算目标的中心点坐标
     * </p>
     *
     * @param detection 检测记录实体
     * @return 中心点坐标 [x, y]
     */
    public BigDecimal[] calculateCenterPoint(VideoObjectDetectionEntity detection) {
        if (detection == null || !StringUtils.hasText(detection.getBoundingBox())) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }

        try {
            Map<String, Object> boundingBox = OBJECT_MAPPER.readValue(
                    detection.getBoundingBox(),
                    new TypeReference<Map<String, Object>>() {
                    });

            BigDecimal x = new BigDecimal(boundingBox.get("x").toString());
            BigDecimal y = new BigDecimal(boundingBox.get("y").toString());
            BigDecimal width = new BigDecimal(boundingBox.get("width").toString());
            BigDecimal height = new BigDecimal(boundingBox.get("height").toString());

            BigDecimal centerX = x.add(width.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            BigDecimal centerY = y.add(height.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));

            return new BigDecimal[]{centerX, centerY};
        } catch (Exception e) {
            log.debug("[视频目标检测] 计算中心点坐标失败: error={}", e.getMessage());
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }
    }

    /**
     * 判断是否为高置信度检测
     * <p>
     * 根据置信度阈值判断检测结果是否可信
     * </p>
     *
     * @param detection 检测记录实体
     * @param threshold 置信度阈值
     * @return 是否为高置信度
     */
    public boolean isHighConfidence(VideoObjectDetectionEntity detection, BigDecimal threshold) {
        if (detection == null || detection.getConfidenceScore() == null) {
            return false;
        }

        BigDecimal confidence = detection.getConfidenceScore();
        return confidence.compareTo(threshold) >= 0;
    }

    /**
     * 判断是否需要触发告警
     * <p>
     * 基于告警级别和触发条件判断是否需要告警
     * </p>
     *
     * @param detection 检测记录实体
     * @return 是否需要触发告警
     */
    public boolean shouldTriggerAlert(VideoObjectDetectionEntity detection) {
        if (detection == null) {
            return false;
        }

        // 检查告警级别
        if (detection.getAlertLevel() == null || detection.getAlertLevel() < VideoObjectDetectionEntity.AlertLevel.WARNING) {
            return false;
        }

        // 检查是否已经触发告警
        if (Boolean.TRUE.equals(detection.getAlertTriggered())) {
            return false;
        }

        // 检查置信度
        if (!isHighConfidence(detection, BigDecimal.valueOf(0.7))) {
            return false;
        }

        return true;
    }

    /**
     * 获取目标类型描述
     *
     * @param objectType 目标类型
     * @return 类型描述
     */
    public String getObjectTypeDesc(Integer objectType) {
        if (objectType == null) {
            return "未知";
        }

        switch (objectType) {
            case VideoObjectDetectionEntity.ObjectType.PERSON:
                return "人员";
            case VideoObjectDetectionEntity.ObjectType.VEHICLE:
                return "车辆";
            case VideoObjectDetectionEntity.ObjectType.OBJECT:
                return "物体";
            case VideoObjectDetectionEntity.ObjectType.ANIMAL:
                return "动物";
            case VideoObjectDetectionEntity.ObjectType.FACE:
                return "人脸";
            case VideoObjectDetectionEntity.ObjectType.LICENSE_PLATE:
                return "车牌";
            case VideoObjectDetectionEntity.ObjectType.LUGGAGE:
                return "行李";
            case VideoObjectDetectionEntity.ObjectType.DANGEROUS_GOODS:
                return "危险品";
            case VideoObjectDetectionEntity.ObjectType.OTHER:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取告警级别描述
     *
     * @param alertLevel 告警级别
     * @return 级别描述
     */
    public String getAlertLevelDesc(Integer alertLevel) {
        if (alertLevel == null) {
            return "未知";
        }

        switch (alertLevel) {
            case VideoObjectDetectionEntity.AlertLevel.INFO:
                return "信息";
            case VideoObjectDetectionEntity.AlertLevel.NOTICE:
                return "提醒";
            case VideoObjectDetectionEntity.AlertLevel.WARNING:
                return "警告";
            case VideoObjectDetectionEntity.AlertLevel.CRITICAL:
                return "严重";
            case VideoObjectDetectionEntity.AlertLevel.URGENT:
                return "紧急";
            default:
                return "未知";
        }
    }

    /**
     * 获取处理状态描述
     *
     * @param processStatus 处理状态
     * @return 状态描述
     */
    public String getProcessStatusDesc(Integer processStatus) {
        if (processStatus == null) {
            return "未知";
        }

        switch (processStatus) {
            case VideoObjectDetectionEntity.ProcessStatus.PENDING:
                return "未处理";
            case VideoObjectDetectionEntity.ProcessStatus.PROCESSING:
                return "处理中";
            case VideoObjectDetectionEntity.ProcessStatus.PROCESSED:
                return "已处理";
            case VideoObjectDetectionEntity.ProcessStatus.IGNORED:
                return "已忽略";
            default:
                return "未知";
        }
    }

    /**
     * 获取验证结果描述
     *
     * @param verificationResult 验证结果
     * @return 结果描述
     */
    public String getVerificationResultDesc(Integer verificationResult) {
        if (verificationResult == null) {
            return "未知";
        }

        switch (verificationResult) {
            case VideoObjectDetectionEntity.VerificationResult.UNVERIFIED:
                return "未验证";
            case VideoObjectDetectionEntity.VerificationResult.CORRECT:
                return "正确";
            case VideoObjectDetectionEntity.VerificationResult.ERROR:
                return "错误";
            case VideoObjectDetectionEntity.VerificationResult.UNCERTAIN:
                return "不确定";
            default:
                return "未知";
        }
    }

    /**
     * 解析目标属性
     * <p>
     * 从JSON格式的属性字符串中解析目标属性
     * </p>
     *
     * @param detection 检测记录实体
     * @return 目标属性Map
     */
    public Map<String, Object> parseObjectAttributes(VideoObjectDetectionEntity detection) {
        if (detection == null || !StringUtils.hasText(detection.getExtendedAttributes())) {
            return new HashMap<>();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    detection.getExtendedAttributes(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            log.debug("[视频目标检测] 解析目标属性失败: error={}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 验证检测结果完整性
     *
     * @param detection 检测记录实体
     * @return 验证结果
     */
    public boolean validateDetection(VideoObjectDetectionEntity detection) {
        if (detection == null) {
            return false;
        }

        // 必填字段验证
        if (detection.getDeviceId() == null) {
            log.warn("[视频目标检测] 设备ID不能为空");
            return false;
        }

        if (!StringUtils.hasText(detection.getDeviceCode())) {
            log.warn("[视频目标检测] 设备编码不能为空");
            return false;
        }

        if (detection.getDetectionTime() == null) {
            log.warn("[视频目标检测] 检测时间不能为空");
            return false;
        }

        if (detection.getObjectType() == null) {
            log.warn("[视频目标检测] 目标类型不能为空");
            return false;
        }

        // 置信度验证
        if (detection.getConfidenceScore() != null) {
            if (detection.getConfidenceScore().compareTo(BigDecimal.ZERO) < 0 ||
                detection.getConfidenceScore().compareTo(BigDecimal.ONE) > 0) {
                log.warn("[视频目标检测] 置信度必须在0-1之间: {}", detection.getConfidenceScore());
                return false;
            }
        }

        return true;
    }

    /**
     * 批量检查高置信度检测结果
     *
     * @param detections 检测记录列表
     * @param threshold 置信度阈值
     * @return 高置信度记录数量
     */
    public int countHighConfidenceDetections(List<VideoObjectDetectionEntity> detections, BigDecimal threshold) {
        if (detections == null || detections.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (VideoObjectDetectionEntity detection : detections) {
            if (isHighConfidence(detection, threshold)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 更新告警触发状态
     *
     * @param detection 检测记录实体
     * @param triggered 是否触发告警
     */
    public void updateAlertTriggered(VideoObjectDetectionEntity detection, boolean triggered) {
        if (detection == null) {
            return;
        }

        detection.setAlertTriggered(triggered ? 1 : 0);

        // 更新到数据库
        try {
            videoObjectDetectionDao.updateById(detection);
        } catch (Exception e) {
            log.error("[视频目标检测] 更新告警触发状态失败: id={}, error={}",
                    detection.getDetectionId(), e.getMessage());
        }
    }

    /**
     * 更新处理状态
     *
     * @param detection 检测记录实体
     * @param processStatus 处理状态
     * @param verifiedBy 验证人员ID
     */
    public void updateProcessStatus(VideoObjectDetectionEntity detection, Integer processStatus, Long verifiedBy) {
        if (detection == null) {
            return;
        }

        detection.setProcessStatus(processStatus);
        detection.setVerifiedBy(verifiedBy);
        detection.setVerificationTime(LocalDateTime.now());

        // 更新到数据库
        try {
            videoObjectDetectionDao.updateById(detection);
        } catch (Exception e) {
            log.error("[视频目标检测] 更新处理状态失败: id={}, status={}, error={}",
                    detection.getDetectionId(), processStatus, e.getMessage());
        }
    }
}