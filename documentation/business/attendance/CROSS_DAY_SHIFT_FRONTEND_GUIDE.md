# 跨天班次前端管理界面实现指南

## 📋 概述

本文档说明如何在前端班次管理页面中添加跨天班次支持功能。

## 🎯 实现目标

1. **班次列表显示跨天标识** - 在班次列表中显示跨天标识
2. **创建/编辑班次时支持跨天设置** - 添加跨天规则选择
3. **自动检测跨天班次** - 根据上下班时间自动判断
4. **跨天规则可视化** - 清晰展示不同跨天规则的效果

## 📂 相关文件

- **班次管理页面**: `smart-admin-web-javascript/src/views/business/attendance/base-info/rule/rule-manage.vue`
- **班次API**: `smart-admin-web-javascript/src/api/business/attendance/workshift-api.js` (需创建)
- **后端Controller**: `WorkShiftController.java`

## 🚀 实现步骤

### 步骤1: 创建班次API文件

创建 `src/api/business/attendance/workshift-api.js`:

```javascript
import { request } from '@/utils/request';

/**
 * 班次配置API
 */
export const workShiftApi = {
  /**
   * 查询所有班次
   */
  getAllWorkShifts() {
    return request.get('/api/v1/attendance/workshift/list');
  },

  /**
   * 根据类型查询班次
   */
  getWorkShiftsByType(shiftType) {
    return request.get(`/api/v1/attendance/workshift/type/${shiftType}`);
  },

  /**
   * 查询班次详情
   */
  getWorkShiftDetail(shiftId) {
    return request.get(`/api/v1/attendance/workshift/${shiftId}`);
  },

  /**
   * 创建班次
   */
  createWorkShift(data) {
    return request.post('/api/v1/attendance/workshift', data);
  },

  /**
   * 更新班次
   */
  updateWorkShift(shiftId, data) {
    return request.put(`/api/v1/attendance/workshift/${shiftId}`, data);
  },

  /**
   * 删除班次
   */
  deleteWorkShift(shiftId) {
    return request.delete(`/api/v1/attendance/workshift/${shiftId}`);
  },

  /**
   * 检测跨天班次
   */
  checkCrossDay(startTime, endTime) {
    return request.get('/api/v1/attendance/workshift/check-cross-day', {
      params: { startTime, endTime }
    });
  }
};
```

### 步骤2: 修改班次管理页面

在 `rule-manage.vue` 中添加跨天支持：

#### 2.1 添加跨天标识列

```javascript
// 在表格columns中添加
{
  title: '跨天班次',
  key: 'isOvernight',
  width: 100,
  align: 'center',
  customRender: ({ record }) => {
    if (record.isOvernight) {
      return h('a-tag', { color: 'orange' }, '跨天');
    }
    return h('a-tag', { color: 'green' }, '正常');
  }
},
{
  title: '跨天规则',
  key: 'crossDayRule',
  width: 120,
  align: 'center',
  customRender: ({ record }) => {
    const ruleMap = {
      'START_DATE': '以开始日期为准',
      'END_DATE': '以结束日期为准',
      'SPLIT': '分别归属'
    };
    return ruleMap[record.crossDayRule] || '-';
  }
}
```

#### 2.2 添加跨天设置表单

在新增/编辑Modal中添加跨天相关字段：

```vue
<a-form-item label="上班时间" name="startTime">
  <a-time-picker
    v-model:value="workShiftForm.startTime"
    format="HH:mm"
    placeholder="请选择上班时间"
    style="width: 100%"
    @change="handleTimeChange"
  />
</a-form-item>

<a-form-item label="下班时间" name="endTime">
  <a-time-picker
    v-model:value="workShiftForm.endTime"
    format="HH:mm"
    placeholder="请选择下班时间"
    style="width: 100%"
    @change="handleTimeChange"
  />
</a-form-item>

<!-- 自动检测跨天 -->
<a-alert
  v-if="isCrossDay"
  message="检测到跨天班次"
  description="该班次的下班时间早于上班时间，系统已自动标记为跨天班次。"
  type="warning"
  show-icon
  style="margin-bottom: 16px"
/>

<a-form-item label="跨天规则" name="crossDayRule" v-if="isCrossDay">
  <a-select
    v-model:value="workShiftForm.crossDayRule"
    placeholder="请选择跨天归属规则"
  >
    <a-select-option value="START_DATE">
      以开始日期为准（推荐）
      <div style="font-size: 12px; color: #999">
        所有打卡记录归属到班次开始日期，适合夜班考勤统计
      </div>
    </a-select-option>
    <a-select-option value="END_DATE">
      以结束日期为准
      <div style="font-size: 12px; color: #999">
        所有打卡记录归属到班次结束日期
      </div>
    </a-select-option>
    <a-select-option value="SPLIT">
      分别归属（不推荐）
      <div style="font-size: 12px; color: #999">
        上班打卡归属开始日期，下班打卡归属结束日期
      </div>
    </a-select-option>
  </a-select>
</a-form-item>
```

