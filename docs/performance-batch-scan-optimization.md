# 批量温度查询第二阶段性能优化方案（scanUnionRegion 瓶颈修复）

> 状态：待评审。评审通过后按"改动一 → 改动二"顺序实现，喵~
>
> 关联：`docs/performance-batch-temperature-query.md`（第一阶段方案，已实现）。
> 
> 文档已废弃：冷汗作者拒绝合并，FIAHI 已改用近似的纯 Raw Temperature 方法。

## 1. 背景与实测问题

第一阶段已交付并验证：

- Cold-Sweat 侧新增 `WorldHelper.getTemperaturesAt` / `getRoughTemperaturesAt` 批 API 及 `BlockTempScanBatch`；
- FIAHI 侧 `ForgeEventHandler` 改为"每维度节流 + 每 LevelTick 一次批查询"。

实测仍明显卡顿。Spark 采样显示时间集中在 Cold-Sweat `BlockTempScanBatch.scanUnionRegion`：

| 热点 | 占比 | 说明 |
| --- | --- | --- |
| `Long2ObjectOpenHashMap.get` | 17.66% | Phase A 每方块一次缓存查 |
| `LevelChunkSection.getBlockState` | 12.87% | 真正的方块状态读取，与被扫方块总数成正比 |
| `Long2ObjectOpenHashMap.put` | 10.58% | 每方块一次缓存写 |
| `HashMap.computeIfAbsent` | 2.88% | 每 (x, z) 列的 chunk 缓存查 |

## 2. 根因分析

### 2.1 扫描体过大：扫的是"全体位置包围盒"，不是"盒子并集"

`getRegionBounds()` 对**所有目标 pos 取 min/max 再外扩 range**，得到单一 AABB。FIAHI 每轮扫描的是"全部已加载区块里的所有容器"，容器散布在数百格见方的加载区时：

```
包围盒体积 ≈ (Δx + 2r)(Δz + 2r)(Δy + 2r)     例：165 × 165 × 18 ≈ 49 万格
```

而真正"距某个 pos ≤ range、可能产生贡献"的方块，只落在各 pos 自己的 (2r+1)³ 盒子内。包围盒中的大片空地、簇间空隙全部被无意义扫过：**容器越分散，批量版反而越慢**，这与第一阶段"区域并集一次扫描"的初衷（面向聚集场景）相悖。

### 2.2 Phase A 的 stateCache 是纯空转

单次 AABB 扫描中**每个方块只会被访问一次**，因此：

- `stateCache.get` 必然 miss（约 17.66%）；
- 之后必然 `getBlockState` + `stateCache.put`（put 约 10.58%）。

即 Phase A 里 get+put 合计约 28% 的开销没有任何收益。`stateCache` 的真正价值只在 **Phase B 的遮挡 ray**：多条 ray 会重复经过邻近方块，缓存命中可省去重复的 `section.getBlockState`。

### 2.3 关键事实（等价性论证的基石）

> 一个方块能影响某个 pos，当且仅当它落在该 pos 的 **±range Chebyshev 盒子**内（欧氏距离 ≤ r ⇒ 各轴差 ≤ r ⇒ 在盒内）。

因此：

- 只需扫描"各 pos 盒子的并集"即可收集到**完全相同的源方块集合**；
- 原包围盒里那些"扫到但距所有 pos 都超 range"的方块，在 Phase B 本就会被 `distance > range` 过滤掉，删掉它们不改变任何结果。

## 3. 改动一（Cold-Sweat 侧）：`BlockTempScanBatch` Phase A 重构

### 3.1 目标算法

1. **聚类**：把 pos 按"盒子是否相交"合并（两 pos 盒子相交 ⇔ Chebyshev 距离 ≤ 2r），用并查集/滑动窗口实现；
2. **每簇扫簇包围盒**：对每个簇取成员 pos 的 min/max 再外扩 range，逐格扫描；
3. 簇之间盒子互不相交 ⇒ **全批每个方块至多被访问一次**，Phase A 不再需要任何按方块索引的 Map；
4. Phase A 直接 `section.getBlockState` 读取，去掉 `stateCache` 的 get/put；
5. `stateCache` 字段保留，仅服务于 Phase B 的 `forBlocksInRay` 遮挡计数；
6. chunk 获取改为"簇 → 枚举覆盖 chunk 列表 → 逐 chunk 内以 `x & 15 / y & 15 / z & 15` 遍历"，或保留 LRU `chunkCache` 仅作跨簇共享（二选一，实现时以代码简洁为准）。

### 3.2 语义等价性

- 源方块收集集合与现实现一致（见 §2.3）：能影响任一 pos 的方块都在某簇盒内，簇盒并集 ⊆ 原包围盒，且"够不着任何 pos"的方块本就被 Phase B 丢弃；
- 距离、遮挡、fade、logarithmic、group clamp 逻辑**逐字不动**；
- 结果口径仍为"阈值等价（非逐位一致）"，与原 Javadoc 声明一致。

