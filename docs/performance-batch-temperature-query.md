# FIAHI 食物温度批量查询性能优化方案

> 文档已废弃：冷汗作者拒绝合并，FIAHI 已改用近似的纯 Raw Temperature 方法。

## 1. 需求背景

Cold-Sweat 引入温度机制；附属模组 FIAHI（Freeze-It-And-Heat-It）把温度扩展到食物，实现冷冻与腐烂。

FIAHI 的 `com.hexagram2021.fiahi.common.ForgeEventHandler#onLevelTick` 每个服务器 tick 遍历所有已加载区块中的容器方块实体，逐个调用 `WorldHelper.getTemperatureAt` 计算该位置的世界温度，再按温度修改容器内食物。

该做法存在两个性能问题，需优化：

1. **维度节流逻辑不一致**：只有主世界（OVERWORLD）按 `TEMPERATURE_CHECKER_INTERVAL`（默认 120 ticks）节流；下界与末地完全不节流，**每个 tick 都会全量扫描并逐个计算**。
2. **逐个查询导致复杂度偏高**：N 个容器就是 N 次独立的、昂贵的 `getTemperatureAt`，重叠区域被重复扫描。

## 2. 现状剖析

### 2.1 FIAHI `onLevelTick` 的结构

```java
@SubscribeEvent
public static void onLevelTick(LevelTickEvent.Post event) {
    if(event.getLevel() instanceof ServerLevel serverLevel) {
        if(serverLevel.dimension().equals(Level.OVERWORLD)) {   // 仅主世界节流
            if(tickAfterCheck < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
                ++tickAfterCheck;
                return;
            }
            tickAfterCheck = 0;
        }
        serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunk -> {
            LevelChunk levelChunk = chunk.getTickingChunk();
            if(levelChunk != null && !levelChunk.isEmpty()) {
                Map<BlockPos, BlockEntity> blockEntities = getBlockPosBlockEntityMap(chunk, levelChunk);
                blockEntities.forEach((blockPos, blockEntity) -> {
                    // ... 过滤出 Container / IItemHandlerModifiable
                    tickContainer(blockEntity, container, blockPos, ..., serverLevel);
                });
            }
        });
    }
}
```

其中每个容器最终调用 `tickContainer`：

```java
private static <T> void tickContainer(...) {
    double temp = WorldHelper.getTemperatureAt(Objects.requireNonNull(blockEntity.getLevel()), blockPos);
    for (int i = 0; i < size; ++i) {
        // ... IFrozenRottenFood.tick(food, ..., c -> c.getTemperature() + 2.0D * temp, ...)
    }
}
```

这正是逐容器、逐 tick 执行昂贵查询的根源。

### 2.2 一次 `WorldHelper.getTemperatureAt` 到底花掉了什么

`getTemperatureAt`（`WorldHelper.java:808`）针对**一个** `BlockPos` 做完整的世界温度计算：

1. `getInsulationAt(level, pos, 2)`（`WorldHelper.java:960`）
   - 遍历以该 pos 为中心的 **5×5 = 25 个 chunk**，对每个 chunk 遍历其全部方块实体，查找 `HearthBlockEntity`。
   - 成本 ∝ 区域内的方块实体总数。

2. `Temperature.apply(0, dummy, Trait.WORLD, modifiers, true)` 依次执行 dummy 玩家（`DummyPlayer`）的
   全套 WORLD 修饰器，其中两个最贵：
   - `BlockTempModifier.calculate()`：在 `(2·range)³` 的空间内（`range = ConfigSettings.BLOCK_RANGE.get()` 默认 **7**，
     即约 **2744 个方块位置**）逐 block 查 `BlockState`、命中后做光线投射 `forBlocksInRay`。
   - `BiomeTempModifier.calculate()`：采样 `49` 个 biome 位置（含结构温度查询 `getStructureAt`）。

单次 `getTemperatureAt` 的成本近似为 **O(采样的方块总量)**，即约千级别 block 查找 + 结构查询 + 25 chunk 扫描。

### 2.3 复杂度结论：为什么是“二次方”

令 N = 需计算温度的容器数，S = 单次计算扫描并被复用的方块/实体规模。

