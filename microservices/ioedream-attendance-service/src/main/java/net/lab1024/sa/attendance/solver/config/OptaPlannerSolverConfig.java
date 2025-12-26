package net.lab1024.sa.attendance.solver.config;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.solver.model.AttendanceScheduleSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * OptaPlanner求解器配置
 *
 * 配置智能排班求解器的参数、终止条件、求解监听器等
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class OptaPlannerSolverConfig {

    /**
     * 创建SolverFactory
     *
     * 使用ConstraintProvider定义约束
     */
    @Bean
    public SolverFactory<AttendanceScheduleSolution> attendanceSolverFactory() {
        log.info("[排班求解器] 初始化SolverFactory");

        SolverConfig solverConfig = SolverConfig.createFromXmlResource(
            "net/lab1024/sa/attendance/solver/config/attendanceSolverConfig.xml");

        // 如果XML配置不存在，使用代码配置
        if (solverConfig == null) {
            solverConfig = createDefaultSolverConfig();
        }

        SolverFactory<AttendanceScheduleSolution> solverFactory =
            SolverFactory.create(solverConfig);

        log.info("[排班求解器] SolverFactory初始化完成");
        return solverFactory;
    }

    /**
     * 创建Solver
     *
     * 单例Solver，线程安全
     */
    @Bean
    public Solver<AttendanceScheduleSolution> attendanceSolver(
        SolverFactory<AttendanceScheduleSolution> solverFactory) {

        log.info("[排班求解器] 创建Solver实例");
        Solver<AttendanceScheduleSolution> solver = solverFactory.buildSolver();

        log.info("[排班求解器] Solver实例创建完成");
        return solver;
    }

    /**
     * 创建默认求解器配置
     *
     * 性能目标: 100 employees × 30 days < 30 seconds
     */
    private SolverConfig createDefaultSolverConfig() {
        log.info("[排班求解器] 使用默认配置创建SolverConfig");

        SolverConfig solverConfig = new SolverConfig();

        // 配置Solution类
        solverConfig.setSolutionClass(AttendanceScheduleSolution.class);

        // 配置Entity类
        solverConfig.setEntityClass(
            net.lab1024.sa.attendance.solver.model.ShiftAssignment.class
        );

        // 配置ConstraintProvider
        solverConfig.setConstraintProviderClass(
            net.lab1024.sa.attendance.solver.constraint.AttendanceScheduleConstraintProvider.class
        );

        // 配置求解策略
        solverConfig.setSolverConfigXml(createSolverConfigXml());

        // 配置终止条件
        TerminationConfig terminationConfig = new TerminationConfig();
        // 针对大规模问题：最多30秒
        terminationConfig.setSecondsSpentLimit(30L);
        // 针对小规模问题：最佳分数保持5秒不变
        terminationConfig.setUnimprovedSecondsSpentLimit(5L);
        // 针对测试环境：最多1000次迭代
        terminationConfig.setStepCountLimit(1000L);

        solverConfig.setTerminationConfig(terminationConfig);

        log.info("[排班求解器] 默认SolverConfig创建完成");
        return solverConfig;
    }

    /**
     * 创建求解器XML配置字符串
     *
     * 使用Late Acceptance算法（适合约束满足问题）
     */
    private String createSolverConfigXml() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <solver>
                <!-- 解决方案类 -->
                <solutionClass>net.lab1024.sa.attendance.solver.model.AttendanceScheduleSolution</solutionClass>

                <!-- 规划实体类 -->
                <entityClass>net.lab1024.sa.attendance.solver.model.ShiftAssignment</entityClass>

                <!-- 约束提供者 -->
                <constraintProviderClass>net.lab1024.sa.attendance.solver.constraint.AttendanceScheduleConstraintProvider</constraintProviderClass>

                <!-- 求解算法 -->
                <solverManagerConfig>
                    <solverConfig>
                        <!-- Late Acceptance算法：适合约束满足问题 -->
                        <localSearch>
                            <localSearchType>LATE_ACCEPTANCE</localSearchType>
                            <acceptor>
                                <lateAcceptanceSize>1000</lateAcceptanceSize>
                            </acceptor>
                            <forager>
                                <pickEarlyType>FIRST_BEST_SCORE_IMPROVING</pickEarlyType>
                            </forager>
                        </localSearch>

                        <!-- 构造启发式：使用最弱环节初始化 -->
                        <constructionHeuristic>
                            <constructionHeuristicType>WEAKEST_FIRST</constructionHeuristicType>
                        </constructionHeuristic>

                        <!-- 终止条件 -->
                        <termination>
                            <!-- 最大运行时间：30秒 -->
                            <secondsSpentLimit>30</secondsSpentLimit>
                            <!-- 最佳分数5秒不变则终止 -->
                            <unimprovedSecondsSpentLimit>5</unimprovedSecondsSpentLimit>
                            <!-- 最大1000次迭代 -->
                            <stepCountLimit>1000</stepCountLimit>
                        </termination>
                    </solverConfig>
                </solverManagerConfig>
            </solver>
            """;
    }
}
