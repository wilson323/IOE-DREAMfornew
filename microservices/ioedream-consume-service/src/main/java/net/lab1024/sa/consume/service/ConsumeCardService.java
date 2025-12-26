package net.lab1024.sa.consume.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeCardLossForm;
import net.lab1024.sa.consume.domain.form.ConsumeCardUnlockForm;
import net.lab1024.sa.consume.domain.vo.ConsumeCardOperationVO;
import net.lab1024.sa.consume.domain.vo.ConsumeCardVO;

/**
 * 消费卡服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeCardService {

    /**
     * 获取用户的所有卡片
     *
     * @param userId 用户ID
     * @return 卡片列表
     */
    List<ConsumeCardVO> getUserCards(Long userId);

    /**
     * 获取卡片详情
     *
     * @param cardId 卡ID
     * @return 卡片详情
     */
    ConsumeCardVO getCardDetail(Long cardId);

    /**
     * 申请卡片挂失
     *
     * @param form 挂失表单
     * @return 操作结果
     */
    ConsumeCardOperationVO reportCardLoss(ConsumeCardLossForm form);

    /**
     * 申请卡片解挂
     *
     * @param form 解挂表单
     * @return 操作结果
     */
    ConsumeCardOperationVO reportCardUnlock(ConsumeCardUnlockForm form);

    /**
     * 获取卡片操作记录（分页）
     *
     * @param cardId 卡ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 操作记录
     */
    PageResult<ConsumeCardOperationVO> getCardOperations(Long cardId, Integer pageNum, Integer pageSize);

    /**
     * 获取操作历史记录（分页）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 操作记录
     */
    PageResult<ConsumeCardOperationVO> getOperationHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 验证卡片密码
     *
     * @param userId 用户ID
     * @param password 密码
     * @return 验证结果
     */
    Boolean verifyCardPassword(Long userId, String password);

    /**
     * 获取卡片统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getCardStatistics(Long userId);
}
