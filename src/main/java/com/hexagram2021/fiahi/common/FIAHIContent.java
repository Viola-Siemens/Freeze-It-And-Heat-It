package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.register.*;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FIAHIContent {
	private FIAHIContent() {}

	public static void modConstruct(IEventBus bus) {
		FIAHICreativeModeTabs.init(bus);
		FIAHIItems.init(bus);
		FIAHIMenuTypes.init(bus);
		FIAHIMobEffects.init(bus);
	}

	@SubscribeEvent
	public static void onRegisterCapability(RegisterCapabilitiesEvent event) {
		FIAHICapabilities.register(event);
	}
}
