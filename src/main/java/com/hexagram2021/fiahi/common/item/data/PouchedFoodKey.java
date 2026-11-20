package com.hexagram2021.fiahi.common.item.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record PouchedFoodKey(Item item, IPouchedFoodData<?>... datas) {
	public static final PouchedFoodKey EMPTY = new PouchedFoodKey(Items.AIR);

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PouchedFoodKey(Item item1, IPouchedFoodData<?>[] datas1) &&
				this.item.equals(item1) && this.datas.length == datas1.length) {
			for(int i = 0; i < this.datas.length; ++i) {
				if(!this.datas[i].equals(datas1[i])) {
					return false;
				}
			}
			return true;
		}

		return false;
	}
	@Override
	public int hashCode() {
		int code = 0;
		for(IPouchedFoodData<?> data: this.datas) {
			code ^= data.hashCode();
		}
		return this.item.hashCode() ^ code;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.item.toString());
		builder.append('[');
		for(IPouchedFoodData<?> data: this.datas) {
			builder.append(data.toString()).append(',');
		}
		builder.append(']');
		return builder.toString();
	}
}
