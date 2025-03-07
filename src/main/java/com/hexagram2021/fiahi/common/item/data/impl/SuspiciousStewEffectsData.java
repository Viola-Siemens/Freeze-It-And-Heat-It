package com.hexagram2021.fiahi.common.item.data.impl;

import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.hexagram2021.fiahi.common.item.data.IPouchedFoodDataType;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodDataTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;

import javax.annotation.Nullable;
import java.util.List;

public record SuspiciousStewEffectsData(List<MobEffectInstance> effects) implements IPouchedFoodData {
	public static final Codec<SuspiciousStewEffectsData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					MOB_EFFECT_INSTANCE_CODEC.listOf().fieldOf("effects").forGetter(SuspiciousStewEffectsData::effects)
			).apply(instance, SuspiciousStewEffectsData::new)
	);

	@Override
	public IPouchedFoodDataType type() {
		return PouchedFoodDataTypes.SUSPICIOUS_STEW_EFFECTS;
	}

	@Override
	public void modifyStack(ItemStack itemStack) {
		this.effects.forEach(effect -> SuspiciousStewItem.saveMobEffect(itemStack, effect.getEffect(), effect.getDuration()));
	}

	@Nullable
	public static SuspiciousStewEffectsData fromStackNBT(CompoundTag nbt) {
		if(nbt.contains(SuspiciousStewItem.EFFECTS_TAG, Tag.TAG_LIST)) {
			return new SuspiciousStewEffectsData(IPouchedFoodData.getEffectsFromTag(nbt.getList(SuspiciousStewItem.EFFECTS_TAG, Tag.TAG_COMPOUND)));
		}
		return null;
	}
}
