package net.lab1024.sa.consume.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果转换工具类
 * <p>
 * 统一处理IPage到PageResult的转换
 * 解决MyBatis-Plus IPage与项目PageResult的类型不一致问题
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
public final class PageResultConverter {

    private PageResultConverter() {
        throw new AssertionError("工具类不应被实例化");
    }

    /**
     * IPage转换为PageResult
     *
     * @param iPage MyBatis-Plus分页结果
     * @param <T>   数据类型
     * @return PageResult分页结果
     */
    public static <T> PageResult<T> of(IPage<T> iPage) {
        if (iPage == null) {
            return new PageResult<>();
        }

        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(iPage.getRecords());
        pageResult.setTotal(iPage.getTotal());
        pageResult.setPageNum((int) iPage.getCurrent());
        pageResult.setPageSize((int) iPage.getSize());
        pageResult.setPages((int) iPage.getPages());

        return pageResult;
    }

    /**
     * IPage转换为PageResult（带数据转换）
     *
     * @param iPage    MyBatis-Plus分页结果
     * @param mapper   数据转换函数
     * @param <S>      源数据类型
     * @param <T>      目标数据类型
     * @return PageResult分页结果
     */
    public static <S, T> PageResult<T> of(IPage<S> iPage, Function<S, T> mapper) {
        if (iPage == null) {
            return new PageResult<>();
        }

        List<T> convertedRows = iPage.getRecords().stream()
                .map(mapper)
                .collect(Collectors.toList());

        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(convertedRows);
        pageResult.setTotal(iPage.getTotal());
        pageResult.setPageNum((int) iPage.getCurrent());
        pageResult.setPageSize((int) iPage.getSize());
        pageResult.setPages((int) iPage.getPages());

        return pageResult;
    }

    /**
     * IPage转换为PageResult（空结果）
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param <T>      数据类型
     * @return PageResult分页结果
     */
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(List.of());
        pageResult.setTotal(0L);
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setPages(0);

        return pageResult;
    }

    /**
     * ResponseDTO<IPage>转换为PageResult
     *
     * @param response ResponseDTO包装的IPage
     * @param <T>      数据类型
     * @return PageResult分页结果
     */
    public static <T> PageResult<T> fromResponse(ResponseDTO<IPage<T>> response) {
        if (response == null || response.getData() == null) {
            return new PageResult<>();
        }

        return of(response.getData());
    }

    /**
     * ResponseDTO<IPage>转换为PageResult（带数据转换）
     *
     * @param response ResponseDTO包装的IPage
     * @param mapper   数据转换函数
     * @param <S>      源数据类型
     * @param <T>      目标数据类型
     * @return PageResult分页结果
     */
    public static <S, T> PageResult<T> fromResponse(ResponseDTO<IPage<S>> response, Function<S, T> mapper) {
        if (response == null || response.getData() == null) {
            return new PageResult<>();
        }

        return of(response.getData(), mapper);
    }

    /**
     * 创建成功的ResponseDTO<PageResult>
     *
     * @param pageResult PageResult结果
     * @param <T>        数据类型
     * @return ResponseDTO包装的PageResult
     */
    public static <T> ResponseDTO<PageResult<T>> success(PageResult<T> pageResult) {
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 创建成功的ResponseDTO<PageResult>（从IPage）
     *
     * @param iPage MyBatis-Plus分页结果
     * @param <T>   数据类型
     * @return ResponseDTO包装的PageResult
     */
    public static <T> ResponseDTO<PageResult<T>> success(IPage<T> iPage) {
        return ResponseDTO.ok(of(iPage));
    }

    /**
     * 创建成功的ResponseDTO<PageResult>（从IPage，带数据转换）
     *
     * @param iPage  MyBatis-Plus分页结果
     * @param mapper 数据转换函数
     * @param <S>    源数据类型
     * @param <T>    目标数据类型
     * @return ResponseDTO包装的PageResult
     */
    public static <S, T> ResponseDTO<PageResult<T>> success(IPage<S> iPage, Function<S, T> mapper) {
        return ResponseDTO.ok(of(iPage, mapper));
    }

    /**
     * 验证PageResult的有效性
     *
     * @param pageResult 分页结果
     * @param <T>        数据类型
     * @return 是否有效
     */
    public static <T> boolean isValid(PageResult<T> pageResult) {
        return pageResult != null
                && pageResult.getPageNum() != null
                && pageResult.getPageSize() != null
                && pageResult.getTotal() != null
                && pageResult.getList() != null; // 修正：使用getList()而不是getRows()
    }

    /**
     * 获取分页信息摘要
     *
     * @param pageResult 分页结果
     * @return 分页信息字符串
     */
    public static String getSummary(PageResult<?> pageResult) {
        if (pageResult == null) {
            return "PageResult{null}";
        }

        return String.format("PageResult{page=%d, size=%d, total=%d, pages=%d}",
                pageResult.getPageNum(),
                pageResult.getPageSize(),
                pageResult.getTotal(),
                pageResult.getPages());
    }
}


