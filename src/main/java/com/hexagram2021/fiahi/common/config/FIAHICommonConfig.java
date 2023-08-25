package com.hexagram2021.fiahi.common.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class FIAHICommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NEVER_FROZEN_FOODS;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NEVER_ROTTEN_FOODS;

	public static final ForgeConfigSpec.BooleanValue ENABLE_FROZEN;
	public static final ForgeConfigSpec.BooleanValue ENABLE_ROTTEN;

	public static final ForgeConfigSpec.IntValue TEMPERATURE_CHECKER_INTERVAL;
	public static final ForgeConfigSpec.IntValue TEMPERATURE_BALANCE_RATE;

	private FIAHICommonConfig() {}

	static {
		BUILDER.push("fiahi-common-config");
			NEVER_FROZEN_FOODS = BUILDER.comment("Which foods will never be frozen.")
					.defineList("NEVER_FROZEN_FOODS", List.of(
							new ResourceLocation("dried_kelp").toString()
					), o -> o instanceof String str && ResourceLocation.isValidResourceLocation(str));
			NEVER_ROTTEN_FOODS = BUILDER.comment("Which foods will never be rotten.")
					.defineList("NEVER_ROTTEN_FOODS", List.of(
							new ResourceLocation("golden_apple").toString(),
							new ResourceLocation("enchanted_golden_apple").toString(),
							new ResourceLocation("golden_carrot").toString(),
							new ResourceLocation("emeraldcraft", "golden_peach").toString(),
							new ResourceLocation("emeraldcraft", "agate_apple").toString(),
							new ResourceLocation("emeraldcraft", "jade_apple").toString()
					), o -> o instanceof String str && ResourceLocation.isValidResourceLocation(str));
			ENABLE_FROZEN = BUILDER.comment("If false, foods will never be frozen.")
					.define("ENABLE_FROZEN", true);
			ENABLE_ROTTEN = BUILDER.comment("If false, foods will never be rotten.")
					.define("ENABLE_ROTTEN", true);
			TEMPERATURE_CHECKER_INTERVAL = BUILDER.comment("How many ticks after a single check will it try again to modify the temperature of the food.")
					.defineInRange("TEMPERATURE_CHECKER_INTERVAL", 50, 1, 24000);
			TEMPERATURE_BALANCE_RATE = BUILDER.comment("When trying to modify the temperature of the food each time, how many difference will be applied.")
					.defineInRange("TEMPERATURE_BALANCE_RATE", 20, 1, 100);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	public static ForgeConfigSpec getConfig() {
		return SPEC;
	}
}
