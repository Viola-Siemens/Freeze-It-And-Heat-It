package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.recipe.FoodRottingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIRecipeSerializers {
	private static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

	public static final DeferredHolder<RecipeSerializer<?>, FoodRottingRecipe.Serializer> FOOD_ROTTING = REGISTER.register("food_rotting", FoodRottingRecipe.Serializer::new);

	private FIAHIRecipeSerializers() {
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
