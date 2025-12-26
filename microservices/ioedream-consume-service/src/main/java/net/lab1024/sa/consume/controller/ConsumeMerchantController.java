package net.lab1024.sa.consume.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeMerchantUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMerchantVO;
import net.lab1024.sa.consume.service.ConsumeMerchantService;
import lombok.extern.slf4j.Slf4j;

/**
 * 商户管理控制器
 * <p>
 * 提供商户的管理功能，包括：
 * 1. 商户基本信息管理
 * 2. 商户设备管理
 * 3. 商户结算管理
 * 4. 商户状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_MERCHANT_MANAGE", description = "商户管理权限")
@RequestMapping("/api/v1/consume/merchants")
@Tag(name = "商户管理", description = "商户管理、设备管理、结算等功能")
public class ConsumeMerchantController {

    @Resource
    private ConsumeMerchantService consumeMerchantService;

    /**
     * 分页查询商户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询商户", description = "根据条件分页查询商户列表")
    public ResponseDTO<PageResult<ConsumeMerchantVO>> queryMerchants(ConsumeMerchantQueryForm queryForm) {
        PageResult<ConsumeMerchantVO> result = consumeMerchantService.queryMerchants(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取商户详情
     *
     * @param merchantId 商户ID
     * @return 商户详情
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商户详情", description = "根据商户ID获取详细的商户信息")
    public ResponseDTO<ConsumeMerchantVO> getMerchantDetail(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId) {
        ConsumeMerchantVO merchant = consumeMerchantService.getMerchantDetail(merchantId);
        return ResponseDTO.ok(merchant);
    }

    /**
     * 新增商户
     *
     * @param addForm 商户新增表单
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增商户", description = "创建新的商户")
    public ResponseDTO<Long> addMerchant(@Valid @RequestBody ConsumeMerchantAddForm addForm) {
        Long merchantId = consumeMerchantService.addMerchant(addForm);
        return ResponseDTO.ok(merchantId);
    }

    /**
     * 更新商户信息
     *
     * @param merchantId 商户ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @PutMapping("/{merchantId}")
    @Operation(summary = "更新商户信息", description = "更新商户的基本信息")
    public ResponseDTO<Void> updateMerchant(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId,
            @Valid @RequestBody ConsumeMerchantUpdateForm updateForm) {
        consumeMerchantService.updateMerchant(merchantId, updateForm);
        return ResponseDTO.ok();
    }

    /**
     * 删除商户
     *
     * @param merchantId 商户ID
     * @return 删除结果
     */
    @DeleteMapping("/{merchantId}")
    @Operation(summary = "删除商户", description = "删除指定的商户")
    public ResponseDTO<Void> deleteMerchant(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId) {
        consumeMerchantService.deleteMerchant(merchantId);
        return ResponseDTO.ok();
    }

    /**
     * 启用商户
     *
     * @param merchantId 商户ID
     * @return 启用结果
     */
    @PutMapping("/{merchantId}/enable")
    @Operation(summary = "启用商户", description = "启用指定的商户")
    public ResponseDTO<Void> enableMerchant(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId) {
        consumeMerchantService.enableMerchant(merchantId);
        return ResponseDTO.ok();
    }

    /**
     * 禁用商户
     *
     * @param merchantId 商户ID
     * @return 禁用结果
     */
    @PutMapping("/{merchantId}/disable")
    @Operation(summary = "禁用商户", description = "禁用指定的商户")
    public ResponseDTO<Void> disableMerchant(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId) {
        consumeMerchantService.disableMerchant(merchantId);
        return ResponseDTO.ok();
    }

    /**
     * 获取活跃商户列表
     *
     * @return 活跃商户列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃商户", description = "获取所有活跃状态的商户列表")
    public ResponseDTO<List<ConsumeMerchantVO>> getActiveMerchants() {
        List<ConsumeMerchantVO> merchants = consumeMerchantService.getActiveMerchants();
        return ResponseDTO.ok(merchants);
    }

    /**
     * 按区域查询商户
     *
     * @param areaId 区域ID
     * @return 商户列表
     */
    @GetMapping("/area/{areaId}")
    @Operation(summary = "按区域查询商户", description = "根据区域ID查询商户列表")
    public ResponseDTO<List<ConsumeMerchantVO>> getMerchantsByArea(
            @Parameter(description = "区域ID", required = true) @PathVariable Long areaId) {
        List<ConsumeMerchantVO> merchants = consumeMerchantService.getMerchantsByArea(areaId);
        return ResponseDTO.ok(merchants);
    }

    /**
     * 商户结算统计
     *
     * @param merchantId 商户ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 结算统计
     */
    @GetMapping("/{merchantId}/settlement")
    @Operation(summary = "商户结算统计", description = "获取商户的结算统计信息")
    public ResponseDTO<java.util.Map<String, Object>> getMerchantSettlement(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {
        java.util.Map<String, Object> settlement = consumeMerchantService.getMerchantSettlement(merchantId, startDate,
                endDate);
        return ResponseDTO.ok(settlement);
    }

    /**
     * 批量分配设备
     *
     * @param merchantId 商户ID
     * @param deviceIds  设备ID列表
     * @return 分配结果
     */
    @PostMapping("/{merchantId}/devices/assign")
    @Operation(summary = "批量分配设备", description = "为商户批量分配设备")
    public ResponseDTO<Void> assignDevices(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId,
            @Parameter(description = "设备ID列表", required = true) @RequestParam List<String> deviceIds) {
        consumeMerchantService.assignDevices(merchantId, deviceIds);
        return ResponseDTO.ok();
    }

    /**
     * 获取商户设备列表
     *
     * @param merchantId 商户ID
     * @return 设备列表
     */
    @GetMapping("/{merchantId}/devices")
    @Operation(summary = "获取商户设备", description = "获取商户关联的设备列表")
    public ResponseDTO<List<String>> getMerchantDevices(
            @Parameter(description = "商户ID", required = true) @PathVariable Long merchantId) {
        List<String> devices = consumeMerchantService.getMerchantDevices(merchantId);
        return ResponseDTO.ok(devices);
    }
}
