package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.visitor.domain.form.BlacklistQueryForm;
import net.lab1024.sa.visitor.entity.VisitorBlacklistEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
// 已移除Repository导入，统一使用@Mapper注解

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客黑名单数据访问层
 * <p>
 * 内存优化设计：
 * - 优化SQL查询，避免全表扫描
 * - 使用复合索引提高查询性能
 * - 分页查询限制数据量
 * - 批量操作减少数据库交互
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
// @Repository  // 禁止使用@Repository注解，统一使用@Mapper
public interface VisitorBlacklistDao extends BaseMapper<VisitorBlacklistEntity> {

    /**
     * 根据访客ID查询黑名单记录
     *
     * @param visitorId 访客ID
     * @return 黑名单记录列表
     */
    @Select("SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE visitor_id = #{visitorId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<VisitorBlacklistEntity> selectByVisitorId(@Param("visitorId") Long visitorId);

    /**
     * 根据身份证号查询黑名单记录
     *
     * @param idCard 身份证号
     * @return 黑名单记录
     */
    @Select("SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE id_card = #{idCard} AND status = 1 AND deleted_flag = 0 " +
            "LIMIT 1")
    VisitorBlacklistEntity selectByIdCard(@Param("idCard") String idCard);

    /**
     * 根据手机号查询黑名单记录
     *
     * @param phone 手机号
     * @return 黑名单记录
     */
    @Select("SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE phone = #{phone} AND status = 1 AND deleted_flag = 0 " +
            "LIMIT 1")
    VisitorBlacklistEntity selectByPhone(@Param("phone") String phone);

    /**
     * 检查访客是否在生效黑名单中
     *
     * @param visitorId 访客ID
     * @return 是否在黑名单中
     */
    @Select("SELECT COUNT(1) > 0 " +
            "FROM t_visitor_blacklist " +
            "WHERE visitor_id = #{visitorId} AND status = 1 AND deleted_flag = 0 " +
            "AND (end_time IS NULL OR end_time > NOW())")
    boolean existsActiveBlacklist(@Param("visitorId") Long visitorId);

    /**
     * 根据条件查询黑名单记录（分页）
     *
     * @param form 查询条件
     * @return 黑名单记录列表
     */
    default List<VisitorBlacklistEntity> selectByCondition(BlacklistQueryForm form) {
        LambdaQueryWrapper<VisitorBlacklistEntity> wrapper = new LambdaQueryWrapper<>();

        // 基础条件：未删除
        wrapper.eq(VisitorBlacklistEntity::getDeletedFlag, 0);

        // 访客ID
        if (form.getVisitorId() != null) {
            wrapper.eq(VisitorBlacklistEntity::getVisitorId, form.getVisitorId());
        }

        // 访客姓名
        if (form.getVisitorName() != null && !form.getVisitorName().trim().isEmpty()) {
            wrapper.like(VisitorBlacklistEntity::getVisitorName, form.getVisitorName().trim());
        }

        // 身份证号
        if (form.getIdCard() != null && !form.getIdCard().trim().isEmpty()) {
            wrapper.eq(VisitorBlacklistEntity::getIdCard, form.getIdCard().trim());
        }

        // 手机号
        if (form.getPhone() != null && !form.getPhone().trim().isEmpty()) {
            wrapper.eq(VisitorBlacklistEntity::getPhone, form.getPhone().trim());
        }

        // 黑名单类型
        if (form.getBlacklistType() != null && !form.getBlacklistType().trim().isEmpty()) {
            wrapper.eq(VisitorBlacklistEntity::getBlacklistType, form.getBlacklistType().trim());
        }

        // 状态
        if (form.getStatus() != null) {
            wrapper.eq(VisitorBlacklistEntity::getStatus, form.getStatus());
        }

        // 操作人姓名
        if (form.getOperatorName() != null && !form.getOperatorName().trim().isEmpty()) {
            wrapper.like(VisitorBlacklistEntity::getOperatorName, form.getOperatorName().trim());
        }

        // 创建时间范围
        if (form.getStartTime() != null) {
            wrapper.ge(VisitorBlacklistEntity::getCreateTime, form.getStartTime());
        }
        if (form.getEndTime() != null) {
            wrapper.le(VisitorBlacklistEntity::getCreateTime, form.getEndTime());
        }

        // 黑名单时间范围
        if (form.getBlacklistStartTime() != null) {
            wrapper.ge(VisitorBlacklistEntity::getStartTime, form.getBlacklistStartTime());
        }
        if (form.getBlacklistEndTime() != null) {
            wrapper.le(VisitorBlacklistEntity::getStartTime, form.getBlacklistEndTime());
        }

        // 关键字搜索（访客姓名、手机号、身份证号、原因）
        if (form.getKeyword() != null && !form.getKeyword().trim().isEmpty()) {
            String keyword = form.getKeyword().trim();
            wrapper.and(w -> w.like(VisitorBlacklistEntity::getVisitorName, keyword)
                    .or().like(VisitorBlacklistEntity::getPhone, keyword)
                    .or().like(VisitorBlacklistEntity::getIdCard, keyword)
                    .or().like(VisitorBlacklistEntity::getBlacklistReason, keyword));
        }

        // 排序
        if (form.getSortBy() != null && !form.getSortBy().trim().isEmpty()) {
            switch (form.getSortBy()) {
                case "createTime":
                    wrapper.orderBy(true, "desc".equals(form.getSortDirection()),
                            VisitorBlacklistEntity::getCreateTime);
                    break;
                case "updateTime":
                    wrapper.orderBy(true, "desc".equals(form.getSortDirection()),
                            VisitorBlacklistEntity::getUpdateTime);
                    break;
                case "blacklistId":
                    wrapper.orderBy(true, "desc".equals(form.getSortDirection()),
                            VisitorBlacklistEntity::getBlacklistId);
                    break;
                default:
                    wrapper.orderByDesc(VisitorBlacklistEntity::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(VisitorBlacklistEntity::getCreateTime);
        }

        // 限制返回数量（避免内存溢出）
        Page<VisitorBlacklistEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        return this.selectPage(page, wrapper).getRecords();
    }

    /**
     * 查询过期黑名单记录
     *
     * @return 过期记录列表
     */
    @Select("SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE status = 1 AND end_time IS NOT NULL AND end_time < NOW() AND deleted_flag = 0 " +
            "ORDER BY end_time ASC " +
            "LIMIT 1000")
    List<VisitorBlacklistEntity> selectExpiredRecords();

    /**
     * 获取黑名单统计数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN blacklist_type = 'PERMANENT' THEN 1 ELSE 0 END) as permanentCount, " +
            "SUM(CASE WHEN blacklist_type = 'TEMPORARY' THEN 1 ELSE 0 END) as temporaryCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as activeCount, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as inactiveCount " +
            "FROM t_visitor_blacklist " +
            "WHERE deleted_flag = 0 " +
            "AND create_time >= #{startTime} " +
            "AND create_time <= #{endTime}")
    Object selectStatistics(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 批量更新过期记录状态
     *
     * @param ids 记录ID列表
     * @return 更新数量
     */
    @Update("UPDATE t_visitor_blacklist " +
            "SET status = 2, update_time = NOW() " +
            "WHERE blacklist_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND deleted_flag = 0")
    int batchUpdateExpiredStatus(@Param("ids") List<Long> ids);

    /**
     * 根据访客ID列表批量查询
     *
     * @param visitorIds 访客ID列表
     * @return 黑名单记录列表
     */
    @Select("<script>" +
            "SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE visitor_id IN " +
            "<foreach collection='visitorIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<VisitorBlacklistEntity> selectByVisitorIds(@Param("visitorIds") List<Long> visitorIds);

    /**
     * 查询即将过期的临时黑名单（未来7天内）
     *
     * @return 即将过期记录列表
     */
    @Select("SELECT blacklist_id, visitor_id, visitor_name, id_card, phone, blacklist_type, " +
            "blacklist_reason, operator_id, operator_name, start_time, end_time, status, " +
            "create_time, update_time, deleted_flag " +
            "FROM t_visitor_blacklist " +
            "WHERE blacklist_type = 'TEMPORARY' AND status = 1 " +
            "AND end_time IS NOT NULL AND end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) " +
            "AND deleted_flag = 0 " +
            "ORDER BY end_time ASC " +
            "LIMIT 100")
    List<VisitorBlacklistEntity> selectSoonToExpireRecords();

    /**
     * 根据操作人查询黑名单数量
     *
     * @param operatorId 操作人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 数量
     */
    @Select("SELECT COUNT(*) " +
            "FROM t_visitor_blacklist " +
            "WHERE operator_id = #{operatorId} " +
            "AND create_time >= #{startTime} " +
            "AND create_time <= #{endTime} " +
            "AND deleted_flag = 0")
    int countByOperator(@Param("operatorId") Long operatorId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}