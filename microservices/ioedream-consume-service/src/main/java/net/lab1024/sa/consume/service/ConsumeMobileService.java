package net.lab1024.sa.consume.service;

import java.util.List;

import net.lab1024.sa.consume.domain.form.ConsumeMobileFaceForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileQuickForm;
import net.lab1024.sa.consume.domain.form.ConsumeMobileScanForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileMealVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;

/**
 * 消费移动端服务接口
 * <p>
 * 提供移动端消费相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeMobileService {

    /**
     * 快速消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    ConsumeMobileResultVO quickConsume(ConsumeMobileQuickForm form);

    /**
     * 扫码消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    ConsumeMobileResultVO scanConsume(ConsumeMobileScanForm form);

    /**
     * NFC消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    ConsumeMobileResultVO nfcConsume(ConsumeMobileNfcForm form);

    /**
     * 人脸识别消费
     *
     * @param form 消费表单
     * @return 消费结果
     */
    ConsumeMobileResultVO faceConsume(ConsumeMobileFaceForm form);

    /**
     * 快速用户查询
     *
     * @param queryType 查询类型
     * @param queryValue 查询值
     * @return 用户信息
     */
    ConsumeMobileUserVO quickUserInfo(String queryType, String queryValue);

    /**
     * 获取用户消费信息
     *
     * @param userId 用户ID
     * @return 用户消费信息
     */
    ConsumeMobileUserInfoVO getUserConsumeInfo(Long userId);

    /**
     * 获取有效餐别
     *
     * @return 餐别列表
     */
    List<ConsumeMobileMealVO> getAvailableMeals();

    /**
     * 获取设备配置
     *
     * @param deviceId 设备ID
     * @return 设备配置
     */
    ConsumeDeviceConfigVO getDeviceConfig(Long deviceId);

    /**
     * 获取设备今日统计
     *
     * @param deviceId 设备ID
     * @return 统计数据
     */
    ConsumeMobileStatsVO getDeviceTodayStats(Long deviceId);

    /**
     * 获取实时交易汇总
     *
     * @param areaId 区域ID
     * @return 交易汇总
     */
    ConsumeMobileSummaryVO getTransactionSummary(String areaId);

    /**
     * 离线交易同步
     *
     * @param form 同步表单
     * @return 同步结果
     */
    ConsumeSyncResultVO syncOfflineTransactions(ConsumeOfflineSyncForm form);

    /**
     * 获取同步数据
     *
     * @param deviceId 设备ID
     * @param lastSyncTime 最后同步时间
     * @return 同步数据
     */
    ConsumeSyncDataVO getSyncData(Long deviceId, String lastSyncTime);

    /**
     * 验证消费权限
     *
     * @param form 验证表单
     * @return 验证结果
     */
    ConsumeValidateResultVO validateConsumePermission(ConsumePermissionValidateForm form);

    /**
     * 获取用户统计
     * <p>
     * 获取指定用户的消费统计数据，包括总交易笔数、总金额、今日统计、本月统计
     * </p>
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    ConsumeMobileUserStatsVO getUserStats(Long userId);

    /**
     * 获取账单详情
     * <p>
     * 根据订单号获取移动端账单详细信息，包括消费记录和支付记录
     * </p>
     *
     * @param orderId 订单ID
     * @return 账单详情
     */
    net.lab1024.sa.consume.domain.vo.MobileBillDetailVO getBillDetail(String orderId);
}



