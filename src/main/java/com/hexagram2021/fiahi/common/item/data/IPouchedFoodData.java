package com.hexagram2021.fiahi.common.item.data;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public interface IPouchedFoodData<T> {
	Codec<IPouchedFoodData<?>> REGISTRY_CODEC = IPouchedFoodDataType.REGISTRY_CODEC.dispatch(IPouchedFoodData::type, IPouchedFoodDataType::codec);

	IPouchedFoodDataType<T> type();
	T object();

	default void modifyStack(ItemStack itemStack) {
		itemStack.set(this.type().getDataComponentType(), this.object());
	}

	@Override @Nullable
	String toString();

	@Nullable
	private static <T> IPouchedFoodData<T> createFromType(ItemStack itemStack, IPouchedFoodDataType<T> type) {
		T object = itemStack.get(type.getDataComponentType());
		if(object == null) {
			return null;
		}
		return type.create(object);
	}

	static PouchedFoodKey tryLoad(ItemStack itemStack) {
		return new PouchedFoodKey(itemStack.getItem(), IPouchedFoodDataType.POUCHED_FOOD_DATA_TYPES.values().stream()
				.map((type) -> createFromType(itemStack, type))
				.filter(Objects::nonNull)
				.toArray(IPouchedFoodData[]::new));
	}
}
