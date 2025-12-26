package net.lab1024.sa.attendance.engine.optimization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 考勤规则版本管理器
 * <p>
 * 支持规则版本控制、回滚、灰度发布等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Slf4j
@Component
public class AttendanceRuleVersionManager {

    /**
     * 规则版本信息
     */
    public static class RuleVersion {
        private Long ruleId;
        private Long versionId;
        private String versionName; // v1.0, v1.1, v2.0等
        private String versionDescription;
        private String ruleCondition; // 规则条件表达式
        private String ruleAction; // 规则动作配置
        private Integer priority;
        private Boolean enabled;
        private LocalDateTime createTime;
        private String createdBy;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private String status; // DRAFT-草稿, ACTIVE-生效, DEPRECATED-废弃, ARCHIVED-归档
        private Map<String, Object> metadata; // 元数据

        // Getters and Setters
        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

        public Long getVersionId() { return versionId; }
        public void setVersionId(Long versionId) { this.versionId = versionId; }

        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }

        public String getVersionDescription() { return versionDescription; }
        public void setVersionDescription(String versionDescription) { this.versionDescription = versionDescription; }

        public String getRuleCondition() { return ruleCondition; }
        public void setRuleCondition(String ruleCondition) { this.ruleCondition = ruleCondition; }

        public String getRuleAction() { return ruleAction; }
        public void setRuleAction(String ruleAction) { this.ruleAction = ruleAction; }

        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }

        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    /**
     * 版本发布结果
     */
    public static class VersionPublishResult {
        private Boolean success;
        private Long versionId;
        private String message;
        private List<String> warnings;

        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public Long getVersionId() { return versionId; }
        public void setVersionId(Long versionId) { this.versionId = versionId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }

    // 版本存储
    private final Map<Long, List<RuleVersion>> ruleVersions = new ConcurrentHashMap<>();
    private final Map<Long, RuleVersion> activeVersions = new ConcurrentHashMap<>();
    private Long versionIdSequence = 1L;

    /**
     * 创建新规则版本
     *
     * @param ruleId 规则ID
     * @param version 版本信息
     * @return 版本ID
     */
    public Long createVersion(Long ruleId, RuleVersion version) {
        log.info("[规则版本管理器] 创建新版本: ruleId={}, versionName={}", ruleId, version.getVersionName());

        version.setRuleId(ruleId);
        version.setVersionId(versionIdSequence++);
        version.setCreateTime(LocalDateTime.now());
        version.setStatus("DRAFT");

        ruleVersions.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(version);

        log.info("[规则版本管理器] 版本创建成功: ruleId={}, versionId={}, versionName={}",
                ruleId, version.getVersionId(), version.getVersionName());

        return version.getVersionId();
    }

    /**
     * 发布版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @param publishTime 生效时间
     * @return 发布结果
     */
    public VersionPublishResult publishVersion(Long ruleId, Long versionId, LocalDateTime publishTime) {
        log.info("[规则版本管理器] 发布版本: ruleId={}, versionId={}, publishTime={}", ruleId, versionId, publishTime);

        VersionPublishResult result = new VersionPublishResult();
        List<String> warnings = new ArrayList<>();

        try {
            // 1. 获取版本
            RuleVersion version = getVersion(ruleId, versionId);
            if (version == null) {
                result.setSuccess(false);
                result.setMessage("版本不存在");
                return result;
            }

            // 2. 验证版本状态
            if ("ACTIVE".equals(version.getStatus())) {
                warnings.add("版本已经是生效状态，重复发布");
            }

            // 3. 将当前生效版本标记为废弃
            RuleVersion currentActive = activeVersions.get(ruleId);
            if (currentActive != null) {
                currentActive.setStatus("DEPRECATED");
                log.info("[规则版本管理器] 当前版本标记为废弃: ruleId={}, oldVersionId={}",
                        ruleId, currentActive.getVersionId());
            }

            // 4. 将新版本标记为生效
            version.setStatus("ACTIVE");
            version.setEffectiveTime(publishTime);
            activeVersions.put(ruleId, version);

            result.setSuccess(true);
            result.setVersionId(versionId);
            result.setMessage("版本发布成功");
            result.setWarnings(warnings);

            log.info("[规则版本管理器] 版本发布成功: ruleId={}, versionId={}, versionName={}",
                    ruleId, versionId, version.getVersionName());

            return result;

        } catch (Exception e) {
            log.error("[规则版本管理器] 版本发布失败: ruleId={}, versionId={}", ruleId, versionId, e);
            result.setSuccess(false);
            result.setMessage("版本发布失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 回滚到指定版本
     *
     * @param ruleId 规则ID
     * @param targetVersionId 目标版本ID
     * @return 是否成功
     */
    public Boolean rollbackToVersion(Long ruleId, Long targetVersionId) {
        log.info("[规则版本管理器] 回滚版本: ruleId={}, targetVersionId={}", ruleId, targetVersionId);

        try {
            // 1. 获取目标版本
            RuleVersion targetVersion = getVersion(ruleId, targetVersionId);
            if (targetVersion == null) {
                log.error("[规则版本管理器] 目标版本不存在: versionId={}", targetVersionId);
                return false;
            }

            // 2. 将当前生效版本标记为废弃
            RuleVersion currentActive = activeVersions.get(ruleId);
            if (currentActive != null) {
                currentActive.setStatus("DEPRECATED");
            }

            // 3. 创建目标版本的新副本
            RuleVersion newVersion = new RuleVersion();
            newVersion.setRuleId(ruleId);
            newVersion.setVersionId(versionIdSequence++);
            newVersion.setVersionName(targetVersion.getVersionName() + "-rollback");
            newVersion.setVersionDescription("回滚到版本: " + targetVersion.getVersionName());
            newVersion.setRuleCondition(targetVersion.getRuleCondition());
            newVersion.setRuleAction(targetVersion.getRuleAction());
            newVersion.setPriority(targetVersion.getPriority());
            newVersion.setEnabled(true);
            newVersion.setCreateTime(LocalDateTime.now());
            newVersion.setEffectiveTime(LocalDateTime.now());
            newVersion.setStatus("ACTIVE");

            // 4. 保存新版本
            ruleVersions.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(newVersion);
            activeVersions.put(ruleId, newVersion);

            log.info("[规则版本管理器] 版本回滚成功: ruleId={}, targetVersionId={}, newVersionId={}",
                    ruleId, targetVersionId, newVersion.getVersionId());

            return true;

        } catch (Exception e) {
            log.error("[规则版本管理器] 版本回滚失败: ruleId={}, targetVersionId={}", ruleId, targetVersionId, e);
            return false;
        }
    }

    /**
     * 获取规则的所有版本
     *
     * @param ruleId 规则ID
     * @return 版本列表
     */
    public List<RuleVersion> getVersions(Long ruleId) {
        return ruleVersions.getOrDefault(ruleId, new ArrayList<>());
    }

    /**
     * 获取指定版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @return 版本信息
     */
    public RuleVersion getVersion(Long ruleId, Long versionId) {
        List<RuleVersion> versions = ruleVersions.get(ruleId);
        if (versions == null) {
            return null;
        }

        return versions.stream()
                .filter(v -> v.getVersionId().equals(versionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取当前生效版本
     *
     * @param ruleId 规则ID
     * @return 生效版本
     */
    public RuleVersion getActiveVersion(Long ruleId) {
        return activeVersions.get(ruleId);
    }

    /**
     * 比较两个版本的差异
     *
     * @param ruleId 规则ID
     * @param versionId1 版本1
     * @param versionId2 版本2
     * @return 差异描述
     */
    public Map<String, Object> compareVersions(Long ruleId, Long versionId1, Long versionId2) {
        log.info("[规则版本管理器] 比较版本差异: ruleId={}, versionId1={}, versionId2={}",
                ruleId, versionId1, versionId2);

        Map<String, Object> differences = new HashMap<>();

        RuleVersion version1 = getVersion(ruleId, versionId1);
        RuleVersion version2 = getVersion(ruleId, versionId2);

        if (version1 == null || version2 == null) {
            differences.put("error", "版本不存在");
            return differences;
        }

        // 比较规则条件
        if (!Objects.equals(version1.getRuleCondition(), version2.getRuleCondition())) {
            differences.put("ruleCondition", Map.of(
                    "old", version1.getRuleCondition(),
                    "new", version2.getRuleCondition()
            ));
        }

        // 比较规则动作
        if (!Objects.equals(version1.getRuleAction(), version2.getRuleAction())) {
            differences.put("ruleAction", Map.of(
                    "old", version1.getRuleAction(),
                    "new", version2.getRuleAction()
            ));
        }

        // 比较优先级
        if (!Objects.equals(version1.getPriority(), version2.getPriority())) {
            differences.put("priority", Map.of(
                    "old", version1.getPriority(),
                    "new", version2.getPriority()
            ));
        }

        // 比较启用状态
        if (!Objects.equals(version1.getEnabled(), version2.getEnabled())) {
            differences.put("enabled", Map.of(
                    "old", version1.getEnabled(),
                    "new", version2.getEnabled()
            ));
        }

        log.info("[规则版本管理器] 版本差异比较完成: differenceCount={}", differences.size());

        return differences;
    }

    /**
     * 归档旧版本
     *
     * @param ruleId 规则ID
     * @param beforeTime 在此时间之前的版本
     * @return 归档的版本数量
     */
    public Integer archiveOldVersions(Long ruleId, LocalDateTime beforeTime) {
        log.info("[规则版本管理器] 归档旧版本: ruleId={}, beforeTime={}", ruleId, beforeTime);

        List<RuleVersion> versions = ruleVersions.get(ruleId);
        if (versions == null) {
            return 0;
        }

        int archivedCount = 0;
        for (RuleVersion version : versions) {
            if ("DEPRECATED".equals(version.getStatus()) &&
                version.getCreateTime().isBefore(beforeTime)) {
                version.setStatus("ARCHIVED");
                archivedCount++;
            }
        }

        log.info("[规则版本管理器] 版本归档完成: ruleId={}, archivedCount={}", ruleId, archivedCount);

        return archivedCount;
    }

    /**
     * 删除版本
     *
     * @param ruleId 规则ID
     * @param versionId 版本ID
     * @return 是否成功
     */
    public Boolean deleteVersion(Long ruleId, Long versionId) {
        log.info("[规则版本管理器] 删除版本: ruleId={}, versionId={}", ruleId, versionId);

        try {
            List<RuleVersion> versions = ruleVersions.get(ruleId);
            if (versions == null) {
                return false;
            }

            // 只能删除非生效版本
            RuleVersion activeVersion = activeVersions.get(ruleId);
            if (activeVersion != null && activeVersion.getVersionId().equals(versionId)) {
                log.warn("[规则版本管理器] 无法删除生效版本: versionId={}", versionId);
                return false;
            }

            boolean removed = versions.removeIf(v -> v.getVersionId().equals(versionId));

            log.info("[规则版本管理器] 版本删除: ruleId={}, versionId={}, removed={}",
                    ruleId, versionId, removed);

            return removed;

        } catch (Exception e) {
            log.error("[规则版本管理器] 版本删除失败: ruleId={}, versionId={}", ruleId, versionId, e);
            return false;
        }
    }
}
