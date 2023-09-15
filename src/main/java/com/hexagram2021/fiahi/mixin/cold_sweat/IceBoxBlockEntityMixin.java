package com.hexagram2021.fiahi.mixin.cold_sweat;

import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import dev.momostudios.coldsweat.common.blockentity.BoilerBlockEntity;
import dev.momostudios.coldsweat.common.blockentity.IceboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceboxBlockEntity.class)
public class IceBoxBlockEntityMixin {
	@Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Ldev/momostudios/coldsweat/common/blockentity/IceboxBlockEntity;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.BEFORE), remap = false)
	private static <T extends BlockEntity> void tickFoods(Level level, BlockPos pos, BlockState state, T te, CallbackInfo ci) {
		IceboxBlockEntity iceboxTE = (IceboxBlockEntity)te;
		if (iceboxTE.getFuel() > 0) {
			if (iceboxTE.ticksExisted % 20 == 0) {
				boolean hasItemStacks = false;

				for(int itemFuel = 1; itemFuel < IceboxBlockEntity.SLOTS; ++itemFuel) {
					ItemStack itemStack = iceboxTE.getItem(itemFuel);
					if (itemStack.isEdible()) {
						hasItemStacks = true;
						itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
							if(c.getTemperature() > -IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD) {
								c.setTemperature(c.getTemperature() - 1.0D);
								c.updateFoodTag();
							}
						});
					}
				}

				if (hasItemStacks) {
					iceboxTE.setFuel(iceboxTE.getFuel() - 1);
				}
			} else if(iceboxTE.ticksExisted % 4 == 1) {
				boolean hasItemStacks = false;

				for(int itemFuel = 1; itemFuel < BoilerBlockEntity.SLOTS; ++itemFuel) {
					ItemStack itemStack = iceboxTE.getItem(itemFuel);
					if(itemStack.getItem() == FIAHIItems.FOOD_POUCH.get()) {
						CompoundTag nbt = itemStack.getOrCreateTag();
						double itemTemp = nbt.getDouble("temperature");
						int itemCount = FoodPouchItem.getItemCount(nbt);
						if(itemCount > 0 && itemTemp < IFrozenRottenFood.FROZEN_ROTTEN_THRESHOLD && iceboxTE.ticksExisted % (4 * itemCount) == 1) {
							hasItemStacks = true;
							nbt.putDouble("temperature", itemTemp + 0.2D);
							itemStack.setTag(nbt);
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
