# IOE-DREAM 中小企业智慧园区文件存储架构 - 最终完整方案

> **项目定位**: 中小企业智慧园区安防综合管理平台 (5000-10000人)  
> **核心目标**: 低内存占用 + 高可扩展性 + 企业级高质量  
> **技术栈**: Spring Boot 3.5.8 + MySQL 8.0 + Redis + 本地存储/MinIO可选

---

## 📊 一、业务规模重新评估 (5000-10000人场景)

### 1.1 人员规模分析

| 人员类型 | 数量 | 占比 | 说明 |
|---------|------|------|------|
| 正式员工 | 3000-6000人 | 60% | 核心员工,每日通行 |
| 临时员工 | 1000-2000人 | 15% | 外包/实习生 |
| 访客 | 500-1000人/月 | 10% | 平均每日30-50人 |
| 保洁/安保 | 500-1000人 | 15% | 驻场服务人员 |

### 1.2 设备规模估算

| 设备类型 | 数量 | 并发使用 | 说明 |
|---------|------|---------|------|
| 门禁闸机 | 50-100台 | 高峰80% | 上下班高峰 |
| 考勤机 | 30-60台 | 高峰90% | 早晚打卡 |
| 监控摄像头 | 200-400个 | 24小时录像 | 关键区域 |
| 消费终端 | 20-40台 | 午餐高峰70% | 食堂/超市 |
| 访客机 | 5-10台 | 平均10% | 前台/大堂 |

### 1.3 日常业务量评估

**高峰期**: 上午8:00-9:00,下午17:30-18:30

| 业务场景 | 峰值频率 | 平均频率 | 日总量 |
|---------|---------|---------|--------|
| 门禁刷卡 | 2000次/小时 | 500次/小时 | 15000次/天 |
| 考勤打卡 | 3000次/小时 | 100次/小时 | 12000次/天 |
| 消费支付 | 500次/小时 | 50次/小时 | 3000次/天 |
| 访客登记 | 10次/小时 | 5次/小时 | 50次/天 |
| 视频告警 | 5次/小时 | 2次/小时 | 30次/天 |

---

## 💾 二、文件存储需求精确计算

### 2.1 门禁服务存储需求

**场景1: 门禁通行抓拍**
- 触发条件: 刷卡成功
- 文件大小: 1-2MB/张 (1920×1080 JPEG)
- 业务频率: 15000次/天
- **日增量**: 15000 × 1.5MB = **22.5GB/天**
- 保留周期: 30天
- **总存储**: 22.5GB × 30 = **675GB**

**场景2: 门禁异常抓拍**
- 触发条件: 刷卡失败/尾随/强闯
- 文件大小: 2MB/张
- 业务频率: 100次/天
- **日增量**: 100 × 2MB = **200MB/天**
- 保留周期: 90天
- **总存储**: 200MB × 90 = **18GB**

**门禁小计**: **日增22.7GB, 总需693GB**

---

### 2.2 考勤服务存储需求

**场景: 考勤打卡照片**
- 文件大小: 1MB/张
- 业务频率: 12000次/天
- **日增量**: 12000 × 1MB = **12GB/天**
- 保留周期: 180天(半年)
- **总存储**: 12GB × 180 = **2.16TB**

**考勤小计**: **日增12GB, 总需2.16TB**

---

### 2.3 视频服务存储需求

**场景1: 关键帧抓拍 (Edge AI分析结果)**
- 触发条件: AI检测到人/车/事件
- 文件大小: 2MB/帧
- 业务频率: 30帧/天/摄像头 × 300摄像头 = 9000帧/天
- **日增量**: 9000 × 2MB = **18GB/天**
- 保留周期: 30天
- **总存储**: 18GB × 30 = **540GB**

**场景2: 告警视频片段**
- 触发条件: 严重告警事件
- 文件大小: 50MB/片段 (30秒H.264)
- 业务频率: 30次/天
- **日增量**: 30 × 50MB = **1.5GB/天**
- 保留周期: 90天
- **总存储**: 1.5GB × 90 = **135GB**

