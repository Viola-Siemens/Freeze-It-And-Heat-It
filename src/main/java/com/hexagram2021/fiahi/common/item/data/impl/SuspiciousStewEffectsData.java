package com.hexagram2021.fiahi.common.item.data.impl;

import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.hexagram2021.fiahi.common.item.data.IPouchedFoodDataType;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodDataTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.component.SuspiciousStewEffects;

public record SuspiciousStewEffectsData(SuspiciousStewEffects object) implements IPouchedFoodData<SuspiciousStewEffects> {
	public static final MapCodec<SuspiciousStewEffectsData> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					SuspiciousStewEffects.CODEC.fieldOf("effects").forGetter(SuspiciousStewEffectsData::object)
			).apply(instance, SuspiciousStewEffectsData::new)
	);

	@Override
	public IPouchedFoodDataType<SuspiciousStewEffects> type() {
		return PouchedFoodDataTypes.SUSPICIOUS_STEW_EFFECTS;
	}
}
