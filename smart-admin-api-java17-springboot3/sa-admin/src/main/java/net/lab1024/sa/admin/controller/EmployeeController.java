package net.lab1024.sa.admin.controller;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity;
import net.lab1024.sa.admin.module.hr.service.EmployeeService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.base.module.support.rbac.RequireResource;

/**
 * 员工管理 Controller
 *
 * 说明：示例移除模拟数据，接入真实 DB（MyBatis-Plus）
 */
@RestController
@RequestMapping("/api/employee")
@Tag(name = "员工管理", description = "员工信息管理相关接口")
@Validated
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/list")
    @Operation(summary = "获取员工列表", description = "分页获取员工列表")
    @RequireResource(code = "hr:employee:list", scope = "DEPT")
    @SaCheckPermission("hr:employee:list")
    public ResponseDTO<PageResult<EmployeeEntity>> getEmployeeList(@Valid PageParam pageParam,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long departmentId) {
        return ResponseDTO
                .ok(employeeService.pageEmployees(pageParam, employeeName, status, departmentId));
    }

    @GetMapping("/detail/{employeeId}")
    @Operation(summary = "获取员工详情", description = "根据员工ID获取详细信息")
    @RequireResource(code = "hr:employee:detail", scope = "DEPT")
    @SaCheckPermission("hr:employee:detail")
    public ResponseDTO<EmployeeEntity> getEmployeeDetail(@PathVariable @NotNull Long employeeId) {
        EmployeeEntity entity = employeeService.getById(employeeId);
        return entity == null ? ResponseDTO.userErrorParam("员工不存在") : ResponseDTO.ok(entity);
    }

    @PostMapping("/add")
    @Operation(summary = "新增员工", description = "新增员工信息")
    @RequireResource(code = "hr:employee:add", scope = "DEPT")
    @SaCheckPermission("hr:employee:add")
    public ResponseDTO<String> addEmployee(@RequestBody @Valid EmployeeEntity employee) {
        return employeeService.addEmployee(employee) ? SmartResponseUtil.success("新增成功")
                : SmartResponseUtil.error("新增失败");
    }

    @PostMapping("/update")
    @Operation(summary = "更新员工", description = "更新员工信息")
    @RequireResource(code = "hr:employee:update", scope = "DEPT")
    @SaCheckPermission("hr:employee:update")
    public ResponseDTO<String> updateEmployee(@RequestBody @Valid EmployeeEntity employee) {
        return employeeService.updateById(employee) ? SmartResponseUtil.success("更新成功")
                : SmartResponseUtil.error("更新失败");
    }

    @PostMapping("/delete/{employeeId}")
    @Operation(summary = "删除员工", description = "根据员工ID删除员工")
    @RequireResource(code = "hr:employee:delete", scope = "DEPT")
    @SaCheckPermission("hr:employee:delete")
    public ResponseDTO<String> deleteEmployee(@PathVariable @NotNull Long employeeId) {
        return employeeService.removeById(employeeId) ? SmartResponseUtil.success("删除成功")
                : SmartResponseUtil.error("删除失败");
    }

    @GetMapping("/departments")
    @Operation(summary = "部门下拉", description = "返回部门树列表（占位，后续接系统部门模块）")
    public ResponseDTO<List<?>> getDepartments() {
        return ResponseDTO.ok(List.of());
    }
}

