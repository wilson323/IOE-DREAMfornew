package net.lab1024.sa.common.organization.controller;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionVO;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;

/**
 * 区域权限管理接口
 * <p>
 * 当前用于管理端“区域权限弹窗”读取/删除权限记录。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@RestController
@Validated
@RequestMapping("/api/v1/area-permission")
public class AreaPermissionController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private AreaUserDao areaUserDao;

    @Observed(name = "areaPermission.list", contextualName = "area-permission-list")
    @GetMapping("/area/{areaId}")
    public ResponseDTO<List<AreaPermissionVO>> listByArea(
            @PathVariable @NotNull Long areaId,
            @RequestParam(required = false) String type) {

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
        return ResponseDTO.ok(result);
    }

    @Observed(name = "areaPermission.delete", contextualName = "area-permission-delete")
    @DeleteMapping("/{permissionId}")
    public ResponseDTO<Void> delete(@PathVariable String permissionId) {
        areaUserDao.deleteById(permissionId);
        return ResponseDTO.ok();
    }

    @Observed(name = "areaPermission.batchDelete", contextualName = "area-permission-batch-delete")
    @PostMapping("/batch-delete")
    @SuppressWarnings("deprecation")  // MyBatis-Plus 3.5.x标记为过时，推荐使用deleteByIds，但当前版本兼容
    public ResponseDTO<Void> batchDelete(@RequestBody @NotEmpty List<String> permissionIds) {
        areaUserDao.deleteBatchIds(permissionIds);
        return ResponseDTO.ok();
    }

    private AreaPermissionVO toUserPermissionVO(AreaUserEntity entity) {
        AreaPermissionVO vo = new AreaPermissionVO();
        vo.setPermissionId(entity.getId());
        vo.setUserName(entity.getRealName());
        vo.setUserCode(entity.getUsername());
        vo.setPermissionLevel(mapPermissionLevel(entity.getPermissionLevel()));
        vo.setStatus(entity.getRelationStatus() != null && entity.getRelationStatus() == 1 ? 1 : 0);

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

