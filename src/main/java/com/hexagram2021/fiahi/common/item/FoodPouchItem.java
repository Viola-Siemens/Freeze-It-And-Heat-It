package com.hexagram2021.fiahi.common.item;

import com.hexagram2021.fiahi.FreezeItAndHeatIt;
import com.hexagram2021.fiahi.common.menu.FoodPouchMenu;
import com.hexagram2021.fiahi.common.network.ClientboundFoodPouchPacket;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

	public static int getItemCount(CompoundTag nbt) {
		if(!nbt.contains("Items", Tag.TAG_LIST)) {
			return 0;
		}
		int ret = 0;
		ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
		for(Tag tag: list) {
			CompoundTag compoundTag = (CompoundTag)tag;
			int count = compoundTag.getInt("Count");
			ret += count;
		}
		return ret;
	}
}
