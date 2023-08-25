package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;
import static com.hexagram2021.fiahi.register.FIAHICapabilities.FOOD_CAPABILITY_ID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventHandler {
	private ForgeEventHandler() {}

	@SubscribeEvent
	public static void onAttackItemStackCapability(AttachCapabilitiesEvent<ItemStack> event) {
		event.addCapability(FOOD_CAPABILITY_ID, new ItemStackFoodHandler(event.getObject()));
	}

	@SubscribeEvent
	public static void onLevelTick(TickEvent.WorldTickEvent event) {
		if(event.phase == TickEvent.Phase.END && event.world instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunk -> {
				LevelChunk levelChunk = chunk.getFullChunk();
				if(levelChunk != null) {
					levelChunk.getBlockEntities().forEach((blockPos, blockEntity) -> {
						if (blockEntity.hasLevel() && blockEntity instanceof Container container) {
							double temp = Temperature.getTemperatureAt(blockPos, Objects.requireNonNull(blockEntity.getLevel()));
							for (int i = 0; i < container.getContainerSize(); ++i) {
								ItemStack food = container.getItem(i);
								food.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> c.foodTick(c.getTemperature() + 5.0 * temp));
							}
						}
					});
				}
			});
		}
	}
}
