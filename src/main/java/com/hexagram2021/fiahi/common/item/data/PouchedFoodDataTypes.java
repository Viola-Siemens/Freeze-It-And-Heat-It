package com.hexagram2021.fiahi.common.item.data;

import com.hexagram2021.fiahi.common.item.data.impl.SuspiciousStewEffectsData;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.SuspiciousStewEffects;

public final class PouchedFoodDataTypes {
	public static IPouchedFoodDataType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register(PouchedFoodDataNames.SUSPICIOUS_STEW_EFFECTS, new IPouchedFoodDataType<>() {
		@Override
		public DataComponentType<SuspiciousStewEffects> getDataComponentType() {
			return DataComponents.SUSPICIOUS_STEW_EFFECTS;
		}

		@Override
		public MapCodec<SuspiciousStewEffectsData> codec() {
			return SuspiciousStewEffectsData.CODEC;
		}

		@Override
		public IPouchedFoodData<SuspiciousStewEffects> create(SuspiciousStewEffects object) {
			return new SuspiciousStewEffectsData(object);
		}
	});

	private PouchedFoodDataTypes() {
	}

	public static <T> IPouchedFoodDataType<T> register(ResourceLocation id, IPouchedFoodDataType<T> type) {
		IPouchedFoodDataType.register(id, type);
		return type;
	}

	public static void init() {
		FIAHILogger.info("Loaded %d pouched food data types.".formatted(IPouchedFoodDataType.POUCHED_FOOD_DATA_TYPES.size()));
	}
}
