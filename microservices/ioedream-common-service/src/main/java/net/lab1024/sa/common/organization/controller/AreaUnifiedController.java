package net.lab1024.sa.common.organization.controller;

import lombok.extern.slf4j.Slf4j;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.service.AreaUnifiedService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一区域空间管理接口
 * <p>
 * 为所有业务模块提供统一的区域管理API
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@RestController
@Validated
@RequestMapping("/api/v1/area/unified")
@Slf4j
public class AreaUnifiedController {

    @Resource
    private AreaUnifiedService areaUnifiedService;

    @Observed(name = "areaUnified.getAreaTree", contextualName = "area-unified-get-tree")
    @GetMapping("/tree")
    public ResponseDTO<List<AreaEntity>> getAreaTree() {
        return ResponseDTO.ok(areaUnifiedService.getAreaTree());
    }

    @Observed(name = "areaUnified.getUserAccessibleAreas", contextualName = "area-unified-get-user-accessible")
    @GetMapping("/user/{userId}/accessible")
    public ResponseDTO<List<AreaEntity>> getUserAccessibleAreas(@PathVariable Long userId) {
        return ResponseDTO.ok(areaUnifiedService.getUserAccessibleAreas(userId));
    }

    @Observed(name = "areaUnified.hasAreaAccess", contextualName = "area-unified-has-access")
    @GetMapping("/user/{userId}/area/{areaId}/has-access")
    public ResponseDTO<Boolean> hasAreaAccess(@PathVariable Long userId, @PathVariable Long areaId) {
        return ResponseDTO.ok(areaUnifiedService.hasAreaAccess(userId, areaId));
    }

    @Observed(name = "areaUnified.getAreaByCode", contextualName = "area-unified-get-by-code")
    @GetMapping("/code/{areaCode}")
    public ResponseDTO<AreaEntity> getAreaByCode(@PathVariable String areaCode) {
        return ResponseDTO.ok(areaUnifiedService.getAreaByCode(areaCode));
    }

    @Observed(name = "areaUnified.getAreaPath", contextualName = "area-unified-get-path")
    @GetMapping("/{areaId}/path")
    public ResponseDTO<List<AreaEntity>> getAreaPath(@PathVariable Long areaId) {
        return ResponseDTO.ok(areaUnifiedService.getAreaPath(areaId));
    }

    @Observed(name = "areaUnified.getChildAreas", contextualName = "area-unified-get-children")
    @GetMapping("/{parentAreaId}/children")
    public ResponseDTO<List<AreaEntity>> getChildAreas(@PathVariable Long parentAreaId) {
        return ResponseDTO.ok(areaUnifiedService.getChildAreas(parentAreaId));
    }

    @Observed(name = "areaUnified.getAreaBusinessAttributes", contextualName = "area-unified-get-business-attributes")
    @GetMapping("/{areaId}/business/{businessModule}/attributes")
    public ResponseDTO<Map<String, Object>> getAreaBusinessAttributes(
            @PathVariable Long areaId,
            @PathVariable String businessModule) {
        return ResponseDTO.ok(areaUnifiedService.getAreaBusinessAttributes(areaId, businessModule));
    }

    @Observed(name = "areaUnified.setAreaBusinessAttributes", contextualName = "area-unified-set-business-attributes")
    @PostMapping("/business/attributes")
    public ResponseDTO<Boolean> setAreaBusinessAttributes(@RequestBody Map<String, Object> request) {
        Long areaId = Long.valueOf(request.get("areaId").toString());
        String businessModule = request.get("businessModule").toString();
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) request.get("attributes");
        return ResponseDTO.ok(areaUnifiedService.setAreaBusinessAttributes(areaId, businessModule, attributes));
    }

    @Observed(name = "areaUnified.getAreaDevices", contextualName = "area-unified-get-devices")
    @GetMapping("/{areaId}/devices")
    public ResponseDTO<List<Map<String, Object>>> getAreaDevices(
            @PathVariable Long areaId,
            @RequestParam(required = false) String deviceType) {
        return ResponseDTO.ok(areaUnifiedService.getAreaDevices(areaId, deviceType));
    }

    @Observed(name = "areaUnified.getAreaStatistics", contextualName = "area-unified-get-statistics")
    @GetMapping("/{areaId}/statistics")
    public ResponseDTO<Map<String, Object>> getAreaStatistics(@PathVariable Long areaId) {
        return ResponseDTO.ok(areaUnifiedService.getAreaStatistics(areaId));
    }

    @Observed(name = "areaUnified.getAreasByBusinessType", contextualName = "area-unified-get-by-business-type")
    @GetMapping("/business-type/{businessType}")
    public ResponseDTO<List<AreaEntity>> getAreasByBusinessType(@PathVariable String businessType) {
        return ResponseDTO.ok(areaUnifiedService.getAreasByBusinessType(businessType));
    }

    @Observed(name = "areaUnified.isAreaSupportBusiness", contextualName = "area-unified-is-support-business")
    @GetMapping("/{areaId}/business/{businessModule}/support")
    public ResponseDTO<Boolean> isAreaSupportBusiness(
            @PathVariable Long areaId,
            @PathVariable String businessModule) {
        return ResponseDTO.ok(areaUnifiedService.isAreaSupportBusiness(areaId, businessModule));
    }

    @Observed(name = "areaUnified.getAreaSupportedBusinessModules", contextualName = "area-unified-get-supported-modules")
    @GetMapping("/{areaId}/supported-modules")
    public ResponseDTO<Set<String>> getAreaSupportedBusinessModules(@PathVariable Long areaId) {
        return ResponseDTO.ok(areaUnifiedService.getAreaSupportedBusinessModules(areaId));
    }
}
