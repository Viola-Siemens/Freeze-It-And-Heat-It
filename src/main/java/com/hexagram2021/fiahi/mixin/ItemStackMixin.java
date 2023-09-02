package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.ForgeEventHandler;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IForgeItemStack {
	@Inject(method = "inventoryTick", at = @At(value = "TAIL"))
	public void tickFood(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
		if(!level.isClientSide && ForgeEventHandler.isAvailableToTickFood()) {
			ItemStack current = (ItemStack) (Object) this;
			if (entity instanceof LivingEntity livingEntity) {
				double temp = Temperature.get(livingEntity, Temperature.Type.CORE);
				current.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> c.foodTick(temp, current.getItem()));
			}
		}
	}
}
