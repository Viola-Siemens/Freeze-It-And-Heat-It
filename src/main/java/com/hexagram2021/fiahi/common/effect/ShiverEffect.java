package com.hexagram2021.fiahi.common.effect;

import com.hexagram2021.fiahi.register.FIAHIMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
		} else {
			if (entity.getLastHurtByMobTimestamp() == entity.tickCount + 1) {
				MobEffectInstance instance = entity.getEffect(FIAHIMobEffects.SHIVER.get());
				int duration = instance == null || instance.getDuration() < 100 ? 100 : instance.getDuration();
				entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, level, level * duration / (level + 1)));
			}
		}
	}
}
