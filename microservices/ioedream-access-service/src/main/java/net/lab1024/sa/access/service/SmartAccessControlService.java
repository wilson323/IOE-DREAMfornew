package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.access.domain.entity.AccessEventEntity;
import net.lab1024.sa.access.domain.entity.AccessRuleEntity;
import net.lab1024.sa.access.domain.request.AccessControlRequestVO;
import net.lab1024.sa.access.domain.request.SmartRuleConfigVO;
import net.lab1024.sa.access.domain.vo.AccessControlResultVO;
import net.lab1024.sa.access.domain.vo.SmartAccessStatusVO;

/**
 * 智能门禁控制服务接口
 * <p>
 * 提供智能化的门禁控制功能，包括：
 * - 智能权限验证
 * - 动态规则控制
 * - 异常行为检测
 * - 个性化访问策略
 * - 实时门禁控制
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
public interface SmartAccessControlService {

    /**
     * 基于AI算法进行智能化门禁权限验证
     *
     * @param request 门禁验证请求
     * @return 验证结果
     */
    AccessControlResultVO smartAccessVerify(AccessControlRequestVO request);

    /**
     * 支持多种验证方式的快速门禁验证
     *
     * @param userId       用户ID
     * @param deviceId     设备ID
     * @param accessType   访问类型
     * @param verifyMethod 验证方式
     * @param verifyData   验证数据
     * @return 验证结果
     */
    AccessControlResultVO quickAccessVerify(
            Long userId,
            Long deviceId,
            String accessType,
            String verifyMethod,
            String verifyData);

    /**
     * 基于人脸识别的智能门禁验证
     *
     * @param userId         用户ID
     * @param deviceId       设备ID
     * @param faceData       人脸特征数据
     * @param livenessResult 活体检测结果
     * @return 验证结果
     */
    AccessControlResultVO faceAccessVerify(
            Long userId,
            Long deviceId,
            String faceData,
            String livenessResult);

    /**
     * 结合多种验证方式进行高安全门禁控制
     *
     * @param userId          用户ID
     * @param deviceId        设备ID
     * @param primaryMethod   主验证方式
     * @param primaryData     主验证数据
     * @param secondaryMethod 副验证方式
     * @param secondaryData   副验证数据
     * @return 验证结果
     */
    AccessControlResultVO multiFactorVerify(
            Long userId,
            Long deviceId,
            String primaryMethod,
            String primaryData,
            String secondaryMethod,
            String secondaryData);

    /**
     * 基于动态规则进行智能门禁控制
     *
     * @param request 门禁验证请求
     * @return 验证结果
     */
    AccessControlResultVO dynamicRuleVerify(AccessControlRequestVO request);

    /**
     * 检测用户门禁访问异常行为
     *
     * @param userId          用户ID
     * @param deviceId        设备ID
     * @param accessTime      访问时间
     * @param accessFrequency 访问频率
     * @return 异常检测结果
     */
    Map<String, Object> detectAbnormalBehavior(
            Long userId,
            Long deviceId,
            LocalDateTime accessTime,
            Integer accessFrequency);

    /**
     * 根据用户特征生成个性化访问策略
     *
     * @param userId    用户ID
     * @param areaId    访问区域ID
     * @param timeRange 时间范围
     * @return 个性化策略
     */
    Map<String, Object> generatePersonalizedStrategy(
            Long userId,
            Long areaId,
            String timeRange);

    /**
     * 实时控制门禁设备状态
     *
     * @param deviceId       设备ID
     * @param controlCommand 控制命令
     * @param controlParams  控制参数
     * @return 是否控制成功
     */
    boolean realTimeControl(Long deviceId, String controlCommand, String controlParams);

    /**
     * 获取智能门禁系统的实时状态
     *
     * @return 系统状态
     */
    SmartAccessStatusVO getSmartAccessStatus();

    /**
     * 创建智能化门禁控制规则
     *
     * @param ruleConfig 智能规则配置
     * @return 规则ID
     */
    Long createSmartRule(SmartRuleConfigVO ruleConfig);

    /**
     * 更新智能化门禁控制规则
     *
     * @param ruleId     规则ID
     * @param ruleConfig 智能规则配置
     * @return 是否更新成功
     */
    boolean updateSmartRule(Long ruleId, SmartRuleConfigVO ruleConfig);

    /**
     * 删除智能化门禁控制规则
     *
     * @param ruleId 规则ID
     * @return 是否删除成功
     */
    boolean deleteSmartRule(Long ruleId);

    /**
     * 查询智能化门禁控制规则列表
     *
     * @param ruleType 规则类型
     * @param status   状态 0-禁用 1-启用
     * @return 规则列表
     */
    List<AccessRuleEntity> getSmartRules(String ruleType, Integer status);

    /**
     * 查询智能门禁访问事件
     *
     * @param userId       用户ID
     * @param deviceId     设备ID
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param accessResult 访问结果
     * @return 访问事件列表
     */
    List<AccessEventEntity> getAccessEvents(
            Long userId,
            Long deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String accessResult);

    /**
     * 获取智能门禁访问统计数据
     *
     * @param statisticsType 统计类型 daily/weekly/monthly
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 统计数据
     */
    Map<String, Object> getAccessStatistics(
            String statisticsType,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 进行门禁访问风险评估
     *
     * @param userId  用户ID
     * @param request 访问请求
     * @return 风险评估结果
     */
    Map<String, Object> performRiskAssessment(Long userId, AccessControlRequestVO request);

    /**
     * 紧急情况下的门禁控制功能
     *
     * @param emergencyType 应急类型
     * @param affectedScope 影响范围
     * @param controlAction 控制动作
     * @return 是否控制成功
     */
    boolean emergencyControl(String emergencyType, String affectedScope, String controlAction);

    /**
     * 分析用户访问模式
     *
     * @param userId       用户ID
     * @param analysisDays 分析天数
     * @return 访问模式
     */
    Map<String, Object> getUserAccessPatterns(Long userId, Integer analysisDays);

    /**
     * 基于历史数据预测用户访问需求
     *
     * @param userId      用户ID
     * @param predictDays 预测天数
     * @return 预测结果
     */
    Map<String, Object> predictAccessNeeds(Long userId, Integer predictDays);
}
