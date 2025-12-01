# area Specification

## Purpose
Area capability defines hierarchical zone modeling (campus → building → floor → room) and maintains canonical path indexes that downstream权限、考勤、门禁等模块使用进行数据域过滤与联动查询。
## Requirements
### Requirement: 区域层级与路径索引
系统 SHALL 维护 `path/level/path_hash` 的一致性与索引，用于高效的层级查询与权限过滤。

#### Scenario: 新增子区域
- WHEN 在任一区域下新增子区域
- THEN 系统自动回填 `path`、`level`、`path_hash` 并创建必要索引
