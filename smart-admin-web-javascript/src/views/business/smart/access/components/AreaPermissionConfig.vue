<template>
  <div class="area-permission-config">
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <a-card title="区域权限配置" size="small">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-input-search
              v-model:value="searchText"
              placeholder="搜索区域名称或编号"
              @search="handleSearch"
              allow-clear
            />
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedAreaLevel"
              placeholder="选择区域级别"
              style="width: 100%"
              allow-clear
              @change="handleAreaLevelChange"
            >
              <a-select-option :value="1">1级 - 公开区域</a-select-option>
              <a-select-option :value="2">2级 - 内部区域</a-select-option>
              <a-select-option :value="3">3级 - 受限区域</a-select-option>
              <a-select-option :value="4">4级 - 机密区域</a-select-option>
              <a-select-option :value="5">5级 - 绝密区域</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="selectedPermissionType"
              placeholder="选择权限类型"
              style="width: 100%"
              allow-clear
              @change="handlePermissionTypeChange"
            >
              <a-select-option value="access">访问权限</a-select-option>
              <a-select-option value="manage">管理权限</a-select-option>
              <a-select-option value="config">配置权限</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="10">
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button type="primary" @click="showBatchConfigModal">
                <template #icon><SettingOutlined /></template>
                批量配置
              </a-button>
              <a-button @click="exportConfig">
                <template #icon><ExportOutlined /></template>
                导出配置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 区域权限列表 -->
    <div class="area-permission-list">
      <a-table
        :columns="columns"
        :data-source="areaList"
        :loading="loading"
        :pagination="pagination"
        row-key="areaId"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
      >
        <!-- 区域信息 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'areaInfo'">
            <div class="area-info">
              <div class="area-header">
                <div class="area-code">{{ record.areaCode }}</div>
                <a-tag :color="getAreaLevelColor(record.areaLevel)">
                  {{ getAreaLevelText(record.areaLevel) }}
                </a-tag>
              </div>
              <div class="area-name">{{ record.areaName }}</div>
              <div class="area-description">{{ record.description }}</div>
            </div>
          </template>

          <!-- 权限配置 -->
          <template v-else-if="column.key === 'permissions'">
            <div class="permission-config">
              <div class="permission-item">
                <span class="permission-label">访问:</span>
                <a-switch
                  :checked="record.hasAccessPermission"
                  :loading="record.accessLoading"
                  @change="(checked) => handlePermissionChange(record, 'ACCESS', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">管理:</span>
                <a-switch
                  :checked="record.hasManagePermission"
                  :loading="record.manageLoading"
                  @change="(checked) => handlePermissionChange(record, 'MANAGE', checked)"
                />
              </div>
              <div class="permission-item">
                <span class="permission-label">配置:</span>
                <a-switch
                  :checked="record.hasConfigPermission"
                  :loading="record.configLoading"
                  @change="(checked) => handlePermissionChange(record, 'CONFIG', checked)"
                />
              </div>
            </div>
          </template>

          <!-- 时间限制 -->
          <template v-else-if="column.key === 'timeRestriction'">
            <div class="time-restriction">
              <div class="restriction-item">
                <span class="restriction-label">工作时间:</span>
                <a-switch
                  :checked="record.timeRestriction.workHours"
                  @change="(checked) => handleTimeRestrictionChange(record, 'workHours', checked)"
                />
              </div>
              <div class="restriction-item">
                <span class="restriction-label">周末:</span>
                <a-switch
                  :checked="record.timeRestriction.weekend"
                  @change="(checked) => handleTimeRestrictionChange(record, 'weekend', checked)"
                />
              </div>
              <div class="restriction-item">
                <span class="restriction-label">节假日:</span>
                <a-switch
                  :checked="record.timeRestriction.holiday"
                  @change="(checked) => handleTimeRestrictionChange(record, 'holiday', checked)"
                />
              </div>
            </div>
          </template>

          <!-- IP限制 -->
          <template v-else-if="column.key === 'ipRestriction'">
            <div class="ip-restriction">
              <div class="ip-list" v-if="record.ipRestrictions && record.ipRestrictions.length">
                <a-tag
                  v-for="(ip, index) in record.ipRestrictions"
                  :key="index"
                  closable
                  @close="removeIpRestriction(record, index)"
                >
                  {{ ip }}
                </a-tag>
              </div>
              <a-button
                type="dashed"
                size="small"
                @click="showIpRestrictionModal(record)"
              >
                <template #icon><PlusOutlined /></template>
                添加IP
              </a-button>
            </div>
          </template>

          <!-- 操作 -->
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="viewAreaDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="editAreaPermission(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="showUserPermissionModal(record)">
                      <UserOutlined />
                      用户权限
                    </a-menu-item>
                    <a-menu-item @click="syncAreaPermission(record)">
                      <SyncOutlined />
                      同步权限
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="resetAreaPermission(record)" class="danger-menu-item">
                      <RollbackOutlined />
                      重置权限
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

    <!-- 区域详情模态框 -->
    <AreaDetailModal
      v-model:visible="detailModalVisible"
      :area="selectedArea"
      @close="handleDetailModalClose"
    />

    <!-- 编辑权限模态框 -->
    <EditAreaPermissionModal
      v-model:visible="editModalVisible"
      :area="selectedArea"
      @success="handleEditSuccess"
      @close="handleEditModalClose"
    />

    <!-- IP限制模态框 -->
    <IpRestrictionModal
      v-model:visible="ipModalVisible"
      :area="selectedArea"
      @success="handleIpSuccess"
      @close="handleIpModalClose"
    />

    <!-- 用户权限模态框 -->
    <UserPermissionModal
      v-model:visible="userPermissionModalVisible"
      :area="selectedArea"
      @close="handleUserPermissionModalClose"
    />

    <!-- 批量配置模态框 -->
    <BatchConfigModal
      v-model:visible="batchConfigModalVisible"
      @success="handleBatchConfigSuccess"
      @close="handleBatchConfigModalClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  SettingOutlined,
  ExportOutlined,
  EyeOutlined,
  EditOutlined,
  UserOutlined,
  SyncOutlined,
  RollbackOutlined,
  DownOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'

import { businessPermissionApi } from '@/api/smart-permission'

// 导入子组件
import AreaDetailModal from './modals/AreaDetailModal.vue'
import EditAreaPermissionModal from './modals/EditAreaPermissionModal.vue'
import IpRestrictionModal from './modals/IpRestrictionModal.vue'
import UserPermissionModal from './modals/UserPermissionModal.vue'
import BatchConfigModal from './modals/BatchConfigModal.vue'

// 响应式数据
const searchText = ref('')
const selectedAreaLevel = ref(null)
const selectedPermissionType = ref(null)
const loading = ref(false)
const areaList = ref([])
const selectedArea = ref(null)

// 模态框状态
const detailModalVisible = ref(false)
const editModalVisible = ref(false)
const ipModalVisible = ref(false)
const userPermissionModalVisible = ref(false)
const batchConfigModalVisible = ref(false)

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
    title: '区域信息',
    key: 'areaInfo',
    width: 250,
    fixed: 'left'
  },
  {
    title: '权限配置',
    key: 'permissions',
    width: 200
  },
  {
    title: '时间限制',
    key: 'timeRestriction',
    width: 200
  },
  {
    title: 'IP限制',
    key: 'ipRestriction',
    width: 250
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '最后更新',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 150
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 方法定义
const handleSearch = () => {
  pagination.current = 1
  fetchAreaList()
}

const handleReset = () => {
  searchText.value = ''
  selectedAreaLevel.value = null
  selectedPermissionType.value = null
  pagination.current = 1
  fetchAreaList()
}

const handleAreaLevelChange = () => {
  pagination.current = 1
  fetchAreaList()
}

const handlePermissionTypeChange = () => {
  pagination.current = 1
  fetchAreaList()
}

const handleTableChange = (paginationInfo) => {
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  fetchAreaList()
}

const handlePermissionChange = async (area, permissionType, checked) => {
  const loadingKey = `${permissionType.toLowerCase()}Loading`
  area[loadingKey] = true

  try {
    const requestData = {
      areaId: area.areaId,
      permissionType,
      enabled: checked
    }

    // 调用API更新权限
    await businessPermissionApi.updateAreaPermissionConfig(requestData)

    // 更新本地状态
    switch (permissionType) {
      case 'ACCESS':
        area.hasAccessPermission = checked
        break
      case 'MANAGE':
        area.hasManagePermission = checked
        break
      case 'CONFIG':
        area.hasConfigPermission = checked
        break
    }

    message.success(`${getPermissionText(permissionType)}权限${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('权限配置更新失败')
    console.error('权限配置更新失败:', error)
  } finally {
    area[loadingKey] = false
  }
}

const handleTimeRestrictionChange = async (area, restrictionType, checked) => {
  try {
    const requestData = {
      areaId: area.areaId,
      timeRestrictions: {
        ...area.timeRestriction,
        [restrictionType]: checked
      }
    }

    await businessPermissionApi.updateAreaPermissionConfig(requestData)
    area.timeRestriction[restrictionType] = checked

    message.success(`时间限制${checked ? '启用' : '禁用'}成功`)
  } catch (error) {
    message.error('时间限制配置失败')
    console.error('时间限制配置失败:', error)
  }
}

const removeIpRestriction = async (area, index) => {
  try {
    const newIpRestrictions = [...area.ipRestrictions]
    newIpRestrictions.splice(index, 1)

    const requestData = {
      areaId: area.areaId,
      ipRestrictions: newIpRestrictions
    }

    await businessPermissionApi.updateAreaPermissionConfig(requestData)
    area.ipRestrictions = newIpRestrictions

    message.success('IP限制删除成功')
  } catch (error) {
    message.error('IP限制删除失败')
    console.error('IP限制删除失败:', error)
  }
}

const showIpRestrictionModal = (area) => {
  selectedArea.value = area
  ipModalVisible.value = true
}

const viewAreaDetail = (area) => {
  selectedArea.value = area
  detailModalVisible.value = true
}

const editAreaPermission = (area) => {
  selectedArea.value = area
  editModalVisible.value = true
}

const showUserPermissionModal = (area) => {
  selectedArea.value = area
  userPermissionModalVisible.value = true
}

const syncAreaPermission = async (area) => {
  try {
    await businessPermissionApi.syncAreaPermission(area.areaId)
    message.success('区域权限同步成功')
    fetchAreaList()
  } catch (error) {
    message.error('区域权限同步失败')
    console.error('区域权限同步失败:', error)
  }
}

const resetAreaPermission = async (area) => {
  try {
    // 重置权限配置到默认状态
    const requestData = {
      areaId: area.areaId,
      hasAccessPermission: false,
      hasManagePermission: false,
      hasConfigPermission: false,
      timeRestriction: {
        workHours: true,
        weekend: false,
        holiday: false
      },
      ipRestrictions: []
    }

    await businessPermissionApi.updateAreaPermissionConfig(requestData)

    // 更新本地状态
    Object.assign(area, requestData)
    message.success('区域权限重置成功')
  } catch (error) {
    message.error('区域权限重置失败')
    console.error('区域权限重置失败:', error)
  }
}

const showBatchConfigModal = () => {
  batchConfigModalVisible.value = true
}

const exportConfig = async () => {
  try {
    const response = await businessPermissionApi.exportAreaPermissionConfig({
      searchText: searchText.value,
      areaLevel: selectedAreaLevel.value,
      permissionType: selectedPermissionType.value
    })

    // 处理文件下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `area-permission-config-${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    message.success('配置导出成功')
  } catch (error) {
    message.error('配置导出失败')
    console.error('配置导出失败:', error)
  }
}

const handleDetailModalClose = () => {
  detailModalVisible.value = false
  selectedArea.value = null
}

const handleEditModalClose = () => {
  editModalVisible.value = false
  selectedArea.value = null
}

const handleIpModalClose = () => {
  ipModalVisible.value = false
  selectedArea.value = null
}

const handleUserPermissionModalClose = () => {
  userPermissionModalVisible.value = false
  selectedArea.value = null
}

const handleEditSuccess = () => {
  editModalVisible.value = false
  fetchAreaList()
  message.success('区域权限编辑成功')
}

const handleIpSuccess = () => {
  ipModalVisible.value = false
  fetchAreaList()
  message.success('IP限制配置成功')
}

const handleBatchConfigSuccess = () => {
  batchConfigModalVisible.value = false
  fetchAreaList()
  message.success('批量配置成功')
}

const fetchAreaList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      searchText: searchText.value,
      areaLevel: selectedAreaLevel.value,
      permissionType: selectedPermissionType.value
    }

    const response = await businessPermissionApi.getAreaPermissionConfig(params)

    if (response.data) {
      areaList.value = response.data.records.map(area => ({
        ...area,
        accessLoading: false,
        manageLoading: false,
        configLoading: false,
        timeRestriction: area.timeRestriction || {
          workHours: true,
          weekend: false,
          holiday: false
        },
        ipRestrictions: area.ipRestrictions || []
      }))
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    message.error('获取区域权限配置失败')
    console.error('获取区域权限配置失败:', error)
  } finally {
    loading.value = false
  }
}

const getAreaLevelColor = (level) => {
  const colors = {
    1: 'blue',    // 公开区域
    2: 'green',   // 内部区域
    3: 'orange',  // 受限区域
    4: 'red',     // 机密区域
    5: 'purple'   // 绝密区域
  }
  return colors[level] || 'default'
}

const getAreaLevelText = (level) => {
  const textMap = {
    1: '公开',
    2: '内部',
    3: '受限',
    4: '机密',
    5: '绝密'
  }
  return textMap[level] || '未知'
}

const getPermissionText = (permissionType) => {
  const textMap = {
    'ACCESS': '访问',
    'MANAGE': '管理',
    'CONFIG': '配置'
  }
  return textMap[permissionType] || permissionType
}

// 生命周期
onMounted(() => {
  fetchAreaList()
})
</script>

<style lang="less" scoped>
.area-permission-config {
  .search-bar {
    margin-bottom: 16px;
  }

  .area-permission-list {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .area-info {
      .area-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .area-code {
          font-weight: 500;
          font-size: 14px;
          color: #1890ff;
        }
      }

      .area-name {
        font-weight: 500;
        font-size: 16px;
        color: #262626;
        margin-bottom: 4px;
      }

      .area-description {
        font-size: 12px;
        color: #8c8c8c;
      }
    }

    .permission-config {
      .permission-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .permission-label {
          width: 50px;
          font-size: 12px;
          color: #595959;
        }
      }
    }

    .time-restriction {
      .restriction-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;

        .restriction-label {
          width: 60px;
          font-size: 12px;
          color: #595959;
        }
      }
    }

    .ip-restriction {
      .ip-list {
        margin-bottom: 8px;

        .ant-tag {
          margin-right: 4px;
          margin-bottom: 4px;
        }
      }
    }

    :deep(.ant-table-tbody > tr:hover > td) {
      background-color: #f5f5f5;
    }
  }
}

.danger-menu-item {
  color: #f5222d;
}
</style>