#### 2.3 添加跨天检测逻辑

```javascript
import { ref, computed, watch } from 'vue';
import { workShiftApi } from '@/api/business/attendance/workshift-api';
import { message } from 'ant-design-vue';

// 表单数据
const workShiftForm = ref({
  shiftName: '',
  startTime: null,
  endTime: null,
  isOvernight: false,
  crossDayRule: 'START_DATE'
});

// 检测是否跨天
const isCrossDay = computed(() => {
  if (workShiftForm.value.startTime && workShiftForm.value.endTime) {
    return workShiftForm.value.endTime.isBefore(workShiftForm.value.startTime);
  }
  return false;
});

// 时间改变时自动检测
const handleTimeChange = async () => {
  if (workShiftForm.value.startTime && workShiftForm.value.endTime) {
    try {
      const response = await workShiftApi.checkCrossDay(
        workShiftForm.value.startTime.format('HH:mm'),
        workShiftForm.value.endTime.format('HH:mm')
      );

      if (response.data) {
        workShiftForm.value.isOvernight = true;
        if (!workShiftForm.value.crossDayRule) {
          workShiftForm.value.crossDayRule = 'START_DATE';
        }
        message.warning('检测到跨天班次，已自动设置跨天规则');
      } else {
        workShiftForm.value.isOvernight = false;
        workShiftForm.value.crossDayRule = null;
      }
    } catch (error) {
      console.error('跨天检测失败:', error);
    }
  }
};

// 监听跨天标识变化
watch(() => workShiftForm.value.isOvernight, (newVal) => {
  if (newVal && !workShiftForm.value.crossDayRule) {
    workShiftForm.value.crossDayRule = 'START_DATE';
  }
});
```

### 步骤3: 跨天规则可视化帮助

添加跨天规则说明组件：

```vue
<template>
  <a-modal
    v-model:visible="helpVisible"
    title="跨天规则说明"
    :footer="null"
    width="700px"
  >
    <a-steps direction="vertical" :current="-1">
      <a-step title="以开始日期为准（START_DATE）- 推荐">
        <template #description>
          <div>
            <p><strong>适用场景</strong>: 夜班考勤，如22:00-06:00</p>
            <p><strong>规则说明</strong>: 所有打卡记录（包括第二天的下班打卡）都归属到班次开始日期</p>
            <p><strong>示例</strong>:</p>
            <ul>
              <li>班次：1月1日 22:00 - 1月2日 06:00</li>
              <li>上班打卡：1月1日 21:55 → 归属到1月1日</li>
              <li>下班打卡：1月2日 06:05 → 归属到1月1日</li>
            </ul>
            <a-tag color="green">适合夜班考勤统计</a-tag>
          </div>
        </template>
      </a-step>

      <a-step title="以结束日期为准（END_DATE）">
        <template #description>
          <div>
            <p><strong>适用场景</strong>: 需要将夜班归属到第二天</p>
            <p><strong>规则说明</strong>: 所有打卡记录都归属到班次结束日期</p>
            <p><strong>示例</strong>:</p>
            <ul>
              <li>班次：1月1日 22:00 - 1月2日 06:00</li>
              <li>上班打卡：1月1日 21:55 → 归属到1月2日</li>
              <li>下班打卡：1月2日 06:05 → 归属到1月2日</li>
            </ul>
          </div>
        </template>
      </a-step>

      <a-step title="分别归属（SPLIT）- 不推荐">
        <template #description>
          <div>
            <p><strong>适用场景</strong>: 特殊需求，分别统计上下班</p>
            <p><strong>规则说明</strong>: 上班打卡归属到开始日期，下班打卡归属到结束日期</p>
            <p><strong>示例</strong>:</p>
            <ul>
              <li>班次：1月1日 22:00 - 1月2日 06:00</li>
              <li>上班打卡：1月1日 21:55 → 归属到1月1日</li>
              <li>下班打卡：1月2日 06:05 → 归属到1月2日</li>
            </ul>
            <a-tag color="orange">可能导致考勤统计异常</a-tag>
          </div>
        </template>
      </a-step>
    </a-steps>

    <div style="text-align: center; margin-top: 24px">
      <a-button type="primary" @click="helpVisible = false">我知道了</a-button>
    </div>
  </a-modal>
</template>

<script setup>
import { ref } from 'vue';

const helpVisible = ref(false);

const showHelp = () => {
  helpVisible.value = true;
};

defineExpose({
  showHelp
});
</script>
```

