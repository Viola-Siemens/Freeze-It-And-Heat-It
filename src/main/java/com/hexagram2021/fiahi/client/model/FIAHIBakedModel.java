package com.hexagram2021.fiahi.client.model;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public record FIAHIBakedModel(BakedModel original, BakedModel frozen1, BakedModel frozen2, BakedModel frozen3, BakedModel rotten1, BakedModel rotten2, BakedModel rotten3) implements BakedModel {
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
		return this.original.getQuads(state, direction, random);
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return this.original.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.original.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.original.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.original.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.original.getParticleIcon();
	}

	@Override
	public ItemOverrides getOverrides() {
		return new ItemOverrides() {
			@Override @Nullable
			public BakedModel resolve(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int seed) {
				BakedModel ret = FIAHIBakedModel.this.getTemperatureEffectBakedModel(itemStack);
				return ret.getOverrides().resolve(ret, itemStack, clientLevel, livingEntity, seed);
			}
		};
	}

	@Override
	public ItemTransforms getTransforms() {
		return FIAHIBakedModel.this.original.getTransforms();
	}

	private BakedModel getTemperatureEffectBakedModel(ItemStack itemStack) {
		if(!IFrozenRottenFood.canBeFrozenRotten(itemStack)) {
			return this.original;
		}
		CompoundTag nbt = itemStack.getTag();
		if(nbt == null || !nbt.contains(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, Tag.TAG_ANY_NUMERIC)) {
			return this.original;
		}
		int temp = (int)nbt.getDouble(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE);
		int frozenLevel = IFrozenRottenFood.getFrozenLevel(temp);
		int rottenLevel = IFrozenRottenFood.getRottenLevel(temp);
		if(frozenLevel > 0) {
			return switch (frozenLevel) {
				case 1 -> this.frozen1;
				case 2 -> this.frozen2;
				case 3 -> this.frozen3;
				default -> this.original;
			};
		}
		if(rottenLevel > 0) {
			return switch (rottenLevel) {
				case 1 -> this.rotten1;
				case 2 -> this.rotten2;
				case 3 -> this.rotten3;
				default -> this.original;
			};
		}
		return this.original;
	}
}
