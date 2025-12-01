/**
 * 位置管理相关类型定义
 */

// 基础位置点接口
export interface LocationPoint {
  /** 位置记录ID */
  recordId?: string;
  /** 用户ID */
  userId?: number;
  /** 设备ID */
  deviceId?: string;
  /** 纬度 */
  latitude: number;
  /** 经度 */
  longitude: number;
  /** 海拔高度（米） */
  altitude?: number;
  /** 定位精度（米） */
  accuracy: number;
  /** 定位方式 */
  positioningMethod: PositioningMethod;
  /** 位置来源 */
  locationSource?: LocationSource;
  /** 速度（米/秒） */
  speed?: number;
  /** 方向角（度） */
  bearing?: number;
  /** 定位时间 */
  positioningTime: string;
  /** 地址信息 */
  address?: string;
  /** 位置描述 */
  description?: string;
  /** 楼层信息 */
  floor?: string;
  /** 创建时间 */
  createTime?: string;
  /** 更新时间 */
  updateTime?: string;
}

// 定位方式枚举
export type PositioningMethod =
  | 'GPS'        // GPS定位
  | 'WIFI'       // WiFi定位
  | 'CELLULAR'   // 基站定位
  | 'BLUETOOTH'  // 蓝牙定位
  | 'MANUAL'     // 手动定位
  | 'HYBRID';    // 混合定位

// 位置来源枚举
export type LocationSource =
  | 'BROWSER'    // 浏览器定位
  | 'MOBILE_APP' // 移动应用
  | 'DEVICE'     // 设备上报
  | 'MANUAL'     // 手动输入
  | 'IMPORT'     // 数据导入
  | 'API';       // API接口

// 地图标记接口
export interface MapMarker {
  /** 标记ID */
  id: string;
  /** 位置坐标 */
  position: [number, number];
  /** 标记标题 */
  title: string;
  /** 标记图标 */
  icon?: string;
  /** 弹窗内容 */
  popup?: string;
  /** 关联数据 */
  data?: any;
  /** 点击事件处理函数 */
  onClick?: () => void;
}

// 地图折线接口
export interface MapPolyline {
  /** 折线ID */
  id: string;
  /** 坐标点数组 */
  positions: [number, number][];
  /** 线条颜色 */
  color?: string;
  /** 线条宽度 */
  weight?: number;
  /** 透明度 */
  opacity?: number;
  /** 虚线样式 */
  dashArray?: string;
  /** 点击事件处理函数 */
  onClick?: () => void;
}

// 地图多边形接口
export interface MapPolygon {
  /** 多边形ID */
  id: string;
  /** 顶点坐标数组 */
  positions: [number, number][];
  /** 边框颜色 */
  color?: string;
  /** 填充颜色 */
  fillColor?: string;
  /** 填充透明度 */
  fillOpacity?: number;
  /** 边框宽度 */
  weight?: number;
  /** 点击事件处理函数 */
  onClick?: () => void;
}

// 地图圆形接口
export interface MapCircle {
  /** 圆形ID */
  id: string;
  /** 圆心坐标 */
  center: [number, number];
  /** 半径（米） */
  radius: number;
  /** 边框颜色 */
  color?: string;
  /** 填充颜色 */
  fillColor?: string;
  /** 填充透明度 */
  fillOpacity?: number;
  /** 边框宽度 */
  weight?: number;
  /** 点击事件处理函数 */
  onClick?: () => void;
}

// 地理围栏接口
export interface GeoFence {
  /** 围栏ID */
  fenceId: string;
  /** 围栏名称 */
  fenceName: string;
  /** 围栏描述 */
  description?: string;
  /** 围栏类型 */
  fenceType: GeoFenceType;
  /** 围栏状态 */
  status: GeoFenceStatus;
  /** 触发类型 */
  triggerType: TriggerType;
  /** 告警等级 */
  alertLevel: AlertLevel;
  /** 通知方式 */
  notificationMethods?: string;
  /** 生效开始时间 */
  activeTimeStart?: string;
  /** 生效结束时间 */
  activeTimeEnd?: string;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;

  // 圆形围栏参数
  centerLatitude?: number;
  centerLongitude?: number;
  radius?: number;

  // 矩形围栏参数
  minLatitude?: number;
  maxLatitude?: number;
  minLongitude?: number;
  maxLongitude?: number;

  // 多边形围栏参数
  polygonCoordinates?: string;
}

// 地理围栏类型枚举
export type GeoFenceType =
  | 'CIRCULAR'   // 圆形围栏
  | 'POLYGON'    // 多边形围栏
  | 'RECTANGLE'; // 矩形围栏

// 地理围栏状态枚举
export type GeoFenceStatus =
  | 'ACTIVE'     // 启用
  | 'INACTIVE';  // 禁用

// 触发类型枚举
export type TriggerType =
  | 'IN'         // 进入时触发
  | 'OUT'        // 离开时触发
  | 'BOTH';      // 进入和离开都触发

