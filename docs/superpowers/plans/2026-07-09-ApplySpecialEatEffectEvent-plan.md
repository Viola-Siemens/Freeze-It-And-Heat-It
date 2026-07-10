# ApplySpecialEatEffectEvent 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `LivingEntityMixin#fiahi$addSpecialEatEffect` 方法添加事件系统，允许附属模组取消原有效果并注册自定义回调喵~

**Architecture:** 创建单一事件类 `ApplySpecialEatEffectEvent`，继承 `Event` 和 `ICancellableEvent`，在 mixin 方法中发布事件，根据取消状态决定是否应用原有效果和执行回调喵~

**Tech Stack:** NeoForge 事件总线、Java 17、Mixin 框架

## Global Constraints

- **命名约定**: 类名大驼峰，方法名小驼峰，常量全大写加下划线喵~
- **代码风格**: Tab 缩进，K&R 大括号风格，每行不超过 150 字符，方法体不超过 300 行喵~
- **Javadoc**: 所有 public 类和方法必须有 Javadoc 注释喵~
- **提交格式**: `type(scope): subject` 格式喵~
- **依赖**: NeoForge 1.21.1，Java 17 LTS 喵~

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

### Task 1: 创建事件类 ApplySpecialEatEffectEvent

**Files:**
- Create: `src/main/java/com/hexagram2021/fiahi/common/event/ApplySpecialEatEffectEvent.java`

**Interfaces:**
- Consumes: `LivingEntity`, `ItemStack`, `IFrozenRottenFood`
- Produces: `ApplySpecialEatEffectEvent` 类，`EffectCallback` 接口

- [ ] **Step 1: 创建事件类基础结构**

```java
package com.hexagram2021.fiahi.common.event;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 事件：在应用冷冻或腐败食物的特殊效果时触发喵~
 *
 * 附属模组可以通过此事件：
 * 1. 取消 FIAHI 的原有效果应用
 * 2. 注册自定义效果回调，在原有效果应用后执行喵~
 *
 * @author liudongyu
 */
public class ApplySpecialEatEffectEvent extends Event implements ICancellableEvent {
	private final LivingEntity entity;
	private final ItemStack itemStack;
	private final IFrozenRottenFood foodData;
	private boolean cancelled = false;
	private final List<EffectCallback> callbacks = new ArrayList<>();

	/**
	 * 创建事件喵~
	 *
	 * @param entity 正在吃东西的实体喵~
	 * @param itemStack 正在吃的物品堆喵~
	 * @param foodData 物品的冷冻腐败数据喵~
	 */
	public ApplySpecialEatEffectEvent(LivingEntity entity, ItemStack itemStack, IFrozenRottenFood foodData) {
		this.entity = entity;
		this.itemStack = itemStack;
		this.foodData = foodData;
	}

	/**
	 * 获取正在吃东西的实体喵~
	 *
	 * @return 正在吃东西的实体喵~
	 */
	public LivingEntity getEntity() {
		return entity;
	}

	/**
	 * 获取正在吃的物品堆喵~
	 *
	 * @return 正在吃的物品堆喵~
	 */
	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * 获取物品的冷冻腐败数据喵~
	 *
	 * @return 物品的冷冻腐败数据喵~
	 */
	public IFrozenRottenFood getFoodData() {
		return foodData;
	}

	/**
	 * 注册自定义效果回调喵~
	 *
	 * 回调将在 FIAHI 的原有效果应用之后执行喵~
	 *
	 * @param callback 回调函数，接收实体和冷冻腐败数据喵~
	 */
	public void addEffectCallback(EffectCallback callback) {
		callbacks.add(callback);
	}

	/**
	 * 执行所有已注册的回调喵~
	 */
	public void executeCallbacks() {
		for(EffectCallback callback : callbacks) {
			callback.apply(entity, foodData);
		}
	}

	/**
	 * 检查事件是否已取消喵~
	 *
	 * @return true 表示事件已取消喵~
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * 取消事件喵~
	 *
	 * 取消后，FIAHI 的原有效果和所有回调都不会执行喵~
	 *
	 * @param cancelled 是否取消喵~
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * 效果回调函数式接口喵~
	 *
	 * 附属模组实现此接口来添加自定义效果喵~
	 *
	 * @author liudongyu
	 */
	@FunctionalInterface
	public interface EffectCallback {
		/**
		 * 应用自定义效果喵~
		 *
		 * @param entity 正在吃东西的实体喵~
		 * @param foodData 物品的冷冻腐败数据喵~
		 */
		void apply(LivingEntity entity, IFrozenRottenFood foodData);
	}
}
```

