package com.hexagram2021.fiahi;

import com.hexagram2021.fiahi.common.FIAHIContent;
import com.hexagram2021.fiahi.common.ModVanillaCompat;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.network.ClientboundFoodPouchPacket;
import com.hexagram2021.fiahi.common.network.IFIAHIPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

@Mod(FreezeItAndHeatIt.MODID)
public class FreezeItAndHeatIt {
	public static final String MODID = "fiahi";
	public static final String VERSION = ModList.get().getModFileById(MODID).versionString();

	public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(MODID, "main"))
			.networkProtocolVersion(() -> VERSION)
			.serverAcceptedVersions(VERSION::equals)
			.clientAcceptedVersions(VERSION::equals)
			.simpleChannel();

	public FreezeItAndHeatIt() {
		System.getProperties().put("production", true);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FIAHICommonConfig.getConfig());

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		FIAHIContent.modConstruct(bus);

		bus.addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(ModVanillaCompat::setup);
		registerMessage(ClientboundFoodPouchPacket.class, ClientboundFoodPouchPacket::new);
	}

	private static int messageId = 0;
	@SuppressWarnings("SameParameterValue")
	private static <T extends IFIAHIPacket> void registerMessage(Class<T> packetType, Function<FriendlyByteBuf, T> constructor) {
		packetHandler.registerMessage(messageId++, packetType, IFIAHIPacket::write, constructor, (packet, ctx) -> packet.handle(ctx.get()));
	}
}
