# IOE-DREAM Entity统一迁移方案

> **文档版本**: v1.0.0
> **制定日期**: 2025-12-25
> **目标**: 消除Entity冗余，统一使用DeviceEntity

---

## 📊 当前状态分析

### 已有的统一Entity

**位置**: `microservices-common-entity/DeviceEntity.java`

**优点**:
- ✅ 已设计良好，包含通用字段
- ✅ 有deviceType字段区分设备类型
- ✅ 有extendedAttributes字段（JSON格式）存储扩展属性
- ✅ 严格遵循CLAUDE.md规范

**当前字段**:
- deviceId, deviceCode, deviceName
- deviceType, deviceSubType
- brand, model, serialNumber
- ipAddress, port, username, password
- areaId, deviceStatus, enabled
- lastOnlineTime, lastOfflineTime
- deviceVersion, firmwareVersion
- extendedAttributes（JSON格式）
- remark

### 业务模块专用Entity

#### ConsumeDeviceEntity（消费设备）

**表名**: `t_consume_device`
**特殊字段**:
- deviceLocation（设备位置）
- macAddress（MAC地址）
- deviceModel（设备型号）
- deviceManufacturer（设备制造商）
- supportOffline（是否支持离线模式）
- deviceDescription（设备描述）
- businessAttributes（业务属性，JSON格式）
- lastCommunicationTime（最后通信时间）
- healthStatus（健康状态）

**业务方法**:
- isOnline(), isOffline(), isFault() 等状态判断
- getDeviceTypeName(), getDeviceStatusName() 等名称转换
- getDeviceStatusColor() 前端显示
- validateBusinessRules() 业务规则验证

#### VideoDeviceEntity（视频设备）

**表名**: `t_video_device`（假设存在）
**可能特殊字段**:
- rtspUrl（RTSP流地址）
- resolution（分辨率）
- frameRate（帧率）
- aiEnabled（是否启用AI分析）

#### FirmwareUpgradeDeviceEntity（固件升级设备）

**表名**: `t_firmware_upgrade_device`（假设存在）
**可能特殊字段**:
- upgradeStatus（升级状态）
- upgradeProgress（升级进度）
- currentVersion（当前版本）
- targetVersion（目标版本）

---

## 🎯 迁移策略

### 渐进式迁移方案（推荐）

**原则**: 分阶段迁移，降低风险，确保业务连续性

#### 阶段1: 增强统一DeviceEntity（Day 1）

**任务**: 为统一DeviceEntity添加缺失字段

```java
@TableName("t_common_device")
public class DeviceEntity extends BaseEntity {

    // ========== 现有字段 ==========

    @TableId(value = "device_id", type = IdType.ASSIGN_ID)
    private Long deviceId;

    private String deviceCode;
    private String deviceName;
    private Integer deviceType;
    private Integer deviceSubType;
    private String brand;
    private String model;
    private String serialNumber;
    private String ipAddress;
    private Integer port;
    private String username;
    private String password;
    private Long areaId;
    private Integer deviceStatus;
    private Integer enabled;
    private LocalDateTime lastOnlineTime;
    private LocalDateTime lastOfflineTime;
    private String deviceVersion;
    private String firmwareVersion;
    private String extendedAttributes;  // JSON格式
    private String remark;

    // ========== 新增字段 ==========

    /**
     * MAC地址（消费、考勤设备使用）
     */
    @TableField("mac_address")
    private String macAddress;

    /**
     * 设备位置（消费设备使用）
     */
    @TableField("device_location")
    private String deviceLocation;

    /**
     * 设备制造商（所有设备通用）
     */
    @TableField("manufacturer")
    private String manufacturer;

    /**
     * 最后通信时间（所有设备通用）
     */
    @TableField("last_communication_time")
    private LocalDateTime lastCommunicationTime;

    /**
     * 健康状态（所有设备通用）
     * 正常/异常/需要关注
     */
    @TableField("health_status")
    private String healthStatus;

    /**
     * 是否支持离线模式（消费设备使用）
     * 1-支持 0-不支持
     */
    @TableField("support_offline")
    private Integer supportOffline;

    /**
     * 启用状态（所有设备通用）
     * 1-启用 0-禁用
     */
    @TableField("enabled")
    private Integer enabled;
}
```