- [ ] **Step 2: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交事件类**

```bash
git add src/main/java/com/hexagram2021/fiahi/common/event/ApplySpecialEatEffectEvent.java
git commit -m "feat: 添加 ApplySpecialEatEffectEvent 事件类，支持附属模组取消效果和注册回调"
```

---

### Task 2: 创建 package-info.java

**Files:**
- Create: `src/main/java/com/hexagram2021/fiahi/common/event/package-info.java`

**Interfaces:**
- Produces: 包声明文件

- [ ] **Step 1: 创建 package-info.java**

```java
/**
 * FIAHI 事件包喵~
 *
 * 包含 FIAHI 暴露给附属模组的事件类喵~
 *
 * @author liudongyu
 */
@NonNullByDefault
package com.hexagram2021.fiahi.common.event;

import org.jetbrains.annotations.Nonnull;
import org.jetbrains.annotations.NonnullByDefault;
```

- [ ] **Step 2: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译成功，无错误

- [ ] **Step 3: 提交 package-info**

```bash
git add src/main/java/com/hexagram2021/fiahi/common/event/package-info.java
git commit -m "docs: 添加 event 包的 package-info.java"
```

---

### Task 3: 修改 LivingEntityMixin 方法

**Files:**
- Modify: `src/main/java/com/hexagram2021/fiahi/mixin/LivingEntityMixin.java`

**Interfaces:**
- Consumes: `ApplySpecialEatEffectEvent`
- Produces: 更新后的 mixin 方法

- [ ] **Step 1: 添加事件发布导入**

```java
import com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent;
import net.neoforged.neoforge.common.NeoForge;
```

在 `import` 区域添加以上两行导入喵~

- [ ] **Step 2: 修改 fiahi$addSpecialEatEffect 方法**

完整方法如下喵~

```java
@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEatEffect(Lnet/minecraft/world/food/FoodProperties;)V", shift = At.Shift.BEFORE))
private void fiahi$addSpecialEatEffect(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
	LivingEntity entity = (LivingEntity)(Object)this;
	IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
	if(c != null) {
		// 发布事件喵~
		ApplySpecialEatEffectEvent event = new ApplySpecialEatEffectEvent(entity, itemStack, c);
		NeoForge.EVENT_BUS.post(event);

		// 如果事件未取消，应用原有效果喵~
		if(!event.isCancelled()) {
			if(c.getFrozenLevel() > 0) {
				entity.addEffect(new MobEffectInstance(FIAHIMobEffects.SHIVER, c.getFrozenLevel() * 200, c.getFrozenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, c.getFrozenLevel() * 400, c.getFrozenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, c.getFrozenLevel() * 400, c.getFrozenLevel() - 1));
				Temperature.add(entity, Temperature.Trait.CORE, -c.getFrozenLevel() * 5);
			}
			if(c.getRottenLevel() > 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, c.getRottenLevel() * 200, c.getRottenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, c.getRottenLevel() * 200, c.getRottenLevel() - 1));
				Temperature.add(entity, Temperature.Trait.CORE, c.getRottenLevel() * 5);
			}

			// 执行附属模组的回调喵~
			event.executeCallbacks();
		}
	}
}
```

- [ ] **Step 3: 编译验证**

Run: `./gradlew compileJava`
Expected: 编译成功，无错误

- [ ] **Step 4: 提交 mixin 修改**

```bash
git add src/main/java/com/hexagram2021/fiahi/mixin/LivingEntityMixin.java
git commit -m "feat: 修改 LivingEntityMixin 发布 ApplySpecialEatEffectEvent，支持事件取消和回调执行"
```

---

### Task 4: 编写测试（可选）

**Files:**
- Create: `src/test/java/com/hexagram2021/fiahi/event/ApplySpecialEatEffectEventTest.java`

**Interfaces:**
- Consumes: `ApplySpecialEatEffectEvent`

- [ ] **Step 1: 创建测试类**

