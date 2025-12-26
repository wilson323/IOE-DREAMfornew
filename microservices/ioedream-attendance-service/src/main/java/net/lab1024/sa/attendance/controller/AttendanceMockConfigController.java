package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.MockConfigForm;
import net.lab1024.sa.attendance.domain.vo.MockConfigVO;
import net.lab1024.sa.attendance.domain.vo.MockDataVO;
import net.lab1024.sa.attendance.service.MockConfigService;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * Mock配置控制器
 * <p>
 * 提供Mock配置管理相关API接口
 * 支持Mock数据生成和场景管理
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/mock-config")
@Tag(name = "Mock配置管理")
public class AttendanceMockConfigController {

    @Resource
    private MockConfigService mockConfigService;

    /**
     * 创建Mock配置
     *
     * @param form Mock配置表单
     * @return 配置ID
     */
    @Observed(name = "mockConfig.create", contextualName = "create-mock-config")
    @PostMapping
    @Operation(summary = "创建Mock配置", description = "创建新的Mock配置")
    public ResponseDTO<Long> createMockConfig(
            @Valid @RequestBody MockConfigForm form) {
        log.info("[Mock服务] 创建Mock配置: name={}, type={}", form.getConfigName(), form.getConfigType());

        Long configId = mockConfigService.createMockConfig(form);

        log.info("[Mock服务] Mock配置创建成功: configId={}", configId);
        return ResponseDTO.ok(configId);
    }

    /**
     * 更新Mock配置
     *
     * @param form Mock配置表单
     * @return 操作结果
     */
    @Observed(name = "mockConfig.update", contextualName = "update-mock-config")
    @PutMapping
    @Operation(summary = "更新Mock配置", description = "更新Mock配置")
    public ResponseDTO<Void> updateMockConfig(
            @Valid @RequestBody MockConfigForm form) {
        log.info("[Mock服务] 更新Mock配置: configId={}", form.getConfigId());

        mockConfigService.updateMockConfig(form);

        log.info("[Mock服务] Mock配置更新成功: configId={}", form.getConfigId());
        return ResponseDTO.ok();
    }

    /**
     * 删除Mock配置
     *
     * @param configId 配置ID
     * @return 操作结果
     */
    @Observed(name = "mockConfig.delete", contextualName = "delete-mock-config")
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除Mock配置", description = "删除指定的Mock配置")
    public ResponseDTO<Void> deleteMockConfig(
            @PathVariable @Parameter(description = "配置ID", required = true) Long configId) {
        log.info("[Mock服务] 删除Mock配置: configId={}", configId);

        mockConfigService.deleteMockConfig(configId);

        log.info("[Mock服务] Mock配置删除成功: configId={}", configId);
        return ResponseDTO.ok();
    }

    /**
     * 查询Mock配置
     *
     * @param configId 配置ID
     * @return Mock配置VO
     */
    @Observed(name = "mockConfig.get", contextualName = "get-mock-config")
    @GetMapping("/{configId}")
    @Operation(summary = "查询Mock配置", description = "根据ID查询Mock配置")
    public ResponseDTO<MockConfigVO> getMockConfig(
            @PathVariable @Parameter(description = "配置ID", required = true) Long configId) {
        log.info("[Mock服务] 查询Mock配置: configId={}", configId);

        MockConfigVO config = mockConfigService.getMockConfig(configId);

        if (config == null) {
            log.warn("[Mock服务] Mock配置不存在: configId={}", configId);
            return ResponseDTO.error("CONFIG_NOT_FOUND", "Mock配置不存在");
        }

        log.info("[Mock服务] 查询Mock配置成功: configId={}", configId);
        return ResponseDTO.ok(config);
    }

    /**
     * 查询所有Mock配置
     *
     * @return Mock配置列表
     */
    @Observed(name = "mockConfig.list", contextualName = "list-mock-configs")
    @GetMapping("/list")
    @Operation(summary = "查询所有Mock配置", description = "查询系统中所有的Mock配置")
    public ResponseDTO<List<MockConfigVO>> getAllMockConfigs() {
        log.info("[Mock服务] 查询所有Mock配置");

        List<MockConfigVO> configs = mockConfigService.getAllMockConfigs();

        log.info("[Mock服务] 查询Mock配置成功: count={}", configs.size());
        return ResponseDTO.ok(configs);
    }