- 当前逐容器调用：总成本 ≈ **N × S**。
- 当容器在空间上聚集（仓储室、冰块箱、冰箱阵列）时，`S` 相同，总成本随 N 线性放大；若把“每个 pos 扫描的区域”视为输入规模 n（N 个 pos 各自覆盖 O(区域内方块) 的网格），则可表述为 **O(n²)**（每增加一个容器，新增 O(n) 的扫描量）。

因此用户的“O(n²) → O(n)”判断在容器聚集的场景下成立。

## 3. 增益评估

### 3.1 改动一：维度节流对齐（增益：确定性、显著）

下界与末地当前是**每个 tick 全量扫描**所有容器的方块实体并逐格计算温度；主世界则是每 `TEMPERATURE_CHECKER_INTERVAL`（默认 120）tick 才跑一次。

- 由于除主世界外，维度节流完全缺失，下界/末地每一累计 tick 的成本是主世界的 ~120 倍。
- 对齐后把这些维度从“每 tick”降到“每 120 tick”，**直接消除约 99%+ 的无效扫描**。
- 实现正确性清晰，风险极低。这是本方案中**最直接、最稳的收益**，应作为第一步落地。

### 3.2 改动二：批量温度查询（增益：场景相关、需正确实现）

**关键结论：单纯把“逐个调用”包成一个“批方法”不会带来收益。**

`BlockTempModifier` 每次 `calculate()` 开头都会 `clear()` 内部的 `chunks`/`stateCache` 缓存，且 `Temperature.apply` 每次都会基于 dummy 当前位置重算全套修饰器。因此现 API 天然不跨 pos 复用。

真正有效的批量化必须**跨 pos 共享昂贵计算**：

| 共享项 | 现状 | 批量后 | 收益量级 |
| --- | --- | --- | --- |
| dummy 玩家及其 WORLD 修饰器链 | 每 pos 重新组装 | 每批组装一次 | 小（常数） |
| `getInsulationAt` 的 25-chunk 方块实体扫描 | 每 pos 扫 25 chunk | 按区块去重，一区块只扫一次 | 中（聚集时显著） |
| `BlockTempModifier` 的 `(2·range)³` 方块体积扫描 | 每 pos 扫 2744 格 | 区域并集一次扫描、按 pos 分派 | **大（方案选择的关键）** |
| `BiomeTempModifier` 的 49 采样 | 每 pos 采样 49 | 按唯一 biome/结构去重 | 中 |

**结论**：在“聚集仓储/冰块箱”的典型 FIAHI 使用场景下，批量查询可以把主项（方块体积扫描）从“每容器一次”降到“区域一次 + 每容器按距离/遮挡衰减分派”，使总成本接近 O(n)。因此在给定场景下**增益明显**，值得实现；但必须采用“区域并集一次扫描、按位分派”的实现路径，否则形同虚设。

### 3.3 两点前提风险（必须接受后再动手）

1. **依赖发布节奏（阻塞项）**：FIAHI 通过 CurseMaven 依赖 Cold-Sweat 的**已发布 jar**（`build.gradle:72`，`curse.maven:cold_sweat-506194:${cold_sweat_file_number}`）。新增的 `WorldHelper` 批量 API 必须先随 Cold-Sweat 发布一个新版本，FIAHI 再升依赖才能编译通过。建议先合并并发布 Cold-Sweat 侧 API，再接 FIAHI 侧改动。
2. **主线程开销**：温度计算在主线程 tick 内进行，批量算法不应阻塞过久；分批 / 上限需评估（见下文 §5）。

## 4. 目标 API 设计（Cold-Sweat 侧，`WorldHelper`）

按主人要求，同时提供“全精度”与“粗精度（复用分段缓存）”两套批 API，附属可按需求选用。

> 说明：以下签名均基于现有 1.21.1 / NeoForge 的 `WorldHelper`。实际命名以最终实现为准。

### 4.1 全精度批 API（对应现有 `getTemperatureAt` 语义）

