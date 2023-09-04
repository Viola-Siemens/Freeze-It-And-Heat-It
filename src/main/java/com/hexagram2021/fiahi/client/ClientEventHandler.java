package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
			CompoundTag nbt = itemStack.getTag();
			int temp = 0;
			if(nbt != null && nbt.contains(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, Tag.TAG_ANY_NUMERIC)) {
				temp = (int)nbt.getDouble(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE);
			}
			Component status = new TranslatableComponent("item.fiahi.temperature.normal").withStyle(ChatFormatting.GRAY);
			int frozenLevel = IFrozenRottenFood.getFrozenLevel(temp);
			int rottenLevel = IFrozenRottenFood.getRottenLevel(temp);
			if(frozenLevel > 0) {
				status = new TranslatableComponent("item.fiahi.temperature.frozen.%d".formatted(Mth.clamp(frozenLevel, 0, 3))).withStyle(ChatFormatting.DARK_AQUA);
			}
			if(rottenLevel > 0) {
				status = new TranslatableComponent("item.fiahi.temperature.rotten.%d".formatted(Mth.clamp(rottenLevel, 0, 3))).withStyle(ChatFormatting.DARK_RED);
			}
			event.getToolTip().add(status);
			if(Minecraft.getInstance().options.advancedItemTooltips) {
				event.getToolTip().add(new TranslatableComponent("item.fiahi.temperature.description", temp));
			}
		} else if(itemStack.is(FIAHIItems.FOOD_POUCH.get()) && Minecraft.getInstance().options.advancedItemTooltips) {
			CompoundTag nbt = itemStack.getTag();
			if(nbt != null && nbt.contains("temperature", Tag.TAG_ANY_NUMERIC)) {
				event.getToolTip().add(new TranslatableComponent("gui.fiahi.count.description", FoodPouchItem.getItemCount(nbt)));
				event.getToolTip().add(new TranslatableComponent("item.fiahi.temperature.description", (int)nbt.getDouble("temperature")));
			}
		}
	}

	@SubscribeEvent
	public static void onRenderPlayerView(EntityViewRenderEvent.CameraSetup event) {
		Player player = Minecraft.getInstance().player;
		if (!Minecraft.getInstance().isPaused() && player != null) {
			float frameTime = Minecraft.getInstance().getDeltaFrameTime();

			MobEffectInstance instance = player.getEffect(FIAHIMobEffects.SHIVER.get());
			if(instance != null) {
				float level = instance.getAmplifier() / 30.0F;

				double tickTime = player.tickCount + event.getPartialTicks();
				float shiverAmount = (float) (Math.sin((tickTime) * 3) * level * (10 * frameTime));
				player.setYRot(player.getYRot() + shiverAmount);
			}
		}
	}
}
