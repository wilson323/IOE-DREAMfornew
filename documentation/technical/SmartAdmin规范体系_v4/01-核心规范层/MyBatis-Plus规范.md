# MyBatis-Plus使用规范（权威文档）

> **📋 文档版本**: v4.0.0 (整合版)
> **📋 文档职责**: SmartAdmin项目的唯一MyBatis-Plus使用规范权威来源，基于MyBatis-Plus 3.5.12，整合代码生成和使用最佳实践。

## ⚠️ MyBatis-Plus使用铁律（不可违反）

### 🚫 绝对禁止
```markdown
❌ 禁止在Service层直接使用BaseMapper的方法
❌ 禁止使用XML配置文件（全部使用注解）
❌ 禁止缺少实体类字段映射注解
❌ 禁止不使用@TableLogic进行软删除
❌ 禁止不使用@Version进行乐观锁控制
❌ 禁止在循环中进行数据库操作
❌ 禁止使用N+1查询问题
❌ 禁止缺少事务管理注解
```

### ✅ 必须执行
```markdown
✅ 必须使用代码生成器生成基础代码
✅ 必须使用@TableField注解进行字段映射
✅ 必须使用@TableName注解指定表名
✅ 必须使用@TableLogic实现软删除
✅ 必须使用@Version实现乐观锁
✅ 必须使用分页插件进行分页查询
✅ 必须使用条件构造器进行复杂查询
✅ 必须使用自动填充处理审计字段
```

## 🛠️ MyBatis-Plus配置规范

### 核心配置类
```java
@Configuration
@EnableTransactionManagement
@Slf4j
public class MybatisPlusConfig {

    /**
     * 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(1000L); // 单页分页条数限制
        paginationInnerInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        paginationInnerInterceptor.setOptimizeJoin(true); // 优化COUNT查询
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 数据权限插件（可选）
        // interceptor.addInnerInterceptor(new DataPermissionInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                log.debug("开始插入填充...");

                // 创建时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

                // 创建人ID
                Long currentUserId = getCurrentUserId();
                if (currentUserId != null) {
                    this.strictInsertFill(metaObject, "createUserId", Long.class, currentUserId);
                    this.strictInsertFill(metaObject, "updateUserId", Long.class, currentUserId);
                }

                // 软删除标记
                this.strictInsertFill(metaObject, "deletedFlag", Integer.class, 0);

                // 版本号
                this.strictInsertFill(metaObject, "version", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                log.debug("开始更新填充...");

                // 更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

                // 更新人ID
                Long currentUserId = getCurrentUserId();
                if (currentUserId != null) {
                    this.strictUpdateFill(metaObject, "updateUserId", Long.class, currentUserId);
                }
            }

            /**
             * 获取当前用户ID
             */
            private Long getCurrentUserId() {
                try {
                    return StpUtil.getLoginIdAsLong();
                } catch (Exception e) {
                    log.debug("获取用户ID失败，可能为系统操作", e);
                    return null;
                }
            }
        };
    }

    /**
     * 数据库字段类型处理器
     */
    @Bean
    public CustomizedDbTypeHandler customizedDbTypeHandler() {
        return new CustomizedDbTypeHandler();
    }
}
```

### 数据源配置
```java
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
@Data
public class DataSourceConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private HikariConfig hikari;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();

        // 基础配置
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // Hikari连接池配置
        if (hikari != null) {
            dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
            dataSource.setMinimumIdle(hikari.getMinimumIdle());
            dataSource.setIdleTimeout(hikari.getIdleTimeout());
            dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
            dataSource.setMaxLifetime(hikari.getMaxLifetime());
            dataSource.setLeakDetectionThreshold(hikari.getLeakDetectionThreshold());
        }

        // 连接测试查询
        dataSource.setConnectionTestQuery("SELECT 1");

        // 连接池名称
        dataSource.setPoolName("SmartAdminHikariPool");

        return dataSource;
    }
}
```

## 🏗️ 实体类设计规范

