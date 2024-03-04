package com.hexagram2021.fiahi.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IFIAHIPacket {
	void write(FriendlyByteBuf buf);
	void handle(NetworkEvent.Context context);
}
