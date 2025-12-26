package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.access.domain.entity.DeviceFirmwareEntity;
import net.lab1024.sa.access.domain.form.FirmwareQueryForm;
import net.lab1024.sa.access.domain.form.FirmwareUploadForm;
import net.lab1024.sa.access.domain.vo.FirmwareDetailVO;
import net.lab1024.sa.access.domain.vo.FirmwareVO;
import net.lab1024.sa.access.service.FirmwareService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 固件管理服务实现类
 * <p>
 * 核心功能：
 * - 固件文件上传和存储
 * - 固件版本管理
 * - 固件下载
 * - MD5校验和版本兼容性检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class FirmwareServiceImpl implements FirmwareService {

    @Resource
    private DeviceFirmwareDao deviceFirmwareDao;

    // 固件文件存储根路径
    private static final String FIRMWARE_STORAGE_PATH = "/data/firmware";

    // ==================== 核心方法实现 ====================

    /**
     * 上传固件文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFirmware(MultipartFile file, FirmwareUploadForm uploadForm,
                              Long operatorId, String operatorName) {
        log.info("[固件管理] 开始上传固件: fileName={}, version={}, operatorId={}",
                file.getOriginalFilename(), uploadForm.getFirmwareVersion(), operatorId);

        try {
            // 1. 验证文件
            validateFile(file);

            // 2. 计算文件MD5
            String fileMd5 = calculateFileMd5(file);
            log.info("[固件管理] 文件MD5计算完成: md5={}", fileMd5);

            // 3. 检查版本是否已存在
            LambdaQueryWrapper<DeviceFirmwareEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceFirmwareEntity::getDeviceType, uploadForm.getDeviceType());
            queryWrapper.eq(DeviceFirmwareEntity::getDeviceModel, uploadForm.getDeviceModel());
            queryWrapper.eq(DeviceFirmwareEntity::getFirmwareVersion, uploadForm.getFirmwareVersion());
            DeviceFirmwareEntity existingFirmware = deviceFirmwareDao.selectOne(queryWrapper);

            if (existingFirmware != null) {
                log.warn("[固件管理] 固件版本已存在: firmwareId={}", existingFirmware.getFirmwareId());
                throw new BusinessException("FIRMWARE_VERSION_EXISTS", "该固件版本已存在");
            }

            // 4. 保存文件到磁盘
            String relativePath = saveFileToDisk(file, uploadForm);
            log.info("[固件管理] 文件保存成功: path={}", relativePath);

            // 5. 创建固件记录
            DeviceFirmwareEntity firmware = new DeviceFirmwareEntity();
            BeanUtils.copyProperties(uploadForm, firmware);
            firmware.setFirmwareFilePath(relativePath);
            firmware.setFirmwareFileName(file.getOriginalFilename());
            firmware.setFirmwareFileSize(file.getSize());
            firmware.setFirmwareFileMd5(fileMd5);
            firmware.setUploadTime(LocalDateTime.now());
            firmware.setUploaderId(operatorId);
            firmware.setUploaderName(operatorName);
            firmware.setIsEnabled(1); // 默认启用
            firmware.setFirmwareStatus(1); // 默认测试中
            firmware.setDownloadCount(0);
            firmware.setUpgradeCount(0);

            deviceFirmwareDao.insert(firmware);
            Long firmwareId = firmware.getFirmwareId();

            log.info("[固件管理] 固件上传成功: firmwareId={}, version={}", firmwareId, firmware.getFirmwareVersion());
            return firmwareId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[固件管理] 固件上传失败: fileName={}, error={}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw new BusinessException("FIRMWARE_UPLOAD_ERROR", "固件上传失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询固件列表
     */
    @Override
    public PageResult<FirmwareVO> queryFirmwarePage(FirmwareQueryForm queryForm) {
        log.info("[固件管理] 分页查询固件列表: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<DeviceFirmwareEntity> queryWrapper = buildQueryWrapper(queryForm);

        // 分页查询
        Page<DeviceFirmwareEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<DeviceFirmwareEntity> pageResult = deviceFirmwareDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<FirmwareVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());

        log.info("[固件管理] 查询完成: total={}, records={}", pageResult.getTotal(), voList.size());

        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    /**
     * 查询固件详情
     */
    @Override
    public FirmwareDetailVO getFirmwareDetail(Long firmwareId) {
        log.info("[固件管理] 查询固件详情: firmwareId={}", firmwareId);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        FirmwareDetailVO detailVO = new FirmwareDetailVO();
        BeanUtils.copyProperties(firmware, detailVO);
        detailVO.setFirmwareFilePath(firmware.getFirmwareFilePath());
        detailVO.setFirmwareFileSizeFormatted(formatFileSize(firmware.getFirmwareFileSize()));

        return detailVO;
    }

    /**
     * 查询可用固件版本列表
     */
    @Override
    public List<FirmwareVO> queryAvailableFirmware(Integer deviceType, String deviceModel, String currentVersion) {
        log.info("[固件管理] 查询可用固件: deviceType={}, deviceModel={}, currentVersion={}",
                deviceType, deviceModel, currentVersion);

        List<DeviceFirmwareEntity> firmwareList = deviceFirmwareDao.selectAvailableFirmware(deviceType, deviceModel);

        // 过滤兼容版本
        List<FirmwareVO> availableFirmware = firmwareList.stream()
                .filter(firmware -> checkVersionCompatibility(firmware.getFirmwareId(), currentVersion))
                .map(this::convertToVO)
                .collect(java.util.stream.Collectors.toList());

        log.info("[固件管理] 可用固件数量: count={}", availableFirmware.size());
        return availableFirmware;
    }

    /**
     * 检查固件版本兼容性
     */
    @Override
    public Boolean checkVersionCompatibility(Long firmwareId, String currentVersion) {
        log.debug("[固件管理] 检查版本兼容性: firmwareId={}, currentVersion={}",
                firmwareId, currentVersion);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            return false;
        }

        // 如果没有指定当前版本，默认兼容
        if (currentVersion == null || currentVersion.isEmpty()) {
            return true;
        }

        // 检查最低版本
        if (firmware.getMinVersion() != null && !firmware.getMinVersion().isEmpty()) {
            if (compareVersion(currentVersion, firmware.getMinVersion()) < 0) {
                log.debug("[固件管理] 版本过低: current={}, min={}",
                        currentVersion, firmware.getMinVersion());
                return false;
            }
        }

        // 检查最高版本
        if (firmware.getMaxVersion() != null && !firmware.getMaxVersion().isEmpty()) {
            if (compareVersion(currentVersion, firmware.getMaxVersion()) >= 0) {
                log.debug("[固件管理] 版本过高: current={}, max={}",
                        currentVersion, firmware.getMaxVersion());
                return false;
            }
        }

        return true;
    }

    /**
     * 更新固件状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFirmwareStatus(Long firmwareId, Integer firmwareStatus) {
        log.info("[固件管理] 更新固件状态: firmwareId={}, status={}", firmwareId, firmwareStatus);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        firmware.setFirmwareStatus(firmwareStatus);
        int rows = deviceFirmwareDao.updateById(firmware);

        log.info("[固件管理] 固件状态更新成功: firmwareId={}, newStatus={}", firmwareId, firmwareStatus);
        return rows > 0;
    }

    /**
     * 启用/禁用固件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFirmwareEnabled(Long firmwareId, Integer isEnabled) {
        log.info("[固件管理] 更新固件启用状态: firmwareId={}, enabled={}", firmwareId, isEnabled);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        firmware.setIsEnabled(isEnabled);
        int rows = deviceFirmwareDao.updateById(firmware);

        log.info("[固件管理] 固件启用状态更新成功: firmwareId={}, enabled={}", firmwareId, isEnabled);
        return rows > 0;
    }

    /**
     * 删除固件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFirmware(Long firmwareId) {
        log.info("[固件管理] 删除固件: firmwareId={}", firmwareId);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        // TODO: 检查是否有关联的升级任务，如果有则禁止删除

        int rows = deviceFirmwareDao.deleteById(firmwareId);

        log.info("[固件管理] 固件删除成功: firmwareId={}", firmwareId);
        return rows > 0;
    }

    /**
     * 下载固件
     */
    @Override
    public FirmwareDetailVO downloadFirmware(Long firmwareId) {
        log.info("[固件管理] 下载固件: firmwareId={}", firmwareId);

        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        // 更新下载次数
        deviceFirmwareDao.incrementDownloadCount(firmwareId);

        FirmwareDetailVO detailVO = new FirmwareDetailVO();
        BeanUtils.copyProperties(firmware, detailVO);
        detailVO.setFirmwareFilePath(firmware.getFirmwareFilePath());

        log.info("[固件管理] 固件下载成功: firmwareId={}, downloadCount={}",
                firmwareId, firmware.getDownloadCount() + 1);

        return detailVO;
    }

    /**
     * 计算文件MD5值
     */
    @Override
    public String calculateFileMd5(MultipartFile file) {
        try {
            return DigestUtils.md5Hex(file.getInputStream());
        } catch (IOException e) {
            log.error("[固件管理] MD5计算失败: fileName={}", file.getOriginalFilename(), e);
            throw new BusinessException("MD5_CALC_ERROR", "MD5计算失败");
        }
    }

    /**
     * 验证文件MD5值
     */
    @Override
    public Boolean verifyFileMd5(MultipartFile file, String expectedMd5) {
        String actualMd5 = calculateFileMd5(file);
        boolean verified = actualMd5.equalsIgnoreCase(expectedMd5);

        if (!verified) {
            log.warn("[固件管理] MD5校验失败: expected={}, actual={}", expectedMd5, actualMd5);
        }

        return verified;
    }

    /**
     * 获取固件文件MD5值
     */
    @Override
    public String getFirmwareMd5(Long firmwareId) {
        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(firmwareId);
        return firmware != null ? firmware.getFirmwareFileMd5() : null;
    }

    /**
     * 获取固件统计信息
     */
    @Override
    public FirmwareDetailVO getFirmwareStatistics() {
        log.info("[固件管理] 获取固件统计信息");

        // TODO: 实现统计信息查询
        FirmwareDetailVO statistics = new FirmwareDetailVO();
        statistics.setTotalFirmwareCount(0);
        statistics.setTestingFirmwareCount(0);
        statistics.setReleasedFirmwareCount(0);
        statistics.setDeprecatedFirmwareCount(0);

        return statistics;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new BusinessException("FILE_NAME_INVALID", "文件名不能为空");
        }

        // 验证文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("bin") && !extension.equals("pkg") && !extension.equals("zip")) {
            throw new BusinessException("FILE_TYPE_INVALID", "只支持.bin/.pkg/.zip格式的固件文件");
        }

        // 验证文件大小（最大100MB）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("FILE_SIZE_EXCEEDED", "文件大小不能超过100MB");
        }
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFileToDisk(MultipartFile file, FirmwareUploadForm uploadForm) throws IOException {
        // 构建存储路径：/data/firmware/{deviceType}/{deviceModel}/{version}/
        String relativePath = String.format("/%d/%s/%s/",
                uploadForm.getDeviceType(),
                uploadForm.getDeviceModel() != null ? uploadForm.getDeviceModel() : "common",
                uploadForm.getFirmwareVersion());

        Path targetPath = Paths.get(FIRMWARE_STORAGE_PATH, relativePath);

        // 创建目录
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        // 保存文件
        String fileName = file.getOriginalFilename();
        Path filePath = targetPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return relativePath + fileName;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<DeviceFirmwareEntity> buildQueryWrapper(FirmwareQueryForm queryForm) {
        LambdaQueryWrapper<DeviceFirmwareEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(queryForm.getFirmwareName() != null,
                DeviceFirmwareEntity::getFirmwareName, queryForm.getFirmwareName())
                .eq(queryForm.getFirmwareVersion() != null,
                DeviceFirmwareEntity::getFirmwareVersion, queryForm.getFirmwareVersion())
                .eq(queryForm.getDeviceType() != null,
                DeviceFirmwareEntity::getDeviceType, queryForm.getDeviceType())
                .eq(queryForm.getDeviceModel() != null,
                DeviceFirmwareEntity::getDeviceModel, queryForm.getDeviceModel())
                .eq(queryForm.getBrand() != null,
                DeviceFirmwareEntity::getBrand, queryForm.getBrand())
                .eq(queryForm.getFirmwareStatus() != null,
                DeviceFirmwareEntity::getFirmwareStatus, queryForm.getFirmwareStatus())
                .eq(queryForm.getIsEnabled() != null,
                DeviceFirmwareEntity::getIsEnabled, queryForm.getIsEnabled())
                .orderByDesc(DeviceFirmwareEntity::getUploadTime);

        return queryWrapper;
    }

    /**
     * 转换为VO对象
     */
    private FirmwareVO convertToVO(DeviceFirmwareEntity entity) {
        FirmwareVO vo = new FirmwareVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setFirmwareFileSizeFormatted(formatFileSize(entity.getFirmwareFileSize()));
        vo.setDeviceTypeName(getDeviceTypeName(entity.getDeviceType()));
        vo.setFirmwareStatusName(getFirmwareStatusName(entity.getFirmwareStatus()));
        return vo;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    /**
     * 获取设备类型名称
     */
    private String getDeviceTypeName(Integer deviceType) {
        if (deviceType == null) {
            return "未知";
        }

        Map<Integer, String> typeNames = new HashMap<>();
        typeNames.put(1, "门禁设备");
        typeNames.put(2, "考勤设备");
        typeNames.put(3, "消费设备");
        typeNames.put(4, "视频设备");
        typeNames.put(5, "访客设备");

        return typeNames.getOrDefault(deviceType, "未知");
    }

    /**
     * 获取固件状态名称
     */
    private String getFirmwareStatusName(Integer firmwareStatus) {
        if (firmwareStatus == null) {
            return "未知";
        }

        Map<Integer, String> statusNames = new HashMap<>();
        statusNames.put(1, "测试中");
        statusNames.put(2, "正式发布");
        statusNames.put(3, "已废弃");

        return statusNames.getOrDefault(firmwareStatus, "未知");
    }

    /**
     * 版本号比较
     * <p>
     * 返回值：
     * - 负数：version1 < version2
     * - 0：version1 == version2
     * - 正数：version1 > version2
     * </p>
     */
    private int compareVersion(String version1, String version2) {
        // 移除版本号前缀（如v）
        String v1 = version1.replaceAll("[^0-9.]", "");
        String v2 = version2.replaceAll("[^0-9.]", "");

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 - num2;
            }
        }

        return 0;
    }
}
