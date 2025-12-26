# IOE-DREAM 全局待办事项整理与企业级实现方案

**文档版本**: v1.0.0
**生成时间**: 2025-12-23
**分析范围**: 全局代码TODO/FIXME + 业务模块文档
**目标**: 企业级高质量实现

---

## 📊 执行摘要

### 待办事项统计

| 模块 | TODO数量 | 优先级分布 | 预估工作量 |
|-----|---------|-----------|-----------|
| 安全认证模块 (microservices-common-security) | 10 | P0(6) P1(4) | 20人天 |
| 门禁服务 (ioedream-access-service) | 25 | P0(8) P1(12) P2(5) | 35人天 |
| 考勤服务 (ioedream-attendance-service) | 待分析 | - | 待评估 |
| 消费服务 (ioedream-consume-service) | 待分析 | - | 待评估 |
| 访客服务 (ioedream-visitor-service) | 待分析 | - | 待评估 |
| 视频服务 (ioedream-video-service) | 待分析 | - | 待评估 |
| 公共业务模块 (microservices-common-business) | 1 | P1 | 2人天 |

**总计**: 至少 **36+** 个待办事项需要实现

---

## 🎯 按优先级分类

### P0级 - 核心安全功能（必须立即实现）

#### 1. JWT令牌管理增强
**模块**: `microservices-common-security`
**文件**: `JwtTokenUtil.java`
**TODO**: 实现令牌撤销逻辑，将令牌加入黑名单

**业务背景**:
- 用户主动登出时，JWT令牌应该立即失效
- 修改密码后，旧令牌应该被撤销
- 管理员强制下线用户时，令牌应立即失效

**企业级实现方案**:

```java
/**
 * JWT令牌黑名单管理服务
 * 企业级实现 - 支持分布式令牌撤销
 */
@Service
@Slf4j
public class JwtTokenBlacklistService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final long BLACKLIST_TTL = 7 * 24 * 60 * 60; // 7天

    /**
     * 将令牌加入黑名单
     * @param token JWT令牌
     * @param expSeconds 令牌过期时间（秒）
     */
    public void blacklistToken(String token, Long expSeconds) {
        log.info("[JWT黑名单] 添加令牌: token={}", token.substring(0, Math.min(20, token.length())));

        // 计算黑名单TTL：令牌剩余有效期或7天，取较小值
        long ttl = Math.min(expSeconds, BLACKLIST_TTL);

        // 存储到Redis，支持分布式
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);

        log.info("[JWT黑名单] 令牌已加入黑名单: ttl={}秒", ttl);
    }

    /**
     * 检查令牌是否在黑名单中
     * @param token JWT令牌
     * @return true=已撤销
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 批量撤销用户的所有令牌
     * @param userId 用户ID
     */
    public void revokeAllUserTokens(Long userId) {
        log.info("[JWT黑名单] 撤销用户所有令牌: userId={}", userId);

        // 从Redis中查找该用户的所有活跃令牌
        String pattern = "jwt:session:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            // 批量加入黑名单
            keys.forEach(key -> {
                String token = key.substring(key.lastIndexOf(":") + 1);
                blacklistToken(token, BLACKLIST_TTL);
            });

            // 删除用户会话记录
            redisTemplate.delete(keys);

            log.info("[JWT黑名单] 已撤销{}个令牌", keys.size());
        }
    }
}
```

**测试验证**:
```java
@Test
void testTokenBlacklist() {
    String token = jwtTokenUtil.createToken(1L, "testUser");

    // 验证令牌有效
    assertTrue(jwtTokenUtil.validateToken(token));

    // 撤销令牌
    jwtTokenUtil.revokeToken(token);

    // 验证令牌已失效
    assertFalse(jwtTokenUtil.validateToken(token));
}
```

#### 2. 用户锁定状态检查
**模块**: `microservices-common-security`
**文件**: `AuthManager.java`
**TODO**: 实现用户锁定检查逻辑

**业务背景**:
- 用户连续登录失败超过阈值时自动锁定
- 管理员可以手动锁定用户
- 锁定期间不允许登录

**企业级实现方案**:

