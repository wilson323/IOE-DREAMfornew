#!/bin/bash
echo "修复sa-support模块的Lombok问题..."

cd "D:\IOE-DREAM\smart-admin-api-java17-springboot3"

# 查找sa-support模块中使用Lombok的文件
find sa-support/src/main/java -name "*.java" -exec grep -l "lombok\." {} \; > support_lombok_files.txt

echo "找到 $(wc -l < support_lombok_files.txt) 个使用Lombok的文件（sa-support模块）"

# 批量处理每个文件
while read file; do
    echo "处理文件: $file"

    # 备份原文件
    cp "$file" "$file.backup"

    # 移除Lombok导入
    sed -i '/^import lombok\./d' "$file"

    # 移除Lombok注解
    sed -i 's/@Data//g' "$file"
    sed -i 's/@Builder//g' "$file"
    sed -i 's/@NoArgsConstructor//g' "$file"
    sed -i 's/@AllArgsConstructor//g' "$file"
    sed -i 's/@RequiredArgsConstructor//g' "$file"
    sed -i 's/@ToString//g' "$file"
    sed -i 's/@EqualsAndHashCode.*$//g' "$file"
    sed -i 's/@Getter//g' "$file"
    sed -i 's/@Setter//g' "$file"

    echo "已清理Lombok注解: $file"

done < support_lombok_files.txt

echo "sa-support模块Lombok清理完成！"

# 立即为RbacResourceEntity添加必要的getter/setter
echo "为关键实体类添加必要的方法..."

# 为RbacResourceEntity添加基本getter/setter方法
rbac_file="sa-support/src/main/java/net/lab1024/sa/base/module/support/rbac/domain/entity/RbacResourceEntity.java"
if [ -f "$rbac_file" ]; then
    echo "为RbacResourceEntity添加getter/setter方法..."
    cat >> "$rbac_file" << 'EOF'

    // Getter和Setter方法
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourcePath() { return resourcePath; }
    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
EOF
    echo "RbacResourceEntity getter/setter方法添加完成"
fi

# 删除临时文件
rm -f support_lombok_files.txt

echo "sa-support模块修复完成！"