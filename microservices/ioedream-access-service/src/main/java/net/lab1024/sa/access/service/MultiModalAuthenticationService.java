package net.lab1024.sa.access.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;

import java.util.List;

/**
 * 多模态认证服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义
 * - 使用ResponseDTO统一响应格式
 * </p>
 * <p>
 * 核心职责：
 * - 提供多模态认证管理API
 * - 支持认证方式配置和查询
 * - 提供认证方式统计和分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface MultiModalAuthenticationService {

    /**
     * 获取所有支持的认证方式
     *
     * @return 认证方式列表
     */
    ResponseDTO<List<VerifyTypeEnum>> getSupportedVerifyTypes();

    /**
     * 获取认证方式详情
     *
     * @param verifyType 认证方式代码
     * @return 认证方式详情
     */
    ResponseDTO<VerifyTypeEnum> getVerifyTypeDetail(Integer verifyType);

    /**
     * 获取认证方式统计信息
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 认证方式统计信息
     */
    ResponseDTO<Object> getVerifyTypeStatistics(String startTime, String endTime);
}
