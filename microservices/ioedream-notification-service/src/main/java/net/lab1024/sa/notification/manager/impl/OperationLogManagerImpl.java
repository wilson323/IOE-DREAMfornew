package net.lab1024.sa.notification.manager.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.notification.dao.OperationLogDao;
import net.lab1024.sa.notification.domain.entity.OperationLogEntity;
import net.lab1024.sa.notification.manager.OperationLogManager;

/**
 * 操作日志Manager实现
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Slf4j
@Component
public class OperationLogManagerImpl implements OperationLogManager {

    @Resource
    private OperationLogDao operationLogDao;

    @Override
    public void saveOperationLog(OperationLogEntity logEntity) {
        if (logEntity == null) {
            return;
        }

        try {
            operationLogDao.insert(logEntity);
            log.debug("操作日志保存成功，用户ID: {}, 操作类型: {}", logEntity.getUserId(), logEntity.getOperationType());
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<OperationLogEntity> queryLogList(Long userId, String operationType, Integer status, Integer pageNum, Integer pageSize) {
        QueryWrapper<OperationLogEntity> queryWrapper = new QueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }

        if (operationType != null && !operationType.trim().isEmpty()) {
            queryWrapper.like("operation_type", operationType);
        }

        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("create_time");

        if (pageNum != null && pageSize != null) {
            Page<OperationLogEntity> page = new Page<>(pageNum, pageSize);
            IPage<OperationLogEntity> pageResult = operationLogDao.selectPage(page, queryWrapper);
            return pageResult.getRecords();
        } else {
            return operationLogDao.selectList(queryWrapper);
        }
    }

    @Override
    public List<OperationLogEntity> queryLogsByUserId(Long userId, Integer limit) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<OperationLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        return operationLogDao.selectList(queryWrapper);
    }

    @Override
    public void cleanExpiredLogs(Integer days) {
        if (days == null || days <= 0) {
            return;
        }

        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
            QueryWrapper<OperationLogEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lt("create_time", expireTime);

            int deletedCount = operationLogDao.delete(queryWrapper);
            log.info("清理过期操作日志完成，删除数量: {}", deletedCount);

        } catch (Exception e) {
            log.error("清理过期操作日志失败: {}", e.getMessage(), e);
            throw e;
        }
    }
}