# IOE-DREAM全局功能完整性补齐 - 技术设计文档

> 基于现有架构深度分析，制定系统性技术方案
> 遵循四层架构规范和微服务最佳实践

## Context

### 现状分析

根据《GLOBAL_FUNCTION_COMPLETENESS_ANALYSIS_REPORT.md》深度分析：

**系统整体完成度**：68%（490/718功能点）

**关键问题**：
1. **功能缺失严重**：228个功能点未实现，其中P0级19个、P1级24个
2. **架构不统一**：部分模块未严格遵循四层架构规范
3. **性能瓶颈**：65%查询缺少索引，缓存命中率仅65%
4. **安全风险**：64个明文密码，权限控制不够细化
5. **测试不足**：单元测试覆盖率60%，E2E测试缺失
6. **文档不完整**：API文档仅70%完整，部分功能缺少文档

**技术债务**：
- 96个@Repository违规（应使用@Mapper）
- 114个@Autowired违规（应使用@Resource）
- 混用JPA和MyBatis-Plus（应统一为MyBatis-Plus）
- 部分模块使用javax而非jakarta包名

### 约束条件

**技术约束**：
- 必须使用Spring Boot 3.5.8 + Spring Cloud 2025.0.0
- 必须使用JDK 17（LTS）
- 必须使用Jakarta EE 3.0+（jakarta.*包名）
- 必须遵循四层架构（Controller → Service → Manager → DAO）
- 必须统一使用MyBatis-Plus + Druid连接池

**业务约束**：
- 必须支持10000+用户企业级场景
- 必须保证99.5%系统可用性
- 必须满足国家三级等保要求
- 必须支持7×24小时不间断运行

**时间约束**：
- 总周期：6个月
- P0级功能：2个月内完成
- P1级功能：4个月内完成
- P2级优化：6个月内完成

**资源约束**：
- 团队规模：5人（第1-2月）→ 4人（第3-4月）→ 2人（第5-6月）
- 总工作量：273人天
- 关键路径：智能排班（12人天）→ 固件升级（5人天）→ 工作流引擎（10人天）

### 利益相关者

**主要利益相关者**：
- 项目架构师：老王（企业级架构分析专家团队）
- 产品负责人：业务需求提出方
- 开发团队：后端5人、前端3人、测试2人
- 运维团队：部署、监控、维护
- 最终用户：园区管理员、企业员工、访客

**次要利益相关者**：
- 第三方系统集成商
- 设备供应商
- 安全审计团队

## Goals / Non-Goals

### Goals

#### P0级核心目标（必须达成）
1. **门禁管理完善**（27人天）
   - 实现设备自动发现、批量导入、固件升级
   - 实现全局反潜回（<100ms响应）
   - 实现实时监控告警（多通道通知）

2. **考勤管理完善**（32人天）
   - 实现智能排班算法（100人30天<30秒）
   - 实现考勤规则配置引擎（Aviator表达式）
   - 实现异常申诉审批流程

3. **消费管理完善**（28人天）
   - 实现离线消费同步（IndexedDB + 冲突解决）
   - 实现补贴规则引擎（多种补贴类型）
   - 实现商品管理和智能推荐

4. **访客管理完善**（22人天）
   - 实现预约审批流程（工作流引擎）
   - 实现电子通行证（二维码 + AES加密）
   - 实现黑名单识别和人脸记录留存

5. **视频监控完善**（23人天）
   - 实现地图展示和视频解码上墙
   - 实现设备质量诊断
   - 实现智能分析增强（AI检测）

#### P1级重要目标（应该达成）
6. **公共模块增强**（45人天）
   - 完善工作流引擎（基于Flowable）
   - 实现通知中心（多通道通知）
   - 实现统一报表中心

#### P2级优化目标（可以达成）
7. **性能优化**（10项）
   - API响应P95<500ms
   - 首屏加载<2s
   - 并发≥1000用户
   - 缓存命中率≥90%

8. **安全加固**（8项）
   - 密码BCrypt加密
   - 敏感数据脱敏
   - 权限细化（字段级）
   - 安全审计完善

9. **测试完善**（7项）
   - 单元测试≥80%
   - 集成测试≥70%
   - E2E测试≥60%
   - SonarQube评分A+

### Non-Goals

