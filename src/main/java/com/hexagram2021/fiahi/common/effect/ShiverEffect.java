package com.hexagram2021.fiahi.common.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class ShiverEffect extends MobEffect {
	public ShiverEffect() {
		super(MobEffectCategory.HARMFUL, 0xb4f0f2);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int level) {
		if(entity.level.isClientSide) {
			Random random = entity.getRandom();
			entity.level.addParticle(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + 1.0D, entity.getZ(), Mth.randomBetween(random, -1.0F, 1.0F) / 12.0F, 0.05F, Mth.randomBetween(random, -1.0F, 1.0F) / 12.0F);
		}
	}
}
