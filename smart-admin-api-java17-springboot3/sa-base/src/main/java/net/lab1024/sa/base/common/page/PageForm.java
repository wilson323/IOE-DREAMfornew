package net.lab1024.sa.base.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2020/04/28 16:19
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright  <a href"https://1024lab.net">1024创新实验室</a>
 */
@Data
@Schema(description = "分页查询参数")
public class PageForm {

    /**
     * 当前页
     */
    @Schema(description = "当前页", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页的数量
     */
    @Schema(description = "每页的数量", example = "20")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    /**
     * 排序方式：asc/desc
     */
    @Schema(description = "排序方式", example = "desc")
    private String sortOrder;

    }