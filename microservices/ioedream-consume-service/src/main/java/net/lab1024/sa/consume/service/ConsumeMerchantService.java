package net.lab1024.sa.consume.service;

import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMerchantVO;

/**
 * 消费商户服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeMerchantService {

    /**
     * 分页查询商户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeMerchantVO> queryMerchants(ConsumeMerchantQueryForm queryForm);

    /**
     * 获取商户详情
     *
     * @param merchantId 商户ID
     * @return 商户详情
     */
    ConsumeMerchantVO getMerchantDetail(Long merchantId);

    /**
     * 新增商户
     *
     * @param addForm 商户新增表单
     * @return 商户ID
     */
    Long addMerchant(ConsumeMerchantAddForm addForm);

    /**
     * 更新商户信息
     *
     * @param merchantId 商户ID
     * @param updateForm 更新表单
     */
    void updateMerchant(Long merchantId, ConsumeMerchantUpdateForm updateForm);

    /**
     * 删除商户
     *
     * @param merchantId 商户ID
     */
    void deleteMerchant(Long merchantId);

    /**
     * 启用商户
     *
     * @param merchantId 商户ID
     */
    void enableMerchant(Long merchantId);

    /**
     * 禁用商户
     *
     * @param merchantId 商户ID
     */
    void disableMerchant(Long merchantId);

    /**
     * 获取活跃商户列表
     *
     * @return 活跃商户列表
     */
    List<ConsumeMerchantVO> getActiveMerchants();

    /**
     * 按区域查询商户
     *
     * @param areaId 区域ID
     * @return 商户列表
     */
    List<ConsumeMerchantVO> getMerchantsByArea(Long areaId);

    /**
     * 获取商户结算统计
     *
     * @param merchantId 商户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 结算统计
     */
    java.util.Map<String, Object> getMerchantSettlement(Long merchantId, String startDate, String endDate);

    /**
     * 批量分配设备
     *
     * @param merchantId 商户ID
     * @param deviceIds 设备ID列表
     */
    void assignDevices(Long merchantId, List<String> deviceIds);

    /**
     * 获取商户设备列表
     *
     * @param merchantId 商户ID
     * @return 设备列表
     */
    List<String> getMerchantDevices(Long merchantId);
}