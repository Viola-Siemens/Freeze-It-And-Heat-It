package com.hexagram2021.fiahi.common.event;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 事件：在应用冷冻或腐败食物的特殊效果时触发喵~
 *
 * 附属模组可以通过此事件：
 * 1. 取消 FIAHI 的原有效果应用
 * 2. 注册自定义效果回调，在原有效果应用后执行喵~
 *
 * @author liudongyu
 */
public class ApplySpecialEatEffectEvent extends Event implements ICancellableEvent {
	private final LivingEntity entity;
	private final ItemStack itemStack;
	private final IFrozenRottenFood foodData;
	private boolean cancelled = false;
	private final List<EffectCallback> callbacks = new ArrayList<>();

	/**
	 * 创建事件喵~
	 *
	 * @param entity 正在吃东西的实体喵~
	 * @param itemStack 正在吃的物品堆喵~
	 * @param foodData 物品的冷冻腐败数据喵~
	 */
	public ApplySpecialEatEffectEvent(LivingEntity entity, ItemStack itemStack, IFrozenRottenFood foodData) {
		this.entity = entity;
		this.itemStack = itemStack;
		this.foodData = foodData;
	}

	/**
	 * 获取正在吃东西的实体喵~
	 *
	 * @return 正在吃东西的实体喵~
	 */
	public LivingEntity getEntity() {
		return entity;
	}

	/**
	 * 获取正在吃的物品堆喵~
	 *
	 * @return 正在吃的物品堆喵~
	 */
	public ItemStack getItemStack() {
		return itemStack;
	}

	/**
	 * 获取物品的冷冻腐败数据喵~
	 *
	 * @return 物品的冷冻腐败数据喵~
	 */
	public IFrozenRottenFood getFoodData() {
		return foodData;
	}

	/**
	 * 注册自定义效果回调喵~
	 *
	 * 回调将在 FIAHI 的原有效果应用之后执行喵~
	 *
	 * @param callback 回调函数，接收实体和冷冻腐败数据喵~
	 */
	public void addEffectCallback(EffectCallback callback) {
		callbacks.add(callback);
	}

	/**
	 * 执行所有已注册的回调喵~
	 */
	public void executeCallbacks() {
		for(EffectCallback callback : callbacks) {
			callback.apply(entity, foodData);
		}
	}

	/**
	 * 检查事件是否已取消喵~
	 *
	 * @return true 表示事件已取消喵~
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * 取消事件喵~
	 *
	 * 取消后，FIAHI 的原有效果和所有回调都不会执行喵~
	 *
	 * @param cancelled 是否取消喵~
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * 效果回调函数式接口喵~
	 *
	 * 附属模组实现此接口来添加自定义效果喵~
	 *
	 * @author liudongyu
	 */
	@FunctionalInterface
	public interface EffectCallback extends BiConsumer<LivingEntity, IFrozenRottenFood> {
		/**
		 * 应用自定义效果喵~
		 *
		 * @param entity 正在吃东西的实体喵~
		 * @param foodData 物品的冷冻腐败数据喵~
		 */
		void apply(LivingEntity entity, IFrozenRottenFood foodData);

		@Override
		default void accept(LivingEntity livingEntity, IFrozenRottenFood foodData) {
			this.apply(livingEntity, foodData);
		}
	}
}
