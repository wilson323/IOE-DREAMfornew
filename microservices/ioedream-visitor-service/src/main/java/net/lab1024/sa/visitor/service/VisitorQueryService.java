package net.lab1024.sa.visitor.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 访客查询服务接口
 * <p>
 * 访客信息查询核心业务服务
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
public interface VisitorQueryService {

    /**
     * 查询访客记录列表
     *
     * @param phone 手机号
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 访客记录列表
     */
    ResponseDTO<?> queryVisitorRecords(String phone, Integer pageNum, Integer pageSize);

    /**
     * 查询预约记录
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 预约记录列表
     */
    ResponseDTO<?> queryAppointments(Long userId, Integer status);

    /**
     * 根据身份证号查询访客信息
     *
     * @param idNumber 身份证号
     * @return 访客信息
     */
    ResponseDTO<?> getVisitorByIdNumber(String idNumber);

    /**
     * 根据被访人ID查询访客列表
     *
     * @param visiteeId 被访人ID
     * @param limit 限制数量
     * @return 访客列表
     */
    ResponseDTO<?> getVisitorsByVisiteeId(Long visiteeId, Integer limit);
}
