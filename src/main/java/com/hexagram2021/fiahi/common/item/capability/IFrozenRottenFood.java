package com.hexagram2021.fiahi.common.item.capability;

import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.recipe.FoodRottingRecipe;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.hexagram2021.fiahi.register.FIAHIRecipes;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import static com.hexagram2021.fiahi.common.util.RegistryHelper.getRegistryName;
import static com.hexagram2021.fiahi.register.FIAHIItemTags.LEFTOVERS;

public interface IFrozenRottenFood {
	int FROZEN_ROTTEN_THRESHOLD = 25;	// [-125, 125]
	double EPS = 1e-2D;

	double getTemperature();
	void setTemperature(double newTemperature);

	default double getTemperatureBalanceRate() {
		return FIAHICommonConfig.TEMPERATURE_BALANCE_RATE.get() / 100.0D;
	}

	default void apply(double newTemperature) {
		this.apply(newTemperature, null);
	}

	default void apply(double newTemperature, @Nullable Item item) {
		double temp = this.getTemperature();
		double diff = (newTemperature - temp) * ConfigSettings.TEMP_RATE.get();
		if(diff < 0 && temp < 0) {
			diff *= FIAHICommonConfig.FROZEN_SPEED_MULTIPLIER.get();
		}
		if(diff > 0 && temp > 0) {
			diff *= FIAHICommonConfig.ROTTEN_SPEED_MULTIPLIER.get();
		}
		boolean flag = temp > 0;
		int level = flag ? this.getRottenLevel() : this.getFrozenLevel();
		this.setTemperature(temp + diff * this.getTemperatureBalanceRate());
		boolean newFlag = this.getTemperature() > 0;
		int newLevel = newFlag ? this.getRottenLevel() : this.getFrozenLevel();
		if(newLevel > 3) {
			newLevel = 3;
			this.setTemperature((FROZEN_ROTTEN_THRESHOLD * 5 - EPS) * (flag ? 1 : -1));
		}
		if(level == newLevel && (flag == newFlag || level == 0)) {
			this.updateFoodTag();
			return;
		}
		if(level < newLevel) {
			if(flag == newFlag || level == 0) {
				if(flag && item != null &&
						FIAHICommonConfig.NEVER_ROTTEN_FOODS.get().contains(getRegistryName(item).toString())) {
					this.setTemperature(FROZEN_ROTTEN_THRESHOLD * 2 - EPS);
				} else if(!flag && item != null &&
						FIAHICommonConfig.NEVER_FROZEN_FOODS.get().contains(getRegistryName(item).toString())) {
					this.setTemperature(-FROZEN_ROTTEN_THRESHOLD * 2 + EPS);
				}
				this.updateFoodTag();
				return;
			}
		}
		this.setTemperature((FROZEN_ROTTEN_THRESHOLD * (1 + level) + EPS) * (flag ? 1 : -1));
		this.updateFoodTag();
	}

	default int getFrozenLevel() {
		return getFrozenLevel((int)this.getTemperature());
	}
	default int getRottenLevel() {
		return getRottenLevel((int)this.getTemperature());
	}

	static int getFrozenLevel(int temp) {
		if(!FIAHICommonConfig.ENABLE_FROZEN.get()) {
			return 0;
		}
		return temp >= 0 ? 0 : (-temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}
	static int getRottenLevel(int temp) {
		if(!FIAHICommonConfig.ENABLE_ROTTEN.get()) {
			return 0;
		}
		return temp <= 0 ? 0 : (temp - FROZEN_ROTTEN_THRESHOLD) / FROZEN_ROTTEN_THRESHOLD;
	}

	void foodTick(double temperature, Item item);

	void updateFoodTag();

	static boolean canBeFrozenRotten(ItemStack itemStack) {
		return itemStack.has(DataComponents.FOOD) && !itemStack.is(LEFTOVERS);
	}

	@SuppressWarnings("deprecation")
	static boolean canBeFrozenRotten(Item item) {
		return item.components().has(DataComponents.FOOD) && !item.builtInRegistryHolder().is(LEFTOVERS);
	}

	static void tick(ItemStack food, Consumer<ItemStack> leftOverSetter, ToDoubleFunction<IFrozenRottenFood> temperatureUpdater, ServerLevel serverLevel, @Nullable LivingEntity entity) {
		IFrozenRottenFood c = food.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
		if(c != null) {
			c.foodTick(temperatureUpdater.applyAsDouble(c), food.getItem());
			if(c.getTemperature() > 120) {
				Optional<RecipeHolder<FoodRottingRecipe>> holder = serverLevel.getRecipeManager().getRecipeFor(FIAHIRecipes.FOOD_ROTTING.get(), new SingleRecipeInput(food), serverLevel);
				if(holder.isPresent()) {
					FoodRottingRecipe recipe = holder.get().value();
					leftOverSetter.accept(recipe.assemble(new SingleRecipeInput(food), serverLevel.registryAccess()));
					if(entity instanceof Player player) {
						for(FoodRottingRecipe.RandomResult result: recipe.randomResults()) {
							if(serverLevel.random.nextDouble() < result.possibility()) {
								ItemStack itemStack = result.result().copy();
								itemStack.setCount(food.getCount());
								if (!player.addItem(result.result())) {
									ItemEntity itementity = player.drop(itemStack, false);
									if (itementity != null) {
										itementity.setNoPickUpDelay();
										itementity.setTarget(player.getUUID());
									}
								}
							}
						}
					}
				} else {
					leftOverSetter.accept(new ItemStack(FIAHIItems.LEFTOVER_VEGETABLE));
				}
			}
		}
	}
}
