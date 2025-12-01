<template>
  <div class="user-permission-overview">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-input-search
            v-model:value="searchText"
            placeholder="搜索用户姓名或用户名"
            @search="handleSearch"
            @pressEnter="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :span="4">
          <a-select
            v-model:value="selectedSecurityLevel"
            placeholder="选择安全级别"
            style="width: 100%"
            allow-clear
            @change="handleSecurityLevelChange"
          >
            <a-select-option
              v-for="level in securityLevels"
              :key="level.levelId"
              :value="level.levelCode"
            >
              {{ level.levelName }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4">
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
        </a-col>
        <a-col :span="4">
          <a-button @click="handleReset">
            <template #icon><ReloadOutlined /></template>
            重置
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        row-key="userId"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 用户头像和基本信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <a-avatar :src="record.avatar" :size="40">
                {{ record.userName?.charAt(0) }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">{{ record.userName }}</div>
                <div class="user-login-name">{{ record.loginName }}</div>
              </div>
            </div>
          </template>

          <!-- 安全级别 -->
          <template v-else-if="column.key === 'securityLevel'">
            <a-tag :color="getSecurityLevelColor(record.securityLevelValue)">
              {{ record.securityLevelName }}
            </a-tag>
          </template>

          <!-- 权限数量 -->
          <template v-else-if="column.key === 'permissionCount'">
            <a-statistic
              :value="record.totalPermissions"
              :value-style="{ fontSize: '16px', fontWeight: 'bold' }"
            >
              <template #suffix>
                <span class="permission-detail">
                  操作: {{ record.totalOperationPermissions }} |
                  数据: {{ record.totalDataPermissions }}
                </span>
              </template>
            </a-statistic>
          </template>

          <!-- 权限状态 -->
          <template v-else-if="column.key === 'permissionStatus'">
            <div class="permission-status">
              <a-tag :color="record.permissionStatus === 'ACTIVE' ? 'green' : 'orange'">
                {{ record.permissionStatus === 'ACTIVE' ? '正常' : '受限' }}
              </a-tag>
              <div class="expire-time" v-if="record.expireTime">
                到期时间: {{ formatDate(record.expireTime) }}
              </div>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewUserDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="manageUserPermission(record)">
                <template #icon><SettingOutlined /></template>
                管理权限
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="grantSecurityLevel(record)">
                      <SafetyOutlined />
                      授予安全级别
                    </a-menu-item>
                    <a-menu-item @click="refreshUserCache(record)">
                      <ReloadOutlined />
                      刷新权限缓存
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="viewPermissionAudit(record)" class="audit-menu-item">
                      <HistoryOutlined />
                      权限审计日志
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 用户详情模态框 -->
    <UserDetailModal
      v-model:visible="detailModalVisible"
      :user="selectedUser"
      @close="handleDetailModalClose"
    />

    <!-- 权限管理模态框 -->
    <PermissionManageModal
      v-model:visible="permissionModalVisible"
      :user="selectedUser"
      @success="handlePermissionSuccess"
      @close="handlePermissionModalClose"
    />

    <!-- 安全级别授予模态框 -->
    <SecurityLevelGrantModal
      v-model:visible="securityLevelModalVisible"
      :user="selectedUser"
      @success="handleSecurityLevelSuccess"
      @close="handleSecurityLevelModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  SettingOutlined,
  DownOutlined,
  SafetyOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue'
import { userPermissionApi } from '@/api/smart-permission'
import { usePermissionStore } from '@/stores/smart-permission'

// 导入子组件
import UserDetailModal from './UserDetailModal.vue'
import PermissionManageModal from './PermissionManageModal.vue'
import SecurityLevelGrantModal from './SecurityLevelGrantModal.vue'

// 事件定义
const emit = defineEmits(['user-select'])

// 状态管理
const permissionStore = usePermissionStore()

// 响应式数据
const searchText = ref('')
const selectedSecurityLevel = ref(null)
const loading = ref(false)
const userList = ref([])
const selectedUser = ref(null)

// 模态框状态
const detailModalVisible = ref(false)
const permissionModalVisible = ref(false)
const securityLevelModalVisible = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列配置
const columns = [
  {
    title: '用户信息',
    key: 'userInfo',
    width: 200,
    fixed: 'left'
  },
  {
    title: '部门',
    dataIndex: 'departmentName',
    key: 'departmentName',
    width: 120
  },
  {
    title: '安全级别',
    key: 'securityLevel',
    width: 100
  },
  {
    title: '权限数量',
    key: 'permissionCount',
    width: 150
  },
  {
    title: '权限状态',
    key: 'permissionStatus',
    width: 120
  },
  {
    title: '最后更新时间',
    dataIndex: 'lastUpdateTime',
    key: 'lastUpdateTime',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right'
  }
]

// 计算属性
const securityLevels = computed(() => permissionStore.securityLevels)

// 方法
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

const handleReset = () => {
  searchText.value = ''
  selectedSecurityLevel.value = null
  pagination.current = 1
  fetchUserList()
}

const handleSecurityLevelChange = () => {
  pagination.current = 1
  fetchUserList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchUserList()
}

const viewUserDetail = (user) => {
  selectedUser.value = user
  detailModalVisible.value = true
}

const manageUserPermission = (user) => {
  selectedUser.value = user
  permissionModalVisible.value = true
  emit('user-select', user)
}

const grantSecurityLevel = (user) => {
  selectedUser.value = user
  securityLevelModalVisible.value = true
}

const refreshUserCache = async (user) => {
  try {
    await permissionStore.clearUserCache()
    message.success('权限缓存刷新成功')
    fetchUserList()
  } catch (error) {
    message.error('权限缓存刷新失败')
  }
}

const viewPermissionAudit = (user) => {
  // 跳转到权限审计页面
  // router.push(`/smart-permission/audit?userId=${user.userId}`)
}

const handleDetailModalClose = () => {
  detailModalVisible.value = false
  selectedUser.value = null
}

const handlePermissionModalClose = () => {
  permissionModalVisible.value = false
  selectedUser.value = null
}

const handleSecurityLevelModalClose = () => {
  securityLevelModalVisible.value = false
  selectedUser.value = null
}

const handlePermissionSuccess = () => {
  permissionModalVisible.value = false
  fetchUserList()
  message.success('权限管理操作成功')
}

const handleSecurityLevelSuccess = () => {
  securityLevelModalVisible.value = false
  fetchUserList()
  message.success('安全级别授予成功')
}

const getSecurityLevelColor = (levelValue) => {
  const colors = {
    1: 'blue',    // 公开级
    2: 'green',   // 内部级
    3: 'orange',  // 秘密级
    4: 'red',     // 机密级
    5: 'purple'   // 绝密级
  }
  return colors[levelValue] || 'default'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString()
}

const fetchUserList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      securityLevel: selectedSecurityLevel.value
    }

    const response = await userPermissionApi.getUserList(params)

    if (response.data) {
      userList.value = response.data.records || []
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(async () => {
  await permissionStore.fetchSecurityLevels()
  await fetchUserList()
})
</script>

<style lang="less" scoped>
.user-permission-overview {
  .search-bar {
    margin-bottom: 16px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .user-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-details {
        .user-name {
          font-weight: 500;
          font-size: 14px;
          color: #262626;
        }

        .user-login-name {
          font-size: 12px;
          color: #8c8c8c;
          margin-top: 2px;
        }
      }
    }

    .permission-status {
      .expire-time {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 4px;
      }
    }

    .permission-detail {
      font-size: 12px;
      color: #8c8c8c;
      font-weight: normal;
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background-color: #f5f5f5;
    }
  }
}

// 模态框菜单项样式
:deep(.audit-menu-item) {
  color: #1890ff;
}
</style>