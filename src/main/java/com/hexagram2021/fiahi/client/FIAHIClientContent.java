package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.client.particle.BreatheOutParticle;
import com.hexagram2021.fiahi.client.screen.FoodPouchScreen;
import com.hexagram2021.fiahi.register.FIAHIMenuTypes;
import com.hexagram2021.fiahi.register.FIAHIParticleTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import javax.annotation.Nullable;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
public class FIAHIClientContent {
	public static final ResourceLocation FROZEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "misc/frozen");
	public static final ResourceLocation ROTTEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "misc/rotten");

	@Nullable
	public static TextureAtlasSprite FROZEN_SPRITE;
	@Nullable
	public static TextureAtlasSprite ROTTEN_SPRITE;

	@SubscribeEvent
	public static void registerContainersAndScreens(RegisterMenuScreensEvent event) {
		event.register(FIAHIMenuTypes.FOOD_POUCH_MENU.get(), FoodPouchScreen::new);
	}

	@SubscribeEvent
	public static void afterTextureAtlasReload(TextureAtlasStitchedEvent event) {
		FROZEN_SPRITE = event.getAtlas().getSprite(FROZEN_TEXTURE);
		ROTTEN_SPRITE = event.getAtlas().getSprite(ROTTEN_TEXTURE);
	}

	@SubscribeEvent
	public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(FIAHIParticleTypes.BREATHE_OUT.get(), BreatheOutParticle.Provider::new);
	}
}