#### 不在本次范围内
1. **架构重构**：现有微服务架构保持稳定，不进行大规模重构
2. **技术栈更换**：继续使用Spring Boot 3.5.8，不更换框架
3. **数据库迁移**：继续使用MySQL 8.0，不迁移到其他数据库
4. **前端重写**：继续使用Vue 3.4，不重写前端
5. **移动端重写**：继续使用uni-app，不重写移动端
6. **设备驱动开发**：设备驱动由设备供应商提供，本次不开发
7. **AI模型训练**：使用现有AI模型，不进行模型训练
8. **国际化支持**：专注于中文场景，不实现多语言
9. **多租户支持**：专注于单租户场景，不实现多租户
10. **云端部署**：专注于私有部署，不提供SaaS服务

### Success Criteria

#### 功能完整性指标
- [ ] 系统整体完成度：68% → 98%（+30%）
- [ ] P0级19项功能全部实现
- [ ] P1级24项功能全部实现
- [ ] P2级15项功能全部实现

#### 质量指标
- [ ] SonarQube评分：B → A+
- [ ] 单元测试覆盖率：60% → 80%
- [ ] 集成测试覆盖率：40% → 70%
- [ ] E2E测试覆盖率：0% → 60%
- [ ] 架构合规率：100%

#### 性能指标
- [ ] API响应P95：800ms → <500ms
- [ ] 首屏加载时间：3.5s → <2s（-43%）
- [ ] 页面切换时间：500ms → <200ms
- [ ] 并发用户数：500 → ≥1000
- [ ] 系统可用性：99% → 99.5%
- [ ] 缓存命中率：65% → 90%（+38%）
- [ ] 数据库查询性能：慢查询减少65%

#### 安全指标
- [ ] 明文密码：64个 → 0个
- [ ] 安全漏洞：高风险 → 低风险
- [ ] 权限细化：模块级 → 字段级
- [ ] 审计日志：部分 → 100%完整

## Decisions

### 决策1：智能排班算法技术栈

**决策**：使用Aviator + OptaPlanner + TensorFlow组合

**理由**：
- **Aviator**：轻量级规则引擎，适合考勤规则表达式
- **OptaPlanner**：约束求解器，适合排班优化问题
- **TensorFlow**：机器学习框架，适合业务量预测

**优势**：
- Aviator表达式执行高效（微秒级）
- OptaPlanner支持复杂约束求解
- TensorFlow提供准确预测

**劣势**：
- 学习曲线较陡
- 集成复杂度高
- 资源消耗较大

**替代方案**：
- 纯规则引擎（Drools）：功能强大但过重
- 纯启发式算法：性能不够好
- 商业排班软件：成本高昂

**技术实现**：
```java
@Service
public class SmartScheduleEngine {
    // Aviator规则引擎
    private final AviatorEvaluator ruleEngine;

    // OptaPlanner求解器
    private final SolverManager<ScheduleSolution, UUID> solverManager;

    // TensorFlow预测模型
    private final SavedModelBundle predictionModel;

    /**
     * 智能排班主方法
     * 性能目标：100人30天排班 < 30秒
     */
    public ScheduleResult smartSchedule(ScheduleRequest request) {
        // 1. 业务量预测（TensorFlow）
        BusinessVolumePrediction prediction =
            predictBusinessVolume(request);

        // 2. 硬约束验证（Aviator）
        validateHardConstraints(request, prediction);

        // 3. 排班优化（OptaPlanner）
        ScheduleSolution solution =
            optimizeSchedule(request, prediction);

        // 4. 结果返回
        return buildResult(solution);
    }
}
```

**性能优化**：
- 规则预编译
- 求解器并行化
- 预测结果缓存
- 增量排班

### 决策2：离线消费同步机制

**决策**：使用IndexedDB + 冲突解决策略

**理由**：
- **IndexedDB**：浏览器本地存储，支持离线
- **冲突解决**：Last-Write-Wins + 人工审核

**技术架构**：
```
【离线消费流程】
移动端离线
  ↓
1. 检查网络状态
  ↓ 离线
2. 本地余额计算（IndexedDB）
  ↓
3. 记录离线消费（IndexedDB）
  ↓
4. 本地提示（离线成功）
  ↓
网络恢复
  ↓
5. 自动触发同步
  ↓
6. 批量上传到服务器
  ↓
7. 服务器校验（余额、签名）
  ↓
8. 冲突检测（时间戳对比）
  ↓
9. 冲突解决
    - Last-Write-Wins（自动）
    - 人工审核（复杂冲突）
  ↓
10. 更新服务器余额
  ↓
11. 返回同步结果
  ↓
12. 更新本地数据
```

