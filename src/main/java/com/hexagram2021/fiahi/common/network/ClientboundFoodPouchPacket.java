package com.hexagram2021.fiahi.common.network;

import com.hexagram2021.fiahi.client.ScreenManager;
import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodKey;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.IdentityHashMap;
import java.util.Map;

public class ClientboundFoodPouchPacket implements IFIAHIPacket {
	private final Map<PouchedFoodKey, Integer> stackedItems;
	private final int containerId;

	public ClientboundFoodPouchPacket(Map<PouchedFoodKey, Integer> stackedItems, int containerId) {
		this.stackedItems = stackedItems;
		this.containerId = containerId;
	}

	public ClientboundFoodPouchPacket(FriendlyByteBuf buf) {
		this.stackedItems = buf.readMap(
				IdentityHashMap::new,
				readBuf -> {
					int itemId = readBuf.readVarInt();
					int arraySize = readBuf.readVarInt();
					IPouchedFoodData[] datas = new IPouchedFoodData[arraySize];
					for(int i = 0; i < arraySize; ++i) {
						datas[i] = IPouchedFoodData.REGISTRY_CODEC.parse(NbtOps.INSTANCE, readBuf.readNbt()).getOrThrow(false, FIAHILogger::error);
					}
					return new PouchedFoodKey(Item.byId(itemId), datas);
				},
				FriendlyByteBuf::readVarInt
		);
		this.containerId = buf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeMap(this.stackedItems, (writeBuf, item) -> {
			writeBuf.writeVarInt(Item.getId(item.item()));
			writeBuf.writeVarInt(item.datas().length);
			for(int i = 0; i < item.datas().length; ++i) {
				writeBuf.writeNbt((CompoundTag) IPouchedFoodData.REGISTRY_CODEC.encode(item.datas()[i], NbtOps.INSTANCE, new CompoundTag()).getOrThrow(false, FIAHILogger::error));
			}
		}, FriendlyByteBuf::writeVarInt);
		buf.writeInt(this.containerId);
	}

	@Override
	public void handle(NetworkEvent.Context context) {
		ScreenManager.openFoodPouchScreen(this.stackedItems, this.containerId);
	}
}
