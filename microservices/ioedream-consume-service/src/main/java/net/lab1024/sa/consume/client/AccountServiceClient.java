package net.lab1024.sa.consume.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceCheckRequest;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.client.dto.BalanceCheckResult;
import net.lab1024.sa.consume.client.fallback.AccountServiceClientFallback;

/**
 * 账户服务Feign Client接口
 * <p>
 * 提供账户余额管理的跨服务调用能力：
 * - 余额增加：补贴发放、充值等场景
 * - 余额扣减：消费、补贴撤销等场景
 * - 余额查询：支付前校验余额是否充足
 * </p>
 *
 * <p>技术特性：</p>
 * <ul>
 *   <li>使用OpenFeign进行服务间调用</li>
 *   <li>集成降级策略（AccountServiceClientFallback）</li>
 *   <li>支持分布式事务（Seata）</li>
 *   <li>提供幂等性保证</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@FeignClient(
    name = "ioedream-account-service",
    path = "/api/v1/account",
    fallback = AccountServiceClientFallback.class
)
public interface AccountServiceClient {

    /**
     * 增加账户余额
     * <p>
     * 使用场景：
     * - 补贴发放：月度餐补、节日补贴、加班餐补等
     * - 充值：现金充值、银行转账等
     * - 退款：误消费退款、订单取消退款等
     * </p>
     *
     * @param request 余额增加请求
     * @return 余额变更结果
     */
    @PostMapping("/balance/increase")
    ResponseDTO<BalanceChangeResult> increaseBalance(@RequestBody BalanceIncreaseRequest request);

    /**
     * 扣减账户余额
     * <p>
     * 使用场景：
     * - 消费支付：扣除消费金额
     * - 补贴撤销：收回已发放的补贴
     * - 提现：用户提现申请
     * </p>
     *
     * @param request 余额扣减请求
     * @return 余额变更结果
     */
    @PostMapping("/balance/decrease")
    ResponseDTO<BalanceChangeResult> decreaseBalance(@RequestBody BalanceDecreaseRequest request);

    /**
     * 检查账户余额是否充足
     * <p>
     * 使用场景：
     * - 支付前校验：确保余额充足
     * - 批量操作前：批量检查多个账户
     * </p>
     *
     * @param request 余额检查请求
     * @return 余额检查结果
     */
    @PostMapping("/balance/check")
    ResponseDTO<BalanceCheckResult> checkBalance(@RequestBody BalanceCheckRequest request);

    /**
     * 查询账户余额
     *
     * @param userId 用户ID
     * @return 账户余额信息
     */
    @GetMapping("/balance/query")
    ResponseDTO<BalanceChangeResult> queryBalance(@RequestParam("userId") Long userId);

    /**
     * 冻结账户余额
     *
     * @param userId 用户ID
     * @param amount 冻结金额
     * @param businessNo 业务编号
     * @return 操作结果
     */
    @PostMapping("/balance/freeze")
    ResponseDTO<BalanceChangeResult> freezeBalance(
        @RequestParam("userId") Long userId,
        @RequestParam("amount") java.math.BigDecimal amount,
        @RequestParam("businessNo") String businessNo
    );

    /**
     * 解冻账户余额
     *
     * @param userId 用户ID
     * @param amount 解冻金额
     * @param businessNo 业务编号
     * @return 操作结果
     */
    @PostMapping("/balance/unfreeze")
    ResponseDTO<BalanceChangeResult> unfreezeBalance(
        @RequestParam("userId") Long userId,
        @RequestParam("amount") java.math.BigDecimal amount,
        @RequestParam("businessNo") String businessNo
    );
}