**数据库设计**：
```sql
-- 本地离线消费表（IndexedDB）
CREATE TABLE offline_consume (
    id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    consume_time DATETIME NOT NULL,
    sync_status TINYINT DEFAULT 0,  -- 0-待同步 1-同步中 2-已同步 3-冲突
    sync_time DATETIME,
    conflict_reason VARCHAR(255),
    created_at DATETIME NOT NULL
);

-- 索引优化
CREATE INDEX idx_user_sync ON offline_consume(user_id, sync_status);
CREATE INDEX idx_device_time ON offline_consume(device_id, consume_time);
```

**同步策略**：
```java
@Service
public class OfflineSyncService {
    /**
     * 批量同步离线消费
     * @param records 离线消费记录
     * @return 同步结果
     */
    public SyncResult batchSync(List<OfflineConsumeRecord> records) {
        // 1. 批量校验（余额、签名、时效）
        ValidationResult validation = validateRecords(records);

        // 2. 冲突检测（时间戳对比）
        List<ConflictRecord> conflicts = detectConflicts(records);

        // 3. 冲突解决
        resolveConflicts(conflicts);

        // 4. 批量插入（1000条<30秒）
        List<ConsumeTransaction> transactions =
            buildTransactions(records);
        consumeTransactionService.saveBatch(transactions);

        // 5. 更新余额
        updateBalance(transactions);

        // 6. 返回结果
        return buildSyncResult(validation, conflicts);
    }
}
```

**性能优化**：
- 批量操作（1000条/批次）
- 异步同步（后台线程）
- 本地缓存（减少网络请求）
- 压缩传输（gzip）

### 决策3：电子通行证技术方案

**决策**：二维码 + AES加密 + 离线验证

**技术架构**：
```
【通行证生成流程】
预约审批通过
  ↓
1. 生成通行证数据
   - 访客ID
   - 预约ID
   - 有效期（≤24小时）
   - 访问区域列表
  ↓
2. AES加密（256位密钥）
  ↓
3. Base64编码
  ↓
4. 二维码生成（ZXing）
  ↓
5. 发送给访客（移动端/邮件）

【通行证验证流程】
门禁设备/移动端扫码
  ↓
1. 二维码解析（ZXing）
  ↓
2. Base64解码
  ↓
3. AES解密（256位密钥）
  ↓
4. 数据验证
   - 签名验证
   - 有效期验证（≤24小时）
   - 黑名单验证
   - 区域权限验证
  ↓
5. 验证通过/拒绝
```

**数据结构**：
```java
/**
 * 电子通行证数据结构
 */
@Data
public class ElectronicPass {
    // 访客信息
    private Long visitorId;          // 访客ID
    private String visitorName;      // 访客姓名
    private String idCardNumber;     // 身份证号（脱敏）

    // 预约信息
    private Long appointmentId;     // 预约ID
    private String appointmentNo;   // 预约编号

    // 有效期
    private LocalDateTime effectiveTime;  // 生效时间
    private LocalDateTime expireTime;      // 失效时间（≤24小时）

    // 访问权限
    private List<Long> areaIds;          // 可访问区域列表
    private List<Integer> passTimes;     // 可通行时间段

    // 安全校验
    private String signature;            // 数字签名
    private Long version;                // 版本号

    // 元数据
    private LocalDateTime generatedTime; // 生成时间
    private String generator;            // 生成者
}

/**
 * 通行证服务
 */
@Service
public class ElectronicPassService {
    // AES密钥（256位）
    private static final String AES_KEY = "xxx-xxx-xxx";

    // 二维码尺寸
    private static final int QR_CODE_SIZE = 300;

    /**
     * 生成电子通行证
     */
    public String generatePass(ElectronicPass pass) {
        // 1. 设置有效期（≤24小时）
        pass.setEffectiveTime(LocalDateTime.now());
        pass.setExpireTime(LocalDateTime.now().plusHours(24));

        // 2. 生成签名
        String signature = generateSignature(pass);
        pass.setSignature(signature);

        // 3. 序列化
        String json = JsonUtils.toJson(pass);

        // 4. AES加密
        String encrypted = AESUtil.encrypt(json, AES_KEY);

        // 5. Base64编码
        String base64 = Base64.getEncoder().encodeToString(
            encrypted.getBytes(StandardCharsets.UTF_8)
        );

        // 6. 生成二维码
        return QRCodeUtil.generate(base64, QR_CODE_SIZE);
    }

    /**
     * 验证电子通行证
     */
    public ValidationResult validatePass(String qrCode) {
        try {
            // 1. 二维码解析
            String base64 = QRCodeUtil.parse(qrCode);

            // 2. Base64解码
            byte[] encryptedBytes = Base64.getDecoder().decode(base64);

            // 3. AES解密
            String json = AESUtil.decrypt(
                new String(encryptedBytes, StandardCharsets.UTF_8),
                AES_KEY
            );

            // 4. 反序列化
            ElectronicPass pass = JsonUtils.fromJson(
                json,
                ElectronicPass.class
            );

            // 5. 签名验证
            if (!verifySignature(pass)) {
                return ValidationResult.fail("签名验证失败");
            }

            // 6. 有效期验证
            if (pass.getExpireTime().isBefore(LocalDateTime.now())) {
                return ValidationResult.fail("通行证已过期");
            }

            // 7. 黑名单验证
            if (blacklistService.isBlacklisted(pass.getVisitorId())) {
                return ValidationResult.fail("访客在黑名单中");
            }

            // 8. 验证通过
            return ValidationResult.success(pass);

        } catch (Exception e) {
            log.error("[通行证验证] 验证失败: {}", e.getMessage(), e);
            return ValidationResult.fail("通行证无效");
        }
    }
}
```

