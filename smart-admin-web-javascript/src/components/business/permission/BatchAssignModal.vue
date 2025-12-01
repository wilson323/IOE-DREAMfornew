<template>
  <a-modal
    v-model:open="open"
    title="批量分配权限"
    :width="800"
    :confirmLoading="loading"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="batch-assign-modal">
      <!-- 选择信息 -->
      <div class="selection-summary">
        <a-alert
          type="info"
          show-icon
          :message="selectionSummary"
        />
      </div>

      <!-- 分配表单 -->
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
        class="assign-form"
      >
        <!-- 权限级别 -->
        <a-form-item label="权限级别" name="securityLevel">
          <a-select
            v-model:value="formData.securityLevel"
            placeholder="选择权限级别"
            allowClear
          >
            <a-select-option
              v-for="level in securityLevelOptions"
              :key="level.value"
              :value="level.value"
            >
              <a-tag :color="level.color" size="small">{{ level.label }}</a-tag>
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 过期时间 -->
        <a-form-item label="过期时间" name="expireTime">
          <a-date-picker
            v-model:value="formData.expireTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择过期时间（可选）"
            style="width: 100%"
            :disabledDate="disabledDate"
          />
          <div class="form-item-tip">
            <a-switch
              v-model:checked="formData.neverExpire"
              size="small"
            />
            永不过期
          </div>
        </a-form-item>

        <!-- 时间策略 -->
        <a-form-item label="时间策略" name="timeStrategyId">
          <a-select
            v-model:value="formData.timeStrategyId"
            placeholder="选择时间策略（可选）"
            allowClear
            :options="timeStrategyOptions"
          />
          <div class="form-item-tip">
            <a-button type="link" size="small" @click="handleCreateTimeStrategy">
              <PlusOutlined />
              创建时间策略
            </a-button>
          </div>
        </a-form-item>

        <!-- 地理围栏 -->
        <a-form-item label="地理围栏" name="geoFenceIds">
          <a-select
            v-model:value="formData.geoFenceIds"
            mode="multiple"
            placeholder="选择地理围栏（可选）"
            allowClear
            :options="geoFenceOptions"
            :filter-option="filterGeoFenceOption"
          />
          <div class="form-item-tip">
            <a-button type="link" size="small" @click="handleCreateGeoFence">
              <PlusOutlined />
              创建地理围栏
            </a-button>
          </div>
        </a-form-item>

        <!-- 设备权限 -->
        <a-form-item label="设备权限" name="deviceIds">
          <a-select
            v-model:value="formData.deviceIds"
            mode="multiple"
            placeholder="选择设备（可选）"
            allowClear
            :options="deviceOptions"
            :filter-option="filterDeviceOption"
          />
          <div class="form-item-tip">
            <a-button type="link" size="small" @click="handleCreateDevice">
              <PlusOutlined />
              添加设备
            </a-button>
          </div>
        </a-form-item>

        <!-- 分配原因 -->
        <a-form-item label="分配原因" name="reason">
          <a-textarea
            v-model:value="formData.reason"
            placeholder="请输入分配原因"
            :rows="3"
            show-count
            :maxlength="500"
          />
        </a-form-item>

        <!-- 高级选项 -->
        <a-form-item>
          <a-collapse ghost>
            <a-collapse-panel key="advanced" header="高级选项">
              <!-- 权限继承 -->
              <a-form-item label="权限继承">
                <a-checkbox v-model:checked="formData.enableInheritance">
                  启用权限继承（用户可通过部门/职位继承权限）
                </a-checkbox>
              </a-form-item>

              <!-- 权限模板 -->
              <a-form-item label="权限模板">
                <a-select
                  v-model:value="formData.templateId"
                  placeholder="选择权限模板（可选）"
                  allowClear
                  :options="templateOptions"
                />
                <div class="form-item-tip">
                  <a-button type="link" size="small" @click="handleCreateTemplate">
                    <PlusOutlined />
                    创建权限模板
                  </a-button>
                </div>
              </a-form-item>

              <!-- 通知设置 -->
              <a-form-item label="通知设置">
                <a-checkbox-group v-model:value="formData.notifications">
                  <a-checkbox value="email">
                    <MailOutlined />
                    邮件通知
                  </a-checkbox>
                  <a-checkbox value="sms">
                    <PhoneOutlined />
                    短信通知
                  </a-checkbox>
                  <a-checkbox value="system">
                    <BellOutlined />
                    系统通知
                  </a-checkbox>
                </a-checkbox-group>
              </a-form-item>
            </a-collapse-panel>
          </a-collapse>
        </a-form-item>
      </a-form>

      <!-- 预览区域 -->
      <div class="preview-section">
        <a-divider>分配预览</a-divider>
        <div class="preview-content">
          <a-table
            :columns="previewColumns"
            :data-source="previewData"
            :pagination="false"
            size="small"
            :scroll="{ y: 200 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'user'">
                <div class="user-info">
                  <a-avatar :src="record.avatar" :size="24">
                    {{ record.userName?.charAt(0) }}
                  </a-avatar>
                  <span>{{ record.userName }}</span>
                </div>
              </template>
              <template v-else-if="column.key === 'permissions'">
                <a-tag
                  v-for="permission in record.permissions"
                  :key="permission.id"
                  :color="getPermissionTypeColor(permission.type)"
                  size="small"
                >
                  {{ permission.name }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>
  </a-modal>

  <!-- 创建时间策略弹窗 -->
  <TimeStrategyModal
    v-model:open="timeStrategyModalVisible"
    @success="handleTimeStrategyCreated"
  />

  <!-- 创建地理围栏弹窗 -->
  <GeoFenceModal
    v-model:open="geoFenceModalVisible"
    @success="handleGeoFenceCreated"
  />

  <!-- 创建权限模板弹窗 -->
  <TemplateModal
    v-model:open="templateModalVisible"
    @success="handleTemplateCreated"
  />
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  MailOutlined,
  PhoneOutlined,
  BellOutlined
} from '@ant-design/icons-vue';
import { SecurityLevel, PermissionType } from '/@/types/permission';
import dayjs from 'dayjs';
import TimeStrategyModal from './TimeStrategyModal.vue';
import GeoFenceModal from './GeoFenceModal.vue';
import TemplateModal from './TemplateModal.vue';

