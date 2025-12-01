-- RBAC 资源注册脚本（按模块）
-- 说明：请根据你们的RBAC资源表实际表名、字段名适配（示例使用 t_rbac_resource）
-- 字段示例：resource_code, resource_name, module, enabled_flag

-- 设备模块
INSERT INTO t_rbac_resource (resource_code, resource_name, module, enabled_flag) VALUES
('device:page','设备-分页查询','device',1),
('device:detail','设备-详情','device',1),
('device:add','设备-新增','device',1),
('device:update','设备-更新','device',1),
('device:delete','设备-删除','device',1),
('device:enable','设备-启用','device',1),
('device:disable','设备-禁用','device',1),
('device:batch','设备-批量操作','device',1),
('device:heartbeat','设备-心跳','device',1),
('device:status','设备-状态更新','device',1),
('device:control','设备-远程控制','device',1),
('device:config:get','设备-配置读取','device',1),
('device:config:update','设备-配置更新','device',1),
('device:online','设备-在线列表','device',1),
('device:offline','设备-离线列表','device',1),
('device:statistics:status','设备-状态统计','device',1),
('device:statistics:type','设备-类型统计','device',1);

-- 人事模块
INSERT INTO t_rbac_resource (resource_code, resource_name, module, enabled_flag) VALUES
('hr:employee:list','员工-分页','hr',1),
('hr:employee:detail','员工-详情','hr',1),
('hr:employee:add','员工-新增','hr',1),
('hr:employee:update','员工-更新','hr',1),
('hr:employee:delete','员工-删除','hr',1);

-- 考勤模块
INSERT INTO t_rbac_resource (resource_code, resource_name, module, enabled_flag) VALUES
('attendance:clock','考勤-打卡','attendance',1),
('attendance:records','考勤-记录分页','attendance',1);

-- 门禁模块（示例，仅列出验证主能力，若有更多控制器方法，可继续补充）
INSERT INTO t_rbac_resource (resource_code, resource_name, module, enabled_flag) VALUES
('access:verify','门禁-通行验证','access',1),
('access:remote-open','门禁-远程开门','access',1),
('access:record','门禁-记录通行','access',1),
('access:token:generate','门禁-生成令牌','access',1),
('access:token:verify','门禁-验证令牌','access',1),
('access:statistics:person','门禁-个人统计','access',1),
('access:statistics:device','门禁-设备统计','access',1),
('access:check-forced-open','门禁-强制开门检查','access',1);

-- 消费模块
INSERT INTO t_rbac_resource (resource_code, resource_name, module, enabled_flag) VALUES
('consume:pay','消费-扣费','consume',1),
('consume:records','消费-记录分页','consume',1);


