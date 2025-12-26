# 向OptimizationResult添加遗传算法所需方法

$resultFile = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java\net\lab1024\sa\attendance\engine\optimizer\OptimizationResult.java"

$content = Get-Content $resultFile -Raw

# 检查是否已经添加过方法
if ($content -match "getBestFitness\(\)") {
    Write-Host "⏭️  方法已存在，跳过添加" -ForegroundColor Yellow
    exit 0
}

# 在类的最后一个}之前添加新方法
$newMethods = @"

    // ==================== 遗传算法优化结果方法 ====================

    /**
     * 获取最佳适应度
     */
    public Double getBestFitness() {
        if (afterMetrics == null || !afterMetrics.containsKey("fitness")) {
            return 0.0;
        }
        return afterMetrics.get("fitness");
    }

    /**
     * 获取最佳染色体
     */
    public Chromosome getBestChromosome() {
        // 需要从优化记录中提取最佳方案
        if (optimizedRecords == null || optimizedRecords.isEmpty()) {
            return null;
        }
        // 简化实现：返回第一个优化记录
        Chromosome chromosome = new Chromosome();
        // TODO: 从optimizedRecords构建染色体
        return chromosome;
    }

    /**
     * 获取公平性得分
     */
    public Double getFairnessScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("fairness")) {
            return 0.0;
        }
        return afterMetrics.get("fairness");
    }

    /**
     * 获取成本得分
     */
    public Double getCostScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("cost")) {
            return 0.0;
        }
        return afterMetrics.get("cost");
    }

    /**
     * 获取效率得分
     */
    public Double getEfficiencyScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("efficiency")) {
            return 0.0;
        }
        return afterMetrics.get("efficiency");
    }

    /**
     * 获取满意度得分
     */
    public Double getSatisfactionScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("satisfaction")) {
            return 0.0;
        }
        return afterMetrics.get("satisfaction");
    }

    /**
     * 获取连续工作违规数
     */
    public Integer getConsecutiveWorkViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("consecutiveWorkViolations")) {
            return 0;
        }
        return beforeMetrics.get("consecutiveWorkViolations").intValue();
    }

    /**
     * 获取休息天数违规数
     */
    public Integer getRestDaysViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("restDaysViolations")) {
            return 0;
        }
        return beforeMetrics.get("restDaysViolations").intValue();
    }

    /**
     * 获取每日人员违规数
     */
    public Integer getDailyStaffViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("dailyStaffViolations")) {
            return 0;
        }
        return beforeMetrics.get("dailyStaffViolations").intValue();
    }

    /**
     * 获取总违规数
     */
    public Integer getTotalViolations() {
        return getConsecutiveWorkViolations() +
               getRestDaysViolations() +
               getDailyStaffViolations();
    }

    /**
     * 获取成本效益得分
     */
    public Double getCostEffectivenessScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("costEffectiveness")) {
            return 0.0;
        }
        return afterMetrics.get("costEffectiveness");
    }

    /**
     * 获取结果摘要
     */
    public String getSummary() {
        if (optimizationSuccessful == null || !optimizationSuccessful) {
            return "优化失败";
        }
        return String.format("优化成功: 得分=%.2f, 改进=%.2f%%",
                getBestFitness(),
                overallImprovementScore != null ? overallImprovementScore : 0.0);
    }

    /**
     * 获取加班班次总数
     */
    public Integer getTotalOvertimeShifts() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("overtimeShifts")) {
            return 0;
        }
        return beforeMetrics.get("overtimeShifts").intValue();
    }

    /**
     * 获取加班成本
     */
    public Double getTotalOvertimeCost() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("overtimeCost")) {
            return 0.0;
        }
        return beforeMetrics.get("overtimeCost");
    }

    /**
     * 获取平均工作天数（每位员工）
     */
    public Double getAvgWorkDaysPerEmployee() {
        if (afterMetrics == null || !afterMetrics.containsKey("avgWorkDays")) {
            return 0.0;
        }
        return afterMetrics.get("avgWorkDays");
    }

    /**
     * 获取工作天数标准差
     */
    public Double getWorkDaysStandardDeviation() {
        if (afterMetrics == null || !afterMetrics.containsKey("workDaysStdDev")) {
            return 0.0;
        }
        return afterMetrics.get("workDaysStdDev");
    }

    /**
     * 获取执行耗时（毫秒）
     */
    public Long getExecutionDurationMs() {
        return optimizationDuration != null ? optimizationDuration : 0L;
    }
"@

# 在类的最后一个}之前插入新方法
$content = $content -replace '(\s*)}\s*$', "$newMethods`$1"

# 写回文件
Set-Content $resultFile -Value $content -NoNewline

Write-Host "✅ 已向OptimizationResult添加遗传算法所需方法" -ForegroundColor Green
