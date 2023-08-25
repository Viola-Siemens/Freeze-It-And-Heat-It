package com.hexagram2021.fiahi.common.item.capability;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import dev.momostudios.coldsweat.config.ConfigSettings;

public interface IFrozenRottenFood {
	int FROZEN_ROTTEN_THRESHOLD = 25;
	double EPS = 1e-6D;

	double getTemperature();
	void setTemperature(double newTemperature);

	default void apply(double newTemperature) {
		double temp = this.getTemperature();
		double diff = (newTemperature - temp) * ConfigSettings.TEMP_RATE.get();
		boolean flag = temp > 0;
		int level = flag ? this.getRottenLevel() : this.getFrozenLevel();
		this.setTemperature(temp + diff * FIAHICommonConfig.TEMPERATURE_BALANCE_RATE.get() / 100.0D);
		boolean newFlag = this.getTemperature() > 0;
		int newLevel = newFlag ? this.getRottenLevel() : this.getFrozenLevel();
		if(level == newLevel && (flag == newFlag || level == 0)) {
			return;
		}
		if(level < newLevel) {
			if(flag == newFlag || level == 0) {
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

	void foodTick(double temperature);

	void updateFoodFrozenRottenLevel();
}
