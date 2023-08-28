package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.effect.ShiverEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIMobEffects {
	private static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

	public static final RegistryObject<MobEffect> SHIVER = REGISTER.register("shiver", ShiverEffect::new);

	private FIAHIMobEffects() {
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
