package gay.lemmaeof.kdlycontent.util;

import com.mojang.serialization.DynamicOps;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.dynamic.ForwardingDynamicOps;

public class PassedSettingsOps<T> extends ForwardingDynamicOps<T> {
	private AbstractBlock.Settings settings;

	public PassedSettingsOps(AbstractBlock.Settings settings, DynamicOps<T> delegate) {
		super(delegate);
		this.settings = settings;
	}

	public AbstractBlock.Settings getSettings() {
		return settings;
	}
}
