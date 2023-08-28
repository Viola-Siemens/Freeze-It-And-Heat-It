package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FrozenRottenFood implements IFrozenRottenFood {
	private static final String FIAHI_TAG_ROTTEN_LEVEL = "fiahi:rotten_level";
	private static final String FIAHI_TAG_FROZEN_LEVEL = "fiahi:frozen_level";

	private double temperature = 0;

	private int tickAfterCheck = 0;

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
		if(this.tickAfterCheck < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
			++this.tickAfterCheck;
			return;
		}
		this.tickAfterCheck = 0;
		if(this.self.isEdible()) {
			this.apply(temperature, item);
		}
	}

	@Override
	public void updateFoodFrozenRottenLevel() {
		int rotten = this.getRottenLevel();
		int frozen = this.getFrozenLevel();
		boolean remove = rotten == 0 && frozen == 0;
		CompoundTag nbt = this.self.getTag();
		if(nbt == null) {
			if(remove) {
				return;
			}
			nbt = new CompoundTag();
		} else if(remove) {
			nbt.remove(FIAHI_TAG_ROTTEN_LEVEL);
			nbt.remove(FIAHI_TAG_FROZEN_LEVEL);
			if(nbt.size() == 0) {
				nbt = null;
			}
		} else if(rotten != 0) {
			nbt.remove(FIAHI_TAG_FROZEN_LEVEL);
			nbt.putInt(FIAHI_TAG_ROTTEN_LEVEL, rotten);
		} else {
			nbt.remove(FIAHI_TAG_ROTTEN_LEVEL);
			nbt.putInt(FIAHI_TAG_FROZEN_LEVEL, frozen);
		}

		this.self.setTag(nbt);
	}
}
