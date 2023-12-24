package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.client.util.DummyResourceLocation;
import com.hexagram2021.fiahi.common.util.FIAHILogger;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
	@Shadow
	protected abstract ResourceLocation getResourceLocation(ResourceLocation textureLocation);

	@Unique
	private static final int COLD_COLOR = 0xfff0fcff;
	@Unique
	private static final int HOT_COLOR = 0xff030800;

	@Inject(method = "getBasicSpriteInfos", at = @At("RETURN"), cancellable = true)
	public void fiahi$getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set, CallbackInfoReturnable<Collection<TextureAtlasSprite.Info>> cir) {
		ArrayList<TextureAtlasSprite.Info> extendedInfos = new ArrayList<>(cir.getReturnValue());

		cir.getReturnValue().stream()
				.filter(info -> info != null && (info.name().getPath().contains("item/") || info.name().getPath().contains("items/")))
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

	@SuppressWarnings({"DataFlowIssue", "UnstableApiUsage"})
	@Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true)
	public void fiahi$load(ResourceManager resourceManager, TextureAtlasSprite.Info spriteInfo, int width, int height, int mipLevel, int originX, int originY, CallbackInfoReturnable<TextureAtlasSprite> cir) {
		TextureAtlas current = (TextureAtlas)(Object) this;
		if (spriteInfo.name() instanceof DummyResourceLocation dummyId) {
			ResourceLocation originTextureLocation = this.getResourceLocation(dummyId.getOrigin());

			Optional<Resource> resource = resourceManager.getResource(originTextureLocation);
			if(resource.isEmpty()) {
				return;
			}

			try(InputStream inputStream = resource.get().open()) {
				NativeImage nativeImage = NativeImage.read(inputStream);
				switch(dummyId.getColor()) {
					case COLD_COLOR -> fiahi$remapColdImage(nativeImage, spriteInfo.metadata.getFrameSize(spriteInfo.width(), spriteInfo.height()), dummyId.getLevel());
					case HOT_COLOR -> fiahi$remapHotImage(nativeImage, spriteInfo.metadata.getFrameSize(spriteInfo.width(), spriteInfo.height()), dummyId.getLevel());
				}
				TextureAtlasSprite ret = ForgeHooksClient.loadTextureAtlasSprite(current, resourceManager, spriteInfo, resource.get(), width, height, originX, originY, mipLevel, nativeImage);
				cir.setReturnValue(ret == null ? new TextureAtlasSprite(current, spriteInfo, mipLevel, width, height, originX, originY, nativeImage) : ret);
			} catch (RuntimeException e) {
				FIAHILogger.error("Unable to parse metadata from %s".formatted(originTextureLocation), e);
				cir.setReturnValue(null);
			} catch (IOException e) {
				FIAHILogger.error("Using missing texture, unable to load %s".formatted(originTextureLocation), e);
				cir.setReturnValue(null);
			}
		}
	}

	@Unique
	private void fiahi$remapColdImage(NativeImage image, Pair<Integer, Integer> size, int level) {
		int targetBlue = FastColor.ARGB32.blue(COLD_COLOR);
		int targetGreen = FastColor.ARGB32.green(COLD_COLOR);
		int targetRed = FastColor.ARGB32.red(COLD_COLOR);
		double blendRate = (level + 1.0D) / 4.0D;
		int xFrameCount = image.getWidth() / size.getFirst();
		int yFrameCount = image.getHeight() / size.getSecond();

		for(int frameX = 0; frameX < xFrameCount; ++frameX) {
			for(int frameY = 0; frameY < yFrameCount; ++frameY) {
				Random source = new Random();
				source.setSeed(19260817L);
				for (int x = 0; x < size.getFirst(); ++x) {
					int firstPixelY = -1;
					for (int y = size.getSecond() - 1; y >= 0; --y) {
						int originalColor = image.getPixelRGBA(x + size.getFirst() * frameX, y + size.getSecond() * frameY);

						int alpha = FastColor.ARGB32.alpha(originalColor);
						int blue = FastColor.ARGB32.blue(originalColor);
						int green = FastColor.ARGB32.green(originalColor);
						int red = FastColor.ARGB32.red(originalColor);
						if (alpha > 0xf) {
							if (firstPixelY < 0) {
								firstPixelY = y;
								if (source.nextInt(level + 2) >= 2) {
									int d = source.nextInt(level + 2);
									if (d > 4) {
										d = 4;
									}
									int dyBound = Math.min(y + d, size.getSecond());
									for (int dy = y + 1; dy < dyBound; ++dy) {
										image.setPixelRGBA(x + size.getFirst() * frameX, dy + size.getSecond() * frameY, FastColor.ARGB32.color(0xff, targetRed, targetGreen, targetBlue));
									}
								}
							}
							double blend = blendRate * (1.28D - (firstPixelY - y + (1 + source.nextInt(3)) / 4.0D) * 0.32D);
							if (blend <= blendRate * 0.2D) {
								blend = blendRate * 0.175D + source.nextInt(4) / 80.0D;
							}
							blue = fiahi$clampRGB((int) (blue * (1.0D - blend) + targetBlue * blend));
							green = fiahi$clampRGB((int) (green * (1.0D - blend) + targetGreen * blend));
							red = fiahi$clampRGB((int) (red * (1.0D - blend) + targetRed * blend));
							image.setPixelRGBA(x + size.getFirst() * frameX, y + size.getSecond() * frameY, FastColor.ARGB32.color(0xff, red, green, blue));
						}
					}
				}
			}
		}
	}

	@Unique
	private void fiahi$remapHotImage(NativeImage image, Pair<Integer, Integer> size, int level) {
		int targetBlue = FastColor.ARGB32.blue(HOT_COLOR);
		int targetGreen = FastColor.ARGB32.green(HOT_COLOR);
		int targetRed = FastColor.ARGB32.red(HOT_COLOR);
		double blendRate = (level + 1.0D) / 5.0D;
		int xFrameCount = image.getWidth() / size.getFirst();
		int yFrameCount = image.getHeight() / size.getSecond();

		for(int frameX = 0; frameX < xFrameCount; ++frameX) {
			for (int frameY = 0; frameY < yFrameCount; ++frameY) {
				Random source = new Random();
				source.setSeed(19260817L);
				//centroid
				double xCenter = (size.getFirst() - 1.0D) / 2.0D;
				double yCenter = (size.getSecond() - 1.0D) / 2.0D;
				double cnt = 0.0D;
				for (int x = 0; x < size.getFirst(); ++x) {
					for (int y = 0; y < size.getSecond(); ++y) {
						int originalColor = image.getPixelRGBA(x + size.getFirst() * frameX, y + size.getSecond() * frameY);
						int alpha = FastColor.ARGB32.alpha(originalColor);
						if (alpha > 0xf) {
							cnt += 1.0D;
							xCenter += x;
							yCenter += y;
						}
					}
				}
				xCenter /= cnt;
				yCenter /= cnt;
				for (int x = 0; x < size.getFirst(); ++x) {
					for (int y = 0; y < size.getSecond(); ++y) {
						int originalColor = image.getPixelRGBA(x + size.getFirst() * frameX, y + size.getSecond() * frameY);

						int alpha = FastColor.ARGB32.alpha(originalColor);
						int blue = FastColor.ARGB32.blue(originalColor);
						int green = FastColor.ARGB32.green(originalColor);
						int red = FastColor.ARGB32.red(originalColor);
						if (alpha > 0xf) {
							double distToCenter = Math.sqrt(Math.pow(x - xCenter, 2) + Math.pow(y - yCenter, 2));
							double blend = (7.5D - distToCenter) / 7.5D * blendRate * (1.0D + source.nextDouble() * 0.2D);
							if (blend <= 0.0D) {
								continue;
							}
							blue = fiahi$clampRGB((int) (blue * (1.0D - blend) + targetBlue * blend));
							green = fiahi$clampRGB((int) (green * (1.0D - blend) + targetGreen * blend));
							red = fiahi$clampRGB((int) (red * (1.0D - blend) + targetRed * blend));
							image.setPixelRGBA(x + size.getFirst() * frameX, y + size.getSecond() * frameY, FastColor.ARGB32.color(0xff, red, green, blue));
						}
					}
				}
			}
		}
	}

	@Unique
	private static int fiahi$clampRGB(int v) {
		return Mth.clamp(v, 0, 255);
	}
}
