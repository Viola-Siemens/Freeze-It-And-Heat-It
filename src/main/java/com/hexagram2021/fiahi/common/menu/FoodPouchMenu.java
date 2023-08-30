package com.hexagram2021.fiahi.common.menu;

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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class FoodPouchMenu extends AbstractContainerMenu {
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
	final ResultContainer resultContainer = new ResultContainer();

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
				return super.mayPlace(itemStack) && itemStack.isEdible();
			}

			@Override
			public void setChanged() {
				ItemStack itemStack = this.getItem();
				if(itemStack.getCount() > 0) {
					if(FoodPouchMenu.this.stackedItems.containsKey(itemStack.getItem()) || FoodPouchMenu.this.stackedItems.size() < MAX_FOOD_TYPES) {
						FoodPouchMenu.this.stackedItems.compute(itemStack.getItem(), (item, count) -> {
							if(count == null) {
								int newCount = itemStack.getCount();
								itemStack.setCount(0);
								return newCount;
							}
							int newCount = count + itemStack.getCount();
							if(newCount > MAX_FOOD_COUNT) {
								itemStack.shrink(MAX_FOOD_COUNT - count);
								return MAX_FOOD_COUNT;
							}
							itemStack.setCount(0);
							return newCount;
						});
						FoodPouchMenu.this.maintainItems();
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
				FoodPouchMenu.this.stackedItems.computeIfPresent(itemStack.getItem(), (item, count) -> count - itemStack.getCount());
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

		this.setContent(content);
		this.title = title;
	}

	@Override
	public boolean stillValid(Player p_38874_) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == RESULT_SLOT) {
				if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index == INPUT_SLOT) {
				if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (itemstack1.isEdible()) {
				if (!this.moveItemStackTo(itemstack1, INPUT_SLOT, INPUT_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= INV_SLOT_START && index < INV_SLOT_END) {
				if (!this.moveItemStackTo(itemstack1, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && !this.moveItemStackTo(itemstack1, INV_SLOT_START, INV_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			}

			slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemstack1);
			this.broadcastChanges();
		}

		return itemstack;
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
		if(count >= item.getMaxStackSize()) {
			this.resultSlot.set(new ItemStack(item, item.getMaxStackSize()));
		} else {
			this.resultSlot.set(new ItemStack(item, count));
		}

		this.broadcastChanges();
	}

	private boolean isValidItemIndex(int index) {
		return index >= 0 && index < this.items.size();
	}

	@SuppressWarnings("deprecation")
	private CompoundTag getContent() {
		CompoundTag ret = new CompoundTag();
		ret.putDouble("temperature", this.getTemperature());
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
		this.items = this.stackedItems.keySet().stream().toList();
	}

	public void setStackedItems(Map<Item, Integer> stackedItems) {
		this.stackedItems.clear();
		this.stackedItems.putAll(stackedItems);
	}

	public double getTemperature() {
		return this.data.get(0) / 100.0D;
	}
	public void setTemperature(double temperature) {
		this.data.set(0, (int)(temperature * 100.0D));
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

	@Override
	public void removed(Player player) {
		super.removed(player);
		if(player instanceof ServerPlayer serverPlayer) {
			ItemStack itemStack = new ItemStack(FIAHIItems.FOOD_POUCH);
			itemStack.setTag(this.getContent());
			itemStack.setHoverName(this.title);
			serverPlayer.level.addFreshEntity(new ItemEntity(
					serverPlayer.getLevel(), serverPlayer.getX(), serverPlayer.getY() + 0.5D, serverPlayer.getZ(), itemStack
			));
		}
	}
}