**数据库DDL**:
```sql
-- 为t_common_device表添加新字段
ALTER TABLE t_common_device
ADD COLUMN mac_address VARCHAR(50) COMMENT 'MAC地址' AFTER ip_address,
ADD COLUMN device_location VARCHAR(200) COMMENT '设备位置' AFTER area_id,
ADD COLUMN manufacturer VARCHAR(100) COMMENT '设备制造商' AFTER model,
ADD COLUMN last_communication_time DATETIME COMMENT '最后通信时间' AFTER last_offline_time,
ADD COLUMN health_status VARCHAR(50) COMMENT '健康状态' AFTER device_status,
ADD COLUMN support_offline TINYINT DEFAULT 0 COMMENT '是否支持离线模式' AFTER device_version;

-- 添加索引
CREATE INDEX idx_device_type_status ON t_common_device(device_type, device_status);
CREATE INDEX idx_mac_address ON t_common_device(mac_address);
```

#### 阶段2: 数据迁移（Day 2-3）

**任务**: 将业务模块设备数据迁移到统一表

**数据迁移脚本**:
```sql
-- ============================================
-- 消费设备数据迁移
-- ============================================

INSERT INTO t_common_device (
    device_id,
    device_code,
    device_name,
    device_type,
    device_sub_type,
    brand,
    model,
    serial_number,
    ip_address,
    port,
    username,
    password,
    mac_address,
    area_id,
    device_status,
    enabled,
    device_location,
    manufacturer,
    firmware_version,
    support_offline,
    extended_attributes,
    last_communication_time,
    health_status,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag,
    version
)
SELECT
    device_id,
    device_code,
    device_name,
    3 as device_type,              -- 3表示消费设备
    device_type as device_sub_type, -- 原设备类型作为子类型
    device_manufacturer as brand,
    device_model as model,
    NULL as serial_number,        -- 消费设备没有序列号
    ip_address,
    NULL as port,                 -- 消费设备没有端口
    NULL as username,
    NULL as password,
    mac_address,
    area_id,
    device_status,
    1 as enabled,                 -- 默认启用
    device_location,
    device_manufacturer as manufacturer,
    firmware_version,
    support_offline,
    business_attributes as extended_attributes,
    last_communication_time,
    health_status,
    device_description as remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag,
    version
FROM t_consume_device
WHERE deleted_flag = 0;

-- ============================================
-- 视频设备数据迁移（如果有）
-- ============================================

-- INSERT INTO t_common_device ...
-- SELECT ... FROM t_video_device

-- ============================================
-- 验证迁移结果
-- ============================================

-- 验证消费设备迁移数量
SELECT
    '消费设备' as device_type,
    (SELECT COUNT(*) FROM t_consume_device WHERE deleted_flag = 0) as before_count,
    (SELECT COUNT(*) FROM t_common_device WHERE device_type = 3 AND deleted_flag = 0) as after_count;
```

#### 阶段3: 代码迁移（Day 4-5）

**任务**: 逐步迁移业务代码使用统一DeviceEntity

