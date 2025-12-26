package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessEvacuationPointDao;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointAddForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointQueryForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessEvacuationPointVO;
import net.lab1024.sa.access.domain.entity.AccessEvacuationPointEntity;
import net.lab1024.sa.access.service.AccessEvacuationService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 门禁疏散点管理Service实现
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Slf4j
@Service
public class AccessEvacuationServiceImpl implements AccessEvacuationService {

    @Resource
    private AccessEvacuationPointDao evacuationPointDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    public PageResult<AccessEvacuationPointVO> queryPage(AccessEvacuationPointQueryForm queryForm) {
        log.info("[疏散管理] 分页查询疏散点: {}", queryForm);

        Page<AccessEvacuationPointEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        LambdaQueryWrapper<AccessEvacuationPointEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 疏散点名称模糊查询
        if (queryForm.getPointName() != null && !queryForm.getPointName().trim().isEmpty()) {
            queryWrapper.like(AccessEvacuationPointEntity::getPointName, queryForm.getPointName());
        }

        // 区域ID过滤
        if (queryForm.getAreaId() != null) {
            queryWrapper.eq(AccessEvacuationPointEntity::getAreaId, queryForm.getAreaId());
        }

        // 疏散类型过滤
        if (queryForm.getEvacuationType() != null && !queryForm.getEvacuationType().trim().isEmpty()) {
            queryWrapper.eq(AccessEvacuationPointEntity::getEvacuationType, queryForm.getEvacuationType());
        }

        // 启用状态过滤
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(AccessEvacuationPointEntity::getEnabled, queryForm.getEnabled());
        }

        // 按优先级降序排序
        queryWrapper.orderByDesc(AccessEvacuationPointEntity::getPriority)
                .orderByDesc(AccessEvacuationPointEntity::getCreateTime);

