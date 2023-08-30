package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;

import java.util.Map;

public class ScreenManager {
	public static void openFoodPouchScreen(Map<Item, Integer> stackedItems, int containerId) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(player != null) {
			AbstractContainerMenu menu = player.containerMenu;
			if(menu.containerId == containerId && menu instanceof FoodPouchMenu foodPouchMenu) {
				foodPouchMenu.setStackedItems(stackedItems);
				foodPouchMenu.runSlotUpdateListener();
			}
		}
	}
}
