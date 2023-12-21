package com.hexagram2021.fiahi.client.util;

import net.minecraft.resources.ResourceLocation;

public class DummyResourceLocation extends ResourceLocation {
	private final ResourceLocation origin;
	private final int color;
	private final int level;

	public DummyResourceLocation(String namespace, String id, ResourceLocation origin, int color, int level) {
		super(namespace, id);
		this.origin = origin;
		this.color = color;
		this.level = level;
	}

	public ResourceLocation getOrigin() {
		return this.origin;
	}

	public int getColor() {
		return this.color;
	}

	public int getLevel() {
		return this.level;
	}
}
