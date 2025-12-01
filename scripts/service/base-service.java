package {{package}}.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.entity.BaseEntity;
import net.lab1024.sa.base.common.response.PageResult;
import net.lab1024.sa.base.common.response.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * {{EntityName}}服务实现类
 *
 * @author {{author}}
 * @date {{date}}
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class {{EntityName}}ServiceImpl implements {{EntityName}}Service {

    @Resource
    private {{EntityName}}Dao {{entityName}}Dao;

    @Resource
    private {{EntityName}}Manager {{entityName}}Manager;

    // ==================== 基础CRUD操作 ====================

    /**
     * 根据ID查询详情
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<{{EntityName}}VO> getById(Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseDTO.userErrorParam("ID不能为空");
            }

            {{EntityName}}Entity entity = {{entityName}}Dao.getById(id);
            if (entity == null) {
                return ResponseDTO.userErrorParam("数据不存在");
            }

            {{EntityName}}VO vo = SmartBeanUtil.copy(entity, {{EntityName}}VO.class);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("查询{}详情失败，ID: {}", this.getEntityName(), id, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 分页查询
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<{{EntityName}}VO>> getPage({{EntityName}}QueryForm queryForm) {
        try {
            Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
            List<{{EntityName}}VO> voList = {{entityName}}Manager.getPage(page, queryForm);
            return ResponseDTO.ok(SmartPageUtil.convert2PageResult(voList));

        } catch (Exception e) {
            log.error("分页查询{}失败", this.getEntityName(), e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 删除
     */
    @Override
    public ResponseDTO<Boolean> delete(Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseDTO.userErrorParam("ID不能为空");
            }

            // 检查是否存在
            {{EntityName}}Entity entity = {{entityName}}Dao.getById(id);
            if (entity == null) {
                return ResponseDTO.userErrorParam("数据不存在");
            }

            // 业务检查
            ResponseDTO<Boolean> checkResult = this.checkBeforeDelete(entity);
            if (!checkResult.getOk()) {
                return checkResult;
            }

            // 执行删除
            int result = {{entityName}}Dao.deleteById(id);
            if (result > 0) {
                log.info("删除{}成功，ID: {}", this.getEntityName(), id);
                return ResponseDTO.ok(true);
            } else {
                return ResponseDTO.error("删除失败");
            }

        } catch (Exception e) {
            log.error("删除{}失败，ID: {}", this.getEntityName(), id, e);
            return ResponseDTO.error("删除失败，请稍后重试");
        }
    }

    /**
     * 批量删除
     */
    @Override
    public ResponseDTO<Boolean> batchDelete(List<Long> idList) {
        try {
            if (idList == null || idList.isEmpty()) {
                return ResponseDTO.userErrorParam("请选择要删除的数据");
            }

            // 检查数量限制
            if (idList.size() > 100) {
                return ResponseDTO.userErrorParam("批量删除数量不能超过100条");
            }

            int successCount = 0;
            for (Long id : idList) {
                ResponseDTO<Boolean> result = this.delete(id);
                if (result.getOk() && result.getData()) {
                    successCount++;
                }
            }

            log.info("批量删除{}完成，成功: {}, 总数: {}", this.getEntityName(), successCount, idList.size());
            return ResponseDTO.ok(successCount == idList.size());

        } catch (Exception e) {
            log.error("批量删除{}失败", this.getEntityName(), e);
            return ResponseDTO.error("批量删除失败，请稍后重试");
        }
    }

    // ==================== 业务扩展方法 ====================

    /**
     * 查询详情（包含关联数据）
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<{{EntityName}}DetailVO> getDetail(Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseDTO.userErrorParam("ID不能为空");
            }

            {{EntityName}}Entity entity = {{entityName}}Dao.getById(id);
            if (entity == null) {
                return ResponseDTO.userErrorParam("数据不存在");
            }

            {{EntityName}}DetailVO detailVO = {{entityName}}Manager.buildDetailVO(entity);
            return ResponseDTO.ok(detailVO);

        } catch (Exception e) {
            log.error("查询{}详情失败，ID: {}", this.getEntityName(), id, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 更新状态
     */
    @Override
    public ResponseDTO<Boolean> updateStatus(Long id, Integer status) {
        try {
            if (id == null || id <= 0) {
                return ResponseDTO.userErrorParam("ID不能为空");
            }

            if (status == null || (status != 0 && status != 1)) {
                return ResponseDTO.userErrorParam("状态值无效");
            }

            {{EntityName}}Entity entity = {{entityName}}Dao.getById(id);
            if (entity == null) {
                return ResponseDTO.userErrorParam("数据不存在");
            }

            // 业务检查
            ResponseDTO<Boolean> checkResult = this.checkBeforeUpdateStatus(entity, status);
            if (!checkResult.getOk()) {
                return checkResult;
            }

            entity.setStatus(status);
            int result = {{entityName}}Dao.updateById(entity);

            if (result > 0) {
                String statusText = status == 1 ? "启用" : "禁用";
                log.info("更新{}状态成功，ID: {}, 状态: {}", this.getEntityName(), id, statusText);
                return ResponseDTO.ok(true);
            } else {
                return ResponseDTO.error("状态更新失败");
            }

        } catch (Exception e) {
            log.error("更新{}状态失败，ID: {}, Status: {}", this.getEntityName(), id, status, e);
            return ResponseDTO.error("状态更新失败，请稍后重试");
        }
    }

    /**
     * 添加
     */
    @Override
    public ResponseDTO<Long> add({{EntityName}}AddForm addForm) {
        try {
            // 参数验证
            ResponseDTO<String> validResult = this.validateAddForm(addForm);
            if (!validResult.getOk()) {
                return ResponseDTO.userErrorParam(validResult.getMsg());
            }

            // 业务检查
            ResponseDTO<Boolean> checkResult = this.checkBeforeAdd(addForm);
            if (!checkResult.getOk()) {
                return ResponseDTO.userErrorParam(checkResult.getMsg());
            }

            {{EntityName}}Entity entity = SmartBeanUtil.copy(addForm, {{EntityName}}Entity.class);

            // 设置默认值
            this.setDefaultValue(entity);

            int result = {{entityName}}Dao.insert(entity);
            if (result > 0) {
                log.info("添加{}成功，ID: {}", this.getEntityName(), entity.getId());
                return ResponseDTO.ok(entity.getId());
            } else {
                return ResponseDTO.error("添加失败");
            }

        } catch (Exception e) {
            log.error("添加{}失败", this.getEntityName(), e);
            return ResponseDTO.error("添加失败，请稍后重试");
        }
    }

    /**
     * 更新
     */
    @Override
    public ResponseDTO<Boolean> update({{EntityName}}UpdateForm updateForm) {
        try {
            if (updateForm.getId() == null || updateForm.getId() <= 0) {
                return ResponseDTO.userErrorParam("ID不能为空");
            }

            {{EntityName}}Entity entity = {{entityName}}Dao.getById(updateForm.getId());
            if (entity == null) {
                return ResponseDTO.userErrorParam("数据不存在");
            }

            // 参数验证
            ResponseDTO<String> validResult = this.validateUpdateForm(updateForm);
            if (!validResult.getOk()) {
                return ResponseDTO.userErrorParam(validResult.getMsg());
            }

            // 业务检查
            ResponseDTO<Boolean> checkResult = this.checkBeforeUpdate(entity, updateForm);
            if (!checkResult.getOk()) {
                return ResponseDTO.userErrorParam(checkResult.getMsg());
            }

            SmartBeanUtil.copy(updateForm, entity);
            int result = {{entityName}}Dao.updateById(entity);

            if (result > 0) {
                log.info("更新{}成功，ID: {}", this.getEntityName(), entity.getId());
                return ResponseDTO.ok(true);
            } else {
                return ResponseDTO.error("更新失败");
            }

        } catch (Exception e) {
            log.error("更新{}失败，ID: {}", this.getEntityName(), updateForm.getId(), e);
            return ResponseDTO.error("更新失败，请稍后重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取实体名称
     */
    private String getEntityName() {
        return "{{EntityName}}";
    }

    /**
     * 设置默认值
     */
    private void setDefaultValue({{EntityName}}Entity entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
    }

    /**
     * 验证添加表单
     */
    private ResponseDTO<String> validateAddForm({{EntityName}}AddForm addForm) {
        if (addForm.getName() == null || addForm.getName().trim().isEmpty()) {
            return ResponseDTO.userErrorParam("名称不能为空");
        }
        if (addForm.getName().length() > 100) {
            return ResponseDTO.userErrorParam("名称长度不能超过100个字符");
        }
        return ResponseDTO.ok();
    }

    /**
     * 验证更新表单
     */
    private ResponseDTO<String> validateUpdateForm({{EntityName}}UpdateForm updateForm) {
        return this.validateAddForm(updateForm);
    }

    /**
     * 删除前业务检查
     */
    private ResponseDTO<Boolean> checkBeforeDelete({{EntityName}}Entity entity) {
        // TODO: 实现具体的业务检查逻辑
        // 例如：检查是否有关联数据、是否处于某种状态等
        return ResponseDTO.ok(true);
    }

    /**
     * 更新状态前业务检查
     */
    private ResponseDTO<Boolean> checkBeforeUpdateStatus({{EntityName}}Entity entity, Integer status) {
        // TODO: 实现具体的业务检查逻辑
        // 例如：检查是否允许修改状态、状态变更的合法性等
        return ResponseDTO.ok(true);
    }

    /**
     * 添加前业务检查
     */
    private ResponseDTO<Boolean> checkBeforeAdd({{EntityName}}AddForm addForm) {
        // TODO: 实现具体的业务检查逻辑
        // 例如：检查名称是否重复、业务规则是否满足等
        return ResponseDTO.ok(true);
    }

    /**
     * 更新前业务检查
     */
    private ResponseDTO<Boolean> checkBeforeUpdate({{EntityName}}Entity entity, {{EntityName}}UpdateForm updateForm) {
        // TODO: 实现具体的业务检查逻辑
        // 例如：检查数据是否被锁定、是否有权限修改等
        return ResponseDTO.ok(true);
    }
}