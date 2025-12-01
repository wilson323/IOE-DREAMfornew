// 实时数据相关类型定义

export interface RealtimeMessage {
  /** 消息唯一标识 */
  id: string;
  /** 消息主题 */
  topic: string;
  /** 消息类型 */
  type: 'data' | 'alert' | 'event' | 'status' | 'heartbeat';
  /** 消息载荷 */
  payload: any;
  /** 时间戳 */
  timestamp: number;
  /** 消息优先级 */
  priority: 'low' | 'medium' | 'high' | 'urgent';
  /** 发送者 */
  sender?: string;
  /** 目标接收者 */
  receiver?: string;
  /** 消息标签 */
  tags?: string[];
  /** 是否已确认 */
  acknowledged?: boolean;
}

export interface WebSocketConfig {
  /** WebSocket服务地址 */
  url: string;
  /** 连接协议 ws/wss */
  protocol?: 'ws' | 'wss';
  /** 连接超时时间（毫秒） */
  timeout?: number;
  /** 心跳间隔（毫秒） */
  heartbeatInterval?: number;
  /** 最大重连次数 */
  maxReconnectAttempts?: number;
  /** 重连间隔（毫秒） */
  reconnectInterval?: number;
  /** 认证token */
  token?: string;
  /** 是否启用消息压缩 */
  enableCompression?: boolean;
  /** 是否启用消息加密 */
  enableEncryption?: boolean;
  /** 消息加密密钥 */
  encryptionKey?: string;
  /** 自定义请求头 */
  headers?: Record<string, string>;
}

export interface ConnectionStatus {
  /** 连接状态 */
  status: 'connecting' | 'connected' | 'disconnecting' | 'disconnected' | 'reconnecting' | 'error';
  /** 连接ID */
  connectionId?: string;
  /** 连接时间 */
  connectedAt?: number;
  /** 最后活跃时间 */
  lastActiveAt?: number;
  /** 重连次数 */
  reconnectCount?: number;
  /** 错误信息 */
  error?: string;
  /** 连接统计 */
  statistics?: ConnectionStatistics;
}

export interface ConnectionStatistics {
  /** 总连接次数 */
  totalConnections: number;
  /** 总断开次数 */
  totalDisconnections: number;
  /** 总重连次数 */
  totalReconnections: number;
  /** 总发送消息数 */
  totalMessagesSent: number;
  /** 总接收消息数 */
  totalMessagesReceived: number;
  /** 平均连接时长（毫秒） */
  averageConnectionDuration: number;
  /** 当前连接时长（毫秒） */
  currentConnectionDuration: number;
}

export interface SubscriptionInfo {
  /** 订阅ID */
  id: string;
  /** 订阅主题 */
  topic: string;
  /** 订阅类型 */
  type: 'subscribe' | 'unsubscribe';
  /** 订阅条件 */
  filters?: SubscriptionFilter[];
  /** 订阅时间 */
  subscribedAt: number;
  /** 是否活跃 */
  active: boolean;
  /** 订阅统计 */
  statistics?: SubscriptionStatistics;
}

export interface SubscriptionFilter {
  /** 过滤字段 */
  field: string;
  /** 过滤操作符 */
  operator: 'eq' | 'ne' | 'gt' | 'gte' | 'lt' | 'lte' | 'in' | 'nin' | 'contains' | 'regex';
  /** 过滤值 */
  value: any;
  /** 逻辑操作符 */
  logicalOperator?: 'and' | 'or';
}

export interface SubscriptionStatistics {
  /** 接收消息数 */
  messagesReceived: number;
  /** 最后接收时间 */
  lastReceivedAt: number;
  /** 平均消息大小（字节） */
  averageMessageSize: number;
  /** 订阅时长（毫秒） */
  subscriptionDuration: number;
}

export interface AlertInfo {
  /** 告警ID */
  id: string;
  /** 告警标题 */
  title: string;
  /** 告警内容 */
  content: string;
  /** 告警级别 */
  level: 'info' | 'warning' | 'error' | 'critical';
  /** 告警来源 */
  source: string;
  /** 告警类型 */
  type: string;
  /** 告警时间 */
  timestamp: number;
  /** 是否已读 */
  read: boolean;
  /** 是否已确认 */
  acknowledged: boolean;
  /** 告警状态 */
  status: 'active' | 'resolved' | 'suppressed';
  /** 影响范围 */
  affectedSystems?: string[];
  /** 处理建议 */
  recommendations?: string[];
  /** 相关标签 */
  tags?: string[];
}

