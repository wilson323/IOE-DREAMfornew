package net.lab1024.sa.biometric.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.biometric.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateAddForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateQueryForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateUpdateForm;
import net.lab1024.sa.biometric.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.biometric.manager.BiometricTemplateManager;
import net.lab1024.sa.biometric.service.BiometricTemplateService;
import net.lab1024.sa.biometric.service.BiometricTemplateSyncService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.UserEntity;
import org.springframework.beans.BeanUtils;

/**
 * 生物模板管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 * - 调用Manager层进行复杂业务编排
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class BiometricTemplateServiceImpl implements BiometricTemplateService {


    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private BiometricTemplateManager biometricTemplateManager;

    @Resource
    private BiometricTemplateSyncService biometricTemplateSyncService;

    @Resource
    private AreaUserDao areaUserDao;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 设备类型常量（门禁设备）
    private static final Integer DEVICE_TYPE_ACCESS = 1;

    @Override
    @CacheEvict(value = { "biometric:template:user", "biometric:template:user:type" }, key = "#addForm.userId")
    public ResponseDTO<BiometricTemplateVO> addTemplate(BiometricTemplateAddForm addForm) {
        try {
            log.info("[生物模板管理] 添加模板开始 userId={}, biometricType={}",
                    addForm.getUserId(), addForm.getBiometricType());

            // 1. 验证表单数据
            validateAddForm(addForm);

            // 2. 调用Manager层注册模板
            BiometricTemplateEntity template = biometricTemplateManager.registerTemplate(
                    addForm.getUserId(),
                    addForm.getBiometricType(),
                    addForm.getFeatureData(),
                    addForm.getQualityScore());

            // 3. 转换为VO
            BiometricTemplateVO templateVO = convertToVO(template);

            // 4. 异步同步模板到设备（根据用户权限智能同步）
            syncTemplateToUserDevicesAsync(addForm.getUserId());

            log.info("[生物模板管理] 添加模板成功 templateId={}", template.getTemplateId());
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 添加模板失败 userId={}, biometricType={}",
                    addForm.getUserId(), addForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_ADD_ERROR", "添加模板失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "biometric:template:detail", "biometric:template:user",
            "biometric:template:user:type" }, allEntries = true)
    public ResponseDTO<Void> updateTemplate(BiometricTemplateUpdateForm updateForm) {
        try {
            log.info("[生物模板管理] 更新模板开始 templateId={}", updateForm.getTemplateId());

            // 1. 查询模板
            BiometricTemplateEntity template = biometricTemplateDao.selectById(updateForm.getTemplateId());
            if (template == null) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 2. 更新字段
            if (updateForm.getTemplateName() != null) {
                template.setTemplateName(updateForm.getTemplateName());
            }
            if (updateForm.getTemplateStatus() != null) {
                template.setTemplateStatus(updateForm.getTemplateStatus());
            }
            if (updateForm.getMatchThreshold() != null) {
                template.setMatchThreshold(updateForm.getMatchThreshold());
            }

            // 3. 保存
            biometricTemplateDao.updateById(template);

            log.info("[生物模板管理] 更新模板成功 templateId={}", updateForm.getTemplateId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 更新模板失败 templateId={}", updateForm.getTemplateId(), e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "更新模板失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "biometric:template:detail", "biometric:template:user",
            "biometric:template:user:type" }, allEntries = true)
    public ResponseDTO<Void> deleteTemplate(Long templateId) {
        try {
            log.info("[生物模板管理] 删除模板开始 templateId={}", templateId);

            // 1. 查询模板
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 2. 调用Manager层删除模板（会从所有设备删除）
            biometricTemplateManager.deleteTemplate(template.getUserId(), template.getBiometricType());

            log.info("[生物模板管理] 删除模板成功 templateId={}", templateId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 删除模板失败 templateId={}", templateId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "biometric:template:user",
            "biometric:template:user:type" }, key = "#userId + ':' + #biometricType")
    public ResponseDTO<Void> deleteTemplateByUserAndType(Long userId, Integer biometricType) {
        try {
            log.info("[生物模板管理] 删除用户模板开始 userId={}, biometricType={}", userId, biometricType);

            // 调用Manager层删除模板
            biometricTemplateManager.deleteTemplate(userId, biometricType);

            log.info("[生物模板管理] 删除用户模板成功 userId={}, biometricType={}", userId, biometricType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 删除用户模板失败 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "biometric:template:detail", key = "#templateId", unless = "#result == null || !#result.isSuccess()")
    public ResponseDTO<BiometricTemplateVO> getTemplateById(Long templateId) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            BiometricTemplateVO templateVO = convertToVO(template);
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询模板失败 templateId={}", templateId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "biometric:template:user", key = "#userId", unless = "#result == null || !#result.isSuccess()")
    public ResponseDTO<List<BiometricTemplateVO>> getTemplatesByUserId(Long userId) {
        try {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);
            List<BiometricTemplateVO> templateVOs = templates.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return ResponseDTO.ok(templateVOs);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询用户模板失败 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "biometric:template:user:type", key = "#userId + ':' + #biometricType", unless = "#result == null || !#result.isSuccess()")
    public ResponseDTO<BiometricTemplateVO> getTemplateByUserAndType(Long userId, Integer biometricType) {
        try {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserIdAndType(userId, biometricType);
            if (templates.isEmpty()) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            BiometricTemplateVO templateVO = convertToVO(templates.get(0));
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询用户模板失败 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<BiometricTemplateVO>> pageTemplate(BiometricTemplateQueryForm queryForm) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<BiometricTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(queryForm.getUserId() != null, BiometricTemplateEntity::getUserId, queryForm.getUserId())
                    .eq(queryForm.getBiometricType() != null, BiometricTemplateEntity::getBiometricType,
                            queryForm.getBiometricType())
                    .eq(queryForm.getTemplateStatus() != null, BiometricTemplateEntity::getTemplateStatus,
                            queryForm.getTemplateStatus())
                    .eq(queryForm.getDeviceId() != null, BiometricTemplateEntity::getDeviceId, queryForm.getDeviceId())
                    .like(queryForm.getTemplateName() != null, BiometricTemplateEntity::getTemplateName,
                            queryForm.getTemplateName())
                    .orderByDesc(BiometricTemplateEntity::getCreateTime);

            // 分页查询
            Page<BiometricTemplateEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<BiometricTemplateEntity> pageResult = biometricTemplateDao.selectPage(page, queryWrapper);

            // 转换为VO
            List<BiometricTemplateVO> templateVOs = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<BiometricTemplateVO> result = PageResult.of(
                    templateVOs,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[生物模板管理] 分页查询模板失败", e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = { "biometric:template:detail", "biometric:template:user",
            "biometric:template:user:type" }, allEntries = true)
    public ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer templateStatus) {
        try {
            int rows = biometricTemplateDao.updateTemplateStatus(templateId, templateStatus);
            if (rows == 0) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 更新模板状态失败 templateId={}, status={}", templateId, templateStatus, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "更新模板状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> batchUpdateTemplateStatus(List<Long> templateIds, Integer templateStatus) {
        try {
            for (Long templateId : templateIds) {
                biometricTemplateDao.updateTemplateStatus(templateId, templateStatus);
            }
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 批量更新模板状态失败 templateIds={}, status={}", templateIds, templateStatus, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "批量更新模板状态失败: " + e.getMessage());
        }
    }

    /**
     * 验证添加表单
     */
    private void validateAddForm(BiometricTemplateAddForm addForm) {
        if (addForm.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "用户ID不能为空");
        }
        if (addForm.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "生物识别类型不能为空");
        }
        if (addForm.getFeatureData() == null || addForm.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "特征数据不能为空");
        }
    }

    /**
     * 转换为VO
     */
    private BiometricTemplateVO convertToVO(BiometricTemplateEntity template) {
        BiometricTemplateVO vo = new BiometricTemplateVO();
        BeanUtils.copyProperties(template, vo);

        // 设置描述字段
        if (template.getBiometricType() != null) {
            BiometricType type = BiometricType.fromCode(template.getBiometricType());
            vo.setBiometricTypeDesc(type.getName());
        }
        if (template.getTemplateStatus() != null) {
            BiometricTemplateEntity.TemplateStatus status = BiometricTemplateEntity.TemplateStatus
                    .fromCode(template.getTemplateStatus());
            vo.setTemplateStatusDesc(status.getDescription());
        }

        // 获取用户姓名
        if (template.getUserId() != null) {
            String userName = getUserName(template.getUserId());
            vo.setUserName(userName);
        }

        // 计算成功率
        if (template.getUseCount() != null && template.getUseCount() > 0) {
            double successRate = (double) template.getSuccessCount() / template.getUseCount() * 100;
            vo.setSuccessRate(String.format("%.2f%%", successRate));
        }

        return vo;
    }

    /**
     * 获取用户姓名
     */
    private String getUserName(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<UserEntity>>() {}
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                UserEntity user = response.getData();
                // 优先使用realName，如果为空则使用username
                return user.getRealName() != null && !user.getRealName().isEmpty()
                        ? user.getRealName()
                        : user.getUsername();
            }

            return "用户" + userId;
        } catch (Exception e) {
            log.warn("[生物模板管理] 获取用户信息失败 userId={}, error={}", userId, e.getMessage());
            return "用户" + userId;
        }
    }

    /**
     * 异步同步模板到用户有权限的设备
     * <p>
     * 场景：用户新增模板时，自动同步到有权限的设备
     * </p>
     */
    @Async("templateSyncExecutor")
    private void syncTemplateToUserDevicesAsync(Long userId) {
        try {
            log.info("[生物模板管理] 开始智能同步模板到设备 userId={}", userId);

            // 1. 查询用户有权限的区域
            List<AreaUserEntity> permissions = areaUserDao.selectByUserId(userId);
            if (permissions.isEmpty()) {
                log.warn("[生物模板管理] 用户无任何区域权限，无需同步模板 userId={}", userId);
                return;
            }

            // 2. 查询这些区域的所有门禁设备
            Set<String> targetDeviceIds = new HashSet<>();
            for (AreaUserEntity permission : permissions) {
                List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(
                        permission.getAreaId(),
                        DEVICE_TYPE_ACCESS);
                devices.forEach(d -> targetDeviceIds.add(d.getDeviceId()));
            }

            if (targetDeviceIds.isEmpty()) {
                log.warn("[生物模板管理] 用户权限区域无门禁设备，无需同步模板 userId={}", userId);
                return;
            }

            log.info("[生物模板管理] 开始同步模板到设备 userId={}, targetDeviceCount={}",
                    userId, targetDeviceIds.size());

            // 3. 同步模板到所有目标设备
            biometricTemplateSyncService.syncTemplateToDevices(userId, new ArrayList<>(targetDeviceIds));

            log.info("[生物模板管理] 智能同步模板完成 userId={}, deviceCount={}", userId, targetDeviceIds.size());

        } catch (Exception e) {
            log.error("[生物模板管理] 智能同步模板失败 userId={}", userId, e);
            // 不抛出异常，避免影响主流程
        }
    }
}
