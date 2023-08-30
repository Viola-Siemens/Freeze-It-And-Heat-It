package com.hexagram2021.fiahi.client.screen;

import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FoodPouchScreen extends AbstractContainerScreen<FoodPouchMenu> {
	private static final ResourceLocation BG_LOCATION = new ResourceLocation(MODID, "textures/gui/container/food_pouch.png");
	private static final int FOOD_IMAGE_SIZE_WIDTH = 16;
	private static final int FOOD_IMAGE_SIZE_HEIGHT = 18;
	private static final int FOOD_X = 46;
	private static final int FOOD_Y = 32;
	private static final int TEMPERATURE_X = 48;
	private static final int TEMPERATURE_Y = 18;
	private static final int COUNT_X = 128;
	private static final int COUNT_Y = 56;

	@Nullable
	private List<ItemStack> stackedItems;

	public FoodPouchScreen(FoodPouchMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		menu.registerUpdateListener(this::containerChanged);
		--this.titleLabelY;
	}

	@Override
	public void render(PoseStack transform, int x, int y, float deltaFrameTime) {
		super.render(transform, x, y, deltaFrameTime);
		this.renderTooltip(transform, x, y);
	}

	@Override
	protected void renderBg(PoseStack transform, float partialTicks, int mouseX, int mouseY) {
		this.renderBackground(transform);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BG_LOCATION);
		int x = this.leftPos;
		int y = this.topPos;
		this.blit(transform, x, y, 0, 0, this.imageWidth, this.imageHeight);
		int buttonX = this.leftPos + FOOD_X;
		int buttonY = this.topPos + FOOD_Y;
		this.renderButtons(transform, mouseX, mouseY, buttonX, buttonY);
		this.renderFoods(buttonX, buttonY);
	}

	@Override
	protected void renderTooltip(PoseStack transform, int mouseX, int mouseY) {
		super.renderTooltip(transform, mouseX, mouseY);
		int buttonX = this.leftPos + FOOD_X;
		int buttonY = this.topPos + FOOD_Y;

		if(this.stackedItems != null && mouseY >= buttonY && mouseY < buttonY + FOOD_IMAGE_SIZE_HEIGHT) {
			for (int l = 0; l < this.stackedItems.size(); ++l) {
				int x = buttonX + l * FOOD_IMAGE_SIZE_WIDTH;
				if (mouseX >= x && mouseX < x + FOOD_IMAGE_SIZE_WIDTH) {
					this.renderTooltip(transform, this.stackedItems.get(l), mouseX, mouseY);
				}
			}
		}
	}

	private void renderButtons(PoseStack transform, int mouseX, int mouseY, int buttonX, int buttonY) {
		if(this.stackedItems != null) {
			for (int i = 0; i < this.stackedItems.size(); ++i) {
				int x = buttonX + i * FOOD_IMAGE_SIZE_WIDTH;
				int y = this.imageHeight;
				if (i == this.menu.getSelectedIndex()) {
					y += FOOD_IMAGE_SIZE_HEIGHT;
				} else if (mouseX >= x && mouseY >= buttonY && mouseX < x + FOOD_IMAGE_SIZE_WIDTH && mouseY < buttonY + FOOD_IMAGE_SIZE_HEIGHT) {
					y += FOOD_IMAGE_SIZE_HEIGHT * 2;
				}

				this.blit(transform, x, buttonY, 0, y, FOOD_IMAGE_SIZE_WIDTH, FOOD_IMAGE_SIZE_HEIGHT);
			}
		}
	}

	private void renderFoods(int buttonX, int buttonY) {
		if(this.stackedItems != null) {
			for(int i = 0; i < this.stackedItems.size(); ++i) {
				int x = buttonX + i * FOOD_IMAGE_SIZE_WIDTH;
				Objects.requireNonNull(this.minecraft).getItemRenderer().renderAndDecorateItem(this.stackedItems.get(i), x, buttonY);
			}
		}
	}

	@Override
	public void renderLabels(PoseStack transform, int mouseX, int mouseY) {
		super.renderLabels(transform, mouseX, mouseY);
		this.font.draw(
				transform, new TranslatableComponent("gui.fiahi.temperature.description", this.menu.getTemperature()),
				TEMPERATURE_X, TEMPERATURE_Y, 0x404040
		);
		this.font.draw(
				transform,
				new TranslatableComponent("gui.fiahi.count.description", this.menu.getItemStockCount()),
				COUNT_X, COUNT_Y, 0x404040
		);
	}

	@Override
	public boolean mouseClicked(double x, double y, int mouseButton) {
		if(this.stackedItems != null) {
			int buttonX = this.leftPos + FOOD_X;
			int buttonY = this.topPos + FOOD_Y;
			for(int i = 0; i < this.stackedItems.size(); ++i) {
				double deltaX = x - (double)(buttonX + i * FOOD_X);
				double deltaY = y - (double)buttonY;
				if (deltaX >= 0.0D && deltaY >= 0.0D && deltaX < FOOD_X && deltaY < FOOD_Y &&
						this.menu.clickMenuButton(Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player), i)) {
					Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
					Objects.requireNonNull(this.minecraft.gameMode).handleInventoryButtonClick((this.menu).containerId, i);
					return true;
				}
			}
		}
		return super.mouseClicked(x, y, mouseButton);
	}

	public void containerChanged() {
		this.stackedItems = this.menu.getStackedItems().stream().map(ItemStack::new).collect(Collectors.toList());
	}
}
