package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.domain.form.RulePerformanceTestForm;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestDetailVO;
import net.lab1024.sa.attendance.domain.vo.RulePerformanceTestResultVO;

import java.util.List;

/**
 * 规则性能测试服务接口
 * <p>
 * 提供性能测试执行和查询功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface RulePerformanceTestService {

    /**
     * 执行性能测试
     *
     * @param testForm 测试请求表单
     * @return 测试结果
     */
    RulePerformanceTestResultVO executePerformanceTest(RulePerformanceTestForm testForm) throws InterruptedException;

    /**
     * 查询测试结果
     *
     * @param testId 测试ID
     * @return 测试结果
     */
    RulePerformanceTestResultVO getTestResult(Long testId);

    /**
     * 查询测试详情
     *
     * @param testId 测试ID
     * @return 测试详情
     */
    RulePerformanceTestDetailVO getTestDetail(Long testId);

    /**
     * 查询最近的测试列表
     *
     * @param limit 限制数量
     * @return 测试列表
     */
    List<RulePerformanceTestResultVO> getRecentTests(Integer limit);

    /**
     * 取消正在运行的测试
     *
     * @param testId 测试ID
     */
    void cancelTest(Long testId);

    /**
     * 删除测试记录
     *
     * @param testId 测试ID
     */
    void deleteTest(Long testId);

    /**
     * 批量删除测试记录
     *
     * @param testIds 测试ID列表
     */
    void batchDeleteTests(List<Long> testIds);
}