### 3.3 预期收益

- Phase A 的 `Long2ObjectOpenHashMap.get` / `.put`（约 28%）**整体消失**；
- 被扫方块数从"全体包围盒"降到"盒子并集"：分散场景可再降约 5～30 倍（视容器散布而定）；聚集场景不劣化（簇包围盒 ≈ 原聚集包围盒）。

### 3.4 边界情况

- 空位置集：调用方已保证非空（`getTemperaturesAt` 有空集早退），仍可加防御；
- 单例簇：扫描体即单个 (2r+1)³ 盒子，与单 pos 版一致；
- y 方向：簇包围盒仍含 y±r，逻辑不变；
- 同簇"对角线"成员：Chebyshev ≤ 2r 即合并，簇包围盒可能略大于盒子严格并集，属可接受上界。

## 4. 改动二（FIAHI 侧）：全精度批 → 粗精度批

### 4.1 缓存语义回顾

`WorldHelper.getRoughTemperatureAt`（WorldHelper:746）：

- 按 **8 格分段**（`pos >> 3 << 3`）缓存 `TempSnapshot`；
- 快照有效期为 `interval / tickSpeedMultiplier`：普通 1000 tick、`Sensitive`(flag 1) 200 tick，`tickSpeedMultiplier = 1 + randomTickSpeed/20`；
- 命中直接返回；未命中/过期才跑完整修饰器链并回填快照。

### 4.2 为什么适合 FIAHI

- FIAHI 每 `TEMPERATURE_CHECKER_INTERVAL`（默认 120 tick）把食物温度朝 `2.0 * worldTemp` **微调一次**，食品温度本身在多轮检查中平滑趋近，不依赖逐位世界温度；
- 同一容器及其邻近容器大概率落在同一 8 格分段内 → 快照共享；
- 各分段按各自时间戳**自然错峰过期**，重算压力被摊到不同 tick，不再有"每 6 秒一次集中大扫描"的尖峰。

### 4.3 具体改动

`ForgeEventHandler.onLevelTick` 中：

```java
// 改前
Object2DoubleMap<BlockPos> temperatures = WorldHelper.getTemperaturesAt(serverLevel, containerPositions);
// 改后
Object2DoubleMap<BlockPos> temperatures = WorldHelper.getRoughTemperaturesAt(serverLevel, containerPositions, 0);
```

flags 取 0（非 Sensitive、非 ForceUpdate），让快照按默认 1000 tick 周期错峰刷新。

### 4.4 预期收益与残余开销

- 每轮 FIAHI tick：N 次分段 map 命中，近乎零成本；
- 残余：世界加载后**首个 tick** 的一次性预热（所有分段同时过期）；此后刷新错峰摊开；
- 与改动一的关系：即便走粗 API，分段过期时仍会执行完整链（含方块体积扫描），Cold-Sweat 侧重构可同时降低该残余重算成本，因此**两项改动独立且互补**，可分别合入。

## 5. 依赖与发布节奏

1. 先合入/构建 Cold-Sweat 侧改动，重新构建 jar 并替换 `Freeze-It-And-Heat-It/libs/ColdSweat-2.4.2.jar`；
2. 再改 FIAHI 侧并重新编译验证；
3. Cold-Sweat PR 文案卖点：批量扫描耗时与**盒子并集体积**成正比（而非与位置包围盒成正比）。

## 6. 验收

- **正确性**：批量 vs 逐个（`getTemperatureAt`）对照，阈值语义一致；FIAHI 食物冻结/腐烂速率与方向不回归；
- **性能**：Spark 对比改动前后 `scanUnionRegion` / `onLevelTick` 累计耗时；场景：主世界大基地（大量箱子）+ 下界/末地 + 容器分散于多个据点；
- **回归**：`isAvailableToTickFood` 节流节奏、Inventory / ItemEntity mixin、维度节流行为不变。

## 7. 风险与后续（可选）

- 若"每 120 tick 一次全量收集 + 单次批查"仍有残余尖峰：可引入**批量预算**（每 tick 只处理 K 个 chunk/容器，轮转），作为第三阶段选项；
- 进阶：利用 `LevelChunkSection` palette 预筛（整节无任何 BlockTemp 状态即跳过），需 AT 支持，暂不做；
- 聚类正确性以 §3.4 边界用例覆盖（单例、对角线同簇、y 差异）。

## 8. 待办清单（评审通过后执行）

- [ ] Cold-Sweat：`BlockTempScanBatch` Phase A 重构（聚类 + 盒子并集扫描 + 去 stateCache 空转）
- [ ] Cold-Sweat：`compileJava` 编译验证
- [ ] Cold-Sweat：重新构建并替换 FIAHI `libs/ColdSweat-2.4.2.jar`
- [ ] FIAHI：`onLevelTick` 改用 `getRoughTemperaturesAt(..., 0)`
- [ ] FIAHI：`compileJava --rerun-tasks` 编译验证
- [ ] 实测：Spark 前后对比 + 冷冻/腐烂回归
