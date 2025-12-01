<template>
  <div class="operation-permission-assignment">
    <!-- 权限类型选择 -->
    <div class="permission-type-selector">
      <a-card title="权限类型" size="small">
        <a-row :gutter="16">
          <a-col :span="6" v-for="type in permissionTypes" :key="type.value">
            <a-card
              :class="['permission-type-card', { active: selectedType === type.value }]"
              hoverable
              @click="selectPermissionType(type.value)"
            >
              <div class="permission-type-content">
                <div class="permission-type-icon">
                  <component :is="type.icon" />
                </div>
                <div class="permission-type-info">
                  <div class="permission-type-name">{{ type.name }}</div>
                  <div class="permission-type-desc">{{ type.description }}</div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 权限操作列表 -->
    <div class="permission-operations" v-if="selectedType">
      <a-card :title="getCurrentTypeInfo().name + '权限操作'">
        <template #extra>
          <a-space>
            <a-button @click="refreshOperations">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
            <a-button type="primary" @click="showBatchGrantModal">
              <template #icon><PlusOutlined /></template>
              批量授予
            </a-button>
          </a-space>
        </template>

        <a-table
          :columns="operationColumns"
          :data-source="operationList"
          :loading="loading"
          :pagination="false"
          row-key="operationId"
        >
          <!-- 权限状态 -->
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-switch
                :checked="record.granted"
                :loading="record.switchLoading"
                @change="(checked) => handlePermissionChange(record, checked)"
              />
            </template>

            <!-- 权限详情 -->
            <template v-else-if="column.key === 'details'">
              <div class="permission-details">
                <div class="permission-name">{{ record.operationName }}</div>
                <div class="permission-code">{{ record.operationCode }}</div>
                <div class="permission-desc">{{ record.operationDesc }}</div>
              </div>
            </template>

            <!-- 风险级别 -->
            <template v-else-if="column.key === 'riskLevel'">
              <a-progress
                :percent="record.riskLevel"
                size="small"
                :stroke-color="getRiskColor(record.riskLevel)"
                :show-info="false"
              />
              <span class="risk-text">{{ record.riskLevel }}分</span>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'actions'">
              <a-space>
                <a-button
                  type="link"
                  size="small"
                  @click="showGrantModal(record)"
                  :disabled="record.granted"
                >
                  授予
                </a-button>
                <a-button
                  type="link"
                  size="small"
                  danger
                  @click="showRevokeModal(record)"
                  :disabled="!record.granted"
                >
                  撤销
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 授予权限模态框 -->
    <GrantPermissionModal
      v-model:visible="grantModalVisible"
      :operation="selectedOperation"
      :user="props.user"
      @success="handleGrantSuccess"
    />

    <!-- 撤销权限模态框 -->
    <RevokePermissionModal
      v-model:visible="revokeModalVisible"
      :operation="selectedOperation"
      :user="props.user"
      @success="handleRevokeSuccess"
    />

    <!-- 批量授予模态框 -->
    <BatchGrantModal
      v-model:visible="batchGrantModalVisible"
      :user="props.user"
      :permission-type="selectedType"
      @success="handleBatchGrantSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  PlusOutlined,
  AreaChartOutlined,
  ControlOutlined,
  CalendarOutlined,
  KeyOutlined
} from '@ant-design/icons-vue'

import { permissionApi } from '@/api/smart-permission'

// 导入子组件
import GrantPermissionModal from './modals/GrantPermissionModal.vue'
import RevokePermissionModal from './modals/RevokePermissionModal.vue'
import BatchGrantModal from './modals/BatchGrantModal.vue'

// Props 定义
const props = defineProps({
  userId: {
    type: Number,
    required: true
  },
  user: {
    type: Object,
    required: true
  }
})

// 事件定义
const emit = defineEmits(['success'])

// 响应式数据
const selectedType = ref('')
const operationList = ref([])
const loading = ref(false)

// 模态框状态
const grantModalVisible = ref(false)
const revokeModalVisible = ref(false)
const batchGrantModalVisible = ref(false)
const selectedOperation = ref(null)

