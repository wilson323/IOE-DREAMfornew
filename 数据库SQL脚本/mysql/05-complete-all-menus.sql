-- ==================================================================
-- Step 4: 补充所有缺失的菜单和功能点
-- ==================================================================
USE ioedream;

-- 获取父菜单ID
SET @business_id = (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 LIMIT 1);
SET @support_id = (SELECT menu_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 LIMIT 1);
SET @visitor_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0 LIMIT 1);
SET @video_id = (SELECT menu_id FROM t_menu WHERE menu_name = '智能视频' AND deleted_flag = 0 LIMIT 1);
SET @oa_id = (SELECT menu_id FROM t_menu WHERE menu_name = 'OA办公' AND deleted_flag = 0 LIMIT 1);
SET @erp_id = (SELECT menu_id FROM t_menu WHERE menu_name = 'ERP管理' AND deleted_flag = 0 LIMIT 1);

-- ==================================================================
-- 1. 补充访客管理缺失的菜单
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('物流访客', 2, @visitor_id, 5, '/business/visitor/logistics', '/business/visitor/logistics.vue', 'business:visitor:logistics', 'CarOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('访客统计', 2, @visitor_id, 6, '/business/visitor/statistics', '/business/visitor/statistics.vue', 'business:visitor:statistics', 'BarChartOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 2. 智能视频模块（14个子菜单）
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('实时监控', 2, @video_id, 1, '/business/smart-video/live', '/business/smart-video/live/index.vue', 'business:video:live', 'EyeOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('录像回放', 2, @video_id, 2, '/business/smart-video/playback', '/business/smart-video/playback/index.vue', 'business:video:playback', 'PlayCircleOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备管理', 2, @video_id, 3, '/business/smart-video/device', '/business/smart-video/device/index.vue', 'business:video:device', 'VideoCameraOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('通道管理', 2, @video_id, 4, '/business/smart-video/channel', '/business/smart-video/channel/index.vue', 'business:video:channel', 'BranchesOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('云台控制', 2, @video_id, 5, '/business/smart-video/ptz', '/business/smart-video/ptz/index.vue', 'business:video:ptz', 'ControlOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('人脸抓拍', 2, @video_id, 6, '/business/smart-video/face-capture', '/business/smart-video/face-capture/index.vue', 'business:video:face', 'UserOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('行为分析', 2, @video_id, 7, '/business/smart-video/behavior', '/business/smart-video/behavior/index.vue', 'business:video:behavior', 'DashboardOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('周界报警', 2, @video_id, 8, '/business/smart-video/perimeter', '/business/smart-video/perimeter/index.vue', 'business:video:perimeter', 'AlertOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('电子地图', 2, @video_id, 9, '/business/smart-video/map', '/business/smart-video/map/index.vue', 'business:video:map', 'EnvironmentOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('报警记录', 2, @video_id, 10, '/business/smart-video/alarm', '/business/smart-video/alarm/index.vue', 'business:video:alarm', 'BellOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('存储管理', 2, @video_id, 11, '/business/smart-video/storage', '/business/smart-video/storage/index.vue', 'business:video:storage', 'DatabaseOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('录像计划', 2, @video_id, 12, '/business/smart-video/record-plan', '/business/smart-video/record-plan/index.vue', 'business:video:plan', 'ScheduleOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('视频配置', 2, @video_id, 13, '/business/smart-video/config', '/business/smart-video/config/index.vue', 'business:video:config', 'SettingOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('统计分析', 2, @video_id, 14, '/business/smart-video/statistics', '/business/smart-video/statistics/index.vue', 'business:video:statistics', 'PieChartOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 3. OA办公模块
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('通知公告', 2, @oa_id, 1, '/business/oa/notice', '/business/oa/notice/index.vue', 'business:oa:notice', 'NotificationOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('企业管理', 2, @oa_id, 2, '/business/oa/enterprise', '/business/oa/enterprise/index.vue', 'business:oa:enterprise', 'BankOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('工作流', 2, @oa_id, 3, '/business/oa/workflow', '/business/oa/workflow/index.vue', 'business:oa:workflow', 'ApartmentOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 4. ERP管理模块
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('商品目录', 2, @erp_id, 1, '/business/erp/goods-category', '/business/erp/goods/goods-category-index.vue', 'business:erp:category', 'AppstoreOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('商品列表', 2, @erp_id, 2, '/business/erp/goods', '/business/erp/goods/goods-index.vue', 'business:erp:goods', 'ShoppingOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 5. 补充支撑功能模块（14个缺失的菜单）
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('数据变更', 2, @support_id, 60, '/support/change-log', '/support/change-log/index.vue', 'support:changeLog', 'HistoryOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('帮助文档', 2, @support_id, 70, '/support/help-doc', '/support/help-doc/index.vue', 'support:helpDoc', 'QuestionCircleOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('消息中心', 2, @support_id, 80, '/support/message', '/support/message/index.vue', 'support:message', 'MessageOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('定时任务', 2, @support_id, 90, '/support/job', '/support/job/index.vue', 'support:job', 'ClockCircleOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('序列号', 2, @support_id, 100, '/support/serial-number', '/support/serial-number/index.vue', 'support:serialNumber', 'NumberOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('心跳记录', 2, @support_id, 110, '/support/heart-beat', '/support/heart-beat/index.vue', 'support:heartBeat', 'HeartOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('缓存管理', 2, @support_id, 120, '/support/cache', '/support/cache/index.vue', 'support:cache', 'CloudServerOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('代码生成', 2, @support_id, 130, '/support/code-generator', '/support/code-generator/index.vue', 'support:codeGenerator', 'CodeOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('reload', 2, @support_id, 140, '/support/reload', '/support/reload/index.vue', 'support:reload', 'ReloadOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('等保日志', 2, @support_id, 150, '/support/level-protect-log', '/support/level-protect-log/index.vue', 'support:levelProtectLog', 'SafetyOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('接口加密', 2, @support_id, 160, '/support/api-encrypt', '/support/api-encrypt/index.vue', 'support:apiEncrypt', 'LockOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('数据导出', 2, @support_id, 170, '/support/data-tracer', '/support/data-tracer/index.vue', 'support:dataTracer', 'ExportOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('邮件发送', 2, @support_id, 180, '/support/mail', '/support/mail/index.vue', 'support:mail', 'MailOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('表格导入', 2, @support_id, 190, '/support/table-column', '/support/table-column/index.vue', 'support:tableColumn', 'TableOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- 更新角色菜单关联
INSERT INTO `t_role_menu` (role_id, menu_id) 
SELECT 1, menu_id FROM t_menu WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

SELECT 'Complete menu initialization finished' AS status;
SELECT COUNT(*) AS total_menus FROM t_menu WHERE deleted_flag = 0;
SELECT 
  m1.menu_name AS module,
  COUNT(m2.menu_id) AS menu_count
FROM t_menu m1 
LEFT JOIN t_menu m2 ON m2.parent_id = m1.menu_id AND m2.deleted_flag = 0
WHERE m1.parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0)
  AND m1.deleted_flag = 0 
GROUP BY m1.menu_id, m1.menu_name 
ORDER BY m1.sort;
