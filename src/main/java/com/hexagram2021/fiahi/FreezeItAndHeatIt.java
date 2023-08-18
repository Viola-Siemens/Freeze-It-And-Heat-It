package com.hexagram2021.fiahi;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(FreezeItAndHeatIt.MODID)
public class FreezeItAndHeatIt {
    public static final String MODID = "fiahi";

    public FreezeItAndHeatIt() {

        MinecraftForge.EVENT_BUS.register(this);
    }
}
