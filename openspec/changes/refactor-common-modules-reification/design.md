## Context

当前仓库存在以下“事实层矛盾”，属于根因级不稳定来源：

- `pom.xml` 中广泛引用 `microservices-common-data/security/cache/...` 等细粒度模块名
- 但这些模块未以 Maven Reactor 子模块形式落地（缺少目录/`pom.xml`/父 `microservices/pom.xml` `<modules>`）
- 结果是：编译/运行在不同机器、不同 CI Runner 上受缓存影响显著，形成“隐性不稳定”

本变更以“文档优先 + 提案先行 + 审批后实施”为门禁，先将“模块存在标准/边界规则”固化，再实施工程落盘与迁移。

## Goals / Non-Goals

### Goals

- 将 8 个细粒度 `microservices-common-*` 模块落盘为真实 Maven 子模块，纳入 Reactor，彻底消除幽灵依赖。
- 明确模块边界与依赖方向：底层稳定、上层可变；禁止环依赖；禁止领域实现回流到公共库底层。
- 提供可执行的迁移顺序、回滚策略与验收标准，确保企业级可控落地。

### Non-Goals

- 本提案阶段不直接修改业务代码与 POM（除非文档与 OpenSpec 产物）。
- 不在本变更中一次性完成所有历史技术债清理（例如 `microservices-common-core` “纯 Java”彻底达标需要分阶段）。

## Decisions

### Decision 1: “模块存在”以 Reactor 为准

- **规则**：模块必须满足“目录 + `pom.xml` + `microservices/pom.xml` `<modules>`”三要素，才允许被任何服务依赖。
- **理由**：以 Reactor 统一工程事实，消除对本地/CI 缓存的隐式依赖。

### Decision 2: 细粒度模块先“工程落盘”，再“逐步迁移实现”

- **规则**：先把 8 个模块作为可构建 jar 落地，并将依赖治理对齐；再按能力归属迁移代码。
- **理由**：先止血构建稳定性，再收敛边界与代码归属，降低一次性迁移风险。

### Decision 3: 领域实现归属到“拥有者微服务”，公共库仅承载“稳定契约/横切能力”

- **规则**：`video`、`visitor` 等领域实现不得以 `net.lab1024.sa.common.<domain>` 形式长期滞留在错误微服务或公共 jar 中。
- **理由**：防止公共库膨胀、依赖半径扩大、版本升级耦合。

## Risks / Trade-offs

- **风险：引用方广、变更面大**
  - **缓解**：分阶段；先落盘模块与依赖治理；再迁移实现；每阶段都有回滚点。
- **风险：历史缓存掩盖问题**
  - **缓解**：以 Reactor 为准；在 CI 中增加“禁止幽灵模块依赖”的检查。

## Migration Plan

1. 文档与 OpenSpec 提案先行（本阶段）
2. 工程落盘：新增 8 个模块并加入 Reactor
3. 依赖治理：统一各服务 POM，移除幽灵依赖/冗余依赖
4. 代码迁移：按能力归属迁移到对应模块或回归拥有者微服务
5. 验收：全量构建通过（按标准构建顺序），并通过依赖一致性/架构合规检查

## Open Questions

- `microservices-common-business` 的职责边界是否需要进一步收敛（例如拆为 domain-api + impl 回归服务）？
- `microservices-common-core` 的“纯 Java”达标拆迁范围是否作为独立后续提案推进？
