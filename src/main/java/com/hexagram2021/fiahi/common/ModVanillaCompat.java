package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.register.FIAHIItems;
import net.minecraft.world.level.block.ComposterBlock;

public class ModVanillaCompat {
	public static void setup() {
		ComposterBlock.COMPOSTABLES.put(FIAHIItems.LEFTOVER_MEAT.get(), 0.65F);
		ComposterBlock.COMPOSTABLES.put(FIAHIItems.LEFTOVER_VEGETABLE.get(), 0.65F);
	}
}