        IPage<AccessEvacuationPointEntity> pageResult = evacuationPointDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<AccessEvacuationPointVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("[疏散管理] 查询到{}条疏散点记录", pageResult.getTotal());
        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public AccessEvacuationPointVO getById(Long pointId) {
        log.info("[疏散管理] 查询疏散点详情: pointId={}", pointId);
        AccessEvacuationPointEntity entity = evacuationPointDao.selectById(pointId);
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPoint(AccessEvacuationPointAddForm addForm) {
        log.info("[疏散管理] 新增疏散点: pointName={}", addForm.getPointName());

        AccessEvacuationPointEntity entity = new AccessEvacuationPointEntity();
        entity.setPointName(addForm.getPointName());
        entity.setAreaId(addForm.getAreaId());
        entity.setEvacuationType(addForm.getEvacuationType());
        entity.setDoorIds(addForm.getDoorIds() != null ? addForm.getDoorIds().toString() : "[]");
        entity.setPriority(addForm.getPriority());
        entity.setEnabled(addForm.getEnabled() != null ? addForm.getEnabled() : 1);
        entity.setDescription(addForm.getDescription());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        evacuationPointDao.insert(entity);
        log.info("[疏散管理] 新增疏散点成功: pointId={}", entity.getPointId());
        return entity.getPointId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePoint(Long pointId, AccessEvacuationPointUpdateForm updateForm) {
        log.info("[疏散管理] 更新疏散点: pointId={}", pointId);

        AccessEvacuationPointEntity entity = evacuationPointDao.selectById(pointId);
        if (entity == null) {
            log.warn("[疏散管理] 疏散点不存在: pointId={}", pointId);
            return false;
        }

        // 更新字段
        if (updateForm.getPointName() != null) {
            entity.setPointName(updateForm.getPointName());
        }
        if (updateForm.getAreaId() != null) {
            entity.setAreaId(updateForm.getAreaId());
        }
        if (updateForm.getEvacuationType() != null) {
            entity.setEvacuationType(updateForm.getEvacuationType());
        }
        if (updateForm.getDoorIds() != null) {
            entity.setDoorIds(updateForm.getDoorIds().toString());
        }
        if (updateForm.getPriority() != null) {
            entity.setPriority(updateForm.getPriority());
        }
        if (updateForm.getDescription() != null) {
            entity.setDescription(updateForm.getDescription());
        }
        entity.setUpdateTime(LocalDateTime.now());

        int result = evacuationPointDao.updateById(entity);
        log.info("[疏散管理] 更新疏散点成功: pointId={}, result={}", pointId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletePoint(Long pointId) {
        log.info("[疏散管理] 删除疏散点: pointId={}", pointId);
        int result = evacuationPointDao.deleteById(pointId);
        log.info("[疏散管理] 删除疏散点成功: pointId={}, result={}", pointId, result);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateEnabled(Long pointId, Integer enabled) {
        log.info("[疏散管理] 更新疏散点启用状态: pointId={}, enabled={}", pointId, enabled);

        AccessEvacuationPointEntity entity = evacuationPointDao.selectById(pointId);
        if (entity == null) {
            log.warn("[疏散管理] 疏散点不存在: pointId={}", pointId);
            return false;
        }

        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        int result = evacuationPointDao.updateById(entity);
        log.info("[疏散管理] 更新启用状态成功: pointId={}, enabled={}, result={}", pointId, enabled, result);
        return result > 0;
    }

    @Override
    public String triggerEvacuation(Long pointId) {
        log.info("[疏散管理] 触发一键疏散: pointId={}", pointId);

        AccessEvacuationPointEntity point = evacuationPointDao.selectById(pointId);
        if (point == null) {
            log.warn("[疏散管理] 疏散点不存在: pointId={}", pointId);
            return "疏散点不存在";
        }

        if (point.getEnabled() != 1) {
            log.warn("[疏散管理] 疏散点未启用: pointId={}", pointId);
            return "疏散点未启用";
        }

        // 解析关联门ID列表
        List<Long> doorIds = parseDoorIds(point.getDoorIds());
        if (doorIds.isEmpty()) {
            log.warn("[疏散管理] 疏散点未配置关联门: pointId={}", pointId);
            return "疏散点未配置关联门";
        }

        // 发送开门指令到所有关联门
        int successCount = 0;
        for (Long doorId : doorIds) {
            try {
                Map<String, Object> controlCommand = new HashMap<>();
                controlCommand.put("doorId", doorId);
                controlCommand.put("action", "OPEN");
                controlCommand.put("evacuationMode", true);
                controlCommand.put("evacuationType", point.getEvacuationType());

                String response = gatewayServiceClient.callDeviceCommService(
                        "/api/v1/device/comm/control",
                        HttpMethod.POST,
                        controlCommand,
                        String.class
                );

                if (response != null && response.contains("success")) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[疏散管理] 发送开门指令失败: doorId={}, error={}", doorId, e.getMessage());
            }
        }

        String result = String.format("一键疏散已触发，成功打开%d/%d个门", successCount, doorIds.size());
        log.info("[疏散管理] 一键疏散完成: pointId={}, result={}", pointId, result);
        return result;
    }

    @Override
    public String cancelEvac(Long pointId) {
        log.info("[疏散管理] 取消疏散: pointId={}", pointId);

        AccessEvacuationPointEntity point = evacuationPointDao.selectById(pointId);
        if (point == null) {
            log.warn("[疏散管理] 疏散点不存在: pointId={}", pointId);
            return "疏散点不存在";
        }

        // 解析关联门ID列表
        List<Long> doorIds = parseDoorIds(point.getDoorIds());
        if (doorIds.isEmpty()) {
            return "疏散点未配置关联门";
        }

        // 发送关闭门指令到所有关联门
        int successCount = 0;
        for (Long doorId : doorIds) {
            try {
                Map<String, Object> controlCommand = new HashMap<>();
                controlCommand.put("doorId", doorId);
                controlCommand.put("action", "CLOSE");
                controlCommand.put("evacuationMode", false);

                String response = gatewayServiceClient.callDeviceCommService(
                        "/api/v1/device/comm/control",
                        HttpMethod.POST,
                        controlCommand,
                        String.class
                );

                if (response != null && response.contains("success")) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[疏散管理] 发送关门指令失败: doorId={}, error={}", doorId, e.getMessage());
            }
        }

        String result = String.format("疏散已取消，成功关闭%d/%d个门", successCount, doorIds.size());
        log.info("[疏散管理] 取消疏散完成: pointId={}, result={}", pointId, result);
        return result;
    }

    @Override
    public String testPoint(Long pointId) {
        log.info("[疏散管理] 测试疏散点: pointId={}", pointId);

        AccessEvacuationPointEntity point = evacuationPointDao.selectById(pointId);
        if (point == null) {
            log.warn("[疏散管理] 疏散点不存在: pointId={}", pointId);
            return "疏散点不存在";
        }

        // 解析关联门ID列表
        List<Long> doorIds = parseDoorIds(point.getDoorIds());
        if (doorIds.isEmpty()) {
            return "疏散点未配置关联门";
        }

        // 模拟测试（不实际开门）
        String evacuationTypeDesc = getEvacuationTypeDesc(point.getEvacuationType());
        String result = String.format("测试成功 - 疏散点: %s, 类型: %s, 关联门: %d个",
                point.getPointName(), evacuationTypeDesc, doorIds.size());
        log.info("[疏散管理] 测试疏散点成功: pointId={}, result={}", pointId, result);
        return result;
    }

    /**
     * 转换为VO
     */
    private AccessEvacuationPointVO convertToVO(AccessEvacuationPointEntity entity) {
        AccessEvacuationPointVO vo = new AccessEvacuationPointVO();
        vo.setPointId(entity.getPointId());
        vo.setPointName(entity.getPointName());
        vo.setAreaId(entity.getAreaId());
        vo.setEvacuationType(entity.getEvacuationType());
        vo.setDoorIds(entity.getDoorIds());
        vo.setPriority(entity.getPriority());
        vo.setEnabled(entity.getEnabled());
        vo.setRemarks(entity.getDescription());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 解析门ID列表
     */
    private List<Long> parseDoorIds(String doorIdsStr) {
        try {
            if (doorIdsStr == null || doorIdsStr.trim().isEmpty() || "[]".equals(doorIdsStr)) {
                return List.of();
            }
            // 简化的JSON解析（实际应使用ObjectMapper）
            String[] parts = doorIdsStr.replaceAll("[\\[\\]\"]", "").split(",");
            return Arrays.stream(parts)
                    .filter(s -> !s.trim().isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[疏散管理] 解析门ID失败: doorIdsStr={}, error={}", doorIdsStr, e.getMessage());
            return List.of();
        }
    }

    /**
     * 获取疏散类型描述
     */
    private String getEvacuationTypeDesc(String evacuationType) {
        return switch (evacuationType) {
            case "FIRE" -> "火灾疏散";
            case "EARTHQUAKE" -> "地震疏散";
            case "EMERGENCY" -> "紧急疏散";
            default -> evacuationType;
        };
    }
}
