package net.lab1024.sa.common.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionVO;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.service.AreaPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 区域权限管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AreaPermissionServiceImpl implements AreaPermissionService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AreaUserDao areaUserDao;

    @Override
    public ResponseDTO<List<AreaPermissionVO>> listByArea(Long areaId, String type) {
        log.info("[区域权限服务] 查询区域权限列表: areaId={}, type={}", areaId, type);

        String normalizedType = type == null ? "user" : type.trim().toLowerCase(Locale.ROOT);
        if (!"user".equals(normalizedType)) {
            return ResponseDTO.ok(Collections.emptyList());
        }

        LambdaQueryWrapper<AreaUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AreaUserEntity::getAreaId, areaId)
                .eq(AreaUserEntity::getDeletedFlag, 0)
                .orderByDesc(AreaUserEntity::getCreateTime);

        List<AreaUserEntity> list = areaUserDao.selectList(wrapper);
        List<AreaPermissionVO> result = list.stream().map(this::toUserPermissionVO).toList();

        log.info("[区域权限服务] 查询完成: areaId={}, count={}", areaId, result.size());
        return ResponseDTO.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> delete(String permissionId) {
        log.info("[区域权限服务] 删除权限: permissionId={}", permissionId);
        areaUserDao.deleteById(permissionId);
        log.info("[区域权限服务] 删除成功: permissionId={}", permissionId);
        return ResponseDTO.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchDelete(List<String> permissionIds) {
        log.info("[区域权限服务] 批量删除权限: count={}", permissionIds.size());
        // MyBatis-Plus 3.5.x标记为过时，推荐使用deleteByIds，但当前版本兼容
        @SuppressWarnings("deprecation")
        int deleted = areaUserDao.deleteBatchIds(permissionIds);
        log.info("[区域权限服务] 批量删除完成: count={}, deleted={}", permissionIds.size(), deleted);
        return ResponseDTO.ok();
    }

    /**
     * 转换为权限VO
     *
     * @param entity 区域用户实体
     * @return 权限VO
     */
    private AreaPermissionVO toUserPermissionVO(AreaUserEntity entity) {
        AreaPermissionVO vo = new AreaPermissionVO();
        vo.setPermissionId(entity.getId());
        vo.setUserName(entity.getRealName());
        vo.setUserCode(entity.getUsername());
        vo.setPermissionLevel(mapPermissionLevel(entity.getPermissionLevel()));
        vo.setStatus(entity.getDeviceSyncStatus() != null && entity.getDeviceSyncStatus() == 2 ? 1 : 0);

        if (entity.getEffectiveTime() != null) {
            vo.setStartTime(entity.getEffectiveTime().format(TIME_FORMATTER));
        }
        if (entity.getExpireTime() != null) {
            vo.setEndTime(entity.getExpireTime().format(TIME_FORMATTER));
        }
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(TIME_FORMATTER));
        }
        return vo;
    }

    /**
     * 映射权限级别
     *
     * @param level 权限级别
     * @return 权限级别字符串
     */
    private String mapPermissionLevel(Integer level) {
        if (level == null) {
            return "READ_ONLY";
        }
        return switch (level) {
            case 1 -> "READ_ONLY";
            case 4 -> "EMERGENCY";
            default -> "FULL_ACCESS";
        };
    }
}