**安全措施**：
- AES-256加密
- 数字签名防篡改
- 短期有效期（≤24小时）
- 黑名单实时验证
- 离线验证支持（签名预存）

### 决策4：视频地图展示方案

**决策**：Leaflet + 百度/高德地图API

**理由**：
- **Leaflet**：开源免费，轻量级
- **百度/高德**：国内地图数据准确

**技术架构**：
```
【视频地图展示】
地图初始化
  ↓
1. 加载地图底图（Leaflet）
  ↓
2. 添加摄像头标注
   - 实时状态图标（在线/离线/告警）
   - 点击事件监听
  ↓
3. 告警联动
   - 告警触发
   - 地图闪烁提示
   - 点击查看视频
  ↓
4. 视频播放
   - 弹窗播放器
   - 实时流播放
   - 云台控制
```

**前端实现**：
```vue
<template>
  <div class="video-map-container">
    <!-- 地图容器 -->
    <div id="map" class="map"></div>

    <!-- 视频播放弹窗 -->
    <a-modal
      v-model:visible="videoVisible"
      title="实时视频"
      :footer="null"
      width="800px"
    >
      <video-player
        :device-id="selectedDevice.id"
        :stream-url="selectedDevice.streamUrl"
      />
    </a-modal>
  </div>
</template>

<script setup>
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const map = ref(null);
const devices = ref([]);
const selectedDevice = ref(null);
const videoVisible = ref(false);

onMounted(async () => {
  // 1. 初始化地图
  initMap();

  // 2. 加载设备列表
  await loadDevices();

  // 3. 添加设备标注
  addDeviceMarkers();

  // 4. 启动实时更新
  startRealtimeUpdate();
});

function initMap() {
  // 创建地图实例
  map.value = L.map('map').setView(
    [39.9042, 116.4074], // 北京坐标
    13 // 缩放级别
  );

  // 添加底图
  L.tileLayer(
    'https://api.map.baidu.com/api?v=2.0&ak=YOUR_API_KEY',
    {
      attribution: '百度地图',
      maxZoom: 18
    }
  ).addTo(map.value);
}

async function loadDevices() {
  const res = await deviceService.listVideoDevices({
    pageNum: 1,
    pageSize: 1000
  });
  devices.value = res.data.list;
}

function addDeviceMarkers() {
  devices.value.forEach(device => {
    // 状态图标
    const icon = getDeviceIcon(device.status);

    // 标注
    const marker = L.marker(
      [device.latitude, device.longitude],
      { icon }
    ).addTo(map.value);

    // 点击事件
    marker.on('click', () => {
      showVideo(device);
    });

    // 弹窗
    marker.bindPopup(`
      <div>
        <strong>${device.deviceName}</strong><br/>
        状态：${getDeviceStatusText(device.status)}<br/>
        点击查看视频
      </div>
    `);
  });
}

function showVideo(device) {
  selectedDevice.value = device;
  videoVisible.value = true;
}

// 告警联动
watch(alerts, (newAlerts) => {
  newAlerts.forEach(alert => {
    const device = devices.value.find(
      d => d.deviceId === alert.deviceId
    );
    if (device) {
      // 地图闪烁提示
      flashMarker(device);
    }
  });
});
</script>
```

