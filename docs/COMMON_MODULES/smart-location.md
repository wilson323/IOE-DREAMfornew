# 地理位置公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 门禁系统、考勤系统等需要地理位置功能模块

---

## 📖 模块概述

### 模块简介
smart-location 是 SmartAdmin 项目的地理位置服务公共模块，提供统一的GPS定位、地理围栏、位置验证等功能，支持多种定位方式和精度控制。

### 核心特性
- **多定位方式支持**: GPS、Wi-Fi、蓝牙、基站定位
- **高精度定位算法**: 多点定位、位置平滑、误差校正
- **地理围栏管理**: 圆形、多边形地理围栏配置和检测
- **位置验证服务**: 实时位置验证、历史轨迹分析
- **移动端优化**: 低功耗定位、离线定位、位置缓存
- **安全保护**: 位置数据加密、轨迹隐私保护

---

## 🏗️ 架构设计

### 模块结构

```
smart-location/
├── controller/                    # 位置控制器
│   ├── LocationController.java           # 位置管理控制器
│   ├── GeofenceController.java         # 地理围栏控制器
│   ├── LocationHistoryController.java    # 位置历史控制器
│   └── LocationValidationController.java # 位置验证控制器
├── service/                      # 位置服务层
│   ├── LocationService.java              # 位置管理服务
│   ├── GeofenceService.java            # 地理围栏服务
│   ├── LocationHistoryService.java      # 位置历史服务
│   └── LocationValidationService.java   # 位置验证服务
├── manager/                      # 位置管理层
│   ├── LocationManager.java              # 位置管理器
│   ├── GeofenceManager.java             # 地理围栏管理器
│   ├── LocationAlgorithmManager.java     # 定位算法管理器
│   └── LocationCacheManager.java        # 位置缓存管理器
├── dao/                          # 位置数据层
│   ├── LocationDao.java                  # 位置DAO
│   ├── GeofenceDao.java                # 地理围栏DAO
│   ├── LocationHistoryDao.java          # 位置历史DAO
│   └── LocationConfigDao.java           # 位置配置DAO
├── entity/                       # 位置实体
│   ├── LocationEntity.java               # 位置实体
│   ├── GeofenceEntity.java             # 地理围栏实体
│   ├── LocationHistoryEntity.java       # 位置历史实体
│   └── LocationConfigEntity.java        # 位置配置实体
├── algorithm/                    # 定位算法
│   ├── LocationAlgorithm.java            # 定位算法接口
│   ├── GPSLocationAlgorithm.java        # GPS定位算法
│   ├── WifiLocationAlgorithm.java       # Wi-Fi定位算法
│   ├── HybridLocationAlgorithm.java     # 混合定位算法
│   └── LocationFilter.java              # 位置滤波算法
├── mobile/                       # 移动端支持
│   ├── MobileLocationService.java       # 移动端位置服务
│   ├── LocationCacheService.java        # 位置缓存服务
│   └── LocationSyncService.java         # 位置同步服务
└── security/                     # 安全模块
    ├── LocationEncryptionService.java   # 位置加密服务
    ├── LocationPrivacyService.java      # 位置隐私服务
    └── LocationAuditService.java        # 位置审计服务
```

### 核心设计模式

```java
// 定位策略模式
@Component
public class LocationAlgorithmFactory {

    private final Map<LocationType, LocationAlgorithm> algorithmMap = new ConcurrentHashMap<>();

    public LocationAlgorithmFactory(List<LocationAlgorithm> algorithms) {
        algorithms.forEach(algorithm ->
            algorithmMap.put(algorithm.getSupportedLocationType(), algorithm));
    }

    /**
     * 获取定位算法
     * @param locationType 定位类型
     * @return 定位算法
     */
    public LocationAlgorithm getAlgorithm(LocationType locationType) {
        LocationAlgorithm algorithm = algorithmMap.get(locationType);
        if (algorithm == null) {
            throw new UnsupportedOperationException("不支持的定位类型: " + locationType);
        }
        return algorithm;
    }

    /**
     * 获取最佳定位算法
     * @param locationRequest 定位请求
     * @return 最佳算法
     */
    public LocationAlgorithm getBestAlgorithm(LocationRequest locationRequest) {
        // 根据精度要求、功耗要求、时间要求等选择最佳算法
        return algorithmMap.values().stream()
            .filter(algorithm -> algorithm.supports(locationRequest))
            .max(Comparator.comparing(algorithm::getAccuracyScore))
            .orElse(algorithmMap.get(LocationType.GPS)); // 默认GPS
    }
}

// 地理围栏策略模式
@Component
public class GeofenceDetectorFactory {

    private final Map<GeofenceType, GeofenceDetector> detectorMap = new ConcurrentHashMap<>();

    public GeofenceDetectorFactory(List<GeofenceDetector> detectors) {
        detectors.forEach(detector ->
            detectorMap.put(detector.getSupportedGeofenceType(), detector));
    }

    /**
     * 检测位置是否在地理围栏内
     */
    public boolean isPointInGeofence(LocationPoint point, GeofenceEntity geofence) {
        GeofenceDetector detector = detectorMap.get(GeofenceType.valueOf(geofence.getGeofenceType()));
        if (detector == null) {
            throw new UnsupportedOperationException("不支持的围栏类型: " + geofence.getGeofenceType());
        }
        return detector.isPointInGeofence(point, geofence);
    }
}
```

---

## 🗄️ 数据库设计

### 位置记录表 (t_location)

```sql
CREATE TABLE t_location (
    location_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '位置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID',
    latitude DECIMAL(10, 8) NOT NULL COMMENT '纬度',
    longitude DECIMAL(11, 8) NOT NULL COMMENT '经度',
    altitude DECIMAL(8, 2) COMMENT '海拔高度(米)',
    accuracy DECIMAL(8, 2) COMMENT '定位精度(米)',
    location_type VARCHAR(20) NOT NULL COMMENT '定位类型',
    location_provider VARCHAR(50) COMMENT '定位提供者',
    location_speed DECIMAL(8, 2) COMMENT '移动速度(m/s)',
    location_bearing DECIMAL(6, 2) COMMENT '移动方向(度)',
    location_time DATETIME NOT NULL COMMENT '定位时间',
    address TEXT COMMENT '地址信息',
    location_data JSON COMMENT '位置扩展数据JSON',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效：1-有效，0-无效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_location_time (location_time),
    INDEX idx_location_type (location_type),
    INDEX idx_spatial (latitude, longitude),
    INDEX idx_user_time (user_id, location_time),
    SPATIAL INDEX idx_location (POINT(longitude, latitude))
) COMMENT = '位置记录表';
```

### 地理围栏表 (t_geofence)

