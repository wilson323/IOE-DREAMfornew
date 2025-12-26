package net.lab1024.sa.attendance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.attendance.dao.RuleTestHistoryDao;
import net.lab1024.sa.attendance.domain.form.RuleTestHistoryQueryForm;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryDetailVO;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryVO;
import net.lab1024.sa.attendance.domain.vo.RuleTestResultVO;
import net.lab1024.sa.attendance.entity.RuleTestHistoryEntity;
import net.lab1024.sa.attendance.service.RuleTestHistoryService;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 规则测试历史服务实现类
 * <p>
 * 提供规则测试历史管理相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class RuleTestHistoryServiceImpl implements RuleTestHistoryService {

    @Resource
    private RuleTestHistoryDao ruleTestHistoryDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 保存测试历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveHistory(RuleTestHistoryDetailVO historyDetail) {
        log.info("[测试历史] 保存测试历史: testId={}", historyDetail.getTestId());

        try {
            RuleTestHistoryEntity entity = convertToEntity(historyDetail);
            ruleTestHistoryDao.insert(entity);

            log.info("[测试历史] 保存成功: historyId={}", entity.getHistoryId());
            return entity.getHistoryId();

        } catch (Exception e) {
            log.error("[测试历史] 保存失败: testId={}, error={}", historyDetail.getTestId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 分页查询测试历史
     */
    @Override
    public PageResult<RuleTestHistoryVO> queryHistoryPage(RuleTestHistoryQueryForm queryForm) {
        log.info("[测试历史] 分页查询测试历史: queryForm={}", queryForm);

        // 构建查询条件
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (queryForm.getStartTime() != null) {
            startTime = queryForm.getStartTime();
        }
        if (queryForm.getEndTime() != null) {
            endTime = queryForm.getEndTime();
        }

        // 查询数据
        List<RuleTestHistoryEntity> entities = ruleTestHistoryDao.queryHistoryList(
                queryForm.getRuleId(),
                queryForm.getTestUserId(),
                queryForm.getTestResult(),
                startTime,
                endTime
        );

        // 转换为VO
        List<RuleTestHistoryVO> vos = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 手动分页
        int total = vos.size();
        int start = (queryForm.getPageNum() - 1) * queryForm.getPageSize();
        int end = Math.min(start + queryForm.getPageSize(), total);

        List<RuleTestHistoryVO> pageData = vos.subList(start, end);

        log.info("[测试历史] 分页查询成功: total={}", total);
        return PageResult.of(pageData, (long) total, queryForm.getPageNum(), queryForm.getPageSize());
    }

    /**
     * 查询历史详情
     */
    @Override
    public RuleTestHistoryDetailVO getHistoryDetail(Long historyId) {
        log.info("[测试历史] 查询历史详情: historyId={}", historyId);

        RuleTestHistoryEntity entity = ruleTestHistoryDao.selectById(historyId);
        if (entity == null) {
            log.warn("[测试历史] 历史记录不存在: historyId={}", historyId);
            return null;
        }

        RuleTestHistoryDetailVO detailVO = convertToDetailVO(entity);
        log.info("[测试历史] 查询历史详情成功: historyId={}", historyId);
        return detailVO;
    }

    /**
     * 查询规则的所有测试历史
     */
    @Override
    public List<RuleTestHistoryVO> getRuleTestHistory(Long ruleId) {
        log.info("[测试历史] 查询规则测试历史: ruleId={}", ruleId);

        List<RuleTestHistoryEntity> entities = ruleTestHistoryDao.queryHistoryList(
                ruleId, null, null, null, null
        );

        List<RuleTestHistoryVO> vos = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("[测试历史] 查询规则测试历史成功: ruleId={}, count={}", ruleId, vos.size());
        return vos;
    }

    /**
     * 查询最近的测试历史
     */
    @Override
    public List<RuleTestHistoryVO> getRecentHistory(Integer limit) {
        log.info("[测试历史] 查询最近测试历史: limit={}", limit);

        List<RuleTestHistoryEntity> entities = ruleTestHistoryDao.queryRecentHistory(limit);

        List<RuleTestHistoryVO> vos = entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        log.info("[测试历史] 查询最近测试历史成功: count={}", vos.size());
        return vos;
    }

    /**
     * 统计规则测试次数
     */
    @Override
    public Integer countRuleTests(Long ruleId) {
        log.info("[测试历史] 统计规则测试次数: ruleId={}", ruleId);

        Integer count = ruleTestHistoryDao.countByRuleId(ruleId);
        log.info("[测试历史] 统计规则测试次数成功: ruleId={}, count={}", ruleId, count);
        return count;
    }

    /**
     * 删除测试历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHistory(Long historyId) {
        log.info("[测试历史] 删除测试历史: historyId={}", historyId);

        int count = ruleTestHistoryDao.deleteById(historyId);
        if (count > 0) {
            log.info("[测试历史] 删除成功: historyId={}", historyId);
        } else {
            log.warn("[测试历史] 删除失败，记录不存在: historyId={}", historyId);
        }
    }

    /**
     * 批量删除测试历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteHistory(List<Long> historyIds) {
        log.info("[测试历史] 批量删除测试历史: count={}", historyIds.size());

        int count = 0;
        for (Long historyId : historyIds) {
            count += ruleTestHistoryDao.deleteById(historyId);
        }

        log.info("[测试历史] 批量删除成功: count={}", count);
    }

    /**
     * 清理过期历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer cleanExpiredHistory(Integer days) {
        log.info("[测试历史] 清理过期历史: days={}", days);

        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        Integer count = ruleTestHistoryDao.deleteExpiredHistory(beforeTime);

        log.info("[测试历史] 清理过期历史成功: days={}, count={}", days, count);
        return count;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为实体
     */
    private RuleTestHistoryEntity convertToEntity(RuleTestHistoryDetailVO detailVO) {
        try {
            return RuleTestHistoryEntity.builder()
                    .historyId(detailVO.getHistoryId())
                    .testId(detailVO.getTestId())
                    .ruleId(detailVO.getRuleId())
                    .ruleName(detailVO.getRuleName())
                    .ruleCondition(detailVO.getRuleCondition())
                    .ruleAction(detailVO.getRuleAction())
                    .testResult(detailVO.getTestResult())
                    .resultMessage(detailVO.getResultMessage())
                    .conditionMatched(detailVO.getConditionMatched())
                    .executionTime(detailVO.getExecutionTime())
                    .executedActions(detailVO.getExecutedActions() != null
                            ? objectMapper.writeValueAsString(detailVO.getExecutedActions())
                            : null)
                    .executionLogs(detailVO.getExecutionLogs() != null
                            ? objectMapper.writeValueAsString(detailVO.getExecutionLogs())
                            : null)
                    .errorMessage(detailVO.getErrorMessage())
                    .testInputData(detailVO.getTestInputData() != null
                            ? objectMapper.writeValueAsString(detailVO.getTestInputData())
                            : null)
                    .testOutputData(detailVO.getTestOutputData() != null
                            ? objectMapper.writeValueAsString(detailVO.getTestOutputData())
                            : null)
                    .testTimestamp(detailVO.getTestTimestamp())
                    .createTime(LocalDateTime.now())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("[测试历史] JSON序列化失败", e);
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * 转换为VO
     */
    private RuleTestHistoryVO convertToVO(RuleTestHistoryEntity entity) {
        return RuleTestHistoryVO.builder()
                .historyId(entity.getHistoryId())
                .testId(entity.getTestId())
                .ruleId(entity.getRuleId())
                .ruleName(entity.getRuleName())
                .testResult(entity.getTestResult())
                .resultMessage(entity.getResultMessage())
                .conditionMatched(entity.getConditionMatched())
                .executionTime(entity.getExecutionTime())
                .testScenario(entity.getTestScenario())
                .testUserId(entity.getTestUserId())
                .testUserName(entity.getTestUserName())
                .testTimestamp(entity.getTestTimestamp())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 转换为详情VO
     */
    private RuleTestHistoryDetailVO convertToDetailVO(RuleTestHistoryEntity entity) {
        RuleTestHistoryDetailVO detailVO = RuleTestHistoryDetailVO.detailBuilder()
                .historyId(entity.getHistoryId())
                .testId(entity.getTestId())
                .ruleId(entity.getRuleId())
                .ruleName(entity.getRuleName())
                .testResult(entity.getTestResult())
                .resultMessage(entity.getResultMessage())
                .conditionMatched(entity.getConditionMatched())
                .executionTime(entity.getExecutionTime())
                .testScenario(entity.getTestScenario())
                .testUserId(entity.getTestUserId())
                .testUserName(entity.getTestUserName())
                .testTimestamp(entity.getTestTimestamp())
                .createTime(entity.getCreateTime())
                .build();

        // 解析JSON字段
        try {
            detailVO.setRuleCondition(entity.getRuleCondition());
            detailVO.setRuleAction(entity.getRuleAction());

            if (entity.getExecutedActions() != null) {
                detailVO.setExecutedActions(
                        objectMapper.readValue(entity.getExecutedActions(),
                                new TypeReference<List<RuleTestResultVO.ActionExecutionVO>>() {})
                );
            }

            if (entity.getExecutionLogs() != null) {
                detailVO.setExecutionLogs(
                        objectMapper.readValue(entity.getExecutionLogs(),
                                new TypeReference<List<RuleTestResultVO.ExecutionLogVO>>() {})
                );
            }

            detailVO.setErrorMessage(entity.getErrorMessage());

            if (entity.getTestInputData() != null) {
                detailVO.setTestInputData(
                        objectMapper.readValue(entity.getTestInputData(),
                                new TypeReference<Map<String, Object>>() {})
                );
            }

            if (entity.getTestOutputData() != null) {
                detailVO.setTestOutputData(
                        objectMapper.readValue(entity.getTestOutputData(),
                                new TypeReference<Map<String, Object>>() {})
                );
            }

        } catch (JsonProcessingException e) {
            log.error("[测试历史] JSON解析失败", e);
        }

        return detailVO;
    }

    /**
     * 导出测试历史为JSON
     */
    @Override
    public String exportHistoryToJson(List<Long> historyIds) {
        log.info("[测试历史] 导出测试历史: count={}", historyIds.size());

        try {
            // 查询测试历史详情
            List<RuleTestHistoryDetailVO> detailList = new ArrayList<>();
            for (Long historyId : historyIds) {
                RuleTestHistoryDetailVO detail = getHistoryDetail(historyId);
                if (detail != null) {
                    detailList.add(detail);
                }
            }

            // 构建导出数据结构
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("version", "1.0");
            exportData.put("exportTime", LocalDateTime.now().toString());
            exportData.put("totalCount", detailList.size());
            exportData.put("testHistoryList", detailList);

            // 转换为JSON字符串
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exportData);

            log.info("[测试历史] 导出测试历史成功: count={}", detailList.size());
            return jsonData;

        } catch (Exception e) {
            log.error("[测试历史] 导出测试历史失败: error={}", e.getMessage(), e);
            throw new RuntimeException("导出测试历史失败", e);
        }
    }

    /**
     * 从JSON导入测试历史
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer importHistoryFromJson(String jsonData) {
        log.info("[测试历史] 导入测试历史: dataLength={}", jsonData != null ? jsonData.length() : 0);

        try {
            // 解析JSON数据
            Map<String, Object> importData = objectMapper.readValue(jsonData,
                    new TypeReference<Map<String, Object>>() {});

            // 获取测试历史列表
            List<Map<String, Object>> historyList = (List<Map<String, Object>>) importData.get("testHistoryList");

            if (historyList == null || historyList.isEmpty()) {
                log.warn("[测试历史] 导入数据为空");
                return 0;
            }

            // 批量导入
            int count = 0;
            for (Map<String, Object> historyData : historyList) {
                try {
                    // 转换为DetailVO
                    RuleTestHistoryDetailVO detailVO = objectMapper.convertValue(historyData,
                            RuleTestHistoryDetailVO.class);

                    // 生成新的历史ID（设置为null让数据库自动分配）
                    detailVO.setHistoryId(null);

                    // 保存到数据库
                    saveHistory(detailVO);
                    count++;

                } catch (Exception e) {
                    log.error("[测试历史] 导入单条记录失败: data={}, error={}",
                            historyData, e.getMessage());
                }
            }

            log.info("[测试历史] 导入测试历史成功: total={}, imported={}",
                    historyList.size(), count);
            return count;

        } catch (Exception e) {
            log.error("[测试历史] 导入测试历史失败: error={}", e.getMessage(), e);
            throw new RuntimeException("导入测试历史失败", e);
        }
    }

    /**
     * 导出测试配置模板
     */
    @Override
    public String exportTestTemplate() {
        log.info("[测试历史] 导出测试配置模板");

        try {
            // 构建模板数据结构
            Map<String, Object> template = new LinkedHashMap<>();
            template.put("version", "1.0");
            template.put("description", "规则测试历史配置模板");
            template.put("exportTime", LocalDateTime.now().toString());

            // 示例测试历史
            List<Map<String, Object>> examples = new ArrayList<>();

            // 示例1：迟到规则测试
            Map<String, Object> example1 = new LinkedHashMap<>();
            example1.put("testId", "test-template-001");
            example1.put("ruleId", 1L);
            example1.put("ruleName", "迟到扣款规则");
            example1.put("ruleCondition", "{\"lateMinutes\": 5}");
            example1.put("ruleAction", "{\"deductAmount\": 50}");
            example1.put("testResult", "MATCH");
            example1.put("resultMessage", "规则匹配成功，迟到10分钟，扣款50元");
            example1.put("conditionMatched", true);
            example1.put("executionTime", 150L);
            example1.put("testScenario", "LATE");
            example1.put("testUserId", 1L);
            example1.put("testUserName", "admin");
            example1.put("testTimestamp", LocalDateTime.now().toString());

            // 示例2：正常打卡测试
            Map<String, Object> example2 = new LinkedHashMap<>();
            example2.put("testId", "test-template-002");
            example2.put("ruleId", 1L);
            example2.put("ruleName", "迟到扣款规则");
            example2.put("ruleCondition", "{\"lateMinutes\": 5}");
            example2.put("ruleAction", "{\"deductAmount\": 50}");
            example2.put("testResult", "NOT_MATCH");
            example2.put("resultMessage", "条件不匹配，正常打卡，未迟到");
            example2.put("conditionMatched", false);
            example2.put("executionTime", 80L);
            example2.put("testScenario", "NORMAL");
            example2.put("testUserId", 1L);
            example2.put("testUserName", "admin");
            example2.put("testTimestamp", LocalDateTime.now().toString());

            examples.add(example1);
            examples.add(example2);

            template.put("testHistoryList", examples);

            // 转换为JSON字符串
            String jsonTemplate = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(template);

            log.info("[测试历史] 导出测试配置模板成功");
            return jsonTemplate;

        } catch (Exception e) {
            log.error("[测试历史] 导出测试配置模板失败: error={}", e.getMessage(), e);
            throw new RuntimeException("导出测试配置模板失败", e);
        }
    }
}
