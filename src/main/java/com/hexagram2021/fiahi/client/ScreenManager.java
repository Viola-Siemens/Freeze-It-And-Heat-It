package com.hexagram2021.fiahi.client;

import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.hexagram2021.fiahi.register.FIAHIParticleTypes;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.Vec3;

public class ScreenManager {
	private static int retry = 5;

	@SuppressWarnings("BusyWait")
	public static void openFoodPouchScreen(FoodPouchData foodPouchData, int containerId) {
		LocalPlayer player = Minecraft.getInstance().player;
		if(player != null) {
			retry = 5;
			new Thread(() -> {
				while(retry != 0) {
					retry -= 1;
					if(!tryOpenFoodPouchScreen(player, foodPouchData, containerId)) {
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

	private static boolean tryOpenFoodPouchScreen(LocalPlayer player, FoodPouchData foodPouchData, int containerId) {
		AbstractContainerMenu menu = player.containerMenu;
		if(menu.containerId == containerId && menu instanceof FoodPouchMenu foodPouchMenu) {
			foodPouchMenu.setContent(foodPouchData);
			foodPouchMenu.runSlotUpdateListener();
			return true;
		}
		return false;
	}

	public static void makePlayerBreatheParticle(Player player) {
		if(Overlays.WORLD_TEMP < 2.0D) {
			RandomSource random = player.getRandom();
			float yRot = player.getYRot() * Mth.DEG_TO_RAD;
			float xRot = player.getXRot() * Mth.DEG_TO_RAD;
			float cosXRot = Mth.cos(xRot);
			float deltaX = -Mth.sin(yRot) * cosXRot;
			float deltaY = -Mth.sin(xRot);
			float deltaZ = Mth.cos(yRot) * cosXRot;
			Vec3 deltaMovement = player.getDeltaMovement();
			for(int ignored = 0; ignored < 4; ++ignored) {
				player.level().addParticle(
						FIAHIParticleTypes.BREATHE_OUT.get(),
						player.getX() + deltaX * 0.5F, player.getY() + player.getEyeHeight() - 0.2D + deltaY * 0.5F, player.getZ() + deltaZ * 0.5F,
						deltaX * 0.15F + 0.05F * random.nextFloat() + 0.8F * deltaMovement.x,
						0.025F + 0.8F * deltaMovement.y,
						deltaZ * 0.15F + 0.05F * random.nextFloat() + 0.8F * deltaMovement.z
				);
			}
		}
	}
}
