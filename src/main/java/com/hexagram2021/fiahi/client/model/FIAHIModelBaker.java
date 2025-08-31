package com.hexagram2021.fiahi.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public record FIAHIModelBaker(Map<ResourceLocation, UnbakedModel> models, Map<ModelResourceLocation, UnbakedModel> topLevelModels, UnbakedModel missingModel, Function<Material, TextureAtlasSprite> spriteGetter, String suffix) implements ModelBaker {
	@Override
	public UnbakedModel getModel(ResourceLocation location) {
		return this.models.getOrDefault(location, this.missingModel);
	}
	
	@Override @Nullable
	public BakedModel bake(ResourceLocation location, ModelState modelState) {
		return this.bake(location, modelState, this.getModelTextureGetter());
	}

	@Override @Nullable
	public UnbakedModel getTopLevelModel(ModelResourceLocation location) {
		return this.topLevelModels.getOrDefault(location, missingModel);
	}

	@Override @Nullable
	public BakedModel bake(ResourceLocation location, ModelState modelState, Function<Material, TextureAtlasSprite> spriteGetter) {
		return this.bakeUncached(this.getModel(location), modelState, spriteGetter);
	}

	@Nullable
	public BakedModel bakeUncached(UnbakedModel model) {
		return this.bakeUncached(model, BlockModelRotation.X0_Y0, this.spriteGetter);
	}

	@Override @Nullable
	public BakedModel bakeUncached(UnbakedModel model, ModelState modelState, Function<Material, TextureAtlasSprite> sprites) {
		if (model instanceof BlockModel blockModel) {
			return new ItemModelGenerator().generateBlockModel(spriteGetter, blockModel).bake(
					this,
					blockModel,
					spriteGetter,
					modelState,
					false
			);
		}
		return model.bake(this, spriteGetter, modelState);
	}

	@Override
	public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
		return this.spriteGetter;
	}
}