**场景3: 手动截图**
- 用户操作: 保安手动截图
- 文件大小: 2MB/张
- 业务频率: 50次/天
- **日增量**: 50 × 2MB = **100MB/天**
- 保留周期: 30天
- **总存储**: 100MB × 30 = **3GB**

**视频小计**: **日增19.6GB, 总需678GB**

---

### 2.4 消费服务存储需求

**场景: 消费小票**
- 文件大小: 500KB/张
- 业务频率: 3000次/天
- **日增量**: 3000 × 0.5MB = **1.5GB/天**
- 保留周期: 30天
- **总存储**: 1.5GB × 30 = **45GB**

**消费小计**: **日增1.5GB, 总需45GB**

---

### 2.5 访客服务存储需求

**场景: 访客登记照片**
- 文件大小: 1.5MB/张
- 业务频率: 50人/天
- **日增量**: 50 × 1.5MB = **75MB/天**
- 保留周期: 365天(1年)
- **总存储**: 75MB × 365 = **27GB**

**访客小计**: **日增75MB, 总需27GB**

---

### 2.6 OA工作流存储需求

**场景: 审批附件**
- 文件类型: Word/PDF/Excel/图片
- 文件大小: 平均5MB/个
- 业务频率: 200次/天 (中小企业审批量)
- **日增量**: 200 × 5MB = **1GB/天**
- 保留周期: 5年(1825天)
- **总存储**: 1GB × 1825 = **1.83TB**

**OA小计**: **日增1GB, 总需1.83TB**

---

### 2.7 人员管理存储需求

**场景1: 人员头像**
- 文件大小: 500KB/张
- 人员数量: 10000人
- 更新频率: 平均每月100人更新
- **日增量**: 100/30 × 0.5MB = **1.7MB/天**
- **总存储**: 10000 × 0.5MB = **5GB**

**场景2: 身份证照片**
- 文件大小: 1MB/张 × 2(正反面)
- 新增频率: 每月50人
- **日增量**: 50/30 × 2MB = **3.3MB/天**
- **总存储**: 10000 × 2MB = **20GB**

**人员小计**: **日增5MB, 总需25GB**

---

## 📊 三、总存储需求汇总 (中小企业场景)

| 业务模块 | 日增量 | 保留期 | 总存储 | 峰值带宽 |
|---------|--------|--------|--------|---------|
| 门禁通行 | 22.7GB | 30天 | 693GB | 50MB/s |
| 考勤打卡 | 12GB | 180天 | 2.16TB | 30MB/s |
| 视频监控 | 19.6GB | 30-90天 | 678GB | 20MB/s |
| 消费支付 | 1.5GB | 30天 | 45GB | 5MB/s |
| 访客登记 | 75MB | 365天 | 27GB | 1MB/s |
| OA审批 | 1GB | 5年 | 1.83TB | 2MB/s |
| 人员管理 | 5MB | 永久 | 25GB | <1MB/s |

**总计**:
- **日均增量**: ~57GB/天 (实际约60GB/天含冗余)
- **活跃存储**(90天): ~5.5TB
- **归档存储**(5年): ~1.83TB (OA附件)
- **建议配置**: **8TB可用存储** (含50%冗余)

---

## 🎯 四、优化后的存储架构方案

### 4.1 默认方案: 本地文件系统 (推荐中小企业)

#### **为什么默认本地存储?**

1. **成本最低**: 无需额外MinIO服务器,节省1GB内存
2. **运维简单**: 中小企业IT人员少,本地存储易于维护
3. **性能足够**: 8TB磁盘 + RAID10,读写速度>500MB/s
4. **可靠性高**: 定期备份到NAS或云端,数据不丢失

#### **本地存储配置**

```yaml
# application-prod.yml (默认配置)
file:
  storage:
    type: local  # ← 默认本地存储
    local:
      base-path: /data/ioedream/files  # ← 挂载8TB磁盘
      url-prefix: ${GATEWAY_URL}/files  # ← 网关代理文件访问
      
      # 分区策略
      partitions:
        access: /data/ioedream/files/access      # 门禁 (1TB)
        attendance: /data/ioedream/files/attendance  # 考勤 (3TB)
        video: /data/ioedream/files/video        # 视频 (1TB)
        oa: /data/ioedream/files/oa              # OA (2TB)
        common: /data/ioedream/files/common      # 公共 (1TB)
      
      # 自动清理策略
      cleanup:
        enabled: true
        rules:
          - path: "access/snapshots"
            retention-days: 30
          - path: "attendance/photos"
            retention-days: 180
          - path: "video/frames"
            retention-days: 30
          - path: "video/alerts"
            retention-days: 90
```

