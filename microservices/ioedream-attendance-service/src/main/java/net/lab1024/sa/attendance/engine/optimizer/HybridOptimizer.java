package net.lab1024.sa.attendance.engine.optimizer;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HybridOptimizer {
    
    @org.springframework.beans.factory.annotation.Autowired
    private GeneticAlgorithmOptimizer geneticOptimizer;
    
    @org.springframework.beans.factory.annotation.Autowired
    private SimulatedAnnealingOptimizer simulatedAnnealingOptimizer;
    
    public OptimizationResult optimize(OptimizationConfig config) {
        log.info("混合算法优化开始");
        
        // 阶段1：使用遗传算法快速搜索
        log.info("阶段1：遗传算法优化");
        OptimizationResult gaResult = geneticOptimizer.optimize(config);
        
        // 阶段2：使用模拟退火精细搜索
        log.info("阶段2：模拟退火精细搜索");
        OptimizationResult saResult = simulatedAnnealingOptimizer.optimize(config);
        
        // 选择更好的结果
        OptimizationResult bestResult = gaResult.getBestFitness() > saResult.getBestFitness()
            ? gaResult
            : saResult;
        
        log.info("混合算法优化完成，最优适应度: {}", bestResult.getBestFitness());
        
        return bestResult;
    }
}
