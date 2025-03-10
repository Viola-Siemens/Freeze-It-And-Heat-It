package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.common.item.data.PouchedFoodKey;
import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Map;

public class ScreenManager {
	private static int retry = 5;

	@SuppressWarnings("BusyWait")
	public static void openFoodPouchScreen(Map<PouchedFoodKey, Integer> stackedItems, int containerId) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(player != null) {
			retry = 5;
			new Thread(() -> {
				while(retry != 0) {
					retry -= 1;
					if(!tryOpenFoodPouchScreen(player, stackedItems, containerId)) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							FIAHILogger.warn(e);
						}
					}
				}
			}, "FoodPouchOpener").start();
		}
	}

	private static boolean tryOpenFoodPouchScreen(LocalPlayer player, Map<PouchedFoodKey, Integer> stackedItems, int containerId) {
		AbstractContainerMenu menu = player.containerMenu;
		if(menu.containerId == containerId && menu instanceof FoodPouchMenu foodPouchMenu) {
			foodPouchMenu.setStackedItems(stackedItems);
			foodPouchMenu.runSlotUpdateListener();
			return true;
		}
		return false;
	}
}
