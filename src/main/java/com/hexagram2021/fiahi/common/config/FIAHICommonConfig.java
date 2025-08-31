package com.hexagram2021.fiahi.common.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class FIAHICommonConfig {
	private static final String REGISTRY_NAME_MATCHER = "([a-z0-9_.-]+:[a-z0-9_/.-]+)";

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	private static final ModConfigSpec SPEC;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> NEVER_FROZEN_FOODS;
	public static final ModConfigSpec.ConfigValue<List<? extends String>> NEVER_ROTTEN_FOODS;

	public static final ModConfigSpec.BooleanValue ENABLE_FROZEN;
	public static final ModConfigSpec.BooleanValue ENABLE_ROTTEN;

	public static final ModConfigSpec.IntValue TEMPERATURE_CHECKER_INTERVAL;
	public static final ModConfigSpec.IntValue TEMPERATURE_BALANCE_RATE;

	public static final ModConfigSpec.DoubleValue FROZEN_SPEED_MULTIPLIER;
	public static final ModConfigSpec.DoubleValue ROTTEN_SPEED_MULTIPLIER;

	public static final ModConfigSpec.ConfigValue<List<? extends String>> STABLE_TEMPERATURE_CONTAINERS;

	private FIAHICommonConfig() {}

	static {
		BUILDER.push("fiahi-common-config");
			NEVER_FROZEN_FOODS = BUILDER.comment("Which foods will never be frozen.")
					.defineList("NEVER_FROZEN_FOODS", List.of(
							ResourceLocation.withDefaultNamespace("dried_kelp").toString()
					), () -> "fiahi:example", o -> o instanceof String str && str.matches(REGISTRY_NAME_MATCHER));
			NEVER_ROTTEN_FOODS = BUILDER.comment("Which foods will never be rotten.")
					.defineList("NEVER_ROTTEN_FOODS", List.of(
							ResourceLocation.withDefaultNamespace("golden_apple").toString(),
							ResourceLocation.withDefaultNamespace("enchanted_golden_apple").toString(),
							ResourceLocation.withDefaultNamespace("golden_carrot").toString(),
							ResourceLocation.fromNamespaceAndPath("emeraldcraft", "golden_peach").toString(),
							ResourceLocation.fromNamespaceAndPath("emeraldcraft", "agate_apple").toString(),
							ResourceLocation.fromNamespaceAndPath("emeraldcraft", "jade_apple").toString()
					), () -> "fiahi:example", o -> o instanceof String str && str.matches(REGISTRY_NAME_MATCHER));
			ENABLE_FROZEN = BUILDER.comment("If false, foods will never be frozen.")
					.define("ENABLE_FROZEN", true);
			ENABLE_ROTTEN = BUILDER.comment("If false, foods will never be rotten.")
					.define("ENABLE_ROTTEN", true);
			TEMPERATURE_CHECKER_INTERVAL = BUILDER.comment("How many ticks after a single check will it try again to modify the temperature of the food.")
					.defineInRange("TEMPERATURE_CHECKER_INTERVAL", 120, 1, 24000);
			TEMPERATURE_BALANCE_RATE = BUILDER.comment("When trying to modify the temperature of the food each time, how many difference will be applied.")
					.defineInRange("TEMPERATURE_BALANCE_RATE", 10, 1, 100);

			FROZEN_SPEED_MULTIPLIER = BUILDER.comment("How fast will a food item get frozen. The bigger, the faster.")
					.defineInRange("FROZEN_SPEED_MULTIPLIER", 1.0D, 0.01D, 100.0D);
			ROTTEN_SPEED_MULTIPLIER = BUILDER.comment("How fast will a food item get rotten. The bigger, the faster.")
					.defineInRange("ROTTEN_SPEED_MULTIPLIER", 0.75D, 0.01D, 100.0D);
			STABLE_TEMPERATURE_CONTAINERS = BUILDER.comment("A whitelist of containers. Food items in these block entities will never be affected by temperature.")
					.defineList("STABLE_TEMPERATURE_CONTAINERS", List.of(
							ResourceLocation.fromNamespaceAndPath("cold_sweat", "boiler").toString(),
							ResourceLocation.fromNamespaceAndPath("cold_sweat", "icebox").toString()
					), () -> "fiahi:example", o -> o instanceof String str && str.matches(REGISTRY_NAME_MATCHER));
		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	public static ModConfigSpec getConfig() {
		return SPEC;
	}
}