    /**
     * 按类型查询Mock配置
     *
     * @param configType 配置类型
     * @return Mock配置列表
     */
    @Observed(name = "mockConfig.listByType", contextualName = "list-mock-configs-by-type")
    @GetMapping("/type/{configType}")
    @Operation(summary = "按类型查询Mock配置", description = "根据配置类型查询Mock配置")
    public ResponseDTO<List<MockConfigVO>> getMockConfigsByType(
            @PathVariable @Parameter(description = "配置类型", required = true) String configType) {
        log.info("[Mock服务] 按类型查询Mock配置: type={}", configType);

        List<MockConfigVO> configs = mockConfigService.getMockConfigsByType(configType);

        log.info("[Mock服务] 查询Mock配置成功: type={}, count={}", configType, configs.size());
        return ResponseDTO.ok(configs);
    }

    /**
     * 生成Mock数据
     *
     * @param configId 配置ID
     * @return Mock数据
     */
    @Observed(name = "mockConfig.generate", contextualName = "generate-mock-data")
    @PostMapping("/generate/{configId}")
    @Operation(summary = "生成Mock数据", description = "根据配置生成Mock测试数据")
    public ResponseDTO<MockDataVO> generateMockData(
            @PathVariable @Parameter(description = "配置ID", required = true) Long configId) {
        log.info("[Mock服务] 生成Mock数据: configId={}", configId);

        MockDataVO data = mockConfigService.generateMockData(configId);

        log.info("[Mock服务] Mock数据生成成功: configId={}, dataCount={}", configId, data.getDataCount());
        return ResponseDTO.ok(data);
    }

    /**
     * 生成Mock数据（按类型）
     *
     * @param configType   配置类型
     * @param mockScenario Mock场景
     * @return Mock数据
     */
    @Observed(name = "mockConfig.generateByType", contextualName = "generate-mock-data-by-type")
    @PostMapping("/generate")
    @Operation(summary = "按类型生成Mock数据", description = "根据配置类型和场景生成Mock测试数据")
    public ResponseDTO<MockDataVO> generateMockDataByType(
            @RequestParam @Parameter(description = "配置类型", required = true) String configType,
            @RequestParam(defaultValue = "NORMAL") @Parameter(description = "Mock场景", required = false) String mockScenario) {
        log.info("[Mock服务] 按类型生成Mock数据: type={}, scenario={}", configType, mockScenario);

        MockDataVO data = mockConfigService.generateMockDataByType(configType, mockScenario);

        log.info("[Mock服务] Mock数据生成成功: type={}, scenario={}, dataCount={}",
                configType, mockScenario, data.getDataCount());
        return ResponseDTO.ok(data);
    }

    /**
     * 启用Mock配置
     *
     * @param configId 配置ID
     * @return 操作结果
     */
    @Observed(name = "mockConfig.enable", contextualName = "enable-mock-config")
    @PostMapping("/{configId}/enable")
    @Operation(summary = "启用Mock配置", description = "启用指定的Mock配置")
    public ResponseDTO<Void> enableMockConfig(
            @PathVariable @Parameter(description = "配置ID", required = true) Long configId) {
        log.info("[Mock服务] 启用Mock配置: configId={}", configId);

        mockConfigService.enableMockConfig(configId);

        log.info("[Mock服务] Mock配置已启用: configId={}", configId);
        return ResponseDTO.ok();
    }

    /**
     * 禁用Mock配置
     *
     * @param configId 配置ID
     * @return 操作结果
     */
    @Observed(name = "mockConfig.disable", contextualName = "disable-mock-config")
    @PostMapping("/{configId}/disable")
    @Operation(summary = "禁用Mock配置", description = "禁用指定的Mock配置")
    public ResponseDTO<Void> disableMockConfig(
            @PathVariable @Parameter(description = "配置ID", required = true) Long configId) {
        log.info("[Mock服务] 禁用Mock配置: configId={}", configId);

        mockConfigService.disableMockConfig(configId);

        log.info("[Mock服务] Mock配置已禁用: configId={}", configId);
        return ResponseDTO.ok();
    }

    /**
     * 检查Mock是否启用
     *
     * @param configType 配置类型
     * @return 是否启用
     */
    @Observed(name = "mockConfig.checkEnabled", contextualName = "check-mock-enabled")
    @GetMapping("/check/{configType}")
    @Operation(summary = "检查Mock是否启用", description = "检查指定类型的Mock是否启用")
    public ResponseDTO<Boolean> isMockEnabled(
            @PathVariable @Parameter(description = "配置类型", required = true) String configType) {
        log.info("[Mock服务] 检查Mock启用状态: type={}", configType);

        boolean enabled = mockConfigService.isMockEnabled(configType);

        log.info("[Mock服务] Mock启用状态: type={}, enabled={}", configType, enabled);
        return ResponseDTO.ok(enabled);
    }
}
