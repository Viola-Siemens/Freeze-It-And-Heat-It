package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.client.particle.BreatheOutParticle;
import com.hexagram2021.fiahi.client.screen.FoodPouchScreen;
import com.hexagram2021.fiahi.client.util.ModelBakeryUtils;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.hexagram2021.fiahi.register.FIAHIMenuTypes;
import com.hexagram2021.fiahi.register.FIAHIParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onModelBake(ModelBakeEvent event) {
		FIAHILogger.info("FIAHI: Adding frozen and rotten sprites...");
		final ModelBakery modelBakery = event.getModelLoader();
		final Map<ResourceLocation, UnbakedModel> topLevelModels = modelBakery.topLevelModels;
		final Map<ResourceLocation, BakedModel> bakedTopLevelModels = modelBakery.getBakedTopLevelModels();
		final AtlasSet atlasSet = modelBakery.getSpriteMap();
		Function<Integer, Function<Material, TextureAtlasSprite>> frozenSprite = level -> material -> atlasSet.getSprite(new Material(
				material.atlasLocation(),
				new ResourceLocation(material.texture().getNamespace(), material.texture().getPath().concat(".frozen.%d".formatted(level)))
		));
		Function<Integer, Function<Material, TextureAtlasSprite>> rottenSprite = level -> material -> atlasSet.getSprite(new Material(
				material.atlasLocation(),
				new ResourceLocation(material.texture().getNamespace(), material.texture().getPath().concat(".rotten.%d".formatted(level)))
		));
		topLevelModels.forEach(((spriteId, unbakedModel) -> {
			if (unbakedModel instanceof BlockModel && ((BlockModel) unbakedModel).getRootModel() == ModelBakery.GENERATION_MARKER) {
				try {
					UnbakedModel model = modelBakery.getModel(spriteId);
					if (model instanceof BlockModel blockmodel) {
						if (blockmodel.getRootModel() == ModelBakery.GENERATION_MARKER) {
							ModelBakeryUtils.putBakedModel(
									bakedTopLevelModels, spriteId,
									ModelBakeryUtils.getBakedBlockModel(modelBakery, frozenSprite.apply(1), blockmodel, spriteId),
									ModelBakeryUtils.getBakedBlockModel(modelBakery, frozenSprite.apply(2), blockmodel, spriteId),
									ModelBakeryUtils.getBakedBlockModel(modelBakery, frozenSprite.apply(3), blockmodel, spriteId),
									ModelBakeryUtils.getBakedBlockModel(modelBakery, rottenSprite.apply(1), blockmodel, spriteId),
									ModelBakeryUtils.getBakedBlockModel(modelBakery, rottenSprite.apply(2), blockmodel, spriteId),
									ModelBakeryUtils.getBakedBlockModel(modelBakery, rottenSprite.apply(3), blockmodel, spriteId)
							);
						}
					} else {
						ModelBakeryUtils.putBakedModel(
								bakedTopLevelModels, spriteId,
								ModelBakeryUtils.getBakedItemModel(modelBakery, frozenSprite.apply(1), model, spriteId),
								ModelBakeryUtils.getBakedItemModel(modelBakery, frozenSprite.apply(2), model, spriteId),
								ModelBakeryUtils.getBakedItemModel(modelBakery, frozenSprite.apply(3), model, spriteId),
								ModelBakeryUtils.getBakedItemModel(modelBakery, rottenSprite.apply(1), model, spriteId),
								ModelBakeryUtils.getBakedItemModel(modelBakery, rottenSprite.apply(2), model, spriteId),
								ModelBakeryUtils.getBakedItemModel(modelBakery, rottenSprite.apply(3), model, spriteId)
						);
					}
				} catch (Exception exception) {
					FIAHILogger.error("Unable to bake model: '%s': ".formatted(spriteId), exception);
				}
			}
		}));
	}

	@SubscribeEvent
	public static void registerParticleProviders(ParticleFactoryRegisterEvent event) {
		ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
		particleEngine.register(FIAHIParticleTypes.BREATHE_OUT.get(), BreatheOutParticle.Provider::new);
	}
}
