package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.client.ScreenManager;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ConcurrentModificationException;
import java.util.Objects;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@EventBusSubscriber(modid = MODID)
public final class ForgeEventHandler {
	private static int tickAfterCheck = 0;

	private ForgeEventHandler() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();
		if(level.isClientSide && !player.isSpectator() && player.tickCount % 75 == 0) {
			ScreenManager.makePlayerBreatheParticle(player);
		}
	}

	@SubscribeEvent
	public static void modifyDefaultComponent(ModifyDefaultComponentsEvent event) {
		event.modifyMatching(IFrozenRottenFood::canBeFrozenRotten, builder -> builder.set(FIAHIAttachmentTypes.FOOD_TEMPERATURE.get(), 0));
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if(event.getLevel() instanceof ServerLevel serverLevel) {
			if(serverLevel.dimension().equals(Level.OVERWORLD)) {
				if(tickAfterCheck < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
					++tickAfterCheck;
					return;
				}
				tickAfterCheck = 0;
			}
			serverLevel.getChunkSource().chunkMap.getChunks().forEach(chunk -> {
				LevelChunk levelChunk = chunk.getTickingChunk();
				if(levelChunk != null && !levelChunk.isEmpty()) {
					try {
						levelChunk.getBlockEntities().forEach((blockPos, blockEntity) -> {
							ResourceLocation beId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
							if (blockEntity.hasLevel() && blockEntity instanceof Container container &&
									beId != null && !FIAHICommonConfig.STABLE_TEMPERATURE_CONTAINERS.get().contains(beId.toString())) {
								if(container instanceof RandomizableContainerBlockEntity lootContainer && lootContainer.lootTable != null) {
									return;
								}
								tickContainer(blockEntity, container, blockPos, container.getContainerSize(), Container::getItem, Container::setItem, serverLevel);
							} else {
								IItemHandler itemHandler = serverLevel.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, blockEntity.getBlockState(), blockEntity, Direction.UP);
								if(itemHandler instanceof IItemHandlerModifiable itemHandlerModifiable) {
									tickContainer(blockEntity, itemHandlerModifiable, blockPos, itemHandlerModifiable.getSlots(), IItemHandlerModifiable::getStackInSlot, IItemHandlerModifiable::setStackInSlot, serverLevel);
								}
							}
						});
					} catch (ConcurrentModificationException cme) {
						ChunkPos pos = chunk.getPos();
						throw new RuntimeException("Block entities of chunk (%d, %d) has been concurrently modified during iterating. This is NOT a bug of FIAHI. See https://github.com/Viola-Siemens/Freeze-It-And-Heat-It/issues/25 to get more information.".formatted(pos.x, pos.z), cme);
					}
				}
			});
		}
	}

	private static <T> void tickContainer(BlockEntity blockEntity, T container, BlockPos blockPos, int size, FoodGetter<T> foodGetter, FoodSetter<T> foodSetter, ServerLevel serverLevel) {
		double temp = WorldHelper.getTemperatureAt(Objects.requireNonNull(blockEntity.getLevel()), blockPos);
		for (int i = 0; i < size; ++i) {
			ItemStack food = foodGetter.getFood(container, i);
			int finalI = i;
			IFrozenRottenFood.tick(food, itemStack -> foodSetter.setFood(container, finalI, itemStack), c -> c.getTemperature() + 2.0D * temp, serverLevel, null);
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
