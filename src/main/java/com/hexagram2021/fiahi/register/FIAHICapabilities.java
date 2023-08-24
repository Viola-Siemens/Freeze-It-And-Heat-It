package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class FIAHICapabilities {
	public static final Capability<IFrozenRottenFood> FOOD_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}
