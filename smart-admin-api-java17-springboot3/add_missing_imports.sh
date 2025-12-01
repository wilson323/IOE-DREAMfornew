#!/bin/bash

echo "🔧 添加缺失的导入语句..."

# 找到使用这些类但没有导入的文件
for file in $(find sa-admin/src/main/java -name "*.java" -exec grep -l "BiometricEngineStatus\|TemplateRegistrationRequest\|TemplateRegistrationResult\|MultimodalAuthRequest\|MultimodalAuthResult\|BiometricAuthRequest" {} \;); do
    echo "检查文件: $file"
    
    # 如果使用BiometricEngineStatus但没有导入
    if grep -q "BiometricEngineStatus" "$file" && ! grep -q "import.*BiometricEngineStatus" "$file"; then
        echo "添加BiometricEngineStatus导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.BiometricEngineStatus;' "$file"
    fi
    
    # 如果使用TemplateRegistrationRequest但没有导入
    if grep -q "TemplateRegistrationRequest" "$file" && ! grep -q "import.*TemplateRegistrationRequest" "$file"; then
        echo "添加TemplateRegistrationRequest导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.TemplateRegistrationRequest;' "$file"
    fi
    
    # 如果使用TemplateRegistrationResult但没有导入
    if grep -q "TemplateRegistrationResult" "$file" && ! grep -q "import.*TemplateRegistrationResult" "$file"; then
        echo "添加TemplateRegistrationResult导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.TemplateRegistrationResult;' "$file"
    fi
    
    # 如果使用MultimodalAuthRequest但没有导入
    if grep -q "MultimodalAuthRequest" "$file" && ! grep -q "import.*MultimodalAuthRequest" "$file"; then
        echo "添加MultimodalAuthRequest导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.MultimodalAuthRequest;' "$file"
    fi
    
    # 如果使用MultimodalAuthResult但没有导入
    if grep -q "MultimodalAuthResult" "$file" && ! grep -q "import.*MultimodalAuthResult" "$file"; then
        echo "添加MultimodalAuthResult导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.MultimodalAuthResult;' "$file"
    fi
    
    # 如果使用BiometricAuthRequest但没有导入
    if grep -q "BiometricAuthRequest" "$file" && ! grep -q "import.*BiometricAuthRequest" "$file"; then
        echo "添加BiometricAuthRequest导入到: $file"
        sed -i '/package/a import net.lab1024.sa.admin.module.smart.biometric.engine.BiometricAuthRequest;' "$file"
    fi
done

echo "✅ 缺失导入语句添加完成"
