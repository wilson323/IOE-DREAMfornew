package net.lab1024.sa.system.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.system.dao.DepartmentDao;
import net.lab1024.sa.system.domain.entity.DepartmentEntity;
import net.lab1024.sa.system.domain.form.DepartmentAddForm;
import net.lab1024.sa.system.domain.form.DepartmentQueryForm;
import net.lab1024.sa.system.domain.form.DepartmentUpdateForm;
import net.lab1024.sa.system.domain.vo.DepartmentVO;
import net.lab1024.sa.system.employee.manager.EmployeeManager;
import net.lab1024.sa.system.service.DepartmentService;

/**
 * 部门服务实现类
 * <p>
 * 严格遵循repowiki Service实现规范：
 * - 使用@Resource依赖注入
 * - 事务管理
 * - 完整的异常处理
 * - 日志记录
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CacheService cacheService;

    @Resource
    private EmployeeManager employeeManager;

    /**
     * 部门缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "department:";

    /**
     * 部门列表缓存键
     */
    private static final String CACHE_KEY_ALL_LIST = CACHE_KEY_PREFIX + "all:list";

    /**
     * 部门树缓存键
     */
    private static final String CACHE_KEY_TREE = CACHE_KEY_PREFIX + "tree";

    /**
     * 构建部门详情缓存键
     *
     * @param departmentId 部门ID
     * @return 缓存键
     */
    private String buildDepartmentCacheKey(Long departmentId) {
        return CACHE_KEY_PREFIX + "info:" + departmentId;
    }

    /**
     * 构建部门列表缓存键（根据父部门）
     *
     * @param parentId 父部门ID
     * @return 缓存键
     */
    private String buildDepartmentListCacheKey(Long parentId) {
        return CACHE_KEY_PREFIX + "list:" + (parentId != null ? parentId : "root");
    }

    @Override
    public PageResult<DepartmentVO> queryDepartmentPage(DepartmentQueryForm queryForm) {
        log.info("查询部门分页数据，查询条件：{}", queryForm);

        try {
            // 创建分页对象 - 使用反射访问 Lombok 生成的 getter 方法
            int pageNum = 1;
            int pageSize = 20;
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
            Page<DepartmentEntity> page = new Page<>(pageNum, pageSize);

            IPage<DepartmentEntity> pageResult = departmentDao.selectPageByCondition(page,
                    queryForm.getDepartmentName(),
                    queryForm.getDepartmentCode(),
                    queryForm.getParentId(),
                    queryForm.getStatus(),
                    queryForm.getManager());

            List<DepartmentVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 转换为Page对象以使用convert2PageResult
            Page<DepartmentEntity> pageObj = new Page<>(pageNum, pageSize);
            pageObj.setTotal(pageResult.getTotal());
            pageObj.setPages(pageResult.getPages());
            PageResult<DepartmentVO> pageResultVO = SmartPageUtil.convert2PageResult(pageObj, voList);

            log.info("部门分页查询成功，总数：{}", pageResultVO.getTotal());
            return pageResultVO;

        } catch (Exception e) {
            log.error("部门分页查询失败", e);
            // 使用反射访问分页参数
            int pageNum = 1;
            int pageSize = 20;
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
            } catch (Exception ex) {
                log.warn("无法通过反射访问分页参数，使用默认值", ex);
            }
            return PageResult.of(new ArrayList<>(), 0L, Long.valueOf(pageNum), Long.valueOf(pageSize));
        }
    }

    @Override
    public List<DepartmentVO> queryDepartmentList(DepartmentQueryForm queryForm) {
        log.info("查询部门列表，查询条件：{}", queryForm);

        try {
            List<DepartmentEntity> entityList = departmentDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                            .like(StringUtils.hasText(queryForm.getDepartmentName()),
                                    DepartmentEntity::getDepartmentName, queryForm.getDepartmentName())
                            .eq(StringUtils.hasText(queryForm.getDepartmentCode()), DepartmentEntity::getDepartmentCode,
                                    queryForm.getDepartmentCode())
                            .eq(queryForm.getParentId() != null, DepartmentEntity::getParentId, queryForm.getParentId())
                            .eq(queryForm.getStatus() != null, DepartmentEntity::getStatus, queryForm.getStatus())
                            .like(StringUtils.hasText(queryForm.getManager()), DepartmentEntity::getManager,
                                    queryForm.getManager())
                            .eq(DepartmentEntity::getDeletedFlag, 0)
                            .orderByAsc(DepartmentEntity::getSortNumber)
                            .orderByAsc(DepartmentEntity::getCreateTime));

            List<DepartmentVO> voList = entityList.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("部门列表查询成功，数量：{}", voList.size());
            return voList;

        } catch (Exception e) {
            log.error("部门列表查询失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DepartmentVO> getDepartmentTree(Boolean onlyEnabled) {
        log.info("获取部门树形结构，onlyEnabled：{}", onlyEnabled);

        try {
            List<DepartmentEntity> allDepartments;

            if (onlyEnabled != null && onlyEnabled) {
                allDepartments = departmentDao.selectAllEnabled();
            } else {
                allDepartments = departmentDao.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                                .eq(DepartmentEntity::getDeletedFlag, 0)
                                .orderByAsc(DepartmentEntity::getSortNumber)
                                .orderByAsc(DepartmentEntity::getCreateTime));
            }

            List<DepartmentVO> voList = allDepartments.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            List<DepartmentVO> treeList = buildDepartmentTree(voList, 0L);

            log.info("部门树形结构构建成功，根节点数量：{}", treeList.size());
            return treeList;

        } catch (Exception e) {
            log.error("获取部门树形结构失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public ResponseDTO<DepartmentVO> getDepartmentById(Long departmentId) {
        log.info("根据ID获取部门详情，departmentId：{}", departmentId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                log.warn("部门不存在或已删除，departmentId：{}", departmentId);
                return ResponseDTO.error("部门不存在");
            }

            DepartmentVO vo = convertToVO(entity);
            log.info("部门详情查询成功，departmentId：{}", departmentId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("根据ID获取部门详情失败，departmentId：{}", departmentId, e);
            return ResponseDTO.error("查询部门详情失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<DepartmentVO> getDepartmentByCode(String departmentCode) {
        log.info("根据编码获取部门详情，departmentCode：{}", departmentCode);

        try {
            DepartmentEntity entity = departmentDao.selectByCode(departmentCode);
            if (entity == null) {
                log.warn("部门不存在，departmentCode：{}", departmentCode);
                return ResponseDTO.error("部门不存在");
            }

            DepartmentVO vo = convertToVO(entity);
            log.info("部门详情查询成功，departmentCode：{}", departmentCode);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("根据编码获取部门详情失败，departmentCode：{}", departmentCode, e);
            return ResponseDTO.error("查询部门详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addDepartment(DepartmentAddForm addForm, Long userId) {
        log.info("新增部门，form：{}，userId：{}", addForm, userId);

        try {
            // 验证部门编码唯一性
            if (checkDepartmentCodeExists(addForm.getDepartmentCode(), null)) {
                return ResponseDTO.error("部门编码已存在");
            }

            // 验证部门名称在同一父部门下唯一性
            if (checkDepartmentNameExists(addForm.getDepartmentName(), addForm.getParentId(), null)) {
                return ResponseDTO.error("同一父部门下部门名称已存在");
            }

            // 构建实体
            DepartmentEntity entity = SmartBeanUtil.copy(addForm, DepartmentEntity.class);
            entity.setCreateTime(LocalDateTime.now());
            entity.setCreateUserId(userId);
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUserId(userId);
            entity.setDeletedFlag(0);

            // 设置层级和路径
            if (addForm.getParentId() != null && addForm.getParentId() > 0) {
                DepartmentEntity parentEntity = departmentDao.selectById(addForm.getParentId());
                if (parentEntity == null || parentEntity.getDeletedFlag() == 1) {
                    return ResponseDTO.error("父部门不存在");
                }
                entity.setLevel(parentEntity.getLevel() + 1);
                entity.setDepartmentPath(parentEntity.getDepartmentPath() + "," + addForm.getParentId());
            } else {
                entity.setLevel(1);
                entity.setParentId(0L);
                entity.setDepartmentPath("0");
            }

            // 默认启用状态
            if (entity.getStatus() == null) {
                entity.setStatus(1);
            }

            // 默认排序
            if (entity.getSortNumber() == null) {
                entity.setSortNumber(getNextSortNumber(addForm.getParentId()));
            }

            // 保存
            int result = departmentDao.insert(entity);
            if (result <= 0) {
                return ResponseDTO.error("新增部门失败");
            }

            // 更新路径（包含自身ID）
            entity.setDepartmentPath(entity.getDepartmentPath() + "," + entity.getDepartmentId());
            departmentDao.updateById(entity);

            // 清除缓存
            clearDepartmentCache(null);

            log.info("新增部门成功，departmentId：{}", entity.getDepartmentId());
            return ResponseDTO.ok(entity.getDepartmentId());

        } catch (Exception e) {
            log.error("新增部门失败", e);
            return ResponseDTO.error("新增部门失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDepartment(DepartmentUpdateForm updateForm, Long userId) {
        log.info("更新部门，form：{}，userId：{}", updateForm, userId);

        try {
            DepartmentEntity existingEntity = departmentDao.selectById(updateForm.getDepartmentId());
            if (existingEntity == null || existingEntity.getDeletedFlag() == 1) {
                return ResponseDTO.error("部门不存在");
            }

            // 验证部门编码唯一性
            if (!existingEntity.getDepartmentCode().equals(updateForm.getDepartmentCode()) &&
                    checkDepartmentCodeExists(updateForm.getDepartmentCode(), updateForm.getDepartmentId())) {
                return ResponseDTO.error("部门编码已存在");
            }

            // 验证部门名称在同一父部门下唯一性
            if (!existingEntity.getParentId().equals(updateForm.getParentId()) &&
                    checkDepartmentNameExists(updateForm.getDepartmentName(), updateForm.getParentId(),
                            updateForm.getDepartmentId())) {
                return ResponseDTO.error("同一父部门下部门名称已存在");
            }

            // 检查是否尝试将部门移动到其子部门下
            if (updateForm.getParentId() != null && updateForm.getParentId() > 0 &&
                    isChildDepartment(updateForm.getDepartmentId(), updateForm.getParentId())) {
                return ResponseDTO.error("不能将部门移动到其子部门下");
            }

            // 更新实体
            DepartmentEntity entity = SmartBeanUtil.copy(updateForm, DepartmentEntity.class);
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUserId(userId);

            // 如果父部门发生变化，需要更新层级和路径
            if (!existingEntity.getParentId().equals(updateForm.getParentId())) {
                if (updateForm.getParentId() != null && updateForm.getParentId() > 0) {
                    DepartmentEntity parentEntity = departmentDao.selectById(updateForm.getParentId());
                    if (parentEntity == null || parentEntity.getDeletedFlag() == 1) {
                        return ResponseDTO.error("父部门不存在");
                    }
                    entity.setLevel(parentEntity.getLevel() + 1);
                    entity.setDepartmentPath(parentEntity.getDepartmentPath() + "," + updateForm.getParentId());
                } else {
                    entity.setLevel(1);
                    entity.setParentId(0L);
                    entity.setDepartmentPath("0");
                }

                // 更新所有子部门的层级和路径
                updateChildrenDepartmentInfo(updateForm.getDepartmentId(), entity.getDepartmentPath(),
                        entity.getLevel());
            }

            // 更新
            int result = departmentDao.updateById(entity);
            if (result <= 0) {
                return ResponseDTO.error("更新部门失败");
            }

            // 更新路径（包含自身ID）
            entity.setDepartmentPath(entity.getDepartmentPath() + "," + entity.getDepartmentId());
            departmentDao.updateById(entity);

            // 清除缓存
            clearDepartmentCache(null);

            log.info("更新部门成功，departmentId：{}", entity.getDepartmentId());
            return ResponseDTO.ok("更新部门成功");

        } catch (Exception e) {
            log.error("更新部门失败", e);
            return ResponseDTO.error("更新部门失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDepartment(Long departmentId, Long userId) {
        log.info("删除部门，departmentId：{}，userId：{}", departmentId, userId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.error("部门不存在");
            }

            // 检查是否有子部门
            if (hasChildren(departmentId)) {
                return ResponseDTO.error("存在子部门，无法删除");
            }

            // 逻辑删除
            entity.setDeletedFlag(1);
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUserId(userId);

            int result = departmentDao.updateById(entity);
            if (result <= 0) {
                return ResponseDTO.error("删除部门失败");
            }

            // 清除缓存
            clearDepartmentCache(departmentId);

            log.info("删除部门成功，departmentId：{}", departmentId);
            return ResponseDTO.ok("删除部门成功");

        } catch (Exception e) {
            log.error("删除部门失败", e);
            return ResponseDTO.error("删除部门失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchDeleteDepartment(List<Long> departmentIds, Long userId) {
        log.info("批量删除部门，departmentIds：{}，userId：{}", departmentIds, userId);

        try {
            int successCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Long departmentId : departmentIds) {
                try {
                    ResponseDTO<String> result = deleteDepartment(departmentId, userId);
                    if (result.getOk()) {
                        successCount++;
                    } else {
                        errorMessages.add("部门ID " + departmentId + ": " + result.getMsg());
                    }
                } catch (Exception e) {
                    errorMessages.add("部门ID " + departmentId + ": " + e.getMessage());
                }
            }

            if (successCount == departmentIds.size()) {
                return ResponseDTO.ok("批量删除部门成功");
            } else {
                return ResponseDTO.error("批量删除部分失败，成功：" + successCount + "，失败：" + errorMessages.size());
            }

        } catch (Exception e) {
            log.error("批量删除部门失败", e);
            return ResponseDTO.error("批量删除部门失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> changeDepartmentStatus(Long departmentId, Integer status, Long userId) {
        log.info("修改部门状态，departmentId：{}，status：{}，userId：{}", departmentId, status, userId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.error("部门不存在");
            }

            entity.setStatus(status);
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUserId(userId);

            int result = departmentDao.updateById(entity);
            if (result <= 0) {
                return ResponseDTO.error("修改部门状态失败");
            }

            // 清除缓存
            clearDepartmentCache(departmentId);

            log.info("修改部门状态成功，departmentId：{}，status：{}", departmentId, status);
            return ResponseDTO.ok("修改部门状态成功");

        } catch (Exception e) {
            log.error("修改部门状态失败", e);
            return ResponseDTO.error("修改部门状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchChangeDepartmentStatus(List<Long> departmentIds, Integer status, Long userId) {
        log.info("批量修改部门状态，departmentIds：{}，status：{}，userId：{}", departmentIds, status, userId);

        try {
            int successCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (Long departmentId : departmentIds) {
                try {
                    ResponseDTO<String> result = changeDepartmentStatus(departmentId, status, userId);
                    if (result.getOk()) {
                        successCount++;
                    } else {
                        errorMessages.add("部门ID " + departmentId + ": " + result.getMsg());
                    }
                } catch (Exception e) {
                    errorMessages.add("部门ID " + departmentId + ": " + e.getMessage());
                }
            }

            if (successCount == departmentIds.size()) {
                return ResponseDTO.ok("批量修改部门状态成功");
            } else {
                return ResponseDTO.error("批量修改部门状态部分失败，成功：" + successCount + "，失败：" + errorMessages.size());
            }

        } catch (Exception e) {
            log.error("批量修改部门状态失败", e);
            return ResponseDTO.error("批量修改部门状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> moveDepartment(Long departmentId, Long newParentId, Long userId) {
        log.info("移动部门，departmentId：{}，newParentId：{}，userId：{}", departmentId, newParentId, userId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return ResponseDTO.error("部门不存在");
            }

            // 检查是否尝试移动到其子部门树
            if (newParentId != null && newParentId > 0 && isChildDepartment(departmentId, newParentId)) {
                return ResponseDTO.error("不能将部门移动到其子部门下");
            }

            DepartmentUpdateForm updateForm = new DepartmentUpdateForm();
            updateForm.setDepartmentId(departmentId);
            SmartBeanUtil.copyProperties(entity, updateForm);
            updateForm.setParentId(newParentId);

            return updateDepartment(updateForm, userId);

        } catch (Exception e) {
            log.error("移动部门失败", e);
            return ResponseDTO.error("移动部门失败：" + e.getMessage());
        }
    }

    @Override
    public List<Long> getDepartmentSelfAndChildrenIds(Long departmentId) {
        log.info("获取部门及其子部门ID，departmentId：{}", departmentId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return new ArrayList<>();
            }

            List<Long> result = new ArrayList<>();
            result.add(departmentId);

            String pathPrefix = entity.getDepartmentPath() + "," + departmentId;
            List<DepartmentEntity> children = departmentDao.selectByPathPrefix(pathPrefix);

            for (DepartmentEntity child : children) {
                if (!child.getDepartmentId().equals(departmentId)) {
                    result.add(child.getDepartmentId());
                }
            }

            log.info("获取部门及其子部门ID成功，departmentId：{}，数量：{}", departmentId, result.size());
            return result;

        } catch (Exception e) {
            log.error("获取部门及其子部门ID失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public String getDepartmentPath(Long departmentId) {
        log.info("获取部门路径，departmentId：{}", departmentId);

        try {
            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return "";
            }

            String path = entity.getDepartmentPath();
            if (StringUtils.hasText(path) && !"0".equals(path)) {
                List<String> pathNames = new ArrayList<>();
                String[] pathIds = path.split(",");

                for (String pathId : pathIds) {
                    if (!"0".equals(pathId)) {
                        DepartmentEntity pathEntity = departmentDao.selectById(Long.parseLong(pathId));
                        if (pathEntity != null) {
                            pathNames.add(pathEntity.getDepartmentName());
                        }
                    }
                }
                pathNames.add(entity.getDepartmentName());
                return String.join("/", pathNames);
            } else {
                return entity.getDepartmentName();
            }

        } catch (Exception e) {
            log.error("获取部门路径失败", e);
            return "";
        }
    }

    @Override
    public Map<Long, String> getDepartmentPathMap() {
        log.info("获取部门路径映射");

        try {
            List<DepartmentEntity> allDepartments = departmentDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                            .eq(DepartmentEntity::getDeletedFlag, 0)
                            .orderByAsc(DepartmentEntity::getLevel)
                            .orderByAsc(DepartmentEntity::getSortNumber));

            Map<Long, String> pathMap = new HashMap<>();
            for (DepartmentEntity entity : allDepartments) {
                pathMap.put(entity.getDepartmentId(), getDepartmentPath(entity.getDepartmentId()));
            }

            log.info("获取部门路径映射成功，数量：{}", pathMap.size());
            return pathMap;

        } catch (Exception e) {
            log.error("获取部门路径映射失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean checkDepartmentCodeExists(String departmentCode, Long excludeId) {
        try {
            return departmentDao.countByCode(departmentCode, excludeId) > 0;
        } catch (Exception e) {
            log.error("检查部门编码是否存在失败", e);
            return false;
        }
    }

    @Override
    public boolean checkDepartmentNameExists(String departmentName, Long parentId, Long excludeId) {
        try {
            return departmentDao.countByName(departmentName, parentId, excludeId) > 0;
        } catch (Exception e) {
            log.error("检查部门名称是否存在失败", e);
            return false;
        }
    }

    @Override
    public boolean hasChildren(Long departmentId) {
        try {
            return departmentDao.countChildren(departmentId) > 0;
        } catch (Exception e) {
            log.error("检查是否存在子部门失败", e);
            return false;
        }
    }

    @Override
    public List<DepartmentVO> getAllEnabledDepartments() {
        log.info("获取所有启用的部门");

        try {
            List<DepartmentEntity> entities = departmentDao.selectAllEnabled();
            return entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取所有启用的部门失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DepartmentVO> getDepartmentsByManager(Long managerUserId) {
        log.info("根据负责人查询部门，managerUserId：{}", managerUserId);

        try {
            List<DepartmentEntity> entities = departmentDao.selectByManager(managerUserId);
            return entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("根据负责人查询部门失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getDepartmentStatistics() {
        log.info("获取部门统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总数统计
            long totalCount = departmentDao.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                            .eq(DepartmentEntity::getDeletedFlag, 0));
            statistics.put("totalCount", totalCount);

            // 启用数统计
            long enabledCount = departmentDao.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                            .eq(DepartmentEntity::getDeletedFlag, 0)
                            .eq(DepartmentEntity::getStatus, 1));
            statistics.put("enabledCount", enabledCount);

            // 禁用数统计
            long disabledCount = totalCount - enabledCount;
            statistics.put("disabledCount", disabledCount);

            // 层级统计
            Map<Integer, Long> levelCount = new HashMap<>();
            List<DepartmentEntity> allDepartments = departmentDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DepartmentEntity>()
                            .eq(DepartmentEntity::getDeletedFlag, 0));
            for (DepartmentEntity entity : allDepartments) {
                levelCount.merge(entity.getLevel(), 1L, Long::sum);
            }
            statistics.put("levelCount", levelCount);

            log.info("获取部门统计信息成功");
            return statistics;

        } catch (Exception e) {
            log.error("获取部门统计信息失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> exportDepartmentData(DepartmentQueryForm queryForm) {
        log.info("导出部门数据，查询条件：{}", queryForm);

        try {
            List<DepartmentVO> departmentList = queryDepartmentList(queryForm);

            return departmentList.stream().map(vo -> {
                Map<String, Object> data = new HashMap<>();
                data.put("部门ID", vo.getDepartmentId());
                data.put("部门名称", vo.getDepartmentName());
                data.put("部门编码", vo.getDepartmentCode());
                data.put("父部门", vo.getParentName());
                data.put("部门层级", vo.getLevel());
                data.put("排序", vo.getSortNumber());
                data.put("状态", vo.getStatusName());
                data.put("负责人", vo.getManager());
                data.put("联系电话", vo.getContactPhone());
                data.put("联系邮箱", vo.getContactEmail());
                data.put("部门地址", vo.getAddress());
                data.put("描述", vo.getDescription());
                data.put("创建时间", vo.getCreateTime());
                data.put("更新时间", vo.getUpdateTime());
                return data;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("导出部门数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> importDepartmentData(List<Map<String, Object>> importData, Long userId) {
        log.info("导入部门数据，数据量：{}，userId：{}", importData.size(), userId);

        try {
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < importData.size(); i++) {
                try {
                    Map<String, Object> data = importData.get(i);
                    DepartmentAddForm addForm = new DepartmentAddForm();

                    addForm.setDepartmentName((String) data.get("部门名称"));
                    addForm.setDepartmentCode((String) data.get("部门编码"));
                    addForm.setSortNumber(data.get("排序") != null ? Integer.parseInt(data.get("排序").toString()) : null);
                    addForm.setManager((String) data.get("负责人"));
                    addForm.setContactPhone((String) data.get("联系电话"));
                    addForm.setContactEmail((String) data.get("联系邮箱"));
                    addForm.setAddress((String) data.get("部门地址"));
                    addForm.setDescription((String) data.get("描述"));

                    // 默认父部门为根部门
                    addForm.setParentId(0L);
                    addForm.setStatus(1);

                    ResponseDTO<Long> result = addDepartment(addForm, userId);
                    if (result.getOk()) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.add("第" + (i + 1) + "行：" + result.getMsg());
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }

            Map<String, Object> importResult = new HashMap<>();
            importResult.put("successCount", successCount);
            importResult.put("failCount", failCount);
            importResult.put("errorMessages", errorMessages);

            if (failCount == 0) {
                return ResponseDTO.ok(importResult);
            } else {
                ResponseDTO<Map<String, Object>> errorResponse = new ResponseDTO<>();
                errorResponse.setOk(false);
                errorResponse.setMsg("导入部分失败");
                errorResponse.setData(importResult);
                return errorResponse;
            }

        } catch (Exception e) {
            log.error("导入部门数据失败", e);
            return ResponseDTO.error("导入部门数据失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> validateDepartmentData(Long departmentId) {
        log.info("验证部门数据完整性，departmentId：{}", departmentId);

        try {
            Map<String, Object> validationResult = new HashMap<>();
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            DepartmentEntity entity = departmentDao.selectById(departmentId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                errors.add("部门不存在或已删除");
                validationResult.put("isValid", false);
                validationResult.put("errors", errors);
                validationResult.put("warnings", warnings);
                return ResponseDTO.ok(validationResult);
            }

            // 验证编码格式
            if (!entity.getDepartmentCode().matches("^[A-Z0-9_]+$")) {
                errors.add("部门编码格式不正确，只能包含大写字母、数字和下划线");
            }

            // 验证层级是否合理
            if (entity.getLevel() > 10) {
                warnings.add("部门层级过深，可能影响性能");
            }

            // 验证父部门是否存在
            if (entity.getParentId() != null && entity.getParentId() > 0) {
                DepartmentEntity parentEntity = departmentDao.selectById(entity.getParentId());
                if (parentEntity == null || parentEntity.getDeletedFlag() == 1) {
                    errors.add("父部门不存在或已删除");
                }
            }

            // 验证路径是否正确
            if (!StringUtils.hasText(entity.getDepartmentPath())) {
                errors.add("部门路径为空");
            }

            boolean isValid = errors.isEmpty();
            validationResult.put("isValid", isValid);
            validationResult.put("errors", errors);
            validationResult.put("warnings", warnings);

            log.info("部门数据验证完成，departmentId：{}，结果：{}", departmentId, isValid);
            return ResponseDTO.ok(validationResult);

        } catch (Exception e) {
            log.error("验证部门数据完整性失败", e);
            return ResponseDTO.error("验证失败：" + e.getMessage());
        }
    }

    @Override
    public void syncDepartmentCache(Long departmentId) {
        log.info("同步部门缓存，departmentId：{}", departmentId);

        try {
            if (departmentId != null) {
                // 同步指定部门的缓存
                DepartmentEntity entity = departmentDao.selectById(departmentId);
                if (entity != null && entity.getDeletedFlag() == 0) {
                    DepartmentVO vo = convertToVO(entity);
                    String cacheKey = buildDepartmentCacheKey(departmentId);
                    cacheService.set(cacheKey, vo, 30, java.util.concurrent.TimeUnit.MINUTES);
                    log.debug("同步部门缓存成功: departmentId={}, cacheKey={}", departmentId, cacheKey);
                } else {
                    // 部门不存在或已删除，清除缓存
                    clearDepartmentCache(departmentId);
                }

                // 同步父部门的列表缓存（如果存在）
                if (entity != null && entity.getParentId() != null && entity.getParentId() > 0) {
                    String parentListKey = buildDepartmentListCacheKey(entity.getParentId());
                    cacheService.delete(parentListKey);
                    log.debug("清除父部门列表缓存: parentId={}, cacheKey={}", entity.getParentId(), parentListKey);
                }

                // 清除所有列表和树缓存，因为这些可能已过时
                cacheService.delete(CACHE_KEY_ALL_LIST, CACHE_KEY_TREE);
                log.debug("清除全局列表和树缓存");
            } else {
                // 同步所有部门的缓存（清除所有缓存，让下次查询时重新加载）
                log.info("同步所有部门缓存，清除所有相关缓存");
                cacheService.delete(CACHE_KEY_ALL_LIST, CACHE_KEY_TREE);
            }

            log.info("部门缓存同步完成，departmentId：{}", departmentId);
        } catch (Exception e) {
            log.error("同步部门缓存失败，departmentId：{}", departmentId, e);
            // 缓存同步失败不应该影响主业务流程，只记录日志
        }
    }

    @Override
    public void clearDepartmentCache(Long departmentId) {
        log.info("清除部门缓存，departmentId：{}", departmentId);

        try {
            if (departmentId != null) {
                // 清除指定部门的详情缓存
                String departmentCacheKey = buildDepartmentCacheKey(departmentId);
                cacheService.delete(departmentCacheKey);
                log.debug("清除部门详情缓存: cacheKey={}", departmentCacheKey);

                // 获取部门信息，清除相关列表缓存
                DepartmentEntity entity = departmentDao.selectById(departmentId);
                if (entity != null) {
                    // 清除父部门的列表缓存
                    if (entity.getParentId() != null && entity.getParentId() > 0) {
                        String parentListKey = buildDepartmentListCacheKey(entity.getParentId());
                        cacheService.delete(parentListKey);
                        log.debug("清除父部门列表缓存: parentId={}, cacheKey={}", entity.getParentId(), parentListKey);
                    }

                    // 清除所有子部门的缓存（如果部门路径存在）
                    if (entity.getDepartmentPath() != null && !entity.getDepartmentPath().isEmpty()) {
                        String pathPrefix = entity.getDepartmentPath() + "," + departmentId;
                        List<DepartmentEntity> children = departmentDao.selectByPathPrefix(pathPrefix);
                        for (DepartmentEntity child : children) {
                            String childCacheKey = buildDepartmentCacheKey(child.getDepartmentId());
                            cacheService.delete(childCacheKey);
                            log.debug("清除子部门缓存: childId={}, cacheKey={}", child.getDepartmentId(), childCacheKey);
                        }
                    }
                }
            } else {
                // 清除所有部门相关缓存（全量清除）
                log.info("清除所有部门缓存");
                // 注意：这里无法精确匹配所有部门缓存键，所以清除已知的全局缓存
                cacheService.delete(CACHE_KEY_ALL_LIST, CACHE_KEY_TREE);
                log.debug("已清除全局列表和树缓存");
            }

            log.info("部门缓存清除完成，departmentId：{}", departmentId);
        } catch (Exception e) {
            log.error("清除部门缓存失败，departmentId：{}", departmentId, e);
            // 缓存清除失败不应该影响主业务流程，只记录日志
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换实体为VO
     */
    private DepartmentVO convertToVO(DepartmentEntity entity) {
        DepartmentVO vo = SmartBeanUtil.copy(entity, DepartmentVO.class);

        // 设置状态名称
        if (entity.getStatus() != null) {
            vo.setStatusName(entity.getStatus() == 1 ? "启用" : "禁用");
        }

        // 设置父部门名称
        if (entity.getParentId() != null && entity.getParentId() > 0) {
            DepartmentEntity parentEntity = departmentDao.selectById(entity.getParentId());
            if (parentEntity != null) {
                vo.setParentName(parentEntity.getDepartmentName());
            }
        }

        // 解析扩展字段
        if (StringUtils.hasText(entity.getExtendInfo())) {
            try {
                vo.setExtendInfo(objectMapper.readValue(entity.getExtendInfo(), new TypeReference<Object>() {
                }));
            } catch (Exception e) {
                log.warn("解析扩展字段失败，departmentId：{}", entity.getDepartmentId(), e);
            }
        }

        // 设置是否有子部门
        vo.setHasChildren(hasChildren(entity.getDepartmentId()));

        // 设置员工数量
        try {
            long employeeCount = employeeManager.countEmployeesByDepartmentId(entity.getDepartmentId());
            vo.setEmployeeCount((int) employeeCount);
            log.debug("设置部门员工数量: departmentId={}, employeeCount={}", entity.getDepartmentId(), employeeCount);
        } catch (Exception e) {
            log.warn("获取部门员工数量失败，departmentId：{}，使用默认值0", entity.getDepartmentId(), e);
            vo.setEmployeeCount(0);
        }

        return vo;
    }

    /**
     * 构建部门树
     */
    private List<DepartmentVO> buildDepartmentTree(List<DepartmentVO> allDepartments, Long parentId) {
        List<DepartmentVO> result = new ArrayList<>();

        for (DepartmentVO department : allDepartments) {
            if (Objects.equals(department.getParentId(), parentId)) {
                List<DepartmentVO> children = buildDepartmentTree(allDepartments, department.getDepartmentId());
                department.setChildren(children);
                department.setHasChildren(!children.isEmpty());
                result.add(department);
            }
        }

        return result;
    }

    /**
     * 获取下一个排序号
     */
    private Integer getNextSortNumber(Long parentId) {
        try {
            List<DepartmentEntity> siblings = departmentDao.selectByParentId(parentId);
            if (siblings.isEmpty()) {
                return 1;
            }

            Integer maxSortNumber = siblings.stream()
                    .map(DepartmentEntity::getSortNumber)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);

            return maxSortNumber + 1;
        } catch (Exception e) {
            log.error("获取下一个排序号失败", e);
            return 1;
        }
    }

    /**
     * 检查是否为子部门
     */
    private boolean isChildDepartment(Long parentId, Long childId) {
        try {
            DepartmentEntity childEntity = departmentDao.selectById(childId);
            if (childEntity == null) {
                return false;
            }

            return childEntity.getDepartmentPath() != null &&
                    childEntity.getDepartmentPath().contains("," + parentId + ",");
        } catch (Exception e) {
            log.error("检查是否为子部门失败", e);
            return false;
        }
    }

    /**
     * 更新子部门信息
     */
    private void updateChildrenDepartmentInfo(Long parentId, String parentPath, Integer parentLevel) {
        try {
            List<DepartmentEntity> children = departmentDao.selectByParentId(parentId);

            for (DepartmentEntity child : children) {
                child.setDepartmentPath(parentPath + "," + parentId);
                child.setLevel(parentLevel + 1);
                departmentDao.updateById(child);

                // 递归更新子部门的子部门
                updateChildrenDepartmentInfo(child.getDepartmentId(), child.getDepartmentPath(), child.getLevel());
            }
        } catch (Exception e) {
            log.error("更新子部门信息失败", e);
        }
    }
}