```java
package com.hexagram2021.fiahi.event;

import com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ApplySpecialEatEffectEvent 测试喵~
 *
 * @author liudongyu
 */
class ApplySpecialEatEffectEventTest {

	@Test
	@DisplayName("测试事件取消功能")
	void testEventCancellation() {
		LivingEntity entity = mock(LivingEntity.class);
		ItemStack itemStack = mock(ItemStack.class);
		IFrozenRottenFood foodData = mock(IFrozenRottenFood.class);

		ApplySpecialEatEffectEvent event = new ApplySpecialEatEffectEvent(entity, itemStack, foodData);

		assertFalse(event.isCancelled(), "初始状态不应取消喵~");

		event.setCancelled(true);
		assertTrue(event.isCancelled(), "取消后应返回 true 喵~");
	}

	@Test
	@DisplayName("测试回调注册和执行")
	void testCallbackRegistrationAndExecution() {
		LivingEntity entity = mock(LivingEntity.class);
		ItemStack itemStack = mock(ItemStack.class);
		IFrozenRottenFood foodData = mock(IFrozenRottenFood.class);

		ApplySpecialEatEffectEvent event = new ApplySpecialEatEffectEvent(entity, itemStack, foodData);

		// 注册回调喵~
		event.addEffectCallback((e, f) -> {
			e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
		});

		event.addEffectCallback((e, f) -> {
			e.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
		});

		// 执行回调喵~
		event.executeCallbacks();

		// 验证实体收到了效果喵~
		verify(entity, times(2)).addEffect(any(MobEffectInstance.class));
	}

	@Test
	@DisplayName("测试事件被取消时不执行回调")
	void testCallbacksNotExecutedWhenCancelled() {
		LivingEntity entity = mock(LivingEntity.class);
		ItemStack itemStack = mock(ItemStack.class);
		IFrozenRottenFood foodData = mock(IFrozenRottenFood.class);

		ApplySpecialEatEffectEvent event = new ApplySpecialEatEffectEvent(entity, itemStack, foodData);

		// 注册回调喵~
		event.addEffectCallback((e, f) -> {
			e.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
		});

		// 取消事件喵~
		event.setCancelled(true);

		// 执行回调喵~
		event.executeCallbacks();

		// 验证实体没有收到效果喵~
		verify(entity, never()).addEffect(any(MobEffectInstance.class));
	}

	@Test
	@DisplayName("测试获取上下文信息")
	void testGetContextInfo() {
		LivingEntity entity = mock(LivingEntity.class);
		ItemStack itemStack = mock(ItemStack.class);
		IFrozenRottenFood foodData = mock(IFrozenRottenFood.class);

		ApplySpecialEatEffectEvent event = new ApplySpecialEatEffectEvent(entity, itemStack, foodData);

		assertSame(entity, event.getEntity(), "应返回正确的实体喵~");
		assertSame(itemStack, event.getItemStack(), "应返回正确的物品堆喵~");
		assertSame(foodData, event.getFoodData(), "应返回正确的冷冻腐败数据喵~");
	}
}
```

- [ ] **Step 2: 运行测试**

Run: `./gradlew test`
Expected: 测试通过

- [ ] **Step 3: 提交测试**

```bash
git add src/test/java/com/hexagram2021/fiahi/event/ApplySpecialEatEffectEventTest.java
git commit -m "test: 添加 ApplySpecialEatEffectEvent 单元测试"
```

---

### Task 5: 提交设计文档

**Files:**
- Modify: git repository

- [ ] **Step 1: 添加设计文档和接入文档**

```bash
git add docs/superpowers/specs/2026-07-09-ApplySpecialEatEffectEvent-design.md
git add docs/superpowers/specs/2026-07-09-ApplySpecialEatEffectEvent-integration.md
git add docs/superpowers/plans/2026-07-09-ApplySpecialEatEffectEvent-plan.md
git commit -m "docs: 添加 ApplySpecialEatEffectEvent 设计规范、接入文档和实现计划"
```

---

## 计划完成检查

- [x] 事件类 `ApplySpecialEatEffectEvent` 创建
- [x] `package-info.java` 创建
- [x] `LivingEntityMixin` 方法修改
- [x] 单元测试编写
- [x] 设计文档和接入文档
- [x] 所有变更提交

---

## 附录：验证清单

实现完成后，请验证喵~

1. **编译检查**: `./gradlew compileJava` 应无错误
2. **测试检查**: `./gradlew test` 应全部通过
3. **功能验证**: 在游戏中食用冷冻/腐败食物，事件应正确触发
4. **取消验证**: 附属模组应能成功取消原有效果
5. **回调验证**: 附属模组注册的回调应正确执行