package com.hexagram2021.fiahi.client.util;

import com.hexagram2021.fiahi.client.model.FIAHIBakedModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ModelBakeryUtils {
	private ModelBakeryUtils() {}

	public static BakedModel getBakedBlockModel(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteFunction, BlockModel blockmodel, ResourceLocation spriteId) {
		return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(spriteFunction, blockmodel).bake(modelBakery, blockmodel, spriteFunction, BlockModelRotation.X0_Y0, spriteId, false);
	}

	public static BakedModel getBakedItemModel(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteFunction, UnbakedModel unbakedModel, ResourceLocation spriteId) {
		return Objects.requireNonNull(unbakedModel.bake(modelBakery, spriteFunction, BlockModelRotation.X0_Y0, spriteId));
	}

	public static void putBakedModel(Map<ResourceLocation, BakedModel> bakedTopLevelModels, ResourceLocation spriteId, BakedModel frozen1, BakedModel frozen2, BakedModel frozen3, BakedModel rotten1, BakedModel rotten2, BakedModel rotten3) {
		bakedTopLevelModels.put(spriteId, new FIAHIBakedModel(bakedTopLevelModels.get(spriteId), frozen1, frozen2, frozen3, rotten1, rotten2, rotten3));
	}
}
