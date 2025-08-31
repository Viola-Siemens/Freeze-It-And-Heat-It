package com.hexagram2021.fiahi.mixin;

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
	private Map<ModelResourceLocation, UnbakedModel> topLevelModels;

	@Shadow @Final
	private Map<ModelResourceLocation, BakedModel> bakedTopLevelModels;

	@Shadow @Final
	private Map<ResourceLocation, UnbakedModel> unbakedCache;

	@Shadow @Final
	private UnbakedModel missingModel;

	@Inject(method = "bakeModels", at = @At(value = "RETURN"))
	public void fiahi$uploadTextures(ModelBakery.TextureGetter spriteGetter, CallbackInfo ci) {
		BiFunction<ModelResourceLocation, String, Function<Material, TextureAtlasSprite>> spriteMapper = (spriteId, suffix) -> material -> {
			TextureAtlasSprite sprite = spriteGetter.get(spriteId, new Material(
					material.atlasLocation(),
					material.texture().withSuffix(suffix)
			));
			return sprite.contents().name().equals(ModelBakery.MISSING_MODEL_LOCATION) ?
					spriteGetter.get(spriteId, material) : sprite;
		};
		this.topLevelModels.forEach(((spriteId, unbakedModel) -> {
			if (unbakedModel instanceof BlockModel && ((BlockModel) unbakedModel).getRootModel() == ModelBakery.GENERATION_MARKER) {
				this.fiahi$putBakedModel(
						spriteId,
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".frozen.1", spriteId),
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".frozen.2", spriteId),
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".frozen.3", spriteId),
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".rotten.1", spriteId),
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".rotten.2", spriteId),
						this.fiahi$bakeModel(spriteMapper, unbakedModel, ".rotten.3", spriteId)
				);
			}
		}));
	}

	@Unique
	private BakedModel fiahi$bakeModel(BiFunction<ModelResourceLocation, String, Function<Material, TextureAtlasSprite>> spriteMapper,
									   UnbakedModel unbakedModel, String suffix, ModelResourceLocation spriteId) {
		return Objects.requireNonNull(new FIAHIModelBaker(
				this.unbakedCache,
				this.topLevelModels,
				this.missingModel,
				spriteMapper.apply(spriteId, suffix),
				suffix
		).bakeUncached(unbakedModel));
	}

	@Unique
	private void fiahi$putBakedModel(ModelResourceLocation spriteId, BakedModel frozen1, BakedModel frozen2, BakedModel frozen3, BakedModel rotten1, BakedModel rotten2, BakedModel rotten3) {
		this.bakedTopLevelModels.put(spriteId, new FIAHIBakedModel(this.bakedTopLevelModels.get(spriteId), frozen1, frozen2, frozen3, rotten1, rotten2, rotten3));
	}
}
