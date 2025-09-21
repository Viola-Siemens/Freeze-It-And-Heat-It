package com.hexagram2021.fiahi.register;

import com.google.common.collect.Lists;
import com.hexagram2021.fiahi.common.item.FoodPouchItem;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@SuppressWarnings("unused")
public class FIAHIItems {
	private static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Registries.ITEM, MODID);

	public static final ItemEntry<FoodPouchItem> FOOD_POUCH = ItemEntry.register("food_pouch", () -> new FoodPouchItem(new Item.Properties().stacksTo(1).component(FIAHIAttachmentTypes.FOOD_POUCH_DATA.get(), FoodPouchData.EMPTY)));

	public static final ItemEntry<Item> LEFTOVER_MEAT = ItemEntry.register("leftover_meat", () -> new Item(new Item.Properties().food(FIAHIFoods.LEFTOVER_MEAT)));
	public static final ItemEntry<Item> LEFTOVER_VEGETABLE = ItemEntry.register("leftover_vegetable", () -> new Item(new Item.Properties().food(FIAHIFoods.LEFTOVER_VEGETABLE)));

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}

	public static class ItemEntry<T extends Item> implements Supplier<T>, ItemLike {
		private final DeferredHolder<Item, T> regObject;

		public static final List<ItemEntry<? extends Item>> ALL_ITEMS = Lists.newArrayList();

		private ItemEntry(DeferredHolder<Item, T> regObject) {
			this.regObject = regObject;
			ALL_ITEMS.add(this);
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
	}
}
