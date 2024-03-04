package com.hexagram2021.fiahi.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(SpriteLoader.class)
public abstract class SpriteLoaderMixin {
	@Shadow @Final
	private ResourceLocation location;

	@Unique
	private static final int COLD_COLOR = 0xfff0fcff;
	@Unique
	private static final int HOT_COLOR = 0xff030800;

	@SuppressWarnings("deprecation")
	@ModifyVariable(method = "stitch", at = @At("HEAD"), argsOnly = true, index = 1)
	public List<SpriteContents> fiahi$modifyStitchContents(List<SpriteContents> contents) {
		if (this.location.equals(TextureAtlas.LOCATION_BLOCKS)) {
			ArrayList<SpriteContents> extendedContents = new ArrayList<>(contents);

			contents.stream()
					.filter(content -> content != null && (content.name().getPath().contains("item/") || content.name().getPath().contains("items/")))
					.forEach(content -> {
						for (int level = 1; level <= 3; ++level) {
							NativeImage coldImage = new NativeImage(content.getOriginalImage().format(), content.getOriginalImage().getWidth(), content.getOriginalImage().getHeight(), true);
							coldImage.copyFrom(content.getOriginalImage());
							this.fiahi$remapColdImage(coldImage, Pair.of(content.width(), content.height()), level);
							SpriteContents frozenContent = new SpriteContents(
									content.name().withSuffix(".frozen.%d".formatted(level)),
									new FrameSize(content.width(), content.height()),
									coldImage,
									AnimationMetadataSection.EMPTY,
									content.forgeMeta
							);
							frozenContent.animatedTexture = content.animatedTexture;
							extendedContents.add(frozenContent);

							NativeImage hotImage = new NativeImage(content.getOriginalImage().format(), content.getOriginalImage().getWidth(), content.getOriginalImage().getHeight(), true);
							hotImage.copyFrom(content.getOriginalImage());
							this.fiahi$remapHotImage(hotImage, Pair.of(content.width(), content.height()), level);
							SpriteContents rottenContent = new SpriteContents(
									content.name().withSuffix(".rotten.%d".formatted(level)),
									new FrameSize(content.width(), content.height()),
									hotImage,
									AnimationMetadataSection.EMPTY,
									content.forgeMeta
							);
							rottenContent.animatedTexture = content.animatedTexture;
							extendedContents.add(rottenContent);
						}
					});

			return extendedContents;
		}
		return contents;
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
