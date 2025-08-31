package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEatEffect(Lnet/minecraft/world/food/FoodProperties;)V", shift = At.Shift.BEFORE))
	private void fiahi$addSpecialEatEffect(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
		LivingEntity entity = (LivingEntity)(Object)this;
		IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
		if(c != null) {
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
		}
	}
}