#### **目录结构**

```
/data/ioedream/files/
├── access/              # 门禁 (693GB)
│   └── snapshots/
│       └── 2025/
│           └── 01/
│               └── 20250118_device001_123456.jpg
├── attendance/          # 考勤 (2.16TB)
│   └── photos/
│       └── 2025/01/
├── video/               # 视频 (678GB)
│   ├── frames/          # AI关键帧
│   └── alerts/          # 告警片段
├── oa/                  # OA (1.83TB)
│   └── attachments/
│       └── workflow/
│           └── approval_001/
├── consume/             # 消费 (45GB)
│   └── receipts/
├── visitor/             # 访客 (27GB)
│   └── photos/
└── common/              # 公共 (25GB)
    ├── avatars/         # 人员头像
    └── id-cards/        # 身份证
```

#### **内存占用**

```
应用服务器内存占用:
- gateway-service:      400MB
- common-service:       300MB
- access-service:       250MB
- attendance-service:   250MB
- video-service:        300MB (含Edge处理)
- consume-service:      200MB
- visitor-service:      200MB
- oa-service:           250MB
- device-comm-service:  200MB

总计: ~2.35GB (应用层)
```

**无需MinIO**, 节省1GB内存!

---

### 4.2 扩展方案: MinIO对象存储 (未来大型企业)

#### **什么时候升级MinIO?**

触发条件 (满足任一即可):
1. 人员规模 > 20000人
2. 日增文件量 > 200GB
3. 需要多园区部署
4. 需要异地容灾

#### **MinIO配置 (可选)**

```yaml
# application-prod.yml (可选切换)
file:
  storage:
    type: minio  # ← 切换到MinIO
    minio:
      endpoint: http://minio-cluster:9000
      access-key: ${MINIO_ACCESS_KEY}
      secret-key: ${MINIO_SECRET_KEY}
      bucket-name: ioedream-prod
      
      # 对象生命周期
      lifecycle-rules:
        - bucket: ioedream-prod
          prefix: access/snapshots/
          expiration-days: 30
        - bucket: ioedream-prod
          prefix: oa/attachments/
          transition-days: 365
          storage-class: GLACIER  # 1年后转冷存储
```

**内存占用增加**:
- MinIO单机: +1GB
- MinIO集群(3节点): +3GB

---

## 🚀 五、最终推荐方案 (中小企业5000-10000人)

### 5.1 硬件配置建议

| 组件 | 配置 | 说明 |
|-----|------|------|
| **服务器** | 8核16GB内存 | 运行所有微服务 |
| **存储** | 8TB SATA硬盘(RAID10) | 实际可用4TB |
| **备份** | 8TB NAS或云存储 | 每日增量备份 |
| **网络** | 千兆以太网 | 峰值带宽100MB/s |

**总成本**: 约3-5万元 (一次性硬件投入)

---

### 5.2 软件架构配置

```yaml
# ============ 默认配置(本地存储) ============
file.storage.type: local
file.storage.local.base-path: /data/ioedream/files

# ============ JVM内存优化 ============
spring:
  application:
    name: ${SERVICE_NAME}
    
# 每个服务JVM参数
JAVA_OPTS: >
  -Xms256m
  -Xmx512m
  -XX:MetaspaceSize=128m
  -XX:MaxMetaspaceSize=256m
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=100

# ============ 数据库连接池 ============
spring:
  datasource:
    druid:
      initial-size: 5        # ← 中小企业降低初始连接
      min-idle: 5
      max-active: 50         # ← 峰值50并发足够
      max-wait: 60000
      
# ============ Redis缓存 ============
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20     # ← 降低Redis连接池
          max-idle: 10
          min-idle: 5
```

---

### 5.3 分阶段实施计划

#### **第一阶段 (立即实施)** - P0优先级

