package net.lab1024.sa.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 游标分页查询模板 - P0级性能优化
 * 替代传统的LIMIT offset, size深度分页
 *
 * 使用说明：
 * 1. 替换现有的QueryForm类
 * 2. 修改DAO查询方法
 * 3. 更新Service分页逻辑
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class CursorPaginationQueryForm {

    /**
     * 页面大小
     */
    private Integer pageSize = 20;

    /**
     * 最后记录ID（游标分页关键字段）
     */
    private Long lastId;

    /**
     * 最后记录时间（时间排序分页关键字段）
     */
    private java.time.LocalDateTime lastTime;

    /**
     * 查询方向
     * ASC: 向前翻页
     * DESC: 向后翻页
     */
    private String direction = "DESC";

    /**
     * 是否第一页查询
     */
    private Boolean firstPage = true;

    // 原有查询条件保持不变
    private String keyword;
    private Long userId;
    private Long deviceId;
    private Long areaId;
    private Integer status;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;

    // =====================================================
    // 游标分页工具方法
    // =====================================================

    /**
     * 初始化为第一页查询
     */
    public void resetToFirstPage() {
        this.firstPage = true;
        this.lastId = null;
        this.lastTime = null;
    }

    /**
     * 设置下一页查询参数
     */
    public void setNextPage(Long lastId, java.time.LocalDateTime lastTime) {
        this.firstPage = false;
        this.lastId = lastId;
        this.lastTime = lastTime;
    }

    /**
     * 设置上一页查询参数
     */
    public void setPreviousPage(Long firstId, java.time.LocalDateTime firstTime) {
        this.firstPage = false;
        this.lastId = firstId;
        this.lastTime = firstTime;
        this.direction = "ASC";
    }

    /**
     * 检查是否有下一页
     */
    public boolean hasNextPage() {
        return !firstPage && "DESC".equals(direction);
    }

    /**
     * 检查是否有上一页
     */
    public boolean hasPreviousPage() {
        return !firstPage && "ASC".equals(direction);
    }

    /**
     * 验证查询参数
     */
    public void validate() {
        if (pageSize == null || pageSize <= 0 || pageSize > 1000) {
            throw new IllegalArgumentException("页面大小必须在1-1000之间");
        }

        if (!firstPage && lastId == null && lastTime == null) {
            throw new IllegalArgumentException("非第一页查询必须设置lastId或lastTime");
        }
    }
}