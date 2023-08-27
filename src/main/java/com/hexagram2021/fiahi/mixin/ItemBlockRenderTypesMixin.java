package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "RETURN"), cancellable = true)
	private static void getFrozenRottenRenderType(ItemStack itemStack, boolean translucentCullBlock, CallbackInfoReturnable<RenderType> cir) {
		if(itemStack.isEdible()) {
			IFrozenRottenFood c = itemStack.getCapability(FIAHICapabilities.FOOD_CAPABILITY).orElse(null);
			if(c != null) {
				int f = Mth.clamp(c.getFrozenLevel(), 0, 3);
				if(f > 0) {
					cir.setReturnValue(getFrozenRenderType(f, cir.getReturnValue() == Sheets.translucentItemSheet(), translucentCullBlock));
					cir.cancel();
				}
				int r = Mth.clamp(c.getRottenLevel(), 0, 3);
				if(r > 0) {
					cir.setReturnValue(getRottenRenderType(r, cir.getReturnValue() == Sheets.translucentItemSheet(), translucentCullBlock));
					cir.cancel();
				}
			}
		}
	}

	private static RenderType getFrozenRenderType(int level, boolean translucent, boolean translucentCullBlock) {
		return Minecraft.useShaderTransparency() && translucent ? FIAHIRenderTypes.frozenTranslucent(level) : translucentCullBlock ? FIAHIRenderTypes.frozenDirect(level) : FIAHIRenderTypes.frozen(level);
	}

	private static RenderType getRottenRenderType(int level, boolean translucent, boolean translucentCullBlock) {
		return Minecraft.useShaderTransparency() && translucent ? FIAHIRenderTypes.rottenTranslucent(level) : translucentCullBlock ? FIAHIRenderTypes.rottenDirect(level) : FIAHIRenderTypes.rotten(level);
	}
}
