package {package}.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import {package}.dao.{BaseEntityName}{ModuleName}ExtDao;
import {package}.dao.{BaseEntityName}Dao;
import {package}.domain.entity.{BaseEntityName}{ModuleName}ExtEntity;
import {package}.domain.entity.{BaseEntityName}Entity;
import {package}.domain.form.{BaseEntityName}{ModuleName}QueryForm;
import {package}.domain.vo.{BaseEntityName}{ModuleName}VO;
import {package}.manager.{BaseEntityName}{ModuleName}CacheManager;

/**
 * {base_entity_comment}{module_name}扩展服务
 * <p>
 * 基于现有成功Service模式，提供完整的扩展表业务逻辑
 * 包含缓存管理、事务处理、批量操作等功能
 * 重点避免代码冗余，提高开发效率
 *
 * @author {author}
 * @since {create_date}
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class {BaseEntityName}{ModuleName}ExtService {

    // ==================== 依赖注入 ====================

    private final {BaseEntityName}{ModuleName}ExtDao {baseEntityName}{ModuleName}ExtDao;
    private final {BaseEntityName}Dao {baseEntityName}Dao;
    private final {BaseEntityName}{ModuleName}CacheManager {baseEntityName}{ModuleName}CacheManager;

    /**
     * 构造函数注入（推荐方式）
     */
    public {BaseEntityName}{ModuleName}ExtService(
            {BaseEntityName}{ModuleName}ExtDao {baseEntityName}{ModuleName}ExtDao,
            {BaseEntityName}Dao {baseEntityName}Dao,
            {BaseEntityName}{ModuleName}CacheManager {baseEntityName}{ModuleName}CacheManager) {
        this.{baseEntityName}{ModuleName}ExtDao = {baseEntityName}{ModuleName}ExtDao;
        this.{baseEntityName}Dao = {baseEntityName}Dao;
        this.{baseEntityName}{ModuleName}CacheManager = {baseEntityName}{ModuleName}CacheManager;
    }

    // ==================== 基础CRUD操作 ====================

    /**
     * 获取{base_entity_comment}的{module_name}扩展信息
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @return {module_name}扩展信息
     */
    public ResponseDTO<{BaseEntityName}{ModuleName}VO> get{BaseEntityName}{ModuleName}Info(Long {baseTableId}) {
        try {
            // 缓存检查（基于现有缓存模式）
            {BaseEntityName}{ModuleName}VO cachedResult = {baseEntityName}{ModuleName}CacheManager.getInfo({baseTableId});
            if (cachedResult != null) {
                return ResponseDTO.ok(cachedResult);
            }

            // 数据库查询
            {BaseEntityName}Entity baseEntity = {baseEntityName}Dao.selectById({baseTableId});
            if (baseEntity == null) {
                return ResponseDTO.error("Data", "{base_entity_comment}不存在");
            }

            {BaseEntityName}{ModuleName}ExtEntity extEntity = {baseEntityName}{ModuleName}ExtDao.selectBy{BaseEntityName}Id({baseTableId});

            // 组装结果（基于现有组装模式）
            {BaseEntityName}{ModuleName}VO result = build{BaseEntityName}{ModuleName}VO(baseEntity, extEntity);

            // 缓存结果
            {baseEntityName}{ModuleName}CacheManager.setInfo({baseTableId}, result);

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取{base_entity_comment}{module_name}扩展信息失败，{baseTableId}: {}", {baseTableId}, e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    /**
     * 分页查询{module_name}扩展信息
     *
     * @param queryForm 查询条件
     * @return 分页查询结果
     */
    public ResponseDTO<Page<{BaseEntityName}{ModuleName}VO>> query{BaseEntityName}{ModuleName}Page({BaseEntityName}{ModuleName}QueryForm queryForm) {
        try {
            // 构建分页查询
            Page<{BaseEntityName}{ModuleName}VO> page = SmartPageUtil.convert2PageQuery(queryForm);

            // 执行查询
            Page<{BaseEntityName}{ModuleName}VO> result = {baseEntityName}{ModuleName}ExtDao.select{BaseEntityName}{ModuleName}Page(
                    page, queryForm.get{BaseEntityName}Name(), queryForm.get{ModuleName}Level());

            // 处理查询结果
            if (result != null && CollectionUtils.isNotEmpty(result.getRecords())) {
                // 设置关联信息
                enrich{BaseEntityName}{ModuleName}VOList(result.getRecords());
            }

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询{module_name}扩展信息失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    /**
     * 查询{module_name}扩展列表
     *
     * @param status 基础状态（可选）
     * @param {module}Level {module_name}等级（可选）
     * @return 扩展信息列表
     */
    public ResponseDTO<List<{BaseEntityName}{ModuleName}VO>> query{BaseEntityName}{ModuleName}List(
            Integer status, Integer {module}Level) {
        try {
            List<{BaseEntityName}{ModuleName}VO> voList = {baseEntityName}{ModuleName}ExtDao.select{BaseEntityName}{ModuleName}List(
                    status, {module}Level);

            if (CollectionUtils.isNotEmpty(voList)) {
                enrich{BaseEntityName}{ModuleName}VOList(voList);
            }

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("查询{module_name}扩展列表失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    // ==================== 扩展信息管理 ====================

    /**
     * 创建或更新{module_name}扩展信息
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @param updateForm 更新表单
     * @return 操作结果
     */
    public ResponseDTO<Boolean> save{BaseEntityName}{ModuleName}Extension(
            Long {baseTableId}, {BaseEntityName}{ModuleName}UpdateForm updateForm) {
        try {
            // 参数验证
            if ({baseTableId} == null) {
                return ResponseDTO.error("ParamError", "{base_entity_comment}ID不能为空");
            }

            // 验证基础{base_entity_comment}存在性
            {BaseEntityName}Entity baseEntity = {baseEntityName}Dao.selectById({baseTableId});
            if (baseEntity == null) {
                return ResponseDTO.error("Data", "{base_entity_comment}不存在");
            }

            // 设置默认值（避免配置冗余）
            setDefaultValues(updateForm);

            // 保存扩展信息
            Long currentTime = System.currentTimeMillis();
            int result = {baseEntityName}{ModuleName}ExtDao.insertOrUpdate{ModuleName}Extension(
                    {baseTableId},
                    updateForm.get{ModuleName}Level(),
                    updateForm.get{ModuleName}Mode(),
                    updateForm.getSpecialRequired(),
                    updateForm.getPriority(),
                    updateForm.get{ModuleName}Status(),
                    currentTime,
                    LoginContext.getUserId()
            );

            if (result <= 0) {
                return ResponseDTO.error("System", "保存失败");
            }

            // 清理缓存（基于现有缓存失效策略）
            {baseEntityName}{ModuleName}CacheManager.evict({baseTableId});

            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("保存{module_name}扩展信息失败，{baseTableId}: {}", {baseTableId}, e);
            throw new SmartException("保存失败");
        }
    }

    /**
     * 批量更新{module_name}扩展信息（基于现有批量操作实践）
     *
     * @param updateList 更新列表
     * @return 操作结果
     */
    public ResponseDTO<Boolean> batchUpdate{ModuleName}Extension(
            List<{BaseEntityName}{ModuleName}UpdateForm> updateList) {
        try {
            if (CollectionUtils.isEmpty(updateList)) {
                return ResponseDTO.error("ParamError", "参数不能为空");
            }

            // 验证基础{base_entity_comment}存在性
            List<Long> {baseTableId}s = updateList.stream()
                    .map({BaseEntityName}{ModuleName}UpdateForm::get{BaseEntityName}Id)
                    .collect(Collectors.toList());

            int count = {baseEntityName}Dao.selectBatchIds({baseTableId}s).size();
            if (count != {baseTableId}s.size()) {
                return ResponseDTO.error("Data", "部分{base_entity_comment}不存在");
            }

            // 批量处理（基于现有批量事务模式）
            for ({BaseEntityName}{ModuleName}UpdateForm updateForm : updateList) {
                save{BaseEntityName}{ModuleName}Extension(updateForm.get{BaseEntityName}Id(), updateForm);
            }

            // 清理缓存
            {baseEntityName}{ModuleName}CacheManager.batchEvict({baseTableId}s);

            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("批量更新{module_name}扩展信息失败", e);
            throw new SmartException("批量更新失败");
        }
    }

    /**
     * 删除{module_name}扩展信息
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @return 操作结果
     */
    public ResponseDTO<Boolean> delete{ModuleName}Extension(Long {baseTableId}) {
        try {
            if ({baseTableId} == null) {
                return ResponseDTO.error("ParamError", "{base_entity_comment}ID不能为空");
            }

            int result = {baseEntityName}{ModuleName}ExtDao.deleteBy{BaseEntityName}Id({baseTableId}, LoginContext.getUserId());
            if (result <= 0) {
                return ResponseDTO.error("System", "删除失败");
            }

            // 清理缓存
            {baseEntityName}{ModuleName}CacheManager.evict({baseTableId});

            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("删除{module_name}扩展信息失败，{baseTableId}: {}", {baseTableId}, e);
            throw new SmartException("删除失败");
        }
    }

    // ==================== 统计分析功能 ====================

    /**
     * 获取{module_name}扩展统计信息
     *
     * @return 统计信息
     */
    public ResponseDTO<{BaseEntityName}{ModuleName}StatisticsVO> get{ModuleName}Statistics() {
        try {
            {BaseEntityName}{ModuleName}StatisticsVO statistics = new {BaseEntityName}{ModuleName}StatisticsVO();

            // 基础统计
            statistics.setTotalCount({baseEntityName}{ModuleName}ExtDao.countTotal());
            statistics.setSpecialRequiredCount({baseEntityName}{ModuleName}ExtDao.countSpecialRequired());

            // 等级分布统计
            List<DictEntity> levelStats = {baseEntityName}{ModuleName}ExtDao.countBy{ModuleName}Level();
            statistics.setLevelDistribution(convertDictToMap(levelStats));

            // 状态分布统计
            List<DictEntity> statusStats = {baseEntityName}{ModuleName}ExtDao.countBy{ModuleName}Status();
            statistics.setStatusDistribution(convertDictToMap(statusStats));

            // 配置复杂度统计
            List<DictEntity> complexityStats = {baseEntityName}{ModuleName}ExtDao.countByConfigComplexity();
            statistics.setComplexityDistribution(convertDictToMap(complexityStats));

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取{module_name}扩展统计信息失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    /**
     * 获取优先级最高的扩展信息
     *
     * @param limit 限制数量
     * @return 高优先级扩展列表
     */
    public ResponseDTO<List<{BaseEntityName}{ModuleName}VO>> getTopPriorityExtensions(Integer limit) {
        try {
            List<{BaseEntityName}{ModuleName}ExtEntity> extEntities =
                    {baseEntityName}{ModuleName}ExtDao.selectTopByPriority(limit);

            if (CollectionUtils.isEmpty(extEntities)) {
                return ResponseDTO.ok(java.util.Collections.emptyList());
            }

            // 构建VO列表
            List<Long> {baseTableId}s = extEntities.stream()
                    .map({BaseEntityName}{ModuleName}ExtEntity::get{BaseTableId})
                    .collect(Collectors.toList());

            Map<Long, {BaseEntityName}Entity> baseEntityMap = {baseEntityName}Dao.selectBatchIds({baseTableId}s)
                    .stream()
                    .collect(Collectors.toMap({BaseEntityName}Entity::get{BaseEntityName}Id, entity -> entity));

            List<{BaseEntityName}{ModuleName}VO> voList = extEntities.stream()
                    .map(extEntity -> build{BaseEntityName}{ModuleName}VO(
                            baseEntityMap.get(extEntity.get{BaseTableId}()), extEntity))
                    .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("获取高优先级扩展信息失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    // ==================== 维护管理功能 ====================

    /**
     * 清理孤立的扩展记录
     *
     * @return 清理的记录数
     */
    public ResponseDTO<Integer> cleanOrphanExtensions() {
        try {
            int cleanedCount = {baseEntityName}{ModuleName}ExtDao.cleanOrphanExtensions();

            // 清理所有缓存（基于现有缓存清理策略）
            {baseEntityName}{ModuleName}CacheManager.clearAll();

            log.info("清理孤立扩展记录完成，清理数量: {}", cleanedCount);
            return ResponseDTO.ok(cleanedCount);
        } catch (Exception e) {
            log.error("清理孤立扩展记录失败", e);
            return ResponseDTO.error("System", "系统异常");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建{BaseEntityName}{ModuleName}VO（基于现有组装模式增强）
     *
     * @param baseEntity 基础实体
     * @param extEntity 扩展实体
     * @return 组装后的VO
     */
    private {BaseEntityName}{ModuleName}VO build{BaseEntityName}{ModuleName}VO(
            {BaseEntityName}Entity baseEntity, {BaseEntityName}{ModuleName}ExtEntity extEntity) {
        {BaseEntityName}{ModuleName}VO vo = new {BaseEntityName}{ModuleName}VO();

        // 复制基础信息
        if (baseEntity != null) {
            SmartBeanUtil.copy(baseEntity, vo);
        }

        // 复制扩展信息
        if (extEntity != null) {
            SmartBeanUtil.copy(extEntity, vo);

            // 处理JSON配置字段
            vo.setTimeRestrictions(parseJsonConfig(extEntity.getTimeRestrictions()));
            vo.setLocationRules(parseJsonConfig(extEntity.getLocationRules()));
            vo.setAlertConfig(parseJsonConfig(extEntity.getAlertConfig()));
            vo.setDeviceLinkageRules(parseJsonConfig(extEntity.getDeviceLinkageRules()));
            vo.setExtensionConfig(parseJsonConfig(extEntity.getExtensionConfig()));

            // 计算配置复杂度
            vo.setConfigComplexity(extEntity.calculateConfigComplexity());
        }

        return vo;
    }

    /**
     * 丰富{BaseEntityName}{ModuleName}VO列表信息
     *
     * @param voList VO列表
     */
    private void enrich{BaseEntityName}{ModuleName}VOList(List<{BaseEntityName}{ModuleName}VO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return;
        }

        // 批量设置计算字段
        for ({BaseEntityName}{ModuleName}VO vo : voList) {
            // 设置是否有特殊配置
            vo.setHasSpecialConfig(hasAnyConfig(vo));

            // 设置类型名称等关联信息（如果需要）
            if (StringUtils.isNotBlank(vo.get{EntityName}Code())) {
                // 可以在这里添加字典查询或其他关联查询
            }
        }
    }

    /**
     * 检查是否有任何配置
     *
     * @param vo VO对象
     * @return true 如果有任何配置
     */
    private boolean hasAnyConfig({BaseEntityName}{ModuleName}VO vo) {
        return vo.getTimeRestrictions() != null && !vo.getTimeRestrictions().isEmpty() ||
               vo.getLocationRules() != null && !vo.getLocationRules().isEmpty() ||
               vo.getAlertConfig() != null && !vo.getAlertConfig().isEmpty() ||
               vo.getDeviceLinkageRules() != null && !vo.getDeviceLinkageRules().isEmpty();
    }

    /**
     * 解析JSON配置（基于现有JSON处理模式）
     *
     * @param jsonConfig JSON配置字符串
     * @return 解析后的Map
     */
    private Map<String, Object> parseJsonConfig(String jsonConfig) {
        if (StringUtils.isBlank(jsonConfig)) {
            return new java.util.HashMap<>();
        }
        try {
            return com.alibaba.fastjson.JSON.parseObject(jsonConfig, Map.class);
        } catch (Exception e) {
            log.warn("解析{module_name}配置失败: {}", jsonConfig, e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 转换字典列表为Map（基于现有转换模式）
     *
     * @param dictList 字典列表
     * @return Map
     */
    private Map<String, Object> convertDictToMap(List<DictEntity> dictList) {
        if (CollectionUtils.isEmpty(dictList)) {
            return new java.util.HashMap<>();
        }
        return dictList.stream()
                .collect(Collectors.toMap(
                        DictEntity::getCode,
                        dict -> Integer.parseInt(dict.getValue())
                ));
    }

    /**
     * 设置默认值（避免配置冗余）
     *
     * @param updateForm 更新表单
     */
    private void setDefaultValues({BaseEntityName}{ModuleName}UpdateForm updateForm) {
        if (updateForm.get{ModuleName}Level() == null) {
            updateForm.set{ModuleName}Level(1);
        }
        if (updateForm.getSpecialRequired() == null) {
            updateForm.setSpecialRequired(false);
        }
        if (updateForm.getPriority() == null) {
            updateForm.setPriority(1);
        }
        if (updateForm.get{ModuleName}Status() == null) {
            updateForm.set{ModuleName}Status(1); // 默认启用
        }
    }
}