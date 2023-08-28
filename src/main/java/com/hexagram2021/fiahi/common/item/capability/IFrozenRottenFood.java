package com.hexagram2021.fiahi.common.item.capability;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;
import net.minecraft.world.item.Item;

import java.util.Objects;

public interface IFrozenRottenFood {
	int FROZEN_ROTTEN_THRESHOLD = 25;	// [-125, 125]
	double EPS = 1e-4D;

	double getTemperature();
	void setTemperature(double newTemperature);

	default void apply(double newTemperature, Item item) {
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
		this.setTemperature(temp + diff * FIAHICommonConfig.TEMPERATURE_BALANCE_RATE.get() / 100.0D);
		boolean newFlag = this.getTemperature() > 0;
		int newLevel = newFlag ? this.getRottenLevel() : this.getFrozenLevel();
		if(level == newLevel && (flag == newFlag || level == 0)) {
			return;
		}
		if(newLevel > 3) {
			newLevel = 3;
			this.setTemperature((FROZEN_ROTTEN_THRESHOLD * 5 - EPS) * (flag ? 1 : -1));
		}
		if(level < newLevel) {
			if(flag == newFlag || level == 0) {
				if(flag && newLevel > 0 && FIAHICommonConfig.NEVER_ROTTEN_FOODS.get().contains(Objects.requireNonNull(item.getRegistryName()).toString())) {
					this.setTemperature(FROZEN_ROTTEN_THRESHOLD * 2 - EPS);
					return;
				}
				if(!flag && newLevel < 0 && FIAHICommonConfig.NEVER_FROZEN_FOODS.get().contains(Objects.requireNonNull(item.getRegistryName()).toString())) {
					this.setTemperature(-FROZEN_ROTTEN_THRESHOLD * 2 + EPS);
					return;
				}
				this.updateFoodFrozenRottenLevel();
				return;
			}
		}
		this.setTemperature((FROZEN_ROTTEN_THRESHOLD * (1 + level) + EPS) * (flag ? 1 : -1));
	}

	default int getFrozenLevel() {
		if(!FIAHICommonConfig.ENABLE_FROZEN.get()) {
			return 0;
		}
		int temp = (int)this.getTemperature();
		return temp >= 0 ? 0 : (-temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}
	default int getRottenLevel() {
		if(!FIAHICommonConfig.ENABLE_ROTTEN.get()) {
			return 0;
		}
		int temp = (int)this.getTemperature();
		return temp <= 0 ? 0 : (temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}

	void foodTick(double temperature, Item item);

	void updateFoodFrozenRottenLevel();
}
