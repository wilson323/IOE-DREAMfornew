package net.lab1024.sa.system.dict.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartStringUtil;
import net.lab1024.sa.system.dict.dao.DictDataDao;
import net.lab1024.sa.system.dict.dao.DictTypeDao;
import net.lab1024.sa.system.dict.domain.entity.DictDataEntity;
import net.lab1024.sa.system.dict.domain.entity.DictTypeEntity;
import net.lab1024.sa.system.dict.manager.DictDataManager;
import net.lab1024.sa.system.dict.service.DictDataService;
import net.lab1024.sa.system.domain.form.DictDataAddForm;
import net.lab1024.sa.system.domain.form.DictDataUpdateForm;
import net.lab1024.sa.system.domain.form.DictQueryForm;
import net.lab1024.sa.system.domain.vo.DictDataVO;

/**
 * 字典数据服务实现
 * <p>
 * 严格遵循四层架构：Service 和 Manager 和 DAO
 * 严格遵循编码规范：jakarta包名、@Resource注入、完整异常处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DictDataServiceImpl implements DictDataService {

    @Resource
    private DictDataDao dictDataDao;

    @Resource
    private DictTypeDao dictTypeDao;

    @Resource
    private DictDataManager dictDataManager;

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Long> addDictData(DictDataAddForm addForm, Long userId) {
        log.info("新增字典数据，dictTypeCode：{}，dictValue：{}，userId：{}", addForm.getDictTypeCode(), addForm.getDictValue(),
                userId);

        try {
            // 参数验证
            if (addForm == null || SmartStringUtil.isEmpty(addForm.getDictValue())) {
                return ResponseDTO.userErrorParam("字典值不能为空");
            }

            // 检查字典类型是否存在（优先使用dictTypeId，如果没有则使用dictTypeCode）
            DictTypeEntity dictType = null;
            if (addForm.getDictTypeId() != null) {
                dictType = dictTypeDao.selectById(addForm.getDictTypeId());
            } else if (SmartStringUtil.isNotEmpty(addForm.getDictTypeCode())) {
                dictType = dictTypeDao.selectOne(
                        new LambdaQueryWrapper<DictTypeEntity>()
                                .eq(DictTypeEntity::getDictTypeCode, addForm.getDictTypeCode())
                                .eq(DictTypeEntity::getDeletedFlag, 0));
            }

            if (dictType == null || Objects.equals(dictType.getDeletedFlag(), 1)) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            // 检查字典值是否唯一
            if (!checkDictValueUnique(dictType.getDictTypeId(), addForm.getDictValue(), null).getData()) {
                return ResponseDTO.userErrorParam("字典值已存在");
            }

            // 如果是默认值，清除其他默认值
            if (Objects.equals(addForm.getIsDefault(), 1)) {
                dictDataManager.clearOtherDefaultValues(dictType.getDictTypeId());
            }

            // 构建实体
            DictDataEntity entity = new DictDataEntity();
            SmartBeanUtil.copyProperties(addForm, entity);
            entity.setDictTypeId(dictType.getDictTypeId());
            entity.setDictTypeCode(dictType.getDictTypeCode());
            entity.setCreateUserId(userId);

            // 设置默认排序
            if (entity.getSortOrder() == null) {
                entity.setSortOrder(dictDataManager.getNextSortOrder(dictType.getDictTypeId()));
            }

            // 保存数据
            dictDataDao.insert(entity);

            log.info("字典数据新增成功，dictDataId：{}，dictTypeCode：{}，dictValue：{}",
                    entity.getDictDataId(), addForm.getDictTypeCode(), addForm.getDictValue());
            return ResponseDTO.ok(entity.getDictDataId());

        } catch (Exception e) {
            log.error("新增字典数据失败，dictTypeCode：{}，dictValue：{}，错误：{}",
                    addForm.getDictTypeCode(), addForm.getDictValue(), e.getMessage(), e);
            throw new BusinessException("新增字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Void> updateDictData(DictDataUpdateForm updateForm, Long userId) {
        log.info("更新字典数据，dictDataId：{}，userId：{}", updateForm.getDictDataId(), userId);

        try {
            if (updateForm == null || updateForm.getDictDataId() == null) {
                return ResponseDTO.userErrorParam("字典数据ID不能为空");
            }

            // 检查字典数据是否存在
            DictDataEntity existingEntity = dictDataDao.selectById(updateForm.getDictDataId());
            if (existingEntity == null) {
                return ResponseDTO.userErrorParam("字典数据不存在");
            }

            // 获取字典类型信息
            DictTypeEntity dictType = dictTypeDao.selectById(existingEntity.getDictTypeId());
            if (dictType == null) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            // 系统内置字典数据不允许修改
            if (Objects.equals(dictType.getIsSystem(), 1)) {
                return ResponseDTO.userErrorParam("系统内置字典数据不允许修改");
            }

            // 检查字典值唯一性
            if (!Objects.equals(existingEntity.getDictValue(), updateForm.getDictValue())) {
                if (!checkDictValueUnique(existingEntity.getDictTypeId(), updateForm.getDictValue(),
                        updateForm.getDictDataId()).getData()) {
                    return ResponseDTO.userErrorParam("字典值已存在");
                }
            }

            // 如果是默认值，清除其他默认值
            if (Objects.equals(updateForm.getIsDefault(), 1) && !Objects.equals(existingEntity.getIsDefault(), 1)) {
                dictDataManager.clearOtherDefaultValues(existingEntity.getDictTypeId());
            }

            // 更新数据
            DictDataEntity entity = new DictDataEntity();
            SmartBeanUtil.copyProperties(updateForm, entity);
            entity.setUpdateUserId(userId);

            dictDataDao.updateById(entity);

            log.info("字典数据更新成功，dictDataId：{}", updateForm.getDictDataId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新字典数据失败，dictDataId：{}，错误：{}", updateForm.getDictDataId(), e.getMessage(), e);
            throw new BusinessException("更新字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Void> deleteDictData(Long dictDataId, Long userId) {
        log.info("删除字典数据，dictDataId：{}，userId：{}", dictDataId, userId);

        try {
            if (dictDataId == null) {
                return ResponseDTO.userErrorParam("字典数据ID不能为空");
            }

            // 检查字典数据是否存在
            DictDataEntity entity = dictDataDao.selectById(dictDataId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("字典数据不存在");
            }

            // 获取字典类型信息
            DictTypeEntity dictType = dictTypeDao.selectById(entity.getDictTypeId());
            if (dictType != null && Objects.equals(dictType.getIsSystem(), 1)) {
                return ResponseDTO.userErrorParam("系统内置字典数据不允许删除");
            }

            // 逻辑删除
            entity.setDeletedFlag(1);
            entity.setUpdateUserId(userId);
            dictDataDao.updateById(entity);

            log.info("字典数据删除成功，dictDataId：{}", dictDataId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("删除字典数据失败，dictDataId：{}，错误：{}", dictDataId, e.getMessage(), e);
            throw new BusinessException("删除字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Void> batchDeleteDictData(List<Long> dictDataIds, Long userId) {
        log.info("批量删除字典数据，dictDataIds：{}，userId：{}", dictDataIds, userId);

        try {
            if (dictDataIds == null || dictDataIds.isEmpty()) {
                return ResponseDTO.userErrorParam("字典数据ID列表不能为空");
            }

            int successCount = 0;
            for (Long dictDataId : dictDataIds) {
                ResponseDTO<Void> result = deleteDictData(dictDataId, userId);
                if (result.getOk()) {
                    successCount++;
                }
            }

            log.info("批量删除字典数据完成，总数：{}，成功：{}", dictDataIds.size(), successCount);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("批量删除字典数据失败，dictDataIds：{}，错误：{}", dictDataIds, e.getMessage(), e);
            throw new BusinessException("批量删除字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictData", key = "#dictDataId", unless = "#result == null")
    public ResponseDTO<DictDataVO> getDictDataDetail(Long dictDataId) {
        log.debug("获取字典数据详情，dictDataId：{}", dictDataId);

        try {
            if (dictDataId == null) {
                return ResponseDTO.userErrorParam("字典数据ID不能为空");
            }

            DictDataEntity entity = dictDataDao.selectById(dictDataId);
            if (entity == null || Objects.equals(entity.getDeletedFlag(), 1)) {
                return ResponseDTO.userErrorParam("字典数据不存在");
            }

            DictDataVO vo = new DictDataVO();
            SmartBeanUtil.copyProperties(entity, vo);

            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("获取字典数据详情失败，dictDataId：{}，错误：{}", dictDataId, e.getMessage(), e);
            throw new BusinessException("获取字典数据详情失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<DictDataVO>> queryDictDataPage(DictQueryForm queryForm) {
        log.debug("分页查询字典数据，查询条件：{}", queryForm);

        try {
            // 构建查询条件
            LambdaQueryWrapper<DictDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictDataEntity::getDeletedFlag, 0);

            if (queryForm != null) {
                if (queryForm.getDictTypeId() != null) {
                    wrapper.eq(DictDataEntity::getDictTypeId, queryForm.getDictTypeId());
                }
                if (SmartStringUtil.isNotEmpty(queryForm.getDictTypeCode())) {
                    // 根据字典类型编码查询
                    DictTypeEntity dictType = dictTypeDao.selectOne(
                            new LambdaQueryWrapper<DictTypeEntity>()
                                    .eq(DictTypeEntity::getDictTypeCode, queryForm.getDictTypeCode())
                                    .eq(DictTypeEntity::getDeletedFlag, 0));
                    if (dictType != null) {
                        wrapper.eq(DictDataEntity::getDictTypeId, dictType.getDictTypeId());
                    }
                }
                if (SmartStringUtil.isNotEmpty(queryForm.getDictLabel())) {
                    wrapper.like(DictDataEntity::getDictLabel, queryForm.getDictLabel());
                }
                if (SmartStringUtil.isNotEmpty(queryForm.getDictValue())) {
                    wrapper.like(DictDataEntity::getDictValue, queryForm.getDictValue());
                }
                if (queryForm.getStatus() != null) {
                    wrapper.eq(DictDataEntity::getStatus, queryForm.getStatus());
                }
            }

            wrapper.orderByAsc(DictDataEntity::getSortOrder, DictDataEntity::getDictDataId);

            // 分页查询 - 使用反射访问 Lombok 生成的 getter 方法
            int pageNum = 1;
            int pageSize = 20;
            if (queryForm != null) {
                try {
                    java.lang.reflect.Method getPageNumMethod = queryForm.getClass().getMethod("getPageNum");
                    Object pageNumObj = getPageNumMethod.invoke(queryForm);
                    if (pageNumObj != null) {
                        pageNum = (Integer) pageNumObj;
                    }

                    java.lang.reflect.Method getPageSizeMethod = queryForm.getClass().getMethod("getPageSize");
                    Object pageSizeObj = getPageSizeMethod.invoke(queryForm);
                    if (pageSizeObj != null) {
                        pageSize = (Integer) pageSizeObj;
                    }
                } catch (Exception e) {
                    log.warn("无法通过反射访问分页参数，使用默认值", e);
                }
            }
            Page<DictDataEntity> page = new Page<>(pageNum, pageSize);

            IPage<DictDataEntity> pageResult = dictDataDao.selectPage(page, wrapper);

            // 转换为VO
            List<DictDataVO> voList = SmartBeanUtil.copyList(pageResult.getRecords(), DictDataVO.class);

            PageResult<DictDataVO> result = new PageResult<>();
            result.setList(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum(pageResult.getCurrent());
            result.setPageSize(pageResult.getSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("分页查询字典数据失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("分页查询字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictDataByType", key = "'typeId:' + #dictTypeId", unless = "#result == null")
    public ResponseDTO<List<DictDataVO>> getDictDataByTypeId(Long dictTypeId) {
        log.debug("根据字典类型ID查询字典数据，dictTypeId：{}", dictTypeId);

        try {
            if (dictTypeId == null) {
                return ResponseDTO.userErrorParam("字典类型ID不能为空");
            }

            LambdaQueryWrapper<DictDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictDataEntity::getDictTypeId, dictTypeId);
            wrapper.eq(DictDataEntity::getDeletedFlag, 0);
            wrapper.eq(DictDataEntity::getStatus, 1);
            wrapper.orderByAsc(DictDataEntity::getSortOrder, DictDataEntity::getDictDataId);

            List<DictDataEntity> entityList = dictDataDao.selectList(wrapper);
            List<DictDataVO> voList = SmartBeanUtil.copyList(entityList, DictDataVO.class);

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("根据字典类型ID查询字典数据失败，dictTypeId：{}，错误：{}", dictTypeId, e.getMessage(), e);
            throw new BusinessException("根据字典类型ID查询字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictDataByType", key = "'typeCode:' + #dictTypeCode", unless = "#result == null")
    public ResponseDTO<List<DictDataVO>> getDictDataByTypeCode(String dictTypeCode) {
        log.debug("根据字典类型编码查询字典数据，dictTypeCode：{}", dictTypeCode);

        try {
            if (SmartStringUtil.isEmpty(dictTypeCode)) {
                return ResponseDTO.userErrorParam("字典类型编码不能为空");
            }

            // 先查询字典类型
            DictTypeEntity dictType = dictTypeDao.selectOne(
                    new LambdaQueryWrapper<DictTypeEntity>()
                            .eq(DictTypeEntity::getDictTypeCode, dictTypeCode)
                            .eq(DictTypeEntity::getDeletedFlag, 0));

            if (dictType == null) {
                return ResponseDTO.ok(new ArrayList<>());
            }

            // 查询字典数据
            return getDictDataByTypeId(dictType.getDictTypeId());

        } catch (Exception e) {
            log.error("根据字典类型编码查询字典数据失败，dictTypeCode：{}，错误：{}", dictTypeCode, e.getMessage(), e);
            throw new BusinessException("根据字典类型编码查询字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "allDictData", unless = "#result == null")
    public ResponseDTO<List<DictDataVO>> getAllDictData() {
        log.debug("获取所有字典数据");

        try {
            LambdaQueryWrapper<DictDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictDataEntity::getDeletedFlag, 0);
            wrapper.orderByAsc(DictDataEntity::getDictTypeId, DictDataEntity::getSortOrder,
                    DictDataEntity::getDictDataId);

            List<DictDataEntity> entityList = dictDataDao.selectList(wrapper);
            List<DictDataVO> voList = SmartBeanUtil.copyList(entityList, DictDataVO.class);

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取所有字典数据失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取所有字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType", "allDictData" }, allEntries = true)
    public ResponseDTO<Void> refreshDictDataCache(Long dictTypeId) {
        log.info("刷新字典数据缓存，dictTypeId：{}", dictTypeId);

        try {
            // 这里可以添加具体的缓存刷新逻辑
            // 由于使用了注解缓存，这个方法主要用于手动触发缓存刷新

            if (dictTypeId != null) {
                // 刷新特定字典类型的缓存
                DictTypeEntity dictType = dictTypeDao.selectById(dictTypeId);
                if (dictType != null) {
                    log.info("刷新字典数据缓存成功，dictTypeCode：{}", dictType.getDictTypeCode());
                }
            } else {
                // 刷新所有字典数据缓存
                log.info("刷新所有字典数据缓存成功");
            }

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("刷新字典数据缓存失败，dictTypeId：{}，错误：{}", dictTypeId, e.getMessage(), e);
            throw new BusinessException("刷新字典数据缓存失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> exportDictData(DictQueryForm queryForm) {
        log.debug("导出字典数据，查询条件：{}", queryForm);

        try {
            ResponseDTO<PageResult<DictDataVO>> pageResult = queryDictDataPage(queryForm);
            if (!pageResult.getOk()) {
                return ResponseDTO.error("查询数据失败");
            }

            List<Map<String, Object>> exportData = new ArrayList<>();
            for (DictDataVO vo : pageResult.getData().getList()) {
                Map<String, Object> item = new HashMap<>();
                item.put("dictDataId", vo.getDictDataId());
                item.put("dictTypeCode", vo.getDictTypeCode());
                item.put("dictLabel", vo.getDictLabel());
                item.put("dictValue", vo.getDictValue());
                item.put("cssClass", vo.getCssClass());
                item.put("listClass", vo.getListClass());
                item.put("isDefault", vo.getIsDefault());
                item.put("sortOrder", vo.getSortOrder());
                item.put("status", vo.getStatus());
                item.put("remark", vo.getRemark());
                exportData.add(item);
            }

            return ResponseDTO.ok(exportData);

        } catch (Exception e) {
            log.error("导出字典数据失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("导出字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Map<String, Object>> importDictData(List<Map<String, Object>> importData, Long userId) {
        log.info("导入字典数据，数据量：{}，userId：{}", importData.size(), userId);

        try {
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Map<String, Object> item : importData) {
                try {
                    DictDataAddForm addForm = new DictDataAddForm();
                    addForm.setDictTypeCode((String) item.get("dictTypeCode"));
                    addForm.setDictLabel((String) item.get("dictLabel"));
                    addForm.setDictValue((String) item.get("dictValue"));
                    addForm.setCssClass((String) item.get("cssClass"));
                    addForm.setListClass((String) item.get("listClass"));
                    addForm.setIsDefault((Integer) item.get("isDefault"));
                    addForm.setSortOrder((Integer) item.get("sortOrder"));
                    addForm.setStatus((Integer) item.get("status"));
                    addForm.setRemark((String) item.get("remark"));

                    ResponseDTO<Long> result = addDictData(addForm, userId);
                    if (result.getOk()) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("导入失败：" + result.getMsg());
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("导入失败：" + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errorMessages", errorMessages);

            log.info("字典数据导入完成，总数：{}，成功：{}，失败：{}", importData.size(), successCount, failCount);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("导入字典数据失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("导入字典数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Void> changeDictDataStatus(Long dictDataId, Integer status, Long userId) {
        log.info("修改字典数据状态，dictDataId：{}，status：{}，userId：{}", dictDataId, status, userId);

        try {
            if (dictDataId == null || status == null) {
                return ResponseDTO.userErrorParam("参数不能为空");
            }

            // 检查字典数据是否存在
            DictDataEntity entity = dictDataDao.selectById(dictDataId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("字典数据不存在");
            }

            // 获取字典类型信息
            DictTypeEntity dictType = dictTypeDao.selectById(entity.getDictTypeId());
            if (dictType != null && Objects.equals(dictType.getIsSystem(), 1)) {
                return ResponseDTO.userErrorParam("系统内置字典数据不允许修改状态");
            }

            // 更新状态
            entity.setStatus(status);
            entity.setUpdateUserId(userId);
            dictDataDao.updateById(entity);

            log.info("字典数据状态修改成功，dictDataId：{}，status：{}", dictDataId, status);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("修改字典数据状态失败，dictDataId：{}，status：{}，错误：{}", dictDataId, status, e.getMessage(), e);
            throw new BusinessException("修改字典数据状态失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictData", "dictDataByType" }, allEntries = true)
    public ResponseDTO<Void> batchChangeDictDataStatus(List<Long> dictDataIds, Integer status, Long userId) {
        log.info("批量修改字典数据状态，dictDataIds：{}，status：{}，userId：{}", dictDataIds, status, userId);

        try {
            if (dictDataIds == null || dictDataIds.isEmpty() || status == null) {
                return ResponseDTO.userErrorParam("参数不能为空");
            }

            int successCount = 0;
            for (Long dictDataId : dictDataIds) {
                ResponseDTO<Void> result = changeDictDataStatus(dictDataId, status, userId);
                if (result.getOk()) {
                    successCount++;
                }
            }

            log.info("批量修改字典数据状态完成，总数：{}，成功：{}", dictDataIds.size(), successCount);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("批量修改字典数据状态失败，dictDataIds：{}，status：{}，错误：{}", dictDataIds, status, e.getMessage(), e);
            throw new BusinessException("批量修改字典数据状态失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictValueUnique", key = "#dictTypeId + '_' + #dictValue + '_' + #excludeId", unless = "#result == null")
    public ResponseDTO<Boolean> checkDictValueUnique(Long dictTypeId, String dictValue, Long excludeId) {
        log.debug("检查字典值唯一性，dictTypeId：{}，dictValue：{}，excludeId：{}", dictTypeId, dictValue, excludeId);

        try {
            if (dictTypeId == null || SmartStringUtil.isEmpty(dictValue)) {
                return ResponseDTO.ok(false);
            }

            LambdaQueryWrapper<DictDataEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictDataEntity::getDictTypeId, dictTypeId);
            wrapper.eq(DictDataEntity::getDictValue, dictValue);
            wrapper.eq(DictDataEntity::getDeletedFlag, 0);

            if (excludeId != null) {
                wrapper.ne(DictDataEntity::getDictDataId, excludeId);
            }

            long count = dictDataDao.selectCount(wrapper);
            return ResponseDTO.ok(count == 0);

        } catch (Exception e) {
            log.error("检查字典值唯一性失败，dictTypeId：{}，dictValue：{}，错误：{}", dictTypeId, dictValue, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDictCache() {
        log.debug("获取字典缓存");

        try {
            Map<String, Object> cacheData = new HashMap<>();

            // 获取所有启用的字典类型
            List<DictTypeEntity> dictTypes = dictTypeDao.selectList(
                    new LambdaQueryWrapper<DictTypeEntity>()
                            .eq(DictTypeEntity::getDeletedFlag, 0)
                            .eq(DictTypeEntity::getStatus, 1));

            for (DictTypeEntity dictType : dictTypes) {
                ResponseDTO<List<DictDataVO>> dictDataResult = getDictDataByTypeId(dictType.getDictTypeId());
                if (dictDataResult.getOk()) {
                    cacheData.put(dictType.getDictTypeCode(), dictDataResult.getData());
                }
            }

            return ResponseDTO.ok(cacheData);

        } catch (Exception e) {
            log.error("获取字典缓存失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取字典缓存失败：" + e.getMessage());
        }
    }
}
