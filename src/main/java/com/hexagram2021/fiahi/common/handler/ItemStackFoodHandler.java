package com.hexagram2021.fiahi.common.handler;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.impl.FrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ItemStackFoodHandler implements ICapabilityProvider, INBTSerializable<Tag> {
	public static final String FIAHI_TAG_TEMPERATURE = "fiahi:temperature";

	private final FrozenRottenFood food;
	private final LazyOptional<IFrozenRottenFood> holder;

	public ItemStackFoodHandler(ItemStack itemStack) {
		this.food = new FrozenRottenFood(itemStack);
		this.food.syncFoodTag();
		this.holder = LazyOptional.of(() -> this.food);
	}

	@Override @Nonnull
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		return FIAHICapabilities.FOOD_CAPABILITY.orEmpty(capability, this.holder);
	}

	@Override
	public Tag serializeNBT() {
		return new CompoundTag();
	}

	@Override
	public void deserializeNBT(Tag nbt) {
		this.food.syncFoodTag();
	}
}
