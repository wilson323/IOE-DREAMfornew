# MicroServices Common模块完整企业级修复脚本
# 彻底解决所有BOM和语法错误问题

Write-Host "=== MicroServices Common模块完整企业级修复脚本 ===" -ForegroundColor Cyan

# 完全重写edge包下的关键文件
$edgeFiles = @{
    "EdgeDeviceRegisterForm.java" = @'
package net.lab1024.sa.common.edge.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 边缘设备注册表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeDeviceRegisterForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    private String deviceDescription;
    private String manufacturer;
    private String model;
    private String version;
}
'@

    "EdgeConfig.java" = @'
package net.lab1024.sa.common.edge.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 边缘计算配置
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String configId;
    private String deviceId;
    private Integer inferenceMode;
    private Integer maxConcurrentTasks;
    private String modelPath;
    private Double threshold;
    private Integer batch_size;
    private Integer interval;
}
'@

    "EdgeDevice.java" = @'
package net.lab1024.sa.common.edge.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 边缘设备实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class EdgeDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String deviceName;
    private Integer deviceType;
    private String deviceStatus;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String description;
    private String ipAddress;
    private Integer port;
}
'@

    "InferenceRequest.java" = @'
package net.lab1024.sa.common.edge.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 推理请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class InferenceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String deviceId;
    private String modelType;
    private String inputData;
    private Integer inferenceMode;
    private String parameters;
}
'@

    "InferenceResult.java" = @'
package net.lab1024.sa.common.edge.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推理结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class InferenceResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String resultId;
    private String requestId;
    private String deviceId;
    private String resultData;
    private Double confidence;
    private Integer statusCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    private Long processDuration;
    private String errorMessage;
}
'@

    "ModelInfo.java" = @'
package net.lab1024.sa.common.edge.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型信息
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Data
public class ModelInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String modelId;
    private String modelName;
    private String modelVersion;
    private String modelType;
    private String modelPath;
    private Long modelSize;
    private String checksum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdateTime;

    private String description;
    private Integer status;
}
'@
}

$processedCount = 0
$errorCount = 0

foreach ($fileName in $edgeFiles.Keys) {
    $filePath = "microservices-common\src\main\java\net\lab1024\sa\common\edge\form\$fileName"
    if ($fileName -like "EdgeConfig*" -or $fileName -like "EdgeDevice*" -or $fileName -like "Inference*" -or $fileName -like "Model*") {
        $filePath = "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\$fileName"
    }

    try {
        Write-Host "创建: $filePath" -ForegroundColor Green

        # 确保目录存在
        $dir = Split-Path $filePath -Parent
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }

        # 写入文件内容，确保UTF8编码且无BOM
        $content = $edgeFiles[$fileName]
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText((Resolve-Path $filePath).Path, $content, $utf8NoBom)

        $processedCount++
        Write-Host "✅ 创建完成: $fileName" -ForegroundColor Green

    } catch {
        Write-Host "❌ 创建失败: $fileName - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host "`n=== Edge包文件创建统计 ===" -ForegroundColor Cyan
Write-Host "✅ 成功创建: $processedCount 个文件" -ForegroundColor Green
Write-Host "❌ 创建失败: $errorCount 个文件" -ForegroundColor Red

Write-Host "`n企业级修复完成！准备编译验证..." -ForegroundColor Cyan