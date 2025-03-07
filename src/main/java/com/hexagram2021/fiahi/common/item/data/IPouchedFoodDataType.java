package com.hexagram2021.fiahi.common.item.data;

import com.google.common.collect.Maps;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public interface IPouchedFoodDataType {
	@Nullable
	IPouchedFoodData fromStackNBT(CompoundTag nbt);
	Codec<? extends IPouchedFoodData> codec();

	Map<ResourceLocation, IPouchedFoodDataType> POUCHED_FOOD_DATA_TYPES = Maps.newHashMap();
	Map<IPouchedFoodDataType, ResourceLocation> POUCHED_FOOD_DATA_IDS = Maps.newIdentityHashMap();
	static void register(ResourceLocation id, IPouchedFoodDataType pouchedFoodDataType) {
		if(POUCHED_FOOD_DATA_TYPES.containsKey(id)) {
			FIAHILogger.warn(new IllegalStateException("Duplicate pouched food data type registered: %s.".formatted(id)));
		}
		if(POUCHED_FOOD_DATA_IDS.containsKey(pouchedFoodDataType)) {
			FIAHILogger.warn(new IllegalStateException("Duplicate pouched food data object registered for %s.".formatted(id)));
		}
		POUCHED_FOOD_DATA_TYPES.put(id, pouchedFoodDataType);
		POUCHED_FOOD_DATA_IDS.put(pouchedFoodDataType, id);
	}

	Codec<IPouchedFoodDataType> REGISTRY_CODEC = new Codec<>() {
		@Override
		public <R> DataResult<Pair<IPouchedFoodDataType, R>> decode(DynamicOps<R> ops, R input) {
			return ResourceLocation.CODEC.decode(ops, input).flatMap(pair -> {
				if(!POUCHED_FOOD_DATA_TYPES.containsKey(pair.getFirst())) {
					return DataResult.error("Unexpected type: %s".formatted(pair.getFirst()));
				}
				return DataResult.success(pair.mapFirst(POUCHED_FOOD_DATA_TYPES::get));
			});
		}

		@Override
		public <R> DataResult<R> encode(IPouchedFoodDataType input, DynamicOps<R> ops, R prefix) {
			ResourceLocation id = POUCHED_FOOD_DATA_IDS.get(input);
			if(id == null) {
				return DataResult.error("Unknown pouched food data type: %s".formatted(input));
			}
			R key = ops.createString(id.toString());
			return ops.mergeToPrimitive(prefix, key);
		}
	};
}