### 标准实体类模板
```java
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_info")
@Accessors(chain = true)
public class UserEntity extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    @TableField("user_name")
    @TableField(condition = SqlCondition.LIKE)
    private String userName;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    @TableField(select = false) // 查询时不返回密码字段
    private String password;

    /**
     * 邮箱
     */
    @TableField("email")
    @TableField(condition = SqlCondition.EQUAL)
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    @TableField(condition = SqlCondition.EQUAL)
    private String phone;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 性别：1-男 2-女 0-未知
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 状态：1-正常 0-禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序值
     */
    @TableField("sort_value")
    private Integer sortValue;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展信息（JSON格式）
     */
    @TableField(value = "extend_info", typeHandler = JacksonTypeHandler.class)
    private UserExtendInfo extendInfo;

    /**
     * 用户标签（JSON数组）
     */
    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    // ========== 审计字段（继承自BaseEntity） ==========
    // @TableField(value = "create_time", fill = FieldFill.INSERT)
    // private LocalDateTime createTime;
    //
    // @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    // private LocalDateTime updateTime;
    //
    // @TableField("create_user_id")
    // private Long createUserId;
    //
    // @TableField("update_user_id")
    // private Long updateUserId;
    //
    // @TableField("deleted_flag")
    // @TableLogic
    // private Integer deletedFlag;
    //
    // @Version
    // @TableField("version")
    // private Integer version;
}
```

### 基础实体类
```java
@Data
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记：0-正常 1-删除
     */
    @TableField("deleted_flag")
    @TableLogic
    private Integer deletedFlag;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;
}
```

### JSON字段类型定义
```java
@Data
public class UserExtendInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 身份证号（加密存储）
     */
    private String idCard;

    /**
     * 学历
     */
    private String education;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 技能标签
     */
    private List<String> skills;

    /**
     * 社交信息
     */
    private Map<String, String> socialInfo;
}
```

## 🔄 Mapper层规范

### 标准Mapper接口
```java
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

    /**
     * 分页查询用户列表
     */
    @Select("<script>" +
            "SELECT * FROM t_user_info " +
            "WHERE deleted_flag = 0 " +
            "<if test='queryForm.userName != null and queryForm.userName != \"\"'>" +
            "AND user_name LIKE CONCAT('%', #{queryForm.userName}, '%') " +
            "</if>" +
            "<if test='queryForm.email != null and queryForm.email != \"\"'>" +
            "AND email = #{queryForm.email} " +
            "</if>" +
            "<if test='queryForm.phone != null and queryForm.phone != \"\"'>" +
            "AND phone = #{queryForm.phone} " +
            "</if>" +
            "<if test='queryForm.status != null'>" +
            "AND status = #{queryForm.status} " +
            "</if>" +
            "<if test='queryForm.deptId != null'>" +
            "AND dept_id = #{queryForm.deptId} " +
            "</if>" +
            "<if test='queryForm.createTimeStart != null and queryForm.createTimeStart != \"\"'>" +
            "AND create_time >= #{queryForm.createTimeStart} " +
            "</if>" +
            "<if test='queryForm.createTimeEnd != null and queryForm.createTimeEnd != \"\"'>" +
            "AND create_time <= #{queryForm.createTimeEnd} " +
            "</if>" +
            "ORDER BY sort_value ASC, create_time DESC " +
            "</script>")
    IPage<UserEntity> selectPage(Page<UserEntity> page, @Param("queryForm") UserQueryForm queryForm);

    /**
     * 根据部门ID查询用户列表
     */
    @Select("SELECT * FROM t_user_info WHERE deleted_flag = 0 AND dept_id = #{deptId} ORDER BY sort_value ASC")
    List<UserEntity> selectByDeptId(Long deptId);

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM t_user_info WHERE deleted_flag = 0 AND user_name = #{userName} LIMIT 1")
    UserEntity selectByUserName(String userName);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM t_user_info WHERE deleted_flag = 0 AND email = #{email} LIMIT 1")
    UserEntity selectByEmail(String email);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM t_user_info WHERE deleted_flag = 0 AND phone = #{phone} LIMIT 1")
    UserEntity selectByPhone(String phone);

    /**
     * 批量查询用户信息
     */
    @Select("<script>" +
            "SELECT * FROM t_user_info " +
            "WHERE deleted_flag = 0 " +
            "AND user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<UserEntity> selectByUserIds(@Param("userIds") Collection<Long> userIds);

    /**
     * 统计部门用户数量
     */
    @Select("SELECT COUNT(*) FROM t_user_info WHERE deleted_flag = 0 AND dept_id = #{deptId}")
    Long countByDeptId(Long deptId);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user_info WHERE deleted_flag = 0 AND user_name = #{userName} AND user_id != #{excludeUserId}")
    Long countByUserNameExcludeId(@Param("userName") String userName, @Param("excludeUserId") Long excludeUserId);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM t_user_info WHERE deleted_flag = 0 AND email = #{email} AND user_id != #{excludeUserId}")
    Long countByEmailExcludeId(@Param("email") String email, @Param("excludeUserId") Long excludeUserId);

    /**
     * 更新用户状态
     */
    @Update("UPDATE t_user_info SET " +
            "status = #{status}, " +
            "update_time = NOW(6), " +
            "update_user_id = #{updateUserId} " +
            "WHERE user_id = #{userId} AND deleted_flag = 0")
    int updateStatus(@Param("userId") Long userId,
                    @Param("status") Integer status,
                    @Param("updateUserId") Long updateUserId);

    /**
     * 软删除用户
     */
    @Update("UPDATE t_user_info SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(6), " +
            "update_user_id = #{updateUserId} " +
            "WHERE user_id = #{userId}")
    int deleteSoft(@Param("userId") Long userId, @Param("updateUserId") Long updateUserId);

    /**
     * 批量软删除用户
     */
    @Update("<script>" +
            "UPDATE t_user_info SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(6), " +
            "update_user_id = #{updateUserId} " +
            "WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    int deleteSoftBatch(@Param("userIds") Collection<Long> userIds, @Param("updateUserId") Long updateUserId);

    /**
     * 更新用户密码
     */
    @Update("UPDATE t_user_info SET " +
            "password = #{password}, " +
            "update_time = NOW(6), " +
            "update_user_id = #{updateUserId} " +
            "WHERE user_id = #{userId} AND deleted_flag = 0")
    int updatePassword(@Param("userId") Long userId,
                      @Param("password") String password,
                      @Param("updateUserId") Long updateUserId);

    /**
     * 查询最近登录的用户
     */
    @Select("SELECT * FROM t_user_info " +
            "WHERE deleted_flag = 0 AND status = 1 " +
            "ORDER BY update_time DESC " +
            "LIMIT #{limit}")
    List<UserEntity> selectRecentUsers(@Param("limit") Integer limit);
}
```

