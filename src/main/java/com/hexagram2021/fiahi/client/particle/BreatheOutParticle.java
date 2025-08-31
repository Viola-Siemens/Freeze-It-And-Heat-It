package com.hexagram2021.fiahi.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BreatheOutParticle extends TextureSheetParticle {
	BreatheOutParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.speedUpWhenYMotionIsBlocked = true;
		this.friction = 0.86F;
		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
		this.quadSize *= 5.0F;
		this.lifetime = 16;
		this.hasPhysics = false;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public float getQuadSize(float ticks) {
		return this.quadSize * Mth.clamp(((float)this.age + ticks) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			BreatheOutParticle particle = new BreatheOutParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(this.sprite);
			particle.setColor(1.0F, 1.0F, 1.0F);
			return particle;
		}
	}
}
