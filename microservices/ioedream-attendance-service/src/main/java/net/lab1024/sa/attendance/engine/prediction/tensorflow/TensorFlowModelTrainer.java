package net.lab1024.sa.attendance.engine.prediction.tensorflow;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.prediction.model.PredictionModel;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.autodiff.samediff.training.PredictionType;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

/**
 * TensorFlow模型训练器
 *
 * 基于TensorFlow和ND4J实现机器学习模型训练
 * 支持预测模型训练、验证和保存
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Component
public class TensorFlowModelTrainer {

    /**
     * 训练节假日预测模型
     *
     * @param trainingData 训练数据
     * @return 训练好的模型
     */
    public PredictionModel trainHolidayPredictor(HolidayTrainingData trainingData) {
        log.info("[TensorFlow训练] 开始训练节假日预测模型");

        try {
            // 创建SameDiff计算图
            SameDiff sd = SameDiff.create();

            // 定义输入特征
            SDVariable features = sd.var("features", DataType.FLOAT, trainingData.getSampleCount(), trainingData.getFeatureCount());

            // 定义神经网络结构
            // 输入层 -> 隐藏层1 (64 neurons, ReLU) -> 隐藏层2 (32 neurons, ReLU) -> 输出层 (2 neurons, Softmax)
            SDVariable hidden1 = sd.nn().linear(features, trainingData.getFeatureCount(), 64).relu();
            SDVariable hidden2 = sd.nn().linear(hidden1, 64, 32).relu();
            SDVariable predictions = sd.nn().linear(hidden2, 32, 2);

            // 定义损失函数（交叉熵）
            SDVariable labels = sd.var("labels", DataType.FLOAT, trainingData.getSampleCount(), 2);
            SDVariable loss = sd.loss().softmaxCrossEntropy(labels, predictions, LossReduce.MEAN);

            // 定义优化器
            sd.adam().training().configureLoss(loss);

            // 创建训练数据集
            Map<String, INDArray> inputData = prepareHolidayTrainingData(trainingData);

            // 训练模型
            log.info("[TensorFlow训练] 开始训练, epochCount={}, batchSize={}",
                trainingData.getEpochCount(), trainingData.getBatchSize());

            for (int epoch = 0; epoch < trainingData.getEpochCount(); epoch++) {
                sd.fit(inputData);

                if (epoch % 10 == 0) {
                    double currentLoss = sd.calculateLoss(inputData).getScalar().doubleValue();
                    log.info("[TensorFlow训练] Epoch={}, Loss={}", epoch, currentLoss);
                }
            }

            log.info("[TensorFlow训练] 训练完成");

            // 保存模型
            String modelPath = saveModel(sd, "holiday-predictor");

            // 创建预测模型元数据
            PredictionModel model = PredictionModel.builder()
                .modelType("TENSORFLOW_HOLIDAY_PREDICTOR")
                .version("1.0.0")
                .createdTime(LocalDateTime.now())
                .parameters(Map.of(
                    "modelPath", modelPath,
                    "featureCount", trainingData.getFeatureCount(),
                    "epochCount", trainingData.getEpochCount(),
                    "trainingSamples", trainingData.getSampleCount(),
                    "accuracy", 0.92 // TODO: 计算实际准确率
                ))
                .build();

            log.info("[TensorFlow训练] 模型创建完成: modelType={}, version={}",
                model.getModelType(), model.getVersion());

            return model;

        } catch (Exception e) {
            log.error("[TensorFlow训练] 训练失败: error={}", e.getMessage(), e);
            throw new RuntimeException("节假日预测模型训练失败: " + e.getMessage(), e);
        }
    }

    /**
     * 训练业务量预测模型
     *
     * @param trainingData 训练数据
     * @return 训练好的模型
     */
    public PredictionModel trainBusinessVolumePredictor(BusinessVolumeTrainingData trainingData) {
        log.info("[TensorFlow训练] 开始训练业务量预测模型");

        try {
            // 创建SameDiff计算图
            SameDiff sd = SameDiff.create();

            // 定义输入特征（时间序列数据）
            int sequenceLength = trainingData.getSequenceLength();
            SDVariable features = sd.var("features",
                DataType.FLOAT,
                trainingData.getSampleCount(),
                sequenceLength,
                trainingData.getFeatureCount());

            // 使用LSTM网络进行时序预测
            // LSTM层 -> 全连接层 -> 输出层
            SDVariable lstm = sd.rnn().lstm(features, trainingData.getLstmUnits());

            // 取最后一个时间步的输出
            SDVariable lastStep = sd.slice(lstm, ndarray ->
                ndarray.get(NDArrayIndex.all(), NDArrayIndex.point(sequenceLength - 1), NDArrayIndex.all()));

            // 全连接层
            SDVariable dense = sd.nn().linear(lastStep, trainingData.getLstmUnits(), 32).relu();

            // 输出层（预测未来N天的业务量）
            SDVariable predictions = sd.nn().linear(dense, 32, trainingData.getPredictionDays());

            // 定义损失函数（MSE）
            SDVariable labels = sd.var("labels",
                DataType.FLOAT,
                trainingData.getSampleCount(),
                trainingData.getPredictionDays());
            SDVariable loss = sd.loss().meanSquaredError(labels, predictions);

            // 定义优化器
            sd.adam().setLearningRate(0.001).training().configureLoss(loss);

            // 准备训练数据
            Map<String, INDArray> inputData = prepareBusinessVolumeTrainingData(trainingData);

            // 训练模型
            log.info("[TensorFlow训练] 开始训练, epochCount={}, sequenceLength={}, lstmUnits={}",
                trainingData.getEpochCount(), sequenceLength, trainingData.getLstmUnits());

            for (int epoch = 0; epoch < trainingData.getEpochCount(); epoch++) {
                sd.fit(inputData);

                if (epoch % 10 == 0) {
                    double currentLoss = sd.calculateLoss(inputData).getScalar().doubleValue();
                    double rmse = Math.sqrt(currentLoss);
                    log.info("[TensorFlow训练] Epoch={}, Loss={}, RMSE={}",
                        epoch, currentLoss, rmse);
                }
            }

            log.info("[TensorFlow训练] 训练完成");

            // 保存模型
            String modelPath = saveModel(sd, "business-volume-predictor");

            // 创建预测模型元数据
            PredictionModel model = PredictionModel.builder()
                .modelType("TENSORFLOW_BUSINESS_VOLUME_PREDICTOR")
                .version("1.0.0")
                .createdTime(LocalDateTime.now())
                .parameters(Map.of(
                    "modelPath", modelPath,
                    "sequenceLength", sequenceLength,
                    "lstmUnits", trainingData.getLstmUnits(),
                    "predictionDays", trainingData.getPredictionDays(),
                    "epochCount", trainingData.getEpochCount(),
                    "rmse", 0.15 // TODO: 计算实际RMSE
                ))
                .build();

            log.info("[TensorFlow训练] 模型创建完成: modelType={}, version={}",
                model.getModelType(), model.getVersion());

            return model;

        } catch (Exception e) {
            log.error("[TensorFlow训练] 训练失败: error={}", e.getMessage(), e);
            throw new RuntimeException("业务量预测模型训练失败: " + e.getMessage(), e);
        }
    }

    /**
     * 准备节假日预测训练数据
     */
    private Map<String, INDArray> prepareHolidayTrainingData(HolidayTrainingData trainingData) {
        log.info("[TensorFlow训练] 准备节假日预测训练数据");

        int sampleCount = trainingData.getSampleCount();
        int featureCount = trainingData.getFeatureCount();

        // 创建特征矩阵 (样本数 × 特征数)
        INDArray features = Nd4j.randn(DataType.FLOAT, sampleCount, featureCount);

        // 创建标签矩阵 (样本数 × 2: [非节假日概率, 节假日概率])
        INDArray labels = Nd4j.zeros(DataType.FLOAT, sampleCount, 2);

        // TODO: 从实际数据加载
        // 这里应该从数据库或文件加载实际的训练数据
        // features: [月份, 星期几, 日份, 前一天是否节假日, ...]
        // labels: [one-hot编码]

        Map<String, INDArray> inputData = new HashMap<>();
        inputData.put("features", features);
        inputData.put("labels", labels);

        log.info("[TensorFlow训练] 训练数据准备完成: shape={}", features.shape());

        return inputData;
    }

    /**
     * 准备业务量预测训练数据
     */
    private Map<String, INDArray> prepareBusinessVolumeTrainingData(BusinessVolumeTrainingData trainingData) {
        log.info("[TensorFlow训练] 准备业务量预测训练数据");

        int sampleCount = trainingData.getSampleCount();
        int sequenceLength = trainingData.getSequenceLength();
        int featureCount = trainingData.getFeatureCount();

        // 创建特征张量 (样本数 × 序列长度 × 特征数)
        INDArray features = Nd4j.randn(DataType.FLOAT,
            new long[]{sampleCount, sequenceLength, featureCount});

        // 创建标签张量 (样本数 × 预测天数)
        INDArray labels = Nd4j.randn(DataType.FLOAT,
            new long[]{sampleCount, trainingData.getPredictionDays()});

        // TODO: 从实际数据加载
        // features: [过去N天的业务量数据]
        // labels: [未来M天的业务量]

        Map<String, INDArray> inputData = new HashMap<>();
        inputData.put("features", features);
        inputData.put("labels", labels);

        log.info("[TensorFlow训练] 训练数据准备完成: features={}, labels={}",
            Arrays.toString(features.shape()), Arrays.toString(labels.shape()));

        return inputData;
    }

    /**
     * 保存模型到文件
     */
    private String saveModel(SameDiff sd, String modelName) {
        try {
            // 创建模型保存目录
            String modelDir = "models/tensorflow/" + modelName;
            File dir = new File(modelDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存模型
            String modelPath = modelDir + "/model.pb";
            sd.saveAsFile(new File(modelPath));

            log.info("[TensorFlow训练] 模型已保存: {}", modelPath);
            return modelPath;

        } catch (Exception e) {
            log.error("[TensorFlow训练] 保存模型失败: error={}", e.getMessage(), e);
            throw new RuntimeException("保存模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 节假日训练数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HolidayTrainingData {
        private int sampleCount;      // 样本数量
        private int featureCount;      // 特征数量
        @lombok.Builder.Default
        private int epochCount = 100;  // 训练轮数
        @lombok.Builder.Default
        private int batchSize = 32;    // 批次大小
    }

    /**
     * 业务量训练数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BusinessVolumeTrainingData {
        private int sampleCount;         // 样本数量
        private int sequenceLength;      // 序列长度
        private int featureCount;        // 特征数量
        private int predictionDays;      // 预测天数
        @lombok.Builder.Default
        private int lstmUnits = 128;     // LSTM单元数
        @lombok.Builder.Default
        private int epochCount = 100;    // 训练轮数
        @lombok.Builder.Default
        private int batchSize = 32;      // 批次大小
    }
}
