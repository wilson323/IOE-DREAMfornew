package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackQueryForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.access.manager.AntiPassbackManager;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.security.entity.UserEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反潜回管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 使用@Transactional事务管理
 * </p>
 * <p>
 * 核心职责：
 * - 反潜回验证
 * - 反潜回记录管理
 * - 反潜回配置管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AntiPassbackServiceImpl implements AntiPassbackService {

    @Resource
    private AntiPassbackManager antiPassbackManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    /**
     * 反潜回验证
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Boolean> verifyAntiPassback(Long userId, Long deviceId, Integer inOutStatus, Long areaId) {
        log.info("[反潜回] 开始验证: userId={}, deviceId={}, inOutStatus={}, areaId={}",
                userId, deviceId, inOutStatus, areaId);

        try {
            // 参数验证
            if (userId == null) {
                return ResponseDTO.error("USER_ID_REQUIRED", "用户ID不能为空");
            }
            if (deviceId == null) {
                return ResponseDTO.error("DEVICE_ID_REQUIRED", "设备ID不能为空");
            }
            if (inOutStatus == null || (inOutStatus != 1 && inOutStatus != 2)) {
                return ResponseDTO.error("IN_OUT_STATUS_INVALID", "进出状态无效，必须为1（进）或2（出）");
            }

            // 调用Manager进行验证
            boolean result = antiPassbackManager.verifyAntiPassback(userId, deviceId, inOutStatus, areaId);

            log.info("[反潜回] 验证完成: userId={}, deviceId={}, result={}", userId, deviceId, result);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[反潜回] 验证异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("VERIFY_ANTI_PASSBACK_ERROR", "反潜回验证失败: " + e.getMessage());
        }
    }

    /**
     * 记录反潜回验证结果
     */
    @Override
    public ResponseDTO<Void> recordAntiPassback(Long userId, Long deviceId, Long areaId,
                                                Integer inOutStatus, Integer verifyType) {
        log.info("[反潜回] 记录验证结果: userId={}, deviceId={}, areaId={}, inOutStatus={}, verifyType={}",
                userId, deviceId, areaId, inOutStatus, verifyType);

        try {
            // 参数验证
            if (userId == null) {
                return ResponseDTO.error("USER_ID_REQUIRED", "用户ID不能为空");
            }
            if (deviceId == null) {
                return ResponseDTO.error("DEVICE_ID_REQUIRED", "设备ID不能为空");
            }
            if (inOutStatus == null || (inOutStatus != 1 && inOutStatus != 2)) {
                return ResponseDTO.error("IN_OUT_STATUS_INVALID", "进出状态无效，必须为1（进）或2（出）");
            }

            // 调用Manager记录
            antiPassbackManager.recordAntiPassback(userId, deviceId, areaId, inOutStatus, verifyType);

            log.info("[反潜回] 记录成功: userId={}, deviceId={}", userId, deviceId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[反潜回] 记录异常: userId={}, deviceId={}, error={}", userId, deviceId, e.getMessage(), e);
            return ResponseDTO.error("RECORD_ANTI_PASSBACK_ERROR", "记录反潜回验证结果失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询反潜回记录
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(AntiPassbackQueryForm queryForm) {
        log.info("[反潜回] 分页查询记录: pageNum={}, pageSize={}, userId={}, deviceId={}, areaId={}, inOutStatus={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getUserId(),
                queryForm.getDeviceId(), queryForm.getAreaId(), queryForm.getInOutStatus());

        try {
            // 调用Manager查询
            Page<AntiPassbackRecordEntity> page = antiPassbackManager.queryRecords(
                    queryForm.getUserId(),
                    queryForm.getDeviceId(),
                    queryForm.getAreaId(),
                    queryForm.getInOutStatus(),
                    queryForm.getStartTime(),
                    queryForm.getEndTime(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            // 转换为VO列表
            List<AntiPassbackRecordVO> voList = page.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AntiPassbackRecordVO> result = PageResult.from(page);
            result.setRecords(voList);

            log.info("[反潜回] 分页查询成功: total={}, pageNum={}, pageSize={}",
                    result.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[反潜回] 分页查询异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_ANTI_PASSBACK_RECORDS_ERROR", "查询反潜回记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取反潜回配置
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AntiPassbackConfigVO> getConfig(Long areaId) {
        log.info("[反潜回] 获取配置: areaId={}", areaId);

        try {
            if (areaId == null) {
                return ResponseDTO.error("AREA_ID_REQUIRED", "区域ID不能为空");
            }

            // 调用Manager获取配置
            AntiPassbackManager.AntiPassbackConfig config = antiPassbackManager.getAntiPassbackConfig(areaId);
            if (config == null) {
                return ResponseDTO.error("CONFIG_NOT_FOUND", "反潜回配置不存在");
            }

            // 转换为VO
            AntiPassbackConfigVO vo = new AntiPassbackConfigVO();
            vo.setAreaId(areaId);
            vo.setEnabled(config.isEnabled());
            vo.setTimeWindow(config.getTimeWindow());

            // 查询区域名称
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + areaId,
                        HttpMethod.GET,
                        null,
                        AreaEntity.class
                );

                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    vo.setAreaName(areaResponse.getData().getAreaName());
                }
            } catch (Exception e) {
                log.debug("[反潜回] 查询区域信息失败: areaId={}, error={}", areaId, e.getMessage());
            }

            log.info("[反潜回] 获取配置成功: areaId={}, enabled={}, timeWindow={}",
                    areaId, vo.getEnabled(), vo.getTimeWindow());
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[反潜回] 获取配置异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_ANTI_PASSBACK_CONFIG_ERROR", "获取反潜回配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新反潜回配置
     */
    @Override
    public ResponseDTO<Void> updateConfig(AntiPassbackConfigForm configForm) {
        log.info("[反潜回] 更新配置: areaId={}, enabled={}, timeWindow={}",
                configForm.getAreaId(), configForm.getEnabled(), configForm.getTimeWindow());

        try {
            // 参数验证
            if (configForm.getAreaId() == null) {
                return ResponseDTO.error("AREA_ID_REQUIRED", "区域ID不能为空");
            }
            if (configForm.getEnabled() == null) {
                return ResponseDTO.error("ENABLED_REQUIRED", "启用状态不能为空");
            }
            if (configForm.getTimeWindow() != null && configForm.getTimeWindow() <= 0) {
                return ResponseDTO.error("TIME_WINDOW_INVALID", "时间窗口必须大于0");
            }

            // 调用Manager更新配置
            antiPassbackManager.updateAntiPassbackConfig(
                    configForm.getAreaId(),
                    configForm.getEnabled(),
                    configForm.getTimeWindow()
            );

            log.info("[反潜回] 更新配置成功: areaId={}", configForm.getAreaId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[反潜回] 更新配置异常: areaId={}, error={}", configForm.getAreaId(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_ANTI_PASSBACK_CONFIG_ERROR", "更新反潜回配置失败: " + e.getMessage());
        }
    }

    /**
     * 转换为VO
     */
    private AntiPassbackRecordVO convertToVO(AntiPassbackRecordEntity entity) {
        AntiPassbackRecordVO vo = new AntiPassbackRecordVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setDeviceId(entity.getDeviceId());
        vo.setAreaId(entity.getAreaId());
        vo.setInOutStatus(entity.getInOutStatus());
        vo.setRecordTime(entity.getRecordTime());
        vo.setAccessType(entity.getAccessType());
        vo.setVerifyType(entity.getVerifyType());
        vo.setCreateTime(entity.getCreateTime());

        // 设置进出状态描述
        if (entity.getInOutStatus() != null) {
            vo.setInOutStatusDesc(entity.getInOutStatus() == 1 ? "进入" : "离开");
        }

        // 设置验证方式描述（使用VerifyTypeEnum统一管理）
        if (entity.getVerifyType() != null) {
            VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.getByCode(entity.getVerifyType());
            if (verifyTypeEnum != null) {
                vo.setVerifyTypeDesc(verifyTypeEnum.getDescription());
            } else {
                vo.setVerifyTypeDesc("未知");
            }
        }

        // 查询用户名称
        if (entity.getUserId() != null) {
            try {
                ResponseDTO<UserEntity> userResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/user/" + entity.getUserId(),
                        HttpMethod.GET,
                        null,
                        UserEntity.class
                );

                if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                    vo.setUserName(userResponse.getData().getUserName());
                }
            } catch (Exception e) {
                log.debug("[反潜回] 查询用户信息失败: userId={}, error={}", entity.getUserId(), e.getMessage());
            }
        }

        // 查询设备名称
        if (entity.getDeviceId() != null) {
            try {
                DeviceEntity device = accessDeviceDao.selectById(entity.getDeviceId().toString());
                if (device != null) {
                    vo.setDeviceName(device.getDeviceName());
                }
            } catch (Exception e) {
                log.debug("[反潜回] 查询设备信息失败: deviceId={}, error={}", entity.getDeviceId(), e.getMessage());
            }
        }

        // 查询区域名称
        if (entity.getAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + entity.getAreaId(),
                        HttpMethod.GET,
                        null,
                        AreaEntity.class
                );

                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    vo.setAreaName(areaResponse.getData().getAreaName());
                }
            } catch (Exception e) {
                log.debug("[反潜回] 查询区域信息失败: areaId={}, error={}", entity.getAreaId(), e.getMessage());
            }
        }

        return vo;
    }
}
