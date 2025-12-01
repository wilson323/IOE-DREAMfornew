package net.lab1024.sa.common.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 分页结果封装
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Data
public class PageResult<T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 是否为空结果
     */
    private Boolean emptyFlag;

    /**
     * 获取总记录数
     */
    public Long getTotal() {
        return total;
    }

    /**
     * 获取总页数
     */
    public Long getPages() {
        return pages;
    }

    /**
     * 获取空结果标记
     */
    public Boolean getEmptyFlag() {
        return emptyFlag;
    }

    /**
     * 获取数据列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 设置数据列表
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * 设置当前页码
     */
    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 设置每页条数
     */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 设置总记录数
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * 设置总页数
     */
    public void setPages(Long pages) {
        this.pages = pages;
    }

    /**
     * 设置空结果标记
     */
    public void setEmptyFlag(Boolean emptyFlag) {
        this.emptyFlag = emptyFlag;
    }

    // ==================== 兼容性方法 ====================
    // 为了兼容consume-service等使用rows字段的服务

    /**
     * 获取数据行（兼容性方法，等同于getList）
     *
     * @return 数据列表
     */
    public List<T> getRows() {
        return this.list;
    }

    /**
     * 设置数据行（兼容性方法，等同于setList）
     *
     * @param rows 数据列表
     */
    public void setRows(List<T> rows) {
        this.list = rows;
    }

    /**
     * 静态工厂方法：创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((total + pageSize - 1) / pageSize);
        result.setEmptyFlag(list == null || list.isEmpty());
        return result;
    }

    /**
     * 静态工厂方法：创建空的分页结果
     */
    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPageNum(1L);
        result.setPageSize(10L);
        result.setPages(0L);
        result.setEmptyFlag(true);
        return result;
    }

    /**
     * 静态工厂方法：创建分页结果（兼容consume-service的2参数版本）
     *
     * @param list 数据列表
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setList(list != null ? list : new ArrayList<>());
        result.setTotal(total != null ? total : 0L);
        result.setPageNum(1L);
        result.setPageSize(list != null && !list.isEmpty() ? (long) list.size() : 10L);
        result.setPages(total != null && total > 0 ? 1L : 0L);
        result.setEmptyFlag(list == null || list.isEmpty());
        return result;
    }
}
