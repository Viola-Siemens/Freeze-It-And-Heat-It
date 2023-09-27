package com.hexagram2021.fiahi.mixin.cold_sweat;

import com.momosoftworks.coldsweat.common.container.BoilerContainer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood.canBeFrozenRotten;

@Mixin(BoilerContainer.class)
public class BoilerContainerMixin {
	@Redirect(method = "quickMoveStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean quickMoveFood(ItemStack instance, TagKey<Item> key) {
		return canBeFrozenRotten(instance) || instance.is(key);
	}
}
