package net.lab1024.sa.access.service;

import net.lab1024.sa.common.entity.device.DeviceFirmwareEntity;
import net.lab1024.sa.access.domain.form.FirmwareQueryForm;
import net.lab1024.sa.access.domain.form.FirmwareUploadForm;
import net.lab1024.sa.access.domain.vo.FirmwareDetailVO;
import net.lab1024.sa.access.domain.vo.FirmwareVO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 固件管理服务接口
 * <p>
 * 核心职责：
 * - 固件文件上传和存储
 * - 固件版本管理（查询、删除、启用/禁用）
 * - 固件下载
 * - MD5校验和完整性验证
 * - 版本兼容性检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface FirmwareService {

    /**
     * 上传固件文件
     * <p>
     * 功能：
     * 1. 接收上传的固件文件
     * 2. 计算文件MD5值
     * 3. 存储文件到指定路径
     * 4. 保存固件信息到数据库
     * 5. 更新统计信息
     * </p>
     *
     * @param file        固件文件
     * @param uploadForm  固件上传表单
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 固件ID
     */
    Long uploadFirmware(MultipartFile file, FirmwareUploadForm uploadForm,
                       Long operatorId, String operatorName);

    /**
     * 分页查询固件列表
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<FirmwareVO> queryFirmwarePage(FirmwareQueryForm queryForm);

    /**
     * 查询固件详情
     *
     * @param firmwareId 固件ID
     * @return 固件详情
     */
    FirmwareDetailVO getFirmwareDetail(Long firmwareId);

    /**
     * 查询指定设备的可用固件版本列表
     * <p>
     * 功能：
     * 1. 根据设备类型和型号查询
     * 2. 过滤已禁用和已废弃的固件
     * 3. 版本兼容性检查（min_version, max_version）
     * 4. 按版本号降序排序
     * </p>
     *
     * @param deviceType   设备类型
     * @param deviceModel  设备型号
     * @param currentVersion 当前版本（用于兼容性检查）
     * @return 可用固件列表
     */
    List<FirmwareVO> queryAvailableFirmware(Integer deviceType,
                                           String deviceModel,
                                           String currentVersion);

    /**
     * 检查固件版本兼容性
     * <p>
     * 检查规则：
     * 1. 如果min_version不为空，当前版本必须 >= min_version
     * 2. 如果max_version不为空，当前版本必须 < max_version
     * </p>
     *
     * @param firmwareId     固件ID
     * @param currentVersion 当前版本
     * @return 兼容返回true，不兼容返回false
     */
    Boolean checkVersionCompatibility(Long firmwareId, String currentVersion);

    /**
     * 更新固件状态
     * <p>
     * 状态类型：
     * 1-测试中 -> 2-正式发布
     * 1-测试中 -> 3-已废弃
     * 2-正式发布 -> 3-已废弃
     * </p>
     *
     * @param firmwareId     固件ID
     * @param firmwareStatus 固件状态
     * @return 更新成功返回true
     */
    Boolean updateFirmwareStatus(Long firmwareId, Integer firmwareStatus);

    /**
     * 启用/禁用固件
     *
     * @param firmwareId 固件ID
     * @param isEnabled   是否启用（1-启用 0-禁用）
     * @return 更新成功返回true
     */
    Boolean updateFirmwareEnabled(Long firmwareId, Integer isEnabled);

    /**
     * 删除固件
     * <p>
     * 注意：
     * 1. 逻辑删除，不删除实际文件
     * 2. 检查是否有关联的升级任务
     * 3. 如果有升级任务，禁止删除
     * </p>
     *
     * @param firmwareId 固件ID
     * @return 删除成功返回true
     */
    Boolean deleteFirmware(Long firmwareId);

    /**
     * 下载固件
     * <p>
     * 功能：
     * 1. 更新下载次数统计
     * 2. 返回文件下载信息
     * </p>
     *
     * @param firmwareId 固件ID
     * @return 文件下载信息（路径、文件名、大小）
     */
    FirmwareDetailVO downloadFirmware(Long firmwareId);

    /**
     * 获取固件文件MD5值
     *
     * @param firmwareId 固件ID
     * @return MD5值
     */
    String getFirmwareMd5(Long firmwareId);

    /**
     * 计算文件MD5值
     *
     * @param file 文件
     * @return MD5值（32位十六进制字符串）
     */
    String calculateFileMd5(MultipartFile file);

    /**
     * 验证文件MD5值
     *
     * @param file         文件
     * @param expectedMd5  期望的MD5值
     * @return 验证成功返回true
     */
    Boolean verifyFileMd5(MultipartFile file, String expectedMd5);

    /**
     * 获取固件统计信息
     *
     * @return 统计信息（总数量、测试中数量、正式发布数量、已废弃数量）
     */
    FirmwareDetailVO getFirmwareStatistics();
}
