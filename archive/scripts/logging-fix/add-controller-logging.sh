#!/bin/bash

# 为Controller层添加日志记录的脚本
echo "开始为Controller层添加日志记录..."

for file in $(find microservices/ioedream-consume-service/src/main/java -name "*Controller.java" -type f); do
    echo "处理文件: $file"

    # 检查是否已经有@Slf4j注解
    if ! grep -q "@Slf4j" "$file"; then
        # 添加lombok.extern.slf4j.Slf4j导入
        if ! grep -q "import lombok.extern.slf4j.Slf4j;" "$file"; then
            sed -i '/import net.lab1024.sa.consume.service.ConsumeAccountService;/a import lombok.extern.slf4j.Slf4j;' "$file"
        fi

        # 在@RestController或@Controller前添加@Slf4j注解
        sed -i '/@RestController/i @Slf4j' "$file"
        sed -i '/@Controller/i @Slf4j' "$file"

        echo "  ✓ 已添加@Slf4j注解"
    else
        echo "  - 已有@Slf4j注解，跳过"
    fi
done

echo ""
echo "Controller层日志注解添加完成！"
echo ""
echo "接下来需要为关键API方法手动添加具体的日志记录语句。"