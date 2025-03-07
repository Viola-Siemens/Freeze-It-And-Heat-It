package com.hexagram2021.fiahi.common.item.data.impl;

import com.hexagram2021.fiahi.common.item.data.IPouchedFoodData;
import com.hexagram2021.fiahi.common.item.data.IPouchedFoodDataType;
import com.hexagram2021.fiahi.common.item.data.PouchedFoodDataTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record AddPotionEffectsData(List<MobEffectInstance> effects, boolean disable, Optional<UUID> owner, String potionType) implements IPouchedFoodData {
	private static final String TAG_EFFECTS = new ResourceLocation("add_potion", "effects").toString();
	private static final String TAG_DISABLE = new ResourceLocation("add_potion", "disable").toString();
	private static final String TAG_OWNER = new ResourceLocation("add_potion", "owner").toString();
	private static final String TAG_POTION_TYPE = new ResourceLocation("add_potion", "potion_type").toString();

	public static final Codec<AddPotionEffectsData> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					MOB_EFFECT_INSTANCE_CODEC.listOf().fieldOf("effects").forGetter(AddPotionEffectsData::effects),
					Codec.BOOL.fieldOf("disable").forGetter(AddPotionEffectsData::disable),
					SerializableUUID.CODEC.optionalFieldOf("owner").forGetter(AddPotionEffectsData::owner),
					Codec.STRING.fieldOf("potion_type").forGetter(AddPotionEffectsData::potionType)
			).apply(instance, AddPotionEffectsData::new)
	);

	@Override
	public IPouchedFoodDataType type() {
		return PouchedFoodDataTypes.ADD_POTION_EFFECTS;
	}

	@Override
	public void modifyStack(ItemStack itemStack) {
		if(!this.effects.isEmpty()) {
			CompoundTag nbt = itemStack.getOrCreateTag();
			nbt.put(TAG_EFFECTS, IPouchedFoodData.getTagFromEffects(this.effects));
			if(this.disable) {
				nbt.putBoolean(TAG_DISABLE, true);
			}
			this.owner.ifPresent(uuid -> nbt.putUUID(TAG_OWNER, uuid));
			nbt.putString(TAG_POTION_TYPE, this.potionType);
		}
	}

	@Nullable
	public static AddPotionEffectsData fromStackNBT(CompoundTag nbt) {
		if(nbt.contains(TAG_EFFECTS, Tag.TAG_LIST)) {
			boolean disable = nbt.getBoolean(TAG_DISABLE);
			Optional<UUID> owner = nbt.contains(TAG_OWNER, Tag.TAG_INT_ARRAY) ? Optional.of(nbt.getUUID(TAG_OWNER)) : Optional.empty();
			String potionType = nbt.contains(TAG_POTION_TYPE, Tag.TAG_STRING) ? nbt.getString(TAG_POTION_TYPE) : "DEFAULT";
			return new AddPotionEffectsData(IPouchedFoodData.getEffectsFromTag(nbt.getList(TAG_EFFECTS, Tag.TAG_COMPOUND)), disable, owner, potionType);
		}
		return null;
	}
}
