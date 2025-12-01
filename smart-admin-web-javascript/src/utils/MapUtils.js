/**
 * 地图工具类
 * 提供地图初始化、标记、围栏绘制等通用功能
 */

// 地图实例缓存
const mapInstances = new Map();

/**
 * 初始化地图
 * @param {string} containerId - 地图容器ID
 * @param {Object} options - 地图配置选项
 * @returns {Object} 地图实例
 */
export function initMap(containerId, options = {}) {
  const defaultOptions = {
    center: [39.9042, 116.4074], // 北京坐标
    zoom: 10,
    maxZoom: 18,
    minZoom: 3,
    attributionControl: true,
    zoomControl: true
  };

  const mapOptions = { ...defaultOptions, ...options };

  try {
    // 检测地图库（这里以Leaflet为例）
    if (window.L) {
      const map = window.L.map(containerId, mapOptions);

      // 添加地图图层
      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 18
      }).addTo(map);

      // 缓存地图实例
      mapInstances.set(containerId, map);

      return map;
    } else {
      console.error('地图库未加载，请确保已引入Leaflet或其他地图库');
      return null;
    }
  } catch (error) {
    console.error('初始化地图失败:', error);
    return null;
  }
}

/**
 * 添加标记点
 * @param {Object} map - 地图实例
 * @param {Array} coordinates - 坐标 [lat, lng]
 * @param {Object} options - 标记选项
 * @returns {Object} 标记实例
 */
export function addMarker(map, coordinates, options = {}) {
  if (!map || !coordinates || !window.L) return null;

  const defaultOptions = {
    title: '位置标记',
    draggable: false
  };

  const markerOptions = { ...defaultOptions, ...options };

  try {
    let marker;

    // 自定义图标
    if (options.icon) {
      const iconConfig = getIconConfig(options.icon);
      markerOptions.icon = window.L.icon(iconConfig);
    }

    marker = window.L.marker(coordinates, markerOptions).addTo(map);

    // 添加弹出窗口
    if (options.popup) {
      marker.bindPopup(options.popup);
    }

    // 添加工具提示
    if (options.title) {
      marker.bindTooltip(options.title, {
        permanent: false,
        direction: 'top'
      });
    }

    return marker;
  } catch (error) {
    console.error('添加标记失败:', error);
    return null;
  }
}

/**
 * 添加圆形围栏
 * @param {Object} map - 地图实例
 * @param {Array} center - 圆心坐标 [lat, lng]
 * @param {number} radius - 半径（米）
 * @param {Object} options - 圆形选项
 * @returns {Object} 圆形实例
 */
export function addCircle(map, center, radius, options = {}) {
  if (!map || !center || !radius || !window.L) return null;

  const defaultOptions = {
    color: '#3388ff',
    weight: 2,
    opacity: 0.8,
    fillOpacity: 0.2,
    fillColor: '#3388ff'
  };

  const circleOptions = { ...defaultOptions, ...options };

  try {
    const circle = window.L.circle(center, {
      radius: radius,
      ...circleOptions
    }).addTo(map);

    // 添加弹出窗口
    if (options.popup) {
      circle.bindPopup(options.popup);
    }

    return circle;
  } catch (error) {
    console.error('添加圆形围栏失败:', error);
    return null;
  }
}

/**
 * 添加多边形
 * @param {Object} map - 地图实例
 * @param {Array} coordinates - 坐标数组 [[lat1, lng1], [lat2, lng2], ...]
 * @param {Object} options - 多边形选项
 * @returns {Object} 多边形实例
 */
export function addPolygon(map, coordinates, options = {}) {
  if (!map || !coordinates || coordinates.length < 3 || !window.L) return null;

  const defaultOptions = {
    color: '#3388ff',
    weight: 2,
    opacity: 0.8,
    fillOpacity: 0.2,
    fillColor: '#3388ff'
  };

  const polygonOptions = { ...defaultOptions, ...options };

  try {
    const polygon = window.L.polygon(coordinates, polygonOptions).addTo(map);

    // 添加弹出窗口
    if (options.popup) {
      polygon.bindPopup(options.popup);
    }

    return polygon;
  } catch (error) {
    console.error('添加多边形失败:', error);
    return null;
  }
}