```sql
CREATE TABLE t_geofence (
    geofence_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '围栏ID',
    geofence_name VARCHAR(200) NOT NULL COMMENT '围栏名称',
    geofence_type VARCHAR(20) NOT NULL COMMENT '围栏类型',
    geofence_geometry GEOMETRY NOT NULL COMMENT '围栏几何形状',
    center_latitude DECIMAL(10, 8) COMMENT '中心点纬度',
    center_longitude DECIMAL(11, 8) COMMENT '中心点经度',
    radius DECIMAL(10, 2) COMMENT '半径(米，圆形围栏)',
    geofence_vertices JSON COMMENT '顶点坐标(多边形围栏)',
    geofence_config JSON COMMENT '围栏配置JSON',
    enable_time_start TIME COMMENT '启用开始时间',
    enable_time_end TIME COMMENT '启用结束时间',
    enable_days VARCHAR(20) COMMENT '启用日期(1-7)',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_geofence_type (geofence_type),
    INDEX idx_status (status),
    INDEX idx_center (center_latitude, center_longitude),
    SPATIAL INDEX idx_geofence_geom (geofence_geometry)
) COMMENT = '地理围栏表';

-- 围栏类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('GEOFENCE_TYPE', 'CIRCLE', '圆形围栏', 1, '圆形地理围栏'),
('GEOFENCE_TYPE', 'POLYGON', '多边形围栏', 2, '多边形地理围栏'),
('GEOFENCE_TYPE', 'RECTANGLE', '矩形围栏', 3, '矩形地理围栏');
```

### 位置验证记录表 (t_location_validation)

```sql
CREATE TABLE t_location_validation (
    validation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    location_id BIGINT NOT NULL COMMENT '位置ID',
    validation_type VARCHAR(50) NOT NULL COMMENT '验证类型',
    geofence_id BIGINT COMMENT '围栏ID',
    validation_result TINYINT NOT NULL COMMENT '验证结果：1-通过，0-失败',
    validation_message TEXT COMMENT '验证信息',
    validation_distance DECIMAL(10, 2) COMMENT '验证距离(米)',
    validation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    request_data JSON COMMENT '请求数据JSON',
    response_data JSON COMMENT '响应数据JSON',
    INDEX idx_user_id (user_id),
    INDEX idx_location_id (location_id),
    INDEX idx_geofence_id (geofence_id),
    INDEX idx_validation_type (validation_type),
    INDEX idx_validation_result (validation_result),
    INDEX idx_validation_time (validation_time)
) COMMENT = '位置验证记录表';
```

### 位置配置表 (t_location_config)

```sql
CREATE TABLE t_location_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT COMMENT '用户ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_description TEXT COMMENT '配置描述',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_config (user_id, config_type, config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_is_default (is_default),
    INDEX idx_status (status)
) COMMENT = '位置配置表';

-- 默认位置配置
INSERT INTO t_location_config (config_type, config_key, config_value, config_description, is_default) VALUES
('GPS', 'min_accuracy', '50', 'GPS最小精度要求(米)', 1),
('GPS', 'max_age', '30', 'GPS位置最大有效期(秒)', 1),
('WIFI', 'min_strength', '-70', 'Wi-Fi最小信号强度(dBm)', 1),
('WIFI', 'max_age', '60', 'Wi-Fi位置最大有效期(秒)', 1),
('GEOFENCE', 'tolerance_distance', '50', '地理围栏容差距离(米)', 1),
('GEOFENCE', 'dwell_time', '30', '驻留时间(秒)', 1),
('PRIVACY', 'retention_days', '90', '位置数据保留天数', 1),
('PRIVACY', 'encryption_enabled', 'true', '是否启用位置数据加密', 1);
```

---

## 🔧 后端实现

### 核心控制器 (LocationController)

```java
@RestController
@RequestMapping("/api/location")
@Tag(name = "位置管理", description = "地理位置相关接口")
public class LocationController {

    @Resource
    private LocationService locationService;

    @PostMapping("/report")
    @Operation(summary = "上报位置")
    @SaCheckLogin
    public ResponseDTO<String> reportLocation(@Valid @RequestBody LocationReportDTO reportDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        locationService.reportLocation(userId, reportDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前位置")
    @SaCheckLogin
    public ResponseDTO<LocationVO> getCurrentLocation() {
        Long userId = SmartRequestUtil.getCurrentUserId();
        LocationVO currentLocation = locationService.getCurrentLocation(userId);
        return ResponseDTO.ok(currentLocation);
    }

    @GetMapping("/history")
    @Operation(summary = "获取位置历史")
    @SaCheckPermission("location:history")
    public ResponseDTO<PageResult<LocationHistoryVO>> getLocationHistory(
            @Valid @RequestBody LocationHistoryQueryDTO queryDTO) {
        PageResult<LocationHistoryVO> history = locationService.getLocationHistory(queryDTO);
        return ResponseDTO.ok(history);
    }

    @PostMapping("/validate")
    @Operation(summary = "验证位置")
    @SaCheckLogin
    public ResponseDTO<LocationValidationVO> validateLocation(@Valid @RequestBody LocationValidationDTO validationDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        LocationValidationVO result = locationService.validateLocation(userId, validationDTO);
        return ResponseDTO.ok(result);
    }

    @GetMapping("/nearby")
    @Operation(summary = "查找附近位置")
    @SaCheckLogin
    public ResponseDTO<List<NearbyLocationVO>> getNearbyLocations(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "1000") Integer radius) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        List<NearbyLocationVO> nearbyLocations = locationService.getNearbyLocations(
            userId, latitude, longitude, radius);
        return ResponseDTO.ok(nearbyLocations);
    }

    @GetMapping("/trajectory")
    @Operation(summary = "获取轨迹")
    @SaCheckPermission("location:trajectory")
    public ResponseDTO<LocationTrajectoryVO> getTrajectory(
            @RequestParam Long userId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        LocationTrajectoryVO trajectory = locationService.getTrajectory(userId, startTime, endTime);
        return ResponseDTO.ok(trajectory);
    }
}
```

### 核心服务层 (LocationService)

