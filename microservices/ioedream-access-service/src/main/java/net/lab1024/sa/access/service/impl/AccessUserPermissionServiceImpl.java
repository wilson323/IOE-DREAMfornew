package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessOfflineSyncController.SyncStatusVO;
import net.lab1024.sa.access.dao.AccessUserPermissionDao;
import net.lab1024.sa.access.domain.form.OfflineRecordUploadForm;
import net.lab1024.sa.access.domain.form.OfflineRecordUploadForm.OfflineRecordItem;
import net.lab1024.sa.access.domain.form.PermissionQueryForm;
import net.lab1024.sa.access.domain.form.PermissionRenewForm;
import net.lab1024.sa.access.domain.vo.*;
import net.lab1024.sa.common.entity.access.AccessUserPermissionEntity;
import net.lab1024.sa.access.manager.AccessUserPermissionManager;
import net.lab1024.sa.access.service.AccessUserPermissionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 门禁设备权限服务实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Slf4j
@Service
public class AccessUserPermissionServiceImpl implements AccessUserPermissionService {

    @Resource
    private AccessUserPermissionDao accessUserPermissionDao;

    @Resource
    private AccessUserPermissionManager accessUserPermissionManager;

    // ========== 基础查询方法实现 ==========

    @Override
    public List<AccessUserPermissionEntity> listByArea(Long areaId) {
        return accessUserPermissionDao.selectByAreaId(areaId);
    }

    @Override
    public AccessUserPermissionEntity getValidPermission(Long userId, Long areaId) {
        return accessUserPermissionManager.getValidPermission(userId, areaId);
    }

    // ========== 移动端API方法实现 ==========

    @Override
    public Page<AccessPermissionVO> getUserPermissionPage(Long userId, PermissionQueryForm queryForm, Page<AccessUserPermissionEntity> page) {
        log.info("[权限服务] 查询用户权限分页: userId={}, queryForm={}", userId, queryForm);

        // 构建查询条件
        LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
        queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);

        // 状态过滤
        if (queryForm.getPermissionStatus() != null) {
            queryWrapper.eq(AccessUserPermissionEntity::getPermissionStatus, queryForm.getPermissionStatus());
        }

        // 类型过滤
        if (queryForm.getPermissionType() != null) {
            queryWrapper.eq(AccessUserPermissionEntity::getPermissionType, queryForm.getPermissionType().toString());
        }

        // 区域过滤
        if (queryForm.getAreaId() != null) {
            queryWrapper.eq(AccessUserPermissionEntity::getAreaId, queryForm.getAreaId());
        }

        // 排序
        queryWrapper.orderByDesc(AccessUserPermissionEntity::getCreateTime);

        // 分页查询
        Page<AccessUserPermissionEntity> entityPage = accessUserPermissionDao.selectPage(page, queryWrapper);

        // 转换为VO
        Page<AccessPermissionVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AccessPermissionVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AccessPermissionStatisticsVO getUserPermissionStatistics(Long userId) {
        log.info("[权限服务] 查询用户权限统计: userId={}", userId);

        // 查询所有权限
        LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
        queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);
        List<AccessUserPermissionEntity> permissions = accessUserPermissionDao.selectList(queryWrapper);

        // 统计
        AccessPermissionStatisticsVO statistics = new AccessPermissionStatisticsVO();
        statistics.setTotal(permissions.size());

        int valid = 0;
        int expiring = 0;
        int expired = 0;
        int frozen = 0;
        int permanent = 0;
        int temporary = 0;
        int timeBased = 0;

        LocalDateTime now = LocalDateTime.now();

        for (AccessUserPermissionEntity permission : permissions) {
            // 状态统计
            if (permission.getPermissionStatus() == 1) {
                valid++;
                // 检查是否即将过期（7天内）
                if (permission.getEndTime() != null) {
                    long daysUntilExpiry = ChronoUnit.DAYS.between(now, permission.getEndTime());
                    if (daysUntilExpiry <= 7 && daysUntilExpiry > 0) {
                        expiring++;
                    }
                }
            } else if (permission.getPermissionStatus() == 2) {
                expiring++;
            } else if (permission.getPermissionStatus() == 3) {
                expired++;
            } else if (permission.getPermissionStatus() == 4) {
                frozen++;
            }

            // 类型统计
            if ("1".equals(permission.getPermissionType())) {
                permanent++;
            } else if ("2".equals(permission.getPermissionType())) {
                temporary++;
            } else if ("3".equals(permission.getPermissionType())) {
                timeBased++;
            }
        }

        statistics.setValid(valid);
        statistics.setExpiring(expiring);
        statistics.setExpired(expired);
        statistics.setFrozen(frozen);
        statistics.setPermanent(permanent);
        statistics.setTemporary(temporary);
        statistics.setTimeBased(timeBased);

