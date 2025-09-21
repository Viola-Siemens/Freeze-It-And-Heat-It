package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@EventBusSubscriber(modid = MODID)
public class FIAHICapabilities {
	public static final ResourceLocation FOOD_CAPABILITY_ID = ResourceLocation.fromNamespaceAndPath(MODID, "food");
	public static final ItemCapability<IFrozenRottenFood, Void> FOOD_CAPABILITY = ItemCapability.createVoid(FOOD_CAPABILITY_ID, IFrozenRottenFood.class);

	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event) {
		event.registerItem(
				FOOD_CAPABILITY,
				(itemStack, ignored) -> ((IFrozenRottenItemStack)(Object)itemStack).fiahi$getFrozenRottenFood(),
				BuiltInRegistries.ITEM.stream().filter(item -> item.components().has(DataComponents.FOOD)).toArray(ItemLike[]::new)
		);
	}
}
