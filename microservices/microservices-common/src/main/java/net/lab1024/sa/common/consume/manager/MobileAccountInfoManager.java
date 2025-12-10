package net.lab1024.sa.common.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.domain.vo.MobileAccountInfoVO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 移动端账户信息Manager
 * <p>
 * 按照四层架构规范，Manager层负责复杂业务流程编排和多DAO数据组装
 * 严格遵循CLAUDE.md全局架构规范：
 * - microservices-common中的Manager类为纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖，保持为纯Java类
 * - 通过网关调用其他服务，禁止直接访问其他服务数据库
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class MobileAccountInfoManager {

    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public MobileAccountInfoManager(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 获取移动端账户信息
     *
     * @param accountId 账户ID
     * @param userId 用户ID
     * @return 移动端账户信息
     */
    public MobileAccountInfoVO getAccountInfo(Long accountId, Long userId) {
        log.info("[账户信息Manager] 开始查询账户信息, accountId={}, userId={}", accountId, userId);

        try {
            // 1. 参数验证
            validateParameters(accountId, userId);

            // 2. 通过网关调用AccountService获取账户信息
            Map<String, Object> account = getAccountFromGateway(accountId, userId);

            if (account == null) {
                throw new RuntimeException("账户不存在");
            }

            // 3. 获取账户消费统计信息
            AccountConsumeStats consumeStats = getAccountConsumeStats(accountId);

            // 4. 构建移动端账户信息
            MobileAccountInfoVO accountInfo = buildMobileAccountInfo(account, consumeStats);

            log.info("[账户信息Manager] 查询完成, accountId={}, balance={}",
                    accountId, accountInfo.getBalance());

            return accountInfo;

        } catch (Exception e) {
            log.error("[账户信息Manager] 查询异常, accountId={}, userId={}, error={}",
                    accountId, userId, e.getMessage(), e);
            throw new RuntimeException("获取账户信息失败", e);
        }
    }

    /**
     * 验证参数
     */
    private void validateParameters(Long accountId, Long userId) {
        if (accountId == null) {
            throw new IllegalArgumentException("账户ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
    }

    /**
     * 通过网关获取账户信息
     * 严格按照架构规范，所有服务间调用必须通过API网关
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAccountFromGateway(Long accountId, Long userId) {
        try {
            // 通过网关调用AccountService获取账户信息
            // 注意：这里使用GatewayServiceClient，禁止直接访问其他服务数据库
            return gatewayServiceClient.callConsumeService(
                    "/api/v1/account/" + accountId + "?userId=" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Map.class
            ).getData();
        } catch (Exception e) {
            log.warn("[账户信息Manager] 通过网关获取账户信息失败, accountId={}, error={}",
                    accountId, e.getMessage());
            // 降级处理：返回默认账户信息
            return createDefaultAccount(accountId, userId);
        }
    }

    /**
     * 降级处理：创建默认账户信息
     */
    private Map<String, Object> createDefaultAccount(Long accountId, Long userId) {
        Map<String, Object> account = new HashMap<>();
        account.put("accountId", accountId);
        account.put("userId", userId);
        account.put("accountNumber", "ACC" + String.format("%06d", accountId));
        account.put("balance", BigDecimal.ZERO);
        account.put("frozenAmount", BigDecimal.ZERO);
        account.put("status", "ACTIVE");
        account.put("accountType", "PERSONAL");
        account.put("createTime", LocalDateTime.now());
        account.put("updateTime", LocalDateTime.now());
        return account;
    }

    /**
     * 获取账户消费统计信息
     * 通过网关调用consume-service获取统计信息
     */
    @SuppressWarnings("unchecked")
    private AccountConsumeStats getAccountConsumeStats(Long accountId) {
        try {
            // 通过网关调用consume-service获取消费统计
            Map<String, Object> stats = gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/statistics/account/" + accountId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Map.class
            ).getData();

            if (stats == null) {
                return createDefaultStats();
            }

            return AccountConsumeStats.builder()
                    .todayConsumeCount((Integer) stats.getOrDefault("todayConsumeCount", 0))
                    .todayConsumeAmount(new BigDecimal(stats.getOrDefault("todayConsumeAmount", "0").toString()))
                    .monthConsumeCount((Integer) stats.getOrDefault("monthConsumeCount", 0))
                    .monthConsumeAmount(new BigDecimal(stats.getOrDefault("monthConsumeAmount", "0").toString()))
                    .lastConsumeTime(stats.get("lastConsumeTime") != null ?
                            LocalDateTime.parse(stats.get("lastConsumeTime").toString()) : null)
                    .lastConsumeAmount(new BigDecimal(stats.getOrDefault("lastConsumeAmount", "0").toString()))
                    .build();

        } catch (Exception e) {
            log.warn("[账户信息Manager] 获取消费统计失败, accountId={}, error={}", accountId, e.getMessage());
            // 返回默认统计信息
            return createDefaultStats();
        }
    }

    /**
     * 创建默认统计信息
     */
    private AccountConsumeStats createDefaultStats() {
        return AccountConsumeStats.builder()
                .todayConsumeCount(0)
                .todayConsumeAmount(BigDecimal.ZERO)
                .monthConsumeCount(0)
                .monthConsumeAmount(BigDecimal.ZERO)
                .lastConsumeTime(null)
                .lastConsumeAmount(BigDecimal.ZERO)
                .build();
    }

    /**
     * 构建移动端账户信息
     */
    private MobileAccountInfoVO buildMobileAccountInfo(Map<String, Object> account, AccountConsumeStats consumeStats) {
        MobileAccountInfoVO accountInfo = new MobileAccountInfoVO();

        // 基础账户信息
        accountInfo.setAccountId(((Number) account.getOrDefault("accountId", 0L)).longValue());
        accountInfo.setAccountNumber((String) account.getOrDefault("accountNumber", ""));
        accountInfo.setBalance(account.get("balance") != null ?
                new BigDecimal(account.get("balance").toString()) : BigDecimal.ZERO);
        accountInfo.setFrozenAmount(account.get("frozenAmount") != null ?
                new BigDecimal(account.get("frozenAmount").toString()) : BigDecimal.ZERO);

        // 可用余额 = 总余额 - 冻结金额
        BigDecimal availableBalance = accountInfo.getBalance().subtract(accountInfo.getFrozenAmount());
        accountInfo.setAvailableBalance(availableBalance);

        // 状态信息
        String status = (String) account.getOrDefault("status", "ACTIVE");
        accountInfo.setStatus(status);
        accountInfo.setStatusDescription(getStatusDescription(status));
        String accountType = (String) account.getOrDefault("accountType", "PERSONAL");
        accountInfo.setAccountType(accountType);
        accountInfo.setAccountTypeDescription(getAccountTypeDescription(accountType));

        // 用户信息（通过网关获取）
        Long userId = ((Number) account.getOrDefault("userId", 0L)).longValue();
        accountInfo.setUserName(getUserInfoFromGateway(userId));
        accountInfo.setUserPhone(maskPhone(getUserPhoneFromGateway(userId)));

        // 消费统计信息
        accountInfo.setLastConsumeTime(consumeStats.getLastConsumeTime());
        accountInfo.setLastConsumeAmount(consumeStats.getLastConsumeAmount());
        accountInfo.setTodayConsumeCount(consumeStats.getTodayConsumeCount());
        accountInfo.setTodayConsumeAmount(consumeStats.getTodayConsumeAmount());

        // 信用额度信息（从账户信息中获取）
        accountInfo.setCreditLimit(account.get("creditLimit") != null ?
                new BigDecimal(account.get("creditLimit").toString()) : BigDecimal.ZERO);
        accountInfo.setUsedCreditLimit(account.get("usedCreditLimit") != null ?
                new BigDecimal(account.get("usedCreditLimit").toString()) : BigDecimal.ZERO);
        BigDecimal availableCreditLimit = accountInfo.getCreditLimit().subtract(accountInfo.getUsedCreditLimit());
        accountInfo.setAvailableCreditLimit(availableCreditLimit);

        return accountInfo;
    }

    /**
     * 通过网关获取用户信息
     */
    private String getUserInfoFromGateway(Long userId) {
        try {
            return gatewayServiceClient.callCommonService(
                    "/api/v1/user/username/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    String.class
            ).getData();
        } catch (Exception e) {
            log.warn("[账户信息Manager] 获取用户信息失败, userId={}, error={}", userId, e.getMessage());
            return "用户" + userId;
        }
    }

    /**
     * 通过网关获取用户手机号
     */
    private String getUserPhoneFromGateway(Long userId) {
        try {
            return gatewayServiceClient.callCommonService(
                    "/api/v1/user/phone/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    String.class
            ).getData();
        } catch (Exception e) {
            log.warn("[账户信息Manager] 获取用户手机号失败, userId={}, error={}", userId, e.getMessage());
            return "138****8888";
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return "138****8888";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 获取账户状态描述
     */
    private String getStatusDescription(String status) {
        switch (status) {
            case "ACTIVE":
                return "正常";
            case "FROZEN":
                return "冻结";
            case "CLOSED":
                return "关闭";
            case "SUSPENDED":
                return "暂停";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取账户类型描述
     */
    private String getAccountTypeDescription(String accountType) {
        switch (accountType) {
            case "PERSONAL":
                return "个人账户";
            case "COMPANY":
                return "公司账户";
            case "FAMILY":
                return "家庭账户";
            case "TEMPORARY":
                return "临时账户";
            default:
                return "未知类型";
        }
    }

    /**
     * 账户消费统计内部类
     */
    @lombok.Data
    @lombok.Builder
    private static class AccountConsumeStats {
        private Integer todayConsumeCount;
        private BigDecimal todayConsumeAmount;
        private Integer monthConsumeCount;
        private BigDecimal monthConsumeAmount;
        private LocalDateTime lastConsumeTime;
        private BigDecimal lastConsumeAmount;
    }
}
