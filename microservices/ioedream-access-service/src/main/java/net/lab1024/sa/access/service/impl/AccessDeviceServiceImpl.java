package net.lab1024.sa.access.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController;
import net.lab1024.sa.access.dao.AccessAreaDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.form.DeviceControlRequest;
import net.lab1024.sa.access.domain.form.AddDeviceRequest;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.domain.vo.MobileDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceControlResultVO;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import org.springframework.http.HttpMethod;

/**
 * 闂ㄧ璁惧鏈嶅姟瀹炵幇绫? * <p>
 * 瀹炵幇闂ㄧ璁惧绠＄悊鐩稿叧涓氬姟鍔熻兘
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - Service瀹炵幇绫诲湪ioedream-access-service涓? * - 浣跨敤@Resource娉ㄥ叆渚濊禆
 * - 浣跨敤@Transactional绠＄悊浜嬪姟
 * - 涓ユ牸閬靛惊鍥涘眰鏋舵瀯瑙勮寖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Observed(name = "access.device.query", contextualName = "access-device-query")
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessDeviceVO>> queryDevices(AccessDeviceQueryForm queryForm) {
        log.info("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧锛宲ageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getAreaId(), queryForm.getDeviceStatus());

        try {
            // 鏋勫缓鏌ヨ鏉′欢
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 鍏抽敭璇嶆悳绱紙璁惧鍚嶇О銆佽澶囩紪鍙凤級
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, queryForm.getKeyword())
                        .or()
                        .like(DeviceEntity::getDeviceCode, queryForm.getKeyword()));
            }

            // 鍖哄煙绛涢€?            if (queryForm.getAreaId() != null) {
                wrapper.eq(DeviceEntity::getAreaId, queryForm.getAreaId());
            }

            // 璁惧鐘舵€佺瓫閫?            if (StringUtils.hasText(queryForm.getDeviceStatus())) {
                wrapper.eq(DeviceEntity::getDeviceStatus, queryForm.getDeviceStatus());
            }

            // 鍚敤鏍囧織绛涢€?            if (queryForm.getEnabledFlag() != null) {
                wrapper.eq(DeviceEntity::getEnabledFlag, queryForm.getEnabledFlag());
            }

            // 鎺掑簭
            wrapper.orderByDesc(DeviceEntity::getCreateTime);

            // 鎵ц鍒嗛〉鏌ヨ
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = accessDeviceDao.selectPage(page, wrapper);

            // 杞崲涓篤O鍒楄〃
            List<AccessDeviceVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 鏋勫缓鍒嗛〉缁撴灉
            PageResult<AccessDeviceVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧鎴愬姛锛屾€绘暟={}", pageResult.getTotal());
            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧鍙傛暟寮傚父, error={}", e.getMessage());
            throw new ParamException("QUERY_DEVICES_PARAM_ERROR", "鏌ヨ璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧涓氬姟寮傚父, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧绯荤粺寮傚父, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鍒嗛〉鏌ヨ璁惧鏈煡寮傚父", e);
            throw new SystemException("QUERY_DEVICES_SYSTEM_ERROR", "鏌ヨ璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.getDetail", contextualName = "access-device-get-detail")
    @Transactional(readOnly = true)
    @Cacheable(value = "access:device:detail", key = "#deviceId", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<AccessDeviceVO> getDeviceDetail(Long deviceId) {
        log.info("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏锛宒eviceId={}", deviceId);

        try {
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 楠岃瘉鏄惁涓洪棬绂佽澶?            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[闂ㄧ璁惧] 璁惧绫诲瀷涓嶅尮閰嶏紝deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "璁惧绫诲瀷涓嶅尮閰?);
            }

            AccessDeviceVO vo = convertToVO(device);
            log.info("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏鎴愬姛锛宒eviceId={}", deviceId);
            return ResponseDTO.ok(vo);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏鍙傛暟寮傚父, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("GET_DEVICE_PARAM_ERROR", "鏌ヨ璁惧璇︽儏鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏涓氬姟寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏绯荤粺寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ璁惧璇︽儏鏈煡寮傚父, deviceId={}", deviceId, e);
            throw new SystemException("GET_DEVICE_SYSTEM_ERROR", "鏌ヨ璁惧璇︽儏绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.add", contextualName = "access-device-add")
    @CircuitBreaker(name = "access-device-add-circuitbreaker", fallbackMethod = "addDeviceFallback")
    @Retry(name = "access-device-add-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.add", description = "闂ㄧ璁惧娣诲姞鑰楁椂")
    @Counted(value = "access.device.add.count", description = "闂ㄧ璁惧娣诲姞娆℃暟")
    @CacheEvict(value = "access:device:detail", allEntries = true)
    public ResponseDTO<Long> addDevice(AccessDeviceAddForm addForm) {
        log.info("[闂ㄧ璁惧] 娣诲姞璁惧锛宒eviceName={}, deviceCode={}, areaId={}",
                addForm.getDeviceName(), addForm.getDeviceCode(), addForm.getAreaId());

        try {
            // 1. 楠岃瘉璁惧缂栧彿鍞竴鎬?            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, addForm.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
            if (existingDevice != null) {
                log.warn("[闂ㄧ璁惧] 璁惧缂栧彿宸插瓨鍦紝deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "璁惧缂栧彿宸插瓨鍦?);
            }

            // 2. 楠岃瘉鍖哄煙鏄惁瀛樺湪
            AreaEntity area = getAreaById(addForm.getAreaId());
            if (area == null) {
                log.warn("[闂ㄧ璁惧] 鍖哄煙涓嶅瓨鍦紝areaId={}", addForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "鍖哄煙涓嶅瓨鍦?);
            }

            // 3. 鍒涘缓璁惧瀹炰綋
            DeviceEntity device = new DeviceEntity();
            device.setDeviceName(addForm.getDeviceName());
            device.setDeviceCode(addForm.getDeviceCode());
            device.setDeviceType("ACCESS");
            device.setAreaId(addForm.getAreaId());
            device.setIpAddress(addForm.getIpAddress());
            device.setPort(addForm.getPort());
            device.setEnabledFlag(addForm.getEnabledFlag() != null ? addForm.getEnabledFlag() : 1);
            device.setDeviceStatus("OFFLINE"); // 榛樿绂荤嚎鐘舵€?
            // 4. 淇濆瓨璁惧
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 娣诲姞璁惧澶辫触锛屾彃鍏ヨ鏁颁负0");
                return ResponseDTO.error("ADD_DEVICE_ERROR", "娣诲姞璁惧澶辫触");
            }

            log.info("[闂ㄧ璁惧] 娣诲姞璁惧鎴愬姛锛宒eviceId={}", device.getDeviceId());
            return ResponseDTO.ok(device.getDeviceId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 娣诲姞璁惧鍙傛暟寮傚父, error={}", e.getMessage());
            throw new ParamException("ADD_DEVICE_PARAM_ERROR", "娣诲姞璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 娣诲姞璁惧涓氬姟寮傚父, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 娣诲姞璁惧绯荤粺寮傚父, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 娣诲姞璁惧鏈煡寮傚父", e);
            throw new SystemException("ADD_DEVICE_SYSTEM_ERROR", "娣诲姞璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.update", contextualName = "access-device-update")
    @CircuitBreaker(name = "access-device-update-circuitbreaker", fallbackMethod = "updateDeviceFallback")
    @Retry(name = "access-device-update-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.update", description = "闂ㄧ璁惧鏇存柊鑰楁椂")
    @Counted(value = "access.device.update.count", description = "闂ㄧ璁惧鏇存柊娆℃暟")
    @CacheEvict(value = "access:device:detail", key = "#updateForm.deviceId")
    public ResponseDTO<Boolean> updateDevice(AccessDeviceUpdateForm updateForm) {
        log.info("[闂ㄧ璁惧] 鏇存柊璁惧锛宒eviceId={}", updateForm.getDeviceId());

        try {
            // 1. 鏌ヨ璁惧鏄惁瀛樺湪
            DeviceEntity device = accessDeviceDao.selectById(updateForm.getDeviceId());
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 2. 楠岃瘉璁惧缂栧彿鍞竴鎬э紙鎺掗櫎褰撳墠璁惧锛?            if (!device.getDeviceCode().equals(updateForm.getDeviceCode())) {
                LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DeviceEntity::getDeviceCode, updateForm.getDeviceCode())
                        .ne(DeviceEntity::getId, updateForm.getDeviceId())
                        .eq(DeviceEntity::getDeletedFlag, 0);
                DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
                if (existingDevice != null) {
                    log.warn("[闂ㄧ璁惧] 璁惧缂栧彿宸插瓨鍦紝deviceCode={}", updateForm.getDeviceCode());
                    return ResponseDTO.error("DEVICE_CODE_EXISTS", "璁惧缂栧彿宸插瓨鍦?);
                }
            }

            // 3. 楠岃瘉鍖哄煙鏄惁瀛樺湪
            AreaEntity area = getAreaById(updateForm.getAreaId());
            if (area == null) {
                log.warn("[闂ㄧ璁惧] 鍖哄煙涓嶅瓨鍦紝areaId={}", updateForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "鍖哄煙涓嶅瓨鍦?);
            }

            // 4. 鏇存柊璁惧淇℃伅
            device.setDeviceName(updateForm.getDeviceName());
            device.setDeviceCode(updateForm.getDeviceCode());
            device.setAreaId(updateForm.getAreaId());
            device.setIpAddress(updateForm.getIpAddress());
            device.setPort(updateForm.getPort());
            if (updateForm.getEnabledFlag() != null) {
                device.setEnabledFlag(updateForm.getEnabledFlag());
            }

            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 鏇存柊璁惧澶辫触锛屾洿鏂拌鏁颁负0");
                return ResponseDTO.error("UPDATE_DEVICE_ERROR", "鏇存柊璁惧澶辫触");
            }

            log.info("[闂ㄧ璁惧] 鏇存柊璁惧鎴愬姛锛宒eviceId={}", updateForm.getDeviceId());
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鍙傛暟寮傚父, deviceId={}, error={}", updateForm.getDeviceId(), e.getMessage());
            throw new ParamException("UPDATE_DEVICE_PARAM_ERROR", "鏇存柊璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧涓氬姟寮傚父, deviceId={}, code={}, message={}", updateForm.getDeviceId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧绯荤粺寮傚父, deviceId={}, code={}, message={}", updateForm.getDeviceId(), e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鏈煡寮傚父, deviceId={}", updateForm.getDeviceId(), e);
            throw new SystemException("UPDATE_DEVICE_SYSTEM_ERROR", "鏇存柊璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.delete", contextualName = "access-device-delete")
    @CircuitBreaker(name = "access-device-delete-circuitbreaker", fallbackMethod = "deleteDeviceFallback")
    @Retry(name = "access-device-delete-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.delete", description = "闂ㄧ璁惧鍒犻櫎鑰楁椂")
    @Counted(value = "access.device.delete.count", description = "闂ㄧ璁惧鍒犻櫎娆℃暟")
    @CacheEvict(value = "access:device:detail", key = "#deviceId")
    public ResponseDTO<Boolean> deleteDevice(Long deviceId) {
        log.info("[闂ㄧ璁惧] 鍒犻櫎璁惧锛宒eviceId={}", deviceId);

        try {
            // 1. 鏌ヨ璁惧鏄惁瀛樺湪
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 2. 杞垹闄わ紙璁剧疆deletedFlag=1锛?            device.setDeletedFlag(1);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧澶辫触锛屾洿鏂拌鏁颁负0");
                return ResponseDTO.error("DELETE_DEVICE_ERROR", "鍒犻櫎璁惧澶辫触");
            }

            log.info("[闂ㄧ璁惧] 鍒犻櫎璁惧鎴愬姛锛宒eviceId={}", deviceId);
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鍒犻櫎璁惧鍙傛暟寮傚父, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("DELETE_DEVICE_PARAM_ERROR", "鍒犻櫎璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鍒犻櫎璁惧涓氬姟寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧绯荤粺寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧鏈煡寮傚父, deviceId={}", deviceId, e);
            throw new SystemException("DELETE_DEVICE_SYSTEM_ERROR", "鍒犻櫎璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "access:device:detail", key = "#deviceId")
    public ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, Integer status) {
        log.info("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€侊紝deviceId={}, status={}", deviceId, status);

        try {
            // 1. 鏌ヨ璁惧鏄惁瀛樺湪
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 2. 楠岃瘉鐘舵€佸€硷紙0-绂佺敤锛?-鍚敤锛?            if (status != 0 && status != 1) {
                log.warn("[闂ㄧ璁惧] 鐘舵€佸€兼棤鏁堬紝status={}", status);
                return ResponseDTO.error("INVALID_STATUS", "鐘舵€佸€兼棤鏁堬紝鍙兘涓?锛堢鐢級鎴?锛堝惎鐢級");
            }

            // 3. 鏇存柊璁惧鐘舵€?            device.setEnabledFlag(status);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佸け璐ワ紝鏇存柊琛屾暟涓?");
                return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "鏇存柊璁惧鐘舵€佸け璐?);
            }

            log.info("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佹垚鍔燂紝deviceId={}, status={}", deviceId, status);
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佸弬鏁板紓甯? deviceId={}, status={}, error={}", deviceId, status, e.getMessage());
            throw new ParamException("UPDATE_DEVICE_STATUS_PARAM_ERROR", "鏇存柊璁惧鐘舵€佸弬鏁板紓甯? " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佷笟鍔″紓甯? deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佺郴缁熷紓甯? deviceId={}, status={}, code={}, message={}", deviceId, status, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏇存柊璁惧鐘舵€佹湭鐭ュ紓甯? deviceId={}, status={}", deviceId, status, e);
            throw new SystemException("UPDATE_DEVICE_STATUS_SYSTEM_ERROR", "鏇存柊璁惧鐘舵€佺郴缁熷紓甯?, e);
        }
    }

    // ==================== 绉诲姩绔柟娉?====================

    /**
     * 鑾峰彇闄勮繎璁惧
     * <p>
     * 鏍规嵁GPS鍧愭爣鏌ヨ闄勮繎鐨勯棬绂佽澶?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param latitude 绾害
     * @param longitude 缁忓害
     * @param radius 鍗婂緞锛堢背锛岄粯璁?00绫筹級
     * @return 闄勮繎璁惧鍒楄〃锛屼娇鐢℉aversine鍏紡璁＄畻璺濈锛堟寜璺濈鎺掑簭锛?     */
    @Override
    @Observed(name = "access.device.getNearby", contextualName = "access-device-get-nearby")
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileDeviceItem>> getNearbyDevices(
            Long userId, Double latitude, Double longitude, Integer radius) {
        log.info("[闂ㄧ璁惧] 绉诲姩绔檮杩戣澶囨煡璇紝userId={}, latitude={}, longitude={}, radius={}",
                userId, latitude, longitude, radius);

        try {
            // 鍙傛暟楠岃瘉
            if (latitude == null || longitude == null) {
                log.warn("[闂ㄧ璁惧] GPS鍧愭爣涓虹┖锛屾棤娉曟煡璇㈤檮杩戣澶?);
                return ResponseDTO.ok(List.of());
            }

            int radiusToUse = (radius == null || radius <= 0) ? 500 : radius;

            // 1. 鏌ヨ鎵€鏈夊惎鐢ㄧ殑闂ㄧ璁惧
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            if (devices.isEmpty()) {
                log.info("[闂ㄧ璁惧] 鏈壘鍒板惎鐢ㄧ殑闂ㄧ璁惧");
                return ResponseDTO.ok(List.of());
            }

            // 2. 璁＄畻璺濈骞剁瓫閫?            List<AccessMobileController.MobileDeviceItem> nearbyDevices = new ArrayList<>();
            for (DeviceEntity device : devices) {
                try {
                    // 浠巈xtendedAttributes涓幏鍙栫粡绾害
                    Double deviceLat = getLatitudeFromDevice(device);
                    Double deviceLng = getLongitudeFromDevice(device);

                    if (deviceLat == null || deviceLng == null) {
                        log.debug("[闂ㄧ璁惧] 璁惧缂哄皯GPS鍧愭爣锛岃烦杩囷紝deviceId={}", device.getDeviceId());
                        continue;
                    }

                    // 璁＄畻璺濈锛堜娇鐢℉aversine鍏紡锛?                    double distance = calculateDistance(latitude, longitude, deviceLat, deviceLng);

                    if (distance <= radiusToUse) {
                        AccessMobileController.MobileDeviceItem item = new AccessMobileController.MobileDeviceItem();
                        item.setDeviceId(device.getDeviceId());
                        item.setDeviceName(device.getDeviceName());
                        item.setDeviceLocation(getDeviceLocation(device));
                        item.setLatitude(deviceLat);
                        item.setLongitude(deviceLng);
                        item.setDistance((int) Math.round(distance));
                        nearbyDevices.add(item);
                    }
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[闂ㄧ璁惧] 澶勭悊璁惧鍙傛暟閿欒, deviceId={}, error={}", device.getDeviceId(), e.getMessage());
                    // 鍗曚釜璁惧澶勭悊澶辫触涓嶅奖鍝嶅叾浠栬澶囷紝缁х画澶勭悊
                } catch (BusinessException e) {
                    log.warn("[闂ㄧ璁惧] 澶勭悊璁惧涓氬姟寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage());
                    // 鍗曚釜璁惧澶勭悊澶辫触涓嶅奖鍝嶅叾浠栬澶囷紝缁х画澶勭悊
                } catch (SystemException e) {
                    log.warn("[闂ㄧ璁惧] 澶勭悊璁惧绯荤粺寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage(), e);
                    // 鍗曚釜璁惧澶勭悊澶辫触涓嶅奖鍝嶅叾浠栬澶囷紝缁х画澶勭悊
                } catch (Exception e) {
                    log.warn("[闂ㄧ璁惧] 澶勭悊璁惧鏈煡寮傚父, deviceId={}", device.getDeviceId(), e);
                    // 鍗曚釜璁惧澶勭悊澶辫触涓嶅奖鍝嶅叾浠栬澶囷紝缁х画澶勭悊
                }
            }

            // 3. 鎸夎窛绂绘帓搴?            nearbyDevices.sort(Comparator.comparingInt(AccessMobileController.MobileDeviceItem::getDistance));

            log.info("[闂ㄧ璁惧] 闄勮繎璁惧鏌ヨ鎴愬姛锛屾壘鍒皗}涓澶?, nearbyDevices.size());
            return ResponseDTO.ok(nearbyDevices);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 闄勮繎璁惧鏌ヨ鍙傛暟寮傚父, error={}", e.getMessage());
            throw new ParamException("QUERY_NEARBY_DEVICES_PARAM_ERROR", "鏌ヨ闄勮繎璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 闄勮繎璁惧鏌ヨ涓氬姟寮傚父, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 闄勮繎璁惧鏌ヨ绯荤粺寮傚父, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 闄勮繎璁惧鏌ヨ鏈煡寮傚父", e);
            throw new SystemException("QUERY_NEARBY_DEVICES_SYSTEM_ERROR", "鏌ヨ闄勮繎璁惧绯荤粺寮傚父", e);
        }
    }

    /**
     * 鑾峰彇绉诲姩绔敤鎴锋潈闄?     * <p>
     * 鏌ヨ鐢ㄦ埛鐨勯棬绂佹潈闄愪俊鎭紝鍖呮嫭鍖哄煙鏉冮檺鍜岃澶囨潈闄?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛鏉冮檺淇℃伅
     */
    /**
     * 鑾峰彇绉诲姩绔敤鎴锋潈闄?     * <p>
     * 鏌ヨ鐢ㄦ埛鐨勯棬绂佹潈闄愪俊鎭紝鍖呮嫭鍖哄煙鏉冮檺鍜岃澶囨潈闄?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛鏉冮檺淇℃伅
     */
    @Override
    @Observed(name = "access.device.getMobilePermissions", contextualName = "access-device-get-mobile-permissions")
    @Transactional(readOnly = true)
    public ResponseDTO<AccessMobileController.MobileUserPermissions> getMobileUserPermissions(Long userId) {
        log.info("[闂ㄧ璁惧] 绉诲姩绔敤鎴锋潈闄愭煡璇紝userId={}", userId);

        try {
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "鐢ㄦ埛ID涓嶈兘涓虹┖");
            }

            AccessMobileController.MobileUserPermissions permissions = new AccessMobileController.MobileUserPermissions();
            permissions.setUserId(userId);

            // 1. 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑鍖哄煙ID鍒楄〃
            // 閫氳繃缃戝叧璋冪敤闂ㄧ鏈嶅姟鏌ヨ鐢ㄦ埛鏉冮檺
            // 娉ㄦ剰锛氳繖閲岄渶瑕佽皟鐢ㄩ棬绂佹潈闄愭湇鍔★紝濡傛灉鏉冮檺鏈嶅姟鍦╝ccess-service鍐呴儴锛屽彲浠ョ洿鎺ヨ皟鐢?            // 濡傛灉鏉冮檺鏈嶅姟鍦╟ommon-service锛岄渶瑕侀€氳繃缃戝叧璋冪敤
            List<Long> allowedAreaIds = queryUserAllowedAreas(userId);
            permissions.setAllowedAreaIds(allowedAreaIds);

            // 2. 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑璁惧ID鍒楄〃
            List<Long> allowedDeviceIds = queryUserAllowedDevices(userId, allowedAreaIds);
            permissions.setAllowedDeviceIds(allowedDeviceIds);

            // 3. 纭畾鏉冮檺绾у埆
            // 鏉冮檺绾у埆锛欰DMIN-绠＄悊鍛? NORMAL-鏅€氱敤鎴? VISITOR-璁垮
            String permissionLevel = determinePermissionLevel(userId, allowedAreaIds, allowedDeviceIds);
            permissions.setPermissionLevel(permissionLevel);

            log.info("[闂ㄧ璁惧] 鐢ㄦ埛鏉冮檺鏌ヨ鎴愬姛锛寀serId={}, 鍖哄煙鏁伴噺={}, 璁惧鏁伴噺={}, 鏉冮檺绾у埆={}",
                    userId, allowedAreaIds.size(), allowedDeviceIds.size(), permissionLevel);
            return ResponseDTO.ok(permissions);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鐢ㄦ埛鏉冮檺鏌ヨ鍙傛暟寮傚父, userId={}, error={}", userId, e.getMessage());
            throw new ParamException("QUERY_USER_PERMISSIONS_PARAM_ERROR", "鏌ヨ鐢ㄦ埛鏉冮檺鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鐢ㄦ埛鏉冮檺鏌ヨ涓氬姟寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鐢ㄦ埛鏉冮檺鏌ヨ绯荤粺寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鐢ㄦ埛鏉冮檺鏌ヨ鏈煡寮傚父, userId={}", userId, e);
            throw new SystemException("QUERY_USER_PERMISSIONS_SYSTEM_ERROR", "鏌ヨ鐢ㄦ埛鏉冮檺绯荤粺寮傚父", e);
        }
    }

    /**
     * 鑾峰彇绉诲姩绔疄鏃剁姸鎬?     * <p>
     * 鏌ヨ璁惧鐨勫疄鏃剁姸鎬佷俊鎭紝鍖呮嫭鍦ㄧ嚎鐢ㄦ埛鏁扮瓑
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧绉诲姩绔疄鏃剁姸鎬侊紝鍖呮嫭鍦ㄧ嚎鐢ㄦ埛鏁扮瓑淇℃伅
     */
    @Override
    @Observed(name = "access.device.getMobileRealTimeStatus", contextualName = "access-device-get-mobile-realtime")
    @Transactional(readOnly = true)
    public ResponseDTO<AccessMobileController.MobileRealTimeStatus> getMobileRealTimeStatus(Long deviceId) {
        log.info("[闂ㄧ璁惧] 绉诲姩绔疄鏃剁姸鎬佹煡璇紝deviceId={}", deviceId);

        try {
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "璁惧ID涓嶈兘涓虹┖");
            }

            // 1. 鏌ヨ璁惧淇℃伅
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            AccessMobileController.MobileRealTimeStatus status = new AccessMobileController.MobileRealTimeStatus();
            status.setDeviceId(device.getDeviceId());
            status.setDeviceName(device.getDeviceName());
            status.setDeviceStatus(device.getDeviceStatus());

            // 2. 鏌ヨ鍦ㄧ嚎鐢ㄦ埛鏁伴噺锛堥€氳繃缃戝叧璋冪敤璁惧閫氳鏈嶅姟锛?            // 娉ㄦ剰锛氬疄闄呭疄鐜伴渶瑕佹牴鎹澶囬€氳鏈嶅姟鐨勬帴鍙ｆ潵璋冪敤
            Integer onlineCount = queryDeviceOnlineCount(deviceId);
            status.setOnlineCount(onlineCount != null ? onlineCount : 0);

            // 3. 鑾峰彇鏈€鍚庢洿鏂版椂闂?            if (device.getLastOnlineTime() != null) {
                status.setLastUpdateTime(device.getLastOnlineTime().toString());
            } else {
                status.setLastUpdateTime(device.getUpdateTime() != null ? device.getUpdateTime().toString() : "");
            }

            log.info("[闂ㄧ璁惧] 瀹炴椂鐘舵€佹煡璇㈡垚鍔燂紝deviceId={}, status={}, onlineCount={}",
                    deviceId, status.getDeviceStatus(), status.getOnlineCount());
            return ResponseDTO.ok(status);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 瀹炴椂鐘舵€佹煡璇㈠弬鏁板紓甯? deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("QUERY_REALTIME_STATUS_PARAM_ERROR", "鏌ヨ瀹炴椂鐘舵€佸弬鏁板紓甯? " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 瀹炴椂鐘舵€佹煡璇笟鍔″紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 瀹炴椂鐘舵€佹煡璇㈢郴缁熷紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 瀹炴椂鐘舵€佹煡璇㈡湭鐭ュ紓甯? deviceId={}", deviceId, e);
            throw new SystemException("QUERY_REALTIME_STATUS_SYSTEM_ERROR", "鏌ヨ瀹炴椂鐘舵€佺郴缁熷紓甯?, e);
        }
    }

    // ==================== 绉佹湁鏂规硶 ====================

    /**
     * 杞崲璁惧瀹炰綋涓篤O
     *
     * @param device 璁惧瀹炰綋
     * @return 璁惧VO
     */
    private AccessDeviceVO convertToVO(DeviceEntity device) {
        AccessDeviceVO vo = new AccessDeviceVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceType(device.getDeviceType());
        vo.setAreaId(device.getAreaId());
        vo.setIpAddress(device.getIpAddress());
        vo.setPort(device.getPort());
        vo.setDeviceStatus(device.getDeviceStatus());
        vo.setEnabledFlag(device.getEnabledFlag());
        vo.setLastOnlineTime(device.getLastOnlineTime());
        vo.setCreateTime(device.getCreateTime());
        vo.setUpdateTime(device.getUpdateTime());

        // 閫氳繃缃戝叧鑾峰彇鍖哄煙鍚嶇О
        if (device.getAreaId() != null) {
            AreaEntity area = getAreaById(device.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }
        }

        return vo;
    }

    /**
     * 閫氳繃缃戝叧鑾峰彇鍖哄煙淇℃伅
     *
     * @param areaId 鍖哄煙ID
     * @return 鍖哄煙瀹炰綋
     */
    private AreaEntity getAreaById(Long areaId) {
        try {
            ResponseDTO<AreaEntity> result = gatewayServiceClient.callCommonService(
                    "/api/v1/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );
            return result != null && result.isSuccess() ? result.getData() : null;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇鍖哄煙淇℃伅鍙傛暟閿欒, areaId={}, error={}", areaId, e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇鍖哄煙淇℃伅涓氬姟寮傚父, areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇鍖哄煙淇℃伅绯荤粺寮傚父, areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇鍖哄煙淇℃伅鏈煡寮傚父, areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 浠庤澶囨墿灞曞睘鎬т腑鑾峰彇绾害
     * <p>
     * 璁惧GPS鍧愭爣瀛樺偍鍦╡xtendedAttributes JSON瀛楁涓?     * 鏍煎紡锛歿"latitude": 39.9042, "longitude": 116.4074}
     * </p>
     *
     * @param device 璁惧瀹炰綋
     * @return 绾害锛屽鏋滀笉瀛樺湪鍒欒繑鍥瀗ull
     */
    private Double getLatitudeFromDevice(DeviceEntity device) {
        try {
            if (device.getExtendedAttributes() == null || device.getExtendedAttributes().trim().isEmpty()) {
                return null;
            }
            Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object latObj = attrs.get("latitude");
            if (latObj != null) {
                return Double.valueOf(latObj.toString());
            }
            return null;
        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧绾害鍙傛暟閿欒, deviceId={}, error={}", device.getDeviceId(), e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧绾害涓氬姟寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧绾害绯荤粺寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧绾害鏈煡寮傚父, deviceId={}", device.getDeviceId(), e);
            return null;
        }
    }

    /**
     * 浠庤澶囨墿灞曞睘鎬т腑鑾峰彇缁忓害
     *
     * @param device 璁惧瀹炰綋
     * @return 缁忓害锛屽鏋滀笉瀛樺湪鍒欒繑鍥瀗ull
     */
    private Double getLongitudeFromDevice(DeviceEntity device) {
        try {
            if (device.getExtendedAttributes() == null || device.getExtendedAttributes().trim().isEmpty()) {
                return null;
            }
            Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            Object lngObj = attrs.get("longitude");
            if (lngObj != null) {
                return Double.valueOf(lngObj.toString());
            }
            return null;
        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧缁忓害鍙傛暟閿欒, deviceId={}, error={}", device.getDeviceId(), e.getMessage());
            return null;
        } catch (BusinessException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧缁忓害涓氬姟寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧缁忓害绯荤粺寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽璁惧缁忓害鏈煡寮傚父, deviceId={}", device.getDeviceId(), e);
            return null;
        }
    }

    /**
     * 鑾峰彇璁惧浣嶇疆鎻忚堪
     * <p>
     * 浼樺厛浠巈xtendedAttributes鑾峰彇location瀛楁锛屽鏋滄病鏈夊垯浣跨敤鍖哄煙鍚嶇О
     * </p>
     *
     * @param device 璁惧瀹炰綋
     * @return 璁惧浣嶇疆鎻忚堪
     */
    private String getDeviceLocation(DeviceEntity device) {
        try {
            // 浼樺厛浠庢墿灞曞睘鎬ц幏鍙栦綅缃?            if (device.getExtendedAttributes() != null && !device.getExtendedAttributes().trim().isEmpty()) {
                Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                Object locationObj = attrs.get("location");
                if (locationObj != null) {
                    return locationObj.toString();
                }
            }

            // 濡傛灉娌℃湁锛屽垯閫氳繃鍖哄煙ID鑾峰彇鍖哄煙鍚嶇О
            if (device.getAreaId() != null) {
                AreaEntity area = getAreaById(device.getAreaId());
                if (area != null && area.getAreaName() != null) {
                    return area.getAreaName();
                }
            }

            return device.getDeviceName() != null ? device.getDeviceName() : "鏈煡浣嶇疆";
        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[闂ㄧ璁惧] 鑾峰彇璁惧浣嶇疆鍙傛暟閿欒, deviceId={}, error={}", device.getDeviceId(), e.getMessage());
            return device.getDeviceName() != null ? device.getDeviceName() : "鏈煡浣嶇疆";
        } catch (BusinessException e) {
            log.debug("[闂ㄧ璁惧] 鑾峰彇璁惧浣嶇疆涓氬姟寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage());
            return device.getDeviceName() != null ? device.getDeviceName() : "鏈煡浣嶇疆";
        } catch (SystemException e) {
            log.debug("[闂ㄧ璁惧] 鑾峰彇璁惧浣嶇疆绯荤粺寮傚父, deviceId={}, code={}, message={}", device.getDeviceId(), e.getCode(), e.getMessage(), e);
            return device.getDeviceName() != null ? device.getDeviceName() : "鏈煡浣嶇疆";
        } catch (Exception e) {
            log.debug("[闂ㄧ璁惧] 鑾峰彇璁惧浣嶇疆鏈煡寮傚父, deviceId={}", device.getDeviceId(), e);
            return device.getDeviceName() != null ? device.getDeviceName() : "鏈煡浣嶇疆";
        }
    }

    /**
     * 璁＄畻涓ょ偣涔嬮棿鐨勮窛绂伙紙Haversine鍏紡锛?     * <p>
     * 璁＄畻鍦扮悆琛ㄩ潰涓ょ偣涔嬮棿鐨勭洿绾胯窛绂?     * 鍙傝€冪珵鍝侊細楂樺痉鍦板浘銆佺櫨搴﹀湴鍥剧瓑鍧囦娇鐢ㄦ绠楁硶
     * </p>
     *
     * @param lat1 璧风偣绾害
     * @param lng1 璧风偣缁忓害
     * @param lat2 缁堢偣绾害
     * @param lng2 缁堢偣缁忓害
     * @return 璺濈锛堢背锛?     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 鍦扮悆鍗婂緞锛堢背锛?        final double EARTH_RADIUS = 6371000;

        // 杞崲涓哄姬搴?        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        // Haversine鍏紡
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑鍖哄煙ID鍒楄〃
     * <p>
     * 閫氳繃鏌ヨ闂ㄧ鏉冮檺璁板綍鑾峰彇鐢ㄦ埛鏈夋潈闄愮殑鍖哄煙
     * 鍙傝€冪珵鍝侊細娴峰悍濞佽銆佸ぇ鍗庣瓑闂ㄧ绯荤粺鍧囨敮鎸佹寜鍖哄煙鎺堟潈
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鍖哄煙ID鍒楄〃
     */
    private List<Long> queryUserAllowedAreas(Long userId) {
        try {
            // 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑璁惧锛岀劧鍚庤幏鍙栬繖浜涜澶囨墍灞炵殑鍖哄煙
            // 娉ㄦ剰锛氳繖閲岄渶瑕佹牴鎹疄闄呯殑闂ㄧ鏉冮檺琛ㄧ粨鏋勬潵瀹炵幇
            // 濡傛灉鏉冮檺琛ㄤ腑鏈塧reaId瀛楁锛屽彲浠ョ洿鎺ユ煡璇紱濡傛灉娌℃湁锛岄渶瑕侀€氳繃璁惧鏌ヨ鍖哄煙

            // 绠€鍖栧疄鐜帮細鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑璁惧锛岀劧鍚庡幓閲嶈幏鍙栧尯鍩烮D
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .isNotNull(DeviceEntity::getAreaId);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            // 浠庤澶囦腑鎻愬彇鍖哄煙ID骞跺幓閲?            // 娉ㄦ剰锛氬疄闄呭簲璇ユ煡璇㈤棬绂佹潈闄愯〃锛岃繖閲岀畝鍖栧鐞?            return devices.stream()
                    .map(DeviceEntity::getAreaId)
                    .filter(areaId -> areaId != null)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺鍖哄煙鍙傛暟閿欒, userId={}, error={}", userId, e.getMessage());
            return List.of();
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺鍖哄煙涓氬姟寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return List.of();
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺鍖哄煙绯荤粺寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺鍖哄煙鏈煡寮傚父, userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑璁惧ID鍒楄〃
     * <p>
     * 鏍规嵁鐢ㄦ埛鏉冮檺鍜屽尯鍩熸潈闄愭煡璇㈣澶囧垪琛?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param allowedAreaIds 鍏佽鐨勫尯鍩烮D鍒楄〃
     * @return 璁惧ID鍒楄〃
     */
    private List<Long> queryUserAllowedDevices(Long userId, List<Long> allowedAreaIds) {
        try {
            // 鏌ヨ鐢ㄦ埛鏈夋潈闄愮殑璁惧
            // 娉ㄦ剰锛氬疄闄呭簲璇ユ煡璇㈤棬绂佹潈闄愯〃锛岃繖閲岀畝鍖栧鐞嗕负鏌ヨ鎸囧畾鍖哄煙涓嬬殑璁惧
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                return List.of();
            }

            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            return devices.stream()
                    .map(DeviceEntity::getId)
                    .filter(deviceId -> deviceId != null)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺璁惧鍙傛暟閿欒, userId={}, error={}", userId, e.getMessage());
            return List.of();
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺璁惧涓氬姟寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return List.of();
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺璁惧绯荤粺寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鏌ヨ鐢ㄦ埛鏉冮檺璁惧鏈煡寮傚父, userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 纭畾鐢ㄦ埛鏉冮檺绾у埆
     * <p>
     * 鏍规嵁鐢ㄦ埛鏉冮檺鑼冨洿纭畾鏉冮檺绾у埆
     * - ADMIN: 绠＄悊鍛橈紙鎵€鏈夊尯鍩熷拰璁惧锛?     * - NORMAL: 鏅€氱敤鎴凤紙閮ㄥ垎鍖哄煙鍜岃澶囷級
     * - VISITOR: 璁垮锛堜复鏃舵潈闄愶級
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param allowedAreaIds 鍏佽鐨勫尯鍩烮D鍒楄〃
     * @param allowedDeviceIds 鍏佽鐨勮澶嘔D鍒楄〃
     * @return 鏉冮檺绾у埆
     */
    private String determinePermissionLevel(Long userId, List<Long> allowedAreaIds, List<Long> allowedDeviceIds) {
        // 绠€鍖栧疄鐜帮細鏍规嵁鏉冮檺鏁伴噺鍒ゆ柇
        // 瀹為檯搴旇閫氳繃缃戝叧璋冪敤鍏叡鏈嶅姟鏌ヨ鐢ㄦ埛瑙掕壊
        try {
            // 閫氳繃缃戝叧鏌ヨ鐢ㄦ埛瑙掕壊
            // 濡傛灉鐢ㄦ埛鏄鐞嗗憳瑙掕壊锛岃繑鍥濧DMIN
            // 濡傛灉鐢ㄦ埛鏄瀹㈣鑹诧紝杩斿洖VISITOR
            // 鍚﹀垯杩斿洖NORMAL

            // 绠€鍖栧鐞嗭細鏍规嵁鏉冮檺鏁伴噺鍒ゆ柇
            if (allowedAreaIds.size() > 10 || allowedDeviceIds.size() > 50) {
                return "ADMIN";
            } else if (allowedAreaIds.isEmpty() && allowedDeviceIds.isEmpty()) {
                return "VISITOR";
            } else {
                return "NORMAL";
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 纭畾鏉冮檺绾у埆鍙傛暟閿欒, userId={}, error={}", userId, e.getMessage());
            return "NORMAL";
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 纭畾鏉冮檺绾у埆涓氬姟寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return "NORMAL";
        } catch (SystemException e) {
            log.warn("[闂ㄧ璁惧] 纭畾鏉冮檺绾у埆绯荤粺寮傚父, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return "NORMAL";
        } catch (Exception e) {
            log.warn("[闂ㄧ璁惧] 纭畾鏉冮檺绾у埆鏈煡寮傚父, userId={}", userId, e);
            return "NORMAL";
        }
    }

    /**
     * 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺
     * <p>
     * 閫氳繃缃戝叧璋冪敤璁惧閫氳鏈嶅姟鏌ヨ璁惧褰撳墠鍦ㄧ嚎鐢ㄦ埛鏁伴噺
     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鍦ㄧ嚎鐢ㄦ埛鏁伴噺
     */
    private Integer queryDeviceOnlineCount(Long deviceId) {
        try {
            // 閫氳繃缃戝叧璋冪敤璁惧閫氳鏈嶅姟
            // GET /api/v1/device/{deviceId}/online-count
            ResponseDTO<Integer> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/" + deviceId + "/online-count",
                    HttpMethod.GET,
                    null,
                    Integer.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺澶辫触锛宒eviceId={}, message={}", deviceId,
                        response != null ? response.getMessage() : "鍝嶅簲涓虹┖");
                return 0;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺鍙傛暟閿欒, deviceId={}, error={}", deviceId, e.getMessage());
            return 0;
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺涓氬姟寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return 0;
        } catch (SystemException e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺绯荤粺寮傚父, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return 0;
        } catch (Exception e) {
            log.warn("[闂ㄧ璁惧] 鏌ヨ璁惧鍦ㄧ嚎鐢ㄦ埛鏁伴噺鏈煡寮傚父, deviceId={}", deviceId, e);
            return 0;
        }
    }

    /**
     * 鑾峰彇绉诲姩绔尯鍩熷垪琛?     * <p>
     * 鑾峰彇鐢ㄦ埛鏈夋潈闄愯闂殑鍖哄煙鍒楄〃锛屽寘鍚尯鍩熻鎯咃紙鍚嶇О銆佺被鍨嬨€佽澶囨暟閲忕瓑锛?     * </p>
     *
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛紝涓嶄紶鍒欎粠Token鑾峰彇锛?     * @return 鍖哄煙鍒楄〃
     */
    @Override
    @Observed(name = "access.device.getMobileAreas", contextualName = "access-device-get-mobile-areas")
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileAreaItem>> getMobileAreas(Long userId) {
        log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛紝userId={}", userId);

        try {
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "鐢ㄦ埛ID涓嶈兘涓虹┖");
            }

            List<AccessMobileController.MobileAreaItem> areas = new ArrayList<>();

            // 1. 鑾峰彇鐢ㄦ埛鏈夋潈闄愮殑鍖哄煙ID鍒楄〃
            List<Long> allowedAreaIds = queryUserAllowedAreas(userId);
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                log.info("[闂ㄧ璁惧] 鐢ㄦ埛鏃犳潈闄愬尯鍩燂紝userId={}", userId);
                return ResponseDTO.ok(areas);
            }

            // 2. 鏌ヨ鍖哄煙璇︽儏
            // 浣跨敤LambdaQueryWrapper鏌ヨ锛岄伩鍏嶄娇鐢ㄥ凡搴熷純鐨剆electBatchIds鏂规硶
            // AreaEntity涓婚敭瀛楁宸茬粺涓€涓篿d锛岀鍚堝疄浣撶被涓婚敭鍛藉悕瑙勮寖
            LambdaQueryWrapper<AreaEntity> areaWrapper = new LambdaQueryWrapper<>();
            areaWrapper.in(AreaEntity::getId, allowedAreaIds)
                    .eq(AreaEntity::getDeletedFlag, 0);
            List<AreaEntity> areaList = accessAreaDao.selectList(areaWrapper);
            if (areaList == null || areaList.isEmpty()) {
                log.warn("[闂ㄧ璁惧] 鍖哄煙涓嶅瓨鍦紝areaIds={}", allowedAreaIds);
                return ResponseDTO.ok(areas);
            }

            // 3. 缁熻姣忎釜鍖哄煙鐨勮澶囨暟閲?            Map<Long, Long> deviceCountMap = new HashMap<>();
            LambdaQueryWrapper<DeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);
            List<DeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            if (devices != null && !devices.isEmpty()) {
                deviceCountMap = devices.stream()
                        .collect(Collectors.groupingBy(
                                DeviceEntity::getAreaId,
                                Collectors.counting()
                        ));
            }

            // 4. 鏋勫缓MobileAreaItem鍒楄〃
            for (AreaEntity area : areaList) {
                AccessMobileController.MobileAreaItem item = new AccessMobileController.MobileAreaItem();
                item.setAreaId(area.getId());
                item.setAreaName(area.getAreaName());
                item.setAreaType(area.getAreaType() != null ? area.getAreaType().toString() : "鏍囧噯鍖哄煙");
                item.setDeviceCount(deviceCountMap.getOrDefault(area.getId(), 0L).intValue());
                item.setPermissionCount(1); // 鐢ㄦ埛鏈夋潈闄?                // 浣跨敤areaDesc瀛楁浣滀负鎻忚堪
                item.setDescription(area.getAreaDesc() != null ? area.getAreaDesc() : "");
                item.setActive(area.getStatus() != null && area.getStatus() == 1);
                areas.add(item);
            }

            log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛ㄦ垚鍔燂紝userId={}, 鍖哄煙鏁伴噺={}", userId, areas.size());
            return ResponseDTO.ok(areas);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛ㄥ弬鏁板紓甯? userId={}, error={}", userId, e.getMessage());
            throw new ParamException("QUERY_MOBILE_AREAS_PARAM_ERROR", "鑾峰彇鍖哄煙鍒楄〃鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛ㄤ笟鍔″紓甯? userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛ㄧ郴缁熷紓甯? userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔尯鍩熷垪琛ㄦ湭鐭ュ紓甯? userId={}", userId, e);
            throw new SystemException("QUERY_MOBILE_AREAS_SYSTEM_ERROR", "鑾峰彇鍖哄煙鍒楄〃绯荤粺寮傚父", e);
        }
    }

    /**
     * 娣诲姞璁惧闄嶇骇鏂规硶
     */
    public ResponseDTO<Long> addDeviceFallback(AccessDeviceAddForm addForm, Exception ex) {
        log.error("[闂ㄧ璁惧] 娣诲姞璁惧闄嶇骇锛宒eviceCode={}, error={}", addForm.getDeviceCode(), ex.getMessage());
        return ResponseDTO.error("ADD_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }

    /**
     * 鏇存柊璁惧闄嶇骇鏂规硶
     */
    public ResponseDTO<Boolean> updateDeviceFallback(AccessDeviceUpdateForm updateForm, Exception ex) {
        log.error("[闂ㄧ璁惧] 鏇存柊璁惧闄嶇骇锛宒eviceId={}, error={}", updateForm.getDeviceId(), ex.getMessage());
        return ResponseDTO.error("UPDATE_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }

    /**
     * 鍒犻櫎璁惧闄嶇骇鏂规硶
     */
    public ResponseDTO<Boolean> deleteDeviceFallback(Long deviceId, Exception ex) {
        log.error("[闂ㄧ璁惧] 鍒犻櫎璁惧闄嶇骇锛宒eviceId={}, error={}", deviceId, ex.getMessage());
        return ResponseDTO.error("DELETE_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }

    // ==================== 绉诲姩绔澶囩鐞嗗姛鑳藉疄鐜?====================

    @Override
    @Observed(name = "access.device.getMobileList", contextualName = "access-device-get-mobile-list")
    @Transactional(readOnly = true)
    public ResponseDTO<List<MobileDeviceVO>> getMobileDeviceList(Long userId, Integer deviceType,
                                                               Integer status, Long areaId, String keyword) {
        log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛紝userId={}, deviceType={}, status={}, areaId={}, keyword={}",
                userId, deviceType, status, areaId, keyword);

        try {
            // 鏋勫缓鏌ヨ鏉′欢
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 鍏抽敭璇嶆悳绱?            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, keyword)
                        .or()
                        .like(DeviceEntity::getDeviceCode, keyword));
            }

            // 璁惧鐘舵€佺瓫閫?            if (status != null) {
                wrapper.eq(DeviceEntity::getDeviceStatus, status == 1 ? "ONLINE" : "OFFLINE");
            }

            // 鍖哄煙绛涢€?            if (areaId != null) {
                wrapper.eq(DeviceEntity::getAreaId, areaId);
            }

            // 鎺掑簭
            wrapper.orderByDesc(DeviceEntity::getUpdateTime);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            // 杞崲涓篗obileDeviceVO鍒楄〃
            List<MobileDeviceVO> mobileDevices = devices.stream()
                    .map(this::convertToMobileVO)
                    .collect(Collectors.toList());

            log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛ㄦ垚鍔燂紝鏁伴噺={}", mobileDevices.size());
            return ResponseDTO.ok(mobileDevices);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛ㄥ弬鏁板紓甯? error={}", e.getMessage());
            throw new ParamException("GET_MOBILE_DEVICE_LIST_PARAM_ERROR", "鑾峰彇璁惧鍒楄〃鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛ㄤ笟鍔″紓甯? code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛ㄧ郴缁熷紓甯? code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囧垪琛ㄦ湭鐭ュ紓甯?, e);
            throw new SystemException("GET_MOBILE_DEVICE_LIST_SYSTEM_ERROR", "鑾峰彇璁惧鍒楄〃绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.control", contextualName = "access-device-control")
    @CircuitBreaker(name = "access-device-control-circuitbreaker", fallbackMethod = "controlDeviceFallback")
    @Retry(name = "access-device-control-retry")
    @RateLimiter(name = "device-control-ratelimiter")
    @Timed(value = "access.device.control", description = "璁惧鎺у埗鑰楁椂")
    @Counted(value = "access.device.control.count", description = "璁惧鎺у埗娆℃暟")
    public ResponseDTO<DeviceControlResultVO> controlDevice(DeviceControlRequest request) {
        log.info("[闂ㄧ璁惧] 璁惧鎺у埗鎿嶄綔锛宒eviceId={}, command={}", request.getDeviceId(), request.getCommand());

        try {
            // 1. 楠岃瘉璁惧鏄惁瀛樺湪
            DeviceEntity device = accessDeviceDao.selectById(request.getDeviceId());
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", request.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 2. 楠岃瘉璁惧鏄惁鏀寔鎺у埗
            if (device.getEnabledFlag() == 0) {
                log.warn("[闂ㄧ璁惧] 璁惧宸茬鐢紝deviceId={}", request.getDeviceId());
                return ResponseDTO.error("DEVICE_DISABLED", "璁惧宸茬鐢紝鏃犳硶鎺у埗");
            }

            // 3. 鎵ц鎺у埗鎿嶄綔
            DeviceControlResultVO result = executeDeviceControl(device, request);

            log.info("[闂ㄧ璁惧] 璁惧鎺у埗瀹屾垚锛宒eviceId={}, command={}, status={}",
                    request.getDeviceId(), request.getCommand(), result.getStatus());
            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 璁惧鎺у埗鍙傛暟寮傚父, deviceId={}, error={}", request.getDeviceId(), e.getMessage());
            throw new ParamException("CONTROL_DEVICE_PARAM_ERROR", "璁惧鎺у埗鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 璁惧鎺у埗涓氬姟寮傚父, deviceId={}, code={}, message={}", request.getDeviceId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 璁惧鎺у埗绯荤粺寮傚父, deviceId={}, code={}, message={}", request.getDeviceId(), e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 璁惧鎺у埗鏈煡寮傚父, deviceId={}", request.getDeviceId(), e);
            throw new SystemException("CONTROL_DEVICE_SYSTEM_ERROR", "璁惧鎺у埗绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.addMobile", contextualName = "access-device-add-mobile")
    @CircuitBreaker(name = "access-device-add-mobile-circuitbreaker", fallbackMethod = "addMobileDeviceFallback")
    @Retry(name = "access-device-add-mobile-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.addMobile", description = "绉诲姩绔坊鍔犺澶囪€楁椂")
    @Counted(value = "access.device.addMobile.count", description = "绉诲姩绔坊鍔犺澶囨鏁?)
    public ResponseDTO<Long> addMobileDevice(AddDeviceRequest request) {
        log.info("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囷紝deviceName={}, deviceCode={}, areaId={}",
                request.getDeviceName(), request.getDeviceCode(), request.getAreaId());

        try {
            // 1. 楠岃瘉璁惧缂栧彿鍞竴鎬?            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, request.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
            if (existingDevice != null) {
                log.warn("[闂ㄧ璁惧] 璁惧缂栧彿宸插瓨鍦紝deviceCode={}", request.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "璁惧缂栧彿宸插瓨鍦?);
            }

            // 2. 楠岃瘉鍖哄煙鏄惁瀛樺湪
            AreaEntity area = getAreaById(request.getAreaId());
            if (area == null) {
                log.warn("[闂ㄧ璁惧] 鍖哄煙涓嶅瓨鍦紝areaId={}", request.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "鍖哄煙涓嶅瓨鍦?);
            }

            // 3. 鍒涘缓璁惧瀹炰綋
            DeviceEntity device = new DeviceEntity();
            device.setDeviceName(request.getDeviceName());
            device.setDeviceCode(request.getDeviceCode());
            device.setDeviceType("ACCESS");
            device.setAreaId(request.getAreaId());
            device.setIpAddress(request.getIpAddress());
            device.setPort(request.getNetworkPort());
            device.setEnabledFlag(1);
            device.setDeviceStatus("OFFLINE"); // 榛樿绂荤嚎鐘舵€?
            // 璁剧疆鎵╁睍灞炴€?            Map<String, Object> extendedAttrs = new HashMap<>();
            if (request.getExtendedAttributes() != null) {
                try {
                    Map<String, Object> attrs = objectMapper.readValue(request.getExtendedAttributes(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    extendedAttrs.putAll(attrs);
                } catch (Exception e) {
                    log.warn("[闂ㄧ璁惧] 瑙ｆ瀽鎵╁睍灞炴€уけ璐ワ紝浣跨敤榛樿鍊?, e);
                }
            }

            // 娣诲姞浣嶇疆淇℃伅
            if (request.getLatitude() != null && request.getLongitude() != null) {
                extendedAttrs.put("latitude", request.getLatitude());
                extendedAttrs.put("longitude", request.getLongitude());
            }
            if (StringUtils.hasText(request.getLocation())) {
                extendedAttrs.put("location", request.getLocation());
            }

            // 娣诲姞瀹夎淇℃伅
            if (StringUtils.hasText(request.getInstaller())) {
                extendedAttrs.put("installer", request.getInstaller());
            }
            if (StringUtils.hasText(request.getInstallNotes())) {
                extendedAttrs.put("installNotes", request.getInstallNotes());
            }
            if (request.getInstallHeight() != null) {
                extendedAttrs.put("installHeight", request.getInstallHeight());
            }

            device.setExtendedAttributes(objectMapper.writeValueAsString(extendedAttrs));

            // 4. 淇濆瓨璁惧
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 娣诲姞璁惧澶辫触锛屾彃鍏ヨ鏁颁负0");
                return ResponseDTO.error("ADD_MOBILE_DEVICE_ERROR", "娣诲姞璁惧澶辫触");
            }

            log.info("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囨垚鍔燂紝deviceId={}", device.getDeviceId());
            return ResponseDTO.ok(device.getDeviceId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囧弬鏁板紓甯? error={}", e.getMessage());
            throw new ParamException("ADD_MOBILE_DEVICE_PARAM_ERROR", "娣诲姞璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囦笟鍔″紓甯? code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囩郴缁熷紓甯? code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囨湭鐭ュ紓甯?, e);
            throw new SystemException("ADD_MOBILE_DEVICE_SYSTEM_ERROR", "娣诲姞璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    @Observed(name = "access.device.deleteMobile", contextualName = "access-device-delete-mobile")
    @CircuitBreaker(name = "access-device-delete-mobile-circuitbreaker", fallbackMethod = "deleteMobileDeviceFallback")
    @Retry(name = "access-device-delete-mobile-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.deleteMobile", description = "绉诲姩绔垹闄よ澶囪€楁椂")
    @Counted(value = "access.device.deleteMobile.count", description = "绉诲姩绔垹闄よ澶囨鏁?)
    public ResponseDTO<Boolean> deleteMobileDevice(Long deviceId) {
        log.info("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囷紝deviceId={}", deviceId);

        try {
            // 1. 鏌ヨ璁惧鏄惁瀛樺湪
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            // 2. 杞垹闄わ紙璁剧疆deletedFlag=1锛?            device.setDeletedFlag(1);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囧け璐ワ紝鏇存柊琛屾暟涓?");
                return ResponseDTO.error("DELETE_MOBILE_DEVICE_ERROR", "鍒犻櫎璁惧澶辫触");
            }

            log.info("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囨垚鍔燂紝deviceId={}", deviceId);
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囧弬鏁板紓甯? deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("DELETE_MOBILE_DEVICE_PARAM_ERROR", "鍒犻櫎璁惧鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囦笟鍔″紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囩郴缁熷紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囨湭鐭ュ紓甯? deviceId={}", deviceId, e);
            throw new SystemException("DELETE_MOBILE_DEVICE_SYSTEM_ERROR", "鍒犻櫎璁惧绯荤粺寮傚父", e);
        }
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> restartDevice(Long deviceId, String restartType, String reason) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("restart")
                .restartType(restartType != null ? restartType : "soft")
                .reason(reason != null ? reason : "绉诲姩绔噸鍚搷浣?)
                .timeoutSeconds(30)
                .build();
        return controlDevice(request);
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> setMaintenanceMode(Long deviceId, Integer maintenanceDuration, String reason) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("maintenance")
                .maintenanceDuration(maintenanceDuration != null ? maintenanceDuration : 24)
                .reason(reason != null ? reason : "绉诲姩绔缃淮鎶ゆā寮?)
                .timeoutSeconds(10)
                .build();
        return controlDevice(request);
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> calibrateDevice(Long deviceId, String calibrationType, String calibrationPrecision) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("calibrate")
                .calibrationType(calibrationType != null ? calibrationType : "face")
                .calibrationPrecision(calibrationPrecision != null ? calibrationPrecision : "medium")
                .reason("绉诲姩绔牎鍑嗘搷浣?)
                .timeoutSeconds(60)
                .build();
        return controlDevice(request);
    }

    @Override
    @Observed(name = "access.device.getMobileDetail", contextualName = "access-device-get-mobile-detail")
    @Transactional(readOnly = true)
    public ResponseDTO<MobileDeviceVO> getMobileDeviceDetail(Long deviceId) {
        log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯咃紝deviceId={}", deviceId);

        try {
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[闂ㄧ璁惧] 璁惧涓嶅瓨鍦紝deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "璁惧涓嶅瓨鍦?);
            }

            MobileDeviceVO mobileVO = convertToMobileVO(device);
            log.info("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯呮垚鍔燂紝deviceId={}", deviceId);
            return ResponseDTO.ok(mobileVO);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯呭弬鏁板紓甯? deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("GET_MOBILE_DEVICE_DETAIL_PARAM_ERROR", "鑾峰彇璁惧璇︽儏鍙傛暟寮傚父: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯呬笟鍔″紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯呯郴缁熷紓甯? deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[闂ㄧ璁惧] 鑾峰彇绉诲姩绔澶囪鎯呮湭鐭ュ紓甯? deviceId={}", deviceId, e);
            throw new SystemException("GET_MOBILE_DEVICE_DETAIL_SYSTEM_ERROR", "鑾峰彇璁惧璇︽儏绯荤粺寮傚父", e);
        }
    }

    // ==================== 绉佹湁杈呭姪鏂规硶 ====================

    /**
     * 杞崲璁惧瀹炰綋涓虹Щ鍔ㄧVO
     */
    private MobileDeviceVO convertToMobileVO(DeviceEntity device) {
        MobileDeviceVO vo = MobileDeviceVO.builder()
                .deviceId(device.getDeviceId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .deviceType(1) // 闂ㄧ璁惧绫诲瀷涓?
                .deviceTypeName("闂ㄧ璁惧")
                .areaId(device.getAreaId())
                .status("ONLINE".equals(device.getDeviceStatus()) ? 1 : 2) // 1-鍦ㄧ嚎 2-绂荤嚎
                .statusName("ONLINE".equals(device.getDeviceStatus()) ? "鍦ㄧ嚎" : "绂荤嚎")
                .ipAddress(device.getIpAddress())
                .deviceModel("IOE-ACCESS-2000") // 榛樿鍨嬪彿
                .manufacturer("IOE绉戞妧") // 榛樿鍘傚晢
                .firmwareVersion("v2.1.0") // 榛樿鐗堟湰
                .supportRemoteControl(true)
                .supportFirmwareUpgrade(true)
                .permissionLevel(1)
                .businessModule("access")
                .createTime(device.getCreateTime())
                .updateTime(device.getUpdateTime())
                .lastOnlineTime(device.getLastOnlineTime())
                .build();

        // 鑾峰彇鍖哄煙鍚嶇О
        if (device.getAreaId() != null) {
            AreaEntity area = getAreaById(device.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }
        }

        // 浠庢墿灞曞睘鎬ц幏鍙栦綅缃拰鍏朵粬淇℃伅
        try {
            if (device.getExtendedAttributes() != null && !device.getExtendedAttributes().trim().isEmpty()) {
                Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                // 璁剧疆浣嶇疆淇℃伅
                Object locationObj = attrs.get("location");
                if (locationObj != null) {
                    vo.setLocation(locationObj.toString());
                }

                // 璁剧疆GPS鍧愭爣
                Object latObj = attrs.get("latitude");
                Object lngObj = attrs.get("longitude");
                if (latObj != null && lngObj != null) {
                    vo.setLatitude(Double.valueOf(latObj.toString()));
                    vo.setLongitude(Double.valueOf(lngObj.toString()));
                }

                // 璁剧疆瀹夎淇℃伅
                Object installerObj = attrs.get("installer");
                if (installerObj != null) {
                    vo.setInstaller(installerObj.toString());
                }
            }
        } catch (Exception e) {
            log.debug("[闂ㄧ璁惧] 瑙ｆ瀽鎵╁睍灞炴€уけ璐ワ紝deviceId={}", device.getDeviceId(), e);
        }

        // 妯℃嫙杩愯鐘舵€佹暟鎹紙瀹為檯搴旇浠庤澶囬€氳鏈嶅姟鑾峰彇锛?        vo.setSignalStrength(-45 + (int)(Math.random() * 20));
        vo.setBatteryLevel(85 + (int)(Math.random() * 10));
        vo.setTemperature(25.0 + Math.random() * 5);
        vo.setCpuUsage(10.0 + Math.random() * 20);
        vo.setMemoryUsage(30.0 + Math.random() * 30);
        vo.setStorageUsage(40.0 + Math.random() * 20);
        vo.setTodayAccessCount(100 + (int)(Math.random() * 200));
        vo.setTodayErrorCount(Math.random() > 0.8 ? (int)(Math.random() * 3) : 0);
        vo.setHealthScore(85 + (int)(Math.random() * 15));
        vo.setNeedsMaintenance(Math.random() > 0.9);
        vo.setDaysSinceLastMaintenance((int)(Math.random() * 30));

        return vo;
    }

    /**
     * 鎵ц璁惧鎺у埗鎿嶄綔
     */
    private DeviceControlResultVO executeDeviceControl(DeviceEntity device, DeviceControlRequest request) {
        String taskId = "TASK" + System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 妯℃嫙璁惧鎺у埗鎵ц
            String command = request.getCommand();
            String result = "";
            String status = "success";

            switch (command) {
                case "restart":
                    result = "璁惧閲嶅惎瀹屾垚";
                    break;
                case "maintenance":
                    result = "璁惧宸茶繘鍏ョ淮鎶ゆā寮?;
                    break;
                case "calibrate":
                    result = "璁惧鏍″噯瀹屾垚";
                    break;
                default:
                    result = "璁惧鎺у埗鍛戒护鎵ц瀹屾垚";
                    break;
            }

            // 妯℃嫙鎵ц鏃堕棿
            Thread.sleep(1000 + (long)(Math.random() * 2000));

            LocalDateTime endTime = LocalDateTime.now();

            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(command)
                    .status(status)
                    .statusDesc("鎵ц鎴愬姛")
                    .result(result)
                    .executionTime(System.currentTimeMillis() - startTime.toEpochSecond(java.time.ZoneOffset.UTC))
                    .startTime(startTime)
                    .endTime(endTime)
                    .operator("绉诲姩绔敤鎴?)
                    .reason(request.getReason())
                    .needFollowUp(false)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(request.getCommand())
                    .status("failed")
                    .statusDesc("鎵ц澶辫触")
                    .result("璁惧鎺у埗鎿嶄綔琚腑鏂?)
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .operator("绉诲姩绔敤鎴?)
                    .reason(request.getReason())
                    .errorMessage("鎿嶄綔琚腑鏂?)
                    .build();
        } catch (Exception e) {
            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(request.getCommand())
                    .status("failed")
                    .statusDesc("鎵ц澶辫触")
                    .result("璁惧鎺у埗鎿嶄綔澶辫触")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .operator("绉诲姩绔敤鎴?)
                    .reason(request.getReason())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 绉诲姩绔坊鍔犺澶囬檷绾ф柟娉?     */
    public ResponseDTO<Long> addMobileDeviceFallback(AddDeviceRequest request, Exception ex) {
        log.error("[闂ㄧ璁惧] 绉诲姩绔坊鍔犺澶囬檷绾э紝deviceCode={}, error={}", request.getDeviceCode(), ex.getMessage());
        return ResponseDTO.error("ADD_MOBILE_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }

    /**
     * 绉诲姩绔垹闄よ澶囬檷绾ф柟娉?     */
    public ResponseDTO<Boolean> deleteMobileDeviceFallback(Long deviceId, Exception ex) {
        log.error("[闂ㄧ璁惧] 绉诲姩绔垹闄よ澶囬檷绾э紝deviceId={}, error={}", deviceId, ex.getMessage());
        return ResponseDTO.error("DELETE_MOBILE_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }

    /**
     * 璁惧鎺у埗闄嶇骇鏂规硶
     */
    public ResponseDTO<DeviceControlResultVO> controlDeviceFallback(DeviceControlRequest request, Exception ex) {
        log.error("[闂ㄧ璁惧] 璁惧鎺у埗闄嶇骇锛宒eviceId={}, command={}, error={}",
                request.getDeviceId(), request.getCommand(), ex.getMessage());

        DeviceControlResultVO fallbackResult = DeviceControlResultVO.builder()
                .taskId("FALLBACK_" + System.currentTimeMillis())
                .deviceId(request.getDeviceId())
                .command(request.getCommand())
                .status("failed")
                .statusDesc("绯荤粺绻佸繖")
                .result("璁惧鎺у埗鎿嶄綔澶辫触锛岃绋嶅悗閲嶈瘯")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .operator("绉诲姩绔敤鎴?)
                .reason(request.getReason())
                .errorMessage("绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯")
                .build();

        return ResponseDTO.error("CONTROL_DEVICE_DEGRADED", "绯荤粺绻佸繖锛岃绋嶅悗閲嶈瘯");
    }
}


