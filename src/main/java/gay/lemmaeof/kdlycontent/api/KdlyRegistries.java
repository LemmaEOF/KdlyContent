package gay.lemmaeof.kdlycontent.api;

import com.mojang.serialization.Lifecycle;
import gay.lemmaeof.kdlycontent.KdlyContent;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;

public class KdlyRegistries {
	public static final RegistryKey<Registry<ContentType>> CONTENT_TYPES_KEY = RegistryKey.ofRegistry(Identifier.of(KdlyContent.MODID, "content_types"));
	public static final Registry<ContentType> CONTENT_TYPES = new SimpleRegistry<>(CONTENT_TYPES_KEY, Lifecycle.stable(), false);

	public static final RegistryKey<Registry<BlockParser>> BLOCK_PARSERS_KEY = RegistryKey.ofRegistry(Identifier.of(KdlyContent.MODID, "block_parsers"));
	public static final Registry<BlockParser> BLOCK_PARSERS = new SimpleDefaultedRegistry<>("kdlycontent:passthrough", BLOCK_PARSERS_KEY, Lifecycle.stable(), false);

	public static final RegistryKey<Registry<ItemGenerator>> ITEM_GENERATORS_KEY = RegistryKey.ofRegistry(Identifier.of(KdlyContent.MODID, "item_generators"));
	public static final Registry<ItemGenerator> ITEM_GENERATORS = new SimpleDefaultedRegistry<>("kdlycontent:standard", ITEM_GENERATORS_KEY, Lifecycle.stable(), false);
}
