<template>
  <div class="permission-assignment">
    <!-- 用户选择 -->
    <div class="user-selector">
      <a-card title="选择用户" size="small">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-select
              v-model:value="selectedUserId"
              placeholder="选择用户"
              style="width: 100%"
              show-search
              :filter-option="filterUserOption"
              @change="handleUserChange"
            >
              <a-select-option
                v-for="user in userList"
                :key="user.userId"
                :value="user.userId"
              >
                {{ user.userName }} ({{ user.loginName }})
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="16">
            <div class="user-info-display" v-if="selectedUser">
              <a-avatar :src="selectedUser.avatar" :size="32">
                {{ selectedUser.userName?.charAt(0) }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">{{ selectedUser.userName }}</div>
                <div class="user-meta">
                  <a-tag :color="getSecurityLevelColor(selectedUser.securityLevelValue)">
                    {{ selectedUser.securityLevelName }}
                  </a-tag>
                  <span class="department">{{ selectedUser.departmentName }}</span>
                </div>
              </div>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 权限分配选项卡 -->
    <div class="permission-tabs" v-if="selectedUserId">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 操作权限分配 -->
        <a-tab-pane key="operation" tab="操作权限分配">
          <OperationPermissionAssignment
            :user-id="selectedUserId"
            :user="selectedUser"
            @success="handleAssignmentSuccess"
          />
        </a-tab-pane>

        <!-- 数据权限分配 -->
        <a-tab-pane key="data" tab="数据权限分配">
          <DataPermissionAssignment
            :user-id="selectedUserId"
            :user="selectedUser"
            @success="handleAssignmentSuccess"
          />
        </a-tab-pane>

        <!-- 安全级别管理 -->
        <a-tab-pane key="security-level" tab="安全级别管理">
          <SecurityLevelManagement
            :user-id="selectedUserId"
            :user="selectedUser"
            @success="handleAssignmentSuccess"
          />
        </a-tab-pane>

        <!-- 临时权限申请 -->
        <a-tab-pane key="temporary" tab="临时权限申请">
          <TemporaryPermissionApplication
            :user-id="selectedUserId"
            :user="selectedUser"
            @success="handleAssignmentSuccess"
          />
        </a-tab-pane>

        <!-- 批量操作 -->
        <a-tab-pane key="batch" tab="批量操作">
          <BatchPermissionOperations
            :user-id="selectedUserId"
            :user="selectedUser"
            @success="handleAssignmentSuccess"
          />
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 未选择用户时的提示 -->
    <div v-else class="no-user-selected">
      <a-empty description="请先选择用户">
        <template #image>
          <UserOutlined style="font-size: 64px; color: #d9d9d9;" />
        </template>
      </a-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { userPermissionApi } from '@/api/smart-permission'

// 导入子组件
import OperationPermissionAssignment from './assignment/OperationPermissionAssignment.vue'
import DataPermissionAssignment from './assignment/DataPermissionAssignment.vue'
import SecurityLevelManagement from './assignment/SecurityLevelManagement.vue'
import TemporaryPermissionApplication from './assignment/TemporaryPermissionApplication.vue'
import BatchPermissionOperations from './assignment/BatchPermissionOperations.vue'

// 响应式数据
const selectedUserId = ref(null)
const activeTab = ref('operation')
const userList = ref([])
const loading = ref(false)

// 计算属性
const selectedUser = computed(() => {
  return userList.value.find(user => user.userId === selectedUserId.value)
})

// 方法定义
const filterUserOption = (input, option) => {
  const user = userList.value.find(u => u.userId === option.value)
  if (!user) return false

  const searchText = input.toLowerCase()
  return user.userName.toLowerCase().includes(searchText) ||
         user.loginName.toLowerCase().includes(searchText) ||
         (user.departmentName && user.departmentName.toLowerCase().includes(searchText))
}

const handleUserChange = (userId) => {
  if (userId) {
    console.log('选择用户:', userId)
    activeTab.value = 'operation' // 切换到操作权限分配
  }
}

const handleTabChange = (key) => {
  console.log('切换到选项卡:', key)
}

const handleAssignmentSuccess = (message) => {
  // 权限分配成功后的处理
  if (typeof message === 'string') {
    message.success(message)
  }

  // 可以刷新用户信息
  fetchUserList()
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

const fetchUserList = async () => {
  try {
    loading.value = true
    const response = await userPermissionApi.getUserList({
      page: 1,
      pageSize: 1000 // 获取所有用户用于选择
    })

    if (response.data) {
      userList.value = response.data.records || []
    }
  } catch (error) {
    message.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchUserList()
})
</script>

<style lang="less" scoped>
.permission-assignment {
  .user-selector {
    margin-bottom: 16px;

    .user-info-display {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;

      .user-details {
        .user-name {
          font-weight: 500;
          font-size: 16px;
          color: #262626;
          margin-bottom: 4px;
        }

        .user-meta {
          display: flex;
          align-items: center;
          gap: 8px;

          .department {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }
  }

  .permission-tabs {
    background: #fff;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.ant-tabs-content-holder) {
      padding-top: 0;
    }
  }

  .no-user-selected {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .permission-assignment {
    .user-selector {
      .ant-row {
        .ant-col {
          margin-bottom: 8px;
        }
      }

      .user-info-display {
        flex-direction: column;
        text-align: center;
      }
    }
  }
}
</style>