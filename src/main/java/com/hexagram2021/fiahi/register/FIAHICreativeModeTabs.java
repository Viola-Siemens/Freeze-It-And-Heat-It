package com.hexagram2021.fiahi.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHICreativeModeTabs {
	private FIAHICreativeModeTabs() {
	}

	private static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

	@SuppressWarnings("unused")
	public static final RegistryObject<CreativeModeTab> ITEM_GROUP = REGISTER.register(
			"fiahi", () -> CreativeModeTab.builder()
					.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
					.title(Component.translatable("itemGroup.fiahi"))
					.icon(() -> new ItemStack(FIAHIItems.FOOD_POUCH))
					.displayItems((flags, output) -> FIAHIItems.ItemEntry.ALL_ITEMS.forEach(output::accept))
					.build()
	);

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
