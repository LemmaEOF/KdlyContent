package gay.lemmaeof.kdlycontent.api;

import com.mojang.serialization.Lifecycle;
import gay.lemmaeof.kdlycontent.KdlyContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public class KdlyRegistries {
	public static final RegistryKey<Registry<ContentType>> CONTENT_TYPES_KEY = RegistryKey.ofRegistry(new Identifier(KdlyContent.MODID, "content_types"));
	public static final Registry<ContentType> CONTENT_TYPES = new SimpleRegistry<>(CONTENT_TYPES_KEY, Lifecycle.stable(), null);

	public static final RegistryKey<Registry<BlockGenerator>> BLOCK_GENERATORS_KEY = RegistryKey.ofRegistry(new Identifier(KdlyContent.MODID, "block_generators"));
	public static final Registry<BlockGenerator> BLOCK_GENERATORS = new DefaultedRegistry<>("kdlycontent:standard", BLOCK_GENERATORS_KEY, Lifecycle.stable(), null);

	public static final RegistryKey<Registry<ItemGenerator>> ITEM_GENERATORS_KEY = RegistryKey.ofRegistry(new Identifier(KdlyContent.MODID, "item_generators"));
	public static final Registry<ItemGenerator> ITEM_GENERATORS = new DefaultedRegistry<>("kdlycontent:standard", ITEM_GENERATORS_KEY, Lifecycle.stable(), null);
}
