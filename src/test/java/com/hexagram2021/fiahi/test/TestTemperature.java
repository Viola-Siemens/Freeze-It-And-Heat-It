package com.hexagram2021.fiahi.test;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public abstract class TestTemperature implements IFrozenRottenFood {
	private static final Logger LOGGER = LogUtils.getLogger();

	double temperature = 0;

	@Override
	public double getTemperature() {
		return this.temperature;
	}

	@Override
	public void setTemperature(double newTemperature) {
		this.temperature = newTemperature;
	}

//	@Override
//	public void tick(double temperature) {
//		this.apply(temperature);
//		LOGGER.info("Current temperature {}, rotten level {}, frozen level {}.", this.temperature, this.getRottenLevel(), this.getFrozenLevel());
//	}
//
//	@Override
//	public void updateFoodFrozenRottenLevel() {
//		if(this.getRottenLevel() > 0) {
//			LOGGER.info("The rotten level has been updated to {}.", this.getRottenLevel());
//		}
//		if(this.getFrozenLevel() > 0) {
//			LOGGER.info("The frozen level has been updated to {}.", this.getFrozenLevel());
//		}
//	}
//
//	public static void main(String[] args) {
//		TestTemperature test = new TestTemperature();
//
//		test.tick(-100);
//		test.tick(-100);
//		test.tick(-100);
//		test.tick(100);
//		test.tick(100);
//		test.tick(100);
//		test.setTemperature(0);
//		LOGGER.info("====================================");
//		test.tick(60);
//		test.tick(60);
//		test.tick(60);
//		test.tick(60);
//		test.tick(-50);
//		test.tick(-40);
//		test.tick(-30);
//	}
}
