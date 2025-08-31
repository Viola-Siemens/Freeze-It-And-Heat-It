package com.hexagram2021.fiahi;

import com.hexagram2021.fiahi.common.FIAHIContent;
import com.hexagram2021.fiahi.common.ModVanillaCompat;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodDataTypes;
import com.hexagram2021.fiahi.register.FIAHICustomPayloadTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(FreezeItAndHeatIt.MODID)
public class FreezeItAndHeatIt {
	public static final String MODID = "fiahi";

	public FreezeItAndHeatIt(IEventBus modBus, ModContainer modContainer) {
		System.getProperties().put("production", true);
		modContainer.registerConfig(ModConfig.Type.COMMON, FIAHICommonConfig.getConfig());

		FIAHIContent.modConstruct(modBus);

		modBus.addListener(this::setup);
		modBus.addListener(this::registerNetworkHandlers);
		modBus.addListener(EventPriority.LOWEST, this::loadRegistry);
		NeoForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(ModVanillaCompat::setup);
	}

	private void loadRegistry(final FMLCommonSetupEvent event) {
		event.enqueueWork(PouchedFoodDataTypes::init);
	}

	private void registerNetworkHandlers(final RegisterPayloadHandlersEvent event) {
		FIAHICustomPayloadTypes.V1.register(event.registrar("1"));
	}
}
