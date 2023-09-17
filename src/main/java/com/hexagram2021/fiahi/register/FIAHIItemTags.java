package com.hexagram2021.fiahi.register;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIItemTags {
	public static final TagKey<Item> LEFTOVERS = create("leftovers");

	private FIAHIItemTags() {
	}

	@SuppressWarnings("SameParameterValue")
	private static TagKey<Item> create(String name) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MODID, name));
	}
}
