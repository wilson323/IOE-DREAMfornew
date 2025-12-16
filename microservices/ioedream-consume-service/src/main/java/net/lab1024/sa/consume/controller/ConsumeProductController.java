package net.lab1024.sa.consume.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.service.ConsumeProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器（管理端）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/product")
public class ConsumeProductController {

    @Resource
    private ConsumeProductService consumeProductService;

    @GetMapping("/query")
    public ResponseDTO<PageResult<ConsumeProductVO>> query(ConsumeProductQueryForm queryForm) {
        try {
            return ResponseDTO.ok(consumeProductService.queryProducts(queryForm));
        } catch (Exception e) {
            log.error("[商品管理] 查询商品失败, queryForm={}", queryForm, e);
            return ResponseDTO.error("QUERY_ERROR", "查询商品失败");
        }
    }

    @PostMapping("/{productId}/available")
    public ResponseDTO<Void> setAvailable(@PathVariable("productId") String productId, @RequestParam("available") Boolean available) {
        try {
            consumeProductService.setAvailable(productId, available);
            return ResponseDTO.ok();
        } catch (BusinessException e) {
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[商品管理] 更新商品状态失败, productId={}, available={}", productId, available, e);
            return ResponseDTO.error("UPDATE_ERROR", "更新商品状态失败");
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseDTO<Void> delete(@PathVariable("productId") String productId) {
        try {
            consumeProductService.deleteById(productId);
            return ResponseDTO.ok();
        } catch (BusinessException e) {
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[商品管理] 删除商品失败, productId={}", productId, e);
            return ResponseDTO.error("DELETE_ERROR", "删除商品失败");
        }
    }
}




