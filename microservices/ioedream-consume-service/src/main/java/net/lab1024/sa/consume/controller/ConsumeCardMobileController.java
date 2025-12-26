package net.lab1024.sa.consume.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeCardLossForm;
import net.lab1024.sa.consume.domain.form.ConsumeCardUnlockForm;
import net.lab1024.sa.consume.domain.vo.ConsumeCardOperationVO;
import net.lab1024.sa.consume.domain.vo.ConsumeCardVO;
import net.lab1024.sa.consume.service.ConsumeCardService;

/**
 * 移动端卡片管理控制器
 * <p>
 * 提供移动端卡片管理功能，包括：
 * 1. 卡片状态查询
 * 2. 卡片挂失申请
 * 3. 卡片解挂操作
 * 4. 操作历史记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/card")
@Tag(name = "移动端卡片管理", description = "移动端卡片管理接口")
@Slf4j
public class ConsumeCardMobileController {

    @Resource
    private ConsumeCardService consumeCardService;

    @Operation(summary = "获取用户卡片状态", description = "获取用户当前所有卡片的状态信息")
    @GetMapping("/status/{userId}")
    public ResponseDTO<List<ConsumeCardVO>> getCardStatus(@PathVariable Long userId) {
        log.info("[移动端卡片] 查询卡片状态: userId={}", userId);
        try {
            List<ConsumeCardVO> cards = consumeCardService.getUserCards(userId);
            log.info("[移动端卡片] 查询卡片状态成功: userId={}, count={}", userId, cards.size());
            return ResponseDTO.ok(cards);
        } catch (Exception e) {
            log.error("[移动端卡片] 查询卡片状态异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "申请卡片挂失", description = "用户申请挂失指定卡片")
    @PostMapping("/loss")
    public ResponseDTO<Void> reportLoss(@Valid @RequestBody ConsumeCardLossForm form) {
        log.info("[移动端卡片] 申请卡片挂失: userId={}, cardId={}, reason={}",
            form.getUserId(), form.getCardId(), form.getLossReason());
        try {
            consumeCardService.reportCardLoss(form);
            log.info("[移动端卡片] 申请卡片挂失成功: userId={}, cardId={}",
                form.getUserId(), form.getCardId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端卡片] 申请卡片挂失异常: userId={}, cardId={}, error={}",
                form.getUserId(), form.getCardId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "申请卡片解挂", description = "用户申请解除挂失，恢复卡片使用")
    @PostMapping("/unlock")
    public ResponseDTO<Void> reportUnlock(@Valid @RequestBody ConsumeCardUnlockForm form) {
        log.info("[移动端卡片] 申请卡片解挂: userId={}, cardId={}, reason={}",
            form.getUserId(), form.getCardId(), form.getUnlockReason());
        try {
            consumeCardService.reportCardUnlock(form);
            log.info("[移动端卡片] 申请卡片解挂成功: userId={}, cardId={}",
                form.getUserId(), form.getCardId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端卡片] 申请卡片解挂异常: userId={}, cardId={}, error={}",
                form.getUserId(), form.getCardId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取卡片操作历史", description = "分页获取用户的卡片操作记录")
    @GetMapping("/history/{userId}")
    public ResponseDTO<PageResult<ConsumeCardOperationVO>> getOperationHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端卡片] 查询操作历史: userId={}, pageNum={}, pageSize={}",
            userId, pageNum, pageSize);
        try {
            PageResult<ConsumeCardOperationVO> history = consumeCardService.getOperationHistory(
                userId, pageNum, pageSize);
            log.info("[移动端卡片] 查询操作历史成功: userId={}, total={}",
                userId, history.getTotal());
            return ResponseDTO.ok(history);
        } catch (Exception e) {
            log.error("[移动端卡片] 查询操作历史异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取卡片详情", description = "获取指定卡片的详细信息")
    @GetMapping("/detail/{cardId}")
    public ResponseDTO<ConsumeCardVO> getCardDetail(@PathVariable Long cardId) {
        log.info("[移动端卡片] 查询卡片详情: cardId={}", cardId);
        try {
            ConsumeCardVO card = consumeCardService.getCardDetail(cardId);
            log.info("[移动端卡片] 查询卡片详情成功: cardId={}, physicalNo={}",
                cardId, card != null ? card.getPhysicalCardNo() : null);
            return ResponseDTO.ok(card);
        } catch (Exception e) {
            log.error("[移动端卡片] 查询卡片详情异常: cardId={}, error={}", cardId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "验证卡片密码", description = "验证卡片密码是否正确（用于敏感操作）")
    @PostMapping("/verify-password")
    public ResponseDTO<Boolean> verifyPassword(
            @RequestParam Long userId,
            @RequestParam String cardPassword) {
        log.info("[移动端卡片] 验证卡片密码: userId={}", userId);
        try {
            boolean valid = consumeCardService.verifyCardPassword(userId, cardPassword);
            log.info("[移动端卡片] 验证卡片密码成功: userId={}, valid={}", userId, valid);
            return ResponseDTO.ok(valid);
        } catch (Exception e) {
            log.error("[移动端卡片] 验证卡片密码异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取卡片统计信息", description = "获取用户卡片使用统计")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getCardStatistics(@PathVariable Long userId) {
        log.info("[移动端卡片] 查询卡片统计: userId={}", userId);
        try {
            Map<String, Object> statistics = consumeCardService.getCardStatistics(userId);
            log.info("[移动端卡片] 查询卡片统计成功: userId={}", userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端卡片] 查询卡片统计异常: userId={}, error={}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
