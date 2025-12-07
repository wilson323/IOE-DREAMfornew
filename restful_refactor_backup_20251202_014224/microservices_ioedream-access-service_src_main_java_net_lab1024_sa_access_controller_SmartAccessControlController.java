package net.lab1024.sa.access.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.access.domain.entity.AccessEventEntity;
import net.lab1024.sa.access.domain.entity.AccessRuleEntity;
import net.lab1024.sa.access.domain.request.AccessControlRequestVO;
import net.lab1024.sa.access.domain.request.SmartRuleConfigVO;
import net.lab1024.sa.access.domain.vo.AccessControlResultVO;
import net.lab1024.sa.access.domain.vo.SmartAccessStatusVO;
import net.lab1024.sa.access.service.SmartAccessControlService;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 智能门禁控制控制器
 * <p>
 * 提供智能化的门禁控制功能，包括：
 * - 智能权限验证
 * - 动态规则控制
 * - 异常行为检测
 * - 个性化访问策略
 * - 实时门禁控制
 * </p>
 * 严格遵循repowiki编码规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - RESTful API设计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@RestController
@RequestMapping("/smart/access")
@Tag(name = "智能门禁控制", description = "智能化门禁控制和管理API")
public class SmartAccessControlController {

    @Resource
    private SmartAccessControlService smartAccessControlService;

