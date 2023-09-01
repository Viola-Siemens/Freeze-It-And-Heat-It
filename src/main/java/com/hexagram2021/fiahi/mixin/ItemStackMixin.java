package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IForgeItemStack {
	@SuppressWarnings("ConstantConditions")
	@Override @Nullable
	public CompoundTag getShareTag() {
		ItemStack current = (ItemStack)(Object)this;
		CompoundTag nbt = current.getShareTag();
		LazyOptional<IFrozenRottenFood> c = current.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
		IFrozenRottenFood food = c.orElse(null);
		if(food != null) {
			if(nbt == null) {
				nbt = new CompoundTag();
			}
			nbt.putDouble(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, food.getTemperature());
		}
		return nbt;
	}

	@Override
	public void readShareTag(@Nullable CompoundTag nbt) {
		ItemStack current = (ItemStack)(Object)this;
		if(nbt != null && nbt.contains(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, Tag.TAG_ANY_NUMERIC)) {
			current.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> c.setTemperature(nbt.getDouble(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE)));
			nbt.remove(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE);
		}
		current.readShareTag(nbt);
	}

	@Inject(method = "inventoryTick", at = @At(value = "TAIL"))
	public void tickFood(Level level, Entity entity, int slot, boolean selected, CallbackInfo ci) {
		ItemStack current = (ItemStack)(Object)this;
		if(entity instanceof LivingEntity livingEntity) {
			double temp = Temperature.get(livingEntity, Temperature.Type.CORE);
			current.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> c.foodTick(temp, current.getItem()));
		}
	}
}