```java
@Service
@Transactional(readOnly = true)
public class LocationService {

    @Resource
    private LocationManager locationManager;
    @Resource
    private LocationAlgorithmFactory algorithmFactory;
    @Resource
    private LocationCacheManager cacheManager;
    @Resource
    private LocationValidationService validationService;

    @Transactional(rollbackFor = Exception.class)
    public void reportLocation(Long userId, LocationReportDTO reportDTO) {
        // 1. 验证定位数据
        validateLocationData(reportDTO);

        // 2. 选择最佳定位算法
        LocationAlgorithm algorithm = algorithmFactory.getBestAlgorithm(
            LocationRequest.builder()
                .accuracy(reportDTO.getAccuracy())
                .locationType(LocationType.valueOf(reportDTO.getLocationType()))
                .build()
        );

        // 3. 处理和优化位置数据
        LocationPoint optimizedLocation = algorithm.processLocation(
            LocationPoint.builder()
                .latitude(reportDTO.getLatitude())
                .longitude(reportDTO.getLongitude())
                .accuracy(reportDTO.getAccuracy())
                .altitude(reportDTO.getAltitude())
                .timestamp(reportDTO.getLocationTime())
                .build()
        );

        // 4. 保存位置记录
        LocationEntity location = LocationEntity.builder()
            .userId(userId)
            .deviceId(reportDTO.getDeviceId())
            .latitude(optimizedLocation.getLatitude())
            .longitude(optimizedLocation.getLongitude())
            .altitude(optimizedLocation.getAltitude())
            .accuracy(optimizedLocation.getAccuracy())
            .locationType(reportDTO.getLocationType())
            .locationProvider(reportDTO.getLocationProvider())
            .locationSpeed(reportDTO.getSpeed())
            .locationBearing(reportDTO.getBearing())
            .locationTime(reportDTO.getLocationTime())
            .locationData(JsonUtils.toJsonString(reportDTO.getExtendedData()))
            .isValid(1)
            .build();

        locationManager.saveLocation(location);

        // 5. 缓存最新位置
        cacheManager.cacheCurrentLocation(userId, location);

        // 6. 触发位置相关事件
        publishLocationEvents(userId, location);

        // 7. 检查地理围栏
        checkGeofences(userId, optimizedLocation);
    }

    public LocationVO getCurrentLocation(Long userId) {
        // 1. 先从缓存获取
        LocationEntity cachedLocation = cacheManager.getCurrentLocation(userId);
        if (cachedLocation != null && isLocationRecent(cachedLocation)) {
            return convertToVO(cachedLocation);
        }

        // 2. 从数据库获取最新位置
        LocationEntity latestLocation = locationManager.getLatestLocation(userId);
        if (latestLocation != null) {
            // 更新缓存
            cacheManager.cacheCurrentLocation(userId, latestLocation);
            return convertToVO(latestLocation);
        }

        return null;
    }

    public PageResult<LocationHistoryVO> getLocationHistory(LocationHistoryQueryDTO queryDTO) {
        // 1. 参数验证
        validateHistoryQuery(queryDTO);

        // 2. 查询位置历史
        PageResult<LocationEntity> result = locationManager.getLocationHistory(queryDTO);

        // 3. 转换为VO并添加地址信息
        List<LocationHistoryVO> records = result.getRecords().stream()
            .map(this::convertToHistoryVO)
            .collect(Collectors.toList());

        return PageResult.<LocationHistoryVO>builder()
            .records(records)
            .total(result.getTotal())
            .pageNum(result.getPageNum())
            .pageSize(result.getPageSize())
            .build();
    }

    public LocationValidationVO validateLocation(Long userId, LocationValidationDTO validationDTO) {
        // 1. 获取当前用户位置
        LocationEntity currentLocation = locationManager.getLatestLocation(userId);
        if (currentLocation == null) {
            return LocationValidationVO.builder()
                .validationResult(false)
                .validationMessage("用户暂无位置信息")
                .build();
        }

        // 2. 执行位置验证
        LocationValidationResult result = validationService.validateLocation(
            userId, currentLocation, validationDTO);

        // 3. 记录验证结果
        locationManager.saveValidationRecord(userId, currentLocation.getLocationId(), result);

        return LocationValidationVO.builder()
            .validationResult(result.isValid())
            .validationMessage(result.getMessage())
            .validationDistance(result.getDistance())
            .validationTime(LocalDateTime.now())
            .build();
    }

    public List<NearbyLocationVO> getNearbyLocations(Long userId, BigDecimal latitude,
                                                    BigDecimal longitude, Integer radius) {
        // 1. 创建查询点
        LocationPoint queryPoint = LocationPoint.builder()
            .latitude(latitude)
            .longitude(longitude)
            .build();

        // 2. 查找附近的位置点
        List<LocationEntity> nearbyLocations = locationManager.findNearbyLocations(queryPoint, radius);

        // 3. 过滤和处理结果
        return nearbyLocations.stream()
            .filter(location -> !location.getUserId().equals(userId)) // 排除自己
            .filter(location -> isLocationRecent(location)) // 只要近期位置
            .map(this::convertToNearbyVO)
            .collect(Collectors.toList());
    }

    public LocationTrajectoryVO getTrajectory(Long userId, String startTime, String endTime) {
        // 1. 解析时间参数
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);

        // 2. 查询轨迹数据
        List<LocationEntity> trajectoryPoints = locationManager.getTrajectory(userId, start, end);

        // 3. 处理轨迹数据
        return processTrajectory(trajectoryPoints);
    }

    private void validateLocationData(LocationReportDTO reportDTO) {
        if (reportDTO.getLatitude() == null || reportDTO.getLongitude() == null) {
            throw new SmartException("经纬度坐标不能为空");
        }

        if (reportDTO.getLatitude().compareTo(BigDecimal.valueOf(90)) > 0 ||
            reportDTO.getLatitude().compareTo(BigDecimal.valueOf(-90)) < 0) {
            throw new SmartException("纬度坐标无效");
        }

        if (reportDTO.getLongitude().compareTo(BigDecimal.valueOf(180)) > 0 ||
            reportDTO.getLongitude().compareTo(BigDecimal.valueOf(-180)) < 0) {
            throw new SmartException("经度坐标无效");
        }

        if (reportDTO.getLocationTime() == null) {
            reportDTO.setLocationTime(LocalDateTime.now());
        }
    }

    private boolean isLocationRecent(LocationEntity location) {
        // 位置在30分钟内认为是近期的
        return Duration.between(location.getLocationTime(), LocalDateTime.now()).toMinutes() <= 30;
    }

    private LocationVO convertToVO(LocationEntity location) {
        LocationVO vo = new LocationVO();
        BeanUtil.copyProperties(location, vo);

        // 添加地址信息
        if (StringUtils.isBlank(location.getAddress())) {
            vo.setAddress(geocodeService.getAddress(location.getLatitude(), location.getLongitude()));
        }

        return vo;
    }

    private LocationHistoryVO convertToHistoryVO(LocationEntity location) {
        LocationHistoryVO vo = new LocationHistoryVO();
        BeanUtil.copyProperties(location, vo);

        // 添加地址信息
        if (StringUtils.isBlank(location.getAddress())) {
            vo.setAddress(geocodeService.getAddress(location.getLatitude(), location.getLongitude()));
        }

        return vo;
    }

    private NearbyLocationVO convertToNearbyVO(LocationEntity location) {
        NearbyLocationVO vo = new NearbyLocationVO();
        BeanUtil.copyProperties(location, vo);

        // 计算与查询点的距离
        LocationPoint currentLocation = cacheManager.getCurrentLocation(SmartRequestUtil.getCurrentUserId());
        if (currentLocation != null) {
            double distance = LocationUtils.calculateDistance(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                location.getLatitude(), location.getLongitude()
            );
            vo.setDistance(BigDecimal.valueOf(Math.round(distance)));
        }

        return vo;
    }

    private LocationTrajectoryVO processTrajectory(List<LocationEntity> trajectoryPoints) {
        if (trajectoryPoints.isEmpty()) {
            return LocationTrajectoryVO.builder()
                .totalDistance(BigDecimal.ZERO)
                .totalTime(0)
                .averageSpeed(BigDecimal.ZERO)
                .points(Collections.emptyList())
                .build();
        }

        // 计算总距离、总时间、平均速度
        double totalDistance = 0.0;
        long totalTime = 0;
        List<LocationTrajectoryPoint> points = new ArrayList<>();

        LocationPoint prevPoint = null;
        for (LocationEntity entity : trajectoryPoints) {
            LocationPoint currentPoint = LocationPoint.builder()
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .timestamp(entity.getLocationTime())
                .accuracy(entity.getAccuracy())
                .build();

            if (prevPoint != null) {
                totalDistance += LocationUtils.calculateDistance(
                    prevPoint.getLatitude(), prevPoint.getLongitude(),
                    currentPoint.getLatitude(), currentPoint.getLongitude()
                );
                totalTime += Duration.between(prevPoint.getTimestamp(), currentPoint.getTimestamp()).getSeconds();
            }

            points.add(LocationTrajectoryPoint.builder()
                .latitude(currentPoint.getLatitude())
                .longitude(currentPoint.getLongitude())
                .timestamp(currentPoint.getTimestamp())
                .distance(BigDecimal.valueOf(totalDistance))
                .build());

            prevPoint = currentPoint;
        }

        return LocationTrajectoryVO.builder()
            .totalDistance(BigDecimal.valueOf(Math.round(totalDistance)))
            .totalTime(totalTime)
            .averageSpeed(totalTime > 0 ? BigDecimal.valueOf(totalDistance / totalTime * 3.6) : BigDecimal.ZERO) // km/h
            .points(points)
            .build();
    }

    private void publishLocationEvents(Long userId, LocationEntity location) {
        // 发布位置更新事件
        LocationUpdateEvent event = new LocationUpdateEvent();
        event.setUserId(userId);
        event.setLocationId(location.getLocationId());
        event.setLatitude(location.getLatitude());
        event.setLongitude(location.getLongitude());
        event.setAccuracy(location.getAccuracy());
        event.setLocationTime(location.getLocationTime());

        eventPublisher.publishEvent(event);
    }

    private void checkGeofences(Long userId, LocationPoint location) {
        try {
            List<GeofenceEntity> activeGeofences = geofenceService.getActiveGeofences();
            for (GeofenceEntity geofence : activeGeofences) {
                boolean isInside = geofenceDetectorFactory.isPointInGeofence(location, geofence);

                GeofenceEvent geofenceEvent = GeofenceEvent.builder()
                    .userId(userId)
                    .geofenceId(geofence.getGeofenceId())
                    .location(location)
                    .eventType(isInside ? GeofenceEventType.ENTER : GeofenceEventType.EXIT)
                    .eventTime(LocalDateTime.now())
                    .build();

                eventPublisher.publishEvent(geofenceEvent);
            }
        } catch (Exception e) {
            log.error("检查地理围栏失败", e);
        }
    }
}
```

