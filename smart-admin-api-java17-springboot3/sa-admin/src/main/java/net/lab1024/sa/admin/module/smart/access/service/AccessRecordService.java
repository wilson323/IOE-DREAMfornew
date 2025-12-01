package net.lab1024.sa.admin.module.smart.access.service;

import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * Access record service interface
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
public interface AccessRecordService {

    /**
     * Record access event
     */
    ResponseDTO<String> recordAccessEvent(AccessRecordEntity record);

    /**
     * Query access records by page
     */
    PageResult<AccessRecordEntity> queryAccessRecordPage(PageParam pageParam, Long userId, Long deviceId,
            String accessResult);

    /**
     * Get access record detail
     */
    ResponseDTO<AccessRecordEntity> getAccessRecordDetail(Long recordId);

    /**
     * Verify access permission
     */
    ResponseDTO<Boolean> verifyAccessPermission(Long userId, Long deviceId);
}