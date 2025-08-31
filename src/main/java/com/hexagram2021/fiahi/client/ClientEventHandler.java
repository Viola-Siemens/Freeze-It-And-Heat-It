package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;
import static com.hexagram2021.fiahi.common.util.RegistryHelper.getRegistryName;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientEventHandler {
	@SubscribeEvent
	public static void onToolTipShow(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if(IFrozenRottenFood.canBeFrozenRotten(itemStack)) {
			String foodId = getRegistryName(itemStack.getItem()).toString();
			if(FIAHICommonConfig.NEVER_ROTTEN_FOODS.get().contains(foodId) && FIAHICommonConfig.NEVER_FROZEN_FOODS.get().contains(foodId)) {
				return;
			}
			Integer temperatureAttachment = itemStack.get(FIAHIAttachmentTypes.FOOD_TEMPERATURE);
			int temp = 0;
			if(temperatureAttachment != null) {
				temp = temperatureAttachment;
			}
			Component status = Component.translatable("item.fiahi.temperature.normal").withStyle(ChatFormatting.GRAY);
			int frozenLevel = IFrozenRottenFood.getFrozenLevel(temp);
			int rottenLevel = IFrozenRottenFood.getRottenLevel(temp);
			if(frozenLevel > 0) {
				status = Component.translatable("item.fiahi.temperature.frozen.%d".formatted(Mth.clamp(frozenLevel, 0, 3))).withStyle(ChatFormatting.DARK_AQUA);
			}
			if(rottenLevel > 0) {
				status = Component.translatable("item.fiahi.temperature.rotten.%d".formatted(Mth.clamp(rottenLevel, 0, 3))).withStyle(ChatFormatting.DARK_RED);
			}
			event.getToolTip().add(status);
			if(Minecraft.getInstance().options.advancedItemTooltips) {
				event.getToolTip().add(Component.translatable("item.fiahi.temperature.description", temp));
			}
		} else if(itemStack.is(FIAHIItems.FOOD_POUCH.get()) && Minecraft.getInstance().options.advancedItemTooltips) {
			FoodPouchData foodPouchData = itemStack.get(FIAHIAttachmentTypes.FOOD_POUCH_DATA);
			if(foodPouchData != null) {
				event.getToolTip().add(Component.translatable("gui.fiahi.count.description", FoodPouchItem.getItemCount(foodPouchData)));
				event.getToolTip().add(Component.translatable("item.fiahi.temperature.description", (int)foodPouchData.temperature()));
			}
		}
	}

	@SubscribeEvent
	public static void onRenderPlayerView(ViewportEvent.ComputeCameraAngles event) {
		Player player = Minecraft.getInstance().player;
		if (!Minecraft.getInstance().isPaused() && player != null) {
			float frameTime = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();

			MobEffectInstance instance = player.getEffect(FIAHIMobEffects.SHIVER);
			if(instance != null) {
				float level = instance.getAmplifier() / 30.0F;

				double tickTime = player.tickCount + event.getPartialTick();
				float shiverAmount = (float) (Math.sin((tickTime) * 3) * level * (10 * frameTime));
				player.setYRot(player.getYRot() + shiverAmount);
			}
		}
	}
}
