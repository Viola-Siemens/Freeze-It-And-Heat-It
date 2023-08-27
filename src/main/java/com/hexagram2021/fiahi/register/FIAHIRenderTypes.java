package com.hexagram2021.fiahi.register;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import static com.hexagram2021.fiahi.FreezeItAndHeatIt.MODID;

public class FIAHIRenderTypes extends RenderStateShard {
	private static final ResourceLocation FROZEN_FOOD_LOCATION = new ResourceLocation(MODID, "textures/item/frozen.png");

	protected static final RenderStateShard.TexturingStateShard GLINT_TEXTURING_SLOW = new RenderStateShard.TexturingStateShard(
			MODID + ":glint_texturing_slow",
			FIAHIRenderTypes::setupGlintTexturingSlow,
			RenderSystem::resetTextureMatrix
	);

	private static void setupGlintTexturingSlow() {
		long i = Util.getMillis() * 4L;
		float x = (float)(i % 110000L) / 110000.0F;
		float y = (float)(i % 30000L) / 30000.0F;
		Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-x, y, 0.0F);
		matrix4f.multiply(Vector3f.ZP.rotationDegrees(10.0F));
		matrix4f.multiply(Matrix4f.createScaleMatrix(4.0F, 4.0F, 4.0F));
		RenderSystem.setTextureMatrix(matrix4f);
	}

	private static final RenderType FROZEN_FOOD_TRANSLUCENT = RenderType.create(
			MODID + ":frozen_food_translucent",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING_SLOW)
					.setOutputState(ITEM_ENTITY_TARGET)
					.createCompositeState(false)
	);
	private static final RenderType FROZEN_FOOD = RenderType.create(
			MODID + ":frozen_food",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING_SLOW)
					.createCompositeState(false)
	);
	private static final RenderType FROZEN_FOOD_DIRECT = RenderType.create(
			MODID + ":frozen_food_direct",
			DefaultVertexFormat.POSITION_TEX,
			VertexFormat.Mode.QUADS,
			256, false, false,
			RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.RENDERTYPE_GLINT_DIRECT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(FROZEN_FOOD_LOCATION, true, false))
					.setWriteMaskState(COLOR_WRITE)
					.setCullState(NO_CULL)
					.setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY)
					.setTexturingState(GLINT_TEXTURING_SLOW)
					.createCompositeState(false)
	);

	public FIAHIRenderTypes(String name, Runnable setupState, Runnable clearState) {
		super(name, setupState, clearState);
	}

	public static RenderType frozenTranslucent(int level) {
		return FROZEN_FOOD_TRANSLUCENT;
	}
	public static RenderType frozen(int level) {
		return FROZEN_FOOD;
	}
	public static RenderType frozenDirect(int level) {
		return FROZEN_FOOD_DIRECT;
	}
	public static RenderType rottenTranslucent(int level) {
		return FROZEN_FOOD_TRANSLUCENT;
	}
	public static RenderType rotten(int level) {
		return FROZEN_FOOD;
	}
	public static RenderType rottenDirect(int level) {
		return FROZEN_FOOD_DIRECT;
	}
}
