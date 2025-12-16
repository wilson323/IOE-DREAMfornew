package net.lab1024.sa.consume.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.consume.domain.vo.MobileAccountInfoVO;
import net.lab1024.sa.consume.consume.manager.MobileAccountInfoManager;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.util.SmartRequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

/**
 * 移动端账户信息Service
 * <p>
 * 严格遵循CLAUDE.md四层架构规范：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@Tag(name = "移动端账户信息服务", description = "移动端账户信息相关功能")
public class MobileAccountInfoService {

    @Resource
    private MobileAccountInfoManager mobileAccountInfoManager;

    /**
     * 获取移动端账户信息
     *
     * @param accountId 账户ID
     * @param userId 用户ID（用于权限验证）
     * @return 账户信息
     */
    @Operation(summary = "获取移动端账户信息", description = "获取用户账户详细信息，包括余额、消费统计等")
    public ResponseDTO<MobileAccountInfoVO> getAccountInfo(
            @Parameter(description = "账户ID", required = true) Long accountId,
            @Parameter(description = "用户ID", required = false) Long userId) {

        log.info("[移动端账户信息服务] 开始查询账户信息, accountId={}, userId={}", accountId, userId);

        try {
            // 业务逻辑验证
            if (accountId == null) {
                return ResponseDTO.error("ACCOUNT_ID_REQUIRED", "账户ID不能为空");
            }

            // 如果没有提供userId，从SecurityContext获取
            if (userId == null) {
                userId = getCurrentUserId();
                if (userId == null) {
                    log.warn("[移动端账户信息服务] 无法获取当前用户ID，请确保已登录或提供userId参数");
                    return ResponseDTO.error("USER_ID_REQUIRED", "用户ID不能为空，请登录或提供userId参数");
                }
                log.info("[移动端账户信息服务] 从SecurityContext获取用户ID: {}", userId);
            }

            // 调用Manager层执行复杂业务逻辑
            MobileAccountInfoVO accountInfo = mobileAccountInfoManager.getAccountInfo(accountId, userId);

            log.info("[移动端账户信息服务] 查询完成, accountId={}, balance={}",
                    accountId, accountInfo.getBalance());

            return ResponseDTO.ok(accountInfo);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端账户信息服务] 参数验证失败, accountId={}, error={}", accountId, e.getMessage());
            return ResponseDTO.error("INVALID_PARAMETER", e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端账户信息服务] 业务异常, accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端账户信息服务] 系统异常, accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[移动端账户信息服务] 未知异常, accountId={}, error={}", accountId, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 获取当前用户ID
     * <p>
     * 从SecurityContext中获取当前登录用户的ID
     * 支持多种获取方式：
     * 1. 从HttpServletRequest属性中获取（由认证拦截器设置）
     * 2. 从JWT Token中解析（如果请求属性中没有）
     * 3. 降级处理：如果无法获取，返回null并记录警告
     * </p>
     *
     * @return 用户ID，如果无法获取则返回null
     */
    private Long getCurrentUserId() {
        try {
            // 方式1: 从HttpServletRequest属性中获取（由认证拦截器或过滤器设置）
            Long userId = SmartRequestUtil.getUserId();
            if (userId != null) {
                log.debug("[移动端账户信息服务] 从请求属性获取用户ID: {}", userId);
                return userId;
            }

            // 方式2: 如果请求属性中没有，尝试从请求头中获取Token并解析
            // 注意：这种方式需要JwtTokenUtil，但为了保持Service层独立性，暂不实现
            // 实际项目中，用户ID应该由认证拦截器统一设置到请求属性中

            // 如果无法获取用户ID，记录警告但不抛出异常（允许部分场景下继续执行）
            log.warn("[移动端账户信息服务] 无法获取当前用户ID，可能未登录或认证拦截器未设置用户ID");
            return null;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[移动端账户信息服务] 获取当前用户ID参数错误: error={}", e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[移动端账户信息服务] 获取当前用户ID业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.error("[移动端账户信息服务] 获取当前用户ID系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("[移动端账户信息服务] 获取当前用户ID未知异常: error={}", e.getMessage(), e);
            return null;
        }
    }
}



