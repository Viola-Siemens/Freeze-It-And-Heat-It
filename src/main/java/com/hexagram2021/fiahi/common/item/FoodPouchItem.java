package com.hexagram2021.fiahi.common.item;

import com.hexagram2021.fiahi.common.item.capability.impl.FoodPouchData;
import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.hexagram2021.fiahi.common.network.ClientboundFoodPouchPacketPayload;
import com.hexagram2021.fiahi.register.FIAHIAttachmentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.OptionalInt;

public class FoodPouchItem extends Item {
	public FoodPouchItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		FoodPouchData attachment = itemStack.get(FIAHIAttachmentTypes.FOOD_POUCH_DATA);
		FoodPouchData content = attachment == null ? FoodPouchData.EMPTY : attachment;
		Component title = itemStack.getHoverName().copy();
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			OptionalInt containerId = serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, player1) -> new FoodPouchMenu(id, inventory, title, content), title));
			if (containerId.isPresent() && serverPlayer.containerMenu instanceof FoodPouchMenu menu) {
				serverPlayer.connection.send(new ClientboundFoodPouchPacketPayload(menu.getContent(), containerId.getAsInt()));
			}
		}
		itemStack.shrink(1);
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	public static int getItemCount(FoodPouchData foodPouchData) {
		return foodPouchData.items().stream().map(ItemStack::getCount).reduce(0, Integer::sum);
	}
}