/**
 * 添加矩形
 * @param {Object} map - 地图实例
 * @param {Array} bounds - 边界坐标 [[lat1, lng1], [lat2, lng2]]
 * @param {Object} options - 矩形选项
 * @returns {Object} 矩形实例
 */
export function addRectangle(map, bounds, options = {}) {
  if (!map || !bounds || bounds.length !== 2 || !window.L) return null;

  const defaultOptions = {
    color: '#3388ff',
    weight: 2,
    opacity: 0.8,
    fillOpacity: 0.2,
    fillColor: '#3388ff'
  };

  const rectangleOptions = { ...defaultOptions, ...options };

  try {
    const rectangle = window.L.rectangle(bounds, rectangleOptions).addTo(map);

    // 添加弹出窗口
    if (options.popup) {
      rectangle.bindPopup(options.popup);
    }

    return rectangle;
  } catch (error) {
    console.error('添加矩形失败:', error);
    return null;
  }
}

/**
 * 添加折线
 * @param {Object} map - 地图实例
 * @param {Array} coordinates - 坐标数组 [[lat1, lng1], [lat2, lng2], ...]
 * @param {Object} options - 折线选项
 * @returns {Object} 折线实例
 */
export function addPolyline(map, coordinates, options = {}) {
  if (!map || !coordinates || coordinates.length < 2 || !window.L) return null;

  const defaultOptions = {
    color: '#3388ff',
    weight: 3,
    opacity: 0.7
  };

  const polylineOptions = { ...defaultOptions, ...options };

  try {
    const polyline = window.L.polyline(coordinates, polylineOptions).addTo(map);

    // 添加弹出窗口
    if (options.popup) {
      polyline.bindPopup(options.popup);
    }

    // 适应地图视野
    if (options.fitBounds) {
      map.fitBounds(polyline.getBounds());
    }

    return polyline;
  } catch (error) {
    console.error('添加折线失败:', error);
    return null;
  }
}

/**
 * 获取图标配置
 * @param {string} iconType - 图标类型
 * @returns {Object} 图标配置
 */
