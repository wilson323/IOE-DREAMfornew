package net.lab1024.sa.notification.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import net.lab1024.sa.notification.dao.OperationLogDao;
import net.lab1024.sa.notification.domain.entity.OperationLogEntity;
import net.lab1024.sa.notification.manager.OperationLogManager;
import net.lab1024.sa.notification.service.OperationLogService;

/**
 * 操作日志Service实现
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLogEntity> implements OperationLogService {

    @Resource
    private OperationLogManager operationLogManager;

    @Override
    public void saveLog(OperationLogEntity logEntity) {
        try {
            operationLogManager.saveOperationLog(logEntity);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    public List<OperationLogEntity> queryLogList(Long userId, String operationType, Integer status, Integer pageNum, Integer pageSize) {
        try {
            return operationLogManager.queryLogList(userId, operationType, status, pageNum, pageSize);
        } catch (Exception e) {
            log.error("查询操作日志列表失败", e);
            return List.of();
        }
    }

    @Override
    public List<OperationLogEntity> queryLogsByUserId(Long userId, Integer limit) {
        try {
            return operationLogManager.queryLogsByUserId(userId, limit);
        } catch (Exception e) {
            log.error("根据用户ID查询操作日志失败", e);
            return List.of();
        }
    }

    @Override
    public void cleanExpiredLogs(Integer days) {
        try {
            operationLogManager.cleanExpiredLogs(days);
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
        }
    }
}