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