### 核心管理层 (LocationManager)

```java
@Component
public class LocationManager {

    @Resource
    private LocationDao locationDao;
    @Resource
    private LocationValidationDao validationDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GeofenceService geofenceService;

    // 缓存常量
    private static final String CURRENT_LOCATION_PREFIX = "location:current:";
    private static final String LOCATION_HISTORY_PREFIX = "location:history:";
    private static final Duration CURRENT_LOCATION_CACHE_EXPIRE = Duration.ofMinutes(5);

    @Cacheable(value = "location", key = "#userId")
    public LocationEntity getLatestLocation(Long userId) {
        return locationDao.selectOne(
            new QueryWrapper<LocationEntity>()
                .eq("user_id", userId)
                .eq("is_valid", 1)
                .orderByDesc("location_time")
                .last("LIMIT 1")
        );
    }

    @CacheEvict(value = "location", key = "#location.userId")
    public void saveLocation(LocationEntity location) {
        locationDao.insert(location);

        // 更新当前位置缓存
        String cacheKey = CURRENT_LOCATION_PREFIX + location.getUserId();
        redisTemplate.opsForValue().set(cacheKey, location, CURRENT_LOCATION_CACHE_EXPIRE);
    }

    public PageResult<LocationEntity> getLocationHistory(LocationHistoryQueryDTO queryDTO) {
        QueryWrapper<LocationEntity> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", queryDTO.getUserId())
                   .eq("is_valid", 1);

        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("location_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("location_time", queryDTO.getEndTime());
        }
        if (queryDTO.getLocationType() != null) {
            queryWrapper.eq("location_type", queryDTO.getLocationType());
        }

        queryWrapper.orderByDesc("location_time");

        Page<LocationEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<LocationEntity> result = locationDao.selectPage(page, queryWrapper);

        return PageResult.<LocationEntity>builder()
            .records(result.getRecords())
            .total(result.getTotal())
            .pageNum(result.getCurrent())
            .pageSize(result.getSize())
            .build();
    }

    /**
     * 使用空间索引查找附近的位置
     */
    @SuppressWarnings("unchecked")
    public List<LocationEntity> findNearbyLocations(LocationPoint centerPoint, Integer radius) {
        // 构建空间查询SQL
        String sql = String.format(
            "SELECT * FROM t_location WHERE " +
            "is_valid = 1 AND " +
            "ST_Distance_Sphere(POINT(longitude, latitude), POINT(%.8f, %.8f)) <= %d " +
            "ORDER BY location_time DESC " +
            "LIMIT 100",
            centerPoint.getLongitude(),
            centerPoint.getLatitude(),
            radius
        );

        List<LocationEntity> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocationEntity location = new LocationEntity();
            location.setLocationId(rs.getLong("location_id"));
            location.setUserId(rs.getLong("user_id"));
            location.setLatitude(rs.getBigDecimal("latitude"));
            location.setLongitude(rs.getBigDecimal("longitude"));
            location.setAccuracy(rs.getBigDecimal("accuracy"));
            location.setLocationTime(rs.getTimestamp("location_time").toLocalDateTime());
            return location;
        });

        return results;
    }

    public List<LocationEntity> getTrajectory(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return locationDao.selectList(
            new QueryWrapper<LocationEntity>()
                .eq("user_id", userId)
                .eq("is_valid", 1)
                .between("location_time", startTime, endTime)
                .orderByAsc("location_time")
        );
    }

    @CacheEvict(value = "location", key = "#userId")
    public void saveValidationRecord(Long userId, Long locationId, LocationValidationResult result) {
        LocationValidationEntity validation = LocationValidationEntity.builder()
            .userId(userId)
            .locationId(locationId)
            .validationType(result.getValidationType())
            .geofenceId(result.getGeofenceId())
            .validationResult(result.isValid() ? 1 : 0)
            .validationMessage(result.getMessage())
            .validationDistance(result.getDistance())
            .validationTime(LocalDateTime.now())
            .requestData(JsonUtils.toJsonString(result.getRequestData()))
            .responseData(JsonUtils.toJsonString(result.getResponseData()))
            .build();

        validationDao.insert(validation);
    }

    /**
     * 清理过期的位置数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanExpiredLocations() {
        // 获取位置数据保留天数配置
        Integer retentionDays = getLocationConfig("PRIVACY", "retention_days", Integer.class, 90);

        LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);

        // 软删除过期数据
        LocationEntity updateEntity = new LocationEntity();
        updateEntity.setIsValid(0);
        updateEntity.setUpdateTime(LocalDateTime.now());

        locationDao.update(updateEntity,
            new QueryWrapper<LocationEntity>()
                .lt("location_time", expireTime)
                .eq("is_valid", 1)
        );

        log.info("清理了{}天前的位置数据，删除时间: {}", retentionDays, expireTime);
    }

    @SuppressWarnings("unchecked")
    private <T> T getLocationConfig(String configType, String configKey, Class<T> targetType, T defaultValue) {
        try {
            String cacheKey = String.format("location:config:%s:%s", configType, configKey);
            String configValue = (String) redisTemplate.opsForValue().get(cacheKey);

            if (configValue == null) {
                // 从数据库获取配置
                LocationConfigEntity config = locationConfigDao.selectOne(
                    new QueryWrapper<LocationConfigEntity>()
                        .eq("config_type", configType)
                        .eq("config_key", configKey)
                        .eq("status", 1)
                );

                if (config != null) {
                    configValue = config.getConfigValue();
                    redisTemplate.opsForValue().set(cacheKey, configValue, Duration.ofHours(1));
                }
            }

            if (configValue != null) {
                if (targetType == String.class) {
                    return (T) configValue;
                } else if (targetType == Integer.class) {
                    return (T) Integer.valueOf(configValue);
                } else if (targetType == Boolean.class) {
                    return (T) Boolean.valueOf(configValue);
                }
            }
        } catch (Exception e) {
            log.error("获取位置配置失败: {}:{}", configType, configKey, e);
        }

        return defaultValue;
    }
}
```

