package net.lab1024.sa.video.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 人脸识别检测响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人脸识别检测响应")
public class FaceDetectionResponse {

    @Schema(description = "检测任务ID", example = "detect_001")
    private String detectionTaskId;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "检测状态", example = "completed", allowableValues = {"processing", "completed", "failed"})
    private String detectionStatus;

    @Schema(description = "检测状态名称", example = "检测完成")
    private String detectionStatusName;

    @Schema(description = "检测到的人脸数量", example = "3")
    private Integer faceCount;

    @Schema(description = "检测到的人脸列表")
    private List<FaceInfo> detectedFaces;

    @Schema(description = "处理时长(毫秒)", example = "1500")
    private Long processingTime;

    @Schema(description = "置信度", example = "0.95")
    private Double confidence;

    @Schema(description = "检测时间", example = "2025-12-16T10:30:00")
    private LocalDateTime detectionTime;

    @Schema(description = "图像URL", example = "https://example.com/detection_image.jpg")
    private String imageUrl;

    @Schema(description = "扩展信息")
    private Map<String, Object> extendedInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "人脸信息")
    public static class FaceInfo {

        @Schema(description = "人脸ID", example = "face_001")
        private String faceId;

        @Schema(description = "人脸框坐标", example = "100,120,80,100")
        private String faceRect;

        @Schema(description = "X坐标", example = "100")
        private Integer x;

        @Schema(description = "Y坐标", example = "120")
        private Integer y;

        @Schema(description = "宽度", example = "80")
        private Integer width;

        @Schema(description = "高度", example = "100")
        private Integer height;

        @Schema(description = "置信度", example = "0.98")
        private Double confidence;

        @Schema(description = "年龄", example = "25")
        private Integer age;

        @Schema(description = "性别", example = "male", allowableValues = {"male", "female", "unknown"})
        private String gender;

        @Schema(description = "性别名称", example = "男")
        private String genderName;

        @Schema(description = "是否佩戴口罩", example = "false")
        private Boolean wearingMask;

        @Schema(description = "是否佩戴眼镜", example = "true")
        private Boolean wearingGlasses;

        @Schema(description = "表情", example = "neutral", allowableValues = {"neutral", "happy", "sad", "angry", "surprised"})
        private String expression;

        @Schema(description = "表情名称", example = "平静")
        private String expressionName;

        @Schema(description = "人脸质量", example = "good", allowableValues = {"excellent", "good", "fair", "poor"})
        private String faceQuality;

        @Schema(description = "人脸质量名称", example = "良好")
        private String faceQualityName;

        @Schema(description = "匹配的用户信息（如果识别成功）")
        private MatchedUserInfo matchedUser;

        @Schema(description = "人脸特征数据", example = "")
        private String faceFeatures;

        @Schema(description = "人脸图像URL", example = "https://example.com/face_image.jpg")
        private String faceImageUrl;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "匹配的用户信息")
        public static class MatchedUserInfo {

            @Schema(description = "用户ID", example = "1001")
            private Long userId;

            @Schema(description = "用户名", example = "john_doe")
            private String username;

            @Schema(description = "真实姓名", example = "张三")
            private String realName;

            @Schema(description = "员工编号", example = "EMP001")
            private String employeeNumber;

            @Schema(description = "部门名称", example = "技术部")
            private String departmentName;

            @Schema(description = "匹配相似度", example = "0.96")
            private Double similarity;

            @Schema(description = "用户头像URL", example = "https://example.com/user_avatar.jpg")
            private String userAvatarUrl;

            @Schema(description = "权限信息")
            private List<String> permissions;
        }
    }
}