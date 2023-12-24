package com.hexagram2021.fiahi.common.item.capability;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import static com.hexagram2021.fiahi.common.util.RegistryHelper.getRegistryName;
import static com.hexagram2021.fiahi.register.FIAHIItemTags.LEFTOVERS;

public interface IFrozenRottenFood {
	int FROZEN_ROTTEN_THRESHOLD = 25;	// [-125, 125]
	double EPS = 1e-2D;

	double getTemperature();
	void setTemperature(double newTemperature);

	default double getTemperatureBalanceRate() {
		return FIAHICommonConfig.TEMPERATURE_BALANCE_RATE.get() / 100.0D;
	}

	default void apply(double newTemperature) {
		this.apply(newTemperature, null);
	}

	default void apply(double newTemperature, @Nullable Item item) {
		double temp = this.getTemperature();
		double diff = (newTemperature - temp) * ConfigSettings.TEMP_RATE.get();
		if(diff < 0 && temp < 0) {
			diff *= FIAHICommonConfig.FROZEN_SPEED_MULTIPLIER.get();
		}
		if(diff > 0 && temp > 0) {
			diff *= FIAHICommonConfig.ROTTEN_SPEED_MULTIPLIER.get();
		}
		boolean flag = temp > 0;
		int level = flag ? this.getRottenLevel() : this.getFrozenLevel();
		this.setTemperature(temp + diff * this.getTemperatureBalanceRate());
		boolean newFlag = this.getTemperature() > 0;
		int newLevel = newFlag ? this.getRottenLevel() : this.getFrozenLevel();
		if(newLevel > 3) {
			newLevel = 3;
			this.setTemperature((FROZEN_ROTTEN_THRESHOLD * 5 - EPS) * (flag ? 1 : -1));
		}
		if(level == newLevel && (flag == newFlag || level == 0)) {
			this.updateFoodTag();
			return;
		}
		if(level < newLevel) {
			if(flag == newFlag || level == 0) {
				if(flag && item != null &&
						FIAHICommonConfig.NEVER_ROTTEN_FOODS.get().contains(getRegistryName(item).toString())) {
					this.setTemperature(FROZEN_ROTTEN_THRESHOLD * 2 - EPS);
				} else if(!flag && item != null &&
						FIAHICommonConfig.NEVER_FROZEN_FOODS.get().contains(getRegistryName(item).toString())) {
					this.setTemperature(-FROZEN_ROTTEN_THRESHOLD * 2 + EPS);
				}
				this.updateFoodTag();
				return;
			}
		}
		this.setTemperature((FROZEN_ROTTEN_THRESHOLD * (1 + level) + EPS) * (flag ? 1 : -1));
		this.updateFoodTag();
	}

	default int getFrozenLevel() {
		return getFrozenLevel((int)this.getTemperature());
	}
	default int getRottenLevel() {
		return getRottenLevel((int)this.getTemperature());
	}

	static int getFrozenLevel(int temp) {
		if(!FIAHICommonConfig.ENABLE_FROZEN.get()) {
			return 0;
		}
		return temp >= 0 ? 0 : (-temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}
	static int getRottenLevel(int temp) {
		if(!FIAHICommonConfig.ENABLE_ROTTEN.get()) {
			return 0;
		}
		return temp <= 0 ? 0 : (temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}

	void foodTick(double temperature, Item item);

	void updateFoodTag();

	static boolean canBeFrozenRotten(ItemStack itemStack) {
		return itemStack.isEdible() && !itemStack.is(LEFTOVERS);
	}
}
