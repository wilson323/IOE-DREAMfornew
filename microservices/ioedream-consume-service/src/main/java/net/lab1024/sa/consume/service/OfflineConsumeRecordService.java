package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.common.entity.consume.OfflineConsumeRecordEntity;

import java.util.List;

/**
 * 离线消费记录Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface OfflineConsumeRecordService extends IService<OfflineConsumeRecordEntity> {

    /**
     * 批量同步离线消费记录
     *
     * @param records 离线消费记录列表
     * @return 同步结果
     */
    Object batchSync(List<OfflineConsumeRecordEntity> records);

    /**
     * 查询用户待同步记录
     *
     * @param userId 用户ID
     * @return 待同步记录列表
     */
    List<OfflineConsumeRecordEntity> getPendingRecordsByUserId(Long userId);

    /**
     * 查询未解决冲突记录
     *
     * @return 冲突记录列表
     */
    List<OfflineConsumeRecordEntity> getUnresolvedConflicts();

    /**
     * 人工解决冲突
     *
     * @param recordId 记录ID
     * @param resolvedRemark 解决备注
     */
    void resolveConflict(String recordId, String resolvedRemark);
}
