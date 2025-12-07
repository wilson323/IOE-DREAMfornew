package net.lab1024.sa.system.employee.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.employee.domain.form.EmployeeAddForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeQueryForm;
import net.lab1024.sa.system.employee.domain.form.EmployeeUpdateForm;
import net.lab1024.sa.system.employee.domain.vo.EmployeeVO;
import net.lab1024.sa.system.employee.service.EmployeeService;

/**
 * 员工管理Controller
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@RestController
@RequestMapping("/api/system/employee")
@Tag(name = "员工管理", description = "员工信息管理相关接口")
@Validated
@RequiredArgsConstructor
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询员工", description = "根据查询条件分页查询员工列表")
    public ResponseDTO<PageResult<EmployeeVO>> queryEmployeePage(@Valid EmployeeQueryForm queryForm) {
        PageResult<EmployeeVO> result = employeeService.queryEmployeePage(queryForm);
        return ResponseDTO.ok(result);
    }

    @GetMapping("/detail/{employeeId}")
    @Operation(summary = "获取员工详情", description = "根据员工ID获取员工详细信息")
    public ResponseDTO<EmployeeVO> getEmployeeDetail(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId) {
        EmployeeVO employeeVO = employeeService.getEmployeeDetail(employeeId);
        if (employeeVO == null) {
            return ResponseDTO.userErrorParam("员工不存在");
        }
        return ResponseDTO.ok(employeeVO);
    }

    @PostMapping("/add")
    @Operation(summary = "新增员工", description = "新增员工信息")
    public ResponseDTO<String> addEmployee(@RequestBody @Valid EmployeeAddForm addForm) {
        boolean result = employeeService.addEmployee(addForm);
        return result ? ResponseDTO.<String>okMsg("新增成功")
                : ResponseDTO.<String>error("新增失败");
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工", description = "更新员工信息")
    public ResponseDTO<String> updateEmployee(@RequestBody @Valid EmployeeUpdateForm updateForm) {
        boolean result = employeeService.updateEmployee(updateForm);
        return result ? ResponseDTO.<String>okMsg("更新成功")
                : ResponseDTO.<String>error("更新失败");
    }

    @PostMapping("/delete/{employeeId}")
    @Operation(summary = "删除员工", description = "根据员工ID删除员工信息")
    public ResponseDTO<String> deleteEmployee(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId) {
        boolean result = employeeService.deleteEmployee(employeeId);
        return result ? ResponseDTO.<String>okMsg("删除成功")
                : ResponseDTO.<String>error("删除失败");
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除员工", description = "根据员工ID列表批量删除员工信息")
    public ResponseDTO<String> batchDeleteEmployees(@RequestBody List<Long> employeeIds) {
        boolean result = employeeService.batchDeleteEmployees(employeeIds);
        return result ? ResponseDTO.<String>okMsg("批量删除成功")
                : ResponseDTO.<String>error("批量删除失败");
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门获取员工", description = "根据部门ID获取该部门下的员工列表")
    public ResponseDTO<List<EmployeeVO>> getEmployeesByDepartmentId(
            @Parameter(description = "部门ID", required = true) @PathVariable @NotNull Long departmentId) {
        List<EmployeeVO> employees = employeeService.getEmployeesByDepartmentId(departmentId);
        return ResponseDTO.ok(employees);
    }

    @PostMapping("/status/{employeeId}/{status}")
    @Operation(summary = "更新员工状态", description = "更新员工状态（启用/禁用）")
    public ResponseDTO<String> updateEmployeeStatus(
            @Parameter(description = "员工ID", required = true) @PathVariable @NotNull Long employeeId,
            @Parameter(description = "状态：0-禁用 1-启用", required = true) @PathVariable @NotNull Integer status) {
        boolean result = employeeService.updateEmployeeStatus(employeeId, status);
        return result ? ResponseDTO.<String>okMsg("状态更新成功")
                : ResponseDTO.<String>error("状态更新失败");
    }
}