**性能优化**：
- 标注聚合（设备密集区域）
- 懒加载（按需加载设备）
- 缓存优化（缓存设备位置）
- 增量更新（只更新变化设备）

### 决策5：全局反潜回架构

**决策**：4种反潜回模式 + 实时检测（<100ms）

**技术架构**：
```
【反潜回检测流程】
用户刷卡
  ↓
1. 采集通行记录
  - 用户ID
  - 设备ID
  - 区域ID
  - 刷卡时间
  ↓
2. 确定反潜回模式
  - 全局反潜回（整个园区）
  - 区域反潜回（单区域）
  - 软反潜回（告警）
  - 硬反潜回（阻止）
  ↓
3. 检测逻辑
  - 查询最近通行记录（Redis缓存）
  - 判断是否违规（时间间隔<5分钟）
  - 违规则触发告警/阻止
  ↓
4. 记录检测结果
  - 保存检测记录
  - 更新统计数据
  ↓
5. 响应
  - 允许通行
  - 告警提示
  - 阻止通行
```

**数据结构**：
```java
/**
 * 反潜回配置
 */
@Data
@TableName("t_anti_passback_config")
public class AntiPassbackConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    // 反潜回模式
    // 1-全局反潜回 2-区域反潜回 3-软反潜回 4-硬反潜回
    private Integer mode;

    // 关联区域（区域模式）
    private Long areaId;

    // 时间窗口（毫秒）
    // 两次刷卡间隔小于此值则触发反潜回
    private Long timeWindow = 300000L; // 默认5分钟

    // 有效期
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;

    // 启用状态
    private Integer enabled = 1;
}

/**
 * 反潜回检测记录
 */
@Data
@TableName("t_anti_passback_record")
public class AntiPassbackRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    // 用户信息
    private Long userId;
    private String userName;

    // 设备信息
    private Long deviceId;
    private String deviceName;

    // 区域信息
    private Long areaId;
    private String areaName;

    // 检测结果
    // 1-正常 2-软反潜回（告警） 3-硬反潜回（阻止）
    private Integer result;

    // 时间信息
    private LocalDateTime passTime;
    private LocalDateTime detectedTime;

    // 处理信息
    private Integer handled = 0; // 0-未处理 1-已处理
    private String handleRemark;
}
```

**检测算法**：
```java
@Service
public class AntiPassbackService {
    @Resource
    private AntiPassbackDao antiPassbackDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 反潜回检测
     * 性能目标：<100ms
     */
    public AntiPassbackResult detect(AccessRecord record) {
        long startTime = System.currentTimeMillis();

        // 1. 查询反潜回配置（Redis缓存）
        AntiPassbackConfig config = getConfig(record.getAreaId());

        // 2. 判断是否启用反潜回
        if (config == null || config.getEnabled() != 1) {
            return AntiPassbackResult.allow();
        }

        // 3. 查询最近通行记录（Redis缓存）
        String cacheKey = buildCacheKey(
            record.getUserId(),
            config.getMode() == 1 ? null : record.getAreaId()
        );

        AccessRecord lastRecord = (AccessRecord) redisTemplate
            .opsForValue()
            .get(cacheKey);

        // 4. 判断是否违规
        if (lastRecord != null) {
            long interval = Duration.between(
                lastRecord.getPassTime(),
                record.getPassTime()
            ).toMillis();

            if (interval < config.getTimeWindow()) {
                // 违规触发
                return handleViolation(
                    config,
                    record,
                    lastRecord
                );
            }
        }

        // 5. 未违规，更新缓存
        redisTemplate.opsForValue().set(
            cacheKey,
            record,
            Duration.ofHours(24)
        );

        // 6. 记录检测
        saveRecord(record, 1);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("[反潜回检测] 检测完成，耗时: {}ms", duration);

        return AntiPassbackResult.allow();
    }

    /**
     * 处理违规
     */
    private AntiPassbackResult handleViolation(
        AntiPassbackConfig config,
        AccessRecord currentRecord,
        AccessRecord lastRecord
    ) {
        // 软反潜回：告警
        if (config.getMode() == 3) {
            // 发送告警
            alertService.sendAlert(AlertType.ANTI_PASSBACK, currentRecord);

            // 记录检测
            saveRecord(currentRecord, 2);

            return AntiPassbackResult.softBlock(
                "检测到反潜回，上次通行时间：" +
                lastRecord.getPassTime()
            );
        }

        // 硬反潜回：阻止
        if (config.getMode() == 4) {
            // 发送告警
            alertService.sendAlert(AlertType.ANTI_PASSBACK, currentRecord);

            // 记录检测
            saveRecord(currentRecord, 3);

            return AntiPassbackResult.hardBlock(
                "检测到反潜回，通行已阻止"
            );
        }

        // 默认：允许
        return AntiPassbackResult.allow();
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(Long userId, Long areaId) {
        if (areaId == null) {
            // 全局反潜回
            return String.format(
                "anti_passback:global:user:%d",
                userId
            );
        } else {
            // 区域反潜回
            return String.format(
                "anti_passback:area:%d:user:%d",
                areaId,
                userId
            );
        }
    }
}
```

