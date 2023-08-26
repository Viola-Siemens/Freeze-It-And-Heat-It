package com.hexagram2021.fiahi.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FIAHIClientContent {
	@Nullable
	private static ShaderInstance frozenFoodTranslucent;
	@Nullable
	private static ShaderInstance frozenFood;
	@Nullable
	private static ShaderInstance frozenFoodDirect;

	@SubscribeEvent
	public static void registerShaders(RegisterShadersEvent event) throws IOException {
		event.registerShader(
				new ShaderInstance(event.getResourceManager(), new ResourceLocation(MODID, "frozen_food_translucent"), DefaultVertexFormat.BLOCK),
				shader -> frozenFoodTranslucent = shader
		);
		event.registerShader(
				new ShaderInstance(event.getResourceManager(), new ResourceLocation(MODID, "frozen_food"), DefaultVertexFormat.BLOCK),
				shader -> frozenFood = shader
		);
		event.registerShader(
				new ShaderInstance(event.getResourceManager(), new ResourceLocation(MODID, "frozen_food_direct"), DefaultVertexFormat.BLOCK),
				shader -> frozenFoodDirect = shader
		);
	}

	@Nullable
	public static ShaderInstance getFrozenFoodTranslucent() {
		return frozenFoodTranslucent;
	}

	@Nullable
	public static ShaderInstance getFrozenFood() {
		return frozenFood;
	}

	@Nullable
	public static ShaderInstance getFrozenFoodDirect() {
		return frozenFoodDirect;
	}
}
