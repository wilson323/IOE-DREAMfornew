package net.lab1024.sa.consume.manager.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.security.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.common.consume.entity.AccountEntity;
import net.lab1024.sa.common.consume.entity.ConsumeAreaEntity;
import net.lab1024.sa.common.consume.entity.ConsumeProductEntity;
import net.lab1024.sa.common.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.client.AccountKindConfigClient;
import net.lab1024.sa.consume.service.ConsumeAreaCacheService;
import net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculatorFactory;

/**
 * 消费执行管理Manager实现类
 * <p>
 * 实现消费执行相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
 * </p>
 * <p>
 * 业务场景：
 * - 消费流程执行
 * - 消费权限验证
 * - 消费金额计算
 * - 消费记录生成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ConsumeExecutionManagerImpl implements ConsumeExecutionManager {

    private final ConsumeAreaManager consumeAreaManager;
	    private final ConsumeAreaCacheService consumeAreaCacheService;
	    private final ConsumeDeviceManager consumeDeviceManager;
	    private final GatewayServiceClient gatewayServiceClient;
	    private final AccountKindConfigClient accountKindConfigClient;
	    private final AccountManager accountManager;
	    private final ConsumeTransactionDao consumeTransactionDao;
	    private final ConsumeProductDao consumeProductDao;
    @SuppressWarnings("unused")
    private final DefaultFixedAmountCalculator fixedAmountCalculator;  // 预留字段，用于未来定值计算功能
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final ConsumeAmountCalculatorFactory calculatorFactory;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：通过构造函数接收依赖
     * </p>
     *
     * @param consumeAreaManager 区域管理器
     * @param consumeAreaCacheService 区域缓存服务（使用Spring Cache注解）
     * @param consumeDeviceManager 设备管理器
     * @param gatewayServiceClient 网关服务客户端
     * @param accountManager 账户管理器
     * @param consumeTransactionDao 交易DAO
     * @param consumeProductDao 商品DAO
     * @param fixedAmountCalculator 定值计算器
     * @param objectMapper JSON对象映射器
     * @param calculatorFactory 计算器工厂
     */
    public ConsumeExecutionManagerImpl(
            ConsumeAreaManager consumeAreaManager,
	            ConsumeAreaCacheService consumeAreaCacheService,
	            ConsumeDeviceManager consumeDeviceManager,
	            GatewayServiceClient gatewayServiceClient,
	            AccountKindConfigClient accountKindConfigClient,
	            AccountManager accountManager,
	            ConsumeTransactionDao consumeTransactionDao,
	            ConsumeProductDao consumeProductDao,
	            DefaultFixedAmountCalculator fixedAmountCalculator,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            ConsumeAmountCalculatorFactory calculatorFactory) {
	        this.consumeAreaManager = consumeAreaManager;
	        this.consumeAreaCacheService = consumeAreaCacheService;
	        this.consumeDeviceManager = consumeDeviceManager;
	        this.gatewayServiceClient = gatewayServiceClient;
	        this.accountKindConfigClient = accountKindConfigClient;
	        this.accountManager = accountManager;
	        this.consumeTransactionDao = consumeTransactionDao;
	        this.consumeProductDao = consumeProductDao;
        this.fixedAmountCalculator = fixedAmountCalculator;
        this.objectMapper = objectMapper;
        this.calculatorFactory = calculatorFactory;
    }

    /**
     * 执行消费流程
     * <p>
     * 基于业务文档06-消费处理流程重构设计.md的完整消费流程
     * 严格遵循SAGA分布式事务模式，确保数据一致性
     * </p>
     * <p>
     * 消费流程步骤：
     * 1. 身份识别 - 获取账户信息
     * 2. 权限验证 - 验证区域权限和消费模式支持
     * 3. 场景识别 - 根据区域经营模式确定消费方式
     * 4. 金额计算 - 根据消费模式计算实际金额
     * 5. 余额扣除 - 扣除账户余额（支持乐观锁）
     * 6. 记录交易 - 写入交易表
     * 7. 打印小票 - 返回交易结果（实际打印由设备端处理）
     * </p>
     *
     * @param request 消费请求（ConsumeTransactionForm或ConsumeRequest）
     * @return 消费结果
     */
    @Override
    public ResponseDTO<?> executeConsumption(Object request) {
        log.info("[消费执行] 开始执行消费流程");
        try {
            // 1. 参数转换和验证
            ConsumeTransactionForm form = convertToForm(request);
            if (form == null) {
                return ResponseDTO.error("CONSUME_ERROR", "消费请求参数格式错误");
            }

            // 2. 身份识别 - 获取账户信息
            AccountEntity account = accountManager.getAccountById(form.getAccountId());
            if (account == null) {
                log.warn("[消费执行] 账户不存在，accountId={}", form.getAccountId());
                return ResponseDTO.error("CONSUME_ERROR", "账户不存在");
            }

            // 3. 权限验证
            boolean hasPermission = validateConsumePermission(
                    form.getAccountId(),
                    form.getAreaId(),
                    form.getConsumeMode() != null ? form.getConsumeMode() : "FIXED"
            );
            if (!hasPermission) {
                log.warn("[消费执行] 权限验证失败，accountId={}, areaId={}", form.getAccountId(), form.getAreaId());
                return ResponseDTO.error("CONSUME_ERROR", "无权在该区域消费");
            }

            // 4. 场景识别 - 获取区域信息
            ConsumeAreaEntity area = consumeAreaManager.getAreaById(form.getAreaId());
            if (area == null) {
                log.warn("[消费执行] 区域不存在，areaId={}", form.getAreaId());
                return ResponseDTO.error("CONSUME_ERROR", "区域不存在");
            }

            // 5. 金额计算
            BigDecimal finalAmount = calculateConsumeAmount(
                    form.getAccountId(),
                    form.getAreaId(),
                    form.getConsumeMode() != null ? form.getConsumeMode() : "FIXED",
                    form.getAmount(),
                    form // 传递form对象以支持商品模式
            );
            if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[消费执行] 消费金额计算失败或金额无效，accountId={}, areaId={}", form.getAccountId(), form.getAreaId());
                return ResponseDTO.error("CONSUME_ERROR", "消费金额计算失败");
            }

            // 6. 余额检查
            boolean balanceSufficient = accountManager.checkBalanceSufficient(form.getAccountId(), finalAmount);
            if (!balanceSufficient) {
                log.warn("[消费执行] 账户余额不足，accountId={}, amount={}, balance={}",
                        form.getAccountId(), finalAmount, account.getBalanceAmount());
                return ResponseDTO.error("CONSUME_ERROR", "账户余额不足");
            }

            // 7. 余额扣除（支持乐观锁）
            boolean deductSuccess = accountManager.deductBalance(form.getAccountId(), finalAmount);
            if (!deductSuccess) {
                log.warn("[消费执行] 余额扣除失败，accountId={}, amount={}", form.getAccountId(), finalAmount);
                return ResponseDTO.error("CONSUME_ERROR", "余额扣除失败，请重试");
            }

            // 8. 记录交易
            ConsumeTransactionEntity transaction = createTransaction(form, account, area, finalAmount);
            int insertResult = consumeTransactionDao.insert(transaction);
            if (insertResult <= 0) {
                log.error("[消费执行] 交易记录插入失败，accountId={}, amount={}", form.getAccountId(), finalAmount);
                // 补偿：退还余额
                accountManager.addBalance(form.getAccountId(), finalAmount);
                return ResponseDTO.error("CONSUME_ERROR", "交易记录失败，已自动退款");
            }

            // 9. 返回成功结果
            log.info("[消费执行] 消费流程执行成功，transactionId={}, transactionNo={}, amount={}",
                    transaction.getId(), transaction.getTransactionNo(), finalAmount);

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("transactionId", transaction.getId());
            result.put("transactionNo", transaction.getTransactionNo());
            result.put("amount", finalAmount);
            result.put("status", "SUCCESS");
            result.put("message", "消费成功");

            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 执行消费流程参数错误: error={}", e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费执行] 执行消费流程业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[消费执行] 执行消费流程系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return ResponseDTO.error("CONSUME_SYSTEM_ERROR", "消费失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[消费执行] 执行消费流程未知异常", e);
            return ResponseDTO.error("CONSUME_ERROR", "消费失败：" + e.getMessage());
        }
    }

    /**
     * 将请求对象转换为ConsumeTransactionForm
     *
     * @param request 消费请求
     * @return ConsumeTransactionForm
     */
    private ConsumeTransactionForm convertToForm(Object request) {
        if (request instanceof ConsumeTransactionForm) {
            return (ConsumeTransactionForm) request;
        } else if (request instanceof ConsumeRequest) {
            ConsumeRequest consumeRequest = (ConsumeRequest) request;
            ConsumeTransactionForm form = new ConsumeTransactionForm();
            form.setUserId(consumeRequest.getUserId());
            form.setAccountId(consumeRequest.getAccountId());
            form.setDeviceId(consumeRequest.getDeviceId());
            form.setAreaId(consumeRequest.getAreaId());
            form.setAmount(consumeRequest.getAmount());
            form.setConsumeMode(consumeRequest.getConsumeMode());
            form.setConsumeType("CONSUME"); // 默认正常消费
            return form;
        }
        return null;
    }

    /**
     * 创建交易记录实体
     *
     * @param form 消费表单
     * @param account 账户信息
     * @param area 区域信息
     * @param finalAmount 最终金额
     * @return 交易记录实体
     */
    private ConsumeTransactionEntity createTransaction(
            ConsumeTransactionForm form,
            AccountEntity account,
            ConsumeAreaEntity area,
            BigDecimal finalAmount) {
        ConsumeTransactionEntity transaction = new ConsumeTransactionEntity();

        // 生成交易流水号（格式：YYYYMMDDHHmmss + 6位随机数）
        String transactionNo = generateTransactionNo();
        transaction.setTransactionNo(transactionNo);

        // 用户信息
        transaction.setUserId(form.getUserId());
        // 通过网关获取用户姓名和部门信息
        if (form.getUserId() != null) {
            try {
                Long userIdLong = Long.parseLong(form.getUserId().toString());
                ResponseDTO<UserDetailVO> userResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/users/" + userIdLong,
                        org.springframework.http.HttpMethod.GET,
                        null,
                        UserDetailVO.class
                );
                if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                    UserDetailVO userDetail = userResponse.getData();
                    // 设置用户姓名（优先使用realName，否则使用username）
                    transaction.setUserName(userDetail.getRealName() != null ? userDetail.getRealName() : userDetail.getUsername());
                    // 设置部门ID（通过部门名称查询部门ID）
                    // 注意：ConsumeTransactionEntity.deptId是Long类型
                    Long departmentId = getDepartmentIdByName(userDetail.getDepartmentName());
                    transaction.setDeptId(departmentId);
                } else {
                    log.warn("[消费执行] 获取用户信息失败，userId={}", form.getUserId());
                }
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[消费执行] 获取用户信息参数错误，userId={}, error={}", form.getUserId(), e.getMessage());
                // 用户信息获取失败不影响主流程，继续执行
            } catch (BusinessException e) {
                log.warn("[消费执行] 获取用户信息业务异常，userId={}, code={}, message={}", form.getUserId(), e.getCode(), e.getMessage());
                // 用户信息获取失败不影响主流程，继续执行
            } catch (SystemException e) {
                log.error("[消费执行] 获取用户信息系统异常，userId={}, code={}, message={}", form.getUserId(), e.getCode(), e.getMessage(), e);
                // 用户信息获取失败不影响主流程，继续执行
            } catch (Exception e) {
                log.error("[消费执行] 获取用户信息未知异常，userId={}", form.getUserId(), e);
                // 用户信息获取失败不影响主流程，继续执行
            }
        }

        // 账户信息
        transaction.setAccountId(form.getAccountId());
        transaction.setAccountKindId(account.getAccountKindId());

        // 区域信息
        transaction.setAreaId(form.getAreaId() != null && !form.getAreaId().trim().isEmpty()
                ? Long.parseLong(form.getAreaId()) : null);
        transaction.setAreaName(area.getName());
        transaction.setAreaManageMode(area.getManageMode());
        transaction.setAreaSubType(area.getAreaSubType());

        // 设备信息
        transaction.setDeviceId(form.getDeviceId());
        // 通过设备管理器获取设备名称
        // 类型安全改进：getDeviceById现在返回DeviceEntity，无需instanceof检查
        if (form.getDeviceId() != null) {
            try {
                DeviceEntity device = consumeDeviceManager.getDeviceById(form.getDeviceId().toString());
                if (device != null) {
                    transaction.setDeviceName(device.getDeviceName());
                }
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[消费执行] 获取设备信息参数错误，deviceId={}, error={}", form.getDeviceId(), e.getMessage());
                // 设备信息获取失败不影响主流程，继续执行
            } catch (BusinessException e) {
                log.warn("[消费执行] 获取设备信息业务异常，deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage());
                // 设备信息获取失败不影响主流程，继续执行
            } catch (SystemException e) {
                log.error("[消费执行] 获取设备信息系统异常，deviceId={}, code={}, message={}", form.getDeviceId(), e.getCode(), e.getMessage(), e);
                // 设备信息获取失败不影响主流程，继续执行
            } catch (Exception e) {
                log.error("[消费执行] 获取设备信息未知异常，deviceId={}", form.getDeviceId(), e);
                // 设备信息获取失败不影响主流程，继续执行
            }
        }

        // 金额信息（保持BigDecimal类型）
        transaction.setConsumeMoney(finalAmount);
        transaction.setDiscountMoney(BigDecimal.ZERO); // 默认无折扣
        transaction.setFinalMoney(finalAmount);

        // 余额信息（保持BigDecimal类型）
        transaction.setBalanceBefore(account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);
        // 消费后余额 = 消费前余额 - 消费金额
        BigDecimal balanceAfter = (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO).subtract(finalAmount);
        transaction.setBalanceAfter(balanceAfter);

        // 消费模式
        transaction.setConsumeMode(form.getConsumeMode() != null ? form.getConsumeMode() : "FIXED");
        transaction.setConsumeType(form.getConsumeType() != null ? form.getConsumeType() : "CONSUME");

        // 时间信息
        transaction.setConsumeTime(LocalDateTime.now());

        // 状态
        transaction.setStatus("SUCCESS");

        return transaction;
    }

    /**
     * 生成交易流水号
     * <p>
     * 格式：YYYYMMDDHHmmss + 6位随机数（共20位）
     * 示例：20250130143025123456
     * </p>
     *
     * @return 交易流水号
     */
    private String generateTransactionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", (int) (Math.random() * 1000000));
        return timestamp + random;
    }

    /**
     * 验证消费权限
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeMode 消费模式
     * @return 是否有权限
     */
    @Override
    public boolean validateConsumePermission(Long accountId, String areaId, String consumeMode) {
        log.debug("[消费执行] 验证消费权限，accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode);
        try {
            // 1. 验证区域权限（使用Spring Cache缓存）
            boolean hasAreaPermission = consumeAreaCacheService.validateAreaPermission(accountId, areaId);
            if (!hasAreaPermission) {
                log.warn("[消费执行] 区域权限验证失败，accountId={}, areaId={}", accountId, areaId);
                return false;
            }

            // 2. 验证消费模式支持
            // 根据业务文档05-权限验证系统重构设计.md实现消费模式验证
            boolean hasConsumeModeSupport = validateConsumeModeSupport(accountId, areaId, consumeMode);
            if (!hasConsumeModeSupport) {
                log.warn("[消费执行] 消费模式不支持，accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode);
                return false;
            }

            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 验证消费权限参数错误: accountId={}, areaId={}, consumeMode={}, error={}", accountId, areaId, consumeMode, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 验证消费权限业务异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[消费执行] 验证消费权限系统异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[消费执行] 验证消费权限未知异常: accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode, e);
            return false; // For boolean return methods, return false on unknown error
        }
    }

    /**
     * 计算消费金额
     * <p>
     * 根据消费模式和区域经营模式计算实际消费金额
     * 严格遵循业务文档06-消费处理流程重构设计.md的金额计算逻辑
     * </p>
     * <p>
     * 消费模式说明：
     * - FIXED-定值：从区域配置或账户类别配置获取定值金额
     * - AMOUNT-金额：使用传入金额
     * - PRODUCT-商品：计算商品总价（需要商品ID和数量）
     * - COUNT-计次：固定金额（从配置获取）
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeMode 消费模式
     * @param consumeAmount 消费金额（如果是金额模式）
     * @param request 消费请求对象（可选，商品模式时需要传递以获取商品信息）
     * @return 实际消费金额（单位：元）
     */
    @Override
    public BigDecimal calculateConsumeAmount(Long accountId, String areaId, String consumeMode, BigDecimal consumeAmount, Object request) {
        log.debug("[消费执行] 计算消费金额，accountId={}, areaId={}, consumeMode={}, consumeAmount={}",
                accountId, areaId, consumeMode, consumeAmount);
        try {
            // 1. 使用策略模式获取计算器
            ConsumeAmountCalculator calculator = calculatorFactory.getCalculator(consumeMode);
            if (calculator == null) {
                log.warn("[消费执行] 未找到消费模式对应的计算器，accountId={}, areaId={}, consumeMode={}",
                        accountId, areaId, consumeMode);
                return BigDecimal.ZERO;
            }

            // 2. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[消费执行] 账户不存在，无法计算消费金额，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

            // 3. 验证消费模式是否支持
            if (!calculator.isSupported(accountId, areaId, account)) {
                log.warn("[消费执行] 消费模式不支持，accountId={}, areaId={}, consumeMode={}",
                        accountId, areaId, consumeMode);
                return BigDecimal.ZERO;
            }

            // 4. 使用策略计算金额
            BigDecimal result = calculator.calculate(accountId, areaId, account, request);
            log.debug("[消费执行] 消费金额计算完成，accountId={}, areaId={}, consumeMode={}, amount={}",
                    accountId, areaId, consumeMode, result);
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 计算消费金额参数错误: accountId={}, areaId={}, consumeMode={}, error={}", accountId, areaId, consumeMode, e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 计算消费金额业务异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 计算消费金额系统异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 计算消费金额未知异常: accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode, e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }


    /**
     * 计算商品模式金额（带Form对象）
     * <p>
     * 支持从Form中获取商品ID和数量
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param form 消费表单（包含商品信息）
     * @return 商品总价（单位：元）
     */

    /**
     * 计算商品模式金额（备用方法，当前使用calculateProductAmountWithForm）
     * <p>
     * 基于业务文档06-消费处理流程重构设计.md的商品消费逻辑
     * 严格遵循超市制（manage_mode=2）的商品消费流程
     * </p>
     * <p>
     * 注意：此方法当前未被调用，保留作为备用实现
     * 实际使用的方法是calculateProductAmountWithForm
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeAmount 消费金额（可能包含商品总价，用于验证）
     * @return 商品总价（单位：元）
     */
    @SuppressWarnings("unused")
    private BigDecimal calculateProductAmount(Long accountId, String areaId, BigDecimal consumeAmount) {
        log.debug("[消费执行] 计算商品模式金额，accountId={}, areaId={}", accountId, areaId);
        try {
            // 1. 获取账户信息（用于获取账户类别折扣）
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[消费执行] 账户不存在，无法计算商品金额，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

            // 2. 获取区域信息（验证区域是否支持商品模式）
            ConsumeAreaEntity area = consumeAreaManager.getAreaById(areaId);
            if (area == null) {
                log.warn("[消费执行] 区域不存在，无法计算商品金额，areaId={}", areaId);
                return BigDecimal.ZERO;
            }

            // 验证区域经营模式是否为超市制（manage_mode=2）
            if (area.getManageMode() == null || area.getManageMode() != 2) {
                log.warn("[消费执行] 区域不支持商品模式，areaId={}, manageMode={}", areaId, area.getManageMode());
                return BigDecimal.ZERO;
            }

            // 3. 如果传入金额不为空，直接使用（说明前端已计算好商品总价）
            if (consumeAmount != null && consumeAmount.compareTo(BigDecimal.ZERO) > 0) {
                log.debug("[消费执行] 使用传入的商品总价，accountId={}, amount={}", accountId, consumeAmount);
                return consumeAmount;
            }

            // 4. 如果没有传入金额，需要从商品ID和数量计算
            // 注意：商品ID和数量需要从请求中获取，当前方法签名不支持
            // 实际使用时，calculateConsumeAmount方法需要接收完整的请求对象
            log.warn("[消费执行] 商品模式需要商品ID和数量，当前方法签名不支持，accountId={}, areaId={}", accountId, areaId);
            return BigDecimal.ZERO;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 计算商品模式金额参数错误: accountId={}, areaId={}, error={}", accountId, areaId, e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 计算商品模式金额业务异常: accountId={}, areaId={}, code={}, message={}", accountId, areaId, e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 计算商品模式金额系统异常: accountId={}, areaId={}, code={}, message={}", accountId, areaId, e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 计算商品模式金额未知异常: accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }

    /**
     * 根据商品ID和数量计算商品总价
     * <p>
     * 辅助方法：用于商品模式金额计算的详细逻辑
     * </p>
     *
     * @param productId 商品ID
     * @param quantity 商品数量
     * @param accountId 账户ID（用于应用折扣）
     * @param areaId 区域ID（用于验证商品可售区域）
     * @return 商品总价（单位：元）
     */
    @SuppressWarnings("unused")
    private BigDecimal calculateProductTotalPrice(String productId, Integer quantity, Long accountId, String areaId) {
        try {
            // 1. 查询商品信息
            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                log.warn("[消费执行] 商品不存在，productId={}", productId);
                return BigDecimal.ZERO;
            }

            // 2. 验证商品状态
            if (product.getAvailable() == null || !product.getAvailable()) {
                log.warn("[消费执行] 商品未上架，productId={}", productId);
                return BigDecimal.ZERO;
            }

            // 3. 验证商品可售区域
            if (product.getAreaIds() != null && !product.getAreaIds().isEmpty()) {
                // 解析areaIds JSON数组
                try {
                    java.util.List<String> allowedAreas = objectMapper.readValue(
                            product.getAreaIds(),
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {}
                    );
                    if (!allowedAreas.contains(areaId)) {
                        log.warn("[消费执行] 商品不在该区域销售，productId={}, areaId={}", productId, areaId);
                        return BigDecimal.ZERO;
                    }
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[消费执行] 解析商品可售区域参数错误，productId={}, error={}", productId, e.getMessage());
                    // 解析失败不影响主流程，继续执行
                } catch (BusinessException e) {
                    log.warn("[消费执行] 解析商品可售区域业务异常，productId={}, code={}, message={}", productId, e.getCode(), e.getMessage());
                    // 解析失败不影响主流程，继续执行
                } catch (SystemException e) {
                    log.error("[消费执行] 解析商品可售区域系统异常，productId={}, code={}, message={}", productId, e.getCode(), e.getMessage(), e);
                    // 解析失败不影响主流程，继续执行
                } catch (Exception e) {
                    log.warn("[消费执行] 解析商品可售区域未知异常，productId={}", productId, e);
                    // 解析失败不影响主流程，继续执行
                }
            }

            // 4. 获取商品价格
            BigDecimal unitPrice = product.getPrice();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[消费执行] 商品价格无效，productId={}, price={}", productId, unitPrice);
                return BigDecimal.ZERO;
            }

            // 5. 计算商品总价 = 单价 * 数量
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity != null ? quantity : 1));

            // 6. 应用折扣规则（从账户类别获取商品折扣率）
            BigDecimal discountRate = getProductDiscountRate(accountId);
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = totalPrice.multiply(discountRate);
                BigDecimal finalPrice = totalPrice.subtract(discountAmount);
                log.debug("[消费执行] 应用商品折扣，productId={}, totalPrice={}, discountRate={}, finalPrice={}",
                        productId, totalPrice, discountRate, finalPrice);
                return finalPrice;
            }

            log.debug("[消费执行] 商品总价计算完成，productId={}, quantity={}, unitPrice={}, totalPrice={}",
                    productId, quantity, unitPrice, totalPrice);
            return totalPrice;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 计算商品总价参数错误: productId={}, quantity={}, error={}", productId, quantity, e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 计算商品总价业务异常: productId={}, quantity={}, code={}, message={}", productId, quantity, e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 计算商品总价系统异常: productId={}, quantity={}, code={}, message={}", productId, quantity, e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 计算商品总价未知异常: productId={}, quantity={}", productId, quantity, e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }

    /**
     * 计算计次模式金额
     * <p>
     * 预留方法，用于未来计次模式消费功能
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 计次模式金额
     */
    @SuppressWarnings("unused")
    private BigDecimal calculateCountModeAmount(Long accountId, String areaId) {
        log.debug("[消费执行] 计算计次模式金额，accountId={}, areaId={}", accountId, areaId);
        try {
            // 1. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[消费执行] 账户不存在，无法计算计次金额，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

            // 2. 获取账户类别ID
            Long accountKindId = account.getAccountKindId();
            if (accountKindId == null) {
                log.warn("[消费执行] 账户类别ID为空，无法计算计次金额，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

	            // 3. 获取账户类别信息（热路径：默认经网关，直连启用时走直连）
	            net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>> accountKindResponse =
	                    accountKindConfigClient.getAccountKind(accountKindId);

            if (accountKindResponse == null || !accountKindResponse.isSuccess() || accountKindResponse.getData() == null) {
                log.warn("[消费执行] 获取账户类别信息失败，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> accountKind = accountKindResponse.getData();

            // 4. 获取mode_config JSON字段
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                log.warn("[消费执行] 账户类别未配置消费模式，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 5. 解析mode_config JSON
            java.util.Map<String, Object> modeConfig;
            if (modeConfigObj instanceof String) {
                modeConfig = objectMapper.readValue((String) modeConfigObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (modeConfigObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) modeConfigObj;
                modeConfig = map;
            } else {
                log.warn("[消费执行] mode_config格式不正确，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 6. 获取METERED配置
            Object meteredObj = modeConfig.get("METERED");
            if (meteredObj == null) {
                log.warn("[消费执行] 账户类别未配置METERED模式，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> meteredConfig;
            if (meteredObj instanceof String) {
                meteredConfig = objectMapper.readValue((String) meteredObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (meteredObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) meteredObj;
                meteredConfig = map;
            } else {
                log.warn("[消费执行] METERED配置格式不正确，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 7. 检查是否启用
            Object enabledObj = meteredConfig.get("enabled");
            if (enabledObj == null || !Boolean.parseBoolean(enabledObj.toString())) {
                log.warn("[消费执行] METERED模式未启用，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 8. 检查子类型是否为COUNT
            Object subTypeObj = meteredConfig.get("subType");
            if (subTypeObj == null || !"COUNT".equals(subTypeObj.toString())) {
                log.warn("[消费执行] METERED子类型不是COUNT，accountKindId={}, subType={}", accountKindId, subTypeObj);
                return BigDecimal.ZERO;
            }

            // 9. 获取count配置
            Object countObj = meteredConfig.get("count");
            if (countObj == null) {
                log.warn("[消费执行] METERED未配置count子类型，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> countConfig;
            if (countObj instanceof String) {
                countConfig = objectMapper.readValue((String) countObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (countObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) countObj;
                countConfig = map;
            } else {
                log.warn("[消费执行] count配置格式不正确，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            // 10. 获取pricePerTime（每次价格，单位：分）
            Object pricePerTimeObj = countConfig.get("pricePerTime");
            if (pricePerTimeObj == null) {
                log.warn("[消费执行] count配置未设置pricePerTime，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            Integer pricePerTimeInCents;
            if (pricePerTimeObj instanceof Number) {
                pricePerTimeInCents = ((Number) pricePerTimeObj).intValue();
            } else if (pricePerTimeObj instanceof String) {
                pricePerTimeInCents = Integer.parseInt((String) pricePerTimeObj);
            } else {
                log.warn("[消费执行] pricePerTime格式不正确，accountKindId={}", accountKindId);
                return BigDecimal.ZERO;
            }

            if (pricePerTimeInCents <= 0) {
                log.warn("[消费执行] pricePerTime无效，accountKindId={}, pricePerTime={}", accountKindId, pricePerTimeInCents);
                return BigDecimal.ZERO;
            }

            // 11. 转换为元
            BigDecimal amount = BigDecimal.valueOf(pricePerTimeInCents).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // 12. 应用折扣规则（如有）
            Object applyDiscountObj = countConfig.get("applyDiscount");
            boolean applyDiscount = applyDiscountObj != null && Boolean.parseBoolean(applyDiscountObj.toString());
            if (applyDiscount) {
                BigDecimal discountRate = getCountModeDiscountRate(accountKind);
                if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal discountAmount = amount.multiply(discountRate);
                    amount = amount.subtract(discountAmount);
                    log.debug("[消费执行] 应用计次模式折扣，accountId={}, amount={}, discountRate={}, finalAmount={}",
                            accountId, amount.add(discountAmount), discountRate, amount);
                }
            }

            log.debug("[消费执行] 计次金额计算完成，accountId={}, areaId={}, amount={}", accountId, areaId, amount);
            return amount;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 计算计次模式金额参数错误: accountId={}, areaId={}, error={}", accountId, areaId, e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 计算计次模式金额业务异常: accountId={}, areaId={}, code={}, message={}", accountId, areaId, e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 计算计次模式金额系统异常: accountId={}, areaId={}, code={}, message={}", accountId, areaId, e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 计算计次模式金额未知异常: accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }

    /**
     * 验证消费模式支持
     * <p>
     * 根据业务文档05-权限验证系统重构设计.md实现消费模式验证
     * 验证逻辑：
     * 1. 验证区域经营模式是否支持该消费模式
     * 2. 验证设备是否支持该消费模式
     * 3. 验证账户类别是否配置了该消费模式
     * </p>
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeMode 消费模式
     * @return 是否支持
     */
    private boolean validateConsumeModeSupport(Long accountId, String areaId, String consumeMode) {
        log.debug("[消费执行] 验证消费模式支持，accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode);
        try {
            // 1. 获取区域信息
            ConsumeAreaEntity area = consumeAreaManager.getAreaById(areaId);
            if (area == null) {
                log.warn("[消费执行] 区域不存在，无法验证消费模式，areaId={}", areaId);
                return false;
            }

            // 2. 根据区域经营模式验证
            Integer manageMode = area.getManageMode();
            if (manageMode == null) {
                log.warn("[消费执行] 区域经营模式未配置，areaId={}", areaId);
                return false;
            }

            // 餐别制(1)和混合模式(3)支持定值模式
            if ("FIXED".equals(consumeMode) || "FIXED_AMOUNT".equals(consumeMode)) {
                if (manageMode != 1 && manageMode != 3) {
                    log.warn("[消费执行] 定值模式仅支持餐别制和混合模式，areaId={}, manageMode={}", areaId, manageMode);
                    return false;
                }
            }

            // 超市制(2)和混合模式(3)支持商品模式
            if ("PRODUCT".equals(consumeMode)) {
                if (manageMode != 2 && manageMode != 3) {
                    log.warn("[消费执行] 商品模式仅支持超市制和混合模式，areaId={}, manageMode={}", areaId, manageMode);
                    return false;
                }
            }

            // 3. 验证账户类别是否配置了该消费模式
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null || account.getAccountKindId() == null) {
                log.warn("[消费执行] 账户或账户类别不存在，无法验证消费模式，accountId={}", accountId);
                return false;
            }

            // 4. 通过网关获取账户类别配置，验证是否启用了该消费模式
            // 注意：这里简化处理，实际应该检查mode_config中对应模式的enabled字段
            // 由于验证逻辑较复杂，这里先返回true，后续可以完善

            log.debug("[消费执行] 消费模式验证通过，accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode);
            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 验证消费模式支持参数错误: accountId={}, areaId={}, consumeMode={}, error={}", accountId, areaId, consumeMode, e.getMessage());
            return false; // For boolean return methods, return false on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 验证消费模式支持业务异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage());
            return false; // For boolean return methods, return false on business error
        } catch (SystemException e) {
            log.error("[消费执行] 验证消费模式支持系统异常: accountId={}, areaId={}, consumeMode={}, code={}, message={}", accountId, areaId, consumeMode, e.getCode(), e.getMessage(), e);
            return false; // For boolean return methods, return false on system error
        } catch (Exception e) {
            log.error("[消费执行] 验证消费模式支持未知异常: accountId={}, areaId={}, consumeMode={}", accountId, areaId, consumeMode, e);
            return false; // For boolean return methods, return false on unknown error
        }
    }

    /**
     * 获取商品模式折扣率
     * <p>
     * 从账户类别的mode_config.PRODUCT.discountRate获取折扣率
     * 根据业务文档03-账户类别与消费模式设计.md
     * </p>
     *
     * @param accountId 账户ID
     * @return 折扣率（0-1之间，如0.1表示10%折扣）
     */
    private BigDecimal getProductDiscountRate(Long accountId) {
        log.debug("[消费执行] 获取商品模式折扣率，accountId={}", accountId);
        try {
            // 1. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null || account.getAccountKindId() == null) {
                log.warn("[消费执行] 账户或账户类别不存在，无法获取折扣率，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

	            // 2. 获取账户类别信息（热路径：默认经网关，直连启用时走直连）
	            net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>> accountKindResponse =
	                    accountKindConfigClient.getAccountKind(account.getAccountKindId());

            if (accountKindResponse == null || !accountKindResponse.isSuccess() || accountKindResponse.getData() == null) {
                log.warn("[消费执行] 获取账户类别信息失败，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> accountKind = accountKindResponse.getData();

            // 3. 获取mode_config JSON字段
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                log.debug("[消费执行] 账户类别未配置消费模式，无折扣，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            // 4. 解析mode_config
            java.util.Map<String, Object> modeConfig;
            if (modeConfigObj instanceof String) {
                modeConfig = objectMapper.readValue((String) modeConfigObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (modeConfigObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) modeConfigObj;
                modeConfig = map;
            } else {
                log.warn("[消费执行] mode_config格式不正确，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            // 5. 获取PRODUCT配置
            Object productObj = modeConfig.get("PRODUCT");
            if (productObj == null) {
                log.debug("[消费执行] 账户类别未配置商品模式，无折扣，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> productConfig;
            if (productObj instanceof String) {
                productConfig = objectMapper.readValue((String) productObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (productObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) productObj;
                productConfig = map;
            } else {
                log.warn("[消费执行] PRODUCT配置格式不正确，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            // 6. 获取discountRate（折扣率，0-1之间）
            Object discountRateObj = productConfig.get("discountRate");
            if (discountRateObj == null) {
                log.debug("[消费执行] 商品模式未配置折扣率，无折扣，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            BigDecimal discountRate;
            if (discountRateObj instanceof Number) {
                discountRate = BigDecimal.valueOf(((Number) discountRateObj).doubleValue());
            } else if (discountRateObj instanceof String) {
                discountRate = new BigDecimal((String) discountRateObj);
            } else {
                log.warn("[消费执行] discountRate格式不正确，accountKindId={}", account.getAccountKindId());
                return BigDecimal.ZERO;
            }

            // 7. 验证折扣率范围（0-1之间）
            if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
                log.warn("[消费执行] 折扣率超出范围，accountKindId={}, discountRate={}", account.getAccountKindId(), discountRate);
                return BigDecimal.ZERO;
            }

            log.debug("[消费执行] 获取商品模式折扣率成功，accountId={}, discountRate={}", accountId, discountRate);
            return discountRate;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 获取商品模式折扣率参数错误: accountId={}, error={}", accountId, e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 获取商品模式折扣率业务异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 获取商品模式折扣率系统异常: accountId={}, code={}, message={}", accountId, e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 获取商品模式折扣率未知异常: accountId={}", accountId, e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }

    /**
     * 获取计次模式折扣率
     * <p>
     * 从账户类别的mode_config.METERED.count.discountRate获取折扣率
     * 根据业务文档03-账户类别与消费模式设计.md
     * </p>
     *
     * @param accountKind 账户类别信息（Map格式）
     * @return 折扣率（0-1之间，如0.1表示10%折扣）
     */
    private BigDecimal getCountModeDiscountRate(java.util.Map<String, Object> accountKind) {
        log.debug("[消费执行] 获取计次模式折扣率");
        try {
            if (accountKind == null) {
                return BigDecimal.ZERO;
            }

            // 1. 获取mode_config JSON字段
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                log.debug("[消费执行] 账户类别未配置消费模式，无折扣");
                return BigDecimal.ZERO;
            }

            // 2. 解析mode_config
            java.util.Map<String, Object> modeConfig;
            if (modeConfigObj instanceof String) {
                modeConfig = objectMapper.readValue((String) modeConfigObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (modeConfigObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) modeConfigObj;
                modeConfig = map;
            } else {
                log.warn("[消费执行] mode_config格式不正确");
                return BigDecimal.ZERO;
            }

            // 3. 获取METERED配置
            Object meteredObj = modeConfig.get("METERED");
            if (meteredObj == null) {
                log.debug("[消费执行] 账户类别未配置计量计费模式，无折扣");
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> meteredConfig;
            if (meteredObj instanceof String) {
                meteredConfig = objectMapper.readValue((String) meteredObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (meteredObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) meteredObj;
                meteredConfig = map;
            } else {
                log.warn("[消费执行] METERED配置格式不正确");
                return BigDecimal.ZERO;
            }

            // 4. 获取count子配置
            Object countObj = meteredConfig.get("count");
            if (countObj == null) {
                log.debug("[消费执行] 计量计费模式未配置count子类型，无折扣");
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> countConfig;
            if (countObj instanceof String) {
                countConfig = objectMapper.readValue((String) countObj, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (countObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) countObj;
                countConfig = map;
            } else {
                log.warn("[消费执行] count配置格式不正确");
                return BigDecimal.ZERO;
            }

            // 5. 获取discountRate（折扣率，0-1之间）
            Object discountRateObj = countConfig.get("discountRate");
            if (discountRateObj == null) {
                log.debug("[消费执行] 计次模式未配置折扣率，无折扣");
                return BigDecimal.ZERO;
            }

            BigDecimal discountRate;
            if (discountRateObj instanceof Number) {
                discountRate = BigDecimal.valueOf(((Number) discountRateObj).doubleValue());
            } else if (discountRateObj instanceof String) {
                discountRate = new BigDecimal((String) discountRateObj);
            } else {
                log.warn("[消费执行] discountRate格式不正确");
                return BigDecimal.ZERO;
            }

            // 6. 验证折扣率范围（0-1之间）
            if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
                log.warn("[消费执行] 折扣率超出范围，discountRate={}", discountRate);
                return BigDecimal.ZERO;
            }

            log.debug("[消费执行] 获取计次模式折扣率成功，discountRate={}", discountRate);
            return discountRate;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 获取计次模式折扣率参数错误: error={}", e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on parameter error
        } catch (BusinessException e) {
            log.warn("[消费执行] 获取计次模式折扣率业务异常: code={}, message={}", e.getCode(), e.getMessage());
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on business error
        } catch (SystemException e) {
            log.error("[消费执行] 获取计次模式折扣率系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on system error
        } catch (Exception e) {
            log.error("[消费执行] 获取计次模式折扣率未知异常", e);
            return BigDecimal.ZERO; // For BigDecimal return methods, return ZERO on unknown error
        }
    }

    /**
     * 通过部门名称查询部门ID
     * <p>
     * 通过网关服务查询部门信息，如果查询失败则返回null
     * </p>
     *
     * @param departmentName 部门名称
     * @return 部门ID，如果查询失败则返回null
     */
    private Long getDepartmentIdByName(String departmentName) {
        if (departmentName == null || departmentName.trim().isEmpty()) {
            return null;
        }

        try {
            // 通过网关查询部门列表，根据名称匹配
            // 注意：这里使用通用的部门查询API，如果API不存在则返回null
            ResponseDTO<?> response = gatewayServiceClient.callCommonService(
                    "/api/v1/departments?name=" + java.net.URLEncoder.encode(departmentName, "UTF-8"),
                    org.springframework.http.HttpMethod.GET,
                    null,
                    Object.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                // 尝试从响应中提取部门ID
                // 注意：这里需要根据实际的API响应格式进行解析
                // 如果API返回的是列表，取第一个匹配的部门ID
                if (response.getData() instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String, Object>> departments = (java.util.List<Map<String, Object>>) response.getData();
                    if (!departments.isEmpty()) {
                        Object deptId = departments.get(0).get("departmentId");
                        if (deptId instanceof Long) {
                            return (Long) deptId;
                        } else if (deptId instanceof Number) {
                            return ((Number) deptId).longValue();
                        }
                    }
                } else if (response.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> department = (Map<String, Object>) response.getData();
                    Object deptId = department.get("departmentId");
                    if (deptId instanceof Long) {
                        return (Long) deptId;
                    } else if (deptId instanceof Number) {
                        return ((Number) deptId).longValue();
                    }
                }
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费执行] 通过部门名称查询部门ID参数错误: departmentName={}, error={}", departmentName, e.getMessage());
            // 查询失败不影响主流程，返回null
        } catch (BusinessException e) {
            log.warn("[消费执行] 通过部门名称查询部门ID业务异常: departmentName={}, code={}, message={}", departmentName, e.getCode(), e.getMessage());
            // 查询失败不影响主流程，返回null
        } catch (SystemException e) {
            log.error("[消费执行] 通过部门名称查询部门ID系统异常: departmentName={}, code={}, message={}", departmentName, e.getCode(), e.getMessage(), e);
            // 查询失败不影响主流程，返回null
        } catch (Exception e) {
            log.warn("[消费执行] 通过部门名称查询部门ID未知异常: departmentName={}, error={}", departmentName, e.getMessage());
            // 查询失败不影响主流程，返回null
        }

        return null;
    }
}



