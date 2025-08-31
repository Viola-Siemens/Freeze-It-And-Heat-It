package com.hexagram2021.fiahi.common.item.capability.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record FoodPouchData(double temperature, List<ItemStack> items) {
	public static final FoodPouchData EMPTY = new FoodPouchData(0, List.of());

	public static final Codec<FoodPouchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("temperature").forGetter(FoodPouchData::temperature),
			ItemStack.CODEC.listOf().fieldOf("items").forGetter(FoodPouchData::items)
	).apply(instance, FoodPouchData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FoodPouchData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, FoodPouchData::temperature,
			ItemStack.LIST_STREAM_CODEC, FoodPouchData::items,
			FoodPouchData::new
	);
}
