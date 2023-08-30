package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.client.screen.FoodPouchScreen;
import com.hexagram2021.fiahi.register.FIAHIMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FIAHIClientContent {
	public static final ResourceLocation FROZEN_TEXTURE = new ResourceLocation(MODID, "misc/frozen");
	public static final ResourceLocation ROTTEN_TEXTURE = new ResourceLocation(MODID, "misc/rotten");

	@Nullable
	public static TextureAtlasSprite FROZEN_SPRITE;
	@Nullable
	public static TextureAtlasSprite ROTTEN_SPRITE;

	@SubscribeEvent
	public static void setup(final FMLClientSetupEvent event) {
		event.enqueueWork(FIAHIClientContent::registerContainersAndScreens);
	}

	private static void registerContainersAndScreens() {
		MenuScreens.register(FIAHIMenuTypes.FOOD_POUCH_MENU.get(), FoodPouchScreen::new);
	}

	@SubscribeEvent
	public static void onTextureAtlasReload(TextureStitchEvent.Pre event) {
		event.addSprite(FROZEN_TEXTURE);
		event.addSprite(ROTTEN_TEXTURE);
	}

	@SubscribeEvent
	public static void afterTextureAtlasReload(TextureStitchEvent.Post event) {
		FROZEN_SPRITE = event.getAtlas().getSprite(FROZEN_TEXTURE);
		ROTTEN_SPRITE = event.getAtlas().getSprite(ROTTEN_TEXTURE);
	}
}
