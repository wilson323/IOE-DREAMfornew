# Visitor Management Capability Specification Delta

## ADDED Requirements

### Requirement: 访客预约审批流程
系统SHALL提供完整的访客预约和审批流程。

#### Scenario: 在线预约提交
- **GIVEN** 访客需要访问公司
- **WHEN** 在线提交预约申请
- **THEN** 应填写访问信息、时间、事由
- **AND** 选择被访人
- **AND** 提交审批

#### Scenario: 被访人审批
- **GIVEN** 被访人收到预约通知
- **WHEN** 被访人审批
- **THEN** 应选择同意或拒绝
- **AND** 填写审批意见
- **AND** 转到门卫审批

#### Scenario: 门卫审批
- **GIVEN** 门卫收到待审批预约
- **WHEN** 门卫审批
- **THEN** 应验证访客身份
- **AND** 确认访问时间
- **AND** 生成通行证

#### Scenario: 预约状态流转
- **GIVEN** 预约记录创建
- **WHEN** 流程流转
- **THEN** 应遵循状态机：待审批→被访人审批→门卫审批→已通过
- **AND** 记录状态变更历史

### Requirement: 电子通行证系统
系统SHALL提供二维码电子通行证。

#### Scenario: 通行证生成
- **GIVEN** 预约审批通过
- **WHEN** 生成电子通行证
- **THEN** 应使用ZXing生成二维码
- **AND** 使用AES加密数据
- **AND** 设置有效期≤24小时

#### Scenario: 通行证验证
- **GIVEN** 访客出示通行证二维码
- **WHEN** 门禁设备扫描
- **THEN** 应解析二维码
- **AND** AES解密数据
- **AND** 验证签名和有效期
- **AND** 验证通过则允许通行

#### Scenario: 通行证离线验证
- **GIVEN** 门禁设备离线
- **WHEN** 扫描通行证
- **THEN** 应使用预存的签名验证
- **AND** 验证有效期
- **AND** 离线记录通行

#### Scenario: 通行证过期
- **GIVEN** 通行证有效期24小时
- **WHEN** 超过有效期
- **THEN** 应验证失败
- **AND** 提示重新申请

### Requirement: 黑名单识别功能
访客系统SHALL自动识别黑名单人员。
系统应自动识别黑名单人员。

#### Scenario: 黑名单管理
- **GIVEN** 管理员添加黑名单
- **WHEN** 添加访客到黑名单
- **THEN** 应记录黑名单原因
- **AND** 设置有效期

#### Scenario: 黑名单实时识别
- **GIVEN** 访客在黑名单中
- **WHEN** 访客预约或到访
- **THEN** 应自动识别
- **AND** 拒绝预约或通行
- **AND** 发送告警通知

#### Scenario: 黑名单自动过期
- **GIVEN** 黑名单设置有效期
- **WHEN** 有效期过期
- **THEN** 应自动移除黑名单
- **AND** 记录移除日志

### Requirement: 人脸记录留存
访客系统SHALL采集和存储访客人脸照片。
系统应采集和存储访客人脸照片。

#### Scenario: 人脸照片采集
- **GIVEN** 访客首次到访
- **WHEN** 门卫采集人脸
- **THEN** 应拍摄高清照片
- **AND** 存储到人脸数据库
- **AND** 设置保留期90天

#### Scenario: 人脸照片检索
- **GIVEN** 访客人脸记录
- **WHEN** 需要检索访客
- **THEN** 应支持以图搜图
- **AND** 显示相似访客列表

#### Scenario: 人脸照片过期删除
- **GIVEN** 照片保留期90天
- **WHEN** 超过保留期
- **THEN** 应自动删除照片
- **AND** 保留必要索引信息

### Requirement: 访客轨迹追踪
访客系统SHALL实时追踪访客位置。
系统应实时追踪访客位置。

#### Scenario: 实时位置显示
- **GIVEN** 访客在园区内
- **WHEN** 访客移动
- **THEN** 应在地图上显示实时位置
- **AND** 更新频率≤5秒

#### Scenario: 通行记录查询
- **GIVEN** 访客通行记录
- **WHEN** 查询轨迹
- **THEN** 应显示所有通行记录
- **AND** 按时间排序
- **AND** 显示通行地点

#### Scenario: 区域轨迹显示
- **GIVEN** 访客访问多个区域
- **WHEN** 在地图上显示轨迹
- **THEN** 应连线显示访问路径
- **AND** 标注访问时间

#### Scenario: 轨迹回放
- **GIVEN** 访客离开后
- **WHEN** 回放轨迹
- **THEN** 应动态显示移动过程
- **AND** 可调整回放速度

### Requirement: VIP访客管理
系统SHALL支持长期有效的VIP访客通行证。

#### Scenario: VIP访客申请
- **GIVEN** 长期合作伙伴
- **WHEN** 申请VIP访客
- **THEN** 应简化审批流程
- **AND** 可设置有效期≥1个月
- **AND** 支持多次进出

#### Scenario: VIP访客自动审批
- **GIVEN** VIP访客预约
- **WHEN** 符合VIP条件
- **THEN** 应自动审批通过
- **AND** 跳过人工审批

#### Scenario: VIP访客多次通行
- **GIVEN** VIP访客通行证
- **WHEN** 有效期内多次到访
- **THEN** 应无需重复预约
- **AND** 扫码即可通行

### Requirement: 访客统计分析
系统SHALL提供访客数据统计分析。

#### Scenario: 访客流量统计
- **GIVEN** 园区访客数据
- **WHEN** 统计访客流量
- **THEN** 应按日/周/月统计
- **AND** 生成流量图表

#### Scenario: 访问时长统计
- **GIVEN** 访客访问记录
- **WHEN** 统计访问时长
- **THEN** 应计算平均访问时长
- **AND** 识别超长访问

#### Scenario: 高频访客分析
- **GIVEN** 访客访问记录
- **WHEN** 分析访客频次
- **THEN** 应识别高频访客
- **AND** 建议升级为VIP

### Requirement: 访客自助服务
系统SHALL提供访客自助服务终端。

#### Scenario: 自助预约机
- **GIVEN** 访客在入口大厅
- **WHEN** 使用自助预约机
- **THEN** 应自助填写预约信息
- **AND** 采集人脸照片
- **AND** 打印临时凭证

#### Scenario: 自助签离
- **GIVEN** 访客离开
- **WHEN** 使用自助签离机
- **THEN** 应扫码签离
- **AND** 记录离开时间
- **AND** 打印访问凭证

#### Scenario: 自助打印
- **GIVEN** 访客需要凭证
- **WHEN** 使用自助打印
- **THEN** 应打印访问凭证
- **AND** 打印通行记录
