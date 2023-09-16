package com.hexagram2021.fiahi.register;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class FIAHIFoods {
	public static final FoodProperties LEFTOVER_MEAT = new FoodProperties.Builder()
			.nutrition(1).saturationMod(0.3F)
			.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400, 0), 0.8F)
			.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 0.8F)
			.meat().build();
	public static final FoodProperties LEFTOVER_VEGETABLE = new FoodProperties.Builder()
			.nutrition(1).saturationMod(0.1F)
			.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400, 0), 0.8F)
			.effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 0.8F)
			.build();
}
