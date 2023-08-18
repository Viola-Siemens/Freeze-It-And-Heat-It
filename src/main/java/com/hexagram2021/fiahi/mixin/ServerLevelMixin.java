package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.item.IFrozenRottenFood;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
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
		if(blockState.hasBlockEntity()) {
			BlockEntity blockEntity = current.getBlockEntity(blockPos);
			if(blockEntity instanceof Container container) {
				for(int i = 0; i < container.getContainerSize(); ++i) {
					IFrozenRottenFood food = (IFrozenRottenFood)(Object)container.getItem(i);
					food.tick(Temperature.getTemperatureAt(blockPos, current));
				}
			}
		}
	}
}
