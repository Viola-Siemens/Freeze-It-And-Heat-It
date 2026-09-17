package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.ForgeEventHandler;
import com.hexagram2021.fiahi.common.item.capability.IFrozenRottenFood;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 玩家物品栏 Mixin<br/>
 * 注入 tick 逻辑，根据玩家体温修改玩家物品栏中食物的温度
 *
 * @author liudongyu
 */
@Mixin(Inventory.class)
public class InventoryMixin {
	@Shadow @Final
	private List<NonNullList<ItemStack>> compartments;

	/** 所属玩家 */
	@Shadow @Final
	public Player player;

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void fiahi$convertFoodIntoLeftoverIfFullyRotten(CallbackInfo ci) {
		Level level = this.player.level();
		if(level instanceof ServerLevel serverLevel && ForgeEventHandler.isAvailableToTickFood(serverLevel.dimension())) {
			for (NonNullList<ItemStack> itemStackList : this.compartments) {
				for (int i = 0; i < itemStackList.size(); ++i) {
					if (!itemStackList.get(i).isEmpty()) {
						ItemStack food = itemStackList.get(i);
						double temp = Temperature.get(this.player, Temperature.Trait.CORE);
						int finalI = i;
						IFrozenRottenFood.tick(food, itemStack -> itemStackList.set(finalI, itemStack), c -> (temp + 2.0D * c.getTemperature()) / 3.0D, serverLevel, this.player);
					}
				}
			}
		}
	}
}
