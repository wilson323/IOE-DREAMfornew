package net.lab1024.sa.common.organization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.auth.dao.UserDao;
import net.lab1024.sa.common.organization.entity.UserEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * <p>
 * 提供用户管理相关的内部API，供其他服务调用
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
@Slf4j
public class UserController {

    @Resource
    private UserDao userDao;

    /**
     * 更新用户锁定状态
     * <p>
     * 内部API，供UserLockService调用
     * </p>
     *
     * @param params 更新参数
     * @return 操作结果
     */
    @Operation(summary = "更新用户锁定状态", description = "更新数据库中的用户锁定状态")
    @PutMapping("/update-lock-status")
    public ResponseDTO<Void> updateLockStatus(@RequestBody Map<String, Object> params) {
        log.info("[用户管理] 更新用户锁定状态: params={}", params);

        try {
            String username = (String) params.get("username");
            Boolean locked = (Boolean) params.get("locked");
            String lockExpireTimeStr = (String) params.get("lockExpireTime");

            // 参数验证
            if (username == null || username.isEmpty()) {
                log.warn("[用户管理] 用户名不能为空");
                return ResponseDTO.error("PARAM_ERROR", "用户名不能为空");
            }

            if (locked == null) {
                log.warn("[用户管理] 锁定状态不能为空");
                return ResponseDTO.error("PARAM_ERROR", "锁定状态不能为空");
            }

            // 解析过期时间
            LocalDateTime lockExpireTime = null;
            if (lockExpireTimeStr != null && !lockExpireTimeStr.isEmpty()) {
                try {
                    lockExpireTime = LocalDateTime.parse(lockExpireTimeStr,
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    log.error("[用户管理] 过期时间解析失败: lockExpireTimeStr={}", lockExpireTimeStr, e);
                    return ResponseDTO.error("PARAM_ERROR", "过期时间格式错误");
                }
            }

            // 查询用户
            LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserEntity::getUsername, username);
            UserEntity user = userDao.selectOne(queryWrapper);

            if (user == null) {
                log.warn("[用户管理] 用户不存在: username={}", username);
                return ResponseDTO.error("USER_NOT_FOUND", "用户不存在");
            }

            // 更新锁定状态
            user.setAccountLocked(locked);
            user.setLockExpireTime(lockExpireTime);

            if (locked) {
                user.setLockReason("登录失败次数过多" +
                    (lockExpireTime != null ? "，锁定至" + lockExpireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""));
            } else {
                user.setLockReason(null);
            }

            int rows = userDao.updateById(user);

            if (rows > 0) {
                log.info("[用户管理] 用户锁定状态更新成功: username={}, locked={}", username, locked);
                return ResponseDTO.ok();
            } else {
                log.error("[用户管理] 用户锁定状态更新失败: username={}", username);
                return ResponseDTO.error("UPDATE_FAILED", "更新失败");
            }

        } catch (Exception e) {
            log.error("[用户管理] 更新锁定状态异常: params={}, error={}", params, e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
}
