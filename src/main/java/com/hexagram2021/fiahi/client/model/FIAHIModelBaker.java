package com.hexagram2021.fiahi.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public record FIAHIModelBaker(Map<ResourceLocation, UnbakedModel> models, UnbakedModel missingModel, Function<Material, TextureAtlasSprite> spriteGetter, String suffix) implements ModelBaker {
	@Override
	public UnbakedModel getModel(ResourceLocation location) {
		return this.models.getOrDefault(location, this.missingModel);
	}
	
	@Override @Nullable
	public BakedModel bake(ResourceLocation location, ModelState modelState) {
		return this.bake(location, modelState, getModelTextureGetter());
	}
	
	@Override @Nullable
	public BakedModel bake(ResourceLocation location, ModelState modelState, Function<Material, TextureAtlasSprite> spriteGetter) {
		UnbakedModel model = this.getModel(location);
		
		return this.bake(location, model, modelState, spriteGetter);
	}
	
	@Nullable
	public BakedModel bake(ResourceLocation location, UnbakedModel model, ModelState modelState, Function<Material, TextureAtlasSprite> spriteGetter) {
		if (model instanceof BlockModel blockModel) {
			return new ItemModelGenerator().generateBlockModel(spriteGetter, blockModel).bake(
					this,
					blockModel,
					spriteGetter,
					modelState,
					getModifiedLocation(location, this.suffix),
					false
			);
		}
		return model.bake(this, spriteGetter, modelState, getModifiedLocation(location, this.suffix));
	}
	
	@Override
	public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
		return this.spriteGetter;
	}

	public static ResourceLocation getModifiedLocation(ResourceLocation location, String suffix) {
		return location instanceof ModelResourceLocation modelLocation ?
				new ModelResourceLocation(location.withSuffix(suffix), modelLocation.getVariant()) : location.withSuffix(suffix);
	}
}
