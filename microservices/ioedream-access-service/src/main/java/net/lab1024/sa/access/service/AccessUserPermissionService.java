package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.controller.AccessOfflineSyncController.SyncStatusVO;
import net.lab1024.sa.access.domain.form.OfflineRecordUploadForm;
import net.lab1024.sa.access.domain.form.OfflineRecordUploadForm.OfflineRecordItem;
import net.lab1024.sa.access.domain.form.PermissionQueryForm;
import net.lab1024.sa.access.domain.form.PermissionRenewForm;
import net.lab1024.sa.access.domain.vo.AccessPermissionStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessPermissionVO;
import net.lab1024.sa.access.domain.vo.OfflineRecordUploadResultVO;
import net.lab1024.sa.access.domain.vo.OfflineSyncDataVO;
import net.lab1024.sa.access.domain.entity.AccessUserPermissionEntity;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备权限服务
 * <p>
 * 提供门禁设备权限的基础查询能力和移动端API服务。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
public interface AccessUserPermissionService {

    // ========== 基础查询方法 ==========

    List<AccessUserPermissionEntity> listByArea(Long areaId);

    AccessUserPermissionEntity getValidPermission(Long userId, Long areaId);

    // ========== 移动端API方法 ==========

    /**
     * 分页查询用户权限
     */
    Page<AccessPermissionVO> getUserPermissionPage(Long userId, PermissionQueryForm queryForm, Page<AccessUserPermissionEntity> page);

    /**
     * 获取用户权限统计
     */
    AccessPermissionStatisticsVO getUserPermissionStatistics(Long userId);

    /**
     * 获取权限详情
     */
    AccessPermissionVO getPermissionDetail(Long permissionId);

    /**
     * 生成权限二维码
     */
    String generatePermissionQRCode(Long permissionId);

    /**
     * 获取通行记录
     */
    List<Map<String, Object>> getPermissionRecords(Long permissionId, Integer pageNum, Integer pageSize);

    /**
     * 获取权限历史
     */
    List<Map<String, Object>> getPermissionHistory(Long permissionId);

    /**
     * 续期权限
     */
    void renewPermission(Long permissionId, PermissionRenewForm renewForm);

    /**
     * 转移权限
     */
    void transferPermission(Long permissionId, Long targetUserId);

    /**
     * 冻结权限
     */
    void freezePermission(Long permissionId);

    /**
     * 解冻权限
     */
    void unfreezePermission(Long permissionId);

    /**
     * 获取过期统计
     */
    Map<String, Object> getExpiringStatistics(Long userId);

    /**
     * 获取即将过期列表
     */
    List<AccessPermissionVO> getExpiringPermissions(Long userId, Integer days);

    /**
     * 批量续期
     */
    Map<String, Object> batchRenewPermissions(List<Long> permissionIds, Integer duration);

    // ========== 离线同步方法 ==========

    /**
     * 获取离线同步数据
     */
    OfflineSyncDataVO getOfflineSyncData(Long userId, Long lastSyncTime, String dataType);

    /**
     * 上传离线通行记录
     */
    OfflineRecordUploadResultVO uploadOfflineRecords(Long userId, String deviceId, List<OfflineRecordItem> records);

    /**
     * 获取同步状态
     */
    SyncStatusVO getSyncStatus(Long userId);

    /**
     * 立即同步
     */
    OfflineSyncDataVO syncNow(Long userId, Long lastSyncTime, String dataType);
}
