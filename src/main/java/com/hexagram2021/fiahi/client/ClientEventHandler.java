package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
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

	@SubscribeEvent
	public static void onRenderPlayerView(EntityViewRenderEvent.CameraSetup event) {
		Player player = Minecraft.getInstance().player;
		if (!Minecraft.getInstance().isPaused() && player != null) {
			float frameTime = Minecraft.getInstance().getDeltaFrameTime();

			MobEffectInstance instance = player.getEffect(FIAHIMobEffects.SHIVER.get());
			if(instance != null) {
				float level = instance.getAmplifier() * 0.025F;

				double tickTime = player.tickCount + event.getPartialTicks();
				float shiverAmount = (float) (Math.sin((tickTime) * 3) * level * (10 * frameTime));
				player.setYRot(player.getYRot() + shiverAmount);
			}
		}
	}
}
