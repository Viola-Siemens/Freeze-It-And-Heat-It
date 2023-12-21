package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.client.model.FIAHIBakedModel;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Shadow @Final
	private Map<ResourceLocation, UnbakedModel> topLevelModels;

	@Shadow @Nullable
	private AtlasSet atlasSet;

	@Shadow
	public abstract UnbakedModel getModel(ResourceLocation spriteId);

	@Shadow @Final
	private static ItemModelGenerator ITEM_MODEL_GENERATOR;

	@Shadow @Final
	private Map<ResourceLocation, BakedModel> bakedTopLevelModels;

	@SuppressWarnings({"DataFlowIssue"})
	@Inject(method = "uploadTextures", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
	public void fiahi$uploadTextures(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir) {
		profilerFiller.push("bakingFrozenRotten");
		Function<Integer, Function<Material, TextureAtlasSprite>> frozenSprite = level -> material -> this.atlasSet.getSprite(new Material(
				material.atlasLocation(),
				new ResourceLocation(material.texture().getNamespace(), material.texture().getPath().concat(".frozen.%d".formatted(level)))
		));
		Function<Integer, Function<Material, TextureAtlasSprite>> rottenSprite = level -> material -> this.atlasSet.getSprite(new Material(
				material.atlasLocation(),
				new ResourceLocation(material.texture().getNamespace(), material.texture().getPath().concat(".rotten.%d".formatted(level)))
		));
		this.topLevelModels.forEach(((spriteId, unbakedModel) -> {
			if (unbakedModel instanceof BlockModel && ((BlockModel) unbakedModel).getRootModel() == ModelBakery.GENERATION_MARKER) {
				try {
					UnbakedModel model = this.getModel(spriteId);
					if (model instanceof BlockModel blockmodel) {
						if (blockmodel.getRootModel() == ModelBakery.GENERATION_MARKER) {
							this.fiahi$putBakedModel(
									spriteId,
									this.fiahi$getBakedBlockModel(frozenSprite.apply(1), blockmodel, spriteId),
									this.fiahi$getBakedBlockModel(frozenSprite.apply(2), blockmodel, spriteId),
									this.fiahi$getBakedBlockModel(frozenSprite.apply(3), blockmodel, spriteId),
									this.fiahi$getBakedBlockModel(rottenSprite.apply(1), blockmodel, spriteId),
									this.fiahi$getBakedBlockModel(rottenSprite.apply(2), blockmodel, spriteId),
									this.fiahi$getBakedBlockModel(rottenSprite.apply(3), blockmodel, spriteId)
							);
						}
					} else {
						this.fiahi$putBakedModel(
							spriteId,
							this.fiahi$getBakedItemModel(frozenSprite.apply(1), model, spriteId),
							this.fiahi$getBakedItemModel(frozenSprite.apply(2), model, spriteId),
							this.fiahi$getBakedItemModel(frozenSprite.apply(3), model, spriteId),
							this.fiahi$getBakedItemModel(rottenSprite.apply(1), model, spriteId),
							this.fiahi$getBakedItemModel(rottenSprite.apply(2), model, spriteId),
							this.fiahi$getBakedItemModel(rottenSprite.apply(3), model, spriteId)
						);
					}
				} catch (Exception exception) {
					FIAHILogger.error("Unable to bake model: '%s': ".formatted(spriteId), exception);
				}
			}
		}));
		profilerFiller.pop();
	}

	@Unique
	private BakedModel fiahi$getBakedBlockModel(Function<Material, TextureAtlasSprite> spriteFunction, BlockModel blockmodel, ResourceLocation spriteId) {
		return ITEM_MODEL_GENERATOR.generateBlockModel(spriteFunction, blockmodel).bake((ModelBakery)(Object) this, blockmodel, spriteFunction, BlockModelRotation.X0_Y0, spriteId, false);
	}

	@Unique
	private BakedModel fiahi$getBakedItemModel(Function<Material, TextureAtlasSprite> spriteFunction, UnbakedModel unbakedModel, ResourceLocation spriteId) {
		return Objects.requireNonNull(unbakedModel.bake((ModelBakery) (Object) this, spriteFunction, BlockModelRotation.X0_Y0, spriteId));
	}

	@Unique
	private void fiahi$putBakedModel(ResourceLocation spriteId, BakedModel frozen1, BakedModel frozen2, BakedModel frozen3, BakedModel rotten1, BakedModel rotten2, BakedModel rotten3) {
		this.bakedTopLevelModels.put(spriteId, new FIAHIBakedModel(this.bakedTopLevelModels.get(spriteId), frozen1, frozen2, frozen3, rotten1, rotten2, rotten3));
	}
}
