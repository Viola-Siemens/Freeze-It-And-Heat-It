package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.FreezeItAndHeatIt;
import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIItems {
	private static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	public static final ItemEntry<FoodPouchItem> FOOD_POUCH = ItemEntry.register("food_pouch", () -> new FoodPouchItem(new Item.Properties().stacksTo(1).tab(FreezeItAndHeatIt.ITEM_GROUP)));

	public static final ItemEntry<Item> LEFTOVER_MEAT = ItemEntry.register("leftover_meat", () -> new Item(new Item.Properties().food(FIAHIFoods.LEFTOVER_MEAT).tab(FreezeItAndHeatIt.ITEM_GROUP)));
	public static final ItemEntry<Item> LEFTOVER_VEGETABLE = ItemEntry.register("leftover_vegetable", () -> new Item(new Item.Properties().food(FIAHIFoods.LEFTOVER_VEGETABLE).tab(FreezeItAndHeatIt.ITEM_GROUP)));

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}

	public static class ItemEntry<T extends Item> implements Supplier<T>, ItemLike {
		private final RegistryObject<T> regObject;

		private ItemEntry(RegistryObject<T> regObject) {
			this.regObject = regObject;
		}

		public static <T extends Item> ItemEntry<T> register(String name, Supplier<? extends T> make) {
			return new ItemEntry<>(REGISTER.register(name, make));
		}

		@Override
		public T get() {
			return this.regObject.get();
		}

		@Override
		public Item asItem() {
			return this.regObject.get();
		}

		public ResourceLocation getId() {
			return this.regObject.getId();
		}
	}
}
