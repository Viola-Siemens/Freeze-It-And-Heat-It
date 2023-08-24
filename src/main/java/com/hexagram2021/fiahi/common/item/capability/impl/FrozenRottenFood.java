package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FrozenRottenFood extends ItemStack implements IFrozenRottenFood, INBTSerializable<CompoundTag> {
	private static final String FIAHI_TAG_TEMPERATURE = "fiahi:temperature";
	private static final String FIAHI_TAG_CHECKER = "fiahi:nextChecker";
	private static final String FIAHI_TAG_ROTTEN_LEVEL = "fiahi:rotten_level";
	private static final String FIAHI_TAG_FROZEN_LEVEL = "fiahi:frozen_level";

	private double temperature = 0;

	private int nextTickChecker = 100;

	public FrozenRottenFood(ItemLike item) {
		super(item);
	}
	public FrozenRottenFood(Holder<Item> item) {
		super(item);
	}
	public FrozenRottenFood(ItemLike item, int count) {
		super(item, count);
	}
	public FrozenRottenFood(ItemLike item, int count, @Nullable CompoundTag nbt) {
		super(item, count, nbt);
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
		if(this.self().isEdible()) {
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
		CompoundTag nbt = this.self().getTag();
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

		this.self().setTag(nbt);
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

	private final LazyOptional<IFrozenRottenFood> holder = LazyOptional.of(() -> this);

	@Override @Nonnull
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == FIAHICapabilities.FOOD_CAPABILITY) {
			return this.holder.cast();
		}
		return super.getCapability(capability, facing);
	}
}
