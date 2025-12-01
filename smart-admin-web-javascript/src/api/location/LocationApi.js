import SmartRequest from '/@/utils/SmartRequest';

export default class LocationApi {

  /**
   * 更新用户位置
   */
  static updateUserPosition(params) {
    return SmartRequest.request({
      url: '/api/location/position',
      method: 'post',
      data: params
    });
  }

  /**
   * 获取用户当前位置
   */
  static getCurrentUserLocation(userId) {
    return SmartRequest.request({
      url: `/api/location/current/${userId}`,
      method: 'get'
    });
  }

  /**
   * 获取用户位置历史
   */
  static getUserLocationHistory(params) {
    return SmartRequest.request({
      url: `/api/location/history/${params.userId}`,
      method: 'get',
      params: {
        startTime: params.startTime,
        endTime: params.endTime,
        limit: params.limit || 100
      }
    });
  }

  /**
   * 搜索附近位置
   */
  static searchNearbyLocations(params) {
    return SmartRequest.request({
      url: '/api/location/nearby',
      method: 'get',
      params: {
        latitude: params.latitude,
        longitude: params.longitude,
        radius: params.radius || 1000,
        limit: params.limit || 20
      }
    });
  }

  /**
   * 计算两点间距离
   */
  static calculateDistance(params) {
    return SmartRequest.request({
      url: '/api/location/distance',
      method: 'get',
      params: {
        lat1: params.lat1,
        lng1: params.lng1,
        lat2: params.lat2,
        lng2: params.lng2
      }
    });
  }

  /**
   * 创建地理围栏
   */
  static createGeoFence(params) {
    return SmartRequest.request({
      url: '/api/location/geo-fence',
      method: 'post',
      data: params
    });
  }

  /**
   * 更新地理围栏
   */
  static updateGeoFence(params) {
    return SmartRequest.request({
      url: '/api/location/geo-fence',
      method: 'put',
      data: params
    });
  }

  /**
   * 删除地理围栏
   */
  static deleteGeoFence(fenceId) {
    return SmartRequest.request({
      url: `/api/location/geo-fence/${fenceId}`,
      method: 'delete'
    });
  }

  /**
   * 获取地理围栏列表
   */
  static getGeoFenceList() {
    return SmartRequest.request({
      url: '/api/location/geo-fence/list',
      method: 'get'
    });
  }

  /**
   * 获取用户位置统计
   */
  static getUserLocationStatistics(params) {
    return SmartRequest.request({
      url: `/api/location/statistics/${params.userId}`,
      method: 'get',
      params: {
        startTime: params.startTime,
        endTime: params.endTime
      }
    });
  }

  /**
   * 实时位置更新
   */
  static startRealTimeTracking(userId, callback) {
    const wsUrl = `ws://localhost:1024/ws/location/track/${userId}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log('位置追踪连接已建立');
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        callback(data);
      } catch (error) {
        console.error('解析位置数据失败:', error);
      }
    };

    ws.onclose = () => {
      console.log('位置追踪连接已关闭');
    };

    ws.onerror = (error) => {
      console.error('位置追踪连接错误:', error);
    };

    return ws;
  }

  /**
   * 获取地理围栏触发记录
   */
  static getGeoFenceTriggers(params) {
    return SmartRequest.request({
      url: '/api/location/geo-fence/triggers',
      method: 'get',
      params: {
        fenceId: params.fenceId,
        userId: params.userId,
        startTime: params.startTime,
        endTime: params.endTime,
        limit: params.limit || 50
      }
    });
  }

  /**
   * 获取位置轨迹
   */
  static getLocationTrajectory(params) {
    return SmartRequest.request({
      url: '/api/location/trajectory',
      method: 'get',
      params: {
        userId: params.userId,
        startTime: params.startTime,
        endTime: params.endTime,
        type: params.type || 'DAILY'
      }
    });
  }

  /**
   * 位置搜索
   */
  static searchLocations(params) {
    return SmartRequest.request({
      url: '/api/location/search',
      method: 'get',
      params: {
        keyword: params.keyword,
        type: params.type,
        areaId: params.areaId,
        limit: params.limit || 20
      }
    });
  }
}