package com.hexagram2021.fiahi.register;

import com.hexagram2021.fiahi.client.FIAHIClientContent;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIRenderTypes extends RenderStateShard {
	private static final ResourceLocation FROZEN_FOOD_LOCATION = new ResourceLocation(MODID, "textures/item/frozen.png");

	protected static final ShaderStateShard FROZEN_FOOD_TRANSLUCENT_SHADER = new ShaderStateShard(FIAHIClientContent::getFrozenFoodTranslucent);
	protected static final ShaderStateShard FROZEN_FOOD_SHADER = new ShaderStateShard(FIAHIClientContent::getFrozenFood);
	protected static final ShaderStateShard FROZEN_FOOD_DIRECT_SHADER = new ShaderStateShard(FIAHIClientContent::getFrozenFoodDirect);

	private static final RenderType FROZEN_FOOD_TRANSLUCENT = RenderType.create(
			"glint_translucent",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(FROZEN_FOOD_TRANSLUCENT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setOutputState(ITEM_ENTITY_TARGET)
					.createCompositeState(false)
	);
	private static final RenderType GLINT = RenderType.create(
			"glint",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(FROZEN_FOOD_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.createCompositeState(false)
	);
	private static final RenderType GLINT_DIRECT = RenderType.create(
			"glint_direct",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(FROZEN_FOOD_DIRECT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.createCompositeState(false)
	);

	public FIAHIRenderTypes(String name, Runnable setupState, Runnable clearState) {
		super(name, setupState, clearState);
	}
}
