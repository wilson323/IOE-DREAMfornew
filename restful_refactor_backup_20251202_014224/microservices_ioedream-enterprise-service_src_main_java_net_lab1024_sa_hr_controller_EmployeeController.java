package net.lab1024.sa.hr.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.hr.domain.dto.EmployeeAddDTO;
import net.lab1024.sa.hr.domain.dto.EmployeeUpdateDTO;
import net.lab1024.sa.hr.domain.dto.EmployeeVO;
import net.lab1024.sa.hr.domain.entity.EmployeeEntity;
import net.lab1024.sa.hr.domain.form.EmployeeQueryForm;
import net.lab1024.sa.hr.domain.request.PhotoQualityCheckRequest;
import net.lab1024.sa.hr.domain.response.PhotoQualityCheckResponse;
import net.lab1024.sa.hr.service.EmployeePhotoQualityService;
import net.lab1024.sa.hr.service.EmployeeService;

/**
 * 员工管理控制器
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@SaCheckLogin
@SaCheckPermission("hr:employee")
@RestController
@RequestMapping("/hr/employee")
@Tag(name = "员工管理", description = "员工信息的增删改查操作")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private EmployeePhotoQualityService employeePhotoQualityService;

    @Operation(summary = "分页查询员工列表")
    @PostMapping("/queryPage")
    public ResponseDTO<PageResult<EmployeeVO>> queryPage(@RequestBody @Valid EmployeeQueryForm queryForm) {
        try {
            log.info("分页查询员工列表，查询条件：{}", queryForm);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<EmployeeEntity> page = employeeService
                    .queryPage(queryForm);

            // 将 Page 转换为 PageResult
            PageResult<EmployeeEntity> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords());

            // 转换为VO
            PageResult<EmployeeVO> voPageResult = SmartPageUtil.convert2PageResult(pageResult, EmployeeVO.class);

            return ResponseDTO.ok(voPageResult);

        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            return ResponseDTO.userErrorParam("查询员工列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询员工详情")
    @GetMapping("/get/{employeeId}")
    public ResponseDTO<EmployeeVO> getById(@Parameter(description = "员工ID") @PathVariable Long employeeId) {
        try {
            log.info("查询员工详情，ID：{}", employeeId);

            EmployeeEntity employee = employeeService.getById(employeeId);
            if (employee == null) {
                return ResponseDTO.userErrorParam("员工不存在");
            }

            EmployeeVO employeeVO = new EmployeeVO();
            SmartBeanUtil.copyProperties(employee, employeeVO);

            return ResponseDTO.ok(employeeVO);

        } catch (Exception e) {
            log.error("查询员工详情失败，ID：{}", employeeId, e);
            return ResponseDTO.userErrorParam("查询员工详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "新增员工")
    @PostMapping("/add")
    public ResponseDTO<Long> add(@RequestBody @Valid EmployeeAddDTO addDTO) {
        try {
            log.info("新增员工，信息：{}", addDTO);

            // 参数验证
            if (!employeeService.validateEmployeeNo(addDTO.getEmployeeCode())) {
                return ResponseDTO.userErrorParam("员工编号已存在");
            }

            Long employeeId = employeeService.add(addDTO);
            log.info("新增员工成功，ID：{}", employeeId);

            return ResponseDTO.ok(employeeId);

        } catch (Exception e) {
            log.error("新增员工失败", e);
            return ResponseDTO.userErrorParam("新增员工失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新员工信息")
    @PostMapping("/update")
    public ResponseDTO<String> update(@RequestBody @Valid EmployeeUpdateDTO updateDTO) {
        try {
            log.info("更新员工信息，ID：{}，信息：{}", updateDTO.getEmployeeId(), updateDTO);

            EmployeeEntity existEmployee = employeeService.getById(updateDTO.getEmployeeId());
            if (existEmployee == null) {
                return ResponseDTO.userErrorParam("员工不存在");
            }

            employeeService.update(updateDTO);
            log.info("更新员工信息成功，ID：{}", updateDTO.getEmployeeId());

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新员工信息失败，ID：{}", updateDTO.getEmployeeId(), e);
            return ResponseDTO.userErrorParam("更新员工信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除员工")
    @PostMapping("/batchDelete")
    public ResponseDTO<String> batchDelete(@RequestBody List<Long> employeeIds) {
        try {
            log.info("批量删除员工，IDs：{}", employeeIds);

            if (employeeIds.isEmpty()) {
                return ResponseDTO.userErrorParam("请选择要删除的员工");
            }

            int count = employeeService.batchDelete(employeeIds);
            log.info("批量删除员工成功，删除数量：{}", count);

            return ResponseDTO.ok("成功删除 " + count + " 个员工");

        } catch (Exception e) {
            log.error("批量删除员工失败，IDs：{}", employeeIds, e);
            return ResponseDTO.userErrorParam("删除员工失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据员工编号查询员工")
    @GetMapping("/getByNo/{employeeNo}")
    public ResponseDTO<EmployeeVO> getByEmployeeNo(@Parameter(description = "员工编号") @PathVariable String employeeNo) {
        try {
            log.info("根据员工编号查询员工，编号：{}", employeeNo);

            EmployeeEntity employee = employeeService.getByEmployeeNo(employeeNo);
            if (employee == null) {
                return ResponseDTO.userErrorParam("员工不存在");
            }

            EmployeeVO employeeVO = new EmployeeVO();
            SmartBeanUtil.copyProperties(employee, employeeVO);

            return ResponseDTO.ok(employeeVO);

        } catch (Exception e) {
            log.error("根据员工编号查询员工失败，编号：{}", employeeNo, e);
            return ResponseDTO.userErrorParam("查询员工失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取我的员工信息")
    @GetMapping("/getMyInfo")
    public ResponseDTO<EmployeeVO> getMyInfo() {
        try {
            log.info("获取当前用户的员工信息");

            Long userId = SmartRequestUtil.getRequestUserId();
            EmployeeEntity employee = employeeService.getByUserId(userId);

            if (employee == null) {
                return ResponseDTO.userErrorParam("未找到员工信息");
            }

            EmployeeVO employeeVO = new EmployeeVO();
            SmartBeanUtil.copyProperties(employee, employeeVO);

            return ResponseDTO.ok(employeeVO);

        } catch (Exception e) {
            log.error("获取员工信息失败", e);
            return ResponseDTO.userErrorParam("获取员工信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "员工离职")
    @PostMapping("/resign/{employeeId}")
    public ResponseDTO<String> resign(@Parameter(description = "员工ID") @PathVariable Long employeeId) {
        try {
            log.info("员工离职，ID：{}", employeeId);

            EmployeeEntity employee = employeeService.getById(employeeId);
            if (employee == null) {
                return ResponseDTO.userErrorParam("员工不存在");
            }

            employeeService.resign(employeeId);
            log.info("员工离职处理成功，ID：{}", employeeId);

            return ResponseDTO.ok("员工离职处理成功");

        } catch (Exception e) {
            log.error("员工离职处理失败，ID：{}", employeeId, e);
            return ResponseDTO.userErrorParam("员工离职处理失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据部门ID查询员工列表")
    @GetMapping("/getByDepartment/{departmentId}")
    public ResponseDTO<List<EmployeeVO>> getByDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("根据部门查询员工，部门ID：{}", departmentId);

            List<EmployeeEntity> employees = employeeService.getByDepartment(departmentId);
            List<EmployeeVO> employeeVOs = SmartBeanUtil.copyList(employees, EmployeeVO.class);

            return ResponseDTO.ok(employeeVOs);

        } catch (Exception e) {
            log.error("根据部门查询员工失败，部门ID：{}", departmentId, e);
            return ResponseDTO.userErrorParam("查询员工失败：" + e.getMessage());
        }
    }

    @Operation(summary = "员工导入")
    @PostMapping("/import")
    public ResponseDTO<String> importEmployees(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            log.info("开始导入员工数据，文件：{}", file.getOriginalFilename());

            String result = employeeService.importEmployees(file);
            log.info("员工数据导入完成：{}", result);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("导入员工数据失败", e);
            return ResponseDTO.userErrorParam("导入员工数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "员工数据导出")
    @PostMapping("/export")
    public ResponseDTO<String> export(@RequestBody @Valid EmployeeQueryForm queryForm) {
        try {
            log.info("开始导出员工数据");

            String filePath = employeeService.exportEmployees(queryForm);
            log.info("员工数据导出完成，文件：{}", filePath);

            return ResponseDTO.ok(filePath);

        } catch (Exception e) {
            log.error("导出员工数据失败", e);
            return ResponseDTO.userErrorParam("导出员工数据失败：" + e.getMessage());
        }
    }

    @Operation(summary = "员工照片质量检查")
    @PostMapping("/photo-quality-check")
    public ResponseDTO<PhotoQualityCheckResponse> checkPhotoQuality(
            @RequestBody @Valid PhotoQualityCheckRequest request) {
        try {
            log.info("检查员工照片质量，用途：{}", request.getUsageType());

            PhotoQualityCheckResponse response = employeePhotoQualityService.checkPhotoQuality(request);
            log.info("照片质量检查完成，结果：{}，得分：{}", response.isPass() ? "通过" : "不通过", response.getScore());

            return ResponseDTO.ok(response);

        } catch (Exception e) {
            log.error("照片质量检查失败", e);
            return ResponseDTO.userErrorParam("照片质量检查失败：" + e.getMessage());
        }
    }
}