// 告警等级枚举
export type AlertLevel =
  | 'LOW'        // 低
  | 'MEDIUM'     // 中
  | 'HIGH'       // 高
  | 'CRITICAL';  // 紧急

// 地理围栏触发记录接口
export interface GeoFenceTrigger {
  /** 触发记录ID */
  triggerId: string;
  /** 围栏ID */
  fenceId: string;
  /** 围栏名称 */
  fenceName: string;
  /** 用户ID */
  userId?: number;
  /** 设备ID */
  deviceId?: string;
  /** 触发类型 */
  triggerType: TriggerType;
  /** 告警等级 */
  alertLevel: AlertLevel;
  /** 触发时间 */
  triggerTime: string;
  /** 触发位置 */
  latitude: number;
  longitude: number;
  /** 处理状态 */
  processStatus: TriggerProcessStatus;
  /** 处理时间 */
  processTime?: string;
  /** 处理人 */
  processBy?: string;
  /** 备注信息 */
  remark?: string;
}

// 触发处理状态枚举
export type TriggerProcessStatus =
  | 'PENDING'    // 待处理
  | 'PROCESSING' // 处理中
  | 'RESOLVED'   // 已解决
  | 'IGNORED';   // 已忽略

// 热力图数据点接口
export interface HeatmapPoint {
  /** 纬度 */
  latitude: number;
  /** 经度 */
  longitude: number;
  /** 权重值 */
  weight: number;
  /** 时间戳 */
  timestamp?: string;
}

// 轨迹点接口
export interface TrajectoryPoint extends LocationPoint {
  /** 轨迹序号 */
  sequenceNumber: number;
  /** 停留时间（秒） */
  stayDuration?: number;
  /** 是否停留点 */
  isStopPoint?: boolean;
}

// 位置统计数据接口
export interface LocationStatistics {
  /** 今日位置更新次数 */
  todayUpdates: number;
  /** 活跃用户数 */
  activeUsers: number;
  /** 总距离（米） */
  totalDistance: number;
  /** 平均精度（米） */
  avgAccuracy: number;
  /** 最高速度（米/秒） */
  maxSpeed: number;
  /** 停留点数量 */
  stopsCount: number;
  /** 有效定位率 */
  validLocationRate: number;
}

// 实时追踪目标接口
export interface TrackingTarget {
  /** 目标ID */
  id: string;
  /** 目标类型 */
  type: 'user' | 'device';
  /** 目标名称 */
  name: string;
  /** 当前位置 */
  currentPosition?: LocationPoint;
  /** 追踪开始时间 */
  startTime: string;
  /** 追踪状态 */
  status: TrackingStatus;
  /** 最后更新时间 */
  lastUpdateTime: string;
}

// 追踪状态枚举
export type TrackingStatus =
  | 'ACTIVE'     // 追踪中
  | 'PAUSED'     // 暂停
  | 'STOPPED'    // 已停止
  | 'ERROR';     // 错误

// 位置搜索参数接口
export interface LocationSearchParams {
  /** 目标类型 */
  targetType?: 'user' | 'device';
  /** 目标ID */
  targetId?: string | number;
  /** 定位方式 */
  positioningMethod?: PositioningMethod;
  /** 开始时间 */
  startTime?: string;
  /** 结束时间 */
  endTime?: string;
  /** 精度范围（最小） */
  minAccuracy?: number;
  /** 精度范围（最大） */
  maxAccuracy?: number;
  /** 区域边界 */
  bounds?: {
    minLat: number;
    maxLat: number;
    minLng: number;
    maxLng: number;
  };
  /** 分页参数 */
  pageNum?: number;
  pageSize?: number;
}

// 位置搜索结果接口
export interface LocationSearchResult {
  /** 位置记录列表 */
  data: LocationPoint[];
  /** 总记录数 */
  total: number;
  /** 当前页码 */
  pageNum: number;
  /** 页面大小 */
  pageSize: number;
  /** 总页数 */
  totalPages: number;
}

// 附近位置搜索参数接口
export interface NearbySearchParams {
  /** 中心点纬度 */
  latitude: number;
  /** 中心点经度 */
  longitude: number;
  /** 搜索半径（米） */
  radius: number;
  /** 限制数量 */
  limit?: number;
  /** 位置类型 */
  locationType?: string;
  /** 排除用户ID */
  excludeUserId?: number;
  /** 排除设备ID */
  excludeDeviceId?: string;
}

// 附近位置结果接口
export interface NearbyLocation {
  /** 位置ID */
  locationId: string;
  /** 位置名称 */
  locationName: string;
  /** 位置描述 */
  description?: string;
  /** 位置类型 */
  locationType: string;
  /** 纬度 */
  latitude: number;
  /** 经度 */
  longitude: number;
  /** 距离（米） */
  distance: number;
  /** 地址 */
  address?: string;
}

// 位置分析参数接口
export interface LocationAnalysisParams {
  /** 用户ID列表 */
  userIds?: number[];
  /** 设备ID列表 */
  deviceIds?: string[];
  /** 分析开始时间 */
  startTime: string;
  /** 分析结束时间 */
  endTime: string;
  /** 分析类型 */
  analysisType: AnalysisType;
  /** 时间粒度 */
  timeGranularity?: TimeGranularity;
  /** 区域边界 */
  areaBounds?: {
    minLat: number;
    maxLat: number;
    minLng: number;
    maxLng: number;
  };
}