## 🎯 Service层规范

### 标准Service接口
```java
public interface UserService extends IService<UserEntity> {

    /**
     * 分页查询用户
     */
    ResponseDTO<PageResult<UserVO>> pageUsers(UserQueryForm queryForm);

    /**
     * 获取用户详情
     */
    ResponseDTO<UserVO> getUserDetail(Long userId);

    /**
     * 新增用户
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> addUser(UserAddForm addForm);

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> updateUser(UserUpdateForm updateForm);

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> deleteUser(Long userId);

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> batchDeleteUsers(List<Long> userIds);

    /**
     * 更新用户状态
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> updateUserStatus(UserStatusForm statusForm);

    /**
     * 重置用户密码
     */
    @Transactional(rollbackFor = Exception.class)
    ResponseDTO<String> resetPassword(ResetPasswordForm form);

    /**
     * 根据用户名获取用户
     */
    UserEntity getByUserName(String userName);

    /**
     * 检查用户名是否存在
     */
    boolean checkUserNameExists(String userName, Long excludeUserId);

    /**
     * 获取部门用户列表
     */
    List<UserVO> getDeptUsers(Long deptId);

    /**
     * 导出用户数据
     */
    void exportUsers(UserQueryForm queryForm, HttpServletResponse response);
}
```

### 标准Service实现
```java
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private UserCacheManager userCacheManager;

    @Resource
    private DeptService deptService;

    @Resource
    private UserRoleService userRoleService;

    @Override
    public ResponseDTO<PageResult<UserVO>> pageUsers(UserQueryForm queryForm) {
        try {
            // 1. 构建分页对象
            Page<UserEntity> page = new Page<>(queryForm.getCurrent(), queryForm.getSize());

            // 2. 执行分页查询
            IPage<UserEntity> userPage = userDao.selectPage(page, queryForm);

            // 3. 转换为VO
            List<UserVO> userVOList = SmartBeanUtil.copyList(userPage.getRecords(), UserVO.class);

            // 4. 填充部门信息
            this.fillDeptInfo(userVOList);

            // 5. 构建分页结果
            PageResult<UserVO> pageResult = new PageResult<>();
            pageResult.setRecords(userVOList);
            pageResult.setTotal(userPage.getTotal());
            pageResult.setCurrent(userPage.getCurrent());
            pageResult.setSize(userPage.getSize());
            pageResult.setPages(userPage.getPages());

            return ResponseDTO.ok(pageResult);

        } catch (Exception e) {
            log.error("分页查询用户失败", e);
            throw new BusinessException("查询用户失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<UserVO> getUserDetail(Long userId) {
        try {
            // 1. 从缓存获取用户信息
            UserEntity user = userCacheManager.queryUserInfo(userId);
            if (user == null || user.getDeletedFlag() == 1) {
                throw new BusinessException("用户不存在");
            }

            // 2. 转换为VO
            UserVO userVO = SmartBeanUtil.copy(user, UserVO.class);

            // 3. 填充部门信息
            this.fillDeptInfo(Collections.singletonList(userVO));

            // 4. 填充角色信息
            List<String> roleNames = userRoleService.getUserRoleNames(userId);
            userVO.setRoleNames(roleNames);

            return ResponseDTO.ok(userVO);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户详情失败：userId={}", userId, e);
            throw new BusinessException("获取用户详情失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> addUser(UserAddForm addForm) {
        try {
            // 1. 参数验证
            this.validateAddForm(addForm);

            // 2. 检查用户名是否存在
            if (this.checkUserNameExists(addForm.getUserName(), null)) {
                throw new BusinessException("用户名已存在");
            }

            // 3. 检查邮箱是否存在
            if (this.checkEmailExists(addForm.getEmail(), null)) {
                throw new BusinessException("邮箱已存在");
            }

            // 4. 转换为实体对象
            UserEntity user = SmartBeanUtil.copy(addForm, UserEntity.class);

            // 5. 密码加密
            String salt = CryptoUtils.generateSalt();
            String encryptedPassword = CryptoUtils.encryptPassword(addForm.getPassword(), salt);
            user.setPassword(encryptedPassword + ":" + salt);

            // 6. 设置默认值
            user.setStatus(1);
            user.setSortValue(0);

            // 7. 保存用户
            int result = userDao.insert(user);
            if (result <= 0) {
                throw new BusinessException("保存用户失败");
            }

            // 8. 清除缓存
            userCacheManager.removeUserCache(user.getUserId());

            // 9. 保存用户角色关系
            if (CollUtil.isNotEmpty(addForm.getRoleIds())) {
                userRoleService.saveUserRoles(user.getUserId(), addForm.getRoleIds());
            }

            log.info("新增用户成功：userId={}, userName={}", user.getUserId(), user.getUserName());
            return ResponseDTO.ok(user.getUserId().toString());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增用户失败", e);
            throw new BusinessException("新增用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateUser(UserUpdateForm updateForm) {
        try {
            // 1. 参数验证
            this.validateUpdateForm(updateForm);

            // 2. 获取原用户信息
            UserEntity originalUser = userDao.selectById(updateForm.getUserId());
            if (originalUser == null || originalUser.getDeletedFlag() == 1) {
                throw new BusinessException("用户不存在");
            }

            // 3. 检查用户名是否被其他用户使用
            if (!Objects.equals(originalUser.getUserName(), updateForm.getUserName()) &&
                this.checkUserNameExists(updateForm.getUserName(), updateForm.getUserId())) {
                throw new BusinessException("用户名已存在");
            }

            // 4. 检查邮箱是否被其他用户使用
            if (!Objects.equals(originalUser.getEmail(), updateForm.getEmail()) &&
                this.checkEmailExists(updateForm.getEmail(), updateForm.getUserId())) {
                throw new BusinessException("邮箱已存在");
            }

            // 5. 转换为实体对象
            UserEntity user = SmartBeanUtil.copy(updateForm, UserEntity.class);
            user.setVersion(originalUser.getVersion()); // 设置版本号用于乐观锁

            // 6. 如果密码不为空，则更新密码
            if (StrUtil.isNotBlank(updateForm.getPassword())) {
                String salt = CryptoUtils.generateSalt();
                String encryptedPassword = CryptoUtils.encryptPassword(updateForm.getPassword(), salt);
                user.setPassword(encryptedPassword + ":" + salt);
            } else {
                user.setPassword(null); // 不更新密码
            }

            // 7. 更新用户
            int result = userDao.updateById(user);
            if (result <= 0) {
                throw new BusinessException("更新用户失败，可能是数据已被其他用户修改");
            }

            // 8. 清除缓存
            userCacheManager.removeUserCache(user.getUserId());

            // 9. 更新用户角色关系
            if (updateForm.getRoleIds() != null) {
                userRoleService.updateUserRoles(user.getUserId(), updateForm.getRoleIds());
            }

            log.info("更新用户成功：userId={}, userName={}", user.getUserId(), user.getUserName());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            throw new BusinessException("更新用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteUser(Long userId) {
        try {
            // 1. 获取用户信息
            UserEntity user = userDao.selectById(userId);
            if (user == null || user.getDeletedFlag() == 1) {
                throw new BusinessException("用户不存在");
            }

            // 2. 检查是否可以删除
            this.checkCanDelete(userId);

            // 3. 软删除用户
            int result = userDao.deleteSoft(userId, StpUtil.getLoginIdAsLong());
            if (result <= 0) {
                throw new BusinessException("删除用户失败");
            }

            // 4. 清除缓存
            userCacheManager.removeUserCache(userId);

            // 5. 删除用户角色关系
            userRoleService.deleteUserRoles(userId);

            log.info("删除用户成功：userId={}, userName={}", userId, user.getUserName());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败：userId={}", userId, e);
            throw new BusinessException("删除用户失败：" + e.getMessage());
        }
    }

    @Override
    public UserEntity getByUserName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }
        return userDao.selectByUserName(userName);
    }

    @Override
    public boolean checkUserNameExists(String userName, Long excludeUserId) {
        if (StrUtil.isBlank(userName)) {
            return false;
        }
        Long count = userDao.countByUserNameExcludeId(userName, excludeUserId);
        return count != null && count > 0;
    }

    /**
     * 验证新增表单
     */
    private void validateAddForm(UserAddForm addForm) {
        if (StrUtil.isBlank(addForm.getUserName())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StrUtil.isBlank(addForm.getEmail())) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!RegexUtils.isEmail(addForm.getEmail())) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StrUtil.isBlank(addForm.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (addForm.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
    }

    /**
     * 验证更新表单
     */
    private void validateUpdateForm(UserUpdateForm updateForm) {
        if (updateForm.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (StrUtil.isBlank(updateForm.getUserName())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StrUtil.isBlank(updateForm.getEmail())) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!RegexUtils.isEmail(updateForm.getEmail())) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StrUtil.isNotBlank(updateForm.getPassword()) && updateForm.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
    }

    /**
     * 检查邮箱是否存在
     */
    private boolean checkEmailExists(String email, Long excludeUserId) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        Long count = userDao.countByEmailExcludeId(email, excludeUserId);
        return count != null && count > 0;
    }

    /**
     * 检查是否可以删除
     */
    private void checkCanDelete(Long userId) {
        // 检查用户是否为超级管理员
        UserEntity user = userDao.selectById(userId);
        if (user != null && "admin".equals(user.getUserName())) {
            throw new BusinessException("超级管理员不能删除");
        }

        // 检查用户是否有待处理的业务数据
        // TODO: 根据实际业务添加检查逻辑
    }

    /**
     * 填充部门信息
     */
    private void fillDeptInfo(List<UserVO> userVOList) {
        if (CollUtil.isEmpty(userVOList)) {
            return;
        }

        // 获取所有部门ID
        Set<Long> deptIds = userVOList.stream()
                .map(UserVO::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollUtil.isEmpty(deptIds)) {
            return;
        }

        // 批量查询部门信息
        List<DeptEntity> deptList = deptService.listByIds(deptIds);
        Map<Long, String> deptNameMap = deptList.stream()
                .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));

        // 填充部门名称
        userVOList.forEach(userVO -> {
            if (userVO.getDeptId() != null) {
                userVO.setDeptName(deptNameMap.get(userVO.getDeptId()));
            }
        });
    }
}
```