    @Operation(summary = "智能门禁验证", description = "基于AI算法进行智能化门禁权限验证")
    @PostMapping("/verify")
    public ResponseDTO<AccessControlResultVO> smartAccessVerify(
            @Parameter(description = "门禁验证请求", required = true) @Valid @RequestBody AccessControlRequestVO request) {

        AccessControlResultVO result = smartAccessControlService.smartAccessVerify(request);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "快速门禁验证", description = "支持多种验证方式的快速门禁验证")
    @PostMapping("/quick-verify")
    public ResponseDTO<AccessControlResultVO> quickAccessVerify(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "访问类型", required = true) @RequestParam String accessType,
            @Parameter(description = "验证方式", required = true) @RequestParam String verifyMethod,
            @Parameter(description = "验证数据") @RequestParam(required = false) String verifyData) {

        AccessControlResultVO result = smartAccessControlService.quickAccessVerify(
                userId, deviceId, accessType, verifyMethod, verifyData);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "人脸门禁验证", description = "基于人脸识别的智能门禁验证")
    @PostMapping("/face-verify")
    public ResponseDTO<AccessControlResultVO> faceAccessVerify(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "人脸特征数据", required = true) @RequestParam String faceData,
            @Parameter(description = "活体检测结果") @RequestParam(required = false) String livenessResult) {

        AccessControlResultVO result = smartAccessControlService.faceAccessVerify(
                userId, deviceId, faceData, livenessResult);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "多因子门禁验证", description = "结合多种验证方式进行高安全门禁控制")
    @PostMapping("/multi-factor-verify")
    public ResponseDTO<AccessControlResultVO> multiFactorVerify(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "主验证方式", required = true) @RequestParam String primaryMethod,
            @Parameter(description = "主验证数据", required = true) @RequestParam String primaryData,
            @Parameter(description = "副验证方式") @RequestParam(required = false) String secondaryMethod,
            @Parameter(description = "副验证数据") @RequestParam(required = false) String secondaryData) {

        AccessControlResultVO result = smartAccessControlService.multiFactorVerify(
                userId, deviceId, primaryMethod, primaryData, secondaryMethod, secondaryData);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "动态规则验证", description = "基于动态规则进行智能门禁控制")
    @PostMapping("/dynamic-rule-verify")
    public ResponseDTO<AccessControlResultVO> dynamicRuleVerify(
            @Parameter(description = "门禁验证请求", required = true) @Valid @RequestBody AccessControlRequestVO request) {

        AccessControlResultVO result = smartAccessControlService.dynamicRuleVerify(request);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "异常行为检测", description = "检测用户门禁访问异常行为")
    @PostMapping("/abnormal-detect")
    public ResponseDTO<Map<String, Object>> detectAbnormalBehavior(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "访问时间") @RequestParam(required = false) LocalDateTime accessTime,
            @Parameter(description = "访问频率") @RequestParam(required = false) Integer accessFrequency) {

        Map<String, Object> abnormalResult = smartAccessControlService.detectAbnormalBehavior(
                userId, deviceId, accessTime, accessFrequency);
        return ResponseDTO.ok(abnormalResult);
    }

    @Operation(summary = "个性化访问策略", description = "根据用户特征生成个性化访问策略")
    @PostMapping("/personalized-strategy")
    public ResponseDTO<Map<String, Object>> generatePersonalizedStrategy(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "访问区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "时间范围") @RequestParam(required = false) String timeRange) {

        Map<String, Object> strategy = smartAccessControlService.generatePersonalizedStrategy(
                userId, areaId, timeRange);
        return ResponseDTO.ok(strategy);
    }

    @Operation(summary = "实时门禁控制", description = "实时控制门禁设备状态")
    @PostMapping("/real-time-control")
    public ResponseDTO<Boolean> realTimeControl(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "控制命令", required = true) @RequestParam String controlCommand,
            @Parameter(description = "控制参数") @RequestParam(required = false) String controlParams) {

        boolean result = smartAccessControlService.realTimeControl(deviceId, controlCommand, controlParams);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取智能门禁状态", description = "获取智能门禁系统的实时状态")
    @GetMapping("/status")
    public ResponseDTO<SmartAccessStatusVO> getSmartAccessStatus() {
        SmartAccessStatusVO status = smartAccessControlService.getSmartAccessStatus();
        return ResponseDTO.ok(status);
    }

    @Operation(summary = "创建智能规则", description = "创建智能化门禁控制规则")
    @PostMapping("/rule/create")
    public ResponseDTO<Long> createSmartRule(
            @Parameter(description = "智能规则配置", required = true) @Valid @RequestBody SmartRuleConfigVO ruleConfig) {

        Long ruleId = smartAccessControlService.createSmartRule(ruleConfig);
        return ResponseDTO.ok(ruleId);
    }

    @Operation(summary = "更新智能规则", description = "更新智能化门禁控制规则")
    @PutMapping("/rule/{ruleId}")
    public ResponseDTO<Boolean> updateSmartRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId,
            @Parameter(description = "智能规则配置", required = true) @Valid @RequestBody SmartRuleConfigVO ruleConfig) {

        boolean result = smartAccessControlService.updateSmartRule(ruleId, ruleConfig);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "删除智能规则", description = "删除智能化门禁控制规则")
    @DeleteMapping("/rule/{ruleId}")
    public ResponseDTO<Boolean> deleteSmartRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {

        boolean result = smartAccessControlService.deleteSmartRule(ruleId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取智能规则列表", description = "查询智能化门禁控制规则列表")
    @GetMapping("/rules")
    public ResponseDTO<List<AccessRuleEntity>> getSmartRules(
            @Parameter(description = "规则类型") @RequestParam(required = false) String ruleType,
            @Parameter(description = "状态 0-禁用 1-启用") @RequestParam(required = false) Integer status) {

        List<AccessRuleEntity> rules = smartAccessControlService.getSmartRules(ruleType, status);
        return ResponseDTO.ok(rules);
    }

    @Operation(summary = "获取访问事件", description = "查询智能门禁访问事件")
    @PostMapping("/events")
    public ResponseDTO<List<AccessEventEntity>> getAccessEvents(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "访问结果") @RequestParam(required = false) String accessResult) {

        List<AccessEventEntity> events = smartAccessControlService.getAccessEvents(
                userId, deviceId, startTime, endTime, accessResult);
        return ResponseDTO.ok(events);
    }

    @Operation(summary = "获取访问统计", description = "获取智能门禁访问统计数据")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getAccessStatistics(
            @Parameter(description = "统计类型 daily/weekly/monthly") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        Map<String, Object> statistics = smartAccessControlService.getAccessStatistics(
                statisticsType, startTime, endTime);
        return ResponseDTO.ok(statistics);
    }

    @Operation(summary = "风险评估", description = "进行门禁访问风险评估")
    @PostMapping("/risk-assessment")
    public ResponseDTO<Map<String, Object>> performRiskAssessment(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "访问请求", required = true) @Valid @RequestBody AccessControlRequestVO request) {

        Map<String, Object> riskAssessment = smartAccessControlService.performRiskAssessment(userId, request);
        return ResponseDTO.ok(riskAssessment);
    }

    @Operation(summary = "应急门禁控制", description = "紧急情况下的门禁控制功能")
    @PostMapping("/emergency-control")
    public ResponseDTO<Boolean> emergencyControl(
            @Parameter(description = "应急类型", required = true) @RequestParam String emergencyType,
            @Parameter(description = "影响范围") @RequestParam(required = false) String affectedScope,
            @Parameter(description = "控制动作", required = true) @RequestParam String controlAction) {

        boolean result = smartAccessControlService.emergencyControl(
                emergencyType, affectedScope, controlAction);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取访问模式", description = "分析用户访问模式")
    @GetMapping("/user/{userId}/patterns")
    public ResponseDTO<Map<String, Object>> getUserAccessPatterns(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "分析天数") @RequestParam(defaultValue = "30") Integer analysisDays) {

        Map<String, Object> patterns = smartAccessControlService.getUserAccessPatterns(userId, analysisDays);
        return ResponseDTO.ok(patterns);
    }

    @Operation(summary = "预测访问需求", description = "基于历史数据预测用户访问需求")
    @GetMapping("/predict-access/{userId}")
    public ResponseDTO<Map<String, Object>> predictAccessNeeds(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "预测天数") @RequestParam(defaultValue = "7") Integer predictDays) {

        Map<String, Object> prediction = smartAccessControlService.predictAccessNeeds(userId, predictDays);
        return ResponseDTO.ok(prediction);
    }
}
