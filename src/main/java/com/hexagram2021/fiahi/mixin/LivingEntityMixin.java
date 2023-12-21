package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "addEatEffect", at = @At(value = "HEAD"))
	private void fiahi$addSpecialEatEffect(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfo ci) {
		itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
			if(c.getFrozenLevel() > 0) {
				entity.addEffect(new MobEffectInstance(FIAHIMobEffects.SHIVER.get(), c.getFrozenLevel() * 200, c.getFrozenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, c.getFrozenLevel() * 400, c.getFrozenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, c.getFrozenLevel() * 400, c.getFrozenLevel() - 1));
				Temperature.add(entity, -c.getFrozenLevel() * 5, Temperature.Type.CORE);
			}
			if(c.getRottenLevel() > 0) {
				entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, c.getRottenLevel() * 200, c.getRottenLevel() - 1));
				entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, c.getRottenLevel() * 200, c.getRottenLevel() - 1));
				Temperature.add(entity, c.getRottenLevel() * 5, Temperature.Type.CORE);
			}
		});
	}
}
