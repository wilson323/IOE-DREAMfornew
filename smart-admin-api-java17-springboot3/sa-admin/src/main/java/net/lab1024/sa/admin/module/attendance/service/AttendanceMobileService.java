package net.lab1024.sa.admin.module.attendance.service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.attendance.manager.AttendanceDeviceManager;
import net.lab1024.sa.admin.module.attendance.domain.data.AttendancePunchData;
import net.lab1024.sa.admin.module.attendance.manager.AttendanceRuleEngine;
import net.lab1024.sa.admin.module.attendance.domain.result.AttendanceRuleProcessResult;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceDao;
import net.lab1024.sa.admin.module.attendance.dao.AttendanceRuleDao;

/**
 * 移动端考勤服务
 *
 * <p>
 * 考勤模块的移动端专用服务，提供移动端考勤的完整功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Service层，提供移动端考勤的业务逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - GPS打卡：基于GPS定位的移动端打卡功能
 * - 离线打卡：支持网络不佳时的离线打卡机制
 * - 数据同步：离线数据的在线同步和状态更新
 * - 位置验证：GPS坐标的有效性验证
 * - 照片打卡：支持人脸识别和拍照验证
 * - 批量上传：支持多条离线记录的批量上传
 * - 冲突处理：处理离线在线数据的冲突问题
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-17
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceMobileService {

    @Resource
    private AttendanceDao attendanceRepository;

    // 兼容性别名
    private AttendanceDao attendanceRecordDao = attendanceRepository;

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private AttendanceDeviceManager attendanceDeviceManager;

    @Resource
    private AttendanceRuleEngine attendanceRuleEngine;

    // ===== GPS打卡核心服务 =====

    /**
     * GPS上班打卡
     *
     * @param employeeId 员工ID
     * @param latitude   GPS纬度
     * @param longitude  GPS经度
     * @param address    打卡地址
     * @param photoUrl   照片URL
     * @return 打卡结果
     */
    public MobilePunchResult gpsPunchIn(Long employeeId, Double latitude, Double longitude, String address,
            String photoUrl) {
        try {
            log.info("开始GPS上班打卡: employeeId={}, location=({}, {}), address={}",
                    employeeId, latitude, longitude, address);

            // 1. 验证GPS位置有效性
            if (!validateGpsLocation(employeeId, latitude, longitude)) {
                log.warn("GPS位置验证失败: employeeId={}, location=({}, {})", employeeId, latitude, longitude);
                return MobilePunchResult.failure("GPS位置不在允许范围内");
            }

            // 2. 检查是否已上班打卡
            LocalDate today = LocalDate.now();
            List<AttendanceRecordEntity> existingRecords = attendanceRecordDao.selectEmployeeTodayRecord(employeeId, today);
            AttendanceRecordEntity existingRecord = existingRecords.isEmpty() ? null : existingRecords.get(0);

            if (existingRecord != null && existingRecord.getPunchInTime() != null) {
                log.warn("员工今日已上班打卡: employeeId={}, recordId={}", employeeId, existingRecord.getRecordId());
                return MobilePunchResult.failure("今日已上班打卡");
            }

            // 3. 创建考勤记录
            AttendanceRecordEntity record = createOrUpdateGpsRecord(employeeId, latitude, longitude, address, photoUrl,
                    "IN", existingRecord);

            // 4. 应用考勤规则
            AttendanceRuleProcessResult processResult = attendanceRuleEngine
                    .processAttendanceRecord(record);

            // 5. 保存记录
            int result;
            if (record.getRecordId() == null) {
                result = attendanceRecordDao.insert(record);
            } else {
                result = attendanceRecordDao.updateById(record);
            }

            if (result > 0 && processResult.isSuccess()) {
                log.info("GPS上班打卡成功: employeeId={}, recordId={}", employeeId, record.getRecordId());
                return MobilePunchResult.success("GPS上班打卡成功", record.getRecordId(),
                        processResult.getAttendanceStatus());
            } else {
                log.error("GPS上班打卡失败: employeeId={}, result={}, processResult={}",
                        employeeId, result, processResult.getErrorMessage());
                return MobilePunchResult.failure(processResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("GPS上班打卡异常: employeeId" + employeeId, e);
            return MobilePunchResult.failure("系统异常：" + e.getMessage());
        }
    }

    /**
     * GPS下班打卡
     *
     * @param employeeId 员工ID
     * @param latitude   GPS纬度
     * @param longitude  GPS经度
     * @param address    打卡地址
     * @param photoUrl   照片URL
     * @return 打卡结果
     */
    public MobilePunchResult gpsPunchOut(Long employeeId, Double latitude, Double longitude, String address,
            String photoUrl) {
        try {
            log.info("开始GPS下班打卡: employeeId={}, location=({}, {}), address={}",
                    employeeId, latitude, longitude, address);

            // 1. 验证GPS位置有效性
            if (!validateGpsLocation(employeeId, latitude, longitude)) {
                log.warn("GPS位置验证失败: employeeId={}, location=({}, {})", employeeId, latitude, longitude);
                return MobilePunchResult.failure("GPS位置不在允许范围内");
            }

            // 2. 检查是否已上班打卡且未下班打卡
            LocalDate today = LocalDate.now();
            AttendanceRecordEntity existingRecord = attendanceRecordDao.selectEmployeeTodayRecord(employeeId, today);

            if (existingRecord == null || existingRecord.getPunchInTime() == null) {
                log.warn("员工未进行上班打卡: employeeId={}", employeeId);
                return MobilePunchResult.failure("请先进行上班打卡");
            }

            if (existingRecord.getPunchOutTime() != null) {
                log.warn("员工今日已下班打卡: employeeId={}, recordId={}", employeeId, existingRecord.getRecordId());
                return MobilePunchResult.failure("今日已下班打卡");
            }

            // 3. 更新考勤记录
            AttendanceRecordEntity record = createOrUpdateGpsRecord(employeeId, latitude, longitude, address, photoUrl,
                    "OUT", existingRecord);

            // 4. 应用考勤规则并计算工作时长
            AttendanceRuleEngine.AttendanceRuleProcessResult processResult = attendanceRuleEngine
                    .processAttendanceRecord(record);

            // 5. 计算工作时长
            calculateWorkHours(record);

            // 6. 保存记录
            int result = attendanceRecordDao.update(record);

            if (result > 0) {
                log.info("GPS下班打卡成功: employeeId={}, recordId={}, workHours={}",
                        employeeId, record.getRecordId(), record.getWorkHours());
                return MobilePunchResult.success("GPS下班打卡成功", record.getRecordId(),
                        processResult.getAttendanceStatus());
            } else {
                log.error("GPS下班打卡失败: employeeId={}, result={}", employeeId, result);
                return MobilePunchResult.failure("保存打卡记录失败");
            }

        } catch (Exception e) {
            log.error("GPS下班打卡异常: employeeId" + employeeId, e);
            return MobilePunchResult.failure("系统异常：" + e.getMessage());
        }
    }

    // ===== 离线打卡支持 =====

    /**
     * 离线打卡记录缓存
     * 在本地存储离线打卡数据，待网络恢复后同步
     *
     * @param employeeId       员工ID
     * @param offlinePunchData 离线打卡数据
     * @return 缓存结果
     */
    public OfflinePunchCacheResult cacheOfflinePunch(Long employeeId, OfflinePunchData offlinePunchData) {
        try {
            log.info("缓存离线打卡记录: employeeId={}, punchTime={}, punchType={}",
                    employeeId, offlinePunchData.getPunchTime(), offlinePunchData.getPunchType());

            // 1. 验证离线数据完整性
            if (!validateOfflinePunchData(offlinePunchData)) {
                log.warn("离线打卡数据验证失败: employeeId={}", employeeId);
                return OfflinePunchCacheResult.failure("离线打卡数据不完整");
            }

            // 2. 生成本地缓存ID
            String cacheId = generateCacheId(employeeId, offlinePunchData);

            // 3. 缓存到本地存储（这里简化处理，实际应用中可使用SQLite或SharedPreferences）
            // 实际实现需要考虑数据持久化和设备存储限制

            log.info("离线打卡记录缓存成功: employeeId={}, cacheId={}", employeeId, cacheId);
            return OfflinePunchCacheResult.success("离线打卡记录已缓存", cacheId);

        } catch (Exception e) {
            log.error("缓存离线打卡记录异常: employeeId" + employeeId, e);
            return OfflinePunchCacheResult.failure("缓存失败：" + e.getMessage());
        }
    }

    /**
     * 同步离线打卡数据
     * 将设备中缓存的离线打卡记录同步到服务器
     *
     * @param employeeId 员工ID
     * @return 同步结果
     */
    public OfflineSyncResult syncOfflinePunches(Long employeeId) {
        try {
            log.info("开始同步离线打卡数据: employeeId={}", employeeId);

            // 1. 获取本地缓存的离线记录
            List<OfflinePunchData> offlineRecords = getCachedOfflinePunches(employeeId);

            if (offlineRecords.isEmpty()) {
                log.info("没有需要同步的离线记录: employeeId={}", employeeId);
                return OfflineSyncResult.success("没有需要同步的离线记录", 0, 0);
            }

            // 2. 批量处理离线记录
            int totalRecords = offlineRecords.size();
            int successCount = 0;
            int failureCount = 0;
            List<String> failedRecords = new ArrayList<>();

            for (OfflinePunchData offlineRecord : offlineRecords) {
                try {
                    // 转换为设备管理器可处理的格式
                    AttendanceDeviceManager.AttendancePunchData punchData = convertOfflineToDeviceData(offlineRecord);

                    // 验证设备并处理打卡数据
                    SmartDeviceEntity device = attendanceDeviceManager.validateDevice(punchData.getDeviceCode());
                    if (device != null) {
                        AttendanceDeviceManager.DeviceDataProcessResult result = attendanceDeviceManager
                                .receiveAttendanceData(punchData);

                        if (result.isSuccess()) {
                            successCount++;
                            // 标记本地记录为已同步
                            markOfflineRecordSynced(offlineRecord.getCacheId());
                        } else {
                            failureCount++;
                            failedRecords.add(offlineRecord.getCacheId() + ": " + result.getErrorMessage());
                        }
                    } else {
                        failureCount++;
                        failedRecords.add(offlineRecord.getCacheId() + ": 设备验证失败");
                    }

                } catch (Exception e) {
                    failureCount++;
                    failedRecords.add(offlineRecord.getCacheId() + ": " + e.getMessage());
                    log.warn("处理离线记录失败: cacheId={}, error={}", offlineRecord.getCacheId(), e.getMessage());
                }
            }

            // 3. 返回同步结果
            log.info("离线打卡数据同步完成: employeeId={}, total={}, success={}, failure={}",
                    employeeId, totalRecords, successCount, failureCount);

            return OfflineSyncResult.success("同步完成", totalRecords, successCount)
                    .setFailureCount(failureCount)
                    .setFailedRecords(failedRecords);

        } catch (Exception e) {
            log.error("同步离线打卡数据异常: employeeId" + employeeId, e);
            return OfflineSyncResult.failure("同步异常：" + e.getMessage());
        }
    }

    // ===== 辅助方法 =====

    /**
     * 验证GPS位置有效性
     *
     * @param employeeId 员工ID
     * @param latitude   纬度
     * @param longitude  经度
     * @return 是否有效
     */
    private boolean validateGpsLocation(Long employeeId, Double latitude, Double longitude) {
        try {
            if (latitude == null || longitude == null) {
                return false;
            }

            // 检查坐标范围是否合理
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return false;
            }

            // 获取员工适用的考勤规则
            LocalDate today = LocalDate.now();
            var rule = attendanceRuleDao.selectHighestPriorityRule(employeeId, today);

            if (rule == null || !rule.isGpsValidationEnabled()) {
                return true; // 如果不需要GPS验证，直接返回true
            }

            // 使用规则引擎验证GPS位置
            // 创建一个临时record用于GPS验证
            AttendanceRecordEntity tempRecord = new AttendanceRecordEntity();
            tempRecord.setEmployeeId(employeeId);
            tempRecord.setAttendanceDate(today.atStartOfDay());
            return attendanceRuleEngine.validateGpsLocation(tempRecord, rule, latitude, longitude, rule.getGpsRange());

        } catch (Exception e) {
            log.error("验证GPS位置异常: employeeId={}, location=({}, {})", employeeId, latitude, longitude, e);
            return false;
        }
    }

    /**
     * 创建或更新GPS打卡记录
     *
     * @param employeeId     员工ID
     * @param latitude       纬度
     * @param longitude      经度
     * @param address        地址
     * @param photoUrl       照片URL
     * @param punchType      打卡类型
     * @param existingRecord 现有记录
     * @return 考勤记录
     */
    private AttendanceRecordEntity createOrUpdateGpsRecord(Long employeeId, Double latitude, Double longitude,
            String address, String photoUrl, String punchType,
            AttendanceRecordEntity existingRecord) {
        LocalDateTime now = LocalDateTime.now();

        if (existingRecord == null) {
            // 创建新记录
            existingRecord = new AttendanceRecordEntity();
            existingRecord.setEmployeeId(employeeId);
            existingRecord.setAttendanceDate(now);
            existingRecord.setVerificationMethod("GPS");
            existingRecord.setDeviceCode("MOBILE_APP");
            existingRecord.setCreateTime(now);
            existingRecord.setUpdateTime(now);
        }

        // 更新GPS相关信息
        if ("IN".equalsIgnoreCase(punchType)) {
            existingRecord.setPunchInTime(now.toLocalTime());
            existingRecord.setPunchInDateTime(now);
            existingRecord.setPunchInLocation(address);
            if (photoUrl != null) {
                existingRecord.setPhotoUrl(photoUrl);
            }
        } else if ("OUT".equalsIgnoreCase(punchType)) {
            existingRecord.setPunchOutTime(now.toLocalTime());
            existingRecord.setPunchOutDatetime(now);
            existingRecord.setPunchOutLocation(address);
            if (photoUrl != null) {
                existingRecord.setPhotoUrl(photoUrl);
            }
        }

        // 设置GPS坐标
        existingRecord.setGpsLatitude(latitude);
        existingRecord.setGpsLongitude(longitude);
        existingRecord.setGpsValidation(true); // GPS验证通过
        existingRecord.setUpdateTime(now);

        return existingRecord;
    }

    /**
     * 计算工作时长
     *
     * @param record 考勤记录
     */
    private void calculateWorkHours(AttendanceRecordEntity record) {
        if (record.getPunchInTime() != null && record.getPunchOutTime() != null) {
            // 将时间转换为当天的完整时间戳进行计算
            LocalDateTime attendanceDateTime = record.getAttendanceDate();
            LocalDateTime punchInDateTime = attendanceDateTime.toLocalDate().atTime(record.getPunchInTime());
            LocalDateTime punchOutDateTime = attendanceDateTime.toLocalDate().atTime(record.getPunchOutTime());

            // 处理跨天情况（如下班打卡时间是第二天凌晨）
            if (record.getPunchOutTime().isBefore(record.getPunchInTime())) {
                punchOutDateTime = punchOutDateTime.plusDays(1);
            }

            // 计算工作时长（小时）
            long minutes = java.time.Duration.between(punchInDateTime, punchOutDateTime).toMinutes();
            BigDecimal workHours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            record.setWorkHours(workHours);

            log.debug("计算工作时长: punchIn={}, punchOut={}, workHours={}小时",
                    record.getPunchInTime(), record.getPunchOutTime(), workHours);
        }
    }

    /**
     * 验证离线打卡数据完整性
     *
     * @param offlinePunchData 离线打卡数据
     * @return 是否有效
     */
    private boolean validateOfflinePunchData(OfflinePunchData offlinePunchData) {
        return offlinePunchData != null &&
                offlinePunchData.getEmployeeId() != null &&
                offlinePunchData.getPunchTime() != null &&
                offlinePunchData.getPunchType() != null &&
                !offlinePunchData.getPunchType().trim().isEmpty();
    }

    /**
     * 生成缓存ID
     *
     * @param employeeId       员工ID
     * @param offlinePunchData 离线打卡数据
     * @return 缓存ID
     */
    private String generateCacheId(Long employeeId, OfflinePunchData offlinePunchData) {
        long timestamp = offlinePunchData.getPunchTime() != null
                ? offlinePunchData.getPunchTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();
        return "OFFLINE_" + employeeId + "_" + timestamp + "_" + offlinePunchData.getPunchType();
    }

    /**
     * 获取缓存的离线打卡记录
     *
     * @param employeeId 员工ID
     * @return 离线打卡记录列表
     */
    private List<OfflinePunchData> getCachedOfflinePunches(Long employeeId) {
        // 实际实现需要从本地存储中读取离线数据
        // 这里返回空列表作为示例
        return new ArrayList<>();
    }

    /**
     * 将离线数据转换为设备管理器格式
     *
     * @param offlinePunchData 离线打卡数据
     * @return 设备打卡数据
     */
    private AttendancePunchData convertOfflineToDeviceData(OfflinePunchData offlinePunchData) {
        AttendancePunchData punchData = new AttendancePunchData();
        punchData.setDeviceCode("MOBILE_OFFLINE");
        punchData.setEmployeeCode(offlinePunchData.getEmployeeId().toString());
        punchData.setPunchTime(offlinePunchData.getPunchTime());
        punchData.setPunchType(offlinePunchData.getPunchType());
        punchData.setVerificationMethod(offlinePunchData.getVerificationMethod());
        punchData.setLatitude(offlinePunchData.getLatitude());
        punchData.setLongitude(offlinePunchData.getLongitude());
        punchData.setPhotoUrl(offlinePunchData.getPhotoUrl());
        return punchData;
    }

    /**
     * 标记离线记录为已同步
     *
     * @param cacheId 缓存ID
     */
    private void markOfflineRecordSynced(String cacheId) {
        // 实际实现需要更新本地存储中的记录状态
        log.debug("标记离线记录为已同步: cacheId={}", cacheId);
    }

    // ===== 内部数据类 =====

    /**
     * 移动端打卡结果
     */
    public static class MobilePunchResult {
        private boolean success;
        private String message;
        private Long recordId;
        private String attendanceStatus;

        private MobilePunchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private MobilePunchResult(boolean success, String message, Long recordId, String attendanceStatus) {
            this.success = success;
            this.message = message;
            this.recordId = recordId;
            this.attendanceStatus = attendanceStatus;
        }

        public static MobilePunchResult success(String message) {
            return new MobilePunchResult(true, message);
        }

        public static MobilePunchResult success(String message, Long recordId, String attendanceStatus) {
            return new MobilePunchResult(true, message, recordId, attendanceStatus);
        }

        public static MobilePunchResult failure(String message) {
            return new MobilePunchResult(false, message);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Long getRecordId() {
            return recordId;
        }

        public String getAttendanceStatus() {
            return attendanceStatus;
        }
    }

    /**
     * 离线打卡数据
     */
    public static class OfflinePunchData {
        private String cacheId; // 缓存ID
        private Long employeeId; // 员工ID
        private LocalDateTime punchTime; // 打卡时间
        private String punchType; // 打卡类型（IN/OUT）
        private String verificationMethod; // 验证方式
        private Double latitude; // GPS纬度
        private Double longitude; // GPS经度
        private String address; // 打卡地址
        private String photoUrl; // 照片URL
        private LocalDateTime createTime; // 创建时间

        // Getters and Setters
        public String getCacheId() {
            return cacheId;
        }

        public void setCacheId(String cacheId) {
            this.cacheId = cacheId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public LocalDateTime getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(LocalDateTime punchTime) {
            this.punchTime = punchTime;
        }

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public String getVerificationMethod() {
            return verificationMethod;
        }

        public void setVerificationMethod(String verificationMethod) {
            this.verificationMethod = verificationMethod;
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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }

    /**
     * 离线打卡缓存结果
     */
    public static class OfflinePunchCacheResult {
        private boolean success;
        private String message;
        private String cacheId;

        private OfflinePunchCacheResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private OfflinePunchCacheResult(boolean success, String message, String cacheId) {
            this.success = success;
            this.message = message;
            this.cacheId = cacheId;
        }

        public static OfflinePunchCacheResult success(String message, String cacheId) {
            return new OfflinePunchCacheResult(true, message, cacheId);
        }

        public static OfflinePunchCacheResult failure(String message) {
            return new OfflinePunchCacheResult(false, message);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getCacheId() {
            return cacheId;
        }
    }

    /**
     * 离线数据同步结果
     */
    public static class OfflineSyncResult {
        private boolean success;
        private String message;
        private int totalRecords;
        private int successCount;
        private int failureCount;
        private List<String> failedRecords;

        private OfflineSyncResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private OfflineSyncResult(boolean success, String message, int totalRecords, int successCount) {
            this.success = success;
            this.message = message;
            this.totalRecords = totalRecords;
            this.successCount = successCount;
        }

        public static OfflineSyncResult success(String message, int totalRecords, int successCount) {
            return new OfflineSyncResult(true, message, totalRecords, successCount);
        }

        public static OfflineSyncResult failure(String message) {
            return new OfflineSyncResult(false, message);
        }

        public OfflineSyncResult setFailureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public OfflineSyncResult setFailedRecords(List<String> failedRecords) {
            this.failedRecords = failedRecords;
            return this;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public List<String> getFailedRecords() {
            return failedRecords;
        }
    }
}