package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.ForgeEventHandler;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
	public void fiahi$tickFood(CallbackInfo ci) {
		ItemEntity current = (ItemEntity) (Object) this;
		if(current.level() instanceof ServerLevel serverLevel && ForgeEventHandler.isAvailableToTickFood()) {
			IFrozenRottenFood.tick(current.getItem(), current::setItem, c -> c.getTemperature() + 2.0D * WorldHelper.getTemperatureAt(current.level(), current.getOnPos()), serverLevel, null);
		}
	}
}
