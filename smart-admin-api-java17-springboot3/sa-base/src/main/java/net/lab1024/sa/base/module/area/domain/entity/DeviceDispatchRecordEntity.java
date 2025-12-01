package net.lab1024.sa.base.module.area.domain.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.alibaba.fastjson2.JSON;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 设备下发执行记录实体类
 * 记录每次人员数据下发的详细执行情况
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_dispatch_record")
public class DeviceDispatchRecordEntity extends BaseEntity {

    /**
     * 记录ID
     */
    @TableId("record_id")
    private Long recordId;

    /**
     * 关联ID（人员区域关联ID）
     */
    @TableField("relation_id")
    private Long relationId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码（冗余字段）
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 设备名称（冗余字段）
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 策略ID
     */
    @TableField("strategy_id")
    private Long strategyId;

    /**
     * 下发类型
     * ADD-新增, UPDATE-更新, DELETE-删除, SYNC-同步
     */
    @TableField("dispatch_type")
    private String dispatchType;

    /**
     * 下发状态
     * PENDING-待下发, DISPATCHING-下发中, SUCCESS-成功, FAILED-失败, TIMEOUT-超时
     */
    @TableField("dispatch_status")
    private String dispatchStatus;

    /**
     * 下发请求数据
     * JSON格式存储
     */
    @TableField("request_data")
    private String requestData;

    /**
     * 下发响应数据
     * JSON格式存储
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 错误代码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry")
    private Integer maxRetry;

    /**
     * 下发时间
     */
    @TableField("dispatch_time")
    private LocalDateTime dispatchTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 执行耗时（毫秒）
     */
    @TableField("duration_ms")
    private Integer durationMs;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    // ==================== 非数据库字段 ====================

    /**
     * 人员ID（关联查询）
     */
    @TableField(exist = false)
    private Long personId;

    /**
     * 人员姓名（关联查询）
     */
    @TableField(exist = false)
    private String personName;

    /**
     * 人员类型（关联查询）
     */
    @TableField(exist = false)
    private String personType;

    /**
     * 区域ID（关联查询）
     */
    @TableField(exist = false)
    private Long areaId;

    /**
     * 区域名称（关联查询）
     */
    @TableField(exist = false)
    private String areaName;

    /**
     * 设备类型（关联查询）
     */
    @TableField(exist = false)
    private String deviceType;

    /**
     * 设备IP地址（关联查询）
     */
    @TableField(exist = false)
    private String deviceIp;

    /**
     * 策略名称（关联查询）
     */
    @TableField(exist = false)
    private String strategyName;

    /**
     * 请求映射数据（解析后）
     */
    @TableField(exist = false)
    private Map<String, Object> requestDataMap;