---

## 🎨 前端实现

### 位置状态管理 (useLocationStore)

```javascript
// /store/location.js
import { defineStore } from 'pinia'
import { locationApi } from '/@/api/location'
import { useWebSocket } from '/@/composables/useWebSocket'

export const useLocationStore = defineStore('location', {
  state: () => ({
    // 当前位置
    currentLocation: null,
    // 位置历史
    locationHistory: [],
    // 地理围栏列表
    geofences: [],
    // 位置验证结果
    validationResults: [],
    // 轨迹数据
    trajectory: null,
    // WebSocket连接
    wsConnection: null,
    // 定位状态
    locationStatus: {
      isWatching: false,
      watchId: null,
      lastError: null
    }
  }),

  getters: {
    // 获取当前位置文本
    getCurrentLocationText: (state) => {
      if (!state.currentLocation) return '未知位置'
      return `${state.currentLocation.latitude}, ${state.currentLocation.longitude}`
    },

    // 获取位置精度等级
    getAccuracyLevel: (state) => (accuracy) => {
      if (!accuracy) return '未知'
      if (accuracy <= 10) return { text: '高精度', color: '#52c41a' }
      if (accuracy <= 50) return { text: '中精度', color: '#faad14' }
      return { text: '低精度', color: '#ff4d4f' }
    },

    // 获取位置时间文本
    getLocationTimeText: () => (locationTime) => {
      if (!locationTime) return ''
      return formatDateTime(locationTime)
    }
  },

  actions: {
    // 上报位置
    async reportLocation(locationData) {
      try {
        const result = await locationApi.reportLocation({
          latitude: locationData.latitude,
          longitude: locationData.longitude,
          altitude: locationData.altitude,
          accuracy: locationData.accuracy,
          locationType: locationData.locationType || 'GPS',
          locationProvider: locationData.locationProvider,
          speed: locationData.speed,
          bearing: locationData.bearing,
          locationTime: locationData.timestamp || new Date().toISOString(),
          extendedData: locationData.extendedData || {}
        })

        // 更新当前位置
        this.currentLocation = {
          ...locationData,
          locationId: result.data,
          reportTime: new Date()
        }

        return result.data
      } catch (error) {
        console.error('上报位置失败:', error)
        throw error
      }
    },

    // 获取当前位置
    async fetchCurrentLocation() {
      try {
        const result = await locationApi.getCurrentLocation()
        this.currentLocation = result.data
        return result.data
      } catch (error) {
        console.error('获取当前位置失败:', error)
        throw error
      }
    },

    // 获取位置历史
    async fetchLocationHistory(params = {}) {
      try {
        const result = await locationApi.getLocationHistory({
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 20,
          startTime: params.startTime,
          endTime: params.endTime,
          locationType: params.locationType
        })

        if (params.append) {
          this.locationHistory = [...this.locationHistory, ...result.data.records]
        } else {
          this.locationHistory = result.data.records
        }

        return result.data
      } catch (error) {
        console.error('获取位置历史失败:', error)
        throw error
      }
    },

    // 验证位置
    async validateLocation(validationData) {
      try {
        const result = await locationApi.validateLocation(validationData)

        // 添加到验证结果
        this.validationResults.unshift({
          ...result.data,
          validationTime: new Date(),
          requestData: validationData
        })

        // 保留最近20条验证结果
        if (this.validationResults.length > 20) {
          this.validationResults = this.validationResults.slice(0, 20)
        }

        return result.data
      } catch (error) {
        console.error('验证位置失败:', error)
        throw error
      }
    },

    // 获取附近位置
    async getNearbyLocations(latitude, longitude, radius = 1000) {
      try {
        const result = await locationApi.getNearbyLocations(latitude, longitude, radius)
        return result.data
      } catch (error) {
        console.error('获取附近位置失败:', error)
        throw error
      }
    },

    // 获取轨迹
    async fetchTrajectory(userId, startTime, endTime) {
      try {
        const result = await locationApi.getTrajectory(userId, startTime, endTime)
        this.trajectory = result.data
        return result.data
      } catch (error) {
        console.error('获取轨迹失败:', error)
        throw error
      }
    },

    // 开始位置监听
    startLocationWatch(options = {}) {
      if (this.locationStatus.isWatching) {
        return
      }

      const defaultOptions = {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000,
        ...options
      }

      this.locationStatus.isWatching = true

      // 获取当前位置
      this.getCurrentPosition(defaultOptions)

      // 设置定时上报
      this.locationStatus.watchId = setInterval(() => {
        this.getCurrentPosition(defaultOptions)
      }, defaultOptions.maximumAge)

      // 初始化WebSocket连接
      this.initWebSocket()
    },

    // 停止位置监听
    stopLocationWatch() {
      if (this.locationStatus.watchId) {
        clearInterval(this.locationStatus.watchId)
        this.locationStatus.watchId = null
      }
      this.locationStatus.isWatching = false

      // 关闭WebSocket连接
      this.closeWebSocket()
    },

    // 获取当前位置（浏览器API）
    getCurrentPosition(options) {
      if (!navigator.geolocation) {
        this.locationStatus.lastError = '浏览器不支持地理定位'
        return
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const locationData = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            altitude: position.coords.altitude,
            accuracy: position.coords.accuracy,
            altitudeAccuracy: position.coords.altitudeAccuracy,
            heading: position.coords.heading,
            speed: position.coords.speed,
            timestamp: position.timestamp,
            locationType: 'GPS'
          }

          this.reportLocation(locationData)
          this.locationStatus.lastError = null
        },
        (error) => {
          this.locationStatus.lastError = this.getGeolocationErrorMessage(error)
          console.error('获取位置失败:', error)
        },
        options
      )
    },

    // 初始化WebSocket连接
    initWebSocket() {
      if (this.wsConnection) {
        this.wsConnection.close()
      }

      const { connect, subscribe } = useWebSocket('/ws/location')

      this.wsConnection = connect()

      // 订阅位置验证结果
      subscribe('location:validation', (data) => {
        this.validationResults.unshift({
          ...data,
          validationTime: new Date()
        })

        if (this.validationResults.length > 20) {
          this.validationResults = this.validationResults.slice(0, 20)
        }
      })

      // 订阅地理围栏事件
      subscribe('location:geofence', (data) => {
        this.handleGeofenceEvent(data)
      })
    },

    // 关闭WebSocket连接
    closeWebSocket() {
      if (this.wsConnection) {
        this.wsConnection.close()
        this.wsConnection = null
      }
    },

    // 处理地理围栏事件
    handleGeofenceEvent(event) {
      const eventType = event.eventType === 'ENTER' ? '进入' : '离开'

      notification[event.eventType === 'ENTER' ? 'success' : 'warning']({
        message: `地理围栏${eventType}`,
        description: `您${eventType}了"${event.geofenceName}"`,
        duration: 0
      })
    },

    // 获取地理定位错误信息
    getGeolocationErrorMessage(error) {
      const errorMessages = {
        1: '用户拒绝了位置请求',
        2: '位置信息不可用',
        3: '请求超时',
        4: '未知错误'
      }
      return errorMessages[error.code] || '未知错误'
    },

    // 清理数据
    clearData() {
      this.currentLocation = null
      this.locationHistory = []
      this.validationResults = []
      this.trajectory = null
      this.stopLocationWatch()
    }
  }
})
```

