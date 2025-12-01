package net.lab1024.sa.base.module.area.service;

import java.util.List;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.area.domain.entity.PersonAreaRelationEntity;
import net.lab1024.sa.base.module.area.domain.form.AutoRenewForm;
import net.lab1024.sa.base.module.area.domain.form.BatchPersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.form.BatchTriggerSyncForm;
import net.lab1024.sa.base.module.area.domain.form.DeviceDispatchRecordForm;
import net.lab1024.sa.base.module.area.domain.form.ImportPersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.form.PersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.form.RetrySyncForm;
import net.lab1024.sa.base.module.area.domain.form.TriggerSyncForm;
import net.lab1024.sa.base.module.area.domain.vo.AreaSimpleVO;
import net.lab1024.sa.base.module.area.domain.vo.DeviceDispatchRecordVO;
import net.lab1024.sa.base.module.area.domain.vo.PersonAreaRelationVO;
import net.lab1024.sa.base.module.area.domain.vo.PersonAreaStatisticsVO;

/**
 * 人员区域管理服务接口
 * 提供人员与区域关联的管理功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public interface PersonAreaService {

    /**
     * 添加人员区域关联
     *
     * @param form 关联表单
     * @return 关联ID
     */
    ResponseDTO<Long> addPersonAreaRelation(PersonAreaRelationForm form);

    /**
     * 更新人员区域关联
     *
     * @param form 关联表单
     * @return 操作结果
     */
    ResponseDTO<Void> updatePersonAreaRelation(PersonAreaRelationForm form);

    /**
     * 删除人员区域关联
     *
     * @param relationId 关联ID
     * @return 操作结果
     */
    ResponseDTO<Void> deletePersonAreaRelation(Long relationId);

    /**
     * 批量删除人员区域关联
     *
     * @param relationIds 关联ID列表
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeletePersonAreaRelation(List<Long> relationIds);

    /**
     * 根据ID查询人员区域关联
     *
     * @param relationId 关联ID
     * @return 关联详情
     */
    ResponseDTO<PersonAreaRelationVO> getPersonAreaRelationById(Long relationId);

    /**
     * 分页查询人员区域关联
     *
     * @param form 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<PersonAreaRelationVO>> queryPersonAreaRelationPage(PersonAreaRelationForm form);

    /**
     * 根据人员ID查询关联列表
     *
     * @param personId 人员ID
     * @return 关联列表
     */
    ResponseDTO<List<PersonAreaRelationVO>> getPersonAreaRelations(Long personId);

    /**
     * 根据区域ID查询关联列表
     *
     * @param areaId 区域ID
     * @return 关联列表
     */
    ResponseDTO<List<PersonAreaRelationVO>> getAreaPersonRelations(Long areaId);

    /**
     * 批量添加人员区域关联
     *
     * @param form 批量操作表单
     * @return 操作结果
     */
    ResponseDTO<String> batchAddPersonAreaRelation(BatchPersonAreaRelationForm form);

    /**
     * 触发设备同步
     *
     * @param form 触发同步表单
     * @return 操作结果
     */
    ResponseDTO<String> triggerDeviceSync(TriggerSyncForm form);

    /**
     * 批量触发设备同步
     *
     * @param form 批量同步表单
     * @return 操作结果
     */
    ResponseDTO<String> batchTriggerDeviceSync(BatchTriggerSyncForm form);

    /**
     * 查询同步记录
     *
     * @param form 查询表单
     * @return 同步记录列表
     */
      // TODO: DeviceDispatchRecordVO类不存在，暂时注释此方法
    // ResponseDTO<PageResult<DeviceDispatchRecordVO>> queryDispatchRecordPage(DeviceDispatchRecordForm form);

    /**
     * 重新执行失败的同步任务
     *
     * @param form 重试表单
     * @return 操作结果
     */
    ResponseDTO<String> retryFailedSync(RetrySyncForm form);

    /**
     * 获取人员可访问的区域列表
     *
     * @param personId 人员ID
     * @return 区域列表
     */
    ResponseDTO<List<AreaSimpleVO>> getPersonAccessibleAreas(Long personId);

    /**
     * 检查人员是否有区域权限
     *
     * @param personId 人员ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    ResponseDTO<Boolean> checkPersonAreaPermission(Long personId, Long areaId);

    /**
     * 更新关联状态
     *
     * @param relationId 关联ID
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<Void> updateRelationStatus(Long relationId, Integer status);

    /**
     * 自动续期过期关联
     *
     * @param form 续期表单
     * @return 操作结果
     */
    ResponseDTO<String> autoRenewExpiredRelations(AutoRenewForm form);

    /**
     * 获取人员区域关联统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<PersonAreaStatisticsVO> getPersonAreaStatistics();

    /**
     * 导出人员区域关联数据
     *
     * @param form 导出表单
     * @return 导出结果
     */
    ResponseDTO<String> exportPersonAreaRelation(PersonAreaRelationForm form);

    /**
     * 导入人员区域关联数据
     *
     * @param form 导入表单
     * @return 导入结果
     */
    ResponseDTO<String> importPersonAreaRelation(ImportPersonAreaRelationForm form);
}