## 🔧 条件构造器使用规范

### QueryWrapper使用示例
```java
@Service
public class UserQueryService {

    @Resource
    private UserDao userDao;

    /**
     * 使用QueryWrapper进行复杂查询
     */
    public List<UserEntity> queryByComplexConditions(UserQueryForm queryForm) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();

        // 1. 基础条件
        queryWrapper.eq("deleted_flag", 0);

        // 2. 用户名模糊查询
        if (StrUtil.isNotBlank(queryForm.getUserName())) {
            queryWrapper.like("user_name", queryForm.getUserName());
        }

        // 3. 邮箱精确查询
        if (StrUtil.isNotBlank(queryForm.getEmail())) {
            queryWrapper.eq("email", queryForm.getEmail());
        }

        // 4. 状态查询
        if (queryForm.getStatus() != null) {
            queryWrapper.eq("status", queryForm.getStatus());
        }

        // 5. 部门查询
        if (queryForm.getDeptId() != null) {
            queryWrapper.eq("dept_id", queryForm.getDeptId());
        }

        // 6. 时间范围查询
        if (StrUtil.isNotBlank(queryForm.getCreateTimeStart())) {
            queryWrapper.ge("create_time", queryForm.getCreateTimeStart());
        }
        if (StrUtil.isNotBlank(queryForm.getCreateTimeEnd())) {
            queryWrapper.le("create_time", queryForm.getCreateTimeEnd());
        }

        // 7. 年龄范围查询（基于JSON字段）
        if (queryForm.getMinAge() != null || queryForm.getMaxAge() != null) {
            if (queryForm.getMinAge() != null) {
                queryWrapper.apply("JSON_EXTRACT(extend_info, '$.age') >= {0}", queryForm.getMinAge());
            }
            if (queryForm.getMaxAge() != null) {
                queryWrapper.apply("JSON_EXTRACT(extend_info, '$.age') <= {0}", queryForm.getMaxAge());
            }
        }

        // 8. 排序
        queryWrapper.orderByAsc("sort_value")
                   .orderByDesc("create_time");

        return userDao.selectList(queryWrapper);
    }

    /**
     * 使用LambdaQueryWrapper（推荐）
     */
    public List<UserEntity> queryByLambdaConditions(UserQueryForm queryForm) {
        LambdaQueryWrapper<UserEntity> lambdaQuery = new LambdaQueryWrapper<>();

        // 1. 基础条件
        lambdaQuery.eq(UserEntity::getDeletedFlag, 0);

        // 2. 用户名模糊查询
        lambdaQuery.like(StrUtil.isNotBlank(queryForm.getUserName()),
                        UserEntity::getUserName, queryForm.getUserName());

        // 3. 邮箱精确查询
        lambdaQuery.eq(StrUtil.isNotBlank(queryForm.getEmail()),
                      UserEntity::getEmail, queryForm.getEmail());

        // 4. 状态查询
        lambdaQuery.eq(queryForm.getStatus() != null,
                      UserEntity::getStatus, queryForm.getStatus());

        // 5. 部门查询
        lambdaQuery.eq(queryForm.getDeptId() != null,
                      UserEntity::getDeptId, queryForm.getDeptId());

        // 6. 时间范围查询
        lambdaQuery.ge(StrUtil.isNotBlank(queryForm.getCreateTimeStart()),
                      UserEntity::getCreateTime, queryForm.getCreateTimeStart());
        lambdaQuery.le(StrUtil.isNotBlank(queryForm.getCreateTimeEnd()),
                      UserEntity::getCreateTime, queryForm.getCreateTimeEnd());

        // 7. 排序
        lambdaQuery.orderByAsc(UserEntity::getSortValue)
                   .orderByDesc(UserEntity::getCreateTime);

        return userDao.selectList(lambdaQuery);
    }

    /**
     * 统计查询
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 1. 总用户数
        Long totalCount = userDao.selectCount(
            new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getDeletedFlag, 0)
        );
        statistics.put("totalCount", totalCount);

        // 2. 活跃用户数
        Long activeCount = userDao.selectCount(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getDeletedFlag, 0)
                .eq(UserEntity::getStatus, 1)
        );
        statistics.put("activeCount", activeCount);

        // 3. 今日新增用户数
        Long todayCount = userDao.selectCount(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getDeletedFlag, 0)
                .ge(UserEntity::getCreateTime, LocalDate.now())
        );
        statistics.put("todayCount", todayCount);

        // 4. 按部门统计用户数
        List<Map<String, Object>> deptStatistics = userDao.selectMaps(
            new QueryWrapper<UserEntity>()
                .select("dept_id", "COUNT(*) as user_count")
                .eq("deleted_flag", 0)
                .groupBy("dept_id")
        );
        statistics.put("deptStatistics", deptStatistics);

        return statistics;
    }
}
```