1. ✅ 使用本地文件存储
2. ✅ 配置自动清理策略
3. ✅ 配置每日备份脚本
4. ✅ 监控磁盘使用率

**工作量**: 2人天  
**内存占用**: 2.35GB (仅应用层)

---

#### **第二阶段 (3个月后)** - P1优先级

1. 评估实际存储增长
2. 优化文件压缩策略
3. 实施冷数据归档
4. 配置CDN加速(人员头像)

**工作量**: 3人天  
**成本**: 无额外硬件投入

---

#### **第三阶段 (1年后,可选)** - P2优先级

触发条件: 人员规模 > 15000人

1. 评估是否升级MinIO
2. 规划多园区部署
3. 实施异地容灾

**工作量**: 10人天  
**成本**: 新增MinIO服务器(2核4GB) 约5000元

---

## 📈 六、内存与性能优化

### 6.1 内存占用对比

| 方案 | 应用内存 | 中间件内存 | 总内存 | 节省 |
|-----|---------|-----------|--------|------|
| **本地存储**(推荐) | 2.35GB | 4GB(MySQL+Redis+Nacos) | **6.35GB** | 基准 |
| MinIO单机 | 2.35GB | 5GB | 7.35GB | -1GB |
| MinIO集群 | 2.35GB | 7GB | 9.35GB | -3GB |

**推荐**: 中小企业使用本地存储, **8GB内存服务器即可**!

---

### 6.2 文件访问优化

#### 静态文件代理 (Nginx)

```nginx
# nginx.conf
location /files/ {
    alias /data/ioedream/files/;
    expires 30d;
    add_header Cache-Control "public, immutable";
    
    # 图片压缩
    image_filter resize 800 600;  # 缩略图
    image_filter_jpeg_quality 85;
}
```

**效果**: 访问速度提升3倍, 减轻应用服务器压力

---

### 6.3 自动清理脚本

```bash
#!/bin/bash
# /data/scripts/cleanup-old-files.sh

# 门禁照片保留30天
find /data/ioedream/files/access/snapshots -type f -mtime +30 -delete

# 考勤照片保留180天
find /data/ioedream/files/attendance/photos -type f -mtime +180 -delete

# 视频关键帧保留30天
find /data/ioedream/files/video/frames -type f -mtime +30 -delete

# 视频告警保留90天
find /data/ioedream/files/video/alerts -type f -mtime +90 -delete

echo "[$(date)] 文件清理完成" >> /var/log/ioedream-cleanup.log
```

**定时任务**:
```cron
# 每天凌晨3点执行清理
0 3 * * * /data/scripts/cleanup-old-files.sh
```

---

## ✅ 七、最终方案总结

### 核心优势

1. **低成本**: 无需MinIO,节省硬件和内存
2. **低内存**: 8GB内存服务器足够运行所有服务
3. **易维护**: 本地存储,中小企业IT人员可轻松管理
4. **可扩展**: 未来可平滑升级到MinIO集群
5. **企业级**: 定时备份+自动清理+监控告警

### 关键指标

- **服务器内存**: 8GB (应用2.35GB + 中间件4GB + 系统1.65GB)
- **存储空间**: 8TB磁盘 (实际可用4TB)
- **日增文件**: 60GB/天
- **存储周期**: 核心数据30-180天, OA归档5年
- **硬件成本**: 3-5万元 (一次性投入)
- **运维成本**: 1人兼职管理

### Edge边缘计算

- **video-service**: ✅ 需要Edge (AI盒子分析视频流)
- **其他服务**: ❌ 不需要Edge

### 下一步行动

```bash
# 1. 启用本地存储(默认已启用)
export FILE_STORAGE_TYPE=local

# 2. 创建存储目录
mkdir -p /data/ioedream/files/{access,attendance,video,oa,consume,visitor,common}

# 3. 配置自动清理
sudo crontab -e
# 添加: 0 3 * * * /data/scripts/cleanup-old-files.sh

# 4. 启动服务
./start.ps1
```

---

**这就是最适合中小企业(5000-10000人)的完整方案!** 🎉

**核心理念**: 简单、实用、可靠、低成本!