// 权限类型配置
const permissionTypes = [
  {
    value: 'area',
    name: '区域权限',
    description: '区域访问和管理权限',
    icon: AreaChartOutlined,
    operations: ['AREA_ACCESS', 'AREA_MANAGE', 'AREA_CONFIG']
  },
  {
    value: 'device',
    name: '设备权限',
    description: '设备查看和控制权限',
    icon: ControlOutlined,
    operations: ['DEVICE_VIEW', 'DEVICE_CONTROL', 'DEVICE_CONFIG']
  },
  {
    value: 'attendance',
    name: '考勤权限',
    description: '考勤查看和管理权限',
    icon: CalendarOutlined,
    operations: ['ATTENDANCE_VIEW', 'ATTENDANCE_MANAGE', 'ATTENDANCE_EXPORT']
  },
  {
    value: 'access',
    name: '门禁权限',
    description: '门禁进入和管理权限',
    icon: KeyOutlined,
    operations: ['ACCESS_ENTER', 'ACCESS_MANAGE', 'ACCESS_CONFIG']
  }
]

// 表格列配置
const operationColumns = [
  {
    title: '权限状态',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '权限详情',
    key: 'details',
    width: 300
  },
  {
    title: '风险级别',
    key: 'riskLevel',
    width: 120
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    align: 'center'
  }
]

// 计算属性
const getCurrentTypeInfo = () => {
  return permissionTypes.find(type => type.value === selectedType.value) || {}
}

// 方法定义
const selectPermissionType = (type) => {
  selectedType.value = type
  fetchOperations(type)
}

const refreshOperations = () => {
  if (selectedType.value) {
    fetchOperations(selectedType.value)
  }
}

