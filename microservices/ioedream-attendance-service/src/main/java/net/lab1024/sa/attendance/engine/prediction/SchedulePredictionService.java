package net.lab1024.sa.attendance.engine.prediction;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.SchedulePrediction;
import net.lab1024.sa.attendance.engine.prediction.SchedulePredictor;

/**
 * 排班预测服务（P2-Batch3阶段1创建）
 * <p>
 * 负责预测排班方案的效果，包括工作负载分布、成本预测、员工满意度预测等
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class SchedulePredictionService {

    private final SchedulePredictor schedulePredictor;

    public SchedulePredictionService(SchedulePredictor schedulePredictor) {
        this.schedulePredictor = schedulePredictor;
    }

    /**
     * 预测排班效果
     *
     * @param scheduleData 排班数据
     * @return 排班效果预测
     */
    public SchedulePrediction predictEffect(ScheduleData scheduleData) {
        log.debug("[排班预测服务] 预测排班效果");

        SchedulePrediction prediction = schedulePredictor.predict(scheduleData);

        log.info("[排班预测服务] 排班效果预测完成, 预测成功: {}", prediction.getPredictionSuccessful());

        return prediction;
    }
}
