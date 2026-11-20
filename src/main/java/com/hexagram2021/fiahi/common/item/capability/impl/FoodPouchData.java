package com.hexagram2021.fiahi.common.item.capability.impl;

import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Objects;

/**
 * 食物口袋附加数据
 * @param temperature 温度
 * @param items 食物
 */
public record FoodPouchData(double temperature, List<ItemWithCount> items) {
	public static final FoodPouchData EMPTY = new FoodPouchData(0, List.of());

	public static final Codec<FoodPouchData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("temperature").forGetter(FoodPouchData::temperature),
			ItemWithCount.CODEC.listOf().fieldOf("items").forGetter(FoodPouchData::items)
	).apply(instance, FoodPouchData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FoodPouchData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, FoodPouchData::temperature,
			ItemWithCount.STREAM_CODEC.apply(
					ByteBufCodecs.collection(NonNullList::createWithCapacity)
			), FoodPouchData::items,
			FoodPouchData::new
	);

	/**
	 * 物品 + 堆叠数业务对象
	 *
	 * @author liudongyu
	 */
	public static final class ItemWithCount implements ItemLike {
		/** 空对象 */
		public static final ItemWithCount EMPTY = new ItemWithCount(Items.AIR.builtInRegistryHolder(), 0, List.of());

		public static final Codec<ItemWithCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemWithCount::item),
				Codec.LONG.fieldOf("count").forGetter(ItemWithCount::count),
				IPouchedFoodData.REGISTRY_CODEC.listOf().fieldOf("datas").forGetter(ItemWithCount::datas)
		).apply(instance, ItemWithCount::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, ItemWithCount> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.holderRegistry(Registries.ITEM), ItemWithCount::item,
				ByteBufCodecs.VAR_LONG, ItemWithCount::count,
				IPouchedFoodData.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemWithCount::datas,
				ItemWithCount::new
		);

		final Holder<Item> item;
		long count;
		final List<IPouchedFoodData<?>> datas;

		/**
		 * 构造函数
		 * @param item 物品
		 * @param count 数量
		 * @param datas 附加数据
		 */
		public ItemWithCount(Holder<Item> item, long count, List<IPouchedFoodData<?>> datas) {
			this.item = item;
			this.count = count;
			this.datas = datas;
		}

		/**
		 * 创建物品堆叠
		 *
		 * @param amount 数量
		 * @return 物品堆叠
		 */
		public ItemStack split(int amount) {
			int i = Math.toIntExact(Math.min(amount, this.count));
			ItemStack itemStack = this.copyWithCount(i);
			this.count -= i;
			return itemStack;
		}

		/**
		 * 复制为物品堆叠
		 *
		 * @param count 数量
		 * @return 物品堆叠
		 */
		public ItemStack copyWithCount(int count) {
			if (this.item.value() == Items.AIR || count == 0) {
				return ItemStack.EMPTY;
			}
			ItemStack ret = new ItemStack(this.item, count);
			this.datas.forEach(data -> data.modifyStack(ret));
			return ret;
		}

		@Override
		public Item asItem() {
			return this.item.value();
		}

		/**
		 * 物品
		 * @return 物品 Holder
		 */
		public Holder<Item> item() {
			return this.item;
		}

		/**
		 * 数量
		 * @return 数量
		 */
		public long count() {
			return this.count;
		}

		/**
		 * 附加区分数据
		 * @return 附加区分数据
		 */
		public List<IPouchedFoodData<?>> datas() {
			return this.datas;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(this.item.toString());
			builder.append('[');
			for(IPouchedFoodData<?> data: this.datas) {
				builder.append(data.toString()).append(',');
			}
			builder.append(']');
			builder.append("x").append(this.count);
			return builder.toString();
		}

		@Override
		public int hashCode() {
			return this.datas.hashCode() ^ Objects.hash(this.item, this.count);
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof ItemWithCount itemWithCount) {
				return this.item.equals(itemWithCount.item) && this.count == itemWithCount.count && this.datas.equals(itemWithCount.datas);
			}
			return false;
		}
	}
}
