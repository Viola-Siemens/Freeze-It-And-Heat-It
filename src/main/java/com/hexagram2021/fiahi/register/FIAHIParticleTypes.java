package com.hexagram2021.fiahi.register;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIParticleTypes {
	private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BREATHE_OUT = REGISTER.register("breathe_out", () -> new SimpleParticleType(false));

	private FIAHIParticleTypes() {
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