// Props定义
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  users: {
    type: Array,
    default: () => []
  },
  permissions: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  // 预设选项
  securityLevelOptions: {
    type: Array,
    default: () => []
  },
  timeStrategyOptions: {
    type: Array,
    default: () => []
  },
  geoFenceOptions: {
    type: Array,
    default: () => []
  },
  deviceOptions: {
    type: Array,
    default: () => []
  },
  templateOptions: {
    type: Array,
    default: () => []
  }
});

// Emits定义
const emit = defineEmits([
  'update:open',
  'confirm',
  'cancel',
  'createTimeStrategy',
  'createGeoFence',
  'createTemplate'
]);

// 响应式数据
const formRef = ref();
const formData = reactive({
  securityLevel: undefined,
  expireTime: null,
  neverExpire: true,
  timeStrategyId: undefined,
  geoFenceIds: [],
  deviceIds: [],
  reason: '',
  enableInheritance: true,
  templateId: undefined,
  notifications: ['system']
});

// 子弹窗状态
const timeStrategyModalVisible = ref(false);
const geoFenceModalVisible = ref(false);
const templateModalVisible = ref(false);

// 表单验证规则
const formRules = {
  securityLevel: [
    { required: true, message: '请选择权限级别', trigger: 'change' }
  ],
  reason: [
    { max: 500, message: '分配原因不能超过500个字符', trigger: 'blur' }
  ]
};

// 计算属性
const selectionSummary = computed(() => {
  return `已选择 ${props.users.length} 个用户，${props.permissions.length} 个权限`;
});

const previewColumns = [
  {
    title: '用户',
    dataIndex: 'userName',
    key: 'user',
    width: 150
  },
  {
    title: '用户编号',
    dataIndex: 'userCode',
    width: 120
  },
  {
    title: '部门',
    dataIndex: 'departmentName',
    width: 120
  },
  {
    title: '权限',
    key: 'permissions',
    ellipsis: true
  }
];

