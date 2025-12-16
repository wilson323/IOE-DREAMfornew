package net.lab1024.sa.access.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.access.domain.form.BiometricDataForm;
import net.lab1024.sa.access.domain.vo.BiometricAntiSpoofResultVO;
import net.lab1024.sa.access.domain.vo.TrajectoryAnomalyResultVO;

/**
 * 安全增强服务接口
 * <p>
 * 提供门禁系统安全增强功能，包括：
 * - 生物识别防伪检测
 * - 访问轨迹异常检测
 * - 安全威胁识别
 * - 异常行为分析
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface SecurityEnhancerService {

    /**
     * 生物识别防伪检测
     * <p>
     * 对生物识别数据进行深度分析，防止照片、视频、硅胶面具等攻击：
     * - 活体检测
     * - 深度伪造检测
     * - 质量评估
     * 3D结构分析
     * </p>
     *
     * @param biometricData 生物识别数据
     * @return 防伪检测结果
     */
    BiometricAntiSpoofResultVO performBiometricAntiSpoofing(BiometricDataForm biometricData);

    /**
     * 访问轨迹异常检测
     * <p>
     * 分析用户访问轨迹，识别异常模式：
     * - 时间模式异常
     * - 空间模式异常
     * - 频率异常
     * - 行为序列异常
     * </p>
     *
     * @param userId 用户ID
     * @param trajectory 访问轨迹数据
     * @return 轨迹异常检测结果
     */
    TrajectoryAnomalyResultVO detectTrajectoryAnomaly(Long userId, AccessTrajectory trajectory);

    /**
     * 安全威胁实时识别
     * <p>
     * 实时监控和识别各种安全威胁：
     * - 暴力破解尝试
     * 异常访问模式
     * 系统漏洞利用
     * 社会工程攻击
     * </p>
     *
     * @param accessEvent 访问事件数据
     * @return 威胁识别结果
     */
    SecurityThreatResult identifySecurityThreat(AccessEvent accessEvent);

    /**
     * 异常行为模式分析
     * <p>
     * 基于机器学习分析用户行为模式：
     * - 行为基线建立
     * - 异常模式识别
     * - 风险等级评估
     * - 预警触发
     * </p>
     *
     * @param userId 用户ID
     * @param timeWindow 分析时间窗口（天）
     * @return 行为分析结果
     */
    BehaviorAnalysisResult analyzeBehaviorPatterns(Long userId, Integer timeWindow);

    /**
     * 安全事件风险评估
     * <p>
     * 对安全事件进行综合风险评估：
     * - 威胁等级评估
     * - 影响范围分析
     * - 处置优先级
     * - 补救建议
     * </p>
     *
     * @param securityEvent 安全事件
     * @return 风险评估结果
     */
    SecurityRiskAssessmentVO assessSecurityRisk(SecurityEvent securityEvent);

    /**
     * 实时安全监控
     * <p>
     * 提供实时的安全状态监控：
     * - 系统安全状态
     * - 活跃威胁统计
     * - 异常事件计数
     * - 安全指标监控
     * </p>
     *
     * @return 安全监控数据
     */
    SecurityMonitoringData getRealtimeSecurityMonitoring();

    // 内部数据传输对象定义
    class AccessTrajectory {
        private Long userId;
        private List<AccessPoint> accessPoints;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // 访问点内部类
        class AccessPoint {
            private Long deviceId;
            private Long areaId;
            private LocalDateTime accessTime;
            private String accessType;
            private String verificationMethod;
            private Map<String, Object> metadata;
        }
    }

    class AccessEvent {
        private Long eventId;
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String eventType;
        private LocalDateTime eventTime;
        private String sourceIp;
        private String userAgent;
        private Map<String, Object> eventData;
    }

    class SecurityEvent {
        private String eventId;
        private String eventType;
        private String threatLevel;
        private LocalDateTime eventTime;
        private String description;
        private Map<String, Object> details;
        private List<String> affectedResources;
    }

    class SecurityThreatResult {
        private boolean threatDetected;
        private String threatType;
        private String threatLevel;
        private String description;
        private List<String> recommendedActions;
        private double confidenceScore;
    }

    class BehaviorAnalysisResult {
        private Long userId;
        private String behaviorPattern;
        private boolean anomalyDetected;
        private String anomalyType;
        private double riskScore;
        private List<String> recommendations;
        private LocalDateTime analysisTime;
    }

    class SecurityRiskAssessmentVO {
        private String riskId;
        private String riskLevel;
        private double riskScore;
        private String impactScope;
        private String priority;
        private List<String> mitigationStrategies;
        private String assessmentTime;
    }

    class SecurityMonitoringData {
        private Map<String, Long> activeThreats;
        private Map<String, Long> anomalyCounts;
        private Map<String, Double> securityMetrics;
        private LocalDateTime lastUpdateTime;
        private String overallSecurityStatus;
    }
}