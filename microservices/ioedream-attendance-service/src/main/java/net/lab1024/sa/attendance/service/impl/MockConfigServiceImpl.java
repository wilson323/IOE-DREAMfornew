package net.lab1024.sa.attendance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.attendance.dao.MockConfigDao;
import net.lab1024.sa.attendance.domain.form.MockConfigForm;
import net.lab1024.sa.attendance.domain.vo.MockConfigVO;
import net.lab1024.sa.attendance.domain.vo.MockDataVO;
import net.lab1024.sa.common.entity.attendance.MockConfigEntity;
import net.lab1024.sa.attendance.service.MockConfigService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mock配置服务实现类
 * <p>
 * 实现Mock配置管理和数据生成功能
 * 支持多种Mock场景和数据类型
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MockConfigServiceImpl implements MockConfigService {

    @Resource
    private MockConfigDao mockConfigDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建Mock配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMockConfig(MockConfigForm form) {
        log.info("[Mock服务] 创建Mock配置: name={}, type={}, scenario={}",
                form.getConfigName(), form.getConfigType(), form.getMockScenario());

        MockConfigEntity entity = MockConfigEntity.builder()
                .configName(form.getConfigName())
                .configType(form.getConfigType())
                .mockScenario(form.getMockScenario())
                .mockStatus(form.getMockStatus() != null ? form.getMockStatus() : "ENABLED")
                .generationRules(form.getGenerationRules())
                .dataTemplate(form.getDataTemplate())
                .responseDelayMs(form.getResponseDelayMs())
                .errorRate(form.getErrorRate())
                .timeoutRate(form.getTimeoutRate())
                .enableRandomDelay(form.getEnableRandomDelay())
                .randomDelayMinMs(form.getRandomDelayMinMs())
                .randomDelayMaxMs(form.getRandomDelayMaxMs())
                .configDescription(form.getConfigDescription())
                .usageCount(0L)
                .build();

        mockConfigDao.insert(entity);
        log.info("[Mock服务] Mock配置创建成功: configId={}", entity.getConfigId());
        return entity.getConfigId();
    }

    /**
     * 更新Mock配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMockConfig(MockConfigForm form) {
        log.info("[Mock服务] 更新Mock配置: configId={}", form.getConfigId());

        MockConfigEntity entity = mockConfigDao.selectById(form.getConfigId());
        if (entity == null) {
            throw new RuntimeException("Mock配置不存在");
        }

        entity.setConfigName(form.getConfigName());
        entity.setConfigType(form.getConfigType());
        entity.setMockScenario(form.getMockScenario());
        entity.setGenerationRules(form.getGenerationRules());
        entity.setDataTemplate(form.getDataTemplate());
        entity.setResponseDelayMs(form.getResponseDelayMs());
        entity.setErrorRate(form.getErrorRate());
        entity.setTimeoutRate(form.getTimeoutRate());
        entity.setEnableRandomDelay(form.getEnableRandomDelay());
        entity.setRandomDelayMinMs(form.getRandomDelayMinMs());
        entity.setRandomDelayMaxMs(form.getRandomDelayMaxMs());
        entity.setConfigDescription(form.getConfigDescription());

        mockConfigDao.updateById(entity);
        log.info("[Mock服务] Mock配置更新成功: configId={}", entity.getConfigId());
    }

    /**
     * 删除Mock配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMockConfig(Long configId) {
        log.info("[Mock服务] 删除Mock配置: configId={}", configId);

        int count = mockConfigDao.deleteById(configId);
        if (count > 0) {
            log.info("[Mock服务] 删除成功: configId={}", configId);
        } else {
            log.warn("[Mock服务] 删除失败，配置不存在: configId={}", configId);
        }
    }

    /**
     * 查询Mock配置
     */
    @Override
    public MockConfigVO getMockConfig(Long configId) {
        log.info("[Mock服务] 查询Mock配置: configId={}", configId);

        MockConfigEntity entity = mockConfigDao.selectById(configId);
        if (entity == null) {
            return null;
        }

        return convertToVO(entity);
    }

    /**
     * 查询所有Mock配置
     */
    @Override
    public List<MockConfigVO> getAllMockConfigs() {
        log.info("[Mock服务] 查询所有Mock配置");

        List<MockConfigEntity> entities = mockConfigDao.selectList(null);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 按类型查询Mock配置
     */
    @Override
    public List<MockConfigVO> getMockConfigsByType(String configType) {
        log.info("[Mock服务] 按类型查询Mock配置: type={}", configType);

        List<MockConfigEntity> entities = mockConfigDao.queryByType(configType);
        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 生成Mock数据
     */
    @Override
    public MockDataVO generateMockData(Long configId) {
        log.info("[Mock服务] 生成Mock数据: configId={}", configId);

        MockConfigEntity config = mockConfigDao.selectById(configId);
        if (config == null) {
            throw new RuntimeException("Mock配置不存在");
        }

        // 更新使用次数
        mockConfigDao.incrementUsage(configId);

        return doGenerateMockData(config);
    }

    /**
     * 生成Mock数据（按类型）
     */
    @Override
    public MockDataVO generateMockDataByType(String configType, String mockScenario) {
        log.info("[Mock服务] 按类型生成Mock数据: type={}, scenario={}", configType, mockScenario);

        List<MockConfigEntity> configs = mockConfigDao.queryEnabledByType(configType);
        MockConfigEntity config = configs.stream()
                .filter(c -> c.getMockScenario().equals(mockScenario))
                .findFirst()
                .orElse(null);

        if (config == null) {
            throw new RuntimeException("未找到匹配的Mock配置");
        }

        // 更新使用次数
        mockConfigDao.incrementUsage(config.getConfigId());

        return doGenerateMockData(config);
    }

    /**
     * 启用Mock配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableMockConfig(Long configId) {
        log.info("[Mock服务] 启用Mock配置: configId={}", configId);

        MockConfigEntity entity = mockConfigDao.selectById(configId);
        if (entity != null) {
            entity.setMockStatus("ENABLED");
            mockConfigDao.updateById(entity);
        }
    }

    /**
     * 禁用Mock配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableMockConfig(Long configId) {
        log.info("[Mock服务] 禁用Mock配置: configId={}", configId);

        MockConfigEntity entity = mockConfigDao.selectById(configId);
        if (entity != null) {
            entity.setMockStatus("DISABLED");
            mockConfigDao.updateById(entity);
        }
    }

    /**
     * 检查Mock是否启用
     */
    @Override
    public boolean isMockEnabled(String configType) {
        List<MockConfigEntity> configs = mockConfigDao.queryEnabledByType(configType);
        return !configs.isEmpty();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成Mock数据（核心方法）
     */
    private MockDataVO doGenerateMockData(MockConfigEntity config) {
        long startTime = System.currentTimeMillis();

        // 解析生成规则
        int dataCount = 100;
        long randomSeed = System.currentTimeMillis();

        try {
            if (config.getGenerationRules() != null) {
                Map<String, Object> rules = objectMapper.readValue(
                        config.getGenerationRules(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                dataCount = ((Number) rules.getOrDefault("dataCount", 100)).intValue();
                randomSeed = ((Number) rules.getOrDefault("randomSeed", System.currentTimeMillis())).longValue();
            }
        } catch (Exception e) {
            log.warn("[Mock服务] 解析生成规则失败，使用默认值", e);
        }

        // 模拟延迟
        simulateDelay(config);

        // 根据配置类型生成数据
        Object data = generateDataByType(config.getConfigType(), config.getMockScenario(), dataCount, randomSeed);

        long generationTime = System.currentTimeMillis() - startTime;

        return MockDataVO.builder()
                .configId(config.getConfigId())
                .configName(config.getConfigName())
                .mockDataType(config.getConfigType())
                .mockScenario(config.getMockScenario())
                .data(data)
                .dataCount(dataCount)
                .generationTimeMs(generationTime)
                .metadata(buildMetadata(config, dataCount))
                .build();
    }

    /**
     * 根据类型生成数据
     */
    private Object generateDataByType(String configType, String scenario, int count, long seed) {
        Random random = new Random(seed);

        return switch (configType) {
            case "EMPLOYEE" -> generateEmployeeData(count, random, scenario);
            case "DEPARTMENT" -> generateDepartmentData(count, random, scenario);
            case "SHIFT" -> generateShiftData(count, random, scenario);
            case "ATTENDANCE" -> generateAttendanceData(count, random, scenario);
            default -> generateDefaultData(count, random);
        };
    }

    /**
     * 生成员工数据
     */
    private List<Map<String, Object>> generateEmployeeData(int count, Random random, String scenario) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<String, Object> employee = new HashMap<>();
                    employee.put("employeeId", (long) (i + 1));
                    employee.put("employeeName", "员工" + (i + 1));
                    employee.put("employeeNo", "EMP" + String.format("%05d", i + 1));
                    employee.put("departmentId", (long) (random.nextInt(10) + 1));
                    employee.put("phone", "138" + String.format("%08d", random.nextInt(100000000)));
                    employee.put("email", "employee" + (i + 1) + "@example.com");
                    employee.put("status", random.nextInt(10) > 0 ? "ACTIVE" : "INACTIVE");
                    employee.put("hireDate", randomDate(random));
                    employee.put("gender", random.nextBoolean() ? "男" : "女");
                    employee.put("age", 20 + random.nextInt(40));
                    return employee;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成部门数据
     */
    private List<Map<String, Object>> generateDepartmentData(int count, Random random, String scenario) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<String, Object> department = new HashMap<>();
                    department.put("departmentId", (long) (i + 1));
                    department.put("departmentName", "部门" + (i + 1));
                    department.put("departmentCode", "DEPT" + String.format("%03d", i + 1));
                    department.put("parentId", i > 0 ? (long) random.nextInt(i) : null);
                    department.put("level", random.nextInt(5) + 1);
                    department.put("status", "ACTIVE");
                    department.put("description", "这是部门" + (i + 1) + "的描述");
                    return department;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成班次数据
     */
    private List<Map<String, Object>> generateShiftData(int count, Random random, String scenario) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<String, Object> shift = new HashMap<>();
                    shift.put("shiftId", (long) (i + 1));
                    shift.put("shiftName", "班次" + (i + 1));
                    shift.put("shiftType", random.nextInt(3)); // 0-正常班 1-弹性班 2-轮班
                    shift.put("workStartTime", String.format("%02d", 8 + random.nextInt(4)) + ":00");
                    shift.put("workEndTime", String.format("%02d", 17 + random.nextInt(4)) + ":00");
                    shift.put("breakStartTime", "12:00");
                    shift.put("breakEndTime", "13:00");
                    shift.put("workHours", 8.0);
                    shift.put("status", "ACTIVE");
                    return shift;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成考勤数据
     */
    private List<Map<String, Object>> generateAttendanceData(int count, Random random, String scenario) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<String, Object> attendance = new HashMap<>();
                    attendance.put("attendanceId", (long) (i + 1));
                    attendance.put("employeeId", (long) (random.nextInt(count) + 1));
                    attendance.put("attendanceDate", randomDate(random));
                    attendance.put("clockInTime", randomTime(random, 8, 10));
                    attendance.put("clockOutTime", randomTime(random, 17, 19));
                    attendance.put("workHours", 8.0 + random.nextDouble() * 2);
                    attendance.put("status", randomAttendanceStatus(random));
                    attendance.put("lateMinutes", random.nextInt(60));
                    attendance.put("earlyLeaveMinutes", random.nextInt(60));
                    return attendance;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成默认数据
     */
    private List<Map<String, Object>> generateDefaultData(int count, Random random) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", (long) (i + 1));
                    data.put("name", "数据" + (i + 1));
                    data.put("value", random.nextInt(1000));
                    data.put("status", random.nextBoolean() ? "ACTIVE" : "INACTIVE");
                    return data;
                })
                .collect(Collectors.toList());
    }

    /**
     * 模拟延迟
     */
    private void simulateDelay(MockConfigEntity config) {
        try {
            long delay;

            if (Boolean.TRUE.equals(config.getEnableRandomDelay())) {
                long minDelay = config.getRandomDelayMinMs() != null ? config.getRandomDelayMinMs() : 50;
                long maxDelay = config.getRandomDelayMaxMs() != null ? config.getRandomDelayMaxMs() : 200;
                delay = ThreadLocalRandom.current().nextLong(minDelay, maxDelay + 1);
            } else {
                delay = config.getResponseDelayMs() != null ? config.getResponseDelayMs() : 100;
            }

            if (delay > 0) {
                Thread.sleep(delay);
            }

            // 模拟错误率
            if (config.getErrorRate() != null && config.getErrorRate() > 0) {
                double randomValue = ThreadLocalRandom.current().nextDouble() * 100;
                if (randomValue < config.getErrorRate()) {
                    throw new RuntimeException("模拟接口错误");
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 构建元数据
     */
    private Map<String, Object> buildMetadata(MockConfigEntity config, int dataCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("configName", config.getConfigName());
        metadata.put("configType", config.getConfigType());
        metadata.put("mockScenario", config.getMockScenario());
        metadata.put("dataCount", dataCount);
        metadata.put("generationTime", LocalDateTime.now());
        return metadata;
    }

    /**
     * 转换为VO
     */
    private MockConfigVO convertToVO(MockConfigEntity entity) {
        return MockConfigVO.builder()
                .configId(entity.getConfigId())
                .configName(entity.getConfigName())
                .configType(entity.getConfigType())
                .mockScenario(entity.getMockScenario())
                .mockStatus(entity.getMockStatus())
                .responseDelayMs(entity.getResponseDelayMs())
                .errorRate(entity.getErrorRate())
                .timeoutRate(entity.getTimeoutRate())
                .usageCount(entity.getUsageCount())
                .lastUsedTime(entity.getLastUsedTime())
                .configDescription(entity.getConfigDescription())
                .createTime(entity.getCreateTime())
                .build();
    }

    // ==================== 工具方法 ====================

    private String randomDate(Random random) {
        int year = 2024 + random.nextInt(2);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private String randomTime(Random random, int startHour, int endHour) {
        int hour = startHour + random.nextInt(endHour - startHour);
        int minute = random.nextInt(60);
        return String.format("%02d:%02d", hour, minute);
    }

    private String randomAttendanceStatus(Random random) {
        int status = random.nextInt(100);
        if (status < 80) return "NORMAL";
        if (status < 90) return "LATE";
        if (status < 95) return "EARLY_LEAVE";
        return "ABSENT";
    }
}
