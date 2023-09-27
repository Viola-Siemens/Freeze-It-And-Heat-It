package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.ForgeEventHandler;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Inventory.class)
public class InventoryMixin {
	@Shadow @Final
	private List<NonNullList<ItemStack>> compartments;

	@Shadow @Final
	public Player player;

	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void convertFoodIntoLeftoverIfFullyRotten(CallbackInfo ci) {
		Level level = this.player.level;
		for(NonNullList<ItemStack> itemStackList : this.compartments) {
			for(int i = 0; i < itemStackList.size(); ++i) {
				if (!itemStackList.get(i).isEmpty()) {
					ItemStack food = itemStackList.get(i);
					if(!level.isClientSide && ForgeEventHandler.isAvailableToTickFood()) {
						double temp = Temperature.get(this.player, Temperature.Type.CORE);
						int finalI = i;
						food.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
							c.foodTick((temp + 2.0D * c.getTemperature()) / 3.0D, food.getItem());
							if(c.getTemperature() > 120) {
								FoodProperties foodProperties = food.getItem().getFoodProperties();
								if(foodProperties != null) {
									itemStackList.set(finalI, new ItemStack(foodProperties.isMeat() ? FIAHIItems.LEFTOVER_MEAT : FIAHIItems.LEFTOVER_VEGETABLE, food.getCount()));
								}
							}
						});
					}
				}
			}
		}
	}
}
