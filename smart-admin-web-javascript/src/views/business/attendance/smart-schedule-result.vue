<template>
  <div class="smart-schedule-result">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总适应度"
            :value="fitnessScore"
            :precision="4"
            suffix="%"
            :value-style="{ color: fitnessColor }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="排班员工数"
            :value="statistics.employeeCount"
            suffix="人"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="排班天数"
            :value="statistics.scheduleDays"
            suffix="天"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="冲突数量"
            :value="statistics.conflictCount"
            suffix="个"
            :value-style="{ color: statistics.conflictCount > 0 ? '#cf1322' : '#3f8600' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 优化结果详情 -->
    <a-card title="优化结果详情" :bordered="false" style="margin-bottom: 16px">
      <a-descriptions bordered :column="3">
        <a-descriptions-item label="公平性得分">
          <a-progress
            :percent="resultDetail.fairnessScore ? (resultDetail.fairnessScore * 100).toFixed(2) : 0"
            size="small"
          />
        </a-descriptions-item>
        <a-descriptions-item label="成本得分">
          <a-progress
            :percent="resultDetail.costScore ? (resultDetail.costScore * 100).toFixed(2) : 0"
            size="small"
          />
        </a-descriptions-item>
        <a-descriptions-item label="效率得分">
          <a-progress
            :percent="resultDetail.efficiencyScore ? (resultDetail.efficiencyScore * 100).toFixed(2) : 0"
            size="small"
          />
        </a-descriptions-item>
        <a-descriptions-item label="满意度得分">
          <a-progress
            :percent="resultDetail.satisfactionScore ? (resultDetail.satisfactionScore * 100).toFixed(2) : 0"
            size="small"
          />
        </a-descriptions-item>
        <a-descriptions-item label="迭代次数">
          {{ resultDetail.iterations }} 次
        </a-descriptions-item>
        <a-descriptions-item label="执行时长">
          {{ executionDurationSeconds }} 秒
        </a-descriptions-item>
        <a-descriptions-item label="是否收敛">
          <a-tag :color="resultDetail.converged ? 'success' : 'default'">
            {{ resultDetail.converged ? '已收敛' : '未收敛' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="质量等级">
          <a-tag :color="getQualityColor(resultDetail.qualityLevel)">
            {{ resultDetail.qualityDescription }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <!-- 筛选条件 -->
    <a-card title="筛选条件" :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="员工">
          <a-select
            v-model:value="filterEmployeeId"
            placeholder="全部员工"
            style="width: 200px"
            allowClear
            @change="loadResultList"
          >
            <a-select-option
              v-for="emp in employeeOptions"
              :key="emp.value"
              :value="emp.value"
            >
              {{ emp.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期范围">
          <a-range-picker
            v-model:value="dateRange"
            style="width: 300px"
            @change="loadResultList"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadResultList">
              查询
            </a-button>
            <a-button @click="resetFilter">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 排班结果表格 -->
    <a-card title="排班结果明细" :bordered="false">
      <a-table
        :columns="resultColumns"
        :data-source="resultList"
        :loading="loading"
        :pagination="resultPagination"
        @change="handleResultTableChange"
        row-key="resultId"
        :scroll="{ x: 1500 }"
      >
        <!-- 自定义单元格渲染 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'scheduleDate'">
            <span>{{ record.scheduleDate }} (周{{ getDayOfWeek(record.scheduleDate) }})</span>
          </template>

          <template v-else-if="column.key === 'shiftName'">
            <a-tag :color="record.shiftType === 1 ? 'blue' : 'orange'">
              {{ record.shiftName || '休息' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'workTime'">
            <span v-if="record.workStartTime && record.workEndTime">
              {{ record.workStartTime }} - {{ record.workEndTime }}
            </span>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.key === 'hasConflict'">
            <a-tag v-if="record.hasConflict" color="red">有冲突</a-tag>
            <a-tag v-else color="green">无冲突</a-tag>
          </template>

          <template v-else-if="column.key === 'fitnessScore'">
            <a-progress
              :percent="record.fitnessScore ? (record.fitnessScore * 100).toFixed(2) : 0"
              :status="getFitnessStatus(record.fitnessScore)"
              size="small"
              style="width: 100px"
            />
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { smartScheduleApi } from '@/api/business/attendance/smart-schedule-api';

// Props
const props = defineProps({
  planId: {
    type: Number,
    required: true
  }
});

// 结果表格列定义
const resultColumns = ref([
  {
    title: '员工姓名',
    dataIndex: 'employeeName',
    key: 'employeeName',
    width: 120,
    fixed: 'left'
  },
  {
    title: '排班日期',
    dataIndex: 'scheduleDate',
    key: 'scheduleDate',
    width: 150
  },
  {
    title: '星期',
    dataIndex: 'dayOfWeek',
    key: 'dayOfWeek',
    width: 80
  },
  {
    title: '班次名称',
    dataIndex: 'shiftName',
    key: 'shiftName',
    width: 120
  },
  {
    title: '工作时间',
    dataIndex: 'workTime',
    key: 'workTime',
    width: 180
  },
  {
    title: '工作时长',
    dataIndex: 'workDuration',
    key: 'workDuration',
    width: 100
  },
  {
    title: '连续工作天数',
    dataIndex: 'consecutiveWorkDays',
    key: 'consecutiveWorkDays',
    width: 120
  },
  {
    title: '连续休息天数',
    dataIndex: 'consecutiveRestDays',
    key: 'consecutiveRestDays',
    width: 120
  },
  {
    title: '是否工作日',
    dataIndex: 'isWorkDay',
    key: 'isWorkDay',
    width: 100
  },
  {
    title: '是否存在冲突',
    dataIndex: 'hasConflict',
    key: 'hasConflict',
    width: 100
  },
  {
    title: '适应度得分',
    dataIndex: 'fitnessScore',
    key: 'fitnessScore',
    width: 150
  }
]);

// 数据
const resultList = ref([]);
const loading = ref(false);
const filterEmployeeId = ref(null);
const dateRange = ref([]);
const employeeOptions = ref([]);

// 统计数据
const statistics = reactive({
  employeeCount: 0,
  scheduleDays: 0,
  conflictCount: 0
});

// 结果详情
const resultDetail = reactive({
  fairnessScore: 0,
  costScore: 0,
  efficiencyScore: 0,
  satisfactionScore: 0,
  iterations: 0,
  executionDurationMs: 0,
  converged: false,
  qualityLevel: 0,
  qualityDescription: ''
});

// 分页
const resultPagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

// 计算属性
const fitnessScore = computed(() => {
  return resultDetail.fairnessScore ? (resultDetail.fairnessScore * 100).toFixed(2) : 0;
});

const fitnessColor = computed(() => {
  const score = parseFloat(fitnessScore.value);
  if (score >= 90) return '#3f8600';
  if (score >= 70) return '#52c41a';
  if (score >= 50) return '#faad14';
  return '#f5222d';
});

const executionDurationSeconds = computed(() => {
  return resultDetail.executionDurationMs
    ? (resultDetail.executionDurationMs / 1000).toFixed(2)
    : 0;
});

// 加载结果列表
const loadResultList = async () => {
  loading.value = true;
  try {
    const res = await smartScheduleApi.queryResultPage({
      planId: props.planId,
      pageNum: resultPagination.current,
      pageSize: resultPagination.pageSize,
      employeeId: filterEmployeeId.value,
      startDate: dateRange.value?.[0]?.format('YYYY-MM-DD'),
      endDate: dateRange.value?.[1]?.format('YYYY-MM-DD')
    });
    resultList.value = res.data.list;
    resultPagination.total = res.data.total;

    // 更新统计
    calculateStatistics();
  } catch (error) {
    message.error('加载排班结果失败');
  } finally {
    loading.value = false;
  }
};

// 计算统计数据
const calculateStatistics = () => {
  const employees = new Set();
  const dates = new Set();
  let conflictCount = 0;

  resultList.value.forEach(item => {
    employees.add(item.employeeId);
    dates.add(item.scheduleDate);
    if (item.hasConflict) {
      conflictCount++;
    }
  });

  statistics.employeeCount = employees.size;
  statistics.scheduleDays = dates.size;
  statistics.conflictCount = conflictCount;
};

// 表格变化处理
const handleResultTableChange = (pag) => {
  resultPagination.current = pag.current;
  resultPagination.pageSize = pag.pageSize;
  loadResultList();
};

// 重置筛选
const resetFilter = () => {
  filterEmployeeId.value = null;
  dateRange.value = [];
  loadResultList();
};

// 获取星期几
const getDayOfWeek = (dateStr) => {
  const day = dayjs(dateStr).day();
  const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
  return weekdays[day];
};

// 获取适应度状态
const getFitnessStatus = (score) => {
  if (score >= 0.9) return 'success';
  if (score >= 0.7) return 'normal';
  if (score >= 0.5) return 'active';
  return 'exception';
};

// 获取质量等级颜色
const getQualityColor = (level) => {
  const colors = {
    1: 'green',
    2: 'blue',
    3: 'orange',
    4: 'red'
  };
  return colors[level] || 'default';
};

// 初始化
onMounted(() => {
  loadResultList();
  // TODO: 加载员工选项
});
</script>

<style lang="scss" scoped>
.smart-schedule-result {
  padding: 16px;
}
</style>
