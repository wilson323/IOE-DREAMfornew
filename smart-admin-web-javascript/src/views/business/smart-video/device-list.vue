<!--
  智能视频-设备列表管理页面（完整增强版）

  @Author:    Claude Code
  @Date:      2024-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012

  功能说明：
  - 设备统计面板（总数、在线率、各类型分布）
  - 分组树导航
  - 高级筛选（多条件组合）
  - 设备详情抽屉
  - 配置管理
  - 批量操作
  - 实时状态监控
  - 通道管理
-->
<template>
  <div class="smart-video-device">
    <!-- 设备统计面板 -->
    <a-row :gutter="16" class="statistics-cards">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="设备总数"
            :value="statistics.total"
            suffix="台"
            :value-style="{ color: '#1890ff', fontSize: '28px' }"
          >
            <template #prefix>
              <VideoCameraOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="在线设备"
            :value="statistics.online"
            suffix="台"
            :value-style="{ color: '#52c41a', fontSize: '28px' }"
          >
            <template #prefix>
              <CheckCircleOutlined />
            </template>
            <template #suffix>
              <a-tag :color="getOnlineRateColor(statistics.onlineRate)">
                {{ statistics.onlineRate }}%
              </a-tag>
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="离线设备"
            :value="statistics.offline"
            suffix="台"
            :value-style="{ color: '#ff4d4f', fontSize: '28px' }"
          >
            <template #prefix>
              <CloseCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>

      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            title="故障设备"
            :value="statistics.fault"
            suffix="台"
            :value-style="{ color: '#faad14', fontSize: '28px' }"
          >
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 设备类型分布 -->
    <a-card :bordered="false" style="margin-top: 16px;">
      <template #title>
        <span>设备类型分布</span>
      </template>
      <template #extra>
        <a-space>
          <a-tag v-for="(item, index) in deviceTypeStats" :key="index" :color="item.color">
            {{ item.label }}: {{ item.count }}台
          </a-tag>
        </a-space>
      </template>
      <a-progress
        :percent="getOnlineProgress()"
        :stroke-color="{
          '0%': '#108ee9',
          '100%': '#87d068',
        }"
        :status="statistics.onlineRate >= 80 ? 'success' : statistics.onlineRate >= 50 ? 'normal' : 'exception'"
      />
      <div style="margin-top: 8px; color: #999; font-size: 12px;">
        总体在线率: {{ statistics.onlineRate }}% (在线{{ statistics.online }}/{{ statistics.total }})
      </div>
    </a-card>

    <!-- 主内容区 -->
    <a-row :gutter="16" style="margin-top: 16px;">
      <!-- 左侧分组树 -->
      <a-col :span="5">
        <a-card :bordered="false" title="设备分组" style="height: 100%;">
          <template #extra>
            <a-button size="small" type="link" @click="refreshGroupTree">
              <template #icon><ReloadOutlined /></template>
            </a-button>
          </template>

          <a-tree
            v-model:selectedKeys="selectedGroupKeys"
            v-model:expandedKeys="expandedGroupKeys"
            :tree-data="groupTreeData"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            show-icon
            @select="onGroupSelect"
          >
            <template #icon="{ dataRef }">
              <FolderOutlined v-if="dataRef.children && dataRef.children.length > 0" />
              <VideoCameraOutlined v-else />
            </template>
            <template #title="{ name, count }">
              <span>{{ name }}</span>
              <a-tag size="small" color="blue" style="margin-left: 8px;">{{ count }}</a-tag>
            </template>
          </a-tree>
        </a-card>
      </a-col>

      <!-- 右侧设备列表 -->
      <a-col :span="19">
        <a-card :bordered="false">
          <!-- 顶部查询工具栏 -->
          <a-form class="smart-query-table-form">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item label="设备名称">
                  <a-input
                    v-model:value="queryForm.deviceName"
                    placeholder="请输入设备名称或编码"
                    allowClear
                  >
                    <template #prefix>
                      <SearchOutlined />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>

              <a-col :span="4">
                <a-form-item label="状态">
                  <a-select
                    v-model:value="queryForm.status"
                    placeholder="请选择状态"
                    allowClear
                  >
                    <a-select-option value="">全部</a-select-option>
                    <a-select-option
                      v-for="item in Object.values(DEVICE_STATUS_ENUM)"
                      :key="item.value"
                      :value="item.value"
                    >
                      <a-badge :status="item.value === 'online' ? 'success' : 'default'" />
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="4">
                <a-form-item label="设备类型">
                  <a-select
                    v-model:value="queryForm.deviceType"
                    placeholder="请选择设备类型"
                    allowClear
                  >
                    <a-select-option value="">全部类型</a-select-option>
                    <a-select-option
                      v-for="item in Object.values(DEVICE_TYPE_ENUM)"
                      :key="item.value"
                      :value="item.value"
                    >
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="4">
                <a-form-item label="厂商">
                  <a-select
                    v-model:value="queryForm.manufacturer"
                    placeholder="请选择厂商"
                    allowClear
                  >
                    <a-select-option value="">全部厂商</a-select-option>
                    <a-select-option
                      v-for="item in Object.values(DEVICE_MANUFACTURER_ENUM)"
                      :key="item.value"
                      :value="item.value"
                    >
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="6">
                <a-form-item>
                  <a-space>
                    <a-button type="primary" @click="queryData">
                      <template #icon><SearchOutlined /></template>
                      查询
                    </a-button>
                    <a-button @click="resetQuery">
                      <template #icon><ReloadOutlined /></template>
                      重置
                    </a-button>
                    <a-button @click="toggleAdvancedSearch">
                      {{ showAdvancedSearch ? '收起' : '展开' }}
                      <template #icon>
                        <DownOutlined v-if="!showAdvancedSearch" />
                        <UpOutlined v-else />
                      </template>
                    </a-button>
                  </a-space>
                </a-form-item>
              </a-col>
            </a-row>

            <!-- 高级筛选 -->
            <a-row v-show="showAdvancedSearch" :gutter="16" style="margin-top: 8px;">
              <a-col :span="6">
                <a-form-item label="IP地址">
                  <a-input
                    v-model:value="queryForm.ipAddress"
                    placeholder="请输入IP地址"
                    allowClear
                  />
                </a-form-item>
              </a-col>

              <a-col :span="6">
                <a-form-item label="所属区域">
                  <a-tree-select
                    v-model:value="queryForm.areaId"
                    :tree-data="areaTreeData"
                    placeholder="请选择区域"
                    allowClear
                    tree-default-expand-all
                  />
                </a-form-item>
              </a-col>

              <a-col :span="6">
                <a-form-item label="通道数">
                  <a-input-number
                    v-model:value="queryForm.channelCount"
                    placeholder="通道数"
                    :min="1"
                    :max="64"
                    style="width: 100%;"
                  />
                </a-form-item>
              </a-col>

              <a-col :span="6">
                <a-form-item label="注册时间">
                  <a-range-picker
                    v-model:value="queryForm.registerTimeRange"
                    style="width: 100%;"
                    format="YYYY-MM-DD"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <!-- 主操作按钮区 -->
          <div class="smart-query-table-operation">
            <a-space>
              <a-button type="primary" @click="showAddModal">
                <template #icon><PlusOutlined /></template>
                新增设备
              </a-button>

              <a-button
                danger
                @click="batchDelete"
                :disabled="selectedRowKeys.length === 0"
              >
                <template #icon><DeleteOutlined /></template>
                批量删除
              </a-button>

              <a-button @click="searchDevices">
                <template #icon><RadarChartOutlined /></template>
                搜索在线设备
              </a-button>

              <a-button @click="syncDevices" :disabled="selectedRowKeys.length === 0">
                <template #icon><SyncOutlined /></template>
                批量同步
              </a-button>

              <a-button @click="exportDevices">
                <template #icon><ExportOutlined /></template>
                导出
              </a-button>

              <a-button @click="showImportModal = true">
                <template #icon><ImportOutlined /></template>
                导入
              </a-button>
            </a-space>
          </div>

          <!-- 更多操作区 -->
          <div class="smart-query-table-operation" style="margin-top: 12px;">
            <a-space wrap>
              <span style="color: #999;">批量操作:</span>

              <a-button size="small" @click="batchConfig" :disabled="selectedRowKeys.length === 0">
                <template #icon><SettingOutlined /></template>
                批量配置
              </a-button>

              <a-button size="small" @click="batchSubscribe" :disabled="selectedRowKeys.length === 0">
                <template #icon><BellOutlined /></template>
                批量订阅
              </a-button>

              <a-button size="small" @click="batchUnsubscribe" :disabled="selectedRowKeys.length === 0">
                <template #icon><BellOutlined /></template>
                取消订阅
              </a-button>

              <a-button size="small" @click="batchSyncTime" :disabled="selectedRowKeys.length === 0">
                <template #icon><ClockCircleOutlined /></template>
                批量校时
              </a-button>

              <a-button size="small" @click="batchRestart" :disabled="selectedRowKeys.length === 0">
                <template #icon><PoweroffOutlined /></template>
                批量重启
              </a-button>

              <a-button size="small" @click="batchUpgrade" :disabled="selectedRowKeys.length === 0">
                <template #icon><UpgradeOutlined /></template>
                批量升级
              </a-button>
            </a-space>
          </div>

          <!-- 设备列表表格 -->
          <a-table
            :columns="columns"
            :data-source="tableData"
            :pagination="pagination"
            :loading="tableLoading"
            :row-selection="rowSelection"
            :scroll="{ x: 2000 }"
            row-key="id"
            bordered
            @change="handleTableChange"
            class="smart-margin-top10"
            :row-class-name="getRowClassName"
          >
            <template #bodyCell="{ column, record }">
              <!-- 设备名称 -->
              <template v-if="column.dataIndex === 'deviceName'">
                <div class="device-name-cell">
                  <VideoCameraOutlined style="margin-right: 4px;" />
                  <span>{{ record.deviceName }}</span>
                  <a-tag v-if="record.status === 'online'" color="success" size="small" style="margin-left: 4px;">
                    在线
                  </a-tag>
                  <a-tag v-else-if="record.status === 'offline'" color="default" size="small" style="margin-left: 4px;">
                    离线
                  </a-tag>
                  <a-tag v-else color="warning" size="small" style="margin-left: 4px;">
                    故障
                  </a-tag>
                </div>
              </template>

              <!-- 设备类型 -->
              <template v-else-if="column.dataIndex === 'deviceType'">
                <a-tag :color="getDeviceTypeColor(record.deviceType)">
                  {{ getDeviceTypeLabel(record.deviceType) }}
                </a-tag>
              </template>

              <!-- 厂商 -->
              <template v-else-if="column.dataIndex === 'manufacturer'">
                <a-tag color="blue">
                  {{ getManufacturerLabel(record.manufacturer) }}
                </a-tag>
              </template>

              <!-- IP地址 -->
              <template v-else-if="column.dataIndex === 'ipAddress'">
                <a-space>
                  <span>{{ record.ipAddress }}</span>
                  <a-tag size="small" color="green">{{ record.port }}</a-tag>
                </a-space>
              </template>

              <!-- 通道数 -->
              <template v-else-if="column.dataIndex === 'channelCount'">
                <a-tag color="purple">通道: {{ record.channelCount }}</a-tag>
              </template>

              <!-- 状态 -->
              <template v-else-if="column.dataIndex === 'status'">
                <a-badge
                  :status="record.status === 'online' ? 'success' : record.status === 'fault' ? 'error' : 'default'"
                  :text="getDeviceStatusLabel(record.status)"
                />
              </template>

              <!-- 最后心跳 -->
              <template v-else-if="column.dataIndex === 'lastHeartbeat'">
                <a-tooltip :title="formatLastHeartbeat(record.lastHeartbeat)">
                  <span style="color: #999;">{{ formatTimeAgo(record.lastHeartbeat) }}</span>
                </a-tooltip>
              </template>

              <!-- 操作 -->
              <template v-else-if="column.dataIndex === 'action'">
                <a-space direction="vertical" :size="4">
                  <a-space>
                    <a-button type="link" size="small" @click="editDevice(record)">
                      <template #icon><EditOutlined /></template>
                      编辑
                    </a-button>

                    <a-button type="link" size="small" @click="viewDevice(record)">
                      <template #icon><EyeOutlined /></template>
                      详情
                    </a-button>

                    <a-dropdown>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item @click="configDevice(record)">
                            <template #icon><SettingOutlined /></template>
                            设备配置
                          </a-menu-item>
                          <a-menu-item @click="manageChannels(record)">
                            <template #icon><ApartmentOutlined /></template>
                            通道管理
                          </a-menu-item>
                          <a-menu-item @click="viewRealtime(record)">
                            <template #icon><PlayCircleOutlined /></template>
                            实时预览
                          </a-menu-item>
                          <a-menu-item @click="playbackRecord(record)">
                            <template #icon><HistoryOutlined /></template>
                            录像回放
                          </a-menu-item>
                          <a-menu-divider />
                          <a-menu-item @click="syncSingleDevice(record)">
                            <template #icon><SyncOutlined /></template>
                            同步信息
                          </a-menu-item>
                          <a-menu-item @click="testConnection(record)">
                            <template #icon><ApiOutlined /></template>
                            测试连接
                          </a-menu-item>
                          <a-menu-item @click="restartDevice(record)">
                            <template #icon><ReloadOutlined /></template>
                            重启设备
                          </a-menu-item>
                          <a-menu-item @click="viewLogs(record)">
                            <template #icon><FileTextOutlined /></template>
                            查看日志
                          </a-menu-item>
                          <a-menu-divider />
                          <a-menu-item @click="deleteDevice(record)" danger>
                            <template #icon><DeleteOutlined /></template>
                            删除设备
                          </a-menu-item>
                        </a-menu>
                      </template>
                      <a-button type="link" size="small">
                        更多
                        <template #icon><DownOutlined /></template>
                      </a-button>
                    </a-dropdown>
                  </a-space>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>

    <!-- 设备表单弹窗 -->
    <DeviceForm
      :visible="modalVisible"
      :is-edit="isEdit"
      :device-data="currentDevice"
      :group-tree-data="groupTreeData"
      @update:visible="modalVisible = $event"
      @success="handleFormSuccess"
      @cancel="handleFormCancel"
    />

    <!-- 设备详情抽屉 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      title="设备详情"
      width="800"
      placement="right"
    >
      <div v-if="currentDeviceDetail" class="device-detail">
        <!-- 基础信息 -->
        <a-descriptions title="基础信息" bordered :column="2">
          <a-descriptions-item label="设备名称">
            {{ currentDeviceDetail.deviceName }}
          </a-descriptions-item>
          <a-descriptions-item label="设备编码">
            {{ currentDeviceDetail.deviceCode }}
          </a-descriptions-item>
          <a-descriptions-item label="设备类型">
            <a-tag :color="getDeviceTypeColor(currentDeviceDetail.deviceType)">
              {{ getDeviceTypeLabel(currentDeviceDetail.deviceType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="厂商">
            {{ getManufacturerLabel(currentDeviceDetail.manufacturer) }}
          </a-descriptions-item>
          <a-descriptions-item label="IP地址">
            {{ currentDeviceDetail.ipAddress }}
          </a-descriptions-item>
          <a-descriptions-item label="端口">
            {{ currentDeviceDetail.port }}
          </a-descriptions-item>
          <a-descriptions-item label="用户名">
            {{ currentDeviceDetail.username }}
          </a-descriptions-item>
          <a-descriptions-item label="通道数">
            <a-tag color="purple">{{ currentDeviceDetail.channelCount }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="所属分组">
            {{ currentDeviceDetail.groupName }}
          </a-descriptions-item>
          <a-descriptions-item label="所属区域">
            {{ currentDeviceDetail.areaName }}
          </a-descriptions-item>
          <a-descriptions-item label="安装位置">
            {{ currentDeviceDetail.installLocation }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-badge
              :status="currentDeviceDetail.status === 'online' ? 'success' : 'default'"
              :text="getDeviceStatusLabel(currentDeviceDetail.status)"
            />
          </a-descriptions-item>
        </a-descriptions>

        <!-- 状态信息 -->
        <a-descriptions title="状态信息" bordered :column="2" style="margin-top: 24px;">
          <a-descriptions-item label="设备状态">
            <a-tag :color="currentDeviceDetail.status === 'online' ? 'success' : 'default'">
              {{ getDeviceStatusLabel(currentDeviceDetail.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="运行时间">
            {{ formatRuntime(currentDeviceDetail.runtime) }}
          </a-descriptions-item>
          <a-descriptions-item label="注册时间">
            {{ currentDeviceDetail.registerTime }}
          </a-descriptions-item>
          <a-descriptions-item label="最后心跳">
            {{ currentDeviceDetail.lastHeartbeat }}
          </a-descriptions-item>
          <a-descriptions-item label="CPU使用率">
            <a-progress
              :percent="currentDeviceDetail.cpuUsage || 0"
              size="small"
              :status="currentDeviceDetail.cpuUsage > 80 ? 'exception' : 'normal'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="内存使用率">
            <a-progress
              :percent="currentDeviceDetail.memoryUsage || 0"
              size="small"
              :status="currentDeviceDetail.memoryUsage > 80 ? 'exception' : 'normal'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="磁盘使用率">
            <a-progress
              :percent="currentDeviceDetail.diskUsage || 0"
              size="small"
              :status="currentDeviceDetail.diskUsage > 80 ? 'exception' : 'normal'"
            />
          </a-descriptions-item>
          <a-descriptions-item label="带宽使用">
            {{ currentDeviceDetail.bandwidth || '0 KB/s' }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 设备能力 -->
        <a-descriptions title="设备能力" bordered :column="2" style="margin-top: 24px;">
          <a-descriptions-item label="支持PTZ">
            <a-tag :color="currentDeviceDetail.supportPTZ ? 'success' : 'default'">
              {{ currentDeviceDetail.supportPTZ ? '支持' : '不支持' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="支持录像">
            <a-tag :color="currentDeviceDetail.supportRecord ? 'success' : 'default'">
              {{ currentDeviceDetail.supportRecord ? '支持' : '不支持' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="支持音频">
            <a-tag :color="currentDeviceDetail.supportAudio ? 'success' : 'default'">
              {{ currentDeviceDetail.supportAudio ? '支持' : '不支持' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="支持智能分析">
            <a-tag :color="currentDeviceDetail.supportAI ? 'success' : 'default'">
              {{ currentDeviceDetail.supportAI ? '支持' : '不支持' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="分辨率">
            {{ currentDeviceDetail.resolution || '不支持' }}
          </a-descriptions-item>
          <a-descriptions-item label="帧率">
            {{ currentDeviceDetail.framerate || '不支持' }}
          </a-descriptions-item>
        </a-descriptions>

        <!-- 操作按钮 -->
        <div style="margin-top: 24px; text-align: center;">
          <a-space>
            <a-button type="primary" @click="editDevice(currentDeviceDetail)">
              <template #icon><EditOutlined /></template>
              编辑设备
            </a-button>
            <a-button @click="configDevice(currentDeviceDetail)">
              <template #icon><SettingOutlined /></template>
              设备配置
            </a-button>
            <a-button @click="manageChannels(currentDeviceDetail)">
              <template #icon><ApartmentOutlined /></template>
              通道管理
            </a-button>
            <a-button @click="viewRealtime(currentDeviceDetail)">
              <template #icon><PlayCircleOutlined /></template>
              实时预览
            </a-button>
          </a-space>
        </div>
      </div>
    </a-drawer>

    <!-- 导入弹窗 -->
    <a-modal
      v-model:open="showImportModal"
      title="批量导入设备"
      @ok="handleImport"
      @cancel="showImportModal = false"
    >
      <a-form layout="vertical">
        <a-form-item label="下载模板">
          <a-space>
            <a-button type="primary" ghost @click="downloadTemplate">
              <template #icon><DownloadOutlined /></template>
              下载Excel模板
            </a-button>
            <span style="color: #999;">请先下载模板，填写后上传</span>
          </a-space>
        </a-form-item>

        <a-form-item label="上传文件" required>
          <a-upload
            :file-list="importFileList"
            :before-upload="beforeUpload"
            @remove="handleRemove"
            accept=".xlsx,.xls"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              选择文件
            </a-button>
          </a-upload>
        </a-form-item>

        <a-alert
          message="导入说明"
          description="支持批量导入设备信息，请确保Excel文件格式正确，必填字段不能为空。导入成功后会自动添加到对应分组。"
          type="info"
          show-icon
        />
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined,
  SyncOutlined,
  BellOutlined,
  ClockCircleOutlined,
  SettingOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
  VideoCameraOutlined,
  FolderOutlined,
  DownOutlined,
  UpOutlined,
  RadarChartOutlined,
  ExportOutlined,
  ImportOutlined,
  EditOutlined,
  ApartmentOutlined,
  PlayCircleOutlined,
  HistoryOutlined,
  ApiOutlined,
  PoweroffOutlined,
  FileTextOutlined,
  UpgradeOutlined,
  DownloadOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue';
import DeviceForm from './components/DeviceForm.vue';
import {
  DEVICE_TYPE_ENUM,
  DEVICE_STATUS_ENUM,
  DEVICE_MANUFACTURER_ENUM,
  getDeviceTypeLabel,
  getDeviceTypeColor,
  getDeviceStatusLabel,
  getDeviceStatusColor,
  getManufacturerLabel,
} from '/@/constants/business/smart-video/device-const';
import deviceMockData, { mockGroupTreeData } from './mock/device-mock-data';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

// 统计数据
const statistics = reactive({
  total: 0,
  online: 0,
  offline: 0,
  fault: 0,
  onlineRate: 0
});

// 设备类型统计
const deviceTypeStats = ref([]);

// 查询表单
const queryForm = reactive({
  deviceName: '',
  status: '',
  deviceType: '',
  manufacturer: '',
  ipAddress: '',
  areaId: null,
  channelCount: null,
  registerTimeRange: null,
});

// 表格数据
const tableData = ref([]);
const tableLoading = ref(false);
const selectedRowKeys = ref([]);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列配置
const columns = [
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    width: 60,
    fixed: 'left',
    customRender: ({ index }) => index + 1,
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 200,
    fixed: 'left',
    ellipsis: true,
  },
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 150,
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
    width: 120,
  },
  {
    title: '厂商',
    dataIndex: 'manufacturer',
    key: 'manufacturer',
    width: 100,
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 160,
  },
  {
    title: '通道数',
    dataIndex: 'channelCount',
    key: 'channelCount',
    width: 100,
  },
  {
    title: '所属分组',
    dataIndex: 'groupName',
    key: 'groupName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '所属区域',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 120,
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '最后心跳',
    dataIndex: 'lastHeartbeat',
    key: 'lastHeartbeat',
    width: 150,
  },
  {
    title: '注册时间',
    dataIndex: 'registerTime',
    key: 'registerTime',
    width: 160,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
];

// 行选择
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
}));

// 弹窗相关
const modalVisible = ref(false);
const isEdit = ref(false);
const currentDevice = ref({});

// 详情抽屉
const detailDrawerVisible = ref(false);
const currentDeviceDetail = ref(null);

// 分组树
const groupTreeData = ref(mockGroupTreeData);
const selectedGroupKeys = ref([]);
const expandedGroupKeys = ref(['1', '2', '3']);

// 区域树
const areaTreeData = ref([
  {
    title: '全部区域',
    value: null,
    key: 'all',
    children: [
      { title: 'A栋', value: '1', key: '1-1' },
      { title: 'B栋', value: '2', key: '1-2' },
      { title: 'C栋', value: '3', key: '1-3' },
    ],
  },
]);

// 高级搜索
const showAdvancedSearch = ref(false);

// 导入
const showImportModal = ref(false);
const importFileList = ref([]);

// 切换高级搜索
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value;
};

// 计算在线率进度条
const getOnlineProgress = () => {
  return statistics.total > 0 ? Math.round((statistics.online / statistics.total) * 100) : 0;
};

// 在线率颜色
const getOnlineRateColor = (rate) => {
  if (rate >= 80) return 'success';
  if (rate >= 50) return 'processing';
  return 'error';
};

// 格式化相对时间
const formatTimeAgo = (time) => {
  return dayjs(time).fromNow();
};

// 格式化最后心跳
const formatLastHeartbeat = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 格式化运行时间
const formatRuntime = (seconds) => {
  if (!seconds) return '未知';
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);

  if (days > 0) return `${days}天${hours}小时`;
  if (hours > 0) return `${hours}小时${minutes}分钟`;
  return `${minutes}分钟`;
};

// 获取行样式
const getRowClassName = (record) => {
  if (record.status === 'offline') return 'row-offline';
  if (record.status === 'fault') return 'row-fault';
  return '';
};

// 分组选择
const onGroupSelect = (selectedKeys, info) => {
  console.log('选中分组:', selectedKeys, info);
  queryData();
};

// 刷新分组树
const refreshGroupTree = () => {
  message.success('分组树已刷新');
};

// 查询数据
const queryData = () => {
  tableLoading.value = true;

  setTimeout(() => {
    const params = {
      ...queryForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    };

    const result = deviceMockData.mockQueryDeviceList(params);

    if (result.code === 1) {
      tableData.value = result.data.list;
      pagination.total = result.data.total;

      // 更新统计数据
      updateStatistics();
    }

    tableLoading.value = false;
  }, 300);
};

// 更新统计数据
const updateStatistics = () => {
  const total = tableData.value.length;
  const online = tableData.value.filter(d => d.status === 'online').length;
  const offline = tableData.value.filter(d => d.status === 'offline').length;
  const fault = tableData.value.filter(d => d.status === 'fault').length;

  statistics.total = total;
  statistics.online = online;
  statistics.offline = offline;
  statistics.fault = fault;
  statistics.onlineRate = total > 0 ? Math.round((online / total) * 100) : 0;

  // 设备类型统计
  const typeMap = {};
  tableData.value.forEach(device => {
    if (!typeMap[device.deviceType]) {
      typeMap[device.deviceType] = 0;
    }
    typeMap[device.deviceType]++;
  });

  deviceTypeStats.value = Object.keys(typeMap).map(type => ({
    type,
    label: getDeviceTypeLabel(type),
    count: typeMap[type],
    color: getDeviceTypeColor(type),
  }));
};

// 重置查询
const resetQuery = () => {
  Object.assign(queryForm, {
    deviceName: '',
    status: '',
    deviceType: '',
    manufacturer: '',
    ipAddress: '',
    areaId: null,
    channelCount: null,
    registerTimeRange: null,
  });
  pagination.current = 1;
  selectedGroupKeys.value = [];
  queryData();
};

// 表格变化处理
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryData();
};

// 显示新增弹窗
const showAddModal = () => {
  isEdit.value = false;
  currentDevice.value = {};
  modalVisible.value = true;
};

// 编辑设备
const editDevice = (record) => {
  isEdit.value = true;
  currentDevice.value = { ...record };
  modalVisible.value = true;
};

// 查看设备详情
const viewDevice = (record) => {
  currentDeviceDetail.value = {
    ...record,
    supportPTZ: true,
    supportRecord: true,
    supportAudio: true,
    supportAI: true,
    resolution: '1920x1080',
    framerate: '25fps',
    runtime: 86400 * 3 + 3600 * 5,
    cpuUsage: 45,
    memoryUsage: 62,
    diskUsage: 78,
    bandwidth: '2048 KB/s',
  };
  detailDrawerVisible.value = true;
};

// 删除设备
const deleteDevice = async (record) => {
  const result = deviceMockData.mockDeleteDevice(record.id);
  if (result.code === 1) {
    message.success('删除成功');
    queryData();
  }
};

// 批量删除
const batchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的设备');
    return;
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个设备吗？此操作不可恢复！`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      const result = deviceMockData.mockBatchDeleteDevice(selectedRowKeys.value);
      if (result.code === 1) {
        message.success(result.msg);
        selectedRowKeys.value = [];
        queryData();
      }
    },
  });
};

// 搜索在线设备
const searchDevices = () => {
  message.loading('正在搜索在线设备...', 1);
  setTimeout(() => {
    message.success('搜索完成，发现 3 台新设备');
  }, 2000);
};

// 批量同步
const syncDevices = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要同步的设备');
    return;
  }
  message.loading('正在同步设备信息...', 1);
  setTimeout(() => {
    message.success(`成功同步 ${selectedRowKeys.value.length} 台设备信息`);
    selectedRowKeys.value = [];
    queryData();
  }, 2000);
};

// 导出设备
const exportDevices = () => {
  message.success('设备数据导出成功');
};

// 批量配置
const batchConfig = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要配置的设备');
    return;
  }
  message.info(`批量配置 ${selectedRowKeys.value.length} 台设备`);
};

// 批量订阅
const batchSubscribe = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要订阅的设备');
    return;
  }
  message.success(`已订阅 ${selectedRowKeys.value.length} 台设备`);
};

// 批量取消订阅
const batchUnsubscribe = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要取消订阅的设备');
    return;
  }
  message.success(`已取消订阅 ${selectedRowKeys.value.length} 台设备`);
};

// 批量校时
const batchSyncTime = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要校时的设备');
    return;
  }
  message.loading('正在批量校时...', 1);
  setTimeout(() => {
    message.success('时间同步成功');
  }, 1500);
};

// 批量重启
const batchRestart = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要重启的设备');
    return;
  }

  Modal.confirm({
    title: '确认重启',
    content: `确定要重启选中的 ${selectedRowKeys.value.length} 台设备吗？设备重启期间将无法访问！`,
    onOk: () => {
      message.loading('正在发送重启指令...', 1);
      setTimeout(() => {
        message.success('重启指令已发送');
        selectedRowKeys.value = [];
      }, 1500);
    },
  });
};

// 批量升级
const batchUpgrade = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要升级的设备');
    return;
  }
  message.info(`批量升级 ${selectedRowKeys.value.length} 台设备`);
};

// 设备配置
const configDevice = (record) => {
  message.info(`配置设备: ${record.deviceName}`);
};

// 通道管理
const manageChannels = (record) => {
  message.info(`管理通道: ${record.deviceName} (${record.channelCount}个通道)`);
};

// 实时预览
const viewRealtime = (record) => {
  message.info(`实时预览: ${record.deviceName}`);
};

// 录像回放
const playbackRecord = (record) => {
  message.info(`录像回放: ${record.deviceName}`);
};

// 同步单个设备
const syncSingleDevice = (record) => {
  message.loading('正在同步设备信息...', 1);
  setTimeout(() => {
    message.success('同步成功');
    queryData();
  }, 1000);
};

// 测试连接
const testConnection = (record) => {
  message.loading('正在测试连接...', 1);
  setTimeout(() => {
    message.success('连接测试成功，延迟 45ms');
  }, 1000);
};

// 重启设备
const restartDevice = (record) => {
  Modal.confirm({
    title: '确认重启',
    content: `确定要重启设备"${record.deviceName}"吗？设备重启期间将无法访问！`,
    onOk: () => {
      message.loading('正在发送重启指令...', 1);
      setTimeout(() => {
        message.success('重启指令已发送');
      }, 1000);
    },
  });
};

// 查看日志
const viewLogs = (record) => {
  message.info(`查看设备日志: ${record.deviceName}`);
};

// 下载模板
const downloadTemplate = () => {
  message.success('模板下载成功');
};

// 上传前检查
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                file.type === 'application/vnd.ms-excel';
  if (!isExcel) {
    message.error('只能上传 Excel 文件！');
    return false;
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB！');
    return false;
  }
  importFileList.value = [file];
  return false;
};

// 移除文件
const handleRemove = () => {
  importFileList.value = [];
};

// 处理导入
const handleImport = () => {
  if (importFileList.value.length === 0) {
    message.warning('请选择要导入的文件');
    return;
  }
  message.loading('正在导入...', 1);
  setTimeout(() => {
    message.success('导入成功，共导入 10 台设备');
    showImportModal.value = false;
    importFileList.value = [];
    queryData();
  }, 2000);
};

// 表单提交成功
const handleFormSuccess = () => {
  queryData();
};

// 表单取消
const handleFormCancel = () => {
  currentDevice.value = {};
};

// 初始化
onMounted(() => {
  queryData();
});
</script>

<style scoped lang="less">
.smart-video-device {
  padding: 24px;
  background-color: #f0f2f5;

  .statistics-cards {
    margin-bottom: 16px;

    .stat-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
      }

      :deep(.ant-statistic-title) {
        color: rgba(255, 255, 255, 0.9);
      }

      :deep(.ant-statistic-content) {
        color: white;
      }

      &:nth-child(2) {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &:nth-child(3) {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &:nth-child(4) {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }
  }

  .device-name-cell {
    display: flex;
    align-items: center;

    .anticon {
      color: #1890ff;
      font-size: 16px;
    }
  }

  .device-detail {
    :deep(.ant-descriptions-item-label) {
      font-weight: 500;
    }
  }
}

// 离线行样式
:deep(.row-offline) {
  background-color: #fafafa;
  opacity: 0.7;
}

// 故障行样式
:deep(.row-fault) {
  background-color: #fff2f0;
}

.smart-margin-top10 {
  margin-top: 10px;
}

.smart-query-table-form {
  margin-bottom: 0;
}

.smart-query-table-operation {
  margin-bottom: 16px;
}
</style>