**性能优化**：
- Redis缓存（最近通行记录）
- 批量查询（减少数据库访问）
- 异步处理（告警异步发送）
- 索引优化（用户ID+区域ID+时间）

## Risks / Trade-offs

### 风险1：智能排班算法性能风险

**风险描述**：
- 100人30天排班可能超过30秒
- 1000人规模可能无法完成

**影响程度**：高（P0级核心功能）

**缓解措施**：
1. **预研POC**（第1周）
   - 验证OptaPlanner性能
   - 测试不同规模排班时间
   - 确定性能瓶颈

2. **性能优化**（第2-3周）
   - 规则预编译
   - 求解器并行化
   - 增量排班
   - 结果缓存

3. **降级方案**（第4周）
   - 小规模排班（≤50人）
   - 简化约束条件
   - 启发式算法

4. **监控告警**
   - 排班时间监控
   - 超时告警
   - 自动降级

**应急预案**：
- 如果性能无法满足，降级为半智能排班（规则+人工调整）
- 预留2周缓冲时间

### 风险2：离线消费数据一致性风险

**风险描述**：
- 离线消费与在线数据可能不一致
- 冲突解决可能复杂
- 余额可能扣减错误

**影响程度**：高（涉及资金）

**缓解措施**：
1. **数据校验**
   - 本地余额计算
   - 服务器余额校验
   - 签名验证
   - 重复交易检测

2. **冲突解决**
   - Last-Write-Wins（简单冲突）
   - 人工审核（复杂冲突）
   - 冲突日志记录

3. **事务保障**
   - 本地事务（ACID）
   - 分布式事务（SAGA）
   - 补偿机制

4. **监控告警**
   - 同步失败监控
   - 余额异常监控
   - 冲突数量监控

**应急预案**：
- 每日对账
- 异常人工处理
- 预留资金缓冲池

### 风险3：新技术栈学习风险

**风险描述**：
- 团队对OptaPlanner、TensorFlow不熟悉
- 学习曲线可能影响进度
- 技术难点可能延期

**影响程度**：中

**缓解措施**：
1. **技术预研**（第1周）
   - OptaPlanner官方文档
   - TensorFlow官方文档
   - POC验证

2. **专家咨询**
   - 聘请外部专家
   - 技术社区支持
   - 官方技术支持

3. **团队培训**
   - 内部技术分享
   - 代码Review
   - 结对编程

4. **时间预留**
   - 关键任务预留20%缓冲
   - 每周进度回顾
   - 及时调整计划

**应急预案**：
- 简化技术方案
- 使用成熟算法替代
- 延期非核心功能

### 风险4：进度延期风险

**风险描述**：
- 273人天工作量可能低估
- 6个月周期可能不够
- 关键路径可能阻塞

**影响程度**：高

**缓解措施**：
1. **分阶段交付**
   - P0级功能优先（2个月）
   - P1级功能次之（4个月）
   - P2级功能最后（6个月）

2. **关键路径管理**
   - 智能排班优先启动
   - 固件升级优先启动
   - 工作流引擎优先启动

3. **每周回顾**
   - 进度跟踪
   - 风险识别
   - 计划调整

4. **资源调配**
   - 关键任务增加人力
   - 非关键任务可延期
   - 外包辅助开发

**应急预案**：
- 降低P2级功能优先级
- 延期非核心功能
- 增加团队规模

### 风险5：质量风险

**风险描述**：
- 测试覆盖不充分
- 线上Bug频发
- 用户体验差

**影响程度**：高

**缓解措施**：
1. **质量门禁**
   - 代码Review强制
   - 单元测试强制（≥80%）
   - 集成测试强制（≥70%）