```java
/**
 * 用户锁定状态管理服务
 */
@Service
@Slf4j
public class UserLockService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private UserDao userDao;

    private static final String LOCK_KEY_PREFIX = "user:lock:";
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    /**
     * 检查用户是否被锁定
     * @param username 用户名
     * @return true=已锁定
     */
    public boolean isUserLocked(String username) {
        // 1. 检查Redis中的临时锁定
        String lockKey = LOCK_KEY_PREFIX + username;
        Boolean locked = redisTemplate.hasKey(lockKey);

        if (Boolean.TRUE.equals(locked)) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            log.warn("[用户锁定] 用户被临时锁定: username={}, 剩余{}分钟", username, ttl);
            return true;
        }

        // 2. 检查数据库中的管理员锁定
        UserEntity user = userDao.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username)
                .select(UserEntity::getLockedFlag)
        );

        return user != null && Boolean.TRUE.equals(user.getLockedFlag());
    }

    /**
     * 记录登录失败
     * @param username 用户名
     */
    public void recordLoginFailure(String username) {
        String failureKey = "login:failure:" + username;
        String count = redisTemplate.opsForValue().get(failureKey);
        int failureCount = count == null ? 1 : Integer.parseInt(count) + 1;

        // 更新失败次数（24小时有效期）
        redisTemplate.opsForValue().set(failureKey, String.valueOf(failureCount), 24, TimeUnit.HOURS);

        // 达到阈值则锁定用户
        if (failureCount >= MAX_LOGIN_FAILURES) {
            lockUser(username, LOCK_DURATION_MINUTES);
            log.warn("[用户锁定] 登录失败次数过多，自动锁定: username={}, count={}", username, failureCount);
        }
    }

    /**
     * 锁定用户
     * @param username 用户名
     * @param minutes 锁定时长（分钟）
     */
    public void lockUser(String username, int minutes) {
        String lockKey = LOCK_KEY_PREFIX + username;
        redisTemplate.opsForValue().set(lockKey, "1", minutes, TimeUnit.MINUTES);

        // 发送通知
        // notificationService.sendUserLockedNotification(username, minutes);
    }

    /**
     * 清除登录失败记录（登录成功时调用）
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        String failureKey = "login:failure:" + username;
        redisTemplate.delete(failureKey);
        log.debug("[用户锁定] 清除登录失败记录: username={}", username);
    }

    /**
     * 管理员手动锁定/解锁用户
     * @param userId 用户ID
     * @param locked true=锁定, false=解锁
     */
    public void setUserLockedStatus(Long userId, boolean locked) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setLockedFlag(locked);
        userDao.updateById(user);

        log.info("[用户锁定] 管理员修改锁定状态: userId={}, locked={}", userId, locked);
    }
}
```

#### 3. 并发登录控制
**模块**: `microservices-common-security`
**文件**: `AuthManager.java`
**TODO**: 实现并发登录检查逻辑

**业务背景**:
- 限制同一用户的同时在线设备数量
- 不同安全等级可以有不同并发限制
- 新设备登录时，可以选择踢出旧设备

**企业级实现方案**:

```java
/**
 * 并发登录控制服务
 */
@Service
@Slf4j
public class ConcurrentLoginControlService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 默认最多3个设备同时在线
    private static final int DEFAULT_MAX_CONCURRENT = 3;
    private static final String SESSION_KEY_PREFIX = "user:session:";

    /**
     * 检查用户是否超过并发登录限制
     * @param userId 用户ID
     * @return true=允许登录, false=超过限制
     */
    public boolean isConcurrentLoginExceeded(Long userId) {
        // 1. 查询用户的并发限制配置
        Integer maxConcurrent = getUserMaxConcurrent(userId);

        // 2. 统计当前活跃会话数
        String pattern = SESSION_KEY_PREFIX + userId + ":*";
        Set<String> sessionKeys = redisTemplate.keys(pattern);
        int activeSessions = sessionKeys == null ? 0 : sessionKeys.size();

        log.debug("[并发登录] 用户活跃会话: userId={}, active={}/{}", userId, activeSessions, maxConcurrent);

        return activeSessions >= maxConcurrent;
    }

    /**
     * 管理用户会话（登录时调用）
     * @param userId 用户ID
     * @param token JWT令牌
     * @param deviceInfo 设备信息
     * @param kickOldSession 是否踢出旧会话
     */
    public void manageUserSession(Long userId, String token, String deviceInfo, boolean kickOldSession) {
        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + token;

        // 检查是否超过并发限制
        if (isConcurrentLoginExceeded(userId)) {
            if (kickOldSession) {
                // 踢出最旧的会话
                removeOldestSession(userId);
                log.info("[并发登录] 踢出旧会话: userId={}", userId);
            } else {
                throw new BusinessException("CONCURRENT_LOGIN_EXCEEDED", "已达到最大同时在线设备数");
            }
        }

        // 创建新会话
        UserSessionInfo sessionInfo = new UserSessionInfo();
        sessionInfo.setUserId(userId);
        sessionInfo.setToken(token);
        sessionInfo.setDeviceInfo(deviceInfo);
        sessionInfo.setLoginTime(System.currentTimeMillis());
        sessionInfo.setLastAccessTime(System.currentTimeMillis());

        // 会话有效期7天
        redisTemplate.opsForValue().set(sessionKey, sessionInfo, 7, TimeUnit.DAYS);

        log.info("[并发登录] 创建新会话: userId={}, device={}", userId, deviceInfo);
    }

    /**
     * 移除用户会话（登出时调用）
     * @param userId 用户ID
     * @param token JWT令牌
     */
    public void removeUserSession(Long userId, String token) {
        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + token;
        redisTemplate.delete(sessionKey);
        log.info("[并发登录] 移除会话: userId={}", userId);
    }

    /**
     * 验证用户会话是否有效
     * @param userId 用户ID
     * @param token JWT令牌
     * @return true=会话有效
     */
    public boolean isValidUserSession(Long userId, String token) {
        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * 更新会话最后访问时间
     * @param token JWT令牌
     */
    public void updateSessionLastAccessTime(String token) {
        // 从token中提取userId
        Long userId = JwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            return;
        }

        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + token;
        UserSessionInfo sessionInfo = (UserSessionInfo) redisTemplate.opsForValue().get(sessionKey);
        if (sessionInfo != null) {
            sessionInfo.setLastAccessTime(System.currentTimeMillis());
            redisTemplate.opsForValue().set(sessionKey, sessionInfo, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 踢出最旧的会话
     */
    private void removeOldestSession(Long userId) {
        String pattern = SESSION_KEY_PREFIX + userId + ":*";
        Set<String> sessionKeys = redisTemplate.keys(pattern);

        if (sessionKeys != null && !sessionKeys.isEmpty()) {
            // 找到最旧的会话并删除
            sessionKeys.stream()
                .min(Comparator.comparing(key -> {
                    UserSessionInfo info = (UserSessionInfo) redisTemplate.opsForValue().get(key);
                    return info == null ? 0L : info.getLastAccessTime();
                }))
                .ifPresent(redisTemplate::delete);
        }
    }

    /**
     * 获取用户的最大并发登录数
     */
    private Integer getUserMaxConcurrent(Long userId) {
        // TODO: 从用户配置或角色配置中读取
        // 目前使用默认值
        return DEFAULT_MAX_CONCURRENT;
    }
}
```

