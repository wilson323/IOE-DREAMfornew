/*
 * 数据分析报表TypeScript类型定义
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

// 通用时间类型
export type TimeRange = {
  startDate: string;
  endDate: string;
  startTime?: string;
  endTime?: string;
};

// 通用统计基础类型
export interface BaseStats {
  total: number;
  count: number;
  percentage?: number;
  trend?: number;
}

// 图表基础数据类型
export interface ChartData {
  name: string;
  value: number;
  color?: string;
}

// 时间序列数据类型
export interface TimeSeriesData {
  timestamp: string;
  value: number;
  label?: string;
}

// KPI指标类型
export interface KPIMetrics {
  title: string;
  value: number;
  unit: string;
  trend: number;
  status: 'up' | 'down' | 'stable';
  icon?: string;
  color?: string;
}

// ==================== 通行数据分析相关类型 ====================

export interface TrafficHeatmapData {
  hour: number;
  day: number;
  value: number;
  date: string;
}

export interface AreaTrafficStats {
  areaId: string;
  areaName: string;
  trafficCount: number;
  percentage: number;
  trend: number;
}

export interface PersonTypeStats {
  personType: 'EMPLOYEE' | 'VISITOR' | 'CONTRACTOR' | 'TEMPORARY';
  typeName: string;
  count: number;
  percentage: number;
  accessRate: number;
}

export interface AbnormalTrendData {
  date: string;
  abnormalCount: number;
  totalCount: number;
  abnormalRate: number;
}

export interface TrafficTrendData {
  date: string;
  inboundCount: number;
  outboundCount: number;
  totalCount: number;
  peakHour?: number;
}

export interface PeakHourAnalysis {
  hourRange: string;
  count: number;
  percentage: number;
  dayOfWeek?: string;
}

// ==================== 设备运行分析相关类型 ====================

export interface DeviceOnlineRate {
  onlineCount: number;
  totalCount: number;
  onlineRate: number;
  deviceType: string;
  lastUpdateTime: string;
}

export interface DeviceFailureRate {
  failureCount: number;
  totalCount: number;
  failureRate: number;
  avgRepairTime: number;
  deviceType: string;
}

export interface DeviceUsageRanking {
  deviceId: string;
  deviceName: string;
  deviceType: string;
  usageCount: number;
  usageRate: number;
  rank: number;
}

export interface MaintenanceReminder {
  deviceId: string;
  deviceName: string;
  maintenanceType: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  lastMaintenanceDate: string;
  nextMaintenanceDate: string;
  daysUntilMaintenance: number;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface DevicePerformanceMetrics {
  deviceId: string;
  responseTime: number;
  successRate: number;
  cpuUsage: number;
  memoryUsage: number;
  networkLatency: number;
  timestamp: string;
}

export interface DeviceHealthScore {
  deviceId: string;
  deviceName: string;
  healthScore: number;
  healthLevel: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL';
  factors: {
    availability: number;
    performance: number;
    maintenance: number;
    security: number;
  };
  lastUpdated: string;
}

// ==================== 权限使用分析相关类型 ====================

export interface PermissionApplicationTrend {
  date: string;
  applicationCount: number;
  approvedCount: number;
  rejectedCount: number;
  pendingCount: number;
}

export interface PermissionUsageFrequency {
  permissionType: string;
  totalUsers: number;
  activeUsers: number;
  usageRate: number;
  avgUsageCount: number;
}

export interface ExpiringPermission {
  permissionId: string;
  userId: string;
  userName: string;
  permissionType: string;
  expiryDate: string;
  daysUntilExpiry: number;
  lastUsedDate?: string;
  usageCount: number;
}

export interface PermissionTypeDistribution {
  permissionType: string;
  count: number;
  percentage: number;
  avgDuration: number;
  activeCount: number;
}

export interface PermissionEfficiencyAnalysis {
  permissionType: string;
  totalAssigned: number;
  actuallyUsed: number;
  efficiencyRate: number;
  avgFirstUseTime: number;
  unusedCount: number;
}

// ==================== 安防态势分析相关类型 ====================

export interface SecurityEventTrend {
  date: string;
  totalEvents: number;
  criticalEvents: number;
  warningEvents: number;
  infoEvents: number;
  resolvedEvents: number;
}

export interface HighRiskArea {
  areaId: string;
  areaName: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  eventCount: number;
  lastEventDate: string;
  riskFactors: string[];
}

export interface SecurityLevelDistribution {
  securityLevel: string;
  areaCount: number;
  userCount: number;
  accessCount: number;
  eventCount: number;
}

export interface AlarmHandlingEfficiency {
  alarmType: string;
  totalAlarms: number;
  resolvedAlarms: number;
  avgResolutionTime: number;
  resolutionRate: number;
  efficiencyTrend: number;
}

export interface SecurityEventTypeStats {
  eventType: string;
  eventCount: number;
  percentage: number;
  severityLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  trendDirection: 'UP' | 'DOWN' | 'STABLE';
}

export interface SecurityPostureScore {
  overallScore: number;
  overallLevel: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'CRITICAL';
  categories: {
    accessControl: number;
    surveillance: number;
    alarmSystem: number;
    incidentResponse: number;
    compliance: number;
  };
  lastCalculated: string;
  recommendations: string[];
}

// ==================== 仪表盘相关类型 ====================

export interface DashboardOverview {
  totalDevices: number;
  onlineDevices: number;
  todayTraffic: number;
  activePermissions: number;
  securityEvents: number;
  systemHealth: number;
}

export interface RealTimeUpdate {
  type: 'DEVICE_STATUS' | 'ACCESS_DATA' | 'SECURITY_EVENT' | 'SYSTEM_ALERT';
  timestamp: string;
  data: any;
  severity?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface SystemHealthScore {
  overallHealth: number;
  components: {
    database: number;
    network: number;
    storage: number;
    security: number;
    performance: number;
  };
  lastCheckTime: string;
  alerts: string[];
}

// ==================== 导出相关类型 ====================

export interface ExportRequest {
  reportType: 'TRAFFIC' | 'DEVICE' | 'PERMISSION' | 'SECURITY' | 'DASHBOARD' | 'CUSTOM';
  timeRange: TimeRange;
  format: 'EXCEL' | 'PDF' | 'CSV';
  filters?: Record<string, any>;
  includeCharts?: boolean;
  emailTo?: string[];
}

export interface ExportResult {
  exportId: string;
  fileName: string;
  fileSize: number;
  downloadUrl: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  completedAt?: string;
  errorMessage?: string;
}

// ==================== API响应类型 ====================

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResult<T = any> {
  total: number;
  size: number;
  current: number;
  records: T[];
  pages: number;
}

// ==================== 图表配置类型 ====================

export interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'heatmap' | 'gauge' | 'radar' | 'scatter';
  title: string;
  subtitle?: string;
  xAxis?: {
    type: 'category' | 'value' | 'time';
    data?: string[];
    name?: string;
  };
  yAxis?: {
    type: 'value' | 'category';
    name?: string;
    min?: number;
    max?: number;
  };
  series: ChartSeries[];
  legend?: {
    show: boolean;
    position: 'top' | 'bottom' | 'left' | 'right';
  };
  tooltip?: {
    show: boolean;
    trigger?: 'item' | 'axis';
  };
  colors?: string[];
  height?: number;
  width?: number;
}

export interface ChartSeries {
  name: string;
  type: string;
  data: any[];
  color?: string;
  smooth?: boolean;
  stack?: string;
  yAxisIndex?: number;
  xAxisIndex?: number;
}

// ==================== 查询参数类型 ====================

export interface AnalyticsQueryParams {
  timeRange: TimeRange;
  deviceIds?: string[];
  areaIds?: string[];
  personTypes?: string[];
  securityLevels?: string[];
  chartType?: string;
  pageSize?: number;
  pageNum?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

// ==================== 自定义报表类型 ====================

export interface CustomReportTemplate {
  templateId: string;
  templateName: string;
  description: string;
  charts: ReportChart[];
  filters: ReportFilter[];
  layout: ReportLayout;
  createdBy: string;
  createdAt: string;
  isPublic: boolean;
}

export interface ReportChart {
  chartId: string;
  title: string;
  type: string;
  dataSource: string;
  config: ChartConfig;
  position: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

export interface ReportFilter {
  field: string;
  label: string;
  type: 'DATE_RANGE' | 'SELECT' | 'INPUT' | 'CHECKBOX';
  options?: string[];
  required: boolean;
  defaultValue?: any;
}

export interface ReportLayout {
  columns: number;
  rows: number;
  gap: number;
  padding: number;
}