2. **自动化测试**
   - 单元测试自动化
   - 集成测试自动化
   - E2E测试自动化

3. **灰度发布**
   - 小范围试点
   - 逐步扩大
   - 问题快速回滚

4. **监控告警**
   - 线上监控
   - 错误日志
   - 用户反馈

**应急预案**：
- 紧急Bug修复通道
- 快速回滚机制
- 补丁发布流程

## Migration Plan

### 数据库迁移

#### Schema升级

**步骤**：
1. **准备阶段**（1周）
   - 备份生产数据库
   - 准备回滚脚本
   - 测试环境验证

2. **Schema变更**（1周）
   - 执行DDL脚本
   - 添加新字段
   - 创建新表
   - 添加索引

3. **数据迁移**（1周）
   - 迁移历史数据
   - 数据完整性验证
   - 性能验证

4. **发布上线**（1天）
   - 选择低峰期
   - 执行迁移
   - 验证结果
   - 监控告警

**回滚方案**：
- 恢复数据库备份
- 回滚应用版本
- 验证回滚成功

**示例脚本**：
```sql
-- 1. 门禁设备表新增字段
ALTER TABLE t_common_device
ADD COLUMN auto_discovery_config JSON COMMENT '自动发现配置',
ADD COLUMN firmware_info JSON COMMENT '固件信息',
ADD COLUMN alert_config JSON COMMENT '告警配置';

-- 2. 考勤班次表新增字段
ALTER TABLE t_attendance_shift
ADD COLUMN cross_day_flag TINYINT DEFAULT 0 COMMENT '是否跨天',
ADD COLUMN flexible_rules JSON COMMENT '弹性规则',
ADD COLUMN smart_config JSON COMMENT '智能配置';

-- 3. 添加索引
CREATE INDEX idx_device_type_status
ON t_common_device(device_type, status);

CREATE INDEX idx_shift_cross_day
ON t_attendance_shift(cross_day_flag, deleted_flag);
```

### 接口迁移

#### 新旧接口共存

**过渡期**：6个月

**步骤**：
1. **新接口开发**
   - 遵循RESTful规范
   - 提供Swagger文档
   - 版本控制（/api/v2/）

2. **旧接口标记**
   - 添加@Deprecated注解
   - 文档标注废弃时间
   - 建议迁移到新接口

3. **客户端迁移**
   - 移动端迁移
   - 前端迁移
   - 第三方集成迁移

4. **旧接口下线**
   - 6个月后下线
   - 提前通知
   - 监控使用情况

**示例**：
```java
// 旧接口（标记废弃）
@Deprecated(since = "2025-06-01", forRemoval = true)
@GetMapping("/api/v1/device/list")
public ResponseDTO<List<DeviceVO>> listDevicesV1() {
    // 旧实现
}

// 新接口
@GetMapping("/api/v2/devices")
public ResponseDTO<PageResult<DeviceVO>> listDevicesV2(
    @RequestParam(defaultValue = "1") Integer pageNum,
    @RequestParam(defaultValue = "20") Integer pageSize
) {
    // 新实现（分页、性能优化）
}
```

### 配置迁移

#### Nacos配置更新

**步骤**：
1. **配置备份**
   - 导出现有配置
   - 版本控制

2. **配置更新**
   - 新增配置项
   - 修改配置值
   - 配置验证

3. **灰度发布**
   - 部分实例更新
   - 验证配置正确性
   - 全量更新

4. **配置回滚**
   - 保留旧配置
   - 快速回滚机制

**示例配置**：
```yaml
# 智能排班配置
smart:
  scheduling:
    enabled: true
    engine: optaplanner
    timeout: 30s
    max-employees: 1000
    max-days: 90

# 离线消费配置
offline:
  consume:
    enabled: true
    batch-size: 1000
    sync-interval: 60s
    conflict-strategy: LAST_WRITE_WINS

# 电子通行证配置
electronic:
  pass:
    enabled: true
    max-valid-hours: 24
    aes-key: ${ELECTRONIC_PASS_AES_KEY}
    qr-code-size: 300

# 反潜回配置
anti:
  passback:
    enabled: true
    mode: GLOBAL
    time-window: 300000
    cache-ttl: 86400
```

## Open Questions

### Q1: 智能排班算法是否能满足性能要求？

**问题**：
- 100人30天排班<30秒是否有把握？
- 1000人规模如何处理？