export interface DeviceMonitorData {
  /** 设备ID */
  deviceId: string;
  /** 设备名称 */
  deviceName: string;
  /** 设备类型 */
  deviceType: string;
  /** 设备状态 */
  status: 'online' | 'offline' | 'error' | 'maintenance';
  /** 最后上报时间 */
  lastReportTime: number;
  /** 设备指标 */
  metrics: DeviceMetric[];
  /** 告警信息 */
  alerts?: AlertInfo[];
  /** 位置信息 */
  location?: {
    latitude: number;
    longitude: number;
    address: string;
  };
}

export interface DeviceMetric {
  /** 指标名称 */
  name: string;
  /** 指标值 */
  value: number | string;
  /** 指标单位 */
  unit?: string;
  /** 指标类型 */
  type: 'number' | 'string' | 'boolean';
  /** 更新时间 */
  timestamp: number;
  /** 是否异常 */
  abnormal?: boolean;
  /** 阈值范围 */
  threshold?: {
    min?: number;
    max?: number;
    optimal?: {
      min: number;
      max: number;
    };
  };
}

export interface SystemMonitorData {
  /** CPU使用率 */
  cpuUsage: number;
  /** 内存使用率 */
  memoryUsage: number;
  /** 磁盘使用率 */
  diskUsage: number;
  /** 网络流量 */
  networkTraffic: {
    inbound: number;
    outbound: number;
  };
  /** 在线用户数 */
  onlineUsers: number;
  /** 活跃连接数 */
  activeConnections: number;
  /** 消息队列长度 */
  messageQueueLength: number;
  /** 系统负载 */
  systemLoad: number;
  /** 更新时间 */
  timestamp: number;
}

export interface RealtimeChartData {
  /** 数据系列 */
  series: {
    /** 系列名称 */
    name: string;
    /** 数据点 */
    data: Array<{
      /** 时间戳 */
      timestamp: number;
      /** 数值 */
      value: number;
      /** 标签 */
      label?: string;
    }>;
    /** 系列颜色 */
    color?: string;
    /** 系列类型 */
    type?: 'line' | 'bar' | 'area';
  }[];
  /** 时间范围 */
  timeRange: {
    start: number;
    end: number;
  };
  /** 数据更新时间 */
  lastUpdated: number;
}

export interface MessageQueueInfo {
  /** 队列名称 */
  name: string;
  /** 队列类型 */
  type: 'redis' | 'rabbitmq' | 'kafka';
  /** 队列大小 */
  size: number;
  /** 消费者数量 */
  consumers: number;
  /** 生产速率（消息/秒） */
  productionRate: number;
  /** 消费速率（消息/秒） */
  consumptionRate: number;
  /** 平均延迟（毫秒） */
  averageLatency: number;
  /** 错误率 */
  errorRate: number;
  /** 更新时间 */
  timestamp: number;
}

export interface EventInfo {
  /** 事件ID */
  id: string;
  /** 事件名称 */
  name: string;
  /** 事件类型 */
  type: string;
  /** 事件源 */
  source: string;
  /** 事件数据 */
  data: any;
  /** 事件时间 */
  timestamp: number;
  /** 事件优先级 */
  priority: 'low' | 'medium' | 'high' | 'urgent';
  /** 事件标签 */
  tags?: string[];
  /** 事件状态 */
  status: 'pending' | 'processing' | 'completed' | 'failed';
  /** 重试次数 */
  retryCount?: number;
  /** 最大重试次数 */
  maxRetries?: number;
}

// WebSocket事件类型
export type WebSocketEventType =
  | 'open'
  | 'close'
  | 'error'
  | 'message'
  | 'reconnect'
  | 'heartbeat'
  | 'subscription'
  | 'unsubscription';

// 实时数据组件事件类型
export type RealtimeComponentEventType =
  | 'data-update'
  | 'alert-received'
  | 'connection-changed'
  | 'subscription-changed'
  | 'device-status-changed'
  | 'system-metrics-updated';

// 工具函数类型
export type EventHandler<T = any> = (data: T) => void;
export type AsyncEventHandler<T = any> = (data: T) => Promise<void>;

// 配置选项类型
export interface RealtimeOptions {
  /** 是否启用调试模式 */
  debug?: boolean;
  /** 是否启用自动重连 */
  autoReconnect?: boolean;
  /** 消息缓存大小 */
  messageCacheSize?: number;
  /** 是否启用本地存储 */
  enableLocalStorage?: boolean;
  /** 数据更新间隔（毫秒） */
  updateInterval?: number;
}

// 数据可视化配置
export interface ChartConfig {
  /** 图表类型 */
  type: 'line' | 'bar' | 'pie' | 'gauge' | 'heatmap';
  /** 图表尺寸 */
  size: {
    width: number;
    height: number;
  };
  /** 图表主题 */
  theme?: 'light' | 'dark';
  /** 是否启用动画 */
  animation?: boolean;
  /** 刷新间隔（毫秒） */
  refreshInterval?: number;
  /** 数据点数量限制 */
  dataPointsLimit?: number;
}