function getIconConfig(iconType) {
  const iconConfigs = {
    'default': {
      iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      shadowSize: [41, 41]
    },
    'current-location': {
      iconUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjUiIGhlaWdodD0iNDEiIHZpZXdCb3g9IjAgMCAyNSA0MSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjUgMEMxMi41IDAgMCAxMi41IDBDMCAxMi41IDQuNSAyNSAxMi41IDQxQzIwLjUgMjUgMjUgMTIuNSAyNSAxMi41QzI1IDAgMTIuNSAwIDEyLjUgWk0xMi41IDJDNy44IDIgNCA1LjggNCAxMi41QzQgMTkuMiA3LjggMjMgMTIuNSAyM0MxNy4yIDIzIDIxIDE5LjIgMjEgMTIuNUMyMSA1LjggMTcuMiAyIDEyLjUgMloiIGZpbGw9IiM0Mjk5ZTEiLz4KPGNpcmNsZSBjeD0iMTIuNSIgY3k9IjEyLjUiIHI9IjMiIGZpbGw9IndoaXRlIi8+Cjwvc3ZnPgo=',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    },
    'real-time': {
      iconUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjUiIGhlaWdodD0iNDEiIHZpZXdCb3g9IjAgMCAyNSA0MSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjUgMEMxMi41IDAgMCAxMi41IDBDMCAxMi41IDQuNSAyNSAxMi41IDQxQzIwLjUgMjUgMjUgMTIuNSAyNSAxMi41QzI1IDAgMTIuNSAwIDEyLjUgWk0xMi41IDJDNy44IDIgNCA1LjggNCAxMi41QzQgMTkuMiA3LjggMjMgMTIuNSAyM0MxNy4yIDIzIDIxIDE5LjIgMjEgMTIuNUMyMSA1LjggMTcuMiAyIDEyLjUgMloiIGZpbGw9IiNmNTIyMmQiLz4KPGNpcmNsZSBjeD0iMTIuNSIgY3k9IjEyLjUiIHI9IjMiIGZpbGw9IndoaXRlIi8+CjxhbmltYXRlIGF0dHJpYnV0ZU5hbWU9ImZpbGwiIHZhbHVlcz0iI2Y1MjIyZDsjZmY2MjYzOyNmNTIyMmQiIGR1cj0iMXMiIHJlcGVhdENvdW50PSJpbmRlZmluaXRlIi8+Cjwvc3ZnPgo=',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    },
    'latest': {
      iconUrl: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjUiIGhlaWdodD0iNDEiIHZpZXdCb3g9IjAgMCAyNSA0MSIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTEyLjUgMEMxMi41IDAgMCAxMi41IDBDMCAxMi41IDQuNSAyNSAxMi41IDQxQzIwLjUgMjUgMjUgMTIuNSAyNSAxMi41QzI1IDAgMTIuNSAwIDEyLjUgWk0xMi41IDJDNy44IDIgNCA1LjggNCAxMi41QzQgMTkuMiA3LjggMjMgMTIuNSAyM0MxNy4yIDIzIDIxIDE5LjIgMjEgMTIuNUMyMSA1LjggMTcuMiAyIDEyLjUgMloiIGZpbGw9IiM1MmM0MWUiLz4KPGNpcmNsZSBjeD0iMTIuNSIgY3k9IjEyLjUiIHI9IjMiIGZpbGw9IndoaXRlIi8+Cjwvc3ZnPgo=',
      iconSize: [25, 41],
      iconAnchor: [12, 41]
    }
  };

  return iconConfigs[iconType] || iconConfigs['default'];
}

/**
 * 清除地图上的所有图层
 * @param {Object} map - 地图实例
 */
export function clearMapLayers(map) {
  if (!map || !window.L) return;

  try {
    // 清除所有图层（除了底图）
    map.eachLayer((layer) => {
      if (!(layer instanceof window.L.TileLayer)) {
        map.removeLayer(layer);
      }
    });
  } catch (error) {
    console.error('清除地图图层失败:', error);
  }
}

/**
 * 适应地图视野到所有标记
 * @param {Object} map - 地图实例
 * @param {Array} markers - 标记数组
 * @param {Object} options - 适应选项
 */
export function fitMapToMarkers(map, markers, options = {}) {
  if (!map || !markers || !markers.length || !window.L) return;

  const defaultOptions = {
    padding: [50, 50],
    maxZoom: 16
  };

  const fitOptions = { ...defaultOptions, ...options };

  try {
    const group = new window.L.featureGroup(markers);
    map.fitBounds(group.getBounds(), fitOptions);
  } catch (error) {
    console.error('适应地图视野失败:', error);
  }
}

/**
 * 计算两点间距离
 * @param {Array} point1 - 点1坐标 [lat1, lng1]
 * @param {Array} point2 - 点2坐标 [lat2, lng2]
 * @returns {number} 距离（米）
 */
export function calculateDistance(point1, point2) {
  if (!point1 || !point2 || point1.length < 2 || point2.length < 2) return 0;

  const R = 6371; // 地球半径（公里）
  const lat1 = point1[0] * Math.PI / 180;
  const lat2 = point2[0] * Math.PI / 180;
  const deltaLat = (point2[0] - point1[0]) * Math.PI / 180;
  const deltaLng = (point2[1] - point1[1]) * Math.PI / 180;

  const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c * 1000; // 返回米
}

/**
 * 销毁地图实例
 * @param {string} containerId - 地图容器ID
 */
export function destroyMap(containerId) {
  if (mapInstances.has(containerId)) {
    const map = mapInstances.get(containerId);
    if (map && map.remove) {
      map.remove();
    }
    mapInstances.delete(containerId);
  }
}

/**
 * 获取地图实例
 * @param {string} containerId - 地图容器ID
 * @returns {Object} 地图实例
 */
export function getMapInstance(containerId) {
  return mapInstances.get(containerId);
}

/**
 * 根据地址搜索坐标（需要集成地理编码API）
 * @param {string} address - 地址
 * @returns {Promise<Array>} 坐标 [lat, lng]
 */
export async function geocodeAddress(address) {
  // 这里需要集成第三方地理编码服务，如高德地图、百度地图等
  // 暂时返回null
  console.warn('地理编码服务需要集成第三方API');
  return null;
}

/**
 * 根据坐标获取地址（需要集成反地理编码API）
 * @param {number} lat - 纬度
 * @param {number} lng - 经度
 * @returns {Promise<string>} 地址
 */
export async function reverseGeocode(lat, lng) {
  // 这里需要集成第三方反地理编码服务
  // 暂时返回坐标信息
  console.warn('反地理编码服务需要集成第三方API');
  return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
}

/**
 * 添加热力图图层
 * @param {Object} map - 地图实例
 * @param {Array} data - 热力图数据点
 * @param {Object} options - 热力图选项
 * @returns {Object} 热力图图层实例
 */
export function addHeatmap(map, data, options = {}) {
  if (!map || !data || !data.length || !window.L) return null;

  const defaultOptions = {
    radius: 25,
    blur: 15,
    maxZoom: 17,
    max: 1.0,
    gradient: {
      0.4: 'blue',
      0.6: 'cyan',
      0.7: 'lime',
      0.8: 'yellow',
      1.0: 'red'
    }
  };

  const heatmapOptions = { ...defaultOptions, ...options };

  try {
    // 检查是否已加载热力图插件
    if (window.L.heatLayer) {
      const heatData = data.map(point => [point.latitude, point.longitude, point.weight || 1]);
      return window.L.heatLayer(heatData, heatmapOptions).addTo(map);
    } else {
      console.warn('热力图插件未加载，请引入 leaflet.heat 插件');
      return null;
    }
  } catch (error) {
    console.error('添加热力图失败:', error);
    return null;
  }
}

/**
 * 创建轨迹动画
 * @param {Object} map - 地图实例
 * @param {Array} coordinates - 轨迹坐标点
 * @param {Object} options - 动画选项
 * @returns {Object} 动画控制器
 */
export function createTrajectoryAnimation(map, coordinates, options = {}) {
  if (!map || !coordinates || coordinates.length < 2) return null;

  const defaultOptions = {
    duration: 5000,        // 动画持续时间（毫秒）
    markerIcon: 'default', // 移动标记图标
    showTrail: true,       // 显示轨迹线
    trailColor: '#1890ff', // 轨迹线颜色
    autoPlay: true,        // 自动播放
    loop: false            // 循环播放
  };

  const animOptions = { ...defaultOptions, ...options };

  try {
    let currentIndex = 0;
    let animationTimer = null;
    let trailPolyline = null;
    let movingMarker = null;

    // 创建轨迹线
    if (animOptions.showTrail) {
      trailPolyline = addPolyline(map, coordinates, {
        color: animOptions.trailColor,
        weight: 3,
        opacity: 0.6
      });
    }

    // 创建移动标记
    movingMarker = addMarker(map, coordinates[0], {
      icon: animOptions.markerIcon,
      title: '移动轨迹'
    });

    const controller = {
      play() {
        if (animationTimer) return;

        const interval = animOptions.duration / coordinates.length;
        animationTimer = setInterval(() => {
          currentIndex++;

          if (currentIndex >= coordinates.length) {
            if (animOptions.loop) {
              currentIndex = 0;
            } else {
              this.pause();
              if (animOptions.onComplete) {
                animOptions.onComplete();
              }
              return;
            }
          }

          movingMarker.setLatLng(coordinates[currentIndex]);

          if (animOptions.onProgress) {
            animOptions.onProgress(currentIndex, coordinates[currentIndex]);
          }
        }, interval);
      },

      pause() {
        if (animationTimer) {
          clearInterval(animationTimer);
          animationTimer = null;
        }
      },

      reset() {
        this.pause();
        currentIndex = 0;
        movingMarker.setLatLng(coordinates[0]);
      },

      setPosition(index) {
        if (index >= 0 && index < coordinates.length) {
          currentIndex = index;
          movingMarker.setLatLng(coordinates[index]);
        }
      },

      getCurrentIndex() {
        return currentIndex;
      },

      destroy() {
        this.pause();
        if (movingMarker) {
          map.removeLayer(movingMarker);
        }
        if (trailPolyline) {
          map.removeLayer(trailPolyline);
        }
      }
    };

    if (animOptions.autoPlay) {
      controller.play();
    }

    return controller;
  } catch (error) {
    console.error('创建轨迹动画失败:', error);
    return null;
  }
}

/**
 * 测量距离
 * @param {Object} map - 地图实例
 * @param {Array} coordinates - 坐标点数组
 * @param {Object} options - 测量选项
 * @returns {Object} 测量结果
 */
export function measureDistance(map, coordinates, options = {}) {
  if (!map || !coordinates || coordinates.length < 2) {
    return { totalDistance: 0, segments: [] };
  }

  const defaultOptions = {
    showMarkers: true,
    showLabels: true,
    unit: 'meters'  // 'meters' | 'kilometers'
  };

  const measureOptions = { ...defaultOptions, ...options };
  const segments = [];
  let totalDistance = 0;

  try {
    // 计算每段距离
    for (let i = 1; i < coordinates.length; i++) {
      const distance = calculateDistance(coordinates[i - 1], coordinates[i]);
      totalDistance += distance;

      segments.push({
        start: coordinates[i - 1],
        end: coordinates[i],
        distance: distance
      });
    }

    // 显示测量线
    if (measureOptions.showLine) {
      addPolyline(map, coordinates, {
        color: '#ff0000',
        weight: 2,
        opacity: 0.8,
        dashArray: '5, 10'
      });
    }

    // 显示标记点
    if (measureOptions.showMarkers) {
      coordinates.forEach((coord, index) => {
        const label = index === 0 ? '起点' :
                     index === coordinates.length - 1 ? '终点' :
                     `点${index}`;

        addMarker(map, coord, {
          title: label,
          icon: 'default'
        });
      });
    }

    // 显示距离标签
    if (measureOptions.showLabels) {
      // 在每段中点显示距离
      segments.forEach((segment, index) => {
        const midPoint = [
          (segment.start[0] + segment.end[0]) / 2,
          (segment.start[1] + segment.end[1]) / 2
        ];

        const distanceText = measureOptions.unit === 'kilometers'
          ? `${(segment.distance / 1000).toFixed(2)}km`
          : `${Math.round(segment.distance)}m`;

        addMarker(map, midPoint, {
          title: distanceText,
          icon: 'default'
        });
      });

      // 显示总距离
      if (coordinates.length > 2) {
        const lastPoint = coordinates[coordinates.length - 1];
        const totalText = measureOptions.unit === 'kilometers'
          ? `总距离: ${(totalDistance / 1000).toFixed(2)}km`
          : `总距离: ${Math.round(totalDistance)}m`;

        addMarker(map, lastPoint, {
          title: totalText,
          icon: 'default'
        });
      }
    }

    return {
      totalDistance,
      segments,
      unit: measureOptions.unit
    };
  } catch (error) {
    console.error('测量距离失败:', error);
    return { totalDistance: 0, segments: [] };
  }
}

/**
 * 计算多边形面积
 * @param {Array} coordinates - 多边形顶点坐标
 * @returns {number} 面积（平方米）
 */
export function calculatePolygonArea(coordinates) {
  if (!coordinates || coordinates.length < 3) return 0;

  try {
    // 使用Shoelace公式计算多边形面积
    let area = 0;
    const n = coordinates.length;

    for (let i = 0; i < n; i++) {
      const j = (i + 1) % n;
      const lat1 = coordinates[i][0] * Math.PI / 180;
      const lng1 = coordinates[i][1] * Math.PI / 180;
      const lat2 = coordinates[j][0] * Math.PI / 180;
      const lng2 = coordinates[j][1] * Math.PI / 180;

      area += (lng2 - lng1) * (2 + Math.sin(lat1) + Math.sin(lat2));
    }

    const R = 6371000; // 地球半径（米）
    area = Math.abs(area * R * R / 2);

    return area;
  } catch (error) {
    console.error('计算多边形面积失败:', error);
    return 0;
  }
}

/**
 * 检查点是否在多边形内
 * @param {Array} point - 点坐标 [lat, lng]
 * @param {Array} polygon - 多边形顶点坐标
 * @returns {boolean} 是否在多边形内
 */
export function isPointInPolygon(point, polygon) {
  if (!point || !polygon || polygon.length < 3) return false;

  try {
    const [lat, lng] = point;
    let inside = false;

    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const [xi, yi] = polygon[i];
      const [xj, yj] = polygon[j];

      if (((yi > lat) !== (yj > lat)) &&
          (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)) {
        inside = !inside;
      }
    }

    return inside;
  } catch (error) {
    console.error('检查点是否在多边形内失败:', error);
    return false;
  }
}

/**
 * 检查点是否在圆内
 * @param {Array} point - 点坐标 [lat, lng]
 * @param {Array} center - 圆心坐标 [lat, lng]
 * @param {number} radius - 半径（米）
 * @returns {boolean} 是否在圆内
 */
export function isPointInCircle(point, center, radius) {
  if (!point || !center || !radius) return false;

  try {
    const distance = calculateDistance(point, center);
    return distance <= radius;
  } catch (error) {
    console.error('检查点是否在圆内失败:', error);
    return false;
  }
}

/**
 * 格式化坐标显示
 * @param {number} lat - 纬度
 * @param {number} lng - 经度
 * @param {number} precision - 精度（小数位数）
 * @returns {string} 格式化后的坐标
 */
export function formatCoordinates(lat, lng, precision = 6) {
  if (lat === null || lat === undefined || lng === null || lng === undefined) {
    return '-';
  }
  return `${lat.toFixed(precision)}, ${lng.toFixed(precision)}`;
}

/**
 * 解析坐标字符串
 * @param {string} coordStr - 坐标字符串 "lat,lng"
 * @returns {Array|null} 坐标数组 [lat, lng] 或 null
 */
export function parseCoordinates(coordStr) {
  if (!coordStr || typeof coordStr !== 'string') return null;

  try {
    const parts = coordStr.split(',').map(part => part.trim());
    if (parts.length !== 2) return null;

    const lat = parseFloat(parts[0]);
    const lng = parseFloat(parts[1]);

    if (isNaN(lat) || isNaN(lng)) return null;

    return [lat, lng];
  } catch (error) {
    console.error('解析坐标字符串失败:', error);
    return null;
  }
}

/**
 * 创建地图边界
 * @param {Array} coordinates - 坐标点数组
 * @param {number} buffer - 缓冲区（度）
 * @returns {Object} 边界对象
 */
export function createBounds(coordinates, buffer = 0.001) {
  if (!coordinates || coordinates.length === 0) {
    return {
      southwest: [0, 0],
      northeast: [0, 0]
    };
  }

  try {
    const lats = coordinates.map(coord => coord[0]);
    const lngs = coordinates.map(coord => coord[1]);

    const minLat = Math.min(...lats) - buffer;
    const maxLat = Math.max(...lats) + buffer;
    const minLng = Math.min(...lngs) - buffer;
    const maxLng = Math.max(...lngs) + buffer;

    return {
      southwest: [minLat, minLng],
      northeast: [maxLat, maxLng]
    };
  } catch (error) {
    console.error('创建地图边界失败:', error);
    return {
      southwest: [0, 0],
      northeast: [0, 0]
    };
  }
}

/**
 * 清除地图（包括所有图层）
 * @param {Object} map - 地图实例
 */
export function clearMap(map) {
  if (!map || !window.L) return;

  try {
    map.eachLayer((layer) => {
      map.removeLayer(layer);
    });
  } catch (error) {
    console.error('清除地图失败:', error);
  }
}