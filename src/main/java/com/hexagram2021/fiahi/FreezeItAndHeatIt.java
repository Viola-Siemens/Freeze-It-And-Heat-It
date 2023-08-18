package com.hexagram2021.fiahi;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(FreezeItAndHeatIt.MODID)
public class FreezeItAndHeatIt {
	public static final String MODID = "fiahi";

	public FreezeItAndHeatIt() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FIAHICommonConfig.getConfig());

		MinecraftForge.EVENT_BUS.register(this);
	}
}
