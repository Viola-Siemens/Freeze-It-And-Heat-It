package com.hexagram2021.fiahi.client.util;

import com.hexagram2021.fiahi.client.FIAHIClientContent;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FoodItemStackRenderUtil {
	public static void renderSpecialFood(PoseStack transform, Slot slot, int blitOffset) {
		render(transform, slot.getItem(), blitOffset, slot.x, slot.y);
	}
	public static void renderSpecialFood(ItemStack itemStack, int blitOffset, int x, int y) {
		render(new PoseStack(), itemStack, blitOffset, x, y);
	}

	private static void render(PoseStack transform, ItemStack itemStack, int blitOffset, int x, int y) {
		if(itemStack.isEdible()) {
			itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
				TextureAtlasSprite sprite = null;
				int level = 0;
				if (c.getFrozenLevel() > 0) {
					sprite = FIAHIClientContent.FROZEN_SPRITE;
					level = c.getFrozenLevel();
				}
				if (c.getRottenLevel() > 0) {
					sprite = FIAHIClientContent.ROTTEN_SPRITE;
					level = c.getRottenLevel();
				}
				if (sprite != null && level > 0) {
					RenderStateShard.TRANSLUCENT_TRANSPARENCY.setupRenderState();
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, ((float) level + 1.0F) / 4.0F);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, sprite.atlas().location());
					GuiComponent.blit(transform, x, y, blitOffset + 100, 16, 16, sprite);
					RenderStateShard.TRANSLUCENT_TRANSPARENCY.clearRenderState();
				}
			});
		}
	}
}
