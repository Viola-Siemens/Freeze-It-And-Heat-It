package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.network.ClientboundFoodPouchPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHICustomPayloadTypes {
	public static final CustomPacketPayload.Type<ClientboundFoodPouchPacketPayload> FOOD_POUCH_DATA = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "food_pouch"));

	private FIAHICustomPayloadTypes() {
	}

	public static final class V1 {
		private V1() {
		}

		public static void register(PayloadRegistrar registrar) {
			registrar.playToClient(FOOD_POUCH_DATA, ClientboundFoodPouchPacketPayload.STREAM_CODEC, ClientboundFoodPouchPacketPayload::handle);
		}
	}
}
