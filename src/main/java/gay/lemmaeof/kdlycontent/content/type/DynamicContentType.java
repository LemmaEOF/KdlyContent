package gay.lemmaeof.kdlycontent.content.type;

import com.google.gson.JsonObject;
import dev.kdl.KdlNode;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.util.Identifier;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicContentType<T> implements ContentType {
	public static final Map<RegistryKey<Registry<?>>, Map<Identifier, JsonObject>> KDLY_DYNAMIC_ENTRIES = new HashMap<>();
	public final Map<Identifier, JsonObject> entries = new HashMap<>();
	private final RegistryLoader.Entry<T> dynamicRegistry;

	public DynamicContentType(RegistryLoader.Entry<T> dynamicRegistry) {
		this.dynamicRegistry = dynamicRegistry;
	}

	@Override
	public void generateFrom(Identifier id, KdlNode parent) throws ParseException {
		RegistryKey<? extends Registry<T>> regKey = dynamicRegistry.key();
		JsonObject data = KdlHelper.parseJsonObject(parent.children());
		entries.put(id, data);
		KDLY_DYNAMIC_ENTRIES.computeIfAbsent((RegistryKey<Registry<?>>) (RegistryKey) regKey, key -> new HashMap<>()).put(id, data);
	}

	@Override
	public Optional<String> getApplyMessage() {
		return formatRegistryText(dynamicRegistry, entries.size());
	}

	private static Optional<String> formatRegistryText(RegistryLoader.Entry<?> registry, int count) {
		if (count == 0) return Optional.empty();
		Identifier id = registry.key().getValue();
		String path = id.getPath().replace('_', ' ').replace('/', ' ');
		if (id.getNamespace().equals("minecraft")) {
			return Optional.of(MessageFormat.format("{0} {1}{2}", count, path, count == 1? "" : "s"));
		} else {
			return Optional.of(MessageFormat.format("{0} {1} {2}{3}", count, id.getNamespace(), path, count == 1? "" : "s"));
		}
	}
}