## 🔨 代码生成器配置

### AutoGenerator配置
```java
@Component
public class MyBatisPlusGenerator {

    /**
     * 生成代码
     */
    public void generateCode(String moduleName, String tableName, String tablePrefix) {
        // 1. 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(
            "jdbc:mysql://localhost:3306/smart_admin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
            "root",
            "password"
        ).build();

        // 2. 全局配置
        GlobalConfig globalConfig = new GlobalConfig.Builder()
            .outputDir(System.getProperty("user.dir") + "/src/main/java")
            .author("SmartAdmin Generator")
            .enableSwagger() // 开启 swagger 模式
            .fileOverride() // 覆盖已生成文件
            .dateType(DateType.TIME_PACK) // 时间策略
            .commentDate("yyyy-MM-dd HH:mm:ss")
            .build();

        // 3. 包配置
        PackageConfig packageConfig = new PackageConfig.Builder()
            .parent("com.smart.admin")
            .moduleName(moduleName)
            .entity("entity")
            .service("service")
            .serviceImpl("service.impl")
            .mapper("mapper")
            .xml("mapper.xml")
            .controller("controller") // 不生成controller
            .pathInfo(Collections.singletonMap(OutputFile.xml,
                System.getProperty("user.dir") + "/src/main/resources/mapper/" + moduleName))
            .build();

        // 4. 策略配置
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
            .addInclude(tableName) // 设置需要生成的表名
            .addTablePrefix(tablePrefix) // 设置过滤表前缀
            .setNaming(NamingStrategy.underline_to_camel) // 下划线转驼峰命名
            .setColumnNaming(NamingStrategy.underline_to_camel) // 下划线转驼峰命名
            .setEntityLombokModel(true) // lombok 模型
            .setRestControllerStyle(true) // REST 风格
            .setControllerStyle(false) // 不生成controller
            .setLogicDeleteFieldName("deleted_flag") // 逻辑删除字段名
            .setVersionFieldName("version") // 乐观锁字段名
            .setTableFillList(Arrays.asList(
                new Column("create_time", FieldFill.INSERT),
                new Column("update_time", FieldFill.INSERT_UPDATE),
                new Column("create_user_id", FieldFill.INSERT),
                new Column("update_user_id", FieldFill.INSERT_UPDATE)
            )) // 填充字段
            .build();

        // 5. 模板引擎配置
        TemplateConfig templateConfig = new TemplateConfig.Builder()
            .disable(TemplateType.CONTROLLER) // 禁用controller生成
            .entity("/templates/entity.java")
            .service("/templates/service.java")
            .serviceImpl("/templates/serviceImpl.java")
            .mapper("/templates/mapper.java")
            .xml("/templates/mapper.xml")
            .build();

        // 6. 自定义配置
        InjectionConfig injectionConfig = new InjectionConfig.Builder()
            .beforeOutputFile((tableInfo, objectMap) -> {
                // 输出文件之前自定义逻辑
                log.info("正在生成文件：{}", tableInfo.getEntityName());
            })
            .customMap(Collections.singletonMap("moduleName", moduleName))
            .customFile(Collections.singletonMap("QueryForm.java",
                "/templates/queryForm.java"))
            .build();

        // 7. 执行生成
        AutoGenerator generator = new AutoGenerator(dataSourceConfig)
            .global(globalConfig)
            .packageInfo(packageConfig)
            .strategy(strategyConfig)
            .template(templateConfig)
            .injection(injectionConfig);

        generator.execute();

        log.info("代码生成完成：moduleName={}, tableName={}", moduleName, tableName);
    }
}
```

---

**🎯 核心原则**：
1. **代码生成优先** - 优先使用代码生成器生成基础代码
2. **注解驱动** - 全面使用注解替代XML配置
3. **安全第一** - 必须实现软删除、乐观锁、事务管理
4. **性能优化** - 避免N+1查询，使用批量操作
5. **可维护性** - 使用Lambda表达式提高代码可读性

**📖 相关文档**：
- [数据规范](./数据规范.md) - 数据库设计和操作规范
- [架构规范](./架构规范.md) - 架构设计规范
- [编码规范](./编码规范.md) - 编码风格规范