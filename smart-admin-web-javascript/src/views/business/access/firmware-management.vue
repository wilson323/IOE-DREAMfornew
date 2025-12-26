<!--
 * 固件管理页面
 *
 * 功能：
 * 1. 固件版本管理：上传、查询、下载、删除固件
 * 2. 升级任务管理：创建升级任务、查看进度、控制任务状态
 * 3. 设备升级监控：实时查看设备升级进度和状态
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="firmware-management-page">
    <a-card :bordered="false">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <!-- 固件版本管理 -->
        <a-tab-pane key="firmware" tab="固件版本管理">
          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="固件名称" class="smart-query-form-item">
                <a-input
                  style="width: 200px"
                  v-model:value="firmwareQueryForm.firmwareName"
                  placeholder="请输入固件名称"
                  allow-clear
                />
              </a-form-item>

              <a-form-item label="设备类型" class="smart-query-form-item">
                <a-select
                  v-model:value="firmwareQueryForm.deviceType"
                  style="width: 150px"
                  :allow-clear="true"
                  placeholder="请选择设备类型"
                >
                  <a-select-option :value="1">门禁设备</a-select-option>
                  <a-select-option :value="2">考勤设备</a-select-option>
                  <a-select-option :value="3">消费设备</a-select-option>
                  <a-select-option :value="4">视频设备</a-select-option>
                  <a-select-option :value="5">访客设备</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item label="固件状态" class="smart-query-form-item">
                <a-select
                  v-model:value="firmwareQueryForm.firmwareStatus"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择状态"
                >
                  <a-select-option :value="1">测试中</a-select-option>
                  <a-select-option :value="2">已发布</a-select-option>
                  <a-select-option :value="3">已废弃</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryFirmwareList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetFirmwareQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 操作按钮 -->
          <a-space style="margin-bottom: 16px">
            <a-button type="primary" @click="handleUploadFirmware">
              <template #icon><UploadOutlined /></template>
              上传固件
            </a-button>
          </a-space>

          <!-- 固件列表表格 -->
          <a-table
            :columns="firmwareColumns"
            :data-source="firmwareTableData"
            :pagination="firmwarePagination"
            :loading="firmwareLoading"
            row-key="firmwareId"
            @change="handleFirmwareTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'firmwareStatus'">
                <a-tag :color="getFirmwareStatusColor(record.firmwareStatus)">
                  {{ getFirmwareStatusText(record.firmwareStatus) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'isEnabled'">
                <a-tag :color="record.isEnabled === 1 ? 'green' : 'red'">
                  {{ record.isEnabled === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'isForce'">
                <a-tag :color="record.isForce === 1 ? 'red' : 'blue'">
                  {{ record.isForce === 1 ? '强制' : '可选' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'fileSize'">
                {{ formatFileSize(record.firmwareFileSize) }}
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleViewFirmware(record)">
                    详情
                  </a-button>
                  <a-button type="link" size="small" @click="handleDownloadFirmware(record)">
                    下载
                  </a-button>
                  <a-dropdown>
                    <a-button type="link" size="small">
                      更多 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item @click="handleUpdateFirmwareStatus(record, 2)" v-if="record.firmwareStatus !== 2">
                          发布固件
                        </a-menu-item>
                        <a-menu-item @click="handleToggleEnabled(record)">
                          {{ record.isEnabled === 1 ? '禁用' : '启用' }}
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item @click="handleDeleteFirmware(record)" danger>
                          删除固件
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 升级任务管理 -->
        <a-tab-pane key="upgrade" tab="升级任务管理">
          <!-- 查询表单 -->
          <a-form class="smart-query-form" layout="inline" style="margin-bottom: 16px">
            <a-row class="smart-query-form-row">
              <a-form-item label="任务名称" class="smart-query-form-item">
                <a-input
                  style="width: 200px"
                  v-model:value="taskQueryForm.taskName"
                  placeholder="请输入任务名称"
                  allow-clear
                />
              </a-form-item>

              <a-form-item label="任务状态" class="smart-query-form-item">
                <a-select
                  v-model:value="taskQueryForm.taskStatus"
                  style="width: 120px"
                  :allow-clear="true"
                  placeholder="请选择状态"
                >
                  <a-select-option :value="1">待执行</a-select-option>
                  <a-select-option :value="2">执行中</a-select-option>
                  <a-select-option :value="3">已暂停</a-select-option>
                  <a-select-option :value="4">已完成</a-select-option>
                  <a-select-option :value="5">已失败</a-select-option>
                </a-select>
              </a-form-item>

              <a-form-item class="smart-query-form-item smart-margin-left10">
                <a-space>
                  <a-button type="primary" @click="queryTaskList">
                    <template #icon><SearchOutlined /></template>
                    查询
                  </a-button>
                  <a-button @click="resetTaskQuery">
                    <template #icon><ReloadOutlined /></template>
                    重置
                  </a-button>
                </a-space>
              </a-form-item>
            </a-row>
          </a-form>

          <!-- 操作按钮 -->
          <a-space style="margin-bottom: 16px">
            <a-button type="primary" @click="handleCreateUpgradeTask">
              <template #icon><PlusOutlined /></template>
              创建升级任务
            </a-button>
          </a-space>

          <!-- 任务列表表格 -->
          <a-table
            :columns="taskColumns"
            :data-source="taskTableData"
            :pagination="taskPagination"
            :loading="taskLoading"
            row-key="taskId"
            @change="handleTaskTableChange"
            :expandable="{ expandedRowRender, expandIcon: 'column' }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'taskStatus'">
                <a-badge
                  :status="getTaskStatusBadge(record.taskStatus)"
                  :text="getTaskStatusText(record.taskStatus)"
                />
              </template>
              <template v-else-if="column.key === 'upgradeStrategy'">
                <a-tag :color="getStrategyColor(record.upgradeStrategy)">
                  {{ getStrategyText(record.upgradeStrategy) }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'upgradeProgress'">
                <a-progress
                  :percent="parseFloat(record.upgradeProgress) || 0"
                  :status="getProgressStatus(record)"
                  size="small"
                />
              </template>
              <template v-else-if="column.key === 'statistics'">
                <a-space direction="vertical" :size="0">
                  <span>目标: {{ record.targetDeviceCount }}台</span>
                  <span style="color: #52c41a">成功: {{ record.successCount }}台</span>
                  <span style="color: #ff4d4f" v-if="record.failedCount > 0">
                    失败: {{ record.failedCount }}台
                  </span>
                  <span style="color: #faad14" v-if="record.pendingCount > 0">
                    待升级: {{ record.pendingCount }}台
                  </span>
                </a-space>
              </template>
              <template v-else-if="column.key === 'rollbackSupported'">
                <a-tag :color="record.rollbackSupported === 1 ? 'green' : 'default'">
                  {{ record.rollbackSupported === 1 ? '支持' : '不支持' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="handleViewTaskDetail(record)">
                    详情
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleStartTask(record)"
                    v-if="record.taskStatus === 1 || record.taskStatus === 3"
                  >
                    {{ record.taskStatus === 1 ? '启动' : '恢复' }}
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handlePauseTask(record)"
                    v-if="record.taskStatus === 2"
                  >
                    暂停
                  </a-button>
                  <a-button
                    type="link"
                    size="small"
                    @click="handleRetryTask(record)"
                    v-if="record.failedCount > 0 && record.taskStatus !== 2"
                  >
                    重试失败
                  </a-button>
                  <a-dropdown>
                    <a-button type="link" size="small">
                      更多 <DownOutlined />
                    </a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item
                          @click="handleRollbackTask(record)"
                          v-if="record.rollbackSupported === 1 && record.taskStatus === 4"
                        >
                          回滚升级
                        </a-menu-item>
                        <a-menu-item @click="handleStopTask(record)" v-if="record.taskStatus === 2">
                          停止任务
                        </a-menu-item>
                        <a-menu-divider />
                        <a-menu-item @click="handleDeleteTask(record)" danger>
                          删除任务
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 上传固件弹窗 -->
    <a-modal
      v-model:visible="uploadModalVisible"
      title="上传固件"
      :width="600"
      @ok="handleUploadSubmit"
      @cancel="uploadModalVisible = false"
      :confirm-loading="uploading"
    >
      <a-form :model="uploadForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="固件文件" required>
          <a-upload
            :file-list="fileList"
            :before-upload="beforeUpload"
            @remove="handleRemoveFile"
            :max-count="1"
          >
            <a-button>
              <template #icon><UploadOutlined /></template>
              选择固件文件
            </a-button>
          </a-upload>
          <div style="margin-top: 8px; color: #999">
            支持 .bin、.pkg、.zip 格式，最大100MB
          </div>
        </a-form-item>

        <a-form-item label="固件名称" required>
          <a-input
            v-model:value="uploadForm.firmwareName"
            placeholder="请输入固件名称"
            :max-length="200"
          />
        </a-form-item>

        <a-form-item label="固件版本" required>
          <a-input
            v-model:value="uploadForm.firmwareVersion"
            placeholder="例如: v1.0.0"
            :max-length="50"
          />
        </a-form-item>

        <a-form-item label="设备类型" required>
          <a-select v-model:value="uploadForm.deviceType" placeholder="请选择设备类型">
            <a-select-option :value="1">门禁设备</a-select-option>
            <a-select-option :value="2">考勤设备</a-select-option>
            <a-select-option :value="3">消费设备</a-select-option>
            <a-select-option :value="4">视频设备</a-select-option>
            <a-select-option :value="5">访客设备</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="设备型号">
          <a-input v-model:value="uploadForm.deviceModel" placeholder="请输入设备型号" />
        </a-form-item>

        <a-form-item label="设备厂商">
          <a-input v-model:value="uploadForm.brand" placeholder="请输入设备厂商" />
        </a-form-item>

        <a-form-item label="最低版本">
          <a-input
            v-model:value="uploadForm.minVersion"
            placeholder="最低兼容版本，例如: v0.9.0"
          />
        </a-form-item>

        <a-form-item label="最高版本">
          <a-input
            v-model:value="uploadForm.maxVersion"
            placeholder="最高兼容版本，例如: v2.0.0"
          />
        </a-form-item>

        <a-form-item label="强制升级">
          <a-switch v-model:checked="uploadForm.isForceChecked" />
          <span style="margin-left: 8px; color: #999">
            开启后，即使版本不兼容也会强制升级
          </span>
        </a-form-item>

        <a-form-item label="发布说明">
          <a-textarea
            v-model:value="uploadForm.releaseNotes"
            :rows="4"
            placeholder="请输入发布说明"
            :max-length="2000"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 创建升级任务弹窗 -->
    <a-modal
      v-model:visible="taskCreateModalVisible"
      title="创建升级任务"
      :width="800"
      @ok="handleCreateTaskSubmit"
      @cancel="taskCreateModalVisible = false"
      :confirm-loading="creatingTask"
    >
      <a-form :model="taskForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="任务名称" required>
          <a-input
            v-model:value="taskForm.taskName"
            placeholder="请输入任务名称"
            :max-length="200"
          />
        </a-form-item>

        <a-form-item label="选择固件" required>
          <a-select
            v-model:value="taskForm.firmwareId"
            placeholder="请选择固件"
            show-search
            :filter-option="filterFirmwareOption"
          >
            <a-select-option
              v-for="firmware in availableFirmwareList"
              :key="firmware.firmwareId"
              :value="firmware.firmwareId"
            >
              {{ firmware.firmwareName }} ({{ firmware.firmwareVersion }})
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="升级策略" required>
          <a-radio-group v-model:value="taskForm.upgradeStrategy">
            <a-radio :value="1">立即升级</a-radio>
            <a-radio :value="2">定时升级</a-radio>
            <a-radio :value="3">分批升级</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="定时时间" v-if="taskForm.upgradeStrategy === 2" required>
          <a-date-picker
            v-model:value="taskForm.scheduleTime"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择执行时间"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="分批大小" v-if="taskForm.upgradeStrategy === 3">
          <a-input-number
            v-model:value="taskForm.batchSize"
            :min="1"
            :max="100"
            style="width: 100%"
          />
          <span style="margin-left: 8px">台/批</span>
        </a-form-item>

        <a-form-item label="分批间隔" v-if="taskForm.upgradeStrategy === 3">
          <a-input-number
            v-model:value="taskForm.batchInterval"
            :min="60"
            :max="3600"
            style="width: 100%"
          />
          <span style="margin-left: 8px">秒</span>
        </a-form-item>

        <a-form-item label="支持回滚">
          <a-switch v-model:checked="taskForm.rollbackSupportedChecked" />
          <span style="margin-left: 8px; color: #999">
            开启后，升级失败时可以回滚到原版本
          </span>
        </a-form-item>

        <a-form-item label="目标设备" required>
          <a-button @click="handleSelectDevices" style="margin-bottom: 8px">
            <template #icon><SearchOutlined /></template>
            选择设备
          </a-button>
          <div v-if="taskForm.deviceIds && taskForm.deviceIds.length > 0">
            已选择 <span style="color: #1890ff">{{ taskForm.deviceIds.length }}</span> 台设备
          </div>
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea
            v-model:value="taskForm.remark"
            :rows="3"
            placeholder="请输入备注信息"
            :max-length="500"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 任务详情弹窗 -->
    <a-modal
      v-model:visible="taskDetailModalVisible"
      title="升级任务详情"
      :width="1000"
      :footer="null"
    >
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="任务名称" :span="2">
          {{ currentTask.taskName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务编号">
          {{ currentTask.taskNo }}
        </a-descriptions-item>
        <a-descriptions-item label="任务状态">
          <a-badge
            :status="getTaskStatusBadge(currentTask.taskStatus)"
            :text="getTaskStatusText(currentTask.taskStatus)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="固件版本">
          {{ currentTask.firmwareVersion }}
        </a-descriptions-item>
        <a-descriptions-item label="升级策略">
          <a-tag :color="getStrategyColor(currentTask.upgradeStrategy)">
            {{ getStrategyText(currentTask.upgradeStrategy) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="升级进度" :span="2">
          <a-progress
            :percent="parseFloat(currentTask.upgradeProgress) || 0"
            :status="getProgressStatus(currentTask)"
          />
        </a-descriptions-item>
        <a-descriptions-item label="目标设备">
          {{ currentTask.targetDeviceCount }}台
        </a-descriptions-item>
        <a-descriptions-item label="成功设备">
          <span style="color: #52c41a">{{ currentTask.successCount }}台</span>
        </a-descriptions-item>
        <a-descriptions-item label="失败设备">
          <span style="color: #ff4d4f">{{ currentTask.failedCount }}台</span>
        </a-descriptions-item>
        <a-descriptions-item label="待升级">
          <span style="color: #faad14">{{ currentTask.pendingCount }}台</span>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(currentTask.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="执行耗时">
          {{ currentTask.durationFormatted || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 设备升级明细 -->
      <a-divider>设备升级明细</a-divider>
      <a-table
        :columns="deviceDetailColumns"
        :data-source="deviceDetailList"
        :pagination="false"
        :loading="deviceDetailLoading"
        row-key="detailId"
        size="small"
        :scroll="{ y: 400 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'upgradeStatus'">
            <a-badge
              :status="getDeviceStatusBadge(record.upgradeStatus)"
              :text="getDeviceStatusText(record.upgradeStatus)"
            />
          </template>
          <template v-else-if="column.key === 'durationFormatted'">
            {{ record.durationFormatted || '-' }}
          </template>
          <template v-else-if="column.key === 'errorMessage'">
            <a-tooltip v-if="record.errorMessage" :title="record.errorMessage">
              <span style="color: #ff4d4f">{{ record.errorMessage }}</span>
            </a-tooltip>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 设备选择弹窗 -->
    <DeviceSelectModal
      v-model:visible="deviceSelectModalVisible"
      @confirm="handleDevicesSelected"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, computed } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    DownOutlined,
    UploadOutlined
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';
  import DeviceSelectModal from './components/DeviceSelectModal.vue';

  // ==================== 状态定义 ====================

  const activeTab = ref('firmware');

  // 固件管理状态
  const firmwareQueryForm = reactive({
    firmwareName: '',
    deviceType: undefined,
    firmwareStatus: undefined
  });

  const firmwareTableData = ref([]);
  const firmwareLoading = ref(false);
  const firmwarePagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
  });

  // 升级任务管理状态
  const taskQueryForm = reactive({
    taskName: '',
    taskStatus: undefined
  });

  const taskTableData = ref([]);
  const taskLoading = ref(false);
  const taskPagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`
  });

  // 弹窗状态
  const uploadModalVisible = ref(false);
  const taskCreateModalVisible = ref(false);
  const taskDetailModalVisible = ref(false);
  const deviceSelectModalVisible = ref(false);

  const uploading = ref(false);
  const creatingTask = ref(false);

  // 表单数据
  const fileList = ref([]);
  const uploadForm = reactive({
    firmwareName: '',
    firmwareVersion: '',
    deviceType: undefined,
    deviceModel: '',
    brand: '',
    minVersion: '',
    maxVersion: '',
    isForceChecked: false,
    releaseNotes: ''
  });

  const taskForm = reactive({
    taskName: '',
    firmwareId: undefined,
    upgradeStrategy: 1,
    scheduleTime: null,
    batchSize: 10,
    batchInterval: 300,
    rollbackSupportedChecked: false,
    deviceIds: [],
    remark: ''
  });

  const availableFirmwareList = ref([]);
  const currentTask = ref({});
  const deviceDetailList = ref([]);
  const deviceDetailLoading = ref(false);

  // ==================== 表格列定义 ====================

  const firmwareColumns = [
    {
      title: '固件ID',
      dataIndex: 'firmwareId',
      key: 'firmwareId',
      width: 100
    },
    {
      title: '固件名称',
      dataIndex: 'firmwareName',
      key: 'firmwareName',
      ellipsis: true
    },
    {
      title: '固件版本',
      dataIndex: 'firmwareVersion',
      key: 'firmwareVersion',
      width: 120
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 100,
      customRender: ({ record }) => getDeviceTypeText(record.deviceType)
    },
    {
      title: '设备型号',
      dataIndex: 'deviceModel',
      key: 'deviceModel',
      width: 120
    },
    {
      title: '设备厂商',
      dataIndex: 'brand',
      key: 'brand',
      width: 120
    },
    {
      title: '文件大小',
      dataIndex: 'firmwareFileSize',
      key: 'fileSize',
      width: 100
    },
    {
      title: 'MD5校验',
      dataIndex: 'firmwareFileMd5',
      key: 'firmwareFileMd5',
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'firmwareStatus',
      key: 'firmwareStatus',
      width: 100
    },
    {
      title: '启用',
      dataIndex: 'isEnabled',
      key: 'isEnabled',
      width: 80
    },
    {
      title: '升级类型',
      dataIndex: 'isForce',
      key: 'isForce',
      width: 80
    },
    {
      title: '下载次数',
      dataIndex: 'downloadCount',
      key: 'downloadCount',
      width: 100
    },
    {
      title: '升级次数',
      dataIndex: 'upgradeCount',
      key: 'upgradeCount',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 180
    }
  ];

  const taskColumns = [
    {
      title: '任务ID',
      dataIndex: 'taskId',
      key: 'taskId',
      width: 80
    },
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      ellipsis: true
    },
    {
      title: '任务编号',
      dataIndex: 'taskNo',
      key: 'taskNo',
      width: 160
    },
    {
      title: '固件版本',
      dataIndex: 'firmwareVersion',
      key: 'firmwareVersion',
      width: 100
    },
    {
      title: '升级策略',
      dataIndex: 'upgradeStrategy',
      key: 'upgradeStrategy',
      width: 100
    },
    {
      title: '任务状态',
      dataIndex: 'taskStatus',
      key: 'taskStatus',
      width: 100
    },
    {
      title: '升级进度',
      dataIndex: 'upgradeProgress',
      key: 'upgradeProgress',
      width: 150
    },
    {
      title: '统计信息',
      key: 'statistics',
      width: 150
    },
    {
      title: '支持回滚',
      dataIndex: 'rollbackSupported',
      key: 'rollbackSupported',
      width: 100
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 200
    }
  ];

  const deviceDetailColumns = [
    {
      title: '设备ID',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 80
    },
    {
      title: '设备编号',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
      width: 150
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
      ellipsis: true
    },
    {
      title: '当前版本',
      dataIndex: 'currentVersion',
      key: 'currentVersion',
      width: 100
    },
    {
      title: '目标版本',
      dataIndex: 'targetVersion',
      key: 'targetVersion',
      width: 100
    },
    {
      title: '升级状态',
      dataIndex: 'upgradeStatus',
      key: 'upgradeStatus',
      width: 100
    },
    {
      title: '耗时',
      dataIndex: 'durationFormatted',
      key: 'durationFormatted',
      width: 100
    },
    {
      title: '重试次数',
      dataIndex: 'retryCount',
      key: 'retryCount',
      width: 80,
      customRender: ({ record }) => `${record.retryCount}/${record.maxRetry}`
    },
    {
      title: '错误信息',
      dataIndex: 'errorMessage',
      key: 'errorMessage',
      ellipsis: true
    }
  ];

  // ==================== 生命周期 ====================

  onMounted(() => {
    queryFirmwareList();
    loadAvailableFirmware();
  });

  // ==================== 固件管理方法 ====================

  async function queryFirmwareList() {
    try {
      firmwareLoading.value = true;
      const params = {
        ...firmwareQueryForm,
        pageNum: firmwarePagination.current,
        pageSize: firmwarePagination.pageSize
      };

      const response = await accessApi.queryFirmwarePage(params);
      if (response.code === 200) {
        firmwareTableData.value = response.data.list || [];
        firmwarePagination.total = response.data.total || 0;
      } else {
        message.error(response.message || '查询固件列表失败');
      }
    } catch (e) {
      console.error('查询固件列表异常:', e);
      message.error('查询固件列表异常');
    } finally {
      firmwareLoading.value = false;
    }
  }

  function resetFirmwareQuery() {
    Object.assign(firmwareQueryForm, {
      firmwareName: '',
      deviceType: undefined,
      firmwareStatus: undefined
    });
    firmwarePagination.current = 1;
    queryFirmwareList();
  }

  function handleFirmwareTableChange(pagination) {
    firmwarePagination.current = pagination.current;
    firmwarePagination.pageSize = pagination.pageSize;
    queryFirmwareList();
  }

  function handleUploadFirmware() {
    uploadModalVisible.value = true;
  }

  function beforeUpload(file) {
    const isValidType = ['bin', 'pkg', 'zip'].includes(
      file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase()
    );
    if (!isValidType) {
      message.error('只支持 .bin、.pkg、.zip 格式的固件文件');
      return false;
    }

    const isValidSize = file.size / 1024 / 1024 < 100;
    if (!isValidSize) {
      message.error('文件大小不能超过 100MB');
      return false;
    }

    fileList.value = [file];
    return false; // 阻止自动上传
  }

  function handleRemoveFile() {
    fileList.value = [];
  }

  async function handleUploadSubmit() {
    // 表单验证
    if (fileList.value.length === 0) {
      message.warning('请选择固件文件');
      return;
    }
    if (!uploadForm.firmwareName) {
      message.warning('请输入固件名称');
      return;
    }
    if (!uploadForm.firmwareVersion) {
      message.warning('请输入固件版本');
      return;
    }
    if (!uploadForm.deviceType) {
      message.warning('请选择设备类型');
      return;
    }

    try {
      uploading.value = true;

      const formData = new FormData();
      formData.append('file', fileList.value[0]);
      formData.append('firmwareName', uploadForm.firmwareName);
      formData.append('firmwareVersion', uploadForm.firmwareVersion);
      formData.append('deviceType', uploadForm.deviceType);
      formData.append('deviceModel', uploadForm.deviceModel || '');
      formData.append('brand', uploadForm.brand || '');
      formData.append('minVersion', uploadForm.minVersion || '');
      formData.append('maxVersion', uploadForm.maxVersion || '');
      formData.append('isForce', uploadForm.isForceChecked ? 1 : 0);
      formData.append('releaseNotes', uploadForm.releaseNotes || '');

      const response = await accessApi.uploadFirmware(formData);
      if (response.code === 200) {
        message.success('固件上传成功');
        uploadModalVisible.value = false;
        resetUploadForm();
        queryFirmwareList();
      } else {
        message.error(response.message || '固件上传失败');
      }
    } catch (e) {
      console.error('上传固件异常:', e);
      message.error('上传固件异常');
    } finally {
      uploading.value = false;
    }
  }

  function resetUploadForm() {
    Object.assign(uploadForm, {
      firmwareName: '',
      firmwareVersion: '',
      deviceType: undefined,
      deviceModel: '',
      brand: '',
      minVersion: '',
      maxVersion: '',
      isForceChecked: false,
      releaseNotes: ''
    });
    fileList.value = [];
  }

  async function handleDownloadFirmware(record) {
    try {
      const response = await accessApi.downloadFirmware(record.firmwareId);
      if (response.code === 200 && response.data.firmwareFilePath) {
        // 创建下载链接
        const link = document.createElement('a');
        link.href = response.data.firmwareFilePath;
        link.download = response.data.firmwareFileName || record.firmwareName;
        link.click();

        // 增加下载次数
        await accessApi.incrementFirmwareDownloadCount(record.firmwareId);
        message.success('下载成功');
        queryFirmwareList();
      } else {
        message.error(response.message || '下载失败');
      }
    } catch (e) {
      console.error('下载固件异常:', e);
      message.error('下载固件异常');
    }
  }

  async function handleUpdateFirmwareStatus(record, status) {
    try {
      const response = await accessApi.updateFirmwareStatus(record.firmwareId, status);
      if (response.code === 200) {
        message.success('固件状态更新成功');
        queryFirmwareList();
      } else {
        message.error(response.message || '状态更新失败');
      }
    } catch (e) {
      console.error('更新固件状态异常:', e);
      message.error('更新固件状态异常');
    }
  }

  async function handleToggleEnabled(record) {
    try {
      const newEnabled = record.isEnabled === 1 ? 0 : 1;
      const response = await accessApi.updateFirmwareEnabled(record.firmwareId, newEnabled);
      if (response.code === 200) {
        message.success('固件启用状态更新成功');
        queryFirmwareList();
      } else {
        message.error(response.message || '状态更新失败');
      }
    } catch (e) {
      console.error('更新固件启用状态异常:', e);
      message.error('更新固件启用状态异常');
    }
  }

  function handleDeleteFirmware(record) {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除固件 "${record.firmwareName}" 吗？`,
      onOk: async () => {
        try {
          const response = await accessApi.deleteFirmware(record.firmwareId);
          if (response.code === 200) {
            message.success('固件删除成功');
            queryFirmwareList();
          } else {
            message.error(response.message || '删除失败');
          }
        } catch (e) {
          console.error('删除固件异常:', e);
          message.error('删除固件异常');
        }
      }
    });
  }

  function handleViewFirmware(record) {
    // TODO: 显示固件详情弹窗
    message.info('固件详情功能开发中');
  }

  // ==================== 升级任务管理方法 ====================

  async function queryTaskList() {
    try {
      taskLoading.value = true;
      const params = {
        ...taskQueryForm,
        pageNum: taskPagination.current,
        pageSize: taskPagination.pageSize
      };

      const response = await accessApi.queryUpgradeTasksPage(params);
      if (response.code === 200) {
        taskTableData.value = response.data.list || [];
        taskPagination.total = response.data.total || 0;
      } else {
        message.error(response.message || '查询升级任务列表失败');
      }
    } catch (e) {
      console.error('查询升级任务列表异常:', e);
      message.error('查询升级任务列表异常');
    } finally {
      taskLoading.value = false;
    }
  }

  function resetTaskQuery() {
    Object.assign(taskQueryForm, {
      taskName: '',
      taskStatus: undefined
    });
    taskPagination.current = 1;
    queryTaskList();
  }

  function handleTaskTableChange(pagination) {
    taskPagination.current = pagination.current;
    taskPagination.pageSize = pagination.pageSize;
    queryTaskList();
  }

  function handleTabChange(activeKey) {
    if (activeKey === 'upgrade') {
      queryTaskList();
    }
  }

  async function loadAvailableFirmware() {
    try {
      const response = await accessApi.queryAvailableFirmware();
      if (response.code === 200) {
        availableFirmwareList.value = response.data || [];
      }
    } catch (e) {
      console.error('加载可用固件列表异常:', e);
    }
  }

  function filterFirmwareOption(input, option) {
    return option.children[0].children.toLowerCase().includes(input.toLowerCase());
  }

  function handleCreateUpgradeTask() {
    taskCreateModalVisible.value = true;
    loadAvailableFirmware();
  }

  function handleSelectDevices() {
    deviceSelectModalVisible.value = true;
  }

  function handleDevicesSelected(selectedDevices) {
    taskForm.deviceIds = selectedDevices.map(d => d.deviceId);
    message.success(`已选择 ${taskForm.deviceIds.length} 台设备`);
  }

  async function handleCreateTaskSubmit() {
    // 表单验证
    if (!taskForm.taskName) {
      message.warning('请输入任务名称');
      return;
    }
    if (!taskForm.firmwareId) {
      message.warning('请选择固件');
      return;
    }
    if (taskForm.upgradeStrategy === 2 && !taskForm.scheduleTime) {
      message.warning('请选择定时执行时间');
      return;
    }
    if (!taskForm.deviceIds || taskForm.deviceIds.length === 0) {
      message.warning('请选择目标设备');
      return;
    }

    try {
      creatingTask.value = true;

      const params = {
        taskName: taskForm.taskName,
        firmwareId: taskForm.firmwareId,
        upgradeStrategy: taskForm.upgradeStrategy,
        scheduleTime: taskForm.scheduleTime ? taskForm.scheduleTime.format('YYYY-MM-DD HH:mm:ss') : null,
        batchSize: taskForm.batchSize,
        batchInterval: taskForm.batchInterval,
        rollbackSupported: taskForm.rollbackSupportedChecked ? 1 : 0,
        deviceIds: taskForm.deviceIds,
        remark: taskForm.remark
      };

      const response = await accessApi.createUpgradeTask(params);
      if (response.code === 200) {
        message.success('升级任务创建成功');
        taskCreateModalVisible.value = false;
        resetTaskForm();
        queryTaskList();
      } else {
        message.error(response.message || '创建升级任务失败');
      }
    } catch (e) {
      console.error('创建升级任务异常:', e);
      message.error('创建升级任务异常');
    } finally {
      creatingTask.value = false;
    }
  }

  function resetTaskForm() {
    Object.assign(taskForm, {
      taskName: '',
      firmwareId: undefined,
      upgradeStrategy: 1,
      scheduleTime: null,
      batchSize: 10,
      batchInterval: 300,
      rollbackSupportedChecked: false,
      deviceIds: [],
      remark: ''
    });
  }

  async function handleStartTask(record) {
    try {
      const response = await accessApi.startUpgradeTask(record.taskId);
      if (response.code === 200) {
        message.success('升级任务已启动');
        queryTaskList();
      } else {
        message.error(response.message || '启动任务失败');
      }
    } catch (e) {
      console.error('启动升级任务异常:', e);
      message.error('启动升级任务异常');
    }
  }

  async function handlePauseTask(record) {
    try {
      const response = await accessApi.pauseUpgradeTask(record.taskId);
      if (response.code === 200) {
        message.success('升级任务已暂停');
        queryTaskList();
      } else {
        message.error(response.message || '暂停任务失败');
      }
    } catch (e) {
      console.error('暂停升级任务异常:', e);
      message.error('暂停升级任务异常');
    }
  }

  async function handleRetryTask(record) {
    try {
      const response = await accessApi.retryFailedDevices(record.taskId);
      if (response.code === 200) {
        message.success(`已触发重试，重试 ${response.data} 台设备`);
        queryTaskList();
      } else {
        message.error(response.message || '重试失败');
      }
    } catch (e) {
      console.error('重试失败设备异常:', e);
      message.error('重试失败设备异常');
    }
  }

  function handleStopTask(record) {
    Modal.confirm({
      title: '确认停止',
      content: '确定要停止该升级任务吗？停止后将无法继续执行。',
      onOk: async () => {
        try {
          const response = await accessApi.stopUpgradeTask(record.taskId);
          if (response.code === 200) {
            message.success('升级任务已停止');
            queryTaskList();
          } else {
            message.error(response.message || '停止任务失败');
          }
        } catch (e) {
          console.error('停止升级任务异常:', e);
          message.error('停止升级任务异常');
        }
      }
    });
  }

  function handleDeleteTask(record) {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除升级任务 "${record.taskName}" 吗？`,
      onOk: async () => {
        try {
          const response = await accessApi.deleteUpgradeTask(record.taskId);
          if (response.code === 200) {
            message.success('升级任务删除成功');
            queryTaskList();
          } else {
            message.error(response.message || '删除任务失败');
          }
        } catch (e) {
          console.error('删除升级任务异常:', e);
          message.error('删除升级任务异常');
        }
      }
    });
  }

  async function handleRollbackTask(record) {
    Modal.confirm({
      title: '确认回滚',
      content: '确定要回滚该升级任务吗？回滚将恢复所有设备到升级前的固件版本。',
      onOk: async () => {
        try {
          const response = await accessApi.rollbackUpgradeTask(record.taskId);
          if (response.code === 200) {
            message.success('回滚任务已创建');
            queryTaskList();
          } else {
            message.error(response.message || '回滚失败');
          }
        } catch (e) {
          console.error('回滚升级任务异常:', e);
          message.error('回滚升级任务异常');
        }
      }
    });
  }

  async function handleViewTaskDetail(record) {
    try {
      // 获取任务详情
      const response = await accessApi.getUpgradeTaskDetail(record.taskId);
      if (response.code === 200) {
        currentTask.value = response.data;
        taskDetailModalVisible.value = true;

        // 获取设备升级明细
        await loadDeviceDetailList(record.taskId);
      } else {
        message.error(response.message || '获取任务详情失败');
      }
    } catch (e) {
      console.error('获取任务详情异常:', e);
      message.error('获取任务详情异常');
    }
  }

  async function loadDeviceDetailList(taskId) {
    try {
      deviceDetailLoading.value = true;
      const response = await accessApi.getTaskDevices(taskId);
      if (response.code === 200) {
        deviceDetailList.value = response.data || [];
      }
    } catch (e) {
      console.error('获取设备升级明细异常:', e);
    } finally {
      deviceDetailLoading.value = false;
    }
  }

  // 展开行渲染
  function expandedRowRender(record) {
    return `创建时间: ${formatDateTime(record.createTime)}`;
  }

  // ==================== 工具方法 ====================

  function formatFileSize(bytes) {
    if (!bytes) return '-';
    const units = ['B', 'KB', 'MB', 'GB'];
    let size = bytes;
    let unitIndex = 0;
    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }
    return `${size.toFixed(2)} ${units[unitIndex]}`;
  }

  function formatDateTime(dateTime) {
    if (!dateTime) return '-';
    return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
  }

  function getDeviceTypeText(deviceType) {
    const map = {
      1: '门禁设备',
      2: '考勤设备',
      3: '消费设备',
      4: '视频设备',
      5: '访客设备'
    };
    return map[deviceType] || '未知';
  }

  function getFirmwareStatusColor(status) {
    const map = {
      1: 'blue',      // 测试中
      2: 'green',     // 已发布
      3: 'default'    // 已废弃
    };
    return map[status] || 'default';
  }

  function getFirmwareStatusText(status) {
    const map = {
      1: '测试中',
      2: '已发布',
      3: '已废弃'
    };
    return map[status] || '未知';
  }

  function getTaskStatusBadge(status) {
    const map = {
      1: 'default',   // 待执行
      2: 'processing', // 执行中
      3: 'warning',    // 已暂停
      4: 'success',    // 已完成
      5: 'error'       // 已失败
    };
    return map[status] || 'default';
  }

  function getTaskStatusText(status) {
    const map = {
      1: '待执行',
      2: '执行中',
      3: '已暂停',
      4: '已完成',
      5: '已失败'
    };
    return map[status] || '未知';
  }

  function getStrategyColor(strategy) {
    const map = {
      1: 'red',    // 立即升级
      2: 'blue',   // 定时升级
      3: 'green'   // 分批升级
    };
    return map[strategy] || 'default';
  }

  function getStrategyText(strategy) {
    const map = {
      1: '立即升级',
      2: '定时升级',
      3: '分批升级'
    };
    return map[strategy] || '未知';
  }

  function getDeviceStatusBadge(status) {
    const map = {
      1: 'default',   // 待升级
      2: 'processing', // 升级中
      3: 'success',    // 升级成功
      4: 'error',      // 升级失败
      5: 'warning'     // 已回滚
    };
    return map[status] || 'default';
  }

  function getDeviceStatusText(status) {
    const map = {
      1: '待升级',
      2: '升级中',
      3: '升级成功',
      4: '升级失败',
      5: '已回滚'
    };
    return map[status] || '未知';
  }

  function getProgressStatus(record) {
    if (record.taskStatus === 4) {
      return record.failedCount > 0 ? 'exception' : 'success';
    }
    if (record.taskStatus === 5) {
      return 'exception';
    }
    return 'normal';
  }
</script>

<style lang="less" scoped>
  .firmware-management-page {
    padding: 0;
  }

  .smart-query-form {
    :deep(.ant-form-item) {
      margin-bottom: 8px;
    }
  }

  .smart-query-form-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }

  .smart-query-form-item {
    margin-right: 16px;
    margin-bottom: 8px;
  }

  .smart-margin-left10 {
    margin-left: auto;
  }

  :deep(.ant-table) {
    .ant-badge-status-dot {
      width: 8px;
      height: 8px;
    }
  }
</style>
