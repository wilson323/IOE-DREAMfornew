package net.lab1024.sa.attendance.decorator;

/**
 * 打卡执行器接口
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用装饰器模式实现打卡流程的动态增强
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IPunchExecutor {

    /**
     * 执行打卡
     *
     * @param request 打卡请求
     * @return 打卡结果
     */
    PunchResult execute(MobilePunchRequest request);

    /**
     * 打卡请求
     */
    class MobilePunchRequest {
        private Long userId;
        private String punchType;
        private Double latitude;
        private Double longitude;
        private String facePhoto;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getFacePhoto() {
            return facePhoto;
        }

        public void setFacePhoto(String facePhoto) {
            this.facePhoto = facePhoto;
        }
    }

    /**
     * 打卡结果
     */
    class PunchResult {
        private final boolean success;
        private final String message;
        private final Object data;

        private PunchResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static PunchResult success(Object data) {
            return new PunchResult(true, "打卡成功", data);
        }

        public static PunchResult failed(String message) {
            return new PunchResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
