<template>
  <div class="area-list-container">
    <!-- 搜索区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form
        :model="searchForm"
        layout="inline"
        @finish="handleSearch"
        class="search-form"
      >
        <a-form-item label="区域名称">
          <a-input
            v-model:value="searchForm.areaName"
            placeholder="请输入区域名称"
            allowClear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="区域类型">
          <a-select
            v-model:value="searchForm.areaType"
            placeholder="请选择区域类型"
            allowClear
            style="width: 150px"
          >
            <a-select-option value="REGION">大区</a-select-option>
            <a-select-option value="PROVINCE">省份</a-select-option>
            <a-select-option value="CITY">城市</a-select-option>
            <a-select-option value="DISTRICT">区县</a-select-option>
            <a-select-option value="BUILDING">建筑</a-select-option>
            <a-select-option value="FLOOR">楼层</a-select-option>
            <a-select-option value="ROOM">房间</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allowClear
            style="width: 120px"
          >
            <a-select-option value="ACTIVE">正常</a-select-option>
            <a-select-option value="INACTIVE">停用</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>

        <a-form-item style="margin-left: auto">
          <a-space>
            <a-button type="primary" @click="showAddModal">
              <template #icon><PlusOutlined /></template>
              新增区域
            </a-button>
            <a-button @click="exportData">
              <template #icon><ExportOutlined /></template>
              导出
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 数据表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        row-key="areaId"
        @change="handleTableChange"
        size="middle"
      >
        <!-- 区域名称列 -->
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.key === 'areaName'">
            <a-space>
              <span>{{ text }}</span>
              <a-tag v-if="record.deviceCount > 0" color="blue">
                {{ record.deviceCount }} 设备
              </a-tag>
              <a-tag v-if="record.userCount > 0" color="green">
                {{ record.userCount }} 用户
              </a-tag>
            </a-space>
          </template>

          <!-- 区域类型列 -->
          <template v-else-if="column.key === 'areaType'">
            <a-tag :color="getTypeColor(record.areaType)">
              {{ getTypeName(record.areaType) }}
            </a-tag>
          </template>

          <!-- 层级列 -->
          <template v-else-if="column.key === 'areaLevel'">
            <a-space>
              <span>Lv.{{ text }}</span>
              <a-tooltip title="显示路径">
                <a-button type="link" size="small" @click="showPath(record)">
                  <UnorderedListOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'success' : 'default'">
              {{ record.status === 'ACTIVE' ? '正常' : '停用' }}
            </a-tag>
          </template>

          <!-- 坐标列 -->
          <template v-else-if="column.key === 'coordinates'">
            <span v-if="record.centerLatitude && record.centerLongitude">
              {{ record.centerLatitude }}, {{ record.centerLongitude }}
            </span>
            <span v-else style="color: #999">
              <EnvironmentOutlined /> 未设置
            </span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showEditModal(record)">
                <EditOutlined /> 编辑
              </a-button>
              <a-button type="link" size="small" @click="showDetail(record)">
                <EyeOutlined /> 详情
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu @click="(e) => handleMenuClick(e, record)">
                    <a-menu-item key="children">
                      <UserOutlined /> 子区域
                    </a-menu-item>
                    <a-menu-item key="permissions">
                      <SafetyOutlined /> 权限
                    </a-menu-item>
                    <a-menu-item key="map">
                      <EnvironmentOutlined /> 地图
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="delete" danger>
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑模态框 -->
    <AreaModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :record="currentRecord"
      @success="handleModalSuccess"
    />

    <!-- 详情模态框 -->
    <AreaDetail
      v-model:visible="detailVisible"
      :record="currentRecord"
    />

    <!-- 路径模态框 -->
    <a-modal
      v-model:visible="pathVisible"
      title="区域路径"
      width="600px"
      :footer="null"
    >
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="区域名称">
          {{ currentRecord?.areaName }}
        </a-descriptions-item>
        <a-descriptions-item label="区域路径">
          <a-timeline>
            <a-timeline-item
              v-for="(area, index) in pathList"
              :key="area.areaId"
              :color="index === pathList.length - 1 ? 'blue' : 'gray'"
            >
              {{ area.areaName }} ({{ getTypeName(area.areaType) }})
            </a-timeline-item>
          </a-timeline>
        </a-descriptions-item>
        <a-descriptions-item label="层级">
          Lv.{{ currentRecord?.areaLevel }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 批量操作栏 -->
    <div v-if="selectedRowKeys.length > 0" class="batch-actions">
      <a-alert
        :message="`已选择 ${selectedRowKeys.length} 项`"
        type="info"
        show-icon
        class="batch-alert"
      >
        <template #action>
          <a-space>
            <a-button size="small" @click="batchEnable">批量启用</a-button>
            <a-button size="small" @click="batchDisable">批量停用</a-button>
            <a-button size="small" danger @click="batchDelete">批量删除</a-button>
          </a-space>
        </template>
      </a-alert>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ExportOutlined,
  EditOutlined,
  EyeOutlined,
  DeleteOutlined,
  UserOutlined,
  SafetyOutlined,
  EnvironmentOutlined,
  DownOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'
import AreaModal from './components/AreaModal.vue'
import AreaDetail from './components/AreaDetail.vue'
import { areaApi } from '@/api/area'

export default {
  name: 'AreaList',
  components: {
    AreaModal,
    AreaDetail,
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    ExportOutlined,
    EditOutlined,
    EyeOutlined,
    DeleteOutlined,
    UserOutlined,
    SafetyOutlined,
    EnvironmentOutlined,
    DownOutlined,
    UnorderedListOutlined
  },
  setup() {
    // 搜索表单
    const searchForm = reactive({
      areaName: '',
      areaType: undefined,
      status: undefined
    })

    // 表格数据
    const tableData = ref([])
    const loading = ref(false)
    const selectedRowKeys = ref([])

    // 分页配置
    const pagination = reactive({
      current: 1,
      pageSize: 20,
      total: 0,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
    })

    // 模态框状态
    const modalVisible = ref(false)
    const modalMode = ref('add')
    const detailVisible = ref(false)
    const pathVisible = ref(false)
    const currentRecord = ref(null)
    const pathList = ref([])

    // 表格列配置
    const columns = [
      {
        title: '区域名称',
        dataIndex: 'areaName',
        key: 'areaName',
        width: 200,
        ellipsis: true
      },
      {
        title: '区域编码',
        dataIndex: 'areaCode',
        key: 'areaCode',
        width: 150,
        ellipsis: true
      },
      {
        title: '类型',
        dataIndex: 'areaType',
        key: 'areaType',
        width: 100
      },
      {
        title: '层级',
        dataIndex: 'areaLevel',
        key: 'areaLevel',
        width: 100
      },
      {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        width: 80
      },
      {
        title: '坐标',
        key: 'coordinates',
        width: 200,
        ellipsis: true
      },
      {
        title: '创建时间',
        dataIndex: 'createTime',
        key: 'createTime',
        width: 150
      },
      {
        title: '操作',
        key: 'action',
        width: 200,
        fixed: 'right'
      }
    ]

    // 行选择配置
    const rowSelection = {
      selectedRowKeys: selectedRowKeys,
      onChange: (keys) => {
        selectedRowKeys.value = keys
      }
    }

    // 获取类型颜色
    const getTypeColor = (type) => {
      const colorMap = {
        REGION: 'purple',
        PROVINCE: 'blue',
        CITY: 'cyan',
        DISTRICT: 'green',
        BUILDING: 'orange',
        FLOOR: 'volcano',
        ROOM: 'red'
      }
      return colorMap[type] || 'default'
    }

    // 获取类型名称
    const getTypeName = (type) => {
      const nameMap = {
        REGION: '大区',
        PROVINCE: '省份',
        CITY: '城市',
        DISTRICT: '区县',
        BUILDING: '建筑',
        FLOOR: '楼层',
        ROOM: '房间'
      }
      return nameMap[type] || type
    }

    // 加载数据
    const loadData = async () => {
      loading.value = true
      try {
        const params = {
          ...searchForm,
          page: pagination.current,
          size: pagination.pageSize
        }
        const response = await areaApi.getAreaList(params)
        tableData.value = response.data.records
        pagination.total = response.data.total
      } catch (error) {
        message.error('加载数据失败')
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      pagination.current = 1
      loadData()
    }

    // 重置
    const handleReset = () => {
      Object.assign(searchForm, {
        areaName: '',
        areaType: undefined,
        status: undefined
      })
      pagination.current = 1
      loadData()
    }

    // 表格变化
    const handleTableChange = (pag) => {
      pagination.current = pag.current
      pagination.pageSize = pag.pageSize
      loadData()
    }

    // 显示新增模态框
    const showAddModal = () => {
      modalMode.value = 'add'
      currentRecord.value = null
      modalVisible.value = true
    }

    // 显示编辑模态框
    const showEditModal = (record) => {
      modalMode.value = 'edit'
      currentRecord.value = record
      modalVisible.value = true
    }

    // 显示详情
    const showDetail = (record) => {
      currentRecord.value = record
      detailVisible.value = true
    }

    // 显示路径
    const showPath = async (record) => {
      try {
        currentRecord.value = record
        const response = await areaApi.getAreaPath(record.areaId)
        pathList.value = response.data
        pathVisible.value = true
      } catch (error) {
        message.error('获取路径失败')
      }
    }

    // 模态框成功回调
    const handleModalSuccess = () => {
      loadData()
    }

    // 菜单点击
    const handleMenuClick = ({ key }, record) => {
      switch (key) {
        case 'children':
          showChildren(record)
          break
        case 'permissions':
          showPermissions(record)
          break
        case 'map':
          showMap(record)
          break
        case 'delete':
          handleDelete(record)
          break
      }
    }

    // 显示子区域
    const showChildren = (record) => {
      // 跳转到子区域页面或显示子区域列表
      message.info('显示子区域功能待实现')
    }

    // 显示权限
    const showPermissions = (record) => {
      // 打开权限管理页面
      message.info('权限管理功能待实现')
    }

    // 显示地图
    const showMap = (record) => {
      // 打开地图页面
      message.info('地图功能待实现')
    }

    // 删除
    const handleDelete = (record) => {
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除区域 "${record.areaName}" 吗？`,
        okText: '确认',
        cancelText: '取消',
        onOk: async () => {
          try {
            await areaApi.deleteArea(record.areaId)
            message.success('删除成功')
            loadData()
          } catch (error) {
            message.error('删除失败')
          }
        }
      })
    }

    // 导出数据
    const exportData = () => {
      message.info('导出功能待实现')
    }

    // 批量启用
    const batchEnable = async () => {
      try {
        await areaApi.batchUpdateStatus(selectedRowKeys.value, 'ACTIVE')
        message.success('批量启用成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error) {
        message.error('批量启用失败')
      }
    }

    // 批量停用
    const batchDisable = async () => {
      try {
        await areaApi.batchUpdateStatus(selectedRowKeys.value, 'INACTIVE')
        message.success('批量停用成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error) {
        message.error('批量停用失败')
      }
    }

    // 批量删除
    const batchDelete = () => {
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除选中的 ${selectedRowKeys.value.length} 个区域吗？`,
        okText: '确认',
        cancelText: '取消',
        onOk: async () => {
          try {
            await areaApi.batchDelete(selectedRowKeys.value)
            message.success('批量删除成功')
            selectedRowKeys.value = []
            loadData()
          } catch (error) {
            message.error('批量删除失败')
          }
        }
      })
    }

    // 初始化
    onMounted(() => {
      loadData()
    })

    return {
      searchForm,
      tableData,
      loading,
      pagination,
      selectedRowKeys,
      columns,
      rowSelection,
      modalVisible,
      modalMode,
      detailVisible,
      pathVisible,
      currentRecord,
      pathList,
      getTypeColor,
      getTypeName,
      handleSearch,
      handleReset,
      handleTableChange,
      showAddModal,
      showEditModal,
      showDetail,
      showPath,
      handleModalSuccess,
      handleMenuClick,
      exportData,
      batchEnable,
      batchDisable,
      batchDelete
    }
  }
}
</script>

<style lang="less" scoped>
.area-list-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .search-card {
    margin-bottom: 16px;

    .search-form {
      .ant-form-item {
        margin-bottom: 16px;
      }
    }
  }

  .table-card {
    .ant-table-tbody > tr:hover > td {
      background-color: #fafafa;
    }
  }

  .batch-actions {
    position: fixed;
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;

    .batch-alert {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-radius: 8px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .area-list-container {
    padding: 16px;

    .search-form {
      .ant-form-item {
        margin-bottom: 8px;
      }
    }

    .batch-actions {
      bottom: 16px;
      width: calc(100% - 32px);
      left: 16px;
      transform: none;
    }
  }
}
</style>