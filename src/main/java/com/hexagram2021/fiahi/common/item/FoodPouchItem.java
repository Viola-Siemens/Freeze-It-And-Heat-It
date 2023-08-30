package com.hexagram2021.fiahi.common.item;

import com.hexagram2021.fiahi.FreezeItAndHeatIt;
import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.hexagram2021.fiahi.common.network.ClientboundFoodPouchPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.OptionalInt;

public class FoodPouchItem extends Item {
	public FoodPouchItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		CompoundTag tag = itemStack.getOrCreateTag().copy();
		Component title = itemStack.getHoverName().copy();
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			OptionalInt containerId = serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, player1) -> new FoodPouchMenu(id, inventory, title, tag), title));
			if (containerId.isPresent() && serverPlayer.containerMenu instanceof FoodPouchMenu menu) {
				FreezeItAndHeatIt.packetHandler.send(
						PacketDistributor.PLAYER.with(() -> serverPlayer),
						new ClientboundFoodPouchPacket(menu.getItemsAndCounts(), containerId.getAsInt())
				);
			}
		}
		itemStack.shrink(1);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}
}
