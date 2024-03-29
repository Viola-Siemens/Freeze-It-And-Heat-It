package com.hexagram2021.fiahi.common.menu;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.util.RegistryHelper;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.hexagram2021.fiahi.register.FIAHIMenuTypes;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class FoodPouchMenu extends AbstractContainerMenu implements IFrozenRottenFood {
	public static final int INPUT_SLOT = 0;
	public static final int RESULT_SLOT = 1;
	private static final int INV_SLOT_START = 2;
	private static final int INV_SLOT_END = 29;
	private static final int USE_ROW_SLOT_START = 29;
	private static final int USE_ROW_SLOT_END = 38;

	public static final int MAX_FOOD_TYPES = 5;
	public static final int MAX_FOOD_COUNT = 1024;

	final Slot inputSlot;
	final Slot resultSlot;
	Runnable slotUpdateListener = () -> {
	};

	public void runSlotUpdateListener() {
		this.slotUpdateListener.run();
	}

	public final Container container = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			super.setChanged();
			FoodPouchMenu.this.slotsChanged(this);
			FoodPouchMenu.this.runSlotUpdateListener();
		}
	};
	final ResultContainer resultContainer = new ResultContainer() {
		@Override
		public void setChanged() {
			super.setChanged();
			FoodPouchMenu.this.slotsChanged(this);
			FoodPouchMenu.this.runSlotUpdateListener();
		}
	};

	final ContainerData data = new SimpleContainerData(2);	//temperature, selectedIndex

	final Map<Item, Integer> stackedItems = new IdentityHashMap<>();

	List<Item> items = new ArrayList<>();

	@Nullable
	private final Component title;

	public FoodPouchMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, null, new CompoundTag());
	}

	public FoodPouchMenu(int containerId, Inventory inventory, @Nullable Component title, CompoundTag content) {
		super(FIAHIMenuTypes.FOOD_POUCH_MENU.get(), containerId);

		this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return super.mayPlace(itemStack) && IFrozenRottenFood.canBeFrozenRotten(itemStack);
			}

			@Override
			public void setChanged() {
				ItemStack itemStack = this.getItem();
				if(itemStack.getCount() > 0) {
					if(FoodPouchMenu.this.stackedItems.containsKey(itemStack.getItem()) || FoodPouchMenu.this.stackedItems.size() < MAX_FOOD_TYPES) {
						itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
							int totalCount = FoodPouchMenu.this.stackedItems.values().stream().reduce(0, Integer::sum);
							FoodPouchMenu.this.stackedItems.compute(itemStack.getItem(), (item, count) -> {
								if(count == null) {
									int newCount = itemStack.getCount();
									FoodPouchMenu.this.apply(
											(totalCount * FoodPouchMenu.this.getTemperature() + c.getTemperature() * itemStack.getCount()) / (totalCount + itemStack.getCount())
									);
									this.set(ItemStack.EMPTY);
									return newCount;
								}
								int newCount = count + itemStack.getCount();
								if(newCount > MAX_FOOD_COUNT) {
									FoodPouchMenu.this.apply(
											(totalCount * FoodPouchMenu.this.getTemperature() + c.getTemperature() * (MAX_FOOD_COUNT - count)) / (totalCount + MAX_FOOD_COUNT - count)
									);
									itemStack.shrink(MAX_FOOD_COUNT - count);
									return MAX_FOOD_COUNT;
								}
								FoodPouchMenu.this.apply(
										(totalCount * FoodPouchMenu.this.getTemperature() + c.getTemperature() * itemStack.getCount()) / (totalCount + itemStack.getCount())
								);
								this.set(ItemStack.EMPTY);
								return newCount;
							});
							FoodPouchMenu.this.maintainItems();
						});
					}
				}
				super.setChanged();
			}
		});
		this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
			@Override
			public boolean mayPlace(ItemStack itemStack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack itemStack) {
				FoodPouchMenu.this.stackedItems.computeIfPresent(itemStack.getItem(), (item, count) -> {
					int ret = count - itemStack.getCount();
					if(ret > 0) {
						return ret;
					}
					return null;
				});
				FoodPouchMenu.this.maintainItems();
				super.onTake(player, itemStack);
			}
		});

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
		}

		this.addDataSlots(this.data);
		this.setContent(content);
		this.title = title;
	}

	@Override
	public boolean stillValid(Player p_38874_) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack slotItem = slot.getItem();
			result = slotItem.copy();
			if (index == RESULT_SLOT) {
				if (!this.moveItemStackTo(slotItem, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index == INPUT_SLOT) {
				if (!this.moveItemStackTo(slotItem, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.inputSlot.mayPlace(slotItem)) {
				if (!this.moveItemStackTo(slotItem, INPUT_SLOT, INPUT_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
				if (!this.moveItemStackTo(slotItem, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && !this.moveItemStackTo(slotItem, INV_SLOT_START, INV_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}

			if (slotItem.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			if (slotItem.getCount() == result.getCount()) {
				return ItemStack.EMPTY;
			}

			ItemStack takeItem = result.copy();
			takeItem.setCount(result.getCount() - slotItem.getCount());
			slot.onTake(player, takeItem);
			this.broadcastChanges();
		}

		return result;
	}

	@Override
	public boolean clickMenuButton(Player player, int index) {
		if (this.isValidItemIndex(index)) {
			this.setSelectedIndex(index);
			this.setupResultSlot();
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	void setupResultSlot() {
		int index = this.getSelectedIndex();
		if(index < 0 || index >= this.items.size()) {
			return;
		}
		Item item = this.items.get(this.getSelectedIndex());
		int count = this.stackedItems.get(item);
		if(count <= 0) {
			return;
		}
		ItemStack itemStack;
		if(count >= item.getMaxStackSize()) {
			itemStack = new ItemStack(item, item.getMaxStackSize());
		} else {
			itemStack = new ItemStack(item, count);
		}
		itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
			c.setTemperature(this.getTemperature());
			c.updateFoodTag();
		});
		this.resultSlot.set(itemStack);

		this.broadcastChanges();
	}

	private boolean isValidItemIndex(int index) {
		return index >= 0 && index < this.items.size();
	}

	@SuppressWarnings("deprecation")
	private CompoundTag getContent() {
		CompoundTag ret = new CompoundTag();
		if(!this.stackedItems.isEmpty()) {
			ret.putDouble("temperature", this.getTemperature());
		}
		ListTag list = new ListTag();

		this.stackedItems.forEach((item, count) -> {
			CompoundTag tag = new CompoundTag();
			tag.putString("id", Registry.ITEM.getKey(item).toString());
			tag.putInt("Count", count);
			list.add(tag);
		});
		ret.put("Items", list);

		return ret;
	}

	@SuppressWarnings("deprecation")
	private void setContent(CompoundTag nbt) {
		if(nbt.contains("temperature", Tag.TAG_ANY_NUMERIC)) {
			this.setTemperature(nbt.getDouble("temperature"));
		}
		this.stackedItems.clear();
		if(nbt.contains("Items", Tag.TAG_LIST)) {
			ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
			for(Tag tag: list) {
				CompoundTag compoundTag = (CompoundTag)tag;
				Item item = Registry.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
				int count = compoundTag.getInt("Count");
				this.stackedItems.put(item, count);
			}
		}
		this.maintainItems();
	}

	public void registerUpdateListener(Runnable runnable) {
		this.slotUpdateListener = runnable;
	}

	private void maintainItems() {
		this.items = this.stackedItems.keySet().stream().sorted(Comparator.comparing(RegistryHelper::getRegistryName)).toList();
	}

	public void setStackedItems(Map<Item, Integer> stackedItems) {
		this.stackedItems.clear();
		this.stackedItems.putAll(stackedItems);
		this.maintainItems();
	}

	@Override
	public double getTemperature() {
		return this.data.get(0) / 100.0D;
	}

	@Override
	public void setTemperature(double temperature) {
		this.data.set(0, (int)(temperature * 100.0D));
	}

	@Override
	public double getTemperatureBalanceRate() {
		return 1.0D;
	}

	@Override
	public void foodTick(double temperature, Item item) {
	}

	@Override
	public void updateFoodTag() {
	}

	public int getSelectedIndex() {
		return this.data.get(1);
	}

	public void setSelectedIndex(int index) {
		this.data.set(1, index);
	}

	public List<Item> getStackedItems() {
		return this.items;
	}

	public Map<Item, Integer> getItemsAndCounts() {
		return this.stackedItems;
	}

	public int getItemStockCount() {
		int index = this.getSelectedIndex();
		if(index < 0 || index >= this.items.size()) {
			return 0;
		}
		return this.stackedItems.getOrDefault(this.items.get(index), 0);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if(player instanceof ServerPlayer serverPlayer) {
			ItemStack itemStack = new ItemStack(FIAHIItems.FOOD_POUCH);
			itemStack.setTag(this.getContent());
			itemStack.setHoverName(this.title);
			if (!serverPlayer.addItem(itemStack)) {
				serverPlayer.drop(itemStack, false);
			}
		}
	}
}
