package com.hexagram2021.fiahi.common;

import com.hexagram2021.fiahi.register.*;
import net.neoforged.bus.api.IEventBus;

public final class FIAHIContent {
	private FIAHIContent() {
	}

	public static void modConstruct(IEventBus bus) {
		FIAHIAttachmentTypes.init(bus);
		FIAHICreativeModeTabs.init(bus);
		FIAHIItems.init(bus);
		FIAHIMenuTypes.init(bus);
		FIAHIMobEffects.init(bus);
		FIAHIParticleTypes.init(bus);
	}
}
