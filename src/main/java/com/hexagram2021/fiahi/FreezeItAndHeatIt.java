package com.hexagram2021.fiahi;

import com.hexagram2021.fiahi.common.FIAHIContent;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FreezeItAndHeatIt.MODID)
public class FreezeItAndHeatIt {
	public static final String MODID = "fiahi";

	public FreezeItAndHeatIt() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FIAHICommonConfig.getConfig());

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		FIAHIContent.modConstruct(bus);

		MinecraftForge.EVENT_BUS.register(this);
	}
}
