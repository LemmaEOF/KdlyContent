package gay.lemmaeof.kdlycontent.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.block.AbstractBlock;

public class BlockSettingsCodec implements Codec<AbstractBlock.Settings> {
	public static final Codec<AbstractBlock.Settings> CODEC = new BlockSettingsCodec();
	@Override
	public <T> DataResult<Pair<AbstractBlock.Settings, T>> decode(DynamicOps<T> ops, T input) {
		if (ops instanceof PassedSettingsOps<T> settingsOps) {
			return DataResult.success(Pair.of(settingsOps.getSettings(), input));
		}
		return AbstractBlock.Settings.CODEC.decode(ops, input);
	}

	@Override
	public <T> DataResult<T> encode(AbstractBlock.Settings input, DynamicOps<T> ops, T prefix) {
		return DataResult.success(prefix);
	}
}
