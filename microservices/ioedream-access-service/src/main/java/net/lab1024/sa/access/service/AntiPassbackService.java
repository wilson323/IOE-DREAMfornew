package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackQueryForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 反潜回管理服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 使用@Resource依赖注入
 * </p>
 * <p>
 * 核心职责：
 * - 反潜回验证
 * - 反潜回记录管理
 * - 反潜回配置管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AntiPassbackService {

    /**
     * 反潜回验证
     * <p>
     * 检查同一用户是否从正确的门进出
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param inOutStatus 进出状态（1=进, 2=出）
     * @param areaId 区域ID
     * @return 是否通过验证
     */
    ResponseDTO<Boolean> verifyAntiPassback(Long userId, Long deviceId, Integer inOutStatus, Long areaId);

    /**
     * 记录反潜回验证结果
     * <p>
     * 验证通过后记录本次进出
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param inOutStatus 进出状态
     * @param verifyType 验证方式
     * @return 操作结果
     */
    ResponseDTO<Void> recordAntiPassback(Long userId, Long deviceId, Long areaId,
                                         Integer inOutStatus, Integer verifyType);

    /**
     * 分页查询反潜回记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(AntiPassbackQueryForm queryForm);

    /**
     * 获取反潜回配置
     *
     * @param areaId 区域ID
     * @return 反潜回配置
     */
    ResponseDTO<AntiPassbackConfigVO> getConfig(Long areaId);

    /**
     * 更新反潜回配置
     *
     * @param configForm 配置表单
     * @return 操作结果
     */
    ResponseDTO<Void> updateConfig(AntiPassbackConfigForm configForm);
}
