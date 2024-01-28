package gay.lemmaeof.kdlycontent;

import com.unascribed.lib39.core.api.ModPostInitializer;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.parse.KDLParser;
import gay.debuggy.staticdata.api.StaticData;
import gay.debuggy.staticdata.api.StaticDataItem;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.type.ItemContentType;
import gay.lemmaeof.kdlycontent.init.KdlyContentTypes;
import gay.lemmaeof.kdlycontent.init.KdlyGenerators;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KdlyContent implements ModInitializer, ModPostInitializer {
	public static final String MODID = "kdlycontent";
	public static final Logger LOGGER = LoggerFactory.getLogger("KdlyContent");

	private static final KDLParser parser = new KDLParser();

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(MODID, "generated"),
			FabricItemGroup.builder()
					.name(Text.translatable("itemGroup.kdlycontent.generated"))
					.icon(() -> new ItemStack(Items.CRAFTING_TABLE))
					.build()
	);

	@Override
	public void onInitialize(ModContainer mod) {
		KdlyContentTypes.init();
		KdlyGenerators.init();
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			for (Item item : ItemContentType.KDLY_ITEM_GROUPS.getOrDefault(group, new ArrayList<>())) {
				entries.addItem(item);
			}
		});
	}

	@Override
	public void onPostInitialize() {
		QuiltLoader.getEntrypoints("kdlycontent:before", Runnable.class).forEach(Runnable::run);
		List<StaticDataItem> data = StaticData.getExactData(new Identifier("", "kdlycontent.kdl"));
		for (StaticDataItem item : data) {
			String namespace = item.getModId();
			try {
				KDLDocument kdl = parser.parse(item.getAsStream());
				parseKdl(namespace, kdl);
			} catch (IOException | ParseException e) {
				throw new RuntimeException("Could not load KDL for file " + item.getResourceId(), e);
			}
		}
		StringBuilder builder = new StringBuilder("Registered ");
		List<String> messages = new ArrayList<>();
		KdlyRegistries.CONTENT_TYPES.forEach(type -> type.getApplyMessage().ifPresent(messages::add));
		for (int i = 0; i < messages.size() - 1; i++) {
			builder.append(messages.get(i));
			if (messages.size() > 2) builder.append(", ");
		}
		if (messages.size() > 1) {
			builder.append("and ").append(messages.get(messages.size() - 1));
		}
		LOGGER.info(builder.toString());
		QuiltLoader.getEntrypoints("kdlycontent:after", Runnable.class).forEach(Runnable::run);
	}

	//TODO: template overrides and such
	//TODO: oh god this method is a nightmare
	protected void parseKdl(String namespace, KDLDocument kdl) {
		Map<ContentType, Map<Identifier, KDLNode>> templates = new HashMap<>();
		for (KDLNode node : kdl.getNodes()) {
			Identifier id = new Identifier(namespace, "anonymous");
			String typeName = toSnakeCase(node.getIdentifier());
			if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
			Identifier typeId = new Identifier(typeName);
			if (KdlyRegistries.CONTENT_TYPES.containsId(typeId)) {
				ContentType type = KdlyRegistries.CONTENT_TYPES.get(typeId);
				if (node.getType().isPresent() && node.getType().get().equals("template")) {
					id = new Identifier(KdlHelper.getArg(node, 0, "anonymous"));
					templates.computeIfAbsent(type, t -> new HashMap<>()).put(id, node);
				} else {
					if (type.needsIdentifier()) {
						String name = KdlHelper.getArg(node, 0, "anonymous");
						id = new Identifier(namespace, name);
					}
					if (node.getProps().containsKey("template")) {
						Identifier templateId = new Identifier(KdlHelper.getProp(node, "template", ""));
						if (templates.containsKey(type)) {
							Map<Identifier, KDLNode> typeTemplates = templates.get(type);
							if (typeTemplates.containsKey(templateId)) {
								KDLNode template = typeTemplates.get(templateId);
								type.generateFrom(id, template);
							} else {
								throw new ParseException(id, "No template named `" + templateId + "` for content type `" + node.getIdentifier() + "` (converted to `" + typeId + "`)");
							}
						} else {
							throw new ParseException(id, "No templates for content type `" + node.getIdentifier() + "` found (converted to `" + typeId + "`)");
						}
					} else {
						//TODO: multiple IDs for quick-instantiation
						type.generateFrom(id, node);
					}
				}
			} else {
				throw new ParseException(id, "Content type `" + node.getIdentifier() + "` not found (converted to `" + typeId + "`)");
			}
		}
	}

	protected String toSnakeCase(String original) {
		//this may be considered sliiiightly evil, my condolences
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		original = original.replaceAll(regex, replacement).toLowerCase();
		return original;
	}
}
