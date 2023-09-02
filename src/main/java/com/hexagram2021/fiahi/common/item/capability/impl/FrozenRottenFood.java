package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FrozenRottenFood implements IFrozenRottenFood {
	private double temperature = 0;

	private final ItemStack self;

	public FrozenRottenFood(ItemStack self) {
		this.self = self;
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
	public void foodTick(double temperature, Item item) {
		if(this.self.isEdible()) {
			this.apply(temperature, item);
		}
	}

	private static final int TEMPERATURE_STEP = 5;
	@Override
	public void updateFoodTag() {
		int temperature = (int)(this.getTemperature() / TEMPERATURE_STEP) * TEMPERATURE_STEP;
		CompoundTag nbt = this.self.getTag();
		if(nbt == null) {
			if(temperature == 0) {
				return;
			}
			nbt = new CompoundTag();
		}
		nbt.putInt(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, temperature);

		this.self.setTag(nbt);
	}

	public void syncFoodTag() {
		CompoundTag nbt = this.self.getTag();
		if(nbt == null || !nbt.contains(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, Tag.TAG_ANY_NUMERIC)) {
			this.setTemperature(0.0D);
			return;
		}
		this.setTemperature(nbt.getDouble(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE));
	}
}