```java
/**
 * 对同一 Level 中的一批 BlockPos 批量计算世界温度（全精度，语义对齐 getTemperatureAt），喵~
 * <br/>
 * 通过跨 pos 复用：dummy 修饰器链、区域并集的方块温度扫描、按区块去重的炉心绝缘查询，分摊重复开销喵~
 *
 * @param level     温度所属世界（须为同一 Level，服务端主线程调用喵~）
 * @param positions 需要查询温度的一批方块位置（可含重复，结果去重喵~）
 * @return 每个位置对应的世界温度（MC 单位），数量与去重后的 positions 一致喵~
 * @throws IllegalStateException 若在逻辑客户端或非主线程调用喵~
 */
public static Object2DoubleMap<BlockPos> getTemperaturesAt(Level level, Collection<BlockPos> positions)
```

### 4.2 粗精度批 API（对应现有 `getRoughTemperatureAt` 语义）

```java
/**
 * 对同一 Level 中的一批 BlockPos 批量计算“粗略”世界温度，语义对齐 getRoughTemperatureAt，
 * 复用 8 格分段温度缓存（Rough 缓存），喵~
 *
 * @param level     温度所属世界喵~
 * @param positions 需要查询温度的一批方块位置喵~
 * @param flags     与 getRoughTemperatureAt 相同的缓存标记位（1=Sensitive，2=Force Update）喵~
 * @return 每个位置对应的粗略世界温度（MC 单位）喵~
 */
public static Object2DoubleMap<BlockPos> getRoughTemperaturesAt(Level level, Collection<BlockPos> positions, int flags)
```

### 4.3 私有共享基础设施

```java
// 内部：跨 pos 共享一次“区域并集”的方块温度扫描，产出每个目标 pos 的贡献喵~
private static Object2DoubleMap<BlockPos> scanBlockTemperatures(
    DummyPlayer dummy, Collection<BlockPos> positions, int range)

// 内部：一区块方块实体只扫描一次，为整批 pos 计算炉心绝缘喵~
private static Object2DoubleMap<BlockPos> getInsulationAtBatch(Level level, Collection<BlockPos> positions, int chunkRadius)
```

- 返回类型采用 `it.unimi.dsi.fastutil.objects.Object2DoubleMap<BlockPos>`（fastutil，对齐项目既有用法，见 `ItemStackHelper`/`EnchantmentRequirement` 对 `Object2IntMap` 的使用）。
- 线程语义：仅在服务端主线程调用，Javadoc 注明，避免并发写共享缓存。

## 5. 实现要点（Cold-Sweat 侧）

### 5.1 `scanBlockTemperatures`：区域并集一次扫描、按位分派

`BlockTempModifier.calculate()` 的核心是把 `(2·range)³` 内每个方块对 pos 的贡献按 **距离（`.fade()`）与遮挡（`forBlocksInRay`）** 累加。批量版本的重构思路：

1. 收集整批 pos 的 **chunk 并集**，一次遍历并加载到 `chunks`（沿用 `BlockTempModifier` 的 LRU `chunks`、`stateCache` 设计，但 **不在每次计算后 clear**，而是按批次管理）。
2. 对每个候选方块，用其 `range`/`fade`/`logarithmic` 规则，只对 **落在其影响半径内的目标 pos** 累加贡献；距离与遮挡按每个目标 pos 单独计算。遮挡计算沿用 `forBlocksInRay`，其内部已使用按 pos 的 `stateCache`，天然可被邻近 pos 复用。
3. 输出以自己的 `BlockTemp` 为维度合并后的 `Object2DoubleMap<BlockPos>`（每个目标 pos 一个总值），再由洗牌阶段按 `BlockTemp` 的 `min/maxTemperature` 与 `min/maxEffect` 夹取。

> 说明：该重构需要把 `BlockTempModifier.calculate()` 的“单 pos”循环拆成“一批 pos”的数据结构
> （`blockTempTotals` 由 `Map<BlockTemp,Double>` 变为 `Map<BlockPos, Map<BlockTemp,Double>>` 或等价物）。
> 这是本方案中**复杂度最高、风险最大**的部分，建议通过新增一次性批计算上下文类
> `BlockTempScanBatch` 承载（详见 §5.2），避免改动 `BlockTempModifier` 的既有单例语义。
> 提供单独的 1.0.0 版本，先不改变原 `getTemperatureAt` 行为。

