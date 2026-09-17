package com.hexagram2021.fiahi.common;

import com.google.common.collect.Lists;
import com.hexagram2021.fiahi.client.ScreenManager;
import com.hexagram2021.fiahi.common.config.FIAHICommonConfig;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

/**
 * NeoForge 模组事件监听处理器
 *
 * @author liudongyu
 */
@EventBusSubscriber(modid = MODID)
public final class ForgeEventHandler {
	// Number of ticks elapsed since the last food-temperature check, tracked per dimension.
	// Throttles every dimension independently so the Nether/End no longer run every tick.
	private static final Object2IntMap<ResourceKey<Level>> tickAfterCheck = new Object2IntOpenHashMap<>();

	private ForgeEventHandler() {
	}

	/**
	 * 当前 tick 是否被允许修改食物温度 - 性能优化
	 * @param dimension 维度 key
	 * @return true：允许修改温度；false：跳过当前刻
	 */
	public static boolean isAvailableToTickFood(ResourceKey<Level> dimension) {
		return tickAfterCheck.getOrDefault(dimension, 0) == 0;
	}

	/**
	 * 玩家 tick 事件处理器：仅客户端增加一段逻辑，让寒冷的玩家呼吸产生粒子
	 * @param event 玩家 tick 事件
	 */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Level level = player.level();
		if(level.isClientSide && !player.isSpectator() && player.tickCount % 75 == 0) {
			ScreenManager.makePlayerBreatheParticle(player);
		}
	}

	/**
	 * 修改所有食物的物品堆叠组件，加入温度数据
	 * @param event 修改默认组件事件
	 */
	@SubscribeEvent
	public static void modifyDefaultComponent(ModifyDefaultComponentsEvent event) {
		event.modifyMatching(IFrozenRottenFood::canBeFrozenRotten, builder -> builder.set(FIAHIAttachmentTypes.FOOD_TEMPERATURE.get(), 0));
	}

	/**
	 * 世界 tick 事件处理器：服务端每 tick 触发，满足条件后更新当前世界所有已加载区块中所有方块实体中食物的温度
	 * @param event 世界 tick 事件
	 */
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if(event.getLevel() instanceof ServerLevel serverLevel) {
			ResourceKey<Level> dimension = serverLevel.dimension();
			if(!shouldCheckTemperature(dimension)) {
				return;
			}
			// Collect every eligible container and its position first; the temperature batch query runs once below.
			List<FoodContainer> containers = Lists.newArrayList();
			List<BlockPos> containerPositions = Lists.newArrayList();
			serverLevel.getChunkSource().chunkMap.getChunks().forEach(
					chunk -> collectContainerPositionsByChunk(serverLevel, chunk, containerPositions, containers)
			);

			if(containerPositions.isEmpty()) {
				return;
			}
			// 一次粗精度批查询（8 格分段缓存），绝大多数 tick 只是分段缓存命中，过期分段自然错峰重算喵~
			Object2DoubleMap<BlockPos> temperatures = getRoughTemperaturesAt(serverLevel, containerPositions);
			for(FoodContainer container : containers) {
				tickFood(container, temperatures.getDouble(container.blockPos()), serverLevel);
			}
		}
	}

	private static Object2DoubleMap<BlockPos> getRoughTemperaturesAt(Level level, Collection<BlockPos> positions) {
		Object2DoubleMap<BlockPos> result = new Object2DoubleOpenHashMap<>();
		for (BlockPos pos : positions) {
			result.put(pos, WorldHelper.getRoughTemperatureAt(level, pos, 0));
		}
		return result;
	}

	private static void collectContainerPositionsByChunk(ServerLevel serverLevel, ChunkHolder chunk,
														 List<BlockPos> containerPositions, List<FoodContainer> containers) {
		LevelChunk levelChunk = chunk.getTickingChunk();
		if(levelChunk != null && !levelChunk.isEmpty()) {
			// CoI 策略防并发修改
			Map<BlockPos, BlockEntity> blockEntities = getBlockPosBlockEntityMap(chunk, levelChunk);
			blockEntities.forEach((blockPos, blockEntity) -> {
				ResourceLocation beId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
				if(beId != null && !FIAHICommonConfig.STABLE_TEMPERATURE_CONTAINERS.get().contains(beId.toString())) {
					try {
						if (blockEntity.hasLevel() && blockEntity instanceof Container container) {
							if (container instanceof RandomizableContainerBlockEntity lootContainer && lootContainer.lootTable != null) {
								return;
							}
							containerPositions.add(blockPos);
							containers.add(new FoodContainer(blockPos, beId, container.getContainerSize(), container::getItem, container::setItem));
						} else {
							IItemHandler itemHandler = serverLevel.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, blockEntity.getBlockState(), blockEntity, Direction.UP);
							if (itemHandler instanceof IItemHandlerModifiable itemHandlerModifiable) {
								containerPositions.add(blockPos);
								containers.add(new FoodContainer(blockPos, beId, itemHandlerModifiable.getSlots(), itemHandlerModifiable::getStackInSlot, itemHandlerModifiable::setStackInSlot));
							}
						}
					} catch (RuntimeException re) {
						ChunkPos pos = chunk.getPos();
						throw new IllegalStateException(
								"Block entity %s at (%d, %d, %d) of chunk (%d, %d) causes the crashed due to inconsistent size data. This is NOT a bug of FIAHI."
										.formatted(beId, blockPos.getX(), blockPos.getY(), blockPos.getZ(), pos.x, pos.z),
								re
						);
					}
				}
			});
		}
	}

	private static Map<BlockPos, BlockEntity> getBlockPosBlockEntityMap(ChunkHolder chunk, LevelChunk levelChunk) {
		try {
			return new Object2ObjectOpenHashMap<>(levelChunk.getBlockEntities());
		} catch (ConcurrentModificationException cme) {
			ChunkPos pos = chunk.getPos();
			throw new IllegalStateException("Block entities of chunk (%d, %d) has been concurrently modified during iterating. This is NOT a bug of FIAHI. See https://github.com/Viola-Siemens/Freeze-It-And-Heat-It/issues/25 to get more information.".formatted(pos.x, pos.z), cme);
		}
	}

	private static void tickFood(FoodContainer container, double temp, ServerLevel serverLevel) {
		try {
			for (int i = 0; i < container.size(); ++i) {
				ItemStack food = container.foodGetter().getFood(i);
				int finalI = i;
				IFrozenRottenFood.tick(food, itemStack -> container.foodSetter().setFood(finalI, itemStack), c -> c.getTemperature() + 2.0D * temp, serverLevel, null);
			}
		} catch (RuntimeException re) {
			BlockPos pos = container.blockPos();
			throw new IllegalStateException(
					"Block entity %s at (%d, %d, %d) causes the crashed due to inconsistent size data. This is NOT a bug of FIAHI.".formatted(container.beId(), pos.getX(), pos.getY(), pos.getZ()),
					re
			);
		}
	}

	private interface FoodGetter {
		ItemStack getFood(int index);
	}
	private interface FoodSetter {
		void setFood(int index, ItemStack food);
	}
	private record FoodContainer(BlockPos blockPos, ResourceLocation beId, int size, FoodGetter foodGetter, FoodSetter foodSetter) {}

	private static boolean shouldCheckTemperature(ResourceKey<Level> dimension) {
		int counter = tickAfterCheck.getOrDefault(dimension, 0);
		if(counter < FIAHICommonConfig.TEMPERATURE_CHECKER_INTERVAL.get()) {
			tickAfterCheck.put(dimension, counter + 1);
			return false;
		}
		tickAfterCheck.put(dimension, 0);
		return true;
	}
}
