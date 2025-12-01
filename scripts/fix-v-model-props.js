#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

// 需要修复的文件列表
const files = [
  'smart-admin-web-javascript/src/components/framework/icon-select/index.vue',
  'smart-admin-web-javascript/src/components/support/data-tracer/index.vue',
  'smart-admin-web-javascript/src/components/system/employee-table-select-modal/index.vue',
  'smart-admin-web-javascript/src/views/business/access/device/components/BatchStatusUpdateModal.vue',
  'smart-admin-web-javascript/src/views/business/access/device/components/DeviceDetailModal.vue',
  'smart-admin-web-javascript/src/views/business/access/device/components/DeviceFormModal.vue',
  'smart-admin-web-javascript/src/views/business/oa/notice/components/notice-form-visible-modal.vue',
  'smart-admin-web-javascript/src/views/common/device/DeviceModal.vue',
  'smart-admin-web-javascript/src/views/location/components/GeoFenceModal.vue',
  'smart-admin-web-javascript/src/views/location/components/LocationDetailModal.vue',
  'smart-admin-web-javascript/src/views/location/components/TrajectoryPlayer.vue',
  'smart-admin-web-javascript/src/views/smart-permission/components/assignment/modals/GrantPermissionModal.vue',
  'smart-admin-web-javascript/src/views/support/help-doc/management/components/help-doc-catalog-form-modal.vue',
  'smart-admin-web-javascript/src/views/support/message/components/message-receiver-modal.vue',
  'smart-admin-web-javascript/src/views/system/department/components/department-form-modal.vue',
  'smart-admin-web-javascript/src/views/system/employee/components/employee-department-form-modal/index.vue',
  'smart-admin-web-javascript/src/views/system/employee/components/employee-password-dialog/index.vue',
  'smart-admin-web-javascript/src/views/system/home/components/quick-entry/home-quick-entry-modal.vue',
  'smart-admin-web-javascript/src/views/system/home/components/to-be-done-card/to-be-done-modal.vue'
];

console.log('开始修复Vue 3 v-model prop问题...');

let fixedCount = 0;
let errorCount = 0;

files.forEach(filePath => {
  try {
    if (fs.existsSync(filePath)) {
      let content = fs.readFileSync(filePath, 'utf8');

      // 修复 v-model:open="visible"  -> :open="visible" @update:open="val => emit('update:visible', val)"
      const vModelPattern = /v-model:open="(visible[^"]*)"/g;
      const originalContent = content;

      content = content.replace(vModelPattern, ':open="$1" @update:open="val => emit(\'update:visible\', val)"');

      // 修复 v-model:checked="visibleFlag"  -> :checked="visibleFlag" @update:checked="val => emit('update:checked', val)"
      const vModelCheckedPattern = /v-model:checked="([^"]*)"/g;
      content = content.replace(vModelCheckedPattern, ':checked="$1" @update:checked="val => emit(\'update:checked\', val)"');

      // 修复 v-model:value="visibleFlag"  -> :value="visibleFlag" @update:value="val => emit('update:value', val)"
      const vModelPropertyPattern = /v-model:value="([^"]*)"/g;
      content = content.replace(vModelPropertyPattern, ':value="$1" @update:value="val => emit(\'update:value\', val)"');

      if (content !== originalContent) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`✅ 修复: ${filePath}`);
        fixedCount++;
      } else {
        console.log(`⚪️  无需修复: ${filePath}`);
      }
    } else {
      console.log(`❌ 文件不存在: ${filePath}`);
      errorCount++;
    }
  } catch (error) {
    console.error(`❌ 修复失败: ${filePath} - ${error.message}`);
    errorCount++;
  }
});

console.log('\n修复完成！');
console.log(`✅ 成功修复: ${fixedCount} 个文件`);
console.log(`❌ 失败/跳过: ${errorCount} 个文件`);