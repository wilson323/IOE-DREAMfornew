package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackDetectForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackDetectResultVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 门禁反潜回服务接口
 * <p>
 * 支持4种反潜回模式：
 * - 全局反潜回（mode=1）：跨所有区域检测
 * - 区域反潜回（mode=2）：同一区域内检测
 * - 软反潜回（mode=3）：记录告警但不阻止通行
 * - 硬反潜回（mode=4）：检测到违规时阻止通行
 * </p>
 * <p>
 * 性能要求：
 * - 检测响应时间 < 100ms
 * - 支持高并发（≥1000 TPS）
 * - Redis缓存最近通行记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AntiPassbackService {

    /**
     * 反潜回检测（核心方法）
     * <p>
     * 检测用户通行是否存在反潜回违规
     * </p>
     *
     * @param detectForm 检测请求
     * @return 检测结果（allowPass=true表示允许通行）
     */
    ResponseDTO<AntiPassbackDetectResultVO> detect(AntiPassbackDetectForm detectForm);

    /**
     * 批量反潜回检测
     *
     * @param detectForms 检测请求列表
     * @return 检测结果列表
     */
    ResponseDTO<List<AntiPassbackDetectResultVO>> batchDetect(List<AntiPassbackDetectForm> detectForms);

    /**
     * 创建反潜回配置
     *
     * @param configForm 配置表单
     * @return 配置ID
     */
    ResponseDTO<Long> createConfig(AntiPassbackConfigForm configForm);

    /**
     * 更新反潜回配置
     *
     * @param configForm 配置表单
     * @return 是否成功
     */
    ResponseDTO<Void> updateConfig(AntiPassbackConfigForm configForm);

    /**
     * 删除反潜回配置
     *
     * @param configId 配置ID
     * @return 是否成功
     */
    ResponseDTO<Void> deleteConfig(Long configId);

    /**
     * 查询反潜回配置详情
     *
     * @param configId 配置ID
     * @return 配置详情
     */
    ResponseDTO<AntiPassbackConfigVO> getConfig(Long configId);

    /**
     * 查询反潜回配置列表
     *
     * @param mode 模式（可选）
     * @param enabled 启用状态（可选）
     * @param areaId 区域ID（可选）
     * @return 配置列表
     */
    ResponseDTO<List<AntiPassbackConfigVO>> listConfigs(Integer mode, Integer enabled, Long areaId);

    /**
     * 查询反潜回检测记录（分页）
     *
     * @param userId 用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param areaId 区域ID（可选）
     * @param result 检测结果（可选）
     * @param handled 处理状态（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(
            Long userId, Long deviceId, Long areaId, Integer result, Integer handled,
            Integer pageNum, Integer pageSize
    );

    /**
     * 处理反潜回记录
     *
     * @param recordId 记录ID
     * @param handleRemark 处理备注
     * @return 是否成功
     */
    ResponseDTO<Void> handleRecord(Long recordId, String handleRemark);

    /**
     * 批量处理反潜回记录
     *
     * @param recordIds 记录ID列表
     * @param handled 处理状态（1-已处理 2-已忽略）
     * @param handleRemark 处理备注
     * @return 是否成功
     */
    ResponseDTO<Void> batchHandleRecords(List<Long> recordIds, Integer handled, String handleRemark);

    /**
     * 清除用户反潜回缓存
     * <p>
     * 用于用户权限变更或离职时清除缓存
     * </p>
     *
     * @param userId 用户ID
     * @return 清除的缓存数量
     */
    ResponseDTO<Integer> clearUserCache(Long userId);

    /**
     * 清除所有反潜回缓存
     * <p>
     * 用于配置变更时清除所有缓存
     * </p>
     *
     * @return 清除的缓存数量
     */
    ResponseDTO<Integer> clearAllCache();
}
