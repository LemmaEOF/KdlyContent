package gay.lemmaeof.kdlycontent;

import dev.kdl.KdlDocument;
import dev.kdl.KdlNode;
import dev.kdl.parse.Kdl1Parser;
import dev.kdl.parse.Kdl2Parser;
import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.KdlParser;
import gay.debuggy.staticdata.api.StaticData;
import gay.debuggy.staticdata.api.StaticDataItem;
import gay.lemmaeof.kdlycontent.api.ContentType;
import gay.lemmaeof.kdlycontent.api.KdlyRegistries;
import gay.lemmaeof.kdlycontent.api.ParseException;
import gay.lemmaeof.kdlycontent.content.custom.CustomBlock;
import gay.lemmaeof.kdlycontent.content.type.ItemContentType;
import gay.lemmaeof.kdlycontent.init.KdlyContentTypes;
import gay.lemmaeof.kdlycontent.init.KdlyGenerators;
import gay.lemmaeof.kdlycontent.util.KdlHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KdlyContent implements ModInitializer {
	public static final String MODID = "kdlycontent";
	public static final Logger LOGGER = LoggerFactory.getLogger("KdlyContent");

	private static final KdlParser V2_PARSER = new Kdl2Parser();
	private static final KdlParser V1_PARSER = new Kdl1Parser();

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MODID, "generated"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.kdlycontent.generated"))
					.icon(() -> new ItemStack(Items.CRAFTING_TABLE))
					.build()
	);

	@Override
	public void onInitialize() {
		KdlyContentTypes.init();
		KdlyGenerators.init();
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			for (Item item : ItemContentType.KDLY_ITEM_GROUPS.getOrDefault(group, new ArrayList<>())) {
				entries.add(item);
			}
		});
		Registry.register(Registries.BLOCK_TYPE, Identifier.of(MODID, "custom"), CustomBlock.CODEC);

		FabricLoader.getInstance().getEntrypoints("kdlycontent:before", Runnable.class).forEach(Runnable::run);
		List<StaticDataItem> data = StaticData.getExactData(Identifier.of("", "kdlycontent.kdl"));
		for (StaticDataItem item : data) {
			String namespace = item.getModId();
			KdlDocument kdl;
			try {
				try {
					//give kdl v2 a shot
					kdl = V2_PARSER.parse(item.getAsStream());
				} catch (KdlParseException e) {
					//parse fail - could be kdl v1?
					try {
						kdl = V1_PARSER.parse(item.getAsStream());
					} catch (KdlParseException x) {
						//nope! freak the fuck out and blow up
						throw new RuntimeException("Could not parse KDL for file" + item.getResourceId(), e);
					}
				}
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
		FabricLoader.getInstance().getEntrypoints("kdlycontent:after", Runnable.class).forEach(Runnable::run);
	}

	//TODO: template overrides and such
	//TODO: oh god this method is a nightmare
	protected void parseKdl(String namespace, KdlDocument kdl) {
		//TODO: global-scope templates - don't currently work in `require` blocks
		Map<ContentType, Map<Identifier, KdlNode>> templates = new HashMap<>();
		for (KdlNode node : kdl.nodes()) {
			Identifier id = Identifier.of(namespace, "anonymous");
			String typeName = node.name();
			if (!typeName.contains(":")) typeName = "kdlycontent:" + typeName;
			Identifier typeId = Identifier.of(typeName);
			if (KdlyRegistries.CONTENT_TYPES.containsId(typeId)) {
				ContentType type = KdlyRegistries.CONTENT_TYPES.get(typeId);
				if (node.type() != null && node.type().equals("template")) {
					id = Identifier.of(KdlHelper.getArg(node, 0, "anonymous"));
					templates.computeIfAbsent(type, t -> new HashMap<>()).put(id, node);
				} else {
					if (type.needsIdentifier()) {
						String name = KdlHelper.getArg(node, 0, "anonymous");
						id = Identifier.of(namespace, name);
					}
					if (node.properties().hasProperty("template")) {
						Identifier templateId = Identifier.of(KdlHelper.getProp(node, "template", ""));
						if (templates.containsKey(type)) {
							Map<Identifier, KdlNode> typeTemplates = templates.get(type);
							if (typeTemplates.containsKey(templateId)) {
								KdlNode template = typeTemplates.get(templateId);
								type.generateFrom(id, template);
							} else {
								throw new ParseException(id, "No template named `" + templateId + "` for content type `" + node.name() + "` (converted to `" + typeId + "`)");
							}
						} else {
							throw new ParseException(id, "No templates for content type `" + node.name() + "` found (converted to `" + typeId + "`)");
						}
					} else {
						//TODO: multiple IDs for quick-instantiation
						type.generateFrom(id, node);
					}
				}
			} else {
				throw new ParseException(id, "Content type `" + node.name() + "` not found (converted to `" + typeId + "`)");
			}
		}
	}
}
