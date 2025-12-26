# 修复video-service类型错误的脚本
cd microservices

# 修复所有 LambdaQueryWrapper<Object> 问题
find ioedream-video-service -name "*.java" -exec sed -i 's/new LambdaQueryWrapper<>()$/new LambdaQueryWrapper<>()/g' {} \;
find ioedream-video-service -name "*.java" -exec sed -i 's/new LambdaQueryWrapper<>()$/new LambdaQueryWrapper<>()/g' {} \;

# 修复所有 Page<Object> 问题
find ioedream-video-service -name "*.java" -exec sed -i 's/new Page<>()$/new Page<>() /g' {} \;

echo "修复完成"