const previewData = computed(() => {
  return props.users.map(user => ({
    ...user,
    permissions: props.permissions.slice(0, 3) // 只显示前3个权限，避免表格过宽
  }));
});

// 监听器
watch(() => formData.neverExpire, (newVal) => {
  if (newVal) {
    formData.expireTime = null;
  }
});

watch(() => props.open, (newVal) => {
  if (newVal) {
    // 重置表单数据
    resetFormData();
  }
});

// 方法
/**
 * 重置表单数据
 */
const resetFormData = () => {
  Object.assign(formData, {
    securityLevel: undefined,
    expireTime: null,
    neverExpire: true,
    timeStrategyId: undefined,
    geoFenceIds: [],
    deviceIds: [],
    reason: '',
    enableInheritance: true,
    templateId: undefined,
    notifications: ['system']
  });
};

/**
 * 禁用过期日期（只能选择未来日期）
 */
const disabledDate = (current) => {
  return current && current < dayjs().startOf('day');
};

/**
 * 过理地理围栏选项
 */
const filterGeoFenceOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

/**
 * 过理设备选项
 */
const filterDeviceOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

/**
 * 获取权限类型颜色
 */
const getPermissionTypeColor = (type) => {
  const colorMap = {
    [PermissionType.MENU]: 'blue',
    [PermissionType.BUTTON]: 'green',
    [PermissionType.API]: 'orange',
    [PermissionType.DATA]: 'purple',
    [PermissionType.DEVICE]: 'cyan',
    [PermissionType.AREA]: 'pink',
    [PermissionType.TIME]: 'geekblue'
  };
  return colorMap[type] || 'default';
};

/**
 * 处理确认
 */
const handleConfirm = async () => {
  try {
    await formRef.value.validate();

    const data = {
      users: props.users,
      permissions: props.permissions,
      ...formData,
      expireTime: formData.neverExpire ? null : formData.expireTime?.format('YYYY-MM-DD HH:mm:ss')
    };

    emit('confirm', data);
  } catch (error) {
    console.error('表单验证失败:', error);
  }
};

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('update:open', false);
  emit('cancel');
};

/**
 * 处理创建时间策略
 */
const handleCreateTimeStrategy = () => {
  timeStrategyModalVisible.value = true;
};

/**
 * 处理时间策略创建成功
 */
const handleTimeStrategyCreated = (timeStrategy) => {
  timeStrategyModalVisible.value = false;
  emit('createTimeStrategy', timeStrategy);
};

/**
 * 处理创建地理围栏
 */
const handleCreateGeoFence = () => {
  geoFenceModalVisible.value = true;
};

/**
 * 处理地理围栏创建成功
 */
const handleGeoFenceCreated = (geoFence) => {
  geoFenceModalVisible.value = false;
  emit('createGeoFence', geoFence);
};

/**
 * 处理创建设备
 */
const handleCreateDevice = () => {
  // 这里可以打开设备创建弹窗或跳转到设备管理页面
  message.info('设备管理功能开发中');
};

/**
 * 处理创建模板
 */
const handleCreateTemplate = () => {
  templateModalVisible.value = true;
};

/**
 * 处理模板创建成功
 */
const handleTemplateCreated = (template) => {
  templateModalVisible.value = false;
  emit('createTemplate', template);
};

// 暴露方法给父组件
defineExpose({
  resetFormData,
  handleConfirm,
  handleCancel
});
</script>

<style scoped lang="less">
.batch-assign-modal {
  .selection-summary {
    margin-bottom: 20px;
  }

  .assign-form {
    margin-bottom: 20px;

    .form-item-tip {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 4px;
      font-size: 12px;
      color: #8c8c8c;
    }

    :deep(.ant-form-item-label > label) {
      font-weight: 500;
    }

    :deep(.ant-collapse) {
      .ant-collapse-header {
        font-weight: 500;
      }
    }
  }

  .preview-section {
    .preview-content {
      :deep(.ant-table) {
        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }
  }
}
</style>