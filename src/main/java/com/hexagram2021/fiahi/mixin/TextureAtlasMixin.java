package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.client.util.DummyResourceLocation;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.*;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
	@Shadow
	protected abstract ResourceLocation getResourceLocation(ResourceLocation textureLocation);

	@Unique
	private static final int COLD_COLOR = 0xffffffff;
	@Unique
	private static final int HOT_COLOR = 0xff000000;

	@Inject(method = "getBasicSpriteInfos", at = @At("RETURN"), cancellable = true)
	public void fiahi$getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set, CallbackInfoReturnable<Collection<TextureAtlasSprite.Info>> cir) {
		ArrayList<TextureAtlasSprite.Info> extendedInfos = new ArrayList<>(cir.getReturnValue());

		cir.getReturnValue().stream()
				.filter(info -> info.name().getPath().contains("item/"))
				.forEach(info -> {
					for(int level = 1; level <= 3; ++level) {
						extendedInfos.add(new TextureAtlasSprite.Info(
								new DummyResourceLocation(info.name().getNamespace(), info.name().getPath().concat(".frozen.%d".formatted(level)), info.name(), COLD_COLOR, level),
								info.width(), info.height(), info.metadata
						));
						extendedInfos.add(new TextureAtlasSprite.Info(
								new DummyResourceLocation(info.name().getNamespace(), info.name().getPath().concat(".rotten.%d".formatted(level)), info.name(), HOT_COLOR, level),
								info.width(), info.height(), info.metadata
						));
					}
				});

		cir.setReturnValue(extendedInfos);
	}

	@SuppressWarnings("DataFlowIssue")
	@Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true)
	public void fiahi$load(ResourceManager resourceManager, TextureAtlasSprite.Info spriteInfo, int width, int height, int mipLevel, int originX, int originY, CallbackInfoReturnable<TextureAtlasSprite> cir) {
		if (spriteInfo.name() instanceof DummyResourceLocation dummyId) {
			ResourceLocation originTextureLocation = this.getResourceLocation(dummyId.getOrigin());

			Optional<Resource> resource = resourceManager.getResource(originTextureLocation);
			if(resource.isEmpty()) {
				return;
			}

			try {
				NativeImage nativeImage = NativeImage.read(resource.get().open());
				switch(dummyId.getColor()) {
					case COLD_COLOR -> fiahi$remapColdImage(nativeImage, dummyId.getLevel());
					case HOT_COLOR -> fiahi$remapHotImage(nativeImage, dummyId.getLevel());
				}

				cir.setReturnValue(new TextureAtlasSprite((TextureAtlas) (Object) this, spriteInfo, mipLevel, width, height, originX, originY, nativeImage));
			} catch (RuntimeException runtimeexception) {
				FIAHILogger.error("Unable to parse metadata from %s".formatted(originTextureLocation), runtimeexception);
				cir.setReturnValue(null);
			} catch (IOException ioexception) {
				FIAHILogger.error("Using missing texture, unable to load %s".formatted(originTextureLocation), ioexception);
				cir.setReturnValue(null);
			}
		}
	}

	@Unique
	private void fiahi$remapColdImage(NativeImage image, int level) {
		Random source = new Random();
		source.setSeed(19260817L);

		int targetBlue = FastColor.ARGB32.blue(COLD_COLOR);
		int targetGreen = FastColor.ARGB32.green(COLD_COLOR);
		int targetRed = FastColor.ARGB32.red(COLD_COLOR);
		double blendRate = (level + 1.0D) / 4.0D;

		for (int x = 0; x < image.getWidth(); ++x) {
			int firstPixelY = -1;
			for (int y = image.getHeight() - 1; y >= 0; --y) {
				int originalColor = image.getPixelRGBA(x, y);

				int alpha = FastColor.ARGB32.alpha(originalColor);
				int blue = FastColor.ARGB32.blue(originalColor);
				int green = FastColor.ARGB32.green(originalColor);
				int red = FastColor.ARGB32.red(originalColor);
				if(alpha >= 8) {
					if(firstPixelY < 0) {
						firstPixelY = y;
						if(source.nextInt(level + 2) >= 2) {
							int d = source.nextInt(level + 2);
							if(d > 4) {
								d = 4;
							}
							for (int dy = y + 1; dy < image.getHeight() && dy < y + d; ++dy) {
								image.setPixelRGBA(x, dy, FastColor.ARGB32.color(0xff, targetRed, targetGreen, targetBlue));
							}
						}
					}
					double blend = blendRate * (1.28D - (firstPixelY - y + (1 + source.nextInt(3)) / 4.0D) * 0.32D);
					if(blend <= blendRate * 0.2D) {
						blend = blendRate * 0.175D + source.nextInt(4) / 80.0D;
					}
					blue  = fiahi$clampRGB((int)(blue  * (1.0D - blend) + targetBlue  * blend));
					green = fiahi$clampRGB((int)(green * (1.0D - blend) + targetGreen * blend));
					red   = fiahi$clampRGB((int)(red   * (1.0D - blend) + targetRed   * blend));
					image.setPixelRGBA(x, y, FastColor.ARGB32.color(alpha, red, green, blue));
				}
			}
		}
	}

	@Unique
	private void fiahi$remapHotImage(NativeImage image, int level) {
		Random source = new Random();
		source.setSeed(19260817L);

		int targetBlue = FastColor.ARGB32.blue(HOT_COLOR);
		int targetGreen = FastColor.ARGB32.green(HOT_COLOR);
		int targetRed = FastColor.ARGB32.red(HOT_COLOR);
		double blendRate = (level + 1.0D) / 5.0D;

		double xCenter = (image.getWidth() - 1.0D) / 2.0D;
		double yCenter = (image.getHeight() - 1.0D) / 2.0D;
		for (int x = 0; x < image.getWidth(); ++x) {
			for (int y = 0; y < image.getHeight(); ++y) {
				int originalColor = image.getPixelRGBA(x, y);

				int alpha = FastColor.ARGB32.alpha(originalColor);
				int blue = FastColor.ARGB32.blue(originalColor);
				int green = FastColor.ARGB32.green(originalColor);
				int red = FastColor.ARGB32.red(originalColor);
				if(alpha >= 8) {
					double distToCenter = Math.sqrt(Math.pow(x - xCenter, 2) + Math.pow(y - yCenter, 2));
					double blend = Math.pow((8.0D - distToCenter) / 7.5D * blendRate * (1.0D + source.nextDouble() * 0.2D), 2.0D);
					if(blend <= 0.0D) {
						break;
					}
					blue  = fiahi$clampRGB((int)(blue  * (1.0D - blend) + targetBlue  * blend));
					green = fiahi$clampRGB((int)(green * (1.0D - blend) + targetGreen * blend));
					red   = fiahi$clampRGB((int)(red   * (1.0D - blend) + targetRed   * blend));
					image.setPixelRGBA(x, y, FastColor.ARGB32.color(alpha, red, green, blue));
				}
			}
		}
	}

	@Unique
	private static int fiahi$clampRGB(int v) {
		return Mth.clamp(v, 0, 255);
	}
}
