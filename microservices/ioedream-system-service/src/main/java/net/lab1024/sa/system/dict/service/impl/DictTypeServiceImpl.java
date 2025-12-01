package net.lab1024.sa.system.dict.service.impl;

import java.util.List;
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
import net.lab1024.sa.system.dict.dao.DictTypeDao;
import net.lab1024.sa.system.dict.domain.entity.DictTypeEntity;
import net.lab1024.sa.system.dict.manager.DictTypeManager;
import net.lab1024.sa.system.dict.service.DictTypeService;
import net.lab1024.sa.system.domain.form.DictQueryForm;
import net.lab1024.sa.system.domain.form.DictTypeAddForm;
import net.lab1024.sa.system.domain.form.DictTypeUpdateForm;
import net.lab1024.sa.system.domain.vo.DictTypeVO;

/**
 * 字典类型服务实现
 * <p>
 * 严格遵循四层架构：Service → Manager → DAO
 * 严格遵循编码规范：jakarta包名、@Resource注入、完整异常处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DictTypeServiceImpl implements DictTypeService {

    @Resource
    private DictTypeDao dictTypeDao;

    @Resource
    private DictTypeManager dictTypeManager;

    @Override
    @CacheEvict(value = "dictType", allEntries = true)
    public ResponseDTO<Long> addDictType(DictTypeAddForm addForm, Long userId) {
        log.info("新增字典类型，dictTypeCode：{}，userId：{}", addForm.getDictTypeCode(), userId);

        try {
            // 参数验证
            if (addForm == null || SmartStringUtil.isEmpty(addForm.getDictTypeCode())) {
                return ResponseDTO.userErrorParam("字典类型编码不能为空");
            }

            // 检查字典类型编码是否已存在
            if (!checkDictTypeCodeUnique(addForm.getDictTypeCode(), null).getData()) {
                return ResponseDTO.userErrorParam("字典类型编码已存在");
            }

            // 构建实体
            DictTypeEntity entity = new DictTypeEntity();
            SmartBeanUtil.copyProperties(addForm, entity);
            entity.setCreateUserId(userId);

            // 保存数据
            dictTypeDao.insert(entity);

            log.info("字典类型新增成功，dictTypeId：{}，dictTypeCode：{}", entity.getDictTypeId(), addForm.getDictTypeCode());
            return ResponseDTO.ok(entity.getDictTypeId());

        } catch (Exception e) {
            log.error("新增字典类型失败，dictTypeCode：{}，错误：{}", addForm.getDictTypeCode(), e.getMessage(), e);
            throw new BusinessException("新增字典类型失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "dictType", allEntries = true)
    public ResponseDTO<Void> updateDictType(DictTypeUpdateForm updateForm, Long userId) {
        log.info("更新字典类型，dictTypeId：{}，userId：{}", updateForm.getDictTypeId(), userId);

        try {
            if (updateForm == null || updateForm.getDictTypeId() == null) {
                return ResponseDTO.userErrorParam("字典类型ID不能为空");
            }

            // 检查字典类型是否存在
            DictTypeEntity existingEntity = dictTypeDao.selectById(updateForm.getDictTypeId());
            if (existingEntity == null) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            // 系统内置字典类型不允许修改编码
            if (Objects.equals(existingEntity.getIsSystem(), 1) &&
                    !Objects.equals(existingEntity.getDictTypeCode(), updateForm.getDictTypeCode())) {
                return ResponseDTO.userErrorParam("系统内置字典类型不允许修改编码");
            }

            // 检查编码唯一性
            if (!checkDictTypeCodeUnique(updateForm.getDictTypeCode(), updateForm.getDictTypeId()).getData()) {
                return ResponseDTO.userErrorParam("字典类型编码已存在");
            }

            // 更新数据
            DictTypeEntity entity = new DictTypeEntity();
            SmartBeanUtil.copyProperties(updateForm, entity);
            entity.setUpdateUserId(userId);

            dictTypeDao.updateById(entity);

            log.info("字典类型更新成功，dictTypeId：{}", updateForm.getDictTypeId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新字典类型失败，dictTypeId：{}，错误：{}", updateForm.getDictTypeId(), e.getMessage(), e);
            throw new BusinessException("更新字典类型失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "dictType", allEntries = true)
    public ResponseDTO<Void> deleteDictType(Long dictTypeId, Long userId) {
        log.info("删除字典类型，dictTypeId：{}，userId：{}", dictTypeId, userId);

        try {
            if (dictTypeId == null) {
                return ResponseDTO.userErrorParam("字典类型ID不能为空");
            }

            // 检查字典类型是否存在
            DictTypeEntity entity = dictTypeDao.selectById(dictTypeId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            // 系统内置字典类型不允许删除
            if (Objects.equals(entity.getIsSystem(), 1)) {
                return ResponseDTO.userErrorParam("系统内置字典类型不允许删除");
            }

            // 检查是否有字典数据
            long dictDataCount = dictTypeManager.countDictDataByTypeId(dictTypeId);
            if (dictDataCount > 0) {
                return ResponseDTO.userErrorParam("该字典类型下存在字典数据，不允许删除");
            }

            // 逻辑删除
            entity.setDeletedFlag(1);
            entity.setUpdateUserId(userId);
            dictTypeDao.updateById(entity);

            log.info("字典类型删除成功，dictTypeId：{}", dictTypeId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("删除字典类型失败，dictTypeId：{}，错误：{}", dictTypeId, e.getMessage(), e);
            throw new BusinessException("删除字典类型失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictType", key = "#dictTypeId", unless = "#result == null")
    public ResponseDTO<DictTypeVO> getDictTypeDetail(Long dictTypeId) {
        log.debug("获取字典类型详情，dictTypeId：{}", dictTypeId);

        try {
            if (dictTypeId == null) {
                return ResponseDTO.userErrorParam("字典类型ID不能为空");
            }

            DictTypeEntity entity = dictTypeDao.selectById(dictTypeId);
            if (entity == null || Objects.equals(entity.getDeletedFlag(), 1)) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            DictTypeVO vo = new DictTypeVO();
            SmartBeanUtil.copyProperties(entity, vo);

            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("获取字典类型详情失败，dictTypeId：{}，错误：{}", dictTypeId, e.getMessage(), e);
            throw new BusinessException("获取字典类型详情失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<DictTypeVO>> queryDictTypePage(DictQueryForm queryForm) {
        log.debug("分页查询字典类型，查询条件：{}", queryForm);

        try {
            // 构建查询条件
            LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictTypeEntity::getDeletedFlag, 0);

            if (queryForm != null) {
                if (SmartStringUtil.isNotEmpty(queryForm.getDictTypeCode())) {
                    wrapper.like(DictTypeEntity::getDictTypeCode, queryForm.getDictTypeCode());
                }
                if (SmartStringUtil.isNotEmpty(queryForm.getDictTypeName())) {
                    wrapper.like(DictTypeEntity::getDictTypeName, queryForm.getDictTypeName());
                }
                if (queryForm.getStatus() != null) {
                    wrapper.eq(DictTypeEntity::getStatus, queryForm.getStatus());
                }
            }

            wrapper.orderByDesc(DictTypeEntity::getCreateTime);

            // 分页查询
            // DictQueryForm 继承自 PageForm，PageForm 使用 @Data 注解会生成 getPageNum() 和
            // getPageSize() 方法
            // 如果 IDE 报错，请确保 Lombok 插件已正确安装和配置，或重新构建项目
            int pageNum = 1;
            int pageSize = 20;
            if (queryForm != null) {
                try {
                    // 使用反射访问 Lombok 生成的 getter 方法（绕过 IDE 的 Lombok 识别问题）
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
                    // 如果反射失败，使用默认值
                    log.warn("无法通过反射访问分页参数，使用默认值", e);
                }
            }
            Page<DictTypeEntity> page = new Page<>(pageNum, pageSize);

            IPage<DictTypeEntity> pageResult = dictTypeDao.selectPage(page, wrapper);

            // 转换为VO
            List<DictTypeVO> voList = SmartBeanUtil.copyList(pageResult.getRecords(), DictTypeVO.class);

            PageResult<DictTypeVO> result = new PageResult<>();
            result.setList(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum(pageResult.getCurrent());
            result.setPageSize(pageResult.getSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("分页查询字典类型失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("分页查询字典类型失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictTypeList", key = "#queryForm.hashCode()", unless = "#result == null")
    public ResponseDTO<List<DictTypeVO>> queryDictTypeList(DictQueryForm queryForm) {
        log.debug("查询字典类型列表，查询条件：{}", queryForm);

        try {
            // 构建查询条件
            LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictTypeEntity::getDeletedFlag, 0);
            wrapper.eq(DictTypeEntity::getStatus, 1); // 只查询启用状态
            if (queryForm != null) {
                if (SmartStringUtil.isNotEmpty(queryForm.getDictTypeCode())) {
                    wrapper.like(DictTypeEntity::getDictTypeCode, queryForm.getDictTypeCode());
                }
                if (SmartStringUtil.isNotEmpty(queryForm.getDictTypeName())) {
                    wrapper.like(DictTypeEntity::getDictTypeName, queryForm.getDictTypeName());
                }
            }

            wrapper.orderByAsc(DictTypeEntity::getDictTypeId, DictTypeEntity::getCreateTime);

            List<DictTypeEntity> entityList = dictTypeDao.selectList(wrapper);
            List<DictTypeVO> voList = SmartBeanUtil.copyList(entityList, DictTypeVO.class);

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("查询字典类型列表失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("查询字典类型列表失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "allDictTypes", unless = "#result == null")
    public ResponseDTO<List<DictTypeVO>> getAllDictTypes() {
        log.debug("获取所有字典类型");

        try {
            LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictTypeEntity::getDeletedFlag, 0);
            wrapper.orderByAsc(DictTypeEntity::getDictTypeId, DictTypeEntity::getCreateTime);

            List<DictTypeEntity> entityList = dictTypeDao.selectList(wrapper);
            List<DictTypeVO> voList = SmartBeanUtil.copyList(entityList, DictTypeVO.class);

            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("获取所有字典类型失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取所有字典类型失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "dictType", "dictTypeList", "allDictTypes" }, allEntries = true)
    public ResponseDTO<Void> refreshDictTypeCache(Long dictTypeId) {
        log.info("刷新字典类型缓存，dictTypeId：{}", dictTypeId);

        try {
            // 这里可以添加具体的缓存刷新逻辑
            // 由于使用了注解缓存，这个方法主要用于手动触发缓存刷新

            if (dictTypeId != null) {
                // 刷新特定字典类型的缓存
                DictTypeEntity entity = dictTypeDao.selectById(dictTypeId);
                if (entity != null) {
                    log.info("刷新字典类型缓存成功，dictTypeCode：{}", entity.getDictTypeCode());
                }
            } else {
                // 刷新所有字典类型缓存
                log.info("刷新所有字典类型缓存成功");
            }

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("刷新字典类型缓存失败，dictTypeId：{}，错误：{}", dictTypeId, e.getMessage(), e);
            throw new BusinessException("刷新字典类型缓存失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "dictTypeCodeUnique", key = "#dictTypeCode + '_' + #excludeId", unless = "#result == null")
    public ResponseDTO<Boolean> checkDictTypeCodeUnique(String dictTypeCode, Long excludeId) {
        log.debug("检查字典类型编码唯一性，dictTypeCode：{}，excludeId：{}", dictTypeCode, excludeId);

        try {
            if (SmartStringUtil.isEmpty(dictTypeCode)) {
                return ResponseDTO.ok(false);
            }

            LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DictTypeEntity::getDictTypeCode, dictTypeCode);
            wrapper.eq(DictTypeEntity::getDeletedFlag, 0);

            if (excludeId != null) {
                wrapper.ne(DictTypeEntity::getDictTypeId, excludeId);
            }

            long count = dictTypeDao.selectCount(wrapper);
            return ResponseDTO.ok(count == 0);

        } catch (Exception e) {
            log.error("检查字典类型编码唯一性失败，dictTypeCode：{}，错误：{}", dictTypeCode, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    @CacheEvict(value = "dictType", allEntries = true)
    public ResponseDTO<Void> changeDictTypeStatus(Long dictTypeId, Integer status, Long userId) {
        log.info("修改字典类型状态，dictTypeId：{}，status：{}，userId：{}", dictTypeId, status, userId);

        try {
            if (dictTypeId == null || status == null) {
                return ResponseDTO.userErrorParam("参数不能为空");
            }

            // 检查字典类型是否存在
            DictTypeEntity entity = dictTypeDao.selectById(dictTypeId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("字典类型不存在");
            }

            // 系统内置字典类型不允许禁用
            if (Objects.equals(entity.getIsSystem(), 1) && Objects.equals(status, 0)) {
                return ResponseDTO.userErrorParam("系统内置字典类型不允许禁用");
            }

            // 更新状态
            entity.setStatus(status);
            entity.setUpdateUserId(userId);
            dictTypeDao.updateById(entity);

            log.info("字典类型状态修改成功，dictTypeId：{}，status：{}", dictTypeId, status);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("修改字典类型状态失败，dictTypeId：{}，status：{}，错误：{}", dictTypeId, status, e.getMessage(), e);
            throw new BusinessException("修改字典类型状态失败：" + e.getMessage());
        }
    }
}
