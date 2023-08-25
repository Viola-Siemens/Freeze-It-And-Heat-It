package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.register.FIAHICapabilities;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
	public void tickFood(CallbackInfo ci) {
		ItemEntity current = (ItemEntity)(Object)this;
		current.getItem().getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c ->
				c.foodTick(c.getTemperature() + 5.0 * Temperature.getTemperatureAt(current.getOnPos(), current.level)));
	}
}
