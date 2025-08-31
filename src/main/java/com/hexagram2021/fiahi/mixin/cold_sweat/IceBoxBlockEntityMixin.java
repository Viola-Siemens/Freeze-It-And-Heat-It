package com.hexagram2021.fiahi.mixin.cold_sweat;

import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood.canBeFrozenRotten;

@Mixin(IceboxBlockEntity.class)
public class IceBoxBlockEntityMixin {
	@Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Lcom/momosoftworks/coldsweat/common/blockentity/IceboxBlockEntity;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.BEFORE), remap = false)
	private static <T extends BlockEntity> void fiahi$tickFoods(Level level, BlockPos pos, BlockState state, T te, CallbackInfo ci) {
		IceboxBlockEntity iceboxTE = (IceboxBlockEntity)te;
		if (iceboxTE.getFuel() > 0) {
			if (iceboxTE.ticksExisted % 20 == 0) {
				boolean hasItemStacks = false;

				for(int itemFuel: IceboxBlockEntity.WATERSKIN_SLOTS) {
					ItemStack itemStack = iceboxTE.getItem(itemFuel);
					if (canBeFrozenRotten(itemStack)) {
						hasItemStacks = true;
						IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
						if(c != null) {
							if(c.getTemperature() > -IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD) {
								c.setTemperature(c.getTemperature() - 1.0D);
								c.updateFoodTag();
							}
						}
					}
				}

				if (hasItemStacks) {
					iceboxTE.setFuel(iceboxTE.getFuel() - 1);
				}
			} else if(iceboxTE.ticksExisted % 4 == 1) {
				boolean hasItemStacks = false;

				for(int itemFuel: IceboxBlockEntity.WATERSKIN_SLOTS) {
					ItemStack itemStack = iceboxTE.getItem(itemFuel);
					if(itemStack.getItem() == FIAHIItems.FOOD_POUCH.get()) {
						FoodPouchData foodPouchData = itemStack.get(FIAHIAttachmentTypes.FOOD_POUCH_DATA);
						if(foodPouchData == null) {
							foodPouchData = FoodPouchData.EMPTY;
						}
						double itemTemp = foodPouchData.temperature();
						int itemCount = FoodPouchItem.getItemCount(foodPouchData);
						if(itemCount > 0 && itemTemp > -IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD && iceboxTE.ticksExisted % (4 * itemCount) == 1) {
							hasItemStacks = true;
							itemStack.set(FIAHIAttachmentTypes.FOOD_POUCH_DATA, new FoodPouchData(itemTemp - 0.2D, foodPouchData.items()));
						}
					}
				}

				if (hasItemStacks) {
					iceboxTE.setFuel(iceboxTE.getFuel() - 1);
				}
			}
		}
	}
}
