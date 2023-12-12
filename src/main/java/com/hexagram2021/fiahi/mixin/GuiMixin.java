package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.client.util.FoodItemStackRenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderSlot(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
	public void fiahi$renderSlot(int x, int y, float time, Player player, ItemStack itemStack, int count, CallbackInfo ci) {
		int blitOffset = ((Gui)(Object)this).getBlitOffset();
		FoodItemStackRenderUtil.renderSpecialFood(itemStack, blitOffset, x, y);
	}
}