const fetchOperations = async (type) => {
  if (!type || !props.userId) return

  try {
    loading.value = true
    const typeInfo = permissionTypes.find(t => t.value === type)

    // 模拟获取操作权限列表
    // 实际应该调用API获取用户的当前权限状态
    const mockOperations = typeInfo.operations.map(op => ({
      operationId: op,
      operationName: getOperationName(op),
      operationCode: op,
      operationDesc: getOperationDesc(op),
      riskLevel: getRiskLevel(op),
      granted: Math.random() > 0.5, // 模拟已授予状态
      switchLoading: false
    }))

    operationList.value = mockOperations
  } catch (error) {
    message.error('获取权限操作列表失败')
    console.error('获取权限操作列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePermissionChange = async (operation, checked) => {
  operation.switchLoading = true

  try {
    if (checked) {
      // 授予权限
      await grantPermission(operation)
    } else {
      // 撤销权限
      await revokePermission(operation)
    }

    operation.granted = checked
    emit('success', checked ? '权限授予成功' : '权限撤销成功')
  } catch (error) {
    // 恢复原状态
    setTimeout(() => {
      operation.granted = !checked
    }, 100)
  } finally {
    operation.switchLoading = false
  }
}

const showGrantModal = (operation) => {
  selectedOperation.value = operation
  grantModalVisible.value = true
}

const showRevokeModal = (operation) => {
  selectedOperation.value = operation
  revokeModalVisible.value = true
}

const showBatchGrantModal = () => {
  batchGrantModalVisible.value = true
}

const handleGrantSuccess = () => {
  grantModalVisible.value = false
  refreshOperations()
}

const handleRevokeSuccess = () => {
  revokeModalVisible.value = false
  refreshOperations()
}

const handleBatchGrantSuccess = () => {
  batchGrantModalVisible.value = false
  refreshOperations()
}

const grantPermission = async (operation) => {
  // 实际调用权限授予API
  console.log('授予权限:', operation.operationCode)
}

const revokePermission = async (operation) => {
  // 实际调用权限撤销API
  console.log('撤销权限:', operation.operationCode)
}

const getOperationName = (code) => {
  const nameMap = {
    'AREA_ACCESS': '区域访问',
    'AREA_MANAGE': '区域管理',
    'AREA_CONFIG': '区域配置',
    'DEVICE_VIEW': '设备查看',
    'DEVICE_CONTROL': '设备控制',
    'DEVICE_CONFIG': '设备配置',
    'ATTENDANCE_VIEW': '考勤查看',
    'ATTENDANCE_MANAGE': '考勤管理',
    'ATTENDANCE_EXPORT': '考勤导出',
    'ACCESS_ENTER': '门禁进入',
    'ACCESS_MANAGE': '门禁管理',
    'ACCESS_CONFIG': '门禁配置'
  }
  return nameMap[code] || code
}

const getOperationDesc = (code) => {
  const descMap = {
    'AREA_ACCESS': '访问指定区域的基础权限',
    'AREA_MANAGE': '管理区域信息的增删改权限',
    'AREA_CONFIG': '配置区域参数的高级权限',
    'DEVICE_VIEW': '查看设备信息和状态',
    'DEVICE_CONTROL': '控制设备操作和执行命令',
    'DEVICE_CONFIG': '配置设备参数和设置',
    'ATTENDANCE_VIEW': '查看考勤记录和统计',
    'ATTENDANCE_MANAGE': '管理考勤数据和规则',
    'ATTENDANCE_EXPORT': '导出考勤报表和分析',
    'ACCESS_ENTER': '门禁进入验证权限',
    'ACCESS_MANAGE': '门禁系统管理权限',
    'ACCESS_CONFIG': '门禁参数配置权限'
  }
  return descMap[code] || '权限描述'
}

const getRiskLevel = (code) => {
  const riskMap = {
    'AREA_ACCESS': 20,
    'AREA_MANAGE': 50,
    'AREA_CONFIG': 80,
    'DEVICE_VIEW': 20,
    'DEVICE_CONTROL': 60,
    'DEVICE_CONFIG': 90,
    'ATTENDANCE_VIEW': 25,
    'ATTENDANCE_MANAGE': 55,
    'ATTENDANCE_EXPORT': 45,
    'ACCESS_ENTER': 15,
    'ACCESS_MANAGE': 70,
    'ACCESS_CONFIG': 95
  }
  return riskMap[code] || 30
}

const getRiskColor = (level) => {
  if (level <= 30) return '#52c41a'
  if (level <= 60) return '#faad14'
  return '#f5222d'
}

// 监听器
watch(() => props.userId, (newUserId) => {
  if (newUserId && selectedType.value) {
    fetchOperations(selectedType.value)
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  if (permissionTypes.length > 0) {
    selectPermissionType(permissionTypes[0].value)
  }
})
</script>

<style lang="less" scoped>
.operation-permission-assignment {
  .permission-type-selector {
    margin-bottom: 16px;

    .permission-type-card {
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.active {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }

      .permission-type-content {
        display: flex;
        align-items: center;
        gap: 12px;

        .permission-type-icon {
          font-size: 24px;
          color: #1890ff;
        }

        .permission-type-info {
          .permission-type-name {
            font-weight: 500;
            font-size: 14px;
            color: #262626;
            margin-bottom: 4px;
          }

          .permission-type-desc {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }
  }

  .permission-operations {
    .permission-details {
      .permission-name {
        font-weight: 500;
        font-size: 14px;
        color: #262626;
        margin-bottom: 4px;
      }

      .permission-code {
        font-family: 'Monaco', 'Menlo', monospace;
        font-size: 12px;
        color: #1890ff;
        background: #f6ffed;
        padding: 2px 6px;
        border-radius: 4px;
        display: inline-block;
        margin-bottom: 4px;
      }

      .permission-desc {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .risk-text {
      font-size: 12px;
      color: #8c8c8c;
      margin-left: 8px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .operation-permission-assignment {
    .permission-type-selector {
      .ant-row {
        .ant-col {
          margin-bottom: 12px;
        }
      }

      .permission-type-card {
        .permission-type-content {
          flex-direction: column;
          text-align: center;
        }
      }
    }
  }
}
</style>