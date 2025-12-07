#!/bin/bash

echo "🧪 测试分布式追踪功能..."

# 检查Zipkin服务状态
echo "📊 检查Zipkin服务..."
if curl -s http://localhost:9411/health > /dev/null; then
    echo "  ✅ Zipkin服务正常"
else
    echo "  ❌ Zipkin服务异常，请先部署Zipkin服务"
    echo "  运行: bash scripts/deploy-zipkin.sh"
    exit 1
fi

# 模拟API调用测试
echo ""
echo "🧪 模拟微服务调用测试..."
echo "请启动任意一个已配置分布式追踪的微服务，然后访问其API接口"
echo ""
echo "示例测试命令:"
echo "curl -H 'X-B3-TraceId: 1234567890abcdef' http://localhost:8080/actuator/health"
echo ""
echo "📋 验证方法:"
echo "1. 查看微服务日志，确认包含Trace ID"
echo "2. 访问Zipkin Web UI: http://localhost:9411"
echo "3. 查看追踪数据是否正常上报"
echo ""
echo "📊 期望结果:"
echo "- 微服务日志包含: [traceId:1234567890abcdef,spanId:xxxxxxxxxxxxxxxx]"
echo "- Zipkin UI显示服务调用链路"
echo "- 可以查看详细的调用时间线和依赖关系"