### 5.2 `BlockTempScanBatch`：一次性批计算上下文

**定位**：`BlockTempScanBatch` 不是长期缓存，也不是挂在 dummy 上的单例，而是**每次调用批 API 时现场 new 出来、算完即弃的临时上下文**（scratch 对象）。它的生命周期严格限定在一次 `getTemperaturesAt` 调用内，因此：

- 不需要像 `TEMPERATURE_CHECKS` / `DUMMY_PLAYERS` 那样在 `clearCachesOnUnload` 里清理，也就**不引入内存泄漏面**；
- 不存在跨维度 / 跨 LevelTick 交错的并发污染问题（服务端主线程单次调用语义下天然安全）；
- 与挂载在 dummy 上的 `BlockTempModifier` 单例完全解耦，原 `getTemperatureAt` 行为不受影响。

**创建与销毁**：

```java
// 示意：在 getTemperaturesAt 内部
BlockTempScanBatch batch = new BlockTempScanBatch(level, positions, range);
batch.scan();                       // 区域并集一次扫描 + 按位分派
Object2DoubleMap<BlockPos> result = batch.buildResult();   // 夹取后输出
// batch 离开作用域即被 GC，无需显式释放
```

**字段设计**：

| 字段 | 类型 | 用途 | 生命周期 |
| --- | --- | --- | --- |
| `level` | `Level` | 本批所属世界 | 构造传入 |
| `positions` | `Collection<BlockPos>` | 目标 pos 清单（构造时去重） | 本批 |
| `range` | `int` | 扫描半径（= `BLOCK_RANGE`，由调用方传入） | 本批 |
| `chunkCache` | `Map<Long, ChunkAccess>`（LRU，沿用既有 `BlockTempModifier` 设计） | 本批需要加载的 chunk 并集 | 本批 |
| `stateCache` | `Long2ObjectOpenHashMap<BlockState>` | 本批共享的 blockstate 缓存 | 本批 |
| `totals` | `Map<BlockPos, Map<BlockTemp, Double>>` | 每个目标 pos 各自的 `BlockTemp` 累加值（由单 pos 版 `Map<BlockTemp,Double>` 扩展而来） | 本批 |
| `groupTotals` | `Map<BlockPos, Map<TagKey<BlockTempData>, Double>>` | 每个目标 pos 各自的分组累计，用于组内 clamp | 本批 |
| `srcBlocks` | `Long2ObjectOpenHashMap<Entry>`（临时） | 第一遍扫描到“非空气且有 blockTemp”的候选源方块及其 blockTemp 集合 | 仅 scan() 期间 |

**两阶段算法**：

1. **阶段 A：区域并集收集（逐源方块扫描，读取 blockstate 一次）**
   - 遍历整批 pos 覆盖的 **chunk 并集**，逐 block 读取 `blockstate`（走 `stateCache`，同 chunk 邻近 pos 共享）。
   - 跳过空气 / `BlockTempRegistry.getBlockTempsFor(state)` 为空 / 仅含 `DEFAULT_BLOCK_TEMP` 的方块（与单 pos 版一致）。
   - 把命中的源方块连同其 `Collection<BlockTemp>` 写入 `srcBlocks`。
   - 此阶段**只读 blockstate，不做任何距离/遮挡计算**，是纯并集扫描，成本 ≈ 并集体积（而非 N × 并集体积）。

2. **阶段 B：按源方块分派（只对影响半径内 pos 计算距离与遮挡）**
   - 对 `srcBlocks` 中每个源方块 `src`，遍历其每个 `BlockTemp`：
     - 先做 `areAnyBlockTempsInRange` 等价预筛（针对当前 src 的 blockTemp，对整批 pos 的当前累计值判断是否需要继续）。
     - 对 `positions` 中 `dist(src, pos) < blockTemp.range()` 的目标 pos：
       - `isValid(level, src, state)` 判定；
       - 计算 `distance`（复用 `WorldHelper.getClosestPointOnEntity(dummy, src)` 到 pos 的距离语义）；
       - 遮挡计数：`forBlocksInRay(pos, src, level, chunk, stateCache, ...)` —— 沿用单 pos 版的 ray，但这里的“观察者”是该目标 pos，因此**每个 (src, pos) 组合各算一次**（这是不可避免的二次项，但仅对落在影响半径内的组合发生，且 ray 内部共享 `stateCache`，成本远低于“每 pos 全体积扫描”）；
       - 按 `fade()`/`logarithmic()` 规则累加进对应 pos 的 `totals[src→pos]` 与 `groupTotals[src→pos]`。

