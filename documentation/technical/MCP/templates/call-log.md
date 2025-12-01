# MCP 调用日志模板

> 版本: v1.0.0  
> 责任人: AI 协作组  
> 最后更新: 2025-11-12

```
- traceId: 20251112-arch-0001
  toolName: mentor.validate_architecture
  taskId: DOC_SYNC
  status: success
  operator: zhangsan
  request:
    contextVersion: v1.0.0
    entry: docs/COMMON_MODULES/api-contracts.md
  response:
    summary: "架构评审通过, 建议同步数据字典"
    nextActions:
      - "update data dictionary"
  startedAt: 2025-11-12T10:00:00+08:00
  finishedAt: 2025-11-12T10:02:30+08:00
```

> 注意: 字段命名统一使用 `camelCase`, YAML 缩进两个空格, 时间采用 ISO8601。
