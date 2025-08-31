package com.hexagram2021.fiahi.common.network;

import com.hexagram2021.fiahi.client.ScreenManager;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.register.FIAHICustomPayloadTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundFoodPouchPacketPayload(FoodPouchData data, int containerId) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFoodPouchPacketPayload> STREAM_CODEC = StreamCodec.composite(
			FoodPouchData.STREAM_CODEC,
			ClientboundFoodPouchPacketPayload::data,
			ByteBufCodecs.VAR_INT,
			ClientboundFoodPouchPacketPayload::containerId,
			ClientboundFoodPouchPacketPayload::new
	);

	@Override
	public Type<ClientboundFoodPouchPacketPayload> type() {
		return FIAHICustomPayloadTypes.FOOD_POUCH_DATA;
	}

	@SuppressWarnings("unused")
	public void handle(IPayloadContext context) {
		ScreenManager.openFoodPouchScreen(this.data, this.containerId);
	}
}
