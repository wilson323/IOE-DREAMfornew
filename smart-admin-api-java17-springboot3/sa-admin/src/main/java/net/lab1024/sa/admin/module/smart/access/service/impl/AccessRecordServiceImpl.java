package net.lab1024.sa.admin.module.smart.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessRecordDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.admin.module.smart.access.service.AccessRecordService;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * Access record service implementation
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
@Service
public class AccessRecordServiceImpl implements AccessRecordService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> recordAccessEvent(AccessRecordEntity record) {
        try {
            // Insert access record
            int result = accessRecordDao.insert(record);

            if (result > 0) {
                log.info("Access event recorded successfully: userId={}, deviceId={}",
                        record.getUserId(), record.getDeviceId());
                return ResponseDTO.ok("Recorded successfully");
            } else {
                return ResponseDTO.error("Failed to record access event");
            }
        } catch (Exception e) {
            log.error("Failed to record access event", e);
            throw new BusinessException("Failed to record access event: " + e.getMessage());
        }
    }

    @Override
    public PageResult<AccessRecordEntity> queryAccessRecordPage(PageParam pageParam, Long userId,
            Long deviceId, String accessResult) {
        try {
            // Build page object
            Page<?> page = SmartPageUtil.convert2PageQuery(pageParam);

            // Build query conditions
            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(userId != null, AccessRecordEntity::getUserId, userId)
                    .eq(deviceId != null, AccessRecordEntity::getDeviceId, deviceId)
                    .eq(accessResult != null, AccessRecordEntity::getAccessResult, accessResult)
                    .orderByDesc(AccessRecordEntity::getAccessTime);

            // Execute paginated query
            Page<AccessRecordEntity> resultPage =
                    accessRecordDao.selectPage((Page<AccessRecordEntity>) page, wrapper);
            return SmartPageUtil.convert2PageResult(resultPage, resultPage.getRecords());
        } catch (Exception e) {
            log.error("Failed to query access records", e);
            throw new BusinessException("Failed to query access records: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<AccessRecordEntity> getAccessRecordDetail(Long recordId) {
        try {
            // Query record detail
            AccessRecordEntity record = accessRecordDao.selectById(recordId);

            // Validate if record exists
            if (record == null) {
                return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST, "Access record not found");
            }

            return ResponseDTO.ok(record);
        } catch (Exception e) {
            log.error("Failed to get access record detail", e);
            throw new BusinessException("Failed to get access record detail: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> verifyAccessPermission(Long userId, Long deviceId) {
        try {
            // For now, default to allow access
            // In actual implementation, this would check user permissions
            Boolean hasPermission = true;

            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("Failed to verify access permission", e);
            throw new BusinessException("Failed to verify access permission: " + e.getMessage());
        }
    }
}