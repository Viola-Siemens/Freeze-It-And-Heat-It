package com.hexagram2021.fiahi.common.item.data;

import com.hexagram2021.fiahi.common.util.RegistryHelper;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public interface IPouchedFoodData {
	Codec<MobEffectInstance> MOB_EFFECT_INSTANCE_CODEC = RecordCodecBuilder.create(effectInstance -> effectInstance.group(
			Codec.mapEither(
					Codec.BYTE.fieldOf("Id"),
					ResourceLocation.CODEC.fieldOf("forge:id")
			).xmap(
					either -> either.map(MobEffect::byId, ForgeRegistries.MOB_EFFECTS::getValue),
					effect -> Either.right(RegistryHelper.getRegistryName(effect))
			).forGetter(MobEffectInstance::getEffect),
			Codec.INT.optionalFieldOf("Duration", 160).forGetter(MobEffectInstance::getDuration),
			Codec.INT.optionalFieldOf("Amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
			Codec.BOOL.optionalFieldOf("Ambient", false).forGetter(MobEffectInstance::isAmbient),
			Codec.BOOL.optionalFieldOf("ShowParticles", true).forGetter(MobEffectInstance::isVisible)
	).apply(effectInstance, MobEffectInstance::new));

	Codec<IPouchedFoodData> REGISTRY_CODEC = IPouchedFoodDataType.REGISTRY_CODEC.dispatch(IPouchedFoodData::type, IPouchedFoodDataType::codec);

	IPouchedFoodDataType type();

	void modifyStack(ItemStack itemStack);

	@Override @Nullable
	String toString();

	static IPouchedFoodData[] tryLoad(@Nullable CompoundTag stackNBT) {
		if(stackNBT == null) {
			return PouchedFoodKey.EMPTY.datas();
		}
		return IPouchedFoodDataType.POUCHED_FOOD_DATA_TYPES.values().stream()
				.map(type -> type.fromStackNBT(stackNBT))
				.filter(Objects::nonNull)
				.toArray(IPouchedFoodData[]::new);
	}
}
