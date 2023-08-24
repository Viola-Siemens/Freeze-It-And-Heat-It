package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class FrozenRottenFood implements IFrozenRottenFood, INBTSerializable<CompoundTag> {
	private static final String FIAHI_TAG_TEMPERATURE = "fiahi:temperature";
	private static final String FIAHI_TAG_CHECKER = "fiahi:nextChecker";
	private static final String FIAHI_TAG_ROTTEN_LEVEL = "fiahi:rotten_level";
	private static final String FIAHI_TAG_FROZEN_LEVEL = "fiahi:frozen_level";

	private double temperature = 0;

	private int nextTickChecker = 100;

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
	public void tick(double temperature) {
		if(this.self.isEdible()) {
			if(this.nextTickChecker > 0) {
				--this.nextTickChecker;
			} else {
				this.nextTickChecker = FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get();
				this.apply(temperature);
			}
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

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putDouble(FIAHI_TAG_TEMPERATURE, this.getTemperature());
		nbt.putInt(FIAHI_TAG_CHECKER, this.nextTickChecker);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.setTemperature(nbt.getDouble(FIAHI_TAG_TEMPERATURE));
		this.updateFoodFrozenRottenLevel();
		this.nextTickChecker = nbt.getInt(FIAHI_TAG_CHECKER);
	}
}
