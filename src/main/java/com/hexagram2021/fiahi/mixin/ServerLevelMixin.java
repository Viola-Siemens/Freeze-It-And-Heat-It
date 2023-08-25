package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(method = "tickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V", shift = At.Shift.AFTER))
	private void tickContainer(BlockPos blockPos, Block block, CallbackInfo ci) {
		ServerLevel current = (ServerLevel)(Object)this;
		BlockState blockState = current.getBlockState(blockPos);
		if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = current.getBlockEntity(blockPos);
			if (blockEntity instanceof Container container) {
				double temp = Temperature.getTemperatureAt(blockPos, current);
				FIAHILogger.debug("Temperature at (%d, %d, %d) is %f.".formatted(blockPos.getX(), blockPos.getY(), blockPos.getZ(), temp));
				for (int i = 0; i < container.getContainerSize(); ++i) {
					ItemStack food = container.getItem(i);
					food.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> c.foodTick(temp));
				}
			}
		}
	}
}
