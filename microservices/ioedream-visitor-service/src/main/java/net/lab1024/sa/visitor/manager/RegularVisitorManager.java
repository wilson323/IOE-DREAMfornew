package net.lab1024.sa.visitor.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.visitor.dao.VisitorDao;
import net.lab1024.sa.common.entity.visitor.VisitorEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 常客管理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 常客信息查询和验证
 * - 通行证有效期检查
 * - 访问权限验证
 * - 黑名单检查
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class RegularVisitorManager {

    @Resource
    private VisitorDao visitorDao;

    /**
     * 根据访客ID查询常客信息
     *
     * @param visitorId 访客ID
     * @return 访客实体
     */
    public VisitorEntity getVisitorById(Long visitorId) {
        log.debug("[常客管理] 查询常客信息: visitorId={}", visitorId);

        VisitorEntity visitor = visitorDao.selectById(visitorId);

        if (visitor == null) {
            log.warn("[常客管理] 常客不存在: visitorId={}", visitorId);
        }

        return visitor;
    }

    /**
     * 检查访客是否为常客
     * <p>
     * 常客判断标准：
     * - visitorLevel = "CONTRACTOR"（承包商）或 "VIP"（重要访客）
     * - 启用状态（shelvesFlag = true）
     * </p>
     *
     * @param visitor 访客实体
     * @return 是否为常客
     */
    public boolean isRegularVisitor(VisitorEntity visitor) {
        if (visitor == null) {
            return false;
        }

        // 检查启用状态
        if (visitor.getShelvesFlag() == null || !visitor.getShelvesFlag()) {
            log.debug("[常客管理] 访客未启用: visitorId={}", visitor.getVisitorId());
            return false;
        }

        // 检查访客等级
        String visitorLevel = visitor.getVisitorLevel();
        boolean isRegular = "VIP".equals(visitorLevel) || "CONTRACTOR".equals(visitorLevel);

        if (isRegular) {
            log.debug("[常客管理] 确认为常客: visitorId={}, level={}",
                    visitor.getVisitorId(), visitorLevel);
        } else {
            log.debug("[常客管理] 非常客: visitorId={}, level={}",
                    visitor.getVisitorId(), visitorLevel);
        }

        return isRegular;
    }

    /**
     * 检查访客是否在黑名单
     *
     * @param visitor 访客实体
     * @return 是否在黑名单
     */
    public boolean isVisitorBlacklisted(VisitorEntity visitor) {
        if (visitor == null) {
            return false;
        }

        boolean blacklisted = visitor.getBlacklisted() != null && visitor.getBlacklisted() == 1;

        if (blacklisted) {
            log.warn("[常客管理] 访客在黑名单: visitorId={}, reason={}",
                    visitor.getVisitorId(), visitor.getBlacklistReason());
        }

        return blacklisted;
    }

    /**
     * 验证访问权限
     * <p>
     * 检查访客的访问权限级别是否有效
     * </p>
     *
     * @param visitor 访客实体
     * @return 权限是否有效
     */
    public boolean validateAccessPermission(VisitorEntity visitor) {
        if (visitor == null) {
            log.warn("[常客管理] 访客为空，无法验证权限");
            return false;
        }

        // 检查访问权限级别
        Long accessLevelId = visitor.getAccessLevelId();
        if (accessLevelId == null) {
            log.warn("[常客管理] 访客无访问权限级别: visitorId={}", visitor.getVisitorId());
            return false;
        }

        // TODO: 这里可以扩展更复杂的权限验证逻辑
        // 例如：查询AccessLevelEntity，检查访问区域、时间限制等

        log.debug("[常客管理] 访问权限有效: visitorId={}, accessLevelId={}",
                visitor.getVisitorId(), accessLevelId);
        return true;
    }

    /**
     * 更新最后访问时间
     *
     * @param visitorId 访客ID
     * @return 更新是否成功
     */
    public boolean updateLastVisitTime(Long visitorId) {
        log.info("[常客管理] 更新最后访问时间: visitorId={}", visitorId);

        VisitorEntity visitor = visitorDao.selectById(visitorId);
        if (visitor == null) {
            log.warn("[常客管理] 访客不存在: visitorId={}", visitorId);
            return false;
        }

        visitor.setLastVisitTime(LocalDateTime.now());

        int rows = visitorDao.updateById(visitor);
        boolean success = rows > 0;

        if (success) {
            log.info("[常客管理] 最后访问时间更新成功: visitorId={}", visitorId);
        } else {
            log.error("[常客管理] 最后访问时间更新失败: visitorId={}", visitorId);
        }

        return success;
    }

    /**
     * 生成常客通行证ID
     * <p>
     * 格式：PASS_{visitorId}_{timestamp}
     * </p>
     *
     * @param visitorId 访客ID
     * @return 通行证ID
     */
    public String generatePassId(Long visitorId) {
        long timestamp = System.currentTimeMillis();
        return String.format("PASS_%d_%d", visitorId, timestamp);
    }
}
