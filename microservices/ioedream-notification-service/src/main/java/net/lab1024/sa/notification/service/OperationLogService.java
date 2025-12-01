package net.lab1024.sa.notification.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import net.lab1024.sa.notification.domain.entity.OperationLogEntity;

/**
 * 操作日志Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
public interface OperationLogService extends IService<OperationLogEntity> {

    /**
     * 保存操作日志
     *
     * @param logEntity 日志实体
     */
    void saveLog(OperationLogEntity logEntity);

    /**
     * 查询操作日志列表
     *
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param status 状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 日志列表
     */
    List<OperationLogEntity> queryLogList(Long userId, String operationType, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID查询操作日志
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 日志列表
     */
    List<OperationLogEntity> queryLogsByUserId(Long userId, Integer limit);

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     */
    void cleanExpiredLogs(Integer days);
}