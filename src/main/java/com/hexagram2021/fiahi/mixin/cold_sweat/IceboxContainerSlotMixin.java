package com.hexagram2021.fiahi.mixin.cold_sweat;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood.canBeFrozenRotten;

@Mixin(targets = "com/momosoftworks/coldsweat/common/container/IceboxContainer$2")
public class IceboxContainerSlotMixin {
	@Inject(method = "mayPlace", at = @At(value = "RETURN"), cancellable = true)
	public void fiahi$mayFoodPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if(!cir.getReturnValue()) {
			cir.setReturnValue(canBeFrozenRotten(stack));
		}
	}
}