**步骤1: 创建业务特定VO**
```java
/**
 * 消费设备VO（包含业务特定字段和方法）
 */
@Data
public class ConsumeDeviceVO extends DeviceVO {

    /**
     * 消费设备特有的扩展属性
     */
    private Boolean supportOffline;

    /**
     * 设备位置
     */
    private String deviceLocation;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 连接状态描述
     */
    private String connectionStatus;

    // ==================== 业务方法 ====================

    public String getDeviceTypeName() {
        if (getDeviceSubType() == null) {
            return "消费设备";
        }
        switch (getDeviceSubType()) {
            case 1: return "消费机";
            case 2: return "充值机";
            case 3: return "闸机";
            case 4: return "自助终端";
            default: return "消费设备";
        }
    }

    public String getConnectionStatus() {
        if (getLastCommunicationTime() == null) {
            return "从未通信";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(getLastCommunicationTime(), now).toMinutes();

        if (minutes <= 5) {
            return "实时连接";
        } else if (minutes <= 30) {
            return "近期活跃";
        } else if (minutes <= 60) {
            return "1小时内";
        } else if (minutes <= 1440) {
            return "1天内";
        } else {
            return "超过1天";
        }
    }
}
```

**步骤2: 更新DAO和Service**
```java
// ❌ 旧代码：使用ConsumeDeviceEntity
@Mapper
public interface ConsumeDeviceDao extends BaseMapper<ConsumeDeviceEntity> {
    // ...
}

@Service
public class ConsumeDeviceServiceImpl implements ConsumeDeviceService {
    @Resource
    private ConsumeDeviceDao consumeDeviceDao;

    public ConsumeDeviceVO getByDeviceId(Long deviceId) {
        ConsumeDeviceEntity entity = consumeDeviceDao.selectById(deviceId);
        return convertToVO(entity);
    }
}

// ✅ 新代码：使用统一DeviceEntity
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {
    // 统一设备DAO
}

@Service
public class ConsumeDeviceServiceImpl implements ConsumeDeviceService {
    @Resource
    private DeviceDao deviceDao;  // 使用统一DAO

    public ConsumeDeviceVO getByDeviceId(Long deviceId) {
        // 按设备类型过滤：deviceType = 3 (消费设备)
        DeviceEntity entity = deviceDao.selectOne(
            new LambdaQueryWrapper<DeviceEntity>()
                .eq(DeviceEntity::getDeviceId, deviceId)
                .eq(DeviceEntity::getDeviceType, 3)  // 消费设备
        );

        if (entity == null) {
            return null;
        }

        // 转换为业务VO
        return convertToConsumeDeviceVO(entity);
    }

    private ConsumeDeviceVO convertToConsumeDeviceVO(DeviceEntity entity) {
        ConsumeDeviceVO vo = new ConsumeDeviceVO();
        BeanUtils.copyProperties(entity, vo);

        // 从extendedAttributes解析业务特定字段
        if (StringUtils.hasText(entity.getExtendedAttributes())) {
            Map<String, Object> attrs = JsonUtils.parseMap(entity.getExtendedAttributes());
            vo.setSupportOffline((Boolean) attrs.get("supportOffline"));
        }

        // 业务方法
        vo.setConnectionStatus(calculateConnectionStatus(entity.getLastCommunicationTime()));

        return vo;
    }
}
```

**步骤3: 更新Controller**
```java
@RestController
@RequestMapping("/api/consume/device")
public class ConsumeDeviceController {

    @Resource
    private ConsumeDeviceService consumeDeviceService;

    @GetMapping("/{deviceId}")
    public ResponseDTO<ConsumeDeviceVO> getByDeviceId(@PathVariable Long deviceId) {
        // 业务逻辑不变，只是底层实现改为使用统一DeviceEntity
        ConsumeDeviceVO vo = consumeDeviceService.getByDeviceId(deviceId);
        return ResponseDTO.ok(vo);
    }
}
```

#### 阶段4: 清理旧代码（Day 6-7）

**任务**: 删除业务模块专用Entity和表

**执行步骤**:
1. 确认所有代码已迁移到统一DeviceEntity
2. 充分测试（功能回归测试、性能测试）
3. 备份旧表数据
4. 删除旧Entity类
5. 重命名或删除旧表（可选）

