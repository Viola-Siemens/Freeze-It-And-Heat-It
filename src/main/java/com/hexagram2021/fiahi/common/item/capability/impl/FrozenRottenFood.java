package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FrozenRottenFood implements IFrozenRottenFood {
	private double temperature = 0;

	private final ItemStack self;

	public FrozenRottenFood(ItemStack self) {
		this.self = self;
		this.syncFoodTag();
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
		if(IFrozenRottenFood.canBeFrozenRotten(this.self)) {
			this.apply(temperature, item);
		}
	}

	private static final int TEMPERATURE_STEP = 5;
	@Override
	public void updateFoodTag() {
		int temperature = (int)(this.getTemperature() / TEMPERATURE_STEP) * TEMPERATURE_STEP;
		Integer attachment = this.self.get(FIAHIAttachmentTypes.FOOD_TEMPERATURE);
		if(attachment == null) {
			if(temperature == 0) {
				return;
			}
		}

		this.self.set(FIAHIAttachmentTypes.FOOD_TEMPERATURE, temperature);
	}

	public void syncFoodTag() {
		Integer attachment = this.self.get(FIAHIAttachmentTypes.FOOD_TEMPERATURE);
		if(attachment == null) {
			this.setTemperature(0.0D);
			return;
		}
		this.setTemperature(attachment);
	}
}