    /**
     * 响应映射数据（解析后）
     */
    @TableField(exist = false)
    private Map<String, Object> responseDataMap;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为成功状态
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(dispatchStatus);
    }

    /**
     * 判断是否为失败状态
     */
    public boolean isFailed() {
        return "FAILED".equals(dispatchStatus) || "TIMEOUT".equals(dispatchStatus);
    }

    /**
     * 判断是否可以重试
     */
    public boolean canRetry() {
        return isFailed() &&
               (retryCount == null || retryCount < (maxRetry != null ? maxRetry : 3));
    }

    /**
     * 判断是否为待处理状态
     */
    public boolean isPending() {
        return "PENDING".equals(dispatchStatus);
    }

    /**
     * 判断是否正在处理中
     */
    public boolean isDispatching() {
        return "DISPATCHING".equals(dispatchStatus);
    }

    /**
     * 判断是否已完成
     */
    public boolean isCompleted() {
        return isSuccess() || isFailed();
    }

    /**
     * 获取下发类型描述
     */
    public String getDispatchTypeDesc() {
        switch (dispatchType) {
            case "ADD":
                return "新增";
            case "UPDATE":
                return "更新";
            case "DELETE":
                return "删除";
            case "SYNC":
                return "同步";
            default:
                return dispatchType;
        }
    }

    /**
     * 获取下发状态描述
     */
    public String getDispatchStatusDesc() {
        switch (dispatchStatus) {
            case "PENDING":
                return "待下发";
            case "DISPATCHING":
                return "下发中";
            case "SUCCESS":
                return "成功";
            case "FAILED":
                return "失败";
            case "TIMEOUT":
                return "超时";
            default:
                return dispatchStatus;
        }
    }

    /**
     * 获取状态颜色标识（用于前端显示）
     */
    public String getStatusColor() {
        switch (dispatchStatus) {
            case "PENDING":
                return "#faad14"; // 橙色
            case "DISPATCHING":
                return "#1890ff"; // 蓝色
            case "SUCCESS":
                return "#52c41a"; // 绿色
            case "FAILED":
                return "#f5222d"; // 红色
            case "TIMEOUT":
                return "#fa8c16"; // 深橙色
            default:
                return "#666666"; // 灰色
        }
    }

    /**
     * 获取执行耗时描述
     */
    public String getDurationDesc() {
        if (durationMs == null || durationMs <= 0) {
            return "-";
        }

        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.1fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm%ds", minutes, seconds);
        }
    }

    /**
     * 获取剩余重试次数
     */
    public int getRemainingRetries() {
        int max = maxRetry != null ? maxRetry : 3;
        int used = retryCount != null ? retryCount : 0;
        return Math.max(0, max - used);
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    /**
     * 标记为开始下发
     */
    public void markAsDispatching() {
        this.dispatchStatus = "DISPATCHING";
        this.dispatchTime = LocalDateTime.now();
    }

    /**
     * 标记为成功
     */
    public void markAsSuccess(String responseData) {
        this.dispatchStatus = "SUCCESS";
        this.completeTime = LocalDateTime.now();
        this.responseData = responseData;
        calculateDuration();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed(String errorMessage, String errorCode) {
        this.dispatchStatus = "FAILED";
        this.completeTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        calculateDuration();
    }

    /**
     * 标记为超时
     */
    public void markAsTimeout() {
        this.dispatchStatus = "TIMEOUT";
        this.completeTime = LocalDateTime.now();
        this.errorMessage = "下发超时";
        this.errorCode = "TIMEOUT";
        calculateDuration();
    }

    /**
     * 计算执行耗时
     */
    private void calculateDuration() {
        if (dispatchTime != null && completeTime != null) {
            this.durationMs = (int) java.time.Duration.between(dispatchTime, completeTime).toMillis();
        }
    }

    /**
     * 解析请求数据映射
     */
    public Map<String, Object> getRequestDataMap() {
        if (requestDataMap != null) {
            return requestDataMap;
        }

        try {
            if (requestData != null && !requestData.trim().isEmpty()) {
                requestDataMap = JSON.parseObject(requestData, Map.class);
            } else {
                requestDataMap = Map.of();
            }
        } catch (Exception e) {
            log.error("解析请求数据失败: {}", requestData, e);
            requestDataMap = Map.of();
        }

        return requestDataMap;
    }

    /**
     * 设置请求数据映射
     */
    public void setRequestDataMap(Map<String, Object> dataMap) {
        this.requestDataMap = dataMap;
        try {
            if (dataMap == null || dataMap.isEmpty()) {
                this.requestData = null;
            } else {
                this.requestData = JSON.toJSONString(dataMap);
            }
        } catch (Exception e) {
            log.error("序列化请求数据失败", e);
            this.requestData = null;
        }
    }

    /**
     * 解析响应数据映射
     */
    public Map<String, Object> getResponseDataMap() {
        if (responseDataMap != null) {
            return responseDataMap;
        }

        try {
            if (responseData != null && !responseData.trim().isEmpty()) {
                responseDataMap = JSON.parseObject(responseData, Map.class);
            } else {
                responseDataMap = Map.of();
            }
        } catch (Exception e) {
            log.error("解析响应数据失败: {}", responseData, e);
            responseDataMap = Map.of();
        }

        return responseDataMap;
    }

    /**
     * 设置响应数据映射
     */
    public void setResponseDataMap(Map<String, Object> dataMap) {
        this.responseDataMap = dataMap;
        try {
            if (dataMap == null || dataMap.isEmpty()) {
                this.responseData = null;
            } else {
                this.responseData = JSON.toJSONString(dataMap);
            }
        } catch (Exception e) {
            log.error("序列化响应数据失败", e);
            this.responseData = null;
        }
    }

    /**
     * 获取请求数据中的关键字段
     */
    public String getRequestField(String fieldName) {
        Map<String, Object> dataMap = getRequestDataMap();
        Object value = dataMap.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取响应数据中的关键字段
     */
    public String getResponseField(String fieldName) {
        Map<String, Object> dataMap = getResponseDataMap();
        Object value = dataMap.get(fieldName);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取错误摘要（用于日志和告警）
     */
    public String getErrorSummary() {
        if (!isFailed()) {
            return null;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("设备下发失败: ").append(deviceName != null ? deviceName : deviceCode);

        if (personName != null) {
            summary.append(", 人员: ").append(personName);
        }

        if (areaName != null) {
            summary.append(", 区域: ").append(areaName);
        }

        if (errorMessage != null && errorMessage.length() > 100) {
            summary.append(", 错误: ").append(errorMessage.substring(0, 100)).append("...");
        } else if (errorMessage != null) {
            summary.append(", 错误: ").append(errorMessage);
        }

        return summary.toString();
    }

    /**
     * 检查是否需要告警
     */
    public boolean needsAlert() {
        // 失败且重试次数达到上限
        if (isFailed() && !canRetry()) {
            return true;
        }

        // 耗时过长（超过30秒）
        if (durationMs != null && durationMs > 30000) {
            return true;
        }

        // 特定错误码需要告警
        if ("NETWORK_ERROR".equals(errorCode) || "DEVICE_OFFLINE".equals(errorCode)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "DeviceDispatchRecordEntity{" +
                "recordId" + recordId +
                ", relationId" + relationId +
                ", deviceId" + deviceId +
                ", deviceCode='" + deviceCode + '\'' +
                ", strategyId" + strategyId +
                ", dispatchType='" + dispatchType + '\'' +
                ", dispatchStatus='" + dispatchStatus + '\'' +
                ", retryCount" + retryCount +
                ", durationMs" + durationMs +
                ", success" + isSuccess() +
                '}';
    }
}