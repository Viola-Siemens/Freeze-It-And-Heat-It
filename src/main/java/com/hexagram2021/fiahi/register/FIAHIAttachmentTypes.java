package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIAttachmentTypes {
	private static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FOOD_TEMPERATURE = REGISTER.register(
			"food",
			() -> DataComponentType.<Integer>builder()
					.persistent(ExtraCodecs.intRange(-IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD * 5, IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD * 5))
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build()
	);
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<FoodPouchData>> FOOD_POUCH_DATA = REGISTER.register(
			"food_pouch",
			() -> DataComponentType.<FoodPouchData>builder()
					.persistent(FoodPouchData.CODEC)
					.networkSynchronized(FoodPouchData.STREAM_CODEC)
					.build()
	);

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
