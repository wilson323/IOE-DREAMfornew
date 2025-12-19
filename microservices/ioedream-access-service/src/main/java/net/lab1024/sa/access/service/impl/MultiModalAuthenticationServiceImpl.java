package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.manager.MultiModalAuthenticationManager;
import net.lab1024.sa.access.service.MultiModalAuthenticationService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模态认证服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解注册为Spring Bean
 * - 使用@Resource注入依赖
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MultiModalAuthenticationServiceImpl implements MultiModalAuthenticationService {

    @Resource
    private MultiModalAuthenticationManager multiModalAuthenticationManager;

    /**
     * 获取所有支持的认证方式
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VerifyTypeEnum>> getSupportedVerifyTypes() {
        log.info("[多模态认证] 获取支持的认证方式列表");

        try {
            List<VerifyTypeEnum> verifyTypes = multiModalAuthenticationManager.getSupportedVerifyTypes();
            log.info("[多模态认证] 支持的认证方式数量: {}", verifyTypes.size());
            return ResponseDTO.ok(verifyTypes);
        } catch (Exception e) {
            log.error("[多模态认证] 获取认证方式列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_VERIFY_TYPES_ERROR", "获取认证方式列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取认证方式详情
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VerifyTypeEnum> getVerifyTypeDetail(Integer verifyType) {
        log.info("[多模态认证] 获取认证方式详情: verifyType={}", verifyType);

        try {
            if (verifyType == null) {
                return ResponseDTO.error("INVALID_PARAM", "认证方式代码不能为空");
            }

            VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.getByCode(verifyType);
            if (verifyTypeEnum == null) {
                return ResponseDTO.error("VERIFY_TYPE_NOT_FOUND", "不支持的认证方式: " + verifyType);
            }

            return ResponseDTO.ok(verifyTypeEnum);
        } catch (Exception e) {
            log.error("[多模态认证] 获取认证方式详情异常: verifyType={}, error={}", verifyType, e.getMessage(), e);
            return ResponseDTO.error("GET_VERIFY_TYPE_DETAIL_ERROR", "获取认证方式详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取认证方式统计信息
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Object> getVerifyTypeStatistics(String startTime, String endTime) {
        log.info("[多模态认证] 获取认证方式统计: startTime={}, endTime={}", startTime, endTime);

        try {
            // TODO: 实现认证方式统计逻辑
            // 1. 从通行记录表统计各认证方式的使用次数
            // 2. 计算各认证方式的成功率
            // 3. 分析各认证方式的使用趋势

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("message", "认证方式统计功能待实现");
            statistics.put("supportedTypes", multiModalAuthenticationManager.getSupportedVerifyTypes().size());

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[多模态认证] 获取认证方式统计异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_VERIFY_TYPE_STATISTICS_ERROR", "获取认证方式统计失败: " + e.getMessage());
        }
    }
}