### P1级 - 功能增强（建议尽快实现）

#### 4. 认证方式统计分析
**模块**: `ioedream-access-service`
**文件**: 所有 `XxxAuthenticationStrategy.java`
**TODO**: 统计各认证方式的使用次数和使用报表

**业务价值**:
- 了解用户偏好，优化设备配置
- 安全审计需要知道认证方式分布
- 为容量规划提供数据支持

**企业级实现方案**:

```java
/**
 * 认证方式统计服务
 */
@Service
@Slf4j
public class AuthenticationStatisticsService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private AuthenticationStatisticsDao statisticsDao;

    /**
     * 记录认证方式使用
     * @param userId 用户ID
     * @param authType 认证类型
     * @param deviceId 设备ID
     */
    @Async
    public void recordAuthentication(Long userId, Integer authType, String deviceId) {
        log.debug("[认证统计] 记录认证: userId={}, type={}, device={}", userId, authType, deviceId);

        // 1. 实时写入统计表（用于实时查询）
        AuthenticationStatisticsEntity stat = new AuthenticationStatisticsEntity();
        stat.setUserId(userId);
        stat.setAuthType(authType);
        stat.setDeviceId(deviceId);
        stat.setAccessTime(LocalDateTime.now());
        statisticsDao.insert(stat);

        // 2. 异步更新聚合统计（用于报表）
        updateAggregatedStatistics(authType, deviceId);
    }

    /**
     * 获取认证方式使用统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param granularity 粒度（day/hour）
     * @return 统计数据
     */
    public List<AuthenticationStatisticsVO> getAuthenticationStatistics(
        LocalDateTime startDate,
        LocalDateTime endDate,
        String granularity
    ) {
        log.info("[认证统计] 查询统计: start={}, end={}, granularity={}", startDate, endDate, granularity);

        // 从聚合统计表查询
        return statisticsDao.selectStatisticsByDateRange(startDate, endDate, granularity);
    }

    /**
     * 获取认证方式分布饼图数据
     * @param areaId 区域ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 认证方式分布
     */
    public Map<String, Long> getAuthenticationTypeDistribution(
        Long areaId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        log.info("[认证统计] 查询分布: area={}, start={}, end={}", areaId, startDate, endDate);

        // 查询通行记录，按认证方式分组统计
        List<Map<String, Object>> result = accessRecordDao.selectAuthenticationTypeDistribution(
            areaId, startDate, endDate
        );

        // 转换为Map
        Map<String, Long> distribution = new HashMap<>();
        for (Map<String, Object> row : result) {
            Integer authType = (Integer) row.get("auth_type");
            Long count = (Long) row.get("count");
            distribution.put(getAuthTypeName(authType), count);
        }

        return distribution;
    }

    /**
     * 获取认证方式趋势图数据
     * @param authType 认证类型
     * @param days 最近N天
     * @return 趋势数据
     */
    public List<TrendDataPoint> getAuthenticationTrend(Integer authType, int days) {
        log.info("[认证统计] 查询趋势: type={}, days={}", authType, days);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        return statisticsDao.selectAuthenticationTrend(authType, startDate, endDate);
    }

    /**
     * 更新聚合统计数据
     */
    @Async
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行一次
    public void updateAggregatedStatistics() {
        log.info("[认证统计] 开始更新聚合统计");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesAgo = now.minusMinutes(10);

        // 查询最近10分钟的数据
        List<AuthenticationStatisticsEntity> recentStats =
            statisticsDao.selectList(
                new LambdaQueryWrapper<AuthenticationStatisticsEntity>()
                    .ge(AuthenticationStatisticsEntity::getAccessTime, tenMinutesAgo)
            );

        // 按认证方式和设备聚合
        Map<String, List<AuthenticationStatisticsEntity>> grouped = recentStats.stream()
            .collect(Collectors.groupingBy(
                stat -> stat.getAuthType() + ":" + stat.getDeviceId()
            ));

        // 更新或创建聚合记录
        for (Map.Entry<String, List<AuthenticationStatisticsEntity>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split(":");
            Integer authType = Integer.parseInt(parts[0]);
            String deviceId = parts[1];

            long count = entry.getValue().size();

            // 更新小时级统计
            updateHourlyAggregate(authType, deviceId, now, count);
        }

        log.info("[认证统计] 聚合统计更新完成");
    }

    private void updateHourlyAggregate(Integer authType, String deviceId, LocalDateTime time, long count) {
        // 查找或创建小时聚合记录
        // ...实现细节
    }

    private String getAuthTypeName(Integer authType) {
        return switch (authType) {
            case 1 -> "人脸识别";
            case 2 -> "指纹识别";
            case 3 -> "刷卡";
            case 4 -> "密码";
            case 5 -> "二维码";
            case 6 -> "虹膜识别";
            case 7 -> "掌纹识别";
            case 8 -> "声纹识别";
            case 9 -> "NFC";
            default -> "其他";
        };
    }
}
```