// 分析类型枚举
export type AnalysisType =
  | 'HEATMAP'        // 热力图分析
  | 'DENSITY'        // 密度分析
  | 'MOVEMENT'       // 移动分析
  | 'STAY_TIME'      // 停留时间分析
  | 'FREQUENCY'      // 频率分析
  | 'CORRELATION';   // 关联分析

// 时间粒度枚举
export type TimeGranularity =
  | 'MINUTE'         // 分钟
  | 'HOUR'           // 小时
  | 'DAY'            // 天
  | 'WEEK'           // 周
  | 'MONTH';         // 月

// 位置分析结果接口
export interface LocationAnalysisResult {
  /** 分析类型 */
  analysisType: AnalysisType;
  /** 分析时间范围 */
  timeRange: {
    startTime: string;
    endTime: string;
  };
  /** 分析数据 */
  data: any;
  /** 统计信息 */
  statistics: {
    totalPoints: number;
    validPoints: number;
    analysisPeriod: number;
  };
  /** 生成时间 */
  generatedAt: string;
}

// 位置导出参数接口
export interface LocationExportParams {
  /** 导出类型 */
  exportType: ExportType;
  /** 时间范围 */
  timeRange: {
    startTime: string;
    endTime: string;
  };
  /** 用户ID列表 */
  userIds?: number[];
  /** 设备ID列表 */
  deviceIds?: string[];
  /** 导出格式 */
  format: ExportFormat;
  /** 是否包含详细信息 */
  includeDetails?: boolean;
}

// 导出类型枚举
export type ExportType =
  | 'LOCATION_HISTORY' // 位置历史
  | 'TRAJECTORY'        // 轨迹数据
  | 'STATISTICS'        // 统计数据
  | 'GEOFENCE'          // 地理围栏
  | 'TRIGGERS';         // 触发记录

// 导出格式枚举
export type ExportFormat =
  | 'JSON'   // JSON格式
  | 'CSV'    // CSV格式
  | 'EXCEL'  // Excel格式
  | 'KML';   // KML格式

// 地图配置接口
export interface MapConfig {
  /** 地图中心 */
  center: [number, number];
  /** 缩放级别 */
  zoom: number;
  /** 地图类型 */
  mapType: MapType;
  /** 最小缩放级别 */
  minZoom?: number;
  /** 最大缩放级别 */
  maxZoom?: number;
  /** 是否显示控件 */
  showControls?: boolean;
  /** 是否显示坐标 */
  showCoordinates?: boolean;
  /** 主题样式 */
  theme?: MapTheme;
}

// 地图类型枚举
export type MapType =
  | 'standard'   // 标准地图
  | 'satellite'  // 卫星地图
  | 'terrain'    // 地形地图
  | 'hybrid';    // 混合地图

// 地图主题枚举
export type MapTheme =
  | 'light'      // 浅色主题
  | 'dark'       // 深色主题
  | 'auto';      // 自动主题

// WebSocket位置消息接口
export interface LocationWebSocketMessage {
  /** 消息类型 */
  type: 'position_update' | 'geofence_trigger' | 'system_status';
  /** 消息时间戳 */
  timestamp: string;
  /** 数据载荷 */
  payload: any;
}

// 位置更新消息载荷
export interface PositionUpdatePayload {
  /** 目标ID */
  targetId: string;
  /** 目标类型 */
  targetType: 'user' | 'device';
  /** 位置信息 */
  position: LocationPoint;
}

// 地理围栏触发消息载荷
export interface GeoFenceTriggerPayload {
  /** 触发记录 */
  trigger: GeoFenceTrigger;
  /** 位置信息 */
  position: LocationPoint;
}

// 地图事件接口
export interface MapEvent {
  /** 事件类型 */
  type: 'click' | 'zoom' | 'move' | 'bounds_change';
  /** 事件时间戳 */
  timestamp: number;
  /** 事件数据 */
  data: any;
}

// 地图点击事件数据
export interface MapClickEventData {
  /** 点击位置 */
  latlng: {
    lat: number;
    lng: number;
  };
  /** 点击的图层 */
  layer?: any;
  /** 屏幕坐标 */
  containerPoint: {
    x: number;
    y: number;
  };
}

// 地图边界变化事件数据
export interface MapBoundsChangeEventData {
  /** 当前边界 */
  bounds: {
    southwest: [number, number];
    northeast: [number, number];
  };
  /** 当前中心点 */
  center: [number, number];
  /** 当前缩放级别 */
  zoom: number;
}

// API响应基础接口
export interface LocationApiResponse<T = any> {
  /** 是否成功 */
  success: boolean;
  /** 响应代码 */
  code: string;
  /** 响应消息 */
  message: string;
  /** 响应数据 */
  data: T;
  /** 时间戳 */
  timestamp: string;
}