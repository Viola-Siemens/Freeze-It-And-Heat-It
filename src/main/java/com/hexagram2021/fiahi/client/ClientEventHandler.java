package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.register.FIAHICapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
	@SubscribeEvent
	public static void onToolTipShow(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if(itemStack.isEdible()) {
			itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
				Component status = new TranslatableComponent("item.fiahi.temperature.normal");
				if(c.getFrozenLevel() > 0) {
					status = new TranslatableComponent("item.fiahi.temperature.frozen.%d".formatted(Mth.clamp(c.getFrozenLevel(), 0, 3)));
				}
				if(c.getRottenLevel() > 0) {
					status = new TranslatableComponent("item.fiahi.temperature.rotten.%d".formatted(Mth.clamp(c.getRottenLevel(), 0, 3)));
				}
				event.getToolTip().add(status);
				if(Minecraft.getInstance().options.advancedItemTooltips) {
					event.getToolTip().add(new TranslatableComponent("item.fiahi.temperature.description", (int)c.getTemperature()));
				}
			});
		}
	}
}
