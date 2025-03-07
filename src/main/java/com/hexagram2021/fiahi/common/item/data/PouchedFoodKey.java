package com.hexagram2021.fiahi.common.item.data;

import com.hexagram2021.fiahi.common.util.FIAHILogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Arrays;

public record PouchedFoodKey(Item item, IPouchedFoodData... datas) {
	public static final PouchedFoodKey EMPTY = new PouchedFoodKey(Items.AIR);

	public ListTag extra() {
		ListTag listTag = new ListTag();
		Arrays.stream(this.datas)
				.map(data -> IPouchedFoodData.REGISTRY_CODEC.encode(data, NbtOps.INSTANCE, new CompoundTag()).getOrThrow(false, FIAHILogger::error))
				.forEach(listTag::add);
		return listTag;
	}
	public static IPouchedFoodData[] readExtraNBT(ListTag nbt) {
		return nbt.stream().map(tag -> IPouchedFoodData.REGISTRY_CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, FIAHILogger::error)).toArray(IPouchedFoodData[]::new);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PouchedFoodKey key) {
			if(this.item.equals(key.item) && this.datas.length == key.datas.length) {
				for(int i = 0; i < this.datas.length; ++i) {
					if(!this.datas[i].equals(key.datas[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		int code = 0;
		for(IPouchedFoodData data: this.datas) {
			code ^= data.hashCode();
		}
		return this.item.hashCode() ^ code;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.item.toString());
		builder.append('[');
		for(IPouchedFoodData data: this.datas) {
			builder.append(data.toString());
			builder.append(',');
		}
		builder.append(']');
		return builder.toString();
	}
}
