package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.common.effect.ShiverEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public final class FIAHIMobEffects {
	private static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, MODID);

	public static final DeferredHolder<MobEffect, MobEffect> SHIVER = REGISTER.register("shiver", ShiverEffect::new);

	private FIAHIMobEffects() {
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