**分析**：
- OptaPlanner理论性能满足要求
- 实际性能需要POC验证
- 大规模可能需要降级方案

**建议**：
- 第1周完成POC验证
- 根据POC结果调整方案
- 预留降级方案

### Q2: 离线消费数据一致性如何保障？

**问题**：
- 网络恢复后同步顺序如何确定？
- 冲突如何解决？
- 余额不一致如何处理？

**分析**：
- 时间戳排序确定顺序
- Last-Write-Wins + 人工审核
- 每日对账 + 人工处理

**建议**：
- 完善冲突解决机制
- 建立对账流程
- 预留缓冲资金

### Q3: 电子通行证安全性是否足够？

**问题**：
- 二维码容易被复制？
- AES密钥如何管理？
- 离线验证是否安全？

**分析**：
- 短期有效期（≤24小时）降低复制风险
- 密钥定期轮换
- 签名验证防篡改

**建议**：
- 密钥安全管理
- 定期安全审计
- 考虑生物识别增强

### Q4: 工作流引擎是否需要如此复杂？

**问题**：
- Flowable是否过重？
- 能否用轻量级方案？

**分析**：
- Flowable功能强大但复杂
- 轻量级方案可能不够灵活
- 需要权衡复杂度和灵活性

**建议**：
- 评估业务流程复杂度
- 考虑简化方案（状态机）
- 必要时使用Flowable

### Q5: 6个月周期是否足够？

**问题**：
- 273人天工作量是否准确？
- 是否有隐藏工作量？
- 风险是否充分考虑？

**分析**：
- 工作量基于历史经验估算
- 可能存在不可预见问题
- 风险预留20%缓冲

**建议**：
- 分阶段交付
- 每周回顾调整
- 必要时调整优先级

## 附录

### 技术栈清单

#### 后端技术栈
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Spring Cloud Alibaba 2025.0.0.0
- JDK 17 (LTS)
- MyBatis-Plus 3.5.15
- Druid 1.2.25
- MySQL 8.0.35
- Redis 6.0+
- RabbitMQ
- Nacos
- Seata
- Resilience4j
- Micrometer
- Aviator (规则引擎)
- OptaPlanner (约束求解)
- TensorFlow (机器学习)
- Flowable (工作流)
- ZXing (二维码)
- Jaeger (分布式追踪)

#### 前端技术栈
- Vue 3.4.x
- Vite 5.x
- Pinia 2.x
- Ant Design Vue 4.x
- Axios 1.6.x
- ECharts 5.4.x
- Leaflet (地图)
- Video.js (视频播放)

#### 移动端技术栈
- uni-app 3.0.x
- Vue 3.2.x
- Pinia 2.0.x
- uni-ui 1.5.x

#### 测试技术栈
- JUnit 5
- Mockito
- Spring Boot Test
- Vitest
- Playwright
- JMeter

### 参考资料

1. **官方文档**
   - Spring Boot: https://spring.io/projects/spring-boot
   - OptaPlanner: https://www.optaplanner.org/
   - TensorFlow: https://www.tensorflow.org/
   - Flowable: https://www.flowable.com/

2. **内部文档**
   - CLAUDE.md（项目架构规范）
   - GLOBAL_SUPPLEMENTARY_DEVELOPMENT_PLAN.md（详细开发计划）
   - GLOBAL_FUNCTION_COMPLETENESS_ANALYSIS_REPORT.md（功能完整性分析）

3. **最佳实践**
   - Alibaba Java Coding Guidelines
   - Google Java Style Guide
   - Spring Boot Best Practices
   - Vue 3 Style Guide

### 术语表

| 术语 | 解释 |
|------|------|
| P0级 | 最高优先级，核心功能，必须完成 |
| P1级 | 高优先级，重要功能，应该完成 |
| P2级 | 中优先级，优化功能，可以完成 |
| 四层架构 | Controller → Service → Manager → DAO |
| 反潜回 | 防止卡片在短时间内多次使用 |
| 离线消费 | 无网络时本地记录消费，网络恢复后同步 |
| 电子通行证 | 二维码通行证，AES加密，短期有效 |
| 智能排班 | 基于算法自动生成排班表 |
| 工作流引擎 | Flowable，支持可视化流程设计 |
| 补贴规则引擎 | 基于Aviator表达式，支持复杂补贴规则 |
| 分布式追踪 | Jaeger，全链路追踪，性能分析 |

### 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| v1.0 | 2025-01-15 | Claude | 初始版本 |
