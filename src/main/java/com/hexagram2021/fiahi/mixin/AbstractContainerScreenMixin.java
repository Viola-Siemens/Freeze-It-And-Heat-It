package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.client.util.FoodItemStackRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {

	@SuppressWarnings("unchecked")
	@Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
	public void renderFoodSpecialTexture(PoseStack transform, Slot slot, CallbackInfo ci) {
		int blitOffset = ((AbstractContainerScreen<T>)(Object)this).getBlitOffset();
		FoodItemStackRenderUtil.renderSpecialFood(transform, slot, blitOffset);
	}
}