**关键差异对照（与单 pos `BlockTempModifier.calculate()`）**：

| 项目 | 单 pos 版 | 批量版（本类） |
| --- | --- | --- |
| 遍历维度 | 每 pos `(2·range)³` 全扫 | 区域并集扫一次 + 按影响半径分派 |
| `blockstate` 读取 | 每 pos 每格读一次 | 每格一次（`stateCache` 共享） |
| 遮挡 ray | 每个 (pos, 源方块) 组合 | 同左，但仅限影响半径内组合 |
| `entity` 参数传给 `BlockTemp.getTemperature` | 传 dummy player | 传同一 dummy player（保持 `getTemperatureAt` 语义） |
| 累加容器 | `Map<BlockTemp,Double>` | `Map<BlockPos, Map<BlockTemp,Double>>` |

**夹取（buildResult）**：遍历每个目标 pos 的 `totals[BlockTemp→Double]`（等价于单 pos 版 `calculate()` 末尾闭包里的逻辑），按 `minTemperature`/`maxTemperature` 过一遍并 `clamp(temp + effectValue, min, max)`，得到该 pos 的最终世界温度，写入返回的 `Object2DoubleMap<BlockPos>`。

**边界与回归**：`BlockTempScanBatch` 只服务于新增的 `getTemperaturesAt`/`scanBlockTemperatures` 内部；既有 `getTemperatureAt` 与 `BlockTempModifier.calculate()` 保持原样。由于两者并行存在，推荐提供“批量 vs 逐个”的对照测试：对同一批 pos 分别用两种路径计算并断言阈值语义一致（见 §7 验收）。

### 5.3 `getInsulationAtBatch`：按区块去重

把现有 `getInsulationAt`（`WorldHelper.java:960`，每 pos 扫 25 chunk）改为：

1. 收集整批 pos 的 chunk 并集；
2. 每个 chunk 只 `getChunk` 一次，`getBlockEntitiesPos()` 遍历一次，把 `HearthBlockEntity` 的 `pathLookup`（包含哪些 pos）索引进一个 `Map<BlockPos, IntegerPair>`；
3. 最后一次性为每个目标 pos 汇总冷却/加热等级。

### 5.4 粗精度批 API 的缓存复用

`getRoughTemperatureAt`（`WorldHelper.java:740`）已有 `TEMPERATURE_CHECKS` 分段缓存（8 格粒度）。
批方法对每个 pos 走同一缓存逻辑（segment → snapshot 命中直接返回，避免一次 `Temperature.apply`），因此只需按 batch 循环走缓存路径即可，无需额外共享。

### 5.5 线程与缓存清理

- 沿用 `clearCachesOnUnload`（`WorldHelper.java:82`）在 `ServerStoppedEvent` 清理新增缓存。
- `BlockTempScanBatch` 这类一次性上下文无需静态清理（本批内创建与销毁）；若复用对象池，须确保每批耗尽时所有权已归还，避免泄漏。
- 新增的静态批量中间结果若引入，须在事件处理或 tick 结束前显式丢弃，避免内存泄漏。

## 6. 实现要点（FIAHI 侧，`ForgeEventHandler`）

### 6.1 改动一：维度节流对齐

把 `tickAfterCheck` 从 `int` 改为按维度维度的计数器：

```java
private static final Object2IntMap<ResourceKey<Level>> tickAfterCheck = new Object2IntOpenHashMap<>();
```

`onLevelTick` 内改为对**每个维度**统一节流：

```java
int counter = tickAfterCheck.addTo(dimension, 1);   // 伪码：见下方精确写法
if(counter < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
    return;
}
tickAfterCheck.put(dimension, 0);
```

