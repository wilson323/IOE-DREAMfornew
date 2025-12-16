package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 访客导出服务接口
 * <p>
 * 访客数据导出核心业务服务
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorExportService {

    /**
     * 导出访客数据
     *
     * @param query 查询条件
     * @return 导出文件路径
     */
    ResponseDTO<String> exportVisitorData(Object query);

    /**
     * 导出访问记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出文件路径
     */
    String exportRecords(String startDate, String endDate);
}

