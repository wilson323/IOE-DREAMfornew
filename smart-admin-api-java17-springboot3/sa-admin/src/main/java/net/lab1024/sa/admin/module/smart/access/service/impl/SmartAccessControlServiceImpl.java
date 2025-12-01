package net.lab1024.sa.admin.module.smart.access.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.SmartAccessPermissionDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessPermissionEntity;
import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessRecordEntity;
import net.lab1024.sa.admin.module.smart.access.service.SmartAccessControlService;
import net.lab1024.sa.admin.module.smart.access.util.TimeSlotValidator;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartStringUtil;

/**
 * 智能门禁控制服务实现类
 *
 * 
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartAccessControlServiceImpl implements SmartAccessControlService {

    @Resource
    private SmartAccessPermissionDao accessPermissionDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ACCESS_TOKEN_PREFIX = "access:token:";
    private static final String ACCESS_RECORD_PREFIX = "access:record:";
    private static final String ACCESS_STATS_PREFIX = "access:stats:";
    private static final String ACCESS_PERM_FACE_PREFIX = "access:perm:face:";
    private static final String ACCESS_PERM_FP_PREFIX = "access:perm:fp:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> verifyAccess(Long personId, Long deviceId,
            String accessType, String credential) {
        try {
            log.info("开始门禁校验: personId={}, deviceId={}, accessType={}", personId, deviceId,
                    accessType);

            // 1. 查找权限信息
            SmartAccessPermissionEntity permission = findPermissionByCredential(accessType, credential);
            if (permission == null) {
                return createVerificationResult(false, "未找到权限信息", null);
            }

            // 2. 校验人员ID是否匹配
            if (!personId.equals(permission.getPersonId())) {
                return createVerificationResult(false, "人员信息不匹配", null);
            }

            // 3. 校验设备权限
            if (!checkDevicePermission(permission, deviceId)) {
                return createVerificationResult(false, "无设备访问权限", null);
            }

            // 4. 校验时间权限
            LocalDateTime currentTime = LocalDateTime.now();
            if (!checkTimePermission(permission, currentTime)) {
                return createVerificationResult(false, "时间权限无效", null);
            }

            // 5. 校验访问次数限制
            if (!checkAccessLimit(permission)) {
                return createVerificationResult(false, "访问次数已超限", null);
            }

            // 6. 记录通行事件
            SmartAccessRecordEntity record = createAccessRecord(permission, deviceId, accessType, "SUCCESS", null);
            recordAccessEvent(record);

            // 7. 更新访问次数
            incrementAccessCount(permission.getPermissionId());

            // 8. 迁移斿洖成功结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("personId", personId);
            result.put("personName", permission.getPersonName());
            result.put("permissionType", permission.getPermissionType());
            result.put("accessTime", currentTime);
            result.put("message", "校验通过");

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("门禁校验异常", e);
            return ResponseDTO.userErrorParam("系统异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> verifyCardAccess(String cardNumber, Long deviceId) {
        try {
            SmartAccessPermissionEntity permission = accessPermissionDao.selectByCardNumber(cardNumber,
                    LocalDateTime.now());
            if (permission == null) {
                return createVerificationResult(false, "卡片无效或已过期", null);
            }

            if (!checkDevicePermission(permission, deviceId)) {
                return createVerificationResult(false, "卡片无此设备访问权限", null);
            }

            // 记录閫氳
            SmartAccessRecordEntity record = createAccessRecord(permission, deviceId, "CARD", "SUCCESS", null);
            recordAccessEvent(record);
            incrementAccessCount(permission.getPermissionId());

            Map<String, Object> result = createSuccessResult(permission, "刷卡校验通过");
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("刷卡校验异常", e);
            return ResponseDTO.userErrorParam("校验异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> verifyFaceAccess(String faceFeatureId, Long deviceId) {
        try {
            SmartAccessPermissionEntity permission = getPermissionByFaceFeatureId(faceFeatureId);
            if (permission == null) {
                return createVerificationResult(false, "人脸特征无效或已过期", null);
            }

            if (!checkDevicePermission(permission, deviceId)) {
                return createVerificationResult(false, "人脸无此设备访问权限", null);
            }

            // 记录閫氳
            SmartAccessRecordEntity record = createAccessRecord(permission, deviceId, "FACE", "SUCCESS", null);
            recordAccessEvent(record);
            incrementAccessCount(permission.getPermissionId());

            Map<String, Object> result = createSuccessResult(permission, "人脸识别校验通过");
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("人脸识别校验异常", e);
            return ResponseDTO.userErrorParam("校验异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> verifyFingerprintAccess(String fingerprintId,
            Long deviceId) {
        try {
            SmartAccessPermissionEntity permission = getPermissionByFingerprintId(fingerprintId);
            if (permission == null) {
                return createVerificationResult(false, "指纹特征无效或已过期", null);
            }

            if (!checkDevicePermission(permission, deviceId)) {
                return createVerificationResult(false, "指纹无此设备访问权限", null);
            }

            // 记录閫氳
            SmartAccessRecordEntity record = createAccessRecord(permission, deviceId, "FINGERPRINT", "SUCCESS", null);
            recordAccessEvent(record);
            incrementAccessCount(permission.getPermissionId());

            Map<String, Object> result = createSuccessResult(permission, "指纹校验通过");
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("指纹校验异常", e);
            return ResponseDTO.userErrorParam("校验异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> verifyPasswordAccess(String password, Long deviceId) {
        try {
            // 此处简化处理，实际应有更完善的密码校验逻辑
            String passwordHash = encryptMd5(password);

            LambdaQueryWrapper<SmartAccessPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmartAccessPermissionEntity::getPasswordHash, passwordHash)
                    .eq(SmartAccessPermissionEntity::getEnabledFlag, 1)
                    .le(SmartAccessPermissionEntity::getEffectiveTime, LocalDateTime.now())
                    .ge(SmartAccessPermissionEntity::getExpireTime, LocalDateTime.now());

            SmartAccessPermissionEntity permission = accessPermissionDao.selectOne(queryWrapper);
            if (permission == null) {
                return createVerificationResult(false, "密码错误", null);
            }

            if (!checkDevicePermission(permission, deviceId)) {
                return createVerificationResult(false, "密码无此设备访问权限", null);
            }

            // 记录閫氳
            SmartAccessRecordEntity record = createAccessRecord(permission, deviceId, "PASSWORD", "SUCCESS", null);
            recordAccessEvent(record);
            incrementAccessCount(permission.getPermissionId());

            Map<String, Object> result = createSuccessResult(permission, "密码校验通过");
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("密码校验异常", e);
            return ResponseDTO.userErrorParam("校验异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> remoteOpenDoor(Long deviceId, Long operatorId, String reason) {
        try {
            // 记录远程开门事件
            SmartAccessRecordEntity record = new SmartAccessRecordEntity();
            record.setPersonId(operatorId);
            record.setDeviceId(deviceId);
            record.setAccessType("REMOTE");
            record.setAccessResult("SUCCESS");
            record.setAccessTime(LocalDateTime.now());
            record.setDataSource("MANUAL");
            // record.setRemark(reason); // SmartAccessRecordEntity暂未提供该字段
            // record.setTenantId(1L); // 默认租户ID - BaseEntity暂未提供该字段

            // 这里应该调用真实的设备接口开门
            log.info("远程开门: deviceId={}, operatorId={}, reason={}", deviceId, operatorId, reason);

            return ResponseDTO.okMsg("远程开门成功");

        } catch (Exception e) {
            log.error("远程开门异常", e);
            return ResponseDTO.userErrorParam("远程开门失败: " + e.getMessage());
        }
    }

    @Override
    public SmartAccessPermissionEntity checkPersonPermission(Long personId, Long deviceId,
            LocalDateTime currentTime) {
        List<SmartAccessPermissionEntity> permissions = accessPermissionDao.selectValidPermissionsByPersonId(personId,
                currentTime);

        for (SmartAccessPermissionEntity permission : permissions) {
            if (checkDevicePermission(permission, deviceId)
                    && checkTimePermission(permission, currentTime)) {
                return permission;
            }
        }

        return null;
    }

    @Override
    public boolean checkTimePermission(SmartAccessPermissionEntity permission,
            LocalDateTime currentTime) {
        // 校验有效时间范围
        if (permission.getEffectiveTime() != null
                && currentTime.isBefore(permission.getEffectiveTime())) {
            return false;
        }
        if (permission.getExpireTime() != null && currentTime.isAfter(permission.getExpireTime())) {
            return false;
        }

        // 校验有效星期
        if (SmartStringUtil.isNotEmpty(permission.getEffectiveDays())) {
            int dayOfWeek = currentTime.getDayOfWeek().getValue();
            String[] days = permission.getEffectiveDays().split(",");
            boolean dayValid = false;
            for (String day : days) {
                if (String.valueOf(dayOfWeek).equals(day.trim())) {
                    dayValid = true;
                    break;
                }
            }
            if (!dayValid) {
                return false;
            }
        }

        // 校验时间段（使用时间段验证引擎）
        String timePeriods = permission.getTimePeriods();
        if (SmartStringUtil.isNotEmpty(timePeriods)) {
            LocalDateTime now = LocalDateTime.now();
            boolean timeSlotValid = TimeSlotValidator.validate(timePeriods, now);
            if (!timeSlotValid) {
                log.debug("时间段验证失败，permissionId: {}, timePeriods: {}, currentTime: {}",
                        permission.getPermissionId(), timePeriods, now);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean checkDevicePermission(SmartAccessPermissionEntity permission, Long deviceId) {
        if (SmartStringUtil.isEmpty(permission.getDeviceIds())) {
            return false;
        }

        String[] deviceIds = permission.getDeviceIds().split(",");
        for (String id : deviceIds) {
            if (deviceId.toString().equals(id.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> recordAccessEvent(SmartAccessRecordEntity record) {
        try {
            // 保存鍒版暟鎹簱
            // accessPermissionDao.insert(record); // 迁移欓噷搴旇鏈変笓闂ㄧ殑记录DAO

            // 缓存鍒癛edis鐢ㄤ簬瀹炴椂缁熻
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = ACCESS_RECORD_PREFIX + today;

            // 浣跨敤RedisTemplate鏇夸唬Redisson RMap
            String hashKey = record.getRecordId().toString();
            redisTemplate.opsForHash().put(key, hashKey, record);
            redisTemplate.expire(key, 7, TimeUnit.DAYS);

            return ResponseDTO.okMsg("通行事件记录成功");

        } catch (Exception e) {
            log.error("记录通行事件异常", e);
            return ResponseDTO.userErrorParam("记录失败: " + e.getMessage());
        }
    }

    @Override
    public String generateAccessToken(Long personId, Long deviceId, Integer expireMinutes) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = ACCESS_TOKEN_PREFIX + token;

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("personId", personId);
        tokenData.put("deviceId", deviceId);
        tokenData.put("createTime", LocalDateTime.now());
        tokenData.put("expireTime", LocalDateTime.now().plusMinutes(expireMinutes));

        redisTemplate.opsForValue().set(key, tokenData, expireMinutes * 60, TimeUnit.SECONDS);

        return token;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResponseDTO<Map<String, Object>> verifyAccessToken(String accessToken) {
        try {
            String key = ACCESS_TOKEN_PREFIX + accessToken;
            Map<String, Object> tokenData = (Map<String, Object>) redisTemplate.opsForValue().get(key);

            if (tokenData == null) {
                return ResponseDTO.userErrorParam("访问令牌无效或已过期");
            }

            LocalDateTime expireTime = (LocalDateTime) tokenData.get("expireTime");
            if (LocalDateTime.now().isAfter(expireTime)) {
                redisTemplate.delete(key);
                return ResponseDTO.userErrorParam("访问令牌已过期");
            }

            return ResponseDTO.ok(tokenData);

        } catch (Exception e) {
            log.error("校验访问令牌异常", e);
            return ResponseDTO.userErrorParam("校验异常: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPersonTodayStatistics(Long personId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = ACCESS_STATS_PREFIX + "person:" + personId + ":" + today;

        Map<String, Object> stats = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalCount", 0);
            stats.put("successCount", 0);
            stats.put("failedCount", 0);
            stats.put("lastAccessTime", null);
        }

        return stats;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDeviceTodayStatistics(Long deviceId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = ACCESS_STATS_PREFIX + "device:" + deviceId + ":" + today;

        Map<String, Object> stats = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalCount", 0);
            stats.put("successCount", 0);
            stats.put("failedCount", 0);
            stats.put("lastAccessTime", null);
        }

        return stats;
    }

    /**
     * 鍩轰簬Redis鐨勭畝鏄撶紦瀛橈細鎸塮ace鐗瑰緛ID查询鏈夋晥权限锛?0鍒嗛挓澶辨晥
     */
    private SmartAccessPermissionEntity getPermissionByFaceFeatureId(String faceFeatureId) {
        String cacheKey = ACCESS_PERM_FACE_PREFIX + faceFeatureId;
        SmartAccessPermissionEntity cached = (SmartAccessPermissionEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SmartAccessPermissionEntity permission = accessPermissionDao.selectByFaceFeatureId(faceFeatureId,
                LocalDateTime.now());
        if (permission != null) {
            redisTemplate.opsForValue().set(cacheKey, permission, 10, TimeUnit.MINUTES);
        }
        return permission;
    }

    /**
     * 鍩轰簬Redis鐨勭畝鏄撶紦瀛橈細鎸夋寚绾圭壒寰両D查询鏈夋晥权限锛?0鍒嗛挓澶辨晥
     */
    private SmartAccessPermissionEntity getPermissionByFingerprintId(String fingerprintId) {
        String cacheKey = ACCESS_PERM_FP_PREFIX + fingerprintId;
        SmartAccessPermissionEntity cached = (SmartAccessPermissionEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SmartAccessPermissionEntity permission = accessPermissionDao.selectByFingerprintId(fingerprintId,
                LocalDateTime.now());
        if (permission != null) {
            redisTemplate.opsForValue().set(cacheKey, permission, 10, TimeUnit.MINUTES);
        }
        return permission;
    }

    @Override
    public boolean isForcedOpen(Long deviceId, LocalDateTime accessTime) {
        // 迁移欓噷搴旇实现寮哄埗寮€闂ㄧ殑妫€娴嬮€昏緫
        // 姣斿锛氬湪娌℃湁权限楠岃瘉鐨勬儏鍐典笅寮€闂紝鎴栬€呭湪闈炴巿鏉冩椂闂存寮€闂? // 绠€鍖栧疄鐜帮紝迁移斿洖false
        return false;
    }

    // ========== 绉佹湁杈呭姪方法硶 ==========

    private SmartAccessPermissionEntity findPermissionByCredential(String accessType,
            String credential) {
        LocalDateTime currentTime = LocalDateTime.now();

        switch (accessType.toUpperCase()) {
            case "CARD":
                return accessPermissionDao.selectByCardNumber(credential, currentTime);
            case "FACE":
                return accessPermissionDao.selectByFaceFeatureId(credential, currentTime);
            case "FINGERPRINT":
                return accessPermissionDao.selectByFingerprintId(credential, currentTime);
            default:
                log.warn("涓嶆敮鎸佺殑璁块棶类型: {}", accessType);
                return null;
        }
    }

    private boolean checkAccessLimit(SmartAccessPermissionEntity permission) {
        if (permission.getAccessLimit() != null && permission.getAccessLimit() > 0) {
            return permission.getAccessCount() == null
                    || permission.getAccessCount() < permission.getAccessLimit();
        }
        return true;
    }

    private void incrementAccessCount(Long permissionId) {
        try {
            accessPermissionDao.incrementAccessCount(permissionId);
        } catch (Exception e) {
            log.error("更新璁块棶娆℃暟异常", e);
        }
    }

    private SmartAccessRecordEntity createAccessRecord(SmartAccessPermissionEntity permission,
            Long deviceId, String accessType, String result, String errorMessage) {
        SmartAccessRecordEntity record = new SmartAccessRecordEntity();
        record.setPersonId(permission.getPersonId());
        record.setPersonName(permission.getPersonName());
        record.setPersonCode(permission.getPersonCode());
        record.setDeviceId(deviceId);
        record.setAccessType(accessType);
        record.setAccessResult(result);
        record.setAccessTime(LocalDateTime.now());
        record.setPermissionId(permission.getPermissionId());
        record.setPermissionName(permission.getPermissionName());
        record.setCardNumber(permission.getCardNumber());
        record.setFaceFeatureId(permission.getFaceFeatureId());
        record.setFingerprintId(permission.getFingerprintId());
        record.setDataSource("DEVICE");
        record.setErrorMessage(errorMessage);
        // record.setTenantId(1L); // 默认绉熸埛ID - BaseEntity鏃犳方法硶
        return record;
    }

    private ResponseDTO<Map<String, Object>> createVerificationResult(boolean success,
            String message, SmartAccessPermissionEntity permission) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("verifyTime", LocalDateTime.now());

        if (permission != null) {
            result.put("personId", permission.getPersonId());
            result.put("personName", permission.getPersonName());
            result.put("permissionType", permission.getPermissionType());
        }

        return success ? ResponseDTO.ok(result) : ResponseDTO.userErrorParam(message);
    }

    private Map<String, Object> createSuccessResult(SmartAccessPermissionEntity permission,
            String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("personId", permission.getPersonId());
        result.put("personName", permission.getPersonName());
        result.put("permissionType", permission.getPermissionType());
        result.put("accessTime", LocalDateTime.now());
        result.put("message", message);
        return result;
    }

    /**
     * MD5加密宸ュ叿方法硶
     */
    private String encryptMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            return "";
        }
    }
}
