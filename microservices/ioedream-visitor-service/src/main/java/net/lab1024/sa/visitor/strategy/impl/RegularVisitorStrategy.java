package net.lab1024.sa.visitor.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.visitor.VisitorEntity;
import net.lab1024.sa.visitor.manager.RegularVisitorManager;
import net.lab1024.sa.visitor.strategy.IVisitorVerificationStrategy;
import net.lab1024.sa.common.factory.StrategyMarker;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 常客验证策略
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现边缘验证模式：常客支持边缘验证，设备端完成验证
 * </p>
 *
 * <p><strong>验证流程：</strong></p>
 * <ol>
 *   <li>查询常客信息</li>
 *   <li>验证常客身份（VIP或承包商）</li>
 *   <li>检查黑名单状态</li>
 *   <li>验证访问权限</li>
 *   <li>更新最后访问时间</li>
 *   <li>返回验证结果（生成通行证ID）</li>
 * </ol>
 *
 * <p><strong>架构说明：</strong></p>
 * <ul>
 *   <li>设备端已完成生物识别验证</li>
 *   <li>软件端只验证访问权限和记录通行结果</li>
 *   <li>无需查询预约记录，常客有长期通行权限</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
@StrategyMarker(name = "REGULAR_VISITOR", type = "VISITOR_VERIFICATION", priority = 90)
@Slf4j
public class RegularVisitorStrategy implements IVisitorVerificationStrategy {

    @Resource
    private RegularVisitorManager regularVisitorManager;

    @Override
    public String getStrategyName() {
        return "常客验证策略";
    }

    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[常客策略] 开始边缘验证: visitorId={}", visitorId);

        try {
            // 1. 查询常客信息
            VisitorEntity visitor = regularVisitorManager.getVisitorById(visitorId);
            if (visitor == null) {
                log.warn("[常客策略] 访客不存在: visitorId={}", visitorId);
                return VisitorVerificationResult.failed("访客不存在");
            }

            // 2. 验证常客身份（VIP或承包商）
            if (!regularVisitorManager.isRegularVisitor(visitor)) {
                log.warn("[常客策略] 非常客身份: visitorId={}, level={}",
                        visitorId, visitor.getVisitorLevel());
                return VisitorVerificationResult.failed("非常客身份，请使用临时访客通道");
            }

            // 3. 检查黑名单
            if (regularVisitorManager.isVisitorBlacklisted(visitor)) {
                log.warn("[常客策略] 访客在黑名单: visitorId={}, reason={}",
                        visitorId, visitor.getBlacklistReason());
                return VisitorVerificationResult.failed("访客在黑名单，禁止访问：" + visitor.getBlacklistReason());
            }

            // 4. 验证访问权限
            if (!regularVisitorManager.validateAccessPermission(visitor)) {
                log.warn("[常客策略] 访问权限无效: visitorId={}, accessLevelId={}",
                        visitorId, visitor.getAccessLevelId());
                return VisitorVerificationResult.failed("访问权限无效，请联系管理员");
            }

            // 5. 更新最后访问时间（异步，不阻塞验证流程）
            try {
                regularVisitorManager.updateLastVisitTime(visitorId);
            } catch (Exception e) {
                log.error("[常客策略] 更新最后访问时间失败: visitorId={}, error={}",
                        visitorId, e.getMessage(), e);
                // 更新失败不影响验证结果
            }

            // 6. 生成通行证ID（常客通行证已预先下发到设备）
            String passId = regularVisitorManager.generatePassId(visitorId);

            log.info("[常客策略] 验证成功: visitorId={}, passId={}, level={}",
                    visitorId, passId, visitor.getVisitorLevel());

            // 边缘验证成功，无需生成模板（模板已在设备端）
            return VisitorVerificationResult.success(null, passId);

        } catch (Exception e) {
            log.error("[常客策略] 验证异常: visitorId={}, error={}",
                    visitorId, e.getMessage(), e);
            return VisitorVerificationResult.failed("验证异常：" + e.getMessage());
        }
    }

    @Override
    public int getPriority() {
        return 90; // 常客策略优先级较低（临时访客优先级更高）
    }

    @Override
    public String getStrategyType() {
        return "REGULAR_VISITOR";
    }
}
