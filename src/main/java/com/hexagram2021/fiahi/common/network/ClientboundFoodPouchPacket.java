package com.hexagram2021.fiahi.common.network;

import com.hexagram2021.fiahi.client.ScreenManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.IdentityHashMap;
import java.util.Map;

public class ClientboundFoodPouchPacket implements IFIAHIPacket {
	private final Map<Item, Integer> stackedItems;
	private final int containerId;

	public ClientboundFoodPouchPacket(Map<Item, Integer> stackedItems, int containerId) {
		this.stackedItems = stackedItems;
		this.containerId = containerId;
	}

	public ClientboundFoodPouchPacket(FriendlyByteBuf buf) {
		this.stackedItems = buf.readMap(
				IdentityHashMap::new,
				readBuf -> {
					int itemId = readBuf.readVarInt();
					return Item.byId(itemId);
				},
				FriendlyByteBuf::readVarInt
		);
		this.containerId = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.stackedItems, (writeBuf, item) -> writeBuf.writeVarInt(Item.getId(item)), FriendlyByteBuf::writeVarInt);
		buf.writeInt(this.containerId);
	}

	@Override
	public void handle(NetworkEvent.Context context) {
		ScreenManager.openFoodPouchScreen(this.stackedItems, this.containerId);
	}
}
