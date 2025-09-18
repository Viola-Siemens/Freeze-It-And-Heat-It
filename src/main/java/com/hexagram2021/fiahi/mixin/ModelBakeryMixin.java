package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.client.model.FIAHIBakedModel;
import com.hexagram2021.fiahi.client.model.FIAHIModelBaker;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Shadow @Final
	private Map<ResourceLocation, UnbakedModel> topLevelModels;

	@Shadow @Final
	private Map<ResourceLocation, BakedModel> bakedTopLevelModels;

	@Shadow @Final
	private Map<ResourceLocation, UnbakedModel> unbakedCache;

	@Inject(method = "bakeModels", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	public void fiahi$uploadTextures(BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteGetter, CallbackInfo ci) {
		BiFunction<ResourceLocation, String, Function<Material, TextureAtlasSprite>> spriteMapper = (spriteId, suffix) -> material -> {
			TextureAtlasSprite sprite = spriteGetter.apply(spriteId, new Material(
					material.atlasLocation(),
					material.texture().withSuffix(suffix)
			));
			return sprite.contents().name().equals(ModelBakery.MISSING_MODEL_LOCATION) ?
					spriteGetter.apply(spriteId, material) : sprite;
		};
		this.topLevelModels.forEach(((spriteId, unbakedModel) -> {
			Boolean skip_frozen = false;
			Boolean skip_rotten = false;
			if (!spriteId.getPath().matches("^$")) {
				String resource = (spriteId.getNamespace() + ":" + spriteId.getPath());
				for (String entry : FIAHICommonConfig.NEVER_FROZEN_FOODS.get()) {
					if (resource.matches(entry)) {
						skip_frozen = true;
					}
				}
				for (String entry : FIAHICommonConfig.NEVER_ROTTEN_FOODS.get()) {
					if (resource.matches(entry)) {
						skip_rotten = true;
					}
				}
			}
			if (unbakedModel instanceof BlockModel && ((BlockModel) unbakedModel).getRootModel() == ModelBakery.GENERATION_MARKER) {
				this.fiahi$putBakedModel(
						spriteId,
						this.fiahi$bakeModel(spriteMapper, skip_frozen ? "" : ".frozen.1", spriteId),
						this.fiahi$bakeModel(spriteMapper, skip_frozen ? "" : ".frozen.2", spriteId),
						this.fiahi$bakeModel(spriteMapper, skip_frozen ? "" : ".frozen.3", spriteId),
						this.fiahi$bakeModel(spriteMapper, skip_rotten ? "" : ".rotten.1", spriteId),
						this.fiahi$bakeModel(spriteMapper, skip_rotten ? "" : ".rotten.2", spriteId),
						this.fiahi$bakeModel(spriteMapper, skip_rotten ? "" : ".rotten.3", spriteId)
				);
			}
		}));
	}

	@Unique
	private BakedModel fiahi$bakeModel(BiFunction<ResourceLocation, String, Function<Material, TextureAtlasSprite>> spriteMapper, String suffix, ResourceLocation spriteId) {
		return Objects.requireNonNull(new FIAHIModelBaker(
				this.unbakedCache,
				this.topLevelModels.get(ModelBakery.MISSING_MODEL_LOCATION),
				spriteMapper.apply(spriteId, suffix),
				suffix
		).bake(spriteId, BlockModelRotation.X0_Y0));
	}

	@Unique
	private void fiahi$putBakedModel(ResourceLocation spriteId, BakedModel frozen1, BakedModel frozen2, BakedModel frozen3, BakedModel rotten1, BakedModel rotten2, BakedModel rotten3) {
		this.bakedTopLevelModels.put(spriteId, new FIAHIBakedModel(this.bakedTopLevelModels.get(spriteId), frozen1, frozen2, frozen3, rotten1, rotten2, rotten3));
	}
}
