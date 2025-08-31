package com.hexagram2021.fiahi.common.menu;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodKey;
import com.hexagram2021.fiahi.common.util.RegistryHelper;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.hexagram2021.fiahi.register.FIAHIMenuTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

	final Map<PouchedFoodKey, Integer> stackedItems = new HashMap<>();

	List<PouchedFoodKey> items = new ArrayList<>();

	@Nullable
	private final Component title;

	public FoodPouchMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, null, FoodPouchData.EMPTY);
	}

	public FoodPouchMenu(int containerId, Inventory inventory, @Nullable Component title, FoodPouchData content) {
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
					PouchedFoodKey key = getKeyFromItem(itemStack);
					if(FoodPouchMenu.this.stackedItems.containsKey(key) || FoodPouchMenu.this.stackedItems.size() < MAX_FOOD_TYPES) {
						IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
						if(c != null) {
							int totalCount = FoodPouchMenu.this.stackedItems.values().stream().reduce(0, Integer::sum);
							FoodPouchMenu.this.stackedItems.compute(key, (item, count) -> {
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
						}
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
				PouchedFoodKey key = getKeyFromItem(itemStack);
				FoodPouchMenu.this.stackedItems.computeIfPresent(key, (item, count) -> {
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

	void setupResultSlot() {
		int index = this.getSelectedIndex();
		if(index < 0 || index >= this.items.size()) {
			return;
		}
		PouchedFoodKey key = this.items.get(this.getSelectedIndex());
		int count = this.stackedItems.get(key);
		if(count <= 0) {
			return;
		}
		ItemStack itemStack;
		if(count >= key.item().getDefaultMaxStackSize()) {
			itemStack = new ItemStack(key.item(), key.item().getDefaultMaxStackSize());
		} else {
			itemStack = new ItemStack(key.item(), count);
		}
		IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY);
		if(c != null) {
			c.setTemperature(this.getTemperature());
			c.updateFoodTag();
		}
		for(IPouchedFoodData<?> data: key.datas()) {
			data.modifyStack(itemStack);
		}
		this.resultSlot.set(itemStack);

		this.broadcastChanges();
	}

	private boolean isValidItemIndex(int index) {
		return index >= 0 && index < this.items.size();
	}

	public FoodPouchData getContent() {
		return new FoodPouchData(this.getTemperature(), this.stackedItems.entrySet().stream().map(entry -> new ItemStack(entry.getKey().item(), entry.getValue())).toList());
	}

	public void setContent(FoodPouchData content) {
		this.setTemperature(content.temperature());
		this.stackedItems.clear();
		for(ItemStack itemStack: content.items()) {
			this.stackedItems.put(getKeyFromItem(itemStack), itemStack.getCount());
		}
		this.maintainItems();
	}

	public void registerUpdateListener(Runnable runnable) {
		this.slotUpdateListener = runnable;
	}

	private void maintainItems() {
		this.items = this.stackedItems.keySet().stream().sorted(Comparator.comparing(key -> RegistryHelper.getRegistryName(key.item()))).toList();
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

	public List<PouchedFoodKey> getStackedItems() {
		return this.items;
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
			itemStack.set(FIAHIAttachmentTypes.FOOD_POUCH_DATA, this.getContent());
			itemStack.set(DataComponents.CUSTOM_NAME, this.title);
			if (!serverPlayer.addItem(itemStack)) {
				serverPlayer.drop(itemStack, false);
			}
		}
	}

	public static PouchedFoodKey getKeyFromItem(ItemStack itemStack) {
		return IPouchedFoodData.tryLoad(itemStack);
	}
}
