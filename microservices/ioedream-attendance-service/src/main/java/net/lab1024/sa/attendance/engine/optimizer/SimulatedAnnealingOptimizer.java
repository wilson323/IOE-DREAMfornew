package net.lab1024.sa.attendance.engine.optimizer;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class SimulatedAnnealingOptimizer {
    private Random random = new Random();

    public OptimizationResult optimize(OptimizationConfig config) {
        long startTime = System.currentTimeMillis();
        
        Chromosome current = initializeChromosome(config);
        double currentFitness = calculateFitness(current, config);
        current.setFitness(currentFitness);
        
        Chromosome best = current.clone();
        double temperature = config.getInitialTemperature();
        
        log.info("模拟退火优化开始，初始温度: {}", temperature);
        
        int iteration = 0;
        int maxIterations = config.getMaxGenerations() * 10;
        
        while (temperature > 1.0 && iteration < maxIterations) {
            Chromosome neighbor = generateNeighbor(current, config);
            double neighborFitness = calculateFitness(neighbor, config);
            neighbor.setFitness(neighborFitness);
            
            double delta = neighborFitness - currentFitness;
            
            if (delta > 0 || Math.exp(delta / temperature) > random.nextDouble()) {
                current = neighbor;
                currentFitness = neighborFitness;
                
                if (currentFitness > best.getFitness()) {
                    best = current.clone();
                }
            }
            
            temperature *= config.getCoolingRate();
            iteration++;
            
            if (iteration % 100 == 0) {
                log.info("迭代{}，温度: {}，最优适应度: {}", iteration, temperature, best.getFitness());
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        return OptimizationResult.success(best, best.getFitness(), iteration, duration);
    }
    
    private Chromosome initializeChromosome(OptimizationConfig config) {
        Chromosome chromosome = new Chromosome();
        List<LocalDate> dates = getDatesInRange(config.getStartDate(), config.getEndDate());
        chromosome.initialize(config.getEmployeeIds(), dates, config.getShiftIds());
        return chromosome;
    }
    
    private Chromosome generateNeighbor(Chromosome current, OptimizationConfig config) {
        Chromosome neighbor = current.clone();
        performMutation(neighbor, config);
        return neighbor;
    }
    
    private double calculateFitness(Chromosome chromosome, OptimizationConfig config) {
        double fairnessScore = calculateFairness(chromosome, config);
        double costScore = 0.8;
        double efficiencyScore = 0.75;
        double satisfactionScore = 0.7;
        
        return config.getFairnessWeight() * fairnessScore +
               config.getCostWeight() * costScore +
               config.getEfficiencyWeight() * efficiencyScore +
               config.getSatisfactionWeight() * satisfactionScore;
    }
    
    private double calculateFairness(Chromosome chromosome, OptimizationConfig config) {
        Map<Long, Integer> workDaysCount = new HashMap<>();
        for (Long employeeId : chromosome.getEmployeeIds()) {
            workDaysCount.put(employeeId, chromosome.getSchedule(employeeId).size());
        }
        if (workDaysCount.isEmpty()) return 0.5;
        
        int maxDays = Collections.max(workDaysCount.values());
        int minDays = Collections.min(workDaysCount.values());
        
        if (maxDays == minDays) return 1.0;
        double variance = (double) (maxDays - minDays) / maxDays;
        return 1.0 - variance;
    }
    
    private void performMutation(Chromosome chromosome, OptimizationConfig config) {
        List<Long> employeeIds = new ArrayList<>(chromosome.getEmployeeIds());
        if (employeeIds.isEmpty()) return;

        Long employeeId = employeeIds.get(random.nextInt(employeeIds.size()));
        Map<LocalDate, Long> schedule = chromosome.getSchedule(employeeId);
        if (schedule.isEmpty()) return;

        List<LocalDate> dates = new ArrayList<>(schedule.keySet());
        LocalDate date = dates.get(random.nextInt(dates.size()));
        Long newShiftId = config.getShiftIds().get(random.nextInt(config.getShiftIds().size()));

        chromosome.setShift(employeeId, date, newShiftId);
    }
    
    private List<LocalDate> getDatesInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }
}
