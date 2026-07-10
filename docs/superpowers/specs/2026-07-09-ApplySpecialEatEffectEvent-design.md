# ApplySpecialEatEffectEvent 设计规范

**日期**: 2026-07-09

**作者**: 刘冬煜

**状态**: 已批准

---

## 概述

为 `LivingEntityMixin#fiahi$addSpecialEatEffect` 方法添加一个事件系统，让附属模组能够在实体食用冷冻或腐败的食物时介入，支持取消原有效果应用和注册自定义效果回调喵~

---

## 背景

`fiahi$addSpecialEatEffect` 方法在实体吃东西时被注入调用，检查物品是否有 `IFrozenRottenFood` capability，如果有冷冻等级或腐败等级，就应用相应的负面效果：

- **冷冻效果**：颤抖、移动缓慢、挖掘缓慢、降低核心温度
- **腐败效果**：反胃、饥饿、提高核心温度

当前实现是封闭的，附属模组无法：
1. 阻止 FIAHI 原有效果的添加
2. 添加自定义的负面效果或特殊逻辑

---

## 需求

### 功能需求

1. 附属模组应能取消 FIAHI 的原有效果应用
2. 附属模组应能注册自定义回调函数，在原有效果应用后执行
3. 回调函数接收 `LivingEntity` 和 `IFrozenRottenFood` 两个参数
4. 取消事件应同时阻止原有效果和回调函数的执行

### 非功能需求

1. 事件系统应符合 NeoForge 的标准事件模式
2. 代码应简单、易于理解和维护
3. 不引入不必要的全局状态或复杂依赖

---

## 设计

### 事件类：ApplySpecialEatEffectEvent

**位置**: `com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent`

**继承**:
- `net.neoforged.bus.api.Event`
- `net.neoforged.bus.api.ICancellableEvent`（支持取消）

**核心字段**:

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `entity` | `LivingEntity` | 正在吃东西的实体 |
| `itemStack` | `ItemStack` | 正在吃的物品堆 |
| `foodData` | `IFrozenRottenFood` | 物品的冷冻腐败数据 |
| `cancelled` | `boolean` | 事件是否已取消 |
| `callbacks` | `List<EffectCallback>` | 已注册的回调列表 |

**核心方法**:

- `getEntity()` - 获取实体
- `getItemStack()` - 获取物品堆
- `getFoodData()` - 获取冷冻腐败数据
- `addEffectCallback(EffectCallback)` - 注册自定义效果回调
- `executeCallbacks()` - 执行所有已注册的回调
- `isCancelled()` - 检查是否已取消
- `setCancelled(boolean)` - 设置取消状态

**回调接口**:

```java
@FunctionalInterface
public interface EffectCallback {
    void apply(LivingEntity entity, IFrozenRottenFood foodData);
}
```

### 执行流程

```
开始
  ↓
发布 ApplySpecialEatEffectEvent
  ↓
附属模组监听并处理事件
  ├─ 取消事件
  └─ 注册回调
  ↓
检查事件是否已取消？
  ├─ 是 → 结束（不执行任何操作）
  └─ 否 → 继续执行
        ↓
应用 FIAHI 原有效果
  ├─ 冷冻效果
  └─ 腐败效果
        ↓
执行所有已注册的回调
  ↓
结束
```

### Mixin 方法修改

在 `LivingEntityMixin#fiahi$addSpecialEatEffect` 中：

1. 在添加原有效果之前发布事件
2. 检查事件是否被取消
3. 如果未取消，应用原有效果
4. 执行所有回调函数

---

## 文件结构

```
src/main/java/com/hexagram2021/fiahi/
├── common/
│   ├── event/
│   │   ├── ApplySpecialEatEffectEvent.java    ← 新增
│   │   └── package-info.java                  ← 新增
│   └── mixin/
│       └── LivingEntityMixin.java              ← 修改
```

---

## 使用示例

### 附属模组取消原有效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        // 如果是特定物品，取消 FIAHI 效果
        if(event.getItemStack().is(Items.SOME_ITEM)) {
            event.setCancelled(true);
        }
    }
}
```

### 附属模组添加自定义效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        // 注册自定义回调
        event.addEffectCallback((entity, foodData) -> {
            if(foodData.getFrozenLevel() > 0) {
                // 添加额外的冻结效果
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
            }
        });
    }
}
```

---

## 设计决策

### 为什么选择单一事件类（方案 A）

1. **简单直观**：事件类承担所有职责，不引入额外的管理类
2. **符合标准**：符合 Minecraft 模组开发的事件模式
3. **无全局状态**：不引入全局状态，避免潜在的并发问题
4. **生命周期清晰**：事件对象本身就有生命周期，在其中存储回调是自然的

### 为什么在原有效果之前触发事件

允许附属模组通过取消事件来阻止 FIAHI 的原有效果应用，同时保留在原有效果之后执行回调的能力。

### 为什么取消事件也阻止回调执行

保持语义一致性：取消事件意味着"完全阻止此事件的全部副作用"，包括原有效果和所有回调。

---

## 未来扩展

如果未来需要更细粒度的控制，可以考虑：

1. 添加事件阶段（Pre/Post）
2. 提供独立的取消控制（`cancelEffect()` / `cancelCallbacks()`）
3. 支持回调优先级

当前设计已满足需求，暂不需要这些扩展喵~