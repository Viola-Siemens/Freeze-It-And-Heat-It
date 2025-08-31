package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIMenuTypes {
	private static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<FoodPouchMenu>> FOOD_POUCH_MENU = REGISTER.register("food_pouch", () -> new MenuType<>(FoodPouchMenu::new, FeatureFlags.VANILLA_SET));

	private FIAHIMenuTypes() {}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