### 地理围栏组件 (GeofenceMap)

```vue
<template>
  <div class="geofence-map">
    <div class="map-container" ref="mapContainer"></div>

    <div class="map-controls">
      <a-space direction="vertical">
        <a-button type="primary" @click="handleDrawGeofence">
          <template #icon><EditOutlined /></template>
          绘制围栏
        </a-button>
        <a-button @click="handleClearGeofences">
          <template #icon><ClearOutlined /></template>
          清除围栏
        </a-button>
        <a-button @click="handleGetCurrentLocation">
          <template #icon><EnvironmentOutlined /></template>
          当前位置
        </a-button>
      </a-space>
    </div>

    <!-- 围栏信息面板 -->
    <div class="geofence-panel" v-if="selectedGeofence">
      <a-card size="small" title="围栏信息">
        <a-descriptions size="small" :column="1">
          <a-descriptions-item label="名称">
            {{ selectedGeofence.geofenceName }}
          </a-descriptions-item>
          <a-descriptions-item label="类型">
            {{ getGeofenceTypeText(selectedGeofence.geofenceType) }}
          </a-descriptions-item>
          <a-descriptions-item label="半径" v-if="selectedGeofence.geofenceType === 'CIRCLE'">
            {{ selectedGeofence.radius }}米
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="selectedGeofence.status === 1 ? 'green' : 'red'">
              {{ selectedGeofence.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <div class="geofence-actions">
          <a-space>
            <a-button size="small" @click="handleEditGeofence">编辑</a-button>
            <a-button size="small" danger @click="handleDeleteGeofence">删除</a-button>
          </a-space>
        </div>
      </a-card>
    </div>

    <!-- 绘制工具栏 -->
    <div class="draw-toolbar" v-if="drawMode">
      <a-radio-group v-model:value="drawType" button-style="solid">
        <a-radio-button value="circle">圆形</a-radio-button>
        <a-radio-button value="polygon">多边形</a-radio-button>
        <a-radio-button value="rectangle">矩形</a-radio-button>
      </a-radio-group>
      <a-button type="primary" @click="handleSaveGeofence" :disabled="!currentDrawing">
        保存
      </a-button>
      <a-button @click="handleCancelDraw">取消</a-button>
    </div>

    <!-- 围栏编辑弹窗 -->
    <GeofenceEditModal
      v-model:visible="editModalVisible"
      :geofence="editGeofence"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { useLocationStore } from '/@/store/location'
import {
  EditOutlined,
  ClearOutlined,
  EnvironmentOutlined
} from '@ant-design/icons-vue'
import GeofenceEditModal from './GeofenceEditModal.vue'

const props = defineProps({
  height: {
    type: String,
    default: '400px'
  },
  editable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['geofenceSelect', 'geofenceUpdate'])

const locationStore = useLocationStore()

const mapContainer = ref(null)
const selectedGeofence = ref(null)
const drawMode = ref(false)
const drawType = ref('circle')
const currentDrawing = ref(null)
const editModalVisible = ref(false)
const editGeofence = ref(null)

// 地图相关变量
let map = null
let drawingManager = null
let geofenceLayer = null
let currentLocationMarker = null

// 组件挂载
onMounted(async () => {
  await nextTick()
  initMap()
  loadGeofences()
})

// 组件卸载
onUnmounted(() => {
  if (map) {
    map.remove()
  }
})

// 初始化地图
const initMap = () => {
  // 这里使用百度地图API作为示例
  map = new BMap.Map(mapContainer.value)
  map.centerAndZoom(new BMap.Point(116.404, 39.915), 15)
  map.addControl(new BMap.MapTypeControl())
  map.addControl(new BMap.NavigationControl())
  map.addControl(new BMap.ScaleControl())
  map.enableScrollWheelZoom(true)

  // 创建围栏图层
  geofenceLayer = new BMap.OverlayLayer()
  map.addOverlay(geofenceLayer)

  // 创建绘制管理器
  if (props.editable) {
    initDrawingManager()
  }

  // 监听地图点击事件
  map.addEventListener('click', handleMapClick)
}

// 初始化绘制管理器
const initDrawingManager = () => {
  const styleOptions = {
    strokeColor: '#3388ff',
    fillColor: '#3388ff',
    strokeWeight: 2,
    strokeOpacity: 1,
    fillOpacity: 0.3
  }

  drawingManager = new BMapLib.DrawingManager(map, {
    isOpen: false,
    enableDrawingTool: true,
    drawingToolOptions: {
      anchor: BMAP_ANCHOR_TOP_RIGHT,
      offset: new BMap.Size(5, 5),
      drawingModes: [
        BMAP_DRAWING_CIRCLE,
        BMAP_DRAWING_POLYGON,
        BMAP_DRAWING_RECTANGLE
      ]
    },
    circleOptions: styleOptions,
    polygonOptions: styleOptions,
    rectangleOptions: styleOptions
  })

  // 监听绘制完成事件
  drawingManager.addEventListener('overlaycomplete', handleDrawingComplete)
}

// 加载围栏
const loadGeofences = async () => {
  try {
    const geofences = await locationStore.fetchGeofences()
    geofences.forEach(geofence => {
      addGeofenceToMap(geofence)
    })
  } catch (error) {
    console.error('加载围栏失败:', error)
  }
}

// 添加围栏到地图
const addGeofenceToMap = (geofence) => {
  let overlay = null

  switch (geofence.geofenceType) {
    case 'CIRCLE':
      overlay = new BMap.Circle(
        new BMap.Point(geofence.centerLongitude, geofence.centerLatitude),
        geofence.radius,
        {
          strokeColor: '#3388ff',
          fillColor: '#3388ff',
          strokeWeight: 2,
          strokeOpacity: 1,
          fillOpacity: 0.3
        }
      )
      break

    case 'POLYGON':
      const points = geofence.geofenceVertices.map(vertex =>
        new BMap.Point(vertex.longitude, vertex.latitude)
      )
      overlay = new BMap.Polygon(points, {
        strokeColor: '#3388ff',
        fillColor: '#3388ff',
        strokeWeight: 2,
        strokeOpacity: 1,
        fillOpacity: 0.3
      })
      break

    case 'RECTANGLE':
      const bounds = geofence.geofenceVertices
      overlay = new BMap.Polygon([
        new BMap.Point(bounds[0].longitude, bounds[0].latitude),
        new BMap.Point(bounds[1].longitude, bounds[0].latitude),
        new BMap.Point(bounds[1].longitude, bounds[1].latitude),
        new BMap.Point(bounds[0].longitude, bounds[1].latitude)
      ], {
        strokeColor: '#3388ff',
        fillColor: '#3388ff',
        strokeWeight: 2,
        strokeOpacity: 1,
        fillOpacity: 0.3
      })
      break
  }

  if (overlay) {
    overlay.geofenceId = geofence.geofenceId
    overlay.geofence = geofence

    // 添加点击事件
    overlay.addEventListener('click', () => {
      selectedGeofence.value = geofence
      emit('geofenceSelect', geofence)
    })

    geofenceLayer.addOverlay(overlay)
  }
}

// 绘制围栏
const handleDrawGeofence = () => {
  drawMode.value = true
  currentDrawing.value = null

  // 根据绘制类型启用相应的绘制工具
  switch (drawType.value) {
    case 'circle':
      drawingManager.setDrawingMode(BMAP_DRAWING_CIRCLE)
      break
    case 'polygon':
      drawingManager.setDrawingMode(BMAP_DRAWING_POLYGON)
      break
    case 'rectangle':
      drawingManager.setDrawingMode(BMAP_DRAWING_RECTANGLE)
      break
  }

  drawingManager.open()
}

// 绘制完成事件
const handleDrawingComplete = (e) => {
  currentDrawing.value = {
    type: drawType.value,
    overlay: e.overlay,
    data: extractDrawingData(e.overlay, drawType.value)
  }

  drawingManager.close()
}

// 提取绘制数据
const extractDrawingData = (overlay, type) => {
  const data = {
    type,
    center: null,
    radius: null,
    vertices: []
  }

  switch (type) {
    case 'circle':
      data.center = overlay.getCenter()
      data.radius = overlay.getRadius()
      break

    case 'polygon':
    case 'rectangle':
      const points = overlay.getPath()
      data.vertices = points.map(point => ({
        longitude: point.lng,
        latitude: point.lat
      }))
      break
  }

  return data
}

// 保存围栏
const handleSaveGeofence = async () => {
  if (!currentDrawing.value) {
    message.warning('请先绘制围栏')
    return
  }

  try {
    const geofenceData = {
      geofenceName: `围栏_${Date.now()}`,
      geofenceType: currentDrawing.value.type.toUpperCase(),
      centerLatitude: currentDrawing.value.data.center?.lat,
      centerLongitude: currentDrawing.value.data.center?.lng,
      radius: currentDrawing.value.data.radius,
      geofenceVertices: currentDrawing.value.data.vertices,
      status: 1
    }

    const result = await locationStore.createGeofence(geofenceData)

    // 添加到地图
    const newGeofence = { ...geofenceData, geofenceId: result.data }
    addGeofenceToMap(newGeofence)

    // 清除绘制
    geofenceLayer.removeOverlay(currentDrawing.value.overlay)
    currentDrawing.value = null
    drawMode.value = false

    message.success('围栏创建成功')
    emit('geofenceUpdate', newGeofence)

  } catch (error) {
    console.error('保存围栏失败:', error)
    message.error('保存围栏失败')
  }
}

// 取消绘制
const handleCancelDraw = () => {
  if (currentDrawing.value) {
    geofenceLayer.removeOverlay(currentDrawing.value.overlay)
    currentDrawing.value = null
  }

  drawMode.value = false
  drawingManager.close()
}

// 清除围栏
const handleClearGeofences = () => {
  geofenceLayer.clearOverlays()
  selectedGeofence.value = null
  message.success('已清除所有围栏')
}

// 获取当前位置
const handleGetCurrentLocation = async () => {
  try {
    const location = await locationStore.fetchCurrentLocation()
    if (location) {
      const point = new BMap.Point(location.longitude, location.latitude)

      // 移除旧的位置标记
      if (currentLocationMarker) {
        map.removeOverlay(currentLocationMarker)
      }

      // 添加新的位置标记
      currentLocationMarker = new BMap.Marker(point)
      map.addOverlay(currentLocationMarker)
      map.panTo(point)

      message.success('已定位到当前位置')
    }
  } catch (error) {
    message.error('获取位置失败')
  }
}

// 地图点击事件
const handleMapClick = (e) => {
  if (!drawMode.value) {
    selectedGeofence.value = null
  }
}

// 编辑围栏
const handleEditGeofence = () => {
  editGeofence.value = { ...selectedGeofence.value }
  editModalVisible.value = true
}

// 删除围栏
const handleDeleteGeofence = async () => {
  if (!selectedGeofence.value) return

  try {
    await locationStore.deleteGeofence(selectedGeofence.value.geofenceId)

    // 从地图移除
    const overlays = geofenceLayer.getOverlays()
    overlays.forEach(overlay => {
      if (overlay.geofenceId === selectedGeofence.value.geofenceId) {
        geofenceLayer.removeOverlay(overlay)
      }
    })

    selectedGeofence.value = null
    message.success('围栏删除成功')
    emit('geofenceUpdate', null)

  } catch (error) {
    console.error('删除围栏失败:', error)
    message.error('删除围栏失败')
  }
}

// 编辑成功
const handleEditSuccess = (updatedGeofence) => {
  editModalVisible.value = false

  // 更新地图上的围栏
  const overlays = geofenceLayer.getOverlays()
  overlays.forEach(overlay => {
    if (overlay.geofenceId === updatedGeofence.geofenceId) {
      geofenceLayer.removeOverlay(overlay)
    }
  })

  addGeofenceToMap(updatedGeofence)
  selectedGeofence.value = updatedGeofence

  message.success('围栏更新成功')
  emit('geofenceUpdate', updatedGeofence)
}

// 获取围栏类型文本
const getGeofenceTypeText = (type) => {
  const typeMap = {
    'CIRCLE': '圆形',
    'POLYGON': '多边形',
    'RECTANGLE': '矩形'
  }
  return typeMap[type] || type
}
</script>

<style lang="less" scoped>
.geofence-map {
  position: relative;
  width: 100%;
  height: v-bind(height);

  .map-container {
    width: 100%;
    height: 100%;
  }

  .map-controls {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 1000;
    background: white;
    padding: 8px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .geofence-panel {
    position: absolute;
    bottom: 10px;
    left: 10px;
    z-index: 1000;
    width: 300px;

    .geofence-actions {
      margin-top: 12px;
      text-align: right;
    }
  }

  .draw-toolbar {
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
    background: white;
    padding: 8px 16px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    display: flex;
    align-items: center;
    gap: 12px;
  }
}
</style>
```

