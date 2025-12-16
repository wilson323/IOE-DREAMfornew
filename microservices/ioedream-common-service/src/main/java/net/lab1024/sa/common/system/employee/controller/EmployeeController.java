package net.lab1024.sa.common.system.employee.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeAddDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeQueryDTO;
import net.lab1024.sa.common.system.employee.domain.dto.EmployeeUpdateDTO;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;
import net.lab1024.sa.common.system.employee.service.EmployeeService;

/**
 * 员工管理Controller
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/system/employee")
@Tag(name = "员工管理", description = "员工信息管理相关接口")
@Validated
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping("/page")
    @Observed(name = "employee.queryEmployeePage", contextualName = "employee-query-page")
    @Operation(summary = "分页查询员工", description = "根据查询条件分页查询员工列表")
    public ResponseDTO<PageResult<EmployeeVO>> queryEmployeePage(@Valid EmployeeQueryDTO queryDTO) {
        PageResult<EmployeeVO> result = employeeService.queryEmployeePage(queryDTO);
        return ResponseDTO.ok(result);
    }

    @GetMapping("/{employeeId}")
    @Observed(name = "employee.getEmployeeDetail", contextualName = "employee-get-detail")
    @Operation(summary = "获取员工详情", description = "根据员工ID获取员工详细信息")
    public ResponseDTO<EmployeeVO> getEmployeeDetail(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId) {
        EmployeeVO employeeVO = employeeService.getEmployeeDetail(employeeId);
        if (employeeVO == null) {
            return ResponseDTO.userErrorParam("员工不存在");
        }
        return ResponseDTO.ok(employeeVO);
    }

    @PostMapping
    @Observed(name = "employee.addEmployee", contextualName = "employee-add")
    @Operation(summary = "新增员工", description = "新增员工信息")
    public ResponseDTO<String> addEmployee(@RequestBody @Valid EmployeeAddDTO addDTO) {
        boolean result = employeeService.addEmployee(addDTO);
        return result ? ResponseDTO.okMsg("新增成功") : ResponseDTO.error("新增失败");
    }

    @PutMapping
    @Observed(name = "employee.updateEmployee", contextualName = "employee-update")
    @Operation(summary = "更新员工", description = "更新员工信息")
    public ResponseDTO<String> updateEmployee(@RequestBody @Valid EmployeeUpdateDTO updateDTO) {
        boolean result = employeeService.updateEmployee(updateDTO);
        return result ? ResponseDTO.okMsg("更新成功") : ResponseDTO.error("更新失败");
    }

    @DeleteMapping("/{employeeId}")
    @Observed(name = "employee.deleteEmployee", contextualName = "employee-delete")
    @Operation(summary = "删除员工", description = "根据员工ID删除员工信息")
    public ResponseDTO<String> deleteEmployee(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId) {
        boolean result = employeeService.deleteEmployee(employeeId);
        return result ? ResponseDTO.okMsg("删除成功") : ResponseDTO.error("删除失败");
    }

    @DeleteMapping("/batch")
    @Observed(name = "employee.batchDeleteEmployees", contextualName = "employee-batch-delete")
    @Operation(summary = "批量删除员工", description = "根据员工ID列表批量删除员工信息")
    public ResponseDTO<String> batchDeleteEmployees(@RequestBody List<Long> employeeIds) {
        boolean result = employeeService.batchDeleteEmployees(employeeIds);
        return result ? ResponseDTO.okMsg("批量删除成功") : ResponseDTO.error("批量删除失败");
    }

    @GetMapping("/department/{departmentId}")
    @Observed(name = "employee.getEmployeesByDepartmentId", contextualName = "employee-get-by-department")
    @Operation(summary = "根据部门获取员工", description = "根据部门ID获取该部门下的员工列表")
    public ResponseDTO<List<EmployeeVO>> getEmployeesByDepartmentId(
            @Parameter(description = "部门ID", required = true) @PathVariable @NotNull Long departmentId) {
        List<EmployeeVO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        return ResponseDTO.ok(employees);
    }

    @PutMapping("/{employeeId}/status/{status}")
    @Observed(name = "employee.updateEmployeeStatus", contextualName = "employee-update-status")
    @Operation(summary = "更新员工状态", description = "更新员工状态（启用/禁用）")
    public ResponseDTO<String> updateEmployeeStatus(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId,
            @Parameter(description = "状态：0-禁用 1-启用", required = true) @PathVariable @NotNull Integer status) {
        boolean result = employeeService.updateEmployeeStatus(employeeId, status);
        return result ? ResponseDTO.okMsg("状态更新成功") : ResponseDTO.error("状态更新失败");
    }
}

