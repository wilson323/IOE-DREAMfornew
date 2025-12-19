package net.lab1024.sa.common.storage.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件清理配置
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class FileCleanupProperties {

    /**
     * 是否启用自动清理
     */
    private Boolean enabled = true;

    /**
     * 清理任务执行时间(Cron表达式)
     * 默认: 每天凌晨3点执行
     */
    private String schedule = "0 3 * * *";

    /**
     * 清理规则列表
     */
    private List<CleanupRule> rules = new ArrayList<>();

    /**
     * 清理规则
     */
    @Data
    public static class CleanupRule {

        /**
         * 文件路径(相对于base-path)
         */
        private String path;

        /**
         * 保留天数
         */
        private Integer retentionDays;
    }
}
