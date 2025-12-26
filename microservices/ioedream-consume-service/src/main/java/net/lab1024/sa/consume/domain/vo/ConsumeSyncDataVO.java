package net.lab1024.sa.consume.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 移动端同步数据
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeSyncDataVO {

    private Long deviceId;

    private List<Map<String, Object>> userList;

    private List<Map<String, Object>> productList;

    private List<Map<String, Object>> mealList;

    private List<Map<String, Object>> areaList;

    private LocalDateTime syncTime;
}