**清理脚本**:
```bash
# 删除旧的Entity类
rm microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/domain/entity/ConsumeDeviceEntity.java

# 重命名旧表（保留备份数据）
mysql -u root -p ioe_dream -e "
RENAME TABLE t_consume_device TO t_consume_device_backup_$(date +%Y%m%d);
RENAME TABLE t_video_device TO t_video_device_backup_$(date +%Y%m%d);
"

# 或者删除旧表（确认无误后）
# mysql -u root -p ioe_dream -e "
# DROP TABLE t_consume_device;
# DROP TABLE t_video_device;
# "
```

---

## ✅ 验收标准

### 数据完整性验收

- [ ] 所有设备数据已迁移到t_common_device表
- [ ] 数据条数一致（迁移前后count相同）
- [ ] 数据字段完整（关键字段无丢失）
- [ ] extendedAttributes JSON格式正确

### 功能完整性验收

- [ ] 所有查询功能正常
- [ ] 所有新增/更新/删除功能正常
- [ ] 业务特定方法（如isOnline()）正常
- [ ] 前端显示正常（设备状态颜色、名称等）

### 性能验收

- [ ] 查询性能未下降（响应时间≤原性能的110%）
- [ ] 索引生效（EXPLAIN查询计划良好）
- [ ] 无N+1查询问题

---

## ⚠️ 风险与缓解

### 风险1: 数据迁移失败

**风险等级**: 🔴 高
**缓解措施**:
- 迁移前完整备份
- 在测试环境充分验证
- 分批次迁移（先迁移100条验证）
- 保留旧表作为备份

### 风险2: 功能回归Bug

**风险等级**: 🟡 中
**缓解措施**:
- 完善单元测试（覆盖率≥80%）
- 完整回归测试
- 灰度发布（10% → 50% → 100%）
- 快速回滚方案

### 风险3: 性能下降

**风险等级**: 🟡 中
**缓解措施**:
- 性能基准测试（迁移前后对比）
- 添加必要索引
- 查询优化
- 监控告警

---

## 📅 实施时间表

| 阶段 | 任务 | 工作量 | 负责人 | 完成标准 |
|------|------|--------|--------|----------|
| **Day 1** | 增强统一DeviceEntity | 0.5人天 | 后端工程师 | Entity添加新字段，DDL执行 |
| **Day 2-3** | 数据迁移 | 1人天 | DBA | 数据迁移100%，验证通过 |
| **Day 4-5** | 代码迁移 | 1.5人天 | 后端工程师 | 代码迁移100%，测试通过 |
| **Day 6-7** | 清理旧代码 | 1人天 | 后端工程师 | 旧Entity删除，测试通过 |
| **总计** | - | **4人天** | - | Entity统一完成 |

---

## 📚 附录

### A. extendedAttributes JSON格式示例

**消费设备扩展属性**:
```json
{
  "allowOffline": true,
  "maxAmount": 1000,
  "supportedPaymentMethods": ["card", "qr", "face"],
  "receiptTemplate": "standard",
  "offlineMaxTransactions": 100
}
```

**视频设备扩展属性**:
```json
{
  "rtspUrl": "rtsp://192.168.1.100:554/stream",
  "resolution": "1080p",
  "frameRate": 30,
  "aiEnabled": true,
  "aiModels": ["face_detection", "person_tracking"]
}
```

### B. 设备类型枚举

```java
public enum DeviceType {
    ACCESS(1, "门禁设备", "access"),
    ATTENDANCE(2, "考勤设备", "attendance"),
    CONSUME(3, "消费设备", "consume"),
    CAMERA(4, "视频设备", "video"),
    VISITOR(5, "访客设备", "visitor"),
    BIOMETRIC(6, "生物识别设备", "biometric"),
    INTERCOM(7, "对讲机", "intercom"),
    ALARM(8, "报警器", "alarm"),
    SENSOR(9, "传感器", "sensor");

    private final Integer code;
    private final String name;
    private final String module;
}
```

---

**文档制定**: IOE-DREAM架构委员会
**最后更新**: 2025-12-25
**版本**: v1.0.0
