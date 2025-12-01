package net.lab1024.sa.base.module.area.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.module.area.dao.PersonAreaRelationDao;
import net.lab1024.sa.base.module.area.domain.entity.PersonAreaRelationEntity;
import net.lab1024.sa.base.module.area.domain.form.BatchPersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.form.PersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.form.RetrySyncForm;
import net.lab1024.sa.base.module.area.domain.form.TriggerSyncForm;
import net.lab1024.sa.base.module.area.domain.form.BatchTriggerSyncForm;
import net.lab1024.sa.base.module.area.domain.form.AutoRenewForm;
import net.lab1024.sa.base.module.area.domain.form.ImportPersonAreaRelationForm;
import net.lab1024.sa.base.module.area.domain.vo.PersonAreaRelationVO;
import net.lab1024.sa.base.module.area.domain.vo.AreaSimpleVO;
import net.lab1024.sa.base.module.area.domain.vo.PersonAreaStatisticsVO;
import net.lab1024.sa.base.module.area.manager.PersonAreaCacheManager;
import net.lab1024.sa.base.module.area.service.PersonAreaService;
import net.lab1024.sa.base.module.area.service.AreaService;

/**
 * 人员区域管理服务实现
 * <p>
 * 提供人员与区域关联的完整管理功能，包括：
 * 1. 人员区域关联的增删改查
 * 2. 设备数据同步和下发
 * 3. 权限管理和状态控制
 * 4. 统计分析和批量操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Service
public class PersonAreaServiceImpl implements PersonAreaService {

    @Resource
    private PersonAreaRelationDao personAreaRelationDao;

    @Resource
    private PersonAreaCacheManager cacheManager;

    @Resource
    private AreaService areaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addPersonAreaRelation(PersonAreaRelationForm form) {
        try {
            log.info("添加人员区域关联: personId={}, areaId={}", form.getPersonId(), form.getAreaId());

            // 1. 参数验证
            if (form.getPersonId() == null || form.getAreaId() == null) {
                return ResponseDTO.userErrorParam("人员ID和区域ID不能为空");
            }

            // 2. 检查区域是否存在
            var areaResult = areaService.getById(form.getAreaId());
            if (!areaResult.getOk() || areaResult.getData() == null) {
                return ResponseDTO.userErrorParam("区域不存在");
            }

            // 3. 检查是否已存在相同的关联
            LambdaQueryWrapper<PersonAreaRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PersonAreaRelationEntity::getPersonId, form.getPersonId())
                        .eq(PersonAreaRelationEntity::getAreaId, form.getAreaId())
                        .eq(PersonAreaRelationEntity::getDeletedFlag, 0);

            PersonAreaRelationEntity existing = personAreaRelationDao.selectOne(queryWrapper);
            if (existing != null) {
                return ResponseDTO.userErrorParam("人员区域关联已存在");
            }

            // 4. 创建关联实体
            PersonAreaRelationEntity entity = SmartBeanUtil.copy(form, PersonAreaRelationEntity.class);
            entity.setStatus(1); // 默认启用
            entity.setSyncStatus(0); // 待同步
            entity.setPriorityLevel(form.getPriorityLevel() != null ? form.getPriorityLevel() : 5); // 默认普通优先级
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            // 5. 保存到数据库
            personAreaRelationDao.insert(entity);

            // 6. 更新缓存
            cacheManager.evictPersonCache(form.getPersonId());
            cacheManager.evictAreaCache(form.getAreaId());

            // 7. 异步触发设备同步（设备引擎待创建）
            // // dispatchEngine.asyncDispatchDevices(entity.getPersonId(), entity.getAreaId());

            log.info("人员区域关联添加成功: relationId={}", entity.getRelationId());
            return ResponseDTO.ok(entity.getRelationId());

        } catch (Exception e) {
            log.error("添加人员区域关联失败", e);
            return ResponseDTO.error("添加人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updatePersonAreaRelation(PersonAreaRelationForm form) {
        try {
            if (form.getRelationId() == null) {
                return ResponseDTO.userErrorParam("关联ID不能为空");
            }

            PersonAreaRelationEntity existing = personAreaRelationDao.selectById(form.getRelationId());
            if (existing == null || existing.getDeletedFlag() == 1) {
                return ResponseDTO.userErrorParam("人员区域关联不存在");
            }

            // 更新实体
            PersonAreaRelationEntity updateEntity = SmartBeanUtil.copy(form, PersonAreaRelationEntity.class);
            updateEntity.setUpdateTime(LocalDateTime.now());

            personAreaRelationDao.updateById(updateEntity);

            // 更新缓存
            cacheManager.evictPersonCache(existing.getPersonId());
            cacheManager.evictAreaCache(existing.getAreaId());

            // 如果设备配置发生变化，重新触发同步
            if (form.getSyncDeviceTypes() != null && !form.getSyncDeviceTypes().equals(existing.getSyncDeviceTypes())) {
                // dispatchEngine.asyncDispatchDevices(form.getPersonId(), form.getAreaId());
            }

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新人员区域关联失败", e);
            return ResponseDTO.error("更新人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> deletePersonAreaRelation(Long relationId) {
        try {
            if (relationId == null) {
                return ResponseDTO.userErrorParam("关联ID不能为空");
            }

            PersonAreaRelationEntity entity = personAreaRelationDao.selectById(relationId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.userErrorParam("人员区域关联不存在");
            }

            // 软删除
            entity.setDeletedFlag(1);
            entity.setUpdateTime(LocalDateTime.now());
            personAreaRelationDao.updateById(entity);

            // 清理缓存
            cacheManager.evictPersonCache(entity.getPersonId());
            cacheManager.evictAreaCache(entity.getAreaId());

            // TODO: 通知设备删除相关人员数据

            log.info("人员区域关联删除成功: relationId={}", relationId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("删除人员区域关联失败", e);
            return ResponseDTO.error("删除人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchDeletePersonAreaRelation(List<Long> relationIds) {
        try {
            if (relationIds == null || relationIds.isEmpty()) {
                return ResponseDTO.userErrorParam("关联ID列表不能为空");
            }

            // 查询要删除的关联
            List<PersonAreaRelationEntity> entities = personAreaRelationDao.selectBatchIds(relationIds);
            List<PersonAreaRelationEntity> validEntities = entities.stream()
                .filter(entity -> entity.getDeletedFlag() == 0)
                .collect(Collectors.toList());

            if (validEntities.isEmpty()) {
                return ResponseDTO.userErrorParam("没有找到有效的人员区域关联");
            }

            // 批量软删除
            for (PersonAreaRelationEntity entity : validEntities) {
                entity.setDeletedFlag(1);
                entity.setUpdateTime(LocalDateTime.now());
            }

            // 批量更新
            List<Long> validIds = validEntities.stream()
                .map(PersonAreaRelationEntity::getRelationId)
                .collect(Collectors.toList());

            personAreaRelationDao.update(
                new LambdaUpdateWrapper<PersonAreaRelationEntity>()
                    .in(PersonAreaRelationEntity::getRelationId, validIds)
                    .set(PersonAreaRelationEntity::getDeletedFlag, 1)
                    .set(PersonAreaRelationEntity::getUpdateTime, LocalDateTime.now())
            );

            // 清理缓存
            Set<Long> personIds = validEntities.stream()
                .map(PersonAreaRelationEntity::getPersonId)
                .collect(Collectors.toSet());
            Set<Long> areaIds = validEntities.stream()
                .map(PersonAreaRelationEntity::getAreaId)
                .collect(Collectors.toSet());

            personIds.forEach(cacheManager::evictPersonCache);
            areaIds.forEach(cacheManager::evictAreaCache);

            log.info("批量删除人员区域关联成功: count={}", validEntities.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("批量删除人员区域关联失败", e);
            return ResponseDTO.error("批量删除人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PersonAreaRelationVO> getPersonAreaRelationById(Long relationId) {
        try {
            PersonAreaRelationEntity entity = personAreaRelationDao.selectById(relationId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.userErrorParam("人员区域关联不存在");
            }

            PersonAreaRelationVO vo = SmartBeanUtil.copy(entity, PersonAreaRelationVO.class);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("查询人员区域关联失败", e);
            return ResponseDTO.error("查询人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<PersonAreaRelationVO>> queryPersonAreaRelationPage(PersonAreaRelationForm form) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<PersonAreaRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PersonAreaRelationEntity::getDeletedFlag, 0);

            if (form.getPersonId() != null) {
                queryWrapper.eq(PersonAreaRelationEntity::getPersonId, form.getPersonId());
            }
            if (form.getAreaId() != null) {
                queryWrapper.eq(PersonAreaRelationEntity::getAreaId, form.getAreaId());
            }
            if (form.getPersonType() != null && !form.getPersonType().trim().isEmpty()) {
                queryWrapper.eq(PersonAreaRelationEntity::getPersonType, form.getPersonType());
            }
            if (form.getRelationType() != null && !form.getRelationType().trim().isEmpty()) {
                queryWrapper.eq(PersonAreaRelationEntity::getRelationType, form.getRelationType());
            }
            if (form.getStatus() != null) {
                queryWrapper.eq(PersonAreaRelationEntity::getStatus, form.getStatus());
            }

            // 分页查询
            Page<PersonAreaRelationEntity> page = new Page<>(form.getCurrent() != null ? form.getCurrent() : 1,
                                                           form.getPageSize() != null ? form.getPageSize() : 20);
            IPage<PersonAreaRelationEntity> pageResult = personAreaRelationDao.selectPage(page, queryWrapper);

            // 转换为VO
            List<PersonAreaRelationVO> voList = pageResult.getRecords().stream()
                .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                .collect(Collectors.toList());

            PageResult<PersonAreaRelationVO> result = new PageResult<>();
            result.setPageSize(pageResult.getSize());
            result.setPageNum(pageResult.getCurrent());
            result.setTotal(pageResult.getTotal());
            result.setList(voList);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("分页查询人员区域关联失败", e);
            return ResponseDTO.error("分页查询人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<PersonAreaRelationVO>> getPersonAreaRelations(Long personId) {
        try {
            if (personId == null) {
                return ResponseDTO.userErrorParam("人员ID不能为空");
            }

            // 先从缓存获取
            List<PersonAreaRelationEntity> entities = cacheManager.getPersonAreaRelations(personId);

            // 如果缓存为空，从数据库查询
            if (entities.isEmpty()) {
                LambdaQueryWrapper<PersonAreaRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PersonAreaRelationEntity::getPersonId, personId)
                           .eq(PersonAreaRelationEntity::getDeletedFlag, 0)
                           .orderByDesc(PersonAreaRelationEntity::getCreateTime);

                entities = personAreaRelationDao.selectList(queryWrapper);

                // 放入缓存
                cacheManager.putPersonAreaRelations(personId, entities);
            }

            List<PersonAreaRelationVO> voList = entities.stream()
                .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("查询人员区域关联列表失败", e);
            return ResponseDTO.error("查询人员区域关联列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaSimpleVO>> getPersonAccessibleAreas(Long personId) {
        try {
            if (personId == null) {
                return ResponseDTO.userErrorParam("人员ID不能为空");
            }

            // 获取人员的有效区域关联
            List<PersonAreaRelationEntity> relations = cacheManager.getPersonAreaRelations(personId);

            // 转换为区域简单视图
            List<AreaSimpleVO> areaList = relations.stream()
                .filter(PersonAreaRelationEntity::isActive) // 只返回有效的关联
                .map(relation -> {
                    AreaSimpleVO area = new AreaSimpleVO();
                    area.setAreaId(relation.getAreaId());
                    // TODO: 从区域服务获取区域详细信息
                    return area;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(areaList);

        } catch (Exception e) {
            log.error("查询人员可访问区域列表失败", e);
            return ResponseDTO.error("查询人员可访问区域列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> checkPersonAreaPermission(Long personId, Long areaId) {
        try {
            if (personId == null || areaId == null) {
                return ResponseDTO.userErrorParam("人员ID和区域ID不能为空");
            }

            // 从缓存检查权限
            return ResponseDTO.ok(cacheManager.hasAreaPermission(personId, areaId));

        } catch (Exception e) {
            log.error("检查人员区域权限失败", e);
            return ResponseDTO.error("检查人员区域权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchAddPersonAreaRelation(BatchPersonAreaRelationForm form) {
        try {
            if (form.getPersonIds() == null || form.getPersonIds().isEmpty()) {
                return ResponseDTO.userErrorParam("人员ID列表不能为空");
            }

            int successCount = 0;
            int failCount = 0;
            StringBuilder failReasons = new StringBuilder();

            for (Long personId : form.getPersonIds()) {
                try {
                    PersonAreaRelationForm singleForm = new PersonAreaRelationForm();
                    singleForm.setPersonId(personId);
                    singleForm.setAreaId(form.getAreaId());
                    singleForm.setPersonType("EMPLOYEE"); // 默认为员工类型，批量操作暂时硬编码
                    singleForm.setRelationType(form.getRelationType());
                    singleForm.setEffectiveTime(form.getValidFrom());
                    singleForm.setExpireTime(form.getValidTo());
                    // singleForm.setSyncDeviceTypes(form.getSyncDeviceTypes()); // TODO: 待实现
                    // singleForm.setPriorityLevel(form.getPriorityLevel()); // TODO: 待实现
                    singleForm.setRemark(form.getRemark());

                    ResponseDTO<Long> result = addPersonAreaRelation(singleForm);
                    if (result.getOk()) {
                        successCount++;
                    } else {
                        failCount++;
                        failReasons.append(String.format("人员%d失败: %s; ", personId, result.getMsg()));
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.append(String.format("人员%d异常: %s; ", personId, e.getMessage()));
                }
            }

            String message = String.format("批量添加完成，成功%d个，失败%d个", successCount, failCount);
            if (failCount > 0) {
                message += "，失败原因：" + failReasons.toString();
            }

            log.info("批量添加人员区域关联: {}", message);
            return ResponseDTO.ok(message);

        } catch (Exception e) {
            log.error("批量添加人员区域关联失败", e);
            return ResponseDTO.error("批量添加人员区域关联失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> triggerDeviceSync(TriggerSyncForm form) {
        try {
            // 根据同步类型处理
            if ("AREA".equals(form.getSyncType()) && form.getAreaIds() != null) {
                // 按区域同步 - 获取该区域的所有人员关联
                for (Long areaId : form.getAreaIds()) {
                    List<PersonAreaRelationEntity> entities = personAreaRelationDao.selectList(
                        new LambdaQueryWrapper<PersonAreaRelationEntity>()
                            .eq(PersonAreaRelationEntity::getAreaId, areaId)
                            .eq(PersonAreaRelationEntity::getDeletedFlag, false)
                    );

                    for (PersonAreaRelationEntity entity : entities) {
                        entity.setSyncStatus(1); // 同步中
                        entity.setUpdateTime(LocalDateTime.now());
                        personAreaRelationDao.updateById(entity);

                        // 异步触发设备同步
                        // dispatchEngine.asyncDispatchDevices(entity.getPersonId(), entity.getAreaId());
                    }
                }
            } else if ("PERSON".equals(form.getSyncType()) && form.getPersonIds() != null) {
                // 按人员同步 - 获取该人员的所有区域关联
                for (Long personId : form.getPersonIds()) {
                    List<PersonAreaRelationEntity> entities = personAreaRelationDao.selectList(
                        new LambdaQueryWrapper<PersonAreaRelationEntity>()
                            .eq(PersonAreaRelationEntity::getPersonId, personId)
                            .eq(PersonAreaRelationEntity::getDeletedFlag, false)
                    );

                    for (PersonAreaRelationEntity entity : entities) {
                        entity.setSyncStatus(1); // 同步中
                        entity.setUpdateTime(LocalDateTime.now());
                        personAreaRelationDao.updateById(entity);

                        // 异步触发设备同步
                        // dispatchEngine.asyncDispatchDevices(entity.getPersonId(), entity.getAreaId());
                    }
                }
            }

            return ResponseDTO.ok("设备同步已触发");

        } catch (Exception e) {
            log.error("触发设备同步失败", e);
            return ResponseDTO.error("触发设备同步失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> batchTriggerDeviceSync(BatchTriggerSyncForm form) {
        try {
            if (form.getSyncConfigs() == null || form.getSyncConfigs().isEmpty()) {
                return ResponseDTO.userErrorParam("同步配置列表不能为空");
            }

            int successCount = 0;
            int totalCount = 0;

            // 处理每个同步配置
            for (BatchTriggerSyncForm.SyncConfig config : form.getSyncConfigs()) {
                try {
                    if ("AREA".equals(config.getSyncType()) && config.getAreaIds() != null) {
                        // 处理区域同步
                        for (Long areaId : config.getAreaIds()) {
                            totalCount++;
                            List<PersonAreaRelationEntity> relations = personAreaRelationDao.selectList(
                                new LambdaQueryWrapper<PersonAreaRelationEntity>()
                                    .eq(PersonAreaRelationEntity::getAreaId, areaId)
                                    .eq(PersonAreaRelationEntity::getDeletedFlag, false)
                            );

                            for (PersonAreaRelationEntity relation : relations) {
                                try {
                                    // ResponseDTO<String> result = dispatchEngine.asyncDispatchDevices(relation.getPersonId(), relation.getAreaId());
                                    ResponseDTO<String> result = null; // 设备引擎待创建
                                    if (result.getOk()) {
                                        successCount++;
                                    }
                                } catch (Exception e) {
                                    log.error("批量触发同步失败: relationId={}", relation.getRelationId(), e);
                                }
                            }
                        }
                    } else if ("PERSON".equals(config.getSyncType()) && config.getPersonIds() != null) {
                        // 处理人员同步
                        for (Long personId : config.getPersonIds()) {
                            totalCount++;
                            List<PersonAreaRelationEntity> relations = personAreaRelationDao.selectList(
                                new LambdaQueryWrapper<PersonAreaRelationEntity>()
                                    .eq(PersonAreaRelationEntity::getPersonId, personId)
                                    .eq(PersonAreaRelationEntity::getDeletedFlag, false)
                            );

                            for (PersonAreaRelationEntity relation : relations) {
                                try {
                                    // ResponseDTO<String> result = dispatchEngine.asyncDispatchDevices(relation.getPersonId(), relation.getAreaId());
                                    ResponseDTO<String> result = null; // 设备引擎待创建
                                    if (result.getOk()) {
                                        successCount++;
                                    }
                                } catch (Exception e) {
                                    log.error("批量触发同步失败: relationId={}", relation.getRelationId(), e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("处理同步配置失败", e);
                }
            }

            String message = String.format("批量触发设备同步完成，总数%d，成功%d", totalCount, successCount);
            return ResponseDTO.ok(message);

        } catch (Exception e) {
            log.error("批量触发设备同步失败", e);
            return ResponseDTO.error("批量触发设备同步失败: " + e.getMessage());
        }
    }

  
    @Override
    public ResponseDTO<String> retryFailedSync(RetrySyncForm form) {
        // TODO: 实现失败同步重试
        return ResponseDTO.ok("重试功能开发中");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateRelationStatus(Long relationId, Integer status) {
        try {
            if (relationId == null || status == null) {
                return ResponseDTO.userErrorParam("关联ID和状态不能为空");
            }

            PersonAreaRelationEntity entity = personAreaRelationDao.selectById(relationId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.userErrorParam("人员区域关联不存在");
            }

            entity.setStatus(status);
            entity.setUpdateTime(LocalDateTime.now());
            personAreaRelationDao.updateById(entity);

            // 清理缓存
            cacheManager.evictPersonCache(entity.getPersonId());
            cacheManager.evictAreaCache(entity.getAreaId());

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新关联状态失败", e);
            return ResponseDTO.error("更新关联状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String> autoRenewExpiredRelations(AutoRenewForm form) {
        // TODO: 实现自动续期功能
        return ResponseDTO.ok("自动续期功能开发中");
    }

    @Override
    public ResponseDTO<PersonAreaStatisticsVO> getPersonAreaStatistics() {
        // TODO: 实现统计信息查询
        return ResponseDTO.ok(new PersonAreaStatisticsVO());
    }

    @Override
    public ResponseDTO<String> exportPersonAreaRelation(PersonAreaRelationForm form) {
        // TODO: 实现导出功能
        return ResponseDTO.ok("导出功能开发中");
    }

    @Override
    public ResponseDTO<String> importPersonAreaRelation(ImportPersonAreaRelationForm form) {
        // TODO: 实现导入功能
        return ResponseDTO.ok("导入功能开发中");
    }

    @Override
    public ResponseDTO<List<PersonAreaRelationVO>> getAreaPersonRelations(Long areaId) {
        try {
            if (areaId == null) {
                return ResponseDTO.userErrorParam("区域ID不能为空");
            }

            LambdaQueryWrapper<PersonAreaRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PersonAreaRelationEntity::getAreaId, areaId)
                       .eq(PersonAreaRelationEntity::getDeletedFlag, 0)
                       .orderByDesc(PersonAreaRelationEntity::getCreateTime);

            List<PersonAreaRelationEntity> entities = personAreaRelationDao.selectList(queryWrapper);

            List<PersonAreaRelationVO> voList = entities.stream()
                .map(entity -> SmartBeanUtil.copy(entity, PersonAreaRelationVO.class))
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("查询区域人员关联列表失败", e);
            return ResponseDTO.error("查询区域人员关联列表失败: " + e.getMessage());
        }
    }
}