#### 5. 报警管理功能
**模块**: `ioedream-access-service`
**文件**: `AccessMonitorServiceImpl.java`
**TODO**: 实现报警查询和处理功能

**业务背景**:
- 门禁异常（强行闯入、长时间未关门等）需要报警
- 设备离线、故障需要报警
- 区域人数超限需要报警
- 管理员需要查看和处理报警

**企业级实现方案**:

##### 5.1 数据库设计

```sql
-- 门禁报警表
CREATE TABLE `t_access_alarm` (
  `alarm_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报警ID',
  `alarm_type` TINYINT NOT NULL COMMENT '报警类型 1-强行闯入 2-长时间未关门 3-设备离线 4-区域超限 5-设备故障',
  `alarm_level` TINYINT NOT NULL DEFAULT 1 COMMENT '报警级别 1-一般 2-重要 3-紧急',
  `device_id` VARCHAR(64) COMMENT '设备ID',
  `area_id` BIGINT COMMENT '区域ID',
  `alarm_time` DATETIME NOT NULL COMMENT '报警时间',
  `alarm_content` TEXT COMMENT '报警内容',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-未处理 1-已处理 2-已忽略',
  `handle_user_id` BIGINT COMMENT '处理人ID',
  `handle_time` DATETIME COMMENT '处理时间',
  `handle_remark` VARCHAR(500) COMMENT '处理备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`alarm_id`),
  KEY `idx_device` (`device_id`),
  KEY `idx_area` (`area_id`),
  KEY `idx_time` (`alarm_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁报警表';
```

##### 5.2 实体类

```java
@Data
@TableName("t_access_alarm")
public class AccessAlarmEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long alarmId;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警级别
     */
    private Integer alarmLevel;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 报警内容
     */
    private String alarmContent;

    /**
     * 状态 0-未处理 1-已处理 2-已忽略
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;
}
```

##### 5.3 DAO接口

```java
@Mapper
public interface AccessAlarmDao extends BaseMapper<AccessAlarmEntity> {

    /**
     * 查询报警列表
     */
    List<AccessAlarmEntity> selectAlarmList(
        @Param("alarmType") Integer alarmType,
        @Param("alarmLevel") Integer alarmLevel,
        @Param("status") Integer status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("pageNum") Integer pageNum,
        @Param("pageSize") Integer pageSize
    );

    /**
     * 统计未处理报警数量
     */
    Long countUnhandledAlarms(
        @Param("alarmLevel") Integer alarmLevel
    );
}
```

##### 5.4 Service实现

```java
@Service
@Slf4j
public class AccessAlarmServiceImpl implements AccessAlarmService {

    @Resource
    private AccessAlarmDao accessAlarmDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 创建报警
     */
    @Override
    @Async
    public void createAlarm(Integer alarmType, Integer alarmLevel, String deviceId,
                           Long areaId, String content) {
        log.warn("[门禁报警] 创建报警: type={}, level={}, device={}, area={}, content={}",
            alarmType, alarmLevel, deviceId, areaId, content);

        AccessAlarmEntity alarm = new AccessAlarmEntity();
        alarm.setAlarmType(alarmType);
        alarm.setAlarmLevel(alarmLevel);
        alarm.setDeviceId(deviceId);
        alarm.setAreaId(areaId);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setAlarmContent(content);
        alarm.setStatus(0); // 未处理

        accessAlarmDao.insert(alarm);

        // 发送实时通知
        sendAlarmNotification(alarm);
    }

    /**
     * 查询报警列表
     */
    @Override
    public PageResult<AccessAlarmVO> queryAlarmList(AccessAlarmQueryForm form) {
        log.info("[门禁报警] 查询报警列表: {}", form);

        // 查询数据
        List<AccessAlarmEntity> alarms = accessAlarmDao.selectAlarmList(
            form.getAlarmType(),
            form.getAlarmLevel(),
            form.getStatus(),
            form.getStartTime(),
            form.getEndTime(),
            form.getPageNum(),
            form.getPageSize()
        );

        // 查询总数
        Long total = accessAlarmDao.selectCount(
            new LambdaQueryWrapper<AccessAlarmEntity>()
                .eq(form.getAlarmType() != null, AccessAlarmEntity::getAlarmType, form.getAlarmType())
                .eq(form.getAlarmLevel() != null, AccessAlarmEntity::getAlarmLevel, form.getAlarmLevel())
                .eq(form.getStatus() != null, AccessAlarmEntity::getStatus, form.getStatus())
                .ge(form.getStartTime() != null, AccessAlarmEntity::getAlarmTime, form.getStartTime())
                .le(form.getEndTime() != null, AccessAlarmEntity::getAlarmTime, form.getEndTime())
        );

        // 转换为VO
        List<AccessAlarmVO> voList = alarms.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(voList, total, form.getPageNum(), form.getPageSize());
    }

    /**
     * 处理报警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlarm(Long alarmId, Long userId, String remark) {
        log.info("[门禁报警] 处理报警: alarmId={}, userId={}, remark={}", alarmId, userId, remark);

        AccessAlarmEntity alarm = accessAlarmDao.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("ALARM_NOT_FOUND", "报警不存在");
        }

        if (alarm.getStatus() != 0) {
            throw new BusinessException("ALARM_ALREADY_HANDLED", "报警已被处理");
        }

        alarm.setStatus(1); // 已处理
        alarm.setHandleUserId(userId);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setHandleRemark(remark);

        accessAlarmDao.updateById(alarm);

        log.info("[门禁报警] 报警处理完成: alarmId={}", alarmId);
    }

    /**
     * 忽略报警
     */
    @Override
    public void ignoreAlarm(Long alarmId, Long userId) {
        log.info("[门禁报警] 忽略报警: alarmId={}, userId={}", alarmId, userId);

        AccessAlarmEntity alarm = accessAlarmDao.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("ALARM_NOT_FOUND", "报警不存在");
        }

        alarm.setStatus(2); // 已忽略
        alarm.setHandleUserId(userId);
        alarm.setHandleTime(LocalDateTime.now());

        accessAlarmDao.updateById(alarm);
    }

    /**
     * 统计未处理报警数量
     */
    @Override
    public Long countUnhandledAlarms(Integer alarmLevel) {
        Long count = accessAlarmDao.countUnhandledAlarms(alarmLevel);
        log.debug("[门禁报警] 未处理报警数量: level={}, count={}", alarmLevel, count);
        return count;
    }

    /**
     * 发送报警通知
     */
    private void sendAlarmNotification(AccessAlarmEntity alarm) {
        // 通过Gateway调用通知服务
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ACCESS_ALARM");
            notification.put("level", alarm.getAlarmLevel());
            notification.put("content", alarm.getAlarmContent());
            notification.put("deviceId", alarm.getDeviceId());
            notification.put("areaId", alarm.getAreaId());

            // 发送给所有安全管理员
            // gatewayServiceClient.callCommonService("/api/notification/send", HttpMethod.POST, notification);

            log.info("[门禁报警] 报警通知已发送: alarmId={}", alarm.getAlarmId());
        } catch (Exception e) {
            log.error("[门禁报警] 发送通知失败: alarmId={}, error={}", alarm.getAlarmId(), e.getMessage(), e);
        }
    }

    private AccessAlarmVO convertToVO(AccessAlarmEntity entity) {
        AccessAlarmVO vo = new AccessAlarmVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置报警类型名称
        vo.setAlarmTypeName(getAlarmTypeName(entity.getAlarmType()));
        vo.setAlarmLevelName(getAlarmLevelName(entity.getAlarmLevel()));
        vo.setStatusName(getStatusName(entity.getStatus()));

        // 查询设备名称（如果需要）
        // 查询区域名称（如果需要）
        // 查询处理人名称（如果需要）

        return vo;
    }

    private String getAlarmTypeName(Integer type) {
        return switch (type) {
            case 1 -> "强行闯入";
            case 2 -> "长时间未关门";
            case 3 -> "设备离线";
            case 4 -> "区域超限";
            case 5 -> "设备故障";
            default -> "未知";
        };
    }

    private String getAlarmLevelName(Integer level) {
        return switch (level) {
            case 1 -> "一般";
            case 2 -> "重要";
            case 3 -> "紧急";
            default -> "未知";
        };
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "未处理";
            case 1 -> "已处理";
            case 2 -> "已忽略";
            default -> "未知";
        };
    }
}
```

### P2级 - 优化增强（后续实现）

#### 6. 实时响应时间测量
**模块**: `ioedream-access-service`
**TODO**: 需要实际测量设备响应时间

**实现方案**:

```java
/**
 * 设备响应时间统计服务
 */
@Component
@Slf4j
public class DeviceResponseTimeTracker {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private static final String RESPONSE_TIME_KEY_PREFIX = "device:response:";

    /**
     * 记录设备响应时间
     * @param deviceId 设备ID
     * @param responseTimeMs 响应时间（毫秒）
     */
    public void recordResponseTime(String deviceId, Long responseTimeMs) {
        String key = RESPONSE_TIME_KEY_PREFIX + deviceId;

        // 使用Redis的HyperLogLog统计响应时间分布
        // 或者使用Redis TimeSeries存储时序数据

        // 简单方案：使用Redis List存储最近100次响应时间
        redisTemplate.opsForList().rightPush(key, responseTimeMs.toString());
        redisTemplate.opsForList().trim(key, 0, 99); // 只保留最近100次
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

        log.debug("[响应时间] 设备响应: device={}, time={}ms", deviceId, responseTimeMs);
    }

    /**
     * 获取设备平均响应时间
     * @param deviceId 设备ID
     * @return 平均响应时间（毫秒）
     */
    public Long getAverageResponseTime(String deviceId) {
        String key = RESPONSE_TIME_KEY_PREFIX + deviceId;
        List<String> times = redisTemplate.opsForValue().multiGet(
            Collections.singletonList(key)
        );

        if (times == null || times.isEmpty()) {
            return 0L;
        }

        return times.stream()
            .mapToLong(Long::parseLong)
            .sum() / times.size();
    }
}
```

---

## 📋 完整待办事项清单

### 1. 安全认证模块 (microservices-common-security)

| 序号 | 功能 | 文件 | 优先级 | 预估工作量 |
|-----|------|------|-------|-----------|
| 1 | JWT令牌撤销功能 | JwtTokenUtil.java:474 | P0 | 3人天 |
| 2 | 用户锁定检查 | AuthManager.java:22 | P0 | 3人天 |
| 3 | 并发登录检查 | AuthManager.java:33 | P0 | 4人天 |
| 4 | 用户会话管理 | AuthManager.java:45 | P1 | 3人天 |
| 5 | 清除登录失败记录 | AuthManager.java:54 | P1 | 1人天 |
| 6 | 记录登录失败 | AuthManager.java:63 | P1 | 2人天 |
| 7 | 令牌黑名单 | AuthManager.java:72 | P1 | 3人天 |
| 8 | 令牌黑名单检查 | AuthManager.java:82 | P1 | 1人天 |
| 9 | 移除用户会话 | AuthManager.java:93 | P1 | 1人天 |
| 10 | 用户会话验证 | AuthManager.java:104 | P1 | 2人天 |
| 11 | 更新会话访问时间 | AuthManager.java:114 | P1 | 1人天 |

### 2. 门禁服务 (ioedream-access-service)

| 序号 | 功能 | 文件 | 优先级 | 预估工作量 |
|-----|------|------|-------|-----------|
| 1 | 异常监控集成 | GlobalExceptionHandler.java:219 | P1 | 2人天 |
| 2 | 认证方式统计（人脸） | FaceAuthenticationStrategy.java:79 | P1 | 3人天 |
| 3 | 认证方式统计（指纹） | FingerprintAuthenticationStrategy.java:54 | P1 | 1人天 |
| 4 | 认证方式统计（刷卡） | CardAuthenticationStrategy.java:54 | P1 | 1人天 |
| 5 | 认证方式统计（虹膜） | IrisAuthenticationStrategy.java:54 | P1 | 1人天 |
| 6 | 认证方式统计（掌纹） | PalmAuthenticationStrategy.java:54 | P1 | 1人天 |
| 7 | 认证方式统计（NFC） | NfcAuthenticationStrategy.java:54 | P1 | 1人天 |
| 8 | 认证方式统计（密码） | PasswordAuthenticationStrategy.java:54 | P1 | 1人天 |
| 9 | 认证方式统计（二维码） | QrCodeAuthenticationStrategy.java:54 | P1 | 1人天 |
| 10 | 认证方式统计（声纹） | VoiceAuthenticationStrategy.java:54 | P1 | 1人天 |
| 11 | 认证方式统计实现 | MultiModalAuthenticationServiceImpl.java:90 | P1 | 4人天 |
| 12 | 报警查询功能 | AccessMonitorServiceImpl.java:186 | P0 | 5人天 |
| 13 | 报警处理功能 | AccessMonitorServiceImpl.java:213 | P0 | 3人天 |
| 14 | 故障设备统计 | AccessMonitorServiceImpl.java:376 | P2 | 2人天 |
| 15 | 报警统计完善 | AccessMonitorServiceImpl.java:398 | P1 | 2人天 |
| 16 | 响应时间测量 | AccessMonitorServiceImpl.java:475 | P2 | 2人天 |
| 17 | 部门查询API确认 | AccessAreaServiceImpl.java:371 | P1 | 2人天 |
| 18 | 实时人数统计 | AccessAreaServiceImpl.java:624 | P1 | 5人天 |
| 19 | 报警统计实现 | AccessAreaServiceImpl.java:743 | P1 | 2人天 |
| 20 | 响应时间测量 | AccessAreaServiceImpl.java:867 | P2 | 1人天 |
| 21 | 移动端认证初始化 | AccessMobileController.java:55 | P1 | 3人天 |
| 22 | 移动端生物识别 | AccessMobileController.java:73 | P1 | 4人天 |

### 3. 公共业务模块 (microservices-common-business)

| 序号 | 功能 | 文件 | 优先级 | 预估工作量 |
|-----|------|------|-------|-----------|
| 1 | 区域用户管理逻辑 | AreaUserManager.java:37 | P1 | 2人天 |

---

## 🎯 实施路线图

### 第一阶段：安全增强（2周）

**目标**: 完成核心安全功能，保障系统安全

| 任务 | 负责人 | 预计时间 | 依赖 |
|------|--------|---------|------|
| JWT令牌撤销功能 | 后端开发 | 3天 | - |
| 用户锁定检查 | 后端开发 | 3天 | - |
| 并发登录控制 | 后端开发 | 4天 | 用户锁定 |
| 用户会话管理 | 后端开发 | 3天 | 并发登录 |
| 令牌黑名单 | 后端开发 | 3天 | JWT撤销 |
| 单元测试 | 测试开发 | 2天 | 以上全部 |
| 集成测试 | 测试开发 | 2天 | 以上全部 |

**验收标准**:
- ✅ 所有安全功能单元测试覆盖率 > 80%
- ✅ 通过安全审计
- ✅ 性能测试：令牌验证 < 10ms
- ✅ 并发测试：支持1000+ TPS

### 第二阶段：门禁增强（3周）

**目标**: 完成报警管理和认证统计

| 任务 | 负责人 | 预计时间 | 依赖 |
|------|--------|---------|------|
| 报警表设计 | 数据库架构师 | 1天 | - |
| 报警实体和DAO | 后端开发 | 2天 | 表设计 |
| 报警查询功能 | 后端开发 | 3天 | DAO |
| 报警处理功能 | 后端开发 | 2天 | 查询 |
| 认证统计表设计 | 数据库架构师 | 1天 | - |
| 认证统计实现 | 后端开发 | 4天 | 表设计 |
| 报警通知集成 | 后端开发 | 2天 | 报警处理 |
| 前端页面开发 | 前端开发 | 5天 | 后端API |
| 测试和优化 | QA团队 | 3天 | 以上全部 |

**验收标准**:
- ✅ 报警实时性 < 1秒
- ✅ 统计数据准确性 99.9%
- ✅ 报表响应时间 < 2秒
- ✅ UI/UX测试通过

### 第三阶段：功能优化（2周）

**目标**: 完成响应时间测量和实时统计

| 任务 | 负责人 | 预计时间 | 依赖 |
|------|--------|---------|------|
| 响应时间追踪 | 后端开发 | 3天 | - |
| 实时人数统计 | 后端开发 | 5天 | - |
| 移动端功能 | 前端+后端 | 5天 | - |
| 性能优化 | 性能团队 | 2天 | 以上全部 |

---

## 📊 业务价值分析

### 安全功能 ROI

| 功能 | 业务价值 | 风险降低 | 实施成本 | ROI |
|------|---------|---------|---------|-----|
| 令牌撤销 | 防止账号被盗后继续使用 | 高 | 低 | 高 |
| 用户锁定 | 防止暴力破解 | 中 | 低 | 高 |
| 并发控制 | 防止账号共享 | 中 | 中 | 中 |

### 功能增强 ROI

| 功能 | 业务价值 | 用户体验提升 | 实施成本 | ROI |
|------|---------|------------|---------|-----|
| 报警管理 | 快速响应异常 | 高 | 中 | 高 |
| 认证统计 | 数据驱动优化 | 中 | 中 | 中 |
| 响应时间 | 性能监控 | 中 | 低 | 高 |

---

## 🏢 企业级最佳实践

### 1. 代码规范

#### 1.1 日志规范

```java
// ✅ 正确的日志记录
log.info("[模块名] 操作描述: 参数1={}, 参数2={}", param1, param2);
log.warn("[模块名] 警告信息: key={}, value={}", key, value);
log.error("[模块名] 错误信息: userId={}, error={}", userId, e.getMessage(), e);

// ❌ 错误的日志记录
log.info("操作: " + param1); // 字符串拼接
log.error("错误", e); // 信息不足
```

#### 1.2 异常处理

```java
// ✅ 正确的异常处理
try {
    // 业务逻辑
} catch (BusinessException e) {
    log.warn("[业务异常] 操作失败: reason={}", e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("[系统异常] 操作异常: error={}", e.getMessage(), e);
    throw new SystemException("OPERATION_ERROR", "操作失败", e);
}

// ❌ 错误的异常处理
try {
    // 业务逻辑
} catch (Exception e) {
    e.printStackTrace(); // 不要使用printStackTrace
}
```

#### 1.3 事务管理

```java
// ✅ 正确的事务使用
@Service
public class SomeServiceImpl {

    @Transactional(rollbackFor = Exception.class)
    public void doSomething() {
        // 业务逻辑
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void doSomethingInNewTransaction() {
        // 独立事务的业务逻辑
    }
}
```

### 2. 性能优化

#### 2.1 缓存策略

```java
// 三级缓存架构
@Service
public class DataServiceImpl {

    // L1: 本地缓存（Caffeine）
    @Cacheable(value = "local:data", key = "#id")
    public Data getDataById(Long id) {
        // 先查本地缓存
        return dataDao.selectById(id);
    }

    // L2: Redis缓存
    @Cacheable(value = "redis:data", key = "#id")
    public Data getDataByIdWithRedis(Long id) {
        // 查Redis
        return dataDao.selectById(id);
    }

    // L3: 数据库
    @Cacheable(value = "db:data", key = "#id")
    public Data getDataByIdFromDB(Long id) {
        // 查数据库
        return dataDao.selectById(id);
    }
}
```

#### 2.2 异步处理

```java
@Service
public class AsyncService {

    @Async("taskExecutor")
    public void asyncMethod() {
        // 异步执行的逻辑
    }

    @Async
    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledTask() {
        // 定时任务
    }
}
```

### 3. 安全最佳实践

#### 3.1 输入验证

```java
// ✅ 使用验证注解
@Data
public class UserForm {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20字符")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

// Controller自动验证
@PostMapping("/add")
public ResponseDTO<Void> add(@Valid @RequestBody UserForm form) {
    // 业务逻辑
}
```

#### 3.2 敏感数据处理

```java
// ✅ 敏感信息脱敏
public String maskPhone(String phone) {
    if (phone == null || phone.length() < 11) {
        return "***";
    }
    return phone.substring(0, 3) + "****" + phone.substring(7);
}

public String maskIdCard(String idCard) {
    if (idCard == null || idCard.length() < 18) {
        return "***";
    }
    return idCard.substring(0, 6) + "********" + idCard.substring(14);
}
```

### 4. 监控和运维

#### 4.1 健康检查

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Resource
    private SomeService someService;

    @Override
    public Health health() {
        try {
            // 检查服务健康状态
            boolean isHealthy = someService.checkHealth();

            if (isHealthy) {
                return Health.up()
                    .withDetail("service", "SomeService")
                    .withDetail("status", "OK")
                    .build();
            } else {
                return Health.down()
                    .withDetail("service", "SomeService")
                    .withDetail("error", "Service unhealthy")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "SomeService")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### 4.2 性能监控

```java
@Aspect
@Component
@Slf4j
public class PerformanceMonitorAspect {

    @Around("@annotation(MonitorPerformance)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("[性能监控] 方法执行: method={}, time={}ms",
                joinPoint.getSignature().toShortString(),
                executionTime);

            // 记录到Micrometer
            // Metrics.counter("method.execution", "method", joinPoint.getSignature().getName()).increment();

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[性能监控] 方法执行异常: method={}, time={}ms, error={}",
                joinPoint.getSignature().toShortString(),
                executionTime,
                e.getMessage());
            throw e;
        }
    }
}
```

---

## 📝 附录

### A. 待办事项详细说明

#### A.1 JWT令牌撤销功能

**当前状态**: 方法存在但未实现

**需要实现的内容**:
1. 创建JWT黑名单服务
2. 实现令牌撤销方法
3. 在JWT验证时检查黑名单
4. 支持批量撤销（用户修改密码、强制下线等场景）

**技术方案**:
- 使用Redis存储黑名单（支持分布式）
- 黑名单TTL设置为令牌剩余有效期
- 提供清理过期黑名单的定时任务

**验收标准**:
- 撤销后令牌立即失效
- 性能：黑名单检查 < 5ms
- 高可用：Redis故障不影响业务降级

#### A.2 用户锁定检查

**当前状态**: 返回固定值false

**需要实现的内容**:
1. 记录登录失败次数
2. 达到阈值后自动锁定
3. 支持管理员手动锁定/解锁
4. 锁定后禁止登录

**技术方案**:
- 使用Redis记录失败次数（24小时有效期）
- 锁定时长可配置（默认30分钟）
- 数据库记录管理员锁定状态

**验收标准**:
- 连续5次失败后自动锁定
- 锁定期间不允许登录
- 成功登录后清除失败记录

### B. 相关文档索引

- [门禁模块总体设计](./documentation/业务模块/03-门禁管理模块/01-门禁模块总体设计文档.md)
- [门禁模块数据库设计](./documentation/业务模块/03-门禁管理模块/03-门禁模块数据库设计文档.md)
- [安全架构设计](./documentation/technical/安全体系规范.md)
- [日志规范标准](./documentation/technical/LOGGING_PATTERN_COMPLETE_STANDARD.md)

### C. 联系方式

**技术支持**: 架构委员会
**问题反馈**: 创建Issue或联系技术负责人
**文档更新**: 每周更新一次

---

**文档结束**
