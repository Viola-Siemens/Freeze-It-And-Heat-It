package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenItemStack;
import com.hexagram2021.fiahi.common.item.capability.impl.FrozenRottenFood;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IFrozenRottenItemStack {
	@Unique @Nullable
	private IFrozenRottenFood fiahi$frozenRottenFood = null;

	@Override
	public IFrozenRottenFood fiahi$getFrozenRottenFood() {
		if(this.fiahi$frozenRottenFood == null) {
			this.fiahi$frozenRottenFood = new FrozenRottenFood((ItemStack)(Object)this);
		}
		return this.fiahi$frozenRottenFood;
	}
}
