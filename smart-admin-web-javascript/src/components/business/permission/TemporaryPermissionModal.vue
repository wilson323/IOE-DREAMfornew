<template>
  <a-modal
    v-model:open="open"
    title="临时权限设置"
    :width="800"
    :confirmLoading="loading"
    @ok="handleConfirm"
    @cancel="handleCancel"
    :destroyOnClose="true"
  >
    <div class="temporary-permission-modal">
      <!-- 步骤指示器 -->
      <a-steps :current="currentStep" size="small" class="steps">
        <a-step title="选择用户" description="选择需要分配临时权限的用户" />
        <a-step title="选择权限" description="选择要分配的权限" />
        <a-step title="设置时限" description="设置权限有效时间和规则" />
        <a-step title="确认配置" description="确认临时权限配置" />
      </a-steps>

      <!-- 步骤内容 -->
      <div class="step-content">
        <!-- 步骤1: 选择用户 -->
        <div v-show="currentStep === 0" class="step-users">
          <div class="step-header">
            <h3>选择用户</h3>
            <a-input-search
              v-model:value="userSearchKeyword"
              placeholder="搜索用户"
              style="width: 300px"
              @search="handleUserSearch"
            />
          </div>

          <UserPermissionSelector
            ref="userSelectorRef"
            :users="availableUsers"
            :multiple="allowMultipleUsers"
            :showPermission="true"
            :height="300"
            @select="handleUserSelect"
            @search="handleUserSearch"
          />
        </div>

        <!-- 步骤2: 选择权限 -->
        <div v-show="currentStep === 1" class="step-permissions">
          <div class="step-header">
            <h3>选择权限</h3>
            <div class="permission-filters">
              <a-select
                v-model:value="permissionFilter.type"
                placeholder="权限类型"
                allowClear
                style="width: 150px"
                @change="handlePermissionFilter"
              >
                <a-select-option
                  v-for="type in permissionTypeOptions"
                  :key="type.value"
                  :value="type.value"
                >
                  {{ type.label }}
                </a-select-option>
              </a-select>

              <SecurityLevelSelector
                v-model:value="permissionFilter.securityLevel"
                :multiple="false"
                :showCount="true"
                :levelCounts="permissionLevelCounts"
                @change="handleSecurityLevelChange"
              />
            </div>
          </div>

          <PermissionTree
            ref="permissionTreeRef"
            :tree-data="filteredPermissions"
            :checkable="true"
            :multiple="true"
            :showSecurityLevel="true"
            :height="300"
            @check="handlePermissionCheck"
          />

          <!-- 已选权限预览 -->
          <div class="selected-permissions" v-if="selectedPermissions.length > 0">
            <a-divider>已选择的权限 ({{ selectedPermissions.length }})</a-divider>
            <div class="permission-tags">
              <a-tag
                v-for="permission in selectedPermissions"
                :key="permission.id"
                :color="getPermissionTypeColor(permission.type)"
                closable
                @close="handleRemovePermission(permission)"
              >
                {{ permission.name }}
                <a-tag
                  :color="getSecurityLevelColor(permission.securityLevel)"
                  size="small"
                  style="margin-left: 4px"
                >
                  {{ getSecurityLevelText(permission.securityLevel) }}
                </a-tag>
              </a-tag>
            </div>
          </div>
        </div>

        <!-- 步骤3: 设置时限 -->
        <div v-show="currentStep === 2" class="step-time">
          <a-form
            ref="timeFormRef"
            :model="timeForm"
            :rules="timeFormRules"
            layout="vertical"
          >
            <a-row :gutter="24">
              <!-- 有效期设置 -->
              <a-col :span="12">
                <a-form-item label="有效期类型" name="expireType">
                  <a-radio-group v-model:value="timeForm.expireType" @change="handleExpireTypeChange">
                    <a-radio value="duration">按时长</a-radio>
                    <a-radio value="specific">指定时间</a-radio>
                    <a-radio value="permanent">永久有效</a-radio>
                  </a-radio-group>
                </a-form-item>
              </a-col>

              <a-col :span="12">
                <a-form-item label="自动撤销" name="autoRevoke">
                  <a-switch
                    v-model:checked="timeForm.autoRevoke"
                    checkedChildren="开启"
                    unCheckedChildren="关闭"
                  />
                  <div class="form-tip">到期后自动撤销权限</div>
                </a-form-item>
              </a-col>
            </a-row>

            <!-- 按时长设置 -->
            <div v-if="timeForm.expireType === 'duration'" class="duration-section">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="时长数值" name="durationValue">
                    <a-input-number
                      v-model:value="timeForm.durationValue"
                      :min="1"
                      :max="365"
                      placeholder="请输入"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="时长单位" name="durationUnit">
                    <a-select v-model:value="timeForm.durationUnit" style="width: 100%">
                      <a-select-option value="hours">小时</a-select-option>
                      <a-select-option value="days">天</a-select-option>
                      <a-select-option value="weeks">周</a-select-option>
                      <a-select-option value="months">月</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="预计过期时间">
                    <a-input :value="calculatedExpireTime" readonly />
                  </a-form-item>
                </a-col>
              </a-row>
            </div>

            <!-- 指定时间设置 -->
            <div v-if="timeForm.expireType === 'specific'" class="specific-section">
              <a-form-item label="过期时间" name="expireTime">
                <a-date-picker
                  v-model:value="timeForm.expireTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  placeholder="选择过期时间"
                  style="width: 100%"
                  :disabledDate="disabledDate"
                />
              </a-form-item>
            </div>

            <!-- 时间策略 -->
            <a-form-item label="时间策略" name="timeStrategyId">
              <a-select
                v-model:value="timeForm.timeStrategyId"
                placeholder="选择时间策略（可选）"
                allowClear
                :options="timeStrategyOptions"
              />
              <div class="form-tip">
                <a-button type="link" size="small" @click="handleCreateTimeStrategy">
                  <PlusOutlined />
                  创建时间策略
                </a-button>
              </div>
            </a-form-item>

            <!-- 地理围栏 -->
            <a-form-item label="地理围栏" name="geoFenceIds">
              <a-select
                v-model:value="timeForm.geoFenceIds"
                mode="multiple"
                placeholder="选择地理围栏（可选）"
                allowClear
                :options="geoFenceOptions"
              />
              <div class="form-tip">
                <a-button type="link" size="small" @click="handleCreateGeoFence">
                  <PlusOutlined />
                  创建地理围栏
                </a-button>
              </div>
            </a-form-item>

            <!-- 分配原因 -->
            <a-form-item label="分配原因" name="reason">
              <a-textarea
                v-model:value="timeForm.reason"
                placeholder="请说明分配临时权限的原因"
                :rows="3"
                show-count
                :maxlength="500"
              />
            </a-form-item>

            <!-- 通知设置 -->
            <a-form-item label="通知设置">
              <a-checkbox-group v-model:value="timeForm.notifications">
                <a-checkbox value="email">
                  <MailOutlined />
                  邮件通知用户
                </a-checkbox>
                <a-checkbox value="sms">
                  <PhoneOutlined />
                  短信通知用户
                </a-checkbox>
                <a-checkbox value="system">
                  <BellOutlined />
                  系统通知
                </a-checkbox>
                <a-checkbox value="warning">
                  <ExclamationCircleOutlined />
                  过期前提醒
                </a-checkbox>
              </a-checkbox-group>
            </a-form-item>
          </a-form>
        </div>

        <!-- 步骤4: 确认配置 -->
        <div v-show="currentStep === 3" class="step-confirm">
          <a-descriptions title="配置确认" :column="2" bordered size="small">
            <!-- 用户信息 -->
            <a-descriptions-item label="选择用户" :span="2">
              <div class="selected-users">
                <a-tag
                  v-for="user in selectedUsers"
                  :key="user.userId"
                  color="blue"
                >
                  <a-avatar :src="user.avatar" :size="16" style="margin-right: 4px">
                    {{ user.userName?.charAt(0) }}
                  </a-avatar>
                  {{ user.userName }}
                </a-tag>
              </div>
            </a-descriptions-item>

            <!-- 权限信息 -->
            <a-descriptions-item label="分配权限" :span="2">
              <div class="selected-permissions-summary">
                <a-tag
                  v-for="permission in selectedPermissions"
                  :key="permission.id"
                  :color="getPermissionTypeColor(permission.type)"
                >
                  {{ permission.name }}
                  <a-tag
                    :color="getSecurityLevelColor(permission.securityLevel)"
                    size="small"
                    style="margin-left: 4px"
                  >
                    {{ getSecurityLevelText(permission.securityLevel) }}
                  </a-tag>
                </a-tag>
              </div>
            </a-descriptions-item>

            <!-- 时间配置 -->
            <a-descriptions-item label="有效期类型">
              {{ getExpireTypeText(timeForm.expireType) }}
            </a-descriptions-item>
            <a-descriptions-item label="过期时间">
              <span :class="{ 'expired-text': isExpired }">
                {{ finalExpireTime }}
              </span>
            </a-descriptions-item>

            <a-descriptions-item label="自动撤销">
              <a-tag :color="timeForm.autoRevoke ? 'green' : 'orange'">
                {{ timeForm.autoRevoke ? '是' : '否' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="通知方式">
              <a-tag
                v-for="notification in timeForm.notifications"
                :key="notification"
                color="blue"
                size="small"
              >
                {{ getNotificationText(notification) }}
              </a-tag>
              <span v-if="timeForm.notifications.length === 0" class="no-notifications">
                无通知
              </span>
            </a-descriptions-item>

            <!-- 原因 -->
            <a-descriptions-item label="分配原因" :span="2">
              {{ timeForm.reason || '无' }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 风险提示 -->
          <div class="risk-warning" v-if="hasHighRiskPermission">
            <a-alert
              type="warning"
              show-icon
              message="高风险权限提示"
              :description="riskWarningText"
            />
          </div>
        </div>
      </div>

      <!-- 步骤操作按钮 -->
      <div class="step-actions">
        <a-button v-if="currentStep > 0" @click="handlePrev">上一步</a-button>
        <a-button v-if="currentStep < 3" type="primary" @click="handleNext" :disabled="!canProceedToNext">
          下一步
        </a-button>
      </div>
    </div>

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
  </a-modal>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  MailOutlined,
  PhoneOutlined,
  BellOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue';
import {
  SecurityLevel,
  PermissionType,
  PermissionStatus,
  TimeStrategyType
} from '/@/types/permission';
import dayjs from 'dayjs';
import UserPermissionSelector from './UserPermissionSelector.vue';
import PermissionTree from './PermissionTree.vue';
import SecurityLevelSelector from './SecurityLevelSelector.vue';
import TimeStrategyModal from './TimeStrategyModal.vue';
import GeoFenceModal from './GeoFenceModal.vue';

// Props定义
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  // 预设数据
  users: {
    type: Array,
    default: () => []
  },
  permissions: {
    type: Array,
    default: () => []
  },
  timeStrategies: {
    type: Array,
    default: () => []
  },
  geoFences: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  // 配置选项
  allowMultipleUsers: {
    type: Boolean,
    default: true
  }
});

// Emits定义
const emit = defineEmits([
  'update:open',
  'confirm',
  'cancel',
  'createTimeStrategy',
  'createGeoFence'
]);

// 响应式数据
const currentStep = ref(0);
const userSelectorRef = ref();
const permissionTreeRef = ref();
const timeFormRef = ref();

// 选择的用户和权限
const selectedUsers = ref([]);
const selectedPermissions = ref([]);

// 搜索和过滤
const userSearchKeyword = ref('');
const permissionFilter = reactive({
  type: null,
  securityLevel: null
});

// 时间表单
const timeForm = reactive({
  expireType: 'duration',
  durationValue: 1,
  durationUnit: 'days',
  expireTime: null,
  autoRevoke: true,
  timeStrategyId: undefined,
  geoFenceIds: [],
  reason: '',
  notifications: ['system', 'warning']
});

// 子弹窗状态
const timeStrategyModalVisible = ref(false);
const geoFenceModalVisible = ref(false);

// 表单验证规则
const timeFormRules = {
  durationValue: [
    { required: true, message: '请输入时长数值', trigger: 'blur' }
  ],
  durationUnit: [
    { required: true, message: '请选择时长单位', trigger: 'change' }
  ],
  expireTime: [
    { required: true, message: '请选择过期时间', trigger: 'change' }
  ],
  reason: [
    { max: 500, message: '分配原因不能超过500个字符', trigger: 'blur' }
  ]
};

// 计算属性
const availableUsers = computed(() => {
  let users = [...props.users];
  if (userSearchKeyword.value) {
    const keyword = userSearchKeyword.value.toLowerCase();
    users = users.filter(user =>
      user.userName?.toLowerCase().includes(keyword) ||
      user.userCode?.toLowerCase().includes(keyword) ||
      user.departmentName?.toLowerCase().includes(keyword)
    );
  }
  return users;
});

const filteredPermissions = computed(() => {
  let permissions = [...props.permissions];

  // 类型过滤
  if (permissionFilter.type) {
    permissions = permissions.filter(p => p.type === permissionFilter.type);
  }

  // 安全级别过滤
  if (permissionFilter.securityLevel) {
    permissions = permissions.filter(p => p.securityLevel === permissionFilter.securityLevel);
  }

  return permissions;
});

const calculatedExpireTime = computed(() => {
  if (timeForm.expireType !== 'duration' || !timeForm.durationValue || !timeForm.durationUnit) {
    return '请设置时长';
  }

  const now = dayjs();
  let expireTime;

  switch (timeForm.durationUnit) {
    case 'hours':
      expireTime = now.add(timeForm.durationValue, 'hour');
      break;
    case 'days':
      expireTime = now.add(timeForm.durationValue, 'day');
      break;
    case 'weeks':
      expireTime = now.add(timeForm.durationValue, 'week');
      break;
    case 'months':
      expireTime = now.add(timeForm.durationValue, 'month');
      break;
    default:
      expireTime = now;
  }

  return expireTime.format('YYYY-MM-DD HH:mm:ss');
});

const finalExpireTime = computed(() => {
  if (timeForm.expireType === 'permanent') {
    return '永久有效';
  }

  if (timeForm.expireType === 'duration') {
    return calculatedExpireTime.value;
  }

  if (timeForm.expireType === 'specific' && timeForm.expireTime) {
    return timeForm.expireTime.format('YYYY-MM-DD HH:mm:ss');
  }

  return '未设置';
});

const isExpired = computed(() => {
  if (timeForm.expireType === 'permanent') return false;

  const expireTime = timeForm.expireType === 'duration'
    ? dayjs(calculatedExpireTime.value)
    : timeForm.expireTime;

  return expireTime && expireTime.isBefore(dayjs());
});

const hasHighRiskPermission = computed(() => {
  return selectedPermissions.value.some(p =>
    p.securityLevel === SecurityLevel.SECRET ||
    p.securityLevel === SecurityLevel.TOP_SECRET
  );
});

const riskWarningText = computed(() => {
  const highRiskCount = selectedPermissions.value.filter(p =>
    p.securityLevel === SecurityLevel.SECRET ||
    p.securityLevel === SecurityLevel.TOP_SECRET
  ).length;

  return `您选择了 ${highRiskCount} 个高风险权限（机密级或绝密级），请谨慎分配临时权限，建议设置较短的有效期并开启自动撤销。`;
});

const canProceedToNext = computed(() => {
  switch (currentStep.value) {
    case 0:
      return selectedUsers.value.length > 0;
    case 1:
      return selectedPermissions.value.length > 0;
    case 2:
      return timeForm.reason.trim().length > 0;
    default:
      return true;
  }
});

const permissionTypeOptions = computed(() => {
  return [
    { value: PermissionType.MENU, label: '菜单权限' },
    { value: PermissionType.BUTTON, label: '按钮权限' },
    { value: PermissionType.API, label: '接口权限' },
    { value: PermissionType.DATA, label: '数据权限' },
    { value: PermissionType.DEVICE, label: '设备权限' },
    { value: PermissionType.AREA, label: '区域权限' },
    { value: PermissionType.TIME, label: '时间权限' }
  ];
});

const permissionLevelCounts = computed(() => {
  const counts = {};
  filteredPermissions.value.forEach(p => {
    counts[p.securityLevel] = (counts[p.securityLevel] || 0) + 1;
  });
  return counts;
});

const timeStrategyOptions = computed(() => {
  return props.timeStrategies.map(strategy => ({
    label: strategy.name,
    value: strategy.id,
    description: strategy.description
  }));
});

const geoFenceOptions = computed(() => {
  return props.geoFences.map(geoFence => ({
    label: geoFence.name,
    value: geoFence.id,
    description: geoFence.description
  }));
});

// 监听器
watch(() => props.open, (newVal) => {
  if (newVal) {
    resetForm();
  }
});

watch(() => timeForm.expireType, (newVal) => {
  if (newVal === 'permanent') {
    timeForm.durationValue = null;
    timeForm.durationUnit = null;
    timeForm.expireTime = null;
  }
});

// 生命周期
onMounted(() => {
  resetForm();
});

// 方法
/**
 * 重置表单
 */
const resetForm = () => {
  currentStep.value = 0;
  selectedUsers.value = [];
  selectedPermissions.value = [];
  userSearchKeyword.value = '';
  permissionFilter.type = null;
  permissionFilter.securityLevel = null;

  Object.assign(timeForm, {
    expireType: 'duration',
    durationValue: 1,
    durationUnit: 'days',
    expireTime: null,
    autoRevoke: true,
    timeStrategyId: undefined,
    geoFenceIds: [],
    reason: '',
    notifications: ['system', 'warning']
  });
};

/**
 * 处理用户搜索
 */
const handleUserSearch = (keyword) => {
  userSearchKeyword.value = keyword;
};

/**
 * 处理用户选择
 */
const handleUserSelect = (users) => {
  if (props.allowMultipleUsers) {
    selectedUsers.value = Array.isArray(users) ? users : [users];
  } else {
    selectedUsers.value = [users];
  }
};

/**
 * 处理权限过滤
 */
const handlePermissionFilter = () => {
  // 权限过滤变化时的处理
};

/**
 * 处理安全级别变化
 */
const handleSecurityLevelChange = (level) => {
  permissionFilter.securityLevel = level;
};

/**
 * 处理权限勾选
 */
const handlePermissionCheck = (checkedKeys, info) => {
  const selectedIds = Array.isArray(checkedKeys) ? checkedKeys : [checkedKeys];

  selectedPermissions.value = selectedIds.map(id => {
    const findPermission = (permissions) => {
      for (const permission of permissions) {
        if (permission.id === id) return permission;
        if (permission.children) {
          const found = findPermission(permission.children);
          if (found) return found;
        }
      }
      return null;
    };

    return findPermission(filteredPermissions.value);
  }).filter(Boolean);
};

/**
 * 移除已选择的权限
 */
const handleRemovePermission = (permission) => {
  const index = selectedPermissions.value.findIndex(p => p.id === permission.id);
  if (index > -1) {
    selectedPermissions.value.splice(index, 1);
  }
};

/**
 * 处理有效期类型变化
 */
const handleExpireTypeChange = (e) => {
  const type = e.target.value;
  if (type === 'permanent') {
    timeForm.autoRevoke = false;
  } else {
    timeForm.autoRevoke = true;
  }
};

/**
 * 禁用过期日期（只能选择未来日期）
 */
const disabledDate = (current) => {
  return current && current < dayjs().startOf('day');
};

/**
 * 处理上一步
 */
const handlePrev = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

/**
 * 处理下一步
 */
const handleNext = async () => {
  if (currentStep.value === 2) {
    try {
      await timeFormRef.value.validate();
      currentStep.value++;
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  } else {
    currentStep.value++;
  }
};

/**
 * 处理确认
 */
const handleConfirm = async () => {
  try {
    if (currentStep.value === 2) {
      await timeFormRef.value.validate();
    }

    const data = {
      users: selectedUsers.value,
      permissions: selectedPermissions.value,
      ...timeForm,
      expireTime: finalExpireTime.value === '永久有效' ? null :
        (timeForm.expireType === 'duration' ? calculatedExpireTime.value : timeForm.expireTime?.format('YYYY-MM-DD HH:mm:ss'))
    };

    emit('confirm', data);
  } catch (error) {
    console.error('确认失败:', error);
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
 * 获取安全级别颜色
 */
const getSecurityLevelColor = (level) => {
  const colorMap = {
    [SecurityLevel.PUBLIC]: 'green',
    [SecurityLevel.INTERNAL]: 'blue',
    [SecurityLevel.CONFIDENTIAL]: 'orange',
    [SecurityLevel.SECRET]: 'red',
    [SecurityLevel.TOP_SECRET]: 'purple'
  };
  return colorMap[level] || 'default';
};

/**
 * 获取安全级别文本
 */
const getSecurityLevelText = (level) => {
  const textMap = {
    [SecurityLevel.PUBLIC]: '公开',
    [SecurityLevel.INTERNAL]: '内部',
    [SecurityLevel.CONFIDENTIAL]: '秘密',
    [SecurityLevel.SECRET]: '机密',
    [SecurityLevel.TOP_SECRET]: '绝密'
  };
  return textMap[level] || '未知';
};

/**
 * 获取有效期类型文本
 */
const getExpireTypeText = (type) => {
  const textMap = {
    'duration': '按时长',
    'specific': '指定时间',
    'permanent': '永久有效'
  };
  return textMap[type] || '未知';
};

/**
 * 获取通知方式文本
 */
const getNotificationText = (notification) => {
  const textMap = {
    'email': '邮件',
    'sms': '短信',
    'system': '系统',
    'warning': '过期提醒'
  };
  return textMap[notification] || '未知';
};

// 暴露方法给父组件
defineExpose({
  resetForm,
  handleConfirm,
  handleCancel
});
</script>

<style scoped lang="less">
.temporary-permission-modal {
  .steps {
    margin-bottom: 24px;
  }

  .step-content {
    min-height: 400px;
    margin-bottom: 24px;

    .step-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 500;
      }

      .permission-filters {
        display: flex;
        gap: 12px;
        align-items: center;
      }
    }

    .step-users,
    .step-permissions,
    .step-time,
    .step-confirm {
      .selected-permissions,
      .selected-permissions-summary {
        .permission-tags {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }

      .selected-users {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }

    .step-time {
      .duration-section,
      .specific-section {
        background-color: #fafafa;
        padding: 16px;
        border-radius: 6px;
        margin-bottom: 16px;
      }

      .form-tip {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-top: 4px;
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .step-confirm {
      .selected-users,
      .selected-permissions-summary {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }

      .no-notifications {
        color: #8c8c8c;
        font-style: italic;
      }

      .expired-text {
        color: #ff4d4f;
        font-weight: 500;
      }

      .risk-warning {
        margin-top: 16px;
      }
    }
  }

  .step-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
  }

  // 表单样式
  :deep(.ant-form) {
    .ant-form-item-label > label {
      font-weight: 500;
    }

    .ant-radio-group {
      display: flex;
      gap: 16px;
    }

    .ant-checkbox-group {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
    }
  }

  // 描述列表样式
  :deep(.ant-descriptions) {
    .ant-descriptions-item-label {
      font-weight: 500;
    }
  }

  // 标签样式
  .ant-tag {
    margin: 0;
  }
}
</style>