<template>
  <div class="area-tree-container">
    <a-row :gutter="16">
      <!-- 左侧树形结构 -->
      <a-col :span="8">
        <a-card :bordered="false" class="tree-card">
          <template #title>
            <a-space>
              <ApartmentOutlined />
              <span>区域结构</span>
            </a-space>
          </template>

          <template #extra>
            <a-space>
              <a-tooltip title="刷新">
                <a-button type="text" size="small" @click="refreshTree">
                  <ReloadOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="展开全部">
                <a-button type="text" size="small" @click="expandAll">
                  <DownOutlined />
                </a-button>
              </a-tooltip>
              <a-tooltip title="折叠全部">
                <a-button type="text" size="small" @click="collapseAll">
                  <UpOutlined />
                </a-button>
              </a-tooltip>
            </a-space>
          </template>

          <div class="tree-toolbar">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="搜索区域名称"
              allowClear
              @search="handleTreeSearch"
              style="margin-bottom: 16px"
            />

            <a-radio-group v-model:value="viewMode" button-style="solid" size="small">
              <a-radio-button value="tree">树形</a-radio-button>
              <a-radio-button value="list">列表</a-radio-button>
            </a-radio-group>
          </div>

          <!-- 树形视图 -->
          <a-tree
            v-if="viewMode === 'tree'"
            :tree-data="treeData"
            :expanded-keys="expandedKeys"
            :selected-keys="selectedKeys"
            :auto-expand-parent="autoExpandParent"
            show-icon
            @select="handleTreeSelect"
            @expand="handleTreeExpand"
          >
            <template #icon="{ dataRef }">
              <HomeOutlined v-if="dataRef.areaLevel === 1" />
              <BankOutlined v-else-if="dataRef.areaLevel <= 3" />
              <BuildOutlined v-else-if="dataRef.areaLevel <= 5" />
              <ApartmentOutlined v-else />
            </template>

            <template #title="{ dataRef }">
              <a-dropdown :trigger="['contextmenu']">
                <span class="tree-node-title" :class="{ 'selected': selectedKeys.includes(dataRef.key) }">
                  {{ dataRef.title }}
                  <a-tag v-if="dataRef.deviceCount > 0" size="small" color="blue">
                    {{ dataRef.deviceCount }}
                  </a-tag>
                </span>
                <template #overlay>
                  <a-menu @click="(e) => handleTreeNodeMenuClick(e, dataRef)">
                    <a-menu-item key="add">
                      <PlusOutlined /> 添加子区域
                    </a-menu-item>
                    <a-menu-item key="edit">
                      <EditOutlined /> 编辑
                    </a-menu-item>
                    <a-menu-item key="detail">
                      <EyeOutlined /> 详情
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="move">
                      <DragOutlined /> 移动
                    </a-menu-item>
                    <a-menu-item key="copy">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="delete" danger>
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </template>
          </a-tree>

          <!-- 列表视图 -->
          <a-list
            v-else
            :data-source="flatTreeData"
            size="small"
            :pagination="false"
          >
            <template #renderItem="{ item }">
              <a-list-item
                :class="{ 'list-item-selected': selectedKeys.includes(item.key) }"
                @click="handleListSelect(item)"
              >
                <a-list-item-meta>
                  <template #avatar>
                    <a-avatar :style="{ backgroundColor: getLevelColor(item.areaLevel) }">
                      {{ item.areaLevel }}
                    </a-avatar>
                  </template>
                  <template #title>
                    <a-space>
                      <span>{{ item.title }}</span>
                      <a-tag size="small">{{ getTypeName(item.areaType) }}</a-tag>
                      <a-tag v-if="item.deviceCount > 0" size="small" color="blue">
                        {{ item.deviceCount }} 设备
                      </a-tag>
                    </a-space>
                  </template>
                  <template #description>
                    <a-space>
                      <span>{{ item.indent }}{{ item.areaName }}</span>
                    </a-space>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧详情/操作面板 -->
      <a-col :span="16">
        <a-card :bordered="false" class="detail-card">
          <template #title>
            <a-space>
              <InfoCircleOutlined />
              <span>{{ selectedArea?.areaName || '区域详情' }}</span>
            </a-space>
          </template>

          <template #extra v-if="selectedArea">
            <a-space>
              <a-button @click="showEditModal">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button @click="showDetailModal">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button @click="showMapModal">
                <template #icon><EnvironmentOutlined /></template>
                地图
              </a-button>
            </a-space>
          </template>

          <div v-if="!selectedArea" class="empty-state">
            <a-empty description="请选择一个区域查看详情" />
          </div>

          <div v-else class="area-detail">
            <!-- 基本信息 -->
            <a-descriptions title="基本信息" :column="2" bordered size="small">
              <a-descriptions-item label="区域名称">
                {{ selectedArea.areaName }}
              </a-descriptions-item>
              <a-descriptions-item label="区域编码">
                {{ selectedArea.areaCode }}
              </a-descriptions-item>
              <a-descriptions-item label="区域类型">
                <a-tag :color="getTypeColor(selectedArea.areaType)">
                  {{ getTypeName(selectedArea.areaType) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="层级">
                Lv.{{ selectedArea.areaLevel }}
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-badge :status="selectedArea.status === 'ACTIVE' ? 'success' : 'default'" />
                {{ selectedArea.status === 'ACTIVE' ? '正常' : '停用' }}
              </a-descriptions-item>
              <a-descriptions-item label="排序">
                {{ selectedArea.sortOrder }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- 位置信息 -->
            <a-descriptions title="位置信息" :column="2" bordered size="small" style="margin-top: 16px">
              <a-descriptions-item label="详细地址" :span="2">
                {{ selectedArea.address || '未设置' }}
              </a-descriptions-item>
              <a-descriptions-item label="中心坐标">
                <span v-if="selectedArea.centerLatitude && selectedArea.centerLongitude">
                  {{ selectedArea.centerLatitude }}, {{ selectedArea.centerLongitude }}
                </span>
                <span v-else style="color: #999">未设置</span>
              </a-descriptions-item>
              <a-descriptions-item label="区域半径">
                {{ selectedArea.areaRadius || 0 }} 米
              </a-descriptions-item>
              <a-descriptions-item label="联系人" :span="2">
                {{ selectedArea.contactPerson || '未设置' }}
              </a-descriptions-item>
              <a-descriptions-item label="联系电话" :span="2">
                {{ selectedArea.contactPhone || '未设置' }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- 统计信息 -->
            <a-row :gutter="16" style="margin-top: 16px">
              <a-col :span="8">
                <a-statistic title="设备数量" :value="statistics.deviceCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="用户数量" :value="statistics.userCount" />
              </a-col>
              <a-col :span="8">
                <a-statistic title="今日访问" :value="statistics.todayVisitCount" />
              </a-col>
            </a-row>

            <!-- 子区域列表 -->
            <div v-if="childAreas.length > 0" style="margin-top: 24px">
              <a-divider>子区域 ({{ childAreas.length }})</a-divider>
              <a-list :data-source="childAreas" size="small" :pagination="false">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #avatar>
                        <a-avatar :style="{ backgroundColor: getLevelColor(item.areaLevel) }">
                          {{ item.areaLevel }}
                        </a-avatar>
                      </template>
                      <template #title>
                        <a-space>
                          <span>{{ item.areaName }}</span>
                          <a-tag size="small">{{ getTypeName(item.areaType) }}</a-tag>
                          <a-button type="link" size="small" @click="selectChildArea(item)">
                            查看详情
                          </a-button>
                        </a-space>
                      </template>
                    </a-list-item-meta>
                    <template #actions>
                      <a-space>
                        <a-tag v-if="item.deviceCount > 0" color="blue">
                          {{ item.deviceCount }} 设备
                        </a-tag>
                        <a-tag v-if="item.userCount > 0" color="green">
                          {{ item.userCount }} 用户
                        </a-tag>
                      </a-space>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 模态框组件 -->
    <AreaModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :record="currentRecord"
      :parent-id="parentId"
      @success="handleModalSuccess"
    />

    <AreaDetail
      v-model:visible="detailVisible"
      :record="currentRecord"
    />

    <AreaMap
      v-model:visible="mapVisible"
      :record="currentRecord"
    />

    <!-- 移动区域模态框 -->
    <a-modal
      v-model:visible="moveVisible"
      title="移动区域"
      width="500px"
      @ok="handleMove"
      :confirm-loading="moveLoading"
    >
      <a-form layout="vertical">
        <a-form-item label="当前路径">
          <a-timeline size="small">
            <a-timeline-item
              v-for="(area, index) in currentPath"
              :key="area.areaId"
              :color="index === currentPath.length - 1 ? 'blue' : 'gray'"
            >
              {{ area.areaName }}
            </a-timeline-item>
          </a-timeline>
        </a-form-item>
        <a-form-item label="目标父区域" required>
          <a-tree-select
            v-model:value="targetParentId"
            :tree-data="moveTreeData"
            placeholder="请选择目标父区域"
            allowClear
            show-search
            tree-default-expand-all
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  ApartmentOutlined,
  ReloadOutlined,
  DownOutlined,
  UpOutlined,
  HomeOutlined,
  BankOutlined,
  BuildOutlined,
  PlusOutlined,
  EditOutlined,
  EyeOutlined,
  DeleteOutlined,
  DragOutlined,
  CopyOutlined,
  InfoCircleOutlined,
  EnvironmentOutlined
} from '@ant-design/icons-vue'
import AreaModal from './components/AreaModal.vue'
import AreaDetail from './components/AreaDetail.vue'
import AreaMap from './components/AreaMap.vue'
import { areaApi } from '@/api/area'

export default {
  name: 'AreaTree',
  components: {
    AreaModal,
    AreaDetail,
    AreaMap,
    ApartmentOutlined,
    ReloadOutlined,
    DownOutlined,
    UpOutlined,
    HomeOutlined,
    BankOutlined,
    BuildOutlined,
    PlusOutlined,
    EditOutlined,
    EyeOutlined,
    DeleteOutlined,
    DragOutlined,
    CopyOutlined,
    InfoCircleOutlined,
    EnvironmentOutlined
  },
  setup() {
    // 响应式数据
    const treeData = ref([])
    const flatTreeData = ref([])
    const selectedKeys = ref([])
    const expandedKeys = ref([])
    const autoExpandParent = ref(true)
    const searchKeyword = ref('')
    const viewMode = ref('tree')

    const selectedArea = ref(null)
    const childAreas = ref([])
    const statistics = reactive({
      deviceCount: 0,
      userCount: 0,
      todayVisitCount: 0
    })

    // 模态框状态
    const modalVisible = ref(false)
    const modalMode = ref('add')
    const detailVisible = ref(false)
    const mapVisible = ref(false)
    const moveVisible = ref(false)
    const moveLoading = ref(false)

    const currentRecord = ref(null)
    const parentId = ref(null)
    const targetParentId = ref(null)
    const currentPath = ref([])
    const moveTreeData = ref([])

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

    // 获取层级颜色
    const getLevelColor = (level) => {
      const colors = ['#f50', '#fa8c16', '#fadb14', '#52c41a', '#1890ff', '#722ed1', '#eb2f96']
      return colors[level - 1] || '#d9d9d9'
    }

    // 加载树形数据
    const loadTreeData = async () => {
      try {
        const response = await areaApi.getAreaTree()
        treeData.value = [response.data]
        flatTreeData.value = flattenTree(response.data)

        // 默认展开第一层
        if (treeData.value.length > 0) {
          expandedKeys.value = [treeData.value[0].key]
        }
      } catch (error) {
        message.error('加载区域树失败')
      }
    }

    // 扁平化树形数据
    const flattenTree = (node, level = 0, parent = null) => {
      const flatNode = {
        ...node,
        indent: '　'.repeat(level),
        parent: parent
      }

      let result = [flatNode]

      if (node.children && node.children.length > 0) {
        for (const child of node.children) {
          result = result.concat(flattenTree(child, level + 1, node))
        }
      }

      return result
    }

    // 搜索树
    const handleTreeSearch = (value) => {
      if (!value) {
        expandedKeys.value = []
        return
      }

      const expandedKeysList = []
      const findAndExpand = (nodes) => {
        nodes.forEach(node => {
          if (node.title.toLowerCase().includes(value.toLowerCase())) {
            expandedKeysList.push(node.key)
          }
          if (node.children) {
            findAndExpand(node.children)
          }
        })
      }

      findAndExpand(treeData.value)
      expandedKeys.value = [...new Set(expandedKeysList)]
    }

    // 树形选择
    const handleTreeSelect = (keys, info) => {
      selectedKeys.value = keys
      if (keys.length > 0) {
        selectArea(info.node.dataRef)
      } else {
        selectedArea.value = null
        childAreas.value = []
      }
    }

    // 列表选择
    const handleListSelect = (item) => {
      selectedKeys.value = [item.key]
      selectArea(item)
    }

    // 选择区域
    const selectArea = async (area) => {
      selectedArea.value = area

      // 加载统计信息
      try {
        const response = await areaApi.getAreaStatistics(area.areaId)
        Object.assign(statistics, response.data)
      } catch (error) {
        console.error('加载统计信息失败', error)
      }

      // 加载子区域
      try {
        const response = await areaApi.getChildAreas(area.areaId)
        childAreas.value = response.data
      } catch (error) {
        console.error('加载子区域失败', error)
      }
    }

    // 选择子区域
    const selectChildArea = (childArea) => {
      selectedKeys.value = [childArea.key]
      selectArea(childArea)
    }

    // 树形展开
    const handleTreeExpand = (keys) => {
      expandedKeys.value = keys
      autoExpandParent.value = false
    }

    // 展开全部
    const expandAll = () => {
      const getAllKeys = (nodes) => {
        let keys = []
        nodes.forEach(node => {
          keys.push(node.key)
          if (node.children) {
            keys = keys.concat(getAllKeys(node.children))
          }
        })
        return keys
      }
      expandedKeys.value = getAllKeys(treeData.value)
    }

    // 折叠全部
    const collapseAll = () => {
      expandedKeys.value = []
    }

    // 刷新树
    const refreshTree = () => {
      loadTreeData()
    }

    // 树节点菜单点击
    const handleTreeNodeMenuClick = ({ key }, node) => {
      currentRecord.value = node

      switch (key) {
        case 'add':
          handleAddChild(node)
          break
        case 'edit':
          showEditModal()
          break
        case 'detail':
          showDetailModal()
          break
        case 'move':
          showMoveModal(node)
          break
        case 'copy':
          handleCopy(node)
          break
        case 'delete':
          handleDelete(node)
          break
      }
    }

    // 添加子区域
    const handleAddChild = (node) => {
      parentId.value = node.areaId
      modalMode.value = 'add'
      currentRecord.value = null
      modalVisible.value = true
    }

    // 显示编辑模态框
    const showEditModal = () => {
      modalMode.value = 'edit'
      modalVisible.value = true
    }

    // 显示详情模态框
    const showDetailModal = () => {
      detailVisible.value = true
    }

    // 显示地图模态框
    const showMapModal = () => {
      mapVisible.value = true
    }

    // 显示移动模态框
    const showMoveModal = async (node) => {
      try {
        // 获取当前路径
        const pathResponse = await areaApi.getAreaPath(node.areaId)
        currentPath.value = pathResponse.data

        // 获取移动树数据（排除自己和子节点）
        const treeResponse = await areaApi.getAreaTree()
        moveTreeData.value = removeNodeAndChildren(treeResponse.data, node.areaId)

        targetParentId.value = null
        moveVisible.value = true
      } catch (error) {
        message.error('加载移动数据失败')
      }
    }

    // 移除节点及其子节点
    const removeNodeAndChildren = (node, excludeId) => {
      if (node.areaId === excludeId) {
        return null
      }

      const newNode = { ...node }
      if (node.children && node.children.length > 0) {
        newNode.children = node.children
          .map(child => removeNodeAndChildren(child, excludeId))
          .filter(child => child !== null)
      }

      return newNode
    }

    // 处理移动
    const handleMove = async () => {
      if (!targetParentId.value) {
        message.error('请选择目标父区域')
        return
      }

      moveLoading.value = true
      try {
        await areaApi.moveArea(currentRecord.value.areaId, targetParentId.value)
        message.success('移动成功')
        moveVisible.value = false
        refreshTree()
      } catch (error) {
        message.error('移动失败')
      } finally {
        moveLoading.value = false
      }
    }

    // 复制区域
    const handleCopy = async (node) => {
      try {
        const response = await areaApi.copyAreaStructure(node.areaId, node.parentAreaId)
        message.success('复制成功')
        refreshTree()
      } catch (error) {
        message.error('复制失败')
      }
    }

    // 删除区域
    const handleDelete = (node) => {
      if (node.children && node.children.length > 0) {
        message.warning('请先删除子区域')
        return
      }

      // 这里应该调用确认对话框
      message.info('删除功能待实现')
    }

    // 模态框成功回调
    const handleModalSuccess = () => {
      refreshTree()
      if (selectedArea.value) {
        selectArea(selectedArea.value)
      }
    }

    // 监听搜索关键词变化
    watch(searchKeyword, (value) => {
      handleTreeSearch(value)
    })

    // 初始化
    onMounted(() => {
      loadTreeData()
    })

    return {
      treeData,
      flatTreeData,
      selectedKeys,
      expandedKeys,
      autoExpandParent,
      searchKeyword,
      viewMode,
      selectedArea,
      childAreas,
      statistics,
      modalVisible,
      modalMode,
      detailVisible,
      mapVisible,
      moveVisible,
      moveLoading,
      currentRecord,
      parentId,
      targetParentId,
      currentPath,
      moveTreeData,
      getTypeColor,
      getTypeName,
      getLevelColor,
      handleTreeSearch,
      handleTreeSelect,
      handleListSelect,
      handleTreeExpand,
      expandAll,
      collapseAll,
      refreshTree,
      handleTreeNodeMenuClick,
      handleMove,
      handleModalSuccess,
      showEditModal,
      showDetailModal,
      showMapModal,
      selectChildArea
    }
  }
}
</script>

<style lang="less" scoped>
.area-tree-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .tree-card {
    .tree-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .ant-radio-group {
        margin-left: auto;
      }
    }

    .tree-node-title {
      cursor: pointer;
      user-select: none;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f5f5;
      }

      &.selected {
        background-color: #e6f7ff;
      }
    }
  }

  .detail-card {
    .empty-state {
      padding: 60px 0;
      text-align: center;
    }

    .area-detail {
      .ant-divider {
        margin: 16px 0;
      }

      .ant-statistic {
        text-align: center;
        padding: 16px;
        background-color: #fafafa;
        border-radius: 6px;
      }
    }
  }

  .list-item-selected {
    background-color: #e6f7ff;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .area-tree-container {
    .ant-col:first-child {
      span: 10;
    }
    .ant-col:last-child {
      span: 14;
    }
  }
}

@media (max-width: 768px) {
  .area-tree-container {
    padding: 16px;

    .ant-col {
      span: 24;
      margin-bottom: 16px;
    }

    .tree-toolbar {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .ant-radio-group {
        margin-left: 0;
        align-self: center;
      }
    }
  }
}
</style>