package net.lab1024.sa.common.organization.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaUserEntity;

@Mapper
public interface AreaUserDao extends BaseMapper<AreaUserEntity> {

    List<AreaUserEntity> selectByAreaId(Long areaId);

    List<AreaUserEntity> selectByUserId(Long userId);

    AreaUserEntity selectByAreaIdAndUserId(Long areaId, Long userId);

    List<AreaUserEntity> selectNeedSync();

    List<AreaUserEntity> selectByAreaIdAndPermissionLevel(Long areaId, Integer permissionLevel);

    List<AreaUserEntity> selectByParentAreaPath(String areaPath);

    List<AreaUserEntity> selectUserAreaPermissions(Long userId, List<Long> areaIds);

    int updateSyncStatus(String id, Integer status, String errorMessage, LocalDateTime lastSyncTime);

    int incrementRetryCount(String id);

    int batchUpdateSyncStatus(List<String> ids, Integer status, LocalDateTime lastSyncTime);

    int countByAreaId(Long areaId);

    List<AreaUserEntity> selectExpired(LocalDateTime now);

    List<AreaUserEntity> selectSoonToExpire(LocalDateTime now);

    List<AreaUserEntity> selectByRelationType(Long areaId, Integer relationType);

    boolean hasPermission(Long userId, Long areaId, Integer permissionLevel);
}
