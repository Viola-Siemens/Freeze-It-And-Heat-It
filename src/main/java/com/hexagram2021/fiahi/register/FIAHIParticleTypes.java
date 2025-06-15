package com.hexagram2021.fiahi.register;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIParticleTypes {
	private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

	public static final RegistryObject<SimpleParticleType> BREATHE_OUT = REGISTER.register("breathe_out", () -> new SimpleParticleType(false));

	private FIAHIParticleTypes() {
	}

	public static void init(IEventBus bus) {
		REGISTER.register(bus);
	}
}
