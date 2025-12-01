package net.lab1024.sa.access.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import net.lab1024.sa.access.domain.entity.BiometricDataEntity;
import net.lab1024.sa.access.domain.vo.BiometricEnrollRequestVO;
import net.lab1024.sa.access.domain.vo.BiometricMatchResultVO;

/**
 * 生物识别服务接口
 * <p>
 * 提供生物识别相关的业务逻辑服务，包括：
 * - 人脸识别
 * - 指纹识别
 * - 虹膜识别
 * - 生物特征注册
 * - 移动端验证
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
public interface BiometricService {

    /**
     * 通过移动端上传人脸进行身份验证
     *
     * @param faceImage 人脸图片文件
     * @param userId    用户ID（可选）
     * @return 识别结果
     */
    BiometricMatchResultVO verifyFaceByMobile(MultipartFile faceImage, Long userId);

    /**
     * 通过移动端上传指纹进行身份验证
     *
     * @param fingerprintData 指纹特征数据
     * @param userId          用户ID（可选）
     * @return 识别结果
     */
    BiometricMatchResultVO verifyFingerprintByMobile(String fingerprintData, Long userId);

    /**
     * 通过移动端上传虹膜进行身份验证
     *
     * @param irisImage 虹膜图片文件
     * @param userId    用户ID（可选）
     * @return 识别结果
     */
    BiometricMatchResultVO verifyIrisByMobile(MultipartFile irisImage, Long userId);

    /**
     * 结合多种生物特征进行身份验证
     *
     * @param faceImage       人脸图片文件
     * @param fingerprintData 指纹特征数据
     * @param irisImage       虹膜图片文件
     * @param userId          用户ID（可选）
     * @return 识别结果
     */
    BiometricMatchResultVO verifyMultiModalByMobile(
            MultipartFile faceImage,
            String fingerprintData,
            MultipartFile irisImage,
            Long userId);

    /**
     * 在移动端注册用户人脸特征
     *
     * @param userId    用户ID
     * @param faceImage 人脸图片文件
     * @return 模板ID
     */
    String enrollFaceByMobile(Long userId, MultipartFile faceImage);

    /**
     * 在移动端注册用户指纹特征
     *
     * @param userId          用户ID
     * @param fingerprintData 指纹特征数据
     * @param fingerType      手指类型 1-拇指 2-食指 3-中指 4-无名指 5-小指
     * @return 模板ID
     */
    String enrollFingerprintByMobile(Long userId, String fingerprintData, Integer fingerType);

    /**
     * 在移动端注册用户虹膜特征
     *
     * @param userId    用户ID
     * @param irisImage 虹膜图片文件
     * @param eyeType   眼睛类型 1-左眼 2-右眼
     * @return 模板ID
     */
    String enrollIrisByMobile(Long userId, MultipartFile irisImage, Integer eyeType);

    /**
     * 批量注册用户多种生物特征
     *
     * @param userId        用户ID
     * @param enrollRequest 生物特征注册请求
     * @return 模板ID映射（key: 生物特征类型, value: 模板ID）
     */
    Map<String, String> batchEnrollByMobile(Long userId, BiometricEnrollRequestVO enrollRequest);

    /**
     * 查询指定用户的注册生物特征信息
     *
     * @param userId 用户ID
     * @return 生物特征数据列表
     */
    List<BiometricDataEntity> getUserBiometrics(Long userId);

    /**
     * 删除指定的生物特征模板
     *
     * @param templateId 模板ID
     * @return 是否删除成功
     */
    boolean deleteBiometricTemplate(Long templateId);

    /**
     * 更新指定的生物特征模板
     *
     * @param templateId    模板ID
     * @param biometricData 生物特征数据
     * @return 是否更新成功
     */
    boolean updateBiometricTemplate(Long templateId, String biometricData);

    /**
     * 检测上传的生物特征数据质量
     *
     * @param biometricType 生物特征类型
     * @param biometricFile 生物特征文件
     * @param biometricData 生物特征数据
     * @return 质量检测结果
     */
    Map<String, Object> checkBiometricQuality(
            String biometricType,
            MultipartFile biometricFile,
            String biometricData);

    /**
     * 获取用户生物识别的统计数据
     *
     * @param userId 用户ID
     * @param days   统计天数
     * @return 统计数据
     */
    Map<String, Object> getBiometricStatistics(Long userId, Integer days);

    /**
     * 进行人脸活体检测，防止照片攻击
     *
     * @param faceImage  人脸图片文件
     * @param detectType 活体检测类型 1-眨眼 2-摇头 3-张嘴 4-综合
     * @return 活体检测结果
     */
    Map<String, Object> performLivenessDetection(MultipartFile faceImage, Integer detectType);

    /**
     * 获取移动端生物识别相关配置信息
     *
     * @return 配置信息
     */
    Map<String, Object> getMobileBiometricConfig();
}
