package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHICapabilities {
	public static final ResourceLocation FOOD_CAPABILITY_ID = new ResourceLocation(MODID, "food");
	public static final Capability<IFrozenRottenFood> FOOD_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

	public static void register(RegisterCapabilitiesEvent event) {
		event.register(IFrozenRottenFood.class);
	}
}