### 步骤4: 表单验证规则

```javascript
const workShiftFormRules = {
  shiftName: [
    { required: true, message: '请输入班次名称', trigger: 'blur' },
    { max: 100, message: '班次名称不能超过100个字符', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择上班时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择下班时间', trigger: 'change' },
    {
      validator: async (rule, value) => {
        if (!value || !workShiftForm.value.startTime) {
          return Promise.resolve();
        }

        // 调用后端API验证跨天逻辑
        const response = await workShiftApi.checkCrossDay(
          workShiftForm.value.startTime.format('HH:mm'),
          value.format('HH:mm')
        );

        if (response.data && !workShiftForm.value.crossDayRule) {
          return Promise.reject('检测到跨天班次，请选择跨天规则');
        }

        return Promise.resolve();
      },
      trigger: 'change'
    }
  ],
  crossDayRule: [
    {
      validator: (rule, value) => {
        if (isCrossDay.value && !value) {
          return Promise.reject('跨天班次必须选择跨天规则');
        }
        return Promise.resolve();
      },
      trigger: 'change'
    }
  ]
};
```

## 📊 UI效果示例

### 班次列表展示

```
┌──────────────────────────────────────────────────────────────┐
│ 班次管理                                    [新增班次] [批量操作] │
├──────────────────────────────────────────────────────────────┤
│ 班次名称    | 开始时间 | 结束时间 | 跨天标识 | 跨天规则    │ 操作 │
├──────────────────────────────────────────────────────────────┤
│ 正常班     | 09:00   | 18:00   │ [正常]   | -          │ 编辑 │
│ 夜班A     | 22:00   | 06:00   │ [跨天]   │ 以开始日期 │ 编辑 │
│ 夜班B     | 20:00   | 04:00   │ [跨天]   │ 以结束日期 │ 编辑 │
└──────────────────────────────────────────────────────────────┘
```

### 创建/编辑跨天班次

```
┌─────────────────────────────────────────────────┐
│ 新增班次                                         │
├─────────────────────────────────────────────────┤
│ 班次名称:     [_____________________]            │
│ 上班时间:     [22:00        ▲]                   │
│ 下班时间:     [06:00        ▲]                   │
│ ┌───────────────────────────────────────────┐   │
│ │ ⚠ 检测到跨天班次                          │   │
│ │ 该班次的下班时间早于上班时间，系统已自动   │   │
│ │ 标记为跨天班次。                           │   │
│ └───────────────────────────────────────────┘   │
│ 跨天规则:     [以开始日期为准（推荐） ▼]         │
│               ├ 以开始日期为准（推荐）           │
│               ├ 以结束日期为准                   │
│               └ 分别归属（不推荐）               │
│               [什么是跨天规则?]                 │
│                                                  │
│                [取消]  [保存]                    │
└─────────────────────────────────────────────────┘
```

## ✅ 验证清单

完成实现后，请验证以下功能：

- [ ] 班次列表正确显示跨天标识
- [ ] 创建班次时自动检测跨天
- [ ] 可以选择跨天规则
- [ ] 跨天规则帮助说明正常显示
- [ ] 表单验证规则生效
- [ ] 编辑班次时可以修改跨天规则
- [ ] API调用成功
- [ ] 错误处理正确

## 🔗 相关文档

- **后端实现**: `CrossDayShiftUtil.java`
- **数据库迁移**: `V2.4__cross_day_shift_support.sql`
- **Controller**: `WorkShiftController.java`
- **API规范**: `CLAUDE.md` - 考勤管理模块

## 📞 技术支持

如有问题，请联系：
- 后端团队: IOE-DREAM架构团队
- 前端团队: SmartAdmin前端团队

---

**文档版本**: v1.0.0
**创建日期**: 2025-01-30
**最后更新**: 2025-01-30
