package com.hexagram2021.fiahi.common.recipe;

import com.google.common.collect.ImmutableList;
import com.hexagram2021.fiahi.register.FIAHIRecipeSerializers;
import com.hexagram2021.fiahi.register.FIAHIRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record FoodRottingRecipe(Ingredient ingredient, ItemStack result, List<RandomResult> randomResults) implements Recipe<SingleRecipeInput> {
	@Override
	public RecipeType<?> getType() {
		return FIAHIRecipes.FOOD_ROTTING.get();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FIAHIRecipeSerializers.FOOD_ROTTING.get();
	}

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return this.ingredient.test(input.item());
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider pRegistries) {
		return this.result;
	}

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(Items.ROTTEN_FLESH);
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return true;
	}

	public ItemStack assemble(SingleRecipeInput pInput, HolderLookup.Provider pRegistries) {
		return this.result.copy();
	}

	public static class Serializer implements RecipeSerializer<FoodRottingRecipe> {
		private static final MapCodec<FoodRottingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FoodRottingRecipe::ingredient),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FoodRottingRecipe::result),
				RandomResult.CODEC.listOf().optionalFieldOf("random_results", ImmutableList.of()).forGetter(FoodRottingRecipe::randomResults)
		).apply(instance, FoodRottingRecipe::new));
		private static final StreamCodec<RegistryFriendlyByteBuf, FoodRottingRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC,
				FoodRottingRecipe::ingredient,
				ItemStack.STREAM_CODEC,
				FoodRottingRecipe::result,
				RandomResult.STREAM_CODEC.apply(ByteBufCodecs.list()),
				FoodRottingRecipe::randomResults,
				FoodRottingRecipe::new
		);

		@Override
		public MapCodec<FoodRottingRecipe> codec() {
			return CODEC;
		}
		@Override
		public StreamCodec<RegistryFriendlyByteBuf, FoodRottingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public record RandomResult(ItemStack result, double possibility) {
		public static final Codec<RandomResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.CODEC.fieldOf("result").forGetter(RandomResult::result),
				Codec.DOUBLE.fieldOf("possibility").forGetter(RandomResult::possibility)
		).apply(instance, RandomResult::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, RandomResult> STREAM_CODEC = StreamCodec.composite(
				ItemStack.STREAM_CODEC,
				RandomResult::result,
				ByteBufCodecs.DOUBLE,
				RandomResult::possibility,
				RandomResult::new
		);
	}
}