注意 fastutil 的 `Object2IntOpenHashMap` 在 `addTo(key,1)` 时会把不存在的 key 视为 0；为语义清晰，建议显式 `getOrDefault` + `put` 或首次放入 0。

同步修正 `isAvailableToTickFood()`（`ForgeEventHandler.java:122`）：其当前依赖 `tickAfterCheck == 0`，改为按维度判断（或明确该判断属于哪个维度）。

### 6.2 改动二：改用批量 API

重构 `onLevelTick`：

1. 先完成逐 chunk → 逐容器**收集目标 `BlockPos` 清单**（仍按完整体积收集，但不在此刻计算温度）。
2. 收集完毕后，一次性调用 `Object2DoubleMap<BlockPos> temps = WorldHelper.getTemperaturesAt(serverLevel, containerPositions);` （或按精度需求选 `getRoughTemperaturesAt`）。
3. 用 `temps.getDouble(blockPos)` 回填每个容器的温度，再逐食物 `IFrozenRottenFood.tick(...)`。

要点：

- **一个 LevelTick 内只调用一次批量 API**，而不是在 `forEach` 里调用。
- `tickContainer` 改为接收“预计算好的温度”：新增重载或把温度作为参数传入，避免回调内重复取。
- 数据竞争：整批 API 一次性返回同 level 的结果，无需加锁（服务端主线程语义）。

```java
// 示意：仅在节流命中、确实需要温度的那一 tick 内执行批查询
if(shouldCheckTemperature(dimension)) {
    Object2DoubleMap<BlockPos> temperatures = WorldHelper.getTemperaturesAt(serverLevel, containerPositions);
    // 逐容器 tick 食物，用 temperatures.getDouble(blockPos) 作为该容器温度
}
```

### 6.3 依赖升级

FIAHI 侧提交前必须把 `gradle.properties` 的 `cold_sweat_file_number` 更新到包含新批 API 的 Cold-Sweat 版本，并在 changelog 中注明。

## 7. 交付节奏与验收

### 阶段一：Cold-Sweat 侧新增批 API（不改变既有行为）

- 在 `WorldHelper` 增加 `getTemperaturesAt` / `getRoughTemperaturesAt` 及私有共享实现。
- 现有 `getTemperatureAt` / `getRoughTemperatureAt` 行为保持不变（回归测试重点）。
- 发布 Cold-Sweat 新版本（阻塞 FIAHI 侧）。

### 阶段二：FIAHI 侧节流对齐 + 批量调用

- 维度节流类型改为 `Object2IntMap<ResourceKey<Level>>`。
- 升级 `cold_sweat_file_number` 依赖，接入批 API。
- 每 LevelTick 只调用一次批 API。

### 验收指标

- **正确性**：批量结果与旧逐个调用的结果在阈值语义上等价（FIAHI 仅依赖 `temp` 的正负与增量，验证冷/热方向与等级变化一致）。
- **性能**：用 profiler（如 `Spark`）对比改动前后 `onLevelTick` 在“主世界 + 下界 + 末地 + 大型冰块箱阵列”场景下的累计耗时；预期下界/末地下降 ~99%（节流），聚集仓储场景批量项下降一整个量级。
- **回归**：确认 `isAvailableToTickFood`、维度登录、容器内食物冷冻/腐烂速率均不回归。

## 8. 风险与后续

- **风险 1（发布时序）**：Cold-Sweat 新 API 必须先行发布，FIAHI 才能编译，协调发布节奏。
- **风险 2（区域并集扫描的正确性）**：把单 pos 的 `BlockTemp` 重构成批 pos 是行为敏感改动，需保证 `fade()`/遮挡/`logarithmic`/group clamp 语义逐一等价，建议提供批量与单个的对照测试。
- **风险 3（主线程耗时）**：批扫描仍可能较高；若超大世界全量容器一次性处理过重，可加“每 tick 处理一批的预算上限（batch budget）”，配合维度节流一起控制。
- **后续可选**：把批扫描的重计算移入后台线程，主线程仅应用结果（当前阶段不做，避免并发复杂度）。