---

## 🧪 测试策略

### 1. 单元测试

```java
@SpringBootTest
class LocationServiceTest {

    @Resource
    private LocationService locationService;

    @Resource
    private LocationDao locationDao;

    @MockBean
    private LocationAlgorithmFactory algorithmFactory;

    @MockBean
    private LocationAlgorithm locationAlgorithm;

    @Test
    void testReportLocation() {
        // 准备测试数据
        Long userId = 1L;
        LocationReportDTO reportDTO = new LocationReportDTO();
        reportDTO.setLatitude(new BigDecimal("39.9042"));
        reportDTO.setLongitude(new BigDecimal("116.4074"));
        reportDTO.setAccuracy(new BigDecimal("10.5"));
        reportDTO.setLocationType("GPS");
        reportDTO.setLocationTime(LocalDateTime.now());

        // Mock算法
        when(algorithmFactory.getBestAlgorithm(any()))
            .thenReturn(locationAlgorithm);
        when(locationAlgorithm.processLocation(any()))
            .thenReturn(LocationPoint.builder()
                .latitude(new BigDecimal("39.9042"))
                .longitude(new BigDecimal("116.4074"))
                .accuracy(new BigDecimal("10.5"))
                .build());

        // 执行测试
        assertDoesNotThrow(() -> locationService.reportLocation(userId, reportDTO));

        // 验证结果
        LocationEntity savedLocation = locationDao.selectOne(
            new QueryWrapper<LocationEntity>()
                .eq("user_id", userId)
                .orderByDesc("location_time")
                .last("LIMIT 1")
        );

        assertNotNull(savedLocation);
        assertEquals(userId, savedLocation.getUserId());
        assertEquals(new BigDecimal("39.9042"), savedLocation.getLatitude());
        assertEquals(new BigDecimal("116.4074"), savedLocation.getLongitude());
        assertEquals("GPS", savedLocation.getLocationType());
        assertEquals(1, savedLocation.getIsValid());
    }

    @Test
    void testValidateLocation() {
        // 创建测试位置
        LocationEntity location = createTestLocation();
        locationDao.insert(location);

        // 准备验证数据
        LocationValidationDTO validationDTO = new LocationValidationDTO();
        validationDTO.setValidationType("GEOFENCE");
        validationDTO.setGeofenceId(1L);
        validationDTO.setToleranceDistance(new BigDecimal("50"));

        // 执行测试
        LocationValidationVO result = locationService.validateLocation(location.getUserId(), validationDTO);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getValidationTime());

        // 验证验证记录已保存
        LocationValidationEntity validationRecord = locationValidationDao.selectOne(
            new QueryWrapper<LocationValidationEntity>()
                .eq("user_id", location.getUserId())
                .eq("location_id", location.getLocationId())
                .orderByDesc("validation_time")
                .last("LIMIT 1")
        );

        assertNotNull(validationRecord);
        assertEquals("GEOFENCE", validationRecord.getValidationType());
    }

    @Test
    void testGetNearbyLocations() {
        // 创建多个用户的位置数据
        Long currentUserId = 1L;
        BigDecimal centerLat = new BigDecimal("39.9042");
        BigDecimal centerLng = new BigDecimal("116.4074");

        // 创建当前位置用户
        LocationEntity currentLocation = createTestLocation(currentUserId, centerLat, centerLng);
        locationDao.insert(currentLocation);

        // 创建附近的其他用户位置
        for (int i = 2; i <= 5; i++) {
            BigDecimal lat = centerLat.add(BigDecimal.valueOf(i * 0.001));
            BigDecimal lng = centerLng.add(BigDecimal.valueOf(i * 0.001));
            LocationEntity nearbyLocation = createTestLocation((long) i, lat, lng);
            locationDao.insert(nearbyLocation);
        }

        // 查询附近位置
        List<NearbyLocationVO> nearbyLocations = locationService.getNearbyLocations(
            currentUserId, centerLat, centerLng, 1000);

        // 验证结果
        assertFalse(nearbyLocations.isEmpty());
        assertEquals(4, nearbyLocations.size()); // 4个其他用户
        nearbyLocations.forEach(location -> {
            assertNotEquals(currentUserId, location.getUserId());
            assertNotNull(location.getDistance());
        });
    }

    private LocationEntity createTestLocation() {
        return createTestLocation(1L, new BigDecimal("39.9042"), new BigDecimal("116.4074"));
    }

    private LocationEntity createTestLocation(Long userId, BigDecimal latitude, BigDecimal longitude) {
        LocationEntity location = new LocationEntity();
        location.setUserId(userId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(new BigDecimal("10.5"));
        location.setLocationType("GPS");
        location.setLocationTime(LocalDateTime.now());
        location.setIsValid(1);
        return location;
    }
}
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已明确支持的定位方式？
- [ ] 是否已确认定位精度要求？
- [ ] 是否已了解地理围栏需求？
- [ ] 是否已确认位置数据安全要求？

### 开发中检查

- [ ] 是否实现了多定位算法支持？
- [ ] 是否添加了位置数据加密？
- [ ] 是否实现了地理围栏检测？
- [ ] 是否添加了位置缓存策略？
- [ ] 是否实现了轨迹分析功能？

### 部署前检查

- [ ] 定位算法配置是否正确？
- [ ] 地理围栏检测是否准确？
- [ ] 位置数据加密是否生效？
- [ ] 轨迹分析功能是否正常？
- [ ] 隐私保护措施是否到位？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [设备管理公共模块](./smart-device.md)
- [权限管理公共模块](./smart-permission.md)
- [实时数据公共模块](./smart-realtime.md)
- [综合开发规范文档](../DEV_STANDARDS.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*