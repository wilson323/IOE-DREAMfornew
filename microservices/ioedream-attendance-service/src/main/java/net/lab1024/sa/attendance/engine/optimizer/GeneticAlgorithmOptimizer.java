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
public class GeneticAlgorithmOptimizer {
    private Random random = new Random();

    public OptimizationResult optimize(OptimizationConfig config) {
        long startTime = System.currentTimeMillis();
        List<Chromosome> population = initializePopulation(config);
        log.info("遗传算法优化开始，种群大小: {}", population.size());

        for (int generation = 0; generation < config.getMaxGenerations(); generation++) {
            evaluateFitness(population, config);
            List<Chromosome> selected = selection(population, config);
            crossover(selected, config);
            mutate(selected, config);
            population = selected;
            log.info("第{}代，最优适应度: {}", generation + 1, population.get(0).getFitness());
        }

        Chromosome best = population.get(0);
        long duration = System.currentTimeMillis() - startTime;
        return OptimizationResult.success(best, best.getFitness(), config.getMaxGenerations(), duration);
    }

    private List<Chromosome> initializePopulation(OptimizationConfig config) {
        List<Chromosome> population = new ArrayList<>();
        List<LocalDate> dates = getDatesInRange(config.getStartDate(), config.getEndDate());

        for (int i = 0; i < config.getPopulationSize(); i++) {
            Chromosome chromosome = new Chromosome();
            chromosome.initialize(config.getEmployeeIds(), dates, config.getShiftIds());
            population.add(chromosome);
        }
        return population;
    }

    private void evaluateFitness(List<Chromosome> population, OptimizationConfig config) {
        for (Chromosome chromosome : population) {
            double fitness = calculateFitness(chromosome, config);
            chromosome.setFitness(fitness);
        }
        population.sort(Collections.reverseOrder());
    }

    private double calculateFitness(Chromosome chromosome, OptimizationConfig config) {
        double fairnessScore = calculateFairness(chromosome, config);
        double costScore = calculateCost(chromosome, config);
        double efficiencyScore = calculateEfficiency(chromosome, config);
        double satisfactionScore = calculateSatisfaction(chromosome, config);

        double fitness = config.getFairnessWeight() * fairnessScore +
                       config.getCostWeight() * costScore +
                       config.getEfficiencyWeight() * efficiencyScore +
                       config.getSatisfactionWeight() * satisfactionScore;

        chromosome.setFairnessScore(fairnessScore);
        chromosome.setCostScore(costScore);
        chromosome.setEfficiencyScore(efficiencyScore);
        chromosome.setSatisfactionScore(satisfactionScore);

        return fitness;
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

    private double calculateCost(Chromosome chromosome, OptimizationConfig config) {
        return 0.8;
    }

    private double calculateEfficiency(Chromosome chromosome, OptimizationConfig config) {
        return 0.75;
    }

    private double calculateSatisfaction(Chromosome chromosome, OptimizationConfig config) {
        return 0.7;
    }

    private List<Chromosome> selection(List<Chromosome> population, OptimizationConfig config) {
        List<Chromosome> selected = new ArrayList<>();
        double totalFitness = population.stream().mapToDouble(Chromosome::getFitness).sum();

        selected.add(population.get(0).clone());
        selected.add(population.get(1).clone());

        while (selected.size() < population.size()) {
            double pointer = random.nextDouble() * totalFitness;
            double sum = 0.0;
            for (Chromosome chromosome : population) {
                sum += chromosome.getFitness();
                if (sum >= pointer) {
                    selected.add(chromosome.clone());
                    break;
                }
            }
        }
        return selected;
    }

    private void crossover(List<Chromosome> population, OptimizationConfig config) {
        int populationSize = population.size();
        for (int i = 0; i < populationSize / 2; i++) {
            Chromosome parent1 = population.get(i * 2);
            Chromosome parent2 = population.get(i * 2 + 1);
            if (random.nextDouble() < config.getCrossoverRate()) {
                performCrossover(parent1, parent2);
            }
        }
    }

    private void performCrossover(Chromosome parent1, Chromosome parent2) {
        List<Long> employeeIds = new ArrayList<>(parent1.getEmployeeIds());
        if (employeeIds.size() < 2) return;

        int crossoverPoint = random.nextInt(employeeIds.size());
        for (int i = crossoverPoint; i < employeeIds.size(); i++) {
            Long employeeId = employeeIds.get(i);
            Map<LocalDate, Long> temp = parent1.getSchedule(employeeId);
            parent1.getGenes().put(employeeId, parent2.getSchedule(employeeId));
            parent2.getGenes().put(employeeId, temp);
        }
    }

    private void mutate(List<Chromosome> population, OptimizationConfig config) {
        for (Chromosome chromosome : population) {
            if (random.nextDouble() < config.getMutationRate()) {
                performMutation(chromosome, config);
            }
        }
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
