package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.client.ScreenManager;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;
import static com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood.canBeFrozenRotten;
import static com.hexagram2021.fiahi.register.FIAHICapabilities.FOOD_CAPABILITY_ID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventHandler {
	private static int tickAfterCheck = 0;

	private ForgeEventHandler() {}

	@SubscribeEvent
	public static void onAttackItemStackCapability(AttachCapabilitiesEvent<ItemStack> event) {
		if(canBeFrozenRotten(event.getObject())) {
			event.addCapability(FOOD_CAPABILITY_ID, new ItemStackFoodHandler(event.getObject()));
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		Player player = event.player;
		Level level = player.level();
		if(level.isClientSide && event.phase == TickEvent.Phase.END && player.tickCount % 75 == 0) {
			ScreenManager.makePlayerBreatheParticle(player);
		}
	}

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if(event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
			if(serverLevel.dimension().equals(Level.OVERWORLD)) {
				if(tickAfterCheck < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
					++tickAfterCheck;
					return;
				}
				tickAfterCheck = 0;
			}
			serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunk -> {
				LevelChunk levelChunk = chunk.getFullChunk();
				if(levelChunk != null && !levelChunk.isEmpty()) {
					levelChunk.getBlockEntities().forEach((blockPos, blockEntity) -> {
						ResourceLocation beId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
						if (blockEntity.hasLevel() && blockEntity instanceof Container container &&
								beId != null && !FIAHICommonConfig.STABLE_TEMPERATURE_CONTAINERS.get().contains(beId.toString())) {
							if(container instanceof RandomizableContainerBlockEntity lootContainer && lootContainer.lootTable != null) {
								return;
							}
							tickContainer(blockEntity, container, blockPos, container.getContainerSize(), Container::getItem, Container::setItem);
						} else {
							blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
								if(itemHandler instanceof IItemHandlerModifiable itemHandlerModifiable) {
									tickContainer(blockEntity, itemHandlerModifiable, blockPos, itemHandlerModifiable.getSlots(), IItemHandlerModifiable::getStackInSlot, IItemHandlerModifiable::setStackInSlot);
								}
							});
						}
					});
				}
			});
		}
	}

	@SuppressWarnings("deprecation")
	private static <T> void tickContainer(BlockEntity blockEntity, T container, BlockPos blockPos, int size, FoodGetter<T> foodGetter, FoodSetter<T> foodSetter) {
		double temp = WorldHelper.getTemperatureAt(Objects.requireNonNull(blockEntity.getLevel()), blockPos);
		for (int i = 0; i < size; ++i) {
			ItemStack food = foodGetter.getFood(container, i);
			int finalI = i;
			food.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
				c.foodTick(c.getTemperature() + 2.0D * temp, food.getItem());
				if(c.getTemperature() > 120) {
					FoodProperties foodProperties = food.getItem().getFoodProperties();
					if(foodProperties != null) {
						foodSetter.setFood(container, finalI, new ItemStack(foodProperties.isMeat() ? FIAHIItems.LEFTOVER_MEAT : FIAHIItems.LEFTOVER_VEGETABLE, food.getCount()));
					}
				}
			});
		}
	}

	private interface FoodGetter<T> {
		ItemStack getFood(T container, int index);
	}
	private interface FoodSetter<T> {
		void setFood(T container, int index, ItemStack food);
	}

	public static boolean isAvailableToTickFood() {
		return tickAfterCheck == 0;
	}
}
