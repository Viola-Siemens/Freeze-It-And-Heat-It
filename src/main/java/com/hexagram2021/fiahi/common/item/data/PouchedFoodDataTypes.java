package com.hexagram2021.fiahi.common.item.data;

import com.hexagram2021.fiahi.common.item.data.impl.AddPotionEffectsData;
import com.hexagram2021.fiahi.common.item.data.impl.SuspiciousStewEffectsData;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class PouchedFoodDataTypes {
	public static IPouchedFoodDataType SUSPICIOUS_STEW_EFFECTS = register(PouchedFoodDataNames.SUSPICIOUS_STEW_EFFECTS, new IPouchedFoodDataType() {
		@Override @Nullable
		public SuspiciousStewEffectsData fromStackNBT(CompoundTag nbt) {
			return SuspiciousStewEffectsData.fromStackNBT(nbt);
		}

		@Override
		public Codec<SuspiciousStewEffectsData> codec() {
			return SuspiciousStewEffectsData.CODEC;
		}
	});
	public static IPouchedFoodDataType ADD_POTION_EFFECTS = register(PouchedFoodDataNames.ADD_POTION_EFFECTS, new IPouchedFoodDataType() {
		@Override @Nullable
		public AddPotionEffectsData fromStackNBT(CompoundTag nbt) {
			return AddPotionEffectsData.fromStackNBT(nbt);
		}

		@Override
		public Codec<AddPotionEffectsData> codec() {
			return AddPotionEffectsData.CODEC;
		}
	});

	private PouchedFoodDataTypes() {
	}

	public static IPouchedFoodDataType register(ResourceLocation id, IPouchedFoodDataType type) {
		IPouchedFoodDataType.register(id, type);
		return type;
	}

	public static void init() {
		FIAHILogger.info("Loaded %d pouched food data types.".formatted(IPouchedFoodDataType.POUCHED_FOOD_DATA_TYPES.size()));
	}
}
