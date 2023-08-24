package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;
import static com.hexagram2021.fiahi.register.FIAHICapabilities.FOOD_CAPABILITY_ID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventHandler {
	private ForgeEventHandler() {}

	@SubscribeEvent
	public static void onAttackItemStackCapability(AttachCapabilitiesEvent<ItemStack> event) {
		event.addCapability(FOOD_CAPABILITY_ID, new ItemStackFoodHandler(event.getObject()));
	}
}
