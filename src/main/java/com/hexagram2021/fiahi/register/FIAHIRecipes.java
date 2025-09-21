package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.recipe.FoodRottingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIRecipes {
	private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

	public static final DeferredHolder<RecipeType<?>, RecipeType<FoodRottingRecipe>> FOOD_ROTTING = register("food_rotting");

	private FIAHIRecipes() {
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> register(String name) {
		return REGISTER.register(name, () -> new RecipeType<>() {
			@Override
			public String toString() {
				return ResourceLocation.fromNamespaceAndPath(MODID, name).toString();
			}
		});
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
