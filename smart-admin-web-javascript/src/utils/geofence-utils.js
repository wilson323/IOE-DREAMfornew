/**
 * 地理围栏工具类
 * 提供地理围栏创建、检测、触发等功能
 */

import { calculateDistance, isPointInPolygon, isPointInCircle } from './MapUtils.js';

/**
 * 创建地理围栏
 * @param {Object} fenceData - 围栏数据
 * @returns {Object} 围栏对象
 */
export function createGeoFence(fenceData) {
  const {
    fenceId,
    fenceName,
    fenceType,
    triggerType,
    alertLevel,
    centerLatitude,
    centerLongitude,
    radius,
    minLatitude,
    maxLatitude,
    minLongitude,
    maxLongitude,
    polygonCoordinates,
    notificationMethods,
    activeTimeStart,
    activeTimeEnd,
    description
  } = fenceData;

  // 验证必填字段
  if (!fenceId || !fenceName || !fenceType) {
    throw new Error('围栏ID、名称和类型为必填字段');
  }

  // 验证围栏特定参数
  if (fenceType === 'CIRCULAR') {
    if (!centerLatitude || !centerLongitude || !radius) {
      throw new Error('圆形围栏需要中心坐标和半径');
    }
  } else if (fenceType === 'RECTANGLE') {
    if (!minLatitude || !maxLatitude || !minLongitude || !maxLongitude) {
      throw new Error('矩形围栏需要边界坐标');
    }
  } else if (fenceType === 'POLYGON') {
    if (!polygonCoordinates) {
      throw new Error('多边形围栏需要顶点坐标');
    }
  }

  const geoFence = {
    fenceId,
    fenceName,
    description: description || '',
    fenceType,
    status: 'ACTIVE',
    triggerType: triggerType || 'BOTH',
    alertLevel: alertLevel || 'MEDIUM',
    notificationMethods: notificationMethods || '',
    activeTimeStart: activeTimeStart || null,
    activeTimeEnd: activeTimeEnd || null,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString()
  };

  // 添加围栏类型特定参数
  switch (fenceType) {
    case 'CIRCULAR':
      geoFence.centerLatitude = centerLatitude;
      geoFence.centerLongitude = centerLongitude;
      geoFence.radius = radius;
      break;
    case 'RECTANGLE':
      geoFence.minLatitude = minLatitude;
      geoFence.maxLatitude = maxLatitude;
      geoFence.minLongitude = minLongitude;
      geoFence.maxLongitude = maxLongitude;
      break;
    case 'POLYGON':
      geoFence.polygonCoordinates = polygonCoordinates;
      break;
  }

  return geoFence;
}

/**
 * 验证地理围栏数据
 * @param {Object} fenceData - 围栏数据
 * @returns {Object} 验证结果
 */
