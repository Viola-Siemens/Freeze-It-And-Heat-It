package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.item.IFrozenRottenFood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IFrozenRottenFood {
	private static final String FIAHI_TAG_TEMPERATURE = "fiahi_temperature";

	@Shadow
	public abstract boolean isEdible();

	private double temperature = 0;

	private int nextTickChecker = 100;

	@Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "RETURN"))
	public void getTemperature(CompoundTag nbt, CallbackInfo ci) {
		if(this.isEdible() && nbt.contains(FIAHI_TAG_TEMPERATURE, Tag.TAG_DOUBLE)) {
			this.setTemperature(nbt.getDouble(FIAHI_TAG_TEMPERATURE));
		}
	}

	@Inject(method = "save", at = @At(value = "TAIL"))
	public void saveTemperature(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
		if(this.isEdible()) {
			nbt.putDouble(FIAHI_TAG_TEMPERATURE, this.getTemperature());
		}
	}

	@Override
	public double getTemperature() {
		return this.temperature;
	}

	@Override
	public void setTemperature(double newTemperature) {
		this.temperature = newTemperature;
	}

	@Override
	public void tick(double temperature) {
		if(this.isEdible()) {
			if(this.nextTickChecker > 0) {
				--this.nextTickChecker;
			} else {
				this.apply(temperature);
			}
		}
	}
}
