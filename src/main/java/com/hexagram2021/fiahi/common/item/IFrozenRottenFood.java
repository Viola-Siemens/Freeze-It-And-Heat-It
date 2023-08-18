package com.hexagram2021.fiahi.common.item;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;

public interface IFrozenRottenFood {
	double getTemperature();
	void setTemperature(double newTemperature);

	default void apply(double newTemperature) {
		double diff = newTemperature - this.getTemperature();
		this.setTemperature(this.getTemperature() + diff * FIAHICommonConfig.TEMPERATURE_BALANCE_RATE.get() / 100.0D);
	}

	default int getFrozenLevel() {
		int temp = (int)this.getTemperature();
		return temp > 0 ? 0 : (-temp - 25) / 25;
	}
	default int getRottenLevel() {
		int temp = (int)this.getTemperature();
		return temp < 0 ? 0 : (temp - 25) / 25;
	}

	void tick(double temperature);
}