export function validateGeoFence(fenceData) {
  const errors = [];

  // 基础字段验证
  if (!fenceData.fenceName || fenceData.fenceName.trim() === '') {
    errors.push('围栏名称不能为空');
  }

  if (!fenceData.fenceType) {
    errors.push('必须选择围栏类型');
  }

  // 围栏类型特定验证
  switch (fenceData.fenceType) {
    case 'CIRCULAR':
      if (!fenceData.centerLatitude || fenceData.centerLatitude < -90 || fenceData.centerLatitude > 90) {
        errors.push('中心纬度必须在-90到90之间');
      }
      if (!fenceData.centerLongitude || fenceData.centerLongitude < -180 || fenceData.centerLongitude > 180) {
        errors.push('中心经度必须在-180到180之间');
      }
      if (!fenceData.radius || fenceData.radius <= 0 || fenceData.radius > 10000) {
        errors.push('半径必须在1到10000米之间');
      }
      break;

    case 'RECTANGLE':
      if (!fenceData.minLatitude || !fenceData.maxLatitude) {
        errors.push('必须提供纬度范围');
      }
      if (!fenceData.minLongitude || !fenceData.maxLongitude) {
        errors.push('必须提供经度范围');
      }
      if (fenceData.minLatitude >= fenceData.maxLatitude) {
        errors.push('最小纬度必须小于最大纬度');
      }
      if (fenceData.minLongitude >= fenceData.maxLongitude) {
        errors.push('最小经度必须小于最大经度');
      }
      break;

    case 'POLYGON':
      if (!fenceData.polygonCoordinates) {
        errors.push('必须提供多边形顶点坐标');
      } else {
        try {
          const coordinates = JSON.parse(fenceData.polygonCoordinates);
          if (!Array.isArray(coordinates) || coordinates.length < 3) {
            errors.push('多边形至少需要3个顶点');
          }
          // 验证每个顶点坐标
          coordinates.forEach((coord, index) => {
            if (!Array.isArray(coord) || coord.length !== 2) {
              errors.push(`顶点${index + 1}坐标格式错误`);
            } else {
              const [lat, lng] = coord;
              if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                errors.push(`顶点${index + 1}坐标超出范围`);
              }
            }
          });
        } catch (error) {
          errors.push('多边形坐标格式错误');
        }
      }
      break;
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * 检查点是否在地理围栏内
 * @param {Object} point - 点信息 {latitude, longitude}
 * @param {Object} geoFence - 地理围栏对象
 * @returns {boolean} 是否在围栏内
 */
export function isPointInGeoFence(point, geoFence) {
  if (!point || !geoFence || geoFence.status !== 'ACTIVE') {
    return false;
  }

  const { latitude, longitude } = point;

  switch (geoFence.fenceType) {
    case 'CIRCULAR':
      return isPointInCircle(
        [latitude, longitude],
        [geoFence.centerLatitude, geoFence.centerLongitude],
        geoFence.radius
      );

    case 'RECTANGLE':
      return latitude >= geoFence.minLatitude &&
             latitude <= geoFence.maxLatitude &&
             longitude >= geoFence.minLongitude &&
             longitude <= geoFence.maxLongitude;

    case 'POLYGON':
      try {
        const coordinates = JSON.parse(geoFence.polygonCoordinates);
        return isPointInPolygon([latitude, longitude], coordinates);
      } catch (error) {
        console.error('解析多边形坐标失败:', error);
        return false;
      }

    default:
      return false;
  }
}

/**
 * 检查地理围栏触发状态
 * @param {Object} newPoint - 新位置点
 * @param {Object} oldPoint - 旧位置点
 * @param {Object} geoFence - 地理围栏
 * @returns {Object|null} 触发结果或null
 */
export function checkGeoFenceTrigger(newPoint, oldPoint, geoFence) {
  if (!geoFence || geoFence.status !== 'ACTIVE') {
    return null;
  }

  // 检查生效时间
  if (geoFence.activeTimeStart || geoFence.activeTimeEnd) {
    const now = new Date();
    const startTime = geoFence.activeTimeStart ? new Date(geoFence.activeTimeStart) : null;
    const endTime = geoFence.activeTimeEnd ? new Date(geoFence.activeTimeEnd) : null;

    if (startTime && now < startTime) {
      return null; // 未到生效时间
    }
    if (endTime && now > endTime) {
      return null; // 已过生效时间
    }
  }

  const wasInside = oldPoint ? isPointInGeoFence(oldPoint, geoFence) : false;
  const isInside = isPointInGeoFence(newPoint, geoFence);

  // 检查是否触发
  let triggerType = null;
  if (!wasInside && isInside && (geoFence.triggerType === 'IN' || geoFence.triggerType === 'BOTH')) {
    triggerType = 'IN';
  } else if (wasInside && !isInside && (geoFence.triggerType === 'OUT' || geoFence.triggerType === 'BOTH')) {
    triggerType = 'OUT';
  }

  if (triggerType) {
    return {
      triggerId: generateTriggerId(),
      fenceId: geoFence.fenceId,
      fenceName: geoFence.fenceName,
      userId: newPoint.userId,
      deviceId: newPoint.deviceId,
      triggerType,
      alertLevel: geoFence.alertLevel,
      triggerTime: new Date().toISOString(),
      latitude: newPoint.latitude,
      longitude: newPoint.longitude,
      processStatus: 'PENDING'
    };
  }

  return null;
}

/**
 * 批量检查多个地理围栏
 * @param {Object} point - 位置点
 * @param {Object} oldPoint - 旧位置点
 * @param {Array} geoFences - 地理围栏数组
 * @returns {Array} 触发记录数组
 */
export function checkMultipleGeoFences(point, oldPoint, geoFences) {
  if (!point || !geoFences || !Array.isArray(geoFences)) {
    return [];
  }

  const triggers = [];

  geoFences.forEach(fence => {
    const trigger = checkGeoFenceTrigger(point, oldPoint, fence);
    if (trigger) {
      triggers.push(trigger);
    }
  });

  return triggers;
}

/**
 * 计算地理围栏面积
 * @param {Object} geoFence - 地理围栏对象
 * @returns {number} 面积（平方米）
 */
export function calculateGeoFenceArea(geoFence) {
  if (!geoFence) return 0;

  switch (geoFence.fenceType) {
    case 'CIRCULAR':
      return Math.PI * Math.pow(geoFence.radius, 2);

    case 'RECTANGLE':
      const latDistance = calculateDistance(
        [geoFence.minLatitude, geoFence.minLongitude],
        [geoFence.maxLatitude, geoFence.minLongitude]
      );
      const lngDistance = calculateDistance(
        [geoFence.minLatitude, geoFence.minLongitude],
        [geoFence.minLatitude, geoFence.maxLongitude]
      );
      return latDistance * lngDistance;

    case 'POLYGON':
      try {
        const coordinates = JSON.parse(geoFence.polygonCoordinates);
        return calculatePolygonAreaFromCoords(coordinates);
      } catch (error) {
        console.error('计算多边形面积失败:', error);
        return 0;
      }

    default:
      return 0;
  }
}

/**
 * 计算多边形面积（基于坐标）
 * @param {Array} coordinates - 坐标数组
 * @returns {number} 面积（平方米）
 */
function calculatePolygonAreaFromCoords(coordinates) {
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
 * 获取地理围栏中心点
 * @param {Object} geoFence - 地理围栏对象
 * @returns {Array} 中心点坐标 [lat, lng]
 */
export function getGeoFenceCenter(geoFence) {
  if (!geoFence) return null;

  switch (geoFence.fenceType) {
    case 'CIRCULAR':
      return [geoFence.centerLatitude, geoFence.centerLongitude];

    case 'RECTANGLE':
      return [
        (geoFence.minLatitude + geoFence.maxLatitude) / 2,
        (geoFence.minLongitude + geoFence.maxLongitude) / 2
      ];

    case 'POLYGON':
      try {
        const coordinates = JSON.parse(geoFence.polygonCoordinates);
        const totalLat = coordinates.reduce((sum, coord) => sum + coord[0], 0);
        const totalLng = coordinates.reduce((sum, coord) => sum + coord[1], 0);
        return [
          totalLat / coordinates.length,
          totalLng / coordinates.length
        ];
      } catch (error) {
        console.error('计算多边形中心点失败:', error);
        return null;
      }

    default:
      return null;
  }
}

/**
 * 获取地理围栏边界
 * @param {Object} geoFence - 地理围栏对象
 * @returns {Object} 边界对象 {minLat, maxLat, minLng, maxLng}
 */
export function getGeoFenceBounds(geoFence) {
  if (!geoFence) return null;

  switch (geoFence.fenceType) {
    case 'CIRCULAR':
      // 近似计算圆形边界
      const radiusInDegrees = geoFence.radius / 111320; // 大约1度=111320米
      return {
        minLat: geoFence.centerLatitude - radiusInDegrees,
        maxLat: geoFence.centerLatitude + radiusInDegrees,
        minLng: geoFence.centerLongitude - radiusInDegrees,
        maxLng: geoFence.centerLongitude + radiusInDegrees
      };

    case 'RECTANGLE':
      return {
        minLat: geoFence.minLatitude,
        maxLat: geoFence.maxLatitude,
        minLng: geoFence.minLongitude,
        maxLng: geoFence.maxLongitude
      };

    case 'POLYGON':
      try {
        const coordinates = JSON.parse(geoFence.polygonCoordinates);
        const lats = coordinates.map(coord => coord[0]);
        const lngs = coordinates.map(coord => coord[1]);
        return {
          minLat: Math.min(...lats),
          maxLat: Math.max(...lats),
          minLng: Math.min(...lngs),
          maxLng: Math.max(...lngs)
        };
      } catch (error) {
        console.error('获取多边形边界失败:', error);
        return null;
      }

    default:
      return null;
  }
}

/**
 * 检查地理围栏是否重叠
 * @param {Object} fence1 - 围栏1
 * @param {Object} fence2 - 围栏2
 * @returns {boolean} 是否重叠
 */
export function areGeoFencesOverlapping(fence1, fence2) {
  if (!fence1 || !fence2) return false;

  // 获取两个围栏的边界
  const bounds1 = getGeoFenceBounds(fence1);
  const bounds2 = getGeoFenceBounds(fence2);

  if (!bounds1 || !bounds2) return false;

  // 检查边界是否重叠
  const overlapLat = !(bounds1.maxLat < bounds2.minLat || bounds2.maxLat < bounds1.minLat);
  const overlapLng = !(bounds1.maxLng < bounds2.minLng || bounds2.maxLng < bounds1.minLng);

  return overlapLat && overlapLng;
}

/**
 * 生成触发记录ID
 * @returns {string} 触发ID
 */
function generateTriggerId() {
  return 'trigger_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

/**
 * 格式化地理围栏类型显示
 * @param {string} fenceType - 围栏类型
 * @returns {string} 格式化后的类型名称
 */
export function formatGeoFenceType(fenceType) {
  const typeMap = {
    'CIRCULAR': '圆形围栏',
    'POLYGON': '多边形围栏',
    'RECTANGLE': '矩形围栏'
  };
  return typeMap[fenceType] || fenceType;
}

/**
 * 格式化触发类型显示
 * @param {string} triggerType - 触发类型
 * @returns {string} 格式化后的触发类型名称
 */
export function formatTriggerType(triggerType) {
  const typeMap = {
    'IN': '进入',
    'OUT': '离开',
    'BOTH': '双向'
  };
  return typeMap[triggerType] || triggerType;
}

/**
 * 格式化告警等级显示
 * @param {string} alertLevel - 告警等级
 * @returns {string} 格式化后的告警等级名称
 */
export function formatAlertLevel(alertLevel) {
  const levelMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'CRITICAL': '紧急'
  };
  return levelMap[alertLevel] || alertLevel;
}

/**
 * 获取告警等级颜色
 * @param {string} alertLevel - 告警等级
 * @returns {string} 颜色值
 */
export function getAlertLevelColor(alertLevel) {
  const colorMap = {
    'LOW': 'green',
    'MEDIUM': 'orange',
    'HIGH': 'red',
    'CRITICAL': 'purple'
  };
  return colorMap[alertLevel] || 'default';
}

/**
 * 创建地理围栏的地图图层数据
 * @param {Object} geoFence - 地理围栏对象
 * @returns {Object} 地图图层数据
 */
export function createGeoFenceMapLayer(geoFence) {
  if (!geoFence || geoFence.status !== 'ACTIVE') {
    return null;
  }

  const baseLayer = {
    id: geoFence.fenceId,
    name: geoFence.fenceName,
    type: geoFence.fenceType.toLowerCase(),
    color: getAlertLevelColor(geoFence.alertLevel),
    fillColor: getAlertLevelColor(geoFence.alertLevel),
    fillOpacity: 0.2,
    weight: 2,
    opacity: 0.8
  };

  switch (geoFence.fenceType) {
    case 'CIRCULAR':
      return {
        ...baseLayer,
        center: [geoFence.centerLatitude, geoFence.centerLongitude],
        radius: geoFence.radius
      };

    case 'POLYGON':
      try {
        const positions = JSON.parse(geoFence.polygonCoordinates);
        return {
          ...baseLayer,
          positions
        };
      } catch (error) {
        console.error('解析多边形坐标失败:', error);
        return null;
      }

    case 'RECTANGLE':
      return {
        ...baseLayer,
        positions: [
          [geoFence.minLatitude, geoFence.minLongitude],
          [geoFence.minLatitude, geoFence.maxLongitude],
          [geoFence.maxLatitude, geoFence.maxLongitude],
          [geoFence.maxLatitude, geoFence.minLongitude]
        ]
      };

    default:
      return null;
  }
}