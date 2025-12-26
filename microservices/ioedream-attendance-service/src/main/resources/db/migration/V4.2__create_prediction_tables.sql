-- ============================================================
-- P0-5: TensorFlow预测模型集成 - 数据库表结构
-- 版本: V4.2
-- 创建时间: 2025-12-26
-- 说明: 预测任务、预测结果、模型版本、训练数据
-- ============================================================

-- ============================================================
-- 表1: 预测任务表 (t_prediction_task)
-- 说明: 存储预测任务的配置和状态
-- ============================================================
CREATE TABLE IF NOT EXISTS t_prediction_task (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型: ATTENDANCE_FORECAST-考勤预测, ANOMALY_DETECTION-异常检测, LATE_PREDICTION-迟到预测',

    -- 预测配置
    prediction_start_date DATE NOT NULL COMMENT '预测开始日期',
    prediction_end_date DATE NOT NULL COMMENT '预测结束日期',
    prediction_horizon INT DEFAULT 7 COMMENT '预测范围(天数)',
    model_type VARCHAR(50) DEFAULT 'LSTM' COMMENT '模型类型: LSTM-神经网络, ARIMA-时间序列, PROPHET- prophet',

    -- 训练配置
    training_data_start_date DATE COMMENT '训练数据开始日期',
    training_data_end_date DATE COMMENT '训练数据结束日期',
    training_epochs INT DEFAULT 100 COMMENT '训练轮数',
    batch_size INT DEFAULT 32 COMMENT '批次大小',
    learning_rate DECIMAL(10,6) DEFAULT 0.001 COMMENT '学习率',

    -- 任务状态
    task_status VARCHAR(50) DEFAULT 'PENDING' COMMENT '任务状态: PENDING-待处理, TRAINING-训练中, COMPLETED-已完成, FAILED-失败',
    progress_percent INT DEFAULT 0 COMMENT '任务进度(%)',
    error_message TEXT COMMENT '错误信息',

    -- 统计信息
    model_accuracy DECIMAL(5,2) COMMENT '模型准确率(0-100)',
    mae_score DECIMAL(10,4) COMMENT '平均绝对误差',
    rmse_score DECIMAL(10,4) COMMENT '均方根误差',
    training_time_seconds INT COMMENT '训练时长(秒)',

    -- 审计字段
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    INDEX idx_status (task_status),
    INDEX idx_dates (prediction_start_date, prediction_end_date),
    INDEX idx_type (task_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测任务表';

-- ============================================================
-- 表2: 预测结果表 (t_prediction_result)
-- 说明: 存储预测任务的执行结果
-- ============================================================
CREATE TABLE IF NOT EXISTS t_prediction_result (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    task_id BIGINT NOT NULL COMMENT '任务ID (外键)',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    prediction_date DATE NOT NULL COMMENT '预测日期',

    -- 预测值
    predicted_attendance_rate DECIMAL(5,2) COMMENT '预测出勤率(%)',
    predicted_check_in_time DATETIME COMMENT '预测签到时间',
    predicted_check_out_time DATETIME COMMENT '预测签退时间',
    predicted_work_hours DECIMAL(4,2) COMMENT '预测工作时长(小时)',

    -- 预测置信度
    confidence_lower DECIMAL(5,2) COMMENT '预测下界(95%置信区间)',
    confidence_upper DECIMAL(5,2) COMMENT '预测上界(95%置信区间)',
    prediction_confidence DECIMAL(5,2) COMMENT '预测置信度(0-100)',

    -- 异常检测
    is_anomalous TINYINT DEFAULT 0 COMMENT '是否异常 0-正常 1-异常',
    anomaly_score DECIMAL(10,6) COMMENT '异常分数(0-1)',
    anomaly_type VARCHAR(100) COMMENT '异常类型: LATE-迟到, EARLY_LEAVE-早退, ABSENCE-缺勤',

    -- 实际值（用于验证）
    actual_attendance_rate DECIMAL(5,2) COMMENT '实际出勤率(%)',
    actual_check_in_time DATETIME COMMENT '实际签到时间',
    actual_check_out_time DATETIME COMMENT '实际签退时间',
    prediction_error DECIMAL(10,4) COMMENT '预测误差',

    -- 审计字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    FOREIGN KEY (task_id) REFERENCES t_prediction_task(task_id) ON DELETE CASCADE,
    INDEX idx_task (task_id),
    INDEX idx_employee (employee_id),
    INDEX idx_date (prediction_date),
    INDEX idx_anomaly (is_anomalous)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预测结果表';

-- ============================================================
-- 表3: 模型版本表 (t_model_version)
-- 说明: 存储训练好的模型版本信息
-- ============================================================
CREATE TABLE IF NOT EXISTS t_model_version (
    model_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    model_name VARCHAR(200) NOT NULL COMMENT '模型名称',
    model_version VARCHAR(50) NOT NULL COMMENT '模型版本 (例如: v1.0.0)',
    model_type VARCHAR(50) NOT NULL COMMENT '模型类型: LSTM-神经网络, ISOLATION_FOREST-孤立森林, ONE_CLASS_SVM-单类SVM',

    -- 模型配置
    model_config JSON COMMENT '模型配置参数(JSON格式)',
    feature_config JSON COMMENT '特征配置(JSON格式)',
    hyperparameters JSON COMMENT '超参数配置(JSON格式)',

    -- 模型文件
    model_file_path VARCHAR(500) COMMENT '模型文件路径',
    model_file_size BIGINT COMMENT '模型文件大小(字节)',
    model_format VARCHAR(50) DEFAULT 'SAVED_MODEL' COMMENT '模型格式: SAVED_MODEL, HDF5, PICKLE',

    -- 训练信息
    training_data_count INT COMMENT '训练数据量',
    validation_data_count INT COMMENT '验证数据量',
    training_date DATETIME COMMENT '训练日期',
    training_duration_seconds INT COMMENT '训练时长(秒)',

    -- 性能指标
    accuracy DECIMAL(5,2) COMMENT '准确率(%)',
    precision_score DECIMAL(5,2) COMMENT '精确率(%)',
    recall_score DECIMAL(5,2) COMMENT '召回率(%)',
    f1_score DECIMAL(5,2) COMMENT 'F1分数',
    mae DECIMAL(10,4) COMMENT '平均绝对误差',
    rmse DECIMAL(10,4) COMMENT '均方根误差',

    -- 模型状态
    is_active TINYINT DEFAULT 0 COMMENT '是否激活 0-未激活 1-激活',
    deployment_status VARCHAR(50) DEFAULT 'NOT_DEPLOYED' COMMENT '部署状态: NOT_DEPLOYED-未部署, DEPLOYED-已部署, DEPRECATED-已废弃',

    -- 审计字段
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    INDEX idx_active (is_active),
    INDEX idx_type (model_type),
    INDEX idx_version (model_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型版本表';

-- ============================================================
-- 表4: 训练数据表 (t_training_data)
-- 说明: 存储用于模型训练的历史数据
-- ============================================================
CREATE TABLE IF NOT EXISTS t_training_data (
    data_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据ID',
    model_id BIGINT COMMENT '模型ID (外键，可选)',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    record_date DATE NOT NULL COMMENT '记录日期',

    -- 原始特征
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    work_hours DECIMAL(4,2) COMMENT '工作时长(小时)',
    shift_type VARCHAR(50) COMMENT '班次类型',

    -- 提取特征（JSON格式）
    time_features JSON COMMENT '时间特征: 星期、月份、是否工作日、是否节假日',
    statistical_features JSON COMMENT '统计特征: 7天平均、30天标准差、趋势',
    behavior_features JSON COMMENT '行为特征: 迟到率、早退率、缺勤率、加班频率',

    -- 标签
    is_late TINYINT COMMENT '是否迟到 0-否 1-是',
    is_early_leave TINYINT COMMENT '是否早退 0-否 1-是',
    is_absent TINYINT COMMENT '是否缺勤 0-否 1-是',
    overtime_hours DECIMAL(4,2) COMMENT '加班时长(小时)',

    -- 数据质量
    data_quality_score DECIMAL(5,2) COMMENT '数据质量分数(0-100)',
    is_outlier TINYINT DEFAULT 0 COMMENT '是否异常值 0-否 1-是',

    -- 审计字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    FOREIGN KEY (model_id) REFERENCES t_model_version(model_id) ON DELETE SET NULL,
    INDEX idx_employee_date (employee_id, record_date),
    INDEX idx_model (model_id),
    INDEX idx_outlier (is_outlier)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='训练数据表';

-- ============================================================
-- 初始化数据: 插入默认模型版本
-- ============================================================

-- LSTM预测模型 v1.0
INSERT INTO t_model_version (
    model_name,
    model_version,
    model_type,
    model_config,
    feature_config,
    hyperparameters,
    is_active,
    deployment_status,
    created_by
) VALUES (
    '考勤预测LSTM模型',
    'v1.0.0',
    'LSTM',
    '{"layers": [{"type": "LSTM", "units": 128}, {"type": "LSTM", "units": 64}, {"type": "DENSE", "units": 32}]}',
    '{"features": ["time_features", "statistical_features", "behavior_features"]}',
    '{"epochs": 100, "batch_size": 32, "learning_rate": 0.001, "optimizer": "adam"}',
    1,
    'DEPLOYED',
    1
);

-- 异常检测模型 v1.0
INSERT INTO t_model_version (
    model_name,
    model_version,
    model_type,
    model_config,
    feature_config,
    hyperparameters,
    is_active,
    deployment_status,
    created_by
) VALUES (
    '考勤异常检测模型',
    'v1.0.0',
    'ISOLATION_FOREST',
    '{"n_estimators": 100, "contamination": 0.1, "max_samples": 256}',
    '{"features": ["time_features", "behavior_features"]}',
    '{"n_estimators": 100, "contamination": 0.1}',
    1,
    'DEPLOYED',
    1
);
