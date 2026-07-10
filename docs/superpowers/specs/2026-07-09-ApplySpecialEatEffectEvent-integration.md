# Freeze-It-And-Heat-It 附属模组接入文档

**版本**: 4.1.1

**更新日期**: 2026-07-09

---

## 概述

`ApplySpecialEatEffectEvent` 事件允许附属模组在实体食用冷冻或腐败的食物时介入，支持取消 FIAHI 原有效果和注册自定义效果回调。

---

## 依赖配置

### Gradle 依赖

在你的 `build.gradle` 中添加对 FIAHI 的依赖。

```gradle
dependencies {
    // NeoForge
    implementation "net.neoforged:neoforge:${neo_version}"

    // FIAHI（运行时依赖）
    runtimeOnly "com.hexagram2021:fiahi:${fiahi_version}"
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    // NeoForge
    implementation("net.neoforged:neoforge:${neo_version}")

    // FIAHI（运行时依赖）
    runtimeOnly("com.hexagram2021:fiahi:${fiahi_version}")
}
```

### neoforge.mods.toml 依赖

在 `src/main/resources/META-INF/neoforge.mods.toml` 中添加依赖声明。

```toml
[[dependencies.example_mod]]
    modId="fiahi"
    mandatory=false
    versionRange="[4.1.1,)"
    ordering="NONE"
    side="BOTH"
```

---

## 事件监听

### 基本用法

使用 `@EventBusSubscriber` 注解创建事件处理器。

```java
package com.example.mod.event;

import com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        // 你的逻辑
    }
}
```

---

## API 参考

### ApplySpecialEatEffectEvent

事件类位于 `com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent`。

#### 获取上下文信息

```java
// 获取正在吃东西的实体
LivingEntity entity = event.getEntity();

// 获取正在吃的物品堆
ItemStack itemStack = event.getItemStack();

// 获取物品的冷冻腐败数据
IFrozenRottenFood foodData = event.getFoodData();

// 获取冷冻等级（0 表示未冷冻）
int frozenLevel = foodData.getFrozenLevel();

// 获取腐败等级（0 表示未腐败）
int rottenLevel = foodData.getRottenLevel();
```

#### 取消原有效果

```java
// 取消 FIAHI 的原有效果和所有回调
if(shouldCancel) {
    event.setCancelled(true);
}
```

#### 注册自定义回调

```java
// 注册一个回调，在 FIAHI 原有效果应用后执行
event.addEffectCallback((entity, foodData) -> {
    // 在这里添加你的自定义效果
    if(foodData.getFrozenLevel() > 0) {
        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
    }
});
```

### EffectCallback 接口

回调函数式接口，接收 `LivingEntity` 和 `IFrozenRottenFood` 两个参数。

```java
@FunctionalInterface
public interface EffectCallback {
    void apply(LivingEntity entity, IFrozenRottenFood foodData);
}
```

---

## 使用示例

### 示例 1：阻止特定物品的原有效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        // 如果是金苹果，取消 FIAHI 效果
        if(event.getItemStack().is(Items.GOLDEN_APPLE)) {
            event.setCancelled(true);
        }
    }
}
```

### 示例 2：添加额外的冻结效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        event.addEffectCallback((entity, foodData) -> {
            if(foodData.getFrozenLevel() >= 3) {
                // 高度冷冻的食物添加凋零效果
                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 1));
            }
        });
    }
}
```

### 示例 3：为特定实体添加特殊效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        event.addEffectCallback((entity, foodData) -> {
            // 如果是玩家，发送提示消息
            if(entity instanceof Player player) {
                if(foodData.getRottenLevel() > 0) {
                    player.sendSystemMessage(Component.literal("你吃了腐败的食物。"));
                }
            }
        });
    }
}
```

### 示例 4：条件性取消和添加效果

```java
@EventBusSubscriber
public class MyModEventHandler {
    @SubscribeEvent
    public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
        // 如果实体带有自定义标签，取消原有效果并添加自定义效果
        if(event.getEntity().hasData(AttachmentTypes.MY_TAG)) {
            event.setCancelled(true);
            event.addEffectCallback((entity, foodData) -> {
                if(foodData.getFrozenLevel() > 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 0));
                }
                if(foodData.getRottenLevel() > 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                }
            });
        }
    }
}
```

---

## 执行顺序

事件的完整执行流程如下：

1. **发布事件** → FIAHI 发布 `ApplySpecialEatEffectEvent`
2. **附属模组处理** → 所有监听此事件的附属模组处理逻辑
3. **检查取消** → 如果事件被取消，跳过后续步骤
4. **应用原有效果** → FIAHI 应用冷冻/腐败效果
5. **执行回调** → 执行所有已注册的回调函数

**重要提示**：回调函数只在事件**未取消**时才会执行。

---

## 注意事项

1. **只在服务端处理**：事件可能在客户端和服务端都会触发，如果需要区分，请检查 `entity.level().isClientSide`。

   ```java
   @SubscribeEvent
   public static void onApplySpecialEatEffect(ApplySpecialEatEffectEvent event) {
       if(!event.getEntity().level().isClientSide) {
           // 只在服务端执行逻辑
       }
   }
   ```

2. **避免空指针**：`foodData` 始终非空（事件仅在存在 `IFrozenRottenFood` capability 时发布），但 `entity` 和 `itemStack` 可能为空，建议做防御性检查。

3. **性能考虑**：事件会在每次食用冷冻/腐败食物时触发，避免在回调中执行耗时的操作。

4. **回调注册时机**：回调必须在事件发布期间（即事件处理器执行时）注册，延迟注册不会生效。

---

## 常见问题

### Q: 为什么我的回调没有执行？

A: 检查以下原因：
- 事件是否被取消？（取消后回调不会执行）
- 是否在客户端执行？（某些效果需要服务端执行）
- 是否正确注册了回调？

### Q: 如何让多个附属模组协同工作？

A: 每个附属模组独立监听事件，通过 `addEffectCallback` 注册自己的回调。所有回调会按注册顺序依次执行。

### Q: 可以在回调中修改物品堆吗？

A: 不推荐在回调中修改物品堆，因为物品已经处于消费过程中。如需修改，请在事件处理器中直接操作。

### Q: 如何获取当前的温度信息？

A: `IFrozenRottenFood` 提供了 `getTemperature()` 方法，可以获取物品的当前温度。

---

## 版本兼容性

| FIAHI 版本 | 事件名称                       | 变更说明 |
|----------|----------------------------|------|
| 4.1.1    | ApplySpecialEatEffectEvent | 初始版本 |
