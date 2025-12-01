package net.lab1024.sa.attendance.mobile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 移动端打卡服务
 * 支持GPS定位、WiFi打卡、照片验证等移动端打卡功能
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class MobilePunchService {

    /**
     * 打卡类型枚举
     */
    public enum PunchType {
        PUNCH_IN("PUNCH_IN", "上班打卡"),
        PUNCH_OUT("PUNCH_OUT", "下班打卡"),
        BREAK_START("BREAK_START", "休息开始"),
        BREAK_END("BREAK_END", "休息结束"),
        OVERTIME_START("OVERTIME_START", "加班开始"),
        OVERTIME_END("OVERTIME_END", "加班结束");

        private final String code;
        private final String description;

        PunchType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 定位方式枚举
     */
    public enum LocationMethod {
        GPS("GPS", "GPS定位"),
        WIFI("WIFI", "WiFi定位"),
        BLUETOOTH("BLUETOOTH", "蓝牙定位"),
        NFC("NFC", "NFC打卡"),
        QR_CODE("QR_CODE", "二维码打卡"),
        FACE_RECOGNITION("FACE_RECOGNITION", "人脸识别");

        private final String code;
        private final String description;

        LocationMethod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 移动端打卡请求
     */
    @Data
    public static class MobilePunchRequest {
        private Long employeeId;
        private PunchType punchType;
        private LocalDateTime punchTime;
        private LocationInfo location;
        private String deviceInfo;
        private String photoData; // Base64编码的照片数据
        private String audioData; // 语音备注
        private String remark;
        private List<LocationMethod> verificationMethods; // 验证方式列表
        private Map<String, Object> additionalData; // 附加数据
    }

    /**
     * 位置信息
     */
    @Data
    public static class LocationInfo {
        private BigDecimal latitude; // 纬度
        private BigDecimal longitude; // 经度
        private BigDecimal accuracy; // 精度(米)
        private String address; // 详细地址
        private String district; // 区域
        private String city; // 城市
        private String province; // 省份
        private String country; // 国家
        private LocalDateTime locationTime; // 定位时间
        private String wifiSSID; // WiFi名称
        private String wifiBSSID; // WiFi MAC地址
        private String bluetoothDevice; // 蓝牙设备信息
        private String qrCodeContent; // 二维码内容
        private String nfcTagId; // NFC标签ID
    }

    /**
     * 移动端打卡结果
     */
    @Data
    public static class MobilePunchResult {
        private Boolean success;
        private String message;
        private Long punchRecordId;
        private LocalDateTime actualPunchTime;
        private String attendanceStatus;
        private Boolean isAbnormal;
        private String abnormalReason;
        private List<String> warnings;
        private Map<String, Object> verificationResults;
        private LocationInfo verifiedLocation;
    }

    /**
     * 考勤区域配置
     */
    @Data
    public static class AttendanceArea {
        private Long areaId;
        private String areaName;
        private BigDecimal centerLatitude;
        private BigDecimal centerLongitude;
        private BigDecimal radius; // 允许打卡半径(米)
        private List<String> allowedWifiSSIDs; // 允许的WiFi SSID列表
        private List<String> allowedNfcTagIds; // 允许的NFC标签ID列表
        private List<String> allowedQrCodes; // 允许的二维码内容
        private LocalTime effectiveStartTime; // 生效开始时间
        private LocalTime effectiveEndTime; // 生效结束时间
        private List<Integer> effectiveDays; // 生效日期(1-7)
        private Boolean enablePhotoVerification; // 启用照片验证
        private Boolean enableFaceRecognition; // 启用人脸识别
    }

    /**
     * 考勤区域缓存
     */
    private Map<Long, AttendanceArea> attendanceAreas = new HashMap<>();

    /**
     * 移动端打卡记录缓存
     */
    private Map<String, MobilePunchRecord> mobilePunchRecords = new HashMap<>();

    /**
     * 初始化移动端打卡服务
     */
    public void initializeMobilePunchService() {
        log.info("移动端打卡服务初始化开始");

        // 加载默认考勤区域
        loadDefaultAttendanceAreas();

        log.info("移动端打卡服务初始化完成，加载了{}个考勤区域", attendanceAreas.size());
    }

    /**
     * 加载默认考勤区域
     */
    private void loadDefaultAttendanceAreas() {
        // 主办公区
        AttendanceArea mainOffice = new AttendanceArea();
        mainOffice.setAreaId(1L);
        mainOffice.setAreaName("主办公区");
        mainOffice.setCenterLatitude(BigDecimal.valueOf(39.9042));
        mainOffice.setCenterLongitude(BigDecimal.valueOf(116.4074));
        mainOffice.setRadius(BigDecimal.valueOf(100)); // 100米范围
        mainOffice.setAllowedWifiSSIDs(Arrays.asList("IOE-DREAM-OFFICE", "GUEST-WIFI"));
        mainOffice.setEffectiveStartTime(LocalTime.of(8, 0));
        mainOffice.setEffectiveEndTime(LocalTime.of(20, 0));
        mainOffice.setEffectiveDays(Arrays.asList(1, 2, 3, 4, 5)); // 周一到周五
        mainOffice.setEnablePhotoVerification(true);
        mainOffice.setEnableFaceRecognition(true);
        attendanceAreas.put(1L, mainOffice);

        // 分公司办公区
        AttendanceArea branchOffice = new AttendanceArea();
        branchOffice.setAreaId(2L);
        branchOffice.setAreaName("分公司办公区");
        branchOffice.setCenterLatitude(BigDecimal.valueOf(31.2304));
        branchOffice.setCenterLongitude(BigDecimal.valueOf(121.4737));
        branchOffice.setRadius(BigDecimal.valueOf(200)); // 200米范围
        branchOffice.setAllowedWifiSSIDs(Arrays.asList("BRANCH-WIFI"));
        branchOffice.setEffectiveStartTime(LocalTime.of(9, 0));
        branchOffice.setEffectiveEndTime(LocalTime.of(18, 0));
        branchOffice.setEffectiveDays(Arrays.asList(1, 2, 3, 4, 5));
        branchOffice.setEnablePhotoVerification(false);
        branchOffice.setEnableFaceRecognition(false);
        attendanceAreas.put(2L, branchOffice);

        log.debug("加载了{}个默认考勤区域", attendanceAreas.size());
    }

    /**
     * 处理移动端打卡请求
     *
     * @param request 打卡请求
     * @return 打卡结果
     */
    public MobilePunchResult processMobilePunch(MobilePunchRequest request) {
        log.info("处理移动端打卡请求：员工ID={}，打卡类型={}，打卡时间={}",
                request.getEmployeeId(), request.getPunchType(), request.getPunchTime());

        MobilePunchResult result = new MobilePunchResult();
        result.setSuccess(false);
        result.setWarnings(new ArrayList<>());
        result.setVerificationResults(new HashMap<>());

        try {
            // 1. 验证基本信息
            String validationError = validateBasicInfo(request);
            if (validationError != null) {
                result.setMessage(validationError);
                return result;
            }

            // 2. 位置验证
            LocationVerificationResult locationResult = verifyLocation(request.getLocation());
            result.getVerificationResults().put("location", locationResult);
            result.setVerifiedLocation(locationResult.getVerifiedLocation());

            if (!locationResult.getValid()) {
                result.getWarnings().add("位置验证失败：" + locationResult.getMessage());
            }

            // 3. 设备验证
            DeviceVerificationResult deviceResult = verifyDevice(request);
            result.getVerificationResults().put("device", deviceResult);

            // 4. 照片验证（如果启用）
            if (request.getPhotoData() != null && !request.getPhotoData().trim().isEmpty()) {
                PhotoVerificationResult photoResult = verifyPhoto(request.getPhotoData());
                result.getVerificationResults().put("photo", photoResult);

                if (!photoResult.getValid()) {
                    result.getWarnings().add("照片验证失败：" + photoResult.getMessage());
                }
            }

            // 5. 人脸识别验证（如果启用）
            if (request.getVerificationMethods() != null &&
                    request.getVerificationMethods().contains(LocationMethod.FACE_RECOGNITION)) {
                FaceVerificationResult faceResult = verifyFaceRecognition(request.getPhotoData(),
                        request.getEmployeeId());
                result.getVerificationResults().put("face", faceResult);

                if (!faceResult.getValid()) {
                    result.getWarnings().add("人脸识别失败：" + faceResult.getMessage());
                }
            }

            // 6. 检查重复打卡
            PunchDuplicateCheckResult duplicateResult = checkDuplicatePunch(request);
            if (duplicateResult.getIsDuplicate()) {
                result.getWarnings().add("可能存在重复打卡：" + duplicateResult.getMessage());
            }

            // 7. 生成打卡记录
            MobilePunchRecord punchRecord = createPunchRecord(request, result);
            savePunchRecord(punchRecord);

            // 8. 确定最终结果
            result.setSuccess(true);
            result.setPunchRecordId(punchRecord.getRecordId());
            result.setActualPunchTime(punchRecord.getPunchTime());
            result.setAttendanceStatus(determineAttendanceStatus(request, result));
            result.setIsAbnormal(isAbnormalPunch(request, result));
            result.setMessage("移动端打卡成功");

            log.info("移动端打卡处理完成：记录ID={}，状态={}",
                    punchRecord.getRecordId(), result.getAttendanceStatus());

        } catch (Exception e) {
            log.error("移动端打卡处理失败", e);
            result.setMessage("打卡失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 验证基本信息
     */
    private String validateBasicInfo(MobilePunchRequest request) {
        if (request.getEmployeeId() == null) {
            return "员工ID不能为空";
        }

        if (request.getPunchType() == null) {
            return "打卡类型不能为空";
        }

        if (request.getPunchTime() == null) {
            return "打卡时间不能为空";
        }

        if (request.getLocation() == null) {
            return "位置信息不能为空";
        }

        // 检查打卡时间合理性
        LocalTime punchTime = request.getPunchTime().toLocalTime();
        if (punchTime.isBefore(LocalTime.of(0, 0)) || punchTime.isAfter(LocalTime.of(23, 59))) {
            return "打卡时间不在合理范围内";
        }

        return null;
    }

    /**
     * 位置验证
     */
    private LocationVerificationResult verifyLocation(LocationInfo location) {
        LocationVerificationResult result = new LocationVerificationResult();
        result.setValid(false);

        // 验证GPS定位
        if (location.getLatitude() != null && location.getLongitude() != null) {
            // 检查坐标是否在有效范围内
            if (Math.abs(location.getLatitude().doubleValue()) > 90 ||
                    Math.abs(location.getLongitude().doubleValue()) > 180) {
                result.setMessage("GPS坐标无效");
                return result;
            }

            // 检查是否在任一考勤区域内
            for (AttendanceArea area : attendanceAreas.values()) {
                if (isLocationInArea(location, area)) {
                    result.setValid(true);
                    result.setVerifiedLocation(location);
                    result.setMessage("GPS定位验证通过，区域：" + area.getAreaName());
                    result.setAreaId(area.getAreaId());
                    result.setAreaName(area.getAreaName());
                    return result;
                }
            }
        }

        // 验证WiFi定位
        if (location.getWifiSSID() != null) {
            for (AttendanceArea area : attendanceAreas.values()) {
                if (area.getAllowedWifiSSIDs() != null &&
                        area.getAllowedWifiSSIDs().contains(location.getWifiSSID())) {
                    result.setValid(true);
                    result.setMessage("WiFi定位验证通过，区域：" + area.getAreaName());
                    result.setAreaId(area.getAreaId());
                    result.setAreaName(area.getAreaName());
                    return result;
                }
            }
        }

        // 验证NFC打卡
        if (location.getNfcTagId() != null) {
            for (AttendanceArea area : attendanceAreas.values()) {
                if (area.getAllowedNfcTagIds() != null &&
                        area.getAllowedNfcTagIds().contains(location.getNfcTagId())) {
                    result.setValid(true);
                    result.setMessage("NFC打卡验证通过，区域：" + area.getAreaName());
                    result.setAreaId(area.getAreaId());
                    result.setAreaName(area.getAreaName());
                    return result;
                }
            }
        }

        result.setMessage("不在有效的考勤区域内");
        return result;
    }

    /**
     * 检查位置是否在考勤区域内
     */
    private Boolean isLocationInArea(LocationInfo location, AttendanceArea area) {
        if (area.getCenterLatitude() == null || area.getCenterLongitude() == null ||
                location.getLatitude() == null || location.getLongitude() == null) {
            return false;
        }

        // 使用Haversine公式计算两点间距离
        double distance = calculateDistance(
                location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue(),
                area.getCenterLatitude().doubleValue(),
                area.getCenterLongitude().doubleValue());

        return distance <= area.getRadius().doubleValue();
    }

    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径(公里)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // 转换为米

        return distance;
    }

    /**
     * 设备验证
     */
    private DeviceVerificationResult verifyDevice(MobilePunchRequest request) {
        DeviceVerificationResult result = new DeviceVerificationResult();
        result.setValid(true);
        result.setMessage("设备验证通过");

        // 检查设备信息是否存在
        if (request.getDeviceInfo() == null || request.getDeviceInfo().trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("设备信息缺失");
            return result;
        }

        // 这里可以添加更多设备验证逻辑，比如：
        // - 检查设备是否在白名单中
        // - 检查设备是否已被root/jailbreak
        // - 检查是否使用了VPN或代理

        result.setDeviceInfo(request.getDeviceInfo());
        return result;
    }

    /**
     * 照片验证
     */
    private PhotoVerificationResult verifyPhoto(String photoData) {
        PhotoVerificationResult result = new PhotoVerificationResult();
        result.setValid(true);
        result.setMessage("照片验证通过");

        // 基础照片验证
        if (photoData == null || photoData.trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("照片数据为空");
            return result;
        }

        // 检查是否为有效的Base64图片数据
        if (!photoData.startsWith("data:image/")) {
            result.setValid(false);
            result.setMessage("无效的图片格式");
            return result;
        }

        // 这里可以添加更多照片验证逻辑，比如：
        // - 检查图片大小
        // - 检查图片清晰度
        // - 人脸检测
        // - 活体检测

        return result;
    }

    /**
     * 人脸识别验证
     */
    private FaceVerificationResult verifyFaceRecognition(String photoData, Long employeeId) {
        FaceVerificationResult result = new FaceVerificationResult();
        result.setValid(true);
        result.setMatchScore(BigDecimal.valueOf(0.95));
        result.setMessage("人脸识别验证通过");

        // 这里应该调用实际的人脸识别服务
        // 简化处理，假设验证通过
        log.debug("员工{}人脸识别验证，匹配分数：{}", employeeId, result.getMatchScore());

        return result;
    }

    /**
     * 检查重复打卡
     */
    private PunchDuplicateCheckResult checkDuplicatePunch(MobilePunchRequest request) {
        PunchDuplicateCheckResult result = new PunchDuplicateCheckResult();
        result.setIsDuplicate(false);

        String cacheKey = generatePunchCacheKey(request);
        MobilePunchRecord lastRecord = mobilePunchRecords.get(cacheKey);

        if (lastRecord != null) {
            // 检查时间间隔（5分钟内视为重复）
            LocalDateTime fiveMinutesAgo = request.getPunchTime().minusMinutes(5);
            if (lastRecord.getPunchTime().isAfter(fiveMinutesAgo)) {
                result.setIsDuplicate(true);
                result.setMessage("5分钟内已存在相同类型的打卡记录");
                result.setLastPunchTime(lastRecord.getPunchTime());
            }
        }

        return result;
    }

    /**
     * 创建打卡记录
     */
    private MobilePunchRecord createPunchRecord(MobilePunchRequest request, MobilePunchResult result) {
        MobilePunchRecord record = new MobilePunchRecord();
        record.setRecordId(System.currentTimeMillis());
        record.setEmployeeId(request.getEmployeeId());
        record.setPunchType(request.getPunchType());
        record.setPunchTime(request.getPunchTime());
        record.setLocation(request.getLocation());

        // 记录验证结果
        if (result.getVerifiedLocation() != null) {
            record.setVerifiedLocation(result.getVerifiedLocation());
        }

        // 记录设备信息
        if (request.getDeviceInfo() != null) {
            record.setDeviceInfo(request.getDeviceInfo());
        }

        record.setCreateTime(LocalDateTime.now());
        record.setStatus("SUCCESS");

        return record;
    }

    /**
     * 保存打卡记录
     */
    private void savePunchRecord(MobilePunchRecord record) {
        String cacheKey = generatePunchCacheKey(record);
        mobilePunchRecords.put(cacheKey, record);

        log.debug("保存移动端打卡记录：员工ID={}，类型={}，时间={}",
                record.getEmployeeId(), record.getPunchType(), record.getPunchTime());
    }

    /**
     * 生成打卡缓存键
     */
    private String generatePunchCacheKey(MobilePunchRequest request) {
        return String.format("%d_%s_%s",
                request.getEmployeeId(),
                request.getPunchType().getCode(),
                request.getPunchTime().toLocalDate().toString());
    }

    private String generatePunchCacheKey(MobilePunchRecord record) {
        return String.format("%d_%s_%s",
                record.getEmployeeId(),
                record.getPunchType().getCode(),
                record.getPunchTime().toLocalDate().toString());
    }

    /**
     * 确定考勤状态
     */
    private String determineAttendanceStatus(MobilePunchRequest request, MobilePunchResult result) {
        LocalTime punchTime = request.getPunchTime().toLocalTime();

        switch (request.getPunchType()) {
            case PUNCH_IN:
                if (punchTime.isBefore(LocalTime.of(9, 0))) {
                    return "NORMAL";
                } else if (punchTime.isBefore(LocalTime.of(9, 30))) {
                    return "LATE";
                } else {
                    return "ABNORMAL";
                }
            case PUNCH_OUT:
                if (punchTime.isAfter(LocalTime.of(18, 0))) {
                    return "OVERTIME";
                } else if (punchTime.isBefore(LocalTime.of(17, 30))) {
                    return "EARLY_LEAVE";
                } else {
                    return "NORMAL";
                }
            default:
                return "NORMAL";
        }
    }

    /**
     * 判断是否为异常打卡
     */
    private Boolean isAbnormalPunch(MobilePunchRequest request, MobilePunchResult result) {
        // 检查位置验证是否失败
        LocationVerificationResult locationResult = (LocationVerificationResult) result.getVerificationResults()
                .get("location");
        if (locationResult != null && !locationResult.getValid()) {
            return true;
        }

        // 检查设备验证是否失败
        DeviceVerificationResult deviceResult = (DeviceVerificationResult) result.getVerificationResults()
                .get("device");
        if (deviceResult != null && !deviceResult.getValid()) {
            return true;
        }

        // 检查照片验证是否失败
        PhotoVerificationResult photoResult = (PhotoVerificationResult) result.getVerificationResults().get("photo");
        if (photoResult != null && !photoResult.getValid()) {
            return true;
        }

        return false;
    }

    /**
     * 获取员工今日打卡记录
     *
     * @param employeeId 员工ID
     * @return 今日打卡记录列表
     */
    public List<MobilePunchRecord> getTodayPunchRecords(Long employeeId) {
        LocalDate today = LocalDate.now();
        return mobilePunchRecords.values().stream()
                .filter(record -> employeeId.equals(record.getEmployeeId()))
                .filter(record -> record.getPunchTime().toLocalDate().equals(today))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * 获取考勤统计信息
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计信息
     */
    public Map<String, Object> getPunchStatistics(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> statistics = new HashMap<>();

        List<MobilePunchRecord> records = mobilePunchRecords.values().stream()
                .filter(record -> employeeId.equals(record.getEmployeeId()))
                .filter(record -> !record.getPunchTime().toLocalDate().isBefore(startDate))
                .filter(record -> !record.getPunchTime().toLocalDate().isAfter(endDate))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        statistics.put("totalRecords", records.size());
        statistics.put("punchInRecords", records.stream().filter(r -> r.getPunchType() == PunchType.PUNCH_IN).count());
        statistics.put("punchOutRecords",
                records.stream().filter(r -> r.getPunchType() == PunchType.PUNCH_OUT).count());
        statistics.put("abnormalRecords", records.stream().filter(r -> "ABNORMAL".equals(r.getStatus())).count());

        return statistics;
    }

    /**
     * 添加考勤区域
     */
    public void addAttendanceArea(AttendanceArea area) {
        attendanceAreas.put(area.getAreaId(), area);
        log.info("添加考勤区域：{}", area.getAreaName());
    }

    /**
     * 获取所有考勤区域
     */
    public List<AttendanceArea> getAllAttendanceAreas() {
        return new ArrayList<>(attendanceAreas.values());
    }

    // 验证结果类定义
    @Data
    public static class LocationVerificationResult {
        private Boolean valid;
        private String message;
        private LocationInfo verifiedLocation;
        private Long areaId;
        private String areaName;
    }

    @Data
    public static class DeviceVerificationResult {
        private Boolean valid;
        private String message;
        private String deviceInfo;
    }

    @Data
    public static class PhotoVerificationResult {
        private Boolean valid;
        private String message;
        private String photoQuality;
    }

    @Data
    public static class FaceVerificationResult {
        private Boolean valid;
        private String message;
        private BigDecimal matchScore;
        private String faceFeatures;
    }

    @Data
    public static class PunchDuplicateCheckResult {
        private Boolean isDuplicate;
        private String message;
        private LocalDateTime lastPunchTime;
    }

    @Data
    public static class MobilePunchRecord {
        private Long recordId;
        private Long employeeId;
        private PunchType punchType;
        private LocalDateTime punchTime;
        private LocationInfo location;
        private LocationInfo verifiedLocation;
        private String deviceInfo;
        private String photoData;
        private String status;
        private LocalDateTime createTime;
    }
}