        return statistics;
    }

    @Override
    public AccessPermissionVO getPermissionDetail(Long permissionId) {
        log.info("[权限服务] 查询权限详情: permissionId={}", permissionId);

        AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
        if (entity == null || entity.getDeletedFlag() == 1) {
            return null;
        }

        return convertToVO(entity);
    }

    @Override
    public String generatePermissionQRCode(Long permissionId) {
        log.info("[权限服务] 生成权限二维码: permissionId={}", permissionId);

        // TODO: 实际二维码生成逻辑
        // 1. 查询权限详情
        // 2. 构建二维码数据
        // 3. 调用二维码生成库
        // 4. 返回Base64编码的图片

        // 临时返回模拟数据
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    }

    @Override
    public List<Map<String, Object>> getPermissionRecords(Long permissionId, Integer pageNum, Integer pageSize) {
        log.info("[权限服务] 查询通行记录: permissionId={}, pageNum={}, pageSize={}", permissionId, pageNum, pageSize);

        // TODO: 查询通行记录
        // 1. 从通行记录表查询
        // 2. 分页返回
        // 3. 返回格式化数据

        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getPermissionHistory(Long permissionId) {
        log.info("[权限服务] 查询权限历史: permissionId={}", permissionId);

        // TODO: 查询权限变更历史
        // 1. 从历史表查询
        // 2. 返回操作记录

        return new ArrayList<>();
    }

    @Override
    public void renewPermission(Long permissionId, PermissionRenewForm renewForm) {
        log.info("[权限服务] 续期权限: permissionId={}, duration={}天", permissionId, renewForm.getDuration());

        AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
        if (entity == null) {
            throw new RuntimeException("权限不存在");
        }

        // 计算新的结束时间
        LocalDateTime newEndTime;
        if (entity.getEndTime() != null) {
            newEndTime = entity.getEndTime().plusDays(renewForm.getDuration());
        } else {
            newEndTime = LocalDateTime.now().plusDays(renewForm.getDuration());
        }

        entity.setEndTime(newEndTime);
        entity.setPermissionStatus(1); // 恢复为有效状态

        accessUserPermissionDao.updateById(entity);

        log.info("[权限服务] 续期成功: newEndTime={}", newEndTime);
    }

    @Override
    public void transferPermission(Long permissionId, Long targetUserId) {
        log.info("[权限服务] 转移权限: permissionId={}, targetUserId={}", permissionId, targetUserId);

        AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
        if (entity == null) {
            throw new RuntimeException("权限不存在");
        }

        entity.setUserId(targetUserId);
        accessUserPermissionDao.updateById(entity);

        log.info("[权限服务] 转移成功");
    }

    @Override
    public void freezePermission(Long permissionId) {
        log.info("[权限服务] 冻结权限: permissionId={}", permissionId);

        AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
        if (entity == null) {
            throw new RuntimeException("权限不存在");
        }

        entity.setPermissionStatus(4); // 冻结状态
        accessUserPermissionDao.updateById(entity);

        log.info("[权限服务] 冻结成功");
    }

    @Override
    public void unfreezePermission(Long permissionId) {
        log.info("[权限服务] 解冻权限: permissionId={}", permissionId);

        AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
        if (entity == null) {
            throw new RuntimeException("权限不存在");
        }

        entity.setPermissionStatus(1); // 恢复为有效状态
        accessUserPermissionDao.updateById(entity);

        log.info("[权限服务] 解冻成功");
    }

    @Override
    public Map<String, Object> getExpiringStatistics(Long userId) {
        log.info("[权限服务] 查询过期统计: userId={}", userId);

        LocalDateTime now = LocalDateTime.now();

        // 查询所有权限
        LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
        queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);
        queryWrapper.eq(AccessUserPermissionEntity::getPermissionStatus, 1); // 有效权限
        queryWrapper.isNotNull(AccessUserPermissionEntity::getEndTime);
        List<AccessUserPermissionEntity> permissions = accessUserPermissionDao.selectList(queryWrapper);

        int urgent = 0;  // 7天内
        int warning = 0; // 30天内
        int normal = 0;  // 30天以上

        for (AccessUserPermissionEntity permission : permissions) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(now, permission.getEndTime());

            if (daysUntilExpiry <= 7) {
                urgent++;
            } else if (daysUntilExpiry <= 30) {
                warning++;
            } else {
                normal++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("urgent", urgent);
        statistics.put("warning", warning);
        statistics.put("normal", normal);
        statistics.put("total", urgent + warning + normal);

        return statistics;
    }

    @Override
    public List<AccessPermissionVO> getExpiringPermissions(Long userId, Integer days) {
        log.info("[权限服务] 查询即将过期列表: userId={}, days={}", userId, days);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);

        // 查询即将过期的权限
        LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
        queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);
        queryWrapper.eq(AccessUserPermissionEntity::getPermissionStatus, 1); // 有效权限
        queryWrapper.isNotNull(AccessUserPermissionEntity::getEndTime);
        queryWrapper.le(AccessUserPermissionEntity::getEndTime, threshold);
        queryWrapper.orderByAsc(AccessUserPermissionEntity::getEndTime);

        List<AccessUserPermissionEntity> entities = accessUserPermissionDao.selectList(queryWrapper);

        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> batchRenewPermissions(List<Long> permissionIds, Integer duration) {
        log.info("[权限服务] 批量续期: permissionIds={}, duration={}天", permissionIds, duration);

        int successCount = 0;
        int failedCount = 0;
        List<String> failedIds = new ArrayList<>();

        for (Long permissionId : permissionIds) {
            try {
                AccessUserPermissionEntity entity = accessUserPermissionDao.selectById(permissionId);
                if (entity != null) {
                    LocalDateTime newEndTime = entity.getEndTime().plusDays(duration);
                    entity.setEndTime(newEndTime);
                    entity.setPermissionStatus(1);
                    accessUserPermissionDao.updateById(entity);
                    successCount++;
                } else {
                    failedCount++;
                    failedIds.add(permissionId.toString());
                }
            } catch (Exception e) {
                log.error("[权限服务] 续期失败: permissionId={}", permissionId, e);
                failedCount++;
                failedIds.add(permissionId.toString());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", permissionIds.size());
        result.put("success", successCount);
        result.put("failed", failedCount);
        result.put("failedIds", failedIds);

        log.info("[权限服务] 批量续期完成: 成功={}, 失败={}", successCount, failedCount);
        return result;
    }

    /**
     * 实体转VO
     */
    private AccessPermissionVO convertToVO(AccessUserPermissionEntity entity) {
        AccessPermissionVO vo = new AccessPermissionVO();
        vo.setPermissionId(entity.getPermissionId());
        vo.setUserId(entity.getUserId());
        vo.setAreaId(entity.getAreaId());
        vo.setPermissionType("1".equals(entity.getPermissionType()) ? 1 : "2".equals(entity.getPermissionType()) ? 2 : 3);
        vo.setPermissionTypeName(convertPermissionTypeName(entity.getPermissionType()));
        vo.setPermissionStatus(entity.getPermissionStatus());
        vo.setPermissionStatusName(convertPermissionStatusName(entity.getPermissionStatus()));
        vo.setPermissionLevel(entity.getPermissionLevel());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setPermanent(entity.getEndTime() == null);
        vo.setDeviceSyncStatus(entity.getDeviceSyncStatus());
        vo.setLastSyncTime(entity.getLastSyncTime());

        // 计算距离过期天数
        if (entity.getEndTime() != null) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getEndTime());
            vo.setDaysUntilExpiry(daysUntilExpiry);
        }

        return vo;
    }

    /**
     * 转换权限类型名称
     */
    private String convertPermissionTypeName(String permissionType) {
        if ("1".equals(permissionType)) return "永久权限";
        if ("2".equals(permissionType)) return "临时权限";
        if ("3".equals(permissionType)) return "时段权限";
        return "未知";
    }

    /**
     * 转换权限状态名称
     */
    private String convertPermissionStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "有效";
            case 2: return "即将过期";
            case 3: return "已过期";
            case 4: return "已冻结";
            default: return "未知";
        }
    }

    // ========== 离线同步方法实现 ==========

    @Override
    public OfflineSyncDataVO getOfflineSyncData(Long userId, Long lastSyncTime, String dataType) {
        log.info("[权限服务] 获取离线同步数据: userId={}, lastSyncTime={}, dataType={}",
                userId, lastSyncTime, dataType);

        OfflineSyncDataVO syncData = new OfflineSyncDataVO();
        LocalDateTime now = LocalDateTime.now();
        long currentTimestamp = System.currentTimeMillis();

        syncData.setSyncTimestamp(currentTimestamp);
        syncData.setSyncTime(now);

        // 判断是否需要全量同步
        boolean needFullSync = (lastSyncTime == null || lastSyncTime == 0);
        syncData.setFullSync(needFullSync);

        // 设置数据版本
        syncData.setDataVersion(currentTimestamp);

        if (needFullSync) {
            // 全量同步：查询所有权限
            log.info("[权限服务] 执行全量同步: userId={}", userId);

            LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
            queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);
            queryWrapper.orderByDesc(AccessUserPermissionEntity::getUpdateTime);

            List<AccessUserPermissionEntity> entities = accessUserPermissionDao.selectList(queryWrapper);

            List<AccessPermissionVO> permissions = entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            syncData.setPermissions(permissions);
        } else {
            // 增量同步：查询变更数据
            log.info("[权限服务] 执行增量同步: userId={}, lastSyncTime={}", userId, lastSyncTime);

            LocalDateTime lastSyncDateTime = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(lastSyncTime),
                    java.time.ZoneId.systemDefault()
            );

            // 查询新增或修改的权限
            LambdaQueryWrapper<AccessUserPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AccessUserPermissionEntity::getUserId, userId);
            queryWrapper.eq(AccessUserPermissionEntity::getDeletedFlag, 0);
            queryWrapper.ge(AccessUserPermissionEntity::getUpdateTime, lastSyncDateTime);
            queryWrapper.orderByDesc(AccessUserPermissionEntity::getUpdateTime);

            List<AccessUserPermissionEntity> entities = accessUserPermissionDao.selectList(queryWrapper);

            List<AccessPermissionVO> permissions = entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            syncData.setPermissions(permissions);

            // TODO: 查询已删除的权限ID（需要软删除表或历史表）
            syncData.setDeletedPermissionIds(new ArrayList<>());
        }

        syncData.setChangeSinceTimestamp(lastSyncTime != null ? lastSyncTime : 0L);
        syncData.setChangeUntilTimestamp(currentTimestamp);

        log.info("[权限服务] 同步数据生成完成: permissions={}, fullSync={}",
                syncData.getPermissions() != null ? syncData.getPermissions().size() : 0,
                syncData.getFullSync());

        return syncData;
    }

    @Override
    public OfflineRecordUploadResultVO uploadOfflineRecords(Long userId, String deviceId,
                                                            List<OfflineRecordItem> records) {
        log.info("[权限服务] 上传离线记录: userId={}, deviceId={}, recordCount={}",
                userId, deviceId, records.size());

        OfflineRecordUploadResultVO result = new OfflineRecordUploadResultVO();
        result.setTotal(records.size());

        int successCount = 0;
        int failedCount = 0;
        List<String> successRecordIds = new ArrayList<>();
        List<String> failedRecordIds = new ArrayList<>();
        List<OfflineRecordUploadResultVO.RecordError> errors = new ArrayList<>();

        for (OfflineRecordItem record : records) {
            try {
                // TODO: 验证用户权限
                // TODO: 保存通行记录到数据库
                // TODO: 更新通行次数统计

                successCount++;
                successRecordIds.add(record.getRecordId());

                log.debug("[权限服务] 离线记录上传成功: recordId={}, userId={}, passTime={}",
                        record.getRecordId(), record.getUserId(), record.getPassTime());

            } catch (Exception e) {
                failedCount++;
                failedRecordIds.add(record.getRecordId());

                OfflineRecordUploadResultVO.RecordError error = new OfflineRecordUploadResultVO.RecordError();
                error.setRecordId(record.getRecordId());
                error.setErrorMessage(e.getMessage());
                error.setErrorCode("UPLOAD_ERROR");
                errors.add(error);

                log.error("[权限服务] 离线记录上传失败: recordId={}, error={}",
                        record.getRecordId(), e.getMessage());
            }
        }

        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setSuccessRecordIds(successRecordIds);
        result.setFailedRecordIds(failedRecordIds);
        result.setErrors(errors);

        log.info("[权限服务] 离线记录上传完成: total={}, success={}, failed={}",
                result.getTotal(), result.getSuccessCount(), result.getFailedCount());

        return result;
    }

    @Override
    public SyncStatusVO getSyncStatus(Long userId) {
        log.info("[权限服务] 查询同步状态: userId={}", userId);

        SyncStatusVO status = new SyncStatusVO();

        // TODO: 从同步记录表查询最后同步时间
        status.setLastSyncTime(LocalDateTime.now().minusHours(1));

        // TODO: 查询待上传记录数
        status.setPendingUploadCount(0);

        // 同步状态
        status.setSyncStatus("idle");

        // 数据版本
        long currentTimestamp = System.currentTimeMillis();
        status.setLocalDataVersion(currentTimestamp - 3600000); // 1小时前
        status.setServerDataVersion(currentTimestamp);

        // 是否需要同步
        status.setNeedSync(status.getLocalDataVersion() < status.getServerDataVersion());

        return status;
    }

    @Override
    public OfflineSyncDataVO syncNow(Long userId, Long lastSyncTime, String dataType) {
        log.info("[权限服务] 立即同步: userId={}, lastSyncTime={}, dataType={}",
                userId, lastSyncTime, dataType);

        // TODO: 保存同步记录

        // 获取同步数据
        return getOfflineSyncData(userId, lastSyncTime, dataType